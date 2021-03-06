/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Causes the shrimp view to navigate to the source of an arc
 * @author Rob Lintern
 */
public class NavigateToArcDestAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = "Go To Destination";

	public NavigateToArcDestAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
	    mustStartAndStop = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		Vector selectedArcs = getSelectedArcs();
		if (selectedArcs.size() == 1) {
			ShrimpArc arc = (ShrimpArc) selectedArcs.firstElement();
			ShrimpNode destNode = arc.getDestNode();
			Object oldTarget = getTargetObject();
			setTargetObject(destNode);
			String oldZoomMode = getZoomMode();
			setZoomMode(DisplayConstants.MAGNIFY);

			MagnifyInAdapter tempMIA = new MagnifyInAdapter(tool);
			tempMIA.startAction();
			if (oldTarget != null) {
				setTargetObject(oldTarget);
			}
			setZoomMode(oldZoomMode);
		}
	}

}
