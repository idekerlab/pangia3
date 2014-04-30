package org.idekerlab.PanGIAPlugin.networks.hashNetworks;

import org.idekerlab.PanGIAPlugin.networks.SBNetwork;
import org.idekerlab.PanGIAPlugin.networks.SEdge;
import org.idekerlab.PanGIAPlugin.networks.SNetwork;
import org.idekerlab.PanGIAPlugin.networks.UndirectedSEdge;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.*;

public class BooleanHashNetwork extends SBNetwork implements Iterable<SEdge>
{
	private Map<String, Set<SEdge>> nodeMap;
	private Set<SEdge> edgeSet;

	public BooleanHashNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
		this.edgeSet = new HashSet<SEdge>();
		this.nodeMap = new HashMap<String, Set<SEdge>>();
	}

	public BooleanHashNetwork(boolean selfOk, boolean directed, int startsize)
	{
		super(selfOk, directed);
		this.edgeSet = new HashSet<SEdge>(startsize);
		this.nodeMap = new HashMap<String, Set<SEdge>>(100);
	}

	private void updateNodeMapAdd(SEdge i)
	{
		Set<SEdge> iset = nodeMap.get(i.getI1());
		if (iset == null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI1(), newIset);
		}
		else iset.add(i);

		iset = nodeMap.get(i.getI2());
		if (iset == null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI2(), newIset);
		}
		else iset.add(i);
	}

	private void updateNodeMapRemove(SEdge i)
	{
		Set<SEdge> iset = nodeMap.get(i.getI1());
		if (iset != null) iset.remove(i);

		iset = nodeMap.get(i.getI2());
		if (iset != null) iset.remove(i);
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

	public void addAll(SNetwork net)
	{
		for (SEdge i : net.edgeIterator())
			this.add(i);
	}

	public int numEdges()
	{
		return this.edgeSet.size();
	}

	public int numNodes()
	{
		return this.nodeMap.size();
	}

	public boolean contains(String n1, String n2)
	{
		return edgeSet.contains(new UndirectedSEdge(n1, n2));
	}

	public boolean contains(SEdge e)
	{
		return edgeSet.contains(e);
	}

	public IIterator<SEdge> edgeIterator()
	{
		return new IIterator<SEdge>(this.iterator());
	}

}
