/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.CollapsedNodeFilter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to collapse descendants of the selected nodes.
 * It uses the {@link DataDisplayBridge} to get the {@link ShrimpArc}
 * objects and recursively filters all the destination nodes.
 *
 * @tag Shrimp(Collapse)
 * @author Chris Callendar
 * @date November 23rd, 2006
 */
public class ExpandCollapseSubgraphAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_EXPAND_COLLAPSE_SUBGRAPH;

	private final boolean outgoing;
	private final boolean animate;

	/**
	 * Constructs a new {@link ExpandCollapseSubgraphAdapter}.
	 * @param tool The tool that this adapter acts upon.
	 * @param outgoing if outgoing relationships should be traversed, or if incoming ones should be
	 */
	public ExpandCollapseSubgraphAdapter(ShrimpTool tool, boolean outgoing) {
		this(tool, outgoing, true);
	}

	/**
	 * Constructs a new {@link ExpandCollapseSubgraphAdapter}.
	 * @param tool The tool that this adapter acts upon.
	 * @param outgoing if outgoing relationships should be traversed, or if incoming ones should be
	 * @param animate if animation should be used when expanding nodes (re-layout)
	 */
	public ExpandCollapseSubgraphAdapter(ShrimpTool tool, boolean outgoing, boolean animate) {
		super(ACTION_NAME, tool);
	    mustStartAndStop = false;
	    this.outgoing = outgoing;
	    this.animate = animate;
	}

	public void startAction() {
	    try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	    	Vector selectedNodes = getSelectedNodes();

	    	// iterate through the selected nodes and add all the subgraphs (outgoing OR incoming connections only)
	    	expandCollapseNodes(displayBean, filterBean, selectedNodes, outgoing, !outgoing, animate);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
	    }
	}

	/**
	 * Iterates through all the nodes and calling
	 * {@link ExpandCollapseSubgraphAdapter#expandCollapseNode(DisplayBean, FilterBean, ShrimpNode, Vector, boolean, boolean, boolean)
	 * @param nodes the nodes to filter, all {@link ShrimpNode} objects in this list will stay visible
	 * @param outgoing if outgoing arcs should be traversed
	 * @param incoming if outgoing arcs should be traversed
	 * @param animate if animation should be used when expanding nodes (re-layout)
	 */
	public static void expandCollapseNodes(DisplayBean displayBean, FilterBean filterBean, Vector nodes,
			boolean outgoing, boolean incoming, boolean animate) {
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			ShrimpNode node = (ShrimpNode) iter.next();
			expandCollapseNode(displayBean, filterBean, node, nodes, outgoing, incoming, animate);
		}
	}

	/**
	 * Toggles whether the given node is expanded or collapsed.
	 * Traverses all the outgoing connections and filters or unfilters all the connected nodes (except
	 * the excluded ones).  If the node is being expanded the current layout will be re-applied.
	 * @param node the node to expand or collapse - it stays visible
	 * @param nodesToExclude the nodes to exclude in the filtering/unfiltering
	 * @param outgoing if outgoing arcs should be traversed
	 * @param incoming if outgoing arcs should be traversed
	 * @param animate if animation should be used when expanding nodes (re-layout)
	 */
	public static void expandCollapseNode(DisplayBean displayBean, FilterBean filterBean, ShrimpNode node,
			Vector nodesToExclude, boolean outgoing, boolean incoming, boolean animate) {
		Vector nodesToFilter = new Vector();
		Vector subgraph = displayBean.getDataDisplayBridge().getShrimpNodeSubgraph(node, outgoing, incoming, false);
		nodesToFilter.addAll(subgraph);

		// remove any excluded nodes, including the current node since it should stay visible
		nodesToFilter.remove(node);
		if ((nodesToExclude != null) && !nodesToExclude.isEmpty()) {
			nodesToFilter.removeAll(nodesToExclude);
		}

		if (nodesToFilter.size() > 0) {
			node.setIsCollapsed(!node.isCollapsed());

			// now that we have all the nodes - add all their IDs to the set
	    	HashSet idsToFilter = new HashSet(nodesToFilter.size());
			for (Iterator iter = nodesToFilter.iterator(); iter.hasNext(); ) {
				ShrimpNode sn = (ShrimpNode) iter.next();
				idsToFilter.add(new Long(sn.getArtifact().getID()));
			}

			filterNodesByID(displayBean, filterBean, node, idsToFilter, animate);
		} else if (node.isCollapsed()) {
			// even though there are not nodes to filter we still want to expand this node
			// this just changes the node shape back to the original shape
			node.setIsCollapsed(false);
		}
	}

	private static void filterNodesByID(DisplayBean displayBean, FilterBean filterBean, ShrimpNode node,
										Set idsToFilter, boolean animate) {
		boolean changed = false;
		if (!idsToFilter.isEmpty()) {
			Vector filters = filterBean.getFiltersOfType(FilterConstants.COLLAPSED_NODE_FILTER_TYPE, FilterConstants.ARTIFACT_FILTER_TYPE);
			Long artifactID = new Long(node.getArtifact().getID());
			if (node.isCollapsed()) {
				if ((filters.size() == 0)) {
					// CREATE FILTER
					CollapsedNodeFilter filter = new CollapsedNodeFilter(artifactID, idsToFilter);
					filterBean.addFilter(filter);
				} else if (filters.size() > 0) {
					// UPDATE FILTER
					CollapsedNodeFilter filter = (CollapsedNodeFilter) filters.get(0);
					filter.collapseArtifact(artifactID, idsToFilter);
					filterBean.applyFilter(filter);	// force the filter to be applied again
				}
				changed = true;
			} else {
				if (filters.size() > 0) {
					// REMOVE FILTER
					CollapsedNodeFilter filter = (CollapsedNodeFilter) filters.get(0);
					filter.expandArtifact(artifactID, idsToFilter);
					if (filter.getFilterCount() == 0) {
						try {
							filterBean.removeFilter(filter);
						} catch (FilterNotFoundException e) {}
					} else {
						filterBean.applyFilter(filter);
					}
					changed = true;
				}
			}
		}
		// only need to re-layout if the node is expanded
		if (changed && !node.isCollapsed()) {
			displayBean.refreshLayout(animate);
		}
	}

}