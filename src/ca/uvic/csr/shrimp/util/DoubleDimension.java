/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.io.Serializable;


/**
  * Basically a copy of {@link edu.umd.cs.piccolo.util.PDimension}
  *
  * @author Rob Lintern
  */
 public class DoubleDimension extends Dimension2D implements Serializable {

	public double width;
	public double height;

	public DoubleDimension() {
		super();
	}

	public DoubleDimension(Dimension2D aDimension) {
		this(aDimension.getWidth(), aDimension.getHeight());
	}

	public DoubleDimension(double aWidth, double aHeight) {
		super();
		width = aWidth;
		height = aHeight;
	}

	public DoubleDimension(Point2D p1, Point2D p2) {
		width = p2.getX() - p1.getX();
		height = p2.getY() - p1.getY();
	}

	public double getHeight() {
		return height;
	}

	public double getWidth() {
		return width;
	}

	public void setSize(double aWidth, double aHeight) {
		width = aWidth;
		height = aHeight;
	}

	public String toString() {
		StringBuffer result = new StringBuffer();

		result.append("DoubleDimension[");
		result.append('[');
		result.append(width);
		result.append(", ");
		result.append(height);
		result.append(']');

		return result.toString();
	}
}