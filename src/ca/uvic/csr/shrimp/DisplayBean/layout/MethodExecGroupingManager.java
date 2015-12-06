/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. 
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * Handles grouping of method executions.
 * Note it is assumed that this processing will be called before Object Life 
 * Line grouping calculations occur.
 * @author Chris Bennett
 *
 */
public class MethodExecGroupingManager extends SequenceGroupingManager {
	
	private HashMap groupedNodeNames = null;

	public MethodExecGroupingManager(ShrimpProject project, DisplayBean displayBean) {
		super(project, displayBean);
	}

	/**
	 * Reset state for new grouping
	 *
	 */
	protected void reinitialize() {
		buildGroupedPropertiesMap();
	}
	
	/**
	 * Build a map of grouped properties for use in determining if 
	 * a method execution is a summary node.
	 */
	private void buildGroupedPropertiesMap() {
		groupedNodeNames = new HashMap();
		Properties properties = project.getProperties();
		Object[] keys = properties.keySet().toArray();
		for (int i=0; i<keys.length; i++) {
			String propertyName = (String)keys[i];
			if (isGroupedProperty(propertyName)) {
				String nodeName = getNodeNamefromGroupProperty(propertyName);
				groupedNodeNames.put(nodeName, nodeName);
			}
		}
	}

	/**
	 * Return true if this node is identified in a grouped property
	 * @param node
	 * @return
	 */
	private boolean isGroupedNode(ShrimpNode node) {
		// Note it is not enough to just check attributes set in the properties file. 
		// We must also check if the specified node's grouped attribute is set.
		// TODO - could this be done more cleanly?

		// Check saved node names
		boolean savedNameMatches = false;
		String[] savedNodeNames = node.getSavedNames();
		for (int i=0; i<savedNodeNames.length; i++) {
			if (groupedNodeNames.get(savedNodeNames[i]) != null) {
				savedNameMatches = true;
			}
		}	
		return savedNameMatches 
			|| groupedNodeNames.get(node.getName()) != null
			|| isGroupedAttributeSet(node) 
			|| isSummaryNode(node);
	}

	/**
	 * Group the specified node, checking to see if it needs to be made into a summary node
 	 * @param node
	 * @param nodes
	 * @return true if the node is a summary node
	 */
	protected boolean groupNode(ShrimpNode node, Vector nodes) {
		if (isSummaryNode(node, nodes)) {
			makeSummaryNode(node);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Override the parent method because we do not need to rename 
	 * the node.
	 */
	protected void makeSummaryNode(ShrimpNode node) {
	    clearGroupedAttribute(node);
	    setSummaryAttribute(node);
	}

	/**
	 * Based on this node's grouping and related method executions,
	 * determine if it is the summary (first method execution) 
	 * @param node
	 * @param nodes
	 * @return
	 */
	private boolean isSummaryNode(ShrimpNode node, Vector nodes) {
		if (!isGroupedNode(node)) {
			return false;
		}
		else { 
			Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
			for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) arcsIter.next();
				if (isReturnValue(arc) && isGroupedNode(arc.getDestNode()) &&
						arc.getSrcNode() == node) {
					return false; // is not the root method execution in a group
				}
			}	
			return true; // is a summary node
		}
	}	

	/**
	 * After grouping clean up any object life lines that may now be orphaned
	 * @param nodes
	 */
	protected void cleanup(Vector nodes) {
		hideOrphanedLifeLines(nodes);
	}

