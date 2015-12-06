/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to unfilter the all filtered relationships/arcs.
 *
 * @tag Shrimp.unfilterAllArcs
 * @author Chris Callendar
 * @date Feb 6, 2007
 */
public class UnfilterAllArcsByIdAdapter extends DefaultToolAction {
	
	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_SHOW_HIDDEN_ARCS;
	public static final String TOOLTIP = "Shows the arcs that have been hidden.  This does not include the ones filtered by arc type.";	
	
	private FilterSelectedArcsAdapter filterSelectedAdapter;
	
	/**
	 * Constructs a new UnfilterAllArcsByIdAdapter
	 * @param tool The tool that this adapter acts upon.
	 * @param filterSelectedAdapter the filter adapter
	 */
	public UnfilterAllArcsByIdAdapter(ShrimpTool tool, FilterSelectedArcsAdapter filterSelectedAdapter) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
	    mustStartAndStop = false;
	    this.filterSelectedAdapter = filterSelectedAdapter;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		filterSelectedAdapter.clearFilter();
		// refresh the view
		try {				
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.refreshLayout();
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}	
	}
	
	public boolean isEnabled() {
		// only enable if there are arcs being filtered
		return filterSelectedAdapter.hasFilteredIDs();
	}
				
}