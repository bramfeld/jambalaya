/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDoubleClickEvent;

/**
 * This is the adapter used to handle double clicking in a protege widget.
 *
 * @author Casey Best, Chris Callendar
 * @date June 29, 2001
 */
public class ProtegeDoubleClickAdapter implements CustomizedPanelActionListener {

	private ViewTool tool;

	public ProtegeDoubleClickAdapter(ViewTool tool) {
		this.tool = tool;
	}

	/**
	 * When the protege form is double clicked, this method is called.
	 */
	public void actionPerformed (CustomizedPanelActionEvent event) {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			if (event instanceof ProtegeDoubleClickEvent) {
				ProtegeDoubleClickEvent pdce = (ProtegeDoubleClickEvent) event;
				Artifact target = pdce.getTarget();
				Vector targetNodes = target == null ? new Vector () : displayBean.getDataDisplayBridge().getShrimpNodes(target, true);
				if (target == null || targetNodes.isEmpty()) {
	    			String msg = "Sorry, the destination node is not currently in this view!";
					JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), msg,
							ApplicationAccessor.getAppName(), JOptionPane.INFORMATION_MESSAGE);
	    		} else {
					ShrimpNode targetNode = (ShrimpNode) targetNodes.firstElement();
					tool.navigateToObject(targetNode);
	    		}
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

	/**
	 * Returns the type of panel this listener is meant for
	 */
	public String getCustomizedPanelType() {
		return ProtegeArtifact.PANEL_FORM;
	}


}