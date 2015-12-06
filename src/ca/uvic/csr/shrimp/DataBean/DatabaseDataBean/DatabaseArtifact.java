/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.DatabaseDataBean;

import java.awt.Component;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.AbstractArtifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;

/**
 * Represents some "artifact" or object in a database.
 * This would most likely be an instance of an "entity" in an ER diagram,
 * or perhaps the entity itself.
 *
 * @author Rob Lintern
 */
public class DatabaseArtifact extends AbstractArtifact {

    /**
     * @param db
     * @param name
     * @param type
     * @param externalId
     */
    public DatabaseArtifact(DataBean db, String name, String type, Object externalId) {
        super(db, name, type, externalId);
    }

    /**
     * @see AbstractArtifact#createPanel(java.lang.String)
     */
    protected Component createPanel(String panelName) {
        // probably don't need to worry about this for now if calling from CreoleDataBean
        return null;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        // probably don't need to worry about this for now if calling from CreoleDataBean
        // TODO - may want to give the clone a different id?
        return new DatabaseArtifact (getDataBean(), getName(), getType(), getExternalId());
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#getDefaultPanelModeOrder()
     */
    public String[] getDefaultPanelModeOrder() {
        // probably don't need to worry about this for now if calling from CreoleDataBean
        return new String[0];
    }

    /**
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#getCustomizedPanelNames()
     */
    public Vector getCustomizedPanelNames() {
        // probably don't need to worry about this for now if calling from CreoleDataBean
        return new Vector(0);
    }

}
