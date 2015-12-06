/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Comparator;

import ca.uvic.csr.shrimp.DataBean.Artifact;

/**
 * Comparator for sorting artifact by name in alphabetical order, ignoring case.
 * 
 * @author Chris Callendar
 */
public class ArtifactNameComparator implements Comparator {
    
 	public int compare(Object obj1, Object obj2){
	    if (!(obj1 instanceof Artifact) || !(obj2 instanceof Artifact)) {
	        System.err.println("not comparing Artifacts");
	        return 0;
	    }
	    Artifact art1 = (Artifact)obj1;
	    Artifact art2 = (Artifact)obj2;
		String name1 = art1.getName();
		String name2 = art2.getName();
		return name1.compareToIgnoreCase(name2);
	}			
}