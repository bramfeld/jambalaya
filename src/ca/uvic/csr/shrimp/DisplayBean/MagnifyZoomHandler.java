/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.geom.Rectangle2D;

/**
 * This event handler magnifies a selected node and makes it automatically fit within
 * the current drawing surface.
 *
 * @author Jingwei Wu, Rob Lintern
 */
public class MagnifyZoomHandler {

	/** The default animation time to magnify or shrink a specific node (1000 milliseconds). */
	public static final int DEFAULT_ANIMATION_TIME = 1000;
	/** The minimum animation time to magnify or shrink a specific node (0 milliseconds). */
	public static final int MIN_ANIMATION_TIME = 0;
	/** The maximum animation time to magnify or shrink a specific node (5000 milliseconds). */
	public static final int MAX_ANIMATION_TIME = 5000;
	/** A slower animation time to magnify or shrink a specific node (2000 milliseconds). */
	public static final int SLOW_ANIMATION_TIME = 3000;

	/**
	 * The time that is used to animate the currently
	 * selected node. Its preferred value is 1000.
	 * Assume the user wants to zoom at a single speed.
	 */
	private static int animationTime = DEFAULT_ANIMATION_TIME;

	/**
	 * Marks if this event handler is active or not.
	 */
	private boolean active = true;

	/**
	 * The display bean used for the shrimpview
	 */
	private DisplayBean displayBean;

	/**
	 * Constructs a new MagnifyZoomHandler.
	 * @param displayBean The displayBean this handler attaches to.
	 */
	public MagnifyZoomHandler(DisplayBean displayBean) {
		this.displayBean = displayBean;
	}

	/**
	 * Sets the animation time in milliseconds.
	 * Assume the user wants to zoom at one speed for all windows - make static.
	 * @param time The animation time.
	 */
	public static void setAnimationTime(int time) {
		animationTime = Math.max(MIN_ANIMATION_TIME, Math.min(MAX_ANIMATION_TIME, time));
	}

	/**
	 * Gets the animation time in milliseconds.
	 * Assume the user wants to zoom at one speed for all windows - make static.
	 * @return The animation time.
	 */
	public static int getAnimationTime() {
		return animationTime;
	}

	/** Sets the handler active/not active */
	public void setActive(boolean active) {
		this.active = active;
	}

	/** Returns whether or not this handler is active */
	public boolean isActive() {
		return active;
	}

	/* Magifies on the selected object*/
	public void focusOn(ShrimpDisplayObject targetObject) {
		if (targetObject == null) {
			return;
		}

		boolean surfInteracting = displayBean.isInteracting();
		boolean toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		try {
			displayBean.setToolTipEnabled(false);
			Rectangle2D.Double nodeBounds = massageBounds(targetObject.getGlobalOuterBounds());
			displayBean.setInteracting(true);
			displayBean.moveViewToCenterBounds(nodeBounds, true, animationTime, true);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			displayBean.setToolTipEnabled(toolTipStateBeforeAction);
			displayBean.setInteracting(surfInteracting);
		}
	}

	/**
	 * Calculates the proper bounds.
	 */
	public Rectangle2D.Double massageBounds(Rectangle2D.Double bounds) {
		double x = bounds.getX();
		double y = bounds.getY();
		double w = bounds.getWidth();
		double h = bounds.getHeight();

		x = x - w * 0.05f;
		y = y - h * 0.05f;
		w = w * 1.1f;
		h = h * 1.1f;
		return new Rectangle2D.Double(x, y, w, h);
	}

}