	/**
	 * Hide all object life lines that might have been orphaned
	 * by grouping method executions.   
	 * @param nodes
	 */
	private void hideOrphanedLifeLines(Vector nodes) {
		for (Iterator nodesIter = nodes.iterator(); nodesIter.hasNext();) {
			ShrimpNode node = (ShrimpNode) nodesIter.next();
			if (SequenceGroupingManager.isObjectLifeline(node)) {
				boolean hasVisibleMethodExecs = false;
				Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
				for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext() && 
				     !hasVisibleMethodExecs;) {
					ShrimpArc arc = (ShrimpArc) arcsIter.next();
					// Assume object life lines contain method executions and nothing else
					if (isContainsRelationship(arc)) { 
						ShrimpNode destNode = arc.getDestNode();
						hasVisibleMethodExecs = !isGroupedAttributeSet(destNode);
					}
				}	
				if (!hasVisibleMethodExecs) {
					super.setGroupedAttribute(node);
				} 
				else { // ensure previously hidden lifelines are restored
					super.clearGroupedAttribute(node);
					
				}
			}
		}	
	}
	
	/**
	 * Ungroup a specified summary node. Overrides parent and adds 
	 * clearing of saved names.
     * @tag Shrimp(grouping)
	 * @param summaryNode
	 */
	protected void ungroupNode(ShrimpNode summaryNode, Vector nodes) {
		super.ungroupNode(summaryNode, nodes);
		for (int i = 0; i < nodes.size(); i++) {
			String[] savedNodeNames = ((ShrimpNode) nodes.get(i)).getSavedNames();
			for (int j=0; j<savedNodeNames.length; j++) {
				String propertyName = makeGroupPropertyName(savedNodeNames[j]);
				clearGroupProperty(propertyName);
			}	
		}
	}

	/**
	 * Restore a grouped node
	 */
	protected void restoreNode(ShrimpNode summaryNode) {
		// Do nothing
	}

	/**
	 * Get the nodes to be grouped with the specified  node
	 * @param node
	 * @param nodes
	 */
	protected Vector getRelatedNodes(ShrimpNode node, Vector nodes) {
		Vector relatedNodes = new Vector();
		getRelatedNodesRecursive(node, relatedNodes, false /* do not stop at summary node */);
		return relatedNodes;
	}
	
	/**
	 * Get the nodes to be grouped with the specified  node
	 * @param node
	 * @param nodes
	 * @param stopAtSummary stop the search if we hit a summary node
	 */
	protected Vector getRelatedNodes(ShrimpNode node, Vector nodes, 
			boolean stopAtSummary) {
		Vector relatedNodes = new Vector();
		getRelatedNodesRecursive(node, relatedNodes, stopAtSummary);
		return relatedNodes;
	}
	
	/**
	 * Get the nodes to be grouped with the specified  node
	 * @param currentNode
	 * @param relatedNodes list of related nodes (to add to)
	 * @param stopAtSummary stop the search if we hit a summary node
	 */
	private void getRelatedNodesRecursive(ShrimpNode currentNode, Vector relatedNodes, 
			boolean stopAtSummary) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(currentNode);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			ShrimpNode nextNode = arc.getDestNode();
			if (isMethodCall(arc) && !visited(nextNode, relatedNodes) &&
				nextNode != currentNode && !(stopAtSummary && isSummaryNode(nextNode))) {
				relatedNodes.add(nextNode);
				getRelatedNodesRecursive(nextNode, relatedNodes, stopAtSummary);
			}
		}
	}

	/**
	 * Return true if the specified node is in nodes
	 * @param node
	 * @param nodes
	 * @return
	 */
	private boolean visited(ShrimpNode node, Vector nodes) {
		for (int i=0; i<nodes.size(); i++) {
			ShrimpNode nodeToCompare = (ShrimpNode)nodes.get(i);
			if (node.equals(nodeToCompare)) {
				return true; // found
			}
		}
		return false; // not visited
	}
	
	/**
	 * Return true if an edge is a method call edge
	 * @param edge
	 * @return
	 */
	private boolean isMethodCall(ShrimpArc arc) {
		return arc.getRelationship().getType().equals(
				JavaDomainConstants.METHOD_CALL_REL_TYPE);
	}	

	/**
	 * Return true if an edge is a method call edge
	 * @param edge
	 * @return
	 */
	private boolean isContainsRelationship(ShrimpArc arc) {
		return arc.getRelationship().getType().equals(
				JavaDomainConstants.CONTAINS_REL_TYPE);
	}	

	/**
	 * Return true if an edge is a method call edge
	 * @param edge
	 * @return
	 */
	private boolean isReturnValue(ShrimpArc arc) {
		return arc.getRelationship().getType().equals(
				JavaDomainConstants.RETURN_VALUE_REL_TYPE);
	}	

	/**
	 * Get the nodes to be grouped with the specified summary node
	 */
	protected Vector getGroupedNodes(ShrimpNode summaryNode, Vector nodes) {
		return getRelatedNodes(summaryNode, nodes, true /* stop at summary node */);
	}
	
	/**
	 * Return true if the specified node can be grouped
	 * @param node
	 * @return
	 */
	protected boolean canBeGrouped(ShrimpNode node) {
		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
		for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) arcsIter.next();
			if (isContainsRelationship(arc)) {
				if (SequenceGroupingManager.isActor(arc.getSrcNode())) {
					return false;
				}
			}
		}	
		return true; // all method executions can be grouped
	}
	
	/**
	 * Return a summary node name (same as the specified node name)
     * @tag Shrimp(grouping)
	 * @param string
	 * @param groupLevel
	 * @return
	 */
	protected String getSummaryNodeName(String nodeName) {
		return nodeName;
	}

	protected boolean isApplicableGroupType(ShrimpNode node) {
		return SequenceGroupingManager.isMethodExecution(node);
	}
}
