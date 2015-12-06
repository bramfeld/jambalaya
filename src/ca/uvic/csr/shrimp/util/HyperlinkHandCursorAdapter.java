/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. 
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.Cursor;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * Displays a hand cursor in Shrimp when the mouse is over a link.
 * 
 * @author Chris Callendar
 * @date 5-Mar-07
 */
public class HyperlinkHandCursorAdapter implements HyperlinkListener {

	public void hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
				ApplicationAccessor.getApplication().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
				ApplicationAccessor.getApplication().defaultCursor();
			} else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				ApplicationAccessor.getApplication().defaultCursor();
			}
		} catch (Exception ex) {
		}
	}
	
}