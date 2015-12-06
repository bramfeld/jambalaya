/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;


/**
 * An attribute that has nominal values, in other words a set of discrete values.
 * Nominal attributes are connected to nominal visual variables via the AttrToVisVarBean
 * 
 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable
 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean
 * 
 * @author Rob Lintern
 *
 */
public class NominalAttribute extends Attribute {

	/**
	 * @param attrToVisVarBean The bean that this attribute is to be registered with.
	 * @param name The name to be given to this attribute
	 * @param specificType The class to be given to this attribute.
	 */
	public NominalAttribute(AttrToVisVarBean attrToVisVarBean, String name, Class specificType) {
		super (attrToVisVarBean, name, specificType);
	}
	

}
