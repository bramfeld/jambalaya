/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.event.RootArtifactsChangeListener;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.ChangeCPRelAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;


/**
 * This is the model panel that appears at the bottom of the ShrimpView.
 * It controls the Hierarchy, node label mode, arc labels, and the navigation (zoom) mode.
 *
 * @author Chris Callendar
 * @date 20-Apr-07
 */
public class ShrimpViewModePanel extends GradientPanel {

	private static final int MAX_LENGTH = 50;
	private static final String ELLIPSES = " ...";

	private ShrimpView shrimpView;
	private DisplayBean displayBean;
	private SelectorBean selectorBean;
	private ChangeCPRelAdapter changeCPRelAdapter;
	private CheckBoxAction magnifyModeAction;
	private CheckBoxAction fisheyeModeAction;
	private CheckBoxAction zoomModeAction;

	private JButton cprelsButton;
	private JComponent rootArtifactsComponent;	// JButton or JLabel

	public ShrimpViewModePanel(ShrimpView shrimpView, ChangeCPRelAdapter changeCPRelAdapter, CheckBoxAction magnifyModeAction,
			CheckBoxAction fisheyeModeAction, CheckBoxAction zoomModeAction) throws BeanNotFoundException {
		super(new BorderLayout());
		this.shrimpView = shrimpView;
		this.changeCPRelAdapter = changeCPRelAdapter;
		this.magnifyModeAction = magnifyModeAction;
		this.fisheyeModeAction = fisheyeModeAction;
		this.zoomModeAction = zoomModeAction;
		this.displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
		this.selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);

