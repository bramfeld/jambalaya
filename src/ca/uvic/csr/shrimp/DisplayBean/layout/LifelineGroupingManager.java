/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * Handles grouping of sequence diagram nodes.
 *
 * @tag Shrimp.sequence
 * @author Chris Bennett
 */
public class LifelineGroupingManager extends SequenceGroupingManager {

	private HashMap methodExecNodeNames = new HashMap();
	private HashMap summaryNodes = new HashMap();

	public LifelineGroupingManager(ShrimpProject project, DisplayBean displayBean) {
		super(project, displayBean);
	}

	/**
	 * Reset state for new grouping
	 *
	 */
	protected void reinitialize() {
		summaryNodes = new HashMap();
		methodExecNodeNames = new HashMap();
	}

	/**
	 * Group the specified node, checking to see if it needs to be made into a summary node
	 * @return true if the node is a summary node
	 */
	protected boolean groupNode(ShrimpNode node, Vector nodes) {
		String summaryName = getSummaryNodeName(node.getName());
		ShrimpNode summaryNode = (ShrimpNode) summaryNodes.get(summaryName);
		if (summaryNode == null) { // need to create the summary node
			makeSummaryNode(node);
			summaryNode = node;
			summaryNodes.put(summaryName, node);
			adjustNode(node, summaryNode, false /* do not redirect arcs */);
			return true;
		} else {
			adjustNode(node, summaryNode, true /* redirect arcs */);
			return false;
		}
	}

	/**
	 * Adjust the edges of an object lifeline node so that they refer to summaryNode instead
     * @tag Shrimp(grouping)
	 * @param node
	 * @param summaryNode
	 */
	private void adjustNode(ShrimpNode node, ShrimpNode summaryNode, boolean redirect) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			ShrimpNode destNode = arc.getDestNode();
			String newMethodExecName =
				getSummaryMethodExecName(destNode.getName());
			while (methodExecNodeNames.containsKey(newMethodExecName)) {
				newMethodExecName = tryNextMethodExecName(newMethodExecName);
			}
			methodExecNodeNames.put(newMethodExecName, null);
			destNode.rename(newMethodExecName);
			if (redirect) {
				arc.redirect(summaryNode, destNode); // redirect source node
			}
		}
	}

	/**
	 * Increment the specified method exec name's counter
	 * @param name
	 * @return
	 */
	private String tryNextMethodExecName(String name) {
		int counter = Integer.parseInt(name.substring(name.lastIndexOf(SEPARATOR)+1));
		return name.substring(0, name.lastIndexOf(SEPARATOR)) + SEPARATOR + (++counter);
	}

	/**
	 * Return a summary methdod execution name by traversing the hierarchical name to
	 * the specified level.
     * @tag Shrimp(grouping)
	 * @param string
	 * @param groupLevel
	 * @return
	 */
	private String getSummaryMethodExecName(String nodeName) {
		String summary = nodeName;
		String methodName = null;
		for (int i=0; i< 3; i++) {
			if (i==0) { // save count
				methodName = summary.substring(summary.lastIndexOf(SEPARATOR));
			}
			if (i==1) { // add on method name
				methodName = summary.substring(summary.lastIndexOf(SEPARATOR)) + methodName;
			}
			summary = summary.substring(0, summary.lastIndexOf(SEPARATOR));
		}
		summary += methodName;
		return summary;
	}

	/**
	 * Ungroup a specified summary node
    * @tag Shrimp(grouping)
	 * @param summaryNode
	 */
	protected void ungroupNode(ShrimpNode summaryNode, Vector nodes) {
		summaryNode.restoreName();
		super.ungroupNode(summaryNode, nodes);
	}

	/**
	 * Restore everything related to this node to its ungrouped state
	 * @param summaryNode
	 * @param methodExecNodeNames
	 */
	protected void restoreNode(ShrimpNode summaryNode) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(summaryNode);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			arc.restore();
			ShrimpNode destNode = arc.getDestNode();
			String oldName = destNode.getName();
			destNode.restoreName();
			adjustPropertyName(oldName, destNode.getName());
		}
	}

	/**
	 * Adjust the node names in the properties file (if necessary)
	 * @param oldNodeName
	 * @param newNodeName
	 */
	private void adjustPropertyName(String oldNodeName, String newNodeName) {
		if (!oldNodeName.equals(newNodeName)) {
			String oldPropertyName = super.makeGroupPropertyName(oldNodeName);
			String property = super.getGroupProperty(oldPropertyName);
			if (property != null) { // property exists
				super.clearGroupProperty(oldPropertyName);
				super.saveGroupProperty(super.makeGroupPropertyName(newNodeName));
			}
		}
	}

	/**
	 * Find all nodes that were grouped to this node.
	 * @param node
	 * @param nodes
	 * @return
	 */
	protected Vector getGroupedNodes(ShrimpNode summaryNode, Vector nodes) {
		Vector relatedNodes = new Vector();
		if (isSummaryNode(summaryNode)) {
			relatedNodes = getNodesAtSameLevel(summaryNode.getName(), nodes, null);
		}
		return relatedNodes;
	}

	/**
	 * Get nodes that are at the same 'level' of abstraction as the specified node,
	 * based on a naming convention of package.subpackage.class
	 * (where abritrary levels of subpackaging are supported).
	 * @param summaryNode
	 * @param nodes
	 * @return
	 */
	protected Vector getRelatedNodes(ShrimpNode summaryNode, Vector nodes) {
		return getNodesAtSameLevel(getSummaryNodeName(summaryNode.getName()), nodes, summaryNode);
	}

	/**
	 * Get all nodes that are at the same hierarchical naming level as the specified
	 * name.
	 * @param nameToCompare
	 * @param nodes
	 * @param ignore - ignore this node (do not add to return vector) - set to null if not needed
	 * @return
	 */
	private Vector getNodesAtSameLevel(String nameToCompare, Vector nodes, ShrimpNode ignore) {
		Vector relatedNodes = new Vector();
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode nodeToCompare = (ShrimpNode) nodes.get(i);
			if (nodeToCompare != ignore &&
					nameToCompare.equals(getSummaryNodeName(nodeToCompare.getName()))) {
				relatedNodes.add(nodeToCompare);
			}
		}
		return relatedNodes;
	}


	/**
	 * Return true if the specified objectlifeline node can be grouped
	 * @param node
	 * @return
	 */
	protected boolean canBeGrouped(ShrimpNode node) {
		return !node.getName().equals(getSummaryNodeName(node.getName()));
	}

	/**
	 * Make a summary node based on the supplied name by choosing
	 * one of the related nodes.
	 */
	protected void makeSummaryNode(ShrimpNode node) {
		node.rename(getSummaryNodeName(node.getName()));
	    clearGroupedAttribute(node);
	    setSummaryAttribute(node);
	}

	/**
	 * Return a summary node name by traversing the hierarchical name to
	 * the specified level.
     * @tag Shrimp(grouping)
	 * @param string
	 * @param groupLevel
	 * @return
	 */
	protected String getSummaryNodeName(String nodeName) {
		String summary = nodeName;
		int index = summary.lastIndexOf(SEPARATOR);
		if (index > 0) {
			summary = summary.substring(0, index);
		}
		return summary;
	}

	protected boolean isApplicableGroupType(ShrimpNode node) {
		return SequenceGroupingManager.isObjectLifeline(node);
	}

}
