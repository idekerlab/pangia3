package org.idekerlab.PanGIAPlugin;


import org.cytoscape.model.*;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.idekerlab.PanGIAPlugin.ModFinder.BFEdge;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkEdge;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNetwork;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNodeModule;

import java.io.InputStream;
import java.util.*;


/**
 * The sole purpose of this class is to sort networks according to decreasing
 * score.
 */
class NetworkAndScore implements Comparable<NetworkAndScore>
{
	private final String nodeName;
	private final Set<String> genes;
	private final double score;
	private final int index;
	private static int nextIndex;

	NetworkAndScore(final String nodeName, final Set<String> genes,
					final double score)
	{
		this.nodeName = nodeName;
		this.genes = genes;
		this.score = score;
		this.index = nextIndex++;
	}

	String getNodeName()
	{
		return nodeName;
	}

	Set<String> getGenes()
	{
		return genes;
	}

	public boolean equals(final Object o)
	{
		if (!(o instanceof NetworkAndScore))
			return false;

		final NetworkAndScore other = (NetworkAndScore) o;
		return other.score == score && other.index == index;
	}

	public int compareTo(final NetworkAndScore other)
	{
		if (other == null)
			throw new NullPointerException("can't compare this against null!");

		if (other.score < score)
			return -1;
		else if (other.score > score)
			return +1;
		return other.index - index;
	}
}


/**
 * @author ruschein
 *         <p/>
 *         Creates an overview network for the detected complexes and
 *         nested networks for each complex.
 */
@SuppressWarnings("unchecked")
public class NestedNetworkCreator
{

	private static final String LAYOUT_ALGORITHM = "force-directed";

	// Also exists in BipartiteVisualiserPlugin!
	public static final String REFERENCE_NETWORK_NAME_ATTRIB = "BipartiteVisualiserReferenceNetworkName";


	/////////////// Node Attribute Names /////////////
	// This is common prefix for all finders.
	private static final String MODULE_FINDER_PREFIX = "PanGIA.";

	// Number of nodes in a module
	private static final String GENE_COUNT = MODULE_FINDER_PREFIX + "module size";
	// And its SQRT value for visual mapping
	private static final String GENE_COUNT_SQRT = MODULE_FINDER_PREFIX + "SQRT of module size";

	private static final String MEMBERS = MODULE_FINDER_PREFIX + "members";

	public static final String PHYSICAL_EDGE_COUNT = MODULE_FINDER_PREFIX + "physical interaction count";
	public static final String GENETIC_EDGE_COUNT = MODULE_FINDER_PREFIX + "genetic interaction count";

	/////////////// Edge Attribute Names /////////////
	public static final String EDGE_SCORE = MODULE_FINDER_PREFIX + "edge score";
	public static final String EDGE_PVALUE = MODULE_FINDER_PREFIX + "p-value";
	public static final String EDGE_SOURCE_SIZE = MODULE_FINDER_PREFIX + "source size";
	public static final String EDGE_TARGET_SIZE = MODULE_FINDER_PREFIX + "target size";
	public static final String EDGE_GENETIC_DENSITY = MODULE_FINDER_PREFIX + "genetic interaction density";


	public static final String COMPLEX_INTERACTION_TYPE = "module-module";

	private CyNetwork overviewNetwork = null;
	private Map<TypedLinkNodeModule<String, BFEdge>, CyNode> moduleToCyNodeMap;
	private int maxSize = 0;
	private final PriorityQueue<NetworkAndScore> networksOrderedByScores = new PriorityQueue(100);

	private final CyNetwork origPhysNetwork;
	private final CyNetwork origGenNetwork;
	private final TypedLinkNetwork<String, Float> physicalNetwork;
	private final TypedLinkNetwork<String, Float> geneticNetwork;
	private final float remainingPercentage;

	private final boolean isGNetSigned;
	private final String geneticEdgeAttrName;
	private final TypedLinkNetwork<TypedLinkNodeModule<String, BFEdge>, BFEdge> networkOfModules;
	private final Map<TypedLinkNodeModule<String, BFEdge>, String> module_name;
	private final String networkName;

	private SearchParameters searchParameters = null;

