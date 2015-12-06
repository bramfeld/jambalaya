/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 * 
 * Created on Oct 10, 2002
 */
package ca.uvic.csr.shrimp.gui;


/**
 * @author Nasir Rather
 *
 */
public interface DoubleSliderListener {
	public void lowerBoundChanged (DoubleSliderEvent event);

	public void upperBoundChanged (DoubleSliderEvent event);
	
	public void rangeMoved (DoubleSliderEvent event);
}
