/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;

/**
 * An "attribute filter" filters objects by the values of their attributes.
 * For example, an artifact has a "type" attribute, and can be filtered according to this attribute.
 * 
 * Attibute filters consist of two types, nominal and ordinal. 
 * @see ca.uvic.csr.shrimp.FilterBean.NominalAttributeFilter
 * @see ca.uvic.csr.shrimp.FilterBean.OrdinalAttributeFilter 
 * 
 * @author Rob Lintern
 */
public abstract class AttributeFilter implements Filter {
	
	protected static final String DESC = "desc=";
	protected static final String DELIM = "|";
	
	protected String attributeName;
	protected Class attributeType;
	private String targetType;
	private String description;

	/**
	 * @param attributeName The name of the attribute that this filter considers.
	 * @param attributeType The class of the attribute that this filter considers.
	 * @param targetType The type of object that this filter acts upon.
	 */
	public AttributeFilter(String attributeName, Class attributeType, String targetType, String description) {
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.targetType = targetType;
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean hasDescription() {
		return ((description != null) && (description.length() > 0));
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.FilterBean.Filter#getTargetType()
	 */
	public String getTargetType() {
		return targetType;
	}

	/** 
	 * @see ca.uvic.csr.shrimp.FilterBean.Filter#isFiltered(java.lang.Object)
	 */
	public boolean isFiltered(Object object) {
		boolean result = false;
		if (object instanceof Artifact) {
			Object attributeValue = ((Artifact)object).getAttribute(attributeName);
			result = isFilteredByAttrValue(attributeValue);
		} else if (object instanceof Relationship) {
			Object attributeValue = ((Relationship)object).getAttribute(attributeName);
			result = isFilteredByAttrValue(attributeValue);
		} else if (object instanceof String) {
		    result = isFilteredByAttrValue(object);
        }
		return result;
	}
	
	protected abstract boolean isFilteredByAttrValue(Object attributeValue);

	/**
	 * @return The name of the attribute that this filter considers.
	 */
	public String getAttributeName() {
		return attributeName;
	}

	/**
	 * @return The class of the attribute that this filter considers.
	 */
	public Class getAttributeType() {
		return attributeType;
	}
	
	public abstract String toString();

	/**
	 * Converts a string that represents a filter, to an actual filter.
	 * @param s The string to convert.
	 * @return A filter described by the given string.
	 */
	public static Filter stringToFilter(String s) {
		AttributeFilter filter = null;
		StringTokenizer st = new StringTokenizer(s, DELIM);
		String filterType = st.nextToken();
		String targetType = st.nextToken();
		String attributeName = st.nextToken();
		String attributeTypeName = st.nextToken();
		Class attributeType = Object.class;
		try {
			attributeType = Class.forName(attributeTypeName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String description = null;
		if (filterType.equals(NOMINAL_ATTRIBUTE_FILTER)) {
			Collection filteredValues = new ArrayList();
			while (st.hasMoreTokens()) {
				String valueStr = st.nextToken();
				if (!st.hasMoreTokens() && ((description = getDescription(valueStr)) != null)) {
					// last one, found the description
					break;
				} else {
					Object valueObj = stringToObject(attributeType, valueStr);
					if (valueObj != null) {
						filteredValues.add(valueObj);
					}
				}
			}
			filter = new NominalAttributeFilter(attributeName, attributeType, targetType, filteredValues);
		} else if (filterType.equals(ORDINAL_ATTRIBUTE_FILTER)) {
			Object minValue = stringToObject(attributeType, st.nextToken());
			Object maxValue = stringToObject(attributeType, st.nextToken());
			if (st.hasMoreTokens()) {
				description = getDescription(st.nextToken());
			}
			filter = new OrdinalAttributeFilter(attributeName, attributeType, targetType, minValue, maxValue);
		} else {
			((new Exception("Can't handle filter type: " + filterType))).printStackTrace();
		}
		filter.setDescription(description);
		return filter;
	}
	
	private static String getDescription(String token) {
		String desc = null;
		if ((token != null) && token.startsWith(DESC)) {
			desc = token.substring(DESC.length());
		}
		return desc;
	}
	
	private static Object stringToObject(Class attributeType, String valueStr) {
		Object valueObj = null;
		if (attributeType == String.class) {
			valueObj = valueStr;
		} else if (attributeType == Integer.class) {
			valueObj = new Integer (valueStr);
		} else if (attributeType == Double.class) {
			valueObj = new Double (valueStr);
		} else if (attributeType == Long.class) {
			valueObj = new Long (valueStr);
		} else if (attributeType == Short.class) {
			valueObj = new Short (valueStr);
		} else if (attributeType == Byte.class) {
			valueObj = new Byte (valueStr);
		} else if (attributeType == Float.class) {
			valueObj = new Float (valueStr);
		} else {
			((new Exception("Can't handle: " + attributeType))).printStackTrace();
		}
		return valueObj;
	}
	
	// for testing
	public static void main(String[] args) {
		Collection filteredValues = new ArrayList(3);
		filteredValues.add("a");
		filteredValues.add("b");
		filteredValues.add("c");
		NominalAttributeFilter nomFilter = new NominalAttributeFilter(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, filteredValues);
		String nomStr = nomFilter.toString();
		System.out.println("nomStr: " + nomStr);
		
		OrdinalAttributeFilter ordFilter = new OrdinalAttributeFilter(AttributeConstants.ORD_ATTR_NUM_CHILDREN, Integer.class, FilterConstants.ARTIFACT_FILTER_TYPE, new Integer(1), new Integer(100));
		String ordStr = ordFilter.toString();
		System.out.println("ordStr: " + ordStr);
		
		Filter newFilter = AttributeFilter.stringToFilter(nomStr);
		System.out.println("newFilter: " + newFilter);
		
		newFilter = AttributeFilter.stringToFilter(ordStr);
		System.out.println("newFilter: " + newFilter);
	}

	
}
