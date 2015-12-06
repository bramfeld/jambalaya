/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewAction;


/**
 * This listener is used to signal that a {@link QuickViewAction} is about to start.
 *
 * @author Chris Callendar
 */
public interface ShrimpActionListener {

	/**
	 * Signals that the action is about to start.
	 */
	public void actionStarting(ShrimpAction action);

	/**
	 * Signals that the action has run.
	 * @param action
	 */
	public void actionFinished(ShrimpAction action);

}
