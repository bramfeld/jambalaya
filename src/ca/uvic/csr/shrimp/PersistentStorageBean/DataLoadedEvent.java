/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.PersistentStorageBean;

/**
 * @author Rob Lintern
 */

public class DataLoadedEvent{
    private Object loadedData;
    
    public DataLoadedEvent(Object loadedData){
    	this.loadedData = loadedData;
    }
    
    public Object getLoadedData () {
    	return loadedData;
    }
    
}