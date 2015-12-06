/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryBean;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryEvent;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryListener;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This adapter handles requests to undo the last action
 *
 * @author Casey Best, Chris Callendar
 * @date July 27, 2000
 */
public class UndoActionAdapter extends DefaultToolAction implements ActionHistoryListener {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_BACK;

	public static final String TOOLTIP = "Works like the back button in a browser - returns to the previous view.";

	private ActionManager actionManager;
	
	public UndoActionAdapter(ShrimpTool tool, ActionManager actionManager) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_back.gif"), tool);
		setToolTip(TOOLTIP);
		this.actionManager = actionManager;
	    mustStartAndStop = false;
	}
	
	public void startAction() {
		try {				
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) tool.getBean(ShrimpTool.ACTION_HISTORY_BEAN);					
			actionHistoryBean.undoAction();
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}	
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryListener#actionHistoryChanged(ActionHistoryEvent)
	 */
	public void actionHistoryChanged(ActionHistoryEvent actionHistoryEvent) {
		try {				
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) tool.getBean(ShrimpTool.ACTION_HISTORY_BEAN);		
			boolean undoPossible = actionHistoryBean.undoIsPossible();
			actionManager.setActionEnabled(ACTION_NAME, ShrimpConstants.MENU_NAVIGATE, undoPossible);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}	
	}

}