		initialize();
	}

	private void initialize() {
		setGradientColors(Color.white, new Color(195, 186, 170));
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.darkGray, 1),
				BorderFactory.createEmptyBorder(1, 1, 1, 2)));

		add(createMainPanel(), BorderLayout.CENTER);

		//put the label and navigation boxes on a separate panel
		JPanel pnlLabelAndNav = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
		pnlLabelAndNav.add(createLabelModePanel());
		pnlLabelAndNav.add(createArcLabelsPanel());
		pnlLabelAndNav.add(createNavModePanel());
		add(pnlLabelAndNav, BorderLayout.EAST);
	}

	private JPanel createMainPanel() {
		JPanel pnl = new TransparentPanel();
		ShrimpProject project = shrimpView.getProject();
		if (project instanceof StandAloneProject) {
			// can't change the root artifacts so no point in displaying them!
			pnl.setLayout(new BorderLayout());
			pnl.add(createCPRelsPanel(), BorderLayout.CENTER);
		} else {
			pnl.setLayout(new GridLayout(1, 2, 1, 0));
			pnl.add(createCPRelsPanel());
			Action changeRoots = project.getActionManager().getAction(ShrimpConstants.ACTION_NAME_CHANGE_ROOT_CLASSES);
			pnl.add(createRootArtifactsPanel(changeRoots));
		}
		return pnl;
	}

	private JPanel createCPRelsPanel() {
		JPanel pnlCprel = new JPanel(new BorderLayout());
		pnlCprel.setOpaque(false);
		cprelsButton = new JButton(changeCPRelAdapter);
		pnlCprel.add(cprelsButton, BorderLayout.CENTER);
		setCprelsButtonText();
		shrimpView.addShrimpViewListener(new ShrimpViewAdapter() {
			public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event){
				setCprelsButtonText();
			}
		});
		return pnlCprel;
	}

	private JPanel createRootArtifactsPanel(Action changeRoots) {
		JPanel pnl = new TransparentPanel(new GridLayout(1, 1, 0, 0));
		if (changeRoots == null) {
			rootArtifactsComponent = new JLabel("", SwingConstants.CENTER);
			rootArtifactsComponent.setOpaque(false);
			rootArtifactsComponent.setBorder(BorderFactory.createEtchedBorder());
			//pnl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		} else {
			rootArtifactsComponent = new JButton(changeRoots);
		}
		pnl.add(rootArtifactsComponent);
		updateRootArtifacts();
		shrimpView.addShrimpViewListener(new ShrimpViewAdapter() {
			public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event){
				updateRootArtifacts();
			}
		});
		addRootsArtifactsListener();
		return pnl;
	}

	private JPanel createLabelModePanel() {
		//add a combobox for choosing label mode
		Vector modes = new Vector(4);
		modes.add(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
		// @tag Shrimp.fitToNodeLabelling
		modes.add(DisplayConstants.LABEL_MODE_FIT_TO_NODE);
		modes.add(DisplayConstants.LABEL_MODE_WRAP_TO_NODE);
		modes.add(DisplayConstants.LABEL_MODE_FIXED);
		modes.add(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL);
		final JComboBox labelModeCombo = new JComboBox(modes);
		labelModeCombo.setOpaque(false);
		labelModeCombo.setSelectedItem(displayBean.getDefaultLabelMode());
		labelModeCombo.setEditable(false);
		setNodeLabelsToolTip(displayBean.getDefaultLabelMode());

		final ItemListener listener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
					setLabelMode((String)e.getItem());
			    }
			}
		};
		labelModeCombo.addItemListener(listener);

		selectorBean.addPropertyChangeListener(DisplayConstants.LABEL_MODE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				labelModeCombo.removeItemListener(listener);
				String labelMode = (String) evt.getNewValue();
				labelModeCombo.setSelectedItem(labelMode);
        		setNodeLabelsToolTip(labelMode);
        		labelModeCombo.addItemListener(listener);
			}
		});

		JPanel pnlLabelMode = new TransparentPanel(new BorderLayout());
		JLabel lbl = new JLabel("   Node Labels: ", JLabel.RIGHT);
		lbl.setOpaque(false);
		pnlLabelMode.add(lbl, BorderLayout.WEST);
		pnlLabelMode.add(labelModeCombo, BorderLayout.CENTER);
		return pnlLabelMode;
	}

	private JCheckBox createArcLabelsPanel() {
		//add a checkbox for arc labels
		final JCheckBox chkArcLabels = new JCheckBox("   Arc Labels: ");
		chkArcLabels.setHorizontalTextPosition(SwingConstants.LEFT);
		chkArcLabels.setOpaque(false);
		chkArcLabels.setSelected(displayBean.getShowArcLabels());
		chkArcLabels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayBean.setShowArcLabels(chkArcLabels.isSelected());
            }
        });
		return chkArcLabels;
	}

	private JPanel createNavModePanel() {
		// add a combobox for choosing navigation mode
		Vector modes = new Vector(3);
		modes.add(DisplayConstants.MAGNIFY);
		modes.add(DisplayConstants.ZOOM);
		modes.add(DisplayConstants.FISHEYE);
		final JComboBox navigateModeCombo = new JComboBox(modes);
		navigateModeCombo.setOpaque(false);
		navigateModeCombo.setSelectedItem(selectorBean.getSelected(DisplayConstants.ZOOM_MODE));
		navigateModeCombo.setEditable(false);

		final ItemListener listener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
			    	changeNavigationMode((String) e.getItem());
			    }
			}
		};
		navigateModeCombo.addItemListener(listener);

		selectorBean.addPropertyChangeListener(DisplayConstants.ZOOM_MODE, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				navigateModeCombo.removeItemListener(listener);
				navigateModeCombo.setSelectedItem(evt.getNewValue());
				navigateModeCombo.addItemListener(listener);
			}
		});

		JPanel pnlNavMode = new TransparentPanel(new BorderLayout());
		JLabel lbl = new JLabel("   Navigation: ", JLabel.RIGHT);
		lbl.setOpaque(false);
		pnlNavMode.add(lbl, BorderLayout.WEST);
		pnlNavMode.add(navigateModeCombo, BorderLayout.CENTER);
		return pnlNavMode;
	}

	private void changeNavigationMode(String navigateMode) {
		if (DisplayConstants.FISHEYE.equals(navigateMode)) {
			fisheyeModeAction.startAction();
		} else if (DisplayConstants.ZOOM.equals(navigateMode)) {
			zoomModeAction.startAction();
		} else if (DisplayConstants.MAGNIFY.equals(navigateMode)) {
			magnifyModeAction.startAction();
		}
	}

	private void setNodeLabelsToolTip(String labelMode) {
		if (labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE)) {
			setToolTipText(DisplayConstants.SCALE_BY_NODE_SIZE_LONG_NAME);
		// @tag Shrimp.fitToNodeLabelling
		} else if (labelMode.equals(DisplayConstants.LABEL_MODE_FIT_TO_NODE)){
			setToolTipText(DisplayConstants.FIT_TO_NODE_SIZE_LONG_NAME);
		} else if (labelMode.equals(DisplayConstants.LABEL_MODE_WRAP_TO_NODE)){
			setToolTipText(DisplayConstants.WRAP_TO_NODE_SIZE_LONG_NAME);
		} else if (labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)){
			setToolTipText(DisplayConstants.SCALE_BY_LEVEL_LONG_NAME);
		} else if (labelMode.equals(DisplayConstants.LABEL_MODE_FIXED)){
			setToolTipText(DisplayConstants.FIXED_LONG_NAME);
		}
	}

	/**
	 * Updates the cprels button text based on the cprels.
	 */
	private void setCprelsButtonText() {
        String text = "No Hierarchy";
        String tooltipText = text;
		String[] cprels = shrimpView.getCprels();
		if (cprels.length > 0) {
            text = "Hierarchy: " + CollectionUtils.arrayToString(cprels);
        	tooltipText = text;
            // truncate the button text if neccessary so the button doesn't get ridiculously long
            if (text.length() > MAX_LENGTH) {
                text = text.substring(0, MAX_LENGTH - ELLIPSES.length());
                text += ELLIPSES;
            }
			if (shrimpView.isInverted()) {
                text += " (inverted)";
                tooltipText += " (inverted)";
			}
		}
		cprelsButton.setText(text);
		cprelsButton.setToolTipText(tooltipText);
	}

	private void setLabelMode(String labelMode) {
		displayBean.setDefaultLabelMode(labelMode);
		setNodeLabelsToolTip(labelMode);
		// @tag Shrimp.LabelMode : update the SelectorBean otherwise combobox gets out of sync [author=ccallend;date=18oct05]
		selectorBean.setSelected(DisplayConstants.LABEL_MODE, displayBean.getDefaultLabelMode());
	}

	private void updateRootArtifacts() {
		Collection roots = Collections.EMPTY_SET;
		try {
			DataBean dataBean = (DataBean) shrimpView.getProject().getBean(ShrimpProject.DATA_BEAN);
			String[] cprels = displayBean.getCprels();
			// if no cprels then all artifacts are returned - so we use the default cprels in this case
			roots = dataBean.getRootArtifacts((cprels.length == 0 ? dataBean.getDefaultCprels() : cprels));
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}

		StringBuffer buffer = new StringBuffer();
		String first = (roots.size() == 1 ? "Root: " : "Roots: ");
		for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
			Artifact art = (Artifact) iter.next();
			buffer.append((buffer.length() == 0 ? first : ", "));
			buffer.append(art.getName());
		}

		String text = buffer.toString();
		rootArtifactsComponent.setToolTipText(text);
		rootArtifactsComponent.setVisible(text.length() > 0);

		if (rootArtifactsComponent instanceof JButton) {
            if (text.length() > MAX_LENGTH) {
                text = text.substring(0, MAX_LENGTH - ELLIPSES.length());
                text += ELLIPSES;
            }
			((JButton) rootArtifactsComponent).setText(text);
		} else if (rootArtifactsComponent instanceof JLabel) {
			((JLabel) rootArtifactsComponent).setText(text);
		}
	}


	private void addRootsArtifactsListener() {
		try {
			DataBean dataBean = (DataBean) shrimpView.getProject().getBean(ShrimpProject.DATA_BEAN);
			RootArtifactsChangeListener listener = new RootArtifactsChangeListener() {
				public void rootArtifactsChange(RootArtifactsChangeEvent race) {
					updateRootArtifacts();
				}
			};
			dataBean.addRootArtifactsChangeListener(listener);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

}
