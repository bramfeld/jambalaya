/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventListener;


/**
 * Listens for events to filter search results.
 * 
 * @author Rob Lintern
 */
public interface FilterResultsActionListener extends EventListener {
	/**
	 * Preforms filtering of search results.
	 */ 
	public void filterResults (FilterResultsActionEvent frae);
}
