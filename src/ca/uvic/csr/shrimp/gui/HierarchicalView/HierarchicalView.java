/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.HierarchicalView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryBean;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.DisplayBean.layout.HierarchicalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HorizontalTreeLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.OrthogonalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.RadialLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SequenceLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SugiyamaLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.VerticalTreeLayout;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationAdapter;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationEvent;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractViewTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.DisplayFilterChangedAdapter;
import ca.uvic.csr.shrimp.adapter.DisplayFilterRequestAdapter;
import ca.uvic.csr.shrimp.adapter.ShrimpInputAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.MouseSelectAndMoveAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManagerListener;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsAddedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsModifiedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsRemovedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.PanEastAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanNorthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanSouthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanWestAdapter;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.ZoomInAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomInZoomModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutZoomModeAdapter;
import ca.uvic.csr.shrimp.util.NodeNameComparator;
import edu.umd.cs.piccolox.swing.PScrollPane;


/**
 * This view shows a flat hierarchy, usually the hierarchy being used in the shrimp view.  It
 * can also be used to filter nodes, navigate, or display search results.
 *
 * @author Rob Lintern
 * @date April 2002
 */
public class HierarchicalView extends AbstractViewTool implements ViewTool {

	private static final int INITIAL_LEVELS_TO_EXPAND = 2;
	public static final int NAVIGATION_MODE = 0;
	public static final int FILTERED_MODE = 1;
	public static final int SEARCH_MODE = 2;

	// gui components
	private JRootPane gui;
	private Action filterSelectedAction;
	private Action unfilterSelectedAction;

	private JPanel basePanel = new JPanel();
	//private JCheckBox chkHideFiltered = new JCheckBox("Hide Filtered");
	private JLabel modeLbl = new JLabel("Display Mode:   ");
	private JRadioButton rbtnNavigation = new JRadioButton("Navigation");
	private JRadioButton rbtnFilter = new JRadioButton("Filtered");
	private JRadioButton rbtnSearch = new JRadioButton("Search Results");
	private PScrollPane pScrollPane;

	// The adapters used in this view
	private ShrimpInputAdapter shrimpInputAdapter;
	private MouseSelectAndMoveAdapter msama;
	private DisplayFilterRequestAdapter filterRequestAdapter;
	//private AttrToVisVarChangeAdapter attrToVisVarChangeAdapter;
	private DisplayFilterChangedAdapter hierFilterChangedAdapter; // handles changes in hierarchical view's filterBean
	private HierSelectedArtifactsChangeListener hierSelectedArtifactsChangeAdapter;
	//private DataChangeAdapter hierDataChangeAdapter;
	private NodeMouseOverAdapter nodeMouseOverAdapter;


	/** The current child parent relationships being used in this view */
	private String[] cprels = new String[0];
    private boolean inverted = false;

	private int currentDisplayMode = NAVIGATION_MODE;
	private boolean hideFilteredMode = false;
	private Font labelFont;

	private JPopupMenu popupMenu;
	private HvPopupListener popupListener;
	private ActionManagerListener actionManagerListener;

	private Vector userControls = new Vector();
	private boolean displayBeanPopulated = false;
	private Vector displayModeChangeListeners = new Vector();
	private FilterBean externalFilterBean;

	private HvSvBridge bridge;

	/**
	 * Initializes the HierachicalView with the given project.
	 */
	public HierarchicalView(ShrimpProject project) {
		super(ShrimpProject.HIERARCHICAL_VIEW, project);
		gui = new JRootPane();
	}

