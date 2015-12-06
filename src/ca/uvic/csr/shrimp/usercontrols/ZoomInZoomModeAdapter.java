/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/** 
 * This {@link UserAction} adapter will handle zooming in while in "zoom" mode.
 *
 * @author Casey Best, Chris Callendar
 */
public class ZoomInZoomModeAdapter extends ZoomAdapter {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_ZOOM_IN_ZOOM_MODE;
	public static final String TOOLTIP = "Zooms in a fixed amount. The zooming is relative to the mouse position."; 
	
    public ZoomInZoomModeAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool, ZOOM_DIRECTION_IN, TOOLTIP);
    }
    		
}
