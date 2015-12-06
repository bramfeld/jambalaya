/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import ca.uvic.csr.shrimp.gui.NodeFilterPalette;
import ca.uvic.csr.shrimp.gui.NodePresentationDialog;

/**
 * @author Rob Lintern
 */
public interface NodeShape extends Comparable, Cloneable {

	/**
	 * Determines the point at which a terminal should attached to the outer edge of this node based on an
	 * angle from this node's center.
	 * @param terminalPositionAngle The angle from this node's center that the terminal will be at.
	 * @param globalBounds Global bounds of node shape
	 * @param localToGlobalTx Transformation from local to global coordinates
	 * @param arcAttachPoint Overwritten and returned with the appropriate x and y values filled in.
	 * @return the angle that the terminal should be drawn at in order to match "nicely" with the edge of this node
	 */
	public double getTerminalAttachPoint(double terminalPositionAngle, Rectangle2D.Double globalBounds, AffineTransform localToGlobalTx, Point2D.Double arcAttachPoint);

	/**
	 * @return inner bounds
	 */
    public Rectangle2D.Double getInnerBounds(Rectangle2D outerBounds);

    /**
     * Renders the node - first it fills the shape, then it renders a possible image,
     * and then it draws the border.
     * @param outerBounds the outer bounds of the shape
     * @param fillColor the fill color (or gradient)
     * @param border the node border information
     * @param nodeImage the node image information
     */
    public void render(Rectangle2D outerBounds, Graphics2D g2, Paint fillColor, NodeBorder border, NodeImage nodeImage);

	public Object clone();

	public Shape getShape(Rectangle2D bounds);

	/**
	 * @param color the color of the node
	 * @param nodeImage the image to render
	 * @return a JPanel which displays a thumbnail of this node shape
	 */
	public JPanel getThumbnail(int x, int y, int width, int height, Paint color,
			Color outerBorderColor, Color innerBorderColor, NodeImage nodeImage);

	/**
	 * @return the name of the node shape.
	 */
	public String getName();

	/**
	 * In the {@link NodeFilterPalette} and the {@link NodePresentationDialog} users can
	 * select a shape for a node type. Each NodeShape subclass that returns true for this
	 * method will be displayed in that dialog.
	 * @return true if the {@link NodeShape} is user selectable.<br>
	 */
	public boolean isUserSelectable();

	/**
	 * Content border probably only looks good for rectangular shapes?
	 * @return true if the inner rectangle border should be drawn
	 */
	public boolean drawContentBorder();

	/**
	 * Sets the image, fill, and draw border properties for this {@link NodeShape}.  This overrides any settings
	 * used in the {@link NodeShape#render(Rectangle2D, Graphics2D, Color, Color, float, NodeImage)} method.
	 * @param image the image to render for this node shape.
	 */
	public void setCustomRendering(NodeImage image);

	public NodeImage getCustomRendering();
	
	public boolean hasCustomRendering();
}
