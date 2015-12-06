/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to group the currently selected artifacts.
 * @tag Shrimp(grouping)
 * @author Chris Bennett
 */
public class UngroupSelectedArtifactsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_UNGROUP;
	public static final String TOOLTIP = "Ungroups the selected node(s).";

	/**
	 * Constructs a new FilterSelectedArtifactsAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public UngroupSelectedArtifactsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	public void startAction() {
		   try {
			Vector selectedNodes = getSelectedNodes();
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				String nodeType = node.getArtifact().getType();
				if (nodeType.equals(JavaDomainConstants.ACTOR_ART_TYPE) ||
					nodeType.equals(JavaDomainConstants.OBJECT_ART_TYPE)) {
					displayBean.getLifelineGroupingManager().ungroup(node);
				}
				else if (nodeType.equals(JavaDomainConstants.METHOD_EXEC_ART_TYPE)) {
					displayBean.getMethodExecGroupingManager().ungroup(node);
				}
				else { // assume structural grouping
					displayBean.getStructuralGroupingManager().ungroup(node);
				}
			}
		} catch (BeanNotFoundException stnfe) {
			stnfe.printStackTrace();
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