/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

import javax.swing.JPanel;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;

/**
 * This interface allows an arc to be displayed with different
 * styles.  For example, it may display with dotted lines, dots
 * squares, etc.
 *
 * @author Casey Best, Rob Lintern
 */
public interface ArcStyle extends Comparable, Cloneable {

	public static final int HIGH_QUALITY_RENDERING = 0; // same as piccolo
	public static final int LOW_QUALITY_RENDERING = 1; // same as piccolo

	public String getName();

	/**
	 * @param mag
	 */
	public void setViewMagnification(double mag);

	/**
	 * @param highlighted
	 */
	public void setHighlighted(boolean highlighted);

	/**
	 * @param active
	 */
	public void setActive(boolean active);

	public boolean getActive();

	/**
	 * @param curveFactor
	 */
	public void setCurveFactor(int curveFactor);

	/**
	 * @param srcPoint
	 * @param destPoint
	 */
	public void setSrcDestPoints(Point2D.Double srcPoint, Point2D.Double destPoint);

	/**
	 * Sets the source and destination points for this arc plus passes in a transform that
	 * can be used to transform bendpoints to global coordinates
	 * @param srcPoint
	 * @param destPoint
	 * @param bendPointToGlobalTransform
	 */
	public void setSrcDestPoints(Double srcPoint, Double destPoint, AffineTransform bendPointToGlobalTransform);

	/**
	 * @param bendPoints
	 */
	public void setBendPoints(LayoutBendPoint[] bendPoints);

	/**
	 * @param weight
	 */
	public void setWeight(double weight);

	/**
	 * @param visible
	 */
	public void setVisible(boolean visible);

	public Rectangle2D getBounds();

	/**
	 * @param r
	 * @return True if this arc style interesects the given rectangle
	 */
	public boolean intersects(Rectangle2D r);

	/**
	 * @param g2
	 * @param renderingQuality
	 * @param arcColor
	 * @param showSrcConnector
	 * @param showDestConnector
	 */
	public void render(Graphics2D g2, int renderingQuality, Color arcColor, boolean showSrcConnector, boolean showDestConnector);

	/**
	 * @param arcColor
	 * @return A JPanel that display a thumbnail of this style
	 */
	public JPanel getThumbnail(Color arcColor);

	public Object clone();

	public int getCurveFactor();

	public Point2D.Double getDestPoint();

	public Point2D.Double getSrcPoint();

	public boolean hasBendPoints();

	public LayoutBendPoint[] getBendPoints();

	public boolean getHighlighted();

	public double getViewMagnification();

	public boolean getVisible();

	public double getWeight();

	public LayoutBendPoint[] getTransformedBendPoints();

}