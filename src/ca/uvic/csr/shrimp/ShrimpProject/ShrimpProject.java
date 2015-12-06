/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;

import java.net.URI;
import java.util.Properties;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.gui.quickview.QuickViews;

/**
 * Defines a "project" for Shrimp.
 * A project usually consists of some data (ie. a DataBean), and has some tools associated with it.
 * An application holds zero or more projects, only one of which can be the "active" project at
 * any given time.
 *
 * @author Nasir Rather, Rob Lintern
 */
public interface ShrimpProject {

	/* *****************  Project wide beans ***********************
	 *  - used by multiple tools
	 */
	/**
	 * Use this identifier to retrieve and set the dataBean.
	 *
	 * @see ShrimpProject#getBean(String name)
	 */
	public static final String DATA_BEAN = "dataBean";

    /**
     * Use this identifier to retrieve and set the Filter Bean
     * that interacts with the Data Bean.
     *
     * @see ShrimpProject#getBean(String name)
     */
    public static final String DATA_FILTER_BEAN = "dataFilterBean";

	/**
	 * Use this identifier to retrieve and set the search window.
	 */
	public static final String SEARCH_BEAN = "SearchBean";

	/**
	 * Use this identifier to retrieve and set the persistent storage bean.
	 *
	 * @see ShrimpProject#getBean(String name)
	 */
	public static final String PERSISTENT_STORAGE_BEAN = "persistentStorageBean";

	/**
	 * Use this identifier to retrieve and set the shrimpView.
	 *
	 * @see ShrimpProject#getTool(String)
	 */
	public static final String SHRIMP_VIEW = ShrimpConstants.TOOL_SHRIMP_VIEW;


	/**
	 * Use this identifier to retrieve and set the hierarchicalView.
	 */
	public static final String HIERARCHICAL_VIEW = ShrimpConstants.TOOL_HIERARCHICAL_VIEW;

	/**
	 * Use this identifier to retrieve and set the Query View.
	 */
	public static final String QUERY_VIEW = ShrimpConstants.TOOL_QUERY_VIEW;

	/**
	 * Use this identifier to retrieve and set the thumbnailView.
	 */
	public static final String THUMBNAIL_VIEW = ShrimpConstants.TOOL_THUMBNAIL_VIEW;

	/**
	 * Use this identifier to retrieve and set the {@link QuickViews}.
	 */
	public static final String QUICK_VIEWS = ShrimpConstants.TOOL_QUICK_VIEWS;

	/**
	 * Use this identifier to retrieve and set the Node Attribute Panel.
	 */
	public static final String NODE_ATTRIBUTE_PANEL = ShrimpConstants.TOOL_NODE_ATTRIBUTE_PANEL;

	/**
	 * Use this identifier to retrieve and set the Arc Attribute Panel.
	 */
	public static final String ARC_ATTRIBUTE_PANEL = ShrimpConstants.TOOL_ARC_ATTRIBUTE_PANEL;

	/**
	 * Use this identifier to retrieve and set the scripting tool.
	 */
	public static final String SCRIPTING_TOOL = ShrimpConstants.TOOL_SCRIPTING;

	/**
	 * Use this identifier to retrieve and set the scripting bean.
	 */
	public static final String SCRIPTING_BEAN = "Scripting_Bean";

	/**
	 * Use this identifier to retrieve and set the search window.
	 */
	public static final String SEARCH_TOOL = ShrimpConstants.TOOL_SEARCH;

	/**
	 * Use this identifier to retrieve and set the filter window.
	 */
	public static final String FILTER_TOOL = ShrimpConstants.TOOL_FILTERS;

	/**
	 * Use this identifier to retrieve and set the attrToVisVarBean.
	 */
	public static final String ATTR_TO_VIS_VAR_BEAN = "attrToVisVarBean";

	// Project-specific property names that are not defined in other areas
	public static  final String	PROPERTY_KEY_NODE_DISPLAY_TEXT_ATTRIBUTE_TYPE = "NodeDisplayText";


    // Project types and file extensions

    public static final String EXT_RSF = "rsf";
    public static final String EXT_PRJ = "prj";
    public static final String PROJECT_TYPE_PRJ = "PRJ";

    public static final String EXT_PPRJ = "pprj";
    public static final String EXT_OWL = "owl";
	public static final String PROJECT_TYPE_PROTEGE = "PROTEGE";

	public static final String EXT_GXL = "gxl";
	public static final String PROJECT_TYPE_GXL = "GXL";

	public static final String EXT_XMI = "xmi";
	public static final String PROJECT_TYPE_XMI = "XMI";

	public static final String EXT_XML = "xml";
	public static final String PROJECT_TYPE_XML = "XML";

