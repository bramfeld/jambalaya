/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.util.Hashtable;

import ca.uvic.csr.shrimp.AttributeConstants;

/**
 * A factory for creating rigi nodes and arcs.
 * @author Derek Rayside
 */
public class Factory {

    /**
     * key:     id
     * value:   GenericRigiNode
     */
    private Hashtable rigiNodes = new Hashtable();
    
    /**
     * key:     id
     * value:   GenericRigiArc
     */
    private Hashtable rigiArcs = new Hashtable();

	private final Hashtable nodeNameIDTable = new Hashtable();
	private int nextNodeID = 1;
	private int nextArcID = 1;



    /** Add factory contents to a storage bean. */
	protected void fireAddDataEvent(final PRJPersistentStorageBean storageBean) {
        storageBean.fireDataLoadedEvent(rigiNodes, rigiArcs);
    }

    /**
     * @param id should be a long encoded in a string
     */
	protected GenericRigiNode makeRigiNode(final String id) {

        // look it up, create if necessary
        final GenericRigiNode node;
        final Object contains = rigiNodes.get(id);
        if (contains == null) {
            // we need to make it
		    node = new GenericRigiNode();
            node.setNodeID(id);
            rigiNodes.put(id, node);
            // these should be set now, just in case they don't get
            // set later ... otherwise there'll be null pointers in
            // the visualizer
            //node.setNodeLabel(id);
            //node.setNodeType(id);
        } else {
            // it already exists, and we've just retrieved it
            node = (GenericRigiNode) contains;
        }
        return node;
	}


	protected GenericRigiNode makeRigiNodeFromName(String name) {
        // see if it's already got an id
        Object obj = nodeNameIDTable.get(name); 
        if (obj == null) {
            // it's new ... we need an id number
            final String id = Integer.toString(nextNodeID);
            nodeNameIDTable.put(name, id);
            nextNodeID++;
            final GenericRigiNode node = makeRigiNode(id);
            // these should be set now, just in case they don't get
            // set later ... otherwise there'll be null pointers in
            // the visualizer
            
            // see if the name contains some carets (^) and just take the first portion
            String shortName = name;
            int caretIndex = name.indexOf('^');
            if (caretIndex != -1) {
                shortName = name.substring(0, caretIndex);
                node.setCustomizedData(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME, name);
            }
            node.setNodeLabel(shortName);
            //node.setNodeType(name);
            return node;
        }
        
        // it's been done, look it up with the id
        return makeRigiNode(obj.toString());
    }
    

    
    /**
     * @param id should be a long encoded in a string
     */
	protected GenericRigiArc makeRigiArc(final String id) {

        // look it up, create if necessary
        final GenericRigiArc arc;
        final Object contains = rigiArcs.get(id);
        if (contains == null) {
            // we need to make it
		    arc = new GenericRigiArc();
            arc.setArcID(id);
            rigiArcs.put(id, arc);
            // these should be set now, just in case they don't get
            // set later ... otherwise there'll be null pointers in
            // the visualizer
            //arc.setArcLabel(id);
            //arc.setArcType(id);
        } else {
            // it already exists, and we've just retrieved it
            arc = (GenericRigiArc) contains;
        }
        return arc;
	}


	protected GenericRigiArc makeRigiArc() {
        return makeRigiArc(Integer.toString(nextArcID++));
    }

}


