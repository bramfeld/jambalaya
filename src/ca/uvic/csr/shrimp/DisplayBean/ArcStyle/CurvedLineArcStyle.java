/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;

/**
 *
 * @author Rob Lintern
 */
public abstract class CurvedLineArcStyle extends AbstractLineArrowArcStyle {

	private final static double CURVE_CONSTANT = 20.0;
	private final static Point2D.Double CP_1_RATIO = new Point2D.Double(0.0d, 0.5d); // first control point for cubic curved arc
	private final static Point2D.Double CP_2_RATIO = new Point2D.Double(1.0d, 0.5d); // second control point for cubic curved arc
	private final static boolean USE_OLD_TECHNIQUE = true;

	public CurvedLineArcStyle(String name) {
		super(name);
	}

	public CurvedLineArcStyle(String name, boolean isDashed) {
		super(name, isDashed);
	}

	protected void createLinePath() {
		if (USE_OLD_TECHNIQUE) {
			createLinePathOld();
		} else {
			createLinePathNew();
		}
	}

	// arc is curved no matter what angle it is at, doesn't look as good in trees
	protected void createLinePathNew() {
		linePath.reset();
		double lineDx = destPoint.x - srcPoint.x;
		double lineDy = destPoint.y - srcPoint.y;
		double lineSlope = lineDy / lineDx;
		double lineTheta = Math.atan(-(1.0 / lineSlope));
		double lineAngle = (lineDy < 0) ? lineTheta + (3.0 / 2.0) * Math.PI : lineTheta + (1.0 / 2.0) * Math.PI;
		double lineLength = srcPoint.distance(destPoint);
		AffineTransform lineT = new AffineTransform();
		AffineTransform rotateLineT = AffineTransform.getRotateInstance(lineAngle, srcPoint.x, srcPoint.y);
		AffineTransform transLineT = AffineTransform.getTranslateInstance(srcPoint.x, srcPoint.y);
		lineT.concatenate(rotateLineT);
		lineT.concatenate(transLineT);
		Point2D.Double srcPt = new Point2D.Double(0, 0);
		Point2D.Double destPt = new Point2D.Double(lineLength, 0);
		Point2D.Double midPt = new Point2D.Double(lineLength / 2.0, 0);
		// the distance that the ctrl points should be offset in the y direction
		double yOffset = ((curveFactor + 1) * lineLength / 4.0);
		Point2D.Double ctrlPoint1 = new Point2D.Double(lineLength / 2.0, -yOffset);
		Point2D.Double ctrlPoint2 = new Point2D.Double(lineLength / 2.0, yOffset);

		Point2D srcPtT = lineT.transform(srcPt, new Point2D.Double());
		Point2D destPtT = lineT.transform(destPt, new Point2D.Double());
		Point2D midPtT = lineT.transform(midPt, new Point2D.Double());
		Point2D ctrlPoint1T = lineT.transform(ctrlPoint1, new Point2D.Double());
		Point2D ctrlPoint2T = lineT.transform(ctrlPoint2, new Point2D.Double());

		CubicCurve2D.Double curve = new CubicCurve2D.Double();
		curve.setCurve(srcPtT, ctrlPoint1T, ctrlPoint2T, destPtT);
		arrowHeadPoint = new Point2D.Double(midPtT.getX(), midPtT.getY());
		// rotate arrow head an additional 15 degrees (calculated by trial and error!)
		// still not quite right when slope is large
		arrowHeadSlope = lineSlope + Math.tan(Math.PI / 6.0);

		linePath.append(curve, false);
		lineShape = curve;
		cubicCtrlPnt1 = ctrlPoint1T;
		cubicCtrlPnt2 = ctrlPoint2T;
	}

	// this way looks much better with trees
	protected void createLinePathOld() {
		linePath.reset();
		Point2D.Double ctrlPoint1 = null;
		Point2D.Double ctrlPoint2 = null;
		CubicCurve2D.Double curve = new CubicCurve2D.Double();
		double d = curveFactor * CURVE_CONSTANT;
		double dx = destPoint.getX() - srcPoint.getX();
		double dy = destPoint.getY() - srcPoint.getY();

		double slope = (destPoint.y - srcPoint.y) / (destPoint.x - srcPoint.x);

		ctrlPoint1 = new Point2D.Double(srcPoint.getX() + dx * CP_1_RATIO.getX() - d,
										srcPoint.getY() + dy * CP_1_RATIO.getY() - d);
		ctrlPoint2 = new Point2D.Double(srcPoint.getX() + dx * CP_2_RATIO.getX() + d,
										srcPoint.getY() + dy * CP_2_RATIO.getY() + d);

		curve.setCurve(srcPoint, ctrlPoint1, ctrlPoint2, destPoint);

		Point2D.Double refPoint1 = new Point2D.Double(srcPoint.x + ((destPoint.x - srcPoint.x) * 3 / 4), srcPoint.y
				+ ((destPoint.y - srcPoint.y) * 3 / 4));
		arrowHeadPoint = new Point2D.Double(ctrlPoint2.x + ((refPoint1.x - ctrlPoint2.x) * 3 / 4), ctrlPoint2.y
				+ ((refPoint1.y - ctrlPoint2.y) * 3 / 4));
		arrowHeadSlope = slope;
		linePath.append(curve, false);
		lineShape = curve;
		cubicCtrlPnt1 = ctrlPoint1;
		cubicCtrlPnt2 = ctrlPoint2;
	}

	protected void setThumbnailPoints(Point2D.Double srcPoint, Point2D.Double destPoint, int width, int height) {
		srcPoint.setLocation(0, height / 2.0 + height / 10.0);
		destPoint.setLocation(width, height / 2.0 + height / 10.0);
	}

}
