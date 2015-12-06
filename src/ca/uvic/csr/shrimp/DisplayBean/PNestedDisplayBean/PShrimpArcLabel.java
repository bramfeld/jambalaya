/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Rob Lintern
 *
 * A label that labels an arc.
 */
public class PShrimpArcLabel extends PShrimpLabel implements ShrimpArcLabel {
    private static final double DEFAULT_X_OFFSET = 3.0; 
    private static final double DEFAULT_Y_OFFSET = 3.0; 
    
    private double xOffset = DEFAULT_X_OFFSET;
    private double yOffset = DEFAULT_Y_OFFSET;
    
    /**
     * @param displayBean
     * @param arc
     * @param font
     * @param text
     */
    public PShrimpArcLabel(DisplayBean displayBean, ShrimpArc arc, Font font, String text) {
        super(displayBean, arc, font, text);
		//setBackgroundColor(arc.getColor());
		setTextColor(arc.getColor());
		setBackgroundOpaque(false);
    }
    
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
//        if (highlighted) {
//            // draw a line from the label to the arc
//            ShrimpArc arc = (ShrimpArc) getLabeledObject();
//            Point2D.Double srcPoint = new Point2D.Double (arc.getSrcTerminal().getArcAttachPoint().x, arc.getSrcTerminal().getArcAttachPoint().y);
//            Point2D.Double destPoint = new Point2D.Double (arc.getDestTerminal().getArcAttachPoint().x, arc.getDestTerminal().getArcAttachPoint().y);
//            
//            double x1 = srcPoint.x + (destPoint.x - srcPoint.x)*0.75;
//            double y1 = srcPoint.y + (destPoint.y - srcPoint.y)*0.75;
//            Point2D.Double midPoint = new Point2D.Double(x1, y1);
//            try {
//                getTransform().inverseTransform(midPoint, midPoint);
//            } catch (NoninvertibleTransformException e) {
//                e.printStackTrace();
//                return;
//            }
//            x1 = midPoint.x;
//            y1 = midPoint.y;
//            double x2 = 0;
//            double y2 = getOuterBounds().height/2.0;
//            paintContext.getGraphics().setColor(Color.BLUE);
//            paintContext.getGraphics().setStroke(new BasicStroke (1.0f/(float)paintContext.getScale()));
//            paintContext.getGraphics().drawLine((int)(x1), (int)(y1), (int)(x2), (int)(y2));
//        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#addShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#removeShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
    }

