/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Comparator;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

/**
 * Comparator for sorting the nodes by the number of children they have.
 * Any nodes that have the same number of children are sorted by artifact name instead.
 * 
 * @author Chris Callendar
 */
public class NodeNumChildrenComparator implements Comparator {
	
	private DisplayBean displayBean;
	
	public NodeNumChildrenComparator(DisplayBean displayBean) {
		this.displayBean = displayBean;
	}
	
	public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof ShrimpNode) || !(obj2 instanceof ShrimpNode)) {
	        System.err.println("not comparing ShrimpNodes");
	        return 0;
	    }
	    ShrimpNode node1 = (ShrimpNode)obj1;
	    ShrimpNode node2 = (ShrimpNode)obj2;
	    
		int num1 = node1.getArtifact().getChildrenCount(displayBean.getCprels());
    	int num2 = node2.getArtifact().getChildrenCount(displayBean.getCprels());
		if (num1 == num2) {
			// sort by artifact name instead
			String name1 = node1.getArtifact().getName();
			String name2 = node2.getArtifact().getName();
		    return name1.compareToIgnoreCase(name2);
		}
		return num2-num1;
	}
	
}