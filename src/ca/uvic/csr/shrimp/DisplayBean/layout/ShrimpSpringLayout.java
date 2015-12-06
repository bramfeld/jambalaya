/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D.Double;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.algorithms.SpringLayoutAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.gui.SpringLayoutDialog;

/**
 * Performs a force-directed "spring" layout.
 *
 * @author Rob Lintern
 */
public class ShrimpSpringLayout extends AbstractLayout {

    /**
     * @param displayBean
     */
    public ShrimpSpringLayout(DisplayBean displayBean) {
        super(displayBean, LayoutConstants.LAYOUT_SPRING, new SpringLayoutAlgorithm());
        ((SpringLayoutAlgorithm)layoutAlgorithm).setSpringTimeout(5000);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#applyLayout(java.util.Vector, java.awt.geom.Rectangle2D.Double, java.util.Vector, boolean, boolean, boolean)
     */
    public void setupAndApplyLayout(Vector nodes, Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents) {
		if (showDialog) {
			// display spring options to user
			SpringLayoutDialog sld = new SpringLayoutDialog((SpringLayoutAlgorithm)layoutAlgorithm);
			if (sld.isCancelled()) {
				return;
			}
		}

        super.setupAndApplyLayout(nodes, bounds, nodesToExclude, showDialog, animate, separateComponents);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#showProgress()
     */
    protected boolean showProgress() {
        return true;
    }

     /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#getAnimateIterations(int, int)
     */
    protected boolean animateIterations(int nodeCount, int arcCount) {
        return false; //nodeCount < 50 && arcCount < 50;
    }

}
