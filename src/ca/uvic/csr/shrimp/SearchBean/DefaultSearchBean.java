/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public class DefaultSearchBean implements SearchBean, Serializable {
    /**
     * Storing all the embedded search strategies.
     */
    private List strategies;
    private List searchBeanListeners;

    /**
     * Constructs a new SearchBean. This SearchBean can contain multiple search
     * strategies.
     */
    public DefaultSearchBean() {
        super();
        strategies = new ArrayList();
        searchBeanListeners = new ArrayList();
    }

     /**
     * @see ca.uvic.csr.shrimp.SearchBean.SearchBean#getStrategy(java.lang.String)
     */
    public SearchStrategy getStrategy(String strategyName) {
        SearchStrategy strategy = null;
        for (Iterator iter = strategies.iterator(); iter.hasNext() && strategy == null;) {
            SearchStrategy tmpS = (SearchStrategy) iter.next();
            if (tmpS.getName().equals(strategyName)) {
                strategy = tmpS;
            }
        }
        return strategy;
    }

    /**
     * 
     * @see ca.uvic.csr.shrimp.SearchBean.SearchBean#getStrategies()
     */
    public Collection getStrategies() {
        return strategies;
    }

    /**
     * Tests if this SearchBean has the specified strategy <code>s</code>.
     * 
     * @param s
     *            the search strategy to be examined.
     * @return <code>true</code> if <code>s</code> is contained in this
     *         SearchBean. Otherwise, <code>false</code>.
     */
    public boolean hasStrategy(SearchStrategy s) {
        if (s == null) {
			return false;
		}
        return strategies.contains(s);
    }

    /**
     * Adds a new search strategy to this SearchBean. The first added search
     * strategy is to be the selected strategy by default.
     * 
     * @param s
     *            the search strategy to be added.
     * @throws java.lang.NullPointerException
     *             if the specified <code>s</code> is <code>null</code>.
     */
    public void addStrategy(final SearchStrategy s) {
        if (s == null) {
            throw new NullPointerException("The argument is null!");
        }

        if (!hasStrategy(s)) {
            strategies.add(s);
            fireStrategiesChangedEvent();
       }
    }

    /**
     * Removes a old search strategy to this SearchBean. Whenever the last
     * search strategy is removed, the selected strategy will be
     * <code>null</code>.
     * 
     * @param strategyToRemove
     *            the search strategy to be removed. The <code>null</code> is
     *            simply ignored. No exception is thrown.
     */
    public void removeStrategy(SearchStrategy strategyToRemove) {
        if (strategyToRemove == null) {
			return;
		}
        if (hasStrategy(strategyToRemove)) {
            strategies.remove(strategyToRemove);
            fireStrategiesChangedEvent();
        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.SearchBean.SearchBean#addSearchBeanListener(ca.uvic.csr.shrimp.SearchBean.listener.SearchBeanListener)
     */
    public void addSearchBeanListener(SearchBeanListener listener) {
        if (!searchBeanListeners.contains(listener)) {
            searchBeanListeners.add(listener);
        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.SearchBean.SearchBean#removeSearchBeanListener(ca.uvic.csr.shrimp.SearchBean.listener.SearchBeanListener)
     */
    public void removeSearchBeanListener(SearchBeanListener listener) {
        searchBeanListeners.remove(listener);
    }
    
    protected void fireStrategiesChangedEvent() {
        for (Iterator iter = searchBeanListeners.iterator(); iter.hasNext();) {
            SearchBeanListener listener = (SearchBeanListener) iter.next();
            listener.strategiesChanged();
        }
    }

} //class DefaultSearchBean
