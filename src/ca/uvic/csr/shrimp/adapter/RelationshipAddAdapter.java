/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipAddListener;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * @author Jeff Michaud, Chris Callendar
 * @date Nov 18, 2002
 */
public class RelationshipAddAdapter implements RelationshipAddListener {

	private ShrimpTool tool;

	public RelationshipAddAdapter(ShrimpTool tool) {
		this.tool = tool;
	}

	public void addRelationship (RelationshipAddEvent e) {
		if (tool == null) {
			return;
		}

		try {
			Relationship relationship = e.getRelationshipAdded();
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
			dataDisplayBridge.addRelationship(relationship);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

}
