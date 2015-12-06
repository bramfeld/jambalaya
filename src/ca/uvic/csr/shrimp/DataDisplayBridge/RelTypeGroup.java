/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataDisplayBridge;

import java.awt.Color;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CompositeArcStyle;

/**
 * A group of arc/relationship types.
 * A RelTypeGroup may be "composite enabled" meaning that composite
 * arcs will be displayed for this group. 
 * This way, a composite arc with a particular style and color can represent 
 * a whole group of arc/relationship types.
 * 
 * @author Rob Lintern
 */
public class RelTypeGroup implements Comparable {

	private static final Color DEFAULT_COMPOSITE_COLOR = Color.blue;
    private static final ArcStyle DEFAULT_COMPOSITE_ARC_STYLE = new CompositeArcStyle();
    private static final boolean DEFAULT_COMPOSITES_ENABLED = false;
    
	private String groupName;
	private Vector relTypes = new Vector();

	private boolean compositesEnabled = DEFAULT_COMPOSITES_ENABLED;
	private ArcStyle compositeStyle = DEFAULT_COMPOSITE_ARC_STYLE;
	private Color compositeColor = DEFAULT_COMPOSITE_COLOR;

	public RelTypeGroup(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Adds the given relationship type to this group.
	 * @param relType
	 */
	protected void add(String relType) {
		if (!relTypes.contains(relType)) {
			relTypes.add(relType);
		}
	}

	/**
	 * Returns the name of this group.
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Sets the name of this group
	 * @param name
	 */
	public void setGroupName(String name) {
		if (groupName.equals(name))
			return;

		this.groupName = name;
		// TODO: update the visible arcs
	}
	
	/**
	 * Whether or not composite arcs are enabled for this group.
	 */
	public boolean areCompositesEnabled() {
		return compositesEnabled;
	}

	/**
	 * Sets whether or not composite arcs are enabled for this group.
	 * @param compositesEnabled
	 */
	void setCompositesEnabled(boolean compositesEnabled) {
		if (this.compositesEnabled != compositesEnabled) {
			this.compositesEnabled = compositesEnabled;
		}
	}

	public void setCompositeColor(Color color) {
		// TODO: update the visible arcs... check if color == compositecolor
		compositeColor = color;
	}

	public ArcStyle getCompositeStyle() {
		return compositeStyle;
	}

	public Color getCompositeColor() {
		return compositeColor;
	}

	public Vector getRelTypes() {
		return new Vector(relTypes);
	}
	
	boolean hasRelType (String relType) {
		return relTypes.contains(relType);
	}

	/**
	 * 
	 * @param relType
	 * @return Whether or not this group had the given rel type
	 */
	boolean remove(String relType) {
		return relTypes.remove(relType);
	}

	public String toString() {
		return groupName;
	}

	/**
	 * RelTypeGroups are sorted alphabetically by group name
	 */
	public int compareTo(Object o) {
		RelTypeGroup that = (RelTypeGroup) o;
		return this.getGroupName().compareTo(that.getGroupName());
	}
	
	public boolean equals(Object obj) {
		return (this.compareTo(obj) == 0);
	}
	
}