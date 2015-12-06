/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.FilterBean.GroupedNodeFilter;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;

/**
 * General class to handle sequence node grouping. This class assumes that
 * grouping is done using the node's hierarchical naming structure.
 * @author Chris Bennett
 */
public abstract class SequenceGroupingManager {

	private static Filter groupNodeFilter = new GroupedNodeFilter();
	protected static char SEPARATOR = '.'; // default name separator - override in child classed if needed
	private final static String COLON_ESCAPE_SEQUENCE = "&#58;";
	protected final static String SET = "true";

	protected DisplayBean displayBean = null;
	protected ShrimpProject project;

	/**
	 * Basic constructor
	 * @param displayBean
	 * @param project
	 */
	public SequenceGroupingManager(ShrimpProject project, DisplayBean displayBean) {
		this.displayBean = displayBean;
		this.project = project;
	}

	/**
	 * Handle the grouping of nodes
     * @tag Shrimp.grouping
	 * @param nodes
	 */
	public void handleNodeGrouping(Vector nodes, HashMap groupedProperties) {
		// Reset grouping manager
		reinitialize();

		//Adjust grouped object lifelines and their edges
		groupNodes(nodes, groupedProperties);

		// Cleanup after grouping
		cleanup(nodes);

		// Turn on filtering of grouped nodes
		addGroupedNodeFilter();
	}

	/**
	 * Reset state for new grouping
	 */
	protected void reinitialize() {
		// do nothing by default - children can override if they need to
	}

	/**
	 * Clean up after grouping
	 * @param nodes
	 */
	protected void cleanup(Vector nodes) {
		// do nothing by default - children can override if they need to
	}

	/**
	 * Get the grouped indicators from this project's properties file
	 * and set these as artifact attributes. Create summary nodes and
	 * updated affected edges.
	 * Format in properties file: sequence.grouped.nodeName = true
	 * e.g. sequence.grouped.:pkg1.class1 = true
	 * @param nodes
	 * @param groupedProperties - tracks active grouped properties for later use in cleanup
	 */
	private void groupNodes(Vector nodes, HashMap activeGroupedProperties) {
		if (project != null) {
			Properties properties = project.getProperties();
			Object[] keys = properties.keySet().toArray();
			Arrays.sort(keys, new GroupNameSorter()); // sort in reverse herarchical order so we group from bottom up
			for (int i=0; i<keys.length; i++) {
				String propertyName = (String)keys[i];
				if (isGroupedProperty(propertyName)) { // only process grouped properties
					String nodeName = getNodeNamefromGroupProperty(propertyName);
					ShrimpNode node = findNode(nodeName, nodes);
					if (node != null && isApplicableGroupType(node)){ // only process nodes applicable to this type of manager
						activeGroupedProperties.put(propertyName, propertyName); // record the fact we used this property for later use in cleanup
						boolean isSummaryNode = groupNode(node, nodes); // child classes will do grouping
						if (!isSummaryNode) { // hide this node unless it is the new summary node
							setGroupedAttribute(node);
						}
					}
				}
			}
		}
	}

	/**
	 * Is the specified node applicable for this type of grouping
	 * (e.g., object life line grouping applies only to object life lines)
	 * @param node
	 * @return
	 */
	protected abstract boolean isApplicableGroupType(ShrimpNode node);

	/**
	 * Group a node
	 * @param node
	 * @param nodes
	 * @return true if the specified node is a summary node.
	 */
	protected abstract boolean groupNode(ShrimpNode node, Vector nodes);