//    /* (non-Javadoc)
//     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#displayObjectPositionChanged(java.util.Vector)
//     */
//    public void displayObjectPositionChanged(Vector currentFocusedOnObjects) {
//        ShrimpArc arc = (ShrimpArc) displayObject;
//        Point2D.Double srcPnt = arc.getSrcTerminal().getArcAttachPoint();
//        Point2D.Double destPnt = arc.getDestTerminal().getArcAttachPoint();
//        double x = 0;
//        double y = 0;
//        double viewScale = canvas.getCamera().getViewTransform().getScale();
//		if (arc.getSrcNode().equals(arc.getDestNode())) {
//            // if this is an self-looping arc then draw the label at the
//            // point at which a 45 degree angle from the attach point
//            // meets the edge of the arc.
//            double padding = 3.0;
//            double theta = Math.PI/4.0;
//            double radius = (AbstractLineArrowArcStyle.ARC_TO_SELF_ABSOLUTE_BASE_DIAMETER /* /viewScale */ *  arc.getCurveFactor())/2.0;
//            double dx = radius*Math.sin(theta);
//            double dy = dx;
//            x = srcPnt.x + radius + dx + padding ;
//            y = srcPnt.y - dy - padding ;
//        } else {           
//            double dx = destPnt.x - srcPnt.x;
//            double dy = destPnt.y - srcPnt.y;
//            x = srcPnt.x + dx/2.0; 
//            y = srcPnt.y + dy/2.0;
//        }
//		x += xOffset;
//        y += yOffset;
//		double labelScale = 1.0/(viewScale);
//		
//		AffineTransform newLabelTx = new AffineTransform();
//		newLabelTx.translate(x, y);
//		newLabelTx.scale (labelScale, labelScale);
//		setTransform(newLabelTx);
//    }
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#displayObjectPositionChanged(java.util.Vector)
     * @tag Shrimp(sequence) :  Add bendpoint handling
     */
    public void displayObjectPositionChanged(Vector currentFocusedOnObjects) {
        ShrimpArc arc = (ShrimpArc) displayObject;
        Point2D.Double srcPnt = arc.getSrcTerminal().getArcAttachPoint();
        Point2D.Double destPnt = arc.getDestTerminal().getArcAttachPoint();
        Point2D.Double displayPoint;
        double viewScale = canvas.getCamera().getViewTransform().getScale();
        
		if (arc.getSrcNode().equals(arc.getDestNode())) {
			displayPoint = getSelfLoopPosition(arc, srcPnt);
        } else {           
        	displayPoint = getRegularPosition(arc, srcPnt, destPnt);
        }

		double labelScale = 1.0/(viewScale);
		
		AffineTransform newLabelTx = new AffineTransform();
		newLabelTx.translate(displayPoint.x, displayPoint.y);
		newLabelTx.scale (labelScale, labelScale);
		setTransform(newLabelTx);
    }
    
    /**
     * If this is a self-looping arc then draw the label at 
     * the point at which a 45 degree angle from the attach 
     * point meets the edge of the arc.
     * If this arc has bendpoints, then use these to find the label position, instead. 
     */
    private Point2D.Double getSelfLoopPosition(ShrimpArc arc, Point2D.Double srcPnt) {
        Point2D.Double displayPoint;
    	if (arc.getStyle().hasBendPoints()) {
    		displayPoint = getBendPointLabelPosition(arc.getStyle().getBendPoints());
    	}
    	else {
    		displayPoint = new Point2D.Double();
    		double padding = 3.0;
    		double theta = Math.PI / 4.0;
			double radius = (AbstractLineArrowArcStyle.ARC_TO_SELF_ABSOLUTE_BASE_DIAMETER /* /viewScale */* arc
					.getCurveFactor()) / 2.0;
			double dx = radius * Math.sin(theta);
			double dy = dx;
			displayPoint.x = srcPnt.x + radius + dx + padding + xOffset;
			displayPoint.y = srcPnt.y - dy - padding + yOffset;
		}
		return displayPoint;
	}   
    
    /**
     * If this is a regular arc then draw the label at the midpoint
     * If this arc has bendpoints, then use these to find the label position, instead. 
     */
    private Point2D.Double getRegularPosition(ShrimpArc arc, Point2D.Double srcPnt,
    		Point2D.Double destPnt) {
        Point2D.Double displayPoint;
    	if (arc.getStyle().hasBendPoints()) {
    		displayPoint = getBendPointLabelPosition(arc.getStyle().getTransformedBendPoints());
    	}
    	else {
    		displayPoint = new Point2D.Double();
    		double dx = destPnt.x - srcPnt.x;
    		double dy = destPnt.y - srcPnt.y;
    		displayPoint.x = srcPnt.x + dx/2.0; 
    		displayPoint.y = srcPnt.y + dy/2.0;
    	}
		return displayPoint;
    }

    /**
     * Return a position that is above the middle of the first located horizontal 
     * bendpoint segment or in the middle of the first segment if no horizontal segment is 
     * found.
     * e.g., 
     *          Label
     *     *-------------*
     *                   |
     *                   |
     *     *-------------*
     *     
     *  or:
     *     *
     *     |
     *     |     Label
     *     *-------------*
     *                   |
     *                   |
     *     *-------------*
     *     |
     *     |
     *     *
     * @param bendPoints
     * @return
     */
    private Point2D.Double getBendPointLabelPosition(LayoutBendPoint[] bendPoints) {
    	// Start at 2nd bendpoint - ignore first and last bendpoints, which are
    	// actually the node source and destination points
    	int startIndex = 1;
    	boolean found = false;
    	final int LEFT_OFFSET = 2; // somewhat arbitrary offsets
    	final int ABOVE_OFFSET = 15;

    	double viewScale = canvas.getCamera().getViewTransform().getScale();
    	double xAdjust = (-LEFT_OFFSET * pText.getText().length()/2)/viewScale;; // place to left of center
    	double yAdjust = -ABOVE_OFFSET/viewScale; // place above line
    	
		for (int i = 2; i < bendPoints.length-1; i++) {
			if (bendPoints[i].getY() == bendPoints[i-1].getY()) { // found a horizontal segment
				startIndex = i-1;
				found = true;
				break;
			}
		}
		double labelX = 
			getMidPoint(bendPoints[startIndex].getX(), bendPoints[startIndex+1].getX());
        
		double labelY;
		if (found) {
        	labelY = bendPoints[startIndex].getY();
        } else {
        	labelY = 
        		getMidPoint(bendPoints[startIndex].getY(), bendPoints[startIndex+1].getY());
        }
		return new Point2D.Double(labelX + xAdjust, labelY + yAdjust);
    }

    /**
     * Return the value halfway between n1 and n2
     * @param n1
     * @param n2
     * @return
     */
    private double getMidPoint(double n1, double n2) {
		double distance = Math.abs(n1-n2);
		return n1 < n2 ? n1 + distance/2 : n2 + distance/2;
    }

	
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#isInAreaOfInterest(java.util.Vector)
     */
    protected boolean isInAreaOfInterest(Vector currentFocusedOnObjects) {
        return true;
    }
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#updateVisibility(java.util.Vector)
     */
    public void updateVisibility(Vector currentFocusedOnObjects) {
        ShrimpArc arc = (ShrimpArc) displayObject;
		boolean arcVisible = displayBean.isVisible (arc);
		boolean newVisibility = getParent() != null && arcVisible /*&& arc.isActive()*/;
		if (newVisibility != isVisible()) {
			setVisible(newVisibility);		
		}
    }

    /**
     * @param dx
     * @param dy
     */
    public void moveOffset(double dx, double dy) {
		double viewScale = canvas.getCamera().getViewTransform().getScale();
        xOffset += dx/viewScale;
        yOffset += dy/viewScale;
        displayObjectPositionChanged();
    }

}
