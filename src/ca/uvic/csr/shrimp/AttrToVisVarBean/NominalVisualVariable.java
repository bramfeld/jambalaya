/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 *
 * A nominal visual variable is a visual variable that has discrete values rather
 * than a range of continuous values. For example, node shape has discrete values of
 * rectangle, triangle, ellipse, and rounded rectangle.
 *
 * A nominal visual variable is connected to a nominal attribute of some kind via the AttrToVisVarBean.
 * Each value of a nominal visual variable is mapped to a single value of this connected attribute.
 *
 * An example:
 * The visual variable of node shape is mapped to the attribute of artifact type.
 * <pre>
 *
 * Nominal Visual Variable Value  | Nominal Attribute Value
 * -------------------------------|------------------------
 *      RectangleNodeShape        |      "package"
 *   RoundedRectangleNodeShape    |      "class"
 *       EllipseNodeShape         |      "method"
 *
 * </pre>
 *
 * @see AttrToVisVarBean
 * @see NominalAttribute
 *
 * @author Rob Lintern
 */
abstract public class NominalVisualVariable extends VisualVariable {

	/**
	 * Constructs a new NominalVisualVariable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name of this visual variable.
	 */
	public NominalVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValue(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute, java.lang.Object)
	 */
	public Object getVisVarValue(Attribute attr, Object attributeValue) {
		Object visVarValue = getSavedNomVisVarValue(attr.getName(), attributeValue);
		if (visVarValue == null) {
			visVarValue = getNextDefaultNomVisVarValue(attributeValue);
			saveNomVisVarValue(attr.getName(), attributeValue, visVarValue);
		}
		return visVarValue;
	}

	/**
	 * Sets the value of this visual variable that is mapped to the given attribute and its value.
	 * @param attr The attribute to map to.
	 * @param attributeValue The value of the attribute to map to.
	 * @param visVarValue The value of this visual variable to be mapped to the given attribute and its value.
	 */
	public void setNomVisVarValue(Attribute attr, Object attributeValue, Object visVarValue) {
		// check if the new value is different from the old value
		Object oldValue = getSavedNomVisVarValue(attr.getName(), attributeValue);
		boolean changed = !ShrimpUtils.equals(oldValue, visVarValue);
		if (changed) {
			saveNomVisVarValue(attr.getName(), attributeValue, visVarValue);
			attrToVisVarBean.fireVisVarValuesChangeEvent(attr, this);
		}
	}

	public void setDefaultNomVisVarValue(Attribute attr, Object attributeValue, Object defaultVisVarValue) {
		Object existingVisVarValue = getSavedNomVisVarValue(attr.getName(), attributeValue);
		if (existingVisVarValue == null) {
			saveNomVisVarValue(attr.getName(), attributeValue, defaultVisVarValue);
		}
	}

	public Object getDefaultNomVisVarValue(String attrName, Object attrValue) {
		return getSavedNomVisVarValue(attrName, attrValue);
	}

	/**
	 * Returns a new default visual variable value based on the given attribute value.
	 * @param attributeValue An attribute value that requires a visual variable value to map to.
	 * @return A default visual variable value based on the given attribute value
	 */
	protected abstract Object getNextDefaultNomVisVarValue(Object attributeValue);

	/**
	 * Creates a properties key based on the given inputs for storing visual variable to attribute mappings.
	 * @param attrName
	 * @param visVarName
	 * @param attrValue
	 */
	protected static String createNomPropertiesKey(String attrName, String visVarName, Object attrValue) {
		return PROP_KEY_PREFIX + PROP_DELIM + attrName + PROP_DELIM + visVarName + PROP_DELIM + attrValue.toString();
	}

	/**
	 * Gets a saved nominal visual variable value.
	 * @param attrName
	 * @param attrValue
	 * @return The saved attribute value to a visual variable value mapping from properties
	 */
	private Object getSavedNomVisVarValue(String attrName, Object attrValue) {
		Object visVarValue = null;
		String propKey = createNomPropertiesKey(attrName, getName(), attrValue);
		String propValue = properties.getProperty(propKey);
		if (propValue != null) {
			visVarValue = getVisVarValueFromString(propValue);
		}
		return visVarValue;
	}

	/**
	 * Saves the mapping of an attribute value to a visual variable value mapping to properties.
	 * @param attrName
	 * @param attrValue
	 * @param visVarValue
	 */
	protected void saveNomVisVarValue(String attrName, Object attrValue, Object visVarValue) {
		String propKey = createNomPropertiesKey(attrName, getName(), attrValue);
		if (visVarValue == null) {
			properties.remove(propKey);
		} else {
			String propValue = getStringFromVisVarValue(visVarValue);
			// @tag Shrimp.NominalVisVar : only save to properties actual values, no blank strings
			if (propValue.length() > 0) {
				properties.setProperty(propKey, propValue);
			} else {
				properties.remove(propKey);
			}
		}
	}

}
