/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.SearchBean.SearchCompletedEvent;
import ca.uvic.csr.shrimp.SearchBean.SearchCompletedListener;
import ca.uvic.csr.shrimp.SearchBean.SearchStrategy;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;

/**
 * Accentuates nodes in hierarchical view that are search results hits in the shrimp view
 *
 * @author Rob Lintern
 */
public class HvSearchCompletedAdapter implements SearchCompletedListener {

	private HierarchicalView hv;

	public HvSearchCompletedAdapter(HierarchicalView hv) {
		this.hv = hv;
	}

	/**
	 * Accentuates search results in hierarchical view
	 */
	public void searchCompleted(SearchCompletedEvent e) {
		try {
			SearchBean searchBean = (SearchBean) hv.getProject().getBean (ShrimpProject.SEARCH_BEAN);
			SearchStrategy searchStrategy = searchBean.getStrategy(ArtifactSearchStrategy.NAME);

			if (hv.getCurrentDisplayMode() != HierarchicalView.SEARCH_MODE) {
				return;
			}

			PFlatDisplayBean flatDisplayBean = (PFlatDisplayBean) hv.getBean(ShrimpTool.DISPLAY_BEAN);
			flatDisplayBean.clearAccentuated();

			Vector svSearchResults = searchStrategy.getSearchResults();
			for (Iterator iter = svSearchResults.iterator(); iter.hasNext();) {
				Object svObject = iter.next();
				// only consider artifacts in search results
				if (svObject instanceof Artifact) {
					Vector hierNodes = flatDisplayBean.getDataDisplayBridge().getShrimpNodes((Artifact) svObject, false);
					flatDisplayBean.accentuate (hierNodes, true);
				}
			}
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

}
