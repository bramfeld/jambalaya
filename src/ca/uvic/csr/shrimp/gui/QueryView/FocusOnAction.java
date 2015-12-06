/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * Focusses on the selected node.
 * 
 * @author Chris Callendar
 * @date 6-Mar-07
 */
class FocusOnAction extends QueryViewMenuAction {

	public static final String FOCUS_ON = ShrimpConstants.ACTION_NAME_FOCUS_ON;

	public FocusOnAction(QueryView queryView) {
		super(FOCUS_ON, null, queryView);
		allowMultipleSelections = false;
	}

	protected void doAction(DisplayBean displayBean, ShrimpNode node) {
		// this action runs the query again using the selected ShrimpNode
		// this isn't ideal - the context is lost and all the nodes are re-layed out
		// it would be better to do what expand does and perform the focus that way		
		Vector srcArtifacts = new Vector(1);
		srcArtifacts.add(node.getArtifact());
		queryView.setSrcArtifacts(srcArtifacts);
		queryView.getQueryHelper().doQuery();
	}
	
}