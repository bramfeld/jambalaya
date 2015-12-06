/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter;

import java.util.Hashtable;

import ca.uvic.csr.shrimp.DataBean.SimpleDataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.DataLoadedEvent;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener;
import ca.uvic.csr.shrimp.PersistentStorageBean.RSFDataLoadedEvent;

/**
 * This adapter handles adding all artifact and relationships to the rigi data bean.
 * Hashtables of generic rigi nodes and arcs are simply passed along to the data bean.
 *
 * @author Rob Lintern
 */

public class PRJPersistentStorageBeanAdapter implements PersistentStorageBeanListener {
    private SimpleDataBean simpleDataBean;

    /** constructor initializing fields */
    public PRJPersistentStorageBeanAdapter (SimpleDataBean simpleDataBean) {
    	this.simpleDataBean = simpleDataBean;
    }
    
    /**
     * Processes the add data event.
     * 
     * @param event A data change event 
     */    
    public void dataLoaded(DataLoadedEvent event) {   	
    	Hashtable nodesTable = ((RSFDataLoadedEvent)event).getRigiNodes();
    	Hashtable arcsTable = ((RSFDataLoadedEvent)event).getRigiArcs(); 	
		simpleDataBean.setData(nodesTable, arcsTable);
    }
       
}
