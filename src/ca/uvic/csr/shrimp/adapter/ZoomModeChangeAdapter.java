/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.event.ModeChangeEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ZoomModeChangeListener;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to change the zoom mode
 *
 * @author Casey Best, Chris Callendar
 * date: July 27, 2000
 */
public class ZoomModeChangeAdapter implements ZoomModeChangeListener {
	
	private ShrimpTool tool;

	public ZoomModeChangeAdapter(ShrimpTool tool) {
		this.tool = tool;
	}
	
	/**
	 * Processes the request to change the zoom mode
	 * 
	 * @param event Contains the new zoom mode
	 */
	public void changeZoomMode (ModeChangeEvent event) {
		String newMode = event.getNewMode();
		changeZoomMode(newMode);
	}
	
	public void changeZoomMode(String newMode) {
		try {				
			SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
			selectorBean.setSelected (DisplayConstants.ZOOM_MODE, newMode);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}
	
}