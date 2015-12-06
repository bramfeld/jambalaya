/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Unfilters all artifacts that have been filtered by id.
 * @author Rob Lintern, Chris Callendar
 */
public class UnfilterAllByIdAdapter extends DefaultToolAction {
	
	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_SHOW_ALL_NODES;
	public static final String TOOLTIP = "Makes any nodes that were hidden either by the \"" + 
		FilterSelectedArtifactsAdapter.ACTION_NAME + "\" action or the \"" + PruneSubgraphAdapter.ACTION_NAME + "\" action visible.\n" + 
		"Any node types that are filtered using the Node Filter Palette will stay hidden.";	
	
	/**
	 * Constructs a new UnfilterAllByIdAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public UnfilterAllByIdAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
	    mustStartAndStop = false;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		try {				
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);		
			filterBean.removeNominalAttrFilter(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Long.class, FilterConstants.ARTIFACT_FILTER_TYPE);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.refreshLayout();
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}	
		
	}

}
