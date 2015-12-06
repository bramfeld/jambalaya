/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ScriptingBean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CompositeArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.ZoomModeChangeAdapter;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.quickview.DefaultViewAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.usercontrols.AttachDocumentToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeLabelIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.CloseSelectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.CollapseAllDescendentsAdapter;
import ca.uvic.csr.shrimp.usercontrols.ExpandCollapseSubgraphAdapter;
import ca.uvic.csr.shrimp.usercontrols.FilterSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.FocusOnHomeAdapter;
import ca.uvic.csr.shrimp.usercontrols.GroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.LayoutModeChangeAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenAllAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenSelectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.PruneSubgraphAdapter;
import ca.uvic.csr.shrimp.usercontrols.RedoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllChildrenAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllDescendantsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectConnectedNodesAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectInverseSiblingsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SnapShotAdapter;
import ca.uvic.csr.shrimp.usercontrols.UndoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.UnfilterAllByIdAdapter;
import ca.uvic.csr.shrimp.usercontrols.UngroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.ViewDocumentsAdapter;
import ca.uvic.csr.shrimp.util.DoubleDimension;
import ca.uvic.csr.shrimp.util.GraphicsUtils;

/**
 * @author Nasir Rather
 *
 *	@see ca.uvic.csr.shrimp.ScriptingBean.ScriptingConstants
 */
public class DefaultMainViewScriptingBean implements MainViewScriptingBean {

	private SnapShotAdapter snapShotAdapter;

	private LayoutModeChangeAdapter layoutChildrenAdapter;
	private LayoutModeChangeAdapter layoutSelectedAdapter;

	private OpenSelectedNodesAdapter openSelectedNodesAdapter;
	private CloseSelectedNodesAdapter closeSelectedNodesAdapter;
	private OpenAllAdapter openAllAdapter;
	private CollapseAllDescendentsAdapter collapseAllDescendentsAdapter;
	private PruneSubgraphAdapter pruneSubgraphAdapter;
	private UnfilterAllByIdAdapter unfilterAllByIdAdapter;
	private FilterSelectedArtifactsAdapter filterSelectedArtifactsAdapter;
	private ExpandCollapseSubgraphAdapter expandCollapseAdapter;

	// @tag Shrimp.grouping
	private GroupSelectedArtifactsAdapter groupSelectedArtifactsAdapter;
	private RenameSelectedArtifactsAdapter renameSelectedNodeAdapter;
	private UngroupSelectedArtifactsAdapter ungroupSelectedArtifactsAdapter;

	private FocusOnHomeAdapter focusOnHomeAdapter;
	private UndoActionAdapter undoActionAdapter;
	private RedoActionAdapter redoActionAdapter;

	private SelectAllChildrenAdapter selectAllChildrenAdapter;
	private SelectAllDescendantsAdapter selectAllDescendentsAdapter;
	private SelectInverseSiblingsAdapter selectInverseSiblingsAdapter;
	private SelectConnectedNodesAdapter selectConnectedNodesAdapter;

	private ZoomModeChangeAdapter zoomModeChangeAdapter;

	private AttachDocumentToNodeAdapter attachDocumentToNodeAdapter;
	private ViewDocumentsAdapter viewDocumentsAdapter;
	private ChangeNodeLabelIconAdapter changeLabelIconAdapter;
	private ChangeNodeOverlayIconAdapter changeOverlayIconAdapter;

	private ShrimpProject project;

	public DefaultMainViewScriptingBean(ShrimpProject project) {
		this.project = project;
	}

	public void setZoomModeChangeAdapter(ZoomModeChangeAdapter zoomModeChangeAdapter) {
		this.zoomModeChangeAdapter = zoomModeChangeAdapter;
	}

	public void setUnfilterAllByIdAdapter(UnfilterAllByIdAdapter unfilterAllByIdAdapter) {
		this.unfilterAllByIdAdapter = unfilterAllByIdAdapter;
	}

