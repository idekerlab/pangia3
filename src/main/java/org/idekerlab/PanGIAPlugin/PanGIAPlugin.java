package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.idekerlab.PanGIAPlugin.ui.SearchPropertyPanel;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;


/**
 * PanGIA Plugin main class.
 * <p/>
 * <p/>
 *
 * @author kono, ruschein, ghannum
 */
public class PanGIAPlugin extends AbstractCyAction implements NetworkViewAddedListener, NetworkDestroyedListener
{

	private static final String PLUGIN_NAME = "PanGIA";
	public static final String VERSION = "1.1";
	public static final Map<String, PanGIAOutput> output = new HashMap<String, PanGIAOutput>();
	private final CytoPanel cytoPanelWest;

	private final SearchPropertyPanel searchPanel;

	public PanGIAPlugin(SearchPropertyPanel searchPanel)
	{
		super(PLUGIN_NAME);
		this.setPreferredMenu("Apps");
		this.searchPanel = searchPanel;
		cytoPanelWest = ServicesUtil.cySwingApplicationServiceRef.getCytoPanel(CytoPanelName.WEST);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// If the state of the cytoPanelEast is HIDE, show it
		if (cytoPanelWest.getState() == CytoPanelState.HIDE)
		{
			cytoPanelWest.setState(CytoPanelState.DOCK);
		}

		// Select the jActiveModules panel
		int index = cytoPanelWest.indexOfComponent(searchPanel);
		if (index == -1)
		{
			return;
		}

		cytoPanelWest.setSelectedIndex(index);
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent e)
	{

	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e)
	{

	}


}

