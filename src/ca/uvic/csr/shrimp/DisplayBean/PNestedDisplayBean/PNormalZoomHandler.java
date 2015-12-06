/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import edu.umd.cs.piccolo.PCamera;

/**
 * NoramlZoomHandler supports the basic zooming over a nested hierarchy.
 * The right mouse button is reserved for the zooming in and the left
 * mouse button for zooming out. The initial mouse press is chosen as
 * the zooming anchor point.
 *
 * Note: this class is hacked on Bederson's ZoomEventHandler.
 *
 * @author Jingwei Wu
 */
public class PNormalZoomHandler implements Runnable {
	
	/** The default zooming in scale value. */
	// @tag Shrimp.zooming -author=Chris -date="enCA:01/03/07" : zoom in scaling used to be 1.2f
	public static final double MAGNIFY_SCALE = 1.05f;	

	/** The default zooming out scale value. */
	public static final double SHRINK_SCALE = 0.80f;

	/** The default pause between zooming steps */
	public static final int DEFAULT_ZOOM_PAUSE = 0;
	public static final int MIN_ZOOM_PAUSE = 0;
	public static final int MAX_ZOOM_PAUSE = 100;
	
	// The amount to pause between zooming steps
	public static int zoomPause = DEFAULT_ZOOM_PAUSE;

	// The scale amount to zoom by.
	private double scaleValue;

	// True when event handlers are attached to a node.
	private boolean active = false;

	// The camera we are zooming within.
	private PCamera camera = null;

	// Event coords of mouse press (in object space)
	private Point2D pressObjPt;

	// True while zooming 
	private boolean zooming = false;

	// The minimum allowed magnification
	private double minMag = 0.0f;

	// The maximum allowed magnification (or disabled if less than 0)
	private double maxMag = -1.0f;

	// The display bean this zoom handler works inside.
	private DisplayBean displayBean;

	/** The previous state of the tooltip */
	private boolean toolTipStateBeforeAction;

	/**
	 * Constructs a new NormalZoomHandler.
	 */
	public PNormalZoomHandler(DisplayBean displayBean) {
		pressObjPt = new Point2D.Double();
		this.displayBean = displayBean;
		// a bit of a hack here
		if (displayBean instanceof PNestedDisplayBean) {
			camera = ((PNestedDisplayBean)displayBean).getPCanvas().getCamera();
		}
	}

	/**
	 * Sets this event handler active or not.
	 * @param active <code>true</code> to make this event handler active.
	 */
	public void setActive(boolean active) {
		if (this.active && !active) {
			// Turn off event handlers.
			this.active = false;
		} else if (!this.active && active) {
			// Turn on event handlers.
			this.active = true;
		}
	}

	public boolean isActive() {
		return active;
	}

	/**
	 *  Start zooming in
	 */
	public void startZoomingIn(ShrimpMouseEvent e) {
		if (e == null) {
			(new Exception ("Warning: ShrimpMouseEvent is null")).getStackTrace()[0].toString();
			return;
		}
		startZoomingIn(e.getX(), e.getY());
	}

	/**
	 *  Start zooming in, at the position expressed as x,y using the
	 *  default magnification factor
	 */
	public void startZoomingIn(double x, double y) {
		startZoomingIn(x, y, MAGNIFY_SCALE);
	}
	
	public void startZoomingIn(double x, double y, double magnification) {
		displayBean.setInteracting(true);
		scaleValue = magnification;
		pressObjPt.setLocation(x, y);
		toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		displayBean.setToolTipEnabled(false);
		startZooming();
	}


	/**
	 *  Start zooming Out
	 */
	public void startZoomingOut(ShrimpMouseEvent e) {
		displayBean.setInteracting(true);

		// Set the shrink scale.
		scaleValue = SHRINK_SCALE;

		pressObjPt.setLocation(e.getX(), e.getY());

		toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		displayBean.setToolTipEnabled(false);

		startZooming();
	}

	/**
	 *  Stop zooming in either direction
	 */
	public void stopZooming(ShrimpMouseEvent e) {
		stopZooming();
		displayBean.setInteracting(false);

		displayBean.setToolTipEnabled(toolTipStateBeforeAction);
	}

	/**
	 * Start animated zooming.
	 */
	private void startZooming() {
		zooming = true;
		zoomOneStep();
	}

	/**
	 * Stop animated zooming.
	 */
	public void stopZooming() {
		zooming = false;
	}

	/**
	 * Set the minimum magnification that the camera can be set to
	 * with this event handler. Setting the min mag to <= 0 disables
	 * this feature. If the min mag if set to a value which is greater
	 * than the current camera magnification, then the camera is left
	 * at its current magnification.
	 * @param newMinMag the new minimum magnification
	 */
	public void setMinMagnification(double newMinMag) {
		minMag = newMinMag;
	}

	/**
	 * Set the maximum magnification that the camera can be set to
	 * with this event handler. Setting the max mag to <= 0 disables
	 * this feature. If the max mag if set to a value which is less
	 * than the current camera magnification, then the camera is left
	 * at its current magnification.
	 * @param newMaxMag the new maximum magnification
	 */
	public void setMaxMagnification(double newMaxMag) {
		maxMag = newMaxMag;
	}

	/**
	 * Set the zoom speed.
	 * @param pause The amount of pause between zooming steps. Should be between 0(fast) and 100(slow).
	 */
	public static void setZoomSpeed(int pause) {
		if (pause < MIN_ZOOM_PAUSE)
			zoomPause = MIN_ZOOM_PAUSE;
		else if (pause > MAX_ZOOM_PAUSE)
			zoomPause = MAX_ZOOM_PAUSE;
		else
			zoomPause = pause;
	}

	/**
	 * Get the zoom speed.
	 * @return The zoom speed.
	 */
	public static int getZoomSpeed() {
		return zoomPause;
	}

	/**
	 * Do one basic zooming step and schedule the next zooming step.
	 */
	private void zoomOneStep() {
		if (zooming) {
			long startTime = System.currentTimeMillis();

			// Check for magnification bounds.
			double newMag, currentMag;
			currentMag = camera.getViewScale();
			newMag = currentMag * scaleValue;
			if (newMag < minMag) {
				scaleValue = minMag / currentMag;
			}
			if ((maxMag > 0) && (newMag > maxMag)) {
				scaleValue = maxMag / currentMag;
			}
			// Now, go ahead and zoom one step
			camera.scaleViewAboutPoint(scaleValue, pressObjPt.getX(), pressObjPt.getY());

			long sleepTime = zoomPause - (System.currentTimeMillis() - startTime);
			// don't want to zoom too fast, so take a little nap if needed 
			while (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (Exception e) {
					e.printStackTrace();
				}
				sleepTime = zoomPause - (System.currentTimeMillis() - startTime);
			}

			SwingUtilities.invokeLater(this); // calls the run method
		}
	}

	public void run() {
		PNormalZoomHandler.this.zoomOneStep();
	}
	
}
