/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;


/**
 * Provides an icon and a position for a {@link ShrimpNode}, as well as whether or not
 * to scale the icon.
 * There is also an option for choosing if the icon is rendered when a node is opened (in a nested view).
 *
 * @see DefaultIconProvider
 * @author Chris Callendar
 * @date 19-Oct-07
 */
public interface IconProvider {

	/** The icon is not scaled, it remains the same size regardless of the zoom level. */
	public static final int SCALE_NONE = 0;

	/** The icon is scaled with the node based on the zoom level. */
	public static final int SCALE_NODE = 1;

	/** The icon is not scaled, and it is tiled across the whole node. */
	public static final int SCALE_TILE = 2;

	/** The icon is scaled to fit the node, the aspect ratio of the icon is not preserved. */
	public static final int SCALE_FIT_TO_NODE = 3;

	/** The icon is scaled to fit the node, but the aspect ratio of the icon is preserved. */
	public static final int SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO = 4;

	public static final String[] SCALE_OPTIONS = { "none", "Scale based on the zoom level", "tile (no scaling)",
								"fit to node", "fit to node (keep aspect ratio)" };


	/** Constant value representing that the icon is always rendered regardless of whether the node is opened or closed. */
	public static final int RENDER_ALWAYS = 0;

	/** Constant value representing that the icon is only rendered on nodes that are opened. */
	public static final int RENDER_OPEN_NODES = 1;

	/** Constant value representing that the icon is only rendered on nodes that are closed. */
	public static final int RENDER_CLOSED_NODES = 2;

	public static final String[] RENDER_OPTIONS = { "all nodes", "open nodes", "closed nodes" };

	/**
	 * Returns the icon to render on the {@link ShrimpNode}.
	 * @return the Icon
	 */
	public Icon getIcon();

	/**
	 * Returns the (x, y) position of where the icon should be rendered.
	 * @param nodeBounds the bounds of the node.
	 * 	These bounds will either be the actual bounds of the node as seen on the screen
	 *  if {@link IconProvider#getScaleIcon()} returns SCALE_NODE or SCALE_FIT_TO_NODE,
	 *  or the node bounds in the node's coordinate system if {@link IconProvider#getScaleMode()} returns SCALE_NONE.
	 * @return the icon's position
	 */
	public Point2D getIconPosition(Rectangle2D nodeBounds);

	/**
	 * Returns the image scaling mode: scale none, scale node, fit to node, or tile
	 * The bounds passed into the {@link IconProvider#getIconPosition(Rectangle2D)} method
	 * depend on the return value of this method.
	 * @return the scale mode integer
	 * @see IconProvider#SCALE_FIT_TO_NODE
	 * @see IconProvider#SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO
	 * @see IconProvider#SCALE_TILE
	 * @see IconProvider#SCALE_NODE
	 * @see IconProvider#SCALE_NONE
	 */
	public int getScaleMode();

	/**
	 * Determines when the icon should be rendered (always, open nodes only, closed nodes only).
	 * This is only applicable in nested views.
	 * @see IconProvider#RENDER_ALWAYS
	 * @see IconProvider#RENDER_CLOSED_NODES
	 * @see IconProvider#RENDER_OPEN_NODES
	 * @return int
	 */
	public int getRenderOption();

}
