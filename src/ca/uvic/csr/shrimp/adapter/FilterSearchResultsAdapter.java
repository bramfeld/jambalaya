/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.FilterResultsActionEvent;
import ca.uvic.csr.shrimp.SearchBean.FilterResultsActionListener;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Filters (by id) the node search results selected in the node search window.
 * 
 * @author Rob Lintern
 */
public class FilterSearchResultsAdapter implements FilterResultsActionListener {
	
	private FilterBean filterBean;
	private String toolName;
	private ShrimpProject project;
	private ShrimpTool tool;

	/**
	 * Constructor for SelectSearchResultsAdapter.
	 */
	public FilterSearchResultsAdapter(ShrimpProject project, String toolName) {
		this.project = project;
		this.toolName = toolName;
	}

	/**
	 * @see FilterResultsActionListener#filterResults(FilterResultsActionEvent)
	 */
	public void filterResults(FilterResultsActionEvent event) {
		try {
			tool = project.getTool(toolName);
			filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
		} catch (ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
		
		if (!(event.getSearchStrategy() instanceof ArtifactSearchStrategy)) {
			return;
		}

		Vector resultsToFilter = event.getResultsToFilter();
		Vector valuesToFilter = new Vector(resultsToFilter.size());
		for (Iterator iterator = resultsToFilter.iterator(); iterator.hasNext();) {
			Artifact artifact = (Artifact) iterator.next();
			valuesToFilter.add(new Long(artifact.getID()));
		}
		
		boolean add = event.isFiltered();
		
		filterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_ID,
			Long.class, FilterConstants.ARTIFACT_FILTER_TYPE, valuesToFilter, add);
	}

}