	public static List<String> getEdgeAttributeNames()
	{
		List<String> names = new ArrayList<String>(2);
		names.add(EDGE_SCORE);
		names.add(EDGE_PVALUE);
		names.add(GENETIC_EDGE_COUNT);
		names.add(PHYSICAL_EDGE_COUNT);
		names.add(EDGE_SOURCE_SIZE);
		names.add(EDGE_TARGET_SIZE);
		names.add(EDGE_GENETIC_DENSITY);

		return names;
	}

	/**
	 * Instantiates an overview network of complexes (modules) and one nested
	 * network for each node in the overview network.
	 *
	 * @param networkOfModules    a representation of the "overview" network
	 * @param origPhysNetwork     the network that the overview network was generated from
	 * @param remainingPercentage 100 - this is where to start with the percent-completed
	 */
	NestedNetworkCreator(
			final TypedLinkNetwork<TypedLinkNodeModule<String, BFEdge>, BFEdge> networkOfModules,
			final CyNetwork origPhysNetwork, final CyNetwork origGenNetwork,
			final TypedLinkNetwork<String, Float> physicalNetwork,
			final TypedLinkNetwork<String, Float> geneticNetwork,
			final float remainingPercentage,
			Map<TypedLinkNodeModule<String, BFEdge>, String> module_name,
			String networkName,
			boolean isGNetSigned,
			String geneticEdgeAttrName,
			SearchParameters searchParameters
	)
	{
		this.origGenNetwork = origGenNetwork;
		this.origPhysNetwork = origPhysNetwork;
		this.physicalNetwork = physicalNetwork;
		this.geneticNetwork = geneticNetwork;
		this.remainingPercentage = remainingPercentage;

		this.isGNetSigned = isGNetSigned;
		this.geneticEdgeAttrName = geneticEdgeAttrName;
		this.networkOfModules = networkOfModules;

		this.module_name = module_name;
		this.networkName = networkName;
		this.searchParameters = searchParameters;
	}

	private String getNodeName(CyNetwork network, TypedLinkNodeModule<String, BFEdge> module, int nodeIndex, Map<TypedLinkNodeModule<String, BFEdge>, String> moduleName)
	{
		//Auto label small complexes with the gene names
		if (module.size() <= 2)
		{
			Iterator<String> genes = module.getMemberValues().iterator();
			String geneName = genes.next();
			String newName = '[' + geneName;

			while (genes.hasNext())
			{
				newName += ", " + genes.next();
			}
			newName += "]";
			return findNextAvailableNodeName(network, newName);
		}

		//Annotate large complexes
		if (moduleName != null)
		{
			String name = moduleName.get(module);
			if (name != null)
				return findNextAvailableNodeName(network, name);
		}

		return findNextAvailableNodeName(network, "Module" + nodeIndex);
	}

	CyNetwork getOverviewNetwork()
	{
		return overviewNetwork;
	}

	/**
	 * @returns a new node in the overview (module/complex) network.
	 */
	private CyNode makeOverviewNode(final String nodeName,
									final TypedLinkNodeModule<String, BFEdge> module,
									TypedLinkNetwork<String, Float> physicalNetwork, TypedLinkNetwork<String, Float> geneticNetwork)
	{

		final CyNode newNode = overviewNetwork.addNode();
		overviewNetwork.getRow(newNode).set("name", nodeName);
		moduleToCyNodeMap.put(module, newNode);

		//Add attributes
		CyRow row = overviewNetwork.getRow(newNode);

		final Set<String> genes = module.getMemberValues();
		final Integer geneCount = genes.size();

		CyTable nodeTable = overviewNetwork.getDefaultNodeTable();
		if (nodeTable.getColumn(GENE_COUNT) == null)
			nodeTable.createColumn(GENE_COUNT, Integer.class, false);
		row.set(GENE_COUNT, geneCount);

		if (nodeTable.getColumn(GENE_COUNT_SQRT) == null)
			nodeTable.createColumn(GENE_COUNT_SQRT, Double.class, false);
		row.set(GENE_COUNT_SQRT, Math.sqrt(geneCount));

		if (genes.size() > maxSize)
			maxSize = genes.size();

		final double score = module.score();

		StringBuilder members = new StringBuilder();
		for (String gene : genes)
		{
			if (members.length() != 0)
				members.append('|');
			members.append(gene);
		}

		if (nodeTable.getColumn(MEMBERS) == null)
			nodeTable.createColumn(MEMBERS, String.class, false);
		overviewNetwork.getRow(newNode).set(MEMBERS, members.toString());

		int physicalEdgeCount = physicalNetwork.subNetwork(module.asStringSet()).numEdges();
		if (nodeTable.getColumn(PHYSICAL_EDGE_COUNT) == null)
			nodeTable.createColumn(PHYSICAL_EDGE_COUNT, Integer.class, false);
		row.set(PHYSICAL_EDGE_COUNT, physicalEdgeCount);

		int geneticEdgeCount = geneticNetwork.subNetwork(module.asStringSet()).numEdges();
		if (nodeTable.getColumn(GENETIC_EDGE_COUNT) == null)
			nodeTable.createColumn(GENETIC_EDGE_COUNT, Integer.class, false);
		row.set(GENETIC_EDGE_COUNT, geneticEdgeCount);

		//Add to network
		networksOrderedByScores.add(new NetworkAndScore(nodeName, genes, score));

		return newNode;
	}

