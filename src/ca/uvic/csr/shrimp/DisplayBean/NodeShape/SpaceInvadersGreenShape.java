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
import java.awt.geom.Rectangle2D.Double;

/**
 *
 * @tag Shrimp(SpaceInvaders)
 * @author Chris Callendar
 */
public class SpaceInvadersGreenShape extends SpaceInvadersMainShape {

	public static final String NODE_SHAPE = "SpaceInvadersGreenShape";
	private static final Color COLOR = new Color(75, 150, 75);

	public SpaceInvadersGreenShape() {
		super(NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new SpaceInvadersGreenShape();
		return shape;
	}

	public Double getInnerBounds(Rectangle2D outerBounds) {
		return RECT.getInnerBounds(outerBounds);
	}

	/**
	 * @see AbstractNodeShape#renderShape(Rectangle2D, Graphics2D)
	 */
	protected void renderShape(Rectangle2D outerBounds, Graphics2D g2) {
		g2.setPaint(COLOR);
		Shape shape = getShape(outerBounds);
		g2.fill(shape);

		// fill the eyes
		g2.setPaint(Color.white);
		int x = (int) outerBounds.getX();
		int y = (int) outerBounds.getY();
		int w = (int) (outerBounds.getWidth() / 10);
		int h = (int) (outerBounds.getHeight() / 8);
		g2.fillRect(x + 3*w, y + 3*h, w, h);
		g2.fillRect(x + 6*w, y + 3*h, w, h);
	}

	public Shape getShape(Rectangle2D bounds) {
		/*
		 *  Start  Width = 10 * w, Height = 8 * h
		 *  x   _        _
		 *     |_|_    _|_|
		 *      _| |__| |_
		 *    _|  _    _  |_
		 *  _|   |_|  |_|   |_
		 * |  _            _  |
		 * | | |  ______  | | |
		 * |_| |_|_    _|_| |_|
		 *       |_|  |_|
		 *
		 */
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) (bounds.getWidth() / 10);
		int h = (int) (bounds.getHeight() / 8);
		GeneralPath path = new GeneralPath();
		path.moveTo(x + 2*w, y + 2*h);
		// main rectangle
		path.append(new Rectangle2D.Double(x + 2*w, y + 2*h, 6*w, 4*h), false);
		// ears
		path.append(new Rectangle2D.Double(x + 2*w, y, w, h), false);
		path.append(new Rectangle2D.Double(x + 3*w, y + h, w, h), false);
		path.append(new Rectangle2D.Double(x + 6*w, y + h, w, h), false);
		path.append(new Rectangle2D.Double(x + 7*w, y, w, h), false);
		// arms
		path.append(new Rectangle2D.Double(x, y + 4*h, w, 3*h), false);
		path.append(new Rectangle2D.Double(x + w, y + 3*h, w, 2*h), false);
		path.append(new Rectangle2D.Double(x + 8*w, y + 3*h, w, 2*h), false);
		path.append(new Rectangle2D.Double(x + 9*w, y + 4*h, w, 3*h), false);
		// legs
		path.append(new Rectangle2D.Double(x + 2*w, y + 6*h, w, h), false);
		path.append(new Rectangle2D.Double(x + 7*w, y + 6*h, w, h), false);
		path.append(new Rectangle2D.Double(x + 3*w, y + 7*h, w, h), false);
		path.append(new Rectangle2D.Double(x + 6*w, y + 7*h, w, h), false);
		return path;
	}

}
