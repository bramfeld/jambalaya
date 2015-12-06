/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/** 
 * This UserAction adapter will handle panning west with the left arrow key
 *
 * @author David Perrin, Chris Callendar
 */
public class PanWestAdapter extends PanAdapter  {
	
	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_PAN_WEST;
	public static final String TOOLTIP = "Moves the viewing area to the left.";
	
    public PanWestAdapter (ShrimpTool tool) {
    	super(ACTION_NAME, tool, PanAdapter.PAN_DIRECTION_WEST);
    	setToolTip(TOOLTIP);
    }
    
}