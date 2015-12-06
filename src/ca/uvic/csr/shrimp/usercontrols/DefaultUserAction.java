/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;


/**
 * This is a default user action.  It defines all of the methods that each {@link UserAction} will need.
 *
 * @author Casey Best
 * @date Feb 6, 2001
 */
public abstract class DefaultUserAction extends DefaultShrimpAction implements UserAction {

	/** A list of events that make this action happen */
	protected Vector userEvents = null;
	protected boolean mustStartAndStop = false;
	private Vector defaultUserEvents = new Vector();
	private Vector listeners = new Vector();

	public DefaultUserAction(String actionName) {
		super(actionName);
	}

	public DefaultUserAction(String actionName, Icon icon) {
		super(actionName, icon);
	}

	/**
	 * Compare {@link UserAction} by the action name.
	 */
	public int compareTo(Object o) {
		int rv = 0;
		if (o != null) {
			String name;
			if (o instanceof UserAction) {
				name = ((UserAction)o).getActionName();
			} else {
				name = o.toString();
			}
			rv = getActionName().compareToIgnoreCase(name);
		}
		return rv;
	}

	/**
	 * Returns the default events that cause this action to happen
	 */
	public Vector getDefaultUserEvents(){
		return defaultUserEvents;
	}

	/**
	 * Sets the default events that cause this action to happen
	 */
	public void setDefaultUserEvents(Vector defaultUserEvents) {
		this.defaultUserEvents = defaultUserEvents;
	}

	protected void addDefaultUserEvent(UserEvent userEvent) {
		this.defaultUserEvents.add(userEvent);
	}

	/**
	 * Creates a {@link UserEvent} and adds it as a default event for this action.
	 * @return the created {@link DefaultUserEvent} for convenience
	 */
	public UserEvent addDefaultUserEvent(boolean mouseActivated, int keyOrButton, boolean controlRequired,
										boolean shiftRequired, boolean altRequired) {
		UserEvent userEvent = new DefaultUserEvent(this);
		userEvent.setCommand(mouseActivated, keyOrButton, controlRequired, shiftRequired, altRequired);
		addDefaultUserEvent(userEvent);
		return userEvent;
	}

	/**
	 * Returns the events that cause this action to happen
	 */
	public Vector getUserEvents(){
		return (userEvents == null ? new Vector(defaultUserEvents) : userEvents);
	}

	/**
	 * Sets the events that cause this action to happen
	 */
	public void setUserEvents(Vector userEvents) {
		if (userEvents == defaultUserEvents) {
			System.out.println("Same!");
			this.userEvents = new Vector(userEvents);
		} else {
			this.userEvents = userEvents;
		}
	}

	/**
	 * Creates a {@link UserEvent} and adds it to this action.
	 * @return the created {@link DefaultUserEvent} for convenience
	 */
	public UserEvent addUserEvent(boolean mouseActivated, int keyOrButton, boolean controlRequired,
								  boolean shiftRequired, boolean altRequired) {
		UserEvent userEvent = new DefaultUserEvent(this);
		userEvent.setCommand(mouseActivated, keyOrButton, controlRequired, shiftRequired, altRequired);
		addUserEvent(userEvent);
		return userEvent;
	}

	public void addUserEvent(UserEvent userEvent) {
		if (userEvents == null) {
			userEvents = new Vector(defaultUserEvents);
			userEvents.add(userEvent);
		} else if (!userEvents.contains(userEvent)) {
			userEvents.add(userEvent);
		}
	}

	public void removeUserEvent(UserEvent userEvent) {
		if ((userEvents != null) && userEvents.contains(userEvent)) {
			userEvents.remove(userEvent);
		}
	}

	/**
	 * Clears list of events that cause this action to happen.
	 */
	public void clearUserEvents() {
		if (userEvents != null) {
			userEvents.clear();
		}
	}

	/**
	 * Returns whether or not this action requires the user to start and stop it.
	 */
	public boolean mustStartAndStop() {
		return mustStartAndStop;
	}

	/**
	 * Sets whether or not this action requires the user to start and stop it.
	 */
	public void setMustStartAndStop(boolean mustStartAndStop) {
		this.mustStartAndStop = mustStartAndStop;
	}

	/**
	 * Fires the action starting event, then calls {@link UserAction#startAction()}, then
	 * fires the action finished event.
	 */
	public void actionPerformed(ActionEvent e) {
		fireActionEvent(true);
		// check if the action is enabled - the action starting listeners might have disabled it?
		if (isEnabled()) {
			startAction();
		}
		fireActionEvent(false);
	}


	/** Notifies listeners that the action is about to start */
	protected void fireActionEvent(boolean starting) {
		if (listeners.size() > 0) {
			Vector clones = (Vector) listeners.clone();
			for (Iterator iter = clones.iterator(); iter.hasNext(); ) {
				ShrimpActionListener listener = (ShrimpActionListener) iter.next();
				if (starting) {
					listener.actionStarting(this);
				} else {
					listener.actionFinished(this);
				}
			}
		}
	}

	public void addActionListener(ShrimpActionListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeActionListener(ShrimpActionListener listener) {
		listeners.remove(listener);
	}

	// for cloning
	private void setActionListeners(Vector listeners) {
		this.listeners = listeners;
	}

	/**
	 * @return true
	 */
	public boolean canStart() {
		return true;
	}

    /**
     * Stops action, as a default do nothing.
     */
    public void stopAction() {
        // do nothing by default
    }


	public String toString() {
		return getActionName();
	}

	/**
	 * Returns a clone of this action
	 */
	public final Object clone() {
		UserAction userAction = cloneAction();
		userAction.setToolTip(getToolTip());
		cloneUserEvents(userAction);
		return userAction;
	}

	private void cloneUserEvents(UserAction userAction) {
		Vector userEventsClone = new Vector();
		for (Iterator iterator = getUserEvents().iterator(); iterator.hasNext();) {
			UserEvent userEvent = (UserEvent) iterator.next();
			UserEvent userEventClone = (UserEvent) userEvent.clone();
			userEventsClone.add(userEventClone);
		}
		userAction.setUserEvents(userEventsClone);
		userAction.setDefaultUserEvents(getDefaultUserEvents());
		userAction.setMustStartAndStop(mustStartAndStop());
	}

	protected DefaultUserAction cloneAction() {
		DefaultUserAction userAction = new DefaultUserAction(getActionName(), getIcon()) {
			public void startAction() {
				startAction();
			}
			public void stopAction() {
				stopAction();
			}
		};
		userAction.setActionListeners(this.listeners);
		return userAction;
	}

	public void dispose() {
		this.listeners = new Vector(1);
		if (userEvents != null) {
			this.userEvents = null;
			this.defaultUserEvents = new Vector();
		}
	}

	/**
	 * Converts a user action into a key to be used for properties.
	 */
	public static String userActionToPropertiesKeyString(UserAction userAction) {
		return "UserAction!" + userAction.getActionName();
	}



}