/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.util.Vector;

/**
 * @author Rob Lintern
 */
public class ActionsRemovedEvent {

	private Vector removedActions;

	public ActionsRemovedEvent(Vector removedActions) {
		this.removedActions = removedActions;
	}

	public Vector getRemovedActions() {
		return removedActions;
	}

}
