/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;


import java.util.Comparator;

import org.eclipse.mylar.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * A horizontal tree layout
 * @see VerticalTreeLayout
 * @author Rob Lintern
 */
public class HorizontalTreeLayout extends AbstractLayout {

    /**
     * @param displayBean
     * @param name
     * @param inverted
     */
    public HorizontalTreeLayout(DisplayBean displayBean, String name, boolean inverted) {
        super(displayBean, name, new HorizontalTreeLayoutAlgorithm(), inverted);
    }

    public HorizontalTreeLayout(DisplayBean displayBean, String name, boolean inverted, Comparator comparator) {
        super(displayBean, name, new HorizontalTreeLayoutAlgorithm(), inverted);
        layoutAlgorithm.setComparator(comparator);
    }

}
