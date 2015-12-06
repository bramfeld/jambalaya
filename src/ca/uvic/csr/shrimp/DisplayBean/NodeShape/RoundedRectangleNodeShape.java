/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.util.GeometryUtils;

/**
 * A rectangle node shape with round corners.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class RoundedRectangleNodeShape extends AbstractNodeShape {

	public static final String NAME = "Rounded Rectangle";

	private double scale = 1;

	public RoundedRectangleNodeShape(String name) {
		super(name);
	}

	public RoundedRectangleNodeShape() {
		this(NAME);
	}

	/**
	 * Draws the inner border if there is no image.
	 */
	public boolean drawContentBorder() {
		return true;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new RoundedRectangleNodeShape();
		return shape;
	}

	protected double getScale() {
		return scale;
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		RoundRectangle2D shape = (RoundRectangle2D) getShape(outerBounds);
		Rectangle2D bounds = shape.getBounds2D();
		double arcH = (shape.getArcHeight() * getScale()) / 4d;
		double insetW = 1; //arcW/Math.sqrt(2.0d);
		double insetH = arcH * 2; //arcH/Math.sqrt(2.0d);
		double x = bounds.getX() + insetW;
		double y = bounds.getY() + insetH;
		double w = bounds.getWidth() - 2.0d*insetW;
		double h = bounds.getHeight() - 2.0d*insetH;
		return new Rectangle2D.Double(x, y, w, h);
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Rectangle2D.Double globalBounds, AffineTransform localToGlobalTx, Point2D.Double arcAttachPoint) {
		double terminalTheta = 0.0;
		RoundRectangle2D shape = (RoundRectangle2D) getShape(globalBounds);
		double nw = globalBounds.getWidth(); // node width
		double nh = globalBounds.getHeight(); // node height
		double nhalfw = nw/2.0d; // half node width
		double nhalfh = nh/2.0d; // half node height
		double tanTheta = Math.tan(terminalPositionAngle);

		double r = (shape.getArcWidth() * getScale()) / 2d; // * localToGlobalTx.getScaleX(); // corner radius, in global coordinates
		double segA = nhalfh - r;
		double segB = nhalfw - r;

		/*
		 * Idea: A rounded rectangle node has four line and four arc seqments.
		 * Firstly, see which segments of the side intersects a line with
		 * an angle of "theta", originating at the centre of the node.
		 * Secondly, see where exactly on the side this intersection happens.
		 * Note: The y axis is inverted like screen coordinates, and theta is between -PI and +PI
		 *
		 * 						 -Y
		 *
		 *                         |
		 *                         | -PI/2
		 *                         |
		 *                         |
		 *                   |     |     |  |
		 *                   |segB |segB |r |
		 *                   |_____|_____|__|_________
		 *                   /     |     \  |  r
		 * 	               /       |       \|_________
		 *                |        |        |
		 *                |        |        |
		 *                |        |        |  segA
		 *                |        |        |
		 *       -PI      |        |        |              - 0
		 * -X  ___________|________|________|__________________   +X
		 *                |        |        |
		 *       +PI      |        |        |              + 0
		 *                |        |        |  segA
		 *                |        |        |
		 *                |        |        |
		 *                |        |        |_________
		 *                 \       |       /
		 *                   \_____|_____/
		 *                         |
		 *                         |
		 *                         |
		 *                         | +PI/2
		 *
		 *                       +Y
		 */
		double alpha = Math.atan2(segA, nhalfw); // angle in radians between line from centre of node to lower right end of segA and x axis
		double beta = Math.atan2(segB, nhalfh); // angle in radians between line from centre of node to lower right end of segB and y axis
		double gamma = DisplayConstants.HALF_PI - alpha - beta; // remainder, ie. the angle between lines from center of node to endpoints of the rounded corner arc
		double x = 0.0d;
		double y = 0.0d;
		if (Math.abs(terminalPositionAngle) <= alpha) {
			// right side
			x = nhalfw;
			y = x * tanTheta;
			terminalTheta = 0.0;
		} else if (Math.abs(terminalPositionAngle) <= (alpha + gamma)) {
			// upper or lower right corner
			Point2D [] result = new Point2D [2];
			if (terminalPositionAngle < 0) {
				// upper right corner
				GeometryUtils.circleLineIntersection(terminalPositionAngle, r, segB, -segA, result);
				x = result[0].getX();
				y = result[0].getY();
				terminalTheta = terminalPositionAngle;
			} else {
				// lower right corner
				GeometryUtils.circleLineIntersection(terminalPositionAngle, r, segB, segA, result);
				x = result[0].getX();
				y = result[0].getY();
				terminalTheta = terminalPositionAngle;
			}
		} else if (Math.abs(terminalPositionAngle) <= (alpha + gamma + 2.0d*beta)) {
			// top or bottom
			y = (terminalPositionAngle < 0) ? -nhalfh : nhalfh;
			x = y / tanTheta;
			terminalTheta = (terminalPositionAngle < 0) ? DisplayConstants.NEG_HALF_PI : DisplayConstants.HALF_PI;
		} else if (Math.abs(terminalPositionAngle) <= (alpha + gamma + 2.0d*beta + gamma)) {
			// upper or lower left corner
			Point2D [] result = new Point2D [2];
			if (terminalPositionAngle < 0) {
				// upper left corner
				GeometryUtils.circleLineIntersection(terminalPositionAngle, r, -segB, -segA, result);
				x = result[1].getX();
				y = result[1].getY();
				terminalTheta = terminalPositionAngle;
			} else {
				// lower left corner
				GeometryUtils.circleLineIntersection(terminalPositionAngle, r, -segB, segA, result);
				x = result[1].getX();
				y = result[1].getY();
				terminalTheta = terminalPositionAngle;
			}
		} else {
			// left side
			x = -nhalfw;
			y = x * tanTheta;
			terminalTheta = DisplayConstants.PI;
		}
		arcAttachPoint.setLocation(x,y);
		return terminalTheta;
	}

	protected double getRadius(Rectangle2D bounds) {
		double size = Math.min(bounds.getWidth(), bounds.getHeight());
		double radius;
		if (size >= 25) {
			radius = Math.min(Math.max(size / 25, 0), 50);
		} else {
			// this is for the thumbnail
			radius = Math.min(size, 5);
		}
		// adjust the radius for small nodes (otherwise the corners are square)
		double sc = getScale();
		if (sc < 0.04) {
			radius *= 8;
		} else if (sc < 0.06) {
			radius *= 6;
		} else if (sc < 0.1) {
			radius *= 4;
		} else if (sc < 0.2) {
			radius *= 2.5;
		} else if (sc < 0.4) {
			radius *= 1.5;
		} else if (sc > 1) {
			radius /= sc;
		}
		return radius;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getShape(java.awt.geom.Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		double radius = getRadius(bounds);
		RoundRectangle2D shape = new RoundRectangle2D.Double(bounds.getX(), bounds.getY(),
									bounds.getWidth(), bounds.getHeight(), radius, radius);

		return shape;
	}

	public Shape getShape(Rectangle2D bounds, Graphics2D g2) {
		// save the scale - it is used to calculate the arc width and height
		if (g2.getTransform() != null) {
			this.scale = Math.min(g2.getTransform().getScaleX(), g2.getTransform().getScaleY());
		} else {
			this.scale = 1;
		}
		return super.getShape(bounds, g2);
	}


}
