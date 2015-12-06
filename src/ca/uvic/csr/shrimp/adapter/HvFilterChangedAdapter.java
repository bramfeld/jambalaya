/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Enumeration;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * Accentuates nodes in hierachical view that have been filtered in the Shrimp view.
 *
 * @author Rob Lintern
 */
public class HvFilterChangedAdapter implements FilterChangedListener {

	private HierarchicalView hierarchicalView;

	public HvFilterChangedAdapter(HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
	}

	/**
	 * Accentuates nodes in hierachical view that have been filtered in the shrimp view
	 */
	public void filterChanged(FilterChangedEvent fce) {
		ShrimpProject project = hierarchicalView.getProject();
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				PFlatDisplayBean displayBean = (PFlatDisplayBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_BEAN);

				if (hierarchicalView.getCurrentDisplayMode() != HierarchicalView.FILTERED_MODE) {
					return;
				}
				displayBean.clearAccentuated();

				// Accentuate all filtered artifacts
				// Note: An artifact is filtered if it has at least one filter acting
				// upon it. An artifact could be filtered by type and by id but currently
				// the user has no way to tell which filters cause an artifact to be "filtered."
				Vector nodes = displayBean.getAllNodes();
				for (Enumeration e = nodes.elements() ; e.hasMoreElements() ;) {
					ShrimpNode node = (ShrimpNode) e.nextElement();
					if (filterBean.isFiltered(node.getArtifact())) {
						displayBean.accentuate(node, true);
					}
				}
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
