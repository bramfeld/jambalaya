/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;

/**
 * @author Rob Lintern
 */
public class EllipseNodeShape extends AbstractNodeShape {
	
	public static final String NAME = "Ellipse";
	
	public EllipseNodeShape() {
		this(NAME);
	}
	
	public EllipseNodeShape(String name) {
		super(name);
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new EllipseNodeShape(name);
		return shape;
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
   		double a = outerBounds.getWidth()/2.0d;
    	double b = outerBounds.getHeight()/2.0d;
    	double x = a/Math.sqrt(2.0d);
    	double y = b/Math.sqrt(2.0d);
    	
    	return new Rectangle2D.Double (outerBounds.getX() + (a - x) + outerBounds.getX(), outerBounds.getY() + (b - y), 2.0d*x, 2.0d*y);
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Rectangle2D.Double globalBounds, AffineTransform localToGlobalTx, Point2D.Double arcAttachPoint) {
		double nw = globalBounds.getWidth(); // node width
		double nh = globalBounds.getHeight(); // node height
		double nhalfw = nw/2.0; // half node width
		double nhalfh = nh/2.0; // half node height
		double tanTheta = Math.tan(terminalPositionAngle);
		
		double x = 0.0;
		double y = 0.0;
		
		double a = nhalfw;
		double b = nhalfh;
		x = (a*b) / Math.sqrt(Math.pow(b, 2) + Math.pow(a, 2) * Math.pow(tanTheta, 2));
		if ((terminalPositionAngle > DisplayConstants.HALF_PI && terminalPositionAngle < 1.5*DisplayConstants.PI) || (terminalPositionAngle < DisplayConstants.NEG_HALF_PI && terminalPositionAngle > -1.5*DisplayConstants.PI)) {
			x = -x;
		}
		y = tanTheta*x;
		arcAttachPoint.setLocation(x, y);
		return terminalPositionAngle;
	}

	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getShape(java.awt.geom.Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		return new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
	}

}
