/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.util.Vector;

import javax.swing.Action;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.usercontrols.FilterSelectedArtifactsAdapter;


/**
 * Updates the filter selected nodes menu action when the Tools menu is displayed. 
 * Moved from being an inner class of {@link ShrimpView}.
 * 
 * @author Chris Callendar
 * @date 26-Oct-06
 */
public class ToolPopupMenuAdapter implements PopupMenuListener {
	
	private ShrimpView shrimpView;
	
	public ToolPopupMenuAdapter(ShrimpView shrimpView) {
		this.shrimpView = shrimpView;
	}
	
	public void popupMenuCanceled(PopupMenuEvent e) {}
	
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		try {
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);				
			Vector selected = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			boolean someSelected = !selected.isEmpty();
			try {
				// Filter Selected Node(s)		
				Action action = shrimpView.getProject().getActionManager().getAction(FilterSelectedArtifactsAdapter.ACTION_NAME, ShrimpConstants.MENU_EDIT); 	
				action.setEnabled(someSelected);
			} catch (Exception ex) { 
				ex.printStackTrace(); 
			}
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
}
