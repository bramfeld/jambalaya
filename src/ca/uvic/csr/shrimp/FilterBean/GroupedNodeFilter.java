/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. 
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;

/**
 * A filter that removes grouped nodes from the display
 * @tag Shrimp(grouping)
 * @author Chris Bennett
 */
public class GroupedNodeFilter implements Filter {
	
	private static final String NAME = "Grouped Node Filter";

	public String getFilterType() {
		return FilterConstants.GROUPED_NODE_FILTER_TYPE;
	}

	/**
	 * This is an attribute filter
	 */
	public String getTargetType() {
		return FilterConstants.ARTIFACT_FILTER_TYPE;
	}

	/**
	 * Determines if the specified artifact has a grouped attribute set to 'true'.
	 */
	public boolean isFiltered(Object object) {
		boolean filtered = false;
		if (object instanceof Artifact) {
			Artifact artifact = (Artifact) object;
			String grouped = (String)artifact.getAttribute(SoftwareDomainConstants.NOM_ATTR_GROUPED);
			filtered = (grouped != null && !grouped.equals(""));
		}
		return filtered;
	}
	
	public String toString() {
		return NAME;
	}
}
