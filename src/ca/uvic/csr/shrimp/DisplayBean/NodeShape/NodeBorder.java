/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Properties;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * Provides a restricted set of border setting methods
 * @author Chris Bennett
 */
public class NodeBorder {

	public final static String PLAIN = "Plain";
	public final static String DASHED = "Dashed";
	public final static String DEFAULT_BORDER_STYLE = PLAIN;

	public static final Color DEFAULT_UNHIGHLIGHT_COLOR = new Color(0x40, 0x40, 0x40); // Dark grey/black
	public static final Color DEFAULT_HIGHLIGHT_COLOR = Color.BLUE;
	public static final Color DEFAULT_EQUIVALENT_SELECTED_COLOR = new Color(0x1e, 0x90, 0xff);
	public static final Color DEFAULT_MOUSEOVER_COLOR = Color.black;

	private static float DEFAULT_MOUSEOVER_STROKE_WIDTH = 1.0f;
	private static float DEFAULT_UNHIGHLIGHT_STROKE_WIDTH = 1.2f;
	private static float DEFAULT_HIGHLIGHT_STROKE_WIDTH = 2.0f;

	// Border cannot be thicker than this - TODO - test and verify this number
	private static float MAXIMUM_MAGNIFIED_STROKE_WIDTH = 40f;

	private static final float[] DASHED_PATTERN = { 30, 20 };

	private Color outerColor = null;
	private Color innerColor = null;
	private float[] outerDashPattern = null;
	private float[] innerDashPattern = null;
	private float width = DEFAULT_UNHIGHLIGHT_STROKE_WIDTH;
	private float userSelectedWidthMultiplier; // selected on options general tab
	//private double magnification;

	/**
	 * Uses defaults for node state variables
	 */
	public NodeBorder(double magnification, Color outerColor, String outerBorderType,
					  Color innerColor, String innerBorderType) {
		this(magnification, outerColor, outerBorderType, innerColor, innerBorderType, false, false, false);
	}

	/**
	 * Uses defaults for node state variables
	 */
	public NodeBorder(double magnification, Color color, String borderType) {
		this(magnification, color, borderType, null, null, false, false, false);
	}

	/**
	 * Uses no magnification and the default border style.
	 */
	public NodeBorder(Color color) {
		this(0, color, DEFAULT_BORDER_STYLE);
	}

	/**
	 * Common constructor
	 */
	public NodeBorder(double magnification, Color outerColor, String outerBorderType,
		Color innerColor, String innerBorderType, boolean highlighted,
		boolean mouseOver, boolean equivalentNodeSelected) {

		//this.magnification = magnification;
		this.userSelectedWidthMultiplier = getWidthMultiplier();
		this.width = getWidth(magnification, highlighted, mouseOver, equivalentNodeSelected, userSelectedWidthMultiplier);

		if (outerColor != null) {
			this.outerColor = getColor(outerColor, highlighted, mouseOver, equivalentNodeSelected);
		}
		if (innerColor != null) {
			this.innerColor = getColor(innerColor, highlighted, mouseOver, equivalentNodeSelected);
		}

		if (DASHED.equals(outerBorderType)) {
			this.outerDashPattern = DASHED_PATTERN;
		}
		if (DASHED.equals(innerBorderType)) {
			this.innerDashPattern = DASHED_PATTERN;
		}
	}

	/**
	 * Returns the outerColor
	 * @return outer border color
	 */
	public Color getOuterColor() {
		return outerColor;
	}

	/**
	 * Returns the innerColor
	 * @return inner border color
	 */
	public Color getInnerColor() {
		return innerColor;
	}

	/**
	 * Calculate the user selected width multiplier
	 */
	private float getWidthMultiplier() {
		float multiplier = 1;
		Properties appProps = ApplicationAccessor.getProperties();
		String widthMultiplerString = appProps.getProperty(DisplayBean.PROPERTY_KEY__BORDER_WIDTH_MULTIPLIER,
				DisplayBean.DEFAULT_BORDER_WIDTH_MULTIPLIER);
		try {
			multiplier = ((Integer.parseInt(widthMultiplerString)-1))/3.0f;
		} catch (NumberFormatException nfe) {
			// ignore
		}
		return multiplier;
	}

