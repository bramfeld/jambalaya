/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.io.Serializable;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * This interface describes a relationship, which joins two or more
 * pieces of information in the Shrimp System.
 * Each data model can implement this connection to customize the relationships
 * avaliable to the system.  It is important that the data model utilizes this
 * interface to best join the model's data.  Note that a single system
 * can have different algorithms for nesting information.  Depending on
 * the algorithm used, the relationships in the system will be organized
 * differently.
 *
 * @author Casey Best
 * @date June 16, 2000
 */
public interface Relationship extends Serializable, Cloneable {

	/**
	 * Returns A unique identifier for this relationship
	 * The id should be set upon creation.  The id has been
	 * made an object to allow different domains to use any object
	 * to uniquely identify an artifact
	 * @return the id of this relationship
	 */
	public long getID();

	public Object getExternalId();

    /**
     * @param newID
     */
    //public void setID(Object id);

	/**
	 * Returns the name of this relationship.
	 * This can be anything, but should not be used to identify a relationship
	 * since two different relationships can have the same name.
	 * @return The name of this relationship.
	 */
	public String getName();
	/**
	 * Sets the name of this relationship.
	 * @param name The name to give this relationship
	 */
	public void setName(String name);

	/**
	 * Returns the type of this relationship.
	 * A group of relationships that have a similiar characteristic will most likely have the same type.
	 * @return the type of this relationship.
	 */
	public String getType();

	/**
	 * Sets the type of this relationship.
	 * @param type The type to give this relationship.
	 */
	public void setType(String type);

	/**
	 * Returns the artifacts that this relationship connects.
	 * Since it is possible for more than two artifacts in a relationship,
	 * a vector is returned.
	 *
	 * Note: Data Models that require a specific order can use the positions
	 * in the Vector to do so.
	 * Ex) In a directional Data Model, the first element may be related
	 *     to the second, but not the other way around.
	 *
	 * In our implementations of relationships so far, they only connect two artifacts:
	 * a parent or source artifact, and a child or destination artifact.
	 * <pre>
	 * Example:
	 * Relationship rel = dataBean.getRelationship("unique rel id");
	 * if (rel != null) {
	 * 		Artifact parent = (Artifact) rel.getArtifacts().elementAt(0);
	 * 		Artifact child = (Artifact) rel.getArtifacts().elementAt(1);
	 * 		System.out.println("Relationship " + rel.getName() + " goes from " + parent.getName() + " to " + child.getName() + ".";
	 * }
	 * </pre>
	 *
	 * @return A Vector of Artifacts that this relationship connects.
	 */
	public Vector getArtifacts();

	/**
	 * Returns true if the given attribute name exists for this relationship.
	 * @param attrName
	 * @return boolean
	 */
	public boolean hasAttribute(String attrName);

	/**
	 * @param attrName
	 * @return The attribute of this relationship with the given name,
	 * or null if this relationship has no such attribute.
	 */
	public Object getAttribute(String attrName);

	/**
	 * Sets the value of an attribute of this relationship.
	 *
	 * @param attrName
	 * @param attrValue
	 */
	public void setAttribute(String attrName, Object attrValue);

	/**
	 * @return The names of the attributes of this relationship.
	 */
	public Vector getAttributeNames();

	/**
	 * Returns different panels this relationship can display.
	 * Certain default display modes won't be found here (such
	 * as displaying children or the annotate panel).  This is here
	 * to allow different data models the opportunity to use model
	 * dependent displays.
	 *
	 * <pre>
	 *
	 * Ex) If the data model wishes to have a file or URL displayed
	 * in a textbox, the programmer would call:
	 *       jpanel = getCustomizedPanel("file");
	 *       jpanel = getCustomizedPanel("URL");
	 *
	 * The possible modes can be retrieved by calling:
	 *       panelNames = getCustomizedPanelNames();
	 * <./pre>
	 *
	 * @param panelName The name of the panel to get.
	 * @return A JPanel corresponding to the passed in panel name.
	 * Null is returned if this relationship has no panel with that name.
	 */
	public JPanel getCustomizedPanel(String panelName);

	/**
	 * Maps the given customized panel to the given panel name.
	 * @see #getCustomizedPanel(String)
	 *
	 *     If the panel name specified already has a panel associated with
	 *     it, the new mapping will overwrite the old one.
	 *
	 * @param panelName The panel name to map to.
	 * @param panel The new panel to associate with the given panel name
	 */
	public void setCustomizedPanel(String panelName, JPanel panel);

	/**
	 * Returns the names of available customized panels for this relationship.
	 * @see #getCustomizedPanel(String)
	 * @return A Vector of Strings containing the names of available customized panels for this relationship.
	 */
	public Vector getCustomizedPanelNames();

	public DataBean getDataBean();

}
