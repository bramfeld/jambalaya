/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.resource.IconFilename;
import ca.uvic.csr.shrimp.util.ShrimpUtils;


/**
 * Provides an icon to a {@link PShrimpNode} that will be rendered in the
 * center of the node, with no scaling.
 * Also exposes some useful methods for getting the popular positions:
 * top left, top middle, top right, left middle, center, right middle,
 * bottom left, bottom middle, and bottom right.
 *
 * @author Chris Callendar
 * @date 19-Oct-07
 */
public class DefaultIconProvider implements IconProvider {

	protected static final String SEP = ";";

	protected IconFilename iconFilename;
	protected int scale;
	protected int padding;
	protected double[] scaleXY;

	private NumberFormat numberFormat;	// for the scale values

	private int renderOption;

	/**
	 * Default constructor - no icon, scaling, or padding.
	 * Must set the icon using {@link DefaultIconProvider#setIcon(Icon)}
	 * otherwise errors will occur.
	 */
	public DefaultIconProvider() {
		this(new IconFilename("", null), SCALE_NODE);
	}

	/**
	 * Initializes this with the icon and scale values from the
	 * given provider.  If the provider is a {@link DefaultIconProvider} then
	 * the icon padding value is copied too.
	 */
	public DefaultIconProvider(IconProvider provider) {
		this();
		if (provider instanceof DefaultIconProvider) {
			DefaultIconProvider dip = (DefaultIconProvider) provider;
			setIconFilename(dip.getIconFilename());
			setScaleMode(dip.getScaleMode());
			setIconPadding(dip.getIconPadding());
			setRenderOption(dip.getRenderOption());
			if (dip.hasScaleValues()) {
				setScaleValues(dip.getScaleX(), dip.getScaleY());
			}
		} else {
			setIcon(provider != null ? provider.getIcon() : null);
			setScaleMode(provider != null ? provider.getScaleMode() : SCALE_NODE);
		}
	}

	/**
	 * Sets the icon, no scaling or padding.
	 */
	public DefaultIconProvider(Icon icon) {
		this(icon, SCALE_NODE);
	}

	public DefaultIconProvider(IconFilename iconFilename) {
		this(iconFilename, SCALE_NODE);
	}

	/**
	 * Sets the icon and scaling for this provider.
	 */
	public DefaultIconProvider(Icon icon, int scaleMode) {
		this(new IconFilename("", icon), scaleMode);
	}

	/**
	 * Sets the icon, filename, and scaling for this provider.
	 */
	public DefaultIconProvider(IconFilename iconFilename, int scaleMode) {
		this.iconFilename = iconFilename;
		this.scale = scaleMode;
		this.padding = 0;
		this.scaleXY = null;
		this.renderOption = RENDER_ALWAYS;
	}

	public String getPropertyValue() {
		String prop = "";
		String path = iconFilename.getFilename();
		if ((path != null) && (path.length() > 0) && new File(path).exists()) {
			prop = path + SEP + getScaleMode() + SEP + getIconPadding() + SEP + getRenderOption();
			if (hasScaleValues()) {
				prop += SEP + getNumberFormat().format(getScaleX()) + SEP + getNumberFormat().format(getScaleY());
			}
		}
		return prop;
	}

	public void parsePropertyValue(String propValue) {
		if ((propValue != null) && (propValue.length() > 0)) {
			String[] split = propValue.split(SEP);
			parsePropertyValue(split);
		}
	}

