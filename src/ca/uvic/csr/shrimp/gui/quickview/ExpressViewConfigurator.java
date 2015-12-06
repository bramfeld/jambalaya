/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.usercontrols.OpenAllAdapter;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Contains a view configuration and methods to create a view based on this configuration.
 *
 * @author Rob Lintern
 */
public class ExpressViewConfigurator {

	private static final int DEFAULT_NODES_TO_SHOW = 15; //the default maximum number of nodes to show in nested view
	private static final int DEFAULT_LEVELS_TO_SHOW = 1; //the default maximum number of levels to show in nested view
	private static final int DEFAULT_ARCS_TO_SHOW = 20; // the default maximum number of arcs to show in nested view;

	private static final boolean DEFAULT_CHANGE_DATA_FILTERS = true;
    private static final boolean DEFAULT_INCLUDE_UNCONNECTED_NODES = false;

    public static final String DEFAULT_LAYOUT_MODE = LayoutConstants.LAYOUT_GRID_BY_TYPE;
    public static final String DEFAULT_LABEL_MODE = DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE;

    private int levelsToOpen = DEFAULT_LEVELS_TO_SHOW;
	private int numNodesToShow = DEFAULT_NODES_TO_SHOW;
	private int numArcsToShow = DEFAULT_ARCS_TO_SHOW;
    private boolean includeUnconnectedNodes = DEFAULT_INCLUDE_UNCONNECTED_NODES;
    private boolean changeDataFilters = DEFAULT_CHANGE_DATA_FILTERS;

	protected Collection nodeTypesOfInterest = Collections.EMPTY_LIST;
	protected Collection arcTypesOfInterest = Collections.EMPTY_LIST;
	private Map compositeArcs = new HashMap(); //key is a desired group name, value is a collection of arc types in the group
	private String [] cprels = new String [0]; //child/parent relationships
    private boolean inverted = false;
	private Collection nodeTypesToOpen = Collections.EMPTY_LIST;
	protected String layoutMode = DEFAULT_LAYOUT_MODE;
	protected String labelMode = DEFAULT_LABEL_MODE;

	protected ShrimpProject project = null;
	protected DataBean dataBean = null;
	protected ShrimpView shrimpView = null;
	protected DisplayBean displayBean = null;
	protected DataDisplayBridge dataDisplayBridge = null;
    private FilterBean displayFilterBean = null;
    private FilterBean dataFilterBean = null;
	protected SelectorBean selectorBean = null;
	private CompositeArcsManager compositeArcsManager = null;
	protected AttrToVisVarBean attrToVisVarBean = null;

	public ExpressViewConfigurator(ShrimpProject project) {
		this.project = project;
	}

    protected void getToolsAndBeans() throws ShrimpToolNotFoundException, BeanNotFoundException {
        dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
        dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
		shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
		displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
		dataDisplayBridge = displayBean.getDataDisplayBridge();
		displayFilterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
		selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
		compositeArcsManager = dataDisplayBridge.getCompositeArcsManager();
		attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
    }

    public void dispose() {
    	nodeTypesOfInterest = Collections.EMPTY_LIST;
    	arcTypesOfInterest = Collections.EMPTY_LIST;
    	nodeTypesToOpen = Collections.EMPTY_LIST;
    	compositeArcs = new HashMap();
    	project = null;
    	dataBean = null;
    	dataFilterBean = null;
    	shrimpView = null;
    	displayBean = null;
    	dataDisplayBridge = null;
    	displayFilterBean = null;
    	selectorBean = null;
    	compositeArcsManager = null;
    	attrToVisVarBean = null;
	}

