/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;



/**
 * The class provides some common code for all arc styles to use.
 * 
 * @author Rob Lintern
 */
public abstract class AbstractArcStyle implements ArcStyle {
 
	/** The amount to add to the line width by when the line is highlighted */
	protected static final double HIGHLIGHT_THICKNESS = 3.0;

	/** The amount to add to the line width by when the line is active */
	protected static final double ACTIVE_THICKNESS = 4.0;
    
   
	private String name;
	protected int curveFactor = 0;
	protected boolean highlighted = false;
	protected boolean active = false;
	protected double mag = 1.0;
	protected double weight = 1.0;
	protected Point2D.Double srcPoint = new Point2D.Double(0,0);
	protected Point2D.Double destPoint = new Point2D.Double(0,0);
	protected boolean isVisible = true;

	// @tag Shrimp(Bendpoints) :  [Author = Chris Bennett;Date = 16/06/06 11:08 AM;]
	protected LayoutBendPoint[] bendPoints;;
	protected AffineTransform bendPointToGlobalTransform = new AffineTransform();
	protected LayoutBendPoint[] transformedBendPoints; // in global coordinates
	
	/**
	 * Constructor for AbstractArcStyle.
	 */
	public AbstractArcStyle (String name) {
		this.name = name;
	}
    
 	/**
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		return getName().compareToIgnoreCase(((ArcStyle)o).getName());
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof AbstractArcStyle) && ((AbstractArcStyle)obj).getName().equals(getName());
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}
	
	/**
	 * Returns the name of this style
	 */
	public String getName () {
		return name;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name;
	}
	
 	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#clone()
	 */
	public abstract Object clone();

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setViewMagnification(double)
	 */
	public void setViewMagnification(double mag) {
		if (this.mag != mag) {
			this.mag = mag;
		}
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setCurveFactor(int)
	 */
	public void setCurveFactor(int curveFactor) {
		if (this.curveFactor != curveFactor) {
			this.curveFactor = curveFactor;
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			this.highlighted = highlighted;
		}
	}
	
	public void setActive(boolean active) {
		if (this.active != active) {
			this.active = active;
		}
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getActive()
	 */
	public boolean getActive() {
		return active;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setWeight(double)
	 */
	public void setWeight(double weight) {
		if (this.weight != weight) {
			this.weight = weight;
		}

	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (this.isVisible != visible) {
			this.isVisible = visible;
		}
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getCurveFactor()
	 */
	public int getCurveFactor() {
		return curveFactor;
	}



	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getHighlighted()
	 */
	public boolean getHighlighted() {
		return highlighted;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getViewMagnification()
	 */
	public double getViewMagnification() {
		return mag;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getVisible()
	 */
	public boolean getVisible() {
		return isVisible;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getWeight()
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getDestPoint()
	 */
	public Point2D.Double getDestPoint() {
		return destPoint;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getSrcPoint()
	 */
	public Point2D.Double getSrcPoint() {
		return srcPoint;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getThumbnail(java.awt.Color)
	 */
	public JPanel getThumbnail(Color arcColor) {
		return getThumbnail(arcColor, 40, 8);
	}

	protected JPanel getThumbnail(Color arcColor, int width, int height) {
		final ArcStyle style = getThumnailArcStyle();
		style.setWeight(getThumnailWeight());
		final Color color = arcColor;
		style.setActive(false);
		style.setCurveFactor(0);
		style.setHighlighted(false);
		Point2D.Double srcPoint = new Point2D.Double();
		Point2D.Double destPoint = new Point2D.Double();
		setThumbnailPoints (srcPoint, destPoint, width, height);
		style.setSrcDestPoints(srcPoint, destPoint);
		style.setViewMagnification(1.0);
		style.setVisible(true);
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				if (g instanceof Graphics2D) {
					((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					style.render((Graphics2D)g, HIGH_QUALITY_RENDERING, color, false, false);
				}
			}
		};
		panel.setPreferredSize(new Dimension(width, height));
		panel.setBorder(null);
		return panel;
	}
	
	protected void setThumbnailPoints (Point2D.Double srcPoint, Point2D.Double destPoint, int width, int height) {
		srcPoint.setLocation(0, height/2.0);
		destPoint.setLocation (width, height/2.0);
	}
	
	protected double getThumnailWeight() {
		return 1.0;
	}
	
	protected ArcStyle getThumnailArcStyle() {
		return (ArcStyle) this.clone();
	}

	// @tag Shrimp(Bendpoints) :  [Author = Chris Bennett;Date = 16/06/06 11:08 AM;]
	public void setBendPoints(LayoutBendPoint[] bendPoints) {
		this.bendPoints = bendPoints;
	}
	
	public boolean hasBendPoints() {
		return bendPoints!= null && bendPoints.length > 0;
	}

	// @tag Shrimp(Bendpoints)
	public LayoutBendPoint[] getBendPoints() {
		return this.bendPoints;
	}
	
	// @tag Shrimp(Bendpoints)
	public LayoutBendPoint[] getTransformedBendPoints() {
		return this.transformedBendPoints;
	}
	
}
