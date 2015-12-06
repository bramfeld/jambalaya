/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalAttribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalAttribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.FilterBean.OrdinalAttributeFilter;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.util.GraphicsUtils;

/**
 * An Attribute Panel used to colour and filter nodes by their
 * associated attributes.
 *
 * As each of the nodes should have a value to each attribute, we suggest you set null to
 * nodes that you don't want them be highlighted or filtered through attribute panel. These nodes may include
 * the Root node or folder nodes, etc. depends on what domain you are working on.
 *
 * Attribute types: attributes are classified into two categories: the nominal attribute and the ordinal
 * attribute. For example, a nominal attribute could be of type of String (name, id, etc.), and a ordinal
 * attribute could have numerical value, or the value could be sort(Integer, Date, etc.). Also, they could
 * be any user-defined type as long as they maintain the basic character of a nominal or ordinal attribute.
 * User should analyze attributes in the domain, determine which could be nominal and which could be ordinal
 * attributes, and use the following methods to categorize them.
 *
 * In the attribute panel, visual metaphors and attribute types correspond to each other as in the following table:
 *
 * <pre>
 * |-------------------------------------------------------------------------|
 * |                          |  Nominal Attribute   |   Ordinal Attribute   |
 * |--------------------------|----------------------|-----------------------|
 * |       Tooltip            |         Yes          |         Yes           |
 * |--------------------------|----------------------|-----------------------|
 * | Color  | Distinct Colors |         Yes          |                       |
 * |        |-----------------|----------------------|-----------------------|
 * | Scheme | Intensity       |                      |         Yes           |
 * |--------|-----------------|----------------------|-----------------------|
 * |        | Checkbox        |         Yes          |                       |
 * | Filter |-----------------|----------------------|-----------------------|
 * |        | DoubleSlider    |                      |         Yes           |
 * |-------------------------------------------------------------------------|
 * </pre>
 *
 * @author Xiaomin Wu, Rob Lintern
 */
public class AttributePanel extends AbstractShrimpTool implements DoubleSliderListener {

	private static final String DEFAULT_TOOLTIP_VALUE = "(default)";
	private static final String DEFAULT_DISPLAY_TEXT_VALUE = "(none)";

	private JPanel gui;			// contains the mainPanel and possibly a panel at the bottom with a Close button
	private JPanel mainPanel;	// contains the tabbed pane
	protected JTabbedPane tabbedPane;
	private JPanel legendTab; // a panel holding all legend settings, child of tabbedPane.
	private JPanel filtersTab; // a panel holding all filter settings, child of tabbedPane.
	private JPanel pnlColors;
	private JPanel ordinalColorPanel; //a panel associated with ordinal color schemes, residing in legendTab.
	private JPanel nominalColorsPanel; //a panel associated with nominal color schemes, residing in legendTab.
	private JComboBox tooltipCombo;
	private JComboBox displayTextCombo;
	private JComboBox colorCombo;

	private DataBean dataBean; // dataBean providing data resource for this attribute panel
	private AttrToVisVarBean attrToVisVarBean;
	private FilterBean filterBean;

	private String[] allAttrNames = new String[0]; //all attibute names in this domain
	private String[] ordAttrNames = new String[0]; //Ordinal attribute names in this domain
	private String[] nomAttrNames = new String[0]; //nominal attribute names in this domain
	private String[] displayAttrNames = new String[0]; //display attribute names in this domain
	private Map ordAttrValuesMap; //the key is the ordinal attribute name, the value is a vector contains the corresponding values of the attribute in the domian.
	private Map nomAttrValuesMap; //the key is the ordinal attribute name, the value is a vector contains the corresponding values of the attribute in the domian.
	private Map nomValueToCheckBoxesMap;
	private Map displayAttrValuesMap; //the key is a display attribute name, the value is a vector that contains the corresponding values of the attribute in the domian.

	//variables for initializing double sliders.
	private DoubleSlider[] ordSlider;
	private JLabel[] ordSliderLabel;
	private Object[] leftValue;
	private Object[] rightValue;

    private int targetType;
    private String targetFilterType;

