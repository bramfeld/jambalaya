/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

/**
 * Interface for user events.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public interface UserEvent extends Cloneable {
	
	public static final int LEFT_MOUSE_BUTTON = 0;
	public static final int MIDDLE_MOUSE_BUTTON = 1;
	public static final int DOUBLE_CLICK__LEFT_MOUSE_BUTTON = 2;
	public static final int DOUBLE_CLICK__MIDDLE_MOUSE_BUTTON = 3;
	public static final int RIGHT_MOUSE_BUTTON = 4;
	public static final int DOUBLE_CLICK__RIGHT_MOUSE_BUTTON = 5;
	public static final int MOUSE_WHEEL_UP = 6;		// @tag Shrimp.MouseWheel
	public static final int MOUSE_WHEEL_DOWN = 7;

	public static final String[] MOUSE_INPUT_NAME = {
		"Left Mouse Button",
		"Middle Mouse Button",
		"Double Click - Left Mouse Button",
		"Double Click - Middle Mouse Button",
		"Right Mouse Button",
		"Double Click - Right Mouse Button",
		"Mouse Wheel Scroll Up",				
		"Mouse Wheel Scroll Down"
	};

	/**
	 * Sets a new set of commands for this action.
	 * @param mouseActivated whether or not the mouse activates the command
	 * @param keyOrButton the key or mouse button that activates the command
	 * @param controlRequired is Ctrl pressed to activate the command
	 * @param shiftRequired is Shift pressed to activate the command
	 * @param altRequired is Alt pressed to activate the command
	 */
	public void setCommand(boolean mouseActivated, int keyOrButton, boolean controlRequired, 
						   boolean shiftRequired, boolean altRequired);

	/**
	 * Returns the choice of keyboard or mouse to activate the action
	 */
	public boolean isMouseActivated();
	
	/**
	 * Sets the choice of keyboard or mouse to activate the action
	 * @param activated True if the mouse will activate this command
	 */
	public void setMouseActivated(boolean activated);
	
	/**
	 * @return true if the event is a mouse wheel event
	 */
	public boolean isMouseWheelEvent();

	/**
	 * Returns the button or key to activate the action
	 * See the KeyEvent interfaces or above for these constants.
	 */
	public int getKeyOrButton();
	
	/**
	 * Sets the button or key to activate the action
	 * @param keyOrButton The key or button to activate this command
	 */
	public void setKeyOrButton(int keyOrButton);
		
	/**
	 * Returns true if this action needs the Ctrl key down
	 */
	public boolean isControlRequired();
	
	/**
	 * Sets whether or not the Ctrl key is required
	 */
	public void setControlRequired(boolean required);

	/**
	 * Returns true if this action needs the Shift key down
	 */
	public boolean isShiftRequired();
	
	/**
	 * Sets whether or not the Shift key is required
	 */
	public void setShiftRequired(boolean required);

	/**
	 * Returns true if this action needs the Alt key down
	 */
	public boolean isAltRequired();
	
	/**
	 * Sets whether or not the Alt key is required
	 */
	public void setAltRequired(boolean required);
	
	/** 
	 * Returns the action associated with this event.
	 */
	public UserAction getAction();
	
	/** 
	 * Sets the action associated with this event.
	 */
	public void setAction(UserAction userAction);
	
	/**
	 * Returns a clone of this event
	 */
	public Object clone();

}
