/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * Comparator for sorting the nodes by the number of <i>visible</i> arcs it has.
 * Any nodes that have the same number of relationships are sorted by name.
 * 
 * @author Chris Callendar
 */
public class NodeNumRelsComparator implements Comparator {
    
	private DisplayBean displayBean;
    
    public NodeNumRelsComparator (DisplayBean displayBean) {
    	this.displayBean = displayBean;
    }
    
	public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof ShrimpNode) || !(obj2 instanceof ShrimpNode)) {
	        System.err.println("not comparing ShrimpNodes");
	        return 0;
	    }
	    
	    ShrimpNode node1 = (ShrimpNode)obj1;
		ShrimpNode node2 = (ShrimpNode)obj2;

		Vector rels1 = node1.getArtifact().getRelationships();
		int num1 = rels1.size();
		// remove the hidden relationships
		for (Iterator iter = rels1.iterator(); iter.hasNext(); ) {
			Relationship rel = (Relationship) iter.next();
			if (displayBean.isFiltered(rel)) {
				num1--;
			}
		}

		Vector rels2 = node2.getArtifact().getRelationships();
		int num2 = rels2.size();
		// remove the hidden relationships
		for (Iterator iter = rels2.iterator(); iter.hasNext(); ) {
			Relationship rel = (Relationship) iter.next();
			if (displayBean.isFiltered(rel)) {
				num2--;
			}
		}

		if (num1 == num2) {
			// sort by artifact name instead
			String name1 = node1.getArtifact().getName();
			String name2 = node2.getArtifact().getName();
		    return name1.compareToIgnoreCase(name2);
		}
		return num2-num1;
	}
	
}