/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.geom.Rectangle2D;

import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;

/**
 * @author Rob Lintern
 */
public interface ShrimpDisplayObject {

	/**
	 * @return True when this object is visible (has been rendered)
	 */
	public boolean isVisible();

	public Rectangle2D.Double getOuterBounds();

	public Rectangle2D.Double getGlobalOuterBounds();

	public void setTransparency(float t);

	public float getTransparency();

	public void dispose();

	public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener);

	public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener);

	/**
	 * Indicates whether or not this display object has been added to the display.
	 * In some cases, although not desirable, a display object can exist without being in the display.
	 */
	public boolean isInDisplay();

}