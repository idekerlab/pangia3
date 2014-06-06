package org.idekerlab.PanGIAPlugin.networks.hashNetworks;

import org.idekerlab.PanGIAPlugin.networks.*;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkEdge;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNetwork;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.*;

public class FloatHashNetwork extends SFNetwork implements Iterable<SFEdge>
{
	private final Map<String, Set<SEdge>> nodeMap;
	private final Map<SEdge, SFEdge> edgeMap;

	public FloatHashNetwork(boolean selfOk, boolean directed, int startsize)
	{
		super(selfOk, directed);
		this.edgeMap = new HashMap<SEdge, SFEdge>(startsize);
		this.nodeMap = new HashMap<String, Set<SEdge>>(100);
	}


	public FloatHashNetwork(TypedLinkNetwork<String, Float> net)
	{
		super(net.isSelfOK(), net.isDirected());

		int numEdges = net.numEdges();
		this.edgeMap = new HashMap<SEdge, SFEdge>(numEdges);
		this.nodeMap = new HashMap<String, Set<SEdge>>();

		for (TypedLinkEdge<String, Float> e : net.edgeIterator())
			this.add(e.source().value(), e.target().value(), e.value());
	}

	private void updateNodeMap(SEdge i)
	{
		Set<SEdge> iset = nodeMap.get(i.getI1());
		if (iset == null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI1(), newIset);
		}
		else
			iset.add(i);

		iset = nodeMap.get(i.getI2());
		if (iset == null)
		{
			Set<SEdge> newIset = new HashSet<SEdge>();
			newIset.add(i);
			nodeMap.put(i.getI2(), newIset);
		}
		else
			iset.add(i);
	}

	public Iterator<SFEdge> iterator()
	{
		return edgeMap.values().iterator();
	}

	public Set<String> getNodes()
	{
		return new HashSet<String>(nodeMap.keySet());
	}

	protected void add(SFEdge i)
	{
		this.edgeMap.put(i, i);
		this.updateNodeMap(i);
	}

	public void add(String n1, String n2, float value)
	{
		if (this.directed)
			this.add(new DirectedSFEdge(n1, n2, value));
		else
			this.add(new UndirectedSFEdge(n1, n2, value));
	}

	public int numEdges()
	{
		return this.edgeMap.size();
	}

	public int numNodes()
	{
		return this.nodeMap.size();
	}

	public float edgeValue(String n1, String n2)
	{
		return edgeValue(new UndirectedSEdge(n1, n2));
	}

	protected float edgeValue(SEdge i)
	{
		SFEdge f = edgeMap.get(i);
		if (f == null)
			return Float.NaN;
		else
			return f.value();
	}

	public TypedLinkNetwork<String, Float> asTypedLinkNetwork()
	{
		TypedLinkNetwork<String, Float> out = new TypedLinkNetwork<String, Float>(
				this.selfOk, this.directed);

		for (String node : this.nodeMap.keySet())
			out.addNode(node);

		for (SFEdge i : this)
			out.addEdgeWNodeUpdate(i.getI1(), i.getI2(), this.edgeValue(i));

		return out;
	}

	public IIterator<String> nodeIterator()
	{
		return new IIterator<String>(this.nodeMap.keySet().iterator());
	}

	public IIterator<SFEdge> edgeIterator()
	{
		return new IIterator<SFEdge>(this.iterator());
	}


}
