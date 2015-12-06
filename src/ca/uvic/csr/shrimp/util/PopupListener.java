/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 * Displays a popup menu when applicable.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class PopupListener extends MouseAdapter {
	
	protected JPopupMenu popup;
	private Component invoker;
	
	public PopupListener(JPopupMenu popup) {
		this.popup = popup;
	}
	
	public PopupListener(JPopupMenu popup, Component invoker) {
		this(popup);
		this.invoker = invoker;
	}
	public final void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	public final void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	protected final void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			boolean show = beforeShowPopup(e);
			if (show) {
				showPopup(e);
			}
		}
	}
	
	/**
	 * Subclasses can override this method to perform operations before the popup menu is displayed.
	 * This method returns true by default.
	 * @return boolean if true the popup menu will be displayed, if false it will not be displayed
	 */
	protected boolean beforeShowPopup(MouseEvent e) {
		return true;
	}
	
	protected void showPopup(MouseEvent e) {
		if (invoker == null) {
			invoker = e.getComponent();
		}
		int x = e.getX();
		int y = e.getY();
		if (x + popup.getWidth() > invoker.getX() + invoker.getWidth()) {
			x = invoker.getX() + invoker.getWidth() - popup.getWidth();
		}
		if (y + popup.getHeight() > invoker.getY() + invoker.getHeight()) {
			y = invoker.getY() + invoker.getHeight() - popup.getHeight();
		}
		popup.show(invoker, e.getX(), e.getY());
	}

}