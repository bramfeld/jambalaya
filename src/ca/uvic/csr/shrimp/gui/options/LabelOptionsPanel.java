/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.util.JIntegerComboBoxEditor;
import ca.uvic.csr.shrimp.util.JIntegerTextField;

/**
 * A dialog box for changing label options.
 */
public class LabelOptionsPanel extends JPanel implements ShrimpOptions, ActionListener, ItemListener {

	private static final String[] DEFAULT_FONT_SIZES = new String[] { "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30" };
	private static final String[] FONT_STYLES = new String[] { "PLAIN", "BOLD", "ITALIC", "BOLD & ITALIC" };

	private DisplayBean displayBean;
	private SelectorBean selectorBean;
	private String currentMode;

	// GUI widgets
	private JRadioButton onNodeRadio;
	private JRadioButton fitToNodeRadio;
	private JRadioButton wrapToNodeRadio;
	private JRadioButton aboveNodeLevelRadio;
	private JRadioButton aboveNodeFixedRadio;
	private JCheckBox switchLabellingCheckBox;
	private JIntegerTextField switchText;
	private JIntegerTextField levelsText;
	private JCheckBox fadeOutCheckBox;
	private JCheckBox opaqueCheckBox;
	private JComboBox fontNameCombo;
	private JComboBox fontSizeCombo;
	private JComboBox fontStyleCombo;
	private JLabel sampleLabel;
	private JLabel showAtMostLabel;
	private JLabel levelsLabel;

	public LabelOptionsPanel(DisplayBean db, SelectorBean selectorBean) {
		displayBean = db;
		this.selectorBean = selectorBean;

		initialize();
		loadInitialValues();
	}

	public void actionPerformed(ActionEvent e) {
		// radio button action
		currentMode = e.getActionCommand();
		updateLevelOptions();
	}

	public void itemStateChanged(ItemEvent e) {
		// font changed
		if (e.getStateChange() == ItemEvent.SELECTED) {
			updateFont();
		}
	}

	private void loadInitialValues() {
		currentMode = displayBean.getDefaultLabelMode();
		if (DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE.equals(currentMode)) {
			onNodeRadio.setSelected(true);
		} else if (DisplayConstants.LABEL_MODE_FIT_TO_NODE.equals(currentMode)) {
			// @tag Shrimp.fitToNodeLabelling
			fitToNodeRadio.setSelected(true);
		} else if (DisplayConstants.LABEL_MODE_WRAP_TO_NODE.equals(currentMode)) {
			wrapToNodeRadio.setSelected(true);
		} else if (DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL.equals(currentMode)) {
			aboveNodeLevelRadio.setSelected(true);
		} else if (DisplayConstants.LABEL_MODE_FIXED.equals(currentMode)) {
			aboveNodeFixedRadio.setSelected(true);
		}
		updateLevelOptions();

		boolean switchLabelling = displayBean.getSwitchLabelling();
		switchLabellingCheckBox.setSelected(switchLabelling);
		switchText.setEnabled(switchLabelling);
		int switchAtNum = displayBean.getSwitchAtNum();
		switchText.setIntegerText(switchAtNum);

		fadeOutCheckBox.setSelected(displayBean.getLabelFadeOut());
		opaqueCheckBox.setSelected(displayBean.getLabelBackgroundOpaque());
		int chosenLevels = displayBean.getLabelLevels();
		levelsText.setIntegerText(chosenLevels);

		// choose current font
		Font currentFont = (Font) displayBean.getLabelFont();
		fontNameCombo.setSelectedItem(currentFont.getName());
		fontSizeCombo.setSelectedItem(String.valueOf(currentFont.getSize()));
		fontStyleCombo.setSelectedIndex(currentFont.getStyle());
		sampleLabel.setFont(currentFont);
	}

