/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.util.CollectionUtils;

/**
 * When something is filtered/unfiltered, this class updates the 
 * the display bean.
 */
public class DisplayFilterChangedAdapter implements FilterChangedListener {

	private ShrimpTool tool;

	public DisplayFilterChangedAdapter(ShrimpTool tool) {
		this.tool = tool;
	}
	
	public void filterChanged (FilterChangedEvent fce) {
		if (tool == null) {
			return;
		}
		try {				
			SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);		
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);							
			
			Vector changedFilters = fce.getChangedFilters();
			Vector addedFilters = fce.getAddedFilters();
			Vector removedFilters = fce.getRemovedFilters();
			
			Vector allFilters = new Vector (changedFilters.size() + addedFilters.size() + removedFilters.size());
			allFilters.addAll(changedFilters);
			allFilters.addAll(addedFilters);
			allFilters.addAll(removedFilters);
			boolean nodesNeedUpdating = false;
			boolean arcsNeedUpdating = false;
			for (Iterator iter = allFilters.iterator(); iter.hasNext();) {
				Filter filter = (Filter) iter.next();
				if (filter.getTargetType().equals(FilterConstants.ARTIFACT_FILTER_TYPE)) {
					nodesNeedUpdating = true;
				} else if (filter.getTargetType().equals(FilterConstants.RELATIONSHIP_FILTER_TYPE)) {
					arcsNeedUpdating = true;
				}
			}
			if (nodesNeedUpdating) {
				updateNodes(displayBean, filterBean);

				// see if any of the currently selected nodes should be deselected because of being filtered
				Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
				for (Iterator iter = ((Vector)selectedNodes.clone()).iterator(); iter.hasNext();) {
					ShrimpNode selectedNode = (ShrimpNode) iter.next();
					boolean selectedIsFiltered = false;
					for (Iterator iterator = addedFilters.iterator(); iterator.hasNext() && !selectedIsFiltered;) {
						Filter addedFilter = (Filter) iterator.next();
						selectedIsFiltered = addedFilter.isFiltered(selectedNode.getArtifact());
					}
					for (Iterator iterator = changedFilters.iterator(); iterator.hasNext() && !selectedIsFiltered;) {
						Filter changedFilter = (Filter) iterator.next();
						selectedIsFiltered = changedFilter.isFiltered(selectedNode.getArtifact());
					}
					if (selectedIsFiltered) {
						selectedNodes.remove(selectedNode);
					}
				}
				// update the selected nodes if different
				if (!CollectionUtils.haveSameElements(selectedNodes, (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES))) {
					selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);	
				}
			}
			
			if (arcsNeedUpdating) {
				updateArcs(displayBean);
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}				
	}
	
	private void updateNodes(DisplayBean displayBean, FilterBean filterBean) {
		Vector nodes = displayBean.getAllNodes();
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			boolean shouldBeFiltered = filterBean.isFiltered(node.getArtifact());
			boolean isVisible = displayBean.isVisible(node);
			if (shouldBeFiltered && isVisible || !shouldBeFiltered && !isVisible) {
				displayBean.setVisible(node, !shouldBeFiltered, true);
			}
		}
	}
	
	private void updateArcs(DisplayBean displayBean) {
		Vector arcs = displayBean.getAllArcs();
		for (Iterator iter = arcs.iterator(); iter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			arc.updateVisibility();
			ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpArcLabel(arc, false);
			if (label != null) {
			    label.updateVisibility();
			}
		}
	}
	
}