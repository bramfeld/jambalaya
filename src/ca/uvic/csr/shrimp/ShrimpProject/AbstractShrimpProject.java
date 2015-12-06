/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.ArcStyleVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.ArrowHeadStyleVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.DefaultAttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.IconVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.LabelStyleVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NodeShapeVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalArcColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalAttribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalArcWeightVisVar;
import ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalAttribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.StringVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.OBODataBean;
import ca.uvic.csr.shrimp.DataBean.event.DataFilterRequestListener;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.layout.OrthogonalLayout;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.PersistentStorageBean.GXLPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.LibSeaPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.PRJPersistentStorageBean;
import ca.uvic.csr.shrimp.ScriptingBean.ScriptingBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.DefaultSearchBean;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.ShrimpApplication.AbstractShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.ArtifactSearchRequestAdapter;
import ca.uvic.csr.shrimp.adapter.DataFilterChangedAdapter;
import ca.uvic.csr.shrimp.adapter.DataFilterRequestAdapter;
import ca.uvic.csr.shrimp.adapter.FilterSearchResultsAdapter;
import ca.uvic.csr.shrimp.adapter.FocusOnSearchResultsAdapter;
import ca.uvic.csr.shrimp.adapter.SelectSearchResultsAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.jambalaya.JambalayaApplication;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.ExportSVGCommandAdapter;
import ca.uvic.csr.shrimp.usercontrols.ExportToImageFileAction;
import ca.uvic.csr.shrimp.usercontrols.ImportSVGCommandAdapter;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.SortedProperties;

/**
 * A base implementation for ShrimpProject. Classes wanting to implement a
 * ShrimpProject should ideally extend this class. This class includes most common operations.
 *
 * @author Nasir Rather
 */
public abstract class AbstractShrimpProject implements ShrimpProject {

	/**  filename where the properties of this project will be stored */
    protected String propertiesFileName = null;

	/** project specific properties */
	protected Properties properties;

	private QuickViewManager quickViewManager;

	/** beans in this project.*/
	protected Map beans;

	/** ShrimpTools in this project. */
	protected Map tools;

	/** Project title. */
	protected String title;

	/** Listeners */
	private Vector listeners = new Vector();

	/** application */
	protected ShrimpApplication application;

	/** actions/menus at the project level */
	protected Vector projectActions;


	/** the URI associated with this project, if there is one */
	protected URI projectURI;

	protected String projectType = PROJECT_TYPE_UNKNOWN;

    private DataFilterRequestListener dataFilterRequestListener;

    private File lastDirectory = null;

	/**
	 * Takes care of necessary initializations.
	 * This has to be called by all subclasses.
	 */
	public AbstractShrimpProject(ShrimpApplication application, String projectType) {
		// initialize members that don't depend on project properties
		this.application = application;
		this.projectType = (projectType != null ? projectType : PROJECT_TYPE_UNKNOWN);
		beans = new Hashtable();
		tools = new Hashtable();
		properties = new SortedProperties();	// sorts the keys which is nice for saving to a file
		projectActions = new Vector();
		quickViewManager = new QuickViewManager(this);
		createScriptingBean();
		createSearchBean();
		createAttrToVisVarBean();
        createDataFilterBean();
	}


	/**
	 * Determins the project type based on the file extension of the URI.
	 * @param uri the project URI.
	 * @return String the project type.  Defaults to unknown.
	 */
	protected static String determineProjectType(URI uri) {
		String projectURIStringLC = uri.toString().toLowerCase();
		String type;
		// Figure out the project type
		if (projectURIStringLC.endsWith(EXT_PPRJ) || projectURIStringLC.endsWith(EXT_OWL)) {
			type = PROJECT_TYPE_PROTEGE;
		} else if (projectURIStringLC.endsWith(EXT_PRJ)) {
			type = PROJECT_TYPE_PRJ;
		} else if (projectURIStringLC.endsWith(EXT_GXL)) {
			type = PROJECT_TYPE_GXL;
		} else if (projectURIStringLC.endsWith(EXT_XMI) ) {
			type = PROJECT_TYPE_XMI;
		} else if (projectURIStringLC.endsWith(EXT_XML)) {
			type = PROJECT_TYPE_XML;
		} else if (projectURIStringLC.endsWith(EXT_OBO)) {
			type = PROJECT_TYPE_OBO;
		} else {
			System.err.println(ApplicationAccessor.getAppName() + ": warning - unknown project type");
			type = PROJECT_TYPE_UNKNOWN;
		}
		return type;
	}

