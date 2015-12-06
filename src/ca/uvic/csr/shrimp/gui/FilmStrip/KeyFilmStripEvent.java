/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.Component;
import java.awt.event.KeyEvent;


/**
 * This class carries the details of the film strip event, including
 * the mouse button pressed, etc.
 *
 * @author Casey Best
 * date: Oct 24, 2000
 */

public class KeyFilmStripEvent extends KeyEvent {
	private SnapShot highlightedSnapShot, selectedSnapShot;
	
	public KeyFilmStripEvent (SnapShot highlightedSnapShot, SnapShot selectedSnapShot, KeyEvent e) {
		super ((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar());
		this.highlightedSnapShot = highlightedSnapShot;
		this.selectedSnapShot = selectedSnapShot;
	}
	
	/**
	 * returns the target snap shot
	 */
	public SnapShot getHighlightedSnapShot() {
		return highlightedSnapShot;
	}

	/**
	 * returns the selected snap shot
	 */
	public SnapShot getSelectedSnapShot() {
		return selectedSnapShot;
	}
}