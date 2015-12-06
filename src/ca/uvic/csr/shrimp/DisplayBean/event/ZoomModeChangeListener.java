/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */ 
package ca.uvic.csr.shrimp.DisplayBean.event;
 

/**
 * Listens for changes to the zoom mode. Two sample zoom 
 * modes are magnify and fisheye.
 *
 * @author Casey Best
 * date: August 1, 2000
 */
public interface ZoomModeChangeListener {

	/**
	 * Handles a request to change the mode in which we zoom.
	 * Ex. Magnify, zoom, or Fisheye
	 *
	 * @param modeChangeEvent Contains important info such as the new mode
	 */
	public void changeZoomMode (ModeChangeEvent modeChangeEvent);
	
}