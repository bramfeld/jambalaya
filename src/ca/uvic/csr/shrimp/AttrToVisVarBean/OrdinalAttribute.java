/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.util.Date;


/**
 * An attribute that has ordinal values, in other words a continuous range of values
 * Ordinal attributes are connected to ordinal visual variables via the AttrToVisVarBean
 *
 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable
 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean
 *
 * @author Rob Lintern
 *
 */
public class OrdinalAttribute extends Attribute {

	private double minAttrValue = Double.NaN;
	private double maxAttrValue = Double.NaN;

	/**
	 * @param attrToVisVarBean The bean that this attribute is to be registered with.
	 * @param name The name to be given to this attribute
	 * @param specificType The class to be given to this attribute.
	 * @param minAttrValue The known minimum value of this attribute.
	 * @param maxAttrValue The known maximum value of this attribute.
	 */
	public OrdinalAttribute(AttrToVisVarBean attrToVisVarBean, String name, Class specificType, Object minAttrValue, Object maxAttrValue) {
		super(attrToVisVarBean, name, specificType);
		this.minAttrValue = convertToDouble (minAttrValue);
		this.maxAttrValue = convertToDouble (maxAttrValue);
	}

    private double convertToDouble (Object obj) {
        if (obj instanceof Number) {
            return ((Number)obj).doubleValue();
        } else if (obj instanceof Date) {
            return ((Date)obj).getTime();
        } else {
            throw new IllegalArgumentException ("ordinal attribute value should be a java.lang.Number or a java.util.Date");
        }
    }

	/**
	 * Sets the maximum value of this attribute.
	 * @param maxAttrValue
	 */
	private void setMaxAttrValue (double newValue) {
        if (newValue != this.maxAttrValue) {
            this.maxAttrValue = newValue;
            attrToVisVarBean.fireVisVarValuesChangeEvent(this, attrToVisVarBean.getVisVars(this));
        }
	}

	/**
	 * Sets the minimum value of this attribute.
	 *
	 * @param minAttrValue
	 */
	private void setMinAttrValue (double newValue) {
        if (newValue != this.minAttrValue) {
            this.minAttrValue = newValue;
            attrToVisVarBean.fireVisVarValuesChangeEvent(this, attrToVisVarBean.getVisVars(this));
        }
	}

	/**
	 *
	 * @return The maximum value of this attribute.
	 */
	public double getMaxAttrValue () {
		return maxAttrValue;
	}

	/**
	 *
	 * @return The minimum value of this attribute.
	 */
	public double getMinAttrValue () {
		return minAttrValue;
	}

	/**
	 * @param attrValueObj
	 * @return The position (between 0.0 and 1.0 inclusive) of the given attribute value between this attribute's max and min values.
	 */
	public double getPosition (Object attrValueObj) {
		double position = 0.50;
        double attrValue = convertToDouble(attrValueObj);

        if (Double.isNaN(minAttrValue)) {
            minAttrValue = attrValue;
        }
        if (Double.isNaN(maxAttrValue)) {
            maxAttrValue = attrValue;
        }

        if (minAttrValue > attrValue) {
            setMinAttrValue(attrValue);
        }

        if (maxAttrValue < attrValue) {
            setMaxAttrValue(attrValue);
        }

        double range = maxAttrValue - minAttrValue;
        if (range != 0.0) {
            position = (attrValue - minAttrValue)/range;
        }

		return position;
	}


}
