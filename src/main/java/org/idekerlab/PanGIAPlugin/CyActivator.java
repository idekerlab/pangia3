package org.idekerlab.PanGIAPlugin;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskManager;
import org.idekerlab.PanGIAPlugin.ui.SearchPropertyPanel;
import org.osgi.framework.BundleContext;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

//import org.cytoscape.task.creation.LoadVisualStylesFromFileFactory;

public class CyActivator extends AbstractCyActivator
{
	public CyActivator()
	{
		super();
	}


	public void start(BundleContext bc)
	{
		System.out.println("Loading PanGIA...");

		CySwingApplication cySwingApplicationServiceRef = getService(bc, CySwingApplication.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		TaskManager taskManagerServiceRef = getService(bc, TaskManager.class);

		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		VisualMappingManager visualMappingManagerRef = getService(bc, VisualMappingManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);

		CyRootNetworkManager cyRootNetworkFactory = getService(bc, CyRootNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);

		//DW: Added, April 4, 2014
		LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);

		//DW: Added, April 9, 2014
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		ServicesUtil.cySwingApplicationServiceRef = cySwingApplicationServiceRef;
		ServicesUtil.cyApplicationManagerServiceRef = cyApplicationManagerServiceRef;
		ServicesUtil.cyNetworkViewManagerServiceRef = cyNetworkViewManagerServiceRef;
		ServicesUtil.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		ServicesUtil.cyServiceRegistrarServiceRef = cyServiceRegistrarServiceRef;
		ServicesUtil.cyEventHelperServiceRef = cyEventHelperServiceRef;
		ServicesUtil.taskManagerServiceRef = taskManagerServiceRef;
		ServicesUtil.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		ServicesUtil.visualMappingManagerRef = visualMappingManagerRef;
		ServicesUtil.cyNetworkFactoryServiceRef = cyNetworkFactoryServiceRef;
		ServicesUtil.cyRootNetworkFactory = cyRootNetworkFactory;
		ServicesUtil.cyNetworkViewFactoryServiceRef = cyNetworkViewFactoryServiceRef;
		ServicesUtil.cyLayoutsServiceRef = cyLayoutsServiceRef;
		//DW: Added, April 4, 2014
		ServicesUtil.loadVizmapFileTaskFactory = loadVizmapFileTaskFactory;
		ServicesUtil.registrar = registrar;

		SearchPropertyPanel searchPanel = new SearchPropertyPanel();
		PanGIACytoPanelComponent panGIACytoPanelComponent = new PanGIACytoPanelComponent(searchPanel);
		PanGIAPlugin panGIAPlugin = new PanGIAPlugin(searchPanel);

		registerService(bc, panGIACytoPanelComponent, CytoPanelComponent.class, new Properties());
		registerAllServices(bc, searchPanel, new Properties());
		registerAllServices(bc, panGIAPlugin, new Properties());

		PanGIANodeViewContextMenuFactory panGIANodeViewContextMenuFactory = new PanGIANodeViewContextMenuFactory();
		Properties panGIANodeViewContextMenuFactoryProps = new Properties();
		panGIANodeViewContextMenuFactoryProps.setProperty("preferredTaskManager", "menu");
		panGIANodeViewContextMenuFactoryProps.setProperty(PREFERRED_MENU, NODE_APPS_MENU);
		panGIANodeViewContextMenuFactoryProps.setProperty(MENU_GRAVITY, "10.0");
		panGIANodeViewContextMenuFactoryProps.setProperty(TITLE, "PanGIA");
		registerAllServices(bc, panGIANodeViewContextMenuFactory, panGIANodeViewContextMenuFactoryProps);

		PanGIAEdgeViewContextMenuFactory panGIAEdgeViewContextMenuFactory = new PanGIAEdgeViewContextMenuFactory();
		Properties panGIAEdgeViewContextMenuFactoryProps = new Properties();
		panGIAEdgeViewContextMenuFactoryProps.setProperty("preferredTaskManager", "menu");
		panGIAEdgeViewContextMenuFactoryProps.setProperty(PREFERRED_MENU, NODE_APPS_MENU);
		panGIAEdgeViewContextMenuFactoryProps.setProperty(MENU_GRAVITY, "10.0");
		panGIAEdgeViewContextMenuFactoryProps.setProperty(TITLE, "PanGIA");
		registerAllServices(bc, panGIAEdgeViewContextMenuFactory, panGIAEdgeViewContextMenuFactoryProps);

		PanGIANetworkViewContextMenuFactory panGIANetworkViewContextMenuFactory = new PanGIANetworkViewContextMenuFactory();
		Properties panGIANetworkViewContextMenuFactoryProps = new Properties();
		panGIANetworkViewContextMenuFactoryProps.setProperty("preferredTaskManager", "menu");
		panGIANetworkViewContextMenuFactoryProps.setProperty(PREFERRED_MENU, NODE_APPS_MENU);
		panGIANetworkViewContextMenuFactoryProps.setProperty(MENU_GRAVITY, "10.0");
		panGIANetworkViewContextMenuFactoryProps.setProperty(TITLE, "PanGIA");
		registerAllServices(bc, panGIANetworkViewContextMenuFactory, panGIANetworkViewContextMenuFactoryProps);
	}
}

