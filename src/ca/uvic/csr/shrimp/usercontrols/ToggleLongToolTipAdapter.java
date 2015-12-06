/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionListener;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;

/**
 * Toggles the use of long tooltips.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class ToggleLongToolTipAdapter extends CheckBoxAction implements ActionListener {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_LONG_TOOLTIPS;
	public static final String TOOLTIP = "Toggles the use of long tooltips.\nLong tooltips display extra information about the selected node or arc.";

	public ToggleLongToolTipAdapter(ShrimpProject project, ShrimpTool tool) {
		super(ACTION_NAME, (Icon)null, project, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;

		if (tool != null) {
			try {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				setChecked(displayBean.getUseLongTooltips());
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Toggles long tooltips
	 */
	public void startAction() {
		try {
			if (tool != null) {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				displayBean.setUseLongTooltips(isChecked());
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

}