	public void init() {
		if (project != null) {
			bridge = new HvSvBridge(this);
			try {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				cprels = dataBean.getDefaultCprels();
                inverted = dataBean.getDefaultCprelsInverted();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			cprels = new String[] { "contains" };
		}

		//---------------------- Create the beans ------------------------/

		//create the display bean and init
		DataDisplayBridge dataDisplayBridge = new DataDisplayBridge(this);
		PFlatDisplayBean displayBean = new PFlatDisplayBean(project, cprels, dataDisplayBridge);
		displayBean.setLabelBackgroundOpaque(true);
		addBean(ShrimpTool.DISPLAY_BEAN, displayBean);

		actionManager = new ActionManager();

		if (labelFont != null) {
			displayBean.setLabelFont(labelFont);
		}

		// create the selector bean and init
		SelectorBean selectorBean = new SelectorBean();
		addBean(ShrimpTool.SELECTOR_BEAN, selectorBean);
		selectorBean.setSelected(DisplayConstants.ZOOM_MODE, DisplayConstants.ZOOM);
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());

		Layout vertTreeLayout = new VerticalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_VERTICAL, false, NodeNameComparator.NODE_NAME_COMPARATOR);
		Layout horizTreeLayout = new HorizontalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_HORIZONTAL, false, NodeNameComparator.NODE_NAME_COMPARATOR);
		Layout radialLayout = new RadialLayout(displayBean, LayoutConstants.LAYOUT_RADIAL, false, NodeNameComparator.NODE_NAME_COMPARATOR);
		displayBean.addLayout(vertTreeLayout); //vertical tree is default layout
		displayBean.addLayout(horizTreeLayout);
		displayBean.addLayout(radialLayout);

		if (OrthogonalLayout.isLoaded()) {
			Layout hierarchicalLayout = new HierarchicalLayout (displayBean, LayoutConstants.LAYOUT_HIERARCHICAL, false, NodeNameComparator.NODE_NAME_COMPARATOR);
			displayBean.addLayout(hierarchicalLayout);
			Layout orthogonalLayout = new OrthogonalLayout (displayBean, LayoutConstants.LAYOUT_ORTHOGONAL, false, NodeNameComparator.NODE_NAME_COMPARATOR);
			displayBean.addLayout(orthogonalLayout);
		}

		//@tag Shrimp.sequence
		try {
			Layout sequenceLayout = new SequenceLayout(displayBean, LayoutConstants.LAYOUT_SEQUENCE, false, NodeNameComparator.NODE_NAME_COMPARATOR);
			displayBean.addLayout(sequenceLayout);
		} catch (NoClassDefFoundError ignore) {
		}

		// @tag Shrimp.sugiyama
		if (SugiyamaLayout.isInstalled()) {
			Layout sugiyamaLayout = new SugiyamaLayout(displayBean, LayoutConstants.LAYOUT_SUGIYAMA, false, NodeNameComparator.NODE_NAME_COMPARATOR);
			displayBean.addLayout(sugiyamaLayout);
		}

		FilterBean internalFilterBean = new FilterBean();
		addBean(ShrimpTool.DISPLAY_FILTER_BEAN, internalFilterBean);
		filterRequestAdapter = new DisplayFilterRequestAdapter(this);

		// want changes in hierarchical view's  filterBean to be reflected in this display
		hierFilterChangedAdapter = new DisplayFilterChangedAdapter(this);

		ActionHistoryBean actionHistoryBean = new ActionHistoryBean();
		addBean(ShrimpTool.ACTION_HISTORY_BEAN, actionHistoryBean);

		msama = new MouseSelectAndMoveAdapter(this);

		//---- DisplayBean Mouse adapters
		shrimpInputAdapter = new ShrimpInputAdapter(this);
		nodeMouseOverAdapter = new NodeMouseOverAdapter();

		//--- user controls
		userControls = new Vector();

		// pan east
		PanEastAdapter panEastAdapter = new PanEastAdapter(this);
		userControls.add(panEastAdapter);

		//pan west
		PanWestAdapter panWestAdapter = new PanWestAdapter(this);
		userControls.add(panWestAdapter);

		//pan north
		PanNorthAdapter panNorthAdapter = new PanNorthAdapter(this);
		userControls.add(panNorthAdapter);

		//pan south
		PanSouthAdapter panSouthAdapter = new PanSouthAdapter(this);
		userControls.add(panSouthAdapter);

		// zoom in while in zoom mode
		ZoomInZoomModeAdapter zoomInZoomModeAdapter = new ZoomInZoomModeAdapter(this);
		userControls.add(zoomInZoomModeAdapter);

		// zoom out while in zoom mode
		ZoomOutZoomModeAdapter zoomOutZoomModeAdapter = new ZoomOutZoomModeAdapter(this);
		userControls.add(zoomOutZoomModeAdapter);

		// zoom in while in any mode
		ZoomInAnyModeAdapter ziama = new ZoomInAnyModeAdapter(this);
		userControls.add(ziama);

		//zoom out while in any mode
		ZoomOutAnyModeAdapter zoama = new ZoomOutAnyModeAdapter(this);
		userControls.add(zoama);


		loadControlPreferences(userControls);
		ApplicationAccessor.getApplication().addApplicationListener(new ShrimpApplicationAdapter() {
			public void userControlsChanged(ShrimpApplicationEvent event) {
				loadControlPreferences(userControls);
			}
		});

		//---- DisplayBean adapters
		displayBean.addShrimpMouseListener(shrimpInputAdapter);
		displayBean.addShrimpMouseListener(nodeMouseOverAdapter);
		displayBean.addShrimpKeyListener(shrimpInputAdapter);
		displayBean.addShrimpMouseListener(msama);
		displayBean.addShrimpKeyListener(msama);
		displayBean.addFilterRequestListener(filterRequestAdapter);

		//---- SelectorBean adapters
		hierSelectedArtifactsChangeAdapter = new HierSelectedArtifactsChangeListener();
		selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, hierSelectedArtifactsChangeAdapter);

		// listen for changes to this filterBean
		internalFilterBean.addFilterChangedListener(hierFilterChangedAdapter);

		dataDisplayBridge.refresh();

		createGUIWidgets(displayBean, selectorBean);
		updateGUI(displayBean, selectorBean);
	}

	/** loads the control preferences from the file */
	public void loadControlPreferences(Vector userActions) {
		ShrimpApplication application =  ApplicationAccessor.getApplication();

		for (Iterator iterator = userActions.iterator(); iterator.hasNext();) {
			UserAction element = (UserAction) iterator.next();
			element.setUserEvents(application.getUserEvents(element));
		}

		shrimpInputAdapter.clearUserActions();

		for (int i = 0; i < userActions.size(); i++) {
			shrimpInputAdapter.addUserAction((UserAction)userActions.elementAt(i));
		}
	}

	public Component getGUI() {
		return gui;
	}

	public void setProject(ShrimpProject project) {
		// separate the bridge here before the project is set to null
		if (getProject() != null) {
			if (bridge != null) {
				bridge.separateShrimpViewHierarchicalView();
				bridge = null;
			}
		}

		// this calls refresh (if project isn't null) which re-creates the bridge
		super.setProject(project);
	}

	public void disposeTool() {
		clear();

		if (bridge != null) {
			bridge.separateShrimpViewHierarchicalView();
			bridge = null;
		}
		filterRequestAdapter = null;
		hierFilterChangedAdapter = null;
		hierSelectedArtifactsChangeAdapter = null;
		msama = null;
		nodeMouseOverAdapter = null;
		shrimpInputAdapter = null;

		removeBean(DISPLAY_BEAN);
		removeBean(SELECTOR_BEAN);
		removeBean(DISPLAY_FILTER_BEAN);
		removeBean(ACTION_HISTORY_BEAN);

		gui.getContentPane().removeAll();
	}

	private PFlatDisplayBean getDisplayBean() {
		PFlatDisplayBean displayBean = null;
		try {
			displayBean = (PFlatDisplayBean) getBean(DISPLAY_BEAN);
		} catch (BeanNotFoundException e) {
		}
		return displayBean;
	}

	public boolean isDisplayBeanPopulated() {
		return displayBeanPopulated;
	}

	public boolean populateDisplayBean() {
		if (project != null) {
			boolean cancelled = false;
			cancelled = createDisplayIncremental();
			displayBeanPopulated = true;
			return cancelled;
		}
		return false;
	}

	/**
	 * Adds nodes to the display to a certain level and does a tree layout.
	 */
	private boolean createDisplayIncremental() {
		final PFlatDisplayBean displayBean = getDisplayBean();
		Collection rootNodes = displayBean.getDataDisplayBridge().createDefaultRootNodes();
		for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
            final ShrimpNode rootNode = (ShrimpNode) iter.next();
    		displayBean.addObject(rootNode);

    		displayBean.setVisible(rootNode, true, false);
    		setNewDisplayMode(NAVIGATION_MODE);

    		try {
    			SwingUtilities.invokeLater(new Runnable() {
    				public void run() {
    					gui.revalidate();
    		    		rootNode.setIsCollapsed(true);
    					Collection nodesToExpand = new ArrayList();
    					nodesToExpand.add(rootNode);
    					displayBean.expandSubTree(rootNode, true, INITIAL_LEVELS_TO_EXPAND);
    					rootNode.setIsCollapsed(false);
    				}
    			});
    		} catch (Exception e) {
    			e.printStackTrace();
    			return false;
    		}
        }
		return true;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		disposeTool();
		init();

		// change to a busy cursor
		Cursor cursor = gui.getCursor();
		gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		PFlatDisplayBean displayBean = getDisplayBean();

		// clear the selector bean
		try {
			SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector());
			selectorBean.clearSelected(SelectorBeanConstants.TARGET_OBJECT);
		} catch (BeanNotFoundException e1) {
			//e1.printStackTrace();
		}

		if (project != null) {
			try {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				this.cprels = dataBean.getDefaultCprels();
                this.inverted = dataBean.getDefaultCprelsInverted();
                displayBean.setInverted(inverted);
				displayBean.setCprels(cprels);
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				DisplayBean sVdisplayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
				this.labelFont = (Font) sVdisplayBean.getLabelFont();
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}


		//remove everything from the display
		displayBean.clear();
		displayBean.getDataDisplayBridge().refresh();

		if (gui.getTopLevelAncestor().isVisible()) {
			populateDisplayBean();
		}

		// change to the old cursor
		gui.setCursor(cursor);
	}

	/** Clears this view */
	public void clear() {
        // TODO implement this... to free up memory
		displayModeChangeListeners.clear();
		doubleClickListeners.clear();
		hideFilteredModeChangeListeners.clear();
		userControls.clear();

		// remove the adapters joining the beans
		//---- FilterBean adapters
		try {
			FilterBean internalFilterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			internalFilterBean.removeFilterChangedListener(hierFilterChangedAdapter);
		} catch (BeanNotFoundException e) {
			// do nothing
		}

		PFlatDisplayBean displayBean = getDisplayBean();
		if (displayBean != null) {
			//---- DisplayBean adapters
			if (shrimpInputAdapter != null) {
				shrimpInputAdapter.clearUserActions();
				displayBean.removeShrimpMouseListener(shrimpInputAdapter);
				displayBean.removeShrimpKeyListener(shrimpInputAdapter);
			}
			if (msama != null) {
				displayBean.removeShrimpMouseListener(msama);
			}
			if (filterRequestAdapter != null) {
				displayBean.removeFilterRequestListener(filterRequestAdapter);
			}
			if (nodeMouseOverAdapter != null) {
				displayBean.addShrimpMouseListener(nodeMouseOverAdapter);
			}
			displayBean.clear();
			displayBean.setEnabled(false);
		}

		//---- SelectorBean adapters
		try {
			SelectorBean selectorBean = (SelectorBean) getBean(SELECTOR_BEAN);
			selectorBean.removePropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, hierSelectedArtifactsChangeAdapter);
		} catch (BeanNotFoundException e) {
		}
	}

	/** Returns whether or not filtered nodes are currently hidden */
	public boolean getHideFilteredMode () {
		return hideFilteredMode;
	}
	/** Returns the current mode for highlighting nodes (ex. "search results" mode) */
	public int getCurrentDisplayMode () {
		return currentDisplayMode;
	}

	/** Creates the GUI components used by this view */
	private void createGUIWidgets(final PFlatDisplayBean displayBean, final SelectorBean selectorBean){
		// Create the menu bar.
		// Edit Menu
		actionManager.addAction(new DefaultShrimpAction(ShrimpConstants.MENU_EDIT), "", "", 1);

		// Node Menu
		actionManager.addAction(new DefaultShrimpAction(ShrimpConstants.MENU_NODE), "", "", 2);

		// Navigate Menu
		actionManager.addAction(new DefaultShrimpAction(ShrimpConstants.MENU_NAVIGATE), "", "", 3);

		// Layout Menu
		actionManager.addAction(new DefaultShrimpAction(ShrimpConstants.MENU_ARRANGE), "", "", 4);

		// Edit -> Select Inverse
		Action action = new AbstractAction (ShrimpConstants.ACTION_NAME_SELECT_INVERSE) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			selectInverse(displayBean, selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_A, 1);

		// Node -> Collapse
		action = new AbstractAction (ShrimpConstants.ACTION_NAME_COLLAPSE) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			collapseSelectedNodes(displayBean, selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 1);
		// Node -> Expand
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_EXPAND) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			expandSelectedNodes(displayBean, selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 2);

		// Node -> Expand All Descendents
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_EXPAND_ALL_DESCENDANTS) {
			public void actionPerformed(ActionEvent e) {
				Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
				if (selectedNodes.size() == 1) {
					Cursor oldCursor = gui.getCursor();
					gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ShrimpNode node = (ShrimpNode) selectedNodes.elementAt(0);
					try {
						displayBean.expandSubTree(node, false, -1);
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						gui.setCursor(oldCursor);
					}
                    updateGUI(displayBean, selectorBean);
				}
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 3);

		// Node -> Expand X Levels
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_EXPAND_X_LEVELS) {
			public void actionPerformed(ActionEvent e) {
				LevelsQueryDialog dialog = new LevelsQueryDialog( ApplicationAccessor.getParentFrame(),
						(Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES), displayBean);
				int levels = dialog.getLevels();
				if(levels == -99) {
					return;
				}

				Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
				if (selectedNodes.size() == 1) {
					Cursor oldCursor = gui.getCursor();
					gui.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
					ShrimpNode node = (ShrimpNode) selectedNodes.elementAt(0);
					try {
						displayBean.expandSubTree (node, true, levels);
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						gui.setCursor (oldCursor);
					}
				}
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_A, 4);

		// Node -> Make Root
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_MAKE_ROOT_NODE) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			makeRootNode(displayBean, selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 1);
		// Node -> Unprune
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_RESTORE_ROOT_NODE) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			restoreOriginalRoot(displayBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 2);

		// Node -> Filter Selected Nodes
		filterSelectedAction = new AbstractAction(ShrimpConstants.ACTION_NAME_HIDE) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			filterSelectedArtifacts(selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(filterSelectedAction, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 1);

		//Node -> Unfilter Selected Nodes
		unfilterSelectedAction = new AbstractAction(ShrimpConstants.ACTION_NAME_UNFILTER) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			unfilterSelectedArtifacts(selectorBean);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(unfilterSelectedAction, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 2);

		// Node -> Unfilter All
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_SHOW_ALL_NODES) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			unfilterAllById();
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 3);


		// Arrange -> Vertical Tree
		action = new AbstractAction(LayoutConstants.LAYOUT_TREE_VERTICAL) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
				displayBean.applyVerticalTreeLayout(true);
				setNewDisplayMode(currentDisplayMode);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 1);

		// Arrange -> Horizontal Tree
		action = new AbstractAction(LayoutConstants.LAYOUT_TREE_HORIZONTAL) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				displayBean.applyHorizontalTreeLayout(true);
				setNewDisplayMode(currentDisplayMode);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 2);

		// Layout -> Radial
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_RADIAL_LAYOUT) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
    			displayBean.applyRadialLayout(true);
				setNewDisplayMode(currentDisplayMode);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 3);

		// Layout -> Orthogonal
		if (OrthogonalLayout.isLoaded()) {
			// Layout -> Hierarchical
			action = new AbstractAction(ShrimpConstants.ACTION_NAME_HIERACHICAL_LAYOUT) {
				public void actionPerformed(ActionEvent e) {
					Cursor oldCursor = gui.getCursor();
					gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    			displayBean.applyHierarchicalLayout(false);
					setNewDisplayMode(currentDisplayMode);
					gui.setCursor(oldCursor);
				}
			};
			actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 5);

			// Layout -> Orthogonal
			action = new AbstractAction(ShrimpConstants.ACTION_NAME_ORTHOGONAL_LAYOUT) {
				public void actionPerformed(ActionEvent e) {
					Cursor oldCursor = gui.getCursor();
					gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	    			displayBean.applyOrthogonalLayout(false);
					setNewDisplayMode(currentDisplayMode);
					gui.setCursor(oldCursor);
				}
			};
			actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 4);
		}

		// @tag Shrimp(sugiyama)
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_SUGIYAMA_LAYOUT) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    			displayBean.applySugiyamaLayout(false);
				setNewDisplayMode(currentDisplayMode);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_ARRANGE, ShrimpConstants.GROUP_A, 6);

		// Tools -> Options -> Label Options
