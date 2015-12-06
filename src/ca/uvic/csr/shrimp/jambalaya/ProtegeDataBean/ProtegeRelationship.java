/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.AbstractRelationship;
import ca.uvic.csr.shrimp.DataBean.DataBean;

/**
 * This class describes a relationship for the Protege Data Model.
 * Please see the Relationship interface for a definition of a 
 * relationship.
 *
 * @author Casey Best
 */
public class ProtegeRelationship extends AbstractRelationship {

	/**
	 * Constructor for ProtegeRelationship.
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 */
	public ProtegeRelationship(DataBean db, String name, String type, Vector artifacts) {
		super(db, name, type, artifacts);
	}
	
	/**
	 * Constructor for ProtegeRelationship.
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 */
	public ProtegeRelationship(DataBean db, String name, String type, Vector artifacts, Object externalId) {
		super(db, name, type, artifacts, externalId);
	}
	
}
