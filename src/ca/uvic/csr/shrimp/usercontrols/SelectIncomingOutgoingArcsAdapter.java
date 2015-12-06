/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * @author Rob Lintern
 *
 * This adapter selects the incoming arcs or outgoing arcs or both of the currently selected nodes.
 */
public class SelectIncomingOutgoingArcsAdapter extends DefaultProjectAction {

	private String direction;

	/**
	 *
	 * @param direction see {@link DisplayConstants}
	 * @see DisplayConstants#INCOMING_AND_OUTGOING
	 * @see DisplayConstants#OUTGOING
	 * @see DisplayConstants#INCOMING
	 */
	public SelectIncomingOutgoingArcsAdapter(ShrimpProject project, String direction) {
		super(direction + " Arcs", project);
		this.direction = direction;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		if (getProject() == null) {
			return;
		}

		try {
			ShrimpView shrimpView = (ShrimpView) getProject().getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
			Vector selectedNodes = getSelectedNodes(shrimpView);
			Vector outgoingArcs = new Vector();
			for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode node = (ShrimpNode) iter.next();
				Vector arcs = dataDisplayBridge.getShrimpArcs(node);
				for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
					ShrimpArc sa = (ShrimpArc) iterator.next();
					if (DisplayConstants.INCOMING.equals(direction)) {
						if (node.equals(sa.getDestNode())) {
							outgoingArcs.add(sa);
						}
					} else if (DisplayConstants.OUTGOING.equals(direction)) {
						if (node.equals(sa.getSrcNode())) {
							outgoingArcs.add(sa);
						}
					} else {
						outgoingArcs.add(sa);
					}
				}
			}
			setSelectedNodes(shrimpView, outgoingArcs);
		} catch (ShrimpToolNotFoundException e) {
			// do nothing
		} catch (BeanNotFoundException e) {
			// do nothing
		}
	}

}
