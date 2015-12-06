/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

/**
 * Defines an abstract line and arrow arc style.
 * The line can be curved or straight, dashed or solid.
 * A line arrow arc style is basically composed of a
 * a line (a general path), a basic stroke, and an optional
 * simple arrowhead.
 *
 * @author Rob Lintern
 */
public abstract class AbstractLineArrowArcStyle extends AbstractArcStyle implements LineArrowArcStyle {

	public static final double ARC_TO_SELF_ABSOLUTE_BASE_DIAMETER = 20.0;
	private static final double CONNECTOR_ABSOLUTE_DIAMETER = 6.0;
	private final static boolean SHOW_CONNECTORS_DEFAULT = true;
	private final static boolean USE_ARROW_HEAD_CONNECTORS_DEFAULT = true;

	/** The stroke that this style is currently using. */
	protected BasicStroke basicStroke;
	protected BasicStroke lowQualityBasicStroke;

	private GeneralPath srcConnectorPath = new GeneralPath();
	private GeneralPath destConnectorPath = new GeneralPath();

	protected Shape lineShape = new Line2D.Double();
	protected GeneralPath linePath = new GeneralPath();
	private GeneralPath lineAndArrowHeadPath = new GeneralPath();
	protected GeneralPath arrowHeadPath = new GeneralPath();

	private Shape strokedShape;

	protected boolean useArrowHeads = true;

	protected Point2D.Double arrowHeadPoint = new Point2D.Double(0, 0);
	protected double arrowHeadSlope = 0.0;

	protected boolean isDashed = false;

	protected boolean showConnectors = SHOW_CONNECTORS_DEFAULT;

	private boolean useArrowHeadConnectors = USE_ARROW_HEAD_CONNECTORS_DEFAULT;

	// for debugging
	private boolean renderCtrlPoints = false;
	protected Point2D quadCtrlPnt = null;
	protected Point2D cubicCtrlPnt1 = null;
	protected Point2D cubicCtrlPnt2 = null;

	private boolean strokedShapeIsDirty = true;

	private boolean strokeIsDirty = true;

	private String arrowHeadStyle = ArrowHead.DEFAULT_STYLE;

	/**
	 * @param name
	 */
	public AbstractLineArrowArcStyle(String name) {
		this(name, false);
	}

