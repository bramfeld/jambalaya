/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

/**
 * @author Nasir Rather
 */
public class ShrimpViewMouseModeChangedEvent {

	protected ShrimpView shrimpView;
	protected String mouseMode;

	public ShrimpViewMouseModeChangedEvent(ShrimpView shrimpView, String mouseMode) {
		this.shrimpView = shrimpView;
		this.mouseMode = mouseMode;
	}

	public ShrimpView getShrimpView() {
		return this.shrimpView;
	}

	public String getMouseMode() {
		return this.mouseMode;
	}
}
