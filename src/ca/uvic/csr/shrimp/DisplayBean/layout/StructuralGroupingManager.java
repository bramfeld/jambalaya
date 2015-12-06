/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.FilterBean.GroupedNodeFilter;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;

/**
 * A manager to look after grouping of nodes based on graph structure
 * (e.g., group all nodes that are called by this node.)
 * 1. When a user groups or ungroups a node, the assigned artifact adaptor
 * calls group or ungroup in this class. These methods do their processing and
 * then trigger a relayout.
 * 2. The group method simply updates the gxl file's related properties file,
 * adding entries for the newly grouped nodes
 * 3. The ungroup method updates the properties file and also resets the node
 * attributes and names (plus moves related edges as needed)
 * 4. When re-layout occurs, the handleNodeGrouping calls groupNodes to
 * adjust node attributes and related edges. It then sets a filter to hide
 * grouped nodes.
 * @tag Shrimp(grouping)
 * @author Chris Bennett
 */
public class StructuralGroupingManager {

	private static Filter groupNodeFilter = new GroupedNodeFilter();
	protected final static String SET = "true";
	protected DisplayBean displayBean = null;
	private ShrimpProject project = null;

	/**
	 * Basic constructor
	 * @param displayBean
	 */
	public StructuralGroupingManager(ShrimpProject project, DisplayBean displayBean) {
		this.project = project;
		this.displayBean = displayBean;
	}

	/**
	 * Handle the grouping of nodes
	 * @param nodes
	 */
	public void handleNodeGrouping(Vector nodes) {
		//Adjust grouped nodes and their edges
		groupNodes(nodes);
		addGroupedNodeFilter();
	}

	/**
	 * Get the grouped and summary indicators from this project's properties file
	 * and set these as artifact attributes. Create summary nodes and
	 * updated affected edges.
	 * Format in properties file: sequence.grouped.nodeId = true
	 * e.g. sequence.grouped.:pkg1.class1 = true
	 * @param nodes
	 * @param groupedProperties - tracks active grouped properties for later use in cleanup
	 * @return the number of grouped nodes
	 */
	private int groupNodes(Vector nodes) {
		int numberOfGroupedNodes = 0;
		ShrimpProject project = getProject();
		if (project != null) {
			Vector summaryNodes = new Vector();
			Vector summaryNumbers = new Vector();
			getSortedSummaryNodes(nodes, summaryNodes, summaryNumbers);
			numberOfGroupedNodes = summaryNodes.size();
			for (int i=0; i< summaryNodes.size(); i++) { // process summary nodes
				ShrimpNode summaryNode = (ShrimpNode)summaryNodes.get(i);
				String summaryNumber = (String)summaryNumbers.get(i);
				setSummaryAttribute(summaryNode);
				Vector groupedNodes = getGroupedNodes(summaryNumber, nodes);
				for (int j=0; j<groupedNodes.size(); j++) { // process grouped nodes
					ShrimpNode groupedNode = (ShrimpNode)groupedNodes.get(j);
					if (groupedNode != null){
						setGroupedAttribute(groupedNode);
						adjustEdges(groupedNode, summaryNode);
					}
				}
			}
		}
		return numberOfGroupedNodes;
	}

	/**
	 * Update the node's names based on what is in the related
	 * property file's name property for this node
	 * @param nodes
	 */
	public void updateNodeNames(Vector nodes) {
		for (int i = 0; i< nodes.size(); i++) { // process all node
			ShrimpNode node = (ShrimpNode)nodes.get(i);
			String propertyName = makeNamePropertyName(getNodeId(node));
			Vector names = getCSVPropertyValues(propertyName);
			for (int j=0; j<names.size(); j++) { // process names
				String newName = (String)names.get(j);
				node.rename(newName);
			}
		}
	}

