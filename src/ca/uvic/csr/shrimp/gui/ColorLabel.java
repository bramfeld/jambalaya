/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;

import ca.uvic.csr.shrimp.util.GraphicsUtils;

/**
 * A label that lets the user choose a color.
 *
 * @author Chris Callendar
 * @date 17-Apr-07
 */
class ColorLabel extends JLabel implements MouseListener {

	private Color color = null;

	public ColorLabel(String text, int alignment, Color c) {
		super(text, alignment);
		setColor(c);
		initialize();
		setOpaque(true);
	}

	public ColorLabel(Color c) {
		this(" Color ", CENTER, c);
		setPreferredSize(new Dimension(60, 20));
		setMinimumSize(new Dimension(60, 20));
		setToolTipText("Click to choose the color for this type");
	}

	private void initialize() {
		setBorder(BorderFactory.createRaisedBevelBorder());
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(this);
	}

	public void setEnabled(boolean enabled) {
		if (isEnabled() != enabled) {
			if (enabled) {
				initialize();
			} else {
				setBorder(BorderFactory.createEmptyBorder());
				setCursor(Cursor.getDefaultCursor());
				removeMouseListener(this);
			}
			super.setEnabled(enabled);
		}
	}

	public void mousePressed(MouseEvent e) {
		if (isEnabled()) {
			setBorder(BorderFactory.createLoweredBevelBorder());
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (isEnabled()) {
			setBorder(BorderFactory.createRaisedBevelBorder());
			Color c = JColorChooser.showDialog(ColorLabel.this, "Choose a color", getColor());
			if (c != null) {
				setColor(c);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void setColor(Color c) {
		this.color = c;
		setBackground(c);
		setForeground(GraphicsUtils.getTextColor(c));
	}

	public Color getColor() {
		return this.color;
	}

}