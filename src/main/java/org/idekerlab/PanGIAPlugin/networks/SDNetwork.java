package org.idekerlab.PanGIAPlugin.networks;

import org.idekerlab.PanGIAPlugin.utilities.IIterator;

public abstract class SDNetwork extends SNetwork
{
	public abstract IIterator<? extends SDEdge> edgeIterator();
	public SDNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}
}
