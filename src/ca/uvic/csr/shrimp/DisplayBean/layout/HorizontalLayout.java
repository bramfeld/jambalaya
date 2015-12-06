/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.algorithms.HorizontalLayoutAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;

/**
 * Displays the nodes in a horizontal grid (1 row).
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class HorizontalLayout extends AbstractLayout {

	private final boolean noOverlapping;
	
    /**
     * @param displayBean
     */
    public HorizontalLayout(DisplayBean displayBean) {
        this(displayBean, false);
    }

    /**
     * @param displayBean
     * @param noOverlappingLabels if true then the nodes are repositioned <b>after</b> the layout has occurred
     * to try and reduce overlapping labels
     */
    public HorizontalLayout(DisplayBean displayBean, boolean noOverlappingLabels) {
        super(displayBean, 
        	  (noOverlappingLabels ? LayoutConstants.LAYOUT_HORIZONTAL_NO_OVERLAP : LayoutConstants.LAYOUT_HORIZONTAL), 
        	  new HorizontalLayoutAlgorithm());
        this.noOverlapping = noOverlappingLabels;
    }
    
    /**
     * Iterate through all the nodes, positions, and dimensions.  If any node label is wider
     * than the node, the subsequent nodes are shifted to the right.  
     * @see AbstractLayout#adjustPositionsAndSizesAfterLayout(Vector, Vector, Vector)
     */
    protected void adjustPositionsAndSizesAfterLayout(Vector nodes, Vector positions, Vector dimensions) {
    	if (noOverlapping && (nodes.size() == positions.size()) && (nodes.size() == dimensions.size())) {
    		// @tag Shrimp(HorizontalLayout(NoOverlap))   
    		// TODO this doesn't work so well if there are separate trees (e.g. Bingo)
    		// then the nodes from the different trees overlap
    		// it works reasonable well for a single row of nodes (especially with scrollbars turned on)
    		
    		double x = 0;
	    	for (int i = 0; i < nodes.size(); i++) {
	    		PShrimpNode node = (PShrimpNode) nodes.get(i);
	    		Point2D.Double position = (Point2D.Double) positions.get(i);
	    		Dimension size = (Dimension) dimensions.get(i);
	    		
	    		boolean aboveNode = DisplayConstants.LABEL_MODE_FIXED.equals(node.getLabelMode()) || 
	    							DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL.equals(node.getLabelMode());
	    		
	    		
 				double halfLabelWidth = (node.getLabelBounds().width / 2) + 4;
 				double halfNodeWidth = size.width / 2;
 				
 				if (x == 0) {
 					x = position.x;
 				} else {
 					x += Math.max(halfLabelWidth, halfNodeWidth);
 				}
	    		x = Math.max(x, position.x);
	    		if (aboveNode && (x > position.x)) {
	    			position.x = x;
	    		}
	    		
	    		x += Math.max(halfLabelWidth, halfNodeWidth);
	    	}
    	}
    }
    
}
