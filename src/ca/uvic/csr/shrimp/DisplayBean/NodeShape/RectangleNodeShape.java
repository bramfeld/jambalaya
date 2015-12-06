/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;

/**
 * @author Rob Lintern
 */
public class RectangleNodeShape extends AbstractNodeShape {

	public static final String NAME = "Rectangle";
	protected static final double BORDER_WIDTH = 20;

	public RectangleNodeShape() {
		this(NAME);
	}

	public RectangleNodeShape(String name) {
		super(name);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new RectangleNodeShape();
		return shape;
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Rectangle2D.Double globalBounds, AffineTransform localToGlobalTx, Point2D.Double arcAttachPoint) {
		double terminalTheta = 0.0;
		double nw = globalBounds.getWidth(); // node width
		double nh = globalBounds.getHeight(); // node height
		double nhalfw = nw/2.0; // half node width
		double nhalfh = nh/2.0; // half node height
		double tanTheta = (terminalPositionAngle == 0.0) ? 0.0 : Math.tan(terminalPositionAngle);

		/*
		 * Firstly, see which segments of the side intersects a line with
		 * an angle of "theta", originating at the centre of the node.
		 * Secondly, see where exactly on the side this intersection happens.
		 * Note: The y axis is inverted like screen coordinates, and theta is between -PI and +PI
		 *
		 * <pre>
		 * 						 -Y
		 *
		 *                         |
		 *                         | -PI/2
		 *                         |
		 *                         |
		 *                         |
		 *                | nhalfw | nhalfw |
		 *                |________|________| ___
		 *                |        |        |
		 * 	              |        |        |
		 *                |        |        |
		 *                |        |        |
		 *                |        |        |  nhalfh
		 *                |        |        |
		 *       -PI      |        |        |              - 0
		 * -X  ___________|________|(0,0)___|__________________   +X
		 *                |        |        |
		 *       +PI      |        |        |              + 0
		 *                |        |        |
		 *                |        |        |
		 *                |        |        |  nhalfh
		 *                |        |        |
		 *                |        |        |
		 *                |________|________| ___
		 *                         |
		 *                         |
		 *                         |
		 *                         | +PI/2
		 *
		 *                       +Y
		 *
		 * </pre>
		 */
		double alpha = Math.atan2(nh, nw); // angle in radians between horizontal line through centre of node and bottom right corner
		double beta = DisplayConstants.HALF_PI - alpha; // in radians
		double x = 0.0;
		double y = 0.0;
		double tmpAngle = alpha + 2.0*beta;
		if (terminalPositionAngle == 0.0 || terminalPositionAngle == -0.0) {
			// intersects middle of right side
			x = nhalfw;
			y = 0.0;
		} else if (terminalPositionAngle == DisplayConstants.PI || terminalPositionAngle == DisplayConstants.NEG_PI) {
			// intersects middle of left side
			x = -nhalfw;
			y = 0.0;
		} else if (terminalPositionAngle == DisplayConstants.HALF_PI) {
			// intersects middle of bottom side
			x = 0.0;
			y = nhalfh;
		} else if (terminalPositionAngle == DisplayConstants.NEG_HALF_PI) {
			// intersects middle of top side
			x = 0.0;
			y = -nhalfh;
		} else if (terminalPositionAngle <= tmpAngle && terminalPositionAngle >= alpha) {
			// intersects with bottom of node
			y = nhalfh;
			x = y / tanTheta;
			terminalTheta = DisplayConstants.HALF_PI;
		} else if (terminalPositionAngle <= -alpha && terminalPositionAngle >= -tmpAngle) {
			// intersects with top side of node
			y = -nhalfh;
			x = y / tanTheta;
			terminalTheta = DisplayConstants.NEG_HALF_PI;
		} else if ((terminalPositionAngle >= -DisplayConstants.PI && terminalPositionAngle <= -tmpAngle) || (terminalPositionAngle <= DisplayConstants.PI && terminalPositionAngle >= tmpAngle)) {
			// intersects with left side of node
			x = -nhalfw;
			y = x * tanTheta;
			terminalTheta = DisplayConstants.PI;
		} else {
			// intersects with right side of node
			x = nhalfw;
			y = x * tanTheta;
			terminalTheta = 0.0;
		}
		arcAttachPoint.setLocation(x, y);
		return terminalTheta;
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getInnerBounds(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double x = outerBounds.getX() + 4;
		double y = outerBounds.getY() + 4;
		double w = outerBounds.getWidth() - 8;
		double h = outerBounds.getHeight() - 8;
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getShape(java.awt.geom.Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		return bounds;
	}

	/**
	 * Draws the inner border if there is no image.
	 */
	public boolean drawContentBorder() {
		return true;
	}

}
