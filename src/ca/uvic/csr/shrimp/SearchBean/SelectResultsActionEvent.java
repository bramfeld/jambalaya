/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventObject;
import java.util.Vector;


/**
 * The action event thrown to select the search results.
 * 
 * @author Rob Lintern
 */
public class SelectResultsActionEvent extends EventObject {
	private Vector resultsToSelect;

	/**
	 * Constructor for FilterResultsActionEvent.
	 */
	public SelectResultsActionEvent(SearchStrategy searchStrategy, Vector resultsToSelect) {
		super(searchStrategy);
		this.resultsToSelect = resultsToSelect;
	}
	
	/**
	 * Returns the results that should be selected
	 */
	public Vector getResultsToSelect() {
		return resultsToSelect;
	}
	
	/**
	 * Returns the search strategy used to get these search results.
	 */
	public SearchStrategy getSearchStrategy() {
		return (SearchStrategy)getSource();
	}
	

}
