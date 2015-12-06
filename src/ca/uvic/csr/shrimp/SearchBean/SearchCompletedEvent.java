/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.Vector;


/** 
 * When a search finishes, this event will be thrown.
 */
public class SearchCompletedEvent {
	private Vector searchResults;
	private SearchStrategy searchStrategy;

	public SearchCompletedEvent (Vector searchResults, SearchStrategy searchStrategy) {
		this.searchResults = searchResults;
		this.searchStrategy = searchStrategy;
	}
	
	/** 
	 * Returns the results of the search as a vector of artifacts and relationships.
	 */
	public Vector getSearchResults () {
		return (Vector)searchResults.clone();
	}
	
	public SearchStrategy getSearchStrategy () {
		return searchStrategy;
	}
}