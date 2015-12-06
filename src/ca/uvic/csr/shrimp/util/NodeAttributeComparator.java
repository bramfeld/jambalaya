/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.util;

import java.util.Comparator;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
 
/**
 * Comparator for sorting nodes based on any attribute.
 * If the attribute specified is not an instance of Comparable
 * then nodes will be compared by name.
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 */
public class NodeAttributeComparator implements Comparator {
    public static final NodeAttributeComparator NODE_ATTRIBUTE_COMPARATOR = new NodeAttributeComparator();
	
    private String attribute;
	
	private NodeAttributeComparator() {
		super();
	}
	
	public void setAttribute (String attribute) {
		this.attribute = attribute;
	}
	
	public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof ShrimpNode) || !(obj2 instanceof ShrimpNode)) {
	        System.err.println("not comparing ShrimpNodes");
	        return 0;
	    }
	    ShrimpNode node1 = (ShrimpNode)obj1;
	    ShrimpNode node2 = (ShrimpNode)obj2;
		if ((attribute != null) && !attribute.equals("")) {
			Object attr1 = node1.getArtifact().getAttribute(attribute);
			Object attr2 = node2.getArtifact().getAttribute(attribute);
			if (attr1 instanceof Comparable && attr2 instanceof Comparable) {
			    int result = ((Comparable)attr1).compareTo(attr2);
			    if (result != 0) {
			        return result;
			    }
			}	
		}
    	String name1 = node1.getArtifact().getName();
    	String name2 = node2.getArtifact().getName();
	    return name1.compareToIgnoreCase(name2);
	}	

} 

