package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskManager;

import java.util.Properties;

public class ServicesUtil
{
	public static CySwingApplication cySwingApplicationServiceRef;
	public static CyApplicationManager cyApplicationManagerServiceRef;
	public static CyNetworkViewManager cyNetworkViewManagerServiceRef;
	public static CyNetworkManager cyNetworkManagerServiceRef;
	public static CyServiceRegistrar cyServiceRegistrarServiceRef;
	public static TaskManager taskManagerServiceRef;
	public static CyProperty<Properties> cytoscapePropertiesServiceRef;
	public static VisualMappingManager visualMappingManagerRef;
	public static CyNetworkFactory cyNetworkFactoryServiceRef;
	public static CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	public static CyLayoutAlgorithmManager cyLayoutsServiceRef;
	//DW: Added April 4, 2014
	public static LoadVizmapFileTaskFactory loadVizmapFileTaskFactory;
	public static CyServiceRegistrar registrar;
	public static CyRootNetworkManager cyNetworkRootManagerServiceRef;
}
