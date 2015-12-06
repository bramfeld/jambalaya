/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.usercontrols.DefaultToolAction;

/**
 * An action associated with a checkbox menu item.
 * @author	Rob Lintern
 */
public abstract class CheckBoxAction extends DefaultToolAction {

    /**  The key used for storing the checked option for the action. */
	public static final String CHECKED = "Checked";

	private boolean checked;
	private ButtonGroup buttonGroup;

	/**
	 * Only use this if this action doesn't need a project.
	 */
	public CheckBoxAction(String actionName) {
		this(actionName, (Icon)null, (ShrimpProject)null);
	}

	public CheckBoxAction(String actionName, ShrimpProject project) {
		this(actionName, (Icon)null, project);
	}

	/**
	 * Only use this if this action doesn't need a project.
	 */
	public CheckBoxAction(String actionName, Icon icon) {
		this(actionName, icon, (ShrimpProject)null, (ShrimpTool)null);
	}

	public CheckBoxAction(String actionName, Icon icon, ShrimpProject project) {
		this(actionName, icon, project, (ShrimpTool)null);
	}

	public CheckBoxAction(String actionName, Icon icon, ShrimpProject project, ShrimpTool tool) {
		super(actionName, icon, project, tool);
	}

	/**
	 * Only use this constructor if the action doesn't need a project or a tool.
	 */
	public CheckBoxAction(String actionName, ButtonGroup buttonGroup) {
		super(actionName, (ShrimpProject)null, (ShrimpTool)null);
		this.buttonGroup = buttonGroup;
	}

	public void setChecked(boolean newValue) {
		boolean oldValue = this.checked;
		if (oldValue != newValue) {
		    this.checked = newValue;
		    firePropertyChange(CheckBoxAction.CHECKED, new Boolean(oldValue), new Boolean(newValue));
		}
	}

	public boolean isChecked() {
		return checked;
	}

	public ButtonGroup getButtonGroup() {
		return buttonGroup;
	}

	public void setActionName(String name) {
		putValue(Action.NAME, name);
	}

}
