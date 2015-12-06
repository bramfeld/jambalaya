/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import org.eclipse.mylar.zest.layouts.algorithms.TGLayoutAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * The <a href="http://www.touchgraph.com/">ToughGraph</a> layout
 * pulled into SHriMP
 * 
 * @author Rob Lintern
 */
public class TGLayout extends AbstractLayout {

    /**
     * @param displayBean
     */
    public TGLayout(DisplayBean displayBean) {
        super(displayBean, LayoutConstants.LAYOUT_TOUCHGRAPH, new TGLayoutAlgorithm ());
        ((TGLayoutAlgorithm)layoutAlgorithm).setTimeout(5000);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#getAnimateIterations(int, int)
     */
    protected boolean animateIterations(int nodeCount, int arcCount) {
        return false; //nodeCount < 50 && arcCount < 50;
    }
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#showProgress()
     */
    protected boolean showProgress() {
        return true;
    }

}
