/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * A small dialog which lets the user choose a name for a new quick view
 * and the type: flat or nested.
 *
 * @author Chris Callendar
 * @date 27-Apr-07
 */
class NewQuickViewDialog extends EscapeDialog {

	private JTextField txtName;
	private JRadioButton btnNested;
	private JRadioButton btnFlat;
	private boolean okPressed;

	public NewQuickViewDialog(Component parent, String defaultName) {
		super(ShrimpUtils.getParentDialog(parent), "New Quick View", true);
		this.okPressed = false;

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl.add(new JLabel("Name: "));
		txtName = new JTextField(defaultName);
		txtName.setPreferredSize(new Dimension(90, 20));
		pnl.add(txtName);
		getContentPane().add(pnl, BorderLayout.NORTH);

		pnl = new JPanel(new GridLayout(2, 1, 0, 0));
		btnNested = new JRadioButton("Nested Quick View", true);
		pnl.add(btnNested);
		btnFlat = new JRadioButton("Flat Quick View");
		pnl.add(btnFlat);
		pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		getContentPane().add(pnl, BorderLayout.CENTER);

		ButtonGroup group = new ButtonGroup();
		group.add(btnNested);
		group.add(btnFlat);

		pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JButton btnOK = new JButton(createOKAction());
		pnl.add(btnOK);
		JButton btnCancel = new JButton(createCancelAction());
		pnl.add(btnCancel);
		getContentPane().add(pnl, BorderLayout.SOUTH);

		txtName.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				btnOK.setEnabled(txtName.getText().trim().length() > 0);
			}
		});

		pack();
		txtName.selectAll();
		txtName.requestFocus();
		setPreferredSize(new Dimension(160, 160));
		ShrimpUtils.centerWindowOnParent(this, parent);
		setDefaultButton(btnOK);
		setVisible(true);
	}

	public String getQuickViewName() {
		return txtName.getText().trim();
	}

	public boolean isOKPressed() {
		return okPressed;
	}

	public boolean isNested() {
		return btnNested.isSelected();
	}

	public boolean isFlat() {
		return btnFlat.isSelected();
	}

	protected void okPressed() {
		okPressed = true;
		super.okPressed();
	}

	protected void cancelPressed() {
		okPressed = false;
		super.cancelPressed();
	}

}