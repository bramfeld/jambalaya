/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.DisplayBean.event;


/**
 * This adapter handles requests to check if an object is filtered 
 *
 * @author Casey Best, Rob Lintern
 */

public interface DisplayFilterRequestListener {
	
    
    /** Check if the passed in object is filtered */
    public boolean isFiltered (Object obj);
    
}