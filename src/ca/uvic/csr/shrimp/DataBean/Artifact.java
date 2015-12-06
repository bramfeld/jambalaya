/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.awt.Component;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;

/**
 * This interface describes an Artifact, which is a container for a
 * single entity of information in the Shrimp system.  Each Data Model
 * can implement this container to customized the information it
 * contains.  It is important that the Data Model utilizes this
 * interface to best represent the model's data.
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 */
public interface Artifact extends Serializable, Cloneable {

	/**
	 * A unique identifier for this artifact
	 * The id should be set upon creation, and not changed after
	 * the artifact is added to a Data Bean.  The id has been
	 * made an object to allow different domains to use any object
	 * to uniquely identify an artifact
	 * @return The unique id of this artifact.
	 */
	public long getID();

	/*
	 * Sets the id of this artifact
	 * @param id The id to give this artifact. This should be unique.
	 */
	//public void setID (long id);

	/**
	 * This should be a unique id that never changes.
	 */
	public Object getExternalId();
	public String getExternalIdString();

	/**
	 * Returns the name of this artifact.
	 * @return the name of this artifact.
	 */
	public String getName();

	/**
	 * Set the name of the artifact.
	 * @param name The name to give this artifact.
	 */
	public void setName(String name);

	/**
	 * Returns the type of this artifact.
	 * Artifacts that have a similiar characteristics will most likely have the same type.
	 * @return the type of this artifact.
	 */
	public String getType();

	/**
	 * Set the type of this artifact.
	 * @param type The type to assign to this artifact.
	 */
	public void setType(String type);

	/**
	 * Returns the data bean that this artifact is contained within.
	 * @return  the data bean that this artifact is contained within.
	 */
	public DataBean getDataBean();

	/**
	 * @see DataBean#getParents(Artifact, String[])
	 * @param cprels
	 */
	public Vector getParents(String[] cprels);

	/**
	 * @see DataBean#getChildren(Artifact, String[])
	 * @param cprels
	 */
	public Vector getChildren(String[] cprels);

	/**
	 * @see DataBean#getSiblings(Artifact, String[])
	 * @param cprels
	 */
	public Vector getSiblings(String[] cprels);

	/**
	 * @see DataBean#getDescendents(Artifact, String[])
	 * @param cprels
	 */
	public Vector getDescendents(String[] cprels);

	/**
	 * @see DataBean#getAncestors(Artifact, String[])
	 * @param cprels
	 */
	public Vector getAncestors(String[] cprels);

	/**
	 * @see DataBean#getChildrenCount(Artifact, String[])
	 * @param cprels
	 */
	public int getChildrenCount(String[] cprels);

	/**
	 * @see DataBean#getChildrenCount(Artifact, String[])
	 * @param cprels
	 */
	public int getParentsCount(String[] cprels);

	/**
	 * @see DataBean#getDescendentsCount(Artifact, String[], boolean)
	 * @param cprels
	 * @param countArtifactMultipleTimes
	 */
	public int getDescendentsCount(String[] cprels, boolean countArtifactMultipleTimes);

	/**
	 * @see DataBean#getIncomingAndOutgoingRelationships(Artifact)
	 */
	public Vector getRelationships();

	/**
	 * Checks if the artifact contains the given attribute.
	 * @param attrName The name of the attribue
	 * @return boolean if the attribute exists
	 */
	public boolean hasAttribute(String attrName);

	/**
	 * Returns an attribute of this artifact
	 *
	 * Ex) URL url = (URL) getAttribute ("Source Code URI");
	 *
	 * @param attrName The name of the attribute.
	 * @return The value of the attribute, or
	 * null if this artifact does not have the specified attribute.
	 */
	public Object getAttribute(String attrName);

	/**
	 * Sets an attribute of this artifact
	 *
	 * @param attrName The name of the attribute to set.
	 * @param attrValue The value of the attribute to be stored.
	 */
	public void setAttribute(String attrName, Object attrValue);

	/**
	 * Returns the names of this artifact's attributes
	 * @return A Vector of Strings containing the names of this artifact's attributes
	 */
	public Vector getAttributeNames();

