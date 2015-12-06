/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Comparator;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * Compares ShrimpNodes by type.  Nodes with the same type are sorted by name.
 * 
 * @author Chris Callendar
 */
public class NodeTypeComparator implements Comparator {
    public final static NodeTypeComparator NODE_TYPE_COMPARATOR = new NodeTypeComparator();
    
    private NodeTypeComparator () {        
    }
    
    public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof ShrimpNode) || !(obj2 instanceof ShrimpNode)) {
	        System.err.println("not comparing ShrimpNodes");
	        return 0;
	    }
	    ShrimpNode node1 = (ShrimpNode)obj1;
	    ShrimpNode node2 = (ShrimpNode)obj2;
	    String type1 = node1.getArtifact().getType();
		String type2 = node2.getArtifact().getType();
		int result = type1.compareTo(type2);
		if (result == 0) {
			String name1 = node1.getArtifact().getName();
			String name2 = node2.getArtifact().getName();
		    return name1.compareToIgnoreCase(name2); 
		}
		return result;
	}
    
}