//		action = new AbstractAction (ShrimpConstants.ACTION_NAME_LABEL_OPTIONS) {
//			public void actionPerformed(ActionEvent e) {
//				Cursor oldCursor = gui.getCursor ();
//				gui.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
//				LabelOptionsPanel opsDialog = new LabelOptionsPanel(owner, displayBean, selectorBean);
//				gui.setCursor (oldCursor);
//				opsDialog.setVisible (true);
//			}
//		};
//		actionManager.addAction(action, "Options", ShrimpConstants.GROUP_A, 1);

		// Navigate -> Zoom Extents
		action = new AbstractAction(ShrimpConstants.ACTION_NAME_HOME , ResourceHandler.getIcon("icon_home.gif")) {
			public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
				displayBean.focusOnExtents (true);
				gui.setCursor(oldCursor);
			}
		};
		actionManager.addAction(action, ShrimpConstants.MENU_NAVIGATE, "", 1);

		actionManager.addActionManagerListener(new ActionManagerListener () {
			public void actionsAdded(ActionsAddedEvent event) {
				recreateMenuBar ();
			}

			public void actionsRemoved(ActionsRemovedEvent event) {
				recreateMenuBar ();
			}

			public void actionsModified(ActionsModifiedEvent event) {
				recreateMenuBar ();
			}
		});

		JToolBar toolBar = new JToolBar();

		action = actionManager.getAction(ShrimpConstants.ACTION_NAME_HOME, ShrimpConstants.MENU_NAVIGATE);
		if(action != null) {
			JButton button = new JButton(action);
			button.setText(null);
			button.setToolTipText("Focus on home");
			button.setMargin(new Insets(0,0,0,0));
			toolBar.add(button);
		}
		toolBar.add(new JToolBar.Separator(new Dimension (10, 10)));
		action = actionManager.getAction(ShrimpConstants.ACTION_NAME_EXPAND_ALL_DESCENDANTS, ShrimpConstants.MENU_NODE);
		if(action != null) {
			JButton button = new JButton(action);
			button.setText(ShrimpConstants.ACTION_NAME_EXPAND_ALL_DESCENDANTS);
			button.setToolTipText("Expand all descendents of the currently selected node(s)");
			button.setMargin(new Insets(0,0,0,0));
			toolBar.add(button);
		}

		gui.getContentPane().add(toolBar, BorderLayout.NORTH);

		JPanel modePanel = new JPanel (new BorderLayout());
		JPanel rbtnsPanel = new JPanel (new GridBagLayout());
		modeLbl.setFont(new Font (modeLbl.getFont().getName(), Font.BOLD, modeLbl.getFont().getSize()));
		modePanel.add(modeLbl, BorderLayout.NORTH);
		rbtnsPanel.add(rbtnNavigation);
		rbtnsPanel.add(rbtnFilter);
		rbtnsPanel.add(rbtnSearch);
		modePanel.add(rbtnsPanel, BorderLayout.CENTER);

		basePanel.setLayout(new GridLayout(2,1,5,5));
		basePanel.add(modePanel);
