/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import org.protege.editor.core.ui.workspace.WorkspaceTab;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.protege.editor.core.ui.view.ViewsPane;
import org.protege.editor.core.ui.view.View;
import org.protege.editor.core.ui.view.ViewComponent;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassHierarchyViewComponent;
import org.protege.editor.owl.ui.tree.OWLObjectTree;
import org.protege.editor.owl.ui.tree.OWLObjectTreeNode;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.DegreeOfInterestFilter;
import ca.uvic.csr.shrimp.FilterBean.DegreeOfInterestHighlighter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.InterestFilter;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.QuickStartComponent;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.gui.ShrimpView.AbstractShrimpViewFactory;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewConfiguration;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewFactory;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewPanel;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.CollapsiblePanel;
import ca.uvic.csr.shrimp.util.DragComponentListener;
import ca.uvic.csr.shrimp.util.GradientPanel;

/**
 *  Creates all the Jambalaya UI Content.
 *
 * @author Chris Callendar
 */
public class JambalayaContentPane {

	private static final String URL_JAMBALAYA_DOCS = "doc/jambalaya_docs.html";

	private IJambalayaContainer parent;
	private OWLEditorKit editorKit;

	// listeners for the instance list and class tree
	private GlobalSelectionListener selectionListener = null;
	private SelectedNodesChangeAdapter selectedNodesChangeAdapter = null;
	private ClsTreeExpansionAdapter clsTreeExpansionAdapter = null;
	private ClsTreeMouseDoubleClickAdapter clsTreeMouseDoubleClickAdapter = null; 
	private DegreeOfInterestChangeAdapter degreeOfInterestChangeAdapter = null;

	private JPanel shrimpViewContainer;
	private JRootPane shrimpViewRootPane;	// holds the shrimpViewContainer

	private CollapsiblePanel quickStartPanel;
	private OWLObjectTree<OWLClass> clsTree;

	public JambalayaContentPane(IJambalayaContainer parent, OWLEditorKit ek) {
		this.parent = parent;
		this.editorKit = ek;
	}

	public void setParent(IJambalayaContainer parent) {
		this.parent = parent;
	}

	public IJambalayaContainer getParent() {
		return parent;
	}

	public void initialize() {
		shrimpViewContainer = new GradientPanel();
		shrimpViewContainer.setLayout(new BorderLayout());

		String version = getParent().getJambalayaApplication().getBuildInfo().toShortString();
		ImageIcon icon = new ImageIcon(ResourceHandler.getResourceImage("shrimplogo_bevel.png"));
		JLabel label = new JLabel(version, icon, SwingConstants.CENTER);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setForeground(Color.white);
		label.setFont(label.getFont().deriveFont(Font.BOLD).deriveFont(14f));
		//shrimpViewContainer.setBackground(ShrimpConstants.SHRIMP_BACKGROUND_COLOR);
		shrimpViewContainer.add(label, BorderLayout.CENTER);

		shrimpViewRootPane = new JRootPane();
		shrimpViewRootPane.getContentPane().setLayout(new BorderLayout());
		shrimpViewRootPane.getContentPane().add(shrimpViewContainer, BorderLayout.CENTER);

		// @tag Jambalaya.QuickStart : show quick start panel if necessary
		Properties props = ApplicationAccessor.getProperties();
		String showDialogStr = props.getProperty(ShrimpApplication.SHOW_QUICKSTART_DIALOG_KEY, "true");
		if ("true".equalsIgnoreCase(showDialogStr)) {
			showQuickStartPanel();
		}
	}

