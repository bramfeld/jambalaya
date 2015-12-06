/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.geom.Rectangle2D;

/**
 * Renders a {@link RectangleNodeShape} with a small {@link TriangleNodeShape} drawn
 * in the bottom middle of the node.
 *
 * @author Chris Callendar
 */
public class RectangleTriangleNodeShape extends RoundedRectangleTriangleNodeShape {

	public static final String NODE_SHAPE = "Rectangle (Triangle Overlay)";

	public RectangleTriangleNodeShape() {
		this(NODE_SHAPE);
	}

	public RectangleTriangleNodeShape(String name) {
		super(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new RectangleTriangleNodeShape();
		return shape;
	}

	protected double getScale() {
		return 1;
	}

	protected double getRadius(Rectangle2D bounds) {
		return 0;	// always a rectangle
	}

}