/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAndKeyAdapter;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.UserEvent;

/**
 * This adapter takes mouse and keyboard events, and maps them to the
 * appropriate user action.
 *
 * @author Casey Best, Chris Callendar
 * @date Jan 30, 2001
 */
public class ShrimpInputAdapter extends ShrimpMouseAndKeyAdapter {

	private Vector mousePressedEvents;
	private Vector mouseReleasedEvents;
	private Vector mouseWheelEvents;
	private Vector keyPressedEvents;
	private Vector keyReleasedEvents;

	private boolean isCtrlPressed;
	private boolean isShiftPressed;
	private boolean isAltPressed;

	private SelectorBean selectorBean;
	private ViewTool tool;

	public ShrimpInputAdapter(ViewTool tool) {
		this.tool = tool;

		mousePressedEvents = new Vector();
		mouseReleasedEvents = new Vector();
		mouseWheelEvents = new Vector();
		keyPressedEvents = new Vector();
		keyReleasedEvents = new Vector();

		isCtrlPressed = false;
		isShiftPressed = false;
		isAltPressed = false;
	}

	/**
	 * Starts the {@link UserAction} and updates the action label.
	 */
	private void startAction(final UserAction action) {
		if (action.canStart()) {
			//System.out.println("Starting: " + action.getActionName());
			// show the action name
			String txt = "Action: " + action.getActionName() + " ";
			tool.setOutputText(txt);

			// run this later to let the above output be painted to the GUI first
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// call actionPerformed() instead of startAction() to fire the events
					action.actionPerformed(new ActionEvent(action, 0, action.getActionName()));

					// clear the output after 1 second
					tool.clearOutputText(1000);
				}
			});
		}
	}

	/**
	 * Stops the {@link UserAction} and clears the action label.
	 */
	private void stopAction(UserAction action) {
		//System.out.println("Stopping: " + action.getActionName());
		action.stopAction();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mousePressed(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mousePressed(ShrimpMouseEvent e) {
		if (!handleAnyMouseEvent(e)) {
			return;
		}
		if (e.isLeftMouseButton()) {
			processMousePress(UserEvent.LEFT_MOUSE_BUTTON,
					UserEvent.DOUBLE_CLICK__LEFT_MOUSE_BUTTON,
					e.getClickCount());
		}
		else if (e.isMiddleMouseButton()) {
			processMousePress(UserEvent.MIDDLE_MOUSE_BUTTON,
					UserEvent.DOUBLE_CLICK__MIDDLE_MOUSE_BUTTON,
					e.getClickCount());
		} else if (e.isRightMouseButton()) {
			processMousePress(UserEvent.RIGHT_MOUSE_BUTTON,
					UserEvent.DOUBLE_CLICK__RIGHT_MOUSE_BUTTON,
					e.getClickCount());
		}
	}


	/**
	 * Process mouse press events.  Starts the actions.
	 * @param singleClickButton
	 * @param doubleClickButton
	 * @param clickCount
	 */
	private void processMousePress(int singleClickButton, int doubleClickButton,
			int clickCount) {
		for (int i = 0; i < mousePressedEvents.size(); i++) {
			UserEvent userEvent = (UserEvent) mousePressedEvents.elementAt(i);
			if ((isShiftPressed == userEvent.isShiftRequired()) &&
				(isCtrlPressed == userEvent.isControlRequired()) &&
				(isAltPressed == userEvent.isAltRequired())) {
				if (userEvent.getKeyOrButton() == singleClickButton && clickCount == 1) {
					startAction(userEvent.getAction());
				} else if (userEvent.getKeyOrButton() == doubleClickButton && clickCount >= 2) {
					startAction(userEvent.getAction());
				}
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mouseReleased(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseReleased(ShrimpMouseEvent e) {
		if (!handleAnyMouseEvent(e)) {
			return;
		}
		if (e.isLeftMouseButton()) {
			processMouseRelease(UserEvent.LEFT_MOUSE_BUTTON);
		} else if (e.isMiddleMouseButton()) {
			processMouseRelease(UserEvent.MIDDLE_MOUSE_BUTTON);
		} else if (e.isRightMouseButton()) {
			processMouseRelease(UserEvent.RIGHT_MOUSE_BUTTON);
		}
	}

	/**
	 * Process mouse release events.  Stops the actions.
	 * @param singleClickButton
	 * @param doubleClickButton
	 * @param clickCount
	 */
	private void processMouseRelease(int singleClickButton) {
		for (int i = 0; i < mouseReleasedEvents.size(); i++) {
			UserEvent userEvent = (UserEvent) mouseReleasedEvents.elementAt(i);
			if ((isShiftPressed == userEvent.isShiftRequired()) &&
				(isCtrlPressed == userEvent.isControlRequired()) &&
				(isAltPressed == userEvent.isAltRequired())) {
				if (userEvent.getKeyOrButton() == singleClickButton) {
					stopAction(userEvent.getAction());
				}
			}
		}
	}

	public void mouseWheelMoved(ShrimpMouseEvent e) {
		// @tag Shrimp.MouseWheel
		if (handleAnyMouseEvent(e)) {
			for (Iterator iter = mouseWheelEvents.iterator(); iter.hasNext(); ) {
				UserEvent userEvent = (UserEvent) iter.next();
				if ((isShiftPressed == userEvent.isShiftRequired()) &&
					(isCtrlPressed == userEvent.isControlRequired()) &&
					(isAltPressed == userEvent.isAltRequired())) {
					if (e.isUpWheelRotation() && (userEvent.getKeyOrButton() == UserEvent.MOUSE_WHEEL_UP)) {
						if (userEvent.getAction().mustStartAndStop()) {
							startAction(userEvent.getAction());
							stopAction(userEvent.getAction());
						} else {
							startAction(userEvent.getAction());
						}
					} else 	if (e.isDownWheelRotation() && (userEvent.getKeyOrButton() == UserEvent.MOUSE_WHEEL_DOWN)) {
						if (userEvent.getAction().mustStartAndStop()) {
							startAction(userEvent.getAction());
							stopAction(userEvent.getAction());
						} else {
							startAction(userEvent.getAction());
						}
					}
				}
			}
		}
	}

	/** This method handles all of the mouse events first */
	public boolean handleAnyMouseEvent(ShrimpMouseEvent e) {
		if (tool == null) {
			return false;
		}
		//tool.clearOutputText();

		try {
			selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		  	return false;
		}

		// in case the system didn't throw all the events, this clears the modifiers for us
		if (e.getModifiers() == 0) {
			isCtrlPressed = false;
			isShiftPressed = false;
			isAltPressed = false;
		}

		// set the current target
		Object targetObj = e.getTarget();
		if (targetObj != null) {
			selectorBean.setSelected(SelectorBeanConstants.TARGET_OBJECT, targetObj);
		} else  {
			selectorBean.clearSelected(SelectorBeanConstants.TARGET_OBJECT);
		}

		// set the current mouse coordinates
		Vector coords = new Vector();
	    coords.addElement(new Double(e.getX()));
	    coords.addElement(new Double(e.getY()));
		selectorBean.setSelected(SelectorBeanConstants.MOUSE_COORDINATES, coords);

		return true;
	}

    /** Handles all the actions when a key is pressed */
    public void keyPressed(ShrimpKeyEvent e) {
    	int code = e.getKeyCode();
		// in case the system didn't throw all the events, this clears the modifiers for us
		if (e.getModifiers() == 0) {
			isCtrlPressed = false;
			isShiftPressed = false;
			isAltPressed = false;
		}
		if (code == KeyEvent.VK_CONTROL) {
			isCtrlPressed = true;
		} else if (code == KeyEvent.VK_SHIFT) {
			isShiftPressed = true;
		} else if (code == KeyEvent.VK_ALT) {
			isAltPressed = true;
		}

		//tool.clearOutputText();

    	for (int i = 0; i < keyPressedEvents.size(); i++) {
			UserEvent userEvent = (UserEvent) keyPressedEvents.elementAt(i);
    		if (userEvent.getKeyOrButton() == code) {
				if ((isShiftPressed == userEvent.isShiftRequired()) &&
					(isCtrlPressed == userEvent.isControlRequired()) &&
					(isAltPressed == userEvent.isAltRequired())) {
					startAction(userEvent.getAction());
				}
    		}
    	}

    }

    /** Handles all the actions when a key is released */
    public void keyReleased(ShrimpKeyEvent e) {
    	int code = e.getKeyCode();
		if (code == KeyEvent.VK_CONTROL) {
			isCtrlPressed = false;
		} else if (code == KeyEvent.VK_SHIFT) {
			isShiftPressed = false;
		} else if (code == KeyEvent.VK_ALT) {
			isAltPressed = false;
		}

    	for (int i = 0; i < keyReleasedEvents.size(); i++) {
			UserEvent userEvent = (UserEvent) keyReleasedEvents.elementAt(i);
    		if (userEvent.getKeyOrButton() == code) {
				if ((isShiftPressed == userEvent.isShiftRequired()) &&
					(isCtrlPressed == userEvent.isControlRequired()) &&
					(isAltPressed == userEvent.isAltRequired())) {
					stopAction(userEvent.getAction());
				}
	    	}
    	}
    }

	public void addUserAction(UserAction action) {
		Vector userEvents = action.getUserEvents();
		for (Iterator iterator = userEvents.iterator(); iterator.hasNext();) {
			UserEvent userEvent = (UserEvent) iterator.next();
			if (action.mustStartAndStop()) {
				if (userEvent.isMouseActivated()) {
					if (userEvent.isMouseWheelEvent()) {
						mouseWheelEvents.addElement(userEvent);
					} else {
						mousePressedEvents.addElement(userEvent);
						mouseReleasedEvents.addElement(userEvent);
					}
				} else {
					keyPressedEvents.addElement(userEvent);
					keyReleasedEvents.addElement(userEvent);
				}
			} else {
				if (userEvent.isMouseActivated()){
					if (userEvent.isMouseWheelEvent()) {
						mouseWheelEvents.addElement(userEvent);
					} else {
						mousePressedEvents.addElement(userEvent);
					}
				} else {
					keyPressedEvents.addElement(userEvent);
				}
			}
		}
	}

	public void removeUserAction(UserAction action) {
		Vector userEvents = action.getUserEvents();
		for (Iterator iterator = userEvents.iterator(); iterator.hasNext();) {
			UserEvent userEvent = (UserEvent) iterator.next();
			if (action.mustStartAndStop()) {
				if (userEvent.isMouseActivated()) {
					if (userEvent.isMouseWheelEvent()) {
						mouseWheelEvents.removeElement(userEvent);
					} else {
						mousePressedEvents.removeElement(userEvent);
						mouseReleasedEvents.removeElement(userEvent);
					}
				} else {
					keyPressedEvents.removeElement(userEvent);
					keyReleasedEvents.removeElement(userEvent);
				}
			} else {
				if (userEvent.isMouseActivated()) {
					if (userEvent.isMouseWheelEvent()) {
						mouseWheelEvents.removeElement(userEvent);
					} else {
						mousePressedEvents.removeElement(userEvent);
					}
				} else {
					keyPressedEvents.removeElement(userEvent);
				}
			}
		}
	}

	public void clearUserActions() {
		mousePressedEvents.clear();
		mouseReleasedEvents.clear();
		mouseWheelEvents.clear();
		keyPressedEvents.clear();
		keyReleasedEvents.clear();
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpKeyListener#resetKeys()
	 */
	public void resetKeys() {
		isCtrlPressed = false;
		isShiftPressed = false;
		isAltPressed = false;
	}

}