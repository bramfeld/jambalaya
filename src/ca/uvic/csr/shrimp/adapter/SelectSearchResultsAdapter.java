/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.FilterResultsActionEvent;
import ca.uvic.csr.shrimp.SearchBean.FilterResultsActionListener;
import ca.uvic.csr.shrimp.SearchBean.SelectResultsActionEvent;
import ca.uvic.csr.shrimp.SearchBean.SelectResultsActionListener;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Selects (by id) the node search results selected in the node search window.
 * 
 * @author Rob Lintern
 */
public class SelectSearchResultsAdapter implements SelectResultsActionListener {

	//private SelectorBean selectorBean;
	private String toolName;
	private ShrimpProject project;
	//private ShrimpTool tool;

	/**
	 * Constructor for SelectSearchResultsAdapter.
	 */
	public SelectSearchResultsAdapter(ShrimpProject project, String toolName) {
		this.project = project;
		this.toolName = toolName;
	}

	/**
	 * @see FilterResultsActionListener#filterResults(FilterResultsActionEvent)
	 */
	public void selectResults(SelectResultsActionEvent srae) {
		if (!(srae.getSearchStrategy() instanceof ArtifactSearchStrategy)) 
			return;

		if (project != null) {
			try {				
				ShrimpTool tool = project.getTool(toolName);	
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);		
				DisplayBean displayBean = (DisplayBean) tool.getBean (ShrimpTool.DISPLAY_BEAN);
				DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
				Vector artifactsToSelect = srae.getResultsToSelect();
				Vector nodesToSelect = dataDisplayBridge.getShrimpNodes(artifactsToSelect, false);
				//remove any nodes that are not visible
				for (Iterator iter = ((Vector)nodesToSelect.clone()).iterator(); iter.hasNext();) {
					ShrimpNode nodeToSelect = (ShrimpNode) iter.next();
					if (!displayBean.isVisible(nodeToSelect)) {
						nodesToSelect.remove(nodeToSelect);
					}
				}
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, nodesToSelect);		
			} catch (ShrimpToolNotFoundException stnfe) {
				stnfe.printStackTrace();
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}	
		}
	}

}
