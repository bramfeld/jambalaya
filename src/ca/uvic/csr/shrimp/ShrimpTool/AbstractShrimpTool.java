/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 *
 * Created on Oct 29, 2002
 */
package ca.uvic.csr.shrimp.ShrimpTool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;

import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;

/**
 *
 * @author Nasir Rather, Chris Callendar
 */
public abstract class AbstractShrimpTool implements ShrimpTool {

	/** The action manager that this tool uses. **/
	protected ActionManager actionManager;
	protected Map beansMap;
	protected String toolName;
	protected ShrimpProject project;

	public AbstractShrimpTool(String toolName, ShrimpProject project) {
		this.toolName = toolName;
		this.project = project;
		this.beansMap = new HashMap();
		if (project != null) {
			project.addTool(toolName, this);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getActionManager()
	 */
	public ActionManager getActionManager() {
		return actionManager;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getName()
	 */
	public String getName() {
		return toolName;
	}

	public String toString() {
		return "ShrimpTool: " + getName();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getBean(String)
	 */
	public Object getBean(String beanName) throws BeanNotFoundException{
		Object bean = beansMap.get(beanName);
		if (bean == null) {
			throw new BeanNotFoundException(beanName);
		}
		return bean;
	}

	/**
	 * Adds a bean to this tool. If the name or the bean is null, nothing will happen.
	 */
	public void addBean(String beanName, Object bean) {
		if (bean != null) {
			beansMap.put(beanName, bean);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#removeBean(java.lang.String)
	 */
	public boolean removeBean(String beanName) {
		boolean b = false;
		if (beansMap.containsKey(beanName)) {
			beansMap.remove(beanName);
			b = true;
		}
		return b;
	}

	public boolean hasBean(String beanName) {
		return beansMap.containsKey(beanName);
	}

	protected void recreateMenuBar() {
		if (actionManager != null && getGUI() != null && (getGUI() instanceof JRootPane)) {
			JMenuBar menuBar = new JMenuBar();
			JMenu parentMenu = actionManager.createMenus(getName());
			Vector childMenus = new Vector (parentMenu.getItemCount());
			for (int i = 0; i < parentMenu.getItemCount(); i++) {
				JMenuItem childMenu = parentMenu.getItem(i);
				if (childMenu instanceof JMenu) {
					childMenus.add(childMenu);
				} else {
					System.out.println("not a jmenu: " + childMenu.getClass());
				}
			}
			for (Iterator iter = childMenus.iterator(); iter.hasNext();) {
				JMenu childMenu = (JMenu) iter.next();
				menuBar.add(childMenu);
			}
			((JRootPane)getGUI()).setJMenuBar(menuBar);
			((JRootPane)getGUI()).invalidate();
			((JRootPane)getGUI()).validate();
		}
	}

	/**
	 * Gets the project for this tool.
	 * @return ShrimpProject which can be null
	 */
	public ShrimpProject getProject() {
		return project;
	}

	/**
	 * Sets the current project for this tool. Calls {@link ShrimpTool#refresh()}.
	 */
	public void setProject(ShrimpProject project) {
		// only remove the tool if the old project exists
		if (this.project != null) {
			// @tag Shrimp.ProjectClosingRemoveTool
			this.project.removeTool(toolName);
		}
		this.project = project;

		if (this.project != null) {
			refresh();
			project.addTool(toolName, this);
		}
	}

}
