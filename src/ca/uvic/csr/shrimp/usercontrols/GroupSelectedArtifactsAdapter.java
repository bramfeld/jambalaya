/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.layout.SequenceGroupingManager;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to group the currently selected artifacts.
 * @tag Shrimp(grouping)
 * @author Chris Bennett
 */
public class GroupSelectedArtifactsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_GROUP;
	public static final String TOOLTIP = "Groups the selected node(s).  If one node is selected then it and its children are grouped into one node.\n" +
		"If multiple nodes are selected then they are grouped into one node.\nGrouping also re-routes the arcs.";

	/**
	 * Constructs a new GroupSelectedArtifactsAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public GroupSelectedArtifactsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	public void startAction() {
	   try {
			Vector selectedNodes = getSelectedNodes();
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			boolean grouped = false;
			for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				if (SequenceGroupingManager.isObjectLifeline(node)) {
					displayBean.getLifelineGroupingManager().group(node);
					grouped = true;
				}
				else if (SequenceGroupingManager.isMethodExecution(node)) {
					displayBean.getMethodExecGroupingManager().group(node);
					grouped = true;
				}
			}
			if (!grouped) {
				displayBean.getStructuralGroupingManager().group(selectedNodes);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean isEnabled() {
	    try {
			SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
	    	Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
	    	return super.isEnabled() && (selectedNodes.size() > 0);
	    } catch (BeanNotFoundException stnfe) {
			return false;
	    }
	}

}