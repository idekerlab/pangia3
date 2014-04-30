package org.idekerlab.PanGIAPlugin.networks.hashNetworks;

import org.idekerlab.PanGIAPlugin.data.StringVector;
import org.idekerlab.PanGIAPlugin.networks.*;
import org.idekerlab.PanGIAPlugin.util.RandomFactory;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;
import org.idekerlab.PanGIAPlugin.utilities.files.FileIterator;
import org.idekerlab.PanGIAPlugin.utilities.files.FileUtil;

import java.util.*;

public class BooleanHashNetwork extends SBNetwork implements Iterable<SEdge>
{
	private Map<String,Set<SEdge>> nodeMap;
	private Set<SEdge> edgeSet;
	
	public BooleanHashNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk,directed);
		this.edgeSet = new HashSet<SEdge>();
		this.nodeMap = new HashMap<String,Set<SEdge>>();
	}
	
	public BooleanHashNetwork(BooleanHashNetwork net)
	{
		super(net.selfOk,net.directed);
		this.edgeSet = new HashSet<SEdge>(net.edgeSet);
		this.nodeMap = new HashMap<String,Set<SEdge>>(net.nodeMap.size());
		
		for (String n : net.nodeMap.keySet())
			this.nodeMap.put(n, new HashSet<SEdge>(net.nodeMap.get(n)));
	}

	public BooleanHashNetwork(boolean selfOk, boolean directed, int startsize)
	{
		super(selfOk,directed);
		this.edgeSet = new HashSet<SEdge>(startsize);
		this.nodeMap = new HashMap<String,Set<SEdge>>(100);
	}

	private void load(String file, int n1col, int n2col)
	{
		int numEdges = FileUtil.countLines(file);
		this.edgeSet = new HashSet<SEdge>(numEdges);
		this.nodeMap = new HashMap<String,Set<SEdge>>();
		
		for (String line : new FileIterator(file))
		{
			String[] cols = line.split("\t");
			this.add(new UndirectedSEdge(cols[n1col],cols[n2col]));
		}
	}
	
	private void updateNodeMapAdd(SEdge i)
	{
		Set<SEdge> iset = nodeMap.get(i.getI1());
		if (iset==null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI1(), newIset);
		}else iset.add(i);
		
		iset = nodeMap.get(i.getI2());
		if (iset==null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI2(), newIset);
		}else iset.add(i);
	}
	
	private void updateNodeMapRemove(SEdge i)
	{
		Set<SEdge> iset = nodeMap.get(i.getI1());
		if (iset!=null) iset.remove(i);
		
		iset = nodeMap.get(i.getI2());
		if (iset!=null) iset.remove(i);
	}
	
	public boolean isSelfOk()
	{
		return selfOk;
	}
	
	public boolean isDirected()
	{
		return directed;
	}
	
	public Iterator<SEdge> iterator()
	{
		return edgeSet.iterator();
	}
	
	public Set<String> getNodes()
	{
		return new HashSet<String>(nodeMap.keySet());
	}
	
	public void add(SEdge i)
	{
		if (!this.selfOk && i.isSelf()) return;
		this.edgeSet.add(i);
		this.updateNodeMapAdd(i);
	}
	
	public void remove(SEdge i)
	{
		this.edgeSet.remove(i);
		this.updateNodeMapRemove(i);
	}
	
	public void addAll(SNetwork net)
	{
		for (SEdge i : net.edgeIterator())
			this.add(i);
	}
	
	public void removeAll(SNetwork net)
	{
		for (SEdge i : net.edgeIterator())
			this.remove(i);
	}
	
	public void add(String n1, String n2)
	{
		if (this.directed) this.add(new DirectedSEdge(n1,n2)); 
		else this.add(new UndirectedSEdge(n1,n2));
	}
	
	public int numEdges()
	{
		return this.edgeSet.size();
	}
	
	public int numNodes()
	{
		return this.nodeMap.size();
	}
	
	public BooleanHashNetwork subNetworkExclusive(Set<String> nodes)
	{
		
		BooleanHashNetwork subnet = new BooleanHashNetwork(this.selfOk, this.directed, this.numEdges());
		
		for (SEdge i : this)
			if (nodes.contains(i.getI1()) && nodes.contains(i.getI2())) subnet.add(i);
	
		return subnet;
	}
	
	public boolean contains(String n1, String n2)
	{
		return edgeSet.contains(new UndirectedSEdge(n1,n2));
	}
	
	public boolean contains(SEdge e)
	{
		return edgeSet.contains(e);
	}
	
	public BooleanHashNetwork shuffleNodes()
	{
		StringVector nodes = new StringVector(this.getNodes());
		StringVector rnodes = nodes.sample(nodes.size(), false);
		Map<String,String> rnodeMap = new HashMap<String,String>(rnodes.size(),1); 
		
		for (int i=0;i<rnodes.size();i++)
			rnodeMap.put(nodes.get(i), rnodes.get(i));
		
		BooleanHashNetwork out = new BooleanHashNetwork(this.selfOk,this.directed,this.numEdges());
		for (SEdge i : this)
		{
			if (this.directed) out.add(new DirectedSEdge(rnodeMap.get(i.getI1()),rnodeMap.get(i.getI2())));
			else out.add(new UndirectedSEdge(rnodeMap.get(i.getI1()),rnodeMap.get(i.getI2())));
		}
		
		return out;
	}
	
	public IIterator<SEdge> edgeIterator()
	{
		return new IIterator<SEdge>(this.iterator());
	}
	
	public IIterator<String> nodeIterator()
	{
		return new IIterator<String>(this.nodeMap.keySet().iterator());
	}
	
	public Set<SEdge> getEdges()
	{
		return new HashSet<SEdge>(this.edgeSet);
	}

}