	protected void parsePropertyValue(String[] split) {
		if (split.length > 0) {
			String path = split[0];
			if (path.length() > 0) {
				iconFilename = new IconFilename(path, null);
				iconFilename.reloadIcon();
			}
			if (split.length > 1) {
				int mode = ShrimpUtils.parseInt(split[1], SCALE_NODE);
				switch (mode) {
					case SCALE_NODE :
					case SCALE_FIT_TO_NODE :
					case SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO :
					case SCALE_TILE :
					case SCALE_NONE :
					setScaleMode(mode);
				}
			}
			if (split.length > 2) {
				setIconPadding(ShrimpUtils.parseInt(split[2], 0));
			}
			if (split.length > 3) {
				int option = ShrimpUtils.parseInt(split[3], RENDER_ALWAYS);
				switch (option) {
					case RENDER_ALWAYS :
					case RENDER_OPEN_NODES :
					case RENDER_CLOSED_NODES :
						setRenderOption(option);
						break;
					default :
						break;
				}
			}
			if (split.length > 5) {
				try {
					double sx = getNumberFormat().parse(split[4]).doubleValue();
					double sy = getNumberFormat().parse(split[5]).doubleValue();;
					setScaleValues(sx, sy);
				} catch (ParseException e) {
					System.err.println("Error parsing scaled values: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * This only applies when the icon is positioned at the edge of the node.
	 * If centered then this doesn't apply.
	 * @return the amount of padding around the icon
	 */
	public int getIconPadding() {
		return padding;
	}

	/**
	 * Sets the amount of padding around the icon.
	 * Only applies when the icon is positioned at the edge of the node.
	 * @param padding
	 */
	public void setIconPadding(int padding) {
		this.padding = padding;
	}

	public Icon getIcon() {
		return iconFilename.getIcon();
	}

	public void setIcon(Icon icon) {
		this.iconFilename = new IconFilename("", icon);
	}

	public IconFilename getIconFilename() {
		return iconFilename;
	}

	public void setIconFilename(String filename, Icon icon) {
		this.iconFilename = new IconFilename(filename, icon);
	}

	public void setIconFilename(IconFilename iconFilename) {
		this.iconFilename = (iconFilename != null ? iconFilename : new IconFilename("", null));
	}

	protected NumberFormat getNumberFormat() {
		if (numberFormat == null) {
			numberFormat = NumberFormat.getNumberInstance();
			numberFormat.setMaximumFractionDigits(3);
		}
		return numberFormat;
	}

	/**
	 * Sets the scale values, they will be constrained to [0, 1].
	 * @param sx the scaled x value
	 * @param sy the scaled y value
	 */
	public void setScaleValues(double sx, double sy) {
		// do we want to constrain these values?  e.g. [0, 1] ?
		sx = Math.max(Math.min(sx, 1), 0);
		sy = Math.max(Math.min(sy, 1), 0);
		this.scaleXY = new double[] { sx, sy };
	}

	public double getScaleX() {
		return (scaleXY != null ? scaleXY[0] : 0);
	}

	public double getScaleY() {
		return (scaleXY != null ? scaleXY[1] : 0);
	}

	public boolean hasScaleValues() {
		return (scaleXY != null);
	}

	public Point2D getScaledIconPosition(Rectangle2D nodeBounds) {
		double x = Math.max(0, nodeBounds.getX() + (getScaleX() * nodeBounds.getWidth()) - (getIconWidth() / 2));
		double y = Math.max(0, nodeBounds.getY() + (getScaleY() * nodeBounds.getHeight()) - (getIconHeight() / 2));
		x = Math.min(maxX(nodeBounds), x);
		y = Math.min(maxY(nodeBounds), y);
		x = Math.max(minX(nodeBounds), x);
		y = Math.max(minY(nodeBounds), y);
		return new Point2D.Double(x, y);
	}

	/**
	 * Returns the center position, unless the scaled position values have been set in which case
	 * it calls the {@link DefaultIconProvider#getScaledIconPosition(Rectangle2D)} method.
	 */
	public Point2D getIconPosition(Rectangle2D nodeBounds) {
		if (hasScaleValues()) {
			return getScaledIconPosition(nodeBounds);
		}
		return getCenter(nodeBounds);
	}

	/**
	 * @return the scale mode
	 * @see IconProvider#SCALE_FIT_TO_NODE
	 * @see IconProvider#SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO
	 * @see IconProvider#SCALE_TILE
	 * @see IconProvider#SCALE_NONE
	 * @see IconProvider#SCALE_NODE
	 */
	public int getScaleMode() {
		return scale;
	}

	public void setScaleMode(int scale) {
		this.scale = scale;
	}

	/**
	 * Sets when the icon should be rendered.
	 * @see IconProvider#RENDER_ALWAYS
	 * @see IconProvider#RENDER_CLOSED_NODES
	 * @see IconProvider#RENDER_OPEN_NODES
	 * @param renderOption
	 */
	public void setRenderOption(int renderOption) {
		this.renderOption = renderOption;
	}

	public int getRenderOption() {
		return renderOption;
	}

	/**
	 * @return the icon with or zero if the icon is null
	 */
	public int getIconWidth() {
		return (getIcon() != null ? getIcon().getIconWidth() : 0);
	}

	/**
	 * @return the icon height or zero if the icon is null
	 */
	public int getIconHeight() {
		return (getIcon() != null ? getIcon().getIconHeight() : 0);
	}

	////////////////////////////
	// Utility methods
	////////////////////////////

	protected Point2D getTopLeft(Rectangle2D nodeBounds) {
		double x = minX(nodeBounds);
		double y = minY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getTop(Rectangle2D nodeBounds) {
		double x = midX(nodeBounds);
		double y = minY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getTopRight(Rectangle2D nodeBounds) {
		double x = maxX(nodeBounds);
		double y = minY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getLeft(Rectangle2D nodeBounds) {
		double x = minX(nodeBounds);
		double y = midY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getCenter(Rectangle2D nodeBounds) {
		double x = midX(nodeBounds);
		double y = midY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getRight(Rectangle2D nodeBounds) {
		double x = maxX(nodeBounds);
		double y = midY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getBottomLeft(Rectangle2D nodeBounds) {
		double x = minX(nodeBounds);
		double y = maxY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getBottom(Rectangle2D nodeBounds) {
		double x = midX(nodeBounds);
		double y = maxY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected Point2D getBottomRight(Rectangle2D nodeBounds) {
		double x = maxX(nodeBounds);
		double y = maxY(nodeBounds);
		return new Point2D.Double(x, y);
	}

	protected double minX(Rectangle2D nodeBounds) {
		return nodeBounds.getX() + getIconPadding();
	}

	protected double midX(Rectangle2D bounds) {
		return Math.max(0, bounds.getX() + (bounds.getWidth() / 2) - (getIconWidth() / 2));
	}

	protected double maxX(Rectangle2D bounds) {
		return Math.max(0, bounds.getX() + bounds.getWidth() - getIconWidth() - getIconPadding());
	}

	protected double minY(Rectangle2D nodeBounds) {
		return nodeBounds.getY() + getIconPadding();
	}

	protected double midY(Rectangle2D bounds) {
		return Math.max(0, bounds.getY() + (bounds.getHeight() / 2) - (getIconHeight() / 2));
	}

	protected double maxY(Rectangle2D bounds) {
		return Math.max(0, bounds.getY() + bounds.getHeight() - getIconHeight() - getIconPadding());
	}

}
