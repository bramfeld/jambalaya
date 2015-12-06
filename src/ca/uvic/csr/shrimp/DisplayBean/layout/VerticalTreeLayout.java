/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.Comparator;

import org.eclipse.mylar.zest.layouts.algorithms.TreeLayoutAlgorithm;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * This layout will place the visible children in a tree layout.
 * The algorithm will display the relationships based on their direction.
 * This layout can be used to see things such as inheritance.
 *
 * @author Jingwei Wu, Rob Lintern
 * @date Apr 5, 2001
 */
public class VerticalTreeLayout extends AbstractLayout {

    public VerticalTreeLayout (DisplayBean displayBean, String name, boolean inverted) {
        super(displayBean, name, new TreeLayoutAlgorithm(), inverted);
    }

    public VerticalTreeLayout (DisplayBean displayBean, String name, boolean inverted, Comparator comparator) {
        super(displayBean, name, new TreeLayoutAlgorithm(), inverted);
        layoutAlgorithm.setComparator(comparator);
    }

}
