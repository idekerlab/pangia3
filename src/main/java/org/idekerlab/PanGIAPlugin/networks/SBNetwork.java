package org.idekerlab.PanGIAPlugin.networks;

public abstract class SBNetwork extends SNetwork
{
	public abstract void add(SEdge e);

	public SBNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}

	public void addAll(SNetwork net)
	{
		for (SEdge e : net.edgeIterator())
			this.add(e);
	}
}