	public void setPruneSubgraphAdapter(PruneSubgraphAdapter pruneSubgraphAdapter) {
		this.pruneSubgraphAdapter = pruneSubgraphAdapter;
	}

	public void setAttachDocumentToNodeAdapter(AttachDocumentToNodeAdapter attachDocumentToNodeAdapter) {
		this.attachDocumentToNodeAdapter = attachDocumentToNodeAdapter;
	}

	public void setViewDocumentsAdapter(ViewDocumentsAdapter viewDocumentsAdapter) {
		this.viewDocumentsAdapter = viewDocumentsAdapter;
	}

	public void setChangeNodeLabelIconAdapter(ChangeNodeLabelIconAdapter changeNodeIconAdapter) {
		this.changeLabelIconAdapter = changeNodeIconAdapter;
	}

	public void setChangeNodeOverlayIconAdapter(ChangeNodeOverlayIconAdapter changeOverlayIconAdapter) {
		this.changeOverlayIconAdapter = changeOverlayIconAdapter;
	}

	public void setLayoutChildrenAdapter(LayoutModeChangeAdapter layoutChildrenAdapter) {
		this.layoutChildrenAdapter = layoutChildrenAdapter;
	}

	public void setLayoutSelectedAdapter(LayoutModeChangeAdapter layoutSelectedAdapter) {
		this.layoutSelectedAdapter = layoutSelectedAdapter;
	}

	public void setCloseSelectedNodesAdapter(CloseSelectedNodesAdapter closeSelectedNodesAdapter) {
		this.closeSelectedNodesAdapter = closeSelectedNodesAdapter;
	}

	public void setCollapseAllDescendentsAdapter(CollapseAllDescendentsAdapter collapseAllDescendentsAdapter) {
		this.collapseAllDescendentsAdapter = collapseAllDescendentsAdapter;
	}

	public void setFilterSelectedArtifactsAdapter(FilterSelectedArtifactsAdapter filterSelectedArtifactsAdapter) {
		this.filterSelectedArtifactsAdapter = filterSelectedArtifactsAdapter;
	}

	public void setFocusOnHomeAdapter(FocusOnHomeAdapter focusOnHomeAdapter) {
		this.focusOnHomeAdapter = focusOnHomeAdapter;
	}

	public void setOpenAllAdapter(OpenAllAdapter openAllAdapter) {
		this.openAllAdapter = openAllAdapter;
	}

	public void setOpenSelectedNodesAdapter(OpenSelectedNodesAdapter openSelectedNodesAdapter) {
		this.openSelectedNodesAdapter = openSelectedNodesAdapter;
	}

	public void setRedoActionAdapter(RedoActionAdapter redoActionAdapter) {
		this.redoActionAdapter = redoActionAdapter;
	}

	public void setSelectAllChildrenAdapter(SelectAllChildrenAdapter selectAllChildrenAdapter) {
		this.selectAllChildrenAdapter = selectAllChildrenAdapter;
	}

	public void setSelectAllDescendentsAdapter(SelectAllDescendantsAdapter selectAllDescendentsAdapter) {
		this.selectAllDescendentsAdapter = selectAllDescendentsAdapter;
	}

	public void setSnapShotAdapter(SnapShotAdapter snapShotAdapter) {
		this.snapShotAdapter = snapShotAdapter;
	}

	public void setUndoActionAdapter(UndoActionAdapter undoActionAdapter) {
		this.undoActionAdapter = undoActionAdapter;
	}
	public void setSelectInverseSiblingsAdapter(SelectInverseSiblingsAdapter adapter) {
		selectInverseSiblingsAdapter = adapter;
	}

    public void setSelectConnectedNodesAdapter(SelectConnectedNodesAdapter selectConnectedNodesAdapter) {
        this.selectConnectedNodesAdapter = selectConnectedNodesAdapter;
    }

	// @tag Shrimp.grouping
	public void setGroupSelectedArtifactsAdapter(GroupSelectedArtifactsAdapter groupSelectedArtifactsAdapter) {
		this.groupSelectedArtifactsAdapter = groupSelectedArtifactsAdapter;
	}