	public static final String EXT_LIBSEA = "graph";
	public static final String PROJECT_TYPE_LIBSEA = "LIBSEA";

	public static final String PROJECT_TYPE_OBO = "OBO";
	public static final String EXT_OBO = "obo";

	public static final String PROJECT_TYPE_UNKNOWN = "unknown";


	/**
	 * Returns the ShrimpApplication that contains this project.
	 *
	 * @return ShrimpAplication this project belongs to.
	 */
	public ShrimpApplication getApplication();

	/**
	 * Returns the action manager this project is using.
	 *
	 * @return ActionManager The action manager this project is using.
	 */
	public ActionManager getActionManager();

	/**
	 * Creates the project specific actions.
	 * This method should only be called by the ShrimpApplication.
	 */
	public void createProjectActions();

	/**
	 * Returns the properties of this project.
	 *
	 * @return properties of this project.
	 */
	public Properties getProperties();

	/**
	 * Returns a Bean from this project.
	 * If the bean does not exist, BeanNotFoundException is thrown.
	 *
	 * @param name Identifies a bean.
	 *
	 * @return Object bean that was requested.
	 *
	 * @exception BeanNotFoundException Thrown if the requested bean is not found.
	 */
	public Object getBean(String name) throws BeanNotFoundException;

	/**
	 * Adds a bean to this project.
	 *
	 * @param name String name identifying the bean.
	 * @param bean Object bean to be added.
	 */
	public void addBean(String name, Object bean);

	/**
	 * Removes a bean from the project.
	 *
	 * @param name String name identifying the bean.
	 */
	public void removeBean(String name);

	/**
	 * Returns a ShrimpTool from this project.
	 * If the tool does not exist, ShrimpToolNotFound exception if thrown.
	 *
	 * @param name Identifies a ShrimpTool.
	 *
	 * @return ShrimpTool tool that was requested.
	 *
	 * @throws ShrimpToolNotFoundException thrown if the requested tool was not found.
	 */
	public ShrimpTool getTool(String name) throws ShrimpToolNotFoundException;

	/**
	 * Returns the tools associated with this project.
	 */
	public ShrimpTool[] getTools();

	/**
	 * Adds a ShrimpTool to this project.
	 *
	 * @param name String name to identify the tool.
	 * @param tool ShrimpTool tool to be added.
	 */
	public void addTool(String name, ShrimpTool tool);

	/**
	 * Removes a ShrimpTool from this project.
	 *
	 * @param name String name of the ShrimpTool to be added.
	 */
	public void removeTool(String name);

	/**
	 * Returns the title of this project.
	 *
	 * @return String title of this project.
	 */
	public String getTitle();

	/**
	 * Sets the title of this project.
	 *
	 * @param title String new title of the Project.
	 */
	public void setTitle(String title);

	/**
	 * Writes the project properties to a file.
	 */
	public void saveProperties();

	/**
	 * @return the Properties filename, might be null.
	 */
	public String getPropertiesFilename();

	/**
	 * Loads the project properties from a file.
	 */
	public void loadProperties();

	/**
	 * Cleanup and close the project.
	 */
	public void disposeProject();

	/**
	 * Adds a listener to this project.
	 *
	 * @param listener ShrimpProjectListener to be added.
	 */
	public void addProjectListener(ShrimpProjectListener listener);

	/**
	 * Removes a listener that was previously added to this project.
	 *
	 * @param listener ShrimpProjectListener to be removed.
	 */
	public void removeProjectListener(ShrimpProjectListener listener);

	/**
	 * Fires projectActivated event.
	 */
	//public void fireProjectActivatedEvent();

	/**
	 * Fires projectDeactivated event.
	 */
	//public void fireProjectDeactivatedEvent();

	/**
	 * Fires projectClosing event.
	 */
	public void fireProjectClosingEvent();

	/**
	 * Refreshes this project. Causes a projectRefreshed event to fire.
	 */
	public void refresh();

	/**
	 * Returns the URI of this project.
	 * @return null if no URI associated with this project
	 */
	public URI getProjectURI();

	public String getProjectType();

	/**
	 * Creates the quick views for this project.
	 * First it tries to attempts to load the quick views from the project
	 * properties file.  If that fails then the default quick views are loaded for this project.
	 * The existing quick views are not cleared.
	 * @see ShrimpProject#createDefaultQuickViewActions()
	 */
	public void createQuickViewActions();

	/**
	 * Creates the default quick views for this project.
	 * This method doesn't load quick views from the project properties file.
	 * The existing quick views are not cleared.
	 * @see ShrimpProject#createQuickViewActions()
	 */
	public void createDefaultQuickViewActions();

	/**
	 * Gets the quick view manager for this project.
	 * @return QuickViewManager
	 */
	public QuickViewManager getQuickViewManager();

}


