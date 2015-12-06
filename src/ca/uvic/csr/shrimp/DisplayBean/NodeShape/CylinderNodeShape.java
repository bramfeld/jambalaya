/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;


/**
 * A cylinder node shape - like you see in Visio to represent a database.
 *
 * @author Chris Callendar
 * @date 1-Dec-06
 */
public class CylinderNodeShape extends AbstractNodeShape {

	public static final String NODE_SHAPE = "Cylinder";

	// used for getting terminal attachment points
	private static final RectangleNodeShape RECT = new RectangleNodeShape();

	private boolean renderingTopBorder = false;
	private boolean renderingBottomBorder = false;

	public CylinderNodeShape() {
		this(NODE_SHAPE);
	}

	public CylinderNodeShape(String name) {
		super(name);
	}

	public Object clone() {
		CylinderNodeShape shape = new CylinderNodeShape(name);
		return shape;
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Double globalBounds, AffineTransform localToGlobalTx, java.awt.geom.Point2D.Double arcAttachPoint) {
		// TODO a rectangle won't work very well for the top and bottom of the cylinder
		return RECT.getTerminalAttachPoint(terminalPositionAngle, globalBounds, localToGlobalTx, arcAttachPoint);
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double x = outerBounds.getX();
		double y = outerBounds.getY();
		double w = outerBounds.getWidth();
		double h = outerBounds.getHeight();
		double ellipseHeight = Math.max(4d, h / 8);
		return new Rectangle2D.Double(x + 4, y + ellipseHeight + 4, w - 8, h - 2*ellipseHeight - 8);
	}

	protected void renderShape(Rectangle2D bounds, Graphics2D g2) {
		Shape shape = getShape(bounds);
		final Color c = g2.getColor();
		// fill the cylinder with a horizontal gradient
		GradientPaint paint = new GradientPaint((int)bounds.getX(), (int)bounds.getY(), c,
				(int)bounds.getX() + (int)bounds.getWidth(), (int)bounds.getY(), c.darker());
		g2.setPaint(paint);
		g2.fill(shape);
	}

	protected void renderBorder(boolean renderOuter, boolean renderInner, Rectangle2D bounds, Graphics2D g2, NodeBorder border) {
		// render the cylinder outline, including the complete top ellipse
		renderingTopBorder = true;
		super.renderBorder(renderOuter, renderInner, bounds, g2, border);
		renderingTopBorder = false;

		// paint the semi-transparent bottom arc (completes the bottom ellipse)
		renderingBottomBorder = true;
		Composite old = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
		super.renderBorder(renderOuter, renderInner, bounds, g2, border);
		g2.setComposite(old);	// restore old composite
		renderingBottomBorder = false;
	}

	public Shape getShape(Rectangle2D bounds) {
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth() - 1;
		float h = (float) bounds.getHeight() - 1;
		float ellipseHeight = Math.max(4f, h / 8);
		float topEllipseMidY = y + (ellipseHeight / 2);
		float bottomEllipseMidY = y + h - (ellipseHeight / 2);
		float midX = x + (w / 2);

		GeneralPath cylinder = new GeneralPath();
		if (renderingBottomBorder) {
			// draw only the top part of the bottom ellipse (it will be partially transparent)
			cylinder.moveTo(x, bottomEllipseMidY);
			cylinder.curveTo(x, bottomEllipseMidY, x, y + h - ellipseHeight, midX, y + h - ellipseHeight);
			cylinder.curveTo(midX, y + h - ellipseHeight, x + w, y + h - ellipseHeight, x + w, bottomEllipseMidY);
		} else {
			// draw the cylinder outline
			cylinder.moveTo(x, topEllipseMidY);
			cylinder.curveTo(x, topEllipseMidY, x, y, midX, y);
			cylinder.curveTo(midX, y, x + w, y, x + w, topEllipseMidY);
			cylinder.lineTo(x + w, bottomEllipseMidY);
			cylinder.curveTo(x + w, bottomEllipseMidY, x + w, y + h, midX, y + h);
			cylinder.curveTo(midX, y + h, x, y + h, x, bottomEllipseMidY);
			cylinder.lineTo(x, topEllipseMidY);

			// complete the top ellipse
			if (renderingTopBorder) {
				cylinder.curveTo(x, topEllipseMidY, x, y + ellipseHeight, midX, y + ellipseHeight);
				cylinder.curveTo(midX, y + ellipseHeight, x + w, y + ellipseHeight, x + w, topEllipseMidY);
			}
		}
		return cylinder;
	}


}