	private List<CyColumn> addColumns(CyTable originTable, CyTable nestedTable)
	{
		List<CyColumn> result = new ArrayList<CyColumn>();
		for( CyColumn c : originTable.getColumns() )
		{
			String columnName = c.getName();
			if( nestedTable.getColumn(columnName) == null )
			{
				if( c.getType().equals(java.util.List.class) )
				{
					nestedTable.createListColumn(columnName, c.getListElementType(), c.isImmutable());
					result.add(c);
				}
				else
				{
					nestedTable.createColumn(columnName, c.getType(), c.isImmutable());
					result.add(c);
				}
			}
		}
		return result;
	}

	public void copyNetwork(CyNetwork source)
	{
		CyNetwork target = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
		target.getRow(target).set(CyNetwork.NAME, "FooNetwork");
		CyTable targetNodeTable = target.getDefaultNodeTable();

		CyTable sourceNodeTable = source.getDefaultNodeTable();

		//Maybe store the type here with the name???
		List<CyColumn> addedColumns = new ArrayList<CyColumn>();
		for( CyColumn sourceColumn : sourceNodeTable.getColumns() )
		{
			if( targetNodeTable.getColumn(sourceColumn.getName()) == null )
			{
				targetNodeTable.createColumn(sourceColumn.getName(), sourceColumn.getType(), sourceColumn.isImmutable() );
				addedColumns.add(sourceColumn);
			}
			else if( sourceColumn.getName().equals( CyNetwork.NAME) )
			{
				addedColumns.add(sourceColumn);
			}
		}

		//Need some bookkeeping for when we add edges....
		HashMap<CyNode, CyNode> targetMap = new HashMap<CyNode, CyNode>();

		for( CyNode sourceNode : source.getNodeList() )
		{
			CyNode targetNode = target.addNode();
			for( CyColumn sourceColumn : addedColumns )
			{
				//Need the type!!!
				Object sourceData = source.getRow(sourceNode).get(sourceColumn.getName(), sourceColumn.getType());
				target.getRow(targetNode).set(sourceColumn.getName(), sourceData);
			}
			String targetNodeName = target.getRow(targetNode).get(CyNetwork.NAME, String.class);
			targetMap.put(sourceNode, targetNode);
		}

		//Start copying edges:

		for( CyEdge sourceEdge : source.getEdgeList() )
		{
			CyNode oldS = sourceEdge.getSource();
			CyNode oldT = sourceEdge.getTarget();

			CyNode s = targetMap.get(oldS);
			CyNode t = targetMap.get(oldT);

			CyEdge newEdge = target.addEdge(s, t, sourceEdge.isDirected() );

			//From here copy columns, just like you did for nodes above...

		}

	}


	private HashMap<String,CyNode> createNodeNameMap(CyNetwork originNetwork)
	{
		HashMap<String, CyNode> result = new HashMap<String, CyNode>();
		for (CyNode node : originNetwork.getNodeList())
		{
			String name = originNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			result.put(name, node);
		}
		return result;
	}

