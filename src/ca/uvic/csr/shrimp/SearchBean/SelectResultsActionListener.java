/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventListener;


/**
 * Listens for events to select search results.
 * 
 * @author Rob Lintern
 */
public interface SelectResultsActionListener extends EventListener {
	/**
	 * Preforms selection of search results.
	 */ 
	public void selectResults (SelectResultsActionEvent srae);
}
