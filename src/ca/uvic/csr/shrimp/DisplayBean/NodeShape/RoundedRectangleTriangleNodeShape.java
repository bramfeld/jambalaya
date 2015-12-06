/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

/**
 * Renders a {@link RectangleNodeShape} with a small {@link TriangleNodeShape} drawn
 * in the bottom middle of the node.
 *
 * @author Chris Callendar
 */
public class RoundedRectangleTriangleNodeShape extends RoundedRectangleNodeShape {

	public static final String NODE_SHAPE = "Rounded Rectangle (Triangle Overlay)";

	private final TriangleNodeShape triangle = new TriangleNodeShape();

	public RoundedRectangleTriangleNodeShape() {
		this(NODE_SHAPE);
	}

	public RoundedRectangleTriangleNodeShape(String name) {
		super(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new RoundedRectangleTriangleNodeShape();
		return shape;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getInnerBounds(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		Rectangle2D.Double rect = super.getInnerBounds(outerBounds);
		double space = outerBounds.getWidth() / 24;
		rect.setFrame(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight() - space);
		return rect;
	}

	protected Rectangle2D adjustHeight(Rectangle2D bounds) {
		double space = bounds.getWidth() / 10;
		return new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight() - space);
	}

	protected void renderShape(Rectangle2D outerBounds, Graphics2D g2) {
		super.renderShape(adjustHeight(outerBounds), g2);
	}

	protected void renderBorder(boolean renderInnerBorder, boolean renderOuterBorder,
			Rectangle2D outerBounds, Graphics2D g2, NodeBorder border) {
		super.renderBorder(renderInnerBorder, renderOuterBorder, adjustHeight(outerBounds), g2, border);
	}

	protected void renderImage(Rectangle2D outerBounds, Graphics2D g2, Image image, String imageSizingMode) {
		super.renderImage(adjustHeight(outerBounds), g2, image, imageSizingMode);
	}

	protected void renderOverlay(Rectangle2D bounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage) {
		double size = bounds.getWidth() / 4;
		double x = Math.max(0, bounds.getCenterX() - (size / 2));
		double y = Math.max(0, bounds.getMaxY() - size);
		// Render the small TriangleNodeShape at the bottom of the bounds
		Rectangle2D triangleBounds = new Rectangle2D.Double(x, y, size, size);
		Paint triangleColor = fillColor;
		if (fillColor instanceof Color) {
			triangleColor = ((Color)fillColor).darker(); //GraphicsUtils.invertColor(fillColor);
		}
		NodeBorder triangleBorder = new NodeBorder(border.getOuterColor());
		triangle.render(triangleBounds, g2, triangleColor, triangleBorder, nodeImage);
	}

}