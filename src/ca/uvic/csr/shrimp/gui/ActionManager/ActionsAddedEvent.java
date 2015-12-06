/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.util.Vector;

/**
 * @author Rob Lintern
 */
public class ActionsAddedEvent {
	private Vector addedActions;
	
	public ActionsAddedEvent (Vector addedActions) {
		this.addedActions = addedActions;
	}
	
	public Vector getAddedActions () {
		return addedActions;
	}
}
