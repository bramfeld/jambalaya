/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ObjectLifeLineNodeShape extends RectangleNodeShape {

	public static final String OBJECT_LIFE_LINE_NODE_SHAPE = "ObjectLifeLine";

	// Descender line width
	protected static final float STROKE_WIDTH = 20.0f;

	private float[] dash = { 30.0f, 60.0f };
	private Stroke basicStroke = new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, dash, 0);

	public ObjectLifeLineNodeShape(String name) {
		super(name);
	}

	public ObjectLifeLineNodeShape() {
		this(OBJECT_LIFE_LINE_NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new ObjectLifeLineNodeShape();
		return shape;
	}

	/**
	 * @see NodeShape#getShape(Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		return bounds;
	}

	/**
	 * Draws a rectangle with a descending line
	 * @see NodeShape#render(Rectangle2D, Graphics2D, Paint, NodeBorder, NodeImage)
	 */
	public void render(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage) {
		super.render(outerBounds, g2, fillColor, border, nodeImage);
		g2.setStroke(basicStroke);
		double startX = super.getInnerBounds(outerBounds).getCenterX();
		double startY = super.getInnerBounds(outerBounds).getMaxY();
		double endY = g2.getClipBounds().getMaxY();
		Line2D line = new Line2D.Double(new Point2D.Double(startX, startY), new Point2D.Double(startX, endY));
		g2.setPaint(fillColor);
		g2.draw(line);
	}

	/**
	 * @see NodeShape#isUserSelectable()
	 * @return false - don't want the user to be able to select this shape for a node type.
	 */
	public boolean isUserSelectable() {
		// @tag Shrimp.NodeStyle.ObjectLifeLife : do we want to display this type?
		return false;
	}

	/**
	 * @see NodeShape#drawContentBorder()
	 * @returns false
	 */
	public boolean drawContentBorder() {
		return false;
	}

}