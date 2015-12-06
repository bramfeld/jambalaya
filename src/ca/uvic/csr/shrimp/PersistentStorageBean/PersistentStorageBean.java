/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.net.URI;
import java.util.Vector;


/**
 * PersistentStorageBean: This is an interface declaring necessary functions
 * to be implemented in storage beans in Shrimp system.
 * 
 * @author Anton An
 * date: July 24, 2000
 */

public interface PersistentStorageBean{
 
    /**
     * load data from a file or a database table 
     * @param uri the name of the file or the database table
     */
    public void loadData(URI uri);

    /** 
     * create a persistent storage to store graph data
     * @param filename the name of the file or the database table to store the data
     * @param artifacts the collection of artifacts
     * @param relationships the collection of relationships
     */
    public void saveData(String filename, Vector artifacts, Vector relationships);

    /**
     * Adds a listener for events coming from this persistent storage bean. 
     * @param listener The PersistentStorageBeanListener to be added.
     */
    public void addPersistentStorageBeanListener(PersistentStorageBeanListener listener);

    /**
     * Removes a listener for events coming from this persistent storage bean. 
     * @param listener The PersistentStorageBeanListener to be removed.
     */
    public void removePersistentStorageBeanListener(PersistentStorageBeanListener listener);
}


