/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;

/**
 * @author Rob Lintern
 *
 */
public interface LineArrowArcStyle {

	public boolean isUsingArrowHeads();

	/**
	 * @param b
	 * @param style The style of this arrow head
	 */
	public void setUseArrowHeads(boolean b, String style);

}
