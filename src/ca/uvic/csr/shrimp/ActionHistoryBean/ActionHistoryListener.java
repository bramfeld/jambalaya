/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ActionHistoryBean;

/**
 * Listens for requests to redo/undo an action.
 *
 * @author Casey Best
 * @date August 1, 2000
 */
public interface ActionHistoryListener {

	/**
	 * Tells the listeners that the ActionHistory has changed.
	 * @param actionHistoryEvent Carries any extra info required to undo/redo the action
	 */
	public void actionHistoryChanged (ActionHistoryEvent actionHistoryEvent);
	
}