	/**
	 *
	 * @param name
	 * @param isDashed
	 */
	public AbstractLineArrowArcStyle(String name, boolean isDashed) {
		super(name);
		this.isDashed = isDashed;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#render(Graphics2D, int, Color, boolean, boolean)
	 */
	public void render(Graphics2D g2, int renderingQuality, Color arcColor, boolean showSrcConnector, boolean showDestConnector) {
		double d = Point2D.distance(srcPoint.x, srcPoint.y, destPoint.x, destPoint.y);
		if (d * mag > 500000) {
			return; // otherwise we get some strange exceptions thrown
		}
		g2.setStroke(getBasicStroke());
		g2.setColor(arcColor);

		// draw the line
		g2.draw(linePath);

		// draw the arrow head if neccessary
		if (useArrowHeads) {
			if (ArrowHead.isFilled(arrowHeadStyle)) {
				g2.fill(arrowHeadPath);
			} else if (renderingQuality == HIGH_QUALITY_RENDERING) {
				g2.setColor(Color.WHITE);
				g2.fill(arrowHeadPath);
			}
			g2.setColor(arcColor);
			g2.setStroke(new BasicStroke((float) (1.0 / mag)));
			g2.draw(arrowHeadPath);
		}

		//draw "connectors" at end of line
		if (showConnectors) {
			if (showSrcConnector) {
				if (renderingQuality == HIGH_QUALITY_RENDERING) {
					g2.setColor(Color.WHITE);
					g2.fill(srcConnectorPath);
				}
				g2.setColor(arcColor);
				g2.setStroke(new BasicStroke((float) (1.0 / mag)));
				g2.draw(srcConnectorPath);
			}
			if (showDestConnector) {
				if (renderingQuality == HIGH_QUALITY_RENDERING) {
					g2.setColor(Color.WHITE);
					g2.fill(destConnectorPath);
				}
				g2.setColor(arcColor);
				g2.setStroke(new BasicStroke((float) (1.0 / mag)));
				g2.draw(destConnectorPath);
			}
		}

		// for debugging
		if (renderCtrlPoints) {
			double diam = 6.0 / mag;

			if (quadCtrlPnt != null) {
				g2.setColor(Color.BLUE);
				g2.fill(new Ellipse2D.Double(quadCtrlPnt.getX() - diam / 2.0, quadCtrlPnt.getY() - diam / 2.0, diam, diam));
			}
			if (cubicCtrlPnt1 != null) {
				g2.setColor(Color.RED);
				g2.fill(new Ellipse2D.Double(cubicCtrlPnt1.getX() - diam / 2.0, cubicCtrlPnt1.getY() - diam / 2.0, diam, diam));
			}
			if (cubicCtrlPnt2 != null) {
				g2.setColor(Color.GREEN);
				g2.fill(new Ellipse2D.Double(cubicCtrlPnt2.getX() - diam / 2.0, cubicCtrlPnt2.getY() - diam / 2.0, diam, diam));
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#intersects(java.awt.geom.Rectangle2D)
	 */
	public boolean intersects(Rectangle2D r) {
		//TODO make stroke a bit bigger so that user does not have to be so exact with cursor
		return getStrokedShape().intersects(r);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getBounds()
	 */
	public Rectangle2D getBounds() {
		Rectangle2D bounds = getStrokedShape().getBounds2D();
		float xf = ((Rectangle2D.Float) bounds).x;
		double xd = bounds.getX();
		if (Float.isNaN(xf) || Double.isNaN(xd)) {
			bounds = new Rectangle2D.Float(0, 0, 0, 0);
		}
		return bounds;
	}

	private Shape getStrokedShape() {
		if (strokedShapeIsDirty) {
			strokedShape = getLowQualityStroke().createStrokedShape(lineAndArrowHeadPath);
			strokedShapeIsDirty = false;
		}
		return strokedShape;

	}

	protected abstract void createLinePath();

	protected void recreateLineAndArrowHead() {
		if (isVisible) {
			lineAndArrowHeadPath = new GeneralPath();
			if (srcPoint.equals(destPoint)) {
				createArcToSelf();
			} else {
				createLinePath();
			}

			lineAndArrowHeadPath.append(linePath, false);
			if (useArrowHeads) {
				createArrowHead();
				lineAndArrowHeadPath.append(arrowHeadPath, false);
			}
			if (showConnectors) {
				createAndAppendConnectors();
			}
		}
	}

	private void createAndAppendConnectors() {
		if (!useArrowHeadConnectors) {
			createAndAppendCircularConnectors();
		} else {
			createAndAppendArrowHeadConnectors();
		}
	}

	private void createAndAppendCircularConnectors() {
		double connectorDiameter = CONNECTOR_ABSOLUTE_DIAMETER / mag; //pixels
		double x = srcPoint.x - connectorDiameter / 2.0;
		double y = srcPoint.y - connectorDiameter / 2.0;
		srcConnectorPath.reset();
		srcConnectorPath.append(new Ellipse2D.Double(x, y, connectorDiameter, connectorDiameter), true);

		x = destPoint.x - connectorDiameter / 2.0;
		y = destPoint.y - connectorDiameter / 2.0;
		destConnectorPath.reset();
		destConnectorPath.append(new Ellipse2D.Double(x, y, connectorDiameter, connectorDiameter), true);
	}

	private void createAndAppendArrowHeadConnectors() {
		double lineDx = destPoint.x - srcPoint.x;
		double lineDy = destPoint.y - srcPoint.y;
		double lineSlope = lineDy / lineDx;
		double lineTheta = Math.atan(-(1.0 / lineSlope));
		double lineAngle = (lineDy < 0) ? lineTheta + (3.0 / 2.0) * Math.PI : lineTheta + Math.PI / 2.0;
		double srcArrowHeadAngle = 0;
		double destArrowHeadAngle = 0;
		if (lineShape instanceof Line2D) {
			srcArrowHeadAngle = lineAngle;
			destArrowHeadAngle = lineAngle;
		} else if (lineShape instanceof CubicCurve2D) {
			CubicCurve2D cubic2D = (CubicCurve2D) lineShape;
			Point2D ctrl1 = cubic2D.getCtrlP1();
			Point2D ctrl2 = cubic2D.getCtrlP2();
			double dx = ctrl1.getX() - srcPoint.x;
			double dy = ctrl1.getY() - srcPoint.y;
			double slope = dy / dx;
			double theta = Math.atan(-(1.0 / slope));
			srcArrowHeadAngle = (dy < 0) ? theta + (3.0 / 2.0) * Math.PI : theta + Math.PI / 2.0;
			double dx2 = destPoint.x - ctrl2.getX();
			double dy2 = destPoint.y - ctrl2.getY();
			double slope2 = dy2 / dx2;
			double theta2 = Math.atan(-(1.0 / slope2));
			destArrowHeadAngle = (dy2 < 0) ? theta2 + (3.0 / 2.0) * Math.PI : theta2 + Math.PI / 2.0;
		} else if (lineShape instanceof QuadCurve2D) {
			QuadCurve2D quad2D = (QuadCurve2D) lineShape;
			Point2D ctrl = quad2D.getCtrlPt();
			double dx1 = ctrl.getX() - srcPoint.x;
			double dy1 = ctrl.getY() - srcPoint.y;
			double slope1 = dy1 / dx1;
			double theta1 = Math.atan(-(1.0 / slope1));
			srcArrowHeadAngle = (dy1 < 0) ? theta1 + (3.0 / 2.0) * Math.PI : theta1 + Math.PI / 2.0;
			double dx2 = destPoint.x - ctrl.getX();
			double dy2 = destPoint.y - ctrl.getY();
			double slope2 = dy2 / dx2;
			double theta2 = Math.atan(-(1.0 / slope2));
			destArrowHeadAngle = (dy2 < 0) ? theta2 + (3.0 / 2.0) * Math.PI : theta2 + Math.PI / 2.0;
		}

		GeneralPath arrowPath = new GeneralPath();
		double arrowHeadWidth = 7.0 / mag;
		double arrowHeadLength = arrowHeadWidth * 1.25;

		Point2D.Double[] pts = new Point2D.Double[] { new Point2D.Double(0, -arrowHeadWidth / 2.0), new Point2D.Double(arrowHeadLength, 0),
				new Point2D.Double(0, arrowHeadWidth / 2.0), };
		for (int i = 0; i < pts.length; i++) {
			Point2D.Double pt = pts[i];
			if (i == 0) {
				arrowPath.moveTo((float) pt.x, (float) pt.y);
			} else {
				arrowPath.lineTo((float) pt.x, (float) pt.y);
			}
		}
		arrowPath.closePath();

		// sometimes the arrow head angle is not a valid number, so we just check it here to make sure
		if (Double.isNaN(srcArrowHeadAngle) || Double.isInfinite(srcArrowHeadAngle)) {
			srcArrowHeadAngle = 0.0;
		}
		if (Double.isNaN(destArrowHeadAngle) || Double.isInfinite(destArrowHeadAngle)) {
			destArrowHeadAngle = 0.0;
		}

		srcConnectorPath.reset();
		AffineTransform srcT = new AffineTransform();
		AffineTransform rotateSrcArrowHeadT = AffineTransform.getRotateInstance(srcArrowHeadAngle, srcPoint.x, srcPoint.y);
		AffineTransform transSrcT = AffineTransform.getTranslateInstance(srcPoint.x, srcPoint.y);
		srcT.concatenate(rotateSrcArrowHeadT);
		srcT.concatenate(transSrcT);
		// @tag Shrimp.Java6 : this shape was cast into a GeneralPath - it throws a ClassCastException in java 1.6
		Shape srcArrowHeadTransformed = srcT.createTransformedShape(arrowPath);
		srcConnectorPath.append(srcArrowHeadTransformed, true);

		destConnectorPath.reset();
		AffineTransform transDestT = AffineTransform.getTranslateInstance(destPoint.x - arrowHeadLength, destPoint.y);
		AffineTransform rotateDestArrowHeadT = AffineTransform.getRotateInstance(destArrowHeadAngle, destPoint.x, destPoint.y);
		AffineTransform destT = new AffineTransform();
		destT.concatenate(rotateDestArrowHeadT);
		destT.concatenate(transDestT);
		Shape destArrowHeadTransformed = destT.createTransformedShape(arrowPath);
		destConnectorPath.append(destArrowHeadTransformed, true);
	}

	private Stroke getLowQualityStroke() {
		if (strokeIsDirty) {
			getBasicStroke();
		}
		return lowQualityBasicStroke;
	}

	private Stroke getBasicStroke() {
		if (strokeIsDirty) {
			double absoluteStrokeSize = weight;
			if (highlighted) {
				absoluteStrokeSize += HIGHLIGHT_THICKNESS;
			}
			if (active) {
				absoluteStrokeSize += ACTIVE_THICKNESS;
			}
			float strokeSize = (float) (absoluteStrokeSize / mag);
			if (Float.isNaN(strokeSize) || Float.isInfinite(strokeSize) || strokeSize == 0.0f) {
				System.err.println("strokeSize is not valid: " + strokeSize);
				strokeSize = 1.0f;
			}

			if (isDashed) {
				float spaceWidth = 3.0f + (float) absoluteStrokeSize;
				float absoluteDashWidth = (float) Math.max(0.1, 10.0 / mag);
				float absoluteSpaceWidth = (float) Math.max(0.1, spaceWidth / mag);
				float[] dash = { absoluteDashWidth, absoluteSpaceWidth };
				try {
					lowQualityBasicStroke = new BasicStroke(strokeSize);
					basicStroke = new BasicStroke(strokeSize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
				} catch (RuntimeException e) {
					//e.printStackTrace();
					basicStroke = new BasicStroke(strokeSize); // to catch any problems with creating a dashed stroke
				}
			} else {
				basicStroke = new BasicStroke(strokeSize);
				lowQualityBasicStroke = basicStroke;
			}
			strokeIsDirty = false;
		}
		return basicStroke;
	}

	private void recreateStroke() {
		strokeIsDirty = true;
	}

	private void recreateStrokedShape() {
		strokedShapeIsDirty = true;
	}

	/**
	 * Create an arrow head for this line using the current arrow head style. The
	 * location is dependent on whether or not bendpoints are used.
	 */
	protected void createArrowHead() {
		double destX = destPoint.x;
		double srcX = srcPoint.x;
		if (bendPoints != null && bendPoints.length >= 3) {
			// Use the last bendpoint segment to select arrow direction calcs
			if (transformedBendPoints == null) {
				System.out.println("AbstractLineArrowArcStyle.createArrowHead(): bendpoints are null!");
			} else {
				// use 2nd last bendpoint (last is actually the destination node point)
				destX = transformedBendPoints[bendPoints.length - 2].getX();
				srcX = transformedBendPoints[bendPoints.length - 3].getX();
			}
		}
		arrowHeadPath = ArrowHead.create(this.arrowHeadStyle, arrowHeadSlope, arrowHeadPoint, mag, active, highlighted, srcX, destX);
	}

	private void createArcToSelf() {
		linePath.reset();
		double diam = ARC_TO_SELF_ABSOLUTE_BASE_DIAMETER /* /mag */* curveFactor;
		linePath.append(new Ellipse2D.Double(srcPoint.x, srcPoint.y - diam / 2.0, diam, diam), false);
		arrowHeadPoint = new Point2D.Double(srcPoint.x + diam, srcPoint.y);
		arrowHeadSlope = -Double.MAX_VALUE;
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.LineArrowArcStyle#isUsingArrowHeads()
	 */
	public boolean isUsingArrowHeads() {
		return useArrowHeads;
	}

	/**
	 *
	 * @see LineArrowArcStyle#setUseArrowHeads(boolean, String)
	 */
	public void setUseArrowHeads(boolean useArrowHeads, String style) {
		this.useArrowHeads = useArrowHeads;
		this.arrowHeadStyle = style;
		recreateLineAndArrowHead();
		recreateStrokedShape();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setViewMagnification(double)
	 */
	public void setViewMagnification(double mag) {
		if (this.mag != mag) {
			// a change in magnification will cause the arrow head to change size
			this.mag = mag;
			if (useArrowHeads) {
				recreateLineAndArrowHead();
			}
			recreateStroke();
			recreateStrokedShape();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setCurveFactor(int)
	 */
	public void setCurveFactor(int curveFactor) {
		if (this.curveFactor != curveFactor) {
			this.curveFactor = curveFactor;
			recreateLineAndArrowHead();
			recreateStrokedShape();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setHighlighted(boolean)
	 */
	public void setHighlighted(boolean highlighted) {
		if (this.highlighted != highlighted) {
			// a change in highlighted will cause arrow head to get bigger and stroke to change
			this.highlighted = highlighted;
			if (useArrowHeads) {
				recreateLineAndArrowHead();
			}
			recreateStroke();
			recreateStrokedShape();
		}
	}

	public void setActive(boolean active) {
		if (this.active != active) {
			// a change in highlighted will cause arrow head to get bigger and stroke to change
			this.active = active;
			if (useArrowHeads) {
				recreateLineAndArrowHead();
			}
			recreateStroke();
			recreateStrokedShape();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setWeight(double)
	 */
	public void setWeight(double weight) {
		if (this.weight != weight) {
			this.weight = weight;
			recreateLineAndArrowHead();
			recreateStroke();
			recreateStrokedShape();
		}

	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (this.isVisible != visible) {
			this.isVisible = visible;
			recreateLineAndArrowHead();
			recreateStroke();
			recreateStrokedShape();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setSrcDestPoints(Point2D.Double, Point2D.Double)
	 */
	public void setSrcDestPoints(Point2D.Double srcPoint, Point2D.Double destPoint) {
		if (!this.srcPoint.equals(srcPoint) || !this.destPoint.equals(destPoint)) {
			this.srcPoint = (Point2D.Double) srcPoint.clone();
			this.destPoint = (Point2D.Double) destPoint.clone();
			recreateLineAndArrowHead();
			recreateStrokedShape();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setSrcDestPoints(Point2D.Double, Point2D.Double)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#setSrcDestPoints(Point2D.Double, Point2D.Double, AffineTransform)
	 */
	public void setSrcDestPoints(Point2D.Double srcPoint, Point2D.Double destPoint, AffineTransform bendPointToGlobalTransform) {
		this.bendPointToGlobalTransform = bendPointToGlobalTransform;
		setSrcDestPoints(srcPoint, destPoint);
	}

	public Point2D.Double getArrowHeadPoint() {
		return arrowHeadPoint;
	}

}
