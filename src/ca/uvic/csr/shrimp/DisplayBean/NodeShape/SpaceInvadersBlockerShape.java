/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

/**
 *
 * @tag Shrimp(SpaceInvaders)
 * @author Chris Callendar
 */
public class SpaceInvadersBlockerShape extends SpaceInvadersMainShape {

	public static final String NODE_SHAPE = "SpaceInvadersBlockerShape";
	private static final Color COLOR = new Color(128, 128, 128);

	public SpaceInvadersBlockerShape() {
		super(NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new SpaceInvadersBlockerShape();
		return shape;
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		double w = (int) outerBounds.getWidth();
		double h = (int) outerBounds.getHeight();
		double shift = (h / 6);
		h = h - shift;
		double x = (int) outerBounds.getX();
		double y = (int) outerBounds.getY() + shift;
		h = h - shift;
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * @see AbstractNodeShape#renderShape(Rectangle2D, Graphics2D)
	 */
	protected void renderShape(Rectangle2D outerBounds, Graphics2D g2) {
		g2.setPaint(COLOR);
		Shape shape = getShape(getInnerBounds(outerBounds));
		g2.fill(shape);
	}

	public Shape getShape(Rectangle2D bounds) {
		/*
		 *  Start  Width = 9 * w, Height = 6 * h
		 *  x
		 *       __________
		 *     _|          |_
		 *   _|              |_
		 *  |                  |
		 *  |        __        |
		 *  |      _|  |_      |
		 *  |_____|      |_____|
		 *  s
		 */
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) (bounds.getWidth() / 9);
		int h = (int) (bounds.getHeight() / 6);
		GeneralPath path = new GeneralPath();
		path.moveTo(x, y + 2*h);
		path.lineTo(x + w, y + 2*h);
		path.lineTo(x + w, y + h);
		path.lineTo(x + 2*w, y + h);
		path.lineTo(x + 2*w, y);
		path.lineTo(x + 7*w, y);
		path.lineTo(x + 7*w, y + h);
		path.lineTo(x + 8*w, y + h);
		path.lineTo(x + 8*w, y + 2*h);
		path.lineTo(x + 9*w, y + 2*h);
		path.lineTo(x + 9*w, y + 6*h);
		path.lineTo(x + 6*w, y + 6*h);
		path.lineTo(x + 6*w, y + 5*h);
		path.lineTo(x + 3*w, y + 5*h);
		path.lineTo(x + 3*w, y + 6*h);
		path.lineTo(x, y + 6*h);
		path.lineTo(x, y + 2*h);
		return path;
	}

}
