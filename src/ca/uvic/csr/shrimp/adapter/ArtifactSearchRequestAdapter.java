/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.SearchBean.SearchRequestEvent;
import ca.uvic.csr.shrimp.SearchBean.SearchRequestListener;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * This adapter will handle the process of searching
 *
 * @author Casey Best
 * date: Aug 15, 2000
 */
public class ArtifactSearchRequestAdapter implements SearchRequestListener {
	
	private ShrimpProject project;
	
	public ArtifactSearchRequestAdapter(ShrimpProject project) {
		this.project = project;
	}
	
	public void requestMade(SearchRequestEvent e) {
		try {				
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			Vector rawData = e.getRawDataCollector();		
			Vector artifacts = dataBean.getArtifacts(true);
			rawData.addAll (artifacts);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}	
	}
}