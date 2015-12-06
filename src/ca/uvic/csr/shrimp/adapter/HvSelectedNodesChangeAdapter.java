/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.util.CollectionUtils;

/**
 * Accentuates all nodes in the hierarchical view that are selected in a given selector bean.
 *
 * @author Rob Lintern
 */
public class HvSelectedNodesChangeAdapter implements PropertyChangeListener {

	private HierarchicalView hierarchicalView;

	public HvSelectedNodesChangeAdapter(HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
	}

	/**
	 * Accentuates all nodes in the hierarchical view that are selected in a selector bean.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		Vector oldSelected = (Vector) evt.getOldValue();
		Vector newSelected = (Vector) evt.getNewValue();
		if (CollectionUtils.haveSameElements(oldSelected, newSelected)) {
			return;
		}

		ShrimpProject project = hierarchicalView.getProject();
		if (project == null) {
			return;
		}

		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			PFlatDisplayBean displayBean = (PFlatDisplayBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean svSelectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);

			if (hierarchicalView.getCurrentDisplayMode() != HierarchicalView.NAVIGATION_MODE) {
				return;
			}

			displayBean.clearAccentuated();

			Vector oldHvSelectedNodes = svNodesToHvNodes(shrimpView, displayBean, oldSelected, false);
			for (Iterator iter = oldHvSelectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode oldHvNode = (ShrimpNode) iter.next();
				displayBean.accentuateMouseOver(oldHvNode, false);
			}

			// accentuate hier view nodes that are selected in ShrimpView
			Vector svSelectedNodes = (Vector)svSelectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			if (svSelectedNodes != null) {
				// convert shrimp view nodes to hierarchical view nodes
				Vector hvNodes = svNodesToHvNodes(shrimpView, displayBean, svSelectedNodes, false);
				displayBean.accentuate(hvNodes, true);
				for (Iterator iter = hvNodes.iterator(); iter.hasNext();) {
					ShrimpNode hvNode = (ShrimpNode) iter.next();
					displayBean.accentuateMouseOver(hvNode, true);
				}
			}
		} catch (BeanNotFoundException e) {
			return;
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Converts a vector of nodes from the ShrimpView into a vector of nodes in the hierView that represent the same artifacts.
	 */
	private Vector svNodesToHvNodes(ShrimpView sv, PFlatDisplayBean displayBean, Vector svNodes, boolean create) {
		Vector hvNodes = new Vector();
		for (Iterator iter = svNodes.iterator(); iter.hasNext();) {
			ShrimpNode svNode = (ShrimpNode) iter.next();
			hvNodes.addAll(displayBean.getDataDisplayBridge().getShrimpNodes(svNode.getArtifact(), create));
		}
		return hvNodes;
	}

}
