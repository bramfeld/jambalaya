/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.HierarchicalView;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.HvDisplayModeChangeAdapter;
import ca.uvic.csr.shrimp.adapter.HvFilterChangedAdapter;
import ca.uvic.csr.shrimp.adapter.HvFilterChangedAdapterForHiding;
import ca.uvic.csr.shrimp.adapter.HvNodeDoubleClickAdapter;
import ca.uvic.csr.shrimp.adapter.HvSelectedNodesChangeAdapter;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * Provides some utilities for converting objects between the HierarchicalView and the ShrimpView.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class HvSvBridge {

	private HierarchicalView hv;

	// Hierarchical View Adapters
	// listens for changes in the hier view's display mode
	private HvDisplayModeChangeAdapter hvdmca;
	// listens for changes in a filter bean, to update hier views "filtered" mode
	private HvFilterChangedAdapter hvfca;
	// listens for changes in a filter bean, if hier view is in "hide filtered" modes
	private HvFilterChangedAdapterForHiding hvfcafh;
	//listens for changes to the hier view's "hide filtered" mode.
	// private HvHideFilteredModeChangeAdapter hvhfmca;
	// listens for double clicking of nodes in the hierarchical view
	private HvNodeDoubleClickAdapter hvndca;
	// accentuates selected nodes in hierarhical view
	private HvSelectedNodesChangeAdapter hvsnca;

	public HvSvBridge(HierarchicalView hv) {
		this.hv = hv;

		// create adapters
		hvdmca = new HvDisplayModeChangeAdapter(hv);
		hvfca = new HvFilterChangedAdapter(hv);
		hvfcafh = new HvFilterChangedAdapterForHiding(hv);
		//hvhfmca = new HvHideFilteredModeChangeAdapter(project, sv, hv);
		hvndca = new HvNodeDoubleClickAdapter(hv);
		hvsnca = new HvSelectedNodesChangeAdapter(hv);

		// add adapters to appropriate places
		hv.addDisplayModeChangeListener(hvdmca);
		hv.addNodeDoubleClickListener(hvndca);
		//hv.addHideFilteredModeChangeListener(hvhfmca);

		try {
			ShrimpView sv = (ShrimpView) hv.getProject().getTool(ShrimpProject.SHRIMP_VIEW);
			FilterBean svFilterBean = (FilterBean) sv.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			SelectorBean selectorBean = (SelectorBean) sv.getBean(ShrimpTool.SELECTOR_BEAN);
			hv.registerExternalFilterBean(svFilterBean);
			svFilterBean.addFilterChangedListener(hvfca);
			svFilterBean.addFilterChangedListener(hvfcafh);
			selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, hvsnca);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void separateShrimpViewHierarchicalView() {
		if (hv != null) {
			hv.removeDisplayModeChangeListener(hvdmca);
			// hierarchicalView.removeHideFilteredModeChangeListener(hvhfmca);
			hv.removeNodeDoubleClickListener(hvndca);

			if (hv.getProject() != null) {
				try {
					ShrimpView sv = (ShrimpView) hv.getProject().getTool(ShrimpProject.SHRIMP_VIEW);
					try {
						FilterBean svFilterBean = (FilterBean) sv.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
						svFilterBean.removeFilterChangedListener(hvfca);
						svFilterBean.removeFilterChangedListener(hvfcafh);
						hv.unregisterExternalFilterBean(svFilterBean);
					} catch (BeanNotFoundException e) {
						e.printStackTrace();
					}

					try {
						SelectorBean selectorBean = (SelectorBean) sv.getBean(ShrimpTool.SELECTOR_BEAN);
						selectorBean.removePropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, hvsnca);
					} catch (BeanNotFoundException e1) {
						e1.printStackTrace();
					}
				} catch (ShrimpToolNotFoundException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

}
