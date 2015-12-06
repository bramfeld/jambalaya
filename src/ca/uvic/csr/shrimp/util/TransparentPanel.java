/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JPanel;


/**
 * Calls {@link JPanel#setOpaque(boolean)} with false.
 *
 * @author Chris Callendar
 * @date 19-Oct-07
 */
public class TransparentPanel extends JPanel {

	public TransparentPanel() {
		super.setOpaque(false);
	}

	public TransparentPanel(LayoutManager layout) {
		super(layout);
		super.setOpaque(false);
	}

	public TransparentPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		super.setOpaque(false);
	}

	public TransparentPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		super.setOpaque(false);
	}

	public void setOpaque(boolean isOpaque) {
		// do nothing
	}

	public void setEmptyBorder(int padding) {
		setEmptyBorder(padding, padding, padding, padding);
	}

	public void setEmptyBorder(int top, int left, int bottom, int right) {
		setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
	}

}
