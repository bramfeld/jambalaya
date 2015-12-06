/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ActionHistoryBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/** 
 * ActionHistoryBean is responsible for tracking the history of a 
 * DisplayBean.  All undo/redo functionality is provided by this
 * bean and the Action class. 
 * 
 * @author Polly Allen
 * @date June 28, 2000
 */
public class ActionHistoryBean implements Serializable {

	private Stack historyStack;
	private Stack futureStack;
	private ArrayList listeners = new ArrayList();

	/** 
	 * Constructor
	 */
	public ActionHistoryBean() {
		clearHistory();
		clearFuture();
	}

	/** 
	 * Adds an Action object to the history stack
	 *
	 * @param a The Action to be added
	 */
	public void addAction(HistoryAction a) {
		historyStack.push(a);
		clearFuture();
		fireActionHistoryChangedEvent();
	}

	/** 
	 * Undoes the last undo-able action and adds the action to the
	 * future stack, so that it can be redone.
	 */
	public void undoAction() {
		if (undoIsPossible()) {
			HistoryAction a = (HistoryAction) historyStack.pop();
			futureStack.push(a);
			a.undo();
			fireActionHistoryChangedEvent();
		}
	}

	/** 
	 * Redoes the last undo-able action and adds the action to the
	 * history stack, so that it can be undone again.
	 */
	public void redoAction() {
		if (redoIsPossible()) {
			HistoryAction a = (HistoryAction) futureStack.pop();
			historyStack.push(a);
			a.redo();
			fireActionHistoryChangedEvent();
		}
	}

	/** 
	 * Checks to see if there are any actions in the history stack
	 */
	public boolean undoIsPossible() {
		return (!historyStack.empty());
	}

	/** 
	 * Checks to see if there are any actions in the future stack
	 */
	public boolean redoIsPossible() {
		return (!futureStack.empty());
	}

	/** 
	 * clearHistory resets the history stack.
	 */
	public void clearHistory() {
		historyStack = new Stack();
	}

	/** 
	 * clearFuture resets the furture stack.
	 */
	public void clearFuture() {
		futureStack = new Stack();
	}

	public void addActionHistoryListener(ActionHistoryListener actionHistoryListener) {
		listeners.add(actionHistoryListener);
	}

	public void removeActionHistoryListener(ActionHistoryListener actionHistoryListener) {
		listeners.remove(actionHistoryListener);
	}

	public void fireActionHistoryChangedEvent() {
		ArrayList list = (ArrayList) listeners.clone();
		ActionHistoryEvent event = new ActionHistoryEvent(this);
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			ActionHistoryListener listener = (ActionHistoryListener) iter.next();
			listener.actionHistoryChanged(event);
		}
	}
}
