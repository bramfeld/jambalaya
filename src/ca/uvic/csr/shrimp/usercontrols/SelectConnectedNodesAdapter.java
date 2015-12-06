/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter selects the nodes connected by incoming or outgoing arcs or both of the currently selected nodes.
 *
 * @author Jeff Michaud, Rob Lintern, Chris Callendar
 */
public class SelectConnectedNodesAdapter extends ConnectedNodesAdapter {

    /**
     * @param tool
     * @param direction see {@link DisplayConstants}
     * @see DisplayConstants#INCOMING_AND_OUTGOING
     * @see DisplayConstants#INCOMING
     * @see DisplayConstants#OUTGOING
     */
    public SelectConnectedNodesAdapter(ShrimpProject project, ShrimpTool tool, String direction) {
		super("Select " + direction + " Nodes", project, tool, direction);
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		Vector selectedNodes = getSelectedNodes();
		Collection connectedNodes = getConnectedNodes(false, true);
		for (Iterator iter = connectedNodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			if (!selectedNodes.contains(node)) {
				selectedNodes.add(node);
			}
		}
		setSelectedNodes(selectedNodes);
	}

}
