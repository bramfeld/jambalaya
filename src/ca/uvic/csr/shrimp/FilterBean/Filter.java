/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;


/**
 * This interface defines a standard Filter, mainly to filter artifacts
 * and relationships.  It will allow the system to employ new types of 
 * filters without modifying the Filter Bean.
 *
 * @author Casey Best, Rob Lintern
 * @date June 16, 2000
 */
public interface Filter {
	
	/** represents a nominal attribute filter */
	public static final String NOMINAL_ATTRIBUTE_FILTER = "nominal attribute";
	/** represents an ordinal attribute filter */
	public static final String ORDINAL_ATTRIBUTE_FILTER = "ordinal attribute";
	
	/**
	 * This method filters an object.  It will return true if this
	 * filter filters the object, and false if it doesn't.
	 * 
	 * @param object The object to be filtered
	 */
	public boolean isFiltered(Object object);
	
	/**
	* Returns this filter's type (eg. "nominal attribute" filter)
	*/
	public String getFilterType();
		
	/**
	 * @return The object type that this filter acts upon (eg. "artifact")
	 */
	public String getTargetType();

}