/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import org.eclipse.mylar.zest.layouts.algorithms.VerticalLayoutAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * @author Rob Lintern
 */
public class VerticalLayout extends AbstractLayout {

    /**
     * @param displayBean
     */
    public VerticalLayout(DisplayBean displayBean) {
        super(displayBean, LayoutConstants.LAYOUT_VERTICAL, new VerticalLayoutAlgorithm());
    }
}
