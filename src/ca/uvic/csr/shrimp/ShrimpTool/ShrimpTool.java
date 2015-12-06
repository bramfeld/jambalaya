/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpTool;

import java.awt.Component;

import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;

/**
 * Defines interface for a ShrimpTool. A tool has a gui and manages a collection of beans.
 * 
 * @author Nasir Rather
 */
public interface ShrimpTool {

	/**
	 * Use this identifier to retrieve and set the displayBean.
	 * 
	 * @see ShrimpTool#getBean(String name)
	 */
	public static final String DISPLAY_BEAN = "displayBean";

	/**
	 * Use this identifier to retrieve and set the Filter Bean that interacts with the Display Bean
	 * 
	 * @see ShrimpTool#getBean(String name)
	 */
	public static final String DISPLAY_FILTER_BEAN = "displayfilterBean";

	/**
	 * Use this identifier to retrieve and set the selectorBean.
	 * 
	 * @see ShrimpTool#getBean(String name)
	 */
	public static final String SELECTOR_BEAN = "selectorBean";

	/**
	 * Use this identifier to retrieve and set the actionHistoryBean.
	 * 
	 * @see ShrimpTool#getBean(String name)
	 */
	public static final String ACTION_HISTORY_BEAN = "actionHistoryBean";

	/**
	 * Returns the user interface of this ShrimpTool.
	 * 
	 * @return Component The user interface of this tool.
	 */
	public Component getGUI();

	/**
	 * Disposes this ShrimpTool.
	 */
	public void disposeTool();

	/**
	 * Causes this tool to refresh.
	 */
	public void refresh();

	/**
	 * Returns the action manager for this tool
	 */
	public ActionManager getActionManager();

	/**
	 * Adds a bean to this tool. If the name or the bean is null, nothing will happen.
	 * 
	 * @param beanName The name of the bean to add
	 * @param bean The bean to add
	 */
	public void addBean(String beanName, Object bean);

	/**
	 * Each Tool will manage the beans it uses. This method retreives a bean by a string name.
	 * 
	 * @see #addBean(String, Object)
	 * @param beanName the name of the bean to get
	 * @return The bean with the given name.
	 * @throws BeanNotFoundException if a bean with the given name has not been added to this ShrimpTool.
	 */
	public Object getBean(String beanName) throws BeanNotFoundException;

	/**
	 * Removes the bean with the given name from this tool. If a bean with the given name has not been added 
	 * to this tool then nothing will happen.
	 * @param beanName
	 * @return Whether or not the bean with the given name was removed successfully.
	 */
	public boolean removeBean(String beanName);

	/**
	 * @return The name of this tool.
	 */
	public String getName();

	/**
	 * Clears up the tool - removes the gui components and clears any collections.
	 */
	public void clear();

	/**
	 * Gets the project associated with this tool. 
	 * @return the ShrimpProject for the tool
	 */
	public ShrimpProject getProject();
	
	/**
	 * Sets the project for the tool.  Calls refresh() on this tool after the project is set.
	 * @param project
	 */
	public void setProject(ShrimpProject project);

}