	// @tag Shrimp.grouping
	public void setUngroupSelectedArtifactsAdapter(UngroupSelectedArtifactsAdapter ungroupAdapter) {
		this.ungroupSelectedArtifactsAdapter = ungroupAdapter;
	}

	// @tag Shrimp.grouping
	public void setRenameSelectedNodeAdapter(RenameSelectedArtifactsAdapter renameAdapter) {
		this.renameSelectedNodeAdapter = renameAdapter;
	}


	//////////////////////////////////////////////
	// MainViewScriptingBean methods
	//////////////////////////////////////////////

	/**
	 * @see MainViewScriptingBean#setHierarchy(String[])
	 */
	public void setHierarchy(String[] cprels) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			shrimpView.setCprels(cprels, false, true);
			shrimpView.addDefaultRootNodes(true);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#layoutChildren(String)
	 */
	public void layoutChildren(String newLayoutMode) {
		if (layoutChildrenAdapter != null) {
			layoutChildrenAdapter.changeLayout(newLayoutMode, false);
		}
	}

	/**
	 * @see MainViewScriptingBean#layoutSelected(String)
	 */
	public void layoutSelected(String newLayoutMode) {
		if (layoutSelectedAdapter != null) {
			layoutSelectedAdapter.changeLayout(newLayoutMode, false);
		}
	}