	/**
	 * Populates lists of summary nodes and summary numbers sorted by summary number
	 * TODO - this allocates an array to the size of the max summary number - a number
	 * that can get potentially very large resulting in a huge but sparse array. It
	 * would be good to either reset this number periodically or use a different
	 * technique for ordering things.
	 * @param nodes
	 * @return
	 */
	private void getSortedSummaryNodes(Vector nodes,
			Vector summaryNodes, Vector summaryNumbers) {
		ShrimpProject project = getProject();
		if (project != null) {
			int maxSummaryNumber = Integer.parseInt(getNextSummaryNumber());
			ShrimpNode[] summaryNodeArray = new ShrimpNode[maxSummaryNumber];
			Properties properties = project.getProperties();
			Object[] keys = properties.keySet().toArray();
			for (int i=0; i<keys.length; i++) {
				String propertyName = (String)keys[i];
				if (isSummaryProperty(propertyName)) {
					String summaryId = getNodeIdfromSummaryProperty(propertyName);
					ShrimpNode summaryNode = findNode(summaryId, nodes);
					if (summaryNode != null) {
						Vector groupedNodes = getGroupedNodes(summaryNode, nodes);
						if (groupedNodes.size() > 0) {
							Vector individualSummaryNumbers = getCSVPropertyValues(propertyName);
							for (int j=0; j<individualSummaryNumbers.size(); j++) {
								int index = Integer.parseInt((String)individualSummaryNumbers.get(j));
								summaryNodeArray[index] = findNode(summaryId, nodes);
							}
						}
						else { // remove node from properties file
							removeSummaryNumber(summaryNode);
						}
					}
				}
			}
			for (int i=0; i<maxSummaryNumber; i++) {
				if (summaryNodeArray[i] != null) {
					summaryNodes.add(summaryNodeArray[i]);
					summaryNumbers.add(Integer.toString(i));
				}
			}
		}
	}


	/**
	 * Adjust all edges of nodes that are identified in the associated project
	 * properties file
	 * @param nodes
	 */
	public void adjustEdges(Vector nodes) {
		Properties properties = getProject().getProperties();
		Object[] keys = properties.keySet().toArray();
		for (int i=0; i<keys.length; i++) {
			String propertyName = (String)keys[i];
			if (isSummaryProperty(propertyName)) {
				String summaryId = getNodeIdfromSummaryProperty(propertyName);
				ShrimpNode summaryNode = findNode(summaryId, nodes);
				if (summaryNode != null) {

				}
			}
		}
	}

