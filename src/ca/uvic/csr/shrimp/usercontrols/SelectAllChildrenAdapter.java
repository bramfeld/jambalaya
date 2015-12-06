/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Selects all the children of the currently selected nodes.
 * @author	Rob Lintern, Chris Callendar
 */
public class SelectAllChildrenAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_SELECT_ALL_CHILDREN;
	public static final String TOOLTIP = "Selects all the children of the selected node(s).";

	public SelectAllChildrenAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		DisplayBean displayBean = null;
		try {
			displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		  	return;
		}

		Vector selected = getSelectedNodes();
		if (selected.isEmpty()) {
		    // if nothing is selected then just select all root nodes
			setSelectedNodes(displayBean.getDataDisplayBridge().getRootNodes());
		} else {
			Vector newSelected = new Vector ();
			for (int i = 0; i < selected.size(); i++) {
				ShrimpNode node = (ShrimpNode)selected.elementAt(i);
				if (displayBean.getPanelMode(node).equals(PanelModeConstants.CHILDREN)) {
					Vector children = displayBean.getDataDisplayBridge().getChildNodes(node);
					for (int j = 0; j < children.size(); j++) {
						if (displayBean.isVisible(children.elementAt(j))) {
							newSelected.add(children.elementAt(j));
						}
					}
				}
			}
			setSelectedNodes(newSelected);
		}
	}


}
