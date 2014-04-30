package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.swing.CyMenuItem;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.idekerlab.PanGIAPlugin.utilities.files.FileUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by David Welker on 4/17/14
 * Copyright © 2014. All rights reserved.
 */
public class PanGIAContextMenuFactory
{


	protected CyMenuItem createMenuItemHelper(CyNetworkView netView, View<CyNode> nodeView)
	{
		boolean selectedHasNested = false;
		CyNetwork network = netView.getModel();
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, "selected", true);
		if( nodeView != null )
		{
			CyNode clickedNode = nodeView.getModel();
			if( !selectedNodes.contains(clickedNode) )
				selectedNodes.add( clickedNode );
		}
		for( CyNode node : selectedNodes )
		{
			if( node.getNetworkPointer() != null )
				selectedHasNested = true;
		}
		final List<CyNode> finalSelectedNodes = selectedNodes;

		String networkType = network.getRow(network).get(PanGIA.NETWORK_TYPE_ATTRIBUTE_NAME, String.class);
		boolean isOverviewNetwork = networkType != null && networkType.equals(NetworkType.OVERVIEW.name());

		JMenu mainMenu = new JMenu("PanGIA");
		CyMenuItem cyMenuItem = new CyMenuItem(mainMenu, 0);
		//ITEM1
		final CyNetworkView finalNetView = netView;
		final View<CyNode> finalNodeView = nodeView;
		if (selectedHasNested && isOverviewNetwork && selectedNodes.size() > 0)
		{
			JMenuItem menuItem = new JMenuItem();
			menuItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					DetailedNetworkCreator.createDetailedView(finalNetView, finalNodeView);
				}
			});
			menuItem.setText("Create Detailed View");
			mainMenu.add(menuItem);
		}

		//ITEM2
		if (isOverviewNetwork)
		{
			JMenuItem item2 = new JMenuItem();
			item2.setText("Export Modules to Tab-Delimited File");
			item2.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) {
					saveModules(finalNetView);
				}
			});
			mainMenu.add(item2);
		}

		//ITEM3
		if (isOverviewNetwork)
		{
			JMenuItem item3 = new JMenuItem();
			item3.setText("Export Module Map to Tab-Delimited File");
			item3.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e) {
					saveOverviewNetwork(finalNetView);
				}
			});
			mainMenu.add(item3);
		}

		//ITEM4
		if (isOverviewNetwork && selectedNodes.size() > 0)
		{
			JMenu item1 = new JMenu();
			item1.setText("Save Selected Nodes to Matrix File");

			// Get the model from the view and use it to retrieve the network ID.
			CyNetwork model = netView.getModel();
			String netID = model.getRow(model).get("name", String.class);

			// Use the network ID to retrive the appropriate PanGIAOutput instance.
			final PanGIAOutput output = PanGIAPlugin.output.get(netID);

			//String[] ean = edgeAttr.getAttributeNames();

			String[] ean = new String[]{output.getPhysEdgeAttrName(),output.getGenEdgeAttrName()};

			List<String> eaNames = new ArrayList<String>(ean.length);
			Collections.addAll(eaNames, ean);

			eaNames.removeAll(NestedNetworkCreator.getEdgeAttributeNames());
			eaNames.remove(NestedNetworkCreator.REFERENCE_NETWORK_NAME_ATTRIB);

			for (final String ea : eaNames)
			{
				JMenuItem eaItem = new JMenuItem();
				eaItem.setText(ea);

				eaItem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						JFileChooser jfc = new JFileChooser();
						jfc.setCurrentDirectory(new File("."));
						int returnVal = jfc.showSaveDialog(ServicesUtil.cySwingApplicationServiceRef.getJFrame());

						if (returnVal==JFileChooser.APPROVE_OPTION)
						{
							if( ea.equals( output.getPhysEdgeAttrName()) )
								saveNodesToMatrix(finalNetView, jfc.getSelectedFile(), ea,finalSelectedNodes, output.getOrigPhysNetwork());
							else if( ea.equals( output.getGenEdgeAttrName() ) )
								saveNodesToMatrix(finalNetView, jfc.getSelectedFile(), ea,finalSelectedNodes, output.getOrigGenNetwork());
						}
					}
				});
				item1.add(eaItem);
			}
			mainMenu.add(item1);
		}

		return cyMenuItem;
	}


	protected static void saveNodesToMatrix(final CyNetworkView view, File file, String eattr, List<CyNode> selectedNodes, CyNetwork originNetwork)
	{
		CyNetwork network = view.getModel();
		String netID = network.getRow(network).get("name", String.class);

		// Use the network ID to retrive the appropriate PanGIAOutput instance.
		PanGIAOutput output = PanGIAPlugin.output.get(netID);

		List<CyNode> choiceNodes = new ArrayList<CyNode>();
		List<String> ids = new ArrayList<String>();
		HashMap<CyNode, String> selectedNodeNameMap = new HashMap<CyNode, String>();
		for( CyNode node : selectedNodes )
		{
			if( node.getNetworkPointer() == null )
			{
				choiceNodes.add(node);
				String id = network.getRow(node).get(output.getNodeAttrName(), String.class);
				ids.add(id);
				selectedNodeNameMap.put(node, id);
			}
			else
			{
				CyNetwork nestedNetwork = node.getNetworkPointer();
				for( CyNode nestedNode : nestedNetwork.getNodeList() )
				{
					choiceNodes.add(nestedNode);
					String id = nestedNetwork.getRow(nestedNode).get(output.getNodeAttrName(), String.class);
					ids.add(id);
					selectedNodeNameMap.put(nestedNode, id);
				}
			}
		}

		HashMap<String, CyNode> originNameNodeMap = new HashMap<String, CyNode>();
		for (CyNode node : originNetwork.getNodeList())
		{
			String name = originNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			originNameNodeMap.put(name, node);
		}

		double[][] m = new double[choiceNodes.size()][];

		for (int i=0;i<choiceNodes.size();i++)
		{
			int jcount = i+1;
			m[i] = new double[jcount];

			CyNode iNode = choiceNodes.get(i);
			String iNodeName = selectedNodeNameMap.get(iNode);
			CyNode firstNode = originNameNodeMap.get(iNodeName);

			for (int j=0;j<jcount;j++)
			{
				m[i][j] = Double.NaN;

				CyNode jNode = choiceNodes.get(j);
				String jNodeName = selectedNodeNameMap.get(jNode);
				CyNode secondNode = originNameNodeMap.get(jNodeName);

				List<CyEdge> edges = originNetwork.getConnectingEdgeList(firstNode, secondNode, CyEdge.Type.ANY);

				for( CyEdge edge : edges )
				{
					Double d = originNetwork.getRow(edge).get(eattr, Double.class);
					if( d != null )
					{
						m[i][j] = d;
						break;
					}
				}
			}
		}

		BufferedWriter bw = FileUtil.getBufferedWriter(file.getAbsolutePath(), false);

		try
		{
			bw.write("Gene");

			for (String id : ids)
				bw.write("\t"+id);

			bw.write("\n");

			for (int i=0;i<m.length;i++)
			{
				bw.write(ids.get(i));
				for (int j=0;j<=i;j++)
					bw.write("\t"+m[i][j]);

				for (int i2=i+1;i2<m.length;i2++)
					bw.write("\t"+m[i2][i]);

				bw.write("\n");
			}

			bw.close();

			JOptionPane.showMessageDialog(null, "Matrix saved successfully.");

		}catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was a problem saving the matrix: "+e.getMessage());
		}
	}

	protected static void saveModules(CyNetworkView view)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("."));
		int returnVal = jfc.showSaveDialog(ServicesUtil.cySwingApplicationServiceRef.getJFrame());
		if( returnVal != JFileChooser.APPROVE_OPTION )
			return;

		String col3 = NestedNetworkCreator.EDGE_SCORE;
		String col4 = NestedNetworkCreator.EDGE_PVALUE;
		String col5 = NestedNetworkCreator.GENETIC_EDGE_COUNT;
		String col6 = NestedNetworkCreator.PHYSICAL_EDGE_COUNT;
		String col7 = NestedNetworkCreator.EDGE_SOURCE_SIZE;
		String col8 = NestedNetworkCreator.EDGE_TARGET_SIZE;
		String col9 = NestedNetworkCreator.EDGE_GENETIC_DENSITY;

		String[] remainingColumns = {col3, col4, col5, col6, col7, col8, col9};

		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter( new BufferedWriter( new FileWriter(jfc.getSelectedFile()) ) );
			String line = "NodeA\tNodeB\t";
			for( int i = 0; i < remainingColumns.length - 1; i++ )
				line += remainingColumns[i] + "\t";
			line += remainingColumns[remainingColumns.length-1];
			pw.println(line);

			CyNetwork overviewNetwork = view.getModel();
			List<CyEdge> edges = overviewNetwork.getEdgeList();
			for( CyEdge edge : edges )
			{
				String name = overviewNetwork.getRow(edge).get(CyNetwork.NAME, String.class);
				line = name.replace(" (" + NestedNetworkCreator.COMPLEX_INTERACTION_TYPE + ") ", "\t");
				line += "\t";
				for( int i = 0; i < remainingColumns.length - 1; i++ )
				{
					String col = remainingColumns[i];
					Object value = overviewNetwork.getRow(edge).get(col, Object.class);
					line += value.toString() + "\t";
				}
				String lastCol = remainingColumns[remainingColumns.length - 1];
				Object lastValue = overviewNetwork.getRow(edge).get(lastCol, Object.class);
				line += lastValue.toString();
				pw.println(line);
			}

			JOptionPane.showMessageDialog(null, "Modules saved successfully.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was a problem saving the modules: "+e.getMessage());
		}
		finally
		{
			if( pw != null )
				pw.close();
		}
	}

	protected static void saveOverviewNetwork(CyNetworkView view)
	{
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File("."));
		int returnVal = jfc.showSaveDialog(ServicesUtil.cySwingApplicationServiceRef.getJFrame());

		if( returnVal != JFileChooser.APPROVE_OPTION )
			return;

		PrintWriter pw = null;
		try
		{
			pw = new PrintWriter( new BufferedWriter( new FileWriter(jfc.getSelectedFile()) ) );
			CyNetwork overviewNetwork = view.getModel();
			for( CyNode node : overviewNetwork.getNodeList() )
			{
				String line = overviewNetwork.getRow(node).get(CyNetwork.NAME, String.class) + "\t";
				CyNetwork nestedNetwork = node.getNetworkPointer();
				if( nestedNetwork != null )
				{
					List<CyNode> nestedNodes = nestedNetwork.getNodeList();
					for( int i = 0; i < nestedNodes.size() - 1; i++)
					{
						CyNode nestedNode = nestedNodes.get(i);
						line += nestedNetwork.getRow(nestedNode).get(CyNetwork.NAME, String.class) + "|";
					}
					CyNode lastNode = nestedNodes.get(nestedNodes.size() - 1);
					line += nestedNetwork.getRow(lastNode).get(CyNetwork.NAME, String.class);
				}
				pw.println(line);
			}
			JOptionPane.showMessageDialog(null, "Overview network saved successfully.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "There was a problem saving the overview network: "+e.getMessage());
		}
		finally
		{
			if( pw != null )
				pw.close();
		}
	}
}