	private void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEtchedBorder());
		topPanel.add(new JLabel(" Label Mode:    "), BorderLayout.WEST);
		topPanel.add(createRadioButtonsPanel(), BorderLayout.CENTER);
		add(topPanel);

		add(createSwitchLabellingPanel());
		add(createLevelOptionsPanel());
		add(createFontPanel());
		add(Box.createVerticalGlue());
	}

	private JPanel createRadioButtonsPanel() {
		onNodeRadio = new JRadioButton(DisplayConstants.SCALE_BY_NODE_SIZE_LONG_NAME);
		fitToNodeRadio = new JRadioButton(DisplayConstants.FIT_TO_NODE_SIZE_LONG_NAME);
		wrapToNodeRadio = new JRadioButton(DisplayConstants.WRAP_TO_NODE_SIZE_LONG_NAME);
		aboveNodeLevelRadio = new JRadioButton(DisplayConstants.SCALE_BY_LEVEL_LONG_NAME);
		aboveNodeFixedRadio = new JRadioButton(DisplayConstants.FIXED_LONG_NAME);

		onNodeRadio.setActionCommand(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
		onNodeRadio.addActionListener(this);
		fitToNodeRadio.setActionCommand(DisplayConstants.LABEL_MODE_FIT_TO_NODE);
		fitToNodeRadio.addActionListener(this);
		wrapToNodeRadio.setActionCommand(DisplayConstants.LABEL_MODE_WRAP_TO_NODE);
		wrapToNodeRadio.addActionListener(this);
		aboveNodeLevelRadio.setActionCommand(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL);
		aboveNodeLevelRadio.addActionListener(this);
		aboveNodeFixedRadio.setActionCommand(DisplayConstants.LABEL_MODE_FIXED);
		aboveNodeFixedRadio.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(onNodeRadio);
		group.add(fitToNodeRadio);
		group.add(wrapToNodeRadio);
		group.add(aboveNodeLevelRadio);
		group.add(aboveNodeFixedRadio);

		JPanel pnl = new JPanel(new GridLayout(5, 1, 0, 2));
		pnl.add(onNodeRadio);
		pnl.add(fitToNodeRadio);
		pnl.add(wrapToNodeRadio);
		pnl.add(aboveNodeLevelRadio);
		pnl.add(aboveNodeFixedRadio);
		return pnl;
	}

	private JPanel createSwitchLabellingPanel() {
		switchLabellingCheckBox = new JCheckBox("Label on node if greater than: ");
		switchText = new JIntegerTextField(0);
		switchText.setPreferredSize(new Dimension(50, 20));

		switchLabellingCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchText.setEnabled(switchLabellingCheckBox.isSelected());
			}
		});

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl.setBorder(BorderFactory.createEtchedBorder());
		pnl.add(switchLabellingCheckBox);
		pnl.add(switchText);
		return pnl;
	}

	private JPanel createLevelOptionsPanel() {
		showAtMostLabel = new JLabel("Show at most");
		levelsText = new JIntegerTextField(2);
		levelsLabel = new JLabel(" level(s)");
		fadeOutCheckBox = new JCheckBox("Fade Out");
		opaqueCheckBox = new JCheckBox("Opaque Background");
		levelsText.setPreferredSize(new Dimension(30, 20));

		JPanel first = new JPanel(new FlowLayout(FlowLayout.LEFT));
		first.add(showAtMostLabel);
		first.add(levelsText);
		first.add(levelsLabel);
		JPanel second = new JPanel(new FlowLayout(FlowLayout.LEFT));
		second.add(fadeOutCheckBox);
		second.add(opaqueCheckBox);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnl.setBorder(BorderFactory.createEtchedBorder());
		pnl.add(first);
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(second);
		return pnl;
	}

	private JPanel createFontPanel() {
		JPanel panel = new JPanel();
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		Vector fontNames = new Vector();
		for (int i = 1; i < fonts.length; i++) {
			fontNames.addElement(fonts[i]);
		}

		fontNameCombo = new JComboBox(fontNames);
		fontSizeCombo = new JComboBox(DEFAULT_FONT_SIZES);
		fontSizeCombo.setEditable(true);
		fontSizeCombo.setEditor(new JIntegerComboBoxEditor(12));
		fontStyleCombo = new JComboBox(FONT_STYLES);

		JPanel choicePanel = new JPanel(new GridLayout(1, 3));
		choicePanel.add(fontNameCombo);
		choicePanel.add(fontSizeCombo);
		choicePanel.add(fontStyleCombo);

		JPanel samplePanel = new JPanel();
		sampleLabel = new JLabel("Sample Text");
		sampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sampleLabel.setVerticalAlignment(SwingConstants.CENTER);
		sampleLabel.setPreferredSize(new Dimension(200, 60));
		samplePanel.add(sampleLabel);
		samplePanel.setBackground(Color.white);

		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(choicePanel, BorderLayout.NORTH);
		panel.add(samplePanel, BorderLayout.CENTER);

		// add listener to each combo box
		fontNameCombo.addItemListener(this);
		fontSizeCombo.addItemListener(this);
		fontStyleCombo.addItemListener(this);
		fontSizeCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFont();
			}
		});

		return panel;
	}
	private void updateLevelOptions() {
		boolean enable = aboveNodeFixedRadio.isSelected() || aboveNodeLevelRadio.isSelected();
		fadeOutCheckBox.setEnabled(enable);
		opaqueCheckBox.setEnabled(enable);
		showAtMostLabel.setEnabled(enable);
		levelsText.setEnabled(enable);
		levelsLabel.setEnabled(enable);
	}

	private void updateFont() {
		String name = (String) fontNameCombo.getSelectedItem();
		int size = getFontSize();
		int style = fontStyleCombo.getSelectedIndex();
		Font font = new Font(name, style, size);
		sampleLabel.setFont(font);
	}

	private int getFontSize() {
		int fontSize = ((Font) displayBean.getLabelFont()).getSize();
		try {
			fontSize = Integer.parseInt((String) fontSizeCombo.getSelectedItem());
		} catch (NumberFormatException ignore) {
		}
		return fontSize;
	}

	private void applyChange() {
		Cursor oldCursor = getCursor();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (!currentMode.equals(displayBean.getDefaultLabelMode())) {
			displayBean.setDefaultLabelMode(currentMode);
			selectorBean.setSelected(DisplayConstants.LABEL_MODE, currentMode);
		}
		boolean switchLabelling = switchLabellingCheckBox.isSelected();
		int switchAtNum = switchText.getIntegerText();
		if ((switchLabelling != displayBean.getSwitchLabelling()) || (switchAtNum != displayBean.getSwitchAtNum())) {
			displayBean.setSwitchLabelling(switchLabelling);
			displayBean.setSwitchAtNum(switchAtNum);
		}
		if (levelsText.isEnabled() && (levelsText.getIntegerText() != displayBean.getLabelLevels())) {
			displayBean.setLabelLevels(levelsText.getIntegerText());
		}
		if (fadeOutCheckBox.isSelected() != displayBean.getLabelFadeOut()) {
			displayBean.setLabelFadeOut(fadeOutCheckBox.isSelected());
		}
		if (opaqueCheckBox.isSelected() != displayBean.getLabelBackgroundOpaque()) {
			displayBean.setLabelBackgroundOpaque(opaqueCheckBox.isSelected());
		}
		Font chosenFont = sampleLabel.getFont();
		if (!chosenFont.equals(displayBean.getLabelFont())) {
			displayBean.setLabelFont(chosenFont);
		}
		setCursor(oldCursor);
	}


	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#ok()
	 */
	public void ok() {
		applyChange();
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#cancel()
	 */
	public void cancel() {
	}

}