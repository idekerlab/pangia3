package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.idekerlab.PanGIAPlugin.ui.SearchPropertyPanel;

import javax.swing.*;
import java.awt.*;

public class PanGIACytoPanelComponent implements CytoPanelComponent
{

	SearchPropertyPanel panel;

	public PanGIACytoPanelComponent(SearchPropertyPanel panel)
	{
		this.panel = panel;
		this.panel.setPreferredSize(new Dimension(450, 300));
	}

	@Override
	public Component getComponent()
	{
		return panel;
	}

	@Override
	public CytoPanelName getCytoPanelName()
	{
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle()
	{
		return "PanGIA";
	}

	@Override
	public Icon getIcon()
	{
		return null;
	}
}
