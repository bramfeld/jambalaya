/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/**
 * Extends the {@link DefaultProjectAction} to also contain a {@link ShrimpTool}.
 * This action can but is not required to also have a {@link ShrimpProject}.  Actions that
 * don't require a project can use one of the provided constructors that will set the project to null.
 *
 * @author Chris Callendar
 * @date July 31st, 2006
 */
public abstract class DefaultToolAction extends DefaultProjectAction {

	protected ShrimpTool tool;

	public DefaultToolAction(String actionName, ShrimpTool tool) {
		this(actionName, (Icon)null, tool);
	}

	public DefaultToolAction(String actionName, ShrimpProject project, ShrimpTool tool) {
		this(actionName, (Icon)null, project, tool);
	}

	public DefaultToolAction(String actionName, Icon icon, ShrimpTool tool) {
		this(actionName, icon, (tool != null ? tool.getProject() : null), tool);
	}

	public DefaultToolAction(String actionName, Icon icon, ShrimpProject project, ShrimpTool tool) {
		super(actionName, icon, project);
		this.tool = tool;
	}

	protected ShrimpTool getTool() {
		return tool;
	}

	protected DefaultUserAction cloneAction() {
		DefaultUserAction userAction = new DefaultToolAction(getActionName(), getIcon(), getProject(), tool) {
			public void startAction() {
				startAction();
			}
			public void stopAction() {
				stopAction();
			}
		};
		return userAction;
	}

	/**
	 * First gets the project from the super class, if that is null it attempts to
	 * get the project from the tool.
	 * @return the {@link ShrimpProject}, might be null
	 */
	public ShrimpProject getProject() {
		ShrimpProject project = super.getProject();
		if ((project == null) && (tool != null)) {
			project = tool.getProject();
		}
		return project;
	}

	public boolean canStart() {
		return super.canStart() && (tool != null);
	}

	public void dispose() {
		super.dispose();
		this.tool = null;
	}

	protected Object getTargetObject() {
		return super.getTargetObject(tool);
	}

	protected void setTargetObject(Object target) {
		super.setTargetObject(tool, target);
	}

	protected ShrimpNode getSelectedNode() {
		return super.getSelectedNode(tool);
	}

	protected Vector getSelectedNodes() {
		return super.getSelectedNodes(tool);
	}

	protected void setSelectedNodes(Vector nodes) {
		super.setSelectedNodes(tool, nodes);
	}

	protected void clearSelectedNodes() {
		super.clearSelectedNodes(tool);
	}

	protected Vector getSelectedArcs() {
		return super.getSelectedArcs(tool);
	}

	protected void setSelectedArcs(Vector arcs) {
		super.setSelectedArcs(tool, arcs);
	}

	protected void clearSelectedArcs() {
		super.clearSelectedArcs(tool);
	}

	protected String getZoomMode() {
		return super.getZoomMode(tool);
	}

	protected void setZoomMode(String zoomMode) {
		super.setZoomMode(tool, zoomMode);
	}

}
