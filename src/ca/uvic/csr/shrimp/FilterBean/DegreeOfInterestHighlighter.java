/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;


/**
 * A filter that doesn't filter.  Instead it highlights (using colors and fonts) the 
 * interesting nodes and fades (using transparency) the uninteresting nodes.
 * It uses a {@link InterestFilter} object to determine if an object is interesting.
 * 
 * @author Chris Callendar
 */
public class DegreeOfInterestHighlighter implements Filter {

	private static final String NAME = "Degree Of Interest Highlighter";
	
	private InterestFilter interest;
	private boolean applied = false;
		
	public DegreeOfInterestHighlighter(InterestFilter interest) {
		this.interest = interest;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}
	
	public boolean isApplied() {
		return applied;
	}
	
	public String toString() {
		return NAME;
	}
	
	public String getFilterType() {
		return FilterConstants.DEGREE_OF_INTEREST_HIGHLIGHTER_TYPE;
	}

	public String getTargetType() {
		return FilterConstants.ARTIFACT_FILTER_TYPE;
	}

	/**
	 * Always returns false.  This class doesn't do any filtering.
	 * Instead it sets 2 boolean attributes on the artifact indicating if the 
	 * artifact is interesting and a landmark.  
	 */
	public boolean isFiltered(Object object) {
		if (object instanceof Artifact) {
			Artifact artifact = (Artifact) object;
			if (!applied) {
				// remove the interest and landmark attribute
				artifact.setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_INTERESTING, null);
				artifact.setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LANDMARK, null);
			} else {
				boolean interesting = interest.isInteresting(artifact);
				boolean landmark = interesting && interest.isLandmark(artifact);
				// set the interest and landmark attributes to true or false
				artifact.setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_INTERESTING, Boolean.toString(interesting));
				artifact.setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LANDMARK, Boolean.toString(landmark));
			}						
			
		}
		return false;
	}

}
