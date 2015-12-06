/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTabbedPane;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLClass;

import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.OWLIcons;
import org.protege.editor.owl.ui.UIHelper;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.EllipseNodeShape;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * JambalayaProject represents a single project in Jambalaya.
 *
 * @author Nasir Rather, Rob Lintern, Chris Callendar
 */
public class JambalayaProject extends AbstractShrimpProject {

	public static final String EXT_OWL = "owl";
	public static final String PROJECT_TYPE_OWL = "OWL";

    public static final Color CLASS_COLOR = new Color (245, 224, 171);
    public static final Color META_CLASS_COLOR = new Color (249, 238, 211);
	public static final Color SYSTEM_CLASS_COLOR = new Color (249, 238, 211);
	public static final Color INSTANCE_COLOR = new Color (223, 214, 223);
    public static final Color DIRECT_SUBCLASS_COLOR = Color.BLUE;
    public static final Color DIRECT_INSTANCE_COLOR = Color.RED;

    private boolean attrToVisVarBeanSetup = false;
	 
	private OWLModelManager modelManager = null;
	private OWLEditorKit editorKit = null;

	/**
	 * Create JambalayaProject.
	 * @param application The application that contains this project.
	 * @param protegeProject The Protege project that this Jambalaya project should be attached to.
	 * @param sourceURI The project URI
	 */
	public JambalayaProject(JambalayaApplication application, 
									OWLModelManager mm, OWLEditorKit ek, OWLOntology ontology, URI sourceURI) {
		super(application, PROJECT_TYPE_OWL);
		modelManager = mm;
		editorKit = ek;
		setProjectURI(ontology, sourceURI);
		this.title = getProjectTitle(ontology);
		this.projectActions = new Vector();

        // filename where the properties of this project will be stored
        String appName = application.getName().replace(' ', '_');
        if (projectURI.getScheme().equals("file")) {
        	File pprjFile = new File(projectURI.getPath());
        	File propFile = new File(pprjFile.getParentFile(), title + "_" + appName + ".properties");
            propertiesFileName =  propFile.getAbsolutePath();
        } else {
            try {
                propertiesFileName = System.getProperty("java.io.tmpdir");
                if (propertiesFileName != null) {
                    propertiesFileName += title + "_" + appName  + ".properties";
                }
            } catch (SecurityException e){
                // thrown if don't have proper security priviledges
                propertiesFileName = null;
            }
        }

		loadProperties();

        // create data bean
		ProtegeDataBean dataBean = new ProtegeDataBean(modelManager, editorKit, ontology);
		dataBean.setDefaultRootClses();

		// add databean to this project
		addBean(ShrimpProject.DATA_BEAN, dataBean);

		createQuickViewActions();
	}

	private void setProjectURI(OWLOntology ontology, URI sourceURI) {
		this.projectURI = sourceURI;
		if (projectURI == null) {
			projectURI = ontology.getOntologyID().getOntologyIRI().toURI();
			if (projectURI == null) {
			    try {
	                projectURI = new URI("file:///unknown");
	            } catch (URISyntaxException e) {}
			}
		}
	}

	public URI getProjectURI() {
		return projectURI;
	}

	/**
	 * Determines a title for the given project.
	 * If the project is null, the "Null Project" is returned.
	 * If the project has a name then that is returned.
	 * Otherwise the project URI or loading URI is used to determine a name.
	 * Defaults to "Unknown".
	 * (Public for applets).
	 * @param project
	 * @return String title - shouldn't be null
	 */
	public String getProjectTitle(OWLOntology project) {
		String title = null;
		if (project != null) {
			title = projectURI.toString();
			int index = title.lastIndexOf('/');
			if (index != -1) {
				 title = title.substring(index + 1);
			}
		} else {
			title = "Null Project";
		}
		if (title == null) {
			title = "Unknown";
		}
		return title;
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject#getBean(java.lang.String)
     */
    public Object getBean(String name) throws BeanNotFoundException {
        Object bean = super.getBean(name);
        if (bean != null && name.equals(ShrimpProject.ATTR_TO_VIS_VAR_BEAN) && !attrToVisVarBeanSetup) {
            setupAttrToVisVarBean((AttrToVisVarBean)bean);
        }
        return bean;
    }

	private void setupAttrToVisVarBean (AttrToVisVarBean attrToVisVarBean) {
		boolean firingEvents = attrToVisVarBean.isFiringEvents();
		attrToVisVarBean.setFiringEvents(false);

		//set some default colours for classes and instances
		//regular protege colours
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.CLASS_ART_TYPE, CLASS_COLOR);
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.INSTANCE_ART_TYPE, INSTANCE_COLOR);

