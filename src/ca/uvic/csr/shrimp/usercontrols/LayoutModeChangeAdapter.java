/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to change the layout of the currently selected nodes.
 *
 * @author Casey Best
 * @date July 27, 2000
 */
public class LayoutModeChangeAdapter extends DefaultToolAction {

	public static final int APPLY_TO_CHILDREN_OF_SELECTED = 0;
	public static final int APPLY_TO_SELECTED = 1;

	private int mode;

	/**
	 * @param tool
	 * @param mode APPLY_TO_CHILDREN_OF_SELECTED or APPLY_TO_SELECTED
	 */
	public LayoutModeChangeAdapter(ShrimpTool tool, int mode) {
		super("LayoutModeChangeAdapter", tool);
		this.mode = mode;
		mustStartAndStop = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		String defaultLayoutName = ApplicationAccessor.getProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE,
				DisplayBean.PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);
		changeLayout(defaultLayoutName, false);
	}

	public void changeLayout(String newMode, boolean showLayoutDialog) {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector targets = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			Vector nodes = new Vector(targets.size());

			// if there are no targets or no hierarchy - apply the layout to the root nodes
			if (targets.isEmpty() || displayBean.isFlat()){
				nodes = displayBean.getDataDisplayBridge().getRootNodes();
			} else {
				if (mode == APPLY_TO_CHILDREN_OF_SELECTED) {
					for (Iterator iter = targets.iterator(); iter.hasNext();) {
						ShrimpNode parentNode = (ShrimpNode) iter.next();
						nodes.addAll(displayBean.getDataDisplayBridge().getChildNodes(parentNode));
					}
				} else if (mode == APPLY_TO_SELECTED) {
					nodes = targets;
				}
			}

			Collections.sort(nodes);
			displayBean.setLayoutMode(nodes, newMode, showLayoutDialog, true);
			displayBean.requestFocus();	// give focus to the canvas
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}


}