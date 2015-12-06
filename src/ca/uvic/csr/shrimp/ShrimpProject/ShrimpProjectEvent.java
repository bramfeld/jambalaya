/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;


/**
 * @author Nasir Rather
 */
public class ShrimpProjectEvent {
	protected ShrimpProject project;
	
	public ShrimpProjectEvent (ShrimpProject project) {
		this.project = project;
	}
	
	public ShrimpProject getProject() {
		return this.project;
	}
}

