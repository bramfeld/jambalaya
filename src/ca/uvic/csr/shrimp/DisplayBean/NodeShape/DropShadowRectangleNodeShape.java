/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * A {@link RectangleNodeShape} that has a drop border.  The thickness of the border
 * depends on how big the shape is.
 *
 * @author Chris Callendar
 */
public class DropShadowRectangleNodeShape extends RectangleNodeShape {

	public static final String NAME = "Rectangle (Drop Shadow)";
	private static final int MAX_THICKNESS = 12;

	private int thickness = 2;

	public DropShadowRectangleNodeShape() {
		this(NAME);
	}

	public DropShadowRectangleNodeShape(String name) {
		super(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.AbstractNodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new DropShadowRectangleNodeShape(name);
		return shape;
	}

	private void updateThickness(Rectangle2D bounds) {
		this.thickness = Math.max(2, Math.min(MAX_THICKNESS, (int)Math.ceil(bounds.getWidth() / 10)));
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape#getInnerBounds(java.awt.geom.Rectangle2D)
	 */
	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double x = outerBounds.getX() + 4;
		double y = outerBounds.getY() + 4;
		updateThickness(outerBounds);
		double w = outerBounds.getWidth() - 8 - thickness;
		double h = outerBounds.getHeight() - 8 - thickness;
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * @see NodeShape#getShape(Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		updateThickness(bounds);
		Rectangle2D shapeBounds = new Rectangle2D.Double(bounds.getX() + 1, bounds.getY() + 1,
				bounds.getWidth() - thickness, bounds.getHeight() - thickness);
		return shapeBounds;
	}

	/**
	 * @see NodeShape#render(Rectangle2D, Graphics2D, Paint, NodeBorder, NodeImage)
	 */
	public void render(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor,
			NodeBorder border, NodeImage nodeImage) {

		Shape shape = getShape(outerBounds);
		Rectangle2D rect = new Rectangle2D.Double(outerBounds.getX() + thickness, outerBounds.getY() + thickness,
				outerBounds.getWidth() - thickness, outerBounds.getHeight() - thickness);

		// what if the original composite is partially transparent?  Then the border will be the wrong transparency
		Composite composite = g2.getComposite();
		float maxTransparency = 1f;
		if (composite instanceof AlphaComposite) {
			maxTransparency = ((AlphaComposite)composite).getAlpha();
		}
		float incr = Math.max(0f, maxTransparency - 0.2f) / thickness;
		float alpha = 0.2f;

		for (int i = thickness - 1; i >= 0; i--) {
			if (i == 0) {
				// use the existing composite for this
				g2.setComposite(composite);
				if (nodeImage.isFillBackground()) {
					g2.setPaint(fillColor);
					g2.fill(shape);
				}
				Image image = nodeImage.getImage();
				if (image != null) {
					renderImage(outerBounds, g2, image, nodeImage.getImageSizing());
				}
				if (nodeImage.isDrawOuterBorder()) {
					border.setGraphics(g2);
					g2.draw(rect);
				}
			} else if (incr > 0f) {
				if (nodeImage.isDrawOuterBorder()) {
					// use a semi-transparent dark gray border with a thickness of 1
					alpha = Math.min(maxTransparency, alpha + incr);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
					g2.setPaint(Color.darkGray);
					g2.setStroke(new BasicStroke(1f));
					// draw right side
					g2.drawLine((int)rect.getMaxX(), (int)rect.getMinY(), (int)rect.getMaxX(), (int)rect.getMaxY());
					// draw bottom
					g2.drawLine((int)rect.getMinX(), (int)rect.getMaxY(), (int)rect.getMaxX(), (int)rect.getMaxY());
				}
			}
			rect.setRect(rect.getX() - 1, rect.getY() - 1, rect.getWidth(), rect.getHeight());
		}
	}

}
