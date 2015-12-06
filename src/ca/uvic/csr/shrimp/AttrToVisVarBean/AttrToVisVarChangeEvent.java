/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.util.List;


/**
 *	This event occurs whenever the values associated with a particular visual variable are changed
 *	via the AttrToVisVarBean.
 *
 * @author Rob Lintern
 */
public class AttrToVisVarChangeEvent {
	private List visVars;
	private Attribute attr;
	
	/**
	 * Constructs a new event.
	 * @param visVars A list of visual variables affected by a change in the AttrToVisVarBean
	 * @param attr The attribute affected by a change in the AttrToVisVarBean.
	 */
	public AttrToVisVarChangeEvent (List visVars, Attribute attr) {
		this.visVars = visVars;
		this.attr = attr;
	}

	/**
	 * @return The attribute who's mappings have changed.
	 */
	public Attribute getAttribute() {
		return attr;
	}
	
	
	/**
	 * 
	 * @return A list of visual variable who's mapping have changed.
	 */
	public List getVisualVariables () {
		return visVars;
	}

}
