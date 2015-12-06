/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Displays a dialog asking the user to input a username, email, and any comments about the snapshot.
 *
 * @author Chris Callendar
 */
public class SnapshotDialog extends EscapeDialog {

	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	public static final int OK_OPTION = JOptionPane.OK_OPTION;

	private JTextArea commentArea;
	private JButton okBtn;
	private JButton cancelBtn;
	private JTextField userNameField;
	private JTextField emailField;
	private int returnValue = OK_OPTION;
	private String comment;

	/** Creates new form JDialog */
	public SnapshotDialog(Frame parent, boolean modal) {
		super(parent, "Snapshot Comments", modal);
		this.comment = "";
		initComponents();
	}

	private void initComponents() {
		JPanel topPanel = new TransparentPanel(new GridLayout(2, 1, 0, 5));
		Dimension d = new Dimension(80, 25);

		JPanel userNamePanel = new TransparentPanel(new BorderLayout(5, 0));
		JLabel userName = new JLabel("User name:");
		userName.setPreferredSize(d);
		userNamePanel.add(userName, BorderLayout.WEST);
		userNamePanel.add(getUserNameTextField(), BorderLayout.CENTER);
		topPanel.add(userNamePanel);

		JPanel emailPanel = new TransparentPanel(new BorderLayout(5, 0));
		JLabel emailLabel = new JLabel("Email:");
		emailLabel.setPreferredSize(d);
		emailPanel.add(emailLabel, BorderLayout.WEST);
		emailPanel.add(getEmailTextField(), BorderLayout.CENTER);
		topPanel.add(emailPanel);

		JLabel lbl = new JLabel("Comments:", SwingConstants.LEFT);
		lbl.setBorder(BorderFactory.createEmptyBorder(10, 0, 2, 0));

		JPanel centerPanel = new TransparentPanel(new BorderLayout());
		centerPanel.add(lbl, BorderLayout.NORTH);
		centerPanel.add(new JScrollPane(getCommentArea()));

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(getOKButton());
		buttonsPanel.add(getCancelButton());

		JPanel mainPanel = new TransparentPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(centerPanel, BorderLayout.CENTER);

		GradientPanel contentPane = new GradientPanel(new BorderLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
		setContentPane(contentPane);

		pack();
		getCommentArea().requestFocus(true);
		setDefaultButton(getOKButton());
		setPreferredSize(new Dimension(300, 300));

		getUserNameTextField().setText(ShrimpSnapShot.USER_NAME);
		getEmailTextField().setText((ShrimpSnapShot.USER_EMAIL == null ? "" : ShrimpSnapShot.USER_EMAIL));
	}

	private JTextField getEmailTextField() {
		if (emailField == null) {
			emailField = new JTextField();
			emailField.setMinimumSize(new Dimension(50, 20));
			emailField.setPreferredSize(new Dimension(200, 25));
		}
		return emailField;
	}

	private JTextField getUserNameTextField() {
		if (userNameField == null) {
			userNameField = new JTextField();
			userNameField.setMinimumSize(new Dimension(50, 20));
			userNameField.setPreferredSize(new Dimension(200, 25));
		}
		return userNameField;
	}

	private JButton getOKButton() {
		if (okBtn == null) {
			okBtn = new JButton(new AbstractAction("  OK  ") {
				public void actionPerformed(ActionEvent evt) {
					saveSnapshot(getUserNameTextField().getText().trim(),
								getEmailTextField().getText().trim(),
								getCommentArea().getText().trim());
					returnValue = OK_OPTION;
					setVisible(false);
				}
			});
		}
		return okBtn;
	}

	private JButton getCancelButton() {
		if (cancelBtn == null) {
			cancelBtn = new JButton(new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent evt) {
					returnValue = CANCEL_OPTION;
					setVisible(false);
				}
			});
		}
		return cancelBtn;
	}

	private JTextArea getCommentArea() {
		if (commentArea == null) {
			commentArea = new JTextArea();
			commentArea.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_TAB) {
						getOKButton().requestFocus();
						e.consume();
					}
				}
			});
			commentArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		}
		return commentArea;
	}

	public int showDialog(Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
		return returnValue;
	}

	private void saveSnapshot(String name, String email, String comment) {
		ShrimpSnapShot.USER_NAME = name;
		ShrimpSnapShot.USER_EMAIL = email;
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}

}