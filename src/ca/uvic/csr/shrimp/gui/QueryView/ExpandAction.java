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
 * Expands the selected node.  This involves displaying all the neighboring nodes that aren't
 * already being displayed.
 *
 * @author Chris Callendar
 * @date 15-Dec-06
 */
class ExpandAction extends QueryViewMenuAction {

	public static final String EXPAND = ShrimpConstants.ACTION_NAME_EXPAND;

	public ExpandAction(QueryView queryView) {
		super(EXPAND, null, queryView);
		allowMultipleSelections = false;
	}

	protected void doAction(DisplayBean displayBean, ShrimpNode node) {
		expandNode(node, displayBean);
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
	 * Expands the neighborhood around this node and performs a layout.
	 * @param node
	 * @param displayBean
	 */
	public void expandNode(ShrimpNode node, DisplayBean displayBean) {
		Vector nodesAlreadyInDisplay = displayBean.getAllNodes();
		Vector connectedNodes = getConnectedNodes(node, displayBean);
		connectedNodes.removeAll(nodesAlreadyInDisplay);
		if (!connectedNodes.isEmpty()) {
			for (Iterator iter = connectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode connectedNode = (ShrimpNode) iter.next();
				// display the plus icon - this node can potentially be expanded
				connectedNode.setOpenable(true);
				displayBean.addObject(connectedNode);
				displayBean.setTransformOf(connectedNode, displayBean.getTransformOf(node));
				displayBean.setVisible(connectedNode, true, false);
			}

			// re-layout the new nodes
			displayBean.setLayoutMode(displayBean.getVisibleNodes(), queryView.getQueryHelper().getLayoutMode(), false, true);
			displayBean.focusOnExtents(true);
		}

		// this node will be expanded, so hide the + icon
		node.setOpenable(false);
	}

}