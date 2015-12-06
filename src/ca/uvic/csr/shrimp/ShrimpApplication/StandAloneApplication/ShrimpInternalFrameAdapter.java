/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication;

import java.beans.PropertyVetoException;

import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;


/**
 * Adapter for the shrimpView tool's internal frame.
 */
public class ShrimpInternalFrameAdapter extends InternalFrameAdapter {
	private CheckBoxAction action;
	private StandAloneProject project;

	public ShrimpInternalFrameAdapter(StandAloneProject project, CheckBoxAction action) {
		this.action = action;
		this.project = project;
	}

	public void internalFrameActivated(InternalFrameEvent e) {
		action.setChecked(true);
		project.setActive();
	}

	public void internalFrameDeactivated(InternalFrameEvent arg0) {
		action.setChecked(false);
		project.fireProjectDeactivatedEvent();
	}

	public void internalFrameDeiconified(InternalFrameEvent arg0) {
		super.internalFrameDeiconified(arg0);
	}

	public void internalFrameClosing(InternalFrameEvent e) {
		// HACK: this will bring focus to the window that will be closed
		try {
			e.getInternalFrame().setMaximum(true);
			e.getInternalFrame().setMaximum(false);
		} catch (PropertyVetoException exception) {
			// do nothing
		}
		project.disposeProject();
	}


}