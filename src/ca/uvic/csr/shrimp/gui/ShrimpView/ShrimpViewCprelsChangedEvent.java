/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;


/**
 * @author Nasir Rather
 *
 */
public class ShrimpViewCprelsChangedEvent {
	protected ShrimpView shrimpView;
	protected String [] cprels;
	
	public ShrimpViewCprelsChangedEvent (ShrimpView shrimpView, String [] cprels) {
		this.shrimpView = shrimpView;
		this.cprels = cprels;
	}
	
	public ShrimpView getShrimpView() {
		return this.shrimpView;
	}
	
	public String [] getCprels () {
		return this.cprels;
	}
}