	/**
	 * Displays the quick start panel in the glass pane of the root pane
	 * ONLY if it isn't already showing.
	 */
	public void showQuickStartPanel() {
		CollapsiblePanel qsPanel = getQuickStartPanel();
		if (!qsPanel.isShowing()) {
			// must set an initial size since there is no layout on the glass pane
			qsPanel.setBounds(75, 75, 550, 500);

			// add the quick start as a panel instead of a dialog
			final JComponent glassPane = (JComponent) shrimpViewRootPane.getGlassPane();
			glassPane.setVisible(true);
			glassPane.setLayout(null);
			glassPane.add(qsPanel);

			// hide the glass pane when the quick start panel is closed
			qsPanel.addChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if (CollapsiblePanel.CLOSED_KEY.equals(evt.getPropertyName())) {
						glassPane.setVisible(false);
					}
				}
			});
		}
	}

	/**
	 * Creates and returns the quick start collapsible panel.
	 */
	private CollapsiblePanel getQuickStartPanel() {
		if (quickStartPanel == null) {
			URL url = ResourceHandler.getFileURL(URL_JAMBALAYA_DOCS);
			QuickStartComponent qsc = new QuickStartComponent(url);
			qsc.setOpaque(false);
			qsc.getDismissButton().setVisible(false);
			qsc.setPreferredSize(new Dimension(540, 450));

			quickStartPanel = new CollapsiblePanel("Jambalaya Quick Start", ResourceHandler.getIcon("icon_jambalaya2.gif"), qsc);
			quickStartPanel.setShowCloseButton(true);
			quickStartPanel.setInnerBorder(BorderFactory.createEtchedBorder());

			// let the user drag the panel around
			DragComponentListener listener = new DragComponentListener(quickStartPanel, true);
			quickStartPanel.getTitleLabel().addMouseListener(listener);
			quickStartPanel.getTitleLabel().addMouseMotionListener(listener);
		}
		return quickStartPanel;
	}

	public void setRootClses(Set rootClses) {
		// @tag Jambalaya.rootClses : reload the root tree node with the root classes
	}

	public Component getComponent() {
		return shrimpViewRootPane;
	}

	private Object getBean(String beanName) throws BeanNotFoundException {
	    ShrimpProject activeProject = parent.getJambalayaProject();
	    if (activeProject == null) {
	        throw new BeanNotFoundException(beanName);
	    }
        return activeProject.getBean(beanName);
	}

	private Object getTool (String toolName) throws ShrimpToolNotFoundException {
	    ShrimpProject activeProject = parent.getJambalayaProject();
	    if (activeProject == null) {
	        throw new ShrimpToolNotFoundException (toolName);
	    }
        return activeProject.getTool(toolName);
	}

	/**
	 * @param activeProject
	 */
	public ShrimpView createShrimpView(ShrimpProject activeProject) {
        ShrimpViewFactory factory = new JambalayaShrimpViewFactory();
        return factory.createShrimpView(activeProject, shrimpViewContainer, ShrimpViewConfiguration.ALL_ON);
	}

	private OWLObjectTree<OWLClass> findClsTree() {
		OWLWorkspace ws = editorKit.getOWLWorkspace();
		if (ws != null) {
			WorkspaceTab tab = ws.getWorkspaceTab("Jambalaya");
			if (tab != null && tab instanceof OWLWorkspaceViewsTab) {
				OWLWorkspaceViewsTab vt = (OWLWorkspaceViewsTab) tab;
				ViewsPane pane = vt.getViewsPane();
				if (pane != null) {
					for (View v : pane.getViews()) {
						ViewComponent view = v.getViewComponent();
						if (view != null && view instanceof AbstractOWLClassHierarchyViewComponent) {
							for (Component cmp : view.getComponents()) {
								if (cmp instanceof JScrollPane) {
									for (Component c : ((JScrollPane) cmp).getComponents()) {
										if (c instanceof OWLObjectTree)
											return (OWLObjectTree<OWLClass>) c;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private void addListeners() {
		selectionListener = new GlobalSelectionListener();
		editorKit.getOWLWorkspace().getOWLSelectionModel().addListener(selectionListener);
		clsTree = findClsTree();
		if (clsTree != null) {
			clsTreeExpansionAdapter = new ClsTreeExpansionAdapter();
			clsTree.addTreeExpansionListener(clsTreeExpansionAdapter);
			clsTreeMouseDoubleClickAdapter = new ClsTreeMouseDoubleClickAdapter();
			clsTree.addMouseListener(clsTreeMouseDoubleClickAdapter);
			degreeOfInterestChangeAdapter = new DegreeOfInterestChangeAdapter();
			clsTree.addPropertyChangeListener(degreeOfInterestChangeAdapter);
		}
	}

	public void dispose() {
		if (selectionListener != null)
			editorKit.getOWLWorkspace().getOWLSelectionModel().removeListener(selectionListener);
		if (clsTree != null) {
			clsTree.removeTreeExpansionListener(clsTreeExpansionAdapter);
			clsTree.removeMouseListener(clsTreeMouseDoubleClickAdapter);
			clsTree.removePropertyChangeListener(degreeOfInterestChangeAdapter);
		}
	}

	// navigate shrimp view to the chosen node
	private void navigateToNode(ShrimpNode node) {
		if (node == null) {
			return;
		}

		ShrimpProject shrimpProject = parent.getJambalayaProject();
		if (shrimpProject == null) {
			return;
		}
		try {
			ShrimpView shrimpView = (ShrimpView) shrimpProject.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);

			Vector currentObjects = (Vector) displayBean.getCurrentFocusedOnObjects().clone();
			Object srcObject;
			if (currentObjects.size() == 0 || currentObjects.elementAt(0) instanceof ShrimpArc) {
			    srcObject = displayBean.getDataDisplayBridge().getRootNodes();
			} else {
			    srcObject = currentObjects;
			}

			if (!srcObject.equals(node)) {
				Vector path = displayBean.getPathBetweenObjects(srcObject, node);
				if (!path.isEmpty()) {
					shrimpView.navigateToObject(node);
				} else {
					JOptionPane.showMessageDialog(getComponent(), "The destination node is not in current hierarchy.");
				}
			}
		} catch (ShrimpToolNotFoundException e) {
			// do nothing
		} catch (BeanNotFoundException e) {
			// do nothing
		}
	}

	/**
	 * Gets the artifacts for the given frames using the databean.
	 * @param c A collection of Frame objects
	 * @return Vector of Artifact objects
	 */
	protected Vector getArtifactsForFrames(Collection<OWLEntity> frames) {
		Vector artifacts = new Vector();
		try {
			ProtegeDataBean dataBean = (ProtegeDataBean) getBean(ShrimpProject.DATA_BEAN);
			for (OWLEntity e : frames) {
				Artifact artifact = dataBean.findArtifact(e);
				if (artifact != null)
					artifacts.add(artifact);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
		return artifacts;
	}

	private Vector getChosenNodes(TreePath[] treePaths) {
		Vector chosenNodes = new Vector();
		for (int i = 0; i < treePaths.length; i++) {
			TreePath treePath = treePaths[i];
			ShrimpNode chosenNode = getChosenNode(treePath);
			if (chosenNode != null)
				chosenNodes.add(chosenNode);
		}
		return chosenNodes;
	}

	private ShrimpNode getChosenNode(TreePath treePath) {
		// find the chosen tree node
		OWLObjectTreeNode<OWLClass> treeNode = (OWLObjectTreeNode<OWLClass>) treePath.getLastPathComponent();
		OWLEntity e = treeNode.getOWLObject();
		List keyParents = createKeyParentListForFrame(treePath);
		return getChosenNode(e, keyParents);
	}

	private ShrimpNode getChosenNode(OWLEntity e, List keyParents) {
		ShrimpNode chosenNode = null;
		try {
			ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			ProtegeDataBean dataBean = (ProtegeDataBean) getBean(ShrimpProject.DATA_BEAN);

			Artifact chosenArtifact = dataBean.findArtifact(e);

			if (chosenArtifact != null) {
				boolean findProperTreePath = (e instanceof OWLNamedIndividual && isHierarchy(ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE) && isHierarchy(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE)) || (e instanceof OWLClass && isHierarchy(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE));
				if (findProperTreePath && keyParents != null && !keyParents.isEmpty()) {
					chosenNode = displayBean.getDataDisplayBridge().getShrimpNode(chosenArtifact, keyParents, true);
				} else {
					Vector allNodesRepresentingChosenArtifact = displayBean.getDataDisplayBridge().getShrimpNodes(chosenArtifact, true);
					// just use the first node in the list
					if (!allNodesRepresentingChosenArtifact.isEmpty()) {
						chosenNode = (ShrimpNode) allNodesRepresentingChosenArtifact.firstElement();
					}
				}
			}
		} catch (ShrimpToolNotFoundException ex) {
			ex.printStackTrace();
		} catch (BeanNotFoundException ex) {
			ex.printStackTrace();
		}

		return chosenNode;
	}

	private boolean isHierarchy (String cprel) {
		boolean b = false;
		try {
			ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
			Set currentCprels = new HashSet(Arrays.asList( shrimpView.getCprels()));
			b = currentCprels.contains(cprel);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
		return b;
	}

	private List createKeyParentListForInstance(TreePath treePath) {
		return createKeyParentList(treePath, 1);
	}

	private List createKeyParentListForFrame(TreePath treePath) {
		return createKeyParentList(treePath, 2);
	}

	private List createKeyParentList(TreePath treePath, int offset) {
		List keyParentsList = new ArrayList();
		// travel up the tree adding key parents to the list
		for (int i = treePath.getPathCount() - offset; i > -1; i--) {
			OWLObjectTreeNode<OWLClass> treeNode = (OWLObjectTreeNode<OWLClass>) treePath.getPathComponent(i);
			OWLEntity e = treeNode.getOWLObject();
			try {
				ProtegeDataBean dataBean = (ProtegeDataBean) getBean(ShrimpProject.DATA_BEAN);
				Artifact keyParent = dataBean.findArtifact(e);
				if (keyParent != null)
					keyParentsList.add(keyParent.getExternalId());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return keyParentsList;
	}


	/**
	 * Sets the interest filter.  This is used to determine which nodes are interesting.
	 * @param filter
	 */
	public void setInterestFilter(InterestFilter filter) {
		degreeOfInterestChangeAdapter.setFilter(new DegreeOfInterestFilter(filter));
		degreeOfInterestChangeAdapter.setHighlighter(new DegreeOfInterestHighlighter(filter));
	}

	/**
	 * Takes a snapshot
	 * @see ShrimpSnapShot#takeSnapShot(ShrimpProject, ShrimpView)
	 */
	public void takeSnapShot() {
		try {
			ShrimpSnapShot.takeSnapShot(parent.getJambalayaProject(), (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW));
		} catch (ShrimpToolNotFoundException ignore) {
		}
	}

	private class GlobalSelectionListener implements OWLSelectionModelListener {

		private boolean enabled = true;

		public void disable() {
			enabled = false;
		}

		public void enable() {
			enabled = true;
		}

		public void selectionChanged() {
			if (! enabled)
				return;

			Vector chosenNodes = new Vector();
			OWLEntity e = editorKit.getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
			if (e != null) {
				ShrimpNode node = getChosenNode(e, null);
				if (node != null)
					chosenNodes.add(node);
			}

			if (chosenNodes.size() > 0) {
				try {
					ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
					SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
					selectedNodesChangeAdapter.disable();
					selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, chosenNodes);
					selectedNodesChangeAdapter.enable();
				} catch (ShrimpToolNotFoundException ex) {
					// do nothing
				} catch (BeanNotFoundException ex) {
					// do nothing
				}
			}
		}
	}

	/**
	 * Listens for changes to "selected nodes" in the shrimp view and selects
	 * the appropriate class or instance in the class tree and/or instance list.
	 */
	class SelectedNodesChangeAdapter implements PropertyChangeListener {
		private boolean enabled = true;

		public void disable() {
			enabled = false;
		}

		public void enable() {
			enabled = true;
		}

		public void propertyChange(PropertyChangeEvent evt) {
		    if (! enabled)
				return;

			Vector selectedNodes = (Vector) evt.getNewValue();

			for (Object itm : selectedNodes) {
				if (itm instanceof ShrimpNode) {
					Artifact art = ((ShrimpNode) itm).getArtifact();
					if (art != null && art instanceof ProtegeArtifact) {
						OWLEntity e = ((ProtegeArtifact) art).getEntity();
						if (e != null) {
							selectionListener.disable();
							editorKit.getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(e);
							selectionListener.enable();
						}
					}
				}
			}
		}
	}

	/**
	 * When node in the class tree is expanded, show children of corresponding node in shrimp view
	 * when node in the class tree is collapsed, close corresponding node in shrimp view.
	 */
	private class ClsTreeExpansionAdapter implements TreeExpansionListener {
		public void treeExpanded(TreeExpansionEvent event) {
			//System.out.println("ClsTreeExpansionAdapter.treeExpanded");
			ShrimpNode node = getChosenNode(event.getPath());
			if (node != null) {
				try {
					ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
					DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
					displayBean.setPanelMode(node, PanelModeConstants.CHILDREN);
				} catch (ShrimpToolNotFoundException e) {
					// do nothing
				} catch (BeanNotFoundException e) {
					// do nothing
				}
			}
		}

		public void treeCollapsed(TreeExpansionEvent event) {
			//System.out.println("ClsTreeExpansionAdapter.treeCollapsed");
			ShrimpNode node = getChosenNode(event.getPath());
			if (node != null) {
				try {
					ShrimpView shrimpView = (ShrimpView) getTool(ShrimpProject.SHRIMP_VIEW);
					DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
					displayBean.setPanelMode(node, PanelModeConstants.CLOSED);
				} catch (ShrimpToolNotFoundException e) {
					// do nothing
				} catch (BeanNotFoundException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * When a node in the class tree is double clicked, navigate shrimp view to that node.
	 */
	private class ClsTreeMouseDoubleClickAdapter extends MouseAdapter {
		public void mouseClicked(MouseEvent event) {
			if (event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event) && clsTree.getSelectionCount() == 1) {
				ShrimpNode node = getChosenNode(clsTree.getSelectionPath());
				if (node != null) {
					navigateToNode(node);
				}
			}
		}
	}

	/**
	 * Listens for filter and highlight property changes events.  These get fired
	 * from the class tree or the instance list when either are filtered or highlighted.
	 * The Diamond plugin for Protege will fire these events.
	 * Adds or removes a visibility Filter to filter invisible nodes.
	 * @see VisibilityFilter
	 * @author Chris Callendar
	 */
	private class DegreeOfInterestChangeAdapter implements PropertyChangeListener {

		/** The property change content type. */
		private static final String FILTER_PROPERTY_CHANGE = "FilterPropertyChange";
		private static final String HIGHLIGHT_PROPERTY_CHANGE = "HighlightPropertyChange";

		private DegreeOfInterestFilter filter = null;
		private DegreeOfInterestHighlighter highlighter = null;

		public void setFilter(DegreeOfInterestFilter filter) {
			this.filter = filter;
		}

		public void setHighlighter(DegreeOfInterestHighlighter highlighter) {
			this.highlighter = highlighter;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (shrimpViewContainer == null) {
				return;
			}

			boolean filterOn = ((evt.getNewValue() instanceof Boolean) &&
								((Boolean)evt.getNewValue()).booleanValue());

			// add the filter to remove invisible nodes
			if (FILTER_PROPERTY_CHANGE.equals(evt.getPropertyName())) {
				if (filter == null) {
					return;
				}

				//System.out.println("FILTER PROPERTY CHANGE " + filterOn);
				try {
					ShrimpView shrimpView = (ShrimpView)getTool(ShrimpProject.SHRIMP_VIEW);
					FilterBean filterBean = (FilterBean)shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
					if (filterOn) {
						if (filterBean.contains(filter)) {
							filterBean.removeFilter(filter);
						}
						filterBean.addFilter(filter);
					} else if (filterBean.contains(filter)) {
						filterBean.removeFilter(filter);
						//updateNodeColors();
					}
				} catch (Exception e) {
					//e.printStackTrace();
				}
			} else if (HIGHLIGHT_PROPERTY_CHANGE.equals(evt.getPropertyName())) {
				if (highlighter == null) {
					return;
				}

				//System.out.println("HIGHLIGHT PROPERTY CHANGE " + filterOn);
				try {
					ShrimpView shrimpView = (ShrimpView)getTool(ShrimpProject.SHRIMP_VIEW);
					FilterBean filterBean = (FilterBean)shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
					highlighter.setApplied(filterOn);
					// we have to remove this filter and then add it back to apply the new highlighting
					if (filterBean.contains(highlighter)) {
						filterBean.removeFilter(highlighter);
					}
					filterBean.addFilter(highlighter);
					updateNodeColors();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
		}

		/**
		 * Forces all the node colors to be updated.  The display is also repainted.
		 */
		private void updateNodeColors() {
	        try {
	        	// update the node colors
				AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) parent.getJambalayaProject().getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
				NominalNodeColorVisualVariable visVar = new NominalNodeColorVisualVariable(attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
				Attribute attr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_COLOR);
				attrToVisVarBean.fireVisVarValuesChangeEvent(attr, visVar);
			} catch (Exception e) {
				e.printStackTrace();
			}
			refreshDisplay();
		}

		/**
		 * Gets the shrimp view for the active project and repaints the display.
		 */
		private void refreshDisplay() {
	        try {
				ShrimpView shrimpView = (ShrimpView) parent.getJambalayaProject().getTool(ShrimpProject.SHRIMP_VIEW);
				DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
				//shrimpView.clear();
				displayBean.repaint();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}


	public class JambalayaShrimpViewFactory extends AbstractShrimpViewFactory {

		protected void projectSpecificDisposeShrimpView() {
		}

		/**
		 * @see ca.uvic.csr.shrimp.gui.ShrimpView.AbstractShrimpViewFactory#projectSpecificCreateShrimpView()
		 */
		protected void projectSpecificCreateShrimpView(ShrimpViewConfiguration config) {
			// @tag Jambalaya.arcProperties : displays the Protege slots/properties dialog
			shrimpView.addUserControl(new ShowArcPropertiesAdapter(project, shrimpView));

			// Add a border around display bean to tell whether it is focused or not
			shrimpViewContainer.removeAll();
			shrimpViewContainer.add(shrimpView.getGUI(), BorderLayout.CENTER);

			if (config.showQuickViewPanel) {
				QuickViewPanel pnlQuickViews = new QuickViewPanel(project, new JambalayaTransferHandler(project));
				shrimpViewContainer.add(pnlQuickViews.getQuickViewPanel(), BorderLayout.WEST);
			}

			//Validate now so that we can get acquire the size of the panel to
			//properly set the root size
			parent.validate();
			shrimpViewContainer.setVisible(true);

			// @tag Shrimp.Jambalaya.Synch_Selection : keep selection synched between class tree and Jambalaya view
			try {
				SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
				selectedNodesChangeAdapter = new SelectedNodesChangeAdapter();
				selectorBean.addPropertyChangeListener(SelectorBeanConstants.SELECTED_NODES, selectedNodesChangeAdapter);
			} catch (BeanNotFoundException e2) {
				e2.printStackTrace();
			}

			addListeners();
		}

		protected void takeClosingSnapShot(ShrimpProject project) {
			Component gui = shrimpView.getGUI();
			// don't bother taking a snapshot if the gui has been disposed already
			if ((gui != null) && gui.isDisplayable()) {
				takeSnapShot();
			}
		}
	}
}
