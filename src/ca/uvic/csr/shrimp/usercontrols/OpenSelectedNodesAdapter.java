/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter is responsible for opening the currently selected nodes,
 * showing their children, or all descendents.
 *
 * @author Rob Lintern
 * @date Feb 12, 2002
 */
public class OpenSelectedNodesAdapter extends DefaultToolAction {

	private boolean showOnlyChildren;

	public OpenSelectedNodesAdapter(String actionName, ShrimpProject project, ShrimpTool tool, boolean showOnlyChildren) {
	    this(actionName, (Icon)null, project, tool, showOnlyChildren);
	}

	/**
	 * Constructs a new OpenSelectedNodesAdapter
	 */
	public OpenSelectedNodesAdapter(String actionName, Icon icon, ShrimpProject project, ShrimpTool tool, boolean showOnlyChildren) {
		super(actionName, icon, project, tool);
		this.showOnlyChildren = showOnlyChildren;
	}

	public void startAction() {
		try {
			DataBean dataBean = (DataBean) getProject().getBean(ShrimpProject.DATA_BEAN);
			boolean dataBeanFiringEvents = dataBean.isFiringEvents();
			dataBean.setFiringEvents(false);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			Vector targets = getSelectedNodes();
			displayBean.setPanelMode(targets, PanelModeConstants.CHILDREN);

			if (showOnlyChildren) {
				for (Iterator iter = targets.iterator(); iter.hasNext(); ) {
					ShrimpNode node = (ShrimpNode) iter.next();
					Vector children = displayBean.getDataDisplayBridge().getChildNodes(node);
					for (Iterator iterator = children.iterator(); iterator.hasNext(); ) {
						ShrimpNode child = (ShrimpNode) iterator.next();
						hideDescendents(displayBean, child);
					}
				}
			}

			// reselect the parents - By doing this the behaviour of this adapter matches double clicking on a node
			clearSelectedNodes();
			setSelectedNodes(targets);

			dataBean.setFiringEvents(dataBeanFiringEvents);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

	private void hideDescendents(DisplayBean displayBean, ShrimpNode node) {
		if (displayBean.getPanelMode(node).equalsIgnoreCase(PanelModeConstants.CHILDREN)) {
			displayBean.setPanelMode(node,PanelModeConstants.CLOSED);
		}
	}

}