/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionEvent;
import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This adapter is responsible for closing the currently selected nodes.
 * This adapter can be set to "remember" or "forget" the 
 * position and panel mode of children when closing a node, . 
 *
 * @author Rob Lintern, Chris Callendar
 * @date Feb 12, 2002
 */
public class CloseSelectedNodesAdapter extends DefaultToolAction {
	
	/**
	 * Constructs a new CloseSelectedNodesAdapter
	 * @param tool The tool that this adapter acts upon.
	 * @param actionName The name to give this action.
	 */
	public CloseSelectedNodesAdapter(String actionName, ShrimpTool tool) {
		super(actionName, ResourceHandler.getIcon("icon_panel_mode_closed.gif"), tool);
	}
	
	public void actionPerformed(ActionEvent e) {
		closeSelectedNodes();
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		closeSelectedNodes();
	}

	public void closeSelectedNodes() {
		try {				
			SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);

			Vector targets = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			for (int i = 0; i < targets.size(); i++) {
				ShrimpNode sn = (ShrimpNode)targets.elementAt(i);
				//sn.setRememberChildrenPositions (rememberChildrenPositions);
				displayBean.setPanelMode(sn, PanelModeConstants.CLOSED);
			}			
			
			// reselect the parents - By doing this the behaviour of this adapter matches double clicking on a node
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());	
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, targets);	
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}	 
	}
}