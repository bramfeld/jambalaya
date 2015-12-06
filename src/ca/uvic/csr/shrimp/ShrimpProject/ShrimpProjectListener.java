/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;

import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;


/**
 * @author Nasir Rather
 */
public interface ShrimpProjectListener {

	/**
	 * This should be fired everytime a ShrimpProject is activated.
	 * That is either the first time the ShrimpProject was opened 
	 * or when the ShrimpProject was activated after being deactivated.
	 */
	public void projectActivated(ShrimpProjectEvent event);

	/**
	 * This should be fired everytime a ShrimpProject is deactivated.
	 * This is only applicable for {@link StandAloneProject}s.
	 * That is when the ShrimpProject was deactivated after being activated.
	 */
	public void projectDeactivated(ShrimpProjectEvent event);

	/**
	 * This should be fired right before ShrimpProject is going to close.
	 */
	public void projectClosing(ShrimpProjectEvent event);

}