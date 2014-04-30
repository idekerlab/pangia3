package org.idekerlab.PanGIAPlugin;


import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.idekerlab.PanGIAPlugin.ModFinder.BFEdge;
import org.idekerlab.PanGIAPlugin.ModFinder.BFEdge.InteractionType;
import org.idekerlab.PanGIAPlugin.ModFinder.HCScoringFunction;
import org.idekerlab.PanGIAPlugin.ModFinder.HCSearch2;
import org.idekerlab.PanGIAPlugin.ModFinder.SouravScore;
import org.idekerlab.PanGIAPlugin.data.BooleanVector;
import org.idekerlab.PanGIAPlugin.data.DoubleVector;
import org.idekerlab.PanGIAPlugin.data.FloatVector;
import org.idekerlab.PanGIAPlugin.networks.SFEdge;
import org.idekerlab.PanGIAPlugin.networks.SFNetwork;
import org.idekerlab.PanGIAPlugin.networks.SNodeModule;
import org.idekerlab.PanGIAPlugin.networks.UndirectedSEdge;
import org.idekerlab.PanGIAPlugin.networks.hashNetworks.FloatHashNetwork;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkEdge;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNetwork;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNode;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNodeModule;
import org.idekerlab.PanGIAPlugin.ui.SearchPropertyPanel;
import org.idekerlab.PanGIAPlugin.util.ScalerFactory;
import org.idekerlab.PanGIAPlugin.utilities.collections.HashMapUtil;
import org.idekerlab.PanGIAPlugin.utilities.collections.ListOps;
import org.idekerlab.PanGIAPlugin.utilities.collections.SetUtil;
import org.idekerlab.PanGIAPlugin.utilities.html.HTMLPage;
import org.idekerlab.PanGIAPlugin.utilities.html.HTMLParagraphBlock;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * @author kono, ruschein, ghannum
 */
public class SearchTask extends AbstractTask
{
	
	private static final float SEARCH_PERCENTAGE      = 40.0f; // Progress bar should go up to here for the search part.
	private static final float COMPUTE_SIG_PERCENTAGE = 95.0f; // Progress bar should go up to here for the permutations part.
	
	protected static final String EDGE_TYPE_ATTR_NAME = "Module Finder.Interaction Type";
	
	private TaskMonitor taskMonitor = null;
	static int numOfRuns = 1;

	private SearchParameters parameters;
	private SearchPropertyPanel searchPropertyPanel;

	
	public SearchTask(final SearchParameters parameters, SearchPropertyPanel searchPropertyPanel) {
		this.parameters = parameters;
		this.searchPropertyPanel = searchPropertyPanel;
	}

	private long startTime;
	
	public void setTaskMonitor(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}

	public String getTitle() {
		return "PanGIA";
	}

	private void setPercentCompleted(int percent) {
		if (taskMonitor != null)
			taskMonitor.setProgress(percent / 100.0);
	}

	private void setStatus(String message) {
		if (taskMonitor != null)
			taskMonitor.setStatusMessage(message);
	}