	/**
	 * Get the border stroke width
	 * @return width of the border
	 */
	private float getWidth(double magnification, boolean highlighted, boolean mouseOver,
			boolean equivalentNodeSelected, float widthMultiplier) {

		double absoluteStrokeWidth = DEFAULT_UNHIGHLIGHT_STROKE_WIDTH;
		if (highlighted || equivalentNodeSelected) {
		    absoluteStrokeWidth = DEFAULT_HIGHLIGHT_STROKE_WIDTH;
		}  else if (mouseOver) {
			absoluteStrokeWidth += DEFAULT_MOUSEOVER_STROKE_WIDTH;
		}
		absoluteStrokeWidth += absoluteStrokeWidth*widthMultiplier;
		absoluteStrokeWidth = ((magnification == 0.0) ? absoluteStrokeWidth : absoluteStrokeWidth/magnification);
		return (float)(Math.min(absoluteStrokeWidth, MAXIMUM_MAGNIFIED_STROKE_WIDTH));
	}

	/**
	 * Return the border color adjusted based on the node's state
	 * @param color
	 * @param highlighted
	 * @param mouseOver
	 * @param equivalentNodeSelected
	 * @return border color
	 */
	private Color getColor(Color color, boolean highlighted, boolean mouseOver, boolean equivalentNodeSelected) {

		Color borderColor = DEFAULT_UNHIGHLIGHT_COLOR;
		if (color  != null) {
			borderColor = color; // use the user-specified color
		}

		if (highlighted) {
		    borderColor = DEFAULT_HIGHLIGHT_COLOR;
		} else if (equivalentNodeSelected) {
		    borderColor = DEFAULT_EQUIVALENT_SELECTED_COLOR;
		} else if (mouseOver) {
			borderColor = DEFAULT_MOUSEOVER_COLOR;
		}
		return borderColor;
	}

	/**
	 * Get the stroke width
	 * @return stroke width
	 */
	public float getStrokeWidth() {
		return width;
	}

	/**
	 * Get the total border width
	 * @return total border width
	 */
	public float getWidth() {
		float width = 1.0f;
		if (hasOuterBorder()) {
			width += getStrokeWidth();
		}
		if (hasInnerBorder()) {
			width += getStrokeWidth();
		}
		return width;
	}

	/**
	 * Gets the maximum width as if both the inner and outer border were specified.
	 * @return max border width
	 */
	public float getMaximumWidth() {
		return 1.0f + (2 * getStrokeWidth());
	}

	public static Vector getStyles() {
		Vector styles = new Vector();
		styles.add(PLAIN);
		styles.add(DASHED);
		return styles;
	}

	/**
	 * Sets up the graphics2D object with this border's parameters.
	 * Uses only the outer border fields
	 * @param g2
	 */
	public void setGraphics(Graphics2D g2) {
		setOuterGraphics(g2);
	}

	/**
	 * Sets up the graphics2D object with this border's parameters.
	 * Uses only the outer border fields
	 * @param g2
	 */
	public void setOuterGraphics(Graphics2D g2) {
		setGraphics(g2, outerColor, outerDashPattern);
	}

	/**
	 * Sets up the graphics2D object with this border's parameters.
	 * Uses only the innerborder fields
	 * @param g2
	 */
	public void setInnerGraphics(Graphics2D g2) {
		setGraphics(g2, innerColor, innerDashPattern);
	}

	/**
	 * Sets up the {@link Graphics2D} object with this border's parameters.
	 */
	private void setGraphics(Graphics2D g2, Color color, float[] dashPattern) {
		g2.setColor(color);
		if (dashPattern != null) {
			g2.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, dashPattern, 0));
		} else {
			g2.setStroke(new BasicStroke(width));
		}
	}

	/**
	 * Return true if there is an inner border set
	 * @return true if this has an outer border
	 */
	public boolean hasOuterBorder() {
		return outerColor != null;
	}

	/**
	 * Return true if there is an inner border set
	 * @return true if this has an inner border
	 */
	public boolean hasInnerBorder() {
		return innerColor != null;
	}

}