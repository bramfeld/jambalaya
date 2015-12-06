/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;


/**
 * @author Nasir Rather
 */
public class ShrimpApplicationEvent {
	protected ShrimpApplication application;
	
	public ShrimpApplicationEvent (ShrimpApplication application) {
		this.application = application;
	}
	
	public ShrimpApplication getApplication() {
		return this.application;
	}
}

