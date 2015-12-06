/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.util.Properties;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * This class represents a visual variable in Shrimp.
 * Visual variables can have a wide range of types.
 * For Example, a visual variable could be the shape of node, or the color of an arc
 *
 * @author Rob Lintern
 */
public abstract class VisualVariable {

	protected final static String PROP_KEY_PREFIX = "attrToVisVarMap";
	protected final static String PROP_DELIM = "|";

	private String name;
	protected AttrToVisVarBean attrToVisVarBean;
	protected Properties properties;

	/**
	 *
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public VisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		this.attrToVisVarBean = attrToVisVarBean;
		this.name = name;

		if (ApplicationAccessor.isApplicationSet()) {
			this.properties = ApplicationAccessor.getProperties();
		} else {
			this.properties = new Properties();	// for testing
		}
	}

	/**
	 * @return The name of this visual variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param attr
	 * @param attrValue
	 * @return The value of this visual variable based upon the given attribute and its given value.
	 */
	abstract public Object getVisVarValue(Attribute attr, Object attrValue);

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Visual Variable: " + name;
	}

	/**
	 * Converts a string that represents a value of this visual variable to an actual value of this visual variable.
	 * @param s A string that represents a value of this visual variable.
	 * @return An actual value of this visual variable based upon the passed in string.
	 */
	public abstract Object getVisVarValueFromString(String s);

	/**
	 * Converts a value of this visual variable to string that represents this value.
	 * @param visVarValue A value of this visual variable.
	 * @return A string that represents the passed in value.
	 */
	public abstract String getStringFromVisVarValue(Object visVarValue);

}
