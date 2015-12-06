/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventObject;
import java.util.Vector;


public class SearchRequestEvent extends EventObject {
	/**
	 * The raw data collector to store the objects which
	 * meet the requirements of this event.
	 */
	private Vector rawData;

	/**
	 * Constructs a new SearchRequestEvent.
	 * @param s the event source.
	 * @param rawData the raw data collector. An interested listener
	 * should immediately populate the raw data collector with the
	 * data objects requested by the event source <code>s</code>.
	 * If <code>null</code>, a new Vector will be created.
	 */
	public SearchRequestEvent(SearchStrategy s, Vector rawData) {
		super(s);
		if (rawData != null) {
			this.rawData = rawData;
		} else {
			this.rawData = new Vector();
		}
	}

	/**
	 * Gets the search strategy which fires this event.
	 * @return the event source which in fact is the search strategy.
	 */
	public SearchStrategy getSearchStrategy() {
		return (SearchStrategy) getSource();
	}

	/**
	 * Gets the raw data collector. An interested listener should
	 * immediately populate the raw data collector with necessary
	 * objects. It will be desirable that the interested listener
	 * uses the {@link SearchStrategy#getSelectedDataReflector()}
	 * to check objects to be added to the raw data collector.
	 * @return the raw data collector in this event.
	 */
	public Vector getRawDataCollector() {
		return rawData;
	}

	/**
	 * Sets a raw data collector for this event. Any interested
	 * listener could set the raw data collector which contains
	 * necessary objects.
	 * @param rawData the data collector for this event.
	 */
	public void setRawDataCollector(Vector rawData) {
		this.rawData = rawData;
	}

}