	/**
	 * Constructor for AttributePanel.
	 * @param targetType one of {@link DataBean#ARTIFACT_TYPE} or {@link DataBean#RELATIONSHIP_TYPE}.
	 */
	public AttributePanel(ShrimpProject project, int targetType) {
		super(targetType == DataBean.ARTIFACT_TYPE ? ShrimpProject.NODE_ATTRIBUTE_PANEL : ShrimpProject.ARC_ATTRIBUTE_PANEL, project);
		this.targetType = targetType;
        this.targetFilterType = (targetType == DataBean.ARTIFACT_TYPE ? FilterConstants.ARTIFACT_FILTER_TYPE : FilterConstants.RELATIONSHIP_FILTER_TYPE);
		createGUI();
	}

	private void createGUI() {
		gui = new JPanel();
		gui.setLayout(new BorderLayout());
		gui.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		mainPanel = new JPanel(new BorderLayout());
		gui.add(mainPanel, BorderLayout.CENTER);
		refresh();
	}

	/**
	 * Returns the userInterface of this ShrimpTool.
	 * @return Component
	 */
	public Component getGUI() {
		return gui;
	}

	/**
	 * Disposes this ShrimpTool.
	 */
	public void disposeTool() {
        // nothing yet
	}

	/**
	 * sets up the Attribute Panel
	 */
	private void setAttributePanelForCurrentProject(ShrimpView shrimpView) {
		mainPanel.removeAll();
		tabbedPane = new JTabbedPane();
		legendTab = new JPanel();
		filtersTab = new JPanel();

		if (dataBean != null) {
			List ordAttrNamesList = new ArrayList();
			List nomAttrNamesList = new ArrayList();
			List displayAttrNamesList = new ArrayList();
			nomValueToCheckBoxesMap = new HashMap();
			String[] cprels = new String[0];
			boolean inverted = false;
			if (shrimpView != null) {
				cprels = shrimpView.getCprels();
				inverted = shrimpView.isInverted();
			}
			ordAttrValuesMap = new HashMap();
			nomAttrValuesMap = new HashMap();
			displayAttrValuesMap = new HashMap();

			Map ordMap = dataBean.getOrdinalAttrValues(cprels, true, inverted, targetType);
			// check for attributes with no values
			for (Iterator iter = ordMap.keySet().iterator(); iter.hasNext();) {
				String ordName = (String) iter.next();
				Collection ordValues = (Collection) ordMap.get(ordName);
				if (!ordValues.isEmpty()) {
					ordAttrValuesMap.put(ordName, ordValues);
					if (!ordAttrNamesList.contains(ordName)) {
						ordAttrNamesList.add(ordName);
					}
				}
			}
			Map nomMap = dataBean.getNominalAttrValues(cprels, true, inverted, targetType);
			// check for attributes with no values
			for (Iterator iter = nomMap.keySet().iterator(); iter.hasNext();) {
				String nomName = (String) iter.next();
				Collection nomValues = (Collection) nomMap.get(nomName);
				if (!nomValues.isEmpty()) {
					nomAttrValuesMap.put(nomName, nomValues);
					if (!nomAttrNamesList.contains(nomName)) {
						nomAttrNamesList.add(nomName);
					}
				}
			}

			// Only show this if we are rendering a node
			if (toolName.equals(ShrimpProject.NODE_ATTRIBUTE_PANEL)) {
				Map displayMap = dataBean.getNodeDisplayAttrValues(true);
				// check for attributes with no values
				for (Iterator iter = displayMap.keySet().iterator(); iter.hasNext();) {
					String displayName = (String) iter.next();
					Collection displayValues = (Collection) displayMap.get(displayName);
					if (!displayValues.isEmpty()) {
						displayAttrValuesMap.put(displayName, displayValues);
						if (!displayAttrNamesList.contains(displayName)) {
							displayAttrNamesList.add(displayName);
						}
					}
				}
				displayAttrNames = new String [displayAttrNamesList.size()];
				displayAttrNamesList.toArray(displayAttrNames);
			}

			Collections.sort(ordAttrNamesList, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(nomAttrNamesList, String.CASE_INSENSITIVE_ORDER);
			Collections.sort(displayAttrNamesList, String.CASE_INSENSITIVE_ORDER);
			ordAttrNames = new String[ordAttrNamesList.size()];
			ordAttrNamesList.toArray(ordAttrNames);
			nomAttrNames = new String[nomAttrNamesList.size()];
			nomAttrNamesList.toArray(nomAttrNames);
			List allAttrNamesList = new ArrayList (nomAttrNames.length + ordAttrNames.length);
			allAttrNamesList.addAll(ordAttrNamesList);
			allAttrNamesList.addAll(nomAttrNamesList);
			Collections.sort(allAttrNamesList, String.CASE_INSENSITIVE_ORDER);
			allAttrNames = new String[allAttrNamesList.size()];
			allAttrNamesList.toArray(allAttrNames);
		}

		tabbedPane.removeAll();
		initLegendTab();
		tabbedPane.addTab("Legend", new JScrollPane(legendTab));
		initFiltersTab();
		tabbedPane.addTab("Filters", new JScrollPane(filtersTab));

		mainPanel.add(tabbedPane, BorderLayout.CENTER);
		mainPanel.revalidate();

		//prime the color selection
		if (attrToVisVarBean != null) {
			Attribute colorAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_COLOR);
			if (colorAttr != null) {
				if (colorCombo.getSelectedItem().equals(colorAttr.getName())) {
					colorComboboxChanged(colorAttr.getName());
				} else {
					colorCombo.setSelectedItem(colorAttr.getName());
				}
			} else {
				colorCombo.setSelectedIndex(-1);
			}

			//prime the tooltip selection
			Attribute tooltipAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_TOOLTIP_TEXT);
			if (tooltipAttr != null) {
				if (tooltipCombo.getSelectedItem().equals(tooltipAttr.getName())) {
					textComboboxChanged(tooltipAttr.getName(),
							VisVarConstants.VIS_VAR_TOOLTIP_TEXT,
							DEFAULT_TOOLTIP_VALUE);
				} else {
					tooltipCombo.setSelectedItem(tooltipAttr.getName());
				}
			} else {
				tooltipCombo.setSelectedItem(DEFAULT_TOOLTIP_VALUE);
			}

			// Only show this if we are rendering a node
			if (toolName.equals(ShrimpProject.NODE_ATTRIBUTE_PANEL)) {
				String displayTextAttributeName = getDisplayTextAttribute();
				if (displayTextCombo != null) {
					if (displayTextAttributeName != null) {
						if (displayTextCombo.getSelectedItem().equals(displayTextAttributeName)) {
							displayTextComboboxChanged(displayTextAttributeName,
									DEFAULT_DISPLAY_TEXT_VALUE);
						} else {
							displayTextCombo.setSelectedItem(displayTextAttributeName);
						}
					} else {
						displayTextCombo.setSelectedItem(DEFAULT_DISPLAY_TEXT_VALUE);
					}
				}
			}
		}
	}

	/**
	 * Saves the node display text choice in the SHriMP properties file
	 * TODO - save in project properties
	 * @param choice
	 */
	private void saveDisplayTextAttribute(String choice) {
		Properties properties = ApplicationAccessor.getProperties();
		properties.setProperty(ShrimpProject.PROPERTY_KEY_NODE_DISPLAY_TEXT_ATTRIBUTE_TYPE, choice);
	}

	/**
	 * Removes the current node display text choice from the SHriMP properties file
	 * TODO - remove from project properties
	 */
	private void clearDisplayTextAttribute() {
		Properties properties = ApplicationAccessor.getProperties();
		properties.remove(ShrimpProject.PROPERTY_KEY_NODE_DISPLAY_TEXT_ATTRIBUTE_TYPE);
	}

	/**
	 * Retrieves the node display text choice from the SHriMP properties file
	 * TODO - get from project properties
	 * @return display text attribute
	 */
	private String getDisplayTextAttribute() {
		Properties properties = ApplicationAccessor.getProperties();
		return properties.getProperty(ShrimpProject.PROPERTY_KEY_NODE_DISPLAY_TEXT_ATTRIBUTE_TYPE);
	}

	public void initLegendTab() {
		legendTab.removeAll();
		legendTab.setLayout(new BoxLayout (legendTab, BoxLayout.Y_AXIS));
		legendTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (project == null) {
			return;
		}

		final Dimension labelDim = new Dimension(80, 20);

		// "Tooltip" combobox
		JLabel lblToolTip = new JLabel("ToolTip: ", JLabel.RIGHT);
		lblToolTip.setPreferredSize(labelDim);
		tooltipCombo = new JComboBox(allAttrNames);
		tooltipCombo.addItem(DEFAULT_TOOLTIP_VALUE);
		//tooltipCombo.setPreferredSize(new Dimension(150, 25));
		tooltipCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String attrName = (String) tooltipCombo.getSelectedItem();
					textComboboxChanged (attrName, VisVarConstants.VIS_VAR_TOOLTIP_TEXT,
							DEFAULT_TOOLTIP_VALUE);
				}
			}
		});

		JPanel pnlToolTipCombo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlToolTipCombo.add(lblToolTip);
		pnlToolTipCombo.add(tooltipCombo);

		// "Display Text" combobox
		JPanel pnlDisplayTextCombo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (displayAttrNames.length > 0) {
			JLabel lblDisplayText = new JLabel("Display Text: ", JLabel.RIGHT);
			lblDisplayText.setPreferredSize(labelDim);
			displayTextCombo = new JComboBox(displayAttrNames);
			displayTextCombo.addItem(DEFAULT_DISPLAY_TEXT_VALUE);
			displayTextCombo.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						displayTextComboboxChanged (
								(String)displayTextCombo.getSelectedItem(),
								DEFAULT_DISPLAY_TEXT_VALUE);

					}
				}
			});
			pnlDisplayTextCombo.add(lblDisplayText);
			pnlDisplayTextCombo.add(displayTextCombo);
		}

		// "Color" combobox
		JLabel lblColor = new JLabel("Color: ", JLabel.RIGHT);
		lblColor.setPreferredSize(labelDim);
		colorCombo = new JComboBox(allAttrNames);
		colorCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String attrName = (String) colorCombo.getSelectedItem();
					colorComboboxChanged(attrName);
				}
			}
		});

		ordinalColorPanel = new JPanel();
		nominalColorsPanel = new JPanel();

		JPanel pnlColorCombo = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlColorCombo.add(lblColor);
		pnlColorCombo.add(colorCombo);

		JPanel pnlCombos = new JPanel();
		pnlCombos.setLayout(new BoxLayout(pnlCombos, BoxLayout.Y_AXIS));
		pnlCombos.add(pnlToolTipCombo);
		if (displayAttrNames.length > 0) {
			pnlCombos.add(pnlDisplayTextCombo);
		}
		pnlCombos.add(pnlColorCombo);

		pnlColors = new JPanel();
		legendTab.add(pnlCombos);
		legendTab.add(pnlColors);
		legendTab.add(Box.createVerticalGlue());
	}

	/**
	 * Handle a user change in the node display text attribute selection
	 * @param attrName
	 * @param defaultTextValue
	 */
	private void displayTextComboboxChanged (final String attrName,
			String defaultTextValue) {
		if (attrName.equals(defaultTextValue)) {
			clearDisplayTextAttribute();
			return;
		}
		else {
			saveDisplayTextAttribute(attrName);
		}
	}

	/**
	 * Handle a change in e.g., tool tip text
	 * @param attrName
	 * @param visVarAttr
	 * @param defaultTextValue
	 */
	private void textComboboxChanged (final String attrName,
			String visVarAttr,  String defaultTextValue) {
		if (attrName.equals(defaultTextValue)) {
			VisualVariable visVar = attrToVisVarBean.getVisVar(visVarAttr);
			if (visVar != null) {
				attrToVisVarBean.removeVisVar(visVar);
			}
			return;
		}

		SortedSet attrValueSet = new TreeSet();
		if (isOrdinal(attrName)) {
			attrValueSet = (SortedSet) ordAttrValuesMap.get(attrName);
		} else if (isNominal(attrName)) {
			attrValueSet = (SortedSet) nomAttrValuesMap.get(attrName);
		}
		else { // assume this is a special class of display attribute
			attrValueSet = (SortedSet) displayAttrValuesMap.get(attrName);
		}
		Attribute attr = attrToVisVarBean.getAttr(attrName);
		if (attr == null) {
			if (isOrdinal(attrName)) {
				attr = new OrdinalAttribute (attrToVisVarBean, attrName, attrValueSet.first().getClass(), attrValueSet.first(), attrValueSet.last());
			} else if (isNominal(attrName)) {
				attr = new NominalAttribute (attrToVisVarBean, attrName, attrValueSet.first().getClass());
			}
			else { // assume this is a special class of display attribute, which happen to be all nominal
				attr = new NominalAttribute (attrToVisVarBean, attrName, attrValueSet.first().getClass());
			}
			attrToVisVarBean.addAttr(attr);
		}

		VisualVariable visVar = attrToVisVarBean.getVisVar(visVarAttr);
		if (visVar == null) {
			visVar = new NominalVisualVariable (attrToVisVarBean, visVarAttr) {
				public Object getVisVarValue(Attribute attr, Object attrValue) {
					return attrValue.toString(); //????
				}
				public Object getVisVarValueFromString(String s) {
					return s;
				}
				public String getStringFromVisVarValue(Object visVarValue) {
					return visVarValue.toString();
				}
				protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
					return null;
				}

			};
			attrToVisVarBean.addVisVar(visVar);
		}
		attrToVisVarBean.mapAttrToVisVar(attrName, visVarAttr);
		attrToVisVarBean.setNominalVisualVariableValue(attrName, visVarAttr, attr, attrToVisVarBean.getVisVar(visVarAttr));
	}

	private void colorComboboxChanged(final String attrName) {
		if (nominalColorsPanel != null) {
			nominalColorsPanel.removeAll();
			pnlColors.remove(nominalColorsPanel);
			legendTab.repaint();
			legendTab.updateUI();
		}
		if (ordinalColorPanel != null) {
			ordinalColorPanel.removeAll();
			pnlColors.remove(ordinalColorPanel);
			legendTab.repaint();
			legendTab.updateUI();
		}
		if (isOrdinal(attrName)) {
			applyOrdinalColors(attrName);
		} else if (isNominal(attrName)) {
			applyNominalColors(attrName);
		}
	}

	private boolean isOrdinal (String attrName) {
		boolean isOrdinal = false;
		for (int i = 0; i < ordAttrNames.length && !isOrdinal; i++) {
			String tmpAttrName = ordAttrNames[i];
			isOrdinal = attrName.equals(tmpAttrName);
		}
		return isOrdinal;
	}

	private boolean isNominal (String attrName) {
		boolean isNominal = false;
		for (int i = 0; i < nomAttrNames.length && !isNominal; i++) {
			String tmpAttrName = nomAttrNames[i];
			isNominal = attrName.equals(tmpAttrName);
		}
		return isNominal;
	}


	private void applyNominalColors(final String attrName) {
		// it's a nominal attribute, so initiate a nominal color panel
		nominalColorsPanel.setLayout(new GridLayout(0, 1));
		nominalColorsPanel.setBorder(BorderFactory.createTitledBorder("Color Schemes "));

		SortedSet nomValues = (SortedSet) nomAttrValuesMap.get(attrName);

		NominalAttribute attr = (NominalAttribute) attrToVisVarBean.getAttr(attrName);
		if (attr == null) {
			attr = new NominalAttribute (attrToVisVarBean, attrName, nomValues.first().getClass());
			attrToVisVarBean.addAttr(attr);
		}

		VisualVariable visVar = attrToVisVarBean.getVisVar(VisVarConstants.VIS_VAR_NODE_COLOR);
		if (visVar == null || !(visVar instanceof NominalNodeColorVisualVariable)) {
			if (visVar != null) {
				attrToVisVarBean.removeVisVar(visVar);
			}
			visVar = new NominalNodeColorVisualVariable (attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
			attrToVisVarBean.addVisVar(visVar);
		}

		attrToVisVarBean.mapAttrToVisVar(attrName, VisVarConstants.VIS_VAR_NODE_COLOR);

		Object [] nomValueArray = nomValues.toArray();
		//JButton [] nominalButtons = new JButton[nomValueArray.length];
		for (int i = 0; i < nomValueArray.length; i++) {
			final Object attrValue = nomValueArray[i];
			final JButton nominalButton = new JButton(attrValue.toString());
			// get this color from the AttrToVisVarBean
			Color nominalColor = (Color) attrToVisVarBean.getVisVarValue(attrName, VisVarConstants.VIS_VAR_NODE_COLOR, attrValue);
			nominalButton.setBackground(nominalColor);
			nominalButton.setForeground(GraphicsUtils.getTextColor(nominalColor));
			// background color doesn't show up properly in Windows L&F under Windows XP
			if (nominalButton.getUI().getClass().getName().equals("com.sun.java.swing.plaf.windows.WindowsButtonUI")) {
				nominalButton.setContentAreaFilled(false);
				nominalButton.setOpaque(true);
			}
			nominalButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//String s1 = e.getActionCommand();
					nominalColorButtonPressed(attrName, attrValue, nominalButton);
				}
			});

			nominalColorsPanel.add(nominalButton);
		}
		pnlColors.add(nominalColorsPanel);
		legendTab.repaint();
		legendTab.updateUI();
	}

	private void applyOrdinalColors(final String attrName) {
		ordinalColorPanel.setBorder(BorderFactory.createTitledBorder("Color Schemes"));
		SortedSet ordinalValuesSet = (SortedSet) ordAttrValuesMap.get(attrName);

		OrdinalAttribute attr = (OrdinalAttribute) attrToVisVarBean.getAttr(attrName);
		if (attr == null) {
			attr = new OrdinalAttribute (attrToVisVarBean, attrName, ordinalValuesSet.first().getClass(), ordinalValuesSet.first(), ordinalValuesSet.last());
			attrToVisVarBean.addAttr(attr);
		}

		VisualVariable visVar = attrToVisVarBean.getVisVar(VisVarConstants.VIS_VAR_NODE_COLOR);
		if (visVar == null || !(visVar instanceof OrdinalColorVisualVariable)) {
			if (visVar != null) {
				attrToVisVarBean.removeVisVar(visVar);
			}
			visVar = new OrdinalColorVisualVariable (attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
			attrToVisVarBean.addVisVar(visVar);
		}

		attrToVisVarBean.mapAttrToVisVar(attrName, VisVarConstants.VIS_VAR_NODE_COLOR);

		Color maxOrdinalColor = (Color)((OrdinalVisualVariable)visVar).getMaxVisVarValue();
		Color minOrdinalColor = (Color)((OrdinalVisualVariable)visVar).getMinVisVarValue();

		final VisualVariable visVarFinal = visVar;
		final JPanel pnlOrdinalPreview = new JPanel() {
			public void paint(Graphics g) {
				if (g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;
					Point2D pt1 = new Point2D.Float(0,0);
					Point2D pt2 = new Point2D.Float(getWidth(), 0);
					Color maxOrdinalColor = (Color)((OrdinalVisualVariable)visVarFinal).getMaxVisVarValue();
					Color minOrdinalColor = (Color)((OrdinalVisualVariable)visVarFinal).getMinVisVarValue();
					GradientPaint paint = new GradientPaint(pt1, minOrdinalColor, pt2, maxOrdinalColor, false);
					g2.setPaint(paint);
					g2.getClipBounds();
					g2.fill(g2.getClipBounds());
				}
			}
		};
		pnlOrdinalPreview.setPreferredSize(new Dimension(100, 20));
		JButton btnMinOrdinalColor = new JButton("...");
		btnMinOrdinalColor.setBackground(minOrdinalColor);
		btnMinOrdinalColor.setForeground(GraphicsUtils.getTextColor(minOrdinalColor));
		// background color doesn't show up properly in Windows L&F under Windows XP
		if (btnMinOrdinalColor.getUI().getClass().getName().equals("com.sun.java.swing.plaf.windows.WindowsButtonUI")) {
		    btnMinOrdinalColor.setContentAreaFilled(false);
		    btnMinOrdinalColor.setOpaque(true);
		}
		btnMinOrdinalColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ordinalColorButtonPressed((JButton)e.getSource(), true);
				pnlOrdinalPreview.repaint();
			}
		});
		ordinalColorPanel.add(btnMinOrdinalColor);

		ordinalColorPanel.add(pnlOrdinalPreview);

		JButton btnMaxOrdinalColor = new JButton("...");
		btnMaxOrdinalColor.setBackground(maxOrdinalColor);
		btnMaxOrdinalColor.setForeground(GraphicsUtils.getTextColor(maxOrdinalColor));
		// background color doesn't show up properly in Windows L&F under Windows XP
		if (btnMaxOrdinalColor.getUI().getClass().getName().equals("com.sun.java.swing.plaf.windows.WindowsButtonUI")) {
		    btnMaxOrdinalColor.setContentAreaFilled(false);
		    btnMaxOrdinalColor.setOpaque(true);
		}
		btnMaxOrdinalColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ordinalColorButtonPressed((JButton)e.getSource(), false);
				pnlOrdinalPreview.repaint();
			}
		});
		ordinalColorPanel.add(btnMaxOrdinalColor);
		pnlColors.add(ordinalColorPanel);
		legendTab.repaint();
		legendTab.updateUI();
	}

	private void ordinalColorButtonPressed(JButton btnMinOrdinalColor, boolean isMin) {
		OrdinalVisualVariable colorVisVar = (OrdinalVisualVariable) attrToVisVarBean.getVisVar(VisVarConstants.VIS_VAR_NODE_COLOR);
		Color oldColor = (Color) (isMin ? colorVisVar.getMinVisVarValue() : colorVisVar.getMaxVisVarValue());
		Color newColor = JColorChooser.showDialog(AttributePanel.this.gui, "Choose Color", oldColor);
		if (newColor != null) {
			if (isMin) {
				colorVisVar.setMinVisVarValue(newColor);
			} else {
				colorVisVar.setMaxVisVarValue(newColor);
			}
			btnMinOrdinalColor.setBackground(newColor);
			btnMinOrdinalColor.setForeground(GraphicsUtils.getTextColor(newColor));
		}
	}

	private void nominalColorButtonPressed(String attrName, Object value, JButton nominalButton) {
		Color color = JColorChooser.showDialog(AttributePanel.this.gui, "Choose Color", nominalButton.getBackground());
		if (color != null) {
			nominalButton.setBackground(color);
			nominalButton.setForeground(GraphicsUtils.getTextColor(color));
			attrToVisVarBean.setNominalVisualVariableValue(attrName, VisVarConstants.VIS_VAR_NODE_COLOR, value, color);
		}
	}

	public void initFiltersTab() {
		filtersTab.removeAll();
		filtersTab.setLayout(new BoxLayout(filtersTab, BoxLayout.Y_AXIS));
		filtersTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		if (project == null) {
			return;
		}

		// nominal checkboxes
		for (int i = 0; i < nomAttrNames.length; i++) {
			final String nomAttrName = nomAttrNames[i];
			JPanel checkBoxPanel = new JPanel();
			checkBoxPanel.setLayout(new GridLayout(0, 1));
			checkBoxPanel.setBorder(BorderFactory.createTitledBorder(nomAttrNames[i]));
			SortedSet nomValues = (SortedSet) nomAttrValuesMap.get(nomAttrName);
			for (Iterator iter = nomValues.iterator(); iter.hasNext();) {
				final Object nomAttrValue = iter.next();
				JCheckBox nominalCheckBox = new JCheckBox(nomAttrValue.toString(), true);
				boolean filtered = false;
				if (filterBean != null) {
					filterBean.isNominalAttrValueFiltered(nomAttrName, nomAttrValue.getClass(), targetFilterType, nomAttrValue);
				}
				nominalCheckBox.setSelected(!filtered);
				nomValueToCheckBoxesMap.put(nomAttrValue, nominalCheckBox);

				nominalCheckBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						if (filterBean != null) {
							filterBean.addRemoveSingleNominalAttrValue(nomAttrName, nomAttrValue.getClass(), targetFilterType, nomAttrValue, e.getStateChange() != ItemEvent.SELECTED);
						}
					}
				});
				checkBoxPanel.add(nominalCheckBox);
			}
			filtersTab.add(checkBoxPanel);
		}

		//double sliders
		int size = ordAttrNames.length;
		Object[] attribute = new Object[size];
		ordSlider = new DoubleSlider[size];
		ordSliderLabel = new JLabel[size];
		JPanel[] ordSliderPanel = new JPanel[size];
		JPanel[] labelPanel = new JPanel[size];
		leftValue = new Object[size];
		rightValue = new Object[size];

        for (int i = 0; i < ordAttrNames.length; i++) {
            final String attributeName = ordAttrNames[i];
			attribute[i] = attributeName;
			Vector values = new Vector ((SortedSet) ordAttrValuesMap.get(attributeName));
			if (values.size() > 0) {
				Vector colors = new Vector (values.size());
				OrdinalVisualVariable visVar = new OrdinalColorVisualVariable (attrToVisVarBean, "tmpVisVar");
				OrdinalAttribute attr = new OrdinalAttribute (attrToVisVarBean, "tmpAttr", values.firstElement().getClass(), values.firstElement(), values.lastElement());
				for (Iterator iterator = values.iterator(); iterator.hasNext();) {
					Object value = iterator.next();
					Color color = (Color) visVar.getVisVarValue(attr, value);
					colors.add(color);
				}
				ordSlider[i] = new DoubleSlider(values, 1, colors);
				String sliderName = attributeName; // the name of the slider is the same of the name of the attribute
				ordSlider[i].setName(sliderName);
				leftValue[i] = values.firstElement();
				rightValue[i] = values.lastElement();
				ordSliderLabel[i] = new JLabel("[" + leftValue[i] + ", " + rightValue[i] + "]");
				ordSlider[i].addDoubleSliderListener(this);
				ordSliderPanel[i] = new JPanel();
				ordSliderPanel[i].setLayout(new BoxLayout(ordSliderPanel[i], BoxLayout.Y_AXIS));
				labelPanel[i] = new JPanel();
				labelPanel[i].add(ordSliderLabel[i]);
				ordSliderPanel[i].add(ordSlider[i]);
				ordSliderPanel[i].add(labelPanel[i]);
				ordSliderPanel[i].setBorder(BorderFactory.createTitledBorder(sliderName));
				ordSliderPanel[i].add(Box.createRigidArea(new Dimension(5, 0)));
				filtersTab.add(ordSliderPanel[i]);
			}
		}
	}

	private void filterByOrdinalAttrType(Class ordAttrType, String ordAttrName, Object minOfUnfiltered, Object maxOfUnfiltered) {
		if (filterBean != null) {
	        Vector attributeFilters = filterBean.getFiltersOfType(Filter.ORDINAL_ATTRIBUTE_FILTER,  targetFilterType);
			OrdinalAttributeFilter ordAttrFilter = null;
			for (Iterator iter = attributeFilters.iterator(); iter.hasNext(); ) {
				OrdinalAttributeFilter anAttrFilter = (OrdinalAttributeFilter) iter.next();
				if (anAttrFilter.getAttributeType().equals(ordAttrType) && anAttrFilter.getAttributeName().equals(ordAttrName)) {
					ordAttrFilter = anAttrFilter;
					break;
				}
			}
			try {
				filterBean.removeFilter(ordAttrFilter);
			} catch (FilterNotFoundException e) {
				// do nothing
			}
			ordAttrFilter = new OrdinalAttributeFilter(ordAttrName, ordAttrType, targetFilterType, minOfUnfiltered, maxOfUnfiltered);
			filterBean.addFilter(ordAttrFilter); // will cause the filterbean to throw events and display to update
		}
	}

	public void lowerBoundChanged(DoubleSliderEvent event) {
		DoubleSlider slider = event.getDoubleSlider();
		updateView(slider);
	}

	public void upperBoundChanged(DoubleSliderEvent event) {
		DoubleSlider slider = event.getDoubleSlider();
		updateView(slider);
	}

	public void rangeMoved(DoubleSliderEvent event) {
		DoubleSlider slider = event.getDoubleSlider();
		updateView(slider);
	}

	/**
	 * update the shrimp view, filter nodes outside of the range.
	 */
	private void updateView(DoubleSlider slider) {
		int left = slider.getLowerBound();
		int right = slider.getUpperBound();
		Vector dataset = slider.getDataset();

		//update the label text of the slider
		for (int i = 0; i < ordAttrValuesMap.size(); i++) {
			if (slider == ordSlider[i]) {
				leftValue[i] = dataset.elementAt(left);
				rightValue[i] = dataset.elementAt(right);
				ordSliderLabel[i].setText("[" + leftValue[i] + ", " + rightValue[i] + "]");
			}
		}

		Object minOfUnfiltered = dataset.elementAt(left);
		Object maxOfUnfiltered = dataset.elementAt(right);
		String attributeName = slider.getName();
		filterByOrdinalAttrType(minOfUnfiltered.getClass(), attributeName, minOfUnfiltered, maxOfUnfiltered);
	}

	public void refresh() {
	    ShrimpView sv = null;
	    dataBean = null;
	    attrToVisVarBean = null;
	    filterBean = null;
	    if (project != null) {
			try {
				dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				attrToVisVarBean = (AttrToVisVarBean) project.getBean (ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
				sv = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				filterBean = (FilterBean) sv.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			} catch (ShrimpToolNotFoundException e) {
			} catch (BeanNotFoundException e) {
			}
	    }
		setAttributePanelForCurrentProject(sv);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getActionManager()
	 */
	public ActionManager getActionManager() {
		return null;
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#clear()
     */
    public void clear() {
        // do nothing for now
    }

}
