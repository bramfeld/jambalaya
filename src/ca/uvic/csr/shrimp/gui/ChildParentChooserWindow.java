/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * This class is a dialog that display a request for the user to choose a child parent relationship.
 *
 * @author Casey Best, Chris Callendar
 * @date Nov 6, 2000
 */
public class ChildParentChooserWindow extends EscapeDialog {

	private final List allCprelTypes;

	private JCheckBox chkNoHierarchy;
	private JButton btnOK;
	private JList lstArcTypes;
	private JCheckBox chkInvert;
	private boolean okPressed;
	
	public ChildParentChooserWindow(Frame frame, DisplayBean displayBean, final DataBean dataBean, final Rectangle bounds) {
		super(frame, "Hierarchy Chooser", true);

		okPressed = false;
		// Create the list of cprels
		allCprelTypes = dataBean.getHierarchicalRelationshipTypes(false, true);

		JPanel buttonsPanel = createButtonPanel(dataBean.getDefaultCprels(), dataBean.getDefaultCprelsInverted());
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		getContentPane().add(getNoHierarchyCheckBox(), BorderLayout.NORTH);
		getContentPane().add(createListPanel(), BorderLayout.CENTER);

		setBackground(Color.white);

		String[] currentCprels = displayBean.getCprels();
		setListSelection(Arrays.asList(currentCprels));
		int[] selectedIndices = getArcTypesList().getSelectedIndices();
		if (selectedIndices.length > 0) {
			getArcTypesList().ensureIndexIsVisible(selectedIndices[0]);
		}

		getInvertCheckBox().setSelected(displayBean.isInverted());
		getNoHierarchyCheckBox().setSelected(currentCprels.length == 0);
		getInvertCheckBox().setEnabled(!getNoHierarchyCheckBox().isSelected());
		getArcTypesList().setEnabled(!getNoHierarchyCheckBox().isSelected());
		
		pack();
		setMinimumSize(new Dimension(220, 220));
		if (bounds.isEmpty()) {
			ShrimpUtils.centerOnScreen(this);
		} else {
			setBounds(bounds);
		}
		setDefaultButton(btnOK);
		setVisible(true);
		// save the bounds (modal)
		bounds.setRect(getBounds());
	}

	private JPanel createListPanel() {
		JPanel pnlList = new JPanel(new BorderLayout());
		pnlList.setBorder(BorderFactory.createTitledBorder("Choose arc types to structure hierarchy"));
		pnlList.add(getInvertCheckBox(), BorderLayout.NORTH);
		pnlList.add(new JScrollPane(getArcTypesList()), BorderLayout.CENTER);
		return pnlList;
	}

	private JList getArcTypesList() {
		if (lstArcTypes == null) {
			lstArcTypes = new JList(allCprelTypes.toArray());
			lstArcTypes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		return lstArcTypes;
	}

	private JCheckBox getInvertCheckBox() {
		if (chkInvert == null) {
			chkInvert = new JCheckBox("Invert Hierarchy");
		}
		return chkInvert;
	}

	private JCheckBox getNoHierarchyCheckBox() {
		if (chkNoHierarchy == null) {
			chkNoHierarchy = new JCheckBox(new AbstractAction("No Hierarchy") {
				public void actionPerformed(ActionEvent e) {
					chkInvert.setEnabled(!chkNoHierarchy.isSelected());
					lstArcTypes.setEnabled(!chkNoHierarchy.isSelected());
				}
			});
			chkNoHierarchy.setFont(new Font(chkNoHierarchy.getFont().getName(), Font.BOLD, chkNoHierarchy.getFont().getSize()));
			chkNoHierarchy.setHorizontalAlignment(SwingConstants.CENTER);
			chkNoHierarchy.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		}
		return chkNoHierarchy;
	}

	private JPanel createButtonPanel(final String[] defaultCprels, final boolean defaultCprelsInverted) {
		// add the ok button
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		// the ok button
		btnOK = new JButton(new AbstractAction("    OK    ") {
			public void actionPerformed(ActionEvent event) {
				okPressed = true;
				dispose();
			}
		});
		buttonPanel.add(btnOK);

		// the cancel button
		JButton btnCancel = new JButton(new AbstractAction(" Cancel ") {
			public void actionPerformed(ActionEvent event) {
				okPressed = false;
				dispose();
			}
		});
		buttonPanel.add(btnCancel);

		// the set defaults button
		JButton btnRestoreDefault = new JButton(new DefaultShrimpAction("Defaults", "Restore default hierarchy values") {
			public void actionPerformed(ActionEvent event) {
				setListSelection(Arrays.asList(defaultCprels));
				chkNoHierarchy.setSelected(false);
				chkInvert.setSelected(defaultCprelsInverted);
				chkInvert.setEnabled(true);
				lstArcTypes.setEnabled(true);
			}
		});
		buttonPanel.add(btnRestoreDefault);

		return buttonPanel;
	}

	private void setListSelection(List relTypes) {
		int[] indices = new int[relTypes.size()];
		for (int i = 0; i < relTypes.size(); i++) {
			String relType = (String) relTypes.get(i);
			indices[i] = allCprelTypes.indexOf(relType);
		}
		getArcTypesList().setSelectedIndices(indices);
	}

	/**
	 * Returns whether or not the accept button was pressed
	 */
	public boolean accepted() {
		return okPressed;
	}

	/**
	 * Returns the chosen Child-Parent Relationships
	 */
	public String[] getCprels() {
		if (chkNoHierarchy.isSelected()) {
			return new String[0];
		}
		Object[] selectedValues = lstArcTypes.getSelectedValues();
		String[] selectedCprels = new String[selectedValues.length];
		System.arraycopy(selectedValues, 0, selectedCprels, 0, selectedValues.length);
		return selectedCprels;
	}

	public boolean isInverted() {
		return chkInvert.isSelected();
	}

}
