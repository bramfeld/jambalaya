/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Visual Variable for node label styles.
 *
 * @author Chris Bennett
 * @date October 2006
 */
public class LabelStyleVisualVariable extends NominalVisualVariable {

	public static final String DEFAULT_LABEL_STYLE = DisplayConstants.LABEL_STYLE_FULL;

	/**
	 * Basic constructor
	 * @param attrToVisVarBean
	 * @param name
	 */
	public LabelStyleVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		return DEFAULT_LABEL_STYLE;
	}

	public String getStringFromVisVarValue(Object visVarValue) {
		return (String) visVarValue; // value is already a string
	}

	public Object getVisVarValueFromString(String s) {
		return s; // value is already a string
	}

	protected void saveNomVisVarValue(String attrName, Object attrValue, Object visVarValue) {
		boolean sameAsDefault = ShrimpUtils.equals(DEFAULT_LABEL_STYLE, visVarValue);
		if (sameAsDefault) {
			// clears the property
			visVarValue = null;
		}
		super.saveNomVisVarValue(attrName, attrValue, visVarValue);
	}

}
