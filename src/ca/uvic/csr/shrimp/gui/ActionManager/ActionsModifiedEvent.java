/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.util.Vector;

/**
 * Fired from the action manager when multiple actions are modified.
 * 
 * @author Rob Lintern
 */
public class ActionsModifiedEvent {

	private Vector actionModifiedEvents;
	
	/**
	 * Constructor for ActionsModifiedEvent.
	 */
	public ActionsModifiedEvent(Vector actionModifiedEvents) {
		this.actionModifiedEvents = actionModifiedEvents;
	}
	
	/** Returns a vector of ActionModifiedEvents */
	public Vector getActionModifiedEvents () {
		return actionModifiedEvents;
	}

}