	/**
	 * Get the node that has the specified name from the supplied vector of nodes
	 *
	 * @param nodeName
	 * @param nodes
	 * @return node of null if not found
	 */
	private ShrimpNode findNode(String nodeName, Vector nodes) {
		for (int i=0; i<nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)nodes.get(i);
			if (node.getName().equals(nodeName)) {
				return node;
			}
			else { // check all saved names
				String[] savedNodeNames = node.getSavedNames();
				for (int j=0; j<savedNodeNames.length; j++) {
					if (savedNodeNames[j].equals(nodeName)) {
						return node;
					}
				}
			}
		}
		return null; // not found
	}

	/**
	 * Set filter for all grouped nodes
     * @tag Shrimp(grouping)
	 * @throws FilterNotFoundException
	 * @throws BeanNotFoundException
	 * @throws ShrimpToolNotFoundException
	 */
	private void addGroupedNodeFilter()  {
		try {
			clearGroupedNodeFilter();
			FilterBean filterBean = getFilterBean();
			filterBean.addFilter(groupNodeFilter);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears filter for all grouped nodes
     * @tag Shrimp(grouping)
	 * @throws FilterNotFoundException
	 * @throws BeanNotFoundException
	 * @throws ShrimpToolNotFoundException
	 */
	public void clearGroupedNodeFilter() {
		try {
			FilterBean filterBean;
			filterBean = getFilterBean();
			if (filterBean.contains(groupNodeFilter)) {
				filterBean.removeFilter(groupNodeFilter);
			}
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (FilterNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return a summary node name based on the specified node name.
     * @tag Shrimp(grouping)
	 * @param string
	 * @param groupLevel
	 * @return
	 */
	protected abstract String getSummaryNodeName(String nodeName);

	/**
	 * Set the selected node to be grouped (and trigger a re-layout)
	 * @param node
	 */
	public void group(ShrimpNode node) {
		Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes();
		Vector relatedNodes = getRelatedNodes(node, nodes);
		relatedNodes.insertElementAt(node,0); // add the original node
		if (canBeGrouped(node) && relatedNodes.size() > 1) { // Must be at least 1 other node (2 or more total)
			for (int i=0; i<relatedNodes.size(); i++) {
				ShrimpNode relatedNode = (ShrimpNode) relatedNodes.get(i);
				String propertyName = makeGroupPropertyName(relatedNode);
				saveGroupProperty(propertyName);
			}
			project.saveProperties();
			refreshQuickView();
		}
	}

	/**
	 * Return true if the specified node can be grouped.
	 * @param node
	 * @return
	 */
	protected abstract boolean canBeGrouped(ShrimpNode node);

	/**
	 * Get nodes that are related to the specified node, not including
	 * the specified node.
	 * @param summaryNode
	 * @param nodes
	 * @return
	 */
	protected abstract Vector getRelatedNodes(ShrimpNode node, Vector nodes);

	/**
	 * Get nodes that have been grouped with the specified summary node.
	 * This method should assume that the specified node has a summarized name and
	 * should use the node naming to determine grouping (as opposed to relying
	 * on the grouping attribute).
	 * The Vector returned should not include the summaryNode.
	 * @param summaryNode
	 * @param nodes
	 * @return
	 */
	protected abstract Vector getGroupedNodes(ShrimpNode summaryNode, Vector nodes);

	/**
	 * Save the group property
	 * @param propertyName
	 */
	protected void saveGroupProperty(String propertyName) {
		Properties properties = project.getProperties();
		properties.put(propertyName, SET);
	}

	/**
	 * Return the group property or null if not found
	 * @param propertyName
	 */
	protected String getGroupProperty(String propertyName) {
		Properties properties = project.getProperties();
		return properties.getProperty(propertyName, null);
	}

	/**
	 * Return true if the specified property is a grouped property
	 * @param propertyName
	 * @return
	 */
	protected boolean isGroupedProperty(String propertyName) {
		return propertyName.startsWith(SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX);
	}

	/**
	 * Make a group propety name for the specified node
	 * @param node
	 * @return
	 */
	protected String makeGroupPropertyName(ShrimpNode node) {
		return makeGroupPropertyName(node.getName());
	}

	/**
	 * Make a group propety name for the specified node
	 * @param nodeName
	 */
	protected String makeGroupPropertyName(String nodeName) {
		return SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX + escapeColon(nodeName);
	}

	protected String getNodeNamefromGroupProperty(String propertyName) {
		return unescapeColon(propertyName.substring(SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX.length()));
	}

	/**
	 * Unescape escaped colons in string s
	 * @param s
	 * @return string containing colons
	 */
	private String unescapeColon(String s) {
		return s.replaceAll(COLON_ESCAPE_SEQUENCE, ":");
	}

	/**
	 * Escape any colons in the specified string
	 * @param name
	 */
	private String escapeColon(String s) {
		return s.replaceAll(":", COLON_ESCAPE_SEQUENCE);
	}

	/**
	 * Initialize filters and display related mods for this layout
	 */
	public void initDisplayFilters() {
		try {
			FilterBean filterBean = getFilterBean();

			// Hide the contains arcs that are only used to create a connected
			// graph by joing object life lines to method executions
			filterBean.addRemoveSingleNominalAttrValue(
					AttributeConstants.NOM_ATTR_REL_TYPE, String.class,
					FilterConstants.RELATIONSHIP_FILTER_TYPE,
					JavaDomainConstants.CONTAINS_REL_TYPE, true);

		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the filter bean
     * @tag Shrimp.grouping
	 * @throws ShrimpToolNotFoundException, BeanNotFoundException
	 */
	private FilterBean getFilterBean() throws ShrimpToolNotFoundException, BeanNotFoundException {
		ShrimpView shrimpView = (ShrimpView)project.getTool(ShrimpProject.SHRIMP_VIEW);
		return (FilterBean)shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	}

	/**
	 * Clear the grouping property for the selected node (and trigger a re-layout)
	 */
	public void ungroup(ShrimpNode summaryNode) {
		if (isSummaryNode(summaryNode)) {
			Vector groupedNodes = getGroupedNodes(summaryNode,
					displayBean.getDataDisplayBridge().getShrimpNodes());
			groupedNodes.insertElementAt(summaryNode,0); // add the original node
			if (!groupedNodes.isEmpty()) {
				ungroupNode(summaryNode, groupedNodes);
				project.saveProperties();
				refreshQuickView();
			}
		}
	}

	/**
	 * Clear the group property for the selected property name
	 * @param propertyName
	 */
	protected void clearGroupProperty(String propertyName) {
		Properties properties = project.getProperties();
		properties.remove(propertyName);
	}

	/**
	 * Clean up the grouped properties entries removing any entry that was
	 * not used in grouping
	 * @param groupedProperties
	 */
	public void cleanupGroupedProperties(HashMap groupedProperties) {
		boolean updated = false;
		if (project != null) {
			Properties properties = project.getProperties();
			Object[] keys = properties.keySet().toArray();
			for (int i=0; i<keys.length; i++) {
				String propertyName = (String)keys[i];
				if (isGroupedProperty(propertyName) &&
						!propertyName.equals(groupedProperties.get(propertyName))) {
					properties.remove(propertyName);
					updated = true;
				}
			}
			if (updated) {
				project.saveProperties();
			}
		}
	}

	/**
	 * Refresh the quick view to force a relayout
	 */
	private void refreshQuickView() {
		if (project != null) {
			QuickViewManager mgr = project.getQuickViewManager();
			QuickViewAction action = mgr.getQuickViewAction(JavaDomainConstants.JAVA_QUICK_VIEW_SEQUENCE_DIAGRAM);
			if (action == null) {
				action = mgr.createSequenceQuickViewAction(project);
			}
			action.startAction();
		}
	}

//	/**
//	 * Ungroup a specified summary node
//     * @tag Shrimp.grouping
//	 * @param summaryNode
//	 */
//	protected void ungroupNode(ShrimpNode summaryNode, Vector nodes) {
//		boolean[] cleared = new boolean[nodes.size()];
//		int count=0;
//		if (isSummaryNode(summaryNode)) {
//			for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
//				ShrimpNode groupedNode = (ShrimpNode)nodeIter.next();
//				clearGroupedAttribute(groupedNode);
//				// Also make sure we clear out the property name before
//				// restoring names
//				String propertyName = makeGroupPropertyName(groupedNode);
//				if (getGroupProperty(propertyName) != null) {
//					clearGroupProperty(propertyName);
//					cleared[count++] = true;
//				}
//				else {
//					cleared[count++] = false; // not found
//				}
//			}
//			summaryNode.restoreName();
//			clearSummaryAttribute(summaryNode);
//			restoreNode(summaryNode);
//
//// TODO - only restore one level at a time
//			for (int i = 0; i < nodes.size(); i++) {
//				if (!cleared[i]) {
//					ShrimpNode groupedNode = (ShrimpNode) nodes.get(i);
//					String propertyName = makeGroupPropertyName(groupedNode);
//					clearGroupProperty(propertyName);
//				}
//			}
//		}
//	}

	/**
	 * Ungroup a specified summary node
     * @tag Shrimp.grouping
	 * @param summaryNode
	 */
	protected void ungroupNode(ShrimpNode summaryNode, Vector nodes) {
		if (isSummaryNode(summaryNode)) {
			for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
				ShrimpNode groupedNode = (ShrimpNode)nodeIter.next();
				clearGroupedAttribute(groupedNode);
			}
			clearSummaryAttribute(summaryNode);
			restoreNode(summaryNode);

			for (int i = 0; i < nodes.size(); i++) {
				ShrimpNode groupedNode = (ShrimpNode) nodes.get(i);
				String propertyName = makeGroupPropertyName(groupedNode);
				clearGroupProperty(propertyName);
			}
		}
	}

	/**
	 * Restore everything related to this node to its ungrouped state.
	 * @param summaryNode
	 * @param methodExecNodeNames
	 */
	protected abstract void restoreNode(ShrimpNode summaryNode);

	/**
	 * Return true if this is a summary node based on its contained artifact
	 * @param node
	 */
	protected boolean isSummaryNode(ShrimpNode node) {
		Artifact artifact = node.getArtifact();
		String summary = (String)artifact.getAttribute(SoftwareDomainConstants.NOM_ATTR_SUMMARY);
		return summary != null && summary.equals(SET);
	}

	/**
	 * Make the supplied node into a summary node.
	 */
	protected abstract void makeSummaryNode(ShrimpNode node);

	/**
	 * Set the summary attribute in the node's artifact
	 * TODO - get color from properties file???
	 * @param node
	 */
	protected void setSummaryAttribute(ShrimpNode node) {
		node.getArtifact().setAttribute(SoftwareDomainConstants.NOM_ATTR_SUMMARY, SET);
		node.getArtifact().setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_COLOR, "green");
		updateNodeColors();
	}

	/**
	 * Force all node colors to be updated.
	 */
	private void updateNodeColors() {
        try {
        	// update the node colors
			AttrToVisVarBean attrToVisVarBean = getAttrToVisVarBean();
			NominalNodeColorVisualVariable visVar =
				new NominalNodeColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
			Attribute attr =
				attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_COLOR);
			attrToVisVarBean.fireVisVarValuesChangeEvent(attr, visVar);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reset the summary attribute
	 * @param node
	 */
	private void clearSummaryAttribute(ShrimpNode node) {
		clearAttribute(node, SoftwareDomainConstants.NOM_ATTR_SUMMARY);
		clearAttribute(node, AttributeConstants.NOM_ATTR_ARTIFACT_COLOR);
	}

	/**
	 * Set the grouped attribute
	 * @param node
	 */
	protected void setGroupedAttribute(ShrimpNode node) {
		node.getArtifact().setAttribute(SoftwareDomainConstants.NOM_ATTR_GROUPED, SET);
	}

	/**
	 * Reset the grouped attribute and clear the color attribute (unless this is
	 * a summary node in which case we shouldleave color as is)
	 * @param node
	 */
	protected void clearGroupedAttribute(ShrimpNode node) {
		clearAttribute(node, SoftwareDomainConstants.NOM_ATTR_GROUPED);
	}

	/**
	 * Return true if the grouped attribute of the specified node is set
	 * @param node
	 */
	protected boolean isGroupedAttributeSet(ShrimpNode node) {
		String groupedAttribute = (String)node.getArtifact().getAttribute(SoftwareDomainConstants.NOM_ATTR_GROUPED);
		return groupedAttribute != null && groupedAttribute.equals(SET);
	}

	/**
	 * Reset an attribute
	 * @param node
	 */
	private void clearAttribute(ShrimpNode node, String attributeName) {
		node.getArtifact().setAttribute(attributeName, null);
	}

	/**
	 * Returns true if the specified node is an Actor node
	 */
	public static final boolean isActor(ShrimpNode node) {
		String type = node.getArtifact().getType();
		return type.equals(JavaDomainConstants.ACTOR_ART_TYPE);
	}

	/**
	 * @return true if the specified node is a method execution type
	 */
	public static final boolean isMethodExecution(ShrimpNode node) {
		String type = node.getArtifact().getType();
		return type.equals(JavaDomainConstants.METHOD_EXEC_ART_TYPE);
	}

	/**
	 * @return true if the specified node is an object lifeline type
	 */
	public static final boolean isObjectLifeline(ShrimpNode node) {
		String type = node.getArtifact().getType();
		return type.equals(JavaDomainConstants.OBJECT_ART_TYPE) ||
		type.equals(JavaDomainConstants.ACTOR_ART_TYPE);
	}

	/**
	 * Get the data display bridge
     * @tag Shrimp.grouping
	 * @throws BeanNotFoundException
	 */
	private AttrToVisVarBean getAttrToVisVarBean() throws BeanNotFoundException {
		return (AttrToVisVarBean)project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
	}

	/**
	 * Sorts group name keys in order of granularity in the hierarchy. I.e.,
	 * group names that are 3 levels deep will be sorted before those that are 2 levels deep
	 * @author Chris Bennett
	 */
	private class GroupNameSorter implements Comparator {

		public int compare(Object arg0, Object arg1) {
			String group1 = (String)arg0;
			String group2 = (String)arg1;
			int group1Level = countSeparators(group1);
			int group2Level = countSeparators(group2);
			if (group1Level > group2Level) {
				return -1;
			}
			else if (group1Level == group2Level) {
				return group1.compareTo(group2);
			}
			else {
				return 1;
			}
		}

		private int countSeparators(String s) {
			int count = 0;
			for (int i=0; i<s.length(); i++) {
				if (s.charAt(i) == SEPARATOR) {
					count++;
				}
			}
			return count;
		}

	}

}