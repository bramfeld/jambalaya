/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.HashMap;
import java.util.Map;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter is the base class which handles zooming in or out while in any mode (magnify, zoom, or fisheye).
 * It can support more zoom types, simply call the {@link ZoomAnyModeAdapter#addZoomMode(String, ZoomAction)} method.
 *
 * @author Chris Callendar
 */
public abstract class ZoomAnyModeAdapter extends ZoomAction {

	private Map/*<String, Action>*/ zoomModes;

	/**
	 * Constructs this adapter.
	 */
    protected ZoomAnyModeAdapter(String actionName, ShrimpTool tool, String direction, String tooltip) {
        super(actionName, tool, direction, tooltip);
        this.zoomModes = new HashMap/*<String, Action>*/();
    }

    /**
     * Use this method to add zoom modes and their adapters.
     * @param name the name of the zoom mode (see {@link DisplayConstants})
     * @param zoomAdapter the adapter that performs the zooming
     */
    public void addZoomMode(String name, ZoomAction zoomAdapter) {
    	zoomModes.put(name, zoomAdapter);
    }

    public boolean canStart() {
    	boolean canStart = false;
    	String mode = getZoomMode();
    	if (zoomModes.containsKey(mode)) {
    		DefaultToolAction zoomAction = (DefaultToolAction) zoomModes.get(mode);
    		canStart = zoomAction.canStart();
    	} else {
    		System.err.println("No zoom mode found for '" + mode + "'");
    	}
    	return canStart && super.canStart();
    }

    public void startAction() {
    	String mode = getZoomMode();
    	if (zoomModes.containsKey(mode)) {
    		DefaultToolAction zoomAction = (DefaultToolAction) zoomModes.get(mode);
    		zoomAction.startAction();
    	} else {
    		System.err.println("No zoom mode found for '" + mode + "'");
    	}
    }

    public void stopAction() {
    	String mode = getZoomMode();
    	if (zoomModes.containsKey(mode)) {
    		DefaultToolAction zoomAction = (DefaultToolAction) zoomModes.get(mode);
    		zoomAction.stopAction();
    	}
    }

}