		//owl plugin colours
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.PRIMITIVE_CLASS_TYPE, CLASS_COLOR);
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.DEFINED_CLASS_TYPE, CLASS_COLOR);
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.INDIVIDUAL_TYPE, INSTANCE_COLOR);
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.LOGICAL_OPERATION_TYPE, new Color(249, 241, 168));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, ProtegeDataBean.RESTRICTION_TYPE, new Color(249, 241, 168));

		//make restrictions round
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_SHAPE, ProtegeDataBean.RESTRICTION_TYPE, new EllipseNodeShape());

		//make logical expressions round
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_SHAPE, ProtegeDataBean.LOGICAL_OPERATION_TYPE, new EllipseNodeShape());

		// setup some default icons for node types
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.CLASS_ART_TYPE, Icons.getIcon("class.gif"));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.INSTANCE_ART_TYPE, Icons.getIcon("instance.gif"));

		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.PRIMITIVE_CLASS_TYPE, OWLIcons.getIcon("class.primitive.png"));      
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.DEFINED_CLASS_TYPE, OWLIcons.getIcon("class.defined.png"));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.INDIVIDUAL_TYPE, OWLIcons.getIcon("individual.png"));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.LOGICAL_OPERATION_TYPE, OWLIcons.getIcon("OWLUnionClass.gif"));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.RESTRICTION_TYPE, OWLIcons.getIcon("OWLRestriction.gif"));
		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.ENUMERATION_CLASS_TYPE, OWLIcons.getIcon("OWLEnumeratedClass.gif"));

		// give specific relationship types some default arc styles
		ArcStyle straightDottedArcStyle = new StraightDottedLineArcStyle();
		ArcStyle straightSolidArcStyle = new StraightSolidLineArcStyle();

        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE, DIRECT_SUBCLASS_COLOR);
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE, DIRECT_INSTANCE_COLOR);

        try {
            ProtegeDataBean dataBean = (ProtegeDataBean) getBean(ShrimpProject.DATA_BEAN);
            Vector allRelTypes = dataBean.getRelationshipTypes(false, true);
            for (Iterator iter = allRelTypes.iterator(); iter.hasNext();) {
            	String relType = (String) iter.next();
            	String groupName = dataBean.getDefaultGroupForRelationshipType(relType);
            	boolean isSystem = groupName.equals(ProtegeDataBean.GROUP_NAME_SYSTEM);
            	boolean isTemplateAllowed = relType.indexOf(ProtegeDataBean.SUFFIX_SLOT_TEMPLATE_ALLOWED_CLASS) != -1;
            	boolean isTemplateValue = relType.indexOf(ProtegeDataBean.SUFFIX_SLOT_TEMPLATE_VALUE) != -1;
            	boolean isDomainRange = relType.indexOf(ProtegeDataBean.SUFFIX_DOMAIN_RANGE) != -1;
            	boolean isInherited = relType.indexOf(ProtegeDataBean.SUFFIX_INHERITED) != -1;
            	if (isSystem) {
            		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, relType, straightSolidArcStyle);
            	} else if (isTemplateAllowed || isTemplateValue || isDomainRange || isInherited) {
            		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, relType, straightDottedArcStyle);
            	} else {
            		attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, relType, straightSolidArcStyle);
            	}
            }
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }

		attrToVisVarBean.setFiringEvents(firingEvents);
		attrToVisVarBeanSetup = true;
	}

	public void createDefaultQuickViewActions() {
		super.createDefaultQuickViewActions();
		createDefaultOWLQuickViewActions();
	}

	protected void createDefaultOWLQuickViewActions() {
		QuickViewManager quickViewManager = getQuickViewManager();

		// Nested Composite View
		List nodeTypesOfInterest = new ArrayList(quickViewManager.getAllNodeTypes());
		List arcTypesOfInterest = new ArrayList(quickViewManager.getAllArcTypes());
		List nodeTypesToOpen = new ArrayList();
		Map compositeArcs = new HashMap();
		compositeArcs.put(ProtegeDataBean.GROUP_NAME_PROPERTY_RESTRICTIONS, Collections.EMPTY_LIST);
		compositeArcs.put(ProtegeDataBean.GROUP_NAME_CONNECTING_INDIVIDUALS, Collections.EMPTY_LIST);
		quickViewManager.createNestedCompositeView(JambalayaApplication.QUICK_VIEW_NESTED_COMPOSITE_VIEW, "icon_quick_view_composite.gif",
				this, nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_GRID_BY_ALPHA, nodeTypesToOpen, compositeArcs);

		// Flat Class & Individual Tree
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(ProtegeDataBean.DEFINED_CLASS_TYPE);
		nodeTypesOfInterest.add(ProtegeDataBean.PRIMITIVE_CLASS_TYPE);
		nodeTypesOfInterest.add(ProtegeDataBean.INDIVIDUAL_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE);
		quickViewManager.createFlatView(JambalayaApplication.QUICK_VIEW_CLASS_INDIVIDUAL_TREE, "icon_quick_view_cih.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// Flat Class Tree
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(ProtegeDataBean.DEFINED_CLASS_TYPE);
		nodeTypesOfInterest.add(ProtegeDataBean.PRIMITIVE_CLASS_TYPE);
		arcTypesOfInterest = new ArrayList();
		arcTypesOfInterest.add(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
		quickViewManager.createFlatView(JambalayaApplication.QUICK_VIEW_CLASS_TREE, "icon_quick_view_ch.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_TREE_VERTICAL, DisplayConstants.LABEL_MODE_FIXED);

		// Flat Domain-Range Spring
		nodeTypesOfInterest = new ArrayList();
		nodeTypesOfInterest.add(ProtegeDataBean.DEFINED_CLASS_TYPE);
		nodeTypesOfInterest.add(ProtegeDataBean.PRIMITIVE_CLASS_TYPE);
		arcTypesOfInterest = new ArrayList();
		try {
			ProtegeDataBean dataBean = (ProtegeDataBean) getBean(ShrimpProject.DATA_BEAN);
			Vector allRelTypes = dataBean.getRelationshipTypes(false, true);
			for (Iterator iter = allRelTypes.iterator(); iter.hasNext(); ) {
				String relType = (String) iter.next();
				String groupName = dataBean.getDefaultGroupForRelationshipType(relType);
				if (groupName.equals(ProtegeDataBean.GROUP_NAME_DOMAIN_RANGE)) {
					arcTypesOfInterest.add(relType);
				}
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
		quickViewManager.createFlatView(JambalayaApplication.QUICK_VIEW_DOMAIN_RANGE, "icon_quick_view_fio.gif", this,
				nodeTypesOfInterest, arcTypesOfInterest, LayoutConstants.LAYOUT_SPRING, DisplayConstants.LABEL_MODE_FIXED);

		// Nested Treemap
		quickViewManager.createNestedTreemapView(this);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject#createProjectActions()
	 */
	public void createProjectActions() {
		super.createProjectActions();

		ActionManager actionManager = getActionManager();
		boolean firingEvents = actionManager.getFiringEvents();
		actionManager.setFiringEvents(false);

		ShrimpAction action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_CHANGE_ROOT_CLASSES,
				ResourceHandler.getIcon("icon_change_hierarchy.gif")) {
			public void actionPerformed(ActionEvent e) {
				changeRootClasses();
			}
		};
		addProjectAction(action, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_D, 1);

		action = new ShowNodePropertiesAdapter(this, ShrimpProject.SHRIMP_VIEW);
		addProjectAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_F, 1);

		action = new ShowArcPropertiesAdapter(this, ShrimpProject.SHRIMP_VIEW);
		addProjectAction(action, ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_C, 1);

		actionManager.setFiringEvents(firingEvents);
	}

	private void changeRootClasses() {
		try {
			ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
			ProtegeDataBean dataBean = (ProtegeDataBean)getBean(ShrimpProject.DATA_BEAN);

			if (editorKit != null) {
				UIHelper hlp = new UIHelper(editorKit);
				OWLClass cls = hlp.pickOWLClass();
				if (cls != null) {
					HashSet clsesSet = new HashSet();
					clsesSet.add(cls);
					dataBean.setRootClses(clsesSet);
					// if the jambalaya tab is open then set the roots of the classes tree
					JambalayaView view = JambalayaView.globalInstance();
					if (view != null) {
						view.setRootClses(clsesSet);
						refresh();
						shrimpView.addDefaultRootNodes(true);
					}
				}
			}
		} catch (BeanNotFoundException e1) {
			//e1.printStackTrace();
		} catch (ShrimpToolNotFoundException e1) {
			//e1.printStackTrace();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject#refresh()
	 */
	public void refresh() {
	    super.refresh();
	    // remove asterisk from the name of the jambalaya tab
        JambalayaView view = JambalayaView.globalInstance();
        if (view != null)
            view.setCaption("");
	}

}
