/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.DisplayBean.event.DisplayFilterRequestListener;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to check if an object should be filtered in the display.
 *
 * @author Casey Best, Rob Lintern
 */
public class DisplayFilterRequestAdapter implements DisplayFilterRequestListener {
	
	private ShrimpTool tool;

	public DisplayFilterRequestAdapter (ShrimpTool tool) {
		this.tool = tool;
	}
	
    public boolean isFiltered(Object obj) {
        if (tool != null) { 
	        try {               
	            FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);         
	            return filterBean.isFiltered(obj);
	        } catch (BeanNotFoundException bnfe) {
	            bnfe.printStackTrace();
	        }   
        }
        return false;
    }

}