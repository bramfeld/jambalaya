/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.DataBean.event.ArtifactAddListener;
import ca.uvic.csr.shrimp.DataBean.event.ArtifactRemoveListener;
import ca.uvic.csr.shrimp.DataBean.event.DataFilterRequestListener;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipAddListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipRemoveListener;
import ca.uvic.csr.shrimp.DataBean.event.RootArtifactsChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener.DataTypesChangeEvent;

/**
 * This is the storage unit for the Shrimp system.
 * It is designed to hold and maintain the graph data structure being
 * used by the system.
 *
 * This interface is intended to support incremental loading of data.
 * That is, not all data will be loaded from some
 * back-end source into this data bean at start-up, but will be
 * loaded in as needed.
 * An important thing to notice here is that some methods have
 * an extra <code>lookInBackEnd</code> boolean parameter to specify
 * whether you want to consider the already loaded "buffered" data,
 * or to get all information from the back-end.
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 */
public interface DataBean {

    public static final int ARTIFACT_TYPE = 0;
    public static final int RELATIONSHIP_TYPE = 1;


	/**
	 * Now that we are using objects as ids, we need to have a way
	 * to get a proper id object without knowledge of how they are being
	 * stored.  This method allows a string version of the id to be
	 * converted into a proper id.  Always assume the id can be written
	 * to and read from a single text line.
	 *
	 * @param idInStringForm An artifact's id as a string
	 * @return An artifact's id as an Object(Note: This may be just a String)
	 */
	public Object getArtifactExternalIDFromString(String idInStringForm);

	/**
	 * Now that we are using objects as ids, we need to have a way
	 * to get a proper id object without knowledge of how they are being
	 * stored.  This method allows a string version of the id to be
	 * converted into a proper id.  Always assume the id can be written
	 * to and read from a single text line.
	 *
	 * @param idInStringForm A relationship's id as a string
	 * @return A relationship's id as an Object(Note: This may be just a String)
	 */
	public Object getRelationshipExternalIDFromString(String idInStringForm);

	public String getStringFromExternalArtifactID(Object externalID);
	public String getStringFromExternalRelationshipID(Object externalID);

	/**
	 * Returns the default child parent relationships for this data bean.
	 * @return the default child parent relationships for this data bean.
	 */
	public String[] getDefaultCprels();

    public boolean getDefaultCprelsInverted();


	/*************************** Artifact ****************************/

	/**
	 * Returns all of the artifacts that have no parents based on the
	 * given child-parent relationships
	 *
	 * @param cprels The Child Parent Relationships desired to find the roots
	 * @return A vector of "root" Artifacts
	 */
	public Vector getRootArtifacts(String[] cprels);

	public Vector getLeafArtifacts(String[] cprels);

	/**
	 * Returns a vector of artifacts, containing the children of the given artifact.
	 *
	 * @param artifact The artifact who's children are returned.
	 * @param cprels The Child Parent Relationships specifying the current hierarchy
	 * @return A Vector of artifacts that are children of the passed in artifact.
	 * If the artifact has no children, an empty vector is returned.
	 */
	public Vector getChildren(Artifact artifact, String[] cprels);

	/**
	 * Returns a vector of artifacts, which contains the parents of the given artifact.
	 * If the artifact has no parents, an empty vector is returned.
	 *
	 * @param artifact The artifact who's parents are returned.
	 * @param cprels The Child Parent Relationships specifying the current hierarchy
	 * @return a vector of artifacts, which contains the parents of the given artifact.
	 * If the artifact has no parents, an empty vector is returned.
	 */
	public Vector getParents(Artifact artifact, String[] cprels);

	/**
	 * Returns a vector of artifacts, which contains the siblings of the given artifact.
	 *
	 * @param artifact The artifact who's siblings are returned.
	 * @param cprels The Child Parent Relationships specifying the current hierarchy
	 * @return a vector of artifacts, which contains the siblings of the given artifact.
	 * If the artifact has no siblings, an empty vector is returned.
	 */
	public Vector getSiblings(Artifact artifact, String[] cprels);

