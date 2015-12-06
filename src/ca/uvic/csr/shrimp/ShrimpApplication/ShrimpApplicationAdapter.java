/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;


/**
 * @author Nasir Rather
 */
public abstract class ShrimpApplicationAdapter implements ShrimpApplicationListener {

	/**
	 * @see ShrimpApplicationListener#applicationActivated(ShrimpApplicationEvent)
	 */
	public void applicationActivated(ShrimpApplicationEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#applicationClosing(ShrimpApplicationEvent)
	 */
	public void applicationClosing(ShrimpApplicationEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#applicationDeactivated(ShrimpApplicationEvent)
	 */
	public void applicationDeactivated(ShrimpApplicationEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#applicationStarted(ShrimpApplicationEvent)
	 */
	public void applicationStarted(ShrimpApplicationEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#userControlsChanged(ShrimpApplicationEvent)
	 */
	public void userControlsChanged(ShrimpApplicationEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#projectCreated(ShrimpProjectEvent)
	 */
	public void projectCreated(ShrimpProjectEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#projectActivated(ShrimpProjectEvent)
	 */
	public void projectActivated(ShrimpProjectEvent event) {
	}

	/**
	 * @see ShrimpApplicationListener#projectClosed(ShrimpProjectEvent)
	 */
	public void projectClosed(ShrimpProjectEvent event) {
	}

}

