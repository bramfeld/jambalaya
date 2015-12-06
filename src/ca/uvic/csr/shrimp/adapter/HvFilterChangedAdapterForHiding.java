/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Enumeration;
import java.util.Vector;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;

/**
 * When the hierarhical view is "hiding filtered nodes" this adapter listens for changes in the 
 * shrimp view's filterbean and updates the hierarchical view's display accordingly.
 * 
 * @author Rob Lintern
 */
public class HvFilterChangedAdapterForHiding implements FilterChangedListener {
	
	private HierarchicalView hierarchicalView; 

	public HvFilterChangedAdapterForHiding(HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
	}
	/**
	 * @see FilterChangedListener#filterChanged(FilterChangedEvent)
	 */
	public void filterChanged(FilterChangedEvent fce) {
		FilterBean filterBean = null;
		try {				
			filterBean = (FilterBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			if (!hierarchicalView.getHideFilteredMode()) {
				return;
			}
		} catch (BeanNotFoundException e) {
			return;
		}

		Vector addedFilters = fce.getAddedFilters();
		Vector removedFilters = fce.getRemovedFilters();
		Vector changedFilters = fce.getChangedFilters();
		
		for (Enumeration e = addedFilters.elements() ; e.hasMoreElements() ;) {
			Filter filter = (Filter) e.nextElement();
			if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
				// add same filter to hierarchical view
				filterBean.addFilter(filter);
			}
		}

		// Remove any deleted artifact filters from hier's filterBean
		for (Enumeration e = removedFilters.elements() ; e.hasMoreElements() ;) {
			Filter filter = (Filter) e.nextElement();
			if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
				try {
					// remove same filter from hierarchical view
					filterBean.removeFilter(filter);
				} catch (FilterNotFoundException fnfe) {
					 // do nothing; 
				}	
			}			
		}
		
		// Remove, then add, any changed artifact filters from hier's filterBean
		for (Enumeration e = changedFilters.elements() ; e.hasMoreElements() ;) {
			Filter filter = (Filter) e.nextElement();
			if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
				try {
					// remove same filter from hierarchical view
					filterBean.removeFilter(filter);
					filterBean.addFilter(filter);
				} catch (FilterNotFoundException fnfe) {
					 // do nothing; 
				}	
			}			
		}
	}
}
