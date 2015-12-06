/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */ 
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/** 
 * This UserAction adapter will handle zooming out while in "zoom" mode.
 *
 * @author Casey Best, Chris Callendar
 */
public class ZoomOutZoomModeAdapter extends ZoomAdapter {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_ZOOM_OUT_ZOOM_MODE;
	public static final String TOOLTIP = "Zooms out a fixed amount. The zooming is relative to the mouse position.";

    public ZoomOutZoomModeAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool, ZOOM_DIRECTION_OUT, TOOLTIP);
    }
    	
}