    /**
     * Configures this view:
    	1.	show progress
    	2.	turn off event firing
    	3.	get references to project and required tools and beans
    	4.	set data filters
    		a.	remove all filters
    		b.	add filters for things that should be hidden
    	5.	set display filters
    		a.	remove all filters (including id filters)
    		b.	add filters for things that should be hidden
    	6.	return attribute and vis var mapping to their defaults
    	7.	change hierarchy if necessary
    	8.	clear and update composite arcs manager with new arc types and groups
    	9.	refresh the project (only necessary to do whole project if data filters have changed)
    	10.	change labeling mode
    	11.	add root nodes to the display
    	12.	create composite arcs if needed
    	13.	if nesting, open up nodes to desired level
    	14.	layout nodes with layout specified
    	15.	turn on event firing
    	16.	hide progress
     */
    public void configureView(String configDescription) {
		if (project == null) {
			return;
		}

		try {
		    getToolsAndBeans();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
			return;
		}
        boolean displayBeanVisible = displayBean.isVisible();
        displayBean.setVisible(false);
        boolean dataBeanFiringEvents = dataBean.isFiringEvents();
        dataBean.setFiringEvents(false);
		try {
			ProgressDialog.showProgress();
            ProgressDialog.setSubtitle("Setting " + ApplicationAccessor.getApplication().getName() + " to '" + configDescription + "'");

            ProgressDialog.setNote("Clearing View", true);
            shrimpView.clear(); // clear the shrimp view first ... makes things go a bit faster

			setupAttrToVisVarBean();

			// make sure that data and display filters are configured
            applyDataFilters();
            applyDisplayFilters();

			// set the hierarchy of the shrimp view
			boolean changeCprels = !CollectionUtils.haveSameElements(shrimpView.getCprels(), cprels) || shrimpView.isInverted() != inverted;
			if (changeCprels) {
				ProgressDialog.setNote("Change hierarchy to \"" + CollectionUtils.arrayToString(cprels) + "\"...", true);
				shrimpView.setCprels(cprels, inverted, false);
			}

			updateArcGroups();

			ProgressDialog.setNote("Refreshing...", true);
			project.refresh();

            ProgressDialog.setNote("Setting label mode to \"" + labelMode + "\"...", true);
			displayBean.setDefaultLabelMode(labelMode);
			selectorBean.setSelected(DisplayConstants.LABEL_MODE, labelMode);

			addRootNodes();
			addComposites();  //TODO find out if this needs to be called?
			openRootNodes();

            applyLayout();
            displayBean.focusOnExtents(false); // ensures the everything fits within view after applying layout

		} catch (Exception e) {
            String showWarningStr = ApplicationAccessor.getProperty(DisplayBean.PROPERTY_KEY__SHOW_EXPRESS_VIEW_WARNING,
            														DisplayBean.DEFAULT_SHOW_EXPRESS_VIEW_WARNING);
            boolean showWarning = Boolean.valueOf(showWarningStr).booleanValue();
            if (showWarning) {
                String message = "Sorry, there was a problem setting the view to '" + configDescription + "'";
                System.err.println(message);
                e.printStackTrace();
                JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message);
            }
		} finally {
            displayBean.setVisible(displayBeanVisible);
			ProgressDialog.tryHideProgress();
            dataBean.setFiringEvents(dataBeanFiringEvents);
		}
    }

    private void openRootNodes() {
        if (cprels.length > 0) {
            if (nodeTypesToOpen.size() > 0) {
				// open everything
				ProgressDialog.setNote("Opening up to " + CollectionUtils.collectionToString(nodeTypesToOpen) + " nodes...", true);
				OpenAllAdapter openAllAdapter = new OpenAllAdapter(project, shrimpView, nodeTypesToOpen);
				openAllAdapter.startAction();
				Vector rootNodes = displayBean.getDataDisplayBridge().getRootNodes();
				boolean atLeastOneRootOpen = false;
				for (Iterator iter = rootNodes.iterator(); iter.hasNext() &&!atLeastOneRootOpen;) {
                    ShrimpNode rootNode = (ShrimpNode) iter.next();
                    atLeastOneRootOpen = rootNode.getPanelMode().equals(PanelModeConstants.CHILDREN);
                }
				if (!atLeastOneRootOpen) {
				    displayBean.setPanelMode(rootNodes, PanelModeConstants.CHILDREN);
				}
            } else {
                defaultOpenNested();
            }
        } else {
            // don't open anything if hierarchy is flat
        }
    }

    /**
     * Apply the specified layout.
     */
    private void applyLayout() {
        if (cprels.length > 0) {
            if (nodeTypesToOpen.size() > 0) {
    			// Filter nodes to just those visible
    			Vector nodesToLayout = new Vector();
    			Vector visibleNodes = displayBean.getVisibleNodes();
    			for (Iterator iter = visibleNodes.iterator(); iter.hasNext();) {
    				ShrimpNode node = (ShrimpNode) iter.next();
    				Vector childNodes = displayBean.getDataDisplayBridge().getChildNodes(node);
    				Vector visibleChildNodes = new Vector();
    				for (Iterator iterator = childNodes.iterator(); iterator.hasNext(); ) {
    					ShrimpNode childNode = (ShrimpNode) iterator.next();
    					if (childNode.isVisible()) {
    						visibleChildNodes.add(childNode);
    					}
    				}
    				if (visibleChildNodes.size() > 1) {
    					for (Iterator iterator = visibleChildNodes.iterator(); iterator.hasNext(); ) {
    						ShrimpNode childNode = (ShrimpNode) iterator.next();
    						if (!nodesToLayout.contains(childNode)) {
    							nodesToLayout.add(childNode);
    						}
    					}
    				}
    			}
    			ProgressDialog.setNote("Laying out nodes...", true);
                if (layoutMode != null) {
        			// Lay out the graph
                    displayBean.setLayoutMode(nodesToLayout, layoutMode, false, false);
                }
            } else {
				// do nothing if nested hierarchy and no layout
            }
        } else {
			// Lay out the graph
			ProgressDialog.setNote("Laying out nodes...", true);
			displayBean.setLayoutMode(dataDisplayBridge.getRootNodes(), layoutMode, false, false);
        }
    }

    private void addRootNodes() {
        if (cprels.length == 0) {
            addFlatRootNodes();
        } else {
            shrimpView.addDefaultRootNodes(true);
        }
		// @tag Shrimp(grouping)
		// update node names from properties file
		try {
			ShrimpView view = (ShrimpView)this.project.getTool(ShrimpProject.SHRIMP_VIEW);
			new RenameSelectedArtifactsAdapter(this.project, view).updateNodeNames();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}

    }

    protected void addFlatRootNodes() {
        // if need be, take out any src artifacts that don't participate in any rels of interest
        Vector artifactsToDisplay = dataBean.getArtifacts(true);
        if (!includeUnconnectedNodes) {
            /* Calling dataBean.getrelationships(true) here ensures that all incoming and outgoing rels
             * are created properly, especially in cases where all incoming relationships cannot
             * be found.
             */
            dataBean.getRelationships(true);
            for (Iterator artIter = artifactsToDisplay.iterator(); artIter.hasNext();) {
                Artifact art = (Artifact) artIter.next();
                Vector rels = art.getRelationships();
                boolean hasRelOfInterest = false;
                for (Iterator relIter = rels.iterator(); relIter.hasNext() && !hasRelOfInterest;) {
                    Relationship rel = (Relationship) relIter.next();
                    hasRelOfInterest = arcTypesOfInterest.contains(rel.getType());
                }
                if (!hasRelOfInterest) {
                    artIter.remove();
                }
            }
            if (artifactsToDisplay.isEmpty()) {
                String showWarningStr = ApplicationAccessor.getProperties().getProperty(
                		DisplayBean.PROPERTY_KEY__SHOW_NO_ROOT_NODES_WARNING,
                		DisplayBean.DEFAULT_SHOW_NO_ROOT_NODES_WARNING);
                boolean showWarning = Boolean.valueOf(showWarningStr).booleanValue();
                if (showWarning) {
                    JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
                    	"Sorry, there is nothing to see since there are no nodes connected by the arc types specified.");
                }
                return;
            }
        }
        if (ShrimpUtils.shouldShowManyRoots(artifactsToDisplay.size())) {
            Vector nodesToDisplay = displayBean.getDataDisplayBridge().getShrimpNodes(artifactsToDisplay, true);
			displayBean.addObject(nodesToDisplay);
			displayBean.setVisible(nodesToDisplay, true, true);
        }
    }

    /**
     * Call this method to create a new arc group, with composite arcs enabled, containing the given arcTypes.
     * @param arcTypes
     * @param groupName
     */
	private void updateArcGroups() {
		compositeArcsManager.clear();

		Set arcTypesInComposites = new HashSet();
		for (Iterator iter = compositeArcs.keySet().iterator(); iter.hasNext();) {
            String groupName = (String) iter.next();
    		RelTypeGroup group = compositeArcsManager.createRelTypeGroup(groupName);
    		Collection arcTypes = (Collection) compositeArcs.get(groupName);
    		arcTypesInComposites.addAll(arcTypes);
    		for (Iterator iterator = arcTypes.iterator(); iterator.hasNext();) {
                String arcType = (String) iterator.next();
    			compositeArcsManager.addRelTypeToGroup(arcType, group);
    		}

    		Color groupColor = Color.RED;
    		if (arcTypes.size() == 1) {
    			Color typeColor = (Color)attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, arcTypes.iterator().next());
    			if (typeColor != null) {
    				groupColor = typeColor;
    			}
    		}
    		group.setCompositeColor(groupColor);

    		attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, group.getGroupName(), groupColor);
    		attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, group.getGroupName(), group.getCompositeStyle());

        }

		// prime the composite arc manager for rel types
		Vector allRelTypes = dataBean.getRelationshipTypes(false, true);
		allRelTypes.removeAll(arcTypesInComposites);
		for (Iterator iter = allRelTypes.iterator(); iter.hasNext();) {
			String nonCompType = (String) iter.next();
			compositeArcsManager.getRelTypeGroupForType(nonCompType); // will create appropriate group for this rel type
		}
	}

	private void addComposites() {
	    if (!compositeArcs.isEmpty()) {
			// create composites for the arc types of interest
			ProgressDialog.setNote("Creating high-level arcs...", true);
			for (Iterator iter = compositeArcs.keySet().iterator(); iter.hasNext();) {
	            String groupName = (String) iter.next();
	    		RelTypeGroup group = compositeArcsManager.createRelTypeGroup(groupName);
	    		compositeArcsManager.setCompositesEnabled(group, true);
			}
			Collection arcs = compositeArcsManager.getCompositeArcs();
			for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
				ShrimpCompositeArc arc = (ShrimpCompositeArc) iterator.next();
				arc.updateVisibility();
			}
	    }
	}

	private void applyDisplayFilters() {
        Vector allArtTypes = dataBean.getArtifactTypes(true, true);
        Vector allRelTypes = dataBean.getRelationshipTypes(true, true);

        //display filters
        boolean firingEvents = displayFilterBean.isFiringEvents();
        displayFilterBean.setFiringEvents(false);

	    // remove all id filters from the display
	    displayFilterBean.removeNominalAttrFilter(AttributeConstants.NOM_ATTR_ARTIFACT_ID, /*String.class?*/ Long.class, FilterConstants.ARTIFACT_FILTER_TYPE);

        displayFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, allArtTypes, true);
        displayFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, nodeTypesOfInterest, false);

        // remove all relationship id filters from the display
        try {
			displayFilterBean.removeFilters(displayFilterBean.getFiltersOfType(Filter.NOMINAL_ATTRIBUTE_FILTER, FilterConstants.RELATIONSHIP_FILTER_TYPE));
		} catch (FilterNotFoundException e) {}
        displayFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, allRelTypes, true);
        displayFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, arcTypesOfInterest, false);
        displayFilterBean.setFiringEvents(firingEvents);

	}

    public void applyDataFilters() {
        if (changeDataFilters) {
            if (dataBean == null || dataFilterBean == null) {
                if (project != null) {
                    try {
                        getToolsAndBeans();
                    } catch (ShrimpToolNotFoundException e) {
                        e.printStackTrace();
                        return;
                    } catch (BeanNotFoundException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            Vector allArtTypes = dataBean.getArtifactTypes(true, true);
            Vector allRelTypes = dataBean.getRelationshipTypes(true, true);

            boolean firingEvents = dataFilterBean.isFiringEvents();
            dataFilterBean.setFiringEvents(false);

            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE, allArtTypes, true);
            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE, nodeTypesOfInterest, false);
            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, allRelTypes, true);
            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, arcTypesOfInterest, false);

            // make sure the cprel types are imported into the databean but filtered from the display
            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, Arrays.asList(cprels), false);

            // make sure the default cprel types are imported into the databean but filtered from the display
            // this is to ensure that the Hierarchical View works as expected, although
            // importing all this information is not desireable if not showing the HV
            // TODO only bring in default cprels when absolutely neccessary
            dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, Arrays.asList(dataBean.getDefaultCprels()), false);

            dataFilterBean.setFiringEvents(firingEvents);
        }
    }
	private void defaultOpenNested() {
		int count = 1;
		//keep opening nodes...
		//stop if more than X nodes, Y arcs, or Z levels are showing
		Vector visibleNodes = displayBean.getVisibleNodes();
		Vector visibleArcs = displayBean.getVisibleArcs();
		while(count <= levelsToOpen && visibleNodes.size() < numNodesToShow && visibleArcs.size() < numArcsToShow) {
			count++;
			Iterator it = visibleNodes.iterator();
			while(it.hasNext()) {
				ShrimpNode sn = (ShrimpNode) it.next();
				displayBean.openNode(sn);
			}
			Vector nowVisibleNodes = displayBean.getVisibleNodes();
			// see whats visible now
			if (CollectionUtils.haveSameElements(visibleNodes, nowVisibleNodes)) {
				break;
			}
			visibleNodes = nowVisibleNodes;
			visibleArcs = displayBean.getVisibleArcs();
			ProgressDialog.setNote("Opened level " + count + " (" + visibleNodes.size() + " nodes and " + visibleArcs.size() + " arcs showing) ...", true);
		}

	}

	protected void setupAttrToVisVarBean() {
		String attrName = AttributeConstants.NOM_ATTR_ARTIFACT_TYPE;
		VisualVariable visVar = attrToVisVarBean.getVisVar(VisVarConstants.VIS_VAR_NODE_COLOR);
		if (visVar == null || !(visVar instanceof NominalNodeColorVisualVariable)) {
			if (visVar != null) {
				attrToVisVarBean.removeVisVar(visVar);
			}
			visVar = new NominalNodeColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
			attrToVisVarBean.addVisVar(visVar);
		}
		attrToVisVarBean.mapAttrToVisVar(attrName, VisVarConstants.VIS_VAR_NODE_COLOR);
	}

    /**
     * @param arcTypesOfInterest The arcTypesOfInterest to set.
     */
    public void setArcTypesOfInterest(Collection arcTypesOfInterest) {
        this.arcTypesOfInterest = (arcTypesOfInterest != null ? arcTypesOfInterest : Collections.EMPTY_LIST);
    }

	/**
	 * @return the arcTypesOfInterest
	 */
	public Collection getArcTypesOfInterest() {
		return arcTypesOfInterest;
	}

    /**
     * A map of the group name to a list of arc types (Strings).
     * @param compositeArcs The compositeArcs to set.
     */
    public void setCompositeArcs(Map compositeArcs) {
        this.compositeArcs = (compositeArcs != null ? compositeArcs : Collections.EMPTY_MAP);
    }

	/**
     * A map of the group name to a list of arc types (Strings).
	 * @return the compositeArcs
	 */
	public Map getCompositeArcs() {
		return compositeArcs;
	}

    /**
     * @param cprels The cprels to set.
     */
    public void setCprels(String[] cprels) {
        this.cprels = cprels;
    }

	/**
	 * @return the cprels
	 */
	public String[] getCprels() {
		return cprels;
	}

    /**
     * @param labelMode The labelMode to set.
	 * @see DisplayConstants
     */
    public void setLabelMode(String labelMode) {
        this.labelMode = labelMode;
    }

	/**
	 * @return the labelMode
	 * @see DisplayConstants
	 */
	public String getLabelMode() {
		return labelMode;
	}

    /**
     * @param layoutMode The layoutMode to set.
	 * @see LayoutConstants
     */
    public void setLayoutMode(String layoutMode) {
        this.layoutMode = layoutMode;
    }

	/**
	 * @return the layoutMode
	 * @see LayoutConstants
	 */
	public String getLayoutMode() {
		return layoutMode;
	}

    /**
     * @param nodeTypesOfInterest The nodeTypesOfInterest to set.
     */
    public void setNodeTypesOfInterest(Collection nodeTypesOfInterest) {
        this.nodeTypesOfInterest = (nodeTypesOfInterest != null ? nodeTypesOfInterest : Collections.EMPTY_LIST);
    }

	/**
	 * The node types of interest - won't be null.
	 * @return the node types of interest
	 */
	public Collection getNodeTypesOfInterest() {
		return nodeTypesOfInterest;
	}

    /**
     * @param nodeTypesToOpen The nodeTypesToOpen to set.
     */
    public void setNodeTypesToOpen(Collection nodeTypesToOpen) {
        this.nodeTypesToOpen = (nodeTypesToOpen != null ? nodeTypesToOpen : Collections.EMPTY_LIST);
    }

	/**
	 * The node types to open - won't be null.
	 * @return the nodeTypesToOpen
	 */
	public Collection getNodeTypesToOpen() {
		return nodeTypesToOpen;
	}

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isInverted() {
    	return inverted;
    }

	public void setProject(ShrimpProject project) {
		this.project = project;
	}

}