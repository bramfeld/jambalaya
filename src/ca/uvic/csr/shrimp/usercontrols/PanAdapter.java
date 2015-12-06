/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Handles panning in all 4 directions.
 *
 * @author Rob Lintern
 */
public abstract class PanAdapter extends DefaultToolAction {

    public static final String PAN_DIRECTION_NORTH = "north";
    public static final String PAN_DIRECTION_EAST = "east";
    public static final String PAN_DIRECTION_SOUTH = "south";
    public static final String PAN_DIRECTION_WEST = "west";

    private String direction;

    public PanAdapter(String actionName, ShrimpTool tool, String direction) {
    	super(actionName, tool);
		this.direction = direction;
		mustStartAndStop = true;
    }

    public boolean canStart() {
    	boolean canStart = (getTargetObject() != null);
		return canStart;
    }

	/**
	 * Starts panning
	 */
    public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.startPanning(direction);
		} catch (BeanNotFoundException bnfe) {
		}
	}

	/**
	 * Stops panning
	 */
	public void stopAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.stopPanning();
		} catch (BeanNotFoundException bnfe) {
		}
    }

}
