/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;



/**
 * This is an algorithm for organizing nodes.
 * It can be done in any way, as long as nodes passed in remain within
 * the given bounds.
 *
 * NOTE: Layouts can include visible and invisible objects.
 *       It is up to the layout algorithm to determine how
 *       this should be approached.
 *
 * @author Casey Best, Rob Lintern
 */

public interface Layout {

	/**
	 * Returns the name of this algorithm
	 */
	public String getName();

	/**
	 * Resets the state of this layout
	 */
	public void resetLayout();

	/**
	 * This will organize the given nodes
	 * in a way based on the algorithm within this layout.
	 * NOTE: The nodes passed in should all be siblings.
	 *
	 * NOTE: This method must determine which arcs should be
	 * 		 included within the layout and whether or not the
	 *       invisible nodes/arcs will be included
	 *
	 * @param potentialNodesToInclude A vector of ShrimpNodes. The nodes to organize.
	 * @param nodesToExclude A vector of ShrimpNodes. The nodes that are definitely not to be
	 * laid out by this algorithm. These will be placed in a grid at the bottom of
	 * the given bounds.
	 * @param bounds The bounds to fit the artifacts into. These bounds will be
	 * split between the nodesToInclude and the nodesToExclude.
	 * @param showDialog Whether or not to show the layout setting dialog.
	 * @param animate Whether or not to animate this layout.
	 * @param separateComponents Whether or not to seperate components or clusters.
	 */
	public void setupAndApplyLayout(Vector potentialNodesToInclude, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents);


	/**
	 * This method sets the arc types of interest for this layout.
	 * If null or an empty collection is passed in then all arcs will be included
	 * when the layout is run.
	 * @param arcTypes
	 * @see Layout#includeArc(ShrimpArc)
	 */
	public void setArcTypes(Collection arcTypes);

	/**
	 * This method returns true if the arc should be included in the layout.
	 * By default <b>true</b> is returned unless {@link Layout#setArcTypes(Collection)} is called
	 * with specific arc types and in that case true is returned only if the arc's type is in the
	 * arc types collection.
	 * @param arc the arc to include
	 * @return true or false
	 * @see Layout#setArcTypes(Collection)
	 */
	public boolean includeArc(ShrimpArc arc);

}
