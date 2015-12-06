/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umd.cs.piccolox.swing.PScrollPane;

import org.eclipse.mylar.zest.layouts.algorithms.GridLayoutAlgorithm;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.ui.UIHelper;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.layout.ForceDirectedLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HierarchicalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.HorizontalTreeLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.MotionLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.OrthogonalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.RadialLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SequenceLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.ShrimpGridLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.ShrimpSpringLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SugiyamaLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.VerticalTreeLayout;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SearchBean.SearchResult;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractViewTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.DisplayFilterChangedAdapter;
import ca.uvic.csr.shrimp.adapter.DisplayFilterRequestAdapter;
import ca.uvic.csr.shrimp.adapter.SelectedArcsChangeAdapter;
import ca.uvic.csr.shrimp.adapter.SelectedNodesChangeAdapter;
import ca.uvic.csr.shrimp.adapter.ShrimpInputAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.MouseHighlightArcAdapter;
import ca.uvic.csr.shrimp.adapter.mouse.MouseSelectAndMoveAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenArcFilterAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenNodeFilterAdapter;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManagerListener;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsAddedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsModifiedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsRemovedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.jambalaya.JambalayaApplication;
import ca.uvic.csr.shrimp.jambalaya.JambalayaProject;
import ca.uvic.csr.shrimp.jambalaya.JambalayaView;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserEvent;
import ca.uvic.csr.shrimp.usercontrols.FocusOnHomeAdapter;
import ca.uvic.csr.shrimp.usercontrols.LayoutModeChangeAdapter;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.UserEvent;
import ca.uvic.csr.shrimp.util.CollapsiblePanel;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.NodeNameComparator;
import ca.uvic.csr.shrimp.util.PopupListener;

