/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Handles zooming out and in.
 *
 * @author Rob Lintern, Chris Callendar
 */
public abstract class ZoomAdapter extends ZoomAction {

    /**
     * @param direction see {@link ZoomAdapter#ZOOM_DIRECTION_IN} and {@link ZoomAdapter#ZOOM_DIRECTION_OUT}
     */
    public ZoomAdapter(String actionName, ShrimpTool tool, String direction, String tooltip) {
		super(actionName, tool, direction, tooltip);
    }

    public boolean canStart() {
		Object targetObject = getTargetObject();
    	boolean canStart = (targetObject != null);
		return canStart;
    }

	/**
	 * Starts zooming out
	 */
    public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			if (ZOOM_DIRECTION_IN.equals(direction)) {
			    displayBean.startZoomingIn();
			} else if (ZOOM_DIRECTION_OUT.equals(direction)) {
			    displayBean.startZoomingOut();
			}
		} catch (BeanNotFoundException bnfe) {
		}
	}

	/**
	 * Stops zooming out
	 */
	public void stopAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.stopZooming();
		} catch (BeanNotFoundException bnfe) {
		}
    }

}