	//This function compute a p-value for each edge in the complex-complex network
	private void computeSig(final TypedLinkNetwork<TypedLinkNodeModule<String,BFEdge>,BFEdge> results, SFNetwork gnet,
	                               final double pValueThreshold, final int numberOfSamples, final TaskMonitor taskMonitor, final float startProgressPercentage,
	                               final float endProgressPercentage)
	{
		taskMonitor.setStatusMessage("4. Computing permutations...");

		Map<Integer,DoubleVector> numLinks2empiricalDist = new HashMap<Integer,DoubleVector>(30);
		TypedLinkNetwork<String,Float> gn = gnet.asTypedLinkNetwork();
        
		DoubleVector allEdgeValues = new DoubleVector(gn.numEdges());
	
		for(TypedLinkEdge<String,Float> eachEdge : gn.edgeIterator())
			allEdgeValues.add(eachEdge.value());
        
		//Iterate through each complex-complex interaction in the network:
		//(1) Check if that number of edges has been previously samples, if so, output p-value
		//(2) If not, sample and produce empirical distribution. Cache distribution!
		final int TOTAL_NUM_EDGES = results.numEdges();
		int currentEdgeNum = 0;
		final Set<TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge>> deleteSet = new HashSet<TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge>>();
		for (TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge> edge : results.edgeIterator()) {
			++currentEdgeNum;
			
			if (!edge.value().isType(InteractionType.Genetic))
			{
				deleteSet.add(edge);
				continue;
			}
			
			if (cancelled) return;
			
			// Find number of edges and sum of edge values in the hyperedge
			int numGeneticLinks = 0;
			double sumOfGeneticValues=0.0;
			for (final TypedLinkEdge<String,Float> eachEdge :
			     gn.getAllEdgeValues(edge.source().value().asStringSet(), edge.target().value().asStringSet()))
			{
				sumOfGeneticValues += eachEdge.value();
				++numGeneticLinks;
			}
            
			
			double pVal;
			if (numLinks2empiricalDist.containsKey(numGeneticLinks)) {
				//How to save p-value?
				pVal = numLinks2empiricalDist.get(numGeneticLinks).getEmpiricalValueFromSortedDist(sumOfGeneticValues);
			} else {
				double[] dist = new double[numberOfSamples];
				
				for (int i = 0; i < numberOfSamples; i++) {
					if (cancelled) return;
					double permVal = allEdgeValues.sample(numGeneticLinks, false).sum();
					dist[i] = permVal;
				}
				
				DoubleVector temp = new DoubleVector(dist);
				temp = temp.sort();
				numLinks2empiricalDist.put(numGeneticLinks, temp);

				// Where to save pval
				pVal = temp.getEmpiricalValueFromSortedDist(sumOfGeneticValues);	
			}

			if (pVal <= pValueThreshold)
				edge.value().setLinkMerge((float)pVal);
			else
				deleteSet.add(edge);
			
			final float permutationsFraction = (float)currentEdgeNum / TOTAL_NUM_EDGES;
			final float percentCompleted = startProgressPercentage + (endProgressPercentage - startProgressPercentage) * permutationsFraction;
			taskMonitor.setProgress(Math.round(percentCompleted / 100.0));
			taskMonitor.setStatusMessage("4. Computing permutations: " + Math.round(permutationsFraction * 100.0f) + "% completed.");
		}
		results.removeAllEdgesWNodeUpdate(deleteSet);
	}

	/**
	 *  Converts a Cytoscape-style network to an SFNetwork.
	 *
	 *  @param inputNetwork    name of the network that will be converted
	 *  @param numericAttrName optional name of a numeric edge attribute.  Should this be missing, 1.0 will be assumed for all edges
	 */
	private SFNetwork convertCyNetworkToSFNetwork(final CyNetwork inputNetwork, String nodeAttrName, final String numericAttrName, final ScalingMethodX scalingMethod)
		throws IllegalArgumentException, ClassCastException
	{
		CyTable nodeAttr = inputNetwork.getDefaultNodeTable();

		List<CyEdge> startingEdges = inputNetwork.getEdgeList();
		
		List<CyEdge> netEdges = new ArrayList<CyEdge>(startingEdges.size());
		for (final CyEdge edge : startingEdges)
		{
			CyRow sourceRow = inputNetwork.getRow(edge.getSource());
			CyRow targetRow = inputNetwork.getRow(edge.getTarget());
			Object source = sourceRow.getRaw(nodeAttrName);
			Object target = targetRow.getRaw(nodeAttrName);
			if( source != null && target != null )
				netEdges.add(edge);
		}
				
		final FloatHashNetwork outputNetwork = new FloatHashNetwork(/* selfOk = */false, /* directed = */false, /* startsize = */1);

		if (inputNetwork == null)
			throw new IllegalArgumentException("input parameter inputNetwork must not be null!");

		if (numericAttrName == null || numericAttrName.length() == 0)
		{
			int numNodes = inputNetwork.getNodeCount();
			
			float defaultScore = -(float)Math.log(netEdges.size()/((float)numNodes*(numNodes-1)/2.0f));
			
			for (final CyEdge edge : netEdges)
			{
				CyRow sourceRow = inputNetwork.getRow(edge.getSource());
				CyRow targetRow = inputNetwork.getRow(edge.getTarget());
				String sourceName = sourceRow.get("name", String.class);
				String targetName = targetRow.get("name", String.class);
				outputNetwork.add(sourceName, targetName, defaultScore);
			}
		}
		else
		{
			// Validate that "numericAttrName" is a known numeric edge attribute.
			final CyTable edgeAttributes = inputNetwork.getDefaultEdgeTable();
			Class<?> edgeAttribType = edgeAttributes.getColumn(numericAttrName).getType();

			if (edgeAttribType != Double.class && edgeAttribType != Integer.class)
				throw new IllegalArgumentException("\"" + numericAttrName
				                                   + "\" is not the name of a known numeric edge attribute!");

			List<CyEdge> edges = new ArrayList<CyEdge>(netEdges.size());
			for (CyEdge e : netEdges)
			{
				if( inputNetwork.getRow(e).isSet(numericAttrName) )
					edges.add(e);
			}
			
			// Collect edge attribute values:
			final float[] edgeAttribValues = new float[edges.size()];
			int edgeIndex = 0;
			for (final CyEdge e : edges) {
				CyRow edgeRow = inputNetwork.getRow(e);
				final String edgeID = edgeRow.get("name", String.class);
				if (edgeAttribType == Double.class) {
					final Double attrValue = edgeRow.get(numericAttrName, Double.class);
					if (attrValue != null) edgeAttribValues[edgeIndex] = (float)(double)attrValue;
					
				} else { // Assume we have an integer attribute.
					final Integer attrValue = edgeRow.get(numericAttrName, Integer.class);
					if (attrValue != null)
						edgeAttribValues[edgeIndex] = (float)(int)attrValue;
				}
				++edgeIndex;
			}
			
			final StringBuilder errorMessage = new StringBuilder();
			final float[] scaledEdgeAttribValues = scaleEdgeAttribValues(edgeAttribValues, scalingMethod, errorMessage);
			if (scaledEdgeAttribValues == null)
				throw new IllegalArgumentException("attribute values scaling failed: " + errorMessage);

			edgeIndex = 0;
			for (final CyEdge edge : edges) {
				CyRow edgeRow = inputNetwork.getRow(edge);
				Double doubleAttr = null;
				try
				{
					doubleAttr = edgeRow.get(numericAttrName, Double.class);
				}
				catch( ClassCastException e )
				{
					doubleAttr = null;
				}
				Integer integerAttr = null;
				try
				{
					integerAttr = edgeRow.get(numericAttrName, Integer.class);
				}
				catch( ClassCastException e )
				{
					integerAttr = null;
				}
				if( doubleAttr != null || integerAttr != null )
				{
					CyRow sourceRow = inputNetwork.getRow(edge.getSource());
					CyRow targetRow = inputNetwork.getRow(edge.getTarget());
					String sourceName = sourceRow.get("name", String.class);
					String targetName = targetRow.get("name", String.class);
					outputNetwork.add(sourceName, targetName, scaledEdgeAttribValues[edgeIndex]);
				}
				++edgeIndex;
			}
		}

		return outputNetwork;
	}
	
