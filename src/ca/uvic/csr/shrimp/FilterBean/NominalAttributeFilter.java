/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.util.Collection;
import java.util.Iterator;

/**
 * A filter that filters objects by their nominal attribute values (eg. "artifact type").
 * A nominal attribute is an attribute of an object that has a value within a set of discrete values,
 * as opposed to an ordinal attribute which has a value within a continuous range of values. 
 * 
 * Each nominal attribute filter contains a list of nominal values that are filtered by it.
 * 
 * @author Rob Lintern
 */
public class NominalAttributeFilter extends AttributeFilter {

	private Collection filteredValues;
	
	/**
	 * @param attributeName The name of the attribute that this filter should consider.
	 * @param attributeType The class of the attribute that this filter should consider.
	 * @param targetType The type of object that this filter acts upon.
	 * @param filteredValues The initial filtered nominal values of this filter.
	 */
	public NominalAttributeFilter(String attributeName, Class attributeType, String targetType, Collection filteredValues) {
		super(attributeName, attributeType, targetType, null);
		this.filteredValues = filteredValues;
	}

	protected boolean isFilteredByAttrValue(Object attributeValue) {
		boolean isFilteredValue = false;
		if (attributeValue != null) {
			// now see if the value of the attribute, is among the filtered values
			for (Iterator iter = filteredValues.iterator(); iter.hasNext() && !isFilteredValue;) {
				Object filteredValue = iter.next();
				isFilteredValue = filteredValue.equals(attributeValue);
			}	
		}
		return isFilteredValue;
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.FilterBean.Filter#getFilterType()
	 */
	public String getFilterType() {
		return NOMINAL_ATTRIBUTE_FILTER;
	}

	/**
	 * @return A reference to the values filtered by this filter.
	 */
	public Collection getFilteredValuesReference() {
		return filteredValues;
	}

	/**
	 * Sets the nominal values filtered by this filter.
	 * @param collection
	 */
	public void setFilteredValues(Collection collection) {
		filteredValues = collection;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer(getFilterType() + DELIM + getTargetType() + DELIM + 
				getAttributeName() + DELIM + getAttributeType().getName());

		Collection filteredValues = getFilteredValuesReference();
		for (Iterator iter = filteredValues.iterator(); iter.hasNext();) {
			Object filteredValue = iter.next();
			s.append(DELIM);
			s.append(filteredValue.toString());
		}

		// @tag Shrimp.AttributeFilter.description
		if (hasDescription()) {
			s.append(DELIM);
			s.append(DESC + getDescription());
		}
		return s.toString();	
	}
	
}
