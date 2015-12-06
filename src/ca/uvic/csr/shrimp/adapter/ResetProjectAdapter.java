/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Properties;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;

/**
 * Handle reset project requests
 * @author Chris Bennett
 */
public class ResetProjectAdapter  extends DefaultProjectAction {

	public static final String ACTION_NAME = "Reset Project";

	/**
	 * Default constructor
	 * @param project
	 */
	public ResetProjectAdapter(ShrimpProject project) {
		super(ACTION_NAME, project);
	}


	public void startAction() {
		int action = resetProperties();
		if (action == JOptionPane.YES_OPTION) {
			Properties projectProperties = getProject().getProperties();
			projectProperties.clear();
			getProject().saveProperties();
		}
	}

	/**
	 * Change a nodes name using a user dialog
	 * @return selected option (0 = continue, 1 = cancel)
	 */
	public int resetProperties() {
		Object[] options = {"Continue", "Cancel"};
		// this method throws an exception if a window is passed in
		return JOptionPane.showOptionDialog(null,
				"This will clear all project properties. " +
				"Project reload required for changes to take effect. Continue?",
			    "Reset Project Properties",
			    JOptionPane.YES_NO_OPTION,
			    JOptionPane.QUESTION_MESSAGE, null,
			    options, options[1]);
	}
}