	public String getProjectType() {
		return projectType;
	}

	protected void createScriptingBean() {
		//it is applicable to more than one tool
		//so each project should have its own
    	if (ShrimpUtils.isScriptingToolInstalled()) {
			ScriptingBean sb = new ScriptingBean(this);
			addBean(ShrimpProject.SCRIPTING_BEAN, sb);
    	}
	}

	protected void createAttrToVisVarBean() {
		AttrToVisVarBean attrToVisVarBean = new DefaultAttrToVisVarBean(this);

		//create data attributes
		Attribute artifactTypeAttr = new NominalAttribute(attrToVisVarBean, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class);
		Attribute relTypeAttr = new NominalAttribute(attrToVisVarBean, AttributeConstants.NOM_ATTR_REL_TYPE, String.class);

		// TODO can't specify a min and max for an attribute at startup, when you don't know what they will until seeing all data
		Attribute relWeightAttr = new OrdinalAttribute(attrToVisVarBean, AttributeConstants.ORD_ATTR_REL_WEIGHT, Double.class, new Double (ShrimpArc.DEFAULT_ARC_WEIGHT), new Double (20.0));

		// create node visual variables
		VisualVariable nodeColorVisVar = new NominalNodeColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
		VisualVariable nodeShapeVisVar = new NodeShapeVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_SHAPE);
		VisualVariable nodeImageVisVar = new StringVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_IMAGE);
		VisualVariable nodeIconVisVar = new IconVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_ICON);
		VisualVariable nodeOuterBorderColorVisVar = new NominalNodeBorderColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR);
		VisualVariable nodeOuterBorderStyleVisVar = new StringVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE);
		VisualVariable nodeInnerBorderColorVisVar = new NominalNodeBorderColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR);
		VisualVariable nodeInnerBorderStyleVisVar = new StringVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE);
		VisualVariable labelStyleVisVar = new LabelStyleVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_LABEL_STYLE);
		// create arc visual variables
		VisualVariable arcColorVisVar = new NominalArcColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_ARC_COLOR);
		VisualVariable arcStyleVisVar = new ArcStyleVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_ARC_STYLE);
		VisualVariable arcWeightVisVar = new OrdinalArcWeightVisVar(attrToVisVarBean, VisVarConstants.VIS_VAR_ARC_WEIGHT);
		VisualVariable arrowHeadStyleVisVar = new ArrowHeadStyleVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_ARC_ARROW_HEAD_STYLE);

		//add data attributes
		attrToVisVarBean.addAttr(artifactTypeAttr);
		attrToVisVarBean.addAttr(relTypeAttr);
		attrToVisVarBean.addAttr(relWeightAttr);

		//add visual variables
		attrToVisVarBean.addVisVar(nodeColorVisVar);	// NODES
		attrToVisVarBean.addVisVar(nodeShapeVisVar);
		attrToVisVarBean.addVisVar(nodeImageVisVar);
		attrToVisVarBean.addVisVar(nodeIconVisVar);
		attrToVisVarBean.addVisVar(nodeOuterBorderColorVisVar);
		attrToVisVarBean.addVisVar(nodeOuterBorderStyleVisVar);
		attrToVisVarBean.addVisVar(nodeInnerBorderColorVisVar);
		attrToVisVarBean.addVisVar(nodeInnerBorderStyleVisVar);
		attrToVisVarBean.addVisVar(labelStyleVisVar);
		attrToVisVarBean.addVisVar(arcColorVisVar);		// ARCS
		attrToVisVarBean.addVisVar(arcStyleVisVar);
		attrToVisVarBean.addVisVar(arcWeightVisVar);
		attrToVisVarBean.addVisVar(arrowHeadStyleVisVar);

		// map art type to node color, shape, label style, image, icon, image sizing, fill background, and draw border
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_SHAPE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_LABEL_STYLE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_IMAGE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON);

		// map rel type to arc shape, style, arrow head style, and weight
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_ARROW_HEAD_STYLE);
		attrToVisVarBean.mapAttrToVisVar(AttributeConstants.ORD_ATTR_REL_WEIGHT, VisVarConstants.VIS_VAR_ARC_WEIGHT);

		addBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN, attrToVisVarBean);
	}

	protected void createDataFilterBean() {
        dataFilterRequestListener = new DataFilterRequestAdapter(this);
        FilterChangedListener dataFilterChangedAdapter = new DataFilterChangedAdapter(this);
        FilterBean dataFilterBean = new FilterBean();
        dataFilterBean.addFilterChangedListener(dataFilterChangedAdapter);

        loadDataFiltersFromProperties();
        addBean(DATA_FILTER_BEAN, dataFilterBean);

        //Filter dataTypeFilter = new NominalAttributeFilter ("type", String.class, FilterConstants.STRING_FILTER_TYPE, new ArrayList());
        //dataFilterBean.addFilter(dataTypeFilter);
    }

	protected void createSearchBean() {
		// create listeners for search events
		ArtifactSearchRequestAdapter artifactSearchRequestAdapter = new ArtifactSearchRequestAdapter(this);
		FocusOnSearchResultsAdapter focusOnSearchResultsAdapter = new FocusOnSearchResultsAdapter(this, ShrimpProject.SHRIMP_VIEW);
		FilterSearchResultsAdapter filterResultsAdapter = new FilterSearchResultsAdapter(this, ShrimpProject.SHRIMP_VIEW);
		SelectSearchResultsAdapter selectResultsAdapter = new SelectSearchResultsAdapter(this, ShrimpProject.SHRIMP_VIEW);

		// create an artifact search strategy
		ArtifactSearchStrategy artifactSearchStrategy = new ArtifactSearchStrategy(null);
		artifactSearchStrategy.addSearchRequestListener(artifactSearchRequestAdapter);
		artifactSearchStrategy.addBrowseActionListener(focusOnSearchResultsAdapter);
		artifactSearchStrategy.addFilterResultsActionListener(filterResultsAdapter);
		artifactSearchStrategy.addSelectResultsActionListener(selectResultsAdapter);
		//artifactSearchStrategy.addArtifactTypes();

		//create a new search bean
		SearchBean searchBean = new DefaultSearchBean();
		//add artifact search
		searchBean.addStrategy(artifactSearchStrategy);

		addBean(ShrimpProject.SEARCH_BEAN, searchBean);
	}

	/**
	 * Creates project specific actions.
	 * This method should only be called in the {@link AbstractShrimpApplication#addProject(ShrimpProject)} method.
	 */
	public void createProjectActions() {
		// stop menumanger events
		ActionManager actionManager = getActionManager();
		boolean firingEvents = actionManager.getFiringEvents();
		actionManager.setFiringEvents(false);

		ShrimpAction action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_REFRESH, ResourceHandler.getIcon("icon_refresh.gif")) {
			public void actionPerformed(ActionEvent e) {
			    getApplication().waitCursor();
				try {
					refresh();
					ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
					shrimpView.addDefaultRootNodes(true);
				} catch (ShrimpToolNotFoundException e1) {
					e1.printStackTrace();
				} finally {
					getApplication().defaultCursor();
				}
			}
		};
		addProjectAction(action, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_C, 1);

		Icon exportIcon = ResourceHandler.getIcon("icon_export.gif");

		action = new ExportToImageFileAction(this, ShrimpProject.SHRIMP_VIEW);
		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_C, 2);

		// Export to PRJ/RSF
		action = new DefaultShrimpAction("Export to PRJ/RSF", exportIcon) {
			public void actionPerformed(ActionEvent e) {
			    export(StandAloneProject.EXT_PRJ, "Shrimp Project");
			}
		};
		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_C, 3);

		//Export to GXL
		action = new DefaultShrimpAction("Export to GXL", exportIcon) {
			public void actionPerformed(ActionEvent e) {
			    export(StandAloneProject.EXT_GXL, "GXL File");
			}
		};
		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_C, 4);

		//Export to LibSea/Walrus graph format
        try {
            new LibSeaPersistentStorageBean(this);
    		action = new DefaultShrimpAction("Export to LibSea/Walrus", exportIcon) {
    			public void actionPerformed(ActionEvent e) {
    			    export(StandAloneProject.EXT_LIBSEA, "LibSea/Walrus File");
    			}
    		};
    		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_C, 5);
        } catch (Throwable e) {
            //thrown if the libsea.jar is not in the classpath
        }

		// File -> SVG Export
		action = new ExportSVGCommandAdapter(this, ShrimpProject.SHRIMP_VIEW);
		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_D, 1);

		action = new ImportSVGCommandAdapter(this, ShrimpProject.SHRIMP_VIEW);
		addProjectAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_D, 2);

		// restore actionmanager events and fire an event
		actionManager.setFiringEvents(firingEvents);
	}

	private void export(String fileExtension, String description) {
		try {
			DataBean dataBean = (DataBean)getBean(ShrimpProject.DATA_BEAN);
			PersistentStorageBean psb;
			if (fileExtension == StandAloneProject.EXT_GXL) {
			    psb = new GXLPersistentStorageBean();
			} else if (fileExtension == StandAloneProject.EXT_LIBSEA){
			    psb = new LibSeaPersistentStorageBean(this);
			} else {
			    psb = new PRJPersistentStorageBean();
			}

			// indicate that an export is happening so that any extra attributes
			// such as source code will be exported too
			dataBean.setExportingData(true);
			Vector artifacts = dataBean.getArtifacts(true);
			dataBean.setExportingData(false);
			Vector rels = dataBean.getRelationships(true);

			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Export");
			chooser.setFileFilter(new ShrimpFileFilter(fileExtension, description));
			if (lastDirectory == null) {
				lastDirectory = new File(System.getProperty("user.dir"));
			}
			chooser.setCurrentDirectory(lastDirectory);
			int val = chooser.showSaveDialog(getApplication().getParentFrame());
			if (val == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
            	lastDirectory = file.getParentFile();
				if (file != null) {
					if (!file.getName().endsWith("." + fileExtension)) {
					    file = new File(file.getAbsolutePath() + "." + fileExtension);
					}
					try {
						if (file.exists()) {
				             int response = JOptionPane.showConfirmDialog (null,
				            		 "Overwrite existing file?", "Confirm Overwrite",
				            		 JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				             if (response == JOptionPane.CANCEL_OPTION) {
								return;
							}
				        } else {
	                        file.createNewFile();
	                    }
	                    if (file.exists()){
	                        psb.saveData(file.getAbsolutePath(), artifacts, rels);
	                    }
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
				}
			}
		} catch (BeanNotFoundException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getApplication()
	 */
	public ShrimpApplication getApplication() {
		return application;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getActionManager()
	 */
	public ActionManager getActionManager() {
		if (getApplication() == null) {
			System.err.println("Warning - application is null");
			return null;
		}
		return getApplication().getActionManager();
	}

	/**
	 * Method addProjectAction.
	 * @param action
	 * @param parentMenu
	 * @param group
	 * @param position
	 */
	public void addProjectAction(ShrimpAction action, String parentMenu, String group, int position) {
		getActionManager().addAction(action, parentMenu, group, position);
		projectActions.add(action);
	}

	/**
	 * Method removeProjectActions.
	 */
	public void removeProjectActions() {
		ActionManager actionManager = getActionManager();
		// stop menumanger events
		boolean firingEvents = actionManager.getFiringEvents();
		actionManager.setFiringEvents(false);

		try {
			for (int i = 0; i < projectActions.size(); i++) {
				ShrimpAction action = (ShrimpAction) projectActions.elementAt(i);
				action.dispose();
				getActionManager().removeAction(action, ActionManager.DISABLE_PARENT);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// restore actionmanager events and fire an event
			actionManager.setFiringEvents(firingEvents);
		}
	}


	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	public QuickViewManager getQuickViewManager() {
		return quickViewManager;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getBean(String)
	 */
	public Object getBean(String name) throws BeanNotFoundException {
		if (!this.beans.containsKey(name)) {
			throw new BeanNotFoundException(name);
		}
		return this.beans.get(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#addBean(String, Object)
	 */
	public void addBean(String name, Object bean) {
        Object oldBean = null;
		if (name != null && bean != null) {
            oldBean = beans.get(name);
			beans.put(name, bean);
		}
        // if this is a databean being added then make sure that
        // it has a filter request listener added to it
        // TODO is there a better place to put this?
		if (name.equals(DATA_BEAN)) {
            if (oldBean != null) {
                ((DataBean)oldBean).removeFilterRequestListener(dataFilterRequestListener);
            }
            ((DataBean)bean).addFilterRequestListener(dataFilterRequestListener);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#removeBean(String)
	 */
	public void removeBean(String name) {
		if (this.beans.containsKey(name)) {
			this.beans.remove(name);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getTool(String)
	 */
	public ShrimpTool getTool(String name) throws ShrimpToolNotFoundException {
		if (tools.containsKey(name)) {
			ShrimpTool tool = (ShrimpTool) tools.get(name);
			return tool;
		}
		throw new ShrimpToolNotFoundException(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getTools()
	 */
	public ShrimpTool[] getTools() {
		ShrimpTool [] toolArray = new ShrimpTool [tools.size()];
		Collection coll = tools.values();
		coll.toArray(toolArray);
		return toolArray;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#addTool(String, ShrimpTool)
	 */
	public void addTool(String name, ShrimpTool tool) {
		if (name != null && tool != null) {
			this.tools.put(name, tool);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#removeTool(String)
	 */
	public void removeTool(String name) {
		if (this.tools.containsKey(name)) {
			this.tools.remove(name);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#setTitle(String)
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public void setPropertiesFilename(String filename) {
		this.propertiesFileName = filename;
	}

	public String getPropertiesFilename() {
		return propertiesFileName;
	}

	/**
	 * Saves the propeties of the application to a file.
	 */
	public void saveProperties() {
        String filename = propertiesFileName;
	    if (filename == null || filename.equals("")) {
			return;
		}

		if (properties != null) {
			try {
				File file = new File(filename);
				if (!file.exists()) {
					boolean created = file.createNewFile();
                    if (!created) {
                        System.err.println(application.getName() +
                       		" Warning: Could not create project properties file at '" + filename + ".'");
                    }
				}
				FileOutputStream out = new FileOutputStream(file);
				String comment = "--- " + getApplication().getName() + ", " + getTitle() + " Project Properties ---";
				properties.store(out, comment);
				out.close();
			} catch (Exception e) {
                System.err.println(application.getName() + " Warning: Could not save project properties file at '" +
                		filename + ".'" + " Reason: " + e.getMessage());
			}
		}
	}

	/**
	 * Loads the propeties of the application from a file.
	 */
	public void loadProperties() {
        String fileName = propertiesFileName;
	    if (fileName == null || fileName.equals("")) {
	    	return;
	    }

	    File file = new File(fileName);
		try {
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
				this.properties.load(in);
				in.close();
			}
		} catch (Exception e) {
            System.err.println(application.getName() + " Warning: Could not load project properties file at '" + fileName + ".'" + " Reason: " + e.getMessage());
		}
	}

	/**
	 * Call this when you want this project to close.
	 */
	public void disposeProject() {
		if (application == null) {
			System.err.println("Warning - project already disposed.");
			return;
		}

		// first fire the closing event to let listeners perform save operations
		fireProjectClosingEvent();

		// now save and dispose project properties
		quickViewManager.save();
		quickViewManager.dispose();
		removeProjectActions();
        saveDataFiltersToProperties();
		saveProperties();

		// @tag Shrimp.ProjectClosingRemoveTool : this is where we dispose and remove all the tools for the project
		// this happens AFTER the projectClosing event is fired
		while (tools.size() > 0) {
			Object key = tools.keySet().iterator().next();
			ShrimpTool tool = (ShrimpTool) tools.remove(key);
			//System.out.println("Disposing Tool " + tool.getName());
			tool.disposeTool();
			tool.setProject(null);
		}

		// done after the closing event is fired because listeners might still want to access the data bean
		try {
			((DataBean) getBean(ShrimpProject.DATA_BEAN)).clearBufferedData();
		} catch (BeanNotFoundException e) {
		}
		beans.clear();

		// this must be the last call - it disposes the desktop frame (standalone) and removes the project
		application.closeProject(this);
		application = null;
	}

    private void loadDataFiltersFromProperties() {
        // TODO implement the loading of data filters from properties
    }

    private void saveDataFiltersToProperties() {
        // TODO implement the saving of data filters to properties
    }


	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#addProjectListener(ShrimpProjectListener)
	 */
	public void addProjectListener(ShrimpProjectListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#removeProjectListener(ShrimpProjectListener)
	 */
	public void removeProjectListener(ShrimpProjectListener listener) {
		this.listeners.remove(listener);
	}

	public void fireProjectActivatedEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpProjectListener) cloneListeners.get(i)).projectActivated(new ShrimpProjectEvent(this));
		}
	}

	public void fireProjectDeactivatedEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpProjectListener) cloneListeners.get(i)).projectDeactivated(new ShrimpProjectEvent(this));
		}
	}

	public void fireProjectClosingEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpProjectListener) cloneListeners.get(i)).projectClosing(new ShrimpProjectEvent(this));
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#refresh()
	 */
	public void refresh() {
		try {
			DataBean dataBean = (DataBean) getBean(DATA_BEAN);
			boolean dataBeanFiringEvents = dataBean.isFiringEvents();
			dataBean.setFiringEvents(false);
			dataBean.clearBufferedData();
			ShrimpTool [] appTools = application.getTools();
			for (int i = 0; i < appTools.length; i++) {
				ShrimpTool tool = appTools[i];
				tool.refresh();
				if (tool.getGUI().getParent() != null) {
					tool.getGUI().getParent().invalidate();
				}
			}
			ShrimpTool [] projectTools = getTools();
			for (int i = 0; i < projectTools.length; i++) {
				ShrimpTool tool = projectTools[i];
				tool.refresh();
				if (tool.getGUI().getParent() != null) {
					tool.getGUI().getParent().invalidate();
				}
			}
			dataBean.setFiringEvents(dataBeanFiringEvents);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject#getProjectURI()
     */
    public URI getProjectURI() {
        return projectURI;
    }

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "\"" + title + "\" " + super.toString();
	}

	/**
	 * Creates the quick view actions.  It will either load them from the project
	 * properties file, or failing that will create the default ones.
	 * {@link QuickViewAction}s are not stored in the ActionManager, instead they
	 * are stored in the {@link QuickViewManager} and written to the project properties
	 * file.
	 */
	public void createQuickViewActions() {
		boolean loaded = quickViewManager.loadQuickViews(true);
		if (!loaded) {
			createDefaultQuickViewActions();
		}
	}

	/**
	 * Creates the default quick view action and the quick view menu item.
	 */
	public void createDefaultQuickViewActions() {
		// default view
		quickViewManager.createDefaultView(this);
	}

	/**
	 * Creates quick view actions specific to Protege (.pprj) files.
	 */
	protected void createDefaultProtegeQuickViewActions() {

		// Nested Composite View
		List nodeTypesOfInterest = new ArrayList(quickViewManager.getAllNodeTypes());
		List arcTypesOfInterest = new ArrayList(quickViewManager.getAllArcTypes());
		List nodeTypesToOpen = new ArrayList();
		Map compositeArcs = new HashMap();
		compositeArcs.put(ProtegeDataBean.GROUP_NAME_SLOT_TEMPLATE_ALLOWED_CLASS, Collections.EMPTY_LIST);
		compositeArcs.put(ProtegeDataBean.GROUP_NAME_SLOT_INSTANCE, Collections.EMPTY_LIST);
		quickViewManager.createNestedCompositeView(JambalayaApplication.QUICK_VIEW_NESTED_COMPOSITE_VIEW, "icon_quick_view_composite.gif",
				this, nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_GRID_BY_ALPHA, nodeTypesToOpen, compositeArcs);


		// Flat Class & Instance Tree
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(ProtegeDataBean.CLASS_ART_TYPE);
		nodeTypesOfInterest.add(ProtegeDataBean.INSTANCE_ART_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE);
		quickViewManager.createFlatView(JambalayaApplication.QUICK_VIEW_CLASS_INSTANCE_TREE, "icon_quick_view_cih.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// Flat Class Tree
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(ProtegeDataBean.CLASS_ART_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
		quickViewManager.createFlatView(JambalayaApplication.QUICK_VIEW_CLASS_TREE, "icon_quick_view_ch.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		quickViewManager.createNestedTreemapView(this);
	}

	/**
	 * @tag Shrimp.quickview : Creates quick view actions
	 */
	protected void createDefaultJavaQuickViewActions() {
		// class hierarchy
        List nodeTypesOfInterest = new ArrayList();
        nodeTypesOfInterest.add(JavaDomainConstants.CLASS_ART_TYPE);
        List arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.EXTENDED_BY_REL_TYPE);
		quickViewManager.createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_CLASS_HIERARCHY, "icon_quick_view_ch.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// interface hierarchy
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.INTERFACE_ART_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.INTERFACE_EXTENDED_BY_REL_TYPE);
		quickViewManager.createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_INTERFACE_HIERARCHY, "icon_quick_view_ih.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// class & interface hierarchy
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.INTERFACE_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CLASS_ART_TYPE);
        nodeTypesOfInterest.add(SoftwareDomainConstants.FUNCTION_ART_TYPE); // a hack, not java, for rigi data
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.INTERFACE_EXTENDED_BY_REL_TYPE);
		arcTypesOfInterest.add(JavaDomainConstants.EXTENDED_BY_REL_TYPE);
		arcTypesOfInterest.add(JavaDomainConstants.IMPLEMENTED_BY_REL_TYPE);
		quickViewManager.createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_CLASS_INTERFACE_HIERARCHY, "icon_quick_view_cih.gif", this,
			nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		//call graph
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.METHOD_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CONSTRUCTOR_ART_TYPE);
        nodeTypesOfInterest.add(SoftwareDomainConstants.FUNCTION_ART_TYPE); // a hack, not java, for rigi data
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.CALLS_REL_TYPE);
		quickViewManager.createFlatView(JavaDomainConstants.JAVA_QUICK_VIEW_CALL_GRAPH, "icon_quick_view_call_graph.gif", this,
			nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);

		//control flow graph
		//@tag Shrimp.grouping : Abstracted this for use by orthogonal layout as well
		if (OrthogonalLayout.isLoaded()) {
			quickViewManager.createControlFlowQuickViewAction(this);
		}

		//@tag Shrimp.sequence : Abstracted this for use by sequence layout as well
        quickViewManager.createSequenceQuickViewAction(this);

		//Package Dependencies - Accesses
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.PROJECT_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.PACKAGE_ROOT_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.PACKAGE_ART_TYPE);				// "Package"
		nodeTypesOfInterest.add(JavaDomainConstants.PACKAGE_FRAGMENT_ART_TYPE);		// "Package Fragment"
		nodeTypesOfInterest.add(JavaDomainConstants.JAVA_FILE_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CLASS_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.INTERFACE_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.INITIALIZER_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CONSTRUCTOR_ART_TYPE);
        nodeTypesOfInterest.add(JavaDomainConstants.METHOD_ART_TYPE);
        nodeTypesOfInterest.add(JavaDomainConstants.FIELD_ART_TYPE);
        nodeTypesOfInterest.add(JavaDomainConstants.CONSTANT_ART_TYPE);
        nodeTypesOfInterest.add(SoftwareDomainConstants.VARIABLE_ART_TYPE); // a hack, not java, for rigi data
        nodeTypesOfInterest.add(SoftwareDomainConstants.FUNCTION_ART_TYPE); // a hack, not java, for rigi data
        nodeTypesOfInterest.add(SoftwareDomainConstants.CONSTANT_ART_TYPE); // a hack, not java, for rigi data
        nodeTypesOfInterest.add(SoftwareDomainConstants.DATATYPE_ART_TYPE); // a hack, not java, for rigi data
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.ACCESSES_REL_TYPE);
		Map compositeArcs = new HashMap();
		compositeArcs.put(JavaDomainConstants.JAVA_QUICK_VIEW_ACCESSES_REL_GROUP_NAME, arcTypesOfInterest);
		List nodeTypesToOpen = new ArrayList();
		nodeTypesToOpen.add(JavaDomainConstants.PROJECT_ART_TYPE);
		nodeTypesToOpen.add(JavaDomainConstants.PACKAGE_ROOT_ART_TYPE);
        nodeTypesToOpen.add(JavaDomainConstants.PACKAGE_FRAGMENT_ART_TYPE);
		quickViewManager.createNestedCompositeView(JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_ACCESSES,
				"icon_quick_view_pd_mc.gif", this, nodeTypesOfInterest, arcTypesOfInterest, nodeTypesToOpen, compositeArcs);

		//Package Dependencies - Calls
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.CALLS_REL_TYPE);
		compositeArcs = new HashMap();
		compositeArcs.put(JavaDomainConstants.JAVA_QUICK_VIEW_CALLS_REL_GROUP_NAME, arcTypesOfInterest);
		quickViewManager.createNestedCompositeView(JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS,
				"icon_quick_view_pd_mc.gif", this, nodeTypesOfInterest, arcTypesOfInterest, nodeTypesToOpen, compositeArcs);

		//Package Dependencies - Calls & Accesses
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.CALLS_REL_TYPE);
		arcTypesOfInterest.add(JavaDomainConstants.ACCESSES_REL_TYPE);
		compositeArcs = new HashMap();
		compositeArcs.put(JavaDomainConstants.JAVA_QUICK_VIEW_CALLS_ACCESSES_REL_GROUP_NAME, arcTypesOfInterest);
		quickViewManager.createNestedCompositeView(JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS_ACCESSES,
				"icon_quick_view_pd_mc.gif",  this, nodeTypesOfInterest, arcTypesOfInterest, nodeTypesToOpen, compositeArcs);

		//fan in/out
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(JavaDomainConstants.METHOD_ART_TYPE);
		nodeTypesOfInterest.add(JavaDomainConstants.CONSTRUCTOR_ART_TYPE);
        nodeTypesOfInterest.add(SoftwareDomainConstants.FUNCTION_ART_TYPE); // a hack, not java, for rigi data
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(JavaDomainConstants.CALLS_REL_TYPE);
        quickViewManager.createQueryViewAction(JavaDomainConstants.JAVA_QUICK_VIEW_FAN_IN_OUT, "icon_quick_view_fio.gif", this,
        	nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_HORIZONTAL, DisplayConstants.LABEL_MODE_FIXED, 1, 1);

        quickViewManager.createNestedTreemapView(this);
	}


	protected void createDefaultOBOQuickViewActions() {
		// IS_A hierarchy
		List nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(OBODataBean.TERM_ART_TYPE);
		List arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(OBODataBean.IS_A_REL_TYPE);
		quickViewManager.createFlatView(OBODataBean.IS_A_HIERARCHY, "icon_quick_view_ch.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// PART_OF hierarchy
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(OBODataBean.TERM_ART_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(OBODataBean.PART_OF_REL_TYPE);
		quickViewManager.createFlatView(OBODataBean.PART_OF_HIERARCHY, "icon_quick_view_ih.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		quickViewManager.createNestedTreemapView(this);
	}

    protected void setupAttrVisVarBeanForJavaDomain() {
        try {
    		// set some default colors
    		AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
    		boolean firingEvents = attrToVisVarBean.isFiringEvents();
    		attrToVisVarBean.setFiringEvents(false);

    		String[] types = new String[] {
    			JavaDomainConstants.PROJECT_ART_TYPE,
    			JavaDomainConstants.PACKAGE_ROOT_ART_TYPE,
    			JavaDomainConstants.PACKAGE_ART_TYPE,
    			JavaDomainConstants.JAVA_FILE_ART_TYPE,
    			JavaDomainConstants.CLASS_ART_TYPE,
    			JavaDomainConstants.INTERFACE_ART_TYPE
    		};

    		Color[] colors = new Color[] {
    			JavaDomainConstants.COLOR_PROJECT_ART_TYPE,
    			JavaDomainConstants.COLOR_PACKAGE_ROOT_ART_TYPE,
    			JavaDomainConstants.COLOR_PACKAGE_FRAGMENT_ART_TYPE,
    			JavaDomainConstants.COLOR_JAVA_FILE_ART_TYPE,
    			JavaDomainConstants.COLOR_CLASS_ART_TYPE,
    			JavaDomainConstants.COLOR_INTERFACE_ART_TYPE
    		};

    		for (int i = 0; i < colors.length; i++) {
    			Color color = colors[i];
    			String type = types[i];
    			attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, type, color);
    		}

    		attrToVisVarBean.setFiringEvents(firingEvents);
    	} catch (BeanNotFoundException e2) {
    		e2.printStackTrace();
    	}
    }


}