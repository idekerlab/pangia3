package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.view.model.CyNetworkView;

/**
 * Created by David Welker on 4/16/14
 * Copyright © 2014. All rights reserved.
 */
public class PanGIANetworkViewContextMenuFactory extends PanGIAContextMenuFactory implements CyNetworkViewContextMenuFactory
{
	@Override
	public CyMenuItem createMenuItem(CyNetworkView netView)
	{
		return createMenuItemHelper(netView, null);
	}

}
