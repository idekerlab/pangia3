package org.idekerlab.PanGIAPlugin.networks;

import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNetwork;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.Set;

public abstract class SFNetwork extends SNetwork
{
	protected SFNetwork(boolean selfOk, boolean directed)
	{
		super(selfOk, directed);
	}

	public abstract float edgeValue(String n1, String n2);

	public abstract int numEdges();

	public abstract int numNodes();

	public abstract IIterator<? extends SFEdge> edgeIterator();

	public abstract IIterator<String> nodeIterator();

	public abstract Set<String> getNodes();

	public abstract TypedLinkNetwork<String, Float> asTypedLinkNetwork();
}
