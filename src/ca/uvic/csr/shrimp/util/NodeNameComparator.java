/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Comparator;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;


/**
 * Comparator for sorting the nodes in the ShrimpGridLayout
 * in alphabetical order, ignoring case.
 *
 */
public class NodeNameComparator implements Comparator {
    public static final NodeNameComparator NODE_NAME_COMPARATOR = new NodeNameComparator ();
    
    private NodeNameComparator () {
    }

    public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof ShrimpNode) || !(obj2 instanceof ShrimpNode)) {
	        System.err.println("not comparing ShrimpNodes");
	        return 0;
	    }
	    ShrimpNode node1 = (ShrimpNode)obj1;
	    ShrimpNode node2 = (ShrimpNode)obj2;
		String name1 = node1.getArtifact().getName();
		String name2 = node2.getArtifact().getName();
		return name1.compareToIgnoreCase(name2);
	}			
}