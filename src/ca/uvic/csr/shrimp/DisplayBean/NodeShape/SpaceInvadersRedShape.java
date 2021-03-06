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
public class SpaceInvadersRedShape extends SpaceInvadersMainShape {

	public static final String NODE_SHAPE = "SpaceInvadersRedShape";
	private static final Color COLOR = new Color(195, 75, 75);

	public SpaceInvadersRedShape() {
		super(NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new SpaceInvadersRedShape();
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
		int w = (int) (outerBounds.getWidth() / 7);
		int h = (int) (outerBounds.getHeight() / 8);
		g2.fillRect(x + 2*w, y + 3*h, w, h);
		g2.fillRect(x + 4*w, y + 3*h, w, h);
	}

	public Shape getShape(Rectangle2D bounds) {
		/*
		 *  Start  Width = 7 * w, Height = 8 * h
		 *  x    _____
		 *     _|     |_
		 *   _|         |_
		 *  |    _   _    |
		 *  |   |_| |_|   |
		 *  |___       ___|
		 *     _|_   _|_
		 *   _|_| |_| |_|_
		 *  |_|         |_|
		 *
		 */
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) (bounds.getWidth() / 7);
		int h = (int) (bounds.getHeight() / 8);
		GeneralPath path = new GeneralPath();
		path.moveTo(x, y + 2*h);
		path.lineTo(x + w, y + 2*h);
		path.lineTo(x + w, y + h);
		path.lineTo(x + 2*w, y + h);
		path.lineTo(x + 2*w, y);
		path.lineTo(x + 5*w, y);
		path.lineTo(x + 5*w, y + h);
		path.lineTo(x + 6*w, y + h);
		path.lineTo(x + 6*w, y + 2*h);
		path.lineTo(x + 7*w, y + 2*h);
		path.lineTo(x + 7*w, y + 5*h);
		path.lineTo(x + 5*w, y + 5*h);
		path.lineTo(x + 5*w, y + 6*h);
		path.lineTo(x + 6*w, y + 6*h);
		path.lineTo(x + 6*w, y + 7*h);
		path.lineTo(x + 7*w, y + 7*h);
		path.lineTo(x + 7*w, y + 8*h);
		path.lineTo(x + 6*w, y + 8*h);
		path.lineTo(x + 6*w, y + 7*h);
		path.lineTo(x + 5*w, y + 7*h);
		path.lineTo(x + 5*w, y + 6*h);
		path.lineTo(x + 4*w, y + 6*h);
		path.lineTo(x + 4*w, y + 7*h);
		path.lineTo(x + 3*w, y + 7*h);
		path.lineTo(x + 3*w, y + 6*h);
		path.lineTo(x + 2*w, y + 6*h);
		path.lineTo(x + 2*w, y + 7*h);
		path.lineTo(x + w, y + 7*h);
		path.lineTo(x + w, y + 8*h);
		path.lineTo(x, y + 8*h);
		path.lineTo(x, y + 7*h);
		path.lineTo(x + w, y + 7*h);
		path.lineTo(x + w, y + 6*h);
		path.lineTo(x + 2*w, y + 6*h);
		path.lineTo(x + 2*w, y + 5*h);
		path.lineTo(x, y + 5*h);
		path.lineTo(x, y + 2*h);
		//path.closePath();
		return path;
	}

}
