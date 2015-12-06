/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.util.Date;

/**
 * A filter that filters objects by their ordinal attribute values (eg. "number of children of an artifact").
 * An ordinal attribute is an attribute of an object that has a value within a range of values
 * as opposed to a nominal attribute which has a value within a set of discrete values. 
 * 
 * Each ordinal attribute filter filters a range of values.
 * 
 * @author Rob Lintern
 */
public class OrdinalAttributeFilter extends AttributeFilter {
	
	private Object minOfUnfiltered;
	private Object maxOfUnfiltered;
	
	/**
	 * @param attributeName The name of the attribute that this filter should consider.
	 * @param attributeType The class of the attribute that this filter should consider.
	 * @param targetType The type of object that this filter acts upon.
	 * @param minOfUnfiltered The minimum value filtered by this filter.
	 * @param maxOfUnfiltered The maximum value filtered by this filter.
	 */
	public OrdinalAttributeFilter(String attributeName, Class attributeType, String targetType, 
			Object minOfUnfiltered, Object maxOfUnfiltered) {
		super(attributeName, attributeType, targetType, null);
		this.minOfUnfiltered = minOfUnfiltered;
		this.maxOfUnfiltered = maxOfUnfiltered;
	}

	protected boolean isFilteredByAttrValue(Object attributeValue) {
		boolean isFilteredValue = false;
		if (attributeValue != null) {
			// now see if the value of the attribute for this artifact, is outside of the min and max unfiltered values
			if (attributeValue instanceof Number) {
				// assumption made here that double value should work even if the attribute is of type float, long, or integer?
				double numValue = ((Number) attributeValue).doubleValue();
				double minNum = ((Number) minOfUnfiltered).doubleValue();
				double maxNum = ((Number) maxOfUnfiltered).doubleValue();
				isFilteredValue =  (numValue < minNum) || (numValue > maxNum);
			} else if (attributeValue instanceof Date) {
				Date attrDate = (Date)attributeValue;
				Date minDate = (Date)minOfUnfiltered;
				Date maxDate = (Date)maxOfUnfiltered;
				isFilteredValue = attrDate.before(minDate) || attrDate.after(maxDate);
			} else {
				System.err.println("OrdinalAttributeFilter can't handle attribute: " + attributeValue);
			}
		}
		return isFilteredValue;
	}

	/** 
	 * @see ca.uvic.csr.shrimp.FilterBean.Filter#getFilterType()
	 */
	public String getFilterType() {
		return ORDINAL_ATTRIBUTE_FILTER;
	}

	/**
	 * @return The maximum value filtered by this filter.
	 */
	public Object getMaxOfUnfiltered() {
		return maxOfUnfiltered;
	}

	/**
	 * @return The minimum value filtered by this filter.
	 */
	public Object getMinOfUnfiltered() {
		return minOfUnfiltered;
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer(getFilterType() + DELIM + getTargetType() + DELIM + 
				getAttributeName() + DELIM + getAttributeType().getName());
		
		s.append(DELIM);
		s.append(getMinOfUnfiltered().toString());
		s.append(DELIM);
		s.append(getMaxOfUnfiltered().toString());
		
		// @tag Shrimp.AttributeFilter.description
		String desc = getDescription();
		if ((desc != null) && (desc.length() > 0)) {
			s.append(DELIM);
			s.append(DESC + desc);
		}
		return s.toString();
	}
	
}