	private float[] scaleEdgeAttribValues(final float[] edgeAttribValues, final ScalingMethodX scalingMethod, final StringBuilder errorMessage)
	{
		if (scalingMethod == ScalingMethodX.NONE)
			return edgeAttribValues;

		for (int i = 0; i < edgeAttribValues.length; ++i)
			edgeAttribValues[i] = Math.abs(edgeAttribValues[i]);
		
		if (scalingMethod == ScalingMethodX.LINEAR_LOWER) {
			for (int i = 0; i < edgeAttribValues.length; ++i)
				edgeAttribValues[i] = -edgeAttribValues[i];
		}

		final String scalingType;
		float from, to;
		if (scalingMethod == ScalingMethodX.LINEAR_LOWER || scalingMethod == ScalingMethodX.LINEAR_UPPER) {
			final float EPS = 0.25f / edgeAttribValues.length;
			from = 0.5f + EPS;
			to   = 1.0f - EPS;
			scalingType = "linear";
		} else
			throw new IllegalArgumentException("unknown scaling method: " + scalingMethod);
		
		float[] scaledEdgeAttribValues;
		try {
			scaledEdgeAttribValues = ScalerFactory.getScaler(scalingType).scale(edgeAttribValues, from, to);
		} catch (final IllegalArgumentException e) {
			errorMessage.append(e.getMessage());
			return null;
		}

		// Generate log-likelihood values:
		for (int i = 0; i < scaledEdgeAttribValues.length; ++i) {
			final double p = (double)scaledEdgeAttribValues[i];
			scaledEdgeAttribValues[i] = (float)Math.log(p / (1.0 - p));
		}

		return scaledEdgeAttribValues;
	}
	
