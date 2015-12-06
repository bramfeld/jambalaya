/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

public class NodeTypePanel extends JPanel {

	private NodeFilterPalette palette;

	private String type;
	private Color color;
	private Color outerBorderColor;
	private String outerBorderStyle;
	private Color innerBorderColor;
	private String innerBorderStyle;
	private Icon icon;
	private NodeShape nodeShape;
	private String labelStyle;
	private int typeCount;
	private boolean showTypeCount;
	private NodeImage nodeImage;

	private JButton btnStyle;

	/**
	 * Creates a single filter panel for the given node type.
	 */
	public NodeTypePanel(NodeFilterPalette palette, String type, Color color,
			Color outerBorderColor,	String outerBorderStyle,
			Color innerBorderColor, String innerBorderStyle,
			NodeImage nodeImage, Icon icon, NodeShape nodeShape, String labelStyle,
			int typeCount, boolean showTypeCount, ShrimpTool viewTool) {
		this.palette = palette;
		this.type = type;
		this.color = color;
		this.outerBorderColor = outerBorderColor;
		this.outerBorderStyle = outerBorderStyle;
		this.innerBorderColor = innerBorderColor;
		this.innerBorderStyle = innerBorderStyle;
		this.nodeImage = nodeImage;
		this.icon = icon;
		this.nodeShape = nodeShape;
		this.labelStyle = labelStyle;
		this.typeCount = typeCount;
		this.showTypeCount = showTypeCount;
		create(viewTool);
	}

	private void create(ShrimpTool tool) {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		setBorder(BorderFactory.createEtchedBorder());

		try {
			// get the beans
			final DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			final FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			final AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) tool.getProject().getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);

			btnStyle = new JButton(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					showNodeStyleDialog(displayBean, attrToVisVarBean);
				}
			});
			btnStyle.add(createNodeThumbnail(color, nodeImage));
			btnStyle.setName(type);
			btnStyle.setPreferredSize(new Dimension(33, 33));
			btnStyle.setToolTipText("Click to change the node style and color");
			btnStyle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btnStyle.setMargin(new Insets(4, 4, 4, 4));

			c.gridx = 1;
			c.gridy = 0;
			c.anchor = GridBagConstraints.EAST;
			c.weightx = 0;
			c.insets = new Insets(2, 4, 2, 4);
			c.fill = GridBagConstraints.NONE;
			add(btnStyle, c);

			// add the check box
			JPanel chkBoxPanel = new JPanel (new BorderLayout());
			JCheckBox chkBox = new JCheckBox();
			JLabel chkBoxLbl = new JLabel(icon);
			chkBoxLbl.setHorizontalAlignment(JLabel.LEFT);
			chkBoxLbl.setText(type + (showTypeCount ? " (" + typeCount + ")" : ""));
			chkBox.setName(type);
			chkBoxPanel.add(chkBox, BorderLayout.WEST);
			chkBoxPanel.add(chkBoxLbl, BorderLayout.CENTER);
			c.weightx = 1.0;
			c.gridx = 2;
			c.insets = new Insets(2, 2, 2, 2);
			c.fill = GridBagConstraints.HORIZONTAL;
			add(chkBoxPanel, c);

			// add the box to the vector of boxes
			this.palette.typeBoxes.put(type, chkBox);

			// check to see if this filter is already turned on
			boolean shouldSelect = !filterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, type);
			chkBox.setSelected(shouldSelect);

			// add the listener for a change of event.
			chkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					// remove palette as a filter changed listener while changing filterbean
					filterBean.removeFilterChangedListener(palette);
					String type = ((JCheckBox) e.getItemSelectable()).getName();
					palette.filter(filterBean, type, e.getStateChange() != ItemEvent.SELECTED);
					filterBean.addFilterChangedListener(palette);
				}
			});
		} catch (BeanNotFoundException e) {
			//e.printStackTrace();
		}
	}

	private void showNodeStyleDialog(DisplayBean displayBean, AttrToVisVarBean attrToVisVarBean) {
		Frame parent = ApplicationAccessor.getParentFrame();
		Vector nodeShapes = displayBean.getNodeShapes();
		Vector labelStyles = displayBean.getLabelStyles();
		Vector borderStyles = displayBean.getBorderStyles();
		Color defaultNodeColor = (Color) attrToVisVarBean.getDefaultNominalVisualVariable(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, type, color);

		NodePresentationDialog npd = new NodePresentationDialog(parent, nodeShapes, nodeShape,
				labelStyles, labelStyle, color, defaultNodeColor, outerBorderColor, outerBorderStyle,
				innerBorderColor, innerBorderStyle, borderStyles, nodeImage, btnStyle);
		if (npd.accepted()) {
			color = npd.getColor();
			outerBorderColor = npd.getOuterBorderColor();
			outerBorderStyle = npd.getOuterBorderStyle();
			innerBorderColor = npd.getInnerBorderColor();
			innerBorderStyle = npd.getInnerBorderStyle();
			labelStyle = npd.getLabelStyle();
			nodeShape = npd.getNodeShape();

			btnStyle.removeAll();
			btnStyle.add(createNodeThumbnail(color, nodeImage));
			btnStyle.validate();

			//update attribute to vis var mapping
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, type, color);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR, type, outerBorderColor);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE, type, outerBorderStyle);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR, type, innerBorderColor);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE, type, innerBorderStyle);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_SHAPE, type, nodeShape);
			// @tag Shrimp.NodeLabelStyle : this is where the user changes the node label style
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_LABEL_STYLE, type, labelStyle);

			// @tag Shrimp.NodeImage
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_IMAGE, type, nodeImage);
		}
	}

	private JComponent createNodeThumbnail(Color color, NodeImage image) {
		JComponent thumbNail = nodeShape.getThumbnail(
			0, 0, 21, 21, color,
			nodeImage.isDrawOuterBorder() ? outerBorderColor : null,
			nodeImage.isDrawInnerBorder() ? innerBorderColor : null,
			nodeImage);
		return thumbNail;
	}


}