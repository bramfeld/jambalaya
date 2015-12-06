/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 *
 * @author Rob Lintern, Chris Callendar
 */
public class SelectInverseSiblingsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_INVERSE_SIBLINGS;

	/**
	 * Constructor for SelectInverseSiblingsAdapter.
	 */
	public SelectInverseSiblingsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			Vector selectedNodes = getSelectedNodes();
            Vector newSelectedNodes = new Vector();
			if (!selectedNodes.isEmpty()) {
                if (displayBean.getCprels().length == 0) {
                    newSelectedNodes = displayBean.getDataDisplayBridge().getRootNodes();
                    newSelectedNodes.removeAll(selectedNodes);
                } else {
    				// Group all the sibling together
    				Map parentNodeIdToSelectedChildrenMap = new HashMap();
    				for (int i = 0; i < selectedNodes.size(); i++) {
    					ShrimpNode node = (ShrimpNode)selectedNodes.get(i);
    					ShrimpNode parentNode = node.getParentShrimpNode();
    					if (parentNode != null ) { // weeds out the root
    						Long parentNodeId = new Long(node.getParentShrimpNode().getID());

    						Vector nodes = (Vector) parentNodeIdToSelectedChildrenMap.get(parentNodeId);
    						if(nodes == null) {
    							nodes = new Vector();
                                parentNodeIdToSelectedChildrenMap.put(parentNodeId, nodes);
    						}
    						nodes.add(node);
    					}
    				}

                    for (Iterator iter = parentNodeIdToSelectedChildrenMap.values().iterator(); iter.hasNext();) {
                        Vector nodes = (Vector) iter.next();
    					ShrimpNode firstNode = (ShrimpNode)nodes.firstElement();
    					ShrimpNode parentNode = firstNode.getParentShrimpNode();
    					Vector siblings = displayBean.getDataDisplayBridge().getChildNodes(parentNode);
    					siblings.removeAll(nodes);
                        newSelectedNodes.addAll(siblings);
    				}
                }
				setSelectedNodes(newSelectedNodes);
			}
		} catch (BeanNotFoundException e) {
            e.printStackTrace();
		}
	}

}
