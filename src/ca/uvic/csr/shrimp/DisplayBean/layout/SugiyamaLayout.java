/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.Comparator;

import org.eclipse.mylar.zest.layouts.LayoutAlgorithm;
import org.eclipse.mylar.zest.layouts.LayoutStyles;

import ca.uvic.cs.sugilayout.algorithms.SugiLayoutAlgorithm;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * @tag Shrimp(sugiyama)
 * @author Chris Bennett
 */
public class SugiyamaLayout extends AbstractLayout {

	public SugiyamaLayout(DisplayBean displayBean, String name, LayoutAlgorithm layoutAlgorithm) {
		super(displayBean, name, layoutAlgorithm);
        super.resizeEntitiesAfterLayout = false;
	}

    /**
     * @param displayBean
     * @param name
     * @param inverted
     * @param comparator
     */
    public SugiyamaLayout (DisplayBean displayBean, String name, boolean inverted, Comparator comparator) {
        super(displayBean, name, new SugiLayoutAlgorithm(LayoutStyles.NONE));
        layoutAlgorithm.setComparator(comparator);
    }

    /**
     * @param displayBean
     * @param name
     */
	public SugiyamaLayout(DisplayBean displayBean, String name) {
		super(displayBean, name, new SugiLayoutAlgorithm(LayoutStyles.NONE));
	}

	public static boolean isInstalled() {
		// TEMP Sugiyama layout hangs on large graphs - need to fix it!
		// well at least it does a terrible and slow job when more than one or two arc types are shown
		return false;
//		try {
//			new SugiLayoutAlgorithm(LayoutStyles.NONE);
//			return true;
//		} catch (Throwable t) {
//			return false;
//		}
	}

}
