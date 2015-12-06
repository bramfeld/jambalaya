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
 * A {@link NodeShape} that looks like a file folder.
 *
 * @author Chris Callendar
 * @date 1-Dec-06
 */
public class FolderNodeShape extends RectangleNodeShape {

	public static final String NODE_SHAPE = "Folder";

	private static final Color START_COLOR = new Color(255, 255, 183);
	private static final Color END_COLOR = new Color(255, 209, 95);
	private static final Color BORDER_COLOR = new Color(165, 115, 13);

	public FolderNodeShape(String name) {
		super(name);
	}


	public FolderNodeShape() {
		this(NODE_SHAPE);
	}

	public Object clone() {
		FolderNodeShape shape = new FolderNodeShape(name);
		return shape;
	}

	public boolean drawContentBorder() {
		return false;
	}

	public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds) {
		float h = (float) outerBounds.getHeight();
		float tab = h / 10;
		float quarterTab = tab / 4;
		Rectangle2D inner = getFrontPath(outerBounds).getBounds2D();
		return new Rectangle2D.Double(inner.getX() + 4, inner.getY() + quarterTab, inner.getWidth() - 8, inner.getHeight() - quarterTab - 4);
	}

	protected void renderShape(Rectangle2D bounds, Graphics2D g2) {
		Shape shape = getShape(bounds);
		// fill the folder with a gradient like in windows explorer
		GradientPaint paint = new GradientPaint((int)bounds.getX(), (int)bounds.getY(), START_COLOR,
				(int)bounds.getX(), (int)bounds.getMaxY(), END_COLOR);
		g2.setPaint(paint);
		g2.fill(shape);
	}

	protected void renderBorder(Rectangle2D bounds, Graphics2D g2, float strokeWidth) {
		Shape shape = getShape(bounds);
		// draw the border of the folder
		g2.setColor(BORDER_COLOR);
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.draw(shape);
	}

	public Shape getShape(Rectangle2D bounds) {
		// combine the front path with the back tab path shapes
		GeneralPath path = getTabPath(bounds);
		path.append(getFrontPath(bounds), false);
		return path;
	}

	/**
	 * Gets a {@link GeneralPath} {@link Shape} which represents the rounded-top
	 * rectangle of the front part of the folder shape.
	 */
	protected GeneralPath getFrontPath(Rectangle2D bounds) {
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth() - 1;
		float h = (float) bounds.getHeight() - 1;
		float tab = Math.max(4, h / 10);
		float quarterTab = tab / 4;

		GeneralPath folder = new GeneralPath();
		folder.moveTo(x + w, y + tab);
		folder.lineTo(x + w, y + h);
		folder.lineTo(x, y + h);
		folder.lineTo(x, y + tab);
		folder.curveTo(x, y + tab, x, y + tab - quarterTab, x + quarterTab, y + tab - quarterTab);
		folder.lineTo(x + w - quarterTab, y + tab - quarterTab);
		folder.curveTo(x + w - quarterTab, y + tab - quarterTab, x + w, y + tab - quarterTab, x + w, y + tab);
		return folder;
	}

	/**
	 * Gets the {@link GeneralPath} {@link Shape} for the back part of the
	 * folder with the tab.
	 */
	protected GeneralPath getTabPath(Rectangle2D bounds) {
		float x = (float) bounds.getX();
		float y = (float) bounds.getY();
		float w = (float) bounds.getWidth() - 1;
		float h = (float) bounds.getHeight() - 1;
		float tab = Math.max(4, h / 10);
		float halfTab = tab / 2;
		float quarterTab = tab / 4;
		float tabWidth = Math.max(8, w / 2.5f);

		GeneralPath folder = new GeneralPath();
		folder.moveTo(x + w, y + tab - quarterTab);
		folder.lineTo(x + w, y + h);
		folder.lineTo(x, y + h);
		folder.lineTo(x, y + tab - quarterTab);
		folder.curveTo(x, y + tab - quarterTab, x, y + halfTab, x + quarterTab, y + halfTab);
		folder.lineTo(x + w - tabWidth, y + halfTab);
		folder.lineTo(x + w - tabWidth + halfTab, y);
		folder.lineTo(x + w - halfTab, y);
		folder.lineTo(x + w, y + tab - quarterTab);
		return folder;
	}

}
