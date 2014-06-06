package org.idekerlab.PanGIAPlugin;

import org.cytoscape.model.CyNetwork;

public class PanGIAOutput
{
	private final CyNetwork origPhysNetwork;
	private final CyNetwork origGenNetwork;
	private final String nodeAttrName;
	private final String physEdgeAttrName;
	private final String genEdgeAttrName;

	public PanGIAOutput(CyNetwork origPhysNetwork, CyNetwork origGenNetwork, String nodeAttrName, String physEdgeAttrName, String genEdgeAttrName)
	{
		this.origPhysNetwork = origPhysNetwork;
		this.origGenNetwork = origGenNetwork;
		this.nodeAttrName = nodeAttrName;
		this.physEdgeAttrName = physEdgeAttrName;
		this.genEdgeAttrName = genEdgeAttrName;
	}

	public CyNetwork getOrigPhysNetwork()
	{
		return origPhysNetwork;
	}

	public CyNetwork getOrigGenNetwork()
	{
		return origGenNetwork;
	}

	public String getNodeAttrName()
	{
		return nodeAttrName;
	}

	public String getPhysEdgeAttrName()
	{
		return physEdgeAttrName;
	}

	public String getGenEdgeAttrName()
	{
		return genEdgeAttrName;
	}
}
