package org.idekerlab.PanGIAPlugin.ui;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A convenience JPanel for selecting networks.
 *
 * @CyAPI.Final.Class
 */
public final class NetworkSelectorPanel extends JPanel implements NetworkAddedListener, NetworkDestroyedListener
{
	private static final long serialVersionUID = 8694272457769377810L;

	private final JComboBox networkComboBox;
	private final CyNetworkManager cyNetworkManager;
	private final CyApplicationManager cyApplicationManager;

	public void setSearchRunning(boolean searchRunning)
	{
		this.searchRunning = searchRunning;
	}

	private boolean searchRunning = false;

	/**
	 * Constructor.
	 *
	 * @param cyApplicationManager The application manager used for tracking the current network.
	 * @param cyNetworkManager     The network manager used for accessing all available networks.
	 */
	public NetworkSelectorPanel(CyApplicationManager cyApplicationManager, CyNetworkManager cyNetworkManager)
	{
		super();
		this.setLayout(new BorderLayout());
		networkComboBox = new JComboBox();

		this.cyNetworkManager = cyNetworkManager;
		this.cyApplicationManager = cyApplicationManager;

		//This should help to limit the length of combobox if the network name is too long
		networkComboBox.setPreferredSize(new java.awt.Dimension(networkComboBox.getPreferredSize().width,
				networkComboBox.getPreferredSize().height));

		add(networkComboBox, BorderLayout.CENTER);
		updateNetworkList();
	}

	/**
	 * If selected, return selected network.
	 * Otherwise, return null.
	 *
	 * @return The network that was selected.
	 */
	public CyNetwork getSelectedNetwork()
	{
		for (CyNetwork net : this.cyNetworkManager.getNetworkSet())
		{
			String networkTitle = net.getRow(net).get("name", String.class);
			if (networkTitle == null || networkTitle.equalsIgnoreCase(""))
			{
				continue;
			}
			if (networkTitle.equals(networkComboBox.getSelectedItem()))
				return net;
		}
		return null;
	}

	private void updateNetworkList()
	{
		Object selectedItem = networkComboBox.getSelectedItem();
		String currentNetworkName = null;
		if( selectedItem != null )
			currentNetworkName = selectedItem.toString();

		final Set<CyNetwork> networks = this.cyNetworkManager.getNetworkSet();
		final SortedSet<String> networkNames = new TreeSet<String>();

		for (CyNetwork net : networks)
		{
			if (net == null)
			{
				continue;
			}
			networkNames.add(net.getRow(net).get("name", String.class));

//			System.out.println("NetworkSelectorPanel.updateNetworkList(): name ="+ net.getRow(net).get("name", String.class) );

		}


		// Clear the comboBox
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		networkComboBox.setModel(model);

		for (String name : networkNames)
			networkComboBox.addItem(name);

		if( currentNetworkName != null )
		{
			if( model.getIndexOf(currentNetworkName) != -1 )
				networkComboBox.setSelectedItem(currentNetworkName);
		}


//		CyNetwork currNetwork = this.cyApplicationManager.getCurrentNetwork();
//		if (currNetwork != null)
//		{
//			String networkTitle = currNetwork.getRow(currNetwork).get("name", String.class);
//			networkComboBox.setSelectedItem(networkTitle);
//		}

//		System.out.println("\nLeaving NetworkSelectorPanel.updateNetworkList()...");

	}

	/**
	 * Updates the list based on network added events.
	 *
	 * @param e The network added event.
	 */
	public void handleEvent(NetworkAddedEvent e)
	{

//		System.out.println("NetworkSelectorPanel: got NetworkAddedEvent");
		if (searchRunning)
			return;
		updateNetworkList();
	}

	/**
	 * Updates the list based on network destroyed events.
	 *
	 * @param e The network destroyed event.
	 */
	public void handleEvent(NetworkDestroyedEvent e)
	{
//		System.out.println("NetworkSelectorPanel: got NetworkDestroyedEvent");
		if (searchRunning)
			return;
		updateNetworkList();
	}

	/**
	 * Installs a new item listener for the embedded combo box.
	 *
	 * @param newListener The new item listener to be added.
	 */
	public void addItemListener(final ItemListener newListener)
	{
		networkComboBox.addItemListener(newListener);
	}

	/**
	 * Returns the network combobox.
	 *
	 * @return The network combobox.
	 */
	public JComboBox getJCombobox()
	{
		return this.networkComboBox;
	}
}
