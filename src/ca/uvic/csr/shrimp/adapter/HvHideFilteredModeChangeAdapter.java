/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HideFilteredModeChangeListener;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * Listens for changes to the hierarchical view's "hide filtered" option and updates the display 
 * accordingly.
 * @author Rob Lintern
 */
public class HvHideFilteredModeChangeAdapter implements HideFilteredModeChangeListener {

	private HierarchicalView hierarchicalView; 
	private FilterBean hvFilterBean;
	private FilterBean svFilterBean;	
	private SelectorBean hvSelectorBean;
	private ShrimpView shrimpView;

	public HvHideFilteredModeChangeAdapter(ShrimpView shrimpView, HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
		this.shrimpView = shrimpView;
	}
	
	/**
	 * @see HideFilteredModeChangeListener#hideFilteredModeChange(boolean)
	 */
	public void hideFilteredModeChange(boolean hideFiltered) {
		try {				
			svFilterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);							
			hvFilterBean = (FilterBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);										
			hvSelectorBean = (SelectorBean) hierarchicalView.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}	
		
		if (hideFiltered) {
			enterHideFilteredMode();	
		} else { 
			exitHideFilteredMode();
		}
	}

	// hide artifacts in hierarchical view that are filtered in shrimp view
	private void enterHideFilteredMode () {
		Vector svFilters;
		
		// add same artifact filters to hierarchical view as in Shrimp view
		hierarchicalView.removeAllArtifactFilters();		
		svFilters = (Vector) svFilterBean.getFilters().clone();
		for (Iterator iter = svFilters.iterator(); iter.hasNext();) {
			Filter filter = (Filter) iter.next();
			if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
				// add same filter to hierarchical view
				hvFilterBean.addFilter(filter);
			}
		}
		hvSelectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
	}
	
	// show artifacts in hierarchical view that are filtered in shrimp view
	private void exitHideFilteredMode () {
		// remove all filters from hierarchical view
		hierarchicalView.removeAllArtifactFilters();	
		//applyCurrentLayout();				
	}

}