	/**
	 * Returns a vector of artifacts, which contains the descendents of the given artifact.
	 *
	 * @param artifact The artifact who's descendents are returned.
	 * @param cprels The Child Parent Relationships specifying the current hierarchy
	 * @return a vector of artifacts, which contains the descendents of the given artifact.
	 * If the artifact has no descendents, an empty vector is returned.
	 */
	public Vector getDescendents(Artifact artifact, String[] cprels);

	/**
	 * Returns a vector of artifacts, which contains the ancestors of the given artifact.
	 *
	 * @param artifact The artifact who's ancestors are returned.
	 * @param cprels The Child Parent Relationships specifying the current hierarchy
	 * @return a vector of artifacts, which contains the descendents of the given artifact.
	 * If the artifact has no ancestors, an empty vector is returned.
	 */
	public Vector getAncestors(Artifact artifact, String[] cprels);

	/**
	 * Returns the number of children of an artifact, with respect to the given child-parent relationship types.
	 * Note: In some cases this may be significantly faster than using <code>DataBean.getChildren(artifact, cprels).size()</code>
	 * @param artifact The artifact the get the number of children of.
	 * @param cprels The child-parent relationships to use to get the children
	 * @return Number of children of the given artifact
	 */
	public int getChildrenCount(Artifact artifact, String[] cprels);

	/**
	 * Returns the number of parents of an artifact, with respect to the given child-parent relationship types.
	 * Note: In some cases this may be significantly faster than using <code>DataBean.getParent(artifact, cprels).size()</code>
	 * @param artifact The artifact the get the number of parents of.
	 * @param cprels The child-parent relationships to use to get the parents
	 * @return Number of parents of the given artifact
	 */
	public int getParentsCount(Artifact artifact, String[] cprels);

	/**
	 * Returns the number of descendents of an artifact, with respect to the given child-parent relationship types.
	 * Note: In some cases this may be significantly faster than using <code>DataBean.getChildren(artifact, cprels).size()</code>
	 * @param artifact The artifact the get the number of descendents of.
	 * @param cprels The child-parent relationships to use to get the children
	 * @param countArtifactMultipleTimes Whether or not to count the same artifact as a descendent more than once.
	 * This happens when the cprels given creates a hierarchy in which artifacts have multiple parents.
	 * @return Number of descendents of the given artifact
	 */
	public int getDescendentsCount(Artifact artifact, String[] cprels, boolean countArtifactMultipleTimes);

	/**
	 * Returns all of the artifacts in this data bean.
	 * @param lookInBackEnd Whether or not to look beyond the currently buffered data, and into the back-end.
	 * NOTE: It could possibly take a long time to get all artifacts from the back-end, so use with lookInBackEnd==true
	 * only when totally necessary.
	 * @return A vector of Artifacts.
	 */
	public Vector getArtifacts(boolean lookInBackEnd);

	/**
	 * Registers a new Artifact with this Bean.
	 * Note: If the artifact is already in this bean, nothing will happen.
	 *
	 * @param artifact the artifact to be added.
	 */
	public boolean addArtifact(Artifact artifact);

	/**
	 * Removes an Artifact from this Bean.
	 * If the given artifact is not in this databean, nothing is done.
	 *
	 * @param artifact the artifact to be removed.
	 */
	public boolean removeArtifact(Artifact artifact);

	/*
	 * Returns the artifact with the given id.  If the artifact hasn't been created yet,
	 * this method will create it, if possible, and return it.
	 * If an artifact with the given id cannot be found or created, then null is returned.
	 *
	 * This method needs to be called to register artifacts in the data bean.
	 * Any method that wants to create artifacts from the back-end should call this method to do so.
	 *
	 * Note: This artifact is fully connected to the back-end and represents data in the back-end
	 *
	 * @param id The id used to find the data in the back-end.
	 * This id will be given to the returned artifact if it needs to be created.
	 * @return an Artifact with the given id. Returns null if no such artifact can be found.
	 */
	//public Artifact findArtifact(long id);

	public Artifact findArtifactByExternalId(Object externalId);

	public Artifact getArtifact(long id);


