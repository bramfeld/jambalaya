/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


/**
 * A {@link NodeShape} that looks like a file.
 *
 * @author Chris Callendar
 * @date 1-Dec-06
 */
public class FileNodeShape extends RectangleNodeShape {

	public static final String NODE_SHAPE = "File";

	private static final Color START_COLOR = Color.white;
	protected static final Color END_COLOR = new Color(171, 212, 251);
	protected static final Color BORDER = new Color(82, 97, 123); 	// Color.blue;

	public FileNodeShape() {
		this(NODE_SHAPE);
	}

	public FileNodeShape(String name) {
		super(name);
	}

	public Object clone() {
		FileNodeShape shape = new FileNodeShape(name);
		return shape;
	}

	public boolean drawContentBorder() {
		return false;
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		int x = (int) outerBounds.getX();
		int y = (int) outerBounds.getY();
		int w = (int) outerBounds.getWidth();
		int h = (int) outerBounds.getHeight();
		float corner = w / 5;
		return new Rectangle2D.Double(x + 4, y + corner + 4, w - 8, h - corner - 8);
	}

	protected void renderShape(Rectangle2D bounds, Graphics2D g2) {
		Shape shape = getShape(bounds);
		// fill the file shape
		GradientPaint paint = new GradientPaint((int)bounds.getX(), (int)bounds.getY(), START_COLOR,
				(int)bounds.getX(), (int)bounds.getMaxY(), END_COLOR);
		g2.setPaint(paint);
		g2.fill(shape);
	}

	protected void renderBorder(Rectangle2D bounds, Graphics2D g2, float strokeWidth) {
		Shape shape = getShape(bounds);
		//g2.setColor(BORDER);
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.draw(shape);
	}

	public Shape getShape(Rectangle2D bounds) {
		return getFileShape(bounds);
	}

	public static Shape getFileShape(Rectangle2D bounds) {
		int x = (int) bounds.getX();
		int y = (int) bounds.getY();
		int w = (int) bounds.getWidth() - 1;
		int h = (int) bounds.getHeight() - 1;
		float corner = w / 5;

		/* (x,y)_____* <- start
		 *     |     |\
		 *     |     |_\
		 *     |        |
		 *     |        |
		 *     |        |
		 *     |________|
		 */

		// counter-clockwise, do the folded corner last
		GeneralPath path = new GeneralPath();
		path.moveTo(x + w - corner, y);
		path.lineTo(x, y);
		path.lineTo(x, y + h);
		path.lineTo(x + w, y + h);
		path.lineTo(x + w, y + corner);
		path.lineTo(x + w - corner, y + corner);
		path.lineTo(x + w - corner, y);
		path.lineTo(x + w, y + corner);
		return path;
	}

}
