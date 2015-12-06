/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionEvent;
import java.util.Vector;

import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;

/**
 * This interface defines an user activated action in shrimp.
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 * @date Jan 29, 2001
 */
public interface UserAction extends ShrimpAction, Cloneable, Comparable {

	/**
	 * Adds a listener which will be notified before the action starts and after the action finishes.
	 * @param listener
	 */
	public void addActionListener(ShrimpActionListener listener);

	/**
	 * Removes a listener which will be notified before the action starts and after the action finishes.
	 * @param listener
	 */
	public void removeActionListener(ShrimpActionListener listener);

	/**
	 * Checks if the action should start.
	 * Most actions will return true by default, some might have preconditions which must be
	 * true before the action starts (e.g. selected node required).
	 * The way this is used is not consistent - some times it will be called
	 * before {@link UserAction#startAction()} is called, but not always.
	 * It is really just a convenience method which some actions can override.
	 * @return true if the action should start
	 */
	public boolean canStart();

	/**
	 * Fires the actionStarting event, then calls {@link UserAction#startAction()}
	 * and then fires the action finished event.
	 * @see ShrimpActionListener
	 * @see UserAction#addActionListener(ShrimpActionListener)
	 * @see UserAction#startAction()
	 */
	public void actionPerformed(ActionEvent evt);

	/**
	 * Starts the action.
	 */
	public void startAction();

	/**
	 * Stops the action
	 * Note: For actions that do not have start and stops, this method won't do anything.
	 * Ex. Opening a node is a single action, and doesn't require stopping
	 */
	public void stopAction();

	/**
	 * Sets whether or not this action requires the user to start and stop it
	 */
	public void setMustStartAndStop(boolean mustStartAndStop);

	/**
	 * Returns whether or not this action requires the user to start and stop it
	 */
	public boolean mustStartAndStop();

	/**
	 * Returns the events that cause this action to happen
	 */
	public Vector getUserEvents();

	/**
	 * Sets the events that cause this action to happen
	 */
	public void setUserEvents(Vector userEvents);

	/**
	 * Returns the default events that cause this action to happen
	 */
	public Vector getDefaultUserEvents();

	/**
	 * Sets the default events that cause this action to happen
	 */
	public void setDefaultUserEvents(Vector defaultUserEvents);

	/**
	 * Adds an event to the list of events that cause this action to happen.
	 */
	public void addUserEvent(UserEvent userEvent);

	/**
	 * Removes an event from the list of events that cause this action to happen.
	 */
	public void removeUserEvent(UserEvent userEvent);

	/**
	 * Clears list of events that cause this action to happen
	 */
	public void clearUserEvents();

	/**
	 * Returns a clone of this action.
	 */
	public Object clone();

	public String toString();

}