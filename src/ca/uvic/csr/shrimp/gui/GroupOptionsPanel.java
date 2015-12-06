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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 *
 * @author Chris Callendar
 */
class GroupOptionsPanel extends GradientPanel {

    private static final Color DEFAULT_COMPOSITE_COLOR = Color.RED;

	private RelTypeGroup relTypeGroup;
	private GroupPanel groupPanel;
	private AttrToVisVarBean attrToVisVarBean;
	private DataDisplayBridge dataDisplayBridge;
	private DataBean dataBean;

	private JCheckBox chkEnableComposites;
	private ColorLabel colorLabel;
	private JButton closeButton;
	private JSlider highLevelSlider;

	public GroupOptionsPanel(GroupPanel groupPanel, AttrToVisVarBean attrToVisVarBean, DataDisplayBridge dataDisplayBridge, DataBean dataBean) {
		this.groupPanel = groupPanel;
		this.attrToVisVarBean = attrToVisVarBean;
		this.dataDisplayBridge = dataDisplayBridge;
		this.dataBean = dataBean;
		this.relTypeGroup = groupPanel.getRelTypeGroup();

		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 5));

		this.add(getEnableCompositesCheckBox(), BorderLayout.NORTH);

		JPanel mainPanel = new TransparentPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.white));
		this.add(mainPanel, BorderLayout.CENTER);

		JPanel colorPanel = new TransparentPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lbl = new JLabel("Color: ");
		colorPanel.add(lbl);
		colorPanel.add(getColorLabel());
		mainPanel.add(colorPanel, BorderLayout.NORTH);

		JPanel sliderPanel = new TransparentPanel(new BorderLayout());
		final JLabel highLevelSliderLabel = new JLabel("Filter by number of Arcs contained:");
		highLevelSliderLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sliderPanel.add(highLevelSliderLabel, BorderLayout.NORTH);
		sliderPanel.add(getHighLevelSlider(), BorderLayout.CENTER);
		mainPanel.add(sliderPanel, BorderLayout.CENTER);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnl.setOpaque(true);
		pnl.add(getCloseButton());
		this.add(pnl, BorderLayout.SOUTH);

		getColorLabel().setEnabled(false);
		getHighLevelSlider().setEnabled(false);
		highLevelSliderLabel.setEnabled(false);

		getEnableCompositesCheckBox().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = getEnableCompositesCheckBox().isSelected();
				getColorLabel().setEnabled(enabled);
				highLevelSliderLabel.setEnabled(enabled);
				getHighLevelSlider().setEnabled(enabled);
				highLevelSliderLabel.setEnabled(enabled);
				applyChanges();
			}
		});
	}

	protected JSlider getHighLevelSlider() {
		if (highLevelSlider == null) {
			highLevelSlider = new JSlider(0, 100, 0);
			highLevelSlider.addChangeListener(new ChangeListener() {
				private int last = 0;
				public void stateChanged(ChangeEvent e) {
					int value = highLevelSlider.getValue();
					if (!highLevelSlider.getValueIsAdjusting() && (value != last)) {
						last = value;
						applyChanges();
					}
				}
			});
			highLevelSlider.setOpaque(false);
			highLevelSlider.setMajorTickSpacing(10);
			highLevelSlider.setMinorTickSpacing(5);
			highLevelSlider.setPaintTicks(true);
			highLevelSlider.setPaintLabels(true);
			highLevelSlider.setSnapToTicks(true);
			highLevelSlider.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		}
		return highLevelSlider;
	}

	private JCheckBox getEnableCompositesCheckBox() {
		if (chkEnableComposites == null) {
			chkEnableComposites = new JCheckBox("Enable High Level Arcs?");
			chkEnableComposites.setOpaque(false);
			chkEnableComposites.setFont(chkEnableComposites.getFont().deriveFont(Font.BOLD));
			chkEnableComposites.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
		}
		return chkEnableComposites;
	}

	private ColorLabel getColorLabel() {
		if (colorLabel == null) {
			colorLabel = new ColorLabel(DEFAULT_COMPOSITE_COLOR);
			colorLabel.setBorder(BorderFactory.createRaisedBevelBorder());
			colorLabel.setPreferredSize(new Dimension(60, 20));
			colorLabel.setToolTipText("The color for composite arcs");
			colorLabel.addPropertyChangeListener(new PropertyChangeListener() {
				private Color last = DEFAULT_COMPOSITE_COLOR;
				public void propertyChange(PropertyChangeEvent evt) {
					if (!last.equals(colorLabel.getBackground())) {
						last = colorLabel.getBackground();
						applyChanges();
					}
				}
			});
		}
		return colorLabel;
	}

	public JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(new AbstractAction("  Close  ") {
				public void actionPerformed(ActionEvent e) {
					Window window = SwingUtilities.windowForComponent(GroupOptionsPanel.this);
					window.setVisible(false);
				}
			});
		}
		return closeButton;
	}

	public void refresh() {
		if (relTypeGroup.areCompositesEnabled()) {
			getEnableCompositesCheckBox().setSelected(true);
			getColorLabel().setBackground(relTypeGroup.getCompositeColor());
		} else {
			getEnableCompositesCheckBox().setSelected(false);
		}
	}

	protected void applyChanges() {
	    boolean firingEvents = dataBean.isFiringEvents();
	    dataBean.setFiringEvents(false);

		boolean enabled = getEnableCompositesCheckBox().isSelected();
	    Color color = getColorLabel().getBackground();
		if (enabled) {
			relTypeGroup.setCompositeColor(color);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, relTypeGroup.getGroupName(), color);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, relTypeGroup.getGroupName(), relTypeGroup.getCompositeStyle());

			// TODO get composite threshold working
			int threshold = getHighLevelSlider().getValue();
			Collection arcs = dataDisplayBridge.getCompositeArcsManager().getCompositeArcs();
			for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
				ShrimpCompositeArc arc = (ShrimpCompositeArc) iterator.next();
				if (arc.isAtHighLevel()) {
					if (arc.getArcCount() < threshold) {
						arc.setVisible(false);
					} else {
						arc.updateVisibility();
					}
				}
			}
		}

		groupPanel.setCompositeArcsEnabled(enabled);
		groupPanel.setCompositeArcsColor(color);
		dataBean.setFiringEvents(firingEvents);
	}

}