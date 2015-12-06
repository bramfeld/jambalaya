/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Selects all the children of the currently selected nodes.
 *
 * @author	Nasir Rather, Chris Callendar
 */
public class SelectAllDescendantsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_SELECT_ALL_DESCENDANTS;
	public static final String TOOLTIP = "Selects all the descendants of the selected node(s).";

	public SelectAllDescendantsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			Vector selected = getSelectedNodes();
			if (!selected.isEmpty()) {
				Set newSelected = new HashSet ();
				for (Iterator iter = selected.iterator(); iter.hasNext();) {
					ShrimpNode node = (ShrimpNode) iter.next();
					Vector descendents = displayBean.getDataDisplayBridge().getDescendentNodes(node, false);
					newSelected.addAll(descendents);
				}
				Vector allDescendents = new Vector (newSelected.size());
				allDescendents.addAll(newSelected);
				setSelectedNodes(allDescendents);
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}


}
