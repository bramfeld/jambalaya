/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArrowHead;


/**
 * @author Rob Lintern
 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc
 */
public class PShrimpCompositeArc extends PShrimpArc implements ShrimpCompositeArc {

	private boolean atHighLevel = true;
	private int arcCount = 0;
	private Set relationships = new HashSet();

	/**
	 *
	 * @param displayBean
	 * @param srcNode
	 * @param destNode
	 * @param srcTerminal
	 * @param destTerminal
	 * @param arcStyle
	 * @param curveFactor
	 * @param useArrowHead
	 * @param weight
	 */
	public PShrimpCompositeArc(DisplayBean displayBean, ShrimpNode srcNode, ShrimpNode destNode,
		ShrimpTerminal srcTerminal, ShrimpTerminal destTerminal, ArcStyle arcStyle,
		int curveFactor, boolean useArrowHead, double weight) {
		super (displayBean, null, srcNode, destNode, srcTerminal, destTerminal, arcStyle,
				curveFactor, useArrowHead, ArrowHead.DEFAULT_STYLE, weight);
	}

	public int getArcCount() {
		return arcCount;
	}

	public void setArcCount(int newArcCount) {
		if (newArcCount == 0) {
			System.err.println("whooa! setting arc count to 0!");
		}
		int oldArcCount = this.arcCount;
		if (oldArcCount != newArcCount) {
			this.arcCount = newArcCount;
			setWeight(newArcCount);
			if (isVisible()) {
				if (oldArcCount != 0) {
					CompositeArcsManager.updateVisibleCompositeArcCounts(this, oldArcCount, false);
				}
				CompositeArcsManager.updateVisibleCompositeArcCounts(this, newArcCount, true);
			}
		}
	}

	public boolean isAtHighLevel() {
		return atHighLevel;
	}

	public void setAtHighLevel(boolean atHighLevel) {
		this.atHighLevel = atHighLevel;
		setTransparency(atHighLevel ? DisplayConstants.DEFAULT_COMPOSITE_ARC_STYLE_TRANSPARENCY : 1.0f);
	}

	/**
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArc#toString()
     */
    public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("PShrimpCompositeArc {");
		//s.append("relationship: " + (relationship == null ? "null" : relationship.getName()));
		s.append((srcNode == null ? null : srcNode.getName()) + " --> ");
		s.append((destNode == null ? null : destNode.getName()));
		s.append(", arcs=" + getArcCount());
		s.append("}");
		return s.toString();
    }

	/**
	 * @see PShrimpArc#setVisible(boolean)
	 */
	public void setVisible(boolean newVisibility) {
		boolean oldVisible = isVisible();
		super.setVisible(newVisibility);
		boolean newVisible = isVisible();
		if (!oldVisible && newVisible) {
		    CompositeArcsManager.updateVisibleCompositeArcCounts(this, getArcCount(), true);
		} else if (oldVisible && !newVisible) {
		    CompositeArcsManager.updateVisibleCompositeArcCounts(this, getArcCount(), false);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#dispose()
	 */
	public void dispose() {
	    //System.out.println("disposing composite " + toString());
		if (isVisible()) {
		    CompositeArcsManager.updateVisibleCompositeArcCounts(this, getArcCount(), false);
		}
		super.dispose();
	}

    protected boolean isFiltered() {
        boolean atLeastOneRelNotFiltered = false;
        for (Iterator iter = relationships.iterator(); iter.hasNext() && !atLeastOneRelNotFiltered;) {
            Relationship rel = (Relationship) iter.next();
            atLeastOneRelNotFiltered = !displayBean.isFiltered(rel);
        }
        return !atLeastOneRelNotFiltered;
    }

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc#getRelationships()
     */
    public Collection getRelationships() {
        return relationships;
    }

    public boolean addRelationship (Relationship rel) {
        return relationships.add(rel);
    }

    public boolean removeRelationship (Relationship rel) {
        return relationships.remove(rel);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#getRelationship()
     */
    public Relationship getRelationship() {
        // if this composite is not showing "at a high level" then
        // it should only represent one relationship, so we can safely return this one
        if (!isAtHighLevel() && relationships.size() == 1) {
            return (Relationship) relationships.iterator().next();
        }
        return null;
    }










}
