/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import org.eclipse.mylar.zest.layouts.LayoutStyles;
import org.eclipse.mylar.zest.layouts.algorithms.ForceDirectedAlgorithm;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;

/**
 * A Force-Directed Layout
 * This one is based on the Prefuse toolkit.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class ForceDirectedLayout extends AbstractLayout {

	private static final int DEFAULT_MAX_RUNS = 100;

    /**
     * @param displayBean
     */
    public ForceDirectedLayout(DisplayBean displayBean) {
        super(displayBean, LayoutConstants.LAYOUT_FORCE_DIRECTED, new ForceDirectedAlgorithm(LayoutStyles.NONE, false, DEFAULT_MAX_RUNS));
        //((ForceDirectedAlgorithm)layoutAlgorithm).setStabilize(true);
        ((ForceDirectedAlgorithm)layoutAlgorithm).setSpringLengthRange(250f, 1000f);
        ((ForceDirectedAlgorithm)layoutAlgorithm).setOverlappingNodesAllowed(false);
    }
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#showProgress()
     */
    protected boolean showProgress() {
        return true; 
    }
    
    /**
     * Determines if the prefuse_force.jar file is in the classpath.
     * @return boolean if prefuse is present in the classpath
     */
    public static boolean isPrefuseInstalled() {
    	boolean prefuse = false;
    	try {
    		new ForceDirectedAlgorithm(LayoutStyles.NONE);
    		prefuse = true;
    	} catch (Throwable ignore) {}
    	return prefuse;
    }
    
    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.layout.AbstractLayout#animateIterations(int, int)
     */
    protected boolean animateIterations(int nodeCount, int arcCount) {
        return false; 
    }


}