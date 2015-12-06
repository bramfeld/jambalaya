/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Action;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewListener.QuickViewEvent;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.ShrimpActionListener;
import ca.uvic.csr.shrimp.util.SortedProperties;


/**
 * This class holds all the quick views.  It also provides methods for saving and loading
 * quick views from the project properties file.
 * It also works with the project action manager to keep the quick view actions in sync.
 *
 * @author Chris Callendar
 * @date 1-Aug-06
 */
public class QuickViewManager implements ShrimpActionListener {

	// keys for saving quick views to a properties file
	private static final String PROPS_QUICKVIEWS_COUNT	= "quickviews.count";
	private static final String PROPS_QUICKVIEW 		= "quickview.";
	private static final String PROPS_NAME 				= ".name";
	private static final String PROPS_CLASS 			= ".class";
	private static final String PROPS_ICON 				= ".icon";
	private static final String PROPS_LABEL 			= ".label";
	private static final String PROPS_LAYOUT 			= ".layout";
	private static final String PROPS_NODES 			= ".nodes.count";
	private static final String PROPS_ALL_NODES 		= ".nodes.all";
	private static final String PROPS_NODE				= ".node.";
	private static final String PROPS_ARCS 				= ".arc.count";
	private static final String PROPS_ALL_ARCS 			= ".arcs.all";
	private static final String PROPS_ARC 				= ".arc.";
	private static final String PROPS_CPRELS_COUNT 		= ".cprels.count";
	private static final String PROPS_CPRELS_INVERTED 	= ".cprels.inverted";
	private static final String PROPS_CPREL 			= ".cprel.";
	private static final String PROPS_COMPOSITE_NODES_COUNT = ".composite.nodes.count";
	private static final String PROPS_COMPOSITE_NODE 	= ".composite.node.";
	private static final String PROPS_GROUPS 			= ".composite.groups.count";
	private static final String PROPS_GROUP 			= ".composite.group.";
	private static final String PROPS_GROUP_ARCS 		= ".arcs.count";
	private static final String PROPS_GROUP_ARC 		= ".arc.";
	private static final String PROPS_DISPLAY	 		= ".display";

	// quick view actions
	private Collection quickViews;
	private ShrimpProject project;
	protected File propertiesFile;

//	protected Collection allNodes;
//	protected Collection allArcs;

	private List listeners;
	private boolean canFire;
	private List addedActions;
	private List removedActions;
	private List changedActions;


	/**
	 * Initializes the manager.
	 */
	public QuickViewManager(ShrimpProject project) {
		this.project = project;
		this.quickViews = new ArrayList();
		this.listeners = new ArrayList(2);
		this.propertiesFile = null;
		this.canFire = true;
		this.addedActions = new ArrayList();
		this.removedActions = new ArrayList();
		this.changedActions = new ArrayList();
	}

	public void setFiring(boolean canFire) {
		if (this.canFire != canFire) {
			this.canFire = canFire;
			if (this.canFire && (addedActions.size() > 0) || (removedActions.size() > 0) || (changedActions.size() > 0)) {
				fireQuickViewsChangedEvent();
			}
		}
	}

	public boolean isFiring() {
		return canFire;
	}

	public void addQuickViewListenener(QuickViewListener qvl) {
		if (!listeners.contains(qvl)) {
			listeners.add(qvl);
		}
	}

	public void removeQuickViewListener(QuickViewListener qvl) {
		listeners.remove(qvl);
	}

	private void fireQuickViewsChangedEvent() {
		if (listeners.size() > 0) {
			if (isFiring()) {
				fireQuickViewsChangedEvent(new QuickViewEvent(this, addedActions, removedActions, changedActions));
				addedActions = new ArrayList(1);
				removedActions = new ArrayList(1);
				changedActions = new ArrayList(1);
			}
		}
	}

	public void fireQuickViewsChangedEvent(QuickViewEvent evt) {
		if (listeners.size() > 0) {
			if (isFiring()) {
				Collection col = new ArrayList(listeners);
				for (Iterator iter = col.iterator(); iter.hasNext(); ) {
					((QuickViewListener) iter.next()).quickViewsChanged(evt);
				}
			}
		}
	}

	/**
	 * Returns the project
	 * @return QuickViewManager
	 */
	public ShrimpProject getProject() {
		return project;
	}

	/**
	 * Gets all the quick view actions.
	 * @return the {@link Action}s
	 */
	public Collection getQuickViews() {
		return quickViews;
	}

	public int getQuickViewCount() {
		return quickViews.size();
	}

	/**
	 * Returns a sorted array of quick views.
	 * @see QuickViewComparator
	 */
	public QuickViewAction[] getSortedQuickViews() {
		Collection actions = getQuickViews();
		QuickViewAction[] quickViewsArray = (QuickViewAction[])actions.toArray(new QuickViewAction[actions.size()]);
		Arrays.sort(quickViewsArray, new QuickViewComparator());
		return quickViewsArray;
	}

