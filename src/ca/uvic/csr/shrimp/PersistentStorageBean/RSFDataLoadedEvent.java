/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.util.Hashtable;


/**
 * @author Rob Lintern
 */

public class RSFDataLoadedEvent extends DataLoadedEvent {
    
	private Hashtable rigiNodes;
	private Hashtable rigiArcs;
    
    public RSFDataLoadedEvent(Hashtable rigiNodes, Hashtable rigiArcs){
    	super (null);
		this.rigiNodes = rigiNodes;
		this.rigiArcs = rigiArcs;
    }
    
    public Hashtable getRigiNodes(){
		return rigiNodes;
    }

    public Hashtable getRigiArcs(){
		return rigiArcs;
    }
    
}