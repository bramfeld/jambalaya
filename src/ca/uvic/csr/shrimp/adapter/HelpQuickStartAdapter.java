/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.jambalaya.JambalayaApplication;
import ca.uvic.csr.shrimp.jambalaya.JambalayaView;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserAction;

/**
 * Shows the Quick Start panel
 * @author Chris Callendar
 */
public class HelpQuickStartAdapter extends DefaultUserAction {

	public static String ACTION_NAME = "Show Quick Start";

	public HelpQuickStartAdapter() {
		super(ACTION_NAME);
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		ShrimpApplication app = ApplicationAccessor.getApplication();
		if (app instanceof JambalayaApplication) {
			JambalayaView tab = JambalayaView.globalInstance();
			// tab will be null in applets
			if (tab != null) {
				tab.getContentPane().showQuickStartPanel();
			}
		}
	}

}
