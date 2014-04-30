package org.idekerlab.PanGIAPlugin;

import org.cytoscape.model.*;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;

import java.util.*;


public class DetailedNetworkCreator
{
	@SuppressWarnings("unchecked")
	public static void createDetailedView(CyNetworkView view, View<CyNode> nodeView)
    {
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(view.getModel(), CyNetwork.SELECTED, true);
		CyNode clickedNode = nodeView.getModel();
		if( clickedNode != null )
		{
			if( !selectedNodes.contains(clickedNode) )
				selectedNodes.add(clickedNode);
		}

		// If only one node is selected, go to the next network for that node.
		if (selectedNodes.size()==1)
		{
			goToNestedNetwork(selectedNodes.get(0));
			return;
		}

		// If we have reached here, there are at least two nodes selected.
		// Get the model from the view and use it to retrieve the network ID.
		CyNetwork model = view.getModel();
		String netID = model.getRow(model).get("name", String.class);

		// Use the network ID to retrive the appropriate PanGIAOutput instance.
		PanGIAOutput output = PanGIAPlugin.output.get(netID);

		//Use the PanGIAOutput instance to get the original physical and genetic networks.
		CyNetwork origPhysNetwork = output.getOrigPhysNetwork();
		CyNetwork origGenNetwork = output.getOrigGenNetwork();
		//And also get the attribute name (e.g. "GScore", etc.) that is used to
		//specify confidence values for genes.
		String genEdgeAttrName = output.getGenEdgeAttrName();

		
		String title = "Detailed View";
		
		if (selectedNodes.size()<=3 && !selectedNodes.isEmpty() )
		{
			title = model.getRow(selectedNodes.get(0)).get("name", String.class);
			for( int i = 1; i < selectedNodes.size(); i++ )
				title += " | " + model.getRow( selectedNodes.get(i) ).get("name", String.class);
		}
		
		String networkName = findNextAvailableNetworkName(title);
		
   	 	CyNetwork detailedNetwork = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
  	 	CyTable networkTable = detailedNetwork.getDefaultNetworkTable();
		CyRow networkRow = detailedNetwork.getRow(detailedNetwork);
		if( networkTable.getColumn("name") == null )
			networkTable.createColumn("name", String.class, false);
		networkRow.set("name", networkName);
		if( networkTable.getColumn(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME) == null )
			networkTable.createColumn(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, String.class, false);
		networkRow.set(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, NetworkType.DETAILED.name() );
		ServicesUtil.cyNetworkManagerServiceRef.addNetwork(detailedNetwork);

		//We need a new node in the detailed network for every node in the existing networks.
		//We need a map data structure that stores that maps nodes from existing networks
		//to new nodes in the detailed network. Here is that map.
		HashMap<String, CyNode> nestedNodeNameMap = new HashMap<String, CyNode>();

		//We have created a new network (called "detailedNetwork" and we have given it a name and set its type to DETAILED.
		//Now it is time to populate the network with nodes and edges.
		List<String> nodeNames = new ArrayList<String>();
		for( CyNode overviewNode : selectedNodes )
		{
			CyNetwork nestedNetwork = overviewNode.getNetworkPointer();
			//Should not happen, but as a sanity check, ensure that the nestedNetwork is not null.
			if( nestedNetwork == null )
				continue;

			//Go ahead and create a bunch of new nodes in the detailed network and
			//create the mapping so that corresponding edges can be correctly created.
			for( CyNode node : nestedNetwork.getNodeList() )
			{
				CyNode detailedNode = detailedNetwork.addNode();
				String nodeName = nestedNetwork.getRow(node).get(CyNetwork.NAME, String.class);
				detailedNetwork.getRow(detailedNode).set(CyNetwork.NAME, nodeName);
				nestedNodeNameMap.put(nodeName, detailedNode);

				nodeNames.add(nodeName);
			}
		}

		CyTable edgeTable = detailedNetwork.getDefaultEdgeTable();
		if (edgeTable.getColumn("PanGIA.Interaction Type") == null)
			edgeTable.createColumn("PanGIA.Interaction Type", String.class, false);

		HashMap<String, CyNode> physicalNodeNameMap = new HashMap<String, CyNode>();
		for (CyNode node : origPhysNetwork.getNodeList())
		{
			String name = origPhysNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			physicalNodeNameMap.put(name, node);
		}

		List<CyNode> nodes = new ArrayList<CyNode>();
		for (String name : nodeNames)
		{
			CyNode node = physicalNodeNameMap.get(name);
			nodes.add(node);
		}

		SearchParameters searchParameters = PanGIA.parameters;
		String pScoreColumnName = searchParameters.getPhysicalEdgeAttrName();
		String gScoreColumnName = searchParameters.getGeneticEdgeAttrName();
		CyTable detailedEdgeTable = detailedNetwork.getDefaultEdgeTable();
		if( detailedEdgeTable.getColumn(pScoreColumnName) == null )
			detailedEdgeTable.createColumn(pScoreColumnName, Double.class, false);
		if( detailedEdgeTable.getColumn(gScoreColumnName) == null )
			detailedEdgeTable.createColumn(gScoreColumnName, Double.class, false);

		//Physical Edges
		List<CyEdge> physicalEdges = origPhysNetwork.getEdgeList();
		for( CyEdge physicalEdge : physicalEdges )
		{
			CyNode source = physicalEdge.getSource();
			CyNode target = physicalEdge.getTarget();
			if( !nodes.contains(source) || !nodes.contains(target) )
				continue;
			String sourceName = origPhysNetwork.getRow(source).get(CyNetwork.NAME, String.class);
			String targetName = origPhysNetwork.getRow(target).get(CyNetwork.NAME, String.class);
			CyNode s = nestedNodeNameMap.get(sourceName);
			CyNode t = nestedNodeNameMap.get(targetName);
			CyEdge newEdge = detailedNetwork.addEdge(s, t, physicalEdge.isDirected());
			CyRow detailedEdgeRow = detailedNetwork.getRow(newEdge);
			CyRow physicalEdgeRow = origPhysNetwork.getRow(physicalEdge);
			//Interaction
			detailedEdgeRow.set(PanGIA.INTERACTION_TYPE, "Physical");
			//Name
			String edgeName = physicalEdgeRow.get(CyNetwork.NAME, String.class);
			detailedEdgeRow.set(CyNetwork.NAME, edgeName);
			//Interaction
			String edgeInteraction = physicalEdgeRow.get(CyEdge.INTERACTION, String.class);
			detailedEdgeRow.set(CyEdge.INTERACTION, edgeInteraction);
			//PScore
			Double pScore = physicalEdgeRow.get(pScoreColumnName, Double.class);
			detailedEdgeRow.set(pScoreColumnName, pScore);
			//GScore
			Double gScore = physicalEdgeRow.get(gScoreColumnName, Double.class);
			detailedEdgeRow.set(gScoreColumnName, gScore);
		}

		//Genetic Edges
		for( CyEdge edge : origGenNetwork.getEdgeList() )
		{
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if( !nodes.contains(source) || !nodes.contains(target) )
				continue;
			String sourceName = origGenNetwork.getRow(source).get(CyNetwork.NAME, String.class);
			String targetName = origGenNetwork.getRow(target).get(CyNetwork.NAME, String.class);
			CyNode s = nestedNodeNameMap.get(sourceName);
			CyNode t = nestedNodeNameMap.get(targetName);
			CyEdge newEdge = detailedNetwork.addEdge(s, t, edge.isDirected());
			CyRow geneticEdgeRow = origGenNetwork.getRow(edge);
			CyRow detailedEdgeRow = detailedNetwork.getRow(newEdge);
			String existing = detailedEdgeRow.get(PanGIA.INTERACTION_TYPE, String.class);
			String interactionType = "Genetic";
			if (existing == null || !existing.equals("Physical") )
				interactionType = "Genetic";
			else
				interactionType = "Physical&Genetic";
			if (PanGIA.isGNetSigned)
			{
				Double geneticScore = geneticEdgeRow.get(genEdgeAttrName, Double.class);
				if( geneticScore != null )
				{
					if ( geneticScore < 0)
						interactionType += "(negative)";
					else
						interactionType += "(positive)";
				}
			}
			//Interaction
			detailedEdgeRow.set(PanGIA.INTERACTION_TYPE, interactionType);
			//Name
			String edgeName = geneticEdgeRow.get(CyNetwork.NAME, String.class);
			detailedEdgeRow.set(CyNetwork.NAME, edgeName);
			//Interaction
			String edgeInteraction = geneticEdgeRow.get(CyEdge.INTERACTION, String.class);
			detailedEdgeRow.set(CyEdge.INTERACTION, edgeInteraction);
			//PScore
			Double pScore = geneticEdgeRow.get(pScoreColumnName, Double.class);
			detailedEdgeRow.set(pScoreColumnName, pScore);
			//GScore
			Double gScore = geneticEdgeRow.get(gScoreColumnName, Double.class);
			detailedEdgeRow.set(gScoreColumnName, gScore);
		}

		CyNetworkView fooView = ServicesUtil.cyNetworkViewFactoryServiceRef.createNetworkView(detailedNetwork);
		ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(fooView);
		CyLayoutAlgorithm algorithm = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");
		TaskIterator ti = algorithm.createTaskIterator(fooView, algorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		ServicesUtil.taskManagerServiceRef.execute(ti);

		isolatePangiaStyles();
		if( moduleVS != null )
		{
			moduleVS.apply(fooView);
			fooView.updateView();
		}
	}

	private static VisualStyle moduleVS = null;

	private static void isolatePangiaStyles()
	{
		Set<VisualStyle> loadedVisualStyles = ServicesUtil.visualMappingManagerRef.getAllVisualStyles();
		for( VisualStyle vs : loadedVisualStyles )
		{
			String title = vs.getTitle();
			if( title.equals(PanGIA.VS_MODULE_NAME) )
				moduleVS = vs;
		}
	}

	public static void goToNestedNetwork(CyNode n)
	{
		// Sanity check.
		// If this network does not have a network pointer, there is no nested network to go to.
		if (n.getNetworkPointer() == null)
            return;

		// Follow the network pointer to get the nested network.
	    CyNetwork nestedNetwork = n.getNetworkPointer();

		// Get any existing networkViews.
		Collection<CyNetworkView> networkViews = ServicesUtil.cyNetworkViewManagerServiceRef.getNetworkViews(nestedNetwork);

		// If no networks currently exist, create one.
		if( networkViews.isEmpty() )
		{
			CyNetworkView view = ServicesUtil.cyNetworkViewFactoryServiceRef.createNetworkView(nestedNetwork);
			ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(view);
			CyLayoutAlgorithm algorithm = ServicesUtil.cyLayoutsServiceRef.getLayout("force-directed");
			TaskIterator ti = algorithm.createTaskIterator(view, algorithm.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
			ServicesUtil.taskManagerServiceRef.execute(ti);
			isolatePangiaStyles();
			moduleVS.apply(view);
			view.updateView();
		}
		else
		{
			CyNetworkView view = networkViews.iterator().next();
			ServicesUtil.cyApplicationManagerServiceRef.setCurrentNetwork(view.getModel());
			ServicesUtil.cyApplicationManagerServiceRef.setCurrentNetworkView(view);
		}
	}
	
	private static String findNextAvailableNetworkName(final String initialPreference) {
		// Try the preferred choice first:
		CyNetwork network = getNetworkByTitle(initialPreference);
		if (network == null)
			return initialPreference;

		for (int suffix = 1; true; ++suffix) {
			final String titleCandidate = initialPreference + "-" + suffix;
			network = getNetworkByTitle(titleCandidate);
			if (network == null)
				return titleCandidate;
		}
	}
	
	/**
	 * Returns the first network with title "networkTitle" or null, if there is
	 * no network w/ this title.
	 */
	private static CyNetwork getNetworkByTitle(final String networkTitle) {
		Set<CyNetwork> networks = ServicesUtil.cyNetworkManagerServiceRef.getNetworkSet();
		for (final CyNetwork network : networks) {
			String title = network.getRow(network).get("name", String.class);
			if (title.equals(networkTitle))
				return network;
		}

		return null;
	}

}
