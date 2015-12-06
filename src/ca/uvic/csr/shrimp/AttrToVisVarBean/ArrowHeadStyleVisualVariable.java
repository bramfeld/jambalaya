/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArrowHead;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * A nominal visual variable to control the display of arrow heads on arcs.
 * @author Chris Bennett
 */
public class ArrowHeadStyleVisualVariable extends NominalVisualVariable {

	private static final String DEFAULT_ARROW_HEAD_STYLE = ArrowHead.DEFAULT_STYLE;

	/**
	 * Basic constructor
	 * @param attrToVisVarBean
	 * @param name
	 */
	public ArrowHeadStyleVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		return DEFAULT_ARROW_HEAD_STYLE;
	}

	public String getStringFromVisVarValue(Object visVarValue) {
		return (String)visVarValue; // value is already a string
	}

	public Object getVisVarValueFromString(String s) {
		return s; // value is already a string
	}

	protected void saveNomVisVarValue(String attrName, Object attrValue, Object visVarValue) {
		boolean sameAsDefault = ShrimpUtils.equals(DEFAULT_ARROW_HEAD_STYLE, visVarValue);
		if (!sameAsDefault) {
			super.saveNomVisVarValue(attrName, attrValue, visVarValue);
		}
	}

}