	/************************* Relationship ********************************/

	/**
	 * Returns the relationship with the given id
	 *
	 * @param id The id of the relationship to be returned
	 * @return the relationship with the given id, or null if no such relationship found
	 */
	public Relationship getRelationship(long id);

	/**
	 * Returns all of the relationships in this data bean.
	 * @param lookInBackEnd Whether or not to look beyond the currently buffered data, and into the back-end.
	 * NOTE: It could possibly take a long time to get all relationships from the back-end, so use with lookInBackEnd==true
	 * only when totally necessary.
	 * @return A vector of Relationships.
	 */
	public Vector getRelationships(boolean lookInBackEnd);

    /**
     * Returns the incoming and outgoing relationships for the given artifact.
     * Note: Not all databeans can find all incoming relationships, @see DataBean.getIncomingRelationships(Artifact)
     * @param artifact The artifact to get the relationships of.
     * @return A vector of Relationship objects. An empty vector is
     * returned if the artifact hasn't got any relationships.
     */
    public Vector getIncomingAndOutgoingRelationships(Artifact artifact);

    /**
     * Returns all the relationships that the given artifact is the destination(or child) of.
     * Note: Not all databeans can find all incoming relationships, therefore it
     * is sometimes necessary to first get all outgoing relationships for all artifacts first
     * using getRelationships(true) before calling this method. **WARNING: Doing this can
     * be a very expensive operation in terms of time and memory.
     * @param destArtifact
     * @return A Vector of Relationship objects
     */
    public Vector getIncomingRelationships(Artifact destArtifact);

    /**
     * Returns all the relationships that the given artifact is the source(or parent) of.
     * @param srcArtifact
     * @return A Vector of Relationship objects
     */
    public Vector getOutgoingRelationships(Artifact srcArtifact);

//    /**
//     * Returns the incoming and outgoing relationships for the given artifact.
//     * Note: Not all databeans can find all incoming relationships, @see DataBean.getIncomingRelationships(Artifact)
//     * @param artifact The artifact to get the relationships of.
//     * @return A vector of Relationship objects. An empty vector is
//     * returned if the artifact hasn't got any relationships.
//     */
//    public Vector getIncomingAndOutgoingRelationshipsOfType(Artifact artifact, String type);
//
//    /**
//     * Returns all the relationships that the given artifact is the destination(or child) of.
//     * Note: Not all databeans can find all incoming relationships, therefore it
//     * is sometimes necessary to first get all outgoing relationships for all artifacts first
//     * using getRelationships(true) before calling this method. **WARNING: Doing this can
//     * be a very expensive operation in terms of time and memory.
//     * @param destArtifact
//     * @return A Vector of Relationship objects
//     */
//    public Vector getIncomingRelationshipsOfType(Artifact destArtifact, String type);
//
//    /**
//     * Returns all the relationships that the given artifact is the source(or parent) of.
//     * @param srcArtifact
//     * @return A Vector of Relationship objects
//     */
//    public Vector getOutgoingRelationshipsOfType(Artifact srcArtifact, String type);

	/**
	 * Registers a new relationship with this Bean. If it is the first relationship
	 * of its type, the type will be added to the type list.
	 *
	 * Note: If the relationship is already in the list, it will not be added
	 *
	 * @param relationship the relationship to be added.
	 */
	public boolean addRelationship(Relationship relationship);

	/**
	 * Removes an relationship from this Bean.  If it is the last relationship
	 * of its type, the type will be removed from the type list.
	 *
	 * @param relationship the relationship to be removed.
	 */
	public boolean removeRelationship(Relationship relationship);

	/******************* Artifact/Relationship Types **********************/

	/**
	 * Returns a vector of artifacts containing all of the artifacts
	 * of a specific type.
	 * Only loaded artifacts will be returned.
	 *
	 * @param type The type of artifacts desired
	 * @param lookInBackEnd Whether or not to search the back-end(Note: this could be time-consuming)
	 *
	 * @return A Vector of <code>Artifact</code>s
	 */
	public Vector getArtifactsOfType(String type, boolean lookInBackEnd);

