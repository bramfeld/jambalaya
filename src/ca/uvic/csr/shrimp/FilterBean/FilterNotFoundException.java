/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */ 
package ca.uvic.csr.shrimp.FilterBean;

/**
 * This exception is thrown when someone tries to remove a filter
 * that doesn't exist.
 *
 * @author Casey Best
 * @date June 19, 2000
 */
public class FilterNotFoundException extends Exception {
	
	public FilterNotFoundException() {
		super();
	}

	public FilterNotFoundException(String msg) {
		super(msg);
	}
}