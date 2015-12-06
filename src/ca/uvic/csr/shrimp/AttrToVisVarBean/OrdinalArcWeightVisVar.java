/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;

/**
 * @author rlintern
 *
 */
public class OrdinalArcWeightVisVar extends OrdinalVisualVariable {
	private final static double MAX_ARC_WEIGHT = 30.0;
	private final static Double DEFAULT_ARC_WEIGHT = new Double (ShrimpArc.DEFAULT_ARC_WEIGHT);
	
	/**
	 * @param attrToVisVarBean
	 * @param name
	 */
	public OrdinalArcWeightVisVar(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable#getVisVarValue(java.lang.Object, double)
	 */
	protected Object getVisVarValue(Object attrsValue, double position) {
		return new Double (position * MAX_ARC_WEIGHT);
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
	 */
	public Object getVisVarValueFromString(String s) {
		Double visVarValue = null;
		if (s != null && !s.equals("")) {
			try {
				visVarValue = new Double (s);
			} catch (NumberFormatException e) {
				visVarValue = DEFAULT_ARC_WEIGHT;
			}
		}
		return visVarValue == null ? DEFAULT_ARC_WEIGHT : visVarValue;
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
	 */
	public String getStringFromVisVarValue(Object visVarValue) {
		return visVarValue.toString();
	}

}