	/**
	 * Associates an icon with an artifact type.
	 * @param type The artifact type.
	 * @param icon The icon to associate.
	 */
	public void setArtifactTypeIcon(String type, Icon icon);

	/**
	 * Returns the icon associated with the given artifact type.
	 * Returns null if no icon associated with the given type.
	 * @param type The artifact type.
	 * @return The icon associated with the given type.
	 */
	public Icon getArtifactTypeIcon(String type);

	/**
	 * Gets the icon associated with the given artifact.
	 * @param artifact The artifact to the icon of.
	 * @return the icon associated with the given artifact. Returns null if artifact has no icon.
	 */
	public Icon getArtifactIcon(Artifact artifact);

	/**
	 * Returns a vector of relationships containing all of the relationships
	 * of a specific type.
	 * Only loaded relationships will be returned.
	 *
	 * @param type The type of relationships desired
	 * @param lookInBackEnd Whether or not to search the back-end(Note: this could be time-consuming)
	 *
	 * @return A Vector of <code>Relationship</code>s
	 */
	public Vector getRelationshipsOfType(String type, boolean lookInBackEnd);

	/**
	 * Returns a vector containing all of the artifact types available in
	 * this bean.
	 * @param includeFilteredTypes Whether or not to include types that are
	 * currently being filtered by data filters.
	 * @param lookInBackEnd Whether or not to look beyond buffered data and into the back-end
	 * to find all the possible types. NOTE: If true, then this method may take a long time.
	 * @return a Vector of Strings containing all of the artifact types available in this bean.
	 * Types will be returned in alphabetical order ignoring case.
	 */
	public Vector getArtifactTypes(boolean includeFilteredTypes, boolean lookInBackEnd);

	/**
	 * Returns a vector containing all of the relationship types available in
	 * this bean.
	 * @param includeFilteredTypes Whether or not to include types that are
	 * currently being filtered by data filters.
	 * @param lookInBackEnd Whether or not to look beyond buffered data and into the back-end
	 * to find all the possible types. NOTE: If true, then this method may take a long time.
	 * @return a Vector of Strings containing all of the relationship types available in this bean.
	 * Types will be returned in alphabetical order ignoring case.
	 */
	public Vector getRelationshipTypes(boolean includeFilteredTypes, boolean lookInBackEnd);

	/**
	 * Returns a vector containing all of the relationship types that can be used to create a valid hierarchy
	 * for this data bean.
	 * @param includeFilteredTypes Whether or not to include types that are
	 * currently being filtered by data filters.
	 * @param lookInBackEnd Whether or not to look beyond buffered data and into the back-end
	 * to find all the possible types. NOTE: If true, then this method may take a long time.
	 * @return a Vector of Strings containing all of the relationship types that can be used to create a valid hierarchy
	 * for this data bean.
	 */
	public Vector getHierarchicalRelationshipTypes(boolean includeFilteredTypes, boolean lookInBackEnd);


	/************************** Listeners ********************************/

	/**
	 * Sets the ability to fire events.  If true is indicated, this bean
	 * will fire events.  If false is indicated, this bean will not fire
	 * events.  Any event that should be fired while turned off will be lost.
	 *
	 * @param on Whether or not firing events is turned on
	 */
	public void setFiringEvents(boolean on);

	/**
	 * Returns whether or not this bean will fire events
	 *
	 * @return boolean Whether or not this bean will fire events
	 */
	public boolean isFiringEvents();

	public void addArtifactAddListener(ArtifactAddListener aal);

	public void removeArtifactAddListener(ArtifactAddListener aal);

	public void fireArtifactAddEvent(Artifact addedArtifact);

	public void addArtifactRemoveListener(ArtifactRemoveListener arl);

	public void removeArtifactRemoveListener(ArtifactRemoveListener arl);

	public void fireArtifactRemoveEvent(Artifact removedArtifact);

	public void addRootArtifactsChangeListener(RootArtifactsChangeListener acl);

	public void removeRootArtifactsChangeListener(RootArtifactsChangeListener acl);

	public void fireRootArtifactsChangeEvent(Set rootArtifacts);

