/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A rectangle shape that looks like multiple rectangles are stacked on top of each other.
 *
 * @author Chris Callendar
 */
public class StackedRectangleNodeShape extends RectangleNodeShape {

	public static final String RECTANGLE_NODE_SHAPE = "Rectangle (Stack)";
	private static final int MAX_RECTANGLES = 4;

	private int rectangles = 4;
	private int spacing = 6;

	public StackedRectangleNodeShape(String name) {
		super(name);
	}

	public StackedRectangleNodeShape() {
		this(RECTANGLE_NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new StackedRectangleNodeShape();
		return shape;
	}

	private void updateRectangles(Rectangle2D bounds) {
		// set the number of rectangles [2-4]
		rectangles = Math.max(2, Math.min(MAX_RECTANGLES, (int)(bounds.getWidth() / 10f)));
		spacing = (int)(rectangles * 1.5);
	}

	/**
	 * @see NodeShape#getInnerBounds(Rectangle2D)
	 */
	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double x = outerBounds.getX() + 4;
		double y = outerBounds.getY() + 4;
		// TODO this sizing isn't quite right
		updateRectangles(outerBounds);
		int thickness = (rectangles - 1) * spacing;
		double w = outerBounds.getWidth() - 6 - thickness;
		double h = outerBounds.getHeight() - 6 - thickness;
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * @see NodeShape#getShape(Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		Rectangle2D shape = getUnion(getShapes(bounds));
		return shape;
	}

	private Rectangle2D getUnion(Rectangle2D[] rectangles) {
		if ((rectangles == null) || (rectangles.length == 0)) {
			return null;
		}
		Rectangle2D first = rectangles[0];
		for (int i = 1; i < rectangles.length; i++) {
			first = first.createUnion(rectangles[i]);
		}
		return first;
	}

	private Rectangle2D[] getShapes(Rectangle2D bounds) {
		updateRectangles(bounds);
		Rectangle2D[] rects = new Rectangle2D[rectangles];
		for (int i = 0; i < rectangles; i++) {
			int add = (i * spacing);
			rects[i] = new Rectangle2D.Double(bounds.getX() + add, bounds.getY() + add,
						bounds.getWidth() - spacing - 1, bounds.getHeight() - spacing - 1);
		}
		return rects;
	}

	/**
	 * @see NodeShape#render(Rectangle2D, Graphics2D, Paint, NodeBorder, NodeImage)
	 */
	public void render(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage) {
		Rectangle2D[] shapes = getShapes(outerBounds);
		for (int i = shapes.length - 1; i >= 0; i--) {
			if (nodeImage.isFillBackground()) {
				g2.setPaint(fillColor);
				g2.fill(shapes[i]);
			}
			// only render the image on the last iteration
			if ((i == 0) && (nodeImage.getImage() != null)) {
				renderImage(shapes[i], g2, nodeImage.getImage(), nodeImage.getImageSizing());
			}
			if (nodeImage.isDrawOuterBorder()) {
				if (i == 0) {
					border.setGraphics(g2);
				} else {
					g2.setPaint(Color.black);
					g2.setStroke(new BasicStroke(1f));
				}
				g2.draw(shapes[i]);
			}
		}
	}

}
