/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.Comparator;

import org.eclipse.mylar.zest.layouts.LayoutStyles;

import ca.uvic.cs.ortholayout.algorithms.OrthogonalLayoutAlgorithm;
import ca.uvic.cs.ortholayout.algorithms.internal.OrthoLayoutFacade;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * This layout uses a default layout with Manhattan edges.
 * 
 * @author Chris Bennett
 * @date June, 2006
 */
public class OrthogonalLayout extends AbstractLayout {

	public OrthogonalLayout(DisplayBean displayBean, String name) {
		super(displayBean, name, new OrthogonalLayoutAlgorithm(LayoutStyles.NONE));
		super.resizeEntitiesAfterLayout = false;
	}

	/**
	 * @param displayBean
	 * @param name
	 * @param inverted
	 * @param comparator
	 */
	public OrthogonalLayout(DisplayBean displayBean, String name, boolean inverted, Comparator comparator) {
		super(displayBean, name, new OrthogonalLayoutAlgorithm(LayoutStyles.NONE));
		layoutAlgorithm.setComparator(comparator);
	}

	/**
	 * Determines if the orthogonal layout is in the classpath and 
	 * if the library is available.
	 * @return boolean if orthogonal layout is present in the classpath
	 */
	public static boolean isLoaded() {
		boolean loaded = false;
		try {
			new OrthogonalLayoutAlgorithm(LayoutStyles.NONE);
			loaded = OrthoLayoutFacade.isLoaded();
		} catch (Throwable ignore) {
		}
		return loaded;
	}

}
