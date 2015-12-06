/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import ca.uvic.csr.shrimp.AttrToVisVarBean.ArcStyleVisualVariable;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;

/**
 * Creates a dialog for choosing the color and style of an arc.
 *
 * @author Chris Callendar
 */
public class ArcPresentationDialog extends StylePresentationDialog {

	private static final ArcStyle DEFAULT_STYLE = ArcStyleVisualVariable.DEFAULT_ARC_STYLE;

	private ArcStyle arcStyle;
	private Vector arcStyles;

	private JComboBox cmbStyle;
	private final Color defaultColor;

	/**
	 * Creates an ArcPresentationDialog.
	 * @param owner The owner of this dialog.
	 * @param arcStyles the available arc styles
	 * @param arcStyle The current arc style.
	 * @param color The current arc color.
	 * @param defaultColor the default arc color
	 * @param relativeTo The component to position this dialog relative to.
	 */
	public ArcPresentationDialog(Frame owner, Vector arcStyles, ArcStyle arcStyle,
						Color color, Color defaultColor, Component relativeTo) {
		super(owner, "Arc Style", color);

		this.arcStyles = arcStyles;
		this.arcStyle = arcStyle;
		this.defaultColor = defaultColor;

		createGUI(relativeTo);
	}

	protected void addRows() {
		createArcStylePanel();
		createColorPanel();
		createPreviewPanel();
		updateThumbnail();
	}

	/**
	 * Creates the components used to choose the arc style.
	 */
	private void createArcStylePanel() {
		cmbStyle = new JComboBox(arcStyles);
		cmbStyle.setSelectedItem(arcStyle);
		cmbStyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					arcStyle = (ArcStyle) e.getItem();
					updateThumbnail();
				}
			}
		});
		addRow("Arc Style", cmbStyle);
	}

	private void createColorPanel() {
		addRow("Arc Color", createColorChooserLabel());
	}

	protected void updateThumbnail() {
		pnlPreview.removeAll();
		JComponent thumbnail = arcStyle.getThumbnail(getColor());
		thumbnail.setOpaque(false);
		Dimension dim = thumbnail.getPreferredSize();
		dim.setSize(dim.width + 20, dim.height + 16);
		pnlPreview.setPreferredSize(dim);
		pnlPreview.setBorder(BorderFactory.createEmptyBorder(6, 6, 0, 6));
		pnlPreview.add(thumbnail, BorderLayout.CENTER);
		pnlPreview.validate();
	}

	protected void setDefaults() {
		cmbStyle.setSelectedItem(DEFAULT_STYLE);
		setColor(defaultColor);
	}

	/**
	 * @return the {@link ArcStyle} selected.
	 */
	public ArcStyle getArcStyle() {
		return arcStyle;
	}

}
