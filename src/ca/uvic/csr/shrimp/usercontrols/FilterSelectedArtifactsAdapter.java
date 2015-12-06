/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to filter the currently selected artifacts.
 * An id filter is added to the FilterBean for each selected artifact and
 * their descendents.
 *
 * @author Rob Lintern, Chris Callendar
 * @date Feb 12, 2002
 */
public class FilterSelectedArtifactsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_HIDE;
	public static final String TOOLTIP = "Hides the selected node(s).  Hidden nodes can be made visible using the \"" +
		UnfilterAllByIdAdapter.ACTION_NAME +"\" action.";

	/**
	 * Constructs a new FilterSelectedArtifactsAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public FilterSelectedArtifactsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	public void actionPerformed(ActionEvent e) {
		startAction();
	}

	public void startAction() {
	    try {
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	    	Vector selectedNodes = getSelectedNodes();
			Vector idsToFilter = new Vector (selectedNodes.size());
			for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				idsToFilter.add(new Long(node.getArtifact().getID()));

				// @tag Shrimp.Collapse : revert back to default shape since the node is being hidden
				// otherwise when the unfilter all action occurs the node will still be collapsed but
				// its children will be visible.
				node.setIsCollapsed(false);
			}
			if (!idsToFilter.isEmpty()) {
				filterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_ID,
						Long.class, FilterConstants.ARTIFACT_FILTER_TYPE, idsToFilter, true);
			}
		} catch (BeanNotFoundException stnfe) {
			stnfe.printStackTrace();
	    }
	}

	public boolean isEnabled() {
	    try {
	    	Vector selectedNodes = getSelectedNodes();
	    	DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
	    	return super.isEnabled() && (selectedNodes.size() > 0) && displayBean.isEnabled();
	    } catch (BeanNotFoundException stnfe) {
			return false;
	    }
	}

}