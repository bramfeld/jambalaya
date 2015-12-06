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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import ca.uvic.csr.shrimp.AttrToVisVarBean.LabelStyleVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NodeShapeVisualVariable;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ImageFileFilter;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Creates a dialog for choosing the color and style of a node.
 *
 * @author Chris Callendar
 */
public class NodePresentationDialog extends StylePresentationDialog {

	private static final NodeShape DEFAULT_SHAPE = NodeShapeVisualVariable.DEFAULT_NODE_SHAPE;
	private static final String DEFAULT_STYLE = LabelStyleVisualVariable.DEFAULT_LABEL_STYLE;

	private NodeShape nodeShape;
	private Vector nodeShapes;
	private String labelStyle;
	private Vector labelStyles;
	private Vector imageModes;
	private NodeImage nodeImage;
	private final Color defaultColor;
	private String outerBorderStyle;
	private String innerBorderStyle;
	private ColorLabel outerBorderLblColor;
	private ColorLabel innerBorderLblColor;

	private ShrimpAction loadAction;
	private ShrimpAction deleteAction;

	private JFileChooser chooser;
	private JComboBox cmbShapes;
	private JLabel lblImageName;
	private JCheckBox chkFillBackground;
	private JCheckBox chkDrawOuterBorder;
	private JComboBox cmbOuterBorderStyle;
	private JCheckBox chkDrawInnerBorder;
	private JComboBox cmbInnerBorderStyle;
	private JComboBox cmbImageSizing;
	private JComboBox cmbLabelStyle;

	/**
	 * Creates a NodePresentationDialog.
	 * @param owner The owner of this dialog.
	 * @param nodeShapes The available {@link NodeShape}s to choose from.
	 * @param nodeShape The current node shape.
	 * @param color The current node color.
	 * @param defaultColor the default node color
	 * @param nodeImage the image and settings
	 * @param relativeTo The component to position this dialog relative to.
	 */
	public NodePresentationDialog(Frame owner, Vector nodeShapes, NodeShape nodeShape, Vector labelStyles,
			String labelStyle, Color color, Color defaultColor, Color outerBorderColor,
			String outerBorderStyle, Color innerBorderColor, String innerBorderStyle,
			Vector borderStyles, NodeImage nodeImage, Component relativeTo) {
		super(owner, "Node Style", color);

		this.nodeShapes = filterShapes(nodeShapes);
		this.nodeShape = nodeShape;
		this.labelStyles = labelStyles;
		this.labelStyle = labelStyle;
		this.defaultColor = defaultColor;
		this.outerBorderStyle = outerBorderStyle;
		this.innerBorderStyle = innerBorderStyle;
		this.nodeImage = nodeImage;

		this.cmbOuterBorderStyle = new JComboBox(borderStyles);
		this.cmbOuterBorderStyle.setSelectedItem(outerBorderStyle);
		this.cmbInnerBorderStyle = new JComboBox(borderStyles);
		this.cmbInnerBorderStyle.setSelectedItem(innerBorderStyle);
		this.outerBorderLblColor = new ColorLabel(outerBorderColor);
		this.innerBorderLblColor = new ColorLabel(innerBorderColor);
		this.chkDrawOuterBorder = new JCheckBox("", nodeImage.isDrawOuterBorder());
		this.chkDrawInnerBorder = new JCheckBox("", nodeImage.isDrawInnerBorder());

		outerBorderLblColor.addPropertyChangeListener(this);
		innerBorderLblColor.addPropertyChangeListener(this);

		imageModes = new Vector(4);
		imageModes.add(NodeImage.NO_SCALING);
		imageModes.add(NodeImage.STRETCHED);
		imageModes.add(NodeImage.CENTERED);
		imageModes.add(NodeImage.TILED);

		createGUI(relativeTo);	// blocks
	}

