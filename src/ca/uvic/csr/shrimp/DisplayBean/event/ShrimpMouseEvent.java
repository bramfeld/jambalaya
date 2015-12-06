/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.event;


import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;


public class ShrimpMouseEvent {

	private Object target;
	private double x;
	private double y;
	private int clickCount;
	private int modifiers;
	private int wheelRotation;
	
	public ShrimpMouseEvent(Object target, double x, double y, int modifiers, int clickCount, int wheelRotation) {
		this.target = target;
		this.x = x;
		this.y = y;
		this.clickCount = clickCount;
		this.modifiers = modifiers;
		this.wheelRotation = wheelRotation;
	}

	public Object getTarget() {
		return target;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Returns true if the last mouse event specifies the left mouse button.
	 *
	 * @return true if the left mouse button was active
	 */
	public boolean isLeftMouseButton() {
		 return ((getModifiers() & InputEvent.BUTTON1_MASK) != 0);   
	}

	/**
	 * Returns true if the last mouse event specifies the middle mouse button.
	 *
	 * @return true if the middle mouse button was active
	 */
	public boolean isMiddleMouseButton() {
		return ((getModifiers() & InputEvent.BUTTON2_MASK) == InputEvent.BUTTON2_MASK);
	}

	/**
	 * Returns true if the last mouse event specifies the right mouse button.
	 *
	 * @return true if the right mouse button was active
	 */
	public boolean isRightMouseButton() {
		return ((getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK);
	}
	
	public int getClickCount() {
		return clickCount;
	}
	
	/**
	 * Gets the wheel rotation, a negative values means the "up" and a positive value means "down".
	 * @see MouseWheelEvent#getWheelRotation()
	 * @return int the number of wheel rotations
	 */
	public int getWheelRotation() {
		return wheelRotation;
	}
	
	/**
	 * @see MouseWheelEvent#getWheelRotation()
	 * @return true if the mouse wheel was rotated upwards.
	 */
	public boolean isUpWheelRotation() {
		return (wheelRotation < 0);
	}

	/**
	 * @see MouseWheelEvent#getWheelRotation()
	 * @return true if the mouse wheel was rotated downwards.
	 */
	public boolean isDownWheelRotation() {
		return (wheelRotation > 0);
	}

}
