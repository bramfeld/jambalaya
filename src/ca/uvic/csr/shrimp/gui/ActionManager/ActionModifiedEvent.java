/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import javax.swing.Action;

/**
 * Thrown from the action manager when an action is modified.
 * @author Rob Lintern
 */
public class ActionModifiedEvent {
	private Action oldAction;
	private Action newAction;
	
	public ActionModifiedEvent (Action oldAction, Action newAction) {
		this.oldAction = oldAction;
		this.newAction = newAction;
	}
	
	/** Returns the action before modification. */
	public Action getOldAction () {
		return oldAction;
	}
	
	/** Returns the action after modification. */
	public Action getNewAction () {
		return newAction;
	}

}
