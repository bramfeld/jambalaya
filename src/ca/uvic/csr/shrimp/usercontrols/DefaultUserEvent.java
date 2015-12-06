/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The default implementation of {@link UserEvent}.
 * Defines the keyboard or mouse shortcut command for the {@link UserAction}.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class DefaultUserEvent implements UserEvent {
	
	private static final String COMMA = ",";
	private static final String SEP = "!";
	
	protected UserAction userAction;
	protected int keyOrButton;
	protected boolean isMouseActivated;
	protected boolean ctrlRequired;
	protected boolean altRequired;
	protected boolean shiftRequired;

	public DefaultUserEvent(UserAction userAction) {
		this.userAction = userAction;
	}
	
	/**
	 * Sets a new set of commands for this action.
	 * @param mouseActivated whether or not the mouse activates the command
	 * @param keyOrButton the key or mouse button that activates the command
	 * @param controlRequired is Ctrl pressed to activate the command
	 * @param shiftRequired is Shift pressed to activate the command
	 * @param altRequired is Alt pressed to activate the command
	 */
	public void setCommand(boolean mouseActivated, int keyOrButton, boolean controlRequired, 
						   boolean shiftRequired, boolean altRequired) {
		setMouseActivated(mouseActivated);
		setKeyOrButton(keyOrButton);
		setControlRequired(controlRequired);
		setShiftRequired(shiftRequired);
		setAltRequired(altRequired);
	}

	/**
	 * Returns the choice of keyboard or mouse to activate the action
	 */
	public boolean isMouseActivated() {
		return isMouseActivated;
	}
	
	/**
	 * Sets the choice of keyboard or mouse to activate the action
	 * @param activated True if the mouse will activate this command
	 */
	public void setMouseActivated(boolean activated) {
		isMouseActivated = activated;
	}

	/**
	 * @return true if this event is a mouse wheel event
	 */
	public boolean isMouseWheelEvent() {
		return (getKeyOrButton() == MOUSE_WHEEL_DOWN) || (getKeyOrButton() == MOUSE_WHEEL_UP);
	}
	
	/**
	 * Returns the button or key to activate the action
	 * See the KeyEvent interfaces or above for these constants.
	 */
	public int getKeyOrButton() {
		return keyOrButton;
	}
	
	/**
	 * Sets the button or key to activate the action
	 * @param keyOrButton The key or button to activate this command
	 */
	public void setKeyOrButton(int keyOrButton) {
		this.keyOrButton = keyOrButton;
	}

	/**
	 * Returns true if this action needs the Ctrl key down
	 */
	public boolean isControlRequired() {
		return ctrlRequired;
	}
	
	/**
	 * Sets whether or not the Ctrl key is required
	 */
	public void setControlRequired(boolean required) {
		ctrlRequired = required;
	}

	/**
	 * Returns true if this action needs the Shift key down
	 */
	public boolean isShiftRequired() {
		return shiftRequired;
	}
	
	/**
	 * Sets whether or not the Shift key is required
	 */
	public void setShiftRequired(boolean required) {
		shiftRequired = required;
	}

	/**
	 * Returns true if this action needs the Alt key down
	 */
	public boolean isAltRequired() {
		return altRequired;
	}
	
	/**
	 * Sets whether or not the Alt key is required
	 */
	public void setAltRequired(boolean required) {
		altRequired = required;
	}

	/** 
	 * Returns the action associated with this event.
	 */
	public UserAction getAction() {
		return userAction;
	}
	
	/** 
	 * Sets the action associated with this event.
	 */
	public void setAction(UserAction userAction) {
		this.userAction = userAction;
	}
	
	public String toString() {
		String s = "";
		if (isControlRequired() && getKeyOrButton() != KeyEvent.VK_CONTROL) 
			s += "Ctrl+";
		if (isAltRequired() && getKeyOrButton() != KeyEvent.VK_ALT) 
			s += "Alt+";
		if (isShiftRequired() && getKeyOrButton() != KeyEvent.VK_SHIFT) 
			s += "Shift+";
		if (isMouseActivated())
			s += MOUSE_INPUT_NAME[getKeyOrButton()];
		else
			s += KeyEvent.getKeyText(getKeyOrButton());	
		return s;		
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj instanceof DefaultUserEvent) {
			DefaultUserEvent event = (DefaultUserEvent) obj;
			result = event.isMouseActivated() == isMouseActivated() &&
					event.getKeyOrButton() == getKeyOrButton() &&
					event.isControlRequired() == isControlRequired() &&
					event.isAltRequired() == isAltRequired() &&
					event.isShiftRequired() == isShiftRequired();
		}			
		return result;
	}
	
	/** 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return userEventToPropertiesString(this).hashCode();
	}

	/**
	 * Returns a clone of this event
	 */
	public Object clone() {
		UserEvent userEvent = new DefaultUserEvent(userAction);
		userEvent.setCommand(isMouseActivated, keyOrButton, ctrlRequired, shiftRequired, altRequired);
		return userEvent;
	}

	/**
	 * Converts a user event into a string to be used for properties
	 */
	private static String userEventToPropertiesString(UserEvent userEvent) {
		StringBuffer sb = new StringBuffer();
		sb.append(userEvent.getKeyOrButton());
		sb.append(SEP); 
		sb.append(userEvent.isMouseActivated());
		sb.append(SEP);
		sb.append(userEvent.isControlRequired());
		sb.append(SEP);
		sb.append(userEvent.isAltRequired());
		sb.append(SEP);
		sb.append(userEvent.isShiftRequired());
		return sb.toString();
	}
	
	/**
	 * Converts a properties string into a user Event
	 */
	private static UserEvent propertiesStringToUserEvent(String propertiesString, UserAction userAction) {
		UserEvent userEvent = new DefaultUserEvent(userAction);
		StringTokenizer st = new StringTokenizer(propertiesString, SEP);
		userEvent.setKeyOrButton(Integer.valueOf(st.nextToken()).intValue());
		userEvent.setMouseActivated(Boolean.valueOf(st.nextToken()).booleanValue());
		userEvent.setControlRequired(Boolean.valueOf(st.nextToken()).booleanValue());
		userEvent.setAltRequired(Boolean.valueOf(st.nextToken()).booleanValue());
		userEvent.setShiftRequired(Boolean.valueOf(st.nextToken()).booleanValue());
		return userEvent;
	}
	
	/**
	 * Converts a vector of user events into a string to be used for properties
	 */
	public static String userEventsToPropertiesString(Vector userEvents) {
		StringBuffer sb = new StringBuffer();
		for (Iterator iterator = userEvents.iterator(); iterator.hasNext(); ) {
			UserEvent userEvent = (UserEvent) iterator.next();
			if (sb.length() > 0) {
				sb.append(COMMA);
			}
			sb.append(userEventToPropertiesString(userEvent));
		}
		return sb.toString();
	}
	
	/**
	 * Converts a properties string into a vector of user events.
	 */
	public static Vector propertiesStringToUserEvents(String propertiesString, UserAction userAction) {
		Vector userEvents = new Vector();
		StringTokenizer st = new StringTokenizer(propertiesString, COMMA);
		while (st.hasMoreTokens()) {
			userEvents.add(propertiesStringToUserEvent(st.nextToken(), userAction));
		}
		return userEvents;
	}

}
