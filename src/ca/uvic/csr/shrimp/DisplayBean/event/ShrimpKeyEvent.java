/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.DisplayBean.event;

import java.util.Vector;

public class ShrimpKeyEvent {
	private Object target;
	private Vector coords;
	private int keyCode;
	private int modifiers;
	

	public ShrimpKeyEvent(Object target, Vector coords, int keyCode, int modifiers) {
		this.target = target;
		this.coords = coords;
		this.keyCode = keyCode;
		this.modifiers = modifiers;
	}
    
	/*
	 * Returns the target of this event
	 */
	public Object getTarget() {
		return target;
	}

	/*
	 * Returns the coordinates of the target
	 */
	public Vector getCoordinates () {
		return coords;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public int getModifiers() {
		return modifiers;
	}

}