/**
 * The intention of the QueryView is to provide a more bottom-up approach to showing a graph. The
 * idea is that you start with one or more nodes of interest and then show the surrounding
 * neighbourhood. Finding the nodes of interest and their surrounding neighbours can be thought of
 * as "querying" the data by specifying parameters for a query.
 *
 * The QueryView depends on a {@link ShrimpProject} - it is only ever associated with one project.
 * If you want to query another project, either create a new QueryView or call
 * {@link QueryView#setProject(ShrimpProject)} to change to the new project.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class QueryView extends AbstractViewTool implements ViewTool {

	private static final int DEFAULT_SEPARATOR_SIZE = 15;

	// top level components
	private JRootPane rootPane = new JRootPane();
	private boolean createdGUI = false;

	private boolean showQueryPanel = true;
	private QueryHelper queryHelper;
	private QueryPanelComponent queryPanel;

	// query view components
	private JToolBar toolBar;
	private List toolbarActions;
	private JPopupMenu nodePopupMenu;
	private QueryViewNodePopupListener nodePopupListener;
	private ActionManagerListener nodeActionManagerListener;
	private PScrollPane pScrollPane;
	private JPanel pnlCanvasAndToolBar;
	private Vector menuActions;

	private boolean doNotShowProgressDialog;

	public QueryView(ShrimpProject project) {
		this(project, true);
	}

	/**
	 * @param showQueryPanel If the query panel should be shown.
	 */
	public QueryView(ShrimpProject project, boolean showQueryPanel) {
		super(ShrimpProject.QUERY_VIEW, project);
		this.project = project;
		this.queryHelper = new QueryHelper(this);
		this.queryHelper.setChangeTransparency(true);
		this.showQueryPanel = showQueryPanel;
		this.doNotShowProgressDialog = false;	// show the progress dialog by default

		actionManager = new ActionManager();
		toolbarActions = new ArrayList();
		menuActions = new Vector();

		GradientPanel contentPane = new GradientPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		rootPane.setContentPane(contentPane);

		// add the listeners
		this.setProject(project);
	}

	private void createBeans(ShrimpProject project) {
		DataDisplayBridge dataDisplayBridge = new DataDisplayBridge(this);
		PNestedDisplayBean displayBean = new PNestedDisplayBean(project, new String[0], dataDisplayBridge);
		displayBean.getPCanvas().addMouseListener(nodePopupListener);
		displayBean.setDefaultLabelMode(DisplayConstants.LABEL_MODE_FIT_TO_NODE);
		addBean(ShrimpTool.DISPLAY_BEAN, displayBean);

		SelectorBean selectorBean = new SelectorBean();
		addBean(ShrimpTool.SELECTOR_BEAN, selectorBean);

		FilterBean filterBean = new FilterBean();
		addBean(ShrimpTool.DISPLAY_FILTER_BEAN, filterBean);

		// the query panel needs to listen for filter changes to update it's node and arc types list
		if (queryPanel != null) {
			filterBean.addFilterChangedListener(queryPanel);
		}
	}

	private void removeBeans() {
		removeBean(ShrimpTool.DISPLAY_BEAN);
		removeBean(ShrimpTool.SELECTOR_BEAN);
		removeBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	}

	/**
	 * This method returns the project associated with the QueryView.
	 */
	public ShrimpProject getProject() {
		return project;
	}

	public void setProject(ShrimpProject newProject) {
		// clear the artifact types, relationship types, and source artifacts
		this.queryHelper.clear();
		super.setProject(newProject); // calls refresh()
		repopulateNodePopupMenu();
	}

	protected void setQueryHelper(QueryHelper queryHelper) {
		this.queryHelper = queryHelper;
	}

	public QueryHelper getQueryHelper() {
		return queryHelper;
	}

	public void clear() {
		ApplicationAccessor.waitCursor();
		try {
			queryHelper.clearMatchingNodes();
			// TODO not sure if this is necessary?
			//queryHelper.clear();

			//clear the selector bean
			SelectorBean selectorBean = getSelectorBean();
			if (selectorBean != null) {
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector());
				selectorBean.clearSelected(SelectorBeanConstants.TARGET_OBJECT);
			}

			//remove everything from the display and data-display bridge
			DisplayBean displayBean = getDisplayBean();
			if (displayBean != null) {
				displayBean.clear();
				displayBean.getDataDisplayBridge().refresh();
			}
		} finally {
			// change to the old cursor
			ApplicationAccessor.defaultCursor();
		}
	}

	private void init() {
		//---- DisplayBean init
		DisplayBean displayBean = getDisplayBean();
		displayBean.setLabelBackgroundOpaque(false);
		//Font defaultFont = displayBean.getLabelFont();
		//Font newFont = defaultFont.deriveFont(Font.BOLD);
		//displayBean.setLabelFont(newFont);

		Layout layout = new ShrimpSpringLayout(displayBean);
		displayBean.addLayout(layout);

		layout = new ShrimpGridLayout(displayBean, NodeNameComparator.NODE_NAME_COMPARATOR, LayoutConstants.LAYOUT_GRID_BY_ALPHA, new GridLayoutAlgorithm());
		displayBean.addLayout(layout);

		layout = new VerticalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_VERTICAL, false);
		displayBean.addLayout(layout);
		layout = new HorizontalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_HORIZONTAL, false);
		displayBean.addLayout(layout);
		layout = new VerticalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_VERTICAL_INVERTED, true);
		displayBean.addLayout(layout);
		layout = new HorizontalTreeLayout(displayBean, LayoutConstants.LAYOUT_TREE_HORIZONTAL_INVERTED, true);
		displayBean.addLayout(layout);
		layout = new RadialLayout(displayBean, LayoutConstants.LAYOUT_RADIAL, false);
		displayBean.addLayout(layout);
		layout = new RadialLayout(displayBean, LayoutConstants.LAYOUT_RADIAL_INVERTED, true);
		displayBean.addLayout(layout);

		if (OrthogonalLayout.isLoaded()) {
			layout = new HierarchicalLayout(displayBean, LayoutConstants.LAYOUT_HIERARCHICAL);
			displayBean.addLayout(layout);

			layout = new OrthogonalLayout(displayBean, LayoutConstants.LAYOUT_ORTHOGONAL);
			displayBean.addLayout(layout);
		}

		//@tag Shrimp.sequence
		try {
			layout = new SequenceLayout(displayBean, LayoutConstants.LAYOUT_SEQUENCE);
			displayBean.addLayout(layout);
		} catch (NoClassDefFoundError ignore) {
		}

		//@tag Shrimp.sugiyama
		if (SugiyamaLayout.isInstalled()) {
			layout = new SugiyamaLayout(displayBean, LayoutConstants.LAYOUT_SUGIYAMA);
			displayBean.addLayout(layout);
		}

		// make sure prefuse_force.jar is in the classpath
		if (ForceDirectedLayout.isPrefuseInstalled()) {
			layout = new ForceDirectedLayout(displayBean);
			displayBean.addLayout(layout);
		}

		layout = new MotionLayout(displayBean, LayoutConstants.LAYOUT_MOTION);
		displayBean.addLayout(layout);

		ShrimpInputAdapter shrimpInputAdapter = new ShrimpInputAdapter(this);

		displayBean.addShrimpMouseListener(shrimpInputAdapter);
		displayBean.addShrimpKeyListener(shrimpInputAdapter);

		addUserEvents(shrimpInputAdapter);

		MouseSelectAndMoveAdapter mouseSelectAndMoveAdapter = new MouseSelectAndMoveAdapter(this);
		displayBean.addShrimpMouseListener(mouseSelectAndMoveAdapter);
		displayBean.addShrimpKeyListener(mouseSelectAndMoveAdapter);

		MouseHighlightArcAdapter mouseHighlightArcAdapter = new MouseHighlightArcAdapter(this);
		displayBean.addShrimpMouseListener(mouseHighlightArcAdapter);

		DisplayFilterRequestAdapter filterRequestAdapter = new DisplayFilterRequestAdapter(this);
		displayBean.addFilterRequestListener(filterRequestAdapter);

		DisplayFilterChangedAdapter filterChangedAdapter = new DisplayFilterChangedAdapter(this);
		getFilterBean().addFilterChangedListener(filterChangedAdapter);

		//---- SelectorBean init
		SelectorBean selectorBean = getSelectorBean();
		SelectedNodesChangeAdapter selectedNodesChangeAdapter = new SelectedNodesChangeAdapter(this, actionManager);
		SelectedArcsChangeAdapter selectedArcsChangeAdapter = new SelectedArcsChangeAdapter(this, actionManager);
		selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, selectedNodesChangeAdapter);
		selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_ARCS, selectedArcsChangeAdapter);
		selectorBean.setSelected(DisplayConstants.ZOOM_MODE, DisplayConstants.MAGNIFY);
	}

	private void addUserEvents(ShrimpInputAdapter shrimpInputAdapter) {
		// Add user controls for Expand/Collapse (DBLCLICK)
        ExpandAction expandAction = (ExpandAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_EXPAND, ShrimpConstants.MENU_NODE);
        CollapseAction collapseAction = (CollapseAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_COLLAPSE, ShrimpConstants.MENU_NODE);
        UserAction action = new ExpandCollapseAction(expandAction, collapseAction, this);
        UserEvent userEvent = new DefaultUserEvent(action);
        userEvent.setCommand(true, UserEvent.DOUBLE_CLICK__LEFT_MOUSE_BUTTON, false, false, false);
        Vector userEvents = new Vector();
        userEvents.add(userEvent);
        action.setUserEvents(userEvents);
        shrimpInputAdapter.addUserAction(action);

        // Add user controls for Hide (DEL)
        action = (UserAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_HIDE, ShrimpConstants.MENU_NODE);
        if (action == null) {
        	action = new HideAction(this);
        }
        userEvent = new DefaultUserEvent(action);
        userEvent.setCommand(false, KeyEvent.VK_DELETE, false, false, false);
        userEvents = new Vector();
        userEvents.add(userEvent);
        action.setUserEvents(userEvents);
        shrimpInputAdapter.addUserAction(action);

        // Add user controls for Focus on Home (MIDDLE MOUSE BUTTON)
        action = new FocusOnHomeAdapter(this);
        userEvent = new DefaultUserEvent(action);
        userEvent.setCommand(true, UserEvent.MIDDLE_MOUSE_BUTTON, false, false, false);
        userEvents = new Vector();
        userEvents.add(userEvent);
        action.setUserEvents(userEvents);
        shrimpInputAdapter.addUserAction(action);
	}

    /*
     * (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getGUI()
     */
    public Component getGUI() {
        return rootPane;
    }

    /*
     * (non-Javadoc)
     * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#disposeTool()
     */
    public void disposeTool() {
    	clear();
    	removeQueryViewActions();
    	removeNodePopupMenu();
    	removeBeans();

		queryHelper.dispose();
		if (queryPanel != null) {
			queryPanel.dispose();
			queryPanel = null;
		}
		rootPane.getContentPane().removeAll();
		createdGUI = false;
		pnlCanvasAndToolBar = null;
		pScrollPane = null;
	}

	/*
	 * (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		if (getProject() != null) {
			if (!createdGUI) {
				createGUI();
				createdGUI = true;
			}
			refreshBeans();
			updateGUI();
		} else {
			rootPane.getContentPane().removeAll();
			//JOptionPane.showMessageDialog(null, "No project - can't refresh the QueryView.");
		}
	}

	private void refreshBeans() {
		// create the beans if they haven't been created yet
		if (!hasBean(DISPLAY_BEAN)) {
			createBeans(getProject());
			init();

			if (pScrollPane != null) {
				pScrollPane.getParent().remove(pScrollPane);
				pScrollPane.removeAll();
				pScrollPane = null;
			}
		}

		// remove all relationships type filters
		getFilterBean().removeNominalAttrFilter(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE);

		SelectorBean selectorBean = getSelectorBean();
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector(0));
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector(0));
		selectorBean.clearSelected(SelectorBeanConstants.TARGET_OBJECT);

		DisplayBean displayBean = getDisplayBean();
		DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
		displayBean.clear();
		dataDisplayBridge.refresh();
		queryHelper.clearMatchingNodes();
	}

	/**
	 * Runs the query and sets the query results to the output label.
	 * @param animate
	 */
	protected void doQuery(boolean animate) {
		SearchResult result = queryHelper.doQuery(animate);
		String text = result.toString() + " ";
		Color color = result.isError() ? Color.red :
			(result.isCancelled() || !result.hasResults() ? Color.magenta : OUTPUT_COLOR);
		setOutputText(text, color);
	}

	/**
	 * Runs a simple query with the default options. Must call init() and refresh() before calling
	 * this method.
	 * @param artifactName the artifact name to search for
	 * @param animate if animation should be used to layout the graph
	 */
	public void query(String artifactName, boolean animate) {
		if (artifactName == null) {
			artifactName = "";
		}
		queryHelper.setSrcArtifactName(artifactName);
		// ensure the query panel is up to date
		if (queryPanel != null) {
			queryPanel.updateSrcArtifactName();
			//queryPanel.addSearchItem(artifactName);
			queryPanel.focusSearchBox();
		}
		doQuery(animate);
	}

	/**
	 * Runs the query on the selected artifacts.
	 * @param animate if animation should be used
	 */
	public void querySelected(boolean animate) {
		doQuery(animate);
	}

	/**
	 * Returns a {@link Vector} of all the visible {@link ShrimpNode} objects.
	 */
	public Vector getAllNodes() {
		Vector nodes = new Vector(0);
		DisplayBean displayBean = getDisplayBean();
		if (displayBean != null) {
			nodes = displayBean.getAllNodes();
		}
		return nodes;
	}

	/**
	 * Returns a {@link Vector} of the selected {@link ShrimpNode} objects.
	 */
	public Vector getSelectedNodes() {
		Vector nodes = new Vector(0);
		SelectorBean selectorBean = getSelectorBean();
		if (selectorBean != null) {
			nodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		}
		return nodes;
	}

	/**
	 * Returns the selected node or null if no node is selected.
	 */
	public ShrimpNode getSelectedNode() {
		ShrimpNode node = null;
		SelectorBean selectorBean = getSelectorBean();
		if (selectorBean != null) {
			Object target = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
			if (target instanceof ShrimpNode) {
				node = (ShrimpNode) target;
			}
		}
		return node;
	}

	public void addSelectionPropertyChangeListener(PropertyChangeListener listener) {
		SelectorBean selectorBean = getSelectorBean();
		if (selectorBean != null) {
			selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, listener);
		}
	}

	public void removeSelectionPropertyChangeListener(PropertyChangeListener listener) {
		SelectorBean selectorBean = getSelectorBean();
		if (selectorBean != null) {
			selectorBean.removePropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, listener);
		}
	}

	/**
	 * Updates the GUI with the relationship and artifact types for the active project.
	 */
	private void updateGUI() {
		if (pScrollPane == null) {
			pScrollPane = new PScrollPane(getDisplayBean().getPCanvas());
			pnlCanvasAndToolBar.add(pScrollPane, BorderLayout.CENTER);
		}

		Vector relTypes = new Vector(0);
		Vector artTypes = new Vector(0);
		TreeSet artifactNames = new TreeSet(Collator.getInstance());
		DataBean dataBean = getDataBean();
		if (dataBean != null) {
			// get ALL rel and arc types - even filtered ones
			relTypes = getRelationshipTypes(true);
			artTypes = getArtifactTypes(true);
			Vector artifacts = dataBean.getArtifacts(true);
			for (int i = 0; i < artifacts.size(); i++) {
				Artifact artifact = (Artifact) artifacts.get(i);
				artifactNames.add(artifact.getName());
			}
		}
		queryHelper.setRelationshipTypes(relTypes);
		queryHelper.setArtifactTypes(artTypes);

		if (queryPanel != null) {
			queryPanel.updateArtifactTypes();
			queryPanel.updateRelationshipTypes();
			queryPanel.setSearchItems(artifactNames, true);
			// make the query panel a good size in height
			queryPanel.adjustHeight();
		}

		rootPane.validate();
	}

	protected Vector getArtifactTypes(boolean includeFiltered) {
		DataBean dataBean = getDataBean();
		return (dataBean != null ? dataBean.getArtifactTypes(includeFiltered, true) : new Vector(0));
	}

	protected Vector getRelationshipTypes(boolean includeFiltered) {
		DataBean dataBean = getDataBean();
		return (dataBean != null ? dataBean.getRelationshipTypes(includeFiltered, true) : new Vector(0));
	}

	private void createGUI() {
		createActions();
		createToolBar();
		createNodePopupMenu();

		pnlCanvasAndToolBar = new JPanel(new BorderLayout());
		pnlCanvasAndToolBar.add(toolBar, BorderLayout.NORTH);
		rootPane.getContentPane().add(pnlCanvasAndToolBar, BorderLayout.CENTER);

		if (showQueryPanel) {
			queryPanel = new QueryPanelComponent(this);
			DataBean dataBean = getDataBean();
			// @tag Shrimp.QueryView.Protege : add select cls and instance buttons
			if (dataBean instanceof ProtegeDataBean) {
				addProtegeSearchButtons((ProtegeDataBean) dataBean);
			}
			CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Query View", queryPanel);
			rootPane.getContentPane().add(collapsiblePanel, BorderLayout.NORTH);
		}

		rootPane.invalidate();
	}

	/**
	 * Adds two buttons to the QueryPanelComponent - one which displays a dialog showing the
	 * SelectClsesPanel and one showing the SelectInstancesPanel. When the user chooses a cls or
	 * instance the name is put into the textfield.
	 *
	 * @param dataBean
	 */
	private void addProtegeSearchButtons(final ProtegeDataBean dataBean) {
		JToolBar toolbar = new JToolBar();
		toolbar.setPreferredSize(new Dimension(50, 24));
		toolbar.setRollover(true);
		toolbar.setOpaque(false);
		toolbar.setFloatable(false);

		JButton btn = new JButton(null, ResourceHandler.getIcon("icon_protege_class_add.gif"));
		btn.setToolTipText("Select a class");
		btn.setOpaque(false);
		Dimension dim = new Dimension(20, 20);
		btn.setPreferredSize(dim);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JambalayaView view = JambalayaView.globalInstance();
				if (view != null) {
					OWLEditorKit ek = view.getOWLEditorKit();
					if (ek != null) {
						UIHelper hlp = new UIHelper(ek);
						OWLClass cls = hlp.pickOWLClass();
						if (cls != null) {
							queryHelper.setSrcArtifactName(ek.getModelManager().getRendering(cls));
							queryPanel.updateSrcArtifactName();
						}
					}
				}
			}
		});
		toolbar.add(btn);

		btn = new JButton(null, ResourceHandler.getIcon("icon_protege_instance_add.gif"));
		btn.setToolTipText("Select a instance");
		btn.setOpaque(false);
		btn.setPreferredSize(dim);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JambalayaView view = JambalayaView.globalInstance();
				if (view != null) {
					OWLEditorKit ek = view.getOWLEditorKit();
					if (ek != null) {
						UIHelper hlp = new UIHelper(ek);
						OWLIndividual ind = hlp.pickOWLIndividual();
						if (ind != null) {
							queryHelper.setSrcArtifactName(ek.getModelManager().getRendering(ind));
							queryPanel.updateSrcArtifactName();
						}
					}
				}
			}
		});
		toolbar.add(btn);
		queryPanel.getExtraSearchButtonsPanel().add(toolbar);
	}

	private void createActions() {
		toolbarActions.clear();
		final LayoutModeChangeAdapter layoutSelectedAdapter = new LayoutModeChangeAdapter(this, LayoutModeChangeAdapter.APPLY_TO_SELECTED);

		ShrimpAction action = new FocusOnHomeAdapter(this);
		toolbarActions.add(action);
		toolbarActions.add(null); // separator

		ArrayList layouts = new ArrayList(10);
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_GRID + " - " + ShrimpConstants.ACTION_NAME_GRID_ALPHABETICAL, "icon_grid_layout_alphabetical.gif", LayoutConstants.LAYOUT_GRID_BY_ALPHA,
				"false", "", ShrimpConstants.GROUP_A, "1" });
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_SPRING_LAYOUT, "icon_spring_layout.gif", LayoutConstants.LAYOUT_SPRING, "false", "", ShrimpConstants.GROUP_A, "2" });
		layouts
				.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_VERTICAL, "icon_tree_layout_vertical.gif", LayoutConstants.LAYOUT_TREE_VERTICAL, "false", "", ShrimpConstants.GROUP_A, "3" });
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_HORIZONTAL, "icon_tree_layout_horizontal.gif", LayoutConstants.LAYOUT_TREE_HORIZONTAL, "false", "", ShrimpConstants.GROUP_A,
				"4" });
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_VERTICAL_INVERTED, "icon_tree_layout_vertical_inverted.gif", LayoutConstants.LAYOUT_TREE_VERTICAL_INVERTED, "false", "",
				ShrimpConstants.GROUP_A, "5" });
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_TREE_LAYOUT_HORIZONTAL_INVERTED, "icon_tree_layout_horizontal_inverted.gif", LayoutConstants.LAYOUT_TREE_HORIZONTAL_INVERTED, "false",
				"", ShrimpConstants.GROUP_A, "6" });
		layouts.add(new String[] { ShrimpConstants.ACTION_NAME_RADIAL_LAYOUT, "icon_radial_layout.gif", LayoutConstants.LAYOUT_RADIAL, "false", "", ShrimpConstants.GROUP_A, "7" });

		if (OrthogonalLayout.isLoaded()) {
			layouts
					.add(new String[] { ShrimpConstants.ACTION_NAME_HIERACHICAL_LAYOUT, "icon_hierarchical_layout.gif", LayoutConstants.LAYOUT_HIERARCHICAL, "false", "", ShrimpConstants.GROUP_A, "8" });
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_ORTHOGONAL_LAYOUT, "icon_controlflow_layout.gif", LayoutConstants.LAYOUT_ORTHOGONAL, "false", "", ShrimpConstants.GROUP_A, "8" });
		}
		// @tag Shrimp.sugiyama
		if (SugiyamaLayout.isInstalled()) {
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_SUGIYAMA_LAYOUT, "icon_sugiyama_layout.gif", LayoutConstants.LAYOUT_SUGIYAMA, "false", "", ShrimpConstants.GROUP_A, "9" });
		}
		if (ForceDirectedLayout.isPrefuseInstalled()) {
			layouts.add(new String[] { ShrimpConstants.ACTION_NAME_FORCE_DIRECTED_LAYOUT, "icon_force_directed_layout.gif", LayoutConstants.LAYOUT_FORCE_DIRECTED, "false", "",
					ShrimpConstants.GROUP_A, "8" });
		}

		for (Iterator iter = layouts.iterator(); iter.hasNext();) {
			String[] params = (String[]) iter.next();
			final String text = params[0];
			final Icon icon = ResourceHandler.getIcon(params[1]);
			final String mode = params[2];
			final boolean showLayoutDialog = Boolean.valueOf(params[3]).booleanValue();

			action = new DefaultShrimpAction(text, icon) {
				public void actionPerformed(ActionEvent e) {
					// @tag Shrimp.QueryView.layouts : first clear the selected nodes so layouts always run on the root nodes
					try {
						SelectorBean selectorBean = (SelectorBean) QueryView.this.getBean(ShrimpTool.SELECTOR_BEAN);
						selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector(0));
					} catch (BeanNotFoundException ignore) {
					}
					layoutSelectedAdapter.changeLayout(mode, showLayoutDialog);
					// update the query helper to remember the chosen layout
					queryHelper.setLayoutMode(mode);
				}
			};
			toolbarActions.add(action);
		}

		toolbarActions.add(null); // separator

		// important - must set the project on these actions
		OpenNodeFilterAdapter nodeFilterAction = new OpenNodeFilterAdapter(ShrimpProject.QUERY_VIEW + " " + ShrimpApplication.NODE_FILTER, ShrimpProject.QUERY_VIEW);
		nodeFilterAction.setProject(getProject());
		toolbarActions.add(nodeFilterAction);

		OpenArcFilterAdapter arcFilterAction = new OpenArcFilterAdapter(ShrimpProject.QUERY_VIEW + " " + ShrimpApplication.ARC_FILTER, ShrimpProject.QUERY_VIEW);
		arcFilterAction.setProject(getProject());
		toolbarActions.add(arcFilterAction);

		// Node->Expand
		action = new ExpandAction(this);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 1);
		// Node->Collapse
		action = new CollapseAction(this);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 2);

		// Node->Focus On
		action = new FocusOnAction(this);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_C, 1);

		// Node->Hide
		action = new HideAction(this);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 1);
		// Node->Hide Ancestors
		action = new HideAction(this, true, false);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 2);
		// Node->Hide Descendants
		action = new HideAction(this, false, true);
		addQueryViewAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_D, 3);
	}

	public void addQueryViewAction(ShrimpAction action, String parentMenu, String groupName, int position) {
		actionManager.addAction(action, parentMenu, groupName, position);
		String[] data = { (String) action.getValue(Action.NAME), parentMenu };
		menuActions.add(data);
	}

	private void createToolBar() {
		toolBar = new JToolBar();
		toolBar.setRollover(true);
		toolBar.setFloatable(false);
		for (Iterator iter = toolbarActions.iterator(); iter.hasNext();) {
			ShrimpAction action = (ShrimpAction) iter.next();
			if (action == null) {
				toolBar.addSeparator(new Dimension(DEFAULT_SEPARATOR_SIZE, DEFAULT_SEPARATOR_SIZE));
			} else {
				JButton btn = new JButton(action);
				Icon icon = (Icon) action.getValue(Action.SMALL_ICON);
				if (icon != null) {
					btn.setText("");
				}
				btn.setToolTipText(action.getText());
				toolBar.add(btn);
			}
		}
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(getOutputLabel());
	}

	private void createNodePopupMenu() {
		nodePopupMenu = new JPopupMenu();
		repopulateNodePopupMenu();
		nodePopupListener = new QueryViewNodePopupListener(nodePopupMenu);

		//add a action manager listener so that the popup can repopulated if the menus change
		nodeActionManagerListener = new ActionManagerListener() {
			public void actionsAdded(ActionsAddedEvent event) {
				repopulateNodePopupMenu();
			}
			public void actionsRemoved(ActionsRemovedEvent event) {
				repopulateNodePopupMenu();
			}
			public void actionsModified(ActionsModifiedEvent event) {
				repopulateNodePopupMenu();
			}
		};
		actionManager.addActionManagerListener(nodeActionManagerListener);
	}

	private void repopulateNodePopupMenu() {
		if (nodePopupMenu == null) {
			return;
		}
		nodePopupMenu.removeAll();
		if (getProject() != null) {
			actionManager.repopulatePopUpMenu(ShrimpConstants.MENU_NODE, "", nodePopupMenu, "QueryView Node Popup");
		}
	}

	private void removeNodePopupMenu() {
		actionManager.removeActionManagerListener(nodeActionManagerListener);
		//Retrieve all the beans
		PNestedDisplayBean displayBean = getDisplayBean();
		if (displayBean != null) {
			displayBean.getPCanvas().removeMouseListener(nodePopupListener);
		}
		if (nodePopupMenu != null) {
			nodePopupMenu.removeAll();
			nodePopupMenu = null;
		}
	}

	private void removeQueryViewActions() {
		// stop actionManager events
		boolean firingEvents = actionManager.getFiringEvents();
		actionManager.setFiringEvents(false);

		for (Iterator iter = menuActions.iterator(); iter.hasNext();) {
			String[] data = (String[]) iter.next();
			actionManager.removeAction(data[0], data[1], ActionManager.DISABLE_PARENT);
			iter.remove();
		}

		// restore actionManager events
		actionManager.setFiringEvents(firingEvents);
	}

	class QueryViewNodePopupListener extends PopupListener {

		public QueryViewNodePopupListener(JPopupMenu popup) {
			super(popup, pScrollPane);
		}

		protected boolean beforeShowPopup(MouseEvent e) {
			boolean show = false;
			try {
				final SelectorBean selectorBean = (SelectorBean) getBean(ShrimpTool.SELECTOR_BEAN);
				Vector targets = new Vector(1);
				Object currentTarget = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);

				//Do not show this menu for arcs
				if (currentTarget instanceof ShrimpNode) {
					ShrimpNode node = (ShrimpNode) currentTarget;
					// If the target node is in the group of selected nodes then the selected nodes should be the targets.
					// If target node is not in the group of selected nodes then the target node should be the only target.
					Vector selected = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
					if (selected.contains(node)) {
						targets = selected;
					} else {
						targets.add(node);
						selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, targets);
					}

					ExpandAction expandAction = (ExpandAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_EXPAND, ShrimpConstants.MENU_NODE);
					expandAction.setEnabled(node.isOpenable());
					// always allow collapse?
					//CollapseAction collapseAction = (CollapseAction) actionManager.getAction(ShrimpConstants.ACTION_NAME_COLLAPSE, ShrimpConstants.MENU_NODE);
					//collapseAction.setEnabled(!node.isOpenable());

					show = true;
				}
			} catch (BeanNotFoundException ex) {
				ex.printStackTrace();
			}
			return show;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ViewTool#navigateToObject(Object)
	 */
	public void navigateToObject(Object destObject) {
		// TODO implement QueryView.navigateToObject
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ViewTool#getMouseMode()
	 */
	public String getMouseMode() {
		return DisplayConstants.MOUSE_MODE_SELECT;
	}

	/**
	 * @return the artifact types
	 */
	public Collection getArtTypes() {
		return queryHelper.getArtTypes();
	}

	/**
	 * Sets the artifact types
	 *
	 * @param artTypes
	 */
	public void setArtTypes(List artTypes) {
		queryHelper.setArtifactTypes(artTypes);
		if (queryPanel != null) {
			queryPanel.updateArtifactTypes();
		}
	}

	/**
	 * Gets the number of levels being used. This is the number of arcs to traverse away from the
	 * source node(s).
	 *
	 * @return the number of levels
	 */
	public int getIncomingLevels() {
		return queryHelper.getIncomingLevels();
	}

	/**
	 * Gets the number of levels being used. This is the number of arcs to traverse away from the
	 * source node(s).
	 *
	 * @return the number of levels
	 */
	public int getOutgoingLevels() {
		return queryHelper.getOutgoingLevels();
	}

	/**
	 * Sets the number of levels for the query. This is the number of arcs to traverse going into
	 * and away from from the source node(s).
	 */
	public void setLevels(int incomingLevels, int outgoingLevels) {
		queryHelper.setLevels(incomingLevels, outgoingLevels);
		if (queryPanel != null) {
			queryPanel.updateLevels();
		}
	}

	/**
	 * @return the relationship types.
	 */
	public Collection getRelTypes() {
		return queryHelper.getRelTypes();
	}

	/**
	 * Sets the relationship types.
	 *
	 * @param relTypes
	 */
	public void setRelationshipTypes(Collection relTypes) {
		queryHelper.setRelationshipTypes(relTypes);
		if (queryPanel != null) {
			queryPanel.updateRelationshipTypes();
		}
	}

	public int getStringMatchingMode() {
		return queryHelper.getStringMatchingMode();
	}

	/**
	 * Sets the string matching mode - either simple or regular expression based.
	 *
	 * @see QueryHelper#STRING_MATCH_CONTAINS_MODE
	 * @see QueryHelper#STRING_MATCH_EXACT_MODE
	 * @see QueryHelper#STRING_MATCH_STARTS_WITH_MODE
	 * @see QueryHelper#STRING_MATCH_ENDS_WITH_MODE
	 * @see QueryHelper#STRING_MATCH_REGEXP_MODE
	 * @param stringMatchingMode
	 */
	public void setStringMatchingMode(int stringMatchingMode) {
		queryHelper.setStringMatchingMode(stringMatchingMode);
		if (queryPanel != null) {
			queryPanel.updateSearchMode();
		}
	}

	public String getSrcArtifactName() {
		return queryHelper.getSrcArtifactName();
	}

	/**
	 * Sets the source artifact name.
	 *
	 * @param srcArtifactName
	 */
	public void setSrcArtifactName(String srcArtifactName) {
		queryHelper.setSrcArtifactName(srcArtifactName);
		if (queryPanel != null) {
			queryPanel.updateSrcArtifactName();
		}
	}

	/**
	 * Sets the source artifacts. These are the "selected" nodes. To query on these, call
	 * {@link QueryView#querySelected(boolean)}.
	 *
	 * @param srcArtifacts
	 */
	public void setSrcArtifacts(Collection srcArtifacts) {
		queryHelper.setSrcArtifacts(srcArtifacts);
	}

	/**
	 * Finds the artifact for the given {@link Frame} and sets it as the source artifact.
	 *
	 * @param srcFrame
	 *            the source Frame.
	 */
	public void setSrcFrame(OWLEntity srcFrame) {
		Vector frames = new Vector(1);
		if (srcFrame != null) {
			frames.add(srcFrame);
		}
		setSrcFrames(frames);
	}

	/**
	 * Finds the artifacts for the given {@link Frame} objects and sets these as the source
	 * artifacts. This will select those nodes when {@link QueryView#querySelected(boolean)} is
	 * called.
	 *
	 * @param srcFrames
	 *            the source Frame objects.
	 */
	public void setSrcFrames(Collection srcFrames) {
		if (getProject() == null) {
			return;
		}
		if (srcFrames == null) {
			srcFrames = new Vector(0);
		}

		Vector artifacts = new Vector(srcFrames.size());
		try {
			ProtegeDataBean dataBean = (ProtegeDataBean) getProject().getBean(ShrimpProject.DATA_BEAN);
			for (Object itm : srcFrames) {
				if (itm instanceof OWLEntity) {
					Artifact art = dataBean.findArtifact((OWLEntity) itm);
					if (art != null) {
						artifacts.add(art);
						//} else {
						//	System.err.println("Couldn't find artifact for " + frame.getName());
					}
				}
			}
			setSrcArtifacts(artifacts);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the layout mode.
	 *
	 * @see LayoutConstants
	 * @param layoutMode
	 */
	public void setLayoutMode(String layoutMode) {
		queryHelper.setLayoutMode(layoutMode);
	}

	/**
	 * Returns all the layouts.
	 *
	 * @return
	 */
	public Vector getLayouts() {
		DisplayBean displayBean = getDisplayBean();
		return (displayBean != null ? displayBean.getLayouts() : new Vector(0));
	}

	/**
	 * Sets whether the toolbar should be shown.
	 */
	public void setToolBarShown(boolean visible) {
		toolBar.setVisible(visible);
	}

	/**
	 * Sets whether to keep the progress dialog hidden during query operations.
	 */
	public void setDoNotShowProgressDialog(boolean doNotShowProgressDialog) {
		this.doNotShowProgressDialog = doNotShowProgressDialog;
	}

	public boolean isDoNotShowProgressDialog() {
		return doNotShowProgressDialog;
	}

	/**
	 * Returns the {@link QueryPanelComponent}. This will return null if false was passed into the
	 * constructor meaning that the panel shouldn't be shown.
	 */
	public QueryPanelComponent getQueryPanel() {
		return queryPanel;
	}

	private PNestedDisplayBean getDisplayBean() {
		PNestedDisplayBean displayBean = null;
		try {
			displayBean = (PNestedDisplayBean) getBean(DISPLAY_BEAN);
		} catch (BeanNotFoundException e) {
			//e.printStackTrace();
		}
		return displayBean;
	}

	private SelectorBean getSelectorBean() {
		SelectorBean selectorBean = null;
		try {
			selectorBean = (SelectorBean) getBean(SELECTOR_BEAN);
		} catch (BeanNotFoundException e) {
			//e.printStackTrace();
		}
		return selectorBean;
	}

	private FilterBean getFilterBean() {
		FilterBean filterBean = null;
		try {
			filterBean = (FilterBean) getBean(DISPLAY_FILTER_BEAN);
		} catch (BeanNotFoundException e) {
			//e.printStackTrace();
		}
		return filterBean;
	}

	private DataBean getDataBean() {
		DataBean dataBean = null;
		if (getProject() != null) {
			try {
				dataBean = (DataBean) getProject().getBean(ShrimpProject.DATA_BEAN);
			} catch (BeanNotFoundException e1) {
				//e1.printStackTrace();
			}
		}
		return dataBean;
	}

	/**
	 * Creates and returns a QueryView which shows the given frame (class or instance). This is a
	 * convenience method - it just wraps the frame in a Vector. It uses a vertical tree layout and
	 * doesn't show the query panel, and doesn't animate the layout.
	 *
	 * @param protegeProject
	 *            the loaded protege project
	 * @param frame
	 *            the class or instance to query
	 * @return QueryView
	 */
	public static QueryView showInQueryView(OWLOntology protegeProject, OWLEntity frame) {
		Vector frames = new Vector(1);
		frames.add(frame);
		return showInQueryView(protegeProject, frames);
	}

	/**
	 * Creates and returns a QueryView which shows the queried frames (classes, instances). It uses
	 * a vertical tree layout and doesn't show the query panel, and doesn't animate the layout.
	 *
	 * @param protegeProject
	 *            the loaded protege project
	 * @param frames
	 *            the classes and instances to query
	 * @return QueryView
	 */
	public static QueryView showInQueryView(OWLOntology protegeProject, Vector frames) {
		return showInQueryView(protegeProject, frames, LayoutConstants.LAYOUT_TREE_VERTICAL, false /*showQueryPanel*/);
	}

	/**
	 * Creates and returns a QueryView which shows the queried frames (classes, instances). Call
	 * {@link QueryView#querySelected(boolean)} to layout the selected frames.
	 *
	 * @param protegeProject
	 *            the loaded protege project
	 * @param frames
	 *            the classes and instances to query
	 * @param layoutMode
	 *            the layout mode - see {@link LayoutConstants}.
	 * @param showQueryPanel
	 *            if the query panel should be shown
	 * @see LayoutConstants
	 * @return QueryView
	 */
	public static QueryView showInQueryView(OWLOntology protegeProject, Vector frames, String layoutMode, boolean showQueryPanel) {
		if (protegeProject == null) {
			throw new NullPointerException("Protege Project can't be null!");
		}

		if (frames == null) {
			frames = new Vector(0);
		}

		JambalayaApplication app;
		if (!ApplicationAccessor.isApplicationSet()) {
			app = new JambalayaApplication();
			app.setMaxOpenProjects(JambalayaApplication.MAX_OPEN_PROJECTS);
			ApplicationAccessor.setApplication(app);
		} else {
			app = (JambalayaApplication) ApplicationAccessor.getApplication();
		}

		JambalayaProject jambalayaProject = app.createJambalayaProject(protegeProject);

		QueryView queryView = new QueryView(jambalayaProject, showQueryPanel);
		queryView.setSrcFrames(frames);
		queryView.setLayoutMode(layoutMode);
		return queryView;
	}

	/**
	 * Creates and returns a QueryView which shows the queried frames (classes, instances). Call
	 * {@link QueryView#querySelected(boolean)} to layout the selected frames.
	 *
	 * @param uri
	 *            the project {@link URI} to open
	 * @param showQueryPanel
	 *            if the query panel should be shown
	 * @return QueryView
	 */
	public static QueryView showInQueryView(URI uri, String frameTitle, boolean showQueryPanel) {
		JFrame frame = new JFrame(frameTitle);
		frame.setIconImage((ResourceHandler.getIcon("icon_query_view.gif")).getImage());
		frame.setLocation(400, 100);

		//StandAloneApplication app = new StandAloneApplication();
		QueryViewApplication app = new QueryViewApplication();
		app.setParentFrame(frame);
		app.initialize();
		app.setMaxOpenProjects(1);
		ApplicationAccessor.setApplication(app);

		ProgressDialog.createProgressDialog(frame, "Shrimp Query View");
		ShrimpProject project = app.openProject(uri);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(600, 600));
		frame.getContentPane().add(mainPanel);
		frame.pack();

		frame.setVisible(true);
		final QueryView queryView = new QueryView(project, showQueryPanel);
		mainPanel.add(queryView.getGUI(), BorderLayout.CENTER);
		queryView.refresh();

		//TEMP - only layout on "is_a" relationship
//        Vector layouts = queryView.getLayouts();
//        for (Iterator iter = layouts.iterator(); iter.hasNext(); ) {
//        	Layout layout = (Layout) iter.next();
//        	layout.setArcTypes(ShrimpUtils.toCollection("is_a"));
//        }

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				queryView.disposeTool();
			}
		});

		frame.invalidate();
		frame.validate();

		return queryView;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		ProgressDialog.createProgressDialog(null, "QueryView Test");

		oboTestFrame();
		//twoQueryViewsTest();

	}

	protected static void oboTestFrame() {
    	URI uri = ResourceHandler.getFileURI(StandAloneApplication.DEMO_OBO_LOCAL);
    	QueryView queryView = showInQueryView(uri, "Query View [OBO]", true);
    	queryView.query("lysosome", false);
    	((JFrame)ApplicationAccessor.getParentFrame()).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected static void twoQueryViewsTest() {
		JFrame frame = new JFrame();
		frame.setTitle("QueryView Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel top = new JPanel(new GridLayout(1, 2));
		frame.getContentPane().add(top, BorderLayout.NORTH);
		JList list1 = new JList(new String[] { "rose", "Consumable thing", "Drink", "Wine region", "Meal course" });
		list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		top.add(new JScrollPane(list1));
		JList list2 = new JList(new String[] { "Person", "Content", "Author", "Kim", "Layout_info" });
		list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		top.add(new JScrollPane(list2));

		JPanel center = new JPanel(new GridLayout(1, 2));
		frame.getContentPane().add(center, BorderLayout.CENTER);
		URI uri = ResourceHandler.getFileURI(StandAloneApplication.DEMO_WINES_PPRJ_LOCAL);
		OWLModelManagerImpl mm = new OWLModelManagerImpl();
		OWLOntology prj = null;
		if (mm != null && mm.loadOntologyFromPhysicalURI(uri))
			prj = mm.getActiveOntology();
		final QueryView qv1 = showInQueryView(prj, new Vector(0));
		qv1.setDoNotShowProgressDialog(true);
		center.add(qv1.getGUI());
		qv1.refresh();

		uri = ResourceHandler.getFileURI(StandAloneApplication.DEMO_NEWSPAPER_PPRJ_LOCAL);
		if (mm != null && mm.loadOntologyFromPhysicalURI(uri))
			prj = mm.getActiveOntology();
		final QueryView qv2 = showInQueryView(prj, new Vector(0));
		center.add(qv2.getGUI());
		qv2.refresh();

		ApplicationAccessor.getApplication().setParentFrame(frame);

		list1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					String value = (String) ((JList)e.getSource()).getSelectedValue();
					qv1.query(value, true);
				}
			}
		});
		list2.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					String value = (String) ((JList)e.getSource()).getSelectedValue();
					qv2.query(value, true);
				}
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				qv1.disposeTool();
				qv2.disposeTool();
			}
		});

		frame.pack();
		frame.setLocation(400, 200);
		frame.setSize(950, 600);
		frame.setVisible(true);
	}

}