package org.idekerlab.PanGIAPlugin.networks;

import org.idekerlab.PanGIAPlugin.utilities.IIterator;

public abstract class SNetwork extends AbstractNetwork
{

	public abstract IIterator<? extends SEdge> edgeIterator();

	protected SNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}

}
