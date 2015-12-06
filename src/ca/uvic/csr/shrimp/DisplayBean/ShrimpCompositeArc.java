/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.util.Collection;

import ca.uvic.csr.shrimp.DataBean.Relationship;

/**
 * A composite arc, from a source node to a destination node, represents one or more arcs between 
 * any of the following:
 * 		1. the source node and any descendents of the destination node
 * 		2. any descendents of the source node and the destination node
 * 		3. any descendents of the source node and any descendents of the destination node
 * 
 * @author Rob Lintern
 */
public interface ShrimpCompositeArc extends ShrimpArc {
	/**
	 * Returns how many arcs this single composite represents.
	 */
	public int getArcCount();

	/**
	 * Sets how many arcs this single composite represents.
	 * @param arcCount
	 */
	public void setArcCount(int arcCount);

	/**
	 * Returns whether or not this composite arc represents arcs lower down in the nested hierarchy.
	 */
	public boolean isAtHighLevel();

	/**
	 * Sets whether or not this composite arc represents arcs lower down in the nested hierarchy.
	 * @param atHighLevel
	 */
	public void setAtHighLevel(boolean atHighLevel);
	
	/**
	 * 
	 * @return A collection of the relationships that this composite represents.
	 */
	public Collection getRelationships();
	
    public boolean addRelationship (Relationship rel);
    
    public boolean removeRelationship (Relationship rel);


}