	private void generateReport(String path, String networkName, int pnetNodes1, int pnetNodes2, int pnetEdges1, int pnetEdges2, int gnetNodes, int gnetEdges, List<SNodeModule> trainingComplexes, ComplexRegressionResult physicalRegress, ComplexRegressionResult geneticRegress, int annotMatches, TypedLinkNetwork<TypedLinkNodeModule<String, BFEdge>, BFEdge> results)
	{
		HTMLPage report = new HTMLPage();
		report.setTitle("PanGIA report: "+networkName);
		
		HTMLParagraphBlock b = new HTMLParagraphBlock(10);
		b.add("PanGIA v. "+PanGIAPlugin.VERSION);
		b.add("Please cite! ...");
		b.add(networkName);
		b.add("");
		b.add("Physical network:");
		CyNetwork physicalNetwork = parameters.getPhysicalNetwork();
		String physicalNetworkName = physicalNetwork.getRow(physicalNetwork).get("name", String.class);
		boolean isBinary = parameters.getPhysicalEdgeAttrName() == null || parameters.getPhysicalEdgeAttrName().length() == 0;
		if (isBinary) b.add(parameters.getPhysicalEdgeAttrName()+"  (binary)");
		else b.add("Edge score: "+parameters.getPhysicalEdgeAttrName()+"  (numeric, scaling="+parameters.getPhysicalScalingMethod()+")");
		b.add("Nodes: "+pnetNodes1+", Edges: "+pnetEdges1+"");
		int nfd = parameters.getPhysicalNetworkFilterDegree();
		if (nfd==-1) b.add("Network filter degree: None");
		else b.add("Network filter degree: "+nfd+"   (Nodes: "+pnetNodes2+", Edges: "+pnetEdges2+")");
		b.add("");
		
		b.add("Genetic network:");
		CyNetwork geneticNetwork = parameters.getGeneticNetwork();
		String geneticNetworkName = geneticNetwork.getRow(geneticNetwork).get("name", String.class);
		b.add(geneticNetworkName);
		isBinary = parameters.getGeneticEdgeAttrName() == null || parameters.getGeneticEdgeAttrName().length() == 0;
		if (isBinary) b.add(parameters.getGeneticEdgeAttrName()+"  (binary)");
		else b.add("Edge score: "+parameters.getGeneticEdgeAttrName()+"  (numeric, scaling="+parameters.getGeneticScalingMethod()+")");
		b.add("Nodes: "+gnetNodes+", Edges: "+gnetEdges+"");
		b.add("");
		
		if (parameters.getComplexTrainingPhysical() || parameters.getComplexTrainingGenetic() || parameters.getComplexAnnotation())
		{
			b.add("Annotation: "+parameters.getAnnotationAttrName()+"  ("+trainingComplexes.size()+" entries)");
			b.add("");
			
			double background = (parameters.getComplexTrainingPhysical() || parameters.getComplexTrainingGenetic()) ? getBackground(trainingComplexes,parameters.getGeneticNetwork(),parameters.getPhysicalNetwork()) : Double.NaN;
			
			if (parameters.getComplexTrainingPhysical())
			{
				b.add("Physical interaction annotation enrichment");
				double[][] curve = getCurve(physicalRegress.x,physicalRegress.y,30,background);
				
				double xmin = DoubleVector.min(curve[0]);
				double xmax = DoubleVector.max(curve[0]);

				double ymin = -2;
				double ymax = 3;
				
				float[] x = new FloatVector(DoubleVector.divideBy(DoubleVector.subtract(curve[0],xmin),(xmax-xmin)/100)).getData();
				float[] y = new FloatVector(DoubleVector.divideBy(DoubleVector.subtract(curve[1],ymin),(ymax-ymin)/100)).getData();
				
				double deltax = xmax-xmin;
				xmin -= .02*deltax;
				xmax += .02*deltax;
				
				b.add("<IMG src=\"http://chart.apis.google.com/chart?cht=lxy&chs=500x300&chd=t:"+ListOps.collectionToString(x,",")+"|"+ListOps.collectionToString(y,",")+"&chxr=0,"+xmin+","+xmax+"&chxt=x,x,y,y&chxl=1:|Interaction_Score|2:|10^-2|10^-1|10^0|10^1|10^2|10^3|3:|Enrichment&chxp=1,60|3,50&chco=0000FF&chxs=0,000000,12,0,lt|1,000000,12,1,lt|2,000000,12,2,lt|3,000000,12,3,lt&chg=0,100,3,3,0,40\">");
				b.add("");
				b.add("Assumed null score = 0, trained on absolute value");
				b.add("Background = "+geneticRegress.background);
				b.add("Absent hits = "+geneticRegress.absentHits+",   Absent misses = "+geneticRegress.absentMisses);
				b.add("");
				b.add("Logistic regression: beta="+physicalRegress.coef+", intercept="+physicalRegress.intercept);
				b.add("");
			}
			
			if (parameters.getComplexTrainingGenetic())
			{
				b.add("Genetic interaction annotation enrichment");
				double[][] curve = getCurve(geneticRegress.x,geneticRegress.y,30,background);
				
				double xmin = DoubleVector.min(curve[0]);
				double xmax = DoubleVector.max(curve[0]);
				
				double ymin = -2;
				double ymax = 3;
				
				float[] x = new FloatVector(DoubleVector.divideBy(DoubleVector.subtract(curve[0],xmin),(xmax-xmin)/100)).getData();
				float[] y = new FloatVector(DoubleVector.divideBy(DoubleVector.subtract(curve[1],ymin),(ymax-ymin)/100)).getData();
				
				double deltax = xmax-xmin;
				xmin -= .02*deltax;
				xmax += .02*deltax;
				
				b.add("<IMG src=\"http://chart.apis.google.com/chart?cht=lxy&chs=500x300&chd=t:"+ListOps.collectionToString(x,",")+"|"+ListOps.collectionToString(y,",")+"&chxr=0,"+xmin+","+xmax+"&chxt=x,x,y,y&chxl=1:|Interaction_Score|2:|10^-2|10^-1|10^0|10^1|10^2|10^3|3:|Enrichment&chxp=1,60|3,50&chco=0000FF&chxs=0,000000,12,0,lt|1,000000,12,1,lt|2,000000,12,2,lt|3,000000,12,3,lt&chg=0,100,3,3,0,40\">");
				b.add("");
				b.add("Assumed null score = 0, trained on absolute value");
				b.add("Background = "+geneticRegress.background);
				b.add("Absent hits = "+geneticRegress.absentHits+",   Absent misses = "+geneticRegress.absentMisses);
				b.add("");
				b.add("Logistic regression: beta="+geneticRegress.coef+", intercept="+geneticRegress.intercept);
				b.add("");
			}
			
			if (annotMatches!=-1)
			{
				b.add("Annotation labeling:");
				b.add("Threshold="+parameters.getAnnotationThreshold());
				b.add("Modules annotated: "+annotMatches+" / "+results.numNodes());
				b.add("");
			}
		}
		
		
		b.add("Search parameters: alpha="+parameters.getAlpha()+",  alphaMultiplier="+parameters.getAlphaMultiplier());
		b.add("Edge filtering: pval="+parameters.getPValueThreshold()+",  samples="+parameters.getNumberOfSamples());
		b.add("");
		b.add("PanGIA overview network: (Nodes: "+results.numNodes()+", Edges: "+results.numEdges()+")");
		
		double sum = 0;
		for (TypedLinkEdge<TypedLinkNodeModule<String, BFEdge>, BFEdge> edge : results.edgeIterator())
			sum += edge.value().link();
		sum /= results.numEdges();
		
		b.add("Average edge score: "+sum);
		
		
		report.addToBody(b);
		
		report.write(path);
	}
	
