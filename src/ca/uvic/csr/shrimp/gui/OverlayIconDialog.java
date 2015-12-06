/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RoundedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.DefaultIconProvider;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.IconProvider;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.ImageFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;
import edu.umd.cs.piccolo.util.PPaintContext;


/**
 *
 *
 * @author Chris Callendar
 * @date 19-Oct-07
 */
public class OverlayIconDialog extends EscapeDialog {

	private static final int MAX_ICON_PADDING = 20;

	private SampleNode sampleNode;
	private DefaultIconProvider sampleProvider;

	private JTextField iconTextField;
	private JButton browseButton;
	private JButton removeIconButton;
	private JButton okButton;
	private JButton cancelButton;
	private JFileChooser fileChooser;
	private JPanel mainPanel;
	private JPanel nodePanel;
	private JComboBox scaleComboBox;
	private JComboBox renderOptionsComboBox;
	private JSpinner paddingSpinner;
	private List/*<JButton>*/ positionButtons;
	private boolean positionsEnabled;

	private boolean okPressed = false;


	public OverlayIconDialog(Component parent, ShrimpNode node, DisplayBean displayBean) {
		super(ShrimpUtils.getParentFrame(parent), "Choose An Overlay Icon", true);
		this.positionButtons = new ArrayList/*<JButton>*/(10);
		this.positionsEnabled = false;
		this.sampleNode = new SampleNode(node.getName(), displayBean);

		createGUI();

		setIconProvider(new DefaultIconProvider(node.getOverlayIconProvider()));
		getScaleComboBox().setSelectedIndex(sampleProvider.getScaleMode());
		getRenderOptionComboBox().setSelectedIndex(sampleProvider.getRenderOption());
		getPaddingSpinner().setValue(new Integer(sampleProvider.getIconPadding()));

		String filename = sampleProvider.getIconFilename().getFilename();
		getIconTextField().setText(filename);
		getRemoveIconButton().setEnabled(filename.length() > 0);

		pack();
		setPreferredSize(new Dimension(500, 600));
		ShrimpUtils.centerWindowOnParent(this, parent);
		setVisible(true);
	}

	public boolean isOKPressed() {
		return okPressed;
	}

	public IconProvider getIconProvider() {
		return sampleProvider;
	}

	public String getIconPath() {
		return getIconTextField().getText();
	}

	private void createGUI() {
		GradientPanel content = new GradientPanel(new BorderLayout());
		setContentPane(content);

		TransparentPanel middle = new TransparentPanel(new BorderLayout(5, 5));
		getContentPane().add(middle, BorderLayout.CENTER);
		middle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JPanel top = new JPanel(new BorderLayout(2, 2));
		top.add(new JLabel("Icon: "), BorderLayout.WEST);
		top.add(getIconTextField(), BorderLayout.CENTER);
		JPanel buttons = new JPanel(new GridLayout(1, 2, 1, 0));
		buttons.add(getBrowseButton());
		buttons.add(getRemoveIconButton());
		top.add(buttons, BorderLayout.EAST);
		top.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
														 BorderFactory.createEmptyBorder(2, 4, 4, 4)));
		middle.add(top, BorderLayout.NORTH);
		middle.add(getMainPanel(), BorderLayout.CENTER);

		JPanel south = new JPanel(new GridLayout(2, 1, 0, 0));

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
										BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		JLabel lbl = new JLabel("Scale Mode ");
		toolbar.add(lbl);
		JComboBox cb = getScaleComboBox();
		toolbar.add(cb);
		panel.add(toolbar);
		south.add(panel);

		panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
										BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		toolbar = new JToolBar(JToolBar.HORIZONTAL);
		toolbar.setFloatable(false);
		lbl = new JLabel("  Render on ");
		toolbar.add(lbl);
		toolbar.add(getRenderOptionComboBox());
		toolbar.addSeparator();
		lbl = new JLabel("  Icon padding ");
		lbl.setToolTipText("The padding around the edge of the icon");
		toolbar.add(lbl);
		toolbar.add(getPaddingSpinner());
		panel.add(toolbar);
		south.add(panel);
		middle.add(south, BorderLayout.SOUTH);

		buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttons.add(getOKButton());
		buttons.add(getCancelButton());
		getContentPane().add(buttons, BorderLayout.SOUTH);

		setDefaultButton(getBrowseButton());
	}

	// COMPONENTS METHODS

	private JTextField getIconTextField() {
		if (iconTextField == null) {
			iconTextField = new JTextField();
			iconTextField.setEditable(false);
			//iconTextField.setEnabled(false);
		}
		return iconTextField;
	}

	private JButton getBrowseButton() {
		if (browseButton == null) {
			DefaultShrimpAction action = new DefaultShrimpAction("...", "Browse...") {
				public void actionPerformed(ActionEvent e) {
					chooseIcon();
				}
			};
			browseButton = new JButton(action);
			browseButton.setPreferredSize(new Dimension(20, 20));
		}
		return browseButton;
	}

	private JButton getRemoveIconButton() {
		if (removeIconButton == null) {
			DefaultShrimpAction action = new DefaultShrimpAction(ResourceHandler.getIcon("icon_delete.gif"), "Remove Icon") {
				public void actionPerformed(ActionEvent e) {
					removeIcon();
				}
			};
			removeIconButton = new JButton(action);
			removeIconButton.setPreferredSize(new Dimension(20, 20));
			removeIconButton.setEnabled(false);
		}
		return removeIconButton;
	}

	private JButton getOKButton() {
		if (okButton == null) {
			okButton = new JButton(createOKAction());
		}
		return okButton;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton(createCancelAction());
		}
		return cancelButton;
	}

	private JComboBox getScaleComboBox() {
		if (scaleComboBox == null) {
			scaleComboBox = new JComboBox(IconProvider.SCALE_OPTIONS);
			scaleComboBox.setSelectedIndex(IconProvider.SCALE_NODE);
			scaleComboBox.setToolTipText("Choose how the icon is scaled");
			scaleComboBox.setOpaque(false);
			scaleComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int mode = scaleComboBox.getSelectedIndex();
					if (sampleProvider.getScaleMode() != mode) {
						DefaultIconProvider dip = new DefaultIconProvider(sampleProvider);
						dip.setScaleMode(mode);
						setIconProvider(dip);
					}
				}
			});
		}
		return scaleComboBox;
	}

	private JSpinner getPaddingSpinner() {
		if (paddingSpinner == null) {
			final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, MAX_ICON_PADDING, 1);
			paddingSpinner = new JSpinner(model);
			paddingSpinner.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int pad = model.getNumber().intValue();
					if (pad != sampleProvider.getIconPadding()) {
						DefaultIconProvider dip = new DefaultIconProvider(sampleProvider);
						dip.setIconPadding(pad);
						setIconProvider(dip);
					}
				}
			});
			paddingSpinner.setToolTipText("The padding around the edge of the icon");
		}
		return paddingSpinner;
	}

	private JComboBox getRenderOptionComboBox() {
		if (renderOptionsComboBox == null) {
			renderOptionsComboBox = new JComboBox(IconProvider.RENDER_OPTIONS);
			renderOptionsComboBox.setToolTipText("When should this icon be rendered?");
			renderOptionsComboBox.setSelectedIndex(IconProvider.RENDER_ALWAYS);
			renderOptionsComboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int index = renderOptionsComboBox.getSelectedIndex();
					if (index != sampleProvider.getRenderOption()) {
						DefaultIconProvider dip = new DefaultIconProvider(sampleProvider);
						dip.setRenderOption(index);
						setIconProvider(dip);
					}
				}
			});
		}
		return renderOptionsComboBox;
	}

	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new BorderLayout(4, 4));
			mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));

			JPanel top = new JPanel(new BorderLayout());
			top.add(createPositionButton("Top Left", 0, 0), BorderLayout.WEST);
			JPanel mid = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
			mid.add(new JLabel("   "));
			mid.add(createPositionButton("Top", 0.5, 0));
			mid.add(createPositionButton("Center", 0.5, 0.5));
			top.add(mid, BorderLayout.CENTER);
			top.add(createPositionButton("Top Right", 1, 0), BorderLayout.EAST);
			mainPanel.add(top, BorderLayout.NORTH);

			JPanel left = new JPanel();
			left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
			left.add(Box.createVerticalGlue());
			left.add(createPositionButton("Left", 0, 0.5));
			left.add(Box.createVerticalGlue());
			mainPanel.add(left, BorderLayout.WEST);

			JPanel middle = new JPanel(new BorderLayout());
			middle.setBackground(Color.white);
			middle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
					BorderFactory.createEmptyBorder(20, 20, 20, 20)));
			middle.add(getNodePanel(), BorderLayout.CENTER);
			mainPanel.add(middle, BorderLayout.CENTER);

			JPanel right = new JPanel();
			right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
			right.add(Box.createVerticalGlue());
			right.add(createPositionButton("Right", 1, 0.5));
			right.add(Box.createVerticalGlue());
			mainPanel.add(right, BorderLayout.EAST);

			JPanel bottom = new JPanel(new BorderLayout());
			bottom.add(createPositionButton("Bottom Left", 0, 1), BorderLayout.WEST);
			mid = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			mid.add(createPositionButton("Bottom", 0.5, 1));
			bottom.add(mid, BorderLayout.CENTER);
			bottom.add(createPositionButton("Bottom Right", 1, 1), BorderLayout.EAST);
			mainPanel.add(bottom, BorderLayout.SOUTH);

			// initially disable all positions
			setPositionsEnabled(false);

		}
		return mainPanel;
	}

	private JPanel getNodePanel() {
		if (nodePanel == null) {
			nodePanel = new TransparentPanel(null, true) {
				protected void paintComponent(Graphics g) {
					sampleNode.renderNode(new PPaintContext((Graphics2D)g));
				}
			};
			IconDragHandler listener = new IconDragHandler();
			nodePanel.addComponentListener(listener);
			nodePanel.addMouseListener(listener);
			nodePanel.addMouseMotionListener(listener);
			sampleNode.setBounds(new Rectangle(0, 0, 200, 200));
		}
		return nodePanel;
	}

	private JButton createPositionButton(String name, final double scaleX, final double scaleY) {
		JButton btn = new JButton(new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
				DefaultIconProvider sp = new DefaultIconProvider(sampleProvider);
				sp.setScaleValues(scaleX, scaleY);
				setIconProvider(sp);
			}
		});
		btn.setToolTipText("Position the icon at the " + name);
		// constrain the height
		btn.setMaximumSize(new Dimension(200, 20));
		positionButtons.add(btn);
		return btn;
	}

	private void setPositionsEnabled(boolean enabled) {
		this.positionsEnabled = enabled;
		for (Iterator iter = positionButtons.iterator(); iter.hasNext();) {
			JButton btn = (JButton) iter.next();
			btn.setEnabled(enabled);
		}
	}

	// ACTION HANDLERS

	protected void chooseIcon() {
		JFileChooser chooser = getFileChooser();
		int choice = chooser.showOpenDialog(getBrowseButton());
		if (choice == JFileChooser.APPROVE_OPTION) {
			String file = chooser.getSelectedFile().getAbsolutePath();
			getIconTextField().setText(file);
			try {
				ImageIcon icon = new ImageIcon(file);
				if ((icon == null) || (icon.getImage() == null)) {
					throw new NullPointerException("Unable to load image: " + file);
				}
				DefaultIconProvider sp = new DefaultIconProvider(sampleProvider);
				sp.setIconFilename(file, icon);
				setIconProvider(sp);
				getRemoveIconButton().setEnabled(true);
			} catch (Exception ex) {
				String msg = "Error message: " + ex.getMessage();
				JOptionPane.showMessageDialog(this, msg, "Error loading icon", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private JFileChooser getFileChooser() {
    	if (fileChooser == null) {
    		fileChooser = new JFileChooser();
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileFilter(new ImageFileFilter());
			fileChooser.setDialogTitle("Choose An Overlay Icon");

			String path = getIconTextField().getText();
			if (path.length() > 0) {
				File file = new File(path);
				if (file.exists()) {
					fileChooser.setSelectedFile(file);
				}
			}
    	}
    	return fileChooser;
	}

	protected void removeIcon() {
		getRemoveIconButton().setEnabled(false);
		DefaultIconProvider sp = new DefaultIconProvider(sampleProvider);
		sp.setIcon(null);
		setIconProvider(sp);
	}

	protected void setIconProvider(DefaultIconProvider provider) {
		this.sampleProvider = provider;
		sampleNode.setOverlayIconProvider(sampleProvider);
		getNodePanel().repaint();

		boolean enabled = (provider.getIcon() != null);
		if (enabled != positionsEnabled) {
			setPositionsEnabled(enabled);
		}
	}

	protected void okPressed() {
		okPressed = true;
		super.okPressed();
	}

	private class SampleNode extends PShrimpNode {

		public SampleNode(String name, DisplayBean displayBean) {
			super(new RoundedRectangleNodeShape(), DisplayConstants.LABEL_MODE_FIXED, null, displayBean, new NodeImage());
			setName(name);
			setLabelMode(DisplayConstants.LABEL_MODE_FIT_TO_NODE);
			setColor(new Color(245, 224, 171));
		}

		public void renderNode(PPaintContext paintContext) {
			super.renderNode(paintContext);
		}

		public boolean shouldRender(PPaintContext paintContext) {
			return true;
		}

		protected boolean shouldRenderOverlayIcon() {
			return true;	// always render
		}

		public boolean shouldRenderPlusIcon() {
			return false;
		}

		public boolean shouldRenderDocumentIcon() {
			return false;
		}

		public boolean isResizable() {
			return false;
		}

	}

	private class IconDragHandler extends ComponentAdapter implements MouseListener, MouseMotionListener {

		private final Cursor CURSOR_DEFAULT;
		private final Cursor CURSOR_HAND;

		private boolean isOver;
		private boolean canDrag;


		public IconDragHandler() {
			CURSOR_DEFAULT = nodePanel.getCursor();
			CURSOR_HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			this.isOver = false;
		}

		public void componentResized(ComponentEvent e) {
			sampleNode.setBounds(new Rectangle2D.Double(0, 0, nodePanel.getWidth(), nodePanel.getHeight()));
			nodePanel.repaint();
		}

		public void componentShown(ComponentEvent e) {
			componentResized(e);
		}

		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {
			if (isOver) {
				mouseout();
				canDrag = false;
			}
		}

		public void mousePressed(MouseEvent e) {
			if (isOver) {
				canDrag = true;
			}
		}

		public void mouseReleased(MouseEvent e) {
			canDrag = false;
		}

		public void mouseDragged(MouseEvent e) {
			if (canDrag) {
				Point p = e.getPoint();
				double w = nodePanel.getWidth();
				double h = nodePanel.getHeight();
				if ((w > 0) && (h > 0)) {
					double dx = p.x / w;
					double dy = p.y / h;
					sampleProvider.setScaleValues(dx, dy);
					nodePanel.repaint();
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (sampleProvider.getIcon() != null) {
				boolean over = isOverIcon(e.getPoint());
				if (!isOver && over) {
					mouseover();
				} else if (isOver && !over) {
					mouseout();
				}
			}
		}

		private void mouseout() {
			isOver = false;
			nodePanel.setCursor(CURSOR_DEFAULT);
			nodePanel.setToolTipText("");
		}

		private void mouseover() {
			isOver = true;
			nodePanel.setCursor(CURSOR_HAND);
			nodePanel.setToolTipText("Drag the icon anywhere on the node");
		}

		protected boolean isOverIcon(Point src) {
			Rectangle2D bounds = sampleNode.getBounds();
			Point2D p = sampleProvider.getIconPosition(bounds);
			Rectangle2D iconBounds = new Rectangle2D.Double(p.getX(), p.getY(),
					sampleProvider.getIconWidth(), sampleProvider.getIconHeight());
			return iconBounds.contains(src);
		}

	}


}
