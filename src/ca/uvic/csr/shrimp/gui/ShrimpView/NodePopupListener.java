/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JPopupMenu;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.NodeDocument;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.PanelModeChangeAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.ExpandCollapseSubgraphAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveDocumentFromNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveNodeLabelIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.util.PopupListener;


/**
 * Updates the Nodes menu before it is displayed.
 * Moved from being an inner class of {@link ShrimpView}.
 *
 * @author Chris Callendar
 * @date 26-Oct-06
 */
public class NodePopupListener extends PopupListener {

	private ShrimpView shrimpView;
	private String nodeType = "";
	private Vector addedActions = null;	// remembers actions that get added on popup

	public NodePopupListener(JPopupMenu popup, ShrimpView shrimpView) {
		super(popup);
		this.shrimpView = shrimpView;
		this.addedActions = new Vector();

		repopulateNodePopupMenu();
	}

	protected boolean beforeShowPopup(MouseEvent e) {
		boolean show = false;

		DisplayBean displayBean = null;
		SelectorBean selectorBean = null;
		try {
			displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		Vector targets = new Vector();
		Object currentTarget = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);

		//Do not show this menu for arcs
		if (currentTarget == null || currentTarget instanceof ShrimpArc || currentTarget instanceof ShrimpLabel) {
			show = false;
		} else {
			ActionManager actionManager = shrimpView.getProject().getActionManager();
			boolean firing = actionManager.getFiringEvents();
			actionManager.setFiringEvents(false);

			// remove any previously added actions
			removeAddedActions();

			if (currentTarget instanceof ShrimpNode) {
				ShrimpNode node = (ShrimpNode) currentTarget;
				// If the target node is in the group of selected nodes then the selected nodes should be the targets.
				// If target node is not in the group of selected nodes then the target node should be the only target.
				Vector selected = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
				if (selected.contains(currentTarget)) {
					targets = selected;
				} else {
					targets.add(currentTarget);
					targets.addAll(selected);
					selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, targets);
				}

				//Remove previous node type select
				String actionName = "Select All '" + nodeType + "' Nodes";
				if (actionManager.getAction(actionName, ShrimpConstants.MENU_NODE_SELECT) != null) {
					actionManager.removeAction(actionName, ShrimpConstants.MENU_NODE_SELECT, ActionManager.IGNORE_PARENT);
				}

				//Add a menu option to select this node type
				nodeType = node.getArtifact().getType();
				ShrimpAction selectNodeTypesAction = new DefaultShrimpAction("Select All '" + nodeType + "' Nodes") {
					public void actionPerformed(ActionEvent e) {
						selectAllNodesByType();
					}
				};
				actionManager.addAction(selectNodeTypesAction, ShrimpConstants.MENU_NODE_SELECT, ShrimpConstants.GROUP_C, 1);

				// map of boolean values indexed by panelName indicating if all targets have this panel mode available
				Hashtable allHaveThisPanel = new Hashtable();
				// map of boolean values indexed by panelName indicating if at least one target has this panel mode available
				Hashtable oneHasThisPanel = new Hashtable();
				// map of boolean values indexed by panelName indicating if all targets are currently in this panel mode
				Hashtable allInThisPanelMode = new Hashtable();
				// map of boolean values indexed by panelName indicating if at least one target is in this panel mode
				Hashtable oneInThisPanelMode = new Hashtable();
				//build a list of all possible custom panel names
				for (int j = 0; j < targets.size(); j++) {
					ShrimpNode targetNode = (ShrimpNode)targets.elementAt(j);
					Vector allPanelNames = targetNode.getArtifact().getCustomizedPanelNames();
					allPanelNames.add(PanelModeConstants.CHILDREN);	// include children mode
					allPanelNames.add(PanelModeConstants.CLOSED);	// include closed mode
					for (Iterator iter = allPanelNames.iterator(); iter.hasNext();) {
						String panelName = (String) iter.next();
						allHaveThisPanel.put(panelName, new Boolean(true));
						oneHasThisPanel.put(panelName, new Boolean(false));
						allInThisPanelMode.put(panelName, new Boolean(true));
						oneInThisPanelMode.put(panelName, new Boolean(false));
					}
				}

				// check each target to see if it has a specific panel and if it is in that specific panel mode already
				for (Iterator iter = allHaveThisPanel.keySet().iterator(); iter.hasNext();) {
					String panelName = (String) iter.next();
					for (int j = 0; j < targets.size(); j++) {
						ShrimpNode targetNode = (ShrimpNode) targets.elementAt(j);
						Vector targetPanelNames = targetNode.getArtifact().getCustomizedPanelNames();
						int childrenCount = displayBean.getDataDisplayBridge().getVisibleChildNodeCount(targetNode);
						if (childrenCount > 0) {
							targetPanelNames.add(PanelModeConstants.CHILDREN);
						}
						targetPanelNames.add(PanelModeConstants.CLOSED);

						if (!targetPanelNames.contains(panelName)) {
							allHaveThisPanel.put(panelName, new Boolean(false));
						} else {
							oneHasThisPanel.put(panelName, new Boolean(true));
						}
						if (!displayBean.getPanelMode(targetNode).equals (panelName)) {
							allInThisPanelMode.put(panelName, new Boolean(false));
						} else {
							oneInThisPanelMode.put(panelName, new Boolean(true));
						}
					}
				}

				for (Iterator iter = allHaveThisPanel.keySet().iterator(); iter.hasNext();) {
					String panelName = (String) iter.next();
					boolean oneHasPanel = ((Boolean)oneHasThisPanel.get(panelName)).booleanValue();
					boolean allInThisMode = ((Boolean)allInThisPanelMode.get(panelName)).booleanValue();

					if (panelName.equals(PanelModeConstants.CHILDREN)) {
						// enable "show children" menu item if at least one target has children
						// and all targets are not in children mode
						Action action = actionManager.getAction(PanelModeConstants.CHILDREN, ShrimpConstants.MENU_NODE_SHOW);
						action = actionManager.getAction(ShrimpConstants.ACTION_NAME_EXPAND, ShrimpConstants.MENU_NODE);
						action.setEnabled (oneHasPanel && !allInThisMode);
					} else if (panelName.equals(PanelModeConstants.CLOSED)) {
						// enable close menus if not all targets closed already
						Action action = actionManager.getAction(ShrimpConstants.ACTION_NAME_COLLAPSE, ShrimpConstants.MENU_NODE);
						action.setEnabled(!allInThisMode);
						action = actionManager.getAction(ShrimpConstants.ACTION_NAME_COLLAPSE_ALL_DESCENDANTS, ShrimpConstants.MENU_NODE);
						action.setEnabled(!allInThisMode);
					} else {
						// Bold a custom panel menu item if all targets have that custom panel and all not in that panel mode.
						// Disable a custom panel menu item if all targets are already in that panel mode
						Action action = new PanelModeChangeAdapter(panelName, shrimpView);
						actionManager.addAction(action, ShrimpConstants.MENU_NODE_SHOW, ShrimpConstants.GROUP_A, 99);
						addedActions.add(action);	// remember this action to remove it later
					}
				}

				// @tag Shrimp.ArrangeChildren : if there is no hierarchy then disable this menu
				Action action = actionManager.getAction(ShrimpConstants.ACTION_NAME_ARRANGE_CHILDREN, ShrimpConstants.MENU_NODE);
				if (action != null) {
					action.setEnabled(!displayBean.isFlat());
				}

				// @tag Shrimp.Collapse: only allow the collapse/expand action for flat graphs
				if (displayBean.isFlat()) {
					action = new ExpandCollapseSubgraphAdapter(shrimpView, true);
					actionManager.addAction(action, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_B, 4);
					addedActions.add(action);
				}

				// @tag Shrimp.Group
				action = actionManager.getAction(ShrimpConstants.ACTION_NAME_UNGROUP, ShrimpConstants.MENU_NODE);
				if (action != null) {
					action.setEnabled(node.isGrouped() ||
						node.getArtifact().hasAttribute(SoftwareDomainConstants.NOM_ATTR_SUMMARY));
				}

				// @tag Shrimp.DocumentManager : add shortcuts for removing the attached documents (files/urls only)
				Artifact artifact = node.getArtifact();
				if (artifact.hasDocuments()) {
					// Node->Remove Documents (menu)
					DefaultShrimpAction removeAction = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_REMOVE_DOCUMENTS, ResourceHandler.getIcon("icon_attachment_delete.gif"));
					actionManager.addAction(removeAction, ShrimpConstants.MENU_NODE, ShrimpConstants.GROUP_E, 3);
					addedActions.add(removeAction);

					int i = 0;
					for (Iterator iter = artifact.getDocuments().iterator(); iter.hasNext(); ) {
						NodeDocument doc = (NodeDocument) iter.next();
						if (doc.canRemove()) {
							action = new RemoveDocumentFromNodeAdapter(shrimpView, doc);
							actionManager.addAction(action, ShrimpConstants.MENU_NODE_REMOVE_DOCUMENTS, ShrimpConstants.GROUP_A, i++);
							addedActions.add(action);
						}
					}
				}

				// @tag Shrimp.LabelIcon : update the action enablement
				action = actionManager.getAction(RemoveNodeLabelIconAdapter.ACTION_NAME, ShrimpConstants.MENU_NODE);
				if (action != null) {
					action.setEnabled(node.getIcon() != null);
				}

				// @tag Shrimp.OverlayIcon : update the action enablement
				action = actionManager.getAction(RemoveNodeOverlayIconAdapter.ACTION_NAME, ShrimpConstants.MENU_NODE);
				if (action != null) {
					action.setEnabled(node.getOverlayIconProvider() != null);
				}
			}

