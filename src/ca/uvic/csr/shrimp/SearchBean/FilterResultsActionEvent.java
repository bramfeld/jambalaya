/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventObject;
import java.util.Vector;


/**
 * The action event thrown to filter the search results.
 * 
 * @author Rob Lintern
 */
public class FilterResultsActionEvent extends EventObject {
	
	private Vector resultsToFilter;
	private boolean filter;

	/**
	 * Constructor for FilterResultsActionEvent.
	 */
	public FilterResultsActionEvent(SearchStrategy searchStrategy, Vector resultsToFilter, boolean filter) {
		super(searchStrategy);
		this.resultsToFilter = resultsToFilter;
		this.filter = filter;
	}
	
	public boolean isFiltered() {
		return filter;
	}
	
	/**
	 * Returns the results that should be filtered
	 */
	public Vector getResultsToFilter() {
		return resultsToFilter;
	}
	
	/**
	 * Returns the search strategy used to get these search results.
	 */
	public SearchStrategy getSearchStrategy() {
		return (SearchStrategy) getSource();
	}

}
