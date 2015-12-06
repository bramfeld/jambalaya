/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.util.Comparator;

import org.eclipse.mylar.zest.layouts.LayoutStyles;

import ca.uvic.cs.ortholayout.algorithms.HierarchicalLayoutAlgorithm;
import ca.uvic.cs.ortholayout.algorithms.internal.OrthoLayoutFacade;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * This implements a hierarchical layout. Similar to the Orthogonal layout
 * but without Manhattan edges.
 * @author Chris Bennett
 * date: June, 2006
 */
public class HierarchicalLayout extends AbstractLayout {
	
	    public HierarchicalLayout (DisplayBean displayBean, String name) {
	        super(displayBean, name, new HierarchicalLayoutAlgorithm (LayoutStyles.NONE));
	        super.resizeEntitiesAfterLayout = false;
	    }
	    
	    /**
	     * @param displayBean
	     * @param name
	     * @param inverted
	     * @param comparator
	     */
	    public HierarchicalLayout (DisplayBean displayBean, String name, 
	    		boolean inverted, Comparator comparator) {
	        super(displayBean, name, new HierarchicalLayoutAlgorithm(LayoutStyles.NONE));
	        layoutAlgorithm.setComparator(comparator);
	    }
	    
	    /**
	     * Determines if the hierarchical layout is in the classpath and 
	     * if the library is available.
	     * @return boolean if hierarchical layout is present in the classpath
	     */
	    public static boolean isLoaded() {
	    	boolean loaded = false;
	    	try {
	    		new HierarchicalLayoutAlgorithm (LayoutStyles.NONE);
	    		loaded = OrthoLayoutFacade.isLoaded();
	    	} catch (Throwable ignore) {}
	    	return loaded;
	    }
	    
	}
