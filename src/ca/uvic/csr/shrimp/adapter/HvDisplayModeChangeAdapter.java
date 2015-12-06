/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.SearchBean.SearchStrategy;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.DisplayModeChangeListener;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * This adapter listens for changes in the Hierachical view's display mode (ex. "Navigation Mode")
 *
 * @author Rob Lintern
 */
public class HvDisplayModeChangeAdapter implements DisplayModeChangeListener {

	private HierarchicalView hierarchicalView;
	private HvSearchCompletedAdapter searchCompletedAdapter; // accentuates search results in hierarchical view

	public HvDisplayModeChangeAdapter(HierarchicalView hierarchicalView) {
		this.hierarchicalView = hierarchicalView;
		this.searchCompletedAdapter = new HvSearchCompletedAdapter(hierarchicalView);
	}

	private ShrimpView getShrimpView() throws ShrimpToolNotFoundException {
		ShrimpView shrimpView = null;
		if (hierarchicalView.getProject() != null) {
			shrimpView = (ShrimpView) hierarchicalView.getProject().getTool(ShrimpProject.SHRIMP_VIEW);
		} else {
			throw new ShrimpToolNotFoundException(ShrimpProject.HIERARCHICAL_VIEW);
		}
		return shrimpView;
	}

	/**
	 * @see DisplayModeChangeListener#displayModeChange(int, int)
	 */
	public void displayModeChange(int oldMode, int newMode) {
		try {
			ShrimpView shrimpView = getShrimpView();
			PFlatDisplayBean displayBean = (PFlatDisplayBean) hierarchicalView.getBean(ShrimpTool.DISPLAY_BEAN);
			SearchBean searchBean = (SearchBean) hierarchicalView.getProject().getBean(ShrimpProject.SEARCH_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			SearchStrategy searchStrategy = searchBean.getStrategy(ArtifactSearchStrategy.NAME);

			// clear accentuated
			displayBean.clearAccentuated();

			// exit current mode
			switch (oldMode) {
				case HierarchicalView.NAVIGATION_MODE:
					exitNavigationMode();
					break;
				case HierarchicalView.FILTERED_MODE:
					exitFilteredMode();
					break;
				case HierarchicalView.SEARCH_MODE:
					exitSearchMode(searchStrategy);
					break;
			}
			// enter new mode
			switch (newMode) {
				case HierarchicalView.NAVIGATION_MODE:
					enterNavigationMode(shrimpView, displayBean, selectorBean);
					break;
				case HierarchicalView.FILTERED_MODE:
					enterFilteredMode(displayBean, filterBean);
					break;
				case HierarchicalView.SEARCH_MODE:
					enterSearchMode(displayBean, searchStrategy);
					break;
			}

		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void enterNavigationMode(ShrimpView shrimpView, PFlatDisplayBean displayBean, SelectorBean selectorBean) {
		// accentuate hier view nodes that are selected in ShrimpView
		Vector svSelectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		// convert shrimp view nodes to hier nodes

		Vector hvNodes = new Vector();
		for (Iterator iter = svSelectedNodes.iterator(); iter.hasNext();) {
			ShrimpNode svNode = (ShrimpNode) iter.next();
			hvNodes.addAll(displayBean.getDataDisplayBridge().getShrimpNodes(svNode.getArtifact(), false));
		}
		displayBean.accentuate(hvNodes, true);
	}

	private void exitNavigationMode() {
		// nothing
	}

	private void enterFilteredMode(PFlatDisplayBean displayBean, FilterBean filterBean) {
		// Accentuate all filtered artifacts
		// Note: An artifact is filtered if it has at least one filter acting
		// upon it. An artifact could be filtered by type and by id but currently
		// the user has no way to tell which filters cause an artifact to be "filtered."
		Vector nodes = displayBean.getAllNodes();
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			ShrimpNode hierNode = (ShrimpNode) iter.next();
			if (filterBean.isFiltered(hierNode.getArtifact())) {
				displayBean.accentuate(hierNode, true);
			}
		}
	}

	private void exitFilteredMode() {
		// nothing
	}

	private void enterSearchMode(PFlatDisplayBean displayBean, SearchStrategy searchStrategy) {
		Vector svSearchResults = searchStrategy.getSearchResults();
		for (Iterator iter = svSearchResults.iterator(); iter.hasNext(); ) {
			Object svObject = iter.next();
			if (svObject instanceof Artifact) {
				Vector hierNodes = displayBean.getDataDisplayBridge().getShrimpNodes((Artifact) svObject, false);
				displayBean.accentuate(hierNodes, true);
			}
		}
		searchStrategy.addSearchCompletedListener(searchCompletedAdapter);
	}

	private void exitSearchMode(SearchStrategy searchStrategy) {
		searchStrategy.removeSearchCompletedListener(searchCompletedAdapter);
	}

}
