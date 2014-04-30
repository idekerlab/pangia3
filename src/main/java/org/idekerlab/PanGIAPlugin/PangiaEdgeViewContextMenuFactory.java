package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * Created by David Welker on 4/17/14
 * Copyright © 2014. All rights reserved.
 */
public class PanGIAEdgeViewContextMenuFactory extends PanGIAContextMenuFactory implements CyEdgeViewContextMenuFactory
{
	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView, View<CyEdge> edgeView)
	{
		return createMenuItemHelper(netView, null);
	}
}
