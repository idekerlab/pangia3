package org.idekerlab.PanGIAPlugin.networks;

public abstract class AbstractNetwork
{
	protected final boolean directed;
	protected final boolean selfOk;

	protected AbstractNetwork(boolean selfOk, boolean directed)
	{
		this.selfOk = selfOk;
		this.directed = directed;
	}

}
