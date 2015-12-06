/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This UserAction adapter opens and closes nodes.
 *
 * @author Casey Best, Chris Callendar
 * @date Jan 30, 2001
 */
public class OpenCloseNodeAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_OPEN_CLOSE_NODE;
	public static final String TOOLTIP = "Expands or collapses the selected node.\n" +
		"In a nested view this means it opens or closes the node (shows/hides the children).\n" +
		"In a flat view it expands or collapses the descendants.";

    public OpenCloseNodeAdapter(ShrimpProject project, ShrimpTool tool) {
    	super(ACTION_NAME, project, tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;
    }

    public void startAction() {
		try {
			DataBean dataBean = (DataBean) getProject().getBean(ShrimpProject.DATA_BEAN);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);

			ShrimpNode target = getSelectedNode();
			if (target == null) {
				return;
			}

			Vector selected = getSelectedNodes();
			Vector targets = new Vector();
			if (selected.contains(target)) {
				targets = selected;
			} else {
				targets.add(target);
			}

			if (targets.isEmpty()) {
				return;
			}

			boolean dataBeanFiringEvents = dataBean.isFiringEvents();
			dataBean.setFiringEvents(false);

			for (int i = 0; i < targets.size(); i++) {
				ShrimpNode node = (ShrimpNode)targets.elementAt(i);
				if (displayBean.isFlat()) {
					// @tag Shrimp.Collapse : expand/collapse flat graphs
					FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
					ExpandCollapseSubgraphAdapter.expandCollapseNode(displayBean, filterBean, node,
							new Vector(), true, false, true);
				} else {
					if (displayBean.isNodeOpen(node)) {
						displayBean.closeNode(node);
					} else {
						displayBean.openNode(node);
					}
				}
			}
			dataBean.setFiringEvents(dataBeanFiringEvents);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
    }

}
