/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

import ca.uvic.csr.shrimp.util.GraphicsUtils;


/**
 * Base class for {@link NodeShape} objects.
 *
 * Can also render images on top of the nodes.  These images can be scaled in different ways:
 * <dl>
 * <dt>Tiled:</dt>
 * <dd>Tiles the image over the whole node.  If the image is larger than the node
 * then the image is cutoff.</dd>
 *
 * <dt>Stretched:</dt>
 * <dd>Stretches or shrinks the image to always fit the size of the node.</dd>
 *
 * <dt>Centered:</dt>
 * <dd>Centers the image on the node and draws it.  If the image is larger than the
 * node then it will be shrunk down to match the node size.</dd>
 *
 * <dt>Normal:</dt>
 * <dd>Draws the image in the top left with no scaling.  If the image is larger than
 * the node then it is clipped. </dd>
 * </dl>
 *
 * @author Rob Lintern, Chris Callendar
 */
public abstract class AbstractNodeShape implements NodeShape, ImageObserver {

	protected String name;
	private int tilePadding = 0;
	private boolean displayNoImageMessage = false;	// not used

	private NodeImage customImage = null;

	protected AbstractNodeShape(String name) {
		this.name = name;
	}

	/////////////////////////////////
	// Extended/Implemented methods
	/////////////////////////////////

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return true;
	}

	/**
	 * @see NodeShape#clone()
	 */
	public abstract Object clone();

	/**
	 * @see NodeShape#getName()
	 */
	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		return this.getName().compareTo(((NodeShape)o).getName());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof NodeShape) {
			return this.getName().equals(((NodeShape)obj).getName());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * Sets the custom image, fill, and draw border properties for this node shape.
	 */
	public void setCustomRendering(NodeImage nodeImage) {
		this.customImage = nodeImage;
	}

	public boolean hasCustomRendering() {
		return (customImage != null);
	}

	public NodeImage getCustomRendering() {
		return customImage;
	}

	/**
	 * Always returns true.  Subclasses that use custom node shapes should return false
	 * if users shouldn't be allowed to select their node shape.
	 * @see NodeShape#isUserSelectable()
	 */
	public boolean isUserSelectable() {
		return true;
	}

	/**
	 * Overriden in child classes (e.g. RectangleNodeShape).
	 * Note this should not be confused with the inner or outer borders
	 * that can be set by the user.
	 */
	public boolean drawContentBorder()  {
		return false;
	}

	///////////////////////////////
	// RENDER METHODS
	///////////////////////////////

	/**
	 * @see NodeShape#render(Rectangle2D, Graphics2D, Paint, NodeBorder, NodeImage)
	 */
	public void render(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage) {
		// override and use the custom settings
		if (customImage != null) {
			nodeImage = customImage;
		}

		// first we render and fill the shape
		if (nodeImage.isFillBackground()) {
			g2.setPaint(fillColor);
			renderShape(outerBounds, g2);
		}

		// now render an image if there is one
		Image image = nodeImage.getImage();
		if (image != null) {
			renderImage(outerBounds, g2, image, nodeImage.getImageSizing());
		}

		if (nodeImage.isDrawOuterBorder() || nodeImage.isDrawInnerBorder()) {
			renderBorder(nodeImage.isDrawOuterBorder(), nodeImage.isDrawInnerBorder(),
					outerBounds, g2, border);
		}

		renderOverlay(outerBounds, g2, fillColor, border, nodeImage);
	}

	/**
	 * Allows subclasses to render something over top of the node (over the node shape, image, and border).
	 */
	protected void renderOverlay(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage) {
		// does nothing
	}

	/**
	 * Fills the shape.
	 * @param outerBounds
	 * @param g2
	 */
	protected void renderShape(Rectangle2D outerBounds, Graphics2D g2) {
		Shape shapeToRender = getShape(outerBounds, g2);
		g2.fill(shapeToRender);
	}

	/**
	 * Gets the shape.  By default this method just calls {@link NodeShape#getShape(Rectangle2D)}.
	 * Subclasses can override this method to do something different.
	 */
	protected Shape getShape(Rectangle2D outerBounds, Graphics2D g2) {
		return getShape(outerBounds);
	}

	/**
	 * @param renderInner
	 * @param renderOuter
	 * @param g2
	 * @param border
	 */
	protected void renderBorder(boolean renderOuter, boolean renderInner,
			Rectangle2D outerBounds, Graphics2D g2, NodeBorder border) {
		if (renderOuter && border.hasOuterBorder()) {
			border.setOuterGraphics(g2);
			double inset = border.getStrokeWidth()/2.0;
			double x = outerBounds.getX() + inset;
			double y = outerBounds.getY() + inset;
			double w = outerBounds.getWidth() - 2*inset;
			double h = outerBounds.getHeight() - 2*inset;
			Rectangle2D boundsWithInset = new Rectangle2D.Double (x, y, w, h);
			g2.draw(getShape(boundsWithInset));
		}
		if (renderInner && border.hasInnerBorder()) {
			border.setInnerGraphics(g2);
			double inset = border.getStrokeWidth() * 2.0;
			double x = outerBounds.getX() + inset;
			double y = outerBounds.getY() + inset;
			double w = outerBounds.getWidth() - 2*inset;
			double h = outerBounds.getHeight() - 2*inset;
			Rectangle2D boundsWithInset = new Rectangle2D.Double (x, y, w, h);
			g2.draw(getShape(boundsWithInset));
		}
	}

	/**
	 * @param outerBounds
	 * @param g2
	 */
	protected void renderImage(Rectangle2D outerBounds, Graphics2D g2, Image image, String imageSizingMode) {
		if (image != null) {
			int imageWidth = image.getWidth(this);
			int imageHeight = image.getHeight(this);
			if ((imageWidth > 0) && (imageHeight > 0)) {
				if (NodeImage.STRETCHED.equals(imageSizingMode)) {
					GraphicsUtils.drawStretchedImage(outerBounds, g2, image, this, false);
				} else if (NodeImage.CENTERED.equals(imageSizingMode)) {
					GraphicsUtils.drawCenteredImage(outerBounds, g2, image, this, imageWidth, imageHeight, true);
				} else if (NodeImage.TILED.equals(imageSizingMode)) {
					GraphicsUtils.drawTiledImage(outerBounds, g2, image, this, imageWidth, imageHeight, tilePadding);
				} else {
					GraphicsUtils.drawNormalImage(outerBounds, g2, image, this, imageWidth, imageHeight);
				}
			}
		} else if (displayNoImageMessage ) {
			drawNoImageString(outerBounds, g2);
		}
	}

	/**
	 * Writes out "No Image" in the bottom right corner of the node.
	 */
	private void drawNoImageString(Rectangle2D outerBounds, Graphics2D g2) {
		// write out a string when no image is found
		String msg = "No Image";
		// save the original font and color
		Font oldFont = g2.getFont();
		g2.setFont(oldFont.deriveFont(24f));
		Color oldColor = g2.getColor();
		g2.setColor(Color.gray);
		int width = g2.getFontMetrics().stringWidth(msg) + 24;
		int height = g2.getFontMetrics().getHeight();
		if ((width < outerBounds.getWidth()) && (height < (2 * outerBounds.getHeight()))) {
			// align bottom right
			float x = (float)(outerBounds.getMaxX() - width);
			float y = (float)(outerBounds.getMaxY() - height);
			g2.drawString(msg, x, y);
		}
		// restore the original font and color
		g2.setColor(oldColor);
		g2.setFont(oldFont);
	}

	/**
	 * @see NodeShape#getThumbnail(int, int, int, int, Paint, Color, Color, NodeImage)
	 */
	public JPanel getThumbnail(int x, int y, int width, int height, Paint color,
			Color outerBorderColor, Color innerBorderColor, NodeImage nodeImage) {
		final NodeShape thumbShape = (NodeShape) this.clone();
		thumbShape.setCustomRendering(customImage);
		JPanel thumbPanel = new ThumbPanel(thumbShape, color, outerBorderColor, innerBorderColor, nodeImage,
				new Rectangle2D.Double(x, y, width, height));
		thumbPanel.setPreferredSize(new Dimension(width, height));
		return thumbPanel;
	}

	class ThumbPanel extends JPanel {

		private Paint fillColor;
		private Color outerBorderColor;
		private Color innerBorderColor;
		private String borderStyle;
		private NodeShape nodeShape;
		private Rectangle2D outerBounds;
		private NodeImage nodeImage;

		public ThumbPanel (NodeShape nodeShape, Paint fillColor, Color outerBorderColor,
				Color innerBorderColor, NodeImage nodeImage, Rectangle2D outerBounds) {
			super ();
			this.nodeShape = nodeShape;
			this.fillColor = fillColor;
			this.outerBorderColor = outerBorderColor;
			this.innerBorderColor = innerBorderColor;
			this.borderStyle = NodeBorder.PLAIN;
			this.nodeImage = nodeImage;
			this.outerBounds = outerBounds;
		}

		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D)g;
			// use antialiasing
			Object antiAliasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// render the node shape
			NodeBorder border = new NodeBorder(1, outerBorderColor, borderStyle, innerBorderColor, borderStyle);
			nodeShape.render(outerBounds, g2, fillColor, border, nodeImage);
			// restore original antialiasing value
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing);
		}
	}

}
