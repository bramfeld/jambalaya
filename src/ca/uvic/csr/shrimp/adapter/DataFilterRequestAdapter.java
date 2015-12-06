/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.DataBean.event.DataFilterRequestListener;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * Handles a request to check if an object should be filtered from the data.
 * 
 * @author Rob Lintern
 */
public class DataFilterRequestAdapter implements DataFilterRequestListener {

	private ShrimpProject project;
	
	public DataFilterRequestAdapter(ShrimpProject project) {
		this.project = project;
	}
	
    public boolean isFiltered(Object obj, String targetFilterType) {
        if (project != null) {
	        try {               
	            FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);                       
	            return dataFilterBean.isFiltered(obj, targetFilterType);
	        } catch (BeanNotFoundException bnfe) {
	            bnfe.printStackTrace();
	        }
        }
        return false;
    }
}