/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.util.Iterator;
import java.util.Vector;

/**
 * This event is created when filters have been added or removed to/from the FilterBean
 */
public class FilterChangedEvent {

	private Vector addedFilters;
	private Vector removedFilters;
	private Vector changedFilters;

	/**
	 * Creates a new Filter Changed Event with the added and removed
	 * artifacts and relationships
	 */
	public FilterChangedEvent(Vector addedFilters, Vector removedFilters, Vector changedFilters) {
		this.addedFilters = addedFilters;
		this.removedFilters = removedFilters;
		this.changedFilters = changedFilters;
	}

	/** Returns all added artifact filters */
	public Vector getAddedFilters() {
		return addedFilters;
	}

	public void addMoreAddedFilters(Vector filters) {
		addedFilters.addAll(filters);
	}

	/** Returns all removed artifact filters */
	public Vector getRemovedFilters() {
		return removedFilters;
	}

	public void addMoreRemovedFilters(Vector filters) {
		removedFilters.addAll(filters);
	}

	public Vector getChangedFilters() {
		return changedFilters;
	}

	public void addMoreChangedFilters(Vector filters) {
		changedFilters.addAll(filters);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (addedFilters.size() > 0) {
			buffer.append("Added filters: ");
			for (Iterator iter = addedFilters.iterator(); iter.hasNext(); ) {
				Filter filter = (Filter) iter.next();
				buffer.append(filter.getFilterType() + " -> " + filter.getTargetType());
				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append("\n");
		}
		if (removedFilters.size() > 0) {
			buffer.append("Removed filters: ");
			for (Iterator iter = removedFilters.iterator(); iter.hasNext(); ) {
				Filter filter = (Filter) iter.next();
				buffer.append(filter.getFilterType() + " -> " + filter.getTargetType());
				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append("\n");
		}
		if (changedFilters.size() > 0) {
			buffer.append("Changed filters: ");
			for (Iterator iter = changedFilters.iterator(); iter.hasNext(); ) {
				Filter filter = (Filter) iter.next();
				buffer.append(filter.getFilterType() + " -> " + filter.getTargetType());
				if (iter.hasNext()) {
					buffer.append(", ");
				}
			}
		}
		return "FilterChangedEvent:\n" + buffer.toString();
	}

}