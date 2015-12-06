/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. 
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
	
/**
 * Wrapper for any mode of zooming in.
 * 
 * @author Chris Callendar
 * @date 1-Mar-07
 */
public class ZoomInAnyModeAdapter extends ZoomAnyModeAdapter {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_ZOOM_IN_ANY_MODE;
	public static final String TOOLTIP = "Zoom in using the current zooming mode (Magnify, Zoom, or Fisheye).  " + 
		"To change the zoom mode use the navigation drop down menu in the bottom right corner of the main view.";
	
	public ZoomInAnyModeAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool, ZOOM_DIRECTION_IN, TOOLTIP);
	}
	
}