/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;


/**
 * This is a default user project action.
 *
 * @author Chris Callendar
 * @date July 31st, 2006
 */
public abstract class DefaultProjectAction extends DefaultUserAction implements Serializable {

	private ShrimpProject project;

	public DefaultProjectAction(String actionName, ShrimpProject project) {
		this(actionName, (Icon)null, project);
	}

	public DefaultProjectAction(String actionName, Icon icon, ShrimpProject project) {
		super(actionName, icon);
		this.project = project;
	}

	public ShrimpProject getProject() {
		return project;
	}

	/**
	 * Sets the project for this action.
	 * @param project
	 */
	protected void setProject(ShrimpProject project) {
		this.project = project;
	}

	/**
	 * Checks if the project is null.
	 */
	public boolean hasProject() {
		return (getProject() != null);
	}

	public boolean canStart() {
		return hasProject();
	}

	/**
	 * Gets the bean with the given name from the project.
	 * If the project is null of the bean is not found null is returned.
	 * No exceptions are thrown.
	 * @param name
	 * @return the bean or null
	 */
	protected Object getBean(String name) {
		Object bean = null;
		if (project != null) {
			try {
				bean = project.getBean(name);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
		return bean;
	}

	/**
	 * Gets a tool from the current project.
	 * If the project is null, or the tool is not found then null is returned.
	 * No exception is thrown.
	 * @param toolName
	 * @return ShrimpTool
	 */
	protected ShrimpTool getTool(String toolName) {
		ShrimpTool tool = null;
		if (project != null) {
			try {
				tool = project.getTool(toolName);
			} catch (ShrimpToolNotFoundException ignore) {
			}
		}
		return tool;
	}

	protected ViewTool getViewTool(String toolName) {
		ShrimpTool stool = getTool(toolName);
		if (stool instanceof ViewTool) {
			return (ViewTool) stool;
		}
		return null;
	}


	protected DefaultUserAction cloneAction() {
		DefaultUserAction userAction = new DefaultProjectAction(getActionName(), getIcon(), getProject()) {
			public void startAction() {
				startAction();
			}
			public void stopAction() {
				stopAction();
			}
		};
		return userAction;
	}

	public void dispose() {
		super.dispose();
		this.project = null;
	}

	protected Object getTargetObject(ShrimpTool tool) {
		Object target = null;
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				target = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
		return target;
	}

	protected void setTargetObject(ShrimpTool tool, Object target) {
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectorBean.setSelected(SelectorBeanConstants.TARGET_OBJECT, target);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
	}

	protected ShrimpNode getSelectedNode(ShrimpTool tool) {
		ShrimpNode node = null;
		if (tool != null) {
			Object target = getTargetObject(tool);
			if (target instanceof ShrimpNode) {
				node = (ShrimpNode) target;
	    	}
		}
		return node;
	}

	protected Vector getSelectedNodes(ShrimpTool tool) {
		Vector selectedNodes = new Vector(0);
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
		return selectedNodes;
	}

	protected void setSelectedNodes(ShrimpTool tool, Vector nodes) {
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, nodes);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
	}

	protected void clearSelectedNodes(ShrimpTool tool) {
		setSelectedNodes(tool, new Vector(0));
	}

	protected Vector getSelectedArcs(ShrimpTool tool) {
		Vector selectedArcs = new Vector(0);
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectedArcs = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_ARCS);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
		return selectedArcs;
	}

	protected void setSelectedArcs(ShrimpTool tool, Vector arcs) {
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, arcs);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
	}

	protected void clearSelectedArcs(ShrimpTool tool) {
		setSelectedArcs(tool, new Vector(0));
	}

	protected String getZoomMode(ShrimpTool tool) {
		String zoomMode = null;
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				zoomMode = (String) selectorBean.getSelected(DisplayConstants.ZOOM_MODE);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
		return zoomMode;
	}

	protected void setZoomMode(ShrimpTool tool, String zoomMode) {
		if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				selectorBean.setSelected(DisplayConstants.ZOOM_MODE, zoomMode);
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		}
	}


}