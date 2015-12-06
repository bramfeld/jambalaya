/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.util.List;
import java.util.Vector;



/**
 * Extends {@link SimpleDataBean} to contain cprels, node, and arc types specific to OBO files.
 * 
 * @author Chris Callendar
 */
public class OBODataBean extends SimpleDataBean {

	public static final String IS_A_REL_TYPE = "is_a";
	public static final String PART_OF_REL_TYPE = "part_of";
	public static final String DEVELOPS_FROM_REL_TYPE = "develops_from";
	public static final String START_REL_TYPE = "start";
	public static final String END_REL_TYPE = "end";
 
	private final static String[] DEFAULT_CPRELS = {"is_a", "part_of", "develops_from"};
    private final static boolean DEFAULT_CPRELS_INVERTED = false;

	public static final String TERM_ART_TYPE= "obo:TERM";
	public static final String TYPE_ART_TYPE = "obo:TYPE";
	
	public static final String IS_A_HIERARCHY = "IS_A Hierarchy";
	public static final String PART_OF_HIERARCHY = "PART_OF Hierarchy";
	
	public void setData(List artifacts, List rels) {
		super.setData(artifacts, rels);
		
		Vector types = findRelationshipTypesInBackEnd();
        if (types.contains(DEFAULT_CPRELS[0]) || types.contains(DEFAULT_CPRELS[1]) || types.contains(DEFAULT_CPRELS[2])) {
        	defaultCprels = DEFAULT_CPRELS;
        	defaultCprelsInverted = DEFAULT_CPRELS_INVERTED;
        }
		
	}

}
