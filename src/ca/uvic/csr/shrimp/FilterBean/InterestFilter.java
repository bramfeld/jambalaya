/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import ca.uvic.csr.shrimp.DataBean.Artifact;


/**
 * Interface for determining if an artifact is interesting or if it is  
 * a landmark (very interesting).
 * 
 * @author Chris Callendar
 */
public interface InterestFilter {

	/**
	 * Determines if the artifact should be filtered.
	 * This will usually depend on whether the artifact is interesting, 
	 * but certain artifacts might be un-interesting but still not be filtered.
	 * @param artifact
	 * @return boolean
	 */
	public boolean isFiltered(Artifact artifact);
	
	/**
	 * Determines if an artifact is interesting.
	 * @param artifact
	 * @return boolean
	 */
	public boolean isInteresting(Artifact artifact);
	
	/**
	 * Determines if an artifact is a  landmark.
	 * @param artifact
	 * @return boolean
	 */
	public boolean isLandmark(Artifact artifact);
	
}
