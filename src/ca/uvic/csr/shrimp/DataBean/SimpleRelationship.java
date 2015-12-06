/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.util.Vector;


/**
 * @author Rob Lintern
 */
public class SimpleRelationship extends AbstractRelationship {

	/**
	 * Constructor for SimpleRelationship.
	 * A unique id will be assigned to this relationship
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 */
	public SimpleRelationship(DataBean db, String name, String type, Vector artifacts) {
		super(db, name, type, artifacts);
	}

	/**
	 * Constructor for SimpleRelationship.
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 */
	public SimpleRelationship(DataBean db, String name, String type, Vector artifacts, Object externalId) {
		super(db, name, type, artifacts, externalId);
	}

}
