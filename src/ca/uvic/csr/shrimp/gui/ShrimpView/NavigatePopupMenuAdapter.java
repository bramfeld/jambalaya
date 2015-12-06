/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;


/**
 * Updates the zoom, magnify, and fisheye menu items when the Navigate menu is displayed.
 * Moved from being an inner class of {@link ShrimpView}. 
 * 
 * @author Chris Callendar
 * @date 26-Oct-06
 */
public class NavigatePopupMenuAdapter implements PopupMenuListener {

	private ShrimpView shrimpView;
	
	public NavigatePopupMenuAdapter(ShrimpView shrimpView) {
		this.shrimpView = shrimpView;
	}
	
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		// when the navigate menu is selected, update the selected zoom mode
		try {
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			ActionManager actionManager = shrimpView.getProject().getActionManager();
			String mode = (String) selectorBean.getSelected(DisplayConstants.ZOOM_MODE);
			try { 
				CheckBoxAction chkZoom = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_ZOOM_MODE, ShrimpConstants.MENU_NAVIGATE);
				CheckBoxAction chkMagnify = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_MAGNIFY_MODE, ShrimpConstants.MENU_NAVIGATE);
				CheckBoxAction chkFisheye = (CheckBoxAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_FISHEYE_MODE, ShrimpConstants.MENU_NAVIGATE);
				chkZoom.setChecked(mode.equals(DisplayConstants.ZOOM));
				chkMagnify.setChecked(mode.equals(DisplayConstants.MAGNIFY));
				chkFisheye.setChecked(mode.equals(DisplayConstants.FISHEYE));			
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
		}
	}

}
