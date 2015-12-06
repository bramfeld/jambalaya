/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JPanel;

import ca.uvic.csr.shrimp.AttributeConstants;

/**
 * An implementation of some of the Relationship interface for use in any domain.
 *
 * @author Rob Lintern
 */
public class AbstractRelationship implements Relationship {

	protected long id;
	protected Object externalId;
	private String name;
	private Map customizedPanels;
	private Map attributes;
	protected DataBean dataBean;
	private Vector artifacts;

	/**
	 * A unique id will automatically be assigned to the relationship
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 */
	public AbstractRelationship(DataBean db, String name, String type, Vector artifacts) {
	    this (db, name, type, artifacts, null);
	}

	/**
	 * @param db
	 * @param name
	 * @param type
	 * @param artifacts
	 * @param externalId
	 */
	public AbstractRelationship(DataBean db, String name, String type, Vector artifacts, Object externalId) {
		this.dataBean = db;
		this.name = name;
		this.artifacts = artifacts;
		this.externalId = externalId;
		id = ((AbstractDataBean)db).nextID();
		customizedPanels = new HashMap();
		attributes = new HashMap();
		setAttribute(AttributeConstants.NOM_ATTR_REL_TYPE, type);
    }

	/**
     * @see ca.uvic.csr.shrimp.DataBean.Relationship#getDataBean()
     */
    public DataBean getDataBean() {
        return dataBean;
    }

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getID()
	 */
	public long getID() {
		return id;
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.Relationship#getExternalId()
     */
    public Object getExternalId() {
        return externalId;
    }

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.Relationship#setID(java.lang.Object)
     */
//    public void setID(Object id) {
//        this.id = id;
//    }

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getType()
	 */
	public String getType() {
		return (String) getAttribute(AttributeConstants.NOM_ATTR_REL_TYPE);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#setType(String)
	 */
	public void setType(String type) {
		setAttribute(AttributeConstants.NOM_ATTR_REL_TYPE, type);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getArtifacts()
	 */
	public Vector getArtifacts() {
		return artifacts;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String attrName) {
		return (attrName != null ? attributes.containsKey(attrName) : false);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attrName) {
		return (attrName != null ? attributes.get(attrName) : null);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attrName, Object attrValue) {
		if (attrName != null) {
			if (attrValue != null) {
				attributes.put(attrName, attrValue);
			} else {
				attributes.remove(attrName);
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getAttributeNames()
	 */
	public Vector getAttributeNames() {
		return new Vector(attributes.keySet());
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getCustomizedPanel(java.lang.String)
	 */
	public JPanel getCustomizedPanel(String mode) {
		return (JPanel)customizedPanels.get(mode);
	}


	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#setCustomizedPanel(java.lang.String, javax.swing.JPanel)
	 */
	public void setCustomizedPanel(String mode, JPanel panel) {
		if (panel != null) {
			customizedPanels.put(mode,panel);
		} else {
			attributes.remove (mode);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Relationship#getCustomizedPanelNames()
	 */
	public Vector getCustomizedPanelNames() {
		Vector names = new Vector(customizedPanels.keySet());
		return names;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Two relationships are equal if they have the same id.
	 * In other words, rel1 equals rel2 if rel1.getID().equals(rel2.getID())
	 */
	public boolean equals(Object obj) {
//	    boolean equal = false;
		if (obj instanceof Relationship) {
//			Relationship that = (Relationship)obj;
//		    boolean equalById = that.getID() == this.getID();
//		    Object thatExternalId = that.getExternalId();
//		    Object thisExternalId = this.getExternalId();
//		    boolean equalByExternalId = thatExternalId != null && thisExternalId != null && thatExternalId.equals(thisExternalId);
//		    boolean equalByOther = CollectionUtils.haveSameElements(that.getArtifacts(), this.getArtifacts()) &&
//		    						that.getName().equals(this.getName()) &&
//		    						that.getType().equals(this.getType());
//		    if (equalByOther && !equalById) {
//		        System.err.println("Relationship is equal arts, type, and name but not by id!");
//		    }
//		    if (equalByExternalId && !equalById) {
//		        System.err.println("Relationship is equal by external id, but not by id!");
//		    }
//		    equal = equalById;
            return ((Relationship)obj).getID() == getID();
	    }
//        return equal;
		return false;
 	}

	/**
	 * @see Relationship#hashCode()
	 */
	public int hashCode() {
	    if (id > Integer.MAX_VALUE) {
	        System.err.println("Warning: Converting from long to int when id greater than Integer.MAX_VALUE");
	    }
		return (int) id;
	}

	/**
	 * @see Relationship#toString
	 * @return the name and type of this relationship
	 */
	public String toString() {
		return getName();
	}
}
