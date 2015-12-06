/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.ShrimpUtils;


/**
 * This dialog will capture a message for this snapshot to be saved with it.
 *
 * @author Chris Callendar
 */
class CommentDialog extends EscapeDialog {

	private JTextArea area;
	private int choice = JOptionPane.CANCEL_OPTION;

	public CommentDialog(Frame frame, String comment, String messageToDisplay) {
		super(frame, "Snapshot Message", true);
		getContentPane().setLayout(new BorderLayout(2, 2));

		// the label
		JLabel title = new JLabel();
		title.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		title.setText(("".equals(messageToDisplay) ? "Insert a message to attach to this snapshot:" : messageToDisplay));
		getContentPane().add(title, BorderLayout.NORTH);

		// the text area
		area = new JTextArea(8, 30);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setText(comment);
		// scroll bar
		JScrollPane areaPanel = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		getContentPane().add(areaPanel, BorderLayout.CENTER);

		// buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

		JButton okButton = new JButton(" OK ");
		buttonPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				choice = JOptionPane.OK_OPTION;
				dispose();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				choice = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		// relocate
		pack();
		ShrimpUtils.centerOnScreen(this);
		setResizable(false);
		setDefaultButton(okButton);
		area.selectAll();
	}

	public int showDialog() {
		setVisible(true);	// blocks
		return choice;
	}

	public String getComment() {
		return area.getText();
	}

}
