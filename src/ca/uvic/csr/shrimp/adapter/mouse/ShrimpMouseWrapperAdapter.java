/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.mouse;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.usercontrols.ZoomInAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutAnyModeAdapter;

/**
 * @author Nasir Rather
 *
 * This adapter directs the mouse event to the MouseSelectAndMoveAdapter or one of the 
 * magnify/zoom/fisheye in/out adapters depending on the mouse mode.
 */
public class ShrimpMouseWrapperAdapter implements ShrimpMouseListener {

	private MouseSelectAndMoveAdapter mouseSelectAndMoveListener;
	private ZoomInAnyModeAdapter zoomInAdapter;
	private ZoomOutAnyModeAdapter zoomOutAdapter;
	
	private ViewTool viewTool;
	
	public ShrimpMouseWrapperAdapter (ViewTool viewTool,
			MouseSelectAndMoveAdapter mouseSelectAndMoveListener,
			ZoomInAnyModeAdapter zoomInAdapter, 
			ZoomOutAnyModeAdapter zoomOutAdapter) {
		this.viewTool = viewTool;
		this.mouseSelectAndMoveListener = mouseSelectAndMoveListener;
		this.zoomInAdapter = zoomInAdapter;
		this.zoomOutAdapter = zoomOutAdapter;
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mousePressed(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mousePressed(ShrimpMouseEvent e) {
		if(viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_ZOOM_IN)) {
		    if (e.isLeftMouseButton()) {
				zoomInAdapter.startAction();
				return;
		    }
		} else if (viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_ZOOM_OUT)) {
		    if (e.isLeftMouseButton()) {
				zoomOutAdapter.startAction();
				return;
			} 			
		} else if (viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_SELECT)){
			mouseSelectAndMoveListener.mousePressed(e);
		}
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseReleased(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseReleased(ShrimpMouseEvent e) {
		if(viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_ZOOM_IN)) {
		    if (e.isLeftMouseButton()) {
				zoomOutAdapter.stopAction();
				return;
		    }
		} else if (viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_ZOOM_OUT)) {
		    if (e.isLeftMouseButton()) {
				zoomOutAdapter.stopAction();
				return;
			} 
		} else if (viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_SELECT)) {
			mouseSelectAndMoveListener.mouseReleased(e);
		}
	}

	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mouseDragged(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseDragged(ShrimpMouseEvent e) {
	    if (viewTool.getMouseMode().equals(DisplayConstants.MOUSE_MODE_SELECT)) {
	        mouseSelectAndMoveListener.mouseDragged(e);
	    }
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseClicked(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseClicked(ShrimpMouseEvent e) {
	    // don't care
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseEntered(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseEntered(ShrimpMouseEvent e) {
	    //don't care
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseExited(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseExited(ShrimpMouseEvent e) {
	    // don't care
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mouseMoved(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseMoved(ShrimpMouseEvent e) {
	    // don't care
	}
	
	public void mouseWheelMoved(ShrimpMouseEvent e) {
		// don't care
	}




}
