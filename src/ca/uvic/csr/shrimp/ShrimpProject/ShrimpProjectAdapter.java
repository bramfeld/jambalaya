/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;


/**
 * @author Nasir Rather
 */
public abstract class ShrimpProjectAdapter implements ShrimpProjectListener {

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectListener#projectActivated(ShrimpProjectEvent)
	 */
	public void projectActivated(ShrimpProjectEvent event) {
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectListener#projectDeactivated(ShrimpProjectEvent)
	 */
	public void projectDeactivated(ShrimpProjectEvent event) {
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectListener#projectClosing(ShrimpProjectEvent)
	 */
	public void projectClosing(ShrimpProjectEvent event) {
	}

}

