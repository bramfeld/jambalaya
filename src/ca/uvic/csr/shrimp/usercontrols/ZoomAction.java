/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/**
 * Base class for all zooming (zoom, fisheye, magnify) actions.
 * 
 * @see ZoomAdapter
 * @see FisheyeAdapter
 * @see MagnifyInAdapter
 * @see MagnifyOutAdapter
 * @tag Shrimp.ZoomAction
 * @author Chris Callendar
 * @date 1-Mar-07
 */
public abstract class ZoomAction extends DefaultToolAction {

    protected static final String ZOOM_DIRECTION_IN = "in";
    protected static final String ZOOM_DIRECTION_OUT = "out";

	protected String direction;
	
	public ZoomAction(String actionName, ShrimpTool tool, String direction, String tooltip) {
		this(actionName, null, tool, direction, tooltip);
	}
	
	public ZoomAction(String actionName, Icon icon, ShrimpTool tool, String direction, String tooltip) {
		super(actionName, icon, tool);
		setToolTip(tooltip);
		this.direction = direction;
		this.mustStartAndStop = true;
	}

	public boolean canStart() {
		return super.canStart() && 	(ZOOM_DIRECTION_IN.equals(direction) ||	ZOOM_DIRECTION_OUT.equals(direction));
	}
	
}
