/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;


/**
 * Listens for changes to the mappings within the AttrToVisVarBean
 * 
 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean
 * @author Rob Lintern
 */
public interface AttrToVisVarChangeListener {

	public void valuesChanged(AttrToVisVarChangeEvent e);
	
}
