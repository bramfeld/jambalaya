/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;


/**
 * When something is filtered/unfiltered, this class updates the 
 * the display bean.
 */
public interface FilterChangedListener {
	
	public void filterChanged(FilterChangedEvent fce);
	
}