	/**
	 * @see MainViewScriptingBean#openSelectedNodes()
	 */
	public void openSelectedNodes() {
		if (openSelectedNodesAdapter != null) {
			openSelectedNodesAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#closeAllDescendents()
	 */
	public void closeAllDescendents() {
		if (collapseAllDescendentsAdapter != null) {
			collapseAllDescendentsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#filterSelectedNodes()
	 */
	public void filterSelectedNodes() {
		if (filterSelectedArtifactsAdapter != null) {
			filterSelectedArtifactsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#hideSelectedNodes()
	 */
	public void hideSelectedNodes() {
		filterSelectedNodes();
	}

	/**
	 * @see MainViewScriptingBean#groupSelectedNodes()
	 */
	public void groupSelectedNodes() {
		if (groupSelectedArtifactsAdapter != null) {
			groupSelectedArtifactsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#ungroupSelectedNodes()
	 */
	public void ungroupSelectedNodes() {
		if (ungroupSelectedArtifactsAdapter != null) {
			ungroupSelectedArtifactsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#renameSelectedNode()
	 */
	public void renameSelectedNode() {
		if (renameSelectedNodeAdapter != null) {
			renameSelectedNodeAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#focusOnHome()
	 */
	public void focusOnHome() {
		if (focusOnHomeAdapter != null) {
			focusOnHomeAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#focusOnSelectedNode()
	 */
	public void focusOnSelectedNode() {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector selected = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			// focusses on the first element
			displayBean.focusOn(selected);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @see MainViewScriptingBean#openAllDescendents()
	 */
	public void openAllDescendents() {
		if (openAllAdapter != null) {
			openAllAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#closeSelectedNodes()
	 */
	public void closeSelectedNodes() {
		if (closeSelectedNodesAdapter != null) {
			closeSelectedNodesAdapter.closeSelectedNodes();
		}
	}

	/**
	 * @see MainViewScriptingBean#redoAction()
	 */
	public void redoAction() {
		if (redoActionAdapter != null) {
			redoActionAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#selectAllChildren()
	 */
	public void selectAllChildren() {
		if (selectAllChildrenAdapter != null) {
			selectAllChildrenAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#selectAllDescendents()
	 */
	public void selectAllDescendents() {
		if (selectAllDescendentsAdapter != null) {
			selectAllDescendentsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#takeSnapShot(boolean)
	 */
	public void takeSnapShot(boolean askForComments) {
		if (snapShotAdapter != null) {
			snapShotAdapter.takeSnapShot(askForComments);
		}
	}

	/**
	 * @see MainViewScriptingBean#unFilterAllArtifacts()
	 */
	public void unFilterAllArtifacts() {
		if (unfilterAllByIdAdapter != null) {
			unfilterAllByIdAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#showAllHiddenNodes()
	 */
	public void showAllHiddenNodes() {
		unFilterAllArtifacts();
	}

	/**
	 * @see MainViewScriptingBean#pruneSelectedNodes()
	 */
	public void pruneSelectedNodes() {
		if (pruneSubgraphAdapter != null) {
			pruneSubgraphAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#attachDocumentToSelectedNode()
	 */
	public void attachDocumentToSelectedNode() {
		if (attachDocumentToNodeAdapter != null) {
			attachDocumentToNodeAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#viewSelectedNodeDocuments()
	 */
	public void viewSelectedNodeDocuments() {
		if (viewDocumentsAdapter != null) {
			viewDocumentsAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#changeSelectedNodeLabelIcon()
	 */
	public void changeSelectedNodeLabelIcon() {
		if (changeLabelIconAdapter != null) {
			changeLabelIconAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#changeSelectedNodeOverlayIcon()
	 */
	public void changeSelectedNodeOverlayIcon() {
		if (changeOverlayIconAdapter != null) {
			changeOverlayIconAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#expandCollapseSelectedNodes()
	 */
	public void expandCollapseSelectedNodes() {
		try {
			// lazy initialization
			if (expandCollapseAdapter == null) {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				expandCollapseAdapter = new ExpandCollapseSubgraphAdapter(shrimpView, true);
			}
			// toggles the collapsed/expanded state of each of the selected nodes
			expandCollapseAdapter.startAction();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @see MainViewScriptingBean#undoAction()
	 */
	public void undoAction() {
		if (undoActionAdapter != null) {
			undoActionAdapter.startAction();
		}
	}

	/**
	 * @see MainViewScriptingBean#changeZoomMode(String)
	 */
	public void changeZoomMode(String newMode) {
		if (zoomModeChangeAdapter != null) {
			zoomModeChangeAdapter.changeZoomMode(newMode);
		}
	}

	/**
	 * @see MainViewScriptingBean#filterAllArcs(boolean)
	 */
	public void filterAllArcs(boolean filter) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);

				Vector types = dataBean.getRelationshipTypes(true, true);
				filterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, types, filter);

			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#filterAllNodes(boolean)
	 */
	public void filterAllNodes(boolean filter) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);

				Vector types = dataBean.getArtifactTypes(true, true);
				filterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, types, filter);

			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#filterArcsByType(String, boolean)
	 */
	public void filterArcsByType(String type, boolean filter) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				filterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, type, filter);
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#filterNodesByType(String, boolean)
	 */
	public void filterNodesByType(String type, boolean filter) {
		if (project != null) {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				FilterBean filterBean = (FilterBean) shrimpView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				filterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, type, filter);
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#filterArtifactDataType(String, boolean)
	 */
	public void filterArtifactDataType(String type, boolean filter) {
        if (project != null) {
            try {
                FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
                dataFilterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, type, filter);
            } catch (BeanNotFoundException e) {
                e.printStackTrace();
            }
        }
	}

	/**
	 * @see MainViewScriptingBean#filterRelationshipDataType(String, boolean)
	 */
	public void filterRelationshipDataType(String type, boolean filter) {
        if (project != null) {
            try {
                FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
                dataFilterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, type, filter);
            } catch (BeanNotFoundException e) {
                e.printStackTrace();
            }
        }
	}

	/**
	 * @see MainViewScriptingBean#filterAllArtifactDataTypes(boolean)
	 */
	public void filterAllArtifactDataTypes(boolean filter) {
		if (project != null) {
			try {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				Vector types = dataBean.getArtifactTypes(!filter, true);
                FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
                dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, types, filter);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#filterAllRelationshipDataTypes(boolean)
	 */
	public void filterAllRelationshipDataTypes(boolean filter) {
		if (project != null) {
			try {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				Vector types = dataBean.getRelationshipTypes(!filter, true);
                FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
                dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, types, filter);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * @see MainViewScriptingBean#refresh()
	 */
	public void refresh() {
		if (project != null) {
			project.refresh();
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				shrimpView.addDefaultRootNodes(true);
			} catch (ShrimpToolNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}


	/**
	 * @see MainViewScriptingBean#selectNodesByName(String, boolean, boolean, boolean, boolean)
	 */
	public void selectNodesByName(String nameToFind, boolean selectAllOccurances, boolean clearPreviouslySelected, boolean exactMatch, boolean caseSensitive) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			final SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);

			final Vector selectedNodes = new Vector();
			if (!clearPreviouslySelected) {
				Vector previouslySelected = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
				if (previouslySelected != null) {
					selectedNodes.addAll(previouslySelected);
				}
			}

			final Vector nodes = new Vector();
			Vector allNodes = displayBean.getAllNodes();
			for (Iterator iterator = allNodes.iterator(); iterator.hasNext();) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				String tmpName = node.getName();
				boolean match = false;
				if (caseSensitive) {
				    if (exactMatch) {
				        match = tmpName.equals(nameToFind);
				    } else {
				        match = tmpName.indexOf(nameToFind) != -1;
				    }
				} else {
				    if (exactMatch) {
				        match = tmpName.toLowerCase().equals(nameToFind.toLowerCase());
				    } else {
				        match = tmpName.toLowerCase().indexOf(nameToFind.toLowerCase()) != -1;
				    }
				}

				if (match) {
					nodes.add(node);
					if (!selectAllOccurances) {
						break;
					}
				}
			}
			selectedNodes.addAll(nodes);

			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);
			if (!nodes.isEmpty()) {
				selectorBean.setSelected(SelectorBeanConstants.TARGET_OBJECT, nodes.firstElement());
			}
		} catch (ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#navigateToNode(String)
	 */
	public void navigateToNode(String name) {
		try {
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);

			ShrimpNode validNode = null;
			// look through all created nodes for a node with the given name
			for (Iterator iterator = displayBean.getAllNodes().iterator(); iterator.hasNext() && validNode == null;) {
				ShrimpNode tempNode = (ShrimpNode) iterator.next();
				if (tempNode.getName().equals(name)) {
					validNode = tempNode;
				}
			}
			if (validNode == null) {
				//pull out all the stops and look through ALL the data for an artifact with this name
				//this could take a while if have huge data store
				Artifact artifact = dataBean.findFirstArtifact(AttributeConstants.NOM_ATTR_ARTIFACT_NAME, name, displayBean.getCprels());
				if (artifact != null) {
					Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(artifact, true);
					if (nodes.size()>0) {
						validNode = (ShrimpNode) nodes.firstElement();
					}
				}
			}
			if (validNode != null) {
				shrimpView.navigateToObject(validNode);
			}
		} catch (ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#pause(long)
	 */
	public void pause(long time) {
		try {
			//wait(time);
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#alert(String)
	 */
	public void alert(String message) {
		alert(message, "Scripting message");
	}

	/**
	 * @see MainViewScriptingBean#prompt(String, String)
	 */
	public String prompt(String message, String title) {
		String input = "";
		try {
			ShrimpView shrimpView =	(ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			boolean displayEnabled = displayBean.isEnabled();
			displayBean.setEnabled(false);
			try {
				input = JOptionPane.showInputDialog(ApplicationAccessor.getParentFrame(), message,
						title, JOptionPane.PLAIN_MESSAGE);
			} catch (Exception e) {
				displayBean.setEnabled(displayEnabled);
			}
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
		return input;
	}

	/**
	 * @see MainViewScriptingBean#alert(String, String)
	 */
	public void alert(String message, String title) {
		JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message, title, JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * @see MainViewScriptingBean#showDialog(String)
	 */
	public void showDialog(String message) {
		showDialog(message, "Scripting message");
	}

	/**
	 * @see MainViewScriptingBean#showDialog(String, String)
	 */
	public void showDialog(String message, String title) {
		//Create the dialog.
		Frame frame = ApplicationAccessor.getParentFrame();
		final JDialog dialog = new JDialog(frame, title);

		JTextArea textArea = new JTextArea(message);
		textArea.setSize(new Dimension(100, 300));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, 14.0f));

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		JPanel closePanel = new JPanel();
		closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
		closePanel.add(Box.createHorizontalGlue());
		closePanel.add(closeButton);
		closePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(textArea, BorderLayout.CENTER);
		contentPane.add(closePanel, BorderLayout.PAGE_END);
		contentPane.setOpaque(true);
		dialog.setContentPane(contentPane);

		dialog.setSize(new Dimension(300, 150));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}

	/**
	 * @see MainViewScriptingBean#setLabelMode(String)
	 */
	public void setLabelMode(String mode) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			displayBean.setDefaultLabelMode(mode);
			selectorBean.setSelected(DisplayConstants.LABEL_MODE, mode);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#setLabelStyleByType(String, String)
	 */
	public void setLabelStyleByType(String nodeType, String style) {
		setVisVarValueForType(nodeType, style, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_LABEL_STYLE);
	}

	/**
	 * @see MainViewScriptingBean#selectNodesByType(String, boolean)
	 */
	public void selectNodesByType(String type, boolean clearPreviouslySelected) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector newSelected = clearPreviouslySelected ? new Vector () : (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			Vector visibleNodes = displayBean.getVisibleNodes();
			for (Iterator iter = visibleNodes.iterator(); iter.hasNext();) {
				ShrimpNode tmpNode = (ShrimpNode) iter.next();
				if (tmpNode.getArtifact().getType().equals(type) && !newSelected.contains(tmpNode)) {
					newSelected.add(tmpNode);
				}
			}
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, newSelected);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#selectInverseSiblings()
	 */
	public void selectInverseSiblings() {
		selectInverseSiblingsAdapter.startAction();
	}

	/**
	 * @see MainViewScriptingBean#selectConnectedNodes()
	 */
	public void selectConnectedNodes() {
		selectConnectedNodesAdapter.startAction();
	}

	/**
	 * @see MainViewScriptingBean#createComposites(String[])
	 */
	public void createComposites(String[] arcTypes) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
			DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
			CompositeArcsManager compositeArcsManager = dataDisplayBridge.getCompositeArcsManager();
			compositeArcsManager.clear();
			String groupName = "";
			for (int i = 0; i < arcTypes.length; i++) {
				String arcType = arcTypes[i];
				groupName += (i > 0) ? ", " + arcType : arcType;
			}
			groupName += " Group";
			RelTypeGroup group = compositeArcsManager.createRelTypeGroup(groupName);
			for (int i = 0; i < arcTypes.length; i++) {
				String arcType = arcTypes[i];
				compositeArcsManager.createRelTypeGroup(arcType);
			}
			Color groupColor = Color.RED;
			if (arcTypes.length == 1) {
				Color typeColor = (Color)attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR,arcTypes[0]);
				if (typeColor != null) {
					groupColor = typeColor;
				}
			}
			ArcStyle arcStyle = new CompositeArcStyle ();
			group.setCompositeColor(groupColor);
			compositeArcsManager.setCompositesEnabled(group, true);

			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, group.getGroupName(), groupColor);
			attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, group.getGroupName(), arcStyle);

			int threshold = 0;
			Collection arcs = compositeArcsManager.getCompositeArcs();
			for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
				ShrimpCompositeArc arc = (ShrimpCompositeArc) iterator.next();
				if (arc.isAtHighLevel()) {
					if (arc.getArcCount() < threshold) {
						displayBean.setVisible(arc, false, false);
					} else {
						displayBean.setVisible(arc, true, false);
					}
				}
			}
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see MainViewScriptingBean#clearComposites()
	 */
	public void clearComposites() {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
			CompositeArcsManager compositeArcsManager = dataDisplayBridge.getCompositeArcsManager();
			compositeArcsManager.clear();
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

    /**
     * @see MainViewScriptingBean#selectRoots()
     */
    public void selectRoots() {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector newSelected = new Vector (displayBean.getDataDisplayBridge().getRootNodes());
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, newSelected);
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
    }

	/**
	 * @see MainViewScriptingBean#runQuickView(String)
	 */
	public void runQuickView(String name) {
		if (project != null) {
			QuickViewManager manager = project.getQuickViewManager();
			QuickViewAction action = manager.getQuickViewAction(name);
			if (action == null) {
				throw new RuntimeException("quick view '" + name + "' doesn't exist.");
			} else if (!manager.isValid(action)) {
				throw new RuntimeException("quick view '" + name + "' isn't valid.");
			} else {
				action.startAction();
			}
		}
	}

	/**
	 * @see MainViewScriptingBean#runDefaultQuickView()
	 */
	public void runDefaultQuickView() {
		runQuickView(DefaultViewAction.ACTION_NAME);
	}

	/**
	 * @see MainViewScriptingBean#quickSearch(String)
	 */
	public void quickSearch(String searchText) {
		quickSearch(searchText, ScriptingConstants.CONTAINS);
	}

	/**
	 * @see MainViewScriptingBean#quickSearch(String, int)
	 */
	public void quickSearch(String searchText, int mode) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			shrimpView.getQuickSearchPanel().search(searchText, mode);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @see MainViewScriptingBean#setNodeColorByType(String, String)
	 */
	public void setNodeColorByType(String nodeType, String color) {
		setVisVarColorForType(nodeType, color, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
				VisVarConstants.VIS_VAR_NODE_COLOR);
	}

	/**
	 * @see MainViewScriptingBean#setNodeOuterBorderColorByType(String, String)
	 */
	public void setNodeOuterBorderColorByType(String nodeType, String color) {
		setVisVarColorForType(nodeType, color, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
				VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR);
	}

	/**
	 * @see MainViewScriptingBean#setNodeInnerBorderColorByType(String, String)
	 */
	public void setNodeInnerBorderColorByType(String nodeType, String color) {
		setVisVarColorForType(nodeType, color, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
				VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR);
	}

	/**
	 * @see MainViewScriptingBean#setNodeOuterBorderStyleByType(String, String)
	 */
	public void setNodeOuterBorderStyleByType(String nodeType, String borderStyle) {
		setVisVarValueForType(nodeType, borderStyle, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
				VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE);
	}

	/**
	 * @see MainViewScriptingBean#setNodeInnerBorderStyleByType(String, String)
	 */
	public void setNodeInnerBorderStyleByType(String nodeType, String borderStyle) {
		setVisVarValueForType(nodeType, borderStyle, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
				VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE);
	}

	/**
	 * @see MainViewScriptingBean#setNodeShapeByType(String, String)
	 */
	public void setNodeShapeByType(String nodeType, String shape) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			NodeShape nodeShape = displayBean.getNodeShape(shape);
			if (nodeShape != null) {
				setVisVarValueForType(nodeType, nodeShape, AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
						VisVarConstants.VIS_VAR_NODE_SHAPE);
			} else {
				throw new NullPointerException("Node shape '" + shape + "' doesn't exist.");
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * @see MainViewScriptingBean#setArcColorByType(String, String)
	 */
	public void setArcColorByType(String arcType, String color) {
		setVisVarColorForType(arcType, color, AttributeConstants.NOM_ATTR_REL_TYPE,
				VisVarConstants.VIS_VAR_ARC_COLOR);
	}

	/**
	 * @see MainViewScriptingBean#setArcStyleByType(String, String)
	 */
	public void setArcStyleByType(String arcType, String style) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			ArcStyle arcStyle = displayBean.getArcStyle(style);
			if (arcStyle != null) {
				setVisVarValueForType(arcType, arcStyle, AttributeConstants.NOM_ATTR_REL_TYPE,
						VisVarConstants.VIS_VAR_ARC_STYLE);
			} else {
				throw new NullPointerException("Arc style '" + style + "' doesn't exist.");
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Shifts nodes relative to their current position.
	 * @see MainViewScriptingBean#moveSelectedNodesBy(double, double)
	 */
	public void moveSelectedNodesBy(double dx, double dy) {
		if ((dx != 0) || (dy != 0)) {
			setSelectedNodeBounds(dx, dy, 0, 0, true, true);
		}
	}

	/**
	 * Moves nodes to the given absolute position.
	 * @see MainViewScriptingBean#moveSelectedNodesTo(double, double)
	 */
	public void moveSelectedNodesTo(double x, double y) {
		setSelectedNodeBounds(x, y, 0, 0, false, true);
	}

	/**
	 * Resize nodes relative to their current size.
	 * @see MainViewScriptingBean#resizeSelectedNodesBy(double, double)
	 */
	public void resizeSelectedNodesBy(double dw, double dh) {
		if ((dw != 0) || (dh != 0)) {
			setSelectedNodeBounds(0, 0, dw, dh, true, true);
		}
	}

	/**
	 * Resize nodes to the given absolute size.
	 * @see MainViewScriptingBean#resizeSelectedNodesTo(double, double)
	 */
	public void resizeSelectedNodesTo(double w, double h) {
		setSelectedNodeBounds(0, 0, w, h, true, false);
	}

	////////////////////////////////////
	// Private and Protected Methods
	////////////////////////////////////

	/**
	 * Sets a generic value (color, string, long etc) in the
	 * {@link AttrToVisVarBean} for the given vis var name.
	 * @param type the node or arc type
	 * @param value the value to set
	 * @param attrName the attribute name (see {@link AttributeConstants})
	 * @param visVarName the vis var name (see {@link VisVarConstants})
	 */
	protected void setVisVarValueForType(String type, Object value, String attrName, String visVarName) {
		try {
			if ((type == null) || (type.length() == 0)) {
				throw new NullPointerException("Invalid type");
			}
			if (value == null) {
				throw new NullPointerException("Invalid value");
			}
			AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
			attrToVisVarBean.setNominalVisualVariableValue(attrName, visVarName, type, value);

			// need to repaint to update the display
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.repaint();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/**
	 * Sets a color value in the {@link AttrToVisVarBean} for the given vis var name.
	 * @param type the node or arc type
	 * @param color the color string - will be parsed into a {@link Color}
	 * @param attrName the attribute name (see {@link AttributeConstants})
	 * @param visVarName the vis var name (see {@link VisVarConstants})
	 */
	protected void setVisVarColorForType(String type, String color, String attrName, String visVarName) {
		try {
			Color c = GraphicsUtils.stringToColor(color);
			if (c != null) {
				setVisVarValueForType(type, c, attrName, visVarName);
			} else {
				throw new NullPointerException("Invalid color: " + color);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	/**
	 * Sets the bounds on the selected nodes.
	 * The position and size can each be relative or absolute.
	 */
	protected void setSelectedNodeBounds(double x, double y, double w, double h,
			boolean relativeLocation, boolean relativeSize) {
		try {
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector selected =  (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			Vector positions = new Vector(selected.size());
			Vector sizes = new Vector(selected.size());
			for (Iterator iter = selected.iterator(); iter.hasNext(); ) {
				PShrimpNode node = (PShrimpNode) iter.next();
				// NOTE: x and y are at the center of the node, not top left corner
				double newX = x;
				double newY = y;
				double newW = w;
				double newH = h;
				// these bounds are relative to the parent
				Rectangle2D.Double relativeBounds = node.getFullBounds();
				if (relativeLocation) {
					newX = relativeBounds.x + x;
					newY = relativeBounds.y + y;
				}
				if (relativeSize) {
					newW = relativeBounds.width + w;
					newH = relativeBounds.height + h;
				}
				// the positions have to be shifted relative to the center of the node
				newX += (newW / 2);
				newY += (newH / 2);
				// make sure the size is not smaller than the minimum node size
				newW = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, newW);
				newH = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, newH);

				positions.add(new Point2D.Double(newX, newY));
				sizes.add(new DoubleDimension(newW, newH));
			}
			displayBean.setPositionsAndSizes(selected, positions, sizes, true);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


}