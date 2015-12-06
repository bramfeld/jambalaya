/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;
import org.eclipse.mylar.zest.layouts.dataStructures.BendPoint;

/**
 * @author Rob Lintern
 * @author Chris Bennett
 */
public abstract class StraightLineArcStyle extends AbstractLineArrowArcStyle {

	private static final double CURVE_FACTOR_BASE_OFFSET = 8.0;

	/**
	 * @param name
	 */
	public StraightLineArcStyle(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param isDashed
	 */
	public StraightLineArcStyle(String name, boolean isDashed) {
		super(name, isDashed);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle#createShape(java.awt.geom.Point2D.Double,
	 *      java.awt.geom.Point2D.Double, int)
	 */
	protected void createLinePath() {
		if (super.hasBendPoints()) {
			createBendPointLinePath();
		} else {
			createSingleLinePath();
		}

	}

	/**
	 * Create a straight or curved single line path.
	 */
	private void createSingleLinePath() {
		linePath.reset();
		Segment segment = new Segment(srcPoint.x, srcPoint.y, destPoint.x, destPoint.y);
		arrowHeadSlope = segment.lineSlope;
		if (curveFactor == 0) {
			Line2D line = segment.getLine();
			arrowHeadPoint = (Point2D.Double) segment.midPtT;
			linePath.append(line, false);
			lineShape = line;
			quadCtrlPnt = null;
		} else {
			// the distance that the ctrl point should be offset in the y direction
			double yOffset = (curveFactor + 1) * segment.lineLength / CURVE_FACTOR_BASE_OFFSET;
			arrowHeadPoint = (Point2D.Double) segment.lineT.transform(
				new Point2D.Double(segment.lineLength / 2.0, yOffset / 2.0), new Point2D.Double());
			Point2D ctrlPoint = new Point2D.Double(segment.lineLength / 2.0, yOffset);
			Point2D ctrlPointT = segment.lineT.transform(ctrlPoint, new Point2D.Double());
			QuadCurve2D curve = new QuadCurve2D.Double();
			curve.setCurve(segment.srcPtT, ctrlPointT, segment.destPtT);
			linePath.append(curve, false);
			lineShape = curve;
			quadCtrlPnt = ctrlPoint;
		}
	}

	/**
	 * Create a line path consisting of a series of segments Does nothing if
	 * there are no bendpoints.
	 */
	private void createBendPointLinePath() {
		if (bendPoints.length >= 3) { // since first and last points are actually
			double srcX = srcPoint.x;
			double srcY = srcPoint.y;
			double destX = destPoint.x;
			double destY = destPoint.y;

			// Transform the bendpoints to this coordinate system
			transformedBendPoints =	transformBendPoints(bendPoints, srcX, srcY, destX, destY);

			linePath.reset();
			linePath.moveTo((float) transformedBendPoints[1].getX(),
					(float) transformedBendPoints[1].getY());
			int i=2;
			// @tag Shrimp(sugiyama): Added curve based on bendpoints to support curvey sugiyama layout
			while (i < bendPoints.length-1) {
				if (transformedBendPoints[i].getIsControlPoint() &&
					transformedBendPoints[i+1].getIsControlPoint()) {
					linePath.curveTo((float)transformedBendPoints[i].getX(),
							(float)transformedBendPoints[i].getY(),
							(float)transformedBendPoints[i+1].getX(),
							(float)transformedBendPoints[i+1].getY(),
							(float)transformedBendPoints[i+2].getX(),
							(float)transformedBendPoints[i+2].getY());
					i += 3;
				}
				else {
					linePath.lineTo((float) transformedBendPoints[i].getX(),
						(float) transformedBendPoints[i].getY());
					i++;
				}
			}
			LayoutBendPoint secondlastPoint = transformedBendPoints[transformedBendPoints.length-3];
			LayoutBendPoint lastPoint = transformedBendPoints[transformedBendPoints.length-2];
			Segment lastSegment = new Segment(secondlastPoint.getX(), secondlastPoint.getY(),
					lastPoint.getX(), lastPoint.getY());
			arrowHeadSlope = lastSegment.lineSlope;
			// @tag Shrimp(sugiyama)
			if (Double.isNaN(arrowHeadSlope)) {
				arrowHeadSlope = Double.MAX_VALUE;
			}
			arrowHeadPoint = new Point2D.Double(lastPoint.getX(), lastPoint.getY());
		}
	}

	/**
	 * @tag Shrimp(Bendpoints) : Create a line path consisting of a series of segments Does nothing if there are no bendpoints.
	 */
	private LayoutBendPoint[] transformBendPoints(LayoutBendPoint[] bendPoints,
			double srcX, double srcY, double destX, double destY) {
		LayoutBendPoint[] result = new BendPoint[bendPoints.length];
		if (bendPoints.length > 0) {
			// Transform all bendpoints from the parent node's coordinate system to global coords
			for (int i = 0; i < bendPoints.length; i++) {
				Point2D.Double from = new Point2D.Double(bendPoints[i].getX(),
						bendPoints[i].getY());
				Point2D.Double to = new Point2D.Double();
				bendPointToGlobalTransform.transform(from, to);
				result[i] = new BendPoint(to.x, to.y, bendPoints[i].getIsControlPoint());
			}

			// Adjust first and last bendpoints for movement of the source or destination nodes
			final double MOVEMENT_TOLERANCE = 5; // How much node location discrepancy before moving
			double x = result[1].getX();
			double y = result[1].getY();
			double dx = srcX - result[0].getX();
			double dy = srcY - result[0].getY();
			x = (Math.abs(dx) > MOVEMENT_TOLERANCE) ? x+dx : x;
			y = (Math.abs(dy) > MOVEMENT_TOLERANCE) ? y+dy : y;
			result[1] = new BendPoint(x,y);

			x = result[result.length-2].getX();
			y = result[result.length-2].getY();
			dx = destX - result[result.length-1].getX();
			dy = destY - result[result.length-1].getY();
			x = (Math.abs(dx) > MOVEMENT_TOLERANCE) ? x+dx : x;
			y = (Math.abs(dy) > MOVEMENT_TOLERANCE) ? y+dy : y;
			result[result.length-2] = new BendPoint(x,y);
		}
		return result;
	}


	/**
	 * A line segment. Line segment set up and common calculations.
	 * @author Chris Bennett
	 *
	 */
	private class Segment {
		public double lineDx;
		public double lineDy;
		public double lineSlope;
		public double lineTheta;
		public double lineAngle;
		public double lineLength;
		public Point2D srcPtT;
		public Point2D midPtT;
		public Point2D destPtT;
		public AffineTransform lineT;

		/**
		 * Build a segment from the specified source to the specified destination
		 * @param srcX
		 * @param srcY
		 * @param destX
		 * @param destY
		 */
		public Segment(double srcX, double srcY, double destX, double destY) {

			this.lineDx = destX - srcX;
			this.lineDy = destY - srcY;
			this.lineSlope = lineDy / lineDx;
			this.lineTheta = Math.atan(- (1.0 / lineSlope));
			this.lineAngle = (lineDy < 0) ? lineTheta + (3.0/2.0)*Math.PI : lineTheta + (1.0/2.0)*Math.PI;
			this.lineLength = srcPoint.distance(destPoint);

			lineT = new AffineTransform();
			lineT.concatenate(AffineTransform.getRotateInstance(lineAngle, srcX, srcY));
			lineT.concatenate(AffineTransform.getTranslateInstance(srcX, srcY));

			Point2D.Double srcPt = new Point2D.Double(0, 0);
			Point2D.Double destPt = new Point2D.Double(lineLength, 0);
			Point2D.Double midPt = new Point2D.Double(lineLength/2.0, 0);
			srcPtT = lineT.transform(srcPt, new Point2D.Double());
			midPtT = lineT.transform(midPt, new Point2D.Double());
			destPtT = lineT.transform(destPt, new Point2D.Double());
		}

		/**
		 * Get a line based on this segment
		 * @return
		 */
		public Line2D getLine() {
			return new Line2D.Double(srcPtT, destPtT);
		}
	}
}
