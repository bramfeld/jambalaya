/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter;
 
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;

/**
 * This adapter listens to changes in the selector bean for when new  nodes are selected.
 * It highlights the nodes and enables/disables appropriate menus.
 * This class tries to first use the ActionManager from the ShrimpTool,
 * if that is null then it uses the project ActionManager.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class SelectedNodesChangeAdapter implements PropertyChangeListener {
	
	private ShrimpTool tool;
	private ActionManager actionManager;
	
	public SelectedNodesChangeAdapter(ShrimpTool tool, ActionManager actionManager) {
		this.tool = tool;
		this.actionManager = actionManager;
	}
	
	public void propertyChange(PropertyChangeEvent evt) {
		if (tool != null) {
			try {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				
				Vector oldSelected = (Vector) evt.getOldValue();
				Vector newSelected = (Vector) evt.getNewValue();
				if (oldSelected == null) {
				    oldSelected = new Vector(0);
				}
				if (newSelected == null) {
					newSelected = new Vector(0);
				}
				displayBean.highlight(oldSelected, false);
				displayBean.highlight(newSelected, true);
				
				updateActions(displayBean, newSelected);
				lowerDeselected(displayBean, oldSelected);
				raiseSelected(displayBean, newSelected);
				
				// (un)highlight equivalent nodes (that is, nodes that represent the same artifact
				Vector oldEquivNodes = getVisibleEquivalentNodes(displayBean, oldSelected);
				for (Iterator iter = oldEquivNodes.iterator(); iter.hasNext();) {
		            ShrimpNode oldEquiv = (ShrimpNode) iter.next();
		            oldEquiv.setEquivalentNodeSelected(false);
		        }
				Vector newEquivNodes = getVisibleEquivalentNodes(displayBean, newSelected);
				for (Iterator iter = newEquivNodes.iterator(); iter.hasNext();) {
		            ShrimpNode newEquiv = (ShrimpNode) iter.next();
		            newEquiv.setEquivalentNodeSelected(true);
		        }
			} catch (BeanNotFoundException bnfe) {
			  	//bnfe.printStackTrace();
			}
		}
		
	}
	
	private Vector getVisibleEquivalentNodes(DisplayBean displayBean, Vector selectedNodes) {
	    Vector allEquivalentNodes = new Vector();
	    for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
            ShrimpNode selectedNode = (ShrimpNode) iter.next();
    	    Vector equivNodes = displayBean.getDataDisplayBridge().getShrimpNodes(selectedNode.getArtifact(), false);
    	    for (Iterator iterator = equivNodes.iterator(); iterator.hasNext();) {
                ShrimpNode equivNode = (ShrimpNode) iterator.next();
                if (equivNode.isVisible() && !equivNode.equals(selectedNode)) {
                    allEquivalentNodes.add(equivNode);
                }
            }
        }
	    return allEquivalentNodes;
	}
	
	private void lowerDeselected(DisplayBean displayBean, Vector oldSelected) {
		Set allArcs = new HashSet ();
		for (Iterator iter = oldSelected.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
			allArcs.addAll(arcs);
		}
		for (Iterator iter = allArcs.iterator(); iter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			//((PNestedDisplayBean)displayBean).returnToNormalLayer(arc);
			arc.setActive(false);
		}
	}
	
	private void raiseSelected(DisplayBean displayBean, Vector newSelected) {
		Set allArcs = new HashSet ();
		for (Iterator iter = newSelected.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			node.raiseAboveSiblings();
			Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
			allArcs.addAll(arcs);
		}
		for (Iterator iter = allArcs.iterator(); iter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			//((PNestedDisplayBean)displayBean).bringToTopLayer(arc);
			arc.setActive(true);
		}
	}

	private void updateActions(DisplayBean displayBean, Vector newSelected) {
		boolean someSelected = !newSelected.isEmpty();
		boolean oneHasChildren = false;
		
		Iterator iterator = newSelected.iterator();
		while (iterator.hasNext() && !oneHasChildren) {
			ShrimpNode node = (ShrimpNode) iterator.next();
			
			// check if the node is in CHILDREN panelMode and if it has at least 1 child
			if (displayBean.getPanelMode(node).equals(PanelModeConstants.CHILDREN) && displayBean.getDataDisplayBridge().getChildNodesCount(node) > 0) {
				oneHasChildren = true;
			}
		}
		
		if (actionManager != null) {
			actionManager.setActionEnabled(ShrimpConstants.MENU_NODE, "", true);
			// allows layout of roots when no nodes selected
			actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_ARRANGE_CHILDREN, ShrimpConstants.MENU_NODE, true);
			actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_ARRANGE_SELECTED_ITEMS, ShrimpConstants.MENU_NODE, someSelected);
			
			actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_SELECT_ALL_CHILDREN, ShrimpConstants.MENU_NODE_SELECT, someSelected && oneHasChildren);
			actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_SELECT_ALL_DESCENDANTS, ShrimpConstants.MENU_NODE_SELECT, someSelected && oneHasChildren);
			actionManager.setActionEnabled(ShrimpConstants.ACTION_NAME_INVERSE_SIBLINGS, ShrimpConstants.MENU_NODE_SELECT, someSelected);
			
			//actionManager.setActionEnabled("Incoming Arcs", ShrimpConstants.MENU_NODE_SELECT, someSelected);
			//actionManager.setActionEnabled("Outgoing Arcs", ShrimpConstants.MENU_NODE_SELECT, someSelected);
			//actionManager.setActionEnabled("Incoming and Outgoing Arcs", ShrimpConstants.MENU_NODE_SELECT, someSelected);
			
			//actionManager.setActionEnabled("Incoming and Outgoing Nodes", ShrimpConstants.MENU_NODE_SELECT, someSelected);
			//actionManager.setActionEnabled("Incoming Nodes", ShrimpConstants.MENU_NODE_SELECT, someSelected);
			//actionManager.setActionEnabled("Outgoing Nodes", ShrimpConstants.MENU_NODE_SELECT, someSelected);
		}
	}
}