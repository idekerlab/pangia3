package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

/**
 * Created by David Welker on 4/1/14
 * Copyright © 2014. All rights reserved.
 */
public class PanGIANodeViewContextMenuFactory extends PanGIAContextMenuFactory implements CyNodeViewContextMenuFactory
{
	//@Override
	public CyMenuItem createMenuItem(CyNetworkView netView, View<CyNode> nodeView)
	{
		return createMenuItemHelper(netView, nodeView);
	}
}
