/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;


/**
 * @author Rob Lintern
 */
public class CheckBoxActionButton extends JButton {

	public CheckBoxActionButton(final CheckBoxAction a) {
		super(a);
		setText(null);
		setToolTipText((String) a.getValue(Action.NAME));
		setMargin(new Insets(0,0,1,1));
		this.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				CheckBoxAction action = (CheckBoxAction) CheckBoxActionButton.this.getAction();
				updateBorder (action);
			}
		});
		SwingUtilities.invokeLater(new Runnable () {
			public void run() {
				updateBorder(a);
			}
		});
	}
	
	private void updateBorder (CheckBoxAction action) {
		if (action.isChecked()) {
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		} else {
			setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		}
	}
	


}
