/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Color;
import java.awt.Font;

/**
 * @author Rob Lintern
 */
public class DisplayConstants {

	public final static Color CANVAS_COLOUR = Color.WHITE;
	public final static float DEFAULT_COMPOSITE_ARC_STYLE_TRANSPARENCY = 0.4f;

	// some useful constants for computing
	public final static double PI = Math.PI;
	public final static double NEG_PI = -PI;
	public final static double TWO_PI = 2.0d * PI;
	public final static double HALF_PI = PI / 2.0d;
	public final static double NEG_HALF_PI = -1.0d * HALF_PI;
	public final static double QUARTER_PI = HALF_PI / 2.0d;

	public final static int NORTH = 0;
	public final static int EAST = 1;
	public final static int SOUTH = 2;
	public final static int WEST = 3;

	// constants for zooming modes
	public static final String ZOOM_MODE = "ZoomMode";
	public static final String ZOOM = "Zoom";
	public static final String MAGNIFY = "Magnify";
	public static final String FISHEYE = "Fisheye";

	// constants for labels
	public static final String LABEL_MODE = "Label Mode";
	public static final String LABEL_MODE_FIXED = "Above Node (fixed)";
	public static final String LABEL_MODE_SCALE_BY_LEVEL = "Above Node (level)";
	public static final String LABEL_MODE_SCALE_BY_NODE_SIZE = "On Node";
	// @tag Shrimp.fitToNodeLabelling
	public static final String LABEL_MODE_FIT_TO_NODE = "Fit To Node";
	public static final String LABEL_MODE_WRAP_TO_NODE = "Wrap To Node";

	/**
	 * @tag Shrimp.fitToNodeLabelling
	 * @param mode
	 * @return true if the label mode is one of the on node modes
	 */
	public static final boolean isLabelOnNode(String mode) {
		return mode.equals(LABEL_MODE_SCALE_BY_NODE_SIZE) ||
			mode.equals(LABEL_MODE_FIT_TO_NODE) ||
			mode.equals(LABEL_MODE_WRAP_TO_NODE);
	}

	// @tag Shrimp.labelling
	public static final String LABEL_STYLE = "Label Style";
	public static final String LABEL_STYLE_FULL = "Full";
	public static final String LABEL_STYLE_ELIDE_RIGHT = "Elide Right";
	public static final String LABEL_STYLE_ELIDE_LEFT = "Elide Left";
	public static final String LABEL_STYLE_HIDE = "Hide";

	// @tag Shrimp.fitToNodeLabelling
	public static final String FIT_TO_NODE_SIZE_LONG_NAME = "Fit to Node - label fills node's width";
	public static final String WRAP_TO_NODE_SIZE_LONG_NAME = "Wrap to Node - label wraps in node if too wide";
	public static final String SCALE_BY_NODE_SIZE_LONG_NAME = "On Node - label scaled to node's size";
	public static final String SCALE_BY_LEVEL_LONG_NAME = "Above Node (level) - label scaled to node's level";
	public static final String FIXED_LONG_NAME = "Above Node (fixed) - label always same size";
	public static final int MIN_FONT_SIZE = 8;
	public static final int DEFAULT_FONT_SIZE = 12;
	public static final int DEFAULT_FONT_STYLE = Font.PLAIN;

	// constants for selecting/showing connected nodes and arcs
	public static final String INCOMING = "Incoming";
	public static final String OUTGOING = "Outgoing";
	public static final String INCOMING_AND_OUTGOING = "Incoming and Outgoing";

	// mouse modes
	public static final String MOUSE_MODE_SELECT = "select";
	public static final String MOUSE_MODE_ZOOM_IN = "zoom_in";
	public static final String MOUSE_MODE_ZOOM_OUT = "zoom_out";

}
