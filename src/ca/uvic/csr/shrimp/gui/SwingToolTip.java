/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JToolTip;


/**
 * Inspired by "Multi-line ToolTip" artical by Zafir Anjum
 * at http://www.codeguru.com/java/articles/122.shtml
 *
 * @author Jeff Michaud
 */
public class SwingToolTip extends JToolTip {

	public SwingToolTip() {
		this(Color.black, Color.white);
	}

	public SwingToolTip(Color fgColor, Color bgColor) {
		updateUI();
		setForeground(fgColor);
		setBackground(bgColor);

		int sum = bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue();
		Color borderColor = (sum > 400 ? bgColor.darker() : bgColor.brighter());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(borderColor, 1),
						BorderFactory.createEmptyBorder(5, 2, 5, 2)));
	}

	public void updateUI() {
		setUI(SwingMultiLineToolTipUI.createUI(this));
	}

}

