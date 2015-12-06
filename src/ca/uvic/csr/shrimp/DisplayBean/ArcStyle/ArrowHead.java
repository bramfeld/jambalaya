/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

/**
 * Code for visualizing arrow heads. Note that this could have been written using
 * multiple subclasses, but doing it in a single class should make maintenance and
 * understanding easier.
 * @author Chris Bennett
 */
public class ArrowHead {

	public static final String FULL_UNFILLED = "Full_Unfilled";
	public static final String FULL_FILLED = "Full_Filled";
	public static final String OPEN = "Open";
	public static final String DEFAULT_STYLE = FULL_UNFILLED;

	private static final int ARROW_SIZE_CONSTANT = 4;


	/**
	 * Create an arrow head path
	 * @param type
	 * @param arrowHeadSlope
	 * @param arrowHeadPoint
	 * @param magnification
	 * @param active
	 * @param highlighted
	 * @param srcX
	 * @param destX
	 * @return a GeneralPath
	 */
	public static GeneralPath create(String type, double arrowHeadSlope, Point2D arrowHeadPoint, double magnification,
									 boolean active, boolean highlighted, double srcX, double destX) {

		// Note that filling is actually done in AbstractLineArrowArcStyle by Graphics2d
		if (type.equals(FULL_FILLED) || type.equals(FULL_UNFILLED)) {
			return createFullArrowHead(arrowHeadSlope, arrowHeadPoint, magnification, active, highlighted, srcX, destX);
		} else if (type.equals(OPEN)) {
			return createOpenArrowHead(arrowHeadSlope, arrowHeadPoint, magnification, active, highlighted, srcX, destX);
		} else { // Default
			return createFullArrowHead(arrowHeadSlope, arrowHeadPoint, magnification, active, highlighted, srcX, destX);
		}
	}

	/**
	 * Create a fully enclosed arrow head
	 */
	private static GeneralPath createFullArrowHead(double arrowHeadSlope, Point2D arrowHeadPoint, double magnification,
												   boolean active, boolean highlighted, double srcX, double destX) {
		GeneralPath arrowHeadPath = createOpenArrowHead(arrowHeadSlope, arrowHeadPoint, magnification,
														active, highlighted, srcX, destX);
		arrowHeadPath.closePath();
		return arrowHeadPath;
	}

	/**
	 * Create an open arrow head
	 */
	private static GeneralPath createOpenArrowHead(double arrowHeadSlope, Point2D arrowHeadPoint, double magnification,
												   boolean active, boolean highlighted, double srcX, double destX) {
		// TODO - create an open arrow head
		GeneralPath arrowHeadPath = new GeneralPath();
		double theta1 = Math.atan(arrowHeadSlope);
		double arrowHeight = ARROW_SIZE_CONSTANT / magnification;
		if (active) {
		    arrowHeight *= 1.5;
		}
		if (highlighted) {
			arrowHeight *= 1.5;
		}
		double dx = arrowHeight * Math.cos(theta1) * 1.25;
		double dy = arrowHeight * Math.sin(theta1) * 1.25;

		double headX;
		double headY;
		double tailX;
		double tailY;
		if (destX >= srcX) {
			headX = arrowHeadPoint.getX() + dx;
			headY = arrowHeadPoint.getY() + dy;
			tailX = arrowHeadPoint.getX() - dx;
			tailY = arrowHeadPoint.getY() - dy;
		} else {
			headX = arrowHeadPoint.getX() - dx;
			headY = arrowHeadPoint.getY() - dy;
			tailX = arrowHeadPoint.getX() + dx;
			tailY = arrowHeadPoint.getY() + dy;
		}

		double theta2 = Math.atan(- (1 / arrowHeadSlope));
		dx = arrowHeight * Math.cos(theta2);
		dy = arrowHeight * Math.sin(theta2);

		double tailX1 = tailX + dx;
		double tailY1 = tailY + dy;
		double tailX2 = tailX - dx;
		double tailY2 = tailY - dy;
		arrowHeadPath.reset();
		arrowHeadPath.moveTo((float) tailX1, (float) tailY1);
		arrowHeadPath.lineTo((float) headX, (float) headY);
		arrowHeadPath.lineTo((float) tailX2, (float) tailY2);
		return arrowHeadPath;
	}

	/**
	 * @param arrowHeadStyle
	 * @return true if the specfied style should be filled
	 */
	public static boolean isFilled(String arrowHeadStyle) {
		return arrowHeadStyle.equals(FULL_FILLED);
	}
}