	private static double getBackground(List<SNodeModule> annots, CyNetwork geneticNetwork, CyNetwork physicalNetwork)
	{
		Set<String> nodes = new HashSet<String>(20000);
		for (SNodeModule m : annots)
			nodes.addAll(m.getMemberData());

		for( CyNode node : geneticNetwork.getNodeList() )
		{
			CyRow nodeRow = geneticNetwork.getRow(node);
			String nodeName = nodeRow.get("name", String.class);
			nodes.add(nodeName);
		}

		for( CyNode node : physicalNetwork.getNodeList() )
		{
			CyRow nodeRow = physicalNetwork.getRow(node);
			String nodeName = nodeRow.get("name", String.class);
			nodes.add(nodeName);
		}
		
		int possible = nodes.size()*(nodes.size()-1)/2;
		
		Set<UndirectedSEdge> net = new HashSet<UndirectedSEdge>(possible/100);
		for (SNodeModule m : annots)
			for (String g1 : m)
				for (String g2 : m)
					if (!g1.equals(g2)) net.add(new UndirectedSEdge(g1, g2));
		
		return net.size() / (double) possible;
	}
	
	private static double[][] getCurve(double[] x, double[] y, int n, double background)
	{
		int[] order = DoubleVector.sort_I(x);
		x = DoubleVector.get(x, order);
		y = DoubleVector.get(y, order);
		
		int stepSize = x.length/(n-1);
		
		double[][] out = new double[2][n];
		
		for (int i=0;i<n-1;i++)
		{
			out[0][i] = getX(x,i*stepSize,(i+1)*stepSize);
			out[1][i] = getY(y,i*stepSize,(i+1)*stepSize,background);
		}
		
		out[0][n-1] = getX(x,x.length-stepSize,x.length);
		out[1][n-1] = getY(y,x.length-stepSize,x.length,background);
		
		int[] ok = new BooleanVector(DoubleVector.isNaN(out[1])).not().asIndexes().getData();
		out[0] = DoubleVector.get(out[0], ok);
		out[1] = DoubleVector.get(out[1], ok);
		
		return out;
	}
	
	private static double getX(double[] x, int i1, int i2)
	{
		double sum = 0;
		for (int i=i1;i<i2;i++)
			sum += x[i];
		
		return sum/(i2-i1);
	}
	
	private static double getY(double[] y, int i1, int i2, double background)
	{
		int hits = 0;
		for (int i=i1;i<i2;i++)
			hits += (int)Math.round(y[i]);
		
		return (hits==0) ? Double.NaN : Math.log10( (hits/(double)(i2-i1)) / background ); 
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception
	{
		searchPropertyPanel.setSearchRunning(true);
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setStatusMessage("Executing PanGIA task...");

		startTime = System.nanoTime();
		taskMonitor.setProgress(0.01);
		taskMonitor.setStatusMessage("Searching for modules...");

		if (cancelled) return;

		final CyNetwork physicalInputNetwork = parameters.getPhysicalNetwork();
		SFNetwork physicalNetwork = convertCyNetworkToSFNetwork(physicalInputNetwork, parameters.getNodeAttrName(), parameters.getPhysicalEdgeAttrName(), parameters.getPhysicalScalingMethod());

		final CyNetwork geneticInputNetwork = parameters.getGeneticNetwork();
		SFNetwork geneticNetwork = convertCyNetworkToSFNetwork(geneticInputNetwork, parameters.getNodeAttrName(), parameters.getGeneticEdgeAttrName(), parameters.getGeneticScalingMethod());


		boolean isGNetSigned = false;
		for (SFEdge e : geneticNetwork.edgeIterator())
			if (e.value()<0 )
			{
				isGNetSigned = true;
				break;
			}

		//TODO: Find out if there is a best practice for this:
		PanGIA.isGNetSigned = isGNetSigned;

		System.out.println("Signed: "+isGNetSigned);

		if (cancelled) return;


		//Load trainingComplexes
		List<SNodeModule> trainingComplexes = null;
		if (parameters.getComplexTrainingPhysical() || parameters.getComplexTrainingGenetic() || parameters.getComplexAnnotation())
		{
			Map<String,Set<String>> annotationNode = new HashMap<String,Set<String>>(1000);

		    for( CyNode node : geneticInputNetwork.getNodeList() )
			{
				CyRow row = geneticInputNetwork.getRow(node);
				List annotationNameList = row.get(parameters.getAnnotationAttrName(), List.class);
				int x = 5 + 1;
				for( Object o : annotationNameList )
				{
					String annotationName = null;
					if( o instanceof String )
						annotationName = (String)o;
					else
						break;
					String nodeNameList = row.get(parameters.getNodeAttrName(), String.class);
					HashMapUtil.updateMapSet(annotationNode, annotationName, String.valueOf(nodeNameList) );
				}
			}
			trainingComplexes = new ArrayList<SNodeModule>(annotationNode.size());

			for (String annotation : annotationNode.keySet())
				trainingComplexes.add(new SNodeModule(annotation,annotationNode.get(annotation)));
		}

		if (cancelled) return;

		//Perform training
		ComplexRegressionResult physicalRegress=null;
		ComplexRegressionResult geneticRegress=null;

		if (parameters.getComplexTrainingPhysical())
		{
			try
			{
				physicalRegress = ComplexRegression.complexRegress(physicalNetwork, trainingComplexes, true, 0);
			}catch (AssertionError e)
			{
				JOptionPane.showMessageDialog(null, "A problem occured during the training step. Make sure the physical network partially overlaps with the training set and has the same node naming convention.");
				cancel();
				return;
			}
			physicalNetwork = physicalRegress.net;
		}

		if (parameters.getComplexTrainingGenetic())
		{
			try
			{
				geneticRegress = ComplexRegression.complexRegress(geneticNetwork, trainingComplexes, true, 0);
			}catch (AssertionError e)
			{
				JOptionPane.showMessageDialog(null, "A problem occured during the training step. Make sure the genetic network partially overlaps with the training set and has the same node naming convention.");
				cancel();
				return;
			}
			geneticNetwork = geneticRegress.net;
		}



		if (cancelled) return;

		//Apply the degree filter
		int pnetNodes1 = physicalNetwork.numNodes();
		int pnetEdges1 = physicalNetwork.numEdges();


		int degreeFilter = parameters.getPhysicalNetworkFilterDegree();
		if (degreeFilter!=-1)
		{
			System.out.println("Applying degree filter = "+degreeFilter);
			TypedLinkNetwork<String,Float> ptlnet = physicalNetwork.asTypedLinkNetwork();
			Set<String> gnodes = geneticNetwork.getNodes();
			physicalNetwork = new FloatHashNetwork(ptlnet.subNetwork(gnodes, degreeFilter));
		}

		int pnetNodes2 = physicalNetwork.numNodes();
		int pnetEdges2 = physicalNetwork.numEdges();

		if (cancelled) return;

		//Check for problems
		if (physicalNetwork.numEdges()==0)
		{
			JOptionPane.showMessageDialog(null, "No edges were found in the physical network. Please verify that the network has edges, that the edge attribute is appropriate, and that the Network filter degree is not too low.");
			throw new IllegalArgumentException();
		}else if (geneticNetwork.numEdges()==0)
		{
			JOptionPane.showMessageDialog(null, "No edges were found in the genetic network. Please verify that the network has edges and that the edge attribute is appropriate.");
			throw new IllegalArgumentException();
		}

		System.out.println("Number of edges: "+physicalNetwork.numEdges()+", "+geneticNetwork.numEdges());

		if (cancelled) return;


		//Initialize the scoring function
		final HCScoringFunction hcScoringFunction =
				new SouravScore(physicalNetwork, geneticNetwork,
						(float)parameters.getAlpha(),
						(float)parameters.getAlphaMultiplier());
		hcScoringFunction.Initialize(physicalNetwork, geneticNetwork);

		if (cancelled) return;

		//Run the clustering algorithm
		final TypedLinkNetwork<TypedLinkNodeModule<String, BFEdge>, BFEdge> results = HCSearch2.search(physicalNetwork, geneticNetwork, hcScoringFunction, taskMonitor, SEARCH_PERCENTAGE, this);

		if (cancelled) return;

		//Compute significance and filter edges
		final double pValueThreshold;
		if (!Double.isNaN(parameters.getPValueThreshold()))
		{
			pValueThreshold = parameters.getPValueThreshold();
			final int numberOfSamples = parameters.getNumberOfSamples();
			computeSig(results, geneticNetwork, pValueThreshold, numberOfSamples, taskMonitor, SEARCH_PERCENTAGE, COMPUTE_SIG_PERCENTAGE);
		}else
		{
			for (TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge> edge : results.edgeIterator())
				edge.value().setLinkMerge(.5f);
			pValueThreshold = 1;
		}

		//Filter edges with negative PanGIA edge scores
		final Set<TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge>> deleteSet = new HashSet<TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge>>(1000);
		for (TypedLinkEdge<TypedLinkNodeModule<String,BFEdge>,BFEdge> edge : results.edgeIterator())
		{
			if (!edge.value().isType(InteractionType.Genetic))
			{
				deleteSet.add(edge);
				continue;
			}

			if (edge.value().link() < 0) deleteSet.add(edge);
		}
		results.removeAllEdgesWNodeUpdate(deleteSet);

		if (cancelled) return;

		//Check for problems
		boolean groupedOnce = false;
		Set<TypedLinkNode<TypedLinkNodeModule<String, BFEdge>,BFEdge>> goodNodes = new HashSet<TypedLinkNode<TypedLinkNodeModule<String, BFEdge>,BFEdge>>(1000);
		for (final TypedLinkEdge<TypedLinkNodeModule<String, BFEdge>, BFEdge> edge : results.edges())
		{
			float pval = edge.value().linkMerge();
			if (pval<=pValueThreshold)
			{
				goodNodes.add(edge.source());
				goodNodes.add(edge.target());
			}

			if (edge.source().value().size()>1 || edge.target().value().size()>1)
				groupedOnce = true;
		}

		if (goodNodes.size()==0)
		{
			JOptionPane.showMessageDialog(null, "PanGIA was not able to identify any modules. Either all of the nodes were grouped into a single module, or no edge passed the filter. Please verify that:\n1. Edge scores are appropriate.\n2. Edge reporting is not set too low.\n3. Module size is not too high.");
			this.cancel();
		}else if (!groupedOnce)
		{
			JOptionPane.showMessageDialog(null, "PanGIA was not able to merge nodes into modules. Please verify that:\n1. Edge scores are appropriate.\n2. Module size is not too low.");
			this.cancel();
		}
		else if (goodNodes.size()>=500)
		{
			Object[] options = {"Yes","No"};
			int a = JOptionPane.showOptionDialog(null, "PanGIA found "+goodNodes.size()+" modules. This may take considerable resources to render. Do you wish to continue?", "PanGIA results", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (a==1) this.cancel();
		}

		if (cancelled) return;

		//Annotate complexes
		Map<TypedLinkNodeModule<String, BFEdge>,String> module_name = null;
		System.out.println("PARAM: "+parameters.getComplexTrainingPhysical()+", "+parameters.getComplexTrainingPhysical()+", "+parameters.getComplexAnnotation()+", "+parameters.getAnnotationThreshold());

		int annotMatches = -1;

		if (parameters.getComplexAnnotation())
		{
			module_name = new HashMap<TypedLinkNodeModule<String, BFEdge>,String>(results.numNodes(),1);

			for (SNodeModule complex : trainingComplexes)
			{
				double bestScore = 0;
				TypedLinkNodeModule<String, BFEdge> bestNode = null;

				for (TypedLinkNode<TypedLinkNodeModule<String, BFEdge>,BFEdge> n : results.nodeIterator())
				{
					double jaccard = SetUtil.jaccard(complex.getMemberData(), n.value().getMemberValues());
					if (jaccard>bestScore)
					{
						bestScore = jaccard;
						bestNode = n.value();
					}
				}

				if (bestNode!=null && bestScore>=parameters.getAnnotationThreshold()) module_name.put(bestNode, complex.getID());
			}
			annotMatches = module_name.size();
			System.out.println("Number of module annotation matches: "+annotMatches);
		}

		if (cancelled) return;

		final TypedLinkNetwork<String, Float> pNet = physicalNetwork.asTypedLinkNetwork();
		final TypedLinkNetwork<String, Float> gNet = geneticNetwork.asTypedLinkNetwork();

		String networkName = "Module Overview Network";
		final NestedNetworkCreator nnCreator = new NestedNetworkCreator(results, physicalInputNetwork, geneticInputNetwork, pNet, gNet, pValueThreshold, 100.0f - COMPUTE_SIG_PERCENTAGE, module_name, networkName,isGNetSigned, parameters.getNodeAttrName(), parameters.getGeneticEdgeAttrName(), parameters);


		nnCreator.run(taskMonitor);
		if (!parameters.getReportPath().equals(""))
			generateReport(parameters.getReportPath(), networkName, pnetNodes1, pnetNodes2, pnetEdges1, pnetEdges2, geneticNetwork.numNodes(), geneticNetwork.numEdges(), trainingComplexes, physicalRegress, geneticRegress, annotMatches, results);
		setPercentCompleted(100);
		//Now that the task is complete, report the results in a popup window and on the console.
		String status = "Search finished!\n\n" + "Number of modules = " + nnCreator.getOverviewNetwork().getNodeCount() + "\n\n" + HCSearch2.report(results);
		DecimalFormat df = new DecimalFormat("0.00");
		double executionTime = (System.nanoTime()-startTime)/1e9;
		status += "\nExecution time: "+df.format(executionTime)+" seconds";
		//finalStatus exists simply so the variable can be accessed by the anonymous inner class that follows.
		final String finalStatus = status;
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Window parent = ServicesUtil.cySwingApplicationServiceRef.getJFrame();
				JOptionPane.showMessageDialog(parent, finalStatus, "PanGIA", JOptionPane.INFORMATION_MESSAGE);

			}
		});
		System.out.println(status);
		CyNetwork overviewNetwork = nnCreator.getOverviewNetwork();
		String overviewNetworkName = overviewNetwork.getRow(overviewNetwork).get("name", String.class);
		PanGIAPlugin.output.put(overviewNetworkName,new PanGIAOutput(nnCreator.getOverviewNetwork(), physicalInputNetwork, geneticInputNetwork,parameters.getNodeAttrName(),parameters.getPhysicalEdgeAttrName(),parameters.getGeneticEdgeAttrName(),isGNetSigned));
		searchPropertyPanel.setSearchRunning(false);
	}

	public boolean wasCancelled()
	{
		return cancelled;
	}

}
