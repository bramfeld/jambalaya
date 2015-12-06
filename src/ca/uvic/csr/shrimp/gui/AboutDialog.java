/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * @author Rob Lintern, Chris Callendar
 */
public class AboutDialog extends EscapeDialog {

	public AboutDialog(Frame owner) {
		super(owner, "About " + ApplicationAccessor.getAppName(), true);
		setContentPane(new GradientPanel(new BorderLayout()));

		JPanel top = new TransparentPanel(new BorderLayout());
		getContentPane().add(top, BorderLayout.NORTH);

		JLabel lblPoweredBy = new JLabel("Powered By", JLabel.CENTER);
		Font font = lblPoweredBy.getFont();
		lblPoweredBy.setFont(font.deriveFont(Font.BOLD).deriveFont(font.getSize2D()+2f));
		lblPoweredBy.setForeground(Color.white);
		top.add(lblPoweredBy, BorderLayout.NORTH);

		Icon icon = new ImageIcon(ResourceHandler.getResourceImage("shrimplogo_bevel.png"));
		JLabel imageLabel = new JLabel(icon);
		imageLabel.setOpaque(false);
		int imageWidth = icon.getIconWidth();
		int imageHeight = icon.getIconHeight();
		imageLabel.setSize(imageWidth, imageHeight);
		top.add(imageLabel, BorderLayout.CENTER);

		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		ShrimpApplication app = ApplicationAccessor.getApplication();
		textArea.setText("CHISEL Group, University of Victoria, Canada\n" + "Copyright 1998-2009\n\n" + app.getBuildInfo() + "\n"
				+ "Send questions, comments, and bug reports to:\n" + app.getHelpEmailAddress() + "\n\n" + app.getName()
				+ " uses the Piccolo zooming library from the\n" + "University of Maryland. See http://www.cs.umd.edu/hcil/piccolo/");
		textArea.setForeground(Color.white);
		textArea.setOpaque(false);

		JPanel center = new TransparentPanel(new FlowLayout(FlowLayout.CENTER));
		center.add(textArea);
		getContentPane().add(center);

		JPanel pnlBottom = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		JButton btnClose = new JButton(createCloseAction());
		setDefaultButton(btnClose);
		pnlBottom.add(btnClose);

		getContentPane().add(pnlBottom, BorderLayout.SOUTH);
		pack();
		setSize(564, 460);
		ShrimpUtils.centerWindowOnParent(this, owner);
		setVisible(true);
	}

}
