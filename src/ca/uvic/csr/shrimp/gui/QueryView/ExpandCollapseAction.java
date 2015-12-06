/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * Uses the {@link ExpandAction} and {@link CollapseAction} to perform an expand or collapse
 * on the selected node depending on whether the node is openable (expand) or not (collapse).
 *
 * @author Chris Callendar
 * @date 15-Dec-06
 */
class ExpandCollapseAction extends QueryViewMenuAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_EXPAND + "/" + ShrimpConstants.ACTION_NAME_COLLAPSE;

	private ExpandAction expandAction;
	private CollapseAction collapseAction;

	public ExpandCollapseAction(ExpandAction expandAction, CollapseAction collapseAction, QueryView queryView) {
		super(ACTION_NAME, null, queryView);
		this.expandAction = expandAction;
		this.collapseAction = collapseAction;
		allowMultipleSelections = false;
	}

	protected void doAction(DisplayBean displayBean, ShrimpNode node) {
		if (node.isOpenable()) {
			expandAction.actionPerformed(null);
		} else {
			collapseAction.actionPerformed(null);
		}
	}


}