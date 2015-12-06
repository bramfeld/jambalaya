/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;

/**
 * @author Rob Lintern
 */
public class CompositeArcStyle extends AbstractLineArrowArcStyle {

	public final static String COMPOSITE_STYLE = "Composite";

	private final static double CURVE_CONSTANT = 20;
	private final static double MIN_ARROW_HEAD_WIDTH = 10.0;
	private final static double MIN_ARROW_HEAD_LENGTH = 10.0;
	private final static double ARROW_HEAD_LENGTH_TO_ARC_WEIGHT_RATIO = 1.5;
	private final static double ARROW_HEAD_WIDTH_TO_ARC_WEIGHT_RATIO = 1.5;

	private boolean scaleFromMax = true;

	public CompositeArcStyle() {
		this(true);
	}

	public CompositeArcStyle(boolean scaleFromMax) {
		super(COMPOSITE_STYLE, false);
		this.scaleFromMax = scaleFromMax;
		showConnectors = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle#createArrowHead()
	 */
	protected void createArrowHead() {
		arrowHeadPath.reset(); // the arrowhead will be part of the linePath
	}

	// TODO a bit of hack
	public void recreateArc() {
		recreateLineAndArrowHead();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D r) {
		return linePath.intersects(r);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightLineArcStyle#createLinePath()
	 */
	protected void createLinePath() {
		linePath.reset();
		double dx = destPoint.x - srcPoint.x;
		double dy = destPoint.y - srcPoint.y;
		double d = Point2D.Double.distance(destPoint.x, destPoint.y, srcPoint.x, srcPoint.y);

		double slope = dy / dx;
		double theta = Math.atan(-(1.0 / slope));

		GeneralPath arrowPath = new GeneralPath();
		double lineWidth = scaleFromMax ? CompositeArcsManager.calculateWeight((int) weight) / mag : weight / mag;
		double arrowHeadLength = Math.max(MIN_ARROW_HEAD_LENGTH / mag, lineWidth * ARROW_HEAD_LENGTH_TO_ARC_WEIGHT_RATIO);
		double arrowHeadWidth = Math.max(MIN_ARROW_HEAD_WIDTH / mag, lineWidth * ARROW_HEAD_WIDTH_TO_ARC_WEIGHT_RATIO);
		double rotate = (dy < 0) ? theta + 3.0 * Math.PI / 2.0 : theta + Math.PI / 2.0;
		AffineTransform rotateT = AffineTransform.getRotateInstance(rotate, srcPoint.x, srcPoint.y);
		AffineTransform t = new AffineTransform();
		t.concatenate(rotateT);

		if (curveFactor == 0) {
			Point2D.Double[] pts = new Point2D.Double[] { new Point2D.Double(srcPoint.x, srcPoint.y),
					new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y - lineWidth / 2.0),
					new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y - arrowHeadWidth / 2.0),
					new Point2D.Double(srcPoint.x + d, srcPoint.y),
					new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y + arrowHeadWidth / 2.0),
					new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y + lineWidth / 2.0) };
			for (int i = 0; i < pts.length; i++) {
				Point2D.Double pt = pts[i];
				if (i == 0) {
					arrowPath.moveTo((float) pt.x, (float) pt.y);
				} else {
					arrowPath.lineTo((float) pt.x, (float) pt.y);
				}
			}
			arrowPath.closePath();
		} else {
			double ctrlOffset = (CURVE_CONSTANT * curveFactor) / mag;
			Point2D.Double ctrlPoint = new Point2D.Double(srcPoint.x + d / 2.0, srcPoint.y + ctrlOffset);

			Point2D.Double p0 = new Point2D.Double(srcPoint.x, srcPoint.y);
			Point2D.Double p1 = new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y - lineWidth / 2.0);
			Point2D.Double p2 = new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y - arrowHeadWidth / 2.0);
			Point2D.Double p3 = new Point2D.Double(srcPoint.x + d, srcPoint.y); // tip of the arrow
			Point2D.Double p4 = new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y + arrowHeadWidth / 2.0);
			Point2D.Double p5 = new Point2D.Double(srcPoint.x + d - arrowHeadLength, srcPoint.y + lineWidth / 2.0);

			// if we are showing a curved composite arc, then rotate the head of the arrow slightly
			// just because it looks better!
			AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 8.0d, srcPoint.x + d, srcPoint.y);
			Point2D.Double[] arrowHeadPts = new Point2D.Double[] { p1, p2, p3, p4, p5 };
			Point2D.Double[] arrowHeadPtsRotated = new Point2D.Double[5];
			at.transform(arrowHeadPts, 0, arrowHeadPtsRotated, 0, 5);
			p1 = arrowHeadPtsRotated[0];
			p2 = arrowHeadPtsRotated[1];
			p3 = arrowHeadPtsRotated[2];
			p4 = arrowHeadPtsRotated[3];
			p5 = arrowHeadPtsRotated[4];

			arrowPath.moveTo((float) p0.x, (float) p0.y);
			arrowPath.append(new QuadCurve2D.Double((float) p0.x, (float) p0.y, (float) ctrlPoint.x,
							(float) ctrlPoint.y, (float) p1.x, (float) p1.y), true);
			arrowPath.lineTo((float) p2.x, (float) p2.y);
			arrowPath.lineTo((float) p3.x, (float) p3.y);
			arrowPath.lineTo((float) p4.x, (float) p4.y);
			arrowPath.lineTo((float) p5.x, (float) p5.y);
			arrowPath.append(new QuadCurve2D.Double((float) p5.x, (float) p5.y, (float) ctrlPoint.x,
							(float) ctrlPoint.y, (float) p0.x, (float) p0.y), true);
			arrowPath.closePath();
		}

		// @tag Shrimp.Java6 : In Java 6 createTransformedShape returns a Path2D.Double which isn't a GeneralPath
		// GeneralPath actually extends Path2D.Double in Java 6
		Shape shape = t.createTransformedShape(arrowPath);
		if (shape instanceof GeneralPath) {
			linePath = (GeneralPath) shape;
		} else {
			linePath.append(shape, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle#render(java.awt.Graphics2D, int, java.awt.Color, boolean, boolean)
	 */
	public void render(Graphics2D g2, int renderingQuality, Color arcColor, boolean showSrcConnector, boolean showDestConnector) {
		// double d = Point2D.distance(srcPoint.x, srcPoint.y, destPoint.x, destPoint.y);
		// if (d*mag > 500000) {
		// return; // otherwise we get some strange exceptions thrown
		// }
		g2.setColor(arcColor);

		// draw the line
		g2.fill(linePath);
		float absoluteStrokeWidth = 1.0f;

		if (highlighted) {
			absoluteStrokeWidth += 1.0f;
		}
		if (active) {
			absoluteStrokeWidth += 1.0f;
		}
		g2.setStroke(new BasicStroke((float) (absoluteStrokeWidth / mag)));
		g2.setPaint(Color.BLACK);
		g2.draw(linePath);

	}

	public Object clone() {
		ArcStyle style = new CompositeArcStyle();
		return style;
	}

	/*
	 * (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractArcStyle#getThumbnail(java.awt.Color)
	 */
	public JPanel getThumbnail(Color arcColor) {
		return getThumbnail(arcColor, 40, 13);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractArcStyle#getThumnailWeight()
	 */
	protected double getThumnailWeight() {
		return 4.0;
	}

	public boolean isScaledFromMax() {
		return scaleFromMax;
	}

	/**
	 * @param b
	 */
	public void setScaledFromMax(boolean b) {
		scaleFromMax = b;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractArcStyle#getThumnailArcStyle()
	 */
	protected ArcStyle getThumnailArcStyle() {
		return new CompositeArcStyle(false);
	}

}
