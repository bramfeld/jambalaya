/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * When something is filtered/unfiltered, this class updates the DataBean
 */
public class DataFilterChangedAdapter implements FilterChangedListener {

	private ShrimpProject project;

	public DataFilterChangedAdapter(ShrimpProject project) {
		this.project = project;
	}

	public void filterChanged (FilterChangedEvent fce) {
		if (project == null) {
			return;
		}

		try {
            DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
            dataBean.dataFiltersHaveChanged();
            // update artifact types in ArtifactSearchStrategy
            try {
                SearchBean searchBean = (SearchBean) project.getBean(ShrimpProject.SEARCH_BEAN);
                ArtifactSearchStrategy artifactSearchStrategy = (ArtifactSearchStrategy) searchBean.getStrategy(ArtifactSearchStrategy.NAME);
                if (artifactSearchStrategy != null) {
                    // **NOTE - the following line had previously caused shrimp to freeze
                    // seems to work now, but something to watch out for
                    artifactSearchStrategy.setArtifactTypes(dataBean.getArtifactTypes(false, true));
                }
            } catch (BeanNotFoundException e) {
                e.printStackTrace();
            }
        } catch (BeanNotFoundException bnfe) {
            bnfe.printStackTrace();
        }
    }
}