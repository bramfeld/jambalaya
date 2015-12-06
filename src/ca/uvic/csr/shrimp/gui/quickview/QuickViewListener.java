/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.ArrayList;
import java.util.Collection;


/**
 *
 *
 * @author Chris Callendar
 * @date 1-May-07
 */
public interface QuickViewListener {

	/**
	 * Notifies listeners that the quick views have changed.
	 * @param evt
	 */
	void quickViewsChanged(QuickViewEvent evt);

	/**
	 * Stores the added actions, removed actions, and changed actions.
	 * The changed actions are only the ones whose name, icon, or display value changes.
	 *
	 * @author Chris Callendar
	 * @date 4-May-07
	 */
	public class QuickViewEvent {

		private QuickViewManager manager;
		private Collection changedActions;
		private Collection addedActions;
		private Collection removedActions;

		public QuickViewEvent(QuickViewManager manager, Collection addedActions,
				Collection removedActions, Collection changedActions) {
			this.manager = manager;
			this.addedActions = new ArrayList(addedActions);
			this.removedActions = new ArrayList(removedActions);
			this.changedActions = new ArrayList(changedActions);
		}

		public QuickViewManager getManager() {
			return manager;
		}

		public boolean hasActions() {
			return hasChangedActions() || hasAddedActions() || hasRemovedActions();
		}

		/**
		 * @return The changed {@link QuickViewAction}s
		 */
		public Collection getChangedActions() {
			return changedActions;
		}

		public boolean hasChangedActions() {
			return !changedActions.isEmpty();
		}

		/**
		 * @return The added {@link QuickViewAction}s
		 */
		public Collection getAddedActions() {
			return addedActions;
		}

		public boolean hasAddedActions() {
			return !addedActions.isEmpty();
		}
		/**
		 * @return the removed {@link QuickViewAction}s
		 */
		public Collection getRemovedActions() {
			return removedActions;
		}

		public boolean hasRemovedActions() {
			return !removedActions.isEmpty();
		}

	}

}