	/**
	 * Gets a quick view action with the given name.
	 * @param name
	 * @return {@link QuickViewAction} or null if not found
	 */
	public QuickViewAction getQuickViewAction(String name) {
		QuickViewAction action = null;
		for (Iterator iter = getQuickViews().iterator(); iter.hasNext(); ) {
			QuickViewAction qva = (QuickViewAction) iter.next();
			if (qva.getActionName().equals(name)) {
				action = qva;
				break;
			}
		}
		return action;
	}

	/**
	 * Removes a quick view with the given name.
	 * @param name
	 * @return QuickViewAction or null if it doesn't exist.
	 */
	public QuickViewAction removeQuickView(String name) {
		QuickViewAction action = getQuickViewAction(name);
		removeQuickView(action);
		return action;
	}

	/**
	 * Removes a quick view action from the collection.
	 * @param action
	 * @return Action or null if it doesn't exist.
	 */
	public boolean removeQuickView(QuickViewAction action) {
		boolean removed = false;
		if (action != null) {
			removed = quickViews.remove(action);
			action.removeActionListener(this);
			if (removed) {
				removedActions.add(action);
				fireQuickViewsChangedEvent();
			}
		}
		return removed;
	}


	/**
	 * Removes all the quick views.
	 */
	public void removeAll() {
		if (quickViews.size() > 0) {
			removedActions.addAll(quickViews);
			for (Iterator iter = quickViews.iterator(); iter.hasNext(); ) {
				QuickViewAction action = (QuickViewAction) iter.next();
				action.removeActionListener(this);
				action.dispose();
			}
			quickViews = new ArrayList(1);
			fireQuickViewsChangedEvent();
		}
	}

	/**
	 * Adds a quick view to the collection.
	 * @param action
	 */
	protected void addQuickView(QuickViewAction action) {
		if ((action != null) && !quickViews.contains(action)) {
			quickViews.add(action);
			action.addActionListener(this);
			addedActions.add(action);
			fireQuickViewsChangedEvent();
		}
	}

	/**
	 * Notifies listeners that a quick view has changed.
	 * This will just be if the name, icon, or display value changes.
	 * @param action
	 */
	public void quickViewChanged(QuickViewAction action) {
		changedActions.add(action);
		fireQuickViewsChangedEvent();
	}

	/**
	 * Checks if that action exists in the quick views collection.
	 * @param action
	 * @return true if the action is found
	 */
	public boolean hasQuickView(QuickViewAction action) {
		return quickViews.contains(action);
	}

	/**
	 * Checks if a quickview with that name exists.
	 * @param name
	 * @return true if an action with that name exists
	 */
	public boolean hasQuickView(String name) {
		return (getQuickViewAction(name) != null);
	}

	public boolean run(QuickViewAction action) {
		boolean run = false;
		if (action != null) {
			// update GUI with action name
			setActionText(action.getActionName());
			action.startAction();
			run = true;
			clearActionText(1000);
		}
		return run;
	}

	public boolean run(String quickViewName) {
		return run(getQuickViewAction(quickViewName));
	}

