/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;

/**
 * A filter that removes grouped nodes from the display
 *
 * @tag Shrimp(Collapse)
 * @author Chris Callendar
 * @date November 24th, 2006
 */
public class CollapsedNodeFilter implements Filter {

	private static final String NAME = "Collapsed Node Filter";

	private HashMap/*<Long, Set>*/ artifactsToMapOfIDs;

	public CollapsedNodeFilter() {
		this.artifactsToMapOfIDs = new HashMap();
	}

	public CollapsedNodeFilter(Long collapsedArtifactID, Set artifactIDs) {
		this();
		collapseArtifact(collapsedArtifactID, artifactIDs);
	}

	/**
	 * Adds all the ids to the collection of artifacts to filter.
	 * @param ids
	 */
	public void collapseArtifact(Long collapsedArtifactID, Set ids) {
		artifactsToMapOfIDs.put(collapsedArtifactID, ids);
	}

	public void expandArtifact(Long artifactID, Set ids) {
		if (artifactsToMapOfIDs.containsKey(artifactID)) {
			HashSet set = (HashSet) artifactsToMapOfIDs.get(artifactID);
			set.removeAll(ids);
			if (set.size() == 0) {
				artifactsToMapOfIDs.remove(artifactID);
			}
		}
	}

	public String toString() {
		return NAME;
	}

	public String getFilterType() {
		return FilterConstants.COLLAPSED_NODE_FILTER_TYPE;
	}

	/**
	 * This is an attribute filter
	 */
	public String getTargetType() {
		return FilterConstants.ARTIFACT_FILTER_TYPE;
	}

	/**
	 * Determines if the specified artifact should be filtered.
	 */
	public boolean isFiltered(Object object) {
		boolean filtered = false;
		if (object instanceof Artifact) {
			Artifact artifact = (Artifact) object;
			Long id = new Long(artifact.getID());
			for (Iterator iter = artifactsToMapOfIDs.keySet().iterator(); iter.hasNext(); ) {
				Set ids = (Set) artifactsToMapOfIDs.get(iter.next());
				if (ids.contains(id)) {
					filtered = true;
					break;
				}
			}
		}
		return filtered;
	}

	/**
	 * @return the number of artifacts being filtered
	 */
	public int getFilterCount() {
		return artifactsToMapOfIDs.size();
	}

}
