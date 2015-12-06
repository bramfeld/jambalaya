/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.DisplayBean.event;
 
/**
 * Carries any needed details for changing a mode of something.
 * Examples of modes that can be changed include the zoom, panel, or 
 * layout modes.  Since panel and layout require a target,
 * the target is passed in to the constructor.  For zoom mode 
 * changes, null is passed in as the target.
 *
 * @author Casey Best
 * date: August 1, 2000
 */

public class ModeChangeEvent {
	private String mode;
	Object display;
	Object target;
	
	/**
	 * Creates a new event with the given mode and the given target.
	 * Pass null to any parameter that isn't needed for this type of
	 * mode change.  For example, the zoom mode doesn't require a target,
	 * so null is passed into the target parameter.
	 *
	 * @param newMode The mode you wish to change to.
	 * @param display The display the change will affect.
	 * @param target The target in which the mode is being changed on.
	 */
	public ModeChangeEvent (String newMode, Object display, Object target) {
		mode = newMode;
		this.display = display;
		this.target = target;
	}
	
	/**
	 * Returns the new mode
	 */
	public String getNewMode() {
		return mode;
	}
	
	/**
	 * Returns the display the mode will be changed on.  Returns null
	 * if the mode being changes doesn't require a display (ex. zoom).
	 */
	public Object getDisplay() {
		return display;
	}
	
	/**
	 * Returns the target of the mode change.  Returns null if the mode
	 * being changed doesn't require a target (ex. zoom).
	 */
	public Object getTarget() {
		return target;
	}
}