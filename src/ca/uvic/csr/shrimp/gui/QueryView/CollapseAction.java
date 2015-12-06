/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 *
 * Collapses the selected node.  This involves hiding any of its neighbors that are leaf nodes and
 * aren't part of the search results.
 *
 * @author Chris Callendar
 * @date 15-Dec-06
 */
class CollapseAction extends QueryViewMenuAction {

	public static final String COLLAPSE = ShrimpConstants.ACTION_NAME_COLLAPSE;

	public CollapseAction(QueryView queryView) {
		super(COLLAPSE, null, queryView);
		allowMultipleSelections = false;
	}

	protected void doAction(DisplayBean displayBean, ShrimpNode node) {
		collapseNode(node, displayBean);
	}

	protected Vector getConnectedNodes(ShrimpNode node, DisplayBean displayBean) {
		Vector nodes = new Vector(0);
		DataBean dataBean = getDataBean();
		if ((dataBean != null) && (node.getArtifact() != null)) {
			Collection artTypes = queryView.getArtTypes();
			Collection relTypes = queryView.getRelTypes();
			List srcArtifacts = new ArrayList(1);
			srcArtifacts.add(node.getArtifact());

			// use incoming/outgoing levels of 2 - this will get the immediate neighbors including the node
			Collection artifacts = dataBean.getConnectedArtifacts(srcArtifacts, artTypes, relTypes, 2, 2);
			if (!artifacts.isEmpty()) {
				nodes = displayBean.getDataDisplayBridge().getShrimpNodes(new Vector(artifacts), true);
			}
		}
		return nodes;
	}

	/**
	 * Collapses all the neighbors of the given node that are not matching nodes.
	 * @param node
	 * @param displayBean
	 */
	public void collapseNode(ShrimpNode node, DisplayBean displayBean) {
		boolean collapsed = false;

		Vector connectedNodes = getConnectedNodes(node, displayBean);
		connectedNodes.remove(node);
		if (!connectedNodes.isEmpty()) {
			// we don't want to collapse matching nodes
			Collection matchingNodes = queryView.getQueryHelper().getMatchingNodes();

			for (Iterator iter = connectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode connectedNode = (ShrimpNode) iter.next();

				// only want to remove nodes that weren't part of the search results
				if (!matchingNodes.contains(connectedNode)) {
					// ccallendar: this was an attempt to only remove leaf nodes, but it didn't work well
					// get the neighbors of this node (includes the connected node)
//					Vector nodes = displayBean.getDataDisplayBridge().getNeighborhood(connectedNode, 1, 1);
//					// we don't want to include the original node, or any of it's connected nodes
//					nodes.remove(node);
//					nodes.removeAll(connectedNodes);
//					// if it is a leaf - no other neighbors then we can collapse it
//					if (nodes.size() == 0) {
						displayBean.removeObject(connectedNode);
						displayBean.setVisible(connectedNode, false, false);
						collapsed = true;
//					}
				}
			}
			if (collapsed) {
				// re-layout?
				displayBean.setLayoutMode(displayBean.getVisibleNodes(), queryView.getQueryHelper().getLayoutMode(), false, true);
				displayBean.focusOnExtents(true);
			}
		}
		node.setOpenable(collapsed);
	}

}