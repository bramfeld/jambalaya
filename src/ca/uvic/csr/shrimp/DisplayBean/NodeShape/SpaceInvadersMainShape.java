/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

/**
 *
 * @tag Shrimp(SpaceInvaders)
 * @author Chris Callendar
 */
public class SpaceInvadersMainShape extends AbstractNodeShape {

	public static final String NODE_SHAPE = "SpaceInvadersMainShape";
	protected static final Color COLOR = new Color(38, 166, 38);

	protected static final RectangleNodeShape RECT = new RectangleNodeShape();

	protected SpaceInvadersMainShape(String name) {
		super(name);

		// don't draw a border
		NodeImage nodeImage = new NodeImage();
		nodeImage.setDrawOuterBorder(false);
		setCustomRendering(nodeImage);
	}

	public SpaceInvadersMainShape() {
		this(NODE_SHAPE);
	}

	/**
	 * @see NodeShape#clone()
	 */
	public Object clone() {
		NodeShape shape = new SpaceInvadersMainShape();
		return shape;
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Double globalBounds, AffineTransform localToGlobalTx, java.awt.geom.Point2D.Double arcAttachPoint) {
		return RECT.getTerminalAttachPoint(terminalPositionAngle, globalBounds, localToGlobalTx, arcAttachPoint);
	}

	/**
	 * @see AbstractNodeShape#renderShape(Rectangle2D, Graphics2D)
	 */
	protected void renderShape(Rectangle2D outerBounds, Graphics2D g2) {
		g2.setPaint(COLOR);
		g2.fill(getShape(getInnerBounds(outerBounds)));
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
	 * @see NodeShape#getShape(Rectangle2D)
	 */
	public Shape getShape(Rectangle2D bounds) {
		/* WIDTH = 9 * w,  HEIGHT = 6 * h
		 *
		 *                8 _ 9
		 *              6__| |__11
		 *              |  7 10 |
		 *     4________|       |_______13
		 *  2__|        5      12       |__15
		 *  |  3                        14 |
		 *  |                              |
		 *  |                              |
		 *  |______________________________|
		 *  1                              0
		 */
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) bounds.getWidth();
		int h = (int) bounds.getHeight();
		int halfHeight = h / 2;
		int sixthHeight = h / 6;
		int ninethWidth = w / 9;

		int[] xp = new int[16];
		int[] yp = new int[16];
		xp[0] = x + w;
		yp[0] = y + h;
		xp[1] = x;
		yp[1] = yp[0];

		xp[2] = x;
		yp[2] = y + halfHeight;
		xp[3] = x + ninethWidth;
		yp[3] = yp[2];

		xp[4] = xp[3];
		yp[4] = yp[2] - sixthHeight;
		xp[5] = xp[4] + (2 * ninethWidth);
		yp[5] = yp[4];

		xp[6] = xp[5];
		yp[6] = y + sixthHeight;
		xp[7] = xp[6] + ninethWidth;
		yp[7] = yp[6];

		xp[8] = xp[7];
		yp[8] = y;
		xp[9] = xp[8] + ninethWidth;
		yp[9] = yp[8];

		xp[10] = xp[9];
		yp[10] = yp[7];
		xp[11] = xp[10] + ninethWidth;
		yp[11] = yp[10];

		xp[12] = xp[11];
		yp[12] = yp[5];
		xp[13] = xp[12] + (2 * ninethWidth);
		yp[13] = yp[12];

		xp[14] = xp[13];
		yp[14] = yp[3];
		xp[15] = xp[0];
		yp[15] = yp[14];

		Polygon poly = new Polygon(xp, yp, xp.length);
		return poly;
	}

}
