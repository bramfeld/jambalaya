/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CompositeArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Defines an arc style visual variable, meaning that this visual variable will have
 * nominal values of type ArcStyle.
 *
 * @author Rob Lintern
 */
public class ArcStyleVisualVariable extends NominalVisualVariable {

	public static final ArcStyle DEFAULT_ARC_STYLE = new StraightSolidLineArcStyle();

	/**
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public ArcStyleVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
	 */
	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		return DEFAULT_ARC_STYLE;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
	 */
	public Object getVisVarValueFromString(String s) {
		Object visVarValue = DEFAULT_ARC_STYLE;
		if (s.equals(StraightSolidLineArcStyle.NAME)) {
			visVarValue = new StraightSolidLineArcStyle();
		} else if (s.equals(StraightDottedLineArcStyle.NAME)) {
			visVarValue = new StraightDottedLineArcStyle();
		} else if (s.equals(CurvedSolidLineArcStyle.NAME)) {
			visVarValue = new CurvedSolidLineArcStyle();
		} else if (s.equals(CurvedDottedLineArcStyle.NAME)) {
			visVarValue = new CurvedDottedLineArcStyle();
		} else if (s.equals(CompositeArcStyle.COMPOSITE_STYLE)) {
		    visVarValue = new CompositeArcStyle();
		}
		return visVarValue;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
	 */
	public String getStringFromVisVarValue(Object visVarValue) {
		return ((ArcStyle)visVarValue).getName();
	}

	protected void saveNomVisVarValue(String attrName, Object attrValue, Object visVarValue) {
		boolean sameAsDefault = ShrimpUtils.equals(DEFAULT_ARC_STYLE, visVarValue);
		if (sameAsDefault) {
			// clears the property
			visVarValue = null;
		}
		super.saveNomVisVarValue(attrName, attrValue, visVarValue);
	}

}