	private CyNetwork generateNestedNetwork(final String networkName,
											final Set<String> nodeNames, final boolean createNetworkView,
											TaskMonitor taskMonitor)
	{
		if (nodeNames.isEmpty())
			return null;

		HashMap<String, CyNode> physicalNodeNameMap = createNodeNameMap(origPhysNetwork);
		HashMap<String, CyNode> geneticNodeNameMap = createNodeNameMap(origGenNetwork);

		List<CyNode> nodes = new ArrayList<CyNode>();
		for (String name : nodeNames)
		{
			if( physicalNodeNameMap.containsKey(name) )
			{
				nodes.add(physicalNodeNameMap.get(name));
			}
			if( geneticNodeNameMap.containsKey(name))
			{
				nodes.add(geneticNodeNameMap.get(name));
			}
		}

		//CyNetwork nestedNetwork = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
		CyNetwork nestedNetwork = root.addSubNetwork();

		//Physical and Genetic Node Tables
		CyTable physicalNodeTable = origPhysNetwork.getDefaultNodeTable();
		CyTable geneticNodeTable = origGenNetwork.getDefaultNodeTable();
		//Nested Node Table
		CyTable nestedNodeTable = nestedNetwork.getDefaultNodeTable();
		List<CyColumn> addedPhysicalNodeColumns = addColumns(physicalNodeTable, nestedNodeTable);
		List<CyColumn> addedGeneticNodeColumns = addColumns(geneticNodeTable, nestedNodeTable);
		List<CyColumn> addedNodeColumns = new ArrayList<CyColumn>();
		addedNodeColumns.addAll(addedPhysicalNodeColumns);
		addedNodeColumns.addAll(addedGeneticNodeColumns);

		HashMap<String, CyNode> nestedNodeNameMap = new HashMap<String, CyNode>();
		for (String nodeName : nodeNames)
		{
			CyNode node = nestedNetwork.addNode();
			CyRow row = nestedNetwork.getRow(node);
			row.set(CyNetwork.NAME, nodeName);
			if( geneticNodeNameMap.containsKey(nodeName) )
			{
				CyNode geneticNode = geneticNodeNameMap.get(nodeName);
				CyRow gRow = origGenNetwork.getRow(geneticNode);
				for (CyColumn c : addedNodeColumns)
				{
					Object gValue = gRow.get(c.getName(), c.getType());
					if( gValue == null )
						continue;
					boolean isEmptyString = false;
					if( c.getType() == String.class )
						isEmptyString = gValue.toString().isEmpty();
					if( !isEmptyString )
						row.set(c.getName(), gValue);
				}
			}
			if( physicalNodeNameMap.containsKey(nodeName) )
			{
				CyNode physicalNode = physicalNodeNameMap.get(nodeName);
				CyRow pRow = origPhysNetwork.getRow(physicalNode);
				for (CyColumn c : addedNodeColumns)
				{
					Object pValue = pRow.get(c.getName(), c.getType());
					if( pValue == null )
						continue;
					boolean isEmptyString = false;
					if( c.getType() == String.class )
						isEmptyString = pValue.toString().isEmpty();
					if( !isEmptyString )
						row.set(c.getName(), pValue);
				}
			}
			nestedNodeNameMap.put(nodeName, node);
		}

		CyTable edgeTable = nestedNetwork.getDefaultEdgeTable();
		if (edgeTable.getColumn(PanGIA.INTERACTION_TYPE) == null)
			edgeTable.createColumn(PanGIA.INTERACTION_TYPE, String.class, false);
		String pScoreColumnName = searchParameters.getPhysicalEdgeAttrName();
		String gScoreColumnName = searchParameters.getGeneticEdgeAttrName();
		if (!pScoreColumnName.equals("none"))
		{
			if (edgeTable.getColumn(pScoreColumnName) == null)
				edgeTable.createColumn(pScoreColumnName, Double.class, false);
		}
		if (!gScoreColumnName.equals("none"))
		{
			if (edgeTable.getColumn(gScoreColumnName) == null)
				edgeTable.createColumn(gScoreColumnName, Double.class, false);
		}

		//Physical and Genetic Edge Tables
		CyTable physicalEdgeTable = origPhysNetwork.getDefaultEdgeTable();
		CyTable geneticEdgeTable = origGenNetwork.getDefaultEdgeTable();
		//Nested Edge Table
		CyTable nestedEdgeTable = nestedNetwork.getDefaultEdgeTable();
		List<CyColumn> addedPhysicalEdgeColumns = addColumns(physicalEdgeTable, nestedEdgeTable);
		List<CyColumn> addedGeneticEdgeColumns = addColumns(geneticEdgeTable, nestedEdgeTable);
		List<CyColumn> addedEdgeColumns = new ArrayList<CyColumn>();
		addedEdgeColumns.addAll(addedPhysicalEdgeColumns);
		addedEdgeColumns.addAll(addedGeneticEdgeColumns);

		for (CyEdge edge : origGenNetwork.getEdgeList())
		{
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			if (!nodes.contains(source) || !nodes.contains(target))
				continue;
			String sourceName = origGenNetwork.getRow(source).get(CyNetwork.NAME, String.class);
			String targetName = origGenNetwork.getRow(target).get(CyNetwork.NAME, String.class);
			CyNode s = nestedNodeNameMap.get(sourceName);
			CyNode t = nestedNodeNameMap.get(targetName);
			CyEdge newEdge = nestedNetwork.addEdge(s, t, edge.isDirected());
			CyRow row = nestedNetwork.getRow(newEdge);
			CyRow gRow = origGenNetwork.getRow(edge);
			row.set(CyNetwork.NAME, gRow.get(CyNetwork.NAME, String.class));
			row.set(CyEdge.INTERACTION, gRow.get(CyEdge.INTERACTION, String.class));
			String interactionType = "Genetic";
			if (isGNetSigned)
			{
				Double geneticScore = origGenNetwork.getRow(edge).get(geneticEdgeAttrName, Double.class);
				if (geneticScore != null)
				{
					if (geneticScore < 0)
						interactionType += "(negative)";
					else
						interactionType += "(positive)";
				}
			}
			row.set(PanGIA.INTERACTION_TYPE, interactionType);
			Double gScore = origGenNetwork.getRow(edge).get(gScoreColumnName, Double.class);
			nestedNetwork.getRow(newEdge).set(gScoreColumnName, gScore);

			for (CyColumn c : addedEdgeColumns)
			{
				Object gValue = gRow.get(c.getName(), c.getType());
				if( gValue == null )
					continue;
				boolean isEmptyString = false;
				if( c.getType() == String.class )
					isEmptyString = gValue.toString().isEmpty();
				if( !isEmptyString )
					row.set(c.getName(), gValue);
			}
		}




		for (CyEdge physicalEdge : origPhysNetwork.getEdgeList())
		{
			CyNode source = physicalEdge.getSource();
			CyNode target = physicalEdge.getTarget();
			if (!nodes.contains(source) || !nodes.contains(target))
				continue;
			String sourceName = origPhysNetwork.getRow(source).get(CyNetwork.NAME, String.class);
			String targetName = origPhysNetwork.getRow(target).get(CyNetwork.NAME, String.class);
			CyNode s = nestedNodeNameMap.get(sourceName);
			CyNode t = nestedNodeNameMap.get(targetName);
			CyEdge newEdge = nestedNetwork.addEdge(s, t, physicalEdge.isDirected());
			CyRow row = nestedNetwork.getRow(newEdge);
			CyRow pRow = origPhysNetwork.getRow(physicalEdge);
			row.set(CyNetwork.NAME, pRow.get(CyNetwork.NAME, String.class));
			row.set(CyEdge.INTERACTION, pRow.get(CyEdge.INTERACTION, String.class));

			String interactionType = "Physical";
			row.set(PanGIA.INTERACTION_TYPE, interactionType);
			Double pScore = origPhysNetwork.getRow(physicalEdge).get(pScoreColumnName, Double.class);
			row.set(pScoreColumnName, pScore);
			for (CyColumn c : addedEdgeColumns)
			{
				Object pValue = pRow.get(c.getName(), c.getType());
				if( pValue == null )
					continue;
				boolean isEmptyString = false;
				if( c.getType() == String.class )
					isEmptyString = pValue.toString().isEmpty();
				if( !isEmptyString )
					row.set(c.getName(), pValue);
			}
		}

		CyTable nestedNetworkTable = nestedNetwork.getDefaultNetworkTable();
		if (nestedNetworkTable.getColumn(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME) == null)
			nestedNetworkTable.createColumn(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, String.class, false);
		CyRow nestedNetworkRow = nestedNetwork.getRow(nestedNetwork);
		nestedNetworkRow.set(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, NetworkType.MODULE.name());
		nestedNetworkRow.set("name", findNextAvailableNetworkName(networkName));

		ServicesUtil.cyNetworkManagerServiceRef.addNetwork(nestedNetwork);

		if (createNetworkView)
		{
			CyNetworkView view = ServicesUtil.cyNetworkViewFactoryServiceRef.createNetworkView(nestedNetwork);
			ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(view);

			CyLayoutAlgorithm alg = ServicesUtil.cyLayoutsServiceRef.getLayout(LAYOUT_ALGORITHM);

			//Setup the layout...
			Object ctx = alg.getDefaultLayoutContext();
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("defaultSpringCoefficient", 1.0E-5);
			settings.put("defaultSpringLength", 100);
			settings.put("defaultNodeMass", 20);
			TunableSetter setter = ServicesUtil.registrar.getService(TunableSetter.class);
			setter.applyTunables(ctx, settings);

			TaskIterator ti = alg.createTaskIterator(view, ctx, CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
			try
			{
				ti.next().run(taskMonitor);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			moduleVS.apply(view);
			view.updateView();
		}

		return nestedNetwork;
	}

	/**
	 * Finds an unused network name starting with a first choice. If the first
	 * choice is not available, we will successively try to append -1 -2, -3 and
	 * so on, until we indentify an unused name.
	 *
	 * @param initialPreference The network name we'd like to use, if it is available. If not
	 *                          we use it as a prefix instead.
	 */
	private String findNextAvailableNetworkName(final String initialPreference)
	{
		// Try the preferred choice first:
		CyNetwork network = getNetworkByTitle(initialPreference);
		if (network == null)
			return initialPreference;

		for (int suffix = 1; true; ++suffix)
		{
			final String titleCandidate = initialPreference + '-' + suffix;
			network = getNetworkByTitle(titleCandidate);
			if (network == null)
				return titleCandidate;
		}
	}

	/**
	 * Finds an unused node name starting with a first choice. If the first
	 * choice is not available, we will successively try to append -1 -2, -3 and
	 * so on, until we indentify an unused name.
	 *
	 * @param initialPreference The node name we'd like to use, if it is available. If not we
	 *                          use it as a prefix instead.
	 */
	private static String findNextAvailableNodeName(CyNetwork network, final String initialPreference)
	{

		CyTable nodeTable = network.getDefaultNodeTable();
		// Try the preferred choice first:
		if (nodeTable.getMatchingRows("name", initialPreference).isEmpty())
			return initialPreference;

		for (int suffix = 1; true; ++suffix)
		{
			final String titleCandidate = initialPreference + '-' + suffix;
			if (nodeTable.getMatchingRows("name", titleCandidate).isEmpty())
				return titleCandidate;
		}
	}

	/**
	 * Returns the first network with title "networkTitle" or null, if there is
	 * no network w/ this title.
	 */
	private static CyNetwork getNetworkByTitle(final String networkTitle)
	{
		Set<CyNetwork> networks = ServicesUtil.cyNetworkManagerServiceRef.getNetworkSet();
		for (final CyNetwork network : networks)
		{
			String nextNetworkTitle = network.getRow(network).get("name", String.class);
			if (networkTitle.equals(nextNetworkTitle))
				return network;
		}
		return null;
	}

	private static void applyNetworkLayout(final CyNetwork network, TaskMonitor taskMonitor)
	{
		final Collection<CyNetworkView> targetViews = ServicesUtil.cyNetworkViewManagerServiceRef.getNetworkViews(network);
		if (!targetViews.isEmpty())
		{
			CyNetworkView view = targetViews.iterator().next();
			CyLayoutAlgorithm alg = ServicesUtil.cyLayoutsServiceRef.getLayout(LAYOUT_ALGORITHM);

			//Setup the layout...
			Object ctx = alg.getDefaultLayoutContext();
			Map<String, Object> settings = new HashMap<String, Object>();
			settings.put("defaultSpringCoefficient", 1.0E-2);
			settings.put("defaultSpringLength", 100);
			settings.put("defaultNodeMass", 20);
			TunableSetter setter = ServicesUtil.registrar.getService(TunableSetter.class);
			setter.applyTunables(ctx, settings);

			TaskIterator ti = alg.createTaskIterator(view, ctx, CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
			try
			{
				ti.next().run(taskMonitor);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			view.updateView();
		}
	}

	private static VisualStyle overviewVS;
	private static VisualStyle moduleVS;

	private static boolean areAllPangiaStylesAlreadyLoaded()
	{
		int numStylesLoaded = 0;
		Set<VisualStyle> loadedVisualStyles = ServicesUtil.visualMappingManagerRef.getAllVisualStyles();
		for (VisualStyle vs : loadedVisualStyles)
		{
			if (vs.getTitle().equals(PanGIA.VS_OVERVIEW_NAME) || vs.getTitle().equals(PanGIA.VS_MODULE_NAME))
				numStylesLoaded++;
		}
		return numStylesLoaded == PanGIA.NUM_PANGIA_STYLES;
	}

	private static void isolatePangiaStyles()
	{
		Set<VisualStyle> loadedVisualStyles = ServicesUtil.visualMappingManagerRef.getAllVisualStyles();
		for (VisualStyle vs : loadedVisualStyles)
		{
			String title = vs.getTitle();
			if (title.equals(PanGIA.VS_OVERVIEW_NAME))
				overviewVS = vs;
			else if (title.equals(PanGIA.VS_MODULE_NAME))
				moduleVS = vs;
		}
	}

	public void createNetworks(TaskMonitor taskMonitor) throws Exception
	{
		int MAX_NETWORK_VIEWS;
		try
		{
			MAX_NETWORK_VIEWS = new Integer(ServicesUtil.cytoscapePropertiesServiceRef.getProperties().getProperty("moduleNetworkViewCreationThreshold"));
		}
		catch (Exception e)
		{
			MAX_NETWORK_VIEWS = 5;
		}

		//Code to load styles.
		if (!areAllPangiaStylesAlreadyLoaded())
		{
			InputStream urlStream = getClass().getResource("/PangiaVS.xml").openStream();
			ServicesUtil.loadVizmapFileTaskFactory.loadStyles(urlStream);
			urlStream.close();
		}
		isolatePangiaStyles();

		// Network attributes created here is required for managing Visual Styles.
		moduleToCyNodeMap = new HashMap<TypedLinkNodeModule<String, BFEdge>, CyNode>();

		overviewNetwork = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
		CyRow overviewRow = overviewNetwork.getRow(overviewNetwork);
		overviewRow.set("name", findNextAvailableNetworkName(networkName));
		ServicesUtil.cyNetworkManagerServiceRef.addNetwork(overviewNetwork);

		CyTable networkTable = overviewNetwork.getDefaultNetworkTable();
		networkTable.createColumn(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, String.class, false);
		overviewRow.set(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, NetworkType.OVERVIEW.name());

		taskMonitor.setStatusMessage("5. Generating Cytoscape networks");
		int nodeIndex = 1;
		double maxScore = Double.NEGATIVE_INFINITY;

		maxSize = 0;
		for (final TypedLinkEdge<TypedLinkNodeModule<String, BFEdge>, BFEdge> edge : networkOfModules.edgeIterator())
		{
			final TypedLinkNodeModule<String, BFEdge> sourceModule = edge.source().value();
			CyNode sourceNode = moduleToCyNodeMap.get(sourceModule);
			if (sourceNode == null)
			{
				final String nodeName = getNodeName(origPhysNetwork, sourceModule, nodeIndex, module_name);
				sourceNode = makeOverviewNode(nodeName, sourceModule, physicalNetwork, geneticNetwork);
				++nodeIndex;
			}

			final TypedLinkNodeModule<String, BFEdge> targetModule = edge.target().value();
			CyNode targetNode = moduleToCyNodeMap.get(targetModule);
			if (targetNode == null)
			{
				final String nodeName = getNodeName(origPhysNetwork, targetModule, nodeIndex, module_name);
				targetNode = makeOverviewNode(nodeName, targetModule, physicalNetwork, geneticNetwork);
				++nodeIndex;
			}

			CyEdge newEdge = overviewNetwork.addEdge(sourceNode, targetNode, false);

			CyRow edgeRow = overviewNetwork.getRow(newEdge);
			edgeRow.set("interaction", COMPLEX_INTERACTION_TYPE);

			String sourceName = overviewNetwork.getRow(sourceNode).get(CyNetwork.NAME, String.class);
			String targetName = overviewNetwork.getRow(targetNode).get(CyNetwork.NAME, String.class);
			edgeRow.set(CyNetwork.NAME, sourceName + " (" + COMPLEX_INTERACTION_TYPE + ") " + targetName);


			CyTable edgeTable = overviewNetwork.getDefaultEdgeTable();
			if (edgeTable.getColumn(EDGE_SCORE) == null)
				edgeTable.createColumn(EDGE_SCORE, Double.class, false);
			final double edgeScore = edge.value().link();
			edgeRow.set(EDGE_SCORE, edgeScore);

			if (edgeScore > maxScore)
				maxScore = edgeScore;

			if (edgeTable.getColumn(EDGE_PVALUE) == null)
				edgeTable.createColumn(EDGE_PVALUE, Double.class, false);
			final double edgePValue = edge.value().linkMerge();
			edgeRow.set(EDGE_PVALUE, edgePValue);

			if (edgeTable.getColumn(GENETIC_EDGE_COUNT) == null)
				edgeTable.createColumn(GENETIC_EDGE_COUNT, Integer.class, false);
			Set<String> sourceSet = sourceModule.asStringSet();
			Set<String> targetSet = targetModule.asStringSet();
			final int geneticConnectedness = geneticNetwork.getConnectedness(sourceSet, targetSet);
			edgeRow.set(GENETIC_EDGE_COUNT, geneticConnectedness);

			if (edgeTable.getColumn(PHYSICAL_EDGE_COUNT) == null)
				edgeTable.createColumn(PHYSICAL_EDGE_COUNT, Integer.class, false);
			final int physicalConnectness = physicalNetwork.getConnectedness(sourceSet, targetSet);
			edgeRow.set(PHYSICAL_EDGE_COUNT, physicalConnectness);

			if (edgeTable.getColumn(EDGE_SOURCE_SIZE) == null)
				edgeTable.createColumn(EDGE_SOURCE_SIZE, Integer.class, false);
			edgeRow.set(EDGE_SOURCE_SIZE, sourceModule.size());

			if (edgeTable.getColumn(EDGE_TARGET_SIZE) == null)
				edgeTable.createColumn(EDGE_TARGET_SIZE, Integer.class, false);
			edgeRow.set(EDGE_TARGET_SIZE, targetModule.size());

			if (edgeTable.getColumn(EDGE_GENETIC_DENSITY) == null)
				edgeTable.createColumn(EDGE_GENETIC_DENSITY, Double.class, false);
			final double density = edgeScore / (sourceModule.size() + targetModule.size());
			edgeRow.set(EDGE_GENETIC_DENSITY, density);
		}

		taskMonitor.setStatusMessage("5. Generating network views");
		int networkViewCount = 0;
		NetworkAndScore network;
		final float percentIncrement = remainingPercentage / networksOrderedByScores.size();
		float percentCompleted = 100.0f - remainingPercentage;

		CyNetwork newNet = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
		root = ServicesUtil.cyNetworkRootManagerServiceRef.getRootNetwork(newNet);
		//root = ServicesUtil.cyNetworkFactoryServiceRef.createNetwork();
		root.getRow(root).set(CyNetwork.NAME, "Modules");
		while ((network = networksOrderedByScores.poll()) != null)
		{
			final boolean createView = networkViewCount++ < MAX_NETWORK_VIEWS;
			final CyNetwork nestedNetwork = generateNestedNetwork(network.getNodeName(), network.getGenes(), createView, taskMonitor);

			for (CyNode node : overviewNetwork.getNodeList())
			{
				String nodeName = overviewNetwork.getRow(node).get("name", String.class);
				if (nodeName.equalsIgnoreCase(network.getNodeName()))
				{
					node.setNetworkPointer(nestedNetwork);
					break;
				}
			}
			percentCompleted += percentIncrement;
			taskMonitor.setProgress(Math.round(percentCompleted));
		}

		CyNetworkView view = ServicesUtil.cyNetworkViewFactoryServiceRef.createNetworkView(overviewNetwork);
		ServicesUtil.cyNetworkViewManagerServiceRef.addNetworkView(view);

		overviewVS.apply(view);
		view.updateView();

		applyNetworkLayout(overviewNetwork, taskMonitor);
	}

	CyRootNetwork root;

}
