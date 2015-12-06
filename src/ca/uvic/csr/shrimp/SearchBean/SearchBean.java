/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.util.Collection;


public interface SearchBean {

	/**
	 * @return The collection of search strategies that this bean is using.
	 */
    public Collection getStrategies();
    
    public SearchStrategy getStrategy(String strategyName);
        
    /**
     * Tests if this SearchBean has the specified strategy
     * <code>s</code>.
     * @param s the search strategy to be examined.
     * @return <code>true</code> if <code>s</code> is contained
     * in this SearchBean. Otherwise, <code>false</code>.
     */
    public boolean hasStrategy(SearchStrategy s);

    /**
     * Adds a new search strategy to this SearchBean. The first
     * added search strategy should be the selected strategy by
     * default.
     * @param s the search strategy to be added.
     * @throws java.lang.NullPointerException if the specified
     * <code>s</code> is <code>null</code>.
     */
    public void addStrategy(SearchStrategy s);

    /**
     * Removes a old search strategy to this SearchBean. Whenever
     * the last search strategy is removed, the selected strategy
     * must be <code>null</code>.
     * @param s the search strategy to be removed.
     * The <code>null</code> is simply ignored. No exception is thrown.
     */
    public void removeStrategy(SearchStrategy s);
    
    public void addSearchBeanListener(SearchBeanListener listener);
    public void removeSearchBeanListener(SearchBeanListener listener);

}
