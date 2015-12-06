/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;


/**
 * Allows a specific string to be mapped to specific attribute values.
 * 
 * @author Rob Lintern
 */
public class StringVisualVariable extends NominalVisualVariable {
    
    /**
     * @param attrToVisVarBean
     * @param name
     */
    public StringVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
        super(attrToVisVarBean, name);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
     */
    protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
        return "";
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
     */
    public Object getVisVarValueFromString(String s) {
        return s;
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
     */
    public String getStringFromVisVarValue(Object visVarValue) {
        return visVarValue.toString();
    }

}
