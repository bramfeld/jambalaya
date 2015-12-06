/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.PersistentStorageBean;


/**
 * Listens for events coming from the persistent storage bean.
 * 
 * @author Rob Lintern
 */

public interface PersistentStorageBeanListener {
    
    /**
     * 
	 * @param event
	 */
	public void dataLoaded(DataLoadedEvent event);
}
