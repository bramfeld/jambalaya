/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;


/**
 * This class represents a data attribute. For example, the atribute may be the number of descendents of an
 * artifact, or the date an artifact was created.
 * 
 * @see AttrToVisVarBean
 * 
 * @author Rob Lintern
 *
 */
public abstract class Attribute {
	/** The name of this attribute **/
	private String name;
	
	/** The specific type of this attribute (eg. "Double") **/
	private Class attributeType; // Double | String | Date | etc
	
	protected AttrToVisVarBean attrToVisVarBean;
	
	/**
	 * 
	 * @param attrToVisVarBean The bean that this attribute is to be registered with.
	 * @param name The name of this attribute
	 * @param attributeType The class of this attribute.
	 */
	public Attribute (AttrToVisVarBean attrToVisVarBean, String name, Class attributeType) {
		this.attrToVisVarBean = attrToVisVarBean;
		this.name = name;
		this.attributeType = attributeType;
	}
	
	/**
	 * @return The name of this attribute.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The specific type of this attribute. (Double | String | Date | etc)
	 */
	public Class getAttibuteType() {
		return attributeType;
	}
	
	/** 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Attribute: " + name;
	}
	
}
