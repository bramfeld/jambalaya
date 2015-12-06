/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;


/**
 * @author Rob Lintern
 *
 * Defines a diamond node shape
 * 
 */
public class DiamondNodeShape extends AbstractNodeShape {
	public static final String NAME = "Diamond";

	public DiamondNodeShape() {
		this(NAME);
	}
	public DiamondNodeShape(String name) {
		super(name);
	}
	
	/**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        DiamondNodeShape shape = new DiamondNodeShape(name);
		return shape;
    }

    /**
     * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getTerminalAttachPoint(double, java.awt.geom.Rectangle2D.Double, java.awt.geom.AffineTransform, java.awt.geom.Point2D.Double)
     */
    public double getTerminalAttachPoint(double terminalPositionAngle, Double globalBounds, AffineTransform localToGlobalTx, java.awt.geom.Point2D.Double arcAttachPoint) {
		double terminalTheta = terminalPositionAngle;
		double nw = globalBounds.getWidth(); // node width
		double nh = globalBounds.getHeight(); // node height
		double nhalfw = nw/2.0; // half node width
		double nhalfh = nh/2.0; // half node height
		//double tanTheta = (terminalPositionAngle == 0.0) ? 0.0 : Math.tan(terminalPositionAngle);

		/* 
		 * Firstly, see which segments of the side intersects a line with 
		 * an angle of "theta", originating at the centre of the node. 
		 * Secondly, see where exactly on the side this intersection happens.
		 * Note: The y axis is inverted like screen coordinates, and theta is between -PI and +PI
		 * 
		 * <pre>
		 * 
		 * 						 -Y	
		 *                          
		 *                         | 
		 *                         | -PI/2
		 *                         | 
		 *                         | 
		 *                         | 
		 *                | nhalfw | nhalfw |
		 *                |________|________| ___
		 *                |       /|\       |  
		 * 	              |      / | \      |
		 *                      /  |  \     |
		 *                    B/   |   \A   |
		 *                    /    |    \   |  nhalfh
		 *                   /     |     \  |
		 *       -PI        /      |      \ |              - 0
		 * -X  ____________/_______|(0,0)__\|__________________   +X
		 *                 \       |       /|
		 *       +PI        \      |      / |              + 0
		 *                   \     |     /  |  
		 *                   D\    |    /C  |
		 *                     \   |   /    |  nhalfh
		 *                      \  |  /     |
		 *                       \ | /      |
		 *                        \|/   ____| ___     
		 *                         | 
		 *                         |
		 *                         |
		 *                         | +PI/2
		 * 
		 *                       +Y
		 * 
		 * </pre>
		 */
		//double alpha = Math.atan2(nh, nw); // angle in radians between horizontal line through centre of node and bottom right corner
		//double beta = DisplayConstants.HALF_PI - alpha; // in radians
		double x = 0.0;
		double y = 0.0;
		double alpha = DisplayConstants.HALF_PI - Math.atan2(nh, nw);
		double angleSideA = - alpha;
		double angleSideB = - (DisplayConstants.PI - alpha);
		double angleSideC = alpha;
		double angleSideD = DisplayConstants.PI - alpha;

		if (terminalPositionAngle == 0.0 || terminalPositionAngle == -0.0) {
			// intersects middle of right side
			x = nhalfw;
			y = 0.0;
			terminalTheta = 0;
		} else if (terminalPositionAngle == DisplayConstants.PI || terminalPositionAngle == DisplayConstants.NEG_PI) {
			// intersects middle of left side
			x = -nhalfw;
			y = 0.0;
			terminalTheta = DisplayConstants.PI;
		} else if (terminalPositionAngle == DisplayConstants.HALF_PI) {
			// intersects middle of bottom side
			x = 0.0;
			y = nhalfh;
			terminalTheta = DisplayConstants.HALF_PI;
		} else if (terminalPositionAngle == DisplayConstants.NEG_HALF_PI) {
			// intersects middle of top side
			x = 0.0;
			y = -nhalfh;
			terminalTheta = DisplayConstants.NEG_HALF_PI;
		} else if (terminalPositionAngle > DisplayConstants.NEG_HALF_PI && terminalPositionAngle < 0) {
			// intersects with side A
//			double line1x1 = 0;
//			double line1y1 = -nhalfh;
//			double line1x2 = nhalfw;
//			double line1y2 = 0;
//			double line2x1 = 0;
//			double line2y1 = 0;
//			double line2x2 = nhalfw;
//			double line2y2 = -nhalfw*tanTheta;
//			double u1 = ((line2x2 - line2x1)*(line1y1-line2y1) - (line2y2 - line2y1)*(line1x1 - line2x1))/((line2y2 - line2y1)*(line1x2 - line1x1) - (line2x2 - line2x1)*(line1y2 - line1y1));
			x = 0; //TODO line1x1 + u1 * (line1x2 - line1x1);
			y = 0; //TODO line1y1 + u1 * (line1y2 - line1y1);
			terminalTheta = angleSideA;
		} else if (terminalPositionAngle < DisplayConstants.NEG_HALF_PI && terminalPositionAngle > DisplayConstants.NEG_PI) {
			// intersects with side B
//			double line1x1 = 0;
//			double line1y1 = -nhalfh;
//			double line1x2 = -nhalfw;
//			double line1y2 = 0;
//			double line2x1 = 0;
//			double line2y1 = 0;
//			double line2x2 = -nhalfh*tanTheta;
//			double line2y2 = -nhalfh;
//			double u1 = ((line2x2 - line2x1)*(line1y1-line2y1) - (line2y2 - line2y1)*(line1x1 - line2x1))/((line2y2 - line2y1)*(line1x2 - line1x1) - (line2x2 - line2x1)*(line1y2 - line1y1));
			x = 0; //TODO line1x1 + u1 * (line1x2 - line1x1);
			y = 0; //TODO line1y1 + u1 * (line1y2 - line1y1);
			terminalTheta = angleSideB;
		} else if (terminalPositionAngle > 0 && terminalPositionAngle < DisplayConstants.HALF_PI) {
			// intersects with side C
//			double line1x1 = 0;
//			double line1y1 = nhalfh;
//			double line1x2 = nhalfw;
//			double line1y2 = 0;
//			double line2x1 = 0;
//			double line2y1 = 0;
//			double line2x2 = nhalfw;
//			double line2y2 = nhalfw*tanTheta;
//			double u1 = ((line2x2 - line2x1)*(line1y1-line2y1) - (line2y2 - line2y1)*(line1x1 - line2x1))/((line2y2 - line2y1)*(line1x2 - line1x1) - (line2x2 - line2x1)*(line1y2 - line1y1));
			x = 0; //TODO line1x1 + u1 * (line1x2 - line1x1);
			y = 0; //TODO line1y1 + u1 * (line1y2 - line1y1);
			terminalTheta = angleSideC;
		} else if (terminalPositionAngle > DisplayConstants.HALF_PI && terminalPositionAngle < DisplayConstants.PI){
			// intersects with side D
//			double line1x1 = 0;
//			double line1y1 = nhalfh;
//			double line1x2 = -nhalfw;
//			double line1y2 = 0;
//			double line2x1 = 0;
//			double line2y1 = 0;
//			double line2x2 = -nhalfh*tanTheta;
//			double line2y2 = nhalfh;
//			double u1 = ((line2x2 - line2x1)*(line1y1-line2y1) - (line2y2 - line2y1)*(line1x1 - line2x1))/((line2y2 - line2y1)*(line1x2 - line1x1) - (line2x2 - line2x1)*(line1y2 - line1y1));
			x = 0; //TODO line1x1 + u1 * (line1x2 - line1x1);
			y = 0; //TODO line1y1 + u1 * (line1y2 - line1y1);
			terminalTheta = angleSideD;
		} else {
		    System.err.println("cant find intersection of arc and node shape");
		}
		arcAttachPoint.setLocation(x, y);
		return terminalTheta;
    }

