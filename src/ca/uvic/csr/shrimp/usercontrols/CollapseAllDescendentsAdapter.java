/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * @author Rob Lintern, Chris Callendar
 */
public class CollapseAllDescendentsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_COLLAPSE_ALL_DESCENDANTS;

	/**
	 * Constructor for {@link CollapseAllDescendentsAdapter}.
	 */
	public CollapseAllDescendentsAdapter(ShrimpProject project, ShrimpTool tool) {
		super(ACTION_NAME, project, tool);
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
		    Vector selected = getSelectedNodes();
		    for (Iterator iter = selected.iterator(); iter.hasNext();) {
				ShrimpNode node = (ShrimpNode) iter.next();
				collapseRecursive(displayBean, node);
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

	private void collapseRecursive(DisplayBean displayBean, ShrimpNode parentNode) {
		DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();

		// if there are any children, collapse them first
		Vector children = dataDisplayBridge.getChildNodes(parentNode);
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			ShrimpNode child = (ShrimpNode) iter.next();
			collapseRecursive(displayBean, child);
		}
		displayBean.setPanelMode(parentNode, PanelModeConstants.CLOSED);
	}

}
