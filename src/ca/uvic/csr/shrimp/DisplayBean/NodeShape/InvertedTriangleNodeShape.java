/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;

/**
 * This is a triangle with the flat base at the top and the point at the bottom.
 *  ________
 *  \      /
 *   \    /
 *    \  /
 *     \/
 *     
 * @author Rob Lintern, Chris Callendar
 */
public class InvertedTriangleNodeShape extends AbstractNodeShape {
	
	public static final String NAME = "Triangle (Inverted)";
	
	public InvertedTriangleNodeShape(String name) {
		super(name);
	}
	
	public InvertedTriangleNodeShape() {
		this(NAME);
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new InvertedTriangleNodeShape();
		return shape;
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Rectangle2D.Double globalBounds, AffineTransform localToGlobalTx, Point2D.Double arcAttachPoint) {
		double nw = globalBounds.getWidth(); // node width
		double nh = globalBounds.getHeight(); // node height
		double nhalfw = nw/2.0; // half node width
		double nhalfh = nh/2.0; // half node height
		double tanTheta = Math.tan(terminalPositionAngle);
		
		/* 
		 * Idea: A node has four sides. Firstly, see which side intersects a line with 
		 * an angle of "theta", originating at the centre of the node. 
		 * Secondly, see where exactly on the side this intersection happens.
		 */
		double alpha = Math.atan2(nh, nw); // angle in radians between horizontal line through centre of node and bottom right corner
		double beta = DisplayConstants.HALF_PI - alpha; // in radians
		double x = 0.0;
		double y = 0.0;
		double tmpAngle = alpha + 2.0*beta;
		if (terminalPositionAngle <= -alpha && terminalPositionAngle >= -tmpAngle) {
			// intersects with top side of node
			y = -nhalfh;			// top of the triangle
			x = y / tanTheta;
		} else if ((terminalPositionAngle >= -DisplayConstants.PI && terminalPositionAngle <= -tmpAngle) || (terminalPositionAngle <= DisplayConstants.PI && terminalPositionAngle >= tmpAngle - beta)) {
			// intersects with left side of node
			double x1 = -nhalfw;		// (x1,y1) is the top left corner of the box and triangle
			double y1 = -nhalfh;
			double x2 = 0;				// (x2,y2) is the bottom middle of the triangle
			double y2 = nhalfh;
			double x3 = 0;				// (x3,y3) is the center of the triangle
			double y3 = 0;
			double x4 = -nhalfw;		// (x4,y4) is the left edge of the box at a y position relative to the angle
			double y4 = x4 * tanTheta;
			double u1 = ((x4 - x3)*(y1-y3) - (y4 - y3)*(x1 - x3))/((y4 - y3)*(x2 - x1) - (x4 - x3)*(y2 - y1));
			x = x1 + u1 * (x2 - x1);
			y = y1 + u1 * (y2 - y1);
		} else {
			// intersects with right side of node
			double x1 = nhalfw;		// (x1,y1) is the top right corner of the box and triangle
			double y1 = -nhalfh;
			double x2 = 0;			// (x2,y2) is the bottom middle of the triangle
			double y2 = nhalfh;
			double x3 = 0;			// (x3,y3) is the center
			double y3 = 0;
			double x4 = nhalfw;		// (x4,y4) is the right edge of the box at a y value relative to the angle
			double y4 = x4 * tanTheta;
			double u1 = ((x4 - x3)*(y1-y3) - (y4 - y3)*(x1 - x3))/((y4 - y3)*(x2 - x1) - (x4 - x3)*(y2 - y1));
			x = x1 + u1 * (x2 - x1);
			y = y1 + u1 * (y2 - y1);
		}
		arcAttachPoint.setLocation(x,y);
		return terminalPositionAngle;
	}
	
	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getInnerBounds(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double halfWidth = outerBounds.getWidth()/2.0d;
		double quartWidth = halfWidth/2.0d;
		double halfHeight = outerBounds.getHeight()/2.0d;
		return new Rectangle2D.Double (outerBounds.getX() + quartWidth, outerBounds.getY(), halfWidth, halfHeight);
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getShape(java.awt.geom.Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		double x = bounds.getX();
		double y = bounds.getY();
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		
		int [] xPoints = new int [3];
		xPoints[0] = (int) x;
		xPoints[1] = (int) (x + w/2.0d);
		xPoints[2] = (int) (x + w);
		
		int [] yPoints = new int [3];
		yPoints[0] = (int) y;
		yPoints[1] = (int) (y + h);
		yPoints[2] = (int) y;
		Polygon p = new Polygon (xPoints, yPoints, 3);
		return p;
	}
	
	
	
}
