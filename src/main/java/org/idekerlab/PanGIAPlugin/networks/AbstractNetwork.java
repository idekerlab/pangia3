package org.idekerlab.PanGIAPlugin.networks;

public abstract class AbstractNetwork
{
	protected final boolean directed;
	protected final boolean selfOk;

	public abstract int numEdges();

	public abstract int numNodes();

	public AbstractNetwork(boolean selfOk, boolean directed)
	{
		this.selfOk = selfOk;
		this.directed = directed;
	}

	public boolean isDirected()
	{
		return directed;
	}

}
