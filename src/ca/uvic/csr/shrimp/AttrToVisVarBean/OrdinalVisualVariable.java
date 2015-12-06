/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * An ordinal visual variable has a range of continuous values, such as a range of colours.
 * The range is defined by setting a minimum and maximum value for this visual variable.
 * An ordinal visual variable is connected an ordinal attribute via the AttrToVisVarBean.
 *  
 * The value of this visual variable is based upon an attribute value and its position within the 
 * range of all this attribute's values.
 * An example:
 * The ordinal visual variable of node colour is mapped to the number of children attribute of an artifact.
 * The maximum value of the visual variable is set to be bright green (red=0, blue=0, green=255) and its minimum is
 * set to be black (red=0, blue=0, green=0). The number of children attribute for a particular data set ranges 
 * from 0 to 20.
 * For an artifact that has 5 children, its corresponding node on the screen will be a darkish 
 * green color (red=0, blue=0, green=255*0.25 or 64) 
 * 
 * <pre>
 * 
 * 	attr value         0        5                       20
 *   position          0.0      0.25                    1.0
 *                      |--------|-----------------------|
 * 	vis var value     black   dark green            bright green
 *  
 * 
 * </pre>
 * 
 * @see AttrToVisVarBean
 * @see OrdinalAttribute
 * @author Rob Lintern
 */
abstract public class OrdinalVisualVariable extends VisualVariable {
	
	/**
	 * The maximum value of this visual variable.
	 */
	protected Object maxVisVarValue;
	/**
	 * The minimum value of this visual variable.
	 */
	protected Object minVisVarValue;
	
	/**
	 * 
	 * @param attrToVisVarBean The bean that this visual variable is to registered with.
	 * @param name The name of this visual variable.
	 */
	public OrdinalVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	public Object getVisVarValue(Attribute attr, Object attrValue) {
		Object visVarValue = null;
		// calculate vis var value based on where attr value lies in range of known values
		if (attr instanceof OrdinalAttribute) {
			double position = ((OrdinalAttribute)attr).getPosition (attrValue);
			visVarValue = getVisVarValue(attrValue, position);
		}
		return visVarValue;
	}
	
	/**
	 * Sets the maximum value of this ordinal visual variable.
	 * @param maxVisVarValue
	 */
	public void setMaxVisVarValue(Object maxVisVarValue) {
		if (!ShrimpUtils.equals(this.maxVisVarValue, maxVisVarValue)) {
			this.maxVisVarValue = maxVisVarValue;
			attrToVisVarBean.fireVisVarValuesChangeEvent(attrToVisVarBean.getMappedAttribute(this.getName()), this);
		}
	}
	
	/**
	 * Sets the minimum value of this ordinal visual variable.
	 * @param minVisVarValue
	 */
	public void setMinVisVarValue(Object minVisVarValue) {
		if (!ShrimpUtils.equals(this.minVisVarValue, minVisVarValue)) {
			this.minVisVarValue = minVisVarValue;
			attrToVisVarBean.fireVisVarValuesChangeEvent(attrToVisVarBean.getMappedAttribute(this.getName()), this);
		}
	}
	
	/**
	 * 
	 * @return The maximum value of this ordinal visual variable
	 */
	public Object getMaxVisVarValue() {
		return maxVisVarValue;
	}
	
	/**
	 * 
	 * @return The minimum value of this ordinal visual variable
	 */
	public Object getMinVisVarValue (){
		return minVisVarValue;
	}
	
	/**
	 * 
	 * @param attrValue An attribute value.
	 * @param position The position of this value within the range of all values of the same attribute. This 
	 * value should be between 0.0 and 1.0 inclusive.
	 * @return The value of this visual variable based upon the given attribute value, and the given position
	 * of this value within the range of all values of the same attribute.
	 */
	abstract protected Object getVisVarValue (Object attrValue, double position);
	
	
	
}
