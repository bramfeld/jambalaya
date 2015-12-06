/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * @author Rob Lintern
 */
public class ChooseAttributeDialog extends JDialog {
	private JComboBox cmbxAttributes;
	private boolean accepted = false;

	public ChooseAttributeDialog (Vector nodes) {
		super(ApplicationAccessor.getParentFrame(), "Choose Attribute", true);
		accepted = false;
		Vector attributes = new Vector (nodes.size());
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			Artifact artifact = ((ShrimpNode) iter.next()).getArtifact();
			Vector keys = artifact.getAttributeNames();
			for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				if (!attributes.contains(key)) {
					attributes.add(key);
				}
			}
		}
		cmbxAttributes = new JComboBox(attributes);
		cmbxAttributes.setEditable(false);

		JPanel pnlButtons = new JPanel();

		// add the Accept button
		JButton applyButton = new JButton("Accept");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accepted = true;
				ChooseAttributeDialog.this.dispose();
			}
		});
		pnlButtons.add(applyButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accepted = false;
				ChooseAttributeDialog.this.dispose();
			}
		});
		pnlButtons.add(cancelButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(cmbxAttributes, BorderLayout.CENTER);
		getContentPane().add(pnlButtons, BorderLayout.SOUTH);

		pack();
		ShrimpUtils.centerOnScreen(this);
		setVisible(true);
	}

	public boolean isAccepted () {
		return accepted;
	}

	public String getSelectedAttribute () {
		return (String)cmbxAttributes.getSelectedItem();
	}

}