	protected void setActionText(String actionName) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				shrimpView.setOutputText(actionName);
			} catch (ShrimpToolNotFoundException ignore) {
			}
		}
	}

	protected void clearActionText(long delay) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				shrimpView.clearOutputText(delay);
			} catch (ShrimpToolNotFoundException ignore) {
			}
		}
	}

	protected DataBean getDataBean() {
		DataBean db = null;
		if (project != null) {
			try {
				db = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			} catch (BeanNotFoundException e) {
			}
		}
		return db;
	}

	/**
	 * Gets all the artifact types from the databean.
	 */
	public Collection getAllNodeTypes() {
		Collection allNodes = Collections.EMPTY_LIST;
		if (getDataBean() != null) {
			allNodes = getDataBean().getArtifactTypes(true, true);
		}
		return allNodes;
	}

	/**
	 * Gets all the relationship types from the databean.
	 */
	public Collection getAllArcTypes() {
		Collection allArcs = Collections.EMPTY_LIST;
		if (getDataBean() != null) {
			allArcs = getDataBean().getRelationshipTypes(true, true);
		}
		return allArcs;
	}

	/**
	 * Returns the default cprels.
	 */
	public String[] getDefaultCprels() {
		String[] defCprels = new String[0];
		if (getDataBean() != null) {
			defCprels = getDataBean().getDefaultCprels();
		}
		return defCprels;
	}

	/**
	 * Gets if the default cprels are inverted.
	 */
	public boolean getDefaultCprelsInverted() {
		boolean defInverted = false;
		if (getDataBean() != null) {
			defInverted = getDataBean().getDefaultCprelsInverted();
		}
		return defInverted;
	}

	/**
	 * Saves the quicks views out to the project properties file.
	 */
	public void save() {
		// remove the quick view properties
		//removeAllQuickViewPropertes(properties);
		Properties properties = new SortedProperties();

		// now put the quick view properties back in
		saveQuickViewsToProperties(properties, getQuickViews());

		try {
			File file = getPropertiesFile();
			if (file != null) {
				file.mkdirs();
				if (file.exists()) {
					file.delete();
				}
				String comment = " " + getProjectTitle() + " Quick Views";
				properties.store(new BufferedOutputStream(new FileOutputStream(file)), comment);
			}
		} catch (Throwable t) {
			System.err.println("Error saving quick views: " + t.getMessage());
		}
	}

	protected File getPropertiesFile() {
		if ((propertiesFile == null) && (project != null)) {
			String propsFile = project.getPropertiesFilename();
			if ((propsFile != null) && (propsFile.length() > 0)) {
				propertiesFile = new File(propsFile);
				String name = propertiesFile.getName();
				int ext = name.toLowerCase().indexOf(".properties");
				if (ext > 0) {
					name = name.substring(0, ext) + "_quickviews" + name.substring(ext);
				} else {
					name = "quickviews.properties";
				}
				propertiesFile = new File(propertiesFile.getParentFile(), name);
			}
		}
		return propertiesFile;
	}

	public String getProjectTitle() {
		String title = "";
		if (project != null) {
			title = project.getTitle();
			int dot = title.indexOf(".");
			if (dot > 0) {
				title = title.substring(0, dot);
			}
		}
		return title;
	}

	/**
	 * Saves the quick views to the given {@link Properties} object.
	 * @param props
	 * @return the number of quick views saved
	 */
	private int saveQuickViewsToProperties(Properties props, Collection qvs) {
		props.setProperty(PROPS_QUICKVIEWS_COUNT, String.valueOf(qvs.size()));
		int index = 0;
		int allNodes = getAllNodeTypes().size();
		int allArcs = getAllArcTypes().size();
		for (Iterator iter = qvs.iterator(); iter.hasNext(); ) {
			QuickViewAction action = (QuickViewAction) iter.next();
			saveQuickView(props, action, index, allNodes, allArcs);
			index++;
		}
		return index;
	}

	/**
	 * Removes all the properties that start with the String <code>quickview.</code>
	 */
	public static void removeAllQuickViewPropertes(Properties props) {
		Enumeration keys = props.propertyNames();
		Collection toRemove = new LinkedList();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith(PROPS_QUICKVIEW)) {
				toRemove.add(key);
			}
		}
		for (Iterator iter = toRemove.iterator(); iter.hasNext(); ) {
			String key = (String) iter.next();
			props.remove(key);
		}
		props.remove(PROPS_QUICKVIEWS_COUNT);
	}

	/**
	 * Sets the properties for a quick view.
	 * @param index	the index of the quick view (starting at 0)
	 * @param allNodes the total number of node types
	 * @param allArcs the total number of arc types
	 */
	private static void saveQuickView(Properties props, QuickViewAction action, int index, int allNodes, int allArcs) {
		String key = PROPS_QUICKVIEW + index;
		props.setProperty(key + PROPS_CLASS, action.getClass().getName());
		props.setProperty(key + PROPS_NAME, action.getActionName());
		props.setProperty(key + PROPS_ICON, action.getIconFilename());
		ExpressViewConfigurator config = action.getConfigurator();
		props.setProperty(key + PROPS_LAYOUT, config.getLayoutMode());
		props.setProperty(key + PROPS_LABEL, config.getLabelMode());
		props.setProperty(key + PROPS_DISPLAY, Boolean.toString(action.isDisplay()));

		Collection nodes = config.getNodeTypesOfInterest();
		if (nodes.size() == allNodes) {
			// no point in saving all of the node types
			props.setProperty(key + PROPS_ALL_NODES, "true");
		} else {
			props.setProperty(key + PROPS_NODES, String.valueOf(nodes.size()));
			int typeIndex = 0;
			for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
				String type = (String) iter.next();
				props.setProperty(key + PROPS_NODE + typeIndex, type);
				typeIndex++;
			}
		}

		Collection arcs = config.getArcTypesOfInterest();
		if (arcs.size() == allArcs) {
			props.setProperty(key + PROPS_ALL_ARCS, "true");
		} else {
			props.setProperty(key + PROPS_ARCS, String.valueOf(arcs.size()));
			int typeIndex = 0;
			for (Iterator iter = arcs.iterator(); iter.hasNext(); ) {
				String type = (String) iter.next();
				props.setProperty(key + PROPS_ARC + typeIndex, type);
				typeIndex++;
			}
		}

		String[] cprels = config.getCprels();
		props.setProperty(key + PROPS_CPRELS_COUNT, String.valueOf(cprels.length));
		props.setProperty(key + PROPS_CPRELS_INVERTED, String.valueOf(config.isInverted()));
		for (int i = 0; i < cprels.length; i++) {
			props.setProperty(key + PROPS_CPREL + i, cprels[i]);
		}

		// save composite nodes to open
		Collection nodesToOpen = config.getNodeTypesToOpen();
		props.setProperty(key + PROPS_COMPOSITE_NODES_COUNT, String.valueOf(nodesToOpen.size()));
		index = 0;
		for (Iterator iter = nodesToOpen.iterator(); iter.hasNext(); ) {
			String type = (String) iter.next();
			props.setProperty(key + PROPS_COMPOSITE_NODE + index, type);
			index++;
		}

		// save composite arc types and groups
		Map groups = config.getCompositeArcs();
		props.setProperty(key + PROPS_GROUPS, String.valueOf(groups.size()));
		int groupIndex = 0;
		for (Iterator iter = groups.keySet().iterator(); iter.hasNext(); ) {
			String groupName = (String) iter.next();
			String key2 = key + PROPS_GROUP + groupIndex;
			props.setProperty(key2, groupName);
			Collection cArcs = (Collection) groups.get(groupName);
			props.setProperty(key2 + PROPS_GROUP_ARCS, String.valueOf(cArcs.size()));
			int typeIndex = 0;
			for (Iterator iter2 = cArcs.iterator(); iter2.hasNext(); ) {
				String arcType = (String) iter2.next();
				props.setProperty(key2 + PROPS_GROUP_ARC + typeIndex, arcType);
				typeIndex++;
			}
			groupIndex++;
		}
	}

	/**
	 * Clears the current quick views and then loads the default quick views for the given project.
	 */
	public void loadDefaults() {
		removeAll();
		createDefaultQuickViewActions();
	}

	protected void createDefaultQuickViewActions() {
		if (project != null) {
			project.createDefaultQuickViewActions();
		}
	}

	/**
	 * Loads the quick views from the project properties file.
	 * This is for legacy purposes - quick views are now stored in their own separate file.
	 * @return true if one or more property was loaded
	 */
	protected boolean loadQuickViewsFromProjectProperties() {
		int loadedCount = 0;
		if (project != null) {
			Properties props = project.getProperties();
			boolean wasFiring = isFiring();
			setFiring(false);
			// load the quick views
			Collection newQuickViews = loadQuickViewsFromProperties(props);
			for (Iterator iter = newQuickViews.iterator(); iter.hasNext(); ) {
				QuickViewAction qva = (QuickViewAction) iter.next();
				if (qva != null) {
					qva.setProject(project);
					addQuickView(qva);
					loadedCount++;
				}
			}

			// now remove all the quick view properties and save the properties file
			removeAllQuickViewPropertes(props);
			project.saveProperties();

			// now force a save of the new properties
			save();

			setFiring(wasFiring);
			fireQuickViewsChangedEvent();
		}
		return (loadedCount > 0);
	}

	/**
	 * Loads the quick views from the project properties file.
	 * This is for legacy purposes - quick views are now stored in their own separate file.
	 * @param clearQuickViews if true the current quick views will be cleared
	 * @return true if one or more property was loaded
	 */
	public boolean loadQuickViews(boolean clearQuickViews) {
		boolean loaded = false;
		boolean wasFiring = isFiring();
		setFiring(false);
		if (clearQuickViews) {
			removeAll();
		}
		// legacy - check if the project properties contains the quick views
		if ((project != null) && hasQuickViews(project.getProperties())) {
			loaded = loadQuickViewsFromProjectProperties();
		}
		if (!loaded) {
			File file = getPropertiesFile();
			try {
				if ((file != null) && file.exists()) {
					Properties props = new Properties();
					props.load(new BufferedInputStream(new FileInputStream(file)));
					Collection loadedQuickViews = loadQuickViewsFromProperties(props);
					int loadCount = 0;
					for (Iterator iter = loadedQuickViews.iterator(); iter.hasNext(); ) {
						QuickViewAction qva = (QuickViewAction) iter.next();
						if (qva != null) {
							loadCount++;
							addQuickView(qva);
						}
					}
					loaded = (loadCount > 0);
				}
			} catch (IOException e) {
				System.err.println("Error loading quickviews: " + e.getMessage());
			}
		}
		setFiring(wasFiring);
		if (loaded || clearQuickViews) {
			fireQuickViewsChangedEvent();
		}

		return loaded;
	}

	/**
	 * Loads quick views from the {@link Properties}.
	 * @param props
	 * @param allowDuplicateNames if quick views with the same names are allowed
	 * @return the number of quick views loaded.
	 */
	private Collection loadQuickViewsFromProperties(Properties props) {
		Collection loadedQuickViews = new ArrayList();
		int count = Integer.parseInt(props.getProperty(PROPS_QUICKVIEWS_COUNT, "0"));
		for (int i = 0; i < count; i++) {
			QuickViewAction action = loadQuickView(props, i);
			if (action != null) {
				loadedQuickViews.add(action);
			}
		}
		return loadedQuickViews;
	}

	/**
	 * Loads the quick view from the properties.  Creates the {@link QuickViewAction} class
	 * and sets its properties.
	 * @param index the index of the quick view (starting at 0)
	 * @return QuickViewAction or null
	 */
	private QuickViewAction loadQuickView(Properties props, int index) {
		QuickViewAction action = null;
		String baseKey = PROPS_QUICKVIEW + index;
		String key = baseKey + PROPS_CLASS;
		try {
			String clsName = props.getProperty(key);
			// create the class
			try {
				Class cls = Class.forName(clsName);
				action = (QuickViewAction) cls.newInstance();
			} catch (Throwable t) {
				// legacy support - these quickview actions were moved into this package
				if (clsName != null) {
					if (clsName.indexOf("DefaultViewAction") != -1) {
						action = new DefaultViewAction();
					} else if (clsName.indexOf("QueryViewAction") != -1) {
						action = new QueryViewAction();
					} else if (clsName.indexOf("NestedTreemapViewAction") != -1) {
						action = new NestedTreemapViewAction();
					}
				}
				if (action == null) {
					System.err.println("Warning - couldn't create a quick view of type " + clsName);
					action = new QuickViewAction();
				}
			}

			// this line is important - the quick views won't run without a project
			action.setProject(project);
			key = baseKey + PROPS_NAME;
			action.putValue(Action.NAME, props.getProperty(key));
			key = baseKey + PROPS_ICON;
			action.setIconFilename(props.getProperty(key, "icon_quick_view_blank.gif"));
			ExpressViewConfigurator config = action.getConfigurator();
			key = baseKey + PROPS_LAYOUT;
			config.setLayoutMode(props.getProperty(key, LayoutConstants.LAYOUT_GRID_BY_TYPE));
			key = baseKey + PROPS_LABEL;
			config.setLabelMode(props.getProperty(key, QuickViews.DEFAULT_LABEL_MODE));
			key = baseKey + PROPS_DISPLAY;
			boolean display = "true".equalsIgnoreCase(props.getProperty(key, "true"));
			action.setDisplay(display);

			Collection nodeTypes;
			key = baseKey + PROPS_ALL_NODES;
			boolean all = "true".equalsIgnoreCase(props.getProperty(key, "false"));
			if (all) {
				// all the node types were chosen
				nodeTypes = getAllNodeTypes();
			} else {
				key = baseKey + PROPS_NODES;
				int count = Integer.parseInt(props.getProperty(key, "0"));
				nodeTypes = new HashSet(count);
				for (int i = 0; i < count; i++) {
					key = baseKey + PROPS_NODE + i;
					String type = props.getProperty(key, null);
					if (type != null) {
						nodeTypes.add(type);
					}
				}
			}
			config.setNodeTypesOfInterest(nodeTypes);

			Collection arcTypes;
			key = baseKey + PROPS_ALL_ARCS;
			all = Boolean.valueOf(props.getProperty(key, "false")).booleanValue();
			if (all) {
				arcTypes = getAllArcTypes();
			} else {
				key = baseKey + PROPS_ARCS;
				int count = Integer.parseInt(props.getProperty(key, "0"));
				arcTypes = new HashSet(count);
				for (int i = 0; i < count; i++) {
					key = baseKey + PROPS_ARC + i;
					String type = props.getProperty(key, null);
					if (type != null) {
						arcTypes.add(type);
					}
				}
			}
			config.setArcTypesOfInterest(arcTypes);

			key = baseKey + PROPS_CPRELS_COUNT;
			int count = Integer.parseInt(props.getProperty(key, "0"));
			key = baseKey + PROPS_CPRELS_INVERTED;
			config.setInverted(Boolean.valueOf(props.getProperty(key, "false")).booleanValue());
			String[] cprels = new String[count];
			for (int i = 0; i < count; i++) {
				key = baseKey + PROPS_CPREL + i;
				cprels[i] = props.getProperty(key, null);
			}
			config.setCprels(cprels);

			// load composite nodes to open
			key = baseKey + PROPS_COMPOSITE_NODES_COUNT;
			count = Integer.parseInt(props.getProperty(key, "0"));
			Collection nodesToOpen = new Vector(count);
			for (int i = 0; i < count; i++) {
				key = baseKey + PROPS_COMPOSITE_NODE + i;
				String type = props.getProperty(key, null);
				if (type != null) {
					nodesToOpen.add(type);
				}
			}
			config.setNodeTypesToOpen(nodesToOpen);

			// load composite arc types and groups
			key = baseKey + PROPS_GROUPS;
			count = Integer.parseInt(props.getProperty(key, "0"));
			HashMap groups = new HashMap(count);
			for (int i = 0; i < count; i++) {
				key = baseKey + PROPS_GROUP + i;
				String groupName = props.getProperty(key, null);
				if (groupName != null) {
					String groupKey = key;
					key = groupKey + PROPS_GROUP_ARCS;
					int arcCount = Integer.parseInt(props.getProperty(key, "0"));
					Collection cArcs = new Vector(arcCount);
					for (int j = 0; j < arcCount; j++) {
						key = groupKey + PROPS_GROUP_ARC + j;
						String arcType = props.getProperty(key, null);
						if (arcType != null) {
							cArcs.add(arcType);
						}
					}
					groups.put(groupName, cArcs);
				}
			}
			config.setCompositeArcs(groups);
		} catch (Exception e) {
			System.err.println("Error loading quick views.  Last property key was '" + key + "'.");
		}

		return action;
	}

	public static boolean hasQuickViews(Properties props) {
		boolean hasQVs = false;
		if (props != null) {
			int count = Integer.parseInt(props.getProperty(PROPS_QUICKVIEWS_COUNT, "0"));
			hasQVs = (count > 0);
		}
		return hasQVs;
	}

	/**
	 * Exports all the quick views to the given file.
	 * No check is done to see if the file already exists.
	 * @return the number of exported quick views
	 * @throws IOException
	 */
	public int exportQuickViews(File propFile) throws IOException {
		return writeQuickViewsToFile(propFile, getQuickViews());
	}

	/**
	 * Exports the given quick views to the given file.
	 * No check is done to see if the file already exists.
	 * @return the number of exported quick views
	 * @throws IOException
	 */
	public int writeQuickViewsToFile(File propFile, Collection quickViews) throws IOException {
		Properties prop = new SortedProperties();
		int exportCount = saveQuickViewsToProperties(prop, quickViews);
		prop.store(new BufferedOutputStream(new FileOutputStream(propFile)), "Exported Quick Views");
		return exportCount;
	}

	/**
	 * Imports quicks views from the given file and returns the number of quicks imported.
	 * @throws IOException
	 */
	public Collection readQuickViewsFromFile(File propFile) throws IOException {
		Properties props = new Properties();
		props.load(new BufferedInputStream(new FileInputStream(propFile)));
		Collection importedQuickViews = loadQuickViewsFromProperties(props);
		return importedQuickViews;
	}

	/**
	 * Imports the given collection of quick views into the manager.
	 * @param importQuickViews the quick views to import
	 * @param allowDuplicates if false then quicks must have unique names
	 * @return the number of quick views imported
	 */
	public int importQuickViews(Collection importQuickViews, boolean allowDuplicates) {
		HashSet names = new HashSet();
		if (!allowDuplicates) {
			for (Iterator iter = quickViews.iterator(); iter.hasNext(); ) {
				QuickViewAction qva = (QuickViewAction) iter.next();
				names.add(qva.getActionName());
			}
		}
		boolean wasFiring = isFiring();
		setFiring(false);
		int added = 0;
		for (Iterator iter = importQuickViews.iterator(); iter.hasNext(); ) {
			QuickViewAction qva = (QuickViewAction) iter.next();
			if (qva != null) {
				boolean add = true;
				if (!allowDuplicates) {
					String name = qva.getActionName();
					if (names.contains(name)) {
						add = false;
					} else {
						names.add(name);
					}
				}
				if (add) {
					addQuickView(qva);
					added++;
				}
			}
		}
		setFiring(wasFiring);
		if (added > 0) {
			fireQuickViewsChangedEvent();
		}
		return added;
	}

	/**
	 * Cleanup.
	 */
	public void dispose() {
		removeAll();
		project = null;
		propertiesFile = null;
	}


	/**
	 * Checks if the chosen node and arc types of interest are found in the
	 * collection of all node and arc types for this project.
	 * For nested actions at least one node type of interest must be present.
	 * For flat actions at least one node and one arc type must be chosen.
	 * @return boolean true if valid
	 */
	public boolean isValid(QuickViewAction action) {
		boolean valid = false;
		if (action != null) {
			valid = action.isValid() && (action.isNested() ? isValidNodes(action) : isValidNodes(action) && isValidArcs(action));
		}
		return valid;
	}

	private boolean isValidNodes(QuickViewAction action) {
		// check that at least one node type is specified and is in the databean
		Collection nodes = action.config.getNodeTypesOfInterest();
		if (nodes.size() > 0) {
			Collection allNodes = getAllNodeTypes();
			if (allNodes.size() > 0) {
				// iterate through all the node types of interest until we find one node type
				// that is in the collection of all node types for this project
				for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
					Object nodeType = iter.next();
					if (allNodes.contains(nodeType)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isValidArcs(QuickViewAction action) {
		// check that at least one arc type is specified and is in the databean
		Collection arcs = action.config.getArcTypesOfInterest();
		if (arcs.size() > 0) {
			Collection allArcs = getAllArcTypes();
			if (allArcs.size() > 0) {
				// iterate through all the arc types of interest until we find one arc type
				// that is in the collection of all arc types for this project
				for (Iterator iter = arcs.iterator(); iter.hasNext(); ) {
					Object arcType = iter.next();
					if (allArcs.contains(arcType)) {
						return true;
					}
				}
			}
		}
		return false;
	}


	/////////////////////////////////////////////////////
	// QuickViewAction CREATION Methods
	/////////////////////////////////////////////////////

	/**
	 * Create a control flow graph quickview action
	 * @tag Shrimp(sequence)
	 * @param project
	 * @return the control flow {@link QuickViewAction}
	 */
	public QuickViewAction createControlFlowQuickViewAction(ShrimpProject project) {
		ArrayList nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.METHOD_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CONSTRUCTOR_ART_TYPE);
        nodeTypesOfInterest.add(SoftwareDomainConstants.FUNCTION_ART_TYPE);
		ArrayList arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.CALLS_REL_TYPE);
		return createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_CONTROL_FLOW_GRAPH, "icon_quick_view_control_flow.gif",
				project, nodeTypesOfInterest, arcTypesOfInterest,
				LayoutConstants.LAYOUT_ORTHOGONAL, DisplayConstants.LABEL_MODE_WRAP_TO_NODE);
	}

	/**
	 * Create a sequence quick view action
	 * @tag Shrimp.sequence
	 * @param project
	 * @return the sequence {@link QuickViewAction}
	 */
	public QuickViewAction createSequenceQuickViewAction(ShrimpProject project) {
		ArrayList nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.METHOD_EXEC_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.OBJECT_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.ACTOR_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.SUMMARY_ART_TYPE);
		ArrayList arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.METHOD_CALL_REL_TYPE);
		arcTypesOfInterest.add(JavaDomainConstants.RETURN_VALUE_REL_TYPE);
		arcTypesOfInterest.add(JavaDomainConstants.CONTAINS_REL_TYPE);
		return createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_SEQUENCE_DIAGRAM,
				"icon_quick_view_sequence.gif", project, nodeTypesOfInterest,
				arcTypesOfInterest, LayoutConstants.LAYOUT_SEQUENCE,
				DisplayConstants.LABEL_MODE_FIXED);
	}

	/**
	 * Creates a nested quick view with all nodes and arcs being of interest.
	 * Uses a layout of {@link LayoutConstants#LAYOUT_GRID_BY_TYPE} and a label mode of {@link DisplayConstants#LABEL_MODE_FIXED}.
	 * Also uses default cprels and default inverted.
	 * @return the {@link NestedTreemapViewAction}
	 */
	public NestedTreemapViewAction createNestedTreemapView(ShrimpProject project) {
		NestedTreemapViewAction action = new NestedTreemapViewAction(project, getAllNodeTypes(),
				getDefaultCprels(), getDefaultCprelsInverted());
		addQuickView(action);
		return action;
	}

	public DefaultViewAction createDefaultView(ShrimpProject project) {
		DefaultViewAction action = new DefaultViewAction(project, getAllNodeTypes(), getAllArcTypes(),
													getDefaultCprels(), getDefaultCprelsInverted());
		addQuickView(action);
		return action;
	}

	public DefaultViewAction createDefaultView(String actionName, String iconFilename, ShrimpProject project) {
		DefaultViewAction action = new DefaultViewAction(actionName, iconFilename, project,
				getAllNodeTypes(), getAllArcTypes(), getDefaultCprels(), getDefaultCprelsInverted());
		addQuickView(action);
		return action;
	}

	public QueryViewAction createQueryViewAction(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode,
			int incomingLevels, int outgoingLevels) {
		QueryViewAction action = new QueryViewAction(actionName, iconFilename, project, nodeTypes, arcTypes,
				layoutMode, labelMode, incomingLevels, outgoingLevels);
		addQuickView(action);
		return action;
	}

	/**
	 * Creates a nested quick view with all nodes and arcs being of interest.
	 * Uses a layout of {@link LayoutConstants#LAYOUT_GRID_BY_TYPE} and a label mode of {@link DisplayConstants#LABEL_MODE_FIXED}.
	 * Also uses default cprels and default inverted.
	 */
	public QuickViewAction createNestedView(String actionName, String iconFilename, ShrimpProject project) {
		QuickViewAction action = createNestedView(actionName, iconFilename, project,
				getAllNodeTypes(),getAllArcTypes(), LayoutConstants.LAYOUT_GRID_BY_TYPE, DisplayConstants.LABEL_MODE_FIXED);
		return action;
	}

	/**
	 * Creates a nested quick view with the given node and arc types of interest.
	 * Uses a layout of {@link LayoutConstants#LAYOUT_GRID_BY_TYPE} and a label mode of {@link DisplayConstants#LABEL_MODE_FIXED}.
	 * Also uses default cprels and default inverted.
	 */
	public QuickViewAction createNestedView(ShrimpProject project, Collection nodeTypes, Collection arcTypes) {
		return createNestedView("Nested View", "icon_quick_view_nested.gif", project, nodeTypes, arcTypes,
				LayoutConstants.LAYOUT_GRID_BY_TYPE, DisplayConstants.LABEL_MODE_FIXED);
	}

	/**
	 * Creates a nested quick view with the given node and arc types of interest and sets the layout and label modes.
	 * Uses default cprels and default inverted.
	 */
	public QuickViewAction createNestedView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode) {
		QuickViewAction action = createNestedView(actionName, iconFilename, project, nodeTypes, arcTypes,
									LayoutConstants.LAYOUT_GRID_BY_TYPE, DisplayConstants.LABEL_MODE_FIXED,
									getDefaultCprels(), getDefaultCprelsInverted());
		return action;
	}

	/**
	 * Creates a nested quick view with the given node and arc types of interest.
	 * Also sets the layout and label modes and cprels.
	 */
	public QuickViewAction createNestedView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode,
			String[] cprels, boolean inverted) {
		QuickViewAction action = new QuickViewAction(actionName, iconFilename, project);
		action.config.setNodeTypesOfInterest(nodeTypes);
		action.config.setArcTypesOfInterest(arcTypes);
		action.config.setLayoutMode(layoutMode);
		action.config.setLabelMode(labelMode);
		action.config.setCprels(cprels);
		action.config.setInverted(inverted);
		addQuickView(action);
		return action;
	}

	/**
	 * Creates a nested composite quick view.
	 * Layout used is a {@link LayoutConstants#LAYOUT_SPRING} and the Label mode is {@link DisplayConstants#LABEL_MODE_FIXED}.
	 * Default cprels are used.
	 */
	public QuickViewAction createNestedCompositeView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, Collection nodeTypesToOpen, Map compositeArcsMap) {
		QuickViewAction action = createNestedCompositeView(actionName, iconFilename, project,
				nodeTypes, arcTypes, LayoutConstants.LAYOUT_SPRING, nodeTypesToOpen, compositeArcsMap);
		return action;
	}

	/**
	 * Creates a nested composite quick view with the given layout.
	 * The Label mode is {@link DisplayConstants#LABEL_MODE_FIXED} and default cprels are used.
	 */
	public QuickViewAction createNestedCompositeView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, Collection nodeTypesToOpen, Map compositeArcsMap) {
		QuickViewAction action = createNestedCompositeView(actionName, iconFilename, project,
				nodeTypes, arcTypes, layoutMode, DisplayConstants.LABEL_MODE_FIXED,
				getDefaultCprels(), getDefaultCprelsInverted(), nodeTypesToOpen, compositeArcsMap);
		return action;
	}

	/**
	 * Creates a nested composite quick view.
	 */
	public QuickViewAction createNestedCompositeView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode,
			String[] cprels, boolean inverted, Collection nodeTypesToOpen, Map compositeArcsMap) {
		return createGenericView(actionName, iconFilename, project,
								 nodeTypes, arcTypes, layoutMode, labelMode,
								 cprels, inverted, nodeTypesToOpen, compositeArcsMap);
	}


	/**
	 * Creates a flat quick view with all node and arc types being of interest.
	 * A layout of {@link LayoutConstants#LAYOUT_TREE_VERTICAL} is used and a
	 * label mode of {@link DisplayConstants#LABEL_MODE_FIXED} is used.
	 * There are no cprels or composite arcs since it is a flat graph.
	 */
	public QuickViewAction createFlatView(String actionName, String iconFilename, ShrimpProject project) {
		QuickViewAction action = createFlatView(actionName, iconFilename, project,
				getAllNodeTypes(), getAllArcTypes(),
				LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);
		return action;
	}
	/**
	 * Creates a flat quick view.
	 * A layout of {@link LayoutConstants#LAYOUT_TREE_VERTICAL} is used and a
	 * label mode of {@link DisplayConstants#LABEL_MODE_FIXED} is used.
	 * There are no cprels or composite arcs since it is a flat graph.
	 */
	public QuickViewAction createFlatView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypesOfInterest, Collection arcTypesOfInterest) {
		return createFlatView(actionName, iconFilename, project, nodeTypesOfInterest, arcTypesOfInterest,
				LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);
	}

	/**
	 * Creates a flat quick view.  Also adds the action to this manager.
	 * There are no cprels or composite arcs since it is a flat graph.
	 */
	public QuickViewAction createFlatView(String actionName, String iconFilename, ShrimpProject project, Collection nodeTypesOfInterest,
										  Collection arcTypesOfInterest, String layoutMode, String labelMode) {
		QuickViewAction action = new QuickViewAction(actionName, iconFilename, project);
	    action.config.setNodeTypesOfInterest(nodeTypesOfInterest);
	    action.config.setArcTypesOfInterest(arcTypesOfInterest);
	    action.config.setLayoutMode(layoutMode);
	    action.config.setLabelMode(labelMode);
	    action.config.setCprels(new String[0]);
	    action.config.setInverted(false);

	    addQuickView(action);
		return action;
	}

	/**
	 * Creates a generic QuickView and sets all the properties of the {@link ExpressViewConfigurator}.
	 * Also adds the action to this manager.
	 */
	public QuickViewAction createGenericView(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode, String[] cprels,
			boolean inverted, Collection nodeTypesToOpen, Map compositeArcsMap) {
		QuickViewAction action = new QuickViewAction(actionName, iconFilename, project);
		action.config.setNodeTypesOfInterest(nodeTypes);
		action.config.setArcTypesOfInterest(arcTypes);
		action.config.setLayoutMode(layoutMode);
		action.config.setLabelMode(labelMode);
		action.config.setCprels(cprels);
		action.config.setInverted(inverted);
		action.config.setNodeTypesToOpen(nodeTypesToOpen);
		action.config.setCompositeArcs(compositeArcsMap);

		addQuickView(action);
		return action;
	}

	/**
	 * Gets the quick view icons.
	 * This is needed for Creole to override.
	 */
	public URL getIconsURL() throws Exception {
		URL url = ResourceHandler.class.getResource("icons/");
		return url;
	}

	public void actionFinished(ShrimpAction action) {
		clearActionText(1000);
	}

	public void actionStarting(ShrimpAction action) {
		// update GUI with action name
		setActionText(action.getActionName());
	}

}