	/**
	 * Returns the order in which customized panels will be displyed by default.  This
	 * can be implemented in any way, allowing different artifact
	 * types to have different defaults.  If the desired panel mode isn't
	 * available, the next one in the list is used.  For example, the first
	 * default panel mode may be "Children", but if the artifact doesn't
	 * have children, it moves to the next in the list (maybe "Code").
	 *
	 * Assume the "Default" panel mode is always available.  Therefore the last
	 * element in your array should always be "Default" in case none of the others
	 * are available.
	 *
	 * @return An array of panel names in the order they should should be opened.
	 */
	public String[] getDefaultPanelModeOrder();

	/**
	 * Returns different panels this artifact can display.
	 * This method is here to allow different data models the opportunity to
	 * provide domain dependent display objects.f
	 *
	 * Ex) If the data model wishes to have a file or URL displayed
	 * in a textbox, the programmer would call:
	 *       jcomponent = getCustomizedPanel("filePanel");
	 *       jcomponent = getCustomizedPanel("URLPanel");
	 *
	 * The possible panels for this artifact can be retrieved by calling:
	 *       panelNames = getCustomizedPanelNames();
	 * @param panelName The name of the panel to get
	 * @return The Swing Component associated with the given panel name.
	 */
	public Component getCustomizedPanel(String panelName);

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
	public void setCustomizedPanel(String panelName, Component panel);

	/**
	 * Returns the names of available customized panels for this relationship.
	 * @see #getCustomizedPanel(String)
	 * @return A Vector of Strings containing the names of available customized panels for this artifact.
	 */
	public Vector getCustomizedPanelNames();

	/**
	 * Adds a listener for a given customized panel
	 *
	 * @param listener The object that will handle an action for the panel
	 */
	public void addCustomizedPanelListener(CustomizedPanelActionListener listener);

	/**
	 * Removes a listener for a given customized panel
	 *
	 * @param listener The object that will handle an action for the panel
	 */
	public void removeCustomizedPanelListener(CustomizedPanelActionListener listener);

	/**
	 * Returns whether or not this artifact is "equivalent" to the passed in artifact.
	 * Note: This may not be the same as calling #equals(Object) which will usually compare
	 * artifacts by their unique id's. This method may compare the attributes
	 * of this artifact with the given artifact only, ignoring id's altogether.
	 *
	 * @param artifact The artifact to compare to this artifact.
	 * @return whether or not this artifact is equivalent to the passed in artifact
	 */
	public boolean equivalent(Artifact artifact);

	/**
	 * Mark this artifact.
	 * This method, along with #unmark() and #isMarked(), is useful for graph traversals etc.
	 **/
	public void mark();

	/**
	 * Unmark this artifact.
	 * This method, along with #mark() and #isMarked(), is useful for graph traversals etc.
	 **/
	public void unmark();

	/**
	 * Returns whether or not this artifact is marked.
	 * This method, along with #mark() and #unmark(), is useful for graph traversals etc.
	 * @return whether or not this artifact is marked.
	 *
	 **/
	public boolean isMarked();

	/**
	 * Creates a clone of this artifact
	 * @return A clone of this artifact.
	 * The clone may or may not have the same id as this artifact
	 */
	public Object clone();

	/**
	 * Determines if this artifact has attached file documents.
	 */
	public boolean hasDocuments();

	/**
	 * Determines if this artifact has annotations.
	 * This can either be the built in Shrimp annotation,
	 * or in Jambalaya's case it could be a Protege Changes Annotation.
	 */
	public boolean hasAnnotations();

	/**
	 * Attaches a document (a file or url) to this artifact.
	 * @param file the document to attach
	 * @param fireEvent if an event should be fired
	 */
	public void attachDocument(String file, boolean fireEvent);

	/**
	 * Attaches a document to this artifact (this could also be an annotation).
	 * @param document the document to attach
	 */
	public void attachDocument(NodeDocument document);

	/**
	 * Removes the document from this artifact.
	 * Causes an event to be fired.
	 * @param doc
	 */
	public void removeDocument(NodeDocument doc);

	/**
	 * Removes all the document from this artifact.
	 * Causes an event to be fired.
	 */
	public void removeAllDocuments();

	/**
	 * Returns an ordered list (ordered by when the document was added) of the documents for this artifact.
	 * This ONLY include file/url documents, it does not include annotation documents.
	 * @return {@link List} of documents.
	 */
	public List/*<NodeDocument>*/ getDocuments();

}
