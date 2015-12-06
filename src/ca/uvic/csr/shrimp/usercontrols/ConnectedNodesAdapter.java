/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 *
 * @author Rob Lintern, Chris Callendar
 */
public abstract class ConnectedNodesAdapter extends DefaultToolAction {

	public final String ACTION_NAME;

	protected String direction;

	/**
	 * @param direction see {@link DisplayConstants}
	 * @see DisplayConstants#INCOMING_AND_OUTGOING
	 * @see DisplayConstants#INCOMING
	 * @see DisplayConstants#OUTGOING
	 */
	public ConnectedNodesAdapter(String actionName, ShrimpProject project, ShrimpTool tool, String direction) {
		this(actionName, (Icon)null, project, tool, direction);
	}

	/**
	 * @param direction see {@link DisplayConstants}
	 * @see DisplayConstants#INCOMING_AND_OUTGOING
	 * @see DisplayConstants#INCOMING
	 * @see DisplayConstants#OUTGOING
	 */
	public ConnectedNodesAdapter(String actionName, Icon icon, ShrimpProject project, ShrimpTool tool, String direction) {
		super(actionName, icon, project, tool);
		ACTION_NAME = actionName;
		this.direction = direction;
		mustStartAndStop = false;
	}

	protected Collection getConnectedNodes(boolean createNodesIfNotFound, boolean arcMustBeVisible) {
		Collection arcTypes = new ArrayList();
		DataBean dataBean = (DataBean) getBean(ShrimpProject.DATA_BEAN);
		if (dataBean != null) {
			arcTypes = dataBean.getRelationshipTypes(false, true);
		}
		return getConnectedNodes(createNodesIfNotFound, arcMustBeVisible, arcTypes);
	}

	protected Collection getConnectedNodes(boolean createNodesIfNotFound, boolean arcMustBeVisible, Collection arcTypes) {
		Collection connectedNodes = new HashSet();
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
			Vector selectedNodes = getSelectedNodes();
			for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode selectedNode = (ShrimpNode) iter.next();
				Artifact selectedArt = selectedNode.getArtifact();
				Vector rels = selectedArt.getRelationships();
				for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
					Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
					if (DisplayConstants.INCOMING.equals(direction)) {
						if (selectedArt.equals(destArt)) {
							dataDisplayBridge.getShrimpNodes(srcArt, createNodesIfNotFound);
						}
					} else if (DisplayConstants.OUTGOING.equals(direction)) {
						if (selectedArt.equals(srcArt)) {
							dataDisplayBridge.getShrimpNodes(destArt, createNodesIfNotFound);
						}
					} else {
						dataDisplayBridge.getShrimpNodes(srcArt, createNodesIfNotFound);
						dataDisplayBridge.getShrimpNodes(destArt, createNodesIfNotFound);
					}
				}

				Vector arcs = dataDisplayBridge.getShrimpArcs(selectedNode);
				for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
					ShrimpArc arc = (ShrimpArc) iterator.next();
					if (!arcMustBeVisible || arc.isVisible()) {
						if (DisplayConstants.INCOMING.equals(direction)) {
							if (selectedNode.equals(arc.getDestNode())) {
								connectedNodes.add(arc.getSrcNode());
							}
						} else if (DisplayConstants.INCOMING.equals(direction)) {
							if (selectedNode.equals(arc.getSrcNode())) {
								connectedNodes.add(arc.getDestNode());
							}
						} else {
							connectedNodes.add(arc.getDestNode());
							connectedNodes.add(arc.getSrcNode());
						}
					}
				}
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
		return connectedNodes;
	}

}