			//We have to repopulate the menu
			repopulateNodePopupMenu();

			// must reset the firing actions state
			actionManager.setFiringEvents(firing);

			show = true;
		}
		return show;
	}

	public void repopulateNodePopupMenu() {
		ShrimpProject project = shrimpView.getProject();
		if ((popup != null) && (project != null)) {
			project.getActionManager().repopulatePopUpMenu(ShrimpConstants.MENU_NODE, "", popup, "ShrimpView Node Popup");
		}
	}

	private void selectAllNodesByType() {
		try {
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
			Vector newSelected = new Vector();
			Vector visibleNodes = displayBean.getVisibleNodes();
			for (Iterator iter = visibleNodes.iterator(); iter.hasNext();) {
				ShrimpNode tmpNode = (ShrimpNode) iter.next();
				if (tmpNode.getArtifact().getType().equals(nodeType)) {
					newSelected.add(tmpNode);
				}
			}
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, newSelected);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

	/**
	 * Removes any extra added actions
	 */
	public void removeAddedActions() {
		ActionManager actionManager = shrimpView.getProject().getActionManager();
		while(addedActions.size() > 0) {
			Action action = (Action)addedActions.remove(0);
			actionManager.removeAction(action, ActionManager.IGNORE_PARENT);
		}
	}

}