    /**
     * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getInnerBounds(java.awt.geom.Rectangle2D)
     */
    public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
        double x = outerBounds.getX();
		double y = outerBounds.getY();
		double w = outerBounds.getWidth();
		double h = outerBounds.getHeight();
		Rectangle2D.Double innerBounds = new Rectangle2D.Double(x+w/4.0, y+h/4.0, w/2.0, h/2.0); //TODO calc proper inner bounds for diamond shape
		return innerBounds;
    }

    /**
     * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getShape(java.awt.geom.Rectangle2D)
     */
    public Shape getShape(Rectangle2D bounds) {
		double x = bounds.getX();
		double y = bounds.getY();
		double w = bounds.getWidth();
		double h = bounds.getHeight();
		
		int [] xPoints = new int [4];
		xPoints[0] = (int) (x + w/2.0);
		xPoints[1] = (int) (x + w);
		xPoints[2] = (int) (x + w/2.0);
		xPoints[3] = (int) (x);
		
		int [] yPoints = new int [4];
		yPoints[0] = (int) y;
		yPoints[1] = (int) (y + h/2.0);
		yPoints[2] = (int) (y + h);
		yPoints[3] = (int) (y + h/2.0);
		Polygon p = new Polygon (xPoints, yPoints, 4);
		return p;
    }


}
