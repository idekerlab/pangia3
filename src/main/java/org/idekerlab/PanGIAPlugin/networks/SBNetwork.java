package org.idekerlab.PanGIAPlugin.networks;

public abstract class SBNetwork extends SNetwork
{
	public abstract void add(SEdge e);

	protected SBNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}
}
