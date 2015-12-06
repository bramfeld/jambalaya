/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;


/**
 * Allows a boolean value: true or false.
 *
 * @author Chris Callendar
 */
public class BooleanVisualVariable extends NominalVisualVariable {

	public static final Boolean DEFAULT = new Boolean(true);

    /**
     * @param attrToVisVarBean
     * @param name
     */
    public BooleanVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
        super(attrToVisVarBean, name);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
     */
    protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
        return DEFAULT;
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
     */
    public Object getVisVarValueFromString(String s) {
    	Boolean value = DEFAULT;
    	try {
    		value = Boolean.valueOf(s);
    	} catch (Exception ex) {
    	}
        return value;
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
     */
    public String getStringFromVisVarValue(Object visVarValue) {
        return visVarValue.toString().toLowerCase();
    }

}
