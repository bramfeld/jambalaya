/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.DisplayBean.event;


public interface ShrimpMouseListener {
	
	public void mouseClicked(ShrimpMouseEvent e);

	public void mouseEntered(ShrimpMouseEvent e);

	public void mouseExited(ShrimpMouseEvent e);

	public void mousePressed(ShrimpMouseEvent e);

	public void mouseReleased(ShrimpMouseEvent e);
	
	public void mouseDragged(ShrimpMouseEvent e);
	
	public void mouseMoved(ShrimpMouseEvent e);
	
	public void mouseWheelMoved(ShrimpMouseEvent e);
	
}
