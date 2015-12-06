/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/** 
 * This UserAction adapter will handle panning east with the right arrow key
 *
 * @author David Perrin, Chris Callendar
 */
public class PanEastAdapter extends PanAdapter {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_PAN_EAST;
	public static final String TOOLTIP = "Moves the viewing area to the right.";
	
    public PanEastAdapter(ShrimpTool tool) {
    	super(ACTION_NAME, tool, PanAdapter.PAN_DIRECTION_EAST);
    	setToolTip(TOOLTIP);
    }

}