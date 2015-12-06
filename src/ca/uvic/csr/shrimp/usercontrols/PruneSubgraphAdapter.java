/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to prune (hide/filter) the selected nodes and all their connected
 * destination nodes. It uses the {@link DataDisplayBridge} to get the {@link ShrimpArc}
 * objects and recursively filters all the destination nodes.
 *
 * @tag Shrimp(Prune)
 * @author Chris Callendar
 * @date November 25th, 2006
 */
public class PruneSubgraphAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_PRUNE;
	public static final String TOOLTIP = "Prunes (hides) the selected node(s) and the connected subgraph.\nThis algorithm follows all outgoing arcs.";

	/**
	 * Constructs a new FilterAllDestinationNodesAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public PruneSubgraphAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
	    mustStartAndStop = false;
	}

	public void startAction() {
	    try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	    	Vector selectedNodes = getSelectedNodes();

	    	// iterate through the selected nodes and add all the subgraphs (outgoing connections only)
	    	boolean outgoing = true, incoming = false;
	    	Vector nodes = new Vector(selectedNodes);
			for (Iterator iter = selectedNodes.iterator(); iter.hasNext(); ) {
				ShrimpNode node = (ShrimpNode) iter.next();
				nodes.addAll(displayBean.getDataDisplayBridge().getShrimpNodeSubgraph(node, outgoing, incoming));
			}

			// now that we have all the nodes - add all their IDs to the set
	    	HashSet idsToFilter = new HashSet(nodes.size());
			for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
				ShrimpNode node = (ShrimpNode) iter.next();
				idsToFilter.add(new Long(node.getArtifact().getID()));
			}

			if (!idsToFilter.isEmpty()) {
				filterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_ID, Long.class,
						FilterConstants.ARTIFACT_FILTER_TYPE, idsToFilter, true);
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
	    }
	}

	public boolean isEnabled() {
    	Vector selectedNodes = getSelectedNodes();
    	return super.isEnabled() && (selectedNodes.size() > 0);
	}

}