/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.util.PopupListener;


/**
 * 
 * 
 * @author Chris Callendar
 * @date 26-Oct-06
 */
public class ArcPopupListener extends PopupListener {

	private ShrimpView shrimpView;
	private String arcType = "";
	
	public ArcPopupListener(JPopupMenu popup, ShrimpView shrimpView) {
		super(popup);
		this.shrimpView = shrimpView;
		repopulateArcPopupMenu();
	}

	protected boolean beforeShowPopup(MouseEvent e) {
		boolean show = false;
		
		SelectorBean selectorBean = null;
		try {
			selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		Object currentTarget = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);

		//This menu only applies to arcs - show menu option for filtering this arc type
		if (currentTarget instanceof ShrimpArc) {
			ShrimpArc arc = (ShrimpArc) currentTarget;

			ActionManager actionManager = shrimpView.getProject().getActionManager();
			boolean firing = actionManager.getFiringEvents();
			actionManager.setFiringEvents(false);

			//Remove previous arc Filter commands
            if (arcType != null) {
				if (actionManager.getAction("Filter All '" + arcType + "' Arcs", ShrimpConstants.MENU_ARC) != null) {
					actionManager.removeAction("Filter All '" + arcType + "' Arcs", ShrimpConstants.MENU_ARC, ActionManager.IGNORE_PARENT);
				}
            }
		
			//Add a menu option to filter this arc type
            if (arc.getRelationship() != null) {
				arcType = arc.getRelationship().getType();
				ShrimpAction action = new DefaultShrimpAction("Filter All '" + arcType + "' Arcs") {
					public void actionPerformed(ActionEvent e) {
						filterAllArcsByType();
					}
				};					
				actionManager.addAction(action, ShrimpConstants.MENU_ARC, ShrimpConstants.GROUP_B, 3);
            } else {
                arcType = null;
            }
            
            repopulateArcPopupMenu();

            // @tag Shrimp.fireActions : must reset the action firing state
			actionManager.setFiringEvents(firing);
            
			show = true;
		}
		return show;
    }	
	
	public void repopulateArcPopupMenu() {
		if ((popup != null) && (shrimpView.getProject() != null)) {
			shrimpView.getProject().getActionManager().repopulatePopUpMenu(ShrimpConstants.MENU_ARC, "", popup, "ShrimpView Arc Popup");			
		}
	}
	
	private void filterAllArcsByType() {
		try {
			FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			filterBean.setFiringEvents(true);
			filterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, 
					FilterConstants.RELATIONSHIP_FILTER_TYPE, arcType, true);
		} catch (BeanNotFoundException ignore) {
		}
	}

}
