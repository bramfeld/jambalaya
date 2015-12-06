/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Color;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;
import org.eclipse.mylar.zest.layouts.LayoutRelationship;

import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;

/**
 * @author Rob Lintern
 */
public interface ShrimpArc extends ShrimpDisplayObject, ShrimpDisplayObjectListener, LayoutRelationship {

	public final static Color DEFAULT_ARC_COLOR = Color.BLUE;
	public final static double DEFAULT_ARC_WEIGHT = 0.6;

	/**
	 * @return The colour of this arc.
	 */
	public abstract Color getColor();

	/**
	 * @return The style of this arc.
	 */
	public abstract ArcStyle getStyle();

	/**
	 * Set the color of this arc.
	 */
	public abstract void setColor(Color color);

	/**
	 * Set the style of this arc.
	 */
	public abstract void setStyle(ArcStyle arcStyle);

	/**
	 * Returns the relationship that this arc represents.
	 */
	public abstract Relationship getRelationship();

	/**
	 * Returns the relationship type.
	 * @see Relationship#getType()
	 */
	public abstract String getType();

	/**
	 * Returns the node that is the source (or parent) of this arc.
	 */
	public abstract ShrimpNode getSrcNode();

	/**
	 * Returns the node that is the destination (or child) of this arc.
	 */
	public abstract ShrimpNode getDestNode();

	/**
	 * Sets whether or not this arc is to be drawn highlighted.
	 */
	public void setHighlighted(boolean b);

	/** @return the source terminal of this arc */
	public ShrimpTerminal getSrcTerminal();

	/** @return the destination terminal of this arc */
	public ShrimpTerminal getDestTerminal();

	public void setCurveFactor(int curveFactor);
	public int getCurveFactor();

	/**
	 * Sets this arc to either drawing arrow heads or not.
	 * @param usingArrowHead if true, this arc will be rendered with an arrow head.
	 * @param arrowHeadStyle TODO
	 */
	public void setUsingArrowHead(boolean usingArrowHead, String arrowHeadStyle);

	public void setWeight(double weight);
	public double getWeight();

	public void setActive(boolean active);
	public boolean isActive();

    public abstract void updateVisibility();

    public void setInvertedInLayout(boolean invertedInLayout);
    public boolean getInvertedInLayout();

    public long getId ();

    public void setVisible(boolean visible);

    public LayoutBendPoint[] getBendPoints();
    public void setBendPoints(LayoutBendPoint[] bendPoints);
    public boolean hasBendPoints();

    /**
     * Redirect this arc to the specified source and destination. It is assumed that
     * the implementation will save the old source and destination nodes for later restoration.
     * @tag Shrimp.grouping
     * @param source
     * @param dest
     */
    public void redirect(ShrimpNode source, ShrimpNode dest);

    /**
     * Restore a redirected arc to its orginal source and destination nodes.
     * @tag Shrimp.grouping
     */
    public void restore();

}