	public void addRelationshipAddListener(RelationshipAddListener ral);

	public void removeRelationshipAddListener(RelationshipAddListener ral);

	public void fireRelationshipAddEvent(Relationship addedRelationship);

	public void addRelationshipRemoveListener(RelationshipRemoveListener rrl);

	public void removeRelationshipRemoveListener(RelationshipRemoveListener rrl);

	public void fireRelationshipRemoveEvent(Relationship removedRelationship);

	public void addRelationshipChangeListener(RelationshipChangeListener rcl);

	public void removeRelationshipChangeListener(RelationshipChangeListener rcl);

	public void fireRelationshipChangeEvent(Relationship changedRelationship);

	public void addDataTypesChangeListener(DataTypesChangeListener dtcl);

	public void removeDataTypesChangeListener(DataTypesChangeListener dtcl);

	public void fireDataTypesChangeEvent(DataTypesChangeEvent dtce);

	/**
	 * This removes all of the buffered data and forces the databean to access the back-end data to get the
	 * real data structure.  This needs to be called any time the back-end changes, or if anything changes
	 * that affects what is imported into this databean.
	 *
	 * Buffered data will not be cleared unless setDataIsDirty has been called beforehand.
	 *
	 * Note: For large projects this buffered data may have taken some time to collect and
	 * will have to be collected from scratch after it is cleared.
	 **/
	public void clearBufferedData();

	/**
	 * Compares this data bean with the passed in data bean. This is simple diff and returns the
	 * passed in databean with appropriate ChangeType attribute(unchanged, changed, added, deleted) set
	 * for each artifact in the passed in data bean.
	 * Each artifact from the DataBean afterDataBean is compared to the respective artifact in
	 * this DataBean(considered the "before" DataBean.)
	 *
	 * @param afterDataBean The DataBean to compare with this "before" DataBean.
	 * @return the passed in databean with appropriate ChangeType attribute added to each artifact
	 */
	public DataBean compare(DataBean afterDataBean);


	/**
	 * @param type A relationship type
	 * @return Whether or not this data bean will filter the given relationship type.
	 */
	public boolean isRelTypeFiltered(String type);

	/**
	 * @param type An artifact type
	 * @return Whether or not this data bean will filter the given artifact type.
	 */
	public boolean isArtTypeFiltered(String type);



	/*  ***************************************** Attribute Panel *******************************************
	 */

	/**
	 * Returns a map which contains all nominal attributes and corresponding value sets in this DataBean.
	 * The key of the map is the attribute name, and the value of the map is a SortedSet of corresponding values in the domain.
	 * @param cprels The child-parent relationships to use for calculating attributes.
	 * @param lookInBackEnd Whether or not to look in the back end for attribute values(NOTE: it may take a long time)
	 * @param targetType ARTIFACT_TYPE or RELATIONSHIP_TYPE
	 *
	 * @return a mapping from an attribute name to a SortedSet of all the values of this attribute
	 */
	public Map getNominalAttrValues(String[] cprels, boolean lookInBackEnd, boolean inverted, int targetType);

	/**
	 * Returns a map which contains all ordinal attributes and corresponding value sets in this DataBean.
	 * The key of the map is the attribute name, and the value of the map is a SortedSet of corresponding values in the domain.
	 * @param cprels The child-parent relationships to use for calculating attributes.
	 * @param lookInBackEnd Whether or not to look in the back end for attribute values(NOTE: it may take a long time)
	 * @param targetType ARTIFACT_TYPE or RELATIONSHIP_TYPE
	 *
	 * @return a mapping from an attribute name to a SortedSet of all the values of this attribute
	 */
	public Map getOrdinalAttrValues(String[] cprels, boolean lookInBackEnd, boolean inverted, int targetType);

	/**
	 * Returns a map which contains node display-relevant (nominal) attributes and corresponding value sets in this DataBean.
	 * The key of the map is the attribute name, and the value of the map is a SortedSet of corresponding values in the domain.
	 * Note that these attributes are excluded from the regular nominal attribute set.
	 * @param lookInBackEnd Whether or not to look in the back end for attribute values(NOTE: it may take a long time)
	 *
	 * @return a mapping from an attribute name to a SortedSet of all the values of this attribute
	 */
	public Map getNodeDisplayAttrValues(boolean lookInBackEnd);

	/*  ***************************************** End Attribute Panel ****************************************** */


	/**
	 * Returns a Vector of CompositeRelationships based on the given relationship type and
	 * given child-parent relationships.
	 * *NOTE: THIS METHOD COULD TAKE A LONG TIME TO COMPLETE.
	 * @param type the relationship type to base the composite relationships on
	 * @param cprels the child-parent relationships to base the composite relationships on
	 * @param inverted Whether or not to base the composites on the child-parent relationships
     * in a backwards direction.
	 * @return a Vector of CompositeRelationships
	 */
	public Vector getCompositeRelationshipsByType(String type, String[] cprels, String compositeType, boolean inverted);

	/**
	 * Indicates whether or not the data buffered in this databean is out of synch with the back-end, or needs
	 * to be recollected.
	 */
	public boolean dataIsDirty();

	/**
	 * Tells the this data bean that its buffered data is out of synch with the back end, or needs
	 * to be recollected.
	 */
	public void setDataIsDirty();

	/**
	 *
	 * @param srcArtifact
	 * @param destArtifact
	 * @param relTypes
	 * @return A list containing artifacts in the shortest path between the given artifacts.
	 * The first artifact will be the passed in source artifact and the
	 * last artifact will be the passed in destination artifact.
	 * Return an empty list if no paths between the two artifact.
	 */
	public List getShortestPath(Artifact srcArtifact, Artifact destArtifact, Collection relTypes);

	/**
	 * Finds and returns the first artifact(via breadth-first search) that has the given attribute with the given value
	 * @param attrName
	 * @param attrValue
	 * @param cprels The child-parent relationships to use for calculating certain attributes
	 * @return A valid artifact, or null if none found
	 */
	public Artifact findFirstArtifact(String attrName, Object attrValue, String cprels []);

	/**
	 * Finds and returns all the artifacts that have the given attribute with the given value
	 * @param attrName
	 * @param attrValue
	 * @param cprels The child-parent relationships to use for calculating certain attributes
	 * @return A collection of valid artifacts or an empty collection if none found.
	 */
	public Collection findArtifacts(String attrName, Object attrValue, String cprels []);

	/**
	 * Find the artifacts connected to the given source artifacts.
	 * @param srcArtifacts The artifacts of interest
	 * @param artTypes The relationship types to consider for connection. Ignored if restrictRelTypes is false.
	 * @param relTypes The relationship types to consider for connection. Ignored if restrictRelTypes is false.
	 * @param incomingLevels the number of levels to traverse over incoming arcs
	 * @param outgoingLevels the number of levels to traverse over outgoing arcs
	 * @return A collection of artifacts that are connected given all these paramaters.
	 */
	public Collection getConnectedArtifacts(Collection srcArtifacts, Collection artTypes, Collection relTypes, int incomingLevels, int outgoingLevels);


	/**
	 * Returns the name of the group that a relationship type should be part of by default.
	 * @param relType
	 * @return null if the given relationship type does not have a default group
	 */
	public String getDefaultGroupForRelationshipType(String relType);


	/**
	 * Sets the scope of the data to be pulled into the DataBean from the backend.
	 * For example, you may want to limit a large amount of hierarchical data to a few smaller
	 * subtrees using this method.
	 * @param newWorkingSet An object that represents a working set.
	 * This parameter is kept as generic as possible since DataBeans in different domains
	 * will have different notions of a working set.
	 */
	public void setWorkingSet(Object newWorkingSet);

	// a hack
	public void dataFiltersHaveChanged();

	public void addFilterRequestListener(DataFilterRequestListener listener);

    public void removeFilterRequestListener(DataFilterRequestListener listener);


    public boolean isExportingData();

    /**
     * Indicates if the data is being exported.
     * This is a bit of a hack for Creole so that the source code
     * will be exported.
     * @param exporting
     */
    public void setExportingData(boolean exporting);

}
