/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.HierarchicalView;

import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.util.PopupListener;


/**
 * Updates the selected targets.
 * Moved from being an inner class of {@link HierarchicalView}.
 * 
 * @author Chris Callendar
 * @date 26-Oct-06
 */
public class HvPopupListener extends PopupListener {

	private SelectorBean selectorBean;
	private HierarchicalView hv;

	public HvPopupListener(JPopupMenu popup, HierarchicalView hv, SelectorBean selectorBean) {
		super(popup, hv.getGUI());
		this.selectorBean = selectorBean;
		this.hv = hv;
		
		repopulatePopupMenu();
	}

	public void repopulatePopupMenu() {
		popup.removeAll();
		hv.getActionManager().repopulatePopUpMenu(ShrimpConstants.MENU_NODE, "", popup, "HierarchicalView Node Popup");
	}	

    protected boolean beforeShowPopup(MouseEvent e) {
    	boolean show = false;
    	Vector targets = new Vector();
		Object currentTarget = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
		
		if (currentTarget != null && currentTarget instanceof ShrimpLabel) {
			currentTarget = ((ShrimpLabel)currentTarget).getLabeledObject();
		}
		if (currentTarget != null && currentTarget instanceof ShrimpNode) {
			// If the target node is in the group of selected nodes then
			// the selected nodes should be the targets.
			// If target node is not in the group of selected nodes then
			// the target node should be the only target.
			Vector selected = (Vector) selectorBean.getSelected (SelectorBeanConstants.SELECTED_NODES);			
			if (selected.contains(currentTarget)) {
				targets = selected;
			} else {
				targets.add(currentTarget);
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, targets);
			}
			show = true;
		} 	
		return show;
    }
    
    
}
