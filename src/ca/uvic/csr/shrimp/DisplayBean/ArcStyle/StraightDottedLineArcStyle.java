/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

import java.io.Serializable;

/**
 * This is the dotted line Arc style used to display a multiple short coloured lines between two nodes.
 * @author Casey Best
 * @date Sept 14, 2001
 * @see ArcStyle
 */
public class StraightDottedLineArcStyle extends StraightLineArcStyle implements Serializable {

	public final static String NAME = "Dotted Line - Straight";

	public StraightDottedLineArcStyle() {
		super(NAME, true);
	}

	public Object clone() {
		ArcStyle style = new StraightDottedLineArcStyle();
		return style;
	}

}