	/**
	 * Removes any node shapes that aren't user selectable.
	 * @return a new Vector containing all the user selectable shapes.
	 */
	protected static Vector filterShapes(Vector nodeShapes) {
		Vector shapes = new Vector(nodeShapes.size());
		for (Iterator iter = nodeShapes.iterator(); iter.hasNext(); ) {
			NodeShape shape = (NodeShape) iter.next();
			if (shape.isUserSelectable()) {
				shapes.add(shape);
			}
		}
		return shapes;
	}

	private JFileChooser getFileChooser() {
		if (chooser == null) {
			chooser = new JFileChooser();
			chooser.setDialogTitle("Choose an image");
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileFilter(new ImageFileFilter());
			if (nodeImage.getImagePath().length() > 0) {
				chooser.setSelectedFile(new File(nodeImage.getImagePath()));
			}
		}
		return chooser;
	}

	protected void addRows() {
		createNodeShapePanel();
		createLabelStylesPanel();
		createImagePanel();
		createCheckBoxRows();
		createPreviewPanel();
		updateImageProperties();
		updateThumbnail();
	}

	/**
	 * Creates the components for choosing a node shape.
	 */
	private void createNodeShapePanel() {
		cmbShapes = new JComboBox(nodeShapes);
		cmbShapes.setSelectedItem(nodeShape);
		cmbShapes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					boolean hadCustomRendering = ((nodeShape != null) && nodeShape.hasCustomRendering());
					
					nodeShape = (NodeShape) e.getItem();
					updateThumbnail();
					updateImageProperties();
					
					// need to update the checkboxes too
					if (nodeShape.hasCustomRendering()) {
						chkDrawOuterBorder.setSelected(nodeShape.getCustomRendering().isDrawOuterBorder());
						chkDrawInnerBorder.setSelected(nodeShape.getCustomRendering().isDrawInnerBorder());
						chkFillBackground.setSelected(nodeShape.getCustomRendering().isFillBackground());
					} else if (hadCustomRendering) {
						// defaults
						chkDrawOuterBorder.setSelected(true);
						chkDrawInnerBorder.setSelected(false);
						chkFillBackground.setSelected(true);
					}
				}
			}
		});
		addRow("Node Shape", cmbShapes);
	}

	/**
	 * Creates the components for choosing the node label style.
	 */
	private void createLabelStylesPanel() {
		cmbLabelStyle = new JComboBox(labelStyles);
		cmbLabelStyle.setSelectedItem(labelStyle);
		cmbLabelStyle.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					labelStyle = (String) e.getItem();
				}
			}
		});
		addRow("Label Style", cmbLabelStyle);
	}

	/**
	 * Creates the panel with the image settings.
	 */
	private void createImagePanel() {
		String imagePath = nodeImage.getImagePath();
		File imageFile = new File(imagePath);

		final JPanel panel = new TransparentPanel(new BorderLayout());
		JToolBar toolbar = createImageToolBar(imagePath, imageFile);
		panel.add(toolbar, BorderLayout.CENTER);
		addRow("Image", panel);

		cmbImageSizing = new JComboBox(imageModes);
		cmbImageSizing.setSelectedItem(nodeImage.getImageSizing());
		cmbImageSizing.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nodeImage.setImageSizing((String) e.getItem());
				}
			}
		});
		addRow("Image Sizing", cmbImageSizing);

		loadAction = new DefaultShrimpAction(ResourceHandler.getIcon("icon_open.gif"), "Load an image for this node type") {
			public void actionPerformed(ActionEvent e) {
				openImage();
			}
		};
		JButton btn = toolbar.add(loadAction);
		btn.setPreferredSize(new Dimension(22, 20));

		deleteAction = new DefaultShrimpAction(ResourceHandler.getIcon("icon_delete.gif"), "Remove the image from this node type") {
			public void actionPerformed(ActionEvent e) {
				deleteImage();
			}
		};
		toolbar.addSeparator(new Dimension(1, 0));
		btn = toolbar.add(deleteAction);
		btn.setPreferredSize(new Dimension(22, 20));
	}

	private void deleteImage() {
		nodeImage.setImagePath("");
		lblImageName.setText("");
		lblImageName.setToolTipText("");
		updateImageProperties();
		updateThumbnail();
	}

	private void openImage() {
		JFileChooser fileChooser = getFileChooser();
		if (fileChooser.showOpenDialog(lblImageName) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String imagePath = file.getAbsolutePath();
			lblImageName.setText(file.getName());
			lblImageName.setToolTipText(imagePath);
			nodeImage.setImagePath(imagePath);
			updateImageProperties();
			updateThumbnail();

			// this is done to force the image to show up
			// it seems to take a few repaints before it appears
			new Thread(new Runnable() {
				public void run() {
					// this is done twice to ensure that the image does show up
					for (int i = 0; i < 2; i++) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {}
						pnlPreview.repaint();
					}
				}
			}).start();
		}
	}

	/**
	 * @param imagePath
	 * @param imageFile
	 */
	private JToolBar createImageToolBar(String imagePath, File imageFile) {
		JToolBar toolbar = new JToolBar();
		//toolbar.setOpaque(false);
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
		lblImageName = new JLabel(imageFile.exists() ? imageFile.getName() : " ");
		lblImageName.setToolTipText(imagePath);
		lblImageName.setOpaque(true);
		lblImageName.setBackground(Color.white);
		lblImageName.setPreferredSize(new Dimension(170, 16));
		lblImageName.setMinimumSize(new Dimension(170, 16));
		lblImageName.setMaximumSize(new Dimension(240, 24));
		lblImageName.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
				BorderFactory.createEmptyBorder(0, 2, 0, 2)));
		toolbar.add(lblImageName);
		toolbar.add(new JLabel(" "));
		return toolbar;
	}

	/**
	 * Create border and fill background checkbox rows
	 */
	private void createCheckBoxRows() {
		addBorderPanel("Draw Outer Border?", chkDrawOuterBorder,
				cmbOuterBorderStyle, outerBorderLblColor);
		addBorderPanel("Draw Inner Border?", chkDrawInnerBorder,
				cmbInnerBorderStyle, innerBorderLblColor);

		JPanel pnl = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		chkFillBackground = new JCheckBox("", nodeImage.isFillBackground());
		chkFillBackground.setOpaque(false);
		pnl.add(chkFillBackground);
		pnl.add(Box.createHorizontalStrut(8));
		pnl.add(createColorChooserLabel());

		addRow("Fill Background?", pnl);

		// Set up listener for all check box rows
		ItemListener checkboxListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				nodeImage.setFillBackground(chkFillBackground.isSelected());
				nodeImage.setDrawOuterBorder(chkDrawOuterBorder.isSelected());
				nodeImage.setDrawInnerBorder(chkDrawInnerBorder.isSelected());
				updateImageProperties();
				updateThumbnail();
			}
		};
		chkFillBackground.addItemListener(checkboxListener);
		chkDrawOuterBorder.addItemListener(checkboxListener);
		chkDrawInnerBorder.addItemListener(checkboxListener);

		// Set up listener for style combo boxes
		ItemListener styleListener = new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setOuterBorderStyle((String)cmbOuterBorderStyle.getSelectedItem());
				setInnerBorderStyle((String)cmbInnerBorderStyle.getSelectedItem());
				updateThumbnail();
			}
		};
		cmbOuterBorderStyle.addItemListener(styleListener);
		cmbInnerBorderStyle.addItemListener(styleListener);
	}

	/**
	 * Add a border panel to this dialog
	 * @param labelText
	 * @param cmbBorderStyle
	 * @param chkDrawBorder
	 */
	private void addBorderPanel(String labelText, JCheckBox chkDrawBorder,
			JComboBox cmbBorderStyle, ColorLabel borderLblColor) {
		JPanel pnl = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		chkDrawBorder.setOpaque(false);
		pnl.add(chkDrawBorder);
		pnl.add(Box.createHorizontalStrut(8));
		pnl.add(borderLblColor);
		pnl.add(Box.createHorizontalStrut(8));
		pnl.add(new JLabel("Border Style"));
		pnl.add(Box.createHorizontalStrut(8));
		pnl.add(cmbBorderStyle);
		addRow(labelText, pnl);
	}

	protected Color getOuterBorderColor() {
		return this.outerBorderLblColor.getColor();
	}

	protected Color getInnerBorderColor() {
		return this.innerBorderLblColor.getColor();
	}

	protected void setOuterBorderStyle(String style) {
		if (style != null) {
			this.outerBorderStyle = style;
			updateThumbnail();
		}
	}

	protected void setInnerBorderStyle(String style) {
		if (style != null) {
			this.innerBorderStyle = style;
			updateThumbnail();
		}
	}

	protected void updateImageProperties() {
		boolean canChange = !nodeShape.hasCustomRendering();
		cmbImageSizing.setEnabled(canChange && nodeImage.hasImage());
		loadAction.setEnabled(canChange);
		deleteAction.setEnabled(canChange && nodeImage.hasImage());
		chkDrawOuterBorder.setEnabled(canChange);
		chkDrawInnerBorder.setEnabled(canChange);
		cmbOuterBorderStyle.setEnabled(canChange && chkDrawOuterBorder.isSelected());
		cmbInnerBorderStyle.setEnabled(canChange && chkDrawInnerBorder.isSelected());
		chkFillBackground.setEnabled(canChange);
	}

	protected void setDefaults() {
		cmbShapes.setSelectedItem(DEFAULT_SHAPE);
		cmbLabelStyle.setSelectedItem(DEFAULT_STYLE);
		setColor(defaultColor);
		setOuterBorderColor(NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR);
		setOuterBorderStyle(NodeBorder.DEFAULT_BORDER_STYLE);
		setInnerBorderColor(NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR);
		setInnerBorderStyle(NodeBorder.DEFAULT_BORDER_STYLE);

		nodeImage.reset();
		lblImageName.setText("");
		lblImageName.setToolTipText("");
		cmbImageSizing.setSelectedItem(nodeImage.getImageSizing());
		chkFillBackground.setSelected(true);
		chkDrawOuterBorder.setSelected(true);
		chkDrawInnerBorder.setSelected(false);
		cmbOuterBorderStyle.setSelectedItem(NodeBorder.DEFAULT_BORDER_STYLE);
		cmbInnerBorderStyle.setSelectedItem(NodeBorder.DEFAULT_BORDER_STYLE);
		updateImageProperties();
		updateThumbnail();
	}

	private void setOuterBorderColor(Color c) {
		this.outerBorderLblColor.setColor(c);
	}

	private void setInnerBorderColor(Color c) {
		this.innerBorderLblColor.setColor(c);
	}

	protected void updateThumbnail() {
		pnlPreview.removeAll();
		JComponent thumbnail = nodeShape.getThumbnail(0, 0, 21, 21, getColor(),
			nodeImage.isDrawOuterBorder() ? getOuterBorderColor() : null,
			nodeImage.isDrawInnerBorder() ? getInnerBorderColor() : null,
			nodeImage); // always draw plain border - too small for dashes
		thumbnail.setOpaque(false);
		pnlPreview.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		pnlPreview.setPreferredSize(new Dimension(33, 33));
		pnlPreview.add(thumbnail, BorderLayout.CENTER);
		pnlPreview.validate();
		pnlPreview.repaint();
	}

	/**
	 * Returns the node shape selected.
	 * This is only used when node shapes are displayed.
	 */
	public NodeShape getNodeShape() {
		return nodeShape;
	}

	/**
	 * Returns the selected node label style.
	 * @return String label style
	 */
	public String getLabelStyle() {
		return labelStyle;
	}

	/**
	 * Returns the selected node outer border style.
	 * @return String border style
	 */
	public String getOuterBorderStyle() {
		return outerBorderStyle;
	}

	/**
	 * Returns the selected node inner border style.
	 * @return String border style
	 */
	public String getInnerBorderStyle() {
		return innerBorderStyle;
	}

	/**
	 * Returns the selected node border style.
	 * @return String border style
	 */
	public String getInnderBorderStyle() {
		return innerBorderStyle;
	}

	public NodeImage getNodeImage() {
		return nodeImage;
	}

}