//		basePanel.add(chkHideFiltered);

		//put radio button in a group
		ButtonGroup displayModeGrp = new ButtonGroup();
		displayModeGrp.add(rbtnNavigation);
		displayModeGrp.add(rbtnFilter);
		displayModeGrp.add(rbtnSearch);
		rbtnNavigation.setSelected(true);

//		ButtonGroup accentuateModeGrp = new ButtonGroup ();
//		accentuateModeGrp.add(colorRBtn);
//		accentuateModeGrp.add(sizeRBtn);

		displayBean.getPCanvas().setPreferredSize(new Dimension(300,300));

        Container contentPane = gui.getContentPane();
        pScrollPane = new PScrollPane (displayBean.getPCanvas());
        contentPane.add(pScrollPane, BorderLayout.CENTER);
        contentPane.add(basePanel, BorderLayout.SOUTH);

        createPopupMenu(displayBean, selectorBean);
		createGUIListeners(displayBean, selectorBean);
		recreateMenuBar();
	}

	/** Creates and adds listeners to the GUI widgets in this view */
	private void createGUIListeners(final PFlatDisplayBean displayBean, final SelectorBean selectorBean) {
		rbtnNavigation.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				setNewDisplayMode(NAVIGATION_MODE);
				gui.setCursor(oldCursor);
    		}
		});

		rbtnFilter.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				setNewDisplayMode(FILTERED_MODE);
				gui.setCursor(oldCursor);
    		}
		});

		rbtnSearch.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				setNewDisplayMode(SEARCH_MODE);
				gui.setCursor(oldCursor);
    		}
		});

		displayBean.getPCanvas().addMouseListener (new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		    	Vector selectedArts = new Vector();
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
		    	if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					selectedArts = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
					if (selectedArts.size() == 1) {
						fireNodeDoubleClickEvent((ShrimpNode)selectedArts.firstElement());
					}
		    	}
				gui.setCursor(oldCursor);
		    }

		    public void mouseReleased(MouseEvent e) {
				Cursor oldCursor = gui.getCursor();
				gui.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	updateGUI();
				gui.setCursor(oldCursor);
		    }
		});

	}

	private void createPopupMenu(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
		popupMenu = new JPopupMenu();
		popupListener = new HvPopupListener(popupMenu, this, selectorBean);
		displayBean.getPCanvas().addMouseListener(popupListener);
		//add a action manager listener so that the popup can repopulated if the menus change
		actionManagerListener = new ActionManagerListener () {
			public void actionsAdded(ActionsAddedEvent event) {
				popupListener.repopulatePopupMenu();
			}
			public void actionsRemoved(ActionsRemovedEvent event) {
				popupListener.repopulatePopupMenu();
			}
			public void actionsModified(ActionsModifiedEvent event) {
				popupListener.repopulatePopupMenu();
			}
		};
		actionManager.addActionManagerListener(actionManagerListener);
	}

	private void updateGUI() {
		try {
			updateGUI(getDisplayBean(), (SelectorBean) getBean(SELECTOR_BEAN));
		} catch (BeanNotFoundException e) {
		}
	}

	/**
	 * Enable/disable Collapse, Expand, Prune, and Unprune buttons.
	 *
	 * Can prune if one node selected
	 * and tree is not pruned already
	 * and node not collapsed
	 *
	 * Can unprune if tree has been pruned
     * Can collapse only if all selected nodes are not collapsed,
	 * and are all siblings,
	 * and have at least one child,
	 * and none of them is the prune parent.
	 *
	 * Can expand only if all selected nodes are collapsed.
	 * Can expand all if at least one node collapsed
	 * Can unfilter selected if all selected are filtered by id
	 * Can filter selected if all selected are unfiltered
	 */
	private void updateGUI(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
		Vector collapseParents = displayBean.getCollapsedNodes();
		boolean canExpand;
		boolean canExpandAllDescendents;
        boolean canExpandXLevels;
		boolean canCollapse;
		boolean canPrune;
		ShrimpNode pruneParent = displayBean.getPrunedToNode();
		boolean canUnprune  = pruneParent != null;
		boolean canFilter = true;
		boolean canUnfilter = true;

		Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);

		if (selectedNodes == null || selectedNodes.size() == 0) {
			canExpand = false;
			canExpandAllDescendents = false;
            canExpandXLevels = false;
			canCollapse = false;
			canPrune = false;
			canFilter = false;
			canUnfilter = false;
		} else if (selectedNodes.size()==1) {
			canExpandAllDescendents = true;
			canExpandXLevels = true;
			ShrimpNode node = (ShrimpNode) selectedNodes.firstElement();
			Vector children = displayBean.getDataDisplayBridge().getChildNodes(node);
			canPrune = pruneParent==null && !collapseParents.contains(node);
			canCollapse = !collapseParents.contains(node) &&
							!children.isEmpty() &&
							(pruneParent==null || (pruneParent!=null && pruneParent != node));
			canExpand = collapseParents.contains(node);

			boolean idFilterExistsForThis = isFilteredByID(node);
			canFilter = !idFilterExistsForThis;
			canUnfilter = idFilterExistsForThis && !displayBean.getDataDisplayBridge().isAncestorFiltered(node.getArtifact());
		} else {
			canPrune = false;
			canCollapse = true;
			canExpand = true;
			canExpandAllDescendents = true;
            canExpandXLevels = true;
			Vector siblings = null;
			boolean isSibling;
			boolean idFiltersExistForAll = true;
			boolean idFiltersExistForNone = true;

			for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
				ShrimpNode node = (ShrimpNode) iter.next();
				if (siblings == null) {
					siblings = node.getArtifact().getSiblings(cprels);
					isSibling = true; //first node is a sibling of itself
				} else {
					isSibling = siblings.contains(node.getArtifact());
				}
				Vector children = displayBean.getDataDisplayBridge().getChildNodes(node);
				canCollapse = 	canCollapse &&
								!collapseParents.contains(node) &&
								isSibling &&
								!children.isEmpty() &&
								(pruneParent==null || (pruneParent!=null && pruneParent != node));
				canExpand = canExpand && collapseParents.contains(node);

				// look for any id filters on this artifact
				boolean idFilterExistsForThis = isFilteredByID(node);

				idFiltersExistForAll = idFiltersExistForAll && idFilterExistsForThis;
				idFiltersExistForNone = idFiltersExistForNone && !idFilterExistsForThis;
			}

			canFilter = idFiltersExistForNone;
			canUnfilter = idFiltersExistForAll;
		}

		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_EXPAND, ShrimpConstants.MENU_NODE, canExpand);
        actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_EXPAND_ALL_DESCENDANTS, ShrimpConstants.MENU_NODE, canExpandAllDescendents);
        actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_EXPAND_X_LEVELS, ShrimpConstants.MENU_NODE, canExpandXLevels);
		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_COLLAPSE, ShrimpConstants.MENU_NODE, canCollapse);
		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_MAKE_ROOT_NODE, ShrimpConstants.MENU_NODE, canPrune);
		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_RESTORE_ROOT_NODE, ShrimpConstants.MENU_NODE, canUnprune);
		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_HIDE, ShrimpConstants.MENU_EDIT, canFilter);
		actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_UNFILTER, ShrimpConstants.MENU_EDIT, canUnfilter);
	}

	private boolean isFilteredByID(ShrimpNode node) {
		// look for any id filters on this artifact
		boolean idFilterExistsForThis = false;
		if (externalFilterBean != null) {
			idFilterExistsForThis = externalFilterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Object.class, FilterConstants.ARTIFACT_FILTER_TYPE, new Long(node.getArtifact().getID()));
		}
		return idFilterExistsForThis;
	}

	/**
	 * Remove all filters from hierarchical filter bean.
	 */
	public void removeAllArtifactFilters() {
		try {
			FilterBean internalFilterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			Vector allFilters = (Vector) internalFilterBean.getFilters().clone();
			Vector artifactFilters = new Vector();
			for (Iterator iter = allFilters.iterator(); iter.hasNext();) {
				Filter filter = (Filter) iter.next();
				if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
					artifactFilters.add(filter);
				}
			}
			try {
				internalFilterBean.removeFilters (artifactFilters);
			} catch (FilterNotFoundException ex) {}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Add an id filter to the shrimp view for all selected nodes
	 */
	private void filterSelectedArtifacts(SelectorBean selectorBean) {
		Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		Set idsToFilter = new HashSet ();
		for (int i = 0; i < selectedNodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)selectedNodes.elementAt(i);
			idsToFilter.add(new Long(node.getArtifact().getID()));
		}
		if (externalFilterBean != null) {
			externalFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Object.class, FilterConstants.ARTIFACT_FILTER_TYPE, idsToFilter, true);
		}
		updateGUI();
	}

	/**
	 *  Attempt to remove id filter from shrimp view for all selected nodes.
	 */
	private void unfilterSelectedArtifacts(SelectorBean selectorBean) {
		Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		Set idsToFilter = new HashSet ();
		for (int i = 0; i < selectedNodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)selectedNodes.elementAt(i);
			idsToFilter.add(new Long(node.getArtifact().getID()));
		}
		if (externalFilterBean != null) {
			externalFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Object.class, FilterConstants.ARTIFACT_FILTER_TYPE, idsToFilter, false);
		}

		updateGUI();
	}

	/**
	 * Attempt to remove id filters from any listening filter beans
	 */
	private void unfilterAllById() {
		if (externalFilterBean != null) {
			externalFilterBean.removeNominalAttrFilter(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Object.class, FilterConstants.ARTIFACT_FILTER_TYPE);
		}
		updateGUI();
	}

	/**
	 *  Selects the inverse of the currently selected nodes.
	 */
	private void selectInverse(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
		Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		Vector allNodes = displayBean.getVisibleNodes();
		Vector inverseNodes = new Vector ();
		for (int i = 0; i < allNodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)allNodes.elementAt(i);
			if (!selectedNodes.contains(node)) {
				inverseNodes.add(node);
			}
		}
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, inverseNodes);
		updateGUI(displayBean, selectorBean);
	}

	/**
	 *  Collapse currently selected nodes.
	 */
	private void collapseSelectedNodes(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
	    Vector selectedNodes = (Vector)((Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES)).clone();
	    displayBean.collapseNodes(selectedNodes);
		updateGUI(displayBean, selectorBean);
	}

	/**
	 *  Expand currently selected nodes.
	 */
	private void expandSelectedNodes(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
		Vector selectedNodes = (Vector)((Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES)).clone();
		displayBean.expandNodes(selectedNodes);
		updateGUI(displayBean, selectorBean);
	}

	/**
	 * Prune the tree to the currently selected node.
	 * Hide every node except the selected one and its descendents
	 */
	private void makeRootNode(PFlatDisplayBean displayBean, SelectorBean selectorBean) {
	    Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
	    if (selectedNodes == null || selectedNodes.size()!=1) {
			return;
		}
		ShrimpNode current = (ShrimpNode) selectedNodes.firstElement();
	    displayBean.makeRootNode(current);
		updateGUI(displayBean, selectorBean);
	}

	/**
	 * Show all artifacts that have been pruned, if not collapsed.
	 */
	private void restoreOriginalRoot(PFlatDisplayBean displayBean) {
		displayBean.restoreOriginalRoot();
		updateGUI();
	}

	/**
	 * Sets the display mode (ex. search results mode)
	 */
	private void setNewDisplayMode(int newDisplayMode) {
		fireDisplayModeChangeEvent(currentDisplayMode, newDisplayMode);
	   	currentDisplayMode = newDisplayMode;
	   	updateGUI();
	}


	// *************** Adapter Classes **************************************************

	/** Listens for changes to the selected artifacts in this hierarchical view. */
	private class HierSelectedArtifactsChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			Object oldSelected = evt.getOldValue();
			getDisplayBean().highlight(oldSelected, false);
			Object newSelected = evt.getNewValue();
			getDisplayBean().highlight(newSelected, true);
		}
	}

	/** Accentuate the node that the mouse is currently over */
	private class NodeMouseOverAdapter extends ShrimpMouseAdapter {
		// keep track of hovered on node
		ShrimpNode currentTarget = null;

		/**
		 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseMoved(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
		 */
		public void mouseMoved(ShrimpMouseEvent e) {
			Object object = e.getTarget();
			PFlatDisplayBean displayBean = getDisplayBean();
			if (!(object instanceof ShrimpNode) || object == null) {
				// not over a node
				if (currentTarget != null) {
					displayBean.accentuateMouseOver (currentTarget, false);
					currentTarget = null;
				}
			} else {
				// back on an artifact, check if it a different target
				ShrimpNode newTarget = (ShrimpNode) object;
				if (currentTarget == null) {
					displayBean.accentuateMouseOver (newTarget, true);
					currentTarget = newTarget;
				} else if (!newTarget.equals(currentTarget)) {
					displayBean.accentuateMouseOver(currentTarget, false);
					displayBean.accentuateMouseOver(newTarget, true);
					currentTarget = newTarget;
				}
			}
		}
	}

	/** Listeners for double-clicking of a node */
	private Vector doubleClickListeners = new Vector();

	/** Adds a listeners for double-clicking of a node */
	public void addNodeDoubleClickListener(NodeDoubleClickListener ndcl) {
		if (!doubleClickListeners.contains(ndcl)) {
			doubleClickListeners.add(ndcl);
		}
	}

	/** Removes a listeners for double-clicking of a node */
	public void removeNodeDoubleClickListener(NodeDoubleClickListener ndcl) {
		if (doubleClickListeners.contains(ndcl)) {
			doubleClickListeners.remove(ndcl);
		}
	}

	/** Informs all node double-click listeners that a node has been double clicked. */
	protected void fireNodeDoubleClickEvent(ShrimpNode node) {
		for (int i = 0; i < doubleClickListeners.size(); i++) {
			NodeDoubleClickListener ndcl = (NodeDoubleClickListener) doubleClickListeners.elementAt(i);
			ndcl.doubleClick(node);
		}
	}

	private Vector hideFilteredModeChangeListeners = new Vector();

	public void addHideFilteredModeChangeListener(HideFilteredModeChangeListener hfmcl) {
		if (!hideFilteredModeChangeListeners.contains(hfmcl)) {
			hideFilteredModeChangeListeners.add(hfmcl);
		}
	}

	public void removeHideFilteredModeChangeListener(HideFilteredModeChangeListener hfmcl) {
		if (hideFilteredModeChangeListeners.contains(hfmcl)) {
			hideFilteredModeChangeListeners.remove(hfmcl);
		}
	}

	protected void fireHideFilteredModeChangeEvent(boolean hideFiltered) {
		for (int i = 0; i < hideFilteredModeChangeListeners.size(); i++) {
			HideFilteredModeChangeListener hfmcl = (HideFilteredModeChangeListener) hideFilteredModeChangeListeners.elementAt(i);
			hfmcl.hideFilteredModeChange(hideFiltered);
		}
	}

	public void registerExternalFilterBean(FilterBean externalFilterBean) {
		this.externalFilterBean = externalFilterBean;
	}

	public boolean unregisterExternalFilterBean(FilterBean externalFilterBean) {
		boolean unregistered = false;
		if (this.externalFilterBean != null && (externalFilterBean == null || this.externalFilterBean.equals(externalFilterBean))) {
			this.externalFilterBean = null;
			unregistered = true;
		}
		return unregistered;
	}

	public void addDisplayModeChangeListener(DisplayModeChangeListener dmcl) {
		if (!displayModeChangeListeners.contains(dmcl)) {
			displayModeChangeListeners.add(dmcl);
		}
	}

	public void removeDisplayModeChangeListener(DisplayModeChangeListener dmcl) {
		if (displayModeChangeListeners.contains(dmcl)) {
			displayModeChangeListeners.remove(dmcl);
		}
	}

	protected void fireDisplayModeChangeEvent(int oldMode, int newMode) {
		for (int i = 0; i < displayModeChangeListeners.size(); i++) {
			DisplayModeChangeListener dmcl = (DisplayModeChangeListener) displayModeChangeListeners.elementAt(i);
			dmcl.displayModeChange(oldMode, newMode);
		}
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ViewTool#navigateToObject(java.lang.Object)
	 */
	public void navigateToObject(Object destObject) {
		try {
			DisplayBean displayBean = (DisplayBean) getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.focusOn(destObject);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ViewTool#getMouseMode()
	 */
	public String getMouseMode() {
		return DisplayConstants.MOUSE_MODE_SELECT;
	}

}