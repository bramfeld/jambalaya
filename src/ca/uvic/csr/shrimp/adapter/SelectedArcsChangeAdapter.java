/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;

/**
 * This adapter listens for changes in the selector bean for
 * when a new arc is selected, or no arc is selected.
 * This class tries to first use the ActionManager from the ShrimpTool,
 * if that is null then it uses the project ActionManager.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class SelectedArcsChangeAdapter implements PropertyChangeListener {
	
	private ActionManager actionManager;
	private ShrimpTool tool;
	
	public SelectedArcsChangeAdapter(ShrimpTool tool, ActionManager actionManager) {
		this.tool = tool;
		this.actionManager = actionManager;
	}
	
	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (tool != null) {
			try {
				Vector oldSelectedArcs = (Vector) evt.getOldValue();
				Vector newSelectedArcs = (Vector) evt.getNewValue();
				if (actionManager != null) {
					boolean enabled = (newSelectedArcs.size() == 1);
					actionManager.setActionEnabled(ShrimpConstants.MENU_ARC, "", enabled);
				}
	
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				displayBean.highlight(oldSelectedArcs, false);
				displayBean.highlight(newSelectedArcs, true);
			} catch (BeanNotFoundException bnfe) {
				//bnfe.printStackTrace();
			}
		}
	}

}
