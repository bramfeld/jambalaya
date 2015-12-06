/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ChooseRelationshipType;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewListener.QuickViewEvent;
import ca.uvic.csr.shrimp.resource.IconFilename;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.BrowserLauncher;
import ca.uvic.csr.shrimp.util.CollapsiblePanel;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.JButtonList;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Displays a GUI which lets the user configure the quick views for the current project.
 * The user can create a new quick view (flat or nested), and can delete existing quick views.
 * Existing views can also be configured differently - for example to change the nesting hierarchy
 * or change the icon.
 *
 * @author Chris Callendar
 */
public class QuickViews extends AbstractShrimpTool implements ListSelectionListener {

	private static final int BUTTON_HEIGHT = 20;
	private static final Dimension LABEL_SIZE = new Dimension(80, 24);
	private static final Dimension COMBO_SIZE = new Dimension(140, 24);
	private static final Color BORDER_COLOR = new Color(127, 157, 185);
	private static final Border DETAILS_BORDER = BorderFactory.createEmptyBorder(4, 0, 0, 0);

	private static final String DEFAULT_LAYOUT = ExpressViewConfigurator.DEFAULT_LAYOUT_MODE;

	// @tag Shrimp.fitToNodeLabelling
	private static final String[] LABEL_MODES = {
		DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE,
		DisplayConstants.LABEL_MODE_FIT_TO_NODE,
		DisplayConstants.LABEL_MODE_WRAP_TO_NODE,
		DisplayConstants.LABEL_MODE_FIXED,
		DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL };
	public static final String DEFAULT_LABEL_MODE = ExpressViewConfigurator.DEFAULT_LABEL_MODE;

	private QuickViewManager manager;

	private boolean okPressed;
	private QuickViewAction selectedAction = null;
	private QuickViewActionListener listener = null;
	private CompositeArcsManager compositeManager = null;

	private JPanel gui;
	private CollapsiblePanel paneDetails;

	private JPanel pnlNodes;
	private JPanel pnlArcs;
	private JPanel pnlHierarchy;
	private JPanel pnlComposites;
	private JPanel pnlDetails;
	private JSplitPane splitNodesArcs;
	private JSplitPane splitCompositeArcs;
	private JSplitPane splMain;
	private JList lstQuickViews;
	private JButtonList lstNodes;
	private JButtonList lstArcs;
	private JButtonList lstHierarchy;
	private JButtonList lstCompositeNodes;
	private JButtonList lstDefaultGroups;
	private JButtonList lstCustomGroups;
	private JList lstDefaultCompositeArcs;
	private JButtonList lstCustomCompositeArcs;
	private DefaultListModel lstModel;
	private JToolBar toolbar;
	private JPanel pnlTopDetails;
	private JTextField txtName;
	private JComboBox cmbLayout;
	private JComboBox cmbLabelMode;
	private JComboBox cmbIcons;
	private JCheckBox chkDisplay;
	private JCheckBox chkInverted;
	private JCheckBox chkNoHierarchy;
	private JCheckBox chkPreviewComposites;

	private DefaultShrimpAction newAction;
	private DefaultShrimpAction deleteAction;
	private DefaultShrimpAction deleteAllAction;
	private DefaultShrimpAction defaultsAction;
	private DefaultShrimpAction runAction;
	private DefaultShrimpAction importAction;
	private DefaultShrimpAction exportAction;
	private DefaultShrimpAction helpAction;


	public QuickViews(ShrimpProject project, QuickViewManager manager) throws HeadlessException {
		super(ShrimpConstants.TOOL_QUICK_VIEWS, project);
		this.okPressed = false;
		this.listener = new QuickViewActionListener();
		this.manager = manager;

		initialize();
	}

	public void disposeTool() {
		if (manager != null) {
			manager.save();
			manager = null;
		}
		project = null;
		clear();
		compositeManager = null;
		selectedAction = null;
	}

	public void clear() {
		loadQuickViews();
	}

	public void setProject(ShrimpProject project) {
		if (project != null) {
			this.manager = project.getQuickViewManager();
		}
		super.setProject(project);
	}

	/**
	 * Returns the composite arcs manager which will be initialized the first time this method is called.
	 */
	public CompositeArcsManager getCompositeArcsManager() {
		if ((compositeManager == null) && (project != null)) {
			try {
				DisplayBean displayBean = (DisplayBean) project.getTool(ShrimpProject.SHRIMP_VIEW).getBean(ShrimpTool.DISPLAY_BEAN);
				DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
				compositeManager = dataDisplayBridge.getCompositeArcsManager();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			}
		}
		return compositeManager;
	}

	public void refresh() {
		selectedAction = null;

		// reset the composite arcs manager
		compositeManager = null;
		getCompositeArcsManager();

		initTypeLists();
		initLayouts();
		loadQuickViews();
	}

	public Component getGUI() {
		return gui;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			JList list = getQuickViewsList();
			selectedAction = (QuickViewAction) list.getSelectedValue();
			setDetailsForSelectedAction();
			updateActionEnablements();
		}
	}

	private void updateActionEnablements() {
		boolean enabled = (selectedAction != null);
		deleteAction.setEnabled(enabled);
		deleteAllAction.setEnabled(enabled);
		runAction.setEnabled(enabled);
		int count = (manager != null ? manager.getQuickViewCount() : 0);
		exportAction.setEnabled(count > 0);

		importAction.setEnabled(manager != null);
		newAction.setEnabled(manager != null);
		defaultsAction.setEnabled(manager != null);
	}

	private void setDetailsForSelectedAction() {
		updateActionEnablements();
		if (selectedAction != null) {
			ExpressViewConfigurator config = selectedAction.getConfigurator();
			String name = selectedAction.getActionName();
			txtName.setText(name);
			validateQuickViewName();
			getDetailsCollapsiblePanel().setTitle(name + " Details");
			getIconsComboBox().setSelectedItem(new IconFilename(selectedAction.getIconFilename(), selectedAction.getIcon()));
			getLayoutComboBox().setSelectedItem(config.getLayoutMode());
			getLabelModeComboBox().setSelectedItem(config.getLabelMode());
			getDisplayCheckBox().setSelected(selectedAction.isDisplay());
			setCheckedItemsInList(getNodesList(), config.getNodeTypesOfInterest());
			setCheckedItemsInList(getArcsList(), config.getArcTypesOfInterest());
			setCheckedCprels(getHierarchyList(), config.getCprels());
			setCheckedItemsInList(getCompositeNodesList(), config.getNodeTypesToOpen());

			Vector relTypeGroups = new Vector(0);
			CompositeArcsManager mgr = getCompositeArcsManager();
			if (mgr != null) {
				relTypeGroups = mgr.getRelTypeGroups();
				for (Iterator iter = config.getCompositeArcs().keySet().iterator(); iter.hasNext(); ) {
					String groupName = (String) iter.next();
					RelTypeGroup group = new RelTypeGroup(groupName);
					if (!relTypeGroups.contains(group)) {
						relTypeGroups.add(group);
					} else {
						System.out.println("Already contains rel type group: " + groupName);
					}
				}
			}
			getDefaultGroupsList().setListData(relTypeGroups);
			getDefaultGroupsList().getSelectionModel().clearSelection();
			loadCustomGroups(config);

			if (config.isInverted() != chkInverted.isSelected()) {
				chkInverted.doClick();		// triggers an action event
			}
			if (!selectedAction.isNested() != chkNoHierarchy.isSelected()) {
				chkNoHierarchy.doClick();	// triggers an action event
			}
		} else {
			txtName.setText("");
			getDisplayCheckBox().setSelected(true);
		}
	}

	private void loadCustomGroups(ExpressViewConfigurator config) {
		Vector customGroups = new Vector(config.getCompositeArcs().keySet());
		Collections.sort(customGroups);
		getCustomGroupsList().setListData(customGroups);
		if (customGroups.size() > 0) {
			getCustomGroupsList().setSelectedIndex(0);
		}
	}

	private void initTypeLists() {
		Vector nodes = (manager != null ? new Vector(manager.getAllNodeTypes()) : new Vector(0));
		Vector rels = (manager != null ? new Vector(manager.getAllArcTypes()) : new Vector(0));
		getNodesList().setListData(nodes);
		getArcsList().setListData(rels);
		getHierarchyList().setListData(rels);
		getCompositeNodesList().setListData(nodes);
	}

	private void loadQuickViews() {
		// reset the selected action
		selectedAction = null;

		// reset the model
		lstModel.clear();

		QuickViewAction[] quickViews = new QuickViewAction[0];
		if (manager != null) {
			quickViews = manager.getSortedQuickViews();
		}
		if (quickViews.length == 0) {
			//System.out.println("Loading default quick views");
			//manager.loadDefaults();
			//quickViews = manager.getSortedQuickViews();
		}
		for (int i = 0; i < quickViews.length; i++) {
			Action action = quickViews[i];
			lstModel.addElement(action);
		}
		if (lstModel.size() > 0) {
			getQuickViewsList().setSelectedIndex(0);
		} else {
			setDetailsForSelectedAction();
		}
	}

	/**
	 * Loads all the icons from the <code>resource/icons</code> directory that start with
	 * <code>icon_quick_view</code>.  If no icons are found then the icons combobox is disabled.
	 */
	private void loadIcons() {
		try {
			URL url;
			if (manager != null) {
				// for Creole
				url = manager.getIconsURL();
			} else {
				url = ResourceHandler.class.getResource("icons");
			}
			IconFilename[] icons = ResourceHandler.getIcons("icon_quick_view", url);
			getIconsComboBox().setModel(new DefaultComboBoxModel(icons));
		} catch (Exception e) {
			System.err.println("Warning - couldn't load icons: " + e.getMessage());
		}

		// @tag Shrimp(QuickViews(Icons)) : if no icons were loaded - disable the icon combobox?
		if (getIconsComboBox().getModel().getSize() == 0) {
			getIconsComboBox().setEnabled(false);
		}
		/*
		if (getIconsComboBox().getModel().getSize() == 0) {
			// load a default icon
			try {
				BufferedImage img = new BufferedImage(50, 52, BufferedImage.TYPE_BYTE_BINARY);
				Graphics g = img.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0, 0, 50, 52);
				ImageIcon icon = new ImageIcon(img);
				g.dispose();
				IconFilename iconFilename = new IconFilename("icon_quick_view_blank.gif", icon);
				getIconsComboBox().setModel(new DefaultComboBoxModel(new IconFilename[] { iconFilename }));
			} catch (Exception e) {
				System.err.println("Couldn't create a default blank icon: " + e.getMessage());
			}
		}
		*/
 	}

	/**
	 * Sort the layout names and add them to the layout combobox, then select the default layout
	 * or the first layout.
	 */
	private void initLayouts() {
		Vector layoutNames = new Vector();
		// load the layouts from the display bean
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
				Vector layouts = displayBean.getLayouts();
				layoutNames = new Vector(layouts.size());
				for (Iterator iter = layouts.iterator(); iter.hasNext(); ) {
					Layout layout = (Layout) iter.next();
					layoutNames.add(layout.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// add some default layouts
			layoutNames.add(LayoutConstants.LAYOUT_GRID_BY_TYPE);
			layoutNames.add(LayoutConstants.LAYOUT_RADIAL);
			layoutNames.add(LayoutConstants.LAYOUT_SPRING);
			layoutNames.add(LayoutConstants.LAYOUT_TREE_VERTICAL);
			layoutNames.add(LayoutConstants.LAYOUT_TREE_HORIZONTAL);
		}

		// don't allow users to choose the motion layout
		layoutNames.remove(LayoutConstants.LAYOUT_MOTION);

		if (layoutNames.size() > 0) {
			// sort names alphabetically
			Collections.sort(layoutNames, String.CASE_INSENSITIVE_ORDER);
			// add them to the combobox
			getLayoutComboBox().setModel(new DefaultComboBoxModel(layoutNames));

			if (layoutNames.contains(DEFAULT_LAYOUT)) {
				getLayoutComboBox().setSelectedItem(DEFAULT_LAYOUT);
			} else  {
				getLayoutComboBox().setSelectedIndex(0);
			}
		}
	}

	private void newQuickView() {
		NewQuickViewDialog dlg = new NewQuickViewDialog(getToolBar(), getDefaultQuickViewName());
		if (dlg.isOKPressed()) {
			// create and add a dummy action
			QuickViewAction action;
			if (dlg.isNested()) {
				action = manager.createNestedView(dlg.getQuickViewName(), "icon_quick_view_nested.gif", getProject());
			} else {
				action = manager.createFlatView(dlg.getQuickViewName(), "icon_quick_view_call_graph.gif", getProject());
			}
			lstModel.addElement(action);
			getQuickViewsList().setSelectedValue(action, true);
			txtName.requestFocus();
			txtName.selectAll();
		}
	}

	/**
	 * Generates a new (unused) quick view name.
	 * @return the default name for a new quick view
	 */
	private String getDefaultQuickViewName() {
		String defaultName = "New Quick View";
		String name = defaultName;
		int i = 2;
		if (manager != null) {
			while ((manager.getQuickViewAction(name) !=  null) && (i < 15)) {
				name = defaultName + " (" + i + ")";
				i++;
			}
		}
		return name;
	}

	private void deleteSelectedQuickView() {
		boolean prompt = true;
		selectedAction = null;
		QuickViewAction action = (QuickViewAction) getQuickViewsList().getSelectedValue();
		if (action != null) {
			if (prompt) {
				String msg = "Are you sure you want to delete '" + action.getActionName() + "'?";
				int choice = JOptionPane.showConfirmDialog(getGUI(), msg, "Delete?",
										JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice == JOptionPane.NO_OPTION) {
					return;
				}
			}
			if (manager != null) {
				manager.removeQuickView(action);
			}
			action.dispose();
			loadQuickViews();
		}
	}

	private void deleteAllQuickViews() {
		if (manager != null) {
			String msg = "Are you sure you want to delete all your Quick Views?\nThis operation cannot be undone.";
			int choice = JOptionPane.showConfirmDialog(getGUI(), msg, "Delete All?",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice == JOptionPane.YES_OPTION) {
				manager.removeAll();
				loadQuickViews();
			}
		}
	}

	private void importExportQuickViews(boolean importing) {
		if (manager != null) {
			Window parent = SwingUtilities.windowForComponent(getGUI());
			if (parent instanceof Dialog) {
				new ImportExportQuickViewsDialog((Dialog) parent, manager, importing);
			} else if (parent instanceof Frame) {
				new ImportExportQuickViewsDialog((Frame) parent, manager, importing);
			}

			// update the list
			if (importing) {
				loadQuickViews();
			}
		}
	}

	private void initialize() {
		createActions();

		gui = new JPanel(new BorderLayout());
		gui.add(getToolBar(), BorderLayout.NORTH);
		gui.add(getMainSplitPane(), BorderLayout.CENTER);

		loadIcons();
		refresh();

		getMainSplitPane().setDividerLocation(220);
	}

	private void createActions() {
		newAction = new DefaultShrimpAction("New Quick View ", ResourceHandler.getIcon("icon_new.gif"),
				"Creates a new quick view") {
			public void actionPerformed(ActionEvent e) {
				newQuickView();
			}
		};
		deleteAction = new DefaultShrimpAction("Delete ", ResourceHandler.getIcon("icon_delete.gif"),
				"Delete the selected quick view") {
			public void actionPerformed(ActionEvent e) {
				deleteSelectedQuickView();
			}
		};
		deleteAllAction = new DefaultShrimpAction("Delete All ", ResourceHandler.getIcon("icon_delete_all.gif"),
				"Deletes all the quick views") {
			public void actionPerformed(ActionEvent e) {
				deleteAllQuickViews();
			}
		};
		runAction = new DefaultShrimpAction("Run Quick View ", ResourceHandler.getIcon("icon_forward.gif"),
				"Run the selected quick view in the main view") {
			public void actionPerformed(ActionEvent e) {
				if (selectedAction != null) {
					if (manager != null) {
						// For Creole
						manager.run(selectedAction);
					} else {
						selectedAction.startAction();
					}
				}
			}
		};
		importAction = new DefaultShrimpAction("Import ", ResourceHandler.getIcon("icon_import.gif"),
				"Import your quick views from a file") {
			public void actionPerformed(ActionEvent e) {
				importExportQuickViews(true);
			}
		};
		exportAction = new DefaultShrimpAction("Export ", ResourceHandler.getIcon("icon_export.gif"),
				"Export your quick views to a file") {
			public void actionPerformed(ActionEvent e) {
				importExportQuickViews(false);
			}
		};
		defaultsAction = new DefaultShrimpAction("Defaults", "Revert back to the default quick views") {
			public void actionPerformed(ActionEvent e) {
				loadDefaults();
			}
		};

		helpAction = new DefaultShrimpAction(ShrimpConstants.MENU_HELP,
				ResourceHandler.getIcon("icon_help.gif"), "Displays extra information about quick views") {
			public void actionPerformed(ActionEvent e) {
				try {
					BrowserLauncher.openURL(ShrimpConstants.SHRIMP_QUICKVIEWS_WEBSITE);
				} catch (IOException e1) {
				}
			}
		};
	}

	private JToolBar getToolBar() {
		if (toolbar == null) {
			toolbar = new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setBorder(BorderFactory.createEtchedBorder(1));
			toolbar.add(new JButton(newAction));
			toolbar.add(new JButton(deleteAction));
			toolbar.add(new JButton(deleteAllAction));
			toolbar.add(new JButton(defaultsAction));
			toolbar.addSeparator();
			toolbar.add(new JButton(runAction));
			toolbar.addSeparator();
			toolbar.add(new JButton(importAction));
			toolbar.add(new JButton(exportAction));
			toolbar.add(Box.createHorizontalGlue());
			toolbar.add(helpAction);
		}
		return toolbar;
	}

	private JSplitPane getMainSplitPane() {
		if (splMain == null) {
			splMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splMain.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
			JScrollPane scroll = new JScrollPane(getQuickViewsList());
			scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
			splMain.setLeftComponent(scroll);
			splMain.setRightComponent(getDetailsPanel());
		}
		return splMain;
	}

	private JList getQuickViewsList() {
		if (lstQuickViews == null) {
			lstModel = new DefaultListModel();
			lstQuickViews = new JList(lstModel) {
				public String getToolTipText(MouseEvent e) {
					return getQuickViewActionToolTip(e);
				}
			};
			lstQuickViews.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstQuickViews.addListSelectionListener(this);
			lstQuickViews.setCellRenderer(new ActionListCellRenderer());
		}
		return lstQuickViews;
	}

	private JPanel getDetailsPanel() {
		if (pnlDetails == null) {
			pnlDetails = new JPanel(new BorderLayout());
			pnlDetails.setBorder(BorderFactory.createLoweredBevelBorder());

			GradientPanel container = new GradientPanel(new BorderLayout());
			container.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			pnlDetails.add(new JScrollPane(container), BorderLayout.CENTER);
			container.add(getDetailsCollapsiblePanel(), BorderLayout.NORTH);

			JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);
			tab.setOpaque(true);
			tab.setBackground(getDetailsCollapsiblePanel().getInnerBackgroundColor());
			tab.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
			tab.addTab("Node & Arc Types", ResourceHandler.getIcon("icon_node_arc.gif"), getNodesAndArcsPanel());
			tab.addTab("Nesting Hierarcy", ResourceHandler.getIcon("icon_hierarchy.gif"), getHierarchyPanel());
			tab.addTab("Composite Arcs", ResourceHandler.getIcon("icon_composite_arcs.gif"), getCompositesPanel());
			container.add(tab, BorderLayout.CENTER);
		}
		return pnlDetails;
	}

	private CollapsiblePanel getDetailsCollapsiblePanel() {
		if (paneDetails == null) {
			paneDetails = new CollapsiblePanel("Quick View Details", ResourceHandler.getIcon("icon_new.gif"), getTopDetailsPanel());
			paneDetails.setCollapsed(false);
		}
		return paneDetails;
	}

	private JPanel getTopDetailsPanel() {
		if (pnlTopDetails == null) {
			pnlTopDetails = new TransparentPanel(new GridBagLayout());
			// create a 3x3 grid
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(2, 2, 2, 4);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.2;
			c.weighty = 1;
			JLabel lbl = new JLabel("Name: ");
			lbl.setPreferredSize(LABEL_SIZE);
			pnlTopDetails.add(lbl, c);
			c.gridy = 1;
			lbl = new JLabel("Layout: ");
			lbl.setPreferredSize(LABEL_SIZE);
			pnlTopDetails.add(lbl, c);
			c.gridy = 2;
			lbl = new JLabel("Label mode: ");
			lbl.setPreferredSize(LABEL_SIZE);
			pnlTopDetails.add(lbl, c);

			c.gridx = 1;
			c.gridy = 0;
			c.weightx = 0.6;
			txtName = new JTextField();
			txtName.setPreferredSize(COMBO_SIZE);
			txtName.addKeyListener(listener);
			pnlTopDetails.add(txtName, c);
			c.gridy = 1;
			pnlTopDetails.add(getLayoutComboBox(), c);
			c.gridy = 2;
			pnlTopDetails.add(getLabelModeComboBox(), c);

			c.gridx = 2;
			c.gridy = 0;
			c.weightx = 0.2;
			pnlTopDetails.add(getDisplayCheckBox(), c);
			c.gridy = 1;
			c.gridheight = GridBagConstraints.REMAINDER;
			JPanel p = new TransparentPanel(new FlowLayout(FlowLayout.LEFT));
			lbl = new JLabel("Icon: ");
			p.add(lbl);
			p.add(getIconsComboBox());
			pnlTopDetails.add(p, c);
		}
		return pnlTopDetails;
	}

	private JCheckBox getDisplayCheckBox() {
		if (chkDisplay == null) {
			chkDisplay = new JCheckBox(new AbstractAction("Display? ") {
				public void actionPerformed(ActionEvent e) {
					changeDisplay();
				}
			});
			chkDisplay.setHorizontalTextPosition(SwingConstants.LEFT);
			chkDisplay.setToolTipText("Display this quick view in the main view?");
			chkDisplay.setForeground(new Color(0, 0, 128));
			chkDisplay.setFont(chkDisplay.getFont().deriveFont(Font.BOLD));
			chkDisplay.setSelected(true);
			chkDisplay.setOpaque(false);
		}
		return chkDisplay;
	}

	private JComboBox getIconsComboBox() {
		if (cmbIcons == null) {
			cmbIcons = new JComboBox();
			cmbIcons.setEditable(false);
			cmbIcons.setPreferredSize(new Dimension(73, 56));
			cmbIcons.setMinimumSize(new Dimension(73, 56));
			cmbIcons.setRenderer(new IconListCellRenderer());
			cmbIcons.addActionListener(listener);
		}
		return cmbIcons;
	}

	private JComboBox getLayoutComboBox() {
		if (cmbLayout == null) {
			cmbLayout = new JComboBox();
			cmbLayout.setEditable(false);
			cmbLayout.setPreferredSize(COMBO_SIZE);
			cmbLayout.addActionListener(listener);
		}
		return cmbLayout;
	}

	private JComboBox getLabelModeComboBox() {
		if (cmbLabelMode == null) {
			cmbLabelMode = new JComboBox(LABEL_MODES);
			cmbLabelMode.setSelectedItem(DEFAULT_LABEL_MODE);
			cmbLabelMode.setEditable(false);
			cmbLabelMode.setPreferredSize(COMBO_SIZE);
			cmbLabelMode.addActionListener(listener);
		}
		return cmbLabelMode;
	}

	private JSplitPane getNodesAndArcsPanel() {
		if (splitNodesArcs == null) {
			splitNodesArcs = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitNodesArcs.setOpaque(false);
			splitNodesArcs.setTopComponent(getNodesPanel());
			splitNodesArcs.setBottomComponent(getArcsPanel());
			splitNodesArcs.setDividerLocation(120);
			splitNodesArcs.setBorder(null);
		}
		return splitNodesArcs;
	}

	private JPanel getNodesPanel() {
		if (pnlNodes == null) {
			pnlNodes = new TransparentPanel(new BorderLayout());
			pnlNodes.setBorder(DETAILS_BORDER);
			JComponent header = createListHeader("Select the node types of interest: ", getNodesList(), true, true);
			pnlNodes.add(header, BorderLayout.NORTH);
			pnlNodes.add(new JScrollPane(getNodesList()), BorderLayout.CENTER);
		}
		return pnlNodes;
	}

	private JButtonList getNodesList() {
		if (lstNodes == null) {
			lstNodes = new JButtonList(new DefaultListModel());
			lstNodes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lstNodes.addItemListener(listener);
		}
		return lstNodes;
	}

	private JPanel getArcsPanel() {
		if (pnlArcs == null) {
			pnlArcs = new TransparentPanel(new BorderLayout());
			pnlArcs.setBorder(DETAILS_BORDER);
			pnlArcs.add(createListHeader("Select the arc types of interest:", getArcsList(), true, true), BorderLayout.NORTH);
			pnlArcs.add(new JScrollPane(getArcsList()), BorderLayout.CENTER);
		}
		return pnlArcs;
	}

	private JButtonList getArcsList() {
		if (lstArcs == null) {
			lstArcs = new JButtonList(new DefaultListModel());
			lstArcs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lstArcs.addItemListener(listener);
		}
		return lstArcs;
	}

	private JPanel getHierarchyPanel() {
		if (pnlHierarchy == null) {
			pnlHierarchy = new TransparentPanel(new BorderLayout());
			pnlHierarchy.setBorder(DETAILS_BORDER);

			JComponent comp = createListHeader("Select the nesting hierarchy types:");
			comp.add(createButton(createDefaultCprelsAction(), false));
			comp.add(createButton(createCheckNoneAction(getHierarchyList()), false));
			pnlHierarchy.add(comp, BorderLayout.NORTH);
			pnlHierarchy.add(new JScrollPane(getHierarchyList()), BorderLayout.CENTER);

			JPanel pnl = new TransparentPanel(new GridLayout(1, 2));
			pnlHierarchy.add(pnl, BorderLayout.SOUTH);
			chkNoHierarchy = new JCheckBox("No hierarchy", false);
			chkNoHierarchy.setOpaque(false);
			pnl.add(chkNoHierarchy);
			chkNoHierarchy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					boolean noHierarchy = chkNoHierarchy.isSelected();
					getHierarchyList().setEnabled(!noHierarchy);
					chkInverted.setEnabled(!noHierarchy);
					if (selectedAction != null) {
						String[] cprels = (noHierarchy ? new String[0] : getSelectionArrayFromList(getHierarchyList()));
						selectedAction.getConfigurator().setCprels(cprels);
					}
				}
			});

			chkInverted = new JCheckBox("Invert hierarchy?");
			chkInverted.setOpaque(false);
			pnl.add(chkInverted);
			chkInverted.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (selectedAction != null) {
						selectedAction.getConfigurator().setInverted(chkInverted.isSelected());
					}
				}
			});
		}
		return pnlHierarchy;
	}

	/** Creates an action for selecting the default cprels. */
	private Action createDefaultCprelsAction() {
		Action defaultAction = new AbstractAction("Default") {
			public void actionPerformed(ActionEvent e) {
				if (selectedAction != null) {
					String[] cprels = (manager != null ? manager.getDefaultCprels() : new String[0]);
					setCheckedCprels(getHierarchyList(), cprels);
					if (((cprels.length > 0) && chkNoHierarchy.isSelected()) ||
						((cprels.length == 0) && !chkNoHierarchy.isSelected())) {
						chkNoHierarchy.doClick();
					}
				}
			}
		};
		defaultAction.putValue(Action.SHORT_DESCRIPTION, "Select the default hierarchy relationships (for nesting)");
		return defaultAction;
	}

	private JButtonList getHierarchyList() {
		if (lstHierarchy == null) {
			lstHierarchy = new JButtonList(new DefaultListModel());
			lstHierarchy.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lstHierarchy.addItemListener(listener);
		}
		return lstHierarchy;
	}

	private JPanel getCompositesPanel() {
		if (pnlComposites == null) {
			pnlComposites = new TransparentPanel(new BorderLayout());
			JLabel lbl = new JLabel("Composite arcs are only applicable in nested quick views", JLabel.CENTER);
			lbl.setForeground(new Color(196, 0, 0));
			lbl.setBackground(Color.white);
			lbl.setOpaque(true);
			lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
			//lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
			pnlComposites.add(lbl, BorderLayout.NORTH);
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane.setOpaque(false);
			splitPane.setBorder(null);

			JPanel pnl = new TransparentPanel(new BorderLayout());
			pnl.setBorder(DETAILS_BORDER);
			pnl.add(createListHeader("Select the nodes types to open:", getCompositeNodesList(), true, true), BorderLayout.NORTH);
			pnl.add(new JScrollPane(getCompositeNodesList()), BorderLayout.CENTER);
			pnl.setPreferredSize(new Dimension(400, 120));
			splitPane.setTopComponent(pnl);

			JSplitPane compositesArcsPanel = getCompositesArcsPanel();
			compositesArcsPanel.setPreferredSize(new Dimension(400, 150));
			splitPane.setBottomComponent(compositesArcsPanel);
			splitPane.setDividerLocation(120);

			pnlComposites.add(splitPane, BorderLayout.CENTER);
		}
		return pnlComposites;
	}

	private JButtonList getCompositeNodesList() {
		if (lstCompositeNodes == null) {
			lstCompositeNodes = new JButtonList(new DefaultListModel());
			lstCompositeNodes.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lstCompositeNodes.addItemListener(listener);
		}
		return lstCompositeNodes;
	}

	private JSplitPane getCompositesArcsPanel() {
		if (splitCompositeArcs == null) {
			splitCompositeArcs = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitCompositeArcs.setOpaque(false);

			JComponent header = createListHeader(/* Default */ "Groups:");
			header.add(getPreviewCompositesCheckBox());
			header.setMaximumSize(new Dimension(1000, 18));

			JPanel left = new TransparentPanel(new BorderLayout());
			left.add(header, BorderLayout.NORTH);
			left.add(new JScrollPane(getDefaultGroupsList()), BorderLayout.CENTER);
			splitCompositeArcs.setLeftComponent(left);

			JPanel right = new TransparentPanel(new BorderLayout());
			right.add(createListHeader("Arc types:"), BorderLayout.NORTH);
			right.add(new JScrollPane(getDefaultCompositeArcsList()), BorderLayout.CENTER);
			splitCompositeArcs.setRightComponent(right);

			// Custom Groups - commented out because it is too complicated for the user?
			// the user can create custom groups inside the arc filter panel
			/* Need to change layout into grid?  Used to use GridBag...
			header = createListHeader("Custom Groups:");
			header.add(createButton(createAddCustomGroupAction(), false));
			header.add(createButton(createDeleteCustomGroupAction(), false));
			pnlCompositeArcs.add(header, c);
			header = createListHeader("Arc types:");
			header.add(createButton(createAddCompositeArcTypeAction(), false));
			header.add(createButton(createDeleteCompositeArcTypeAction(), false));
			pnlCompositeArcs.add(header, c);

			pnlCompositeArcs.add(new JScrollPane(getCustomGroupsList()), c);
			pnlCompositeArcs.add(new JScrollPane(getCustomCompositeArcsList()), c);
			*/
			splitCompositeArcs.setDividerLocation(180);
		}
		return splitCompositeArcs;
	}

	private JList getDefaultGroupsList() {
		if (lstDefaultGroups == null) {
			lstDefaultGroups = new JButtonList(new DefaultListModel());
			GroupsListCellRenderer groupsRenderer = new GroupsListCellRenderer(lstDefaultGroups);
			lstDefaultGroups.setCellRenderer(groupsRenderer);
			lstDefaultGroups.registerKeyboardAction(groupsRenderer, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), JComponent.WHEN_FOCUSED);
			lstDefaultGroups.addMouseListener(groupsRenderer);
			lstDefaultGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstDefaultGroups.addItemListener(listener);
		}
		return lstDefaultGroups;
	}

	private JButtonList getCustomGroupsList() {
		if (lstCustomGroups == null) {
			lstCustomGroups = new JButtonList(new DefaultListModel());
			lstCustomGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			lstCustomGroups.addItemListener(listener);
		}
		return lstCustomGroups;
	}

	private JList getDefaultCompositeArcsList() {
		if (lstDefaultCompositeArcs == null) {
			lstDefaultCompositeArcs = new JList(new DefaultListModel());
			//lstDefaultCompositeArcs.setEnabled(false);
		}
		return lstDefaultCompositeArcs;
	}

	private JList getCustomCompositeArcsList() {
		if (lstCustomCompositeArcs == null) {
			lstCustomCompositeArcs = new JButtonList(new DefaultListModel());
			lstCustomCompositeArcs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			lstCustomCompositeArcs.addItemListener(listener);
		}
		return lstCustomCompositeArcs;
	}

	private JComponent createListHeader(String title) {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setOpaque(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		toolbar.add(new JLabel(title));
		toolbar.add(Box.createHorizontalGlue());	// right align buttons
		toolbar.setMinimumSize(new Dimension(20, 16));
		toolbar.setPreferredSize(new Dimension(100, BUTTON_HEIGHT));
		return toolbar;
	}

	private JComponent createListHeader(String title, JButtonList list, boolean selectAll, boolean selectNone) {
		JComponent header = createListHeader(title);
		if (selectAll) {
			header.add(createButton(createCheckAllAction(list), false));
		}
		if (selectNone) {
			header.add(createButton(createCheckNoneAction(list), false));
		}
		return header;
	}

	private AbstractButton createButton(Action action, boolean toggle) {
		AbstractButton btn;
		if (toggle) {
			btn = new JToggleButton(action);
		} else {
			btn = new JButton(action);
		}
		btn.setOpaque(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		String txt = btn.getText();
		String tt = (String)action.getValue(Action.SHORT_DESCRIPTION);
		btn.setToolTipText((tt != null ? tt : txt));
		Dimension dim;
		if (txt == null) {
			// no text - assume a 16x16 icon only
			dim = new Dimension(28, BUTTON_HEIGHT);
		} else {
			dim = btn.getPreferredSize();
			dim.setSize(dim.width, BUTTON_HEIGHT);
			btn.setForeground(Color.blue);
		}
		btn.setPreferredSize(dim);
		btn.setMaximumSize(new Dimension(dim.width + 12, BUTTON_HEIGHT));
		toolbar.add(btn);
		return btn;
	}

	private Action createCheckAllAction(final JButtonList list) {
		Action action = new AbstractAction("Check All") {
			public void actionPerformed(ActionEvent e) {
				list.checkAll();
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Select all the items in the list");
		return action;
	}

	private Action createCheckNoneAction(final JButtonList list) {
		Action action = new AbstractAction("Check None") {
			public void actionPerformed(ActionEvent e) {
				list.checkNone();
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Select none of the items in the list");
		return action;
	}


	private JCheckBox getPreviewCompositesCheckBox() {
		if (chkPreviewComposites == null) {
			Action previewAction = new AbstractAction("Preview?", null) {
				public void actionPerformed(ActionEvent e) {}
			};
			previewAction.putValue(Action.SHORT_DESCRIPTION, "Preview composite arcs in the main view?");
			chkPreviewComposites = new JCheckBox(previewAction);
			chkPreviewComposites.setOpaque(false);
			chkPreviewComposites.setSelected(true);
			chkPreviewComposites.setPreferredSize(new Dimension(80, 18));
		}
		return chkPreviewComposites;
	}

	protected Action createAddCustomGroupAction() {
		Action action = new AbstractAction("Add") {
			public void actionPerformed(ActionEvent e) {
				if (selectedAction != null) {
					ExpressViewConfigurator config = selectedAction.getConfigurator();
					String newName = JOptionPane.showInputDialog(getCustomGroupsList(), "Enter a name for the new group");
					Map groups = config.getCompositeArcs();
					if (!groups.containsKey(newName)) {
						groups.put(newName, Collections.EMPTY_LIST);
						loadCustomGroups(config);
					} else {
						JOptionPane.showMessageDialog(getCustomGroupsList(), "A group with that name already exists.", "Duplicate group name", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Add a new group");
		return action;
	}

	protected Action createDeleteCustomGroupAction() {
		Action action = new AbstractAction(null, ResourceHandler.getIcon("icon_delete.gif")) {
			public void actionPerformed(ActionEvent e) {
				String groupName = (String) getCustomGroupsList().getSelectedValue();
				if ((selectedAction != null) && (groupName != null)) {
					selectedAction.getConfigurator().getCompositeArcs().remove(groupName);
					loadCustomGroups(selectedAction.getConfigurator());
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Delete the selected custom group");
		return action;
	}

	protected Action createAddCompositeArcTypeAction() {
		Action action = new AbstractAction("Add") {
			public void actionPerformed(ActionEvent ae) {
				String groupName = (String) getCustomGroupsList().getSelectedValue();
				if ((getProject() != null) && (selectedAction != null) && (groupName != null)) {
					Map map = selectedAction.getConfigurator().getCompositeArcs();
					Collection cArcs = new HashSet();
					// add existing arc types
					if (map.containsKey(groupName)) {
						cArcs.addAll((Collection)map.get(groupName));
					}

					Frame frame = ApplicationAccessor.getParentFrame();
					ChooseRelationshipType dlg = new ChooseRelationshipType(frame, getProject(), true, cArcs);
					if (dlg.isOKPressed()) {
						cArcs.addAll(dlg.getSelectedRelationships());
						map.put(groupName, cArcs);

						// reset the selection to the group - this will update the composite arcs list
						getCustomGroupsList().setSelectedIndices(new int[0]);
						getCustomGroupsList().setSelectedValue(groupName, false);
					}
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Add a composite arc type");
		return action;
	}

	protected Action createDeleteCompositeArcTypeAction() {
		Action action = new AbstractAction(null, ResourceHandler.getIcon("icon_delete.gif")) {
			public void actionPerformed(ActionEvent e) {
				Object[] cArcs = getCustomCompositeArcsList().getSelectedValues();
				String groupName = (String) getCustomGroupsList().getSelectedValue();
				if ((selectedAction != null) && (cArcs != null) && (groupName != null)) {
					Map map = selectedAction.getConfigurator().getCompositeArcs();
					Collection existingArcs = (Collection) map.get(groupName);
					for (int i = 0; i < cArcs.length; i++) {
						existingArcs.remove(cArcs[i]);
					}
					// reset the selection to the group - this will update the composite arcs list
					getCustomGroupsList().setSelectedIndices(new int[0]);
					getCustomGroupsList().setSelectedValue(groupName, false);
				}
			}
		};
		action.putValue(Action.SHORT_DESCRIPTION, "Delete the selected composite arc type(s)");
		return action;
	}

	/**
	 * Gets a {@link List} of the selected objects in the {@link JList}.
	 * @param lst the list with selected items
	 * @return {@link List} of objects
	 */
	private List getCheckedItemsFromList(JButtonList lst) {
		return ShrimpUtils.toList(lst.getCheckedItems());
	}

	/**
	 * Gets an array of the checked objects from the {@link JButtonList}.
	 * This method assumes that the items in the list are {@link String}s!
	 * @param lst the list with selected items
	 * @return String[]
	 */
	private String[] getSelectionArrayFromList(JButtonList lst) {
		List list = getCheckedItemsFromList(lst);
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * Selects the String types in the given {@link JList}.
	 * @param lst the list to set the selection on
	 * @param cprels the values in the list to select
	 */
	private void setCheckedCprels(JButtonList lst, String[] cprels) {
		ArrayList list = new ArrayList(cprels.length);
		for (int i = 0; i < cprels.length; i++) {
			list.add(cprels[i]);
		}
		setCheckedItemsInList(lst, list);
	}

	/**
	 * Utility method for setting the selection to a JList.
	 * @param lst the list to set the selection on
	 * @param types the objects in the list that should be selected
	 */
	private void setCheckedItemsInList(JButtonList lst, Collection types) {
		if (types == null) {
			return;
		}

		List indicesList = new ArrayList();
		//lst.setSelectedIndices(indices); // clear selected
		for (int i = 0; i < lst.getModel().getSize(); i++) {
			Object artType = lst.getModel().getElementAt(i);
			if (types.contains(artType)) {
				indicesList.add(new Integer(i));
			}
		}
		int[] indices = new int[indicesList.size()];
		int i = 0;
		for (Iterator iter = indicesList.iterator(); iter.hasNext();) {
			Integer indexInt = (Integer) iter.next();
			indices[i] = indexInt.intValue();
			i++;
		}
		lst.setCheckedIndices(indices);
		// ensure the first index is visible
		if (indices.length > 0) {
			lst.ensureIndexIsVisible(indices[0]);
		}
	}

	public boolean okPressed() {
		return okPressed;
	}

	private void validateQuickViewName() {
		String txt = txtName.getText().trim();
		if ((txt.length() > 0) && Color.yellow.equals(txtName.getBackground())) {
			txtName.setBackground(Color.white);
		} else if (txt.length() == 0) {
			txtName.setBackground(Color.yellow);
		}

		// update the selctedAction's name
		if (selectedAction != null) {
			if ((txt.length() > 0) && !txt.equals(selectedAction.getActionName())) {
				selectedAction.setActionName(txt);
				// probably don't need to fire this event - the name is only used
				// as a tooltip which should get updated automatically
				//manager.quickViewChanged(selectedAction);
			}
		}
	}

	public void loadDefaults() {
		if (manager != null) {
			String msg = "Are you sure you want to revert to the default quick views?\n" +
						 "You will lose any new quick views that you've created.";
			int choice = JOptionPane.showConfirmDialog(gui, msg, "Confirm", JOptionPane.YES_NO_OPTION);
			if (choice == JOptionPane.YES_OPTION) {
				manager.loadDefaults();
				loadQuickViews();
			}
		}
	}

	private void changeDisplay() {
		if (selectedAction != null) {
			boolean display = chkDisplay.isSelected();
			if (display != selectedAction.isDisplay()) {
				selectedAction.setDisplay(display);
				getQuickViewsList().repaint();
				// need to add or remove this from the quick views panel
				List empty = Collections.EMPTY_LIST;
				QuickViewEvent evt = (display ?
					new QuickViewEvent(manager, ShrimpUtils.toList(selectedAction), empty, empty) :
					new QuickViewEvent(manager, empty, ShrimpUtils.toList(selectedAction), empty));
				manager.fireQuickViewsChangedEvent(evt);
			}
		}
	}

	private String getQuickViewActionToolTip(MouseEvent e) {
		String tt = null;
		// Convert the mouse coordinates where the left mouse button was pressed
		int index = getQuickViewsList().locationToIndex(e.getPoint());
		ListModel model = getQuickViewsList().getModel();
		if ((index >= 0) && (index < model.getSize())) {
			Object obj = model.getElementAt(index);
			if (obj instanceof QuickViewAction) {
				QuickViewAction action = (QuickViewAction) obj;
				tt = action.getToolTip();
			}
		}
		return tt;
	}

	/**
	 * Listens for many actions - key events, list selections, actions.
	 *
	 * @author Chris Callendar
	 * @date 4-May-07
	 */
	class QuickViewActionListener extends KeyAdapter implements ItemListener, ActionListener {

		public void valueChanged(ListSelectionEvent e) {

		}

		public void itemStateChanged(ItemEvent e) {
			// fired by the lists (nodes, arcs, hierarchy, ...)
			if (selectedAction == null) {
				return;
			}

			ExpressViewConfigurator config = selectedAction.getConfigurator();
			Object src = e.getSource();
			if (src == getNodesList()) {
				List sel = getCheckedItemsFromList(getNodesList());
				config.setNodeTypesOfInterest(sel);
				getNodesList().setBackground((sel.size() == 0 ? Color.yellow : Color.white));
				// need to update the validity of the action
				manager.quickViewChanged(selectedAction);
			} else if (src == getArcsList()) {
				List sel = getCheckedItemsFromList(getArcsList());
				config.setArcTypesOfInterest(sel);
				if (!selectedAction.isNested()) {
					getArcsList().setBackground((sel.size() == 0 ? Color.yellow : Color.white));
				}
				// need to update the validity of the action
				manager.quickViewChanged(selectedAction);
			} else if (src == getHierarchyList()) {
				config.setCprels(getSelectionArrayFromList(getHierarchyList()));
			} else if (src == getCompositeNodesList()) {
				config.setNodeTypesToOpen(getCheckedItemsFromList(getCompositeNodesList()));
			} else if (src == getDefaultGroupsList()) {
				setGroupRelTypes(config, (RelTypeGroup) getDefaultGroupsList().getSelectedValue());
			} else if (src == getCustomGroupsList()) {
				setCustomGroupRelTypes(config, (String) getCustomGroupsList().getSelectedValue());
			}
		}

		private void setCustomGroupRelTypes(ExpressViewConfigurator config, String groupName) {
			Collection arcs = (Collection) config.getCompositeArcs().get(groupName);
			Vector v = (arcs == null ? new Vector(0) : (arcs instanceof Vector ? (Vector)arcs : new Vector(arcs)));
			Collections.sort(v);
			getCustomCompositeArcsList().setListData(v);
			if (v.size() > 0) {
				getCustomCompositeArcsList().setSelectedIndex(0);
			}
		}

		private void setGroupRelTypes(ExpressViewConfigurator config, RelTypeGroup group) {
			if (group != null) {
				Collection c = group.getRelTypes();
				if (c.size() == 0) {
					// the size will be 0 for any of the custom groups, and will be > 0 for the default group
					c = (Collection) config.getCompositeArcs().get(group.getGroupName());
					if (c == null) {
						c = Collections.EMPTY_LIST;
					}
				}
				Vector types = (c instanceof Vector ? (Vector)c : new Vector(c));
				getDefaultCompositeArcsList().setListData(types);
			}
		}

		public void actionPerformed(ActionEvent e) {
			// fired by the 3 combo boxes
			if (selectedAction != null) {
				ExpressViewConfigurator config = selectedAction.getConfigurator();
				Object src = e.getSource();
				if (src == getIconsComboBox()) {
					IconFilename icon = (IconFilename) getIconsComboBox().getSelectedItem();
					if ((icon != null) && !icon.getFilename().equalsIgnoreCase(selectedAction.getIconFilename())) {
						selectedAction.setIconFilename(icon.getFilename());
						getQuickViewsList().repaint();	// re-render the list
						// need to update the icon this from the quick views panel
						manager.quickViewChanged(selectedAction);
					}
				} else if (src == getLayoutComboBox()) {
					String newLayout = (String) getLayoutComboBox().getSelectedItem();
					if ((newLayout != null) && !newLayout.equals(config.getLayoutMode())) {
						config.setLayoutMode(newLayout);
					}
				} else if (src == getLabelModeComboBox()) {
					String newLabelMode = (String) getLabelModeComboBox().getSelectedItem();
					if ((newLabelMode != null) && !newLabelMode.equals(config.getLabelMode())) {
						config.setLabelMode(newLabelMode);
					}
				}
			}
		}

		public void keyReleased(KeyEvent e) {
			validateQuickViewName();
		}

	}

	class ActionListCellRenderer extends JLabel implements ListCellRenderer {
		private final Border NO_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		private final Border INVALID_BORDER = BorderFactory.createLineBorder(Color.red, 2);
		private final Border VALID_FOCUS_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		private final Color INVALID = new Color(210, 210, 210);

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			QuickViewAction action = (QuickViewAction) value;
			setText(action.getActionName());
			setIcon(action.getIcon());
			boolean valid = manager.isValid(action);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
		  		setForeground((valid && action.isDisplay()) ? list.getSelectionForeground() : list.getSelectionForeground().darker());
			} else {
				setBackground(action.isDisplay() ? list.getBackground() : INVALID);
				setForeground(list.getForeground());
			}
			setBorder(valid ? (cellHasFocus ? VALID_FOCUS_BORDER : NO_BORDER) : INVALID_BORDER);
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	class IconListCellRenderer extends JLabel implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value instanceof IconFilename) {
				IconFilename iconFilename = (IconFilename) value;
				Icon icon = iconFilename.getIcon();
				if (icon == null) {
					icon = iconFilename.reloadResourceIcon();	// reload the icon using the ResourceHandler
				}
				setIcon(icon);
				String filename = iconFilename.getFilename();
				setText((icon == null ? filename : null));
				setToolTipText(filename);
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
		  		setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}
	}

	class GroupsListCellRenderer extends JCheckBox implements ListCellRenderer, ActionListener, MouseListener {
		private JList list;
		private final int hotspot;

		public GroupsListCellRenderer(JList list) {
			super();
			this.list = list;
			hotspot = this.getPreferredSize().width;
		}

		public void actionPerformed(ActionEvent e) {
			toggleSelection(list.getSelectedIndex());
		}

	    private void toggleSelection(int index){
	        if (index < 0) {
				return;
			}

	        RelTypeGroup group = (RelTypeGroup) list.getModel().getElementAt(index);
        	CompositeArcsManager mgr = getCompositeArcsManager();
	        if ((selectedAction != null) && (group != null) && (mgr != null)) {
	        	if (chkPreviewComposites.isSelected()) {
	        		mgr.setCompositesEnabled(group, !group.areCompositesEnabled());
	        	}
	        	ExpressViewConfigurator config = selectedAction.getConfigurator();
				Map cArcs = config.getCompositeArcs();
	        	String groupName = group.getGroupName();
	        	boolean add = false;
				if (cArcs.containsKey(groupName)) {
	        		cArcs.remove(groupName);
	        	} else {
	        		cArcs.put(groupName, group.getRelTypes());
	        		add = true;
	        	}
				// repaint this list - this will check/uncheck the checkbox
		        list.repaint(list.getCellBounds(index, index));

		        // now reload the custom groups list and selec the added group
		        loadCustomGroups(config);
		        if (add) {
		        	getCustomGroupsList().setSelectedValue(groupName, true);
		        }
	        }
	    }

	    /*------------------------------[ MouseListener ]-------------------------------------*/

	    public void mouseEntered(MouseEvent e) {}
	    public void mouseExited(MouseEvent e) {}
	    public void mousePressed(MouseEvent e) {}
	    public void mouseReleased(MouseEvent e) {}

	    public void mouseClicked(MouseEvent me) {
	        int index = list.locationToIndex(me.getPoint());
	        if (index < 0) {
				return;
			}

	        // return if the click is not on the checkbox and is a single click
	        // toggle if it is a double click
			if ((me.getX() > list.getCellBounds(index, index).x + hotspot) && (me.getClickCount() == 1)) {
				return;
			}

			toggleSelection(index);
	    }

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			RelTypeGroup group = (RelTypeGroup) value;
			String groupName = group.getGroupName();
			setText(groupName);
			setToolTipText("Check the box to show composite arcs for this group and its arc types");
			if (selectedAction != null) {
				// group.areCompositesEnabled()
				setSelected(selectedAction.getConfigurator().getCompositeArcs().containsKey(groupName));
			}
			if (isSelected) {
				setBackground(list.getSelectionBackground());
		  		setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			return this;
		}

	}

}
