/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * This action hides nodes (and arcs) from the {@link QueryView}'s {@link DisplayBean}.
 * It doesn't use filters, it just removes the objects from the display.
 * It can hide one single node (and all the connected arcs), and it can hide all the
 * ancestor nodes (incoming arcs) and descendant nodes (outgoing arcs).
 *
 * @author Chris Callendar
 * @date 15-Dec-06
 */
class HideAction extends QueryViewMenuAction {

	public static final String HIDE = ShrimpConstants.ACTION_NAME_HIDE;
	public static final String HIDE_ANCESTORS = ShrimpConstants.ACTION_NAME_HIDE_ANCESTORS;
	public static final String HIDE_DESCENDANTS = ShrimpConstants.ACTION_NAME_HIDE_DESCENDANTS;

	private boolean hideAncestors;
	private boolean hideDescendants;

	public HideAction(QueryView queryView) {
		this(queryView, false, false);
	}

	/**
	 * @param hideAncestors
	 * @param hideDescendants
	 */
	public HideAction(QueryView queryView, boolean hideAncestors, boolean hideDescendants) {
		super((hideAncestors ? HIDE_ANCESTORS : (hideDescendants ? HIDE_DESCENDANTS : HIDE)), null, queryView);
		this.hideAncestors = hideAncestors;
		this.hideDescendants = hideDescendants;
	}

	public HideAction(String name, Icon icon, QueryView queryView, boolean hideAncestors, boolean hideDescendants) {
		super(name, icon, queryView);
	}

	protected void doAction(DisplayBean displayBean, ShrimpNode node) {
		Vector expandableNodes = null;
		if (hideAncestors || hideDescendants) {
			// collect the neighboring nodes that are still going to be visible
			if (hideAncestors) {
				expandableNodes = displayBean.getDataDisplayBridge().getNeighborhood(node, 1, 0);
			} else if (hideDescendants) {
				expandableNodes = displayBean.getDataDisplayBridge().getNeighborhood(node, 1, 0);
			}
			// indicate that this node can be expanded (shows a plus icon)
			node.setOpenable(true);
			hideNodesRecursively(displayBean, node, new HashSet());

		} else {
			// show the plus icon for all directly attached nodes
			expandableNodes = displayBean.getDataDisplayBridge().getNeighborhood(node);

			// hide just this node
			displayBean.removeObject(node);
			displayBean.setVisible(node, false, false);
		}

		// update these nodes to be expandable
		expandableNodes.remove(node);
		for (Iterator iter = expandableNodes.iterator(); iter.hasNext();) {
			ShrimpNode sn = (ShrimpNode) iter.next();
			sn.setOpenable(true);
		}

	}

	private void hideNodesRecursively(DisplayBean displayBean, ShrimpNode node, HashSet seenNodes) {
		if (seenNodes.contains(node)) {
			return;
		}
		seenNodes.add(node);

		Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
		if (!arcs.isEmpty()) {
			for (Iterator iter = arcs.iterator(); iter.hasNext(); ) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				//displayBean.removeObject(arc);
				//displayBean.setVisible(arc, false, false);

				// if we are hiding descendants or ancestors then recurse on the "other" node
				ShrimpNode src = arc.getSrcNode();
				ShrimpNode dest = arc.getDestNode();
				boolean isSrc = (node.equals(src));
				boolean isDest = !isSrc && (node.equals(dest));
				ShrimpNode otherNode = (isSrc ? dest : src);
				if (hideAncestors && isDest) {
					hideNodesRecursively(displayBean, otherNode, seenNodes);
					displayBean.removeObject(otherNode);
					displayBean.setVisible(otherNode, false, false);
				}
				if (hideDescendants && isSrc) {
					hideNodesRecursively(displayBean, otherNode, seenNodes);
					displayBean.removeObject(otherNode);
					displayBean.setVisible(otherNode, false, false);
				}
			}
		}
	}

}