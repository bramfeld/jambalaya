/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.DisplayBean.layout.TreeMapLayout;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.util.JIntegerComboBoxEditor;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * A dialog for selecting options for the the TreeMap Layout
 *
 * @author  Jeff Michaud
 */
public class TreeMapDialog extends JDialog {

	private boolean cancelled = false;

	private JPanel jPanel;
	private JLabel jLabel12;
	private JComboBox cboOrder;
	private JLabel jLabel1;
	private JLabel jLabel13;
	private JComboBox cboBorder;
	private JComboBox cboSize;
	private JLabel jLabel111;
	private JLabel jLabel11;
	private JComboBox cboAlgorithm;
	private JButton btnOK;
	private JButton btnCancel;

	private TreeMapLayout treeMapLayout;

	/**
	 * calls initComponents and sets name on Frame
	 */
	public TreeMapDialog(TreeMapLayout treeMapLayout) {
		super(ApplicationAccessor.getParentFrame(), "TreeMap Layout", true);
		this.treeMapLayout = treeMapLayout;
		initComponents();
	}

	/**
	 * Method initComponents.
	 * Draws all the widgets as well as populates all the
	 * combo boxes
	 */
	private void initComponents() {
		jPanel = new JPanel();
		cboAlgorithm = new JComboBox();
		jLabel1 = new JLabel();
		jLabel11 = new JLabel();
		jLabel12 = new JLabel();
		cboSize = new JComboBox();
		cboOrder = new JComboBox();
		cboBorder = new JComboBox();
		jLabel13 = new JLabel();
		jLabel111 = new JLabel();
		btnOK = new JButton();
		btnCancel = new JButton();

		jPanel.setSize(new Dimension(324, 392));
		jPanel.setLocation(new Point(32, 32));
		jPanel.setVisible(true);
		jPanel.setLayout(null);

		jPanel.add(cboAlgorithm);
		cboAlgorithm.setBounds(160, 70, 200, 25);

		jLabel1.setText("Order By:");
		jPanel.add(jLabel1);
		jLabel1.setBounds(40, 150, 120, 20);

		jLabel11.setText("Instructions:");
		jPanel.add(jLabel11);
		jLabel11.setBounds(40, 30, 320, 20);

		jLabel12.setText("Size By:");
		jPanel.add(jLabel12);
		jLabel12.setBounds(40, 110, 120, 20);

		jPanel.add(cboSize);
		cboSize.setBounds(160, 110, 200, 25);

		jPanel.add(cboOrder);
		cboOrder.setBounds(160, 150, 200, 25);

		jPanel.add(cboBorder);
		cboBorder.setBounds(160, 190, 200, 25);

		jLabel13.setText("Node Padding:");
		jPanel.add(jLabel13);
		jLabel13.setBounds(40, 190, 120, 20);

		jLabel111.setText("Algorithm:");
		jPanel.add(jLabel111);
		jLabel111.setBounds(40, 70, 80, 20);

		cboSize.setModel(new DefaultComboBoxModel(treeMapLayout.getSizeFields()));
		cboSize.getModel().setSelectedItem(treeMapLayout.getSizeField());

		cboOrder.setModel(new DefaultComboBoxModel(treeMapLayout.getOrderFields()));
		cboOrder.getModel().setSelectedItem(treeMapLayout.getOrderField());

		//Poplulate the Algorithm combo box and set a listener for when this field changes
		cboAlgorithm.setModel(new DefaultComboBoxModel(treeMapLayout.getAlgorithmFields()));
		cboAlgorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				cboAlgorithmActionPerformed(evt);
			}
		});
		//Get the current setting
		cboAlgorithm.getModel().setSelectedItem(treeMapLayout.getMapLayoutName());

		cboBorder.setModel(new DefaultComboBoxModel(new String[] { "0", "5", "10" }));
		cboBorder.getModel().setSelectedItem("" + treeMapLayout.getBorderField());
		cboBorder.setEditable(true);
		cboBorder.setEditor(new JIntegerComboBoxEditor(TreeMapLayout.DEFAULT_BORDER_SIZE));

		btnOK.setMnemonic('O');
		btnOK.setText("OK");
		btnOK.setDefaultCapable(true);
		btnOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOKActionPerformed(evt);
			}
		});
		jPanel.add(btnOK);
		btnOK.setBounds(170, 270, 81, 26);

		btnCancel.setMnemonic('C');
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnCancelActionPerformed(evt);
			}
		});

		jPanel.add(btnCancel);
		btnCancel.setBounds(280, 270, 81, 26);

		getContentPane().add(jPanel);
		jPanel.setPreferredSize(new Dimension(400, 400));
		pack();

		// put in center of screen
		ShrimpUtils.centerOnScreen(this);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				cancelled = true;
				dispose();
			}
		});

		setVisible(true);
	}

	/**
	 * Method btnCancelActionPerformed.
	 * @param evt
	 * Cancels out of the layout
	 */
	private void btnCancelActionPerformed(ActionEvent evt) {
		cancelled = true;
		setVisible(false);
		dispose();
	}

	/**
	 * Method btnOKActionPerformed.
	 * @param evt
	 * Sets the algorithm, orderfield, sizefield, and border size
	 */
	private void btnOKActionPerformed(ActionEvent evt) {
		treeMapLayout.setMapLayoutName(this.cboAlgorithm.getSelectedItem().toString());
		treeMapLayout.setOrderField(this.cboOrder.getSelectedItem().toString());
		treeMapLayout.setSizeField(this.cboSize.getSelectedItem().toString());
		treeMapLayout.setBorderField(Integer.parseInt(this.cboBorder.getSelectedItem().toString()));
		cancelled = false;
		dispose();
	}

	/**
	 * Method cboAlgorithmActionPerformed.
	 * @param evt
	 * All it does is enable and disable Order Field based on the algorithm selected
	 */
	private void cboAlgorithmActionPerformed(ActionEvent evt) {
		if (((String) cboAlgorithm.getSelectedItem()).equalsIgnoreCase(TreeMapLayout.LAYOUT_ORDERED)) {
			cboOrder.setEnabled(true);
			jLabel1.setEnabled(true);
		} else {
			cboOrder.setEnabled(false);
			jLabel1.setEnabled(false);
		}

	}

	/**
	 * Method isCancelled.
	 * @return boolean
	 */
	public boolean isCancelled() {
		return cancelled;
	}

}
