/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;


/**
 * @author Nasir Rather
 */
public interface ShrimpApplicationListener {

	/**
	 * This should be fired everytime a ShrimpApplication is activated 
	 * after being deactivated.
	 */
	public void applicationActivated(ShrimpApplicationEvent event);

	/**
	 * This should be fired everytime a ShrimpApplication is deactivated.
	 */
	public void applicationDeactivated(ShrimpApplicationEvent event);

	/**
	 * This should be fired right before ShrimpApplication is going to close.
	 */
	public void applicationClosing(ShrimpApplicationEvent event);

	/**
	 * This should be fired when the ShrimpApplication is opened/started.
	 */
	public void applicationStarted(ShrimpApplicationEvent event);

	/**
	 * This should be fired right after the User Controls are changed.
	 */
	public void userControlsChanged(ShrimpApplicationEvent event);

	/**
	 * This should be fired right after a ShrimpProject is created.
	 */
	public void projectCreated(ShrimpProjectEvent event);
	
	/**
	 * This is fired after a project is activated.
	 * @param event
	 */
	public void projectActivated(ShrimpProjectEvent event);

	/**
	 * This is fired after a project is closed.
	 * @param event
	 */
	public void projectClosed(ShrimpProjectEvent event);
	
}