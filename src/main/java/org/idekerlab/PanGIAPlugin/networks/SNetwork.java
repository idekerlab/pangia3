package org.idekerlab.PanGIAPlugin.networks;

import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.Set;

public abstract class SNetwork extends AbstractNetwork
{

	public abstract IIterator<? extends SEdge> edgeIterator();

	public abstract int numEdges();

	public abstract int numNodes();

	public abstract Set<String> getNodes();

	public abstract boolean contains(String n1, String n2);

	public abstract boolean contains(SEdge e);

	public SNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}

}