	/**
	 * Adjust the edges of a grouped node so that they refer to summaryNode instead
     * @tag Shrimp(grouping)
	 * @param node
	 * @param summaryNode
	 */
	private void adjustEdges(ShrimpNode groupedNode, ShrimpNode summaryNode) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(groupedNode);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			ShrimpNode srcNode = arc.getSrcNode();
			ShrimpNode destNode = arc.getDestNode();
			if (!(srcNode.equals(summaryNode) || destNode.equals(summaryNode))) {
				if (srcNode.equals(groupedNode) && !arcExists(summaryNode, destNode)) { // outgoing arc
					arc.redirect(summaryNode, destNode); // redirect source node
				}
				else if (destNode.equals(groupedNode) && !arcExists(srcNode, summaryNode)){
					arc.redirect(srcNode, summaryNode); // redirect destination node
				}
			}
		}
	}

	/**
	 * Return true if a calls arc already exists between the source and destination nodes
	 * @param summaryNode
	 * @param destNode
	 * @return
	 */
	private boolean arcExists(ShrimpNode srcNode, ShrimpNode destNode) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(srcNode, destNode);
		if (arcs.size() > 0) {
			for (int i=0; i<arcs.size(); i++) {
				if (isAppropriateType((ShrimpArc)arcs.get(i))) {
					return true;
				}
			}
		}
		return false; // no arc exists
	}

	/**
	 * Get the node that has the specified ID from the supplied vector of nodes
	 *
	 * @param nodeName
	 * @param nodes
	 * @return node of null if not found
	 */
	private ShrimpNode findNode(String nodeId, Vector nodes) {
		for (int i=0; i<nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)nodes.get(i);
			if (getNodeId(node).equals(nodeId)) {
				return node;
			}
		}
		return null; // not found
	}

	/**
	 * Set filter for all grouped nodes
	 * @throws FilterNotFoundException
	 * @throws BeanNotFoundException
	 * @throws ShrimpToolNotFoundException
	 */
	private void addGroupedNodeFilter()  {
		try {
			FilterBean filterBean = getFilterBean();
			// applies the filter (also adds the filter too if necessary)
			filterBean.applyFilter(groupNodeFilter);
		} catch (ShrimpToolNotFoundException e) {
			// ShrimpView not found
			//e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Clears filter for all grouped nodes
	 * @throws FilterNotFoundException
	 * @throws BeanNotFoundException
	 * @throws ShrimpToolNotFoundException
	 */
	public void clearGroupedNodeFilter() {
		try {
			FilterBean filterBean = getFilterBean();
			if (filterBean.contains(groupNodeFilter)) {
				filterBean.removeFilter(groupNodeFilter);
			}
		} catch (ShrimpToolNotFoundException e) {
			//ignore
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (FilterNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Groups all called nodes under the selected node (and triggers a re-layout)
	 * @param node
	 */
	public void group(ShrimpNode node) {
		Vector nodes = new Vector(1);
		nodes.add(node);
		group(nodes);
	}

	/**
	 * Groups the selected node(s) (and triggers a re-layout)
	 * If there is more than one selected node, this assumes that these nodes
	 * should be grouped together (using the first selected node as the summary). Otherwise
	 * groups all called nodes under the selected node
	 * @param node
	 */
	public void group(Vector selectedNodes) {
		if (selectedNodes.size() == 0) {
			return;
		}

		ShrimpNode node = (ShrimpNode)selectedNodes.get(0);
		Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes();
		Vector relatedNodes;
		if (selectedNodes.size() > 1) { // use selected nodes minus first element
			relatedNodes = selectedNodes;
			selectedNodes.remove(0);
		} else {
			relatedNodes = getRelatedNodes(node, nodes);
		}
		if (relatedNodes.size() >= 1) { // Must be at least 1 other node

			if (renameNode(node)) { // returns false if the user clicked cancel
				String summaryNum = getNextSummaryNumber();
				saveSummaryProperty(node, summaryNum);
				for (int i = 0; i < relatedNodes.size(); i++) {
					ShrimpNode relatedNode = (ShrimpNode) relatedNodes.get(i);
					saveGroupProperty(relatedNode, summaryNum);
				}
				getProject().saveProperties();
				refreshLayout();
			}
		}
	}

	/**
	 * Shows the rename dialog.
	 * @param node the node to rename
	 * @return true if the node name was changed, false if cancel was clicked or if the dialog was closed
	 */
	private boolean renameNode(ShrimpNode node) {
		boolean renamed = false;
		try {
			ShrimpView view = (ShrimpView)getProject().getTool(ShrimpProject.SHRIMP_VIEW);
			int result = new RenameSelectedArtifactsAdapter(getProject(), view).changeName(node);
			renamed = (result == RenameSelectedArtifactsAdapter.RENAME_CHANGED);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
		return renamed;
	}

	/**
	 * Remove the current node's name from the name property
	 * @param node
	 */
	private void removeNameProperty(ShrimpNode node) {
		try {
			ShrimpView view = (ShrimpView)getProject().getTool(ShrimpProject.SHRIMP_VIEW);
			new RenameSelectedArtifactsAdapter(getProject(), view).removeNameProperty(node);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the next summary number for use in ordering summary clusters
	 * @return
	 */
	private String getNextSummaryNumber() {
		String nextSummaryNumber = "1";
		Properties properties = getProject().getProperties();
		String currentSummaryNum = properties.getProperty(
				SoftwareDomainConstants.SUMMARY_COUNT_PROPERTY, null);
		if (currentSummaryNum != null) {
			int num = Integer.parseInt(currentSummaryNum)+1;
			nextSummaryNumber = Integer.toString(num);
		}
		return nextSummaryNumber;
	}

	/**
	 * Get the nodes to be grouped with the specified  node
	 * @param node
	 * @param nodes
	 * @param stopAtSummary stop the search if we hit a summary node
	 */
	private Vector getRelatedNodes(ShrimpNode node, Vector nodes) {
		Vector relatedNodes = new Vector();
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			ShrimpNode nextNode = arc.getDestNode();
			// get outgoing arcs of the appropriate type
			if (isAppropriateType(arc) && node != nextNode &&
				!isGroupedAttributeSet(nextNode)) { // do not return already grouped nodes
				relatedNodes.add(nextNode);
			}
		}
		return relatedNodes;
	}

	/**
	 * Return true if an edge is a method call edge
	 * @param edge
	 * @return
	 */
	private boolean isAppropriateType(ShrimpArc arc) {
		return true;
// Allow all arc types for now (TODO - may want to tighten this up later)
//		return arc.getRelationship().getType().equals(JavaDomainConstants.CALLS_REL_TYPE);
	}

	/**
	 * Save the group property
	 * @param propertyName
	 * @param summaryNodeId
	 */
	private void saveGroupProperty(ShrimpNode groupedNode,
			String summaryNumber) {
		Properties properties = getProject().getProperties();
		String propertyName = makeGroupPropertyName(getNodeId(groupedNode));
		properties.put(propertyName, summaryNumber);
	}

	/**
	 * Save the summary property with a new summary number
	 * @param node
	 * @param summaryNum
	 */
	private void saveSummaryProperty(ShrimpNode node, String summaryNum) {
		Properties properties = getProject().getProperties();
		String propertyName = makeSummaryPropertyName(getNodeId(node));
		Vector values = getCSVPropertyValues(propertyName);
		if (!values.contains(summaryNum)) {
			values.add(summaryNum);
		}
		properties.put(propertyName, makeStringFromValues(values));
		properties.put(SoftwareDomainConstants.SUMMARY_COUNT_PROPERTY,
				summaryNum); // save max value
	}

	/**
	 * Make a comma separated string from a list of values
	 * @param values
	 * @return
	 */
	private Object makeStringFromValues(Vector values) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<values.size(); i++) {
			sb.append(values.get(i));
			if (i<values.size()-1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	/**
	 * Get a list of values associated with the specified property. Assumes
	 * that the property value is a comma separated list
	 * property (may be empty)
	 * @param summaryPropertyName
	 * @return
	 */
	private Vector getCSVPropertyValues(String propertyName) {
		Vector values = new Vector();
		Properties properties = getProject().getProperties();
		String currentSummaryValue = properties.getProperty(propertyName, null);
		if (currentSummaryValue != null) {
			StringTokenizer tokenizer = new StringTokenizer(currentSummaryValue, ",");
			while (tokenizer.hasMoreTokens()) {
				values.add(tokenizer.nextToken());
			}
		}
		return values;
	}

	/**
	 * Clear the group property for the selected property name
	 * @param propertyName
	 */
	private void clearProperty(String propertyName) {
		Properties properties = getProject().getProperties();
		properties.remove(propertyName);
	}

	/**
	 * Return true if the specified property is a grouped property
	 * @param propertyName
	 * @return
	 */
	private boolean isGroupedProperty(String propertyName) {
		return propertyName.startsWith(
				SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX);
	}

	/**
	 * Return true if the specified property is a grouped property
	 * @param propertyName
	 * @return
	 */
	private boolean isSummaryProperty(String propertyName) {
		return propertyName.startsWith(
				SoftwareDomainConstants.SUMMARY_PROPERTY_PREFIX);
	}

	/**
	 * Make a group property name for the specified node name
	 * @param node
	 * @return
	 */
	private String makeGroupPropertyName(String nodeId) {
		return SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX + nodeId;
	}

	/**
	 * Make a summary property name for the specified node
	 * @param node
	 * @return
	 */
	private String makeNamePropertyName(String nodeId) {
		return SoftwareDomainConstants.NAME_PROPERTY_PREFIX + nodeId;
	}

	/**
	 * Make a summary property name for the specified node
	 * @param node
	 * @return
	 */
	private String makeSummaryPropertyName(String nodeId) {
		return SoftwareDomainConstants.SUMMARY_PROPERTY_PREFIX + nodeId;
	}

	/**
	 * Extract node name from grouped property name
	 * @param propertyName
	 * @return
	 */
	private String getNodeIdfromGroupProperty(String propertyName) {
		return propertyName.substring(
				SoftwareDomainConstants.GROUPED_PROPERTY_PREFIX.length());
	}

	/**
	 * Extract node name from summary property name
	 * @param propertyName
	 * @return
	 */
	private String getNodeIdfromSummaryProperty(String propertyName) {
		return propertyName.substring(
				SoftwareDomainConstants.SUMMARY_PROPERTY_PREFIX.length());
	}

	/**
	 * Get the filter bean
     * @tag Shrimp(grouping)
	 * @return
	 * @throws ShrimpToolNotFoundException
	 * @throws BeanNotFoundException
	 */
	private FilterBean getFilterBean()
	throws ShrimpToolNotFoundException, BeanNotFoundException {
		ShrimpView shrimpView =
			(ShrimpView)getProject().getTool(ShrimpProject.SHRIMP_VIEW);
		return (FilterBean)shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	}

	/**
	 * Get the current ShrimpProject
	 * @return
	 */
	private ShrimpProject getProject() {
		return project;
	}

	/**
	 * Clear the grouping and summary properties for the selected node (and trigger a re-layout)
	 * @param node
	 */
	public void ungroup(ShrimpNode summaryNode) {
		if (isSummaryNode(summaryNode)) {
			// Restore the name if required
			summaryNode.restoreName();
			removeNameProperty(summaryNode);

			Vector groupedNodes = getGroupedNodes(summaryNode,
					displayBean.getDataDisplayBridge().getShrimpNodes());
			if (!groupedNodes.isEmpty()) {
				ungroupNode(summaryNode, groupedNodes);
				getProject().saveProperties();
				refreshLayout();
			}
		}
	}

	/**
	 * Get the nodes grouped directly under the specified summary node.
	 * Always gets the nodes associated with the last summary number
	 * @param summaryNode
	 * @param shrimpNodes
	 * @return
	 */
	private Vector getGroupedNodes(ShrimpNode summaryNode, Vector nodes) {
		Vector groupedNodes = new Vector();
		Vector summaryNumbers = getCSVPropertyValues(makeSummaryPropertyName(getNodeId(summaryNode)));
		if (!summaryNumbers.isEmpty()) {
			String summaryNumber = (String)summaryNumbers.get(summaryNumbers.size()-1);
			groupedNodes = getGroupedNodes(summaryNumber, nodes);
		}
		return groupedNodes;
	}

	/**
	 * Get the nodes grouped by the specified summary number.
	 * @param summaryNumber
	 * @param shrimpNodes
	 * @return
	 */
	private Vector getGroupedNodes(String summaryNumber, Vector nodes) {
		Vector groupedNodes = new Vector();
		Properties properties = getProject().getProperties();
		Object[] keys = properties.keySet().toArray();
		for (int i=0; i<keys.length; i++) {
			String propertyName = (String)keys[i];
			if (isGroupedProperty(propertyName)) {
				String num = properties.getProperty(propertyName);
				if (num.equals(summaryNumber)) {
					// @tag Shrimp(grouping) : sometimes the node isn't found - probably shouldn't add null!
					groupedNodes.add(findNode(getNodeIdfromGroupProperty(propertyName), nodes));
				}
			}
		}
		return groupedNodes;
	}

	/**
	 * Refresh the layout
	 */
	private void refreshLayout() {
		try {
			SelectorBean selectorBean = (SelectorBean) getProject().getTool(
					ShrimpProject.SHRIMP_VIEW).getBean(ShrimpTool.SELECTOR_BEAN);
			Vector targets =
				(Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			Vector nodes;
			if (targets.isEmpty()){
				nodes = displayBean.getDataDisplayBridge().getRootNodes();
			} else {
				ShrimpNode parentNode = ((ShrimpNode)targets.get(0)).getParentShrimpNode();
				if (parentNode == null) {
					nodes = displayBean.getDataDisplayBridge().getRootNodes();
				}
				else {
					nodes = displayBean.getDataDisplayBridge().getChildNodes(parentNode);
				}
			}
			Collections.sort(nodes);
			displayBean.setLayoutMode(nodes, displayBean.getLastLayoutMode(), false, true);
			displayBean.requestFocus(); // give focus to the canvas

//			displayBean.refreshLayout(targets);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ungroup a specified summary node
	 * @param summaryNode
	 */
	private void ungroupNode(ShrimpNode summaryNode, Vector nodes) {

		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			ShrimpNode groupedNode = (ShrimpNode)nodeIter.next();
			clearGroupedAttribute(groupedNode);
		}
		clearSummaryAttribute(summaryNode);
		restoreArcs(summaryNode);
		removeSummaryNumber(summaryNode);
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode groupedNode = (ShrimpNode) nodes.get(i);
			clearProperty(makeGroupPropertyName(getNodeId(groupedNode)));
		}
	}

	/**
	 * Remove the last summaryNumber for the specified node
	 * @param string
	 */
	private void removeSummaryNumber(ShrimpNode summaryNode) {
		String summaryPropertyName = makeSummaryPropertyName(getNodeId(summaryNode));
		Vector summaryNumbers =
			getCSVPropertyValues(summaryPropertyName);
		if (summaryNumbers.size() <= 1) {
			clearProperty(summaryPropertyName);
		}
		else { // prune list of summary numbers
			summaryNumbers.remove(summaryNumbers.size()-1);
			getProject().getProperties().put(summaryPropertyName,
					makeStringFromValues(summaryNumbers));
		}
	}

	/**
	 * Restore everything related to this node to its last state.
	 * @param summaryNode
	 * @param methodExecNodeNames
	 */
	private void restoreArcs(ShrimpNode summaryNode) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(summaryNode);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			arc.restore();
		}
	}

	/**
	 * Return true if this is a summary node based on its contained artifact
	 * @param node
	 * @return
	 */
	private boolean isSummaryNode(ShrimpNode node) {
		Artifact artifact = node.getArtifact();
		String summary = (String)artifact.getAttribute(SoftwareDomainConstants.NOM_ATTR_SUMMARY);
		return summary != null && summary.equals(SET);
	}

	/**
	 * Set the summary attribute in the node's artifact
	 * @param node
	 */
	private void setSummaryAttribute(ShrimpNode node) {
		node.getArtifact().setAttribute(SoftwareDomainConstants.NOM_ATTR_SUMMARY, SET);
		node.setIsGrouped(true);
	}

	/**
	 * Reset the summary attribute
	 * @param node
	 */
	private void clearSummaryAttribute(ShrimpNode node) {
		clearAttribute(node, SoftwareDomainConstants.NOM_ATTR_SUMMARY);
		node.setIsGrouped(false);
	}

	/**
	 * Set the grouped attribute
	 * @param node
	 */
	private void setGroupedAttribute(ShrimpNode node) {
		node.getArtifact().setAttribute(SoftwareDomainConstants.NOM_ATTR_GROUPED, SET);
	}

	/**
	 * Reset the grouped attribute and clear the color attribute (unless this is
	 * a summary node in which case we shouldleave color as is)
	 * @param node
	 */
	private void clearGroupedAttribute(ShrimpNode node) {
		clearAttribute(node, SoftwareDomainConstants.NOM_ATTR_GROUPED);
	}

	/**
	 * Return true if the grouped attribute of the specified node is set
	 * @param node
	 */
	private boolean isGroupedAttributeSet(ShrimpNode node) {
		String groupedAttribute =
			(String)node.getArtifact().getAttribute(
					SoftwareDomainConstants.NOM_ATTR_GROUPED);
		return groupedAttribute != null && groupedAttribute.equals(SET);
	}

	/**
	 * Reset an attribute
	 * @param node
	 */
	private void clearAttribute(ShrimpNode node, String attributeName) {
		node.getArtifact().setAttribute(attributeName, null);
	}

	private String getNodeId(ShrimpNode node) {
		return node.getArtifact().getExternalId().toString();
	}

	/**
	 * Test
	 */
	public static void main(String[] args) {
	    String fileName = "C:/callgraph1.gxl";
		DisplayBean displayBean = createDisplayBean(fileName);
		StructuralGroupingManager manager = new StructuralGroupingManager(null, displayBean);
		test1(manager, displayBean);
		test2(manager, displayBean);
		test3(manager, displayBean);
		System.exit(-1);
	}

	/**
	 * Restore the graph properties file(s) to original state
	 */
	private static void clearProperties(StructuralGroupingManager manager) {
		Properties properties = manager.getProject().getProperties();
		Object[] keys = properties.keySet().toArray();
		for (int i=0; i<keys.length; i++) { // process summary nodes first
			String propertyName = (String)keys[i];
			if (manager.isSummaryProperty(propertyName) ||
					manager.isGroupedProperty(propertyName)) {
				manager.clearProperty(propertyName);
			}
		}
		manager.clearProperty(SoftwareDomainConstants.SUMMARY_COUNT_PROPERTY);
	}

	/**
	 * Basic test of grouping
	 *    -1-    4
	 *   |   |   |
	 *   V   V   |
	 *   2   3 <-|
	 *     |  |  |
	 *     V  V  |
	 *     5  6 <-
	 * @param manager
	 * @param displayBean
	 */
	private static void test1(StructuralGroupingManager manager,
			DisplayBean displayBean ) {
		clearProperties(manager);
		Vector nodes = displayBean.getAllNodes();

		// Initial condition
		manager.groupNodes(nodes);
		assertEquals(nodes.size(), 7);
	    assertEquals(countGrouped(nodes, manager), 0);
	    assertEquals(countSummary(nodes, manager), 0);
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("1","3", nodes, displayBean, manager), "arc from 1-3");
	    assertTrue(arcExists("4","3", nodes, displayBean, manager), "arc from 4-3");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");
	    assertTrue(arcExists("3","5", nodes, displayBean, manager), "arc from 3-5");
	    assertTrue(arcExists("3","6", nodes, displayBean, manager), "arc from 3-6");

		// Group nodes under node 1
	    ShrimpNode node1 = manager.findNode("1", nodes);
	    manager.group(node1);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 2);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","5", nodes, displayBean, manager), "arc from 1-5");
	    assertTrue(arcExists("1","6", nodes, displayBean, manager), "arc from 1-6");
	    assertTrue(arcExists("4","1", nodes, displayBean, manager), "arc from 4-1");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");

		// Ungroup nodes under node 1 - should be back to normal
	    manager.ungroup(node1);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 0);
	    assertEquals(countSummary(nodes, manager), 0);
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("1","3", nodes, displayBean, manager), "arc from 1-3");
	    assertTrue(arcExists("4","3", nodes, displayBean, manager), "arc from 4-3");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");
	    assertTrue(arcExists("3","5", nodes, displayBean, manager), "arc from 3-5");
	    assertTrue(arcExists("3","6", nodes, displayBean, manager), "arc from 3-6");
	    assertTrue(!arcExists("1","5", nodes, displayBean, manager), "No arc from 1-5");
	    assertTrue(!arcExists("1","6", nodes, displayBean, manager), "No arc from 1-6");
	    assertTrue(!arcExists("4","1", nodes, displayBean, manager), "No arc from 4-1");

	    // Group nodes under 2 (no effect)
	    ShrimpNode node2 = manager.findNode("2", nodes);
	    manager.group(node2);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 0);
	    assertEquals(countSummary(nodes, manager), 0);
	}

	private static void test2(StructuralGroupingManager manager,
			DisplayBean displayBean ) {
		clearProperties(manager);
		Vector nodes = displayBean.getAllNodes();

		// Group nodes under node 1
	    ShrimpNode node1 = manager.findNode("1", nodes);
	    manager.group(node1);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 2);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","5", nodes, displayBean, manager), "arc from 1-5");
	    assertTrue(arcExists("1","6", nodes, displayBean, manager), "arc from 1-6");
	    assertTrue(arcExists("4","1", nodes, displayBean, manager), "arc from 4-1");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");

		// Group nodes under node 4
	    ShrimpNode node4 = manager.findNode("4", nodes);
	    manager.group(node4);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 4);
	    assertEquals(countSummary(nodes, manager), 2);
	    assertTrue(arcExists("4","5", nodes, displayBean, manager), "arc from 4-5");

	    // Ungroup nodes under node 4
	    manager.ungroup(node4);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 2);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","5", nodes, displayBean, manager), "arc from 1-5");
	    assertTrue(arcExists("1","6", nodes, displayBean, manager), "arc from 1-6");
	    assertTrue(arcExists("4","1", nodes, displayBean, manager), "arc from 4-1");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");

	    // Ungroup nodes under node 1
	    manager.ungroup(node1);
	    manager.handleNodeGrouping(nodes);
		assertEquals(nodes.size(), 7);
	    assertEquals(countGrouped(nodes, manager), 0);
	    assertEquals(countSummary(nodes, manager), 0);
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("1","3", nodes, displayBean, manager), "arc from 1-3");
	    assertTrue(arcExists("4","3", nodes, displayBean, manager), "arc from 4-3");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");
	    assertTrue(arcExists("3","5", nodes, displayBean, manager), "arc from 3-5");
	    assertTrue(arcExists("3","6", nodes, displayBean, manager), "arc from 3-6");
}

	private static void test3(StructuralGroupingManager manager,
			DisplayBean displayBean ) {
		clearProperties(manager);
		Vector nodes = displayBean.getAllNodes();

		// Group nodes under node 4
	    ShrimpNode node4 = manager.findNode("4", nodes);
	    manager.group(node4);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 2);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","4", nodes, displayBean, manager), "arc from 1-4");
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("4","5", nodes, displayBean, manager), "arc from 4-5");

		// Group nodes under node 4 again
	    manager.group(node4);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 3);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","4", nodes, displayBean, manager), "arc from 1-4");
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");

	    // Ungroup nodes under node 4
	    manager.ungroup(node4);
	    manager.handleNodeGrouping(nodes);
	    assertEquals(countGrouped(nodes, manager), 2);
	    assertEquals(countSummary(nodes, manager), 1);
	    assertTrue(arcExists("1","4", nodes, displayBean, manager), "arc from 1-4");
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("4","5", nodes, displayBean, manager), "arc from 4-5");


	    // Ungroup nodes under node 4 again
	    manager.ungroup(node4);
	    manager.handleNodeGrouping(nodes);
		assertEquals(nodes.size(), 7);
	    assertEquals(countGrouped(nodes, manager), 0);
	    assertEquals(countSummary(nodes, manager), 0);
	    assertTrue(arcExists("1","2", nodes, displayBean, manager), "arc from 1-2");
	    assertTrue(arcExists("1","3", nodes, displayBean, manager), "arc from 1-3");
	    assertTrue(arcExists("4","3", nodes, displayBean, manager), "arc from 4-3");
	    assertTrue(arcExists("4","6", nodes, displayBean, manager), "arc from 4-6");
	    assertTrue(arcExists("3","5", nodes, displayBean, manager), "arc from 3-5");
	    assertTrue(arcExists("3","6", nodes, displayBean, manager), "arc from 3-6");
	}


	private static int countGrouped(Vector nodes, StructuralGroupingManager manager) {
		int nGrouped = 0;
		for (int i=0; i<nodes.size(); i++) {
			PShrimpNode node = (PShrimpNode)nodes.get(i);
			if (manager.isGroupedAttributeSet(node)) {
				nGrouped++;
			}
		}
		return nGrouped;
	}

	private static int countSummary(Vector nodes, StructuralGroupingManager manager) {
		int nGrouped = 0;
		for (int i=0; i<nodes.size(); i++) {
			PShrimpNode node = (PShrimpNode)nodes.get(i);
			if (manager.isSummaryNode(node)) {
				nGrouped++;
			}
		}
		return nGrouped;
	}

	private static boolean arcExists(String from, String to, Vector nodes,
			DisplayBean displayBean, StructuralGroupingManager manager) {
		Vector arcs = displayBean.getAllArcs();
		for (int i=0; i<arcs.size(); i++) {
			PShrimpArc arc = (PShrimpArc)arcs.get(i);
			if (arc.getSrcNode().equals(manager.findNode(from, nodes)) &&
				arc.getDestNode().equals(manager.findNode(to, nodes))) {
				return true;
			}
		}
		return false; // not found
	}

	private static void assertEquals(int a, int b) {
		if (a == b) {
			System.out.println("OK -> " + a + " equals " + b);
		}
		else {
			System.err.println("Failed -> " + a + " does not equal " + b);
		}
	}

	private static void assertTrue(boolean assertion, String msg) {
		if (assertion) {
			System.out.println("OK -> " + msg + " is true");
		}
		else {
			System.out.println("Failed -> " + msg + " is not true");
		}
	}

	private static DisplayBean createDisplayBean(String fileName) {
	    final URI uri = ResourceHandler.getFileURI(fileName);
		StandAloneApplication application = new StandAloneApplication();
		application.initialize(application.createParentFrame());
		application.openProject(uri);
		ShrimpProject project = application.getActiveProject();
        DisplayBean displayBean = null;
		try {
			ShrimpView view = (ShrimpView)project.getTool(ShrimpProject.SHRIMP_VIEW);
			displayBean =
				(DisplayBean) view.getBean(ShrimpTool.DISPLAY_BEAN);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
		return displayBean;
	}

}