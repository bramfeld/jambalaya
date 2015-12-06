/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.HierarchicalView.NodeDoubleClickListener;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.usercontrols.MagnifyInAdapter;

/**
 * Causes shrimp view to navigate to the node that was double-clicked in the hierarchical view.
 *
 * @author Rob Lintern
 */
public class HvNodeDoubleClickAdapter implements NodeDoubleClickListener {

	private HierarchicalView hierarchicalView;

	public HvNodeDoubleClickAdapter(HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
	}

	/**
	 * Causes shrimp view to navigate to the node double clicked on in the hierarchical view.
	 */
	public void doubleClick(ShrimpNode hvNode) {
		ShrimpProject project = hierarchicalView.getProject();
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);

				if (hvNode == null) {
					return;
				}

				Vector svNodes = new Vector();
				try {
					DisplayBean svDisplayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
					svNodes.addAll(svDisplayBean.getDataDisplayBridge().getShrimpNodes(hvNode.getArtifact(), true));
				} catch (BeanNotFoundException e) {
					e.printStackTrace();
				}

				ShrimpNode svNode = null;
				if (!svNodes.isEmpty()) {
					svNode = (ShrimpNode) svNodes.firstElement();
				}
				if (svNode == null) {
					JOptionPane.showMessageDialog(shrimpView.getGUI(), "The destination node is not in the hierarchy of the main view.");
				} else if (filterBean.isFiltered(svNode.getArtifact())) {
					JOptionPane.showMessageDialog(shrimpView.getGUI(), "The destination node is currently filtered.");
				} else if (displayBean.getDataDisplayBridge().isAncestorFiltered(svNode.getArtifact())) {
					JOptionPane.showMessageDialog(shrimpView.getGUI(), "An ancestor of the destination node is currently filtered.");
				} else {
					// Create a temporary magnifyInAdapter, using the Shrimp View's selector bean, data bean, and display bean
					// to take care of focusing on the right shrimp view object
					Object oldTarget = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
					selectorBean.setSelected (SelectorBeanConstants.TARGET_OBJECT, svNode);
					Object oldZoomMode = selectorBean.getSelected(DisplayConstants.ZOOM_MODE);
					selectorBean.setSelected(DisplayConstants.ZOOM_MODE, DisplayConstants.MAGNIFY);

					MagnifyInAdapter tempMIA = new MagnifyInAdapter(shrimpView);
					tempMIA.startAction();
					if (oldTarget != null) {
						selectorBean.setSelected(SelectorBeanConstants.TARGET_OBJECT, oldTarget);
					}
					selectorBean.setSelected(DisplayConstants.ZOOM_MODE, oldZoomMode);
				}

				// now expand this node
				PFlatDisplayBean flatDisplayBean = (PFlatDisplayBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_BEAN);
				Vector nodesToExpand = new Vector(1);
				nodesToExpand.add(hvNode);
				flatDisplayBean.expandNodes(nodesToExpand);

			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
