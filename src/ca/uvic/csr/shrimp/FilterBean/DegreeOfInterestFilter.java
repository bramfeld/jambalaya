/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;


/**
 * This class filters artifacts that aren't interesting.  
 * It uses a {@link InterestFilter} object to determine if an object is interesting.
 * 
 * @author Chris Callendar
 */
public class DegreeOfInterestFilter implements Filter {

	private static final String NAME = "Degree Of Interest Filter";

	private InterestFilter interest;
	
	public DegreeOfInterestFilter(InterestFilter interest) {
		this.interest = interest;
	}

	public String toString() {
		return NAME;
	}
	
	public String getFilterType() {
		return FilterConstants.DEGREE_OF_INTEREST_FILTER_TYPE;
	}

	public String getTargetType() {
		return FilterConstants.ARTIFACT_FILTER_TYPE;
	}

	public boolean isFiltered(Object object) {
		boolean filtered = false;
		if (object instanceof Artifact) {
			Artifact artifact = (Artifact) object;
			filtered = interest.isFiltered(artifact); 
		}
		return filtered;
	}

}
