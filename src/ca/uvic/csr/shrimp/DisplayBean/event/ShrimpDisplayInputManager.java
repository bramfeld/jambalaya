/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.event;



/**
 * Manages all input events coming from the display and fire events off to the registered display listeners.
 * 
 * @author Rob Lintern
 */
public interface ShrimpDisplayInputManager {
	
	public abstract void setActive(boolean b);
	public abstract boolean isActive();
	
	/**
	 * Add a shrimpMouseListener to the DisplayBean
	 */
	public abstract void addShrimpMouseListener(ShrimpMouseListener sml);
	/**
	 * Remove a shrimpMouseListener from the DisplayBean
	 */
	public abstract void removeShrimpMouseListener(ShrimpMouseListener sml);

	public abstract void dispose();

	/**
	 * Add a shrimpKeyListener to the DisplayBean
	 */
	public abstract void addShrimpKeyListener(ShrimpKeyListener skl);
	/**
	 * Remove a shrimpKeyListener from the DisplayBean
	 */
	public abstract void removeShrimpKeyListener(ShrimpKeyListener skl);
	
	public abstract ShrimpMouseEvent getLatestMouseEvent();
	public abstract ShrimpMouseEvent getPreviousMouseEvent();
	

}
