/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.DatabaseDataBean;

import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.AbstractRelationship;
import ca.uvic.csr.shrimp.DataBean.DataBean;

/**
 * Represents some "relationship" between "artifacts" or objects in a database.
 * This would most likely be an instance of an "relationship" in an ER diagram,
 * or perhaps the relationship itself.
 *
 * @author Rob Lintern
 */
public class DatabaseRelationship extends AbstractRelationship {

    /**
     * @param db
     * @param name
     * @param type
     * @param artifacts
     */
    public DatabaseRelationship(DataBean db, String name, String type, Vector artifacts, Object id) {
        super(db, name, type, artifacts, id);
    }

}
