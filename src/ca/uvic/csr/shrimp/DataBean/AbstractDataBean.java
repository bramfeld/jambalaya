/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.event.ArtifactAddListener;
import ca.uvic.csr.shrimp.DataBean.event.ArtifactRemoveListener;
import ca.uvic.csr.shrimp.DataBean.event.DataFilterRequestListener;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipAddListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipRemoveListener;
import ca.uvic.csr.shrimp.DataBean.event.RootArtifactsChangeListener;
import ca.uvic.csr.shrimp.DataBean.event.ArtifactAddListener.ArtifactAddEvent;
import ca.uvic.csr.shrimp.DataBean.event.ArtifactRemoveListener.ArtifactRemoveEvent;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener.DataTypesChangeEvent;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipAddListener.RelationshipAddEvent;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipChangeListener.RelationshipChangeEvent;
import ca.uvic.csr.shrimp.DataBean.event.RelationshipRemoveListener.RelationshipRemoveEvent;
import ca.uvic.csr.shrimp.DataBean.event.RootArtifactsChangeListener.RootArtifactsChangeEvent;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * <pre>
 * This class implements all the default structures expected to be
 * used by the average data bean.  Data beans may overwrite any of the default methods
 * in this bean, but should ensure their implementations provide the same services.
 *
 * The {@link AbstractDataBean#clearBufferedData()} method needs to be called any time back-end data is changed.
 * Back-end data is considered the data that you will be using to create the artifacts and relationships.
 * Failure to do so will cause the data buffered in this data bean from the back-end to become
 * out of sync with the back-end data.  This will cause unexpected behaviour.
 *
 * To enhance efficiency for your new databean, override these methods:
 *
 * public int getChildrenCount(Artifact artifact, String cprel);
 * public int getDescendentsCount(Artifact artifact, String cprel);
 *
 *
 * Conceptual Details:
 *			1) The term "cprel" stands for Child-Parent Relationship.  The child-parent relationship can be any
 *             relationship type used to build the nested view.  For example, if the cprel is the relationship
 *             type "contains", the children of a node would be determined by examining all of the "contains"
 *             relationships associated with that node.  We pull out any relationship in which the node
 *             is the source (or parent) of the relationship, and return the child of the relationship as one
 *             of the node's children.
 *             In the following example, if the cprel was "contains", Node B would be the child of Node A, but
 *             Node C wouldn't be a child or parent of Node A.  Instead it simply has a relationship with Node A.
 *             If the cprel was "mother of", Node C would be the child of Node A and Node B wouldn't.
 *             Note: We allow more than one cprel at once, allowing related relationship
 *                   types to be used in the same view.  For example, "daughter of" and "son of" could both
 *                   be used to show a family tree.
 *  <code>
 *                                              ________________________
 *                                              |                      |
 *                                              |       Node A         |
 *                                              ________________________
 *                                                    /           \
 *                                                   /             \
 *                                       "contains" /               \ "mother of"
 *                                                 /                 \
 *                                                /                   \
 *                                               \/                   \/
 *                                ________________________     ________________________
 *                                |                      |     |                      |
 *                                |       Node B         |     |       Node C         |
 *                                ________________________     ________________________
 *</code>
 *
 *			2) To extend this abstract databean and add a new databean onto SHriMP,
 *             the abstract databean will assume the data is to be loaded incrementally from
 *             its source upon demand.
 *
 *             IMPORTANT: You should never directly instantiate a new artifact outside of these methods:
 *				          You should also never directly call these three methods (this abstract data bean will call them when appropriate):
 * 					protected Artifact createArtifact (Object id, String cprel, Artifact keyParent);
 * 					protected Relationship createEmptyRelationship (String name, String type, Vector artifacts);
 *
 *             When you need to get an artifact or relationship from the back-end, use the following approaches:
 *
 *             Artifacts:
 *                  Any time you have an id and need the associated artifact, call findArtifact (id) in this abstract data bean.
 *                  If the artifact hasn't previously been created, the findArtifact method will create it.  The following happens:
 *
 *					      * Call findArtifact (works both externally and internally to the databean).
 *     					  * findArtifact returns the artifact if already created, or creates the artifact using createArtifact and returns it
 *					      * createArtifact is implemented by you in your domain specific databean.
 *
 *             Relationships:
 *                  Any time you have the information for a relationship and need to create it, call createRel in this abstract
 *                  data bean.  The following happens when createRel is called:
 *
 *                  NOTE: This differs from findArtifacts in that the validity isn't checked.  A relationship will always be created and returned,
 *                        where the findArtifacts method will return null if the id doesn't reference data in the back-end.
 *
 *                        * Call createRel
 *                        * createRel returns the relationship if already created, or creates the relationship using createEmptyRelationship and returns it
 *					      * createEmptyRelationship is implemented by you in your domain specific databean.
 *
 *
 *			3) All artifacts are found incrementally from the root artifacts down.  When calling findArtifact (id),
 *             an object with this id may exist in the back-end but may not be loaded yet due to the incremental loading.
 *
 * </pre>
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 */
public abstract class AbstractDataBean implements DataBean, Serializable {

	protected static final int NOMINAL_ATTRIBUTE = 0;
	protected static final int ORDINAL_ATTRIBUTE = 1;

	// The data change listeners that have been registered with this data bean.
	private Vector artifactAddListeners;
	private Vector artifactRemoveListeners;
	private Vector rootArtifactsChangeListeners;
	private Vector relationshipAddListeners;
	private Vector relationshipRemoveListeners;
	private Vector relationshipChangeListeners;
	private Vector dataTypesChangeListeners;
	private boolean firingEvents;

	/* ************************* BUFFERED DATA STARTS HERE ****************************/

	/**
	 * Buffers the artifact types that are currently being used in this bean.
	 * Note: The keys are the artifact type, and the values are vectors of artifacts of that type.
	 */
	private Map bufferArtifactTypes;

	/**
	 * Buffers the relationship types that are currently being used in this bean.
	 * Note: The keys are the relationship type, and the values are vectors of relationships of that type.
	 */
	protected Map bufferRelationshipTypes;

	/**
	 * Buffers all relationships created so far
	 * key: id, value: relationship
	 * **/
	protected Map bufferRelationshipsCreated;

	/**
	 * Buffers all artifacts created so far
	 * key: id, val: artifact.
	 */
	protected Map bufferArtifactsCreated;

	/**
	 * Buffers roots found so far
	 * key is cprel, value is a vector of roots
	 **/
	private Map bufferRootsCreated;

	/**
	 * Buffers all the incoming relationships found for a particular artifact in the back end
	 * key is the artifact, value is a vector of relationships
	 */
	protected Map bufferIncomingBackendRelsForArtifact;

	/**
	 * Buffers all the incoming relationships found for a particular artifact in the back end
	 * key is the artifact, value is a vector of relationships
	 */
	protected Map bufferOutgoingBackendRelsForArtifact;

	/** Buffers all artifacts from the back end **/
	protected Vector bufferAllBackEndArtifacts;

	/** Buffers all relationships from the back end **/
	protected Set bufferAllBackEndRels;

	/** Buffers artifact types from the back end **/
	protected Vector bufferBackEndArtTypes;

	/** Buffers relationship types from the back end **/
	protected Vector bufferBackEndRelTypes;

	/**
	 * Buffers a map between an artifact and the relationships in which it is the destination (or child)
	 * This mapping is needed because some domains are not able to find all incoming relationships
	 * for an artifact.
	 */
	protected Map bufferWaitingIncomingRelsForArtifact;

	/**
	 * Buffers a map between an artifact and the relationships in which it is the source (or parent)
	 * This mapping is needed because some domains are not able to find all outgoing relationships
	 * for an artifact.
	 */
	protected Map bufferWaitingOutgoingRelsForArtifact;

	/** Buffers a map from an artifact's external id object to its internal java.lang.long id */
	protected Map bufferExternalToInternalIdMap;

	/* ************************* BUFFERED DATA ENDS HERE **************************/

	/** The next id available for artifacts or relationships. */
	private long currID = 1;

	protected boolean isDirty = false;

	/** maps an artifact type to an icon that represents that type **/
	private Map artTypeToIcon;

	private boolean delayedFireDataTypesChangeEvent;

	private List shortestPathSoFar;
	private Collection filterRequestListeners;

	private boolean isExportingData = false;

	/**
	 * Creates a new Data Bean, initializing all of the attributes.
	 */
	public AbstractDataBean() {
		artifactAddListeners = new Vector();
		artifactRemoveListeners = new Vector();
		rootArtifactsChangeListeners = new Vector();
		relationshipAddListeners = new Vector();
		relationshipRemoveListeners = new Vector();
		relationshipChangeListeners = new Vector();
		dataTypesChangeListeners = new Vector();
		filterRequestListeners = new Vector();

		delayedFireDataTypesChangeEvent = false;
		setFiringEvents(true);

		bufferArtifactsCreated = new HashMap();
		bufferRelationshipsCreated = new HashMap();
		bufferWaitingIncomingRelsForArtifact = new HashMap();
		bufferWaitingOutgoingRelsForArtifact = new HashMap();
		bufferRootsCreated = new HashMap();
		bufferArtifactTypes = new HashMap();
		bufferRelationshipTypes = new HashMap();
		bufferIncomingBackendRelsForArtifact = new HashMap();
		bufferOutgoingBackendRelsForArtifact = new HashMap();
		bufferAllBackEndArtifacts = null;
		bufferAllBackEndRels = null;
		bufferBackEndArtTypes = null;
		bufferBackEndRelTypes = null;
		bufferExternalToInternalIdMap = new HashMap();

		artTypeToIcon = new HashMap();
	}

	/**
	 *
	 * @see DataBean#clearBufferedData()
	 */
	public void clearBufferedData() {
		//if (dataIsDirty()) {
		//TODO what buffers should be cleared only if the data in the databean is out of sync with the back-end
		isDirty = false;
		bufferArtifactsCreated.clear();
		bufferRelationshipsCreated.clear();
		//}
		// TODO what buffers should be cleared always (as a result of changes in data filters, working set, root classes, etc.)
		bufferWaitingIncomingRelsForArtifact.clear();
		bufferWaitingOutgoingRelsForArtifact.clear();
		bufferRootsCreated.clear();
		bufferArtifactTypes.clear();
		bufferRelationshipTypes.clear();
		bufferIncomingBackendRelsForArtifact.clear();
		bufferOutgoingBackendRelsForArtifact.clear();
		bufferAllBackEndArtifacts = null;
		bufferAllBackEndRels = null;
		bufferBackEndArtTypes = null;
		bufferBackEndRelTypes = null;
		bufferExternalToInternalIdMap.clear();
	}

	/**
	 * @see DataBean#compare(ca.uvic.csr.shrimp.DataBean.DataBean)
	 */
	public DataBean compare(DataBean afterDataBean) {
		DataBean beforeDataBean = this;
		ArrayList afterArtifacts = new ArrayList(afterDataBean.getArtifacts(true));

		for (Iterator iter = afterArtifacts.iterator(); iter.hasNext();) {
			Artifact afterArtifact = (Artifact) iter.next();
			Object commonID = afterArtifact.getExternalId();

			Artifact beforeArtifact = beforeDataBean.findArtifactByExternalId(commonID);

			if (beforeArtifact == null) {
				System.out.println(afterArtifact.getName() + " has been added.");
				afterArtifact.setAttribute(AttributeConstants.NOM_ATTR_CHANGE_TYPE, AttributeConstants.NOM_ATTR_CHANGE_VALUE_ADDED);
			} else if (beforeArtifact.equivalent(afterArtifact)) {
				System.out.println(afterArtifact.getName() + " has not been changed.");
				afterArtifact.setAttribute(AttributeConstants.NOM_ATTR_CHANGE_TYPE, AttributeConstants.NOM_ATTR_CHANGE_VALUE_UNCHANGED);
			} else {
				System.out.println(afterArtifact.getName() + " has been changed.");
				afterArtifact.setAttribute(AttributeConstants.NOM_ATTR_CHANGE_TYPE, AttributeConstants.NOM_ATTR_CHANGE_VALUE_CHANGED);
			}
			/* TODO: Once DataBean.addArtifact() and DataBean.addRelationship() are working,
			 * 		handle artifact deleted case.
			 */

		}
		return afterDataBean;
	}

	/**
	 * @see DataBean#getRelationshipExternalIDFromString(String)
	 */
	public Object getRelationshipExternalIDFromString(String idInStringForm) {
		return idInStringForm;
	}

	/* (non-Javadoc)
	 * @see DataBean#findArtifactByExternalId(java.lang.Object)
	 */
	public Artifact findArtifactByExternalId(Object externalId) {
		Artifact art = null;
		Long internalIdLong = (Long) bufferExternalToInternalIdMap.get(externalId);
		if (internalIdLong != null) {
			art = getArtifactReference(internalIdLong);
		}
		if (art == null) {
			art = createArtifact(externalId);
			if (art != null) {
				addArtifactReference(art);
			}
		}
		return art;
	}

	/* (non-Javadoc)
	 * @see DataBean#getArtifact(long)
	 */
	public Artifact getArtifact(long id) {
		return getArtifactReference(id);
	}

	/**
	 * This method creates a single artifact with the given id
	 *
	 * Note: This method assumes the name, type, and any other info can be deduced from the id.
	 *
	 * @param externalId The id of the data and artifact to be created.
	 */
	protected abstract Artifact createArtifact(Object externalId);

	/**
	 * Create a new relationship of this domain.
	 * Note this method shouldn't create new data in the back-end.  It should just create a hollow relationship.
	 * The addRelationshipToDomain method should create new data in the back-end.
	 * To create and fully connect a relationship representing data already in the back-end, use createRelationship.
	 */
	protected abstract Relationship createEmptyRelationship(Object externalId, String name, String type, Vector artifacts);

	/**
	 * This searches the back-end data to find all of the outgoing relationships for the given artifact.
	 *
	 * @param artifact Search for outgoing relationships for this artifact
	 *
	 * @return A vector of Relationship objects, of which the passed in artifact is the parent or source.
	 */
	protected abstract Vector findOutgoingRelationshipsInBackEnd(Artifact artifact);

	/**
	 * This searches the back-end data to find all of the incoming relationships for the given artifact.
	 *
	 * @param artifact Search for incoming relationships for this artifact
	 *
	 * @return A vector of Relationship objects, of which the passed in artifact is the child or destination.
	 */
	protected abstract Vector findIncomingRelationshipsInBackEnd(Artifact artifact);

	/**
	 * This searches the back-end data to find all of the artifacts that should the roots
	 * of a given hierarchy.
	 * **WARNING: Calling this method may take a considerable amount of time, depending
	 * on the cprels used
	 * Override this method to make it faster.
	 * @param cprels The child-parent relationship types that the hierarchy should be based on
	 * @return A Vector of artifacts
	 */
	protected abstract Vector findRootArtifactsInBackEnd(String[] cprels);

	/**
	 * Warning: This is a slow implementation.
	 * Basically it goes through the whole graph finding artifact with only outgoing relationships.
	 * @param cprels
	 */
	protected Vector defaultFindRootArtifactsInBackEnd(String[] cprels) {
		if (cprels.length == 0) {
			return getArtifacts(true);
		}
		// TODO this is a slow implementation
		Vector roots = new Vector();
		Vector allArtifacts = getArtifacts(true);
		List cprelsList = Arrays.asList(cprels);
		ProgressDialog.showProgress();
		String subTitle = ProgressDialog.getSubtitle();
		ProgressDialog.setSubtitle("Finding all root nodes ...");
		int count = 0;
		for (Iterator iter = allArtifacts.iterator(); iter.hasNext() && !ProgressDialog.isCancelled();) {
			Artifact art = (Artifact) iter.next();
			boolean isParentOfAtLeastOneCprel = false;
			boolean isChildOfAtLeastOneCprel = false;
			Vector rels = getIncomingAndOutgoingRelationships(art);
			for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
				Relationship rel = (Relationship) iterator.next();
				if (cprelsList.contains(rel.getType())) {
					Artifact parentOfRel = (Artifact) rel.getArtifacts().elementAt(0);
					Artifact childOfRel = (Artifact) rel.getArtifacts().elementAt(1);
					if (childOfRel.equals(art)) {
						isChildOfAtLeastOneCprel = true;
					} else if (parentOfRel.equals(art)) {
						isParentOfAtLeastOneCprel = true;
					}
				}
			}
			if (isParentOfAtLeastOneCprel && !isChildOfAtLeastOneCprel) {
				roots.add(art);
			}
			count++;
			ProgressDialog.setNote(count + " of " + allArtifacts.size() + " nodes checked ...");
		}
		ProgressDialog.setSubtitle(subTitle);
		ProgressDialog.tryHideProgress();
		return roots;
	}

	/**
	 * Finds and returns the leaves of the tree formed by the given relationship types.
	 * **WARNING: Calling this method may take a considerable amount of time, depending
	 * on the cprels used
	 * Override this method to make it faster.
	 * @param cprels The child-parent relationship types that the hierarchy should be based on
	 * @return A Vector of artifacts
	 */
	protected Vector findLeafArtifactsInBackEnd(String[] cprels) {
		// TODO this is a slow implementation
		Vector leaves = new Vector();
		Vector allArtifacts = getArtifacts(true);
		List cprelsList = Arrays.asList(cprels);
		ProgressDialog.showProgress();
		String subTitle = ProgressDialog.getSubtitle();
		ProgressDialog.setSubtitle("Finding all root nodes ...");
		int count = 0;
		for (Iterator iter = allArtifacts.iterator(); iter.hasNext() && !ProgressDialog.isCancelled();) {
			Artifact art = (Artifact) iter.next();
			boolean isParentOfAtLeastOneCprel = false;
			boolean isChildOfAtLeastOneCprel = false;
			Vector rels = getIncomingAndOutgoingRelationships(art);
			for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
				Relationship rel = (Relationship) iterator.next();
				if (cprelsList.contains(rel.getType())) {
					Artifact parentOfRel = (Artifact) rel.getArtifacts().elementAt(0);
					Artifact childOfRel = (Artifact) rel.getArtifacts().elementAt(1);
					if (childOfRel.equals(art)) {
						isChildOfAtLeastOneCprel = true;
					} else if (parentOfRel.equals(art)) {
						isParentOfAtLeastOneCprel = true;
					}
				}
			}
			if (isChildOfAtLeastOneCprel && !isParentOfAtLeastOneCprel) {
				leaves.add(art);
			}
			count++;
			ProgressDialog.setNote(count + " of " + allArtifacts.size() + " nodes checked ...");
		}
		ProgressDialog.setSubtitle(subTitle);
		ProgressDialog.tryHideProgress();
		return leaves;
	}

	/**
	 *
	 * @see DataBean#getChildrenCount(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public int getChildrenCount(Artifact artifact, String[] cprels) {
		if (cprels.length == 0) {
			return 0;
		}
		// add cprels to attribute name because number of children depends on the given cprels
		String attrName = AttributeConstants.ORD_ATTR_NUM_CHILDREN + ShrimpUtils.cprelsToKey(cprels);
		Integer numChildrenInt = (Integer) artifact.getAttribute(attrName);
		if (numChildrenInt == null) {
			numChildrenInt = new Integer(getChildren(artifact, cprels).size());
			artifact.setAttribute(attrName, numChildrenInt);
		}
		return numChildrenInt.intValue();
	}

	/* (non-Javadoc)
	 * @see DataBean#getParentsCount(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public int getParentsCount(Artifact artifact, String[] cprels) {
		if (cprels.length == 0) {
			return 0;
		}
		String key = AttributeConstants.ORD_ATTR_NUM_PARENTS + ShrimpUtils.cprelsToKey(cprels); // add cprels to attribute name because number of parents depends on the given cprels
		Integer numParentsInt = (Integer) artifact.getAttribute(key);
		if (numParentsInt == null) {
			numParentsInt = new Integer(getParents(artifact, cprels).size());
			artifact.setAttribute(key, numParentsInt); // add cprels to attribute name because number of parents depends on the given cprels
		}
		return numParentsInt.intValue();
	}

	/**
	 * @see DataBean#getDescendentsCount(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[], boolean)
	 */
	public int getDescendentsCount(Artifact artifact, String[] cprels, boolean countArtifactMultipleTimes) {
		if (cprels.length == 0) {
			return 0;
		}
		return getDescendentsCountRecursive(artifact, cprels, new HashSet(), new HashSet(), countArtifactMultipleTimes);
	}

	protected int getDescendentsCountRecursive(Artifact artifact, String[] cprels, Set seenSoFar, Set pathSoFar, boolean countArtifactMultipleTimes) {
		int descendentCount = 0;
		// add cprels to attribute name because number of descendents depends on the given cprels
		String attrName = AttributeConstants.ORD_ATTR_NUM_DESCENDENTS + ShrimpUtils.cprelsToKey(cprels);
		Integer numDesInt = (Integer) artifact.getAttribute(attrName);
		if (numDesInt == null) {
			if ((!countArtifactMultipleTimes && seenSoFar.contains(artifact)) || pathSoFar.contains(artifact)) {
				System.err.println("on path already!: " + artifact);
				return 0; // prevent infinite loops
			}
			pathSoFar.add(artifact);
			seenSoFar.add(artifact);

			int childrenCount = getChildrenCount(artifact, cprels);
			if (childrenCount > 0) {
				descendentCount += childrenCount;
				Vector children = getChildren(artifact, cprels);
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					Artifact child = (Artifact) iter.next();
					descendentCount += getDescendentsCountRecursive(child, cprels, seenSoFar, pathSoFar, countArtifactMultipleTimes);
				}
			}
			pathSoFar.remove(artifact);
			artifact.setAttribute(attrName, new Integer(descendentCount));
		} else {
			descendentCount += numDesInt.intValue();
		}
		return descendentCount;
	}

	/**
	 * @see DataBean#getRootArtifacts(java.lang.String[])
	 */
	public Vector getRootArtifacts(String[] cprels) {
		String key = ShrimpUtils.cprelsToKey(cprels);
		if (bufferRootsCreated.get(key) == null) {
			Vector roots = findRootArtifactsInBackEnd(cprels);
			bufferRootsCreated.put(key, roots);
		}
		return (Vector) bufferRootsCreated.get(key);
	}

	/* (non-Javadoc)
	 * @see DataBean#getLeafArtifacts(java.lang.String[])
	 */
	public Vector getLeafArtifacts(String[] cprels) {
		//TODO should we buffer this data, or is it too much
		return findLeafArtifactsInBackEnd(cprels);
	}

	/**
	 * @see DataBean#getChildren(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public Vector getChildren(Artifact artifact, String[] cprels) {
		if (cprels.length == 0) {
			return new Vector();
		}

		List cprelsList = Arrays.asList(cprels);
		Vector relationships = getIncomingAndOutgoingRelationships(artifact);
		Set children = new HashSet();

		for (int i = 0; i < relationships.size(); i++) {
			Relationship rel = (Relationship) relationships.elementAt(i);
			if (cprelsList.contains(rel.getType())) {
				Vector artifacts = rel.getArtifacts();
				Artifact parentOfRel = (Artifact) artifacts.elementAt(0);
				Artifact childOfRel = (Artifact) artifacts.elementAt(1);
				if (parentOfRel.equals(artifact)) {
					children.add(childOfRel);
				}
			}
		}
		children.remove(artifact); // make sure artifact is not a child of itself
		return new Vector(children);
	}

	/**
	 * @see DataBean#getParents(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public Vector getParents(Artifact artifact, String[] cprels) {
		if (cprels.length == 0) {
			return new Vector();
		}
		List cprelsList = Arrays.asList(cprels);
		Vector relationships = getIncomingAndOutgoingRelationships(artifact);
		Set parents = new HashSet();

		for (int i = 0; i < relationships.size(); i++) {
			Relationship rel = (Relationship) relationships.elementAt(i);
			if (cprelsList.contains(rel.getType())) {
				Vector artifacts = rel.getArtifacts();
				Artifact parentOfRel = (Artifact) artifacts.elementAt(0);
				Artifact childOfRel = (Artifact) artifacts.elementAt(1);
				if (childOfRel.equals(artifact)) {
					parents.add(parentOfRel);
				}
			}
		}
		parents.remove(artifact); // make sure artifact is not a child of itself
		return new Vector(parents);
	}

	/**
	 * @see DataBean#getSiblings(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public Vector getSiblings(Artifact artifact, String[] cprels) {
		// first get all of the artifact's parents
		Vector parents = getParents(artifact, cprels);

		// Add the children of the remaining parents.
		Vector siblings = new Vector();
		for (int i = 0; i < parents.size(); i++) {
			Artifact parent = (Artifact) parents.elementAt(i);
			Vector children = getChildren(parent, cprels);
			for (int j = 0; j < children.size(); j++) {
				Artifact child = (Artifact) children.elementAt(j);
				if ((!siblings.contains(child)) && child != artifact) {
					siblings.addElement(children.elementAt(j));
				}
			}
		}
		return siblings;
	}

	/**
	 * @see DataBean#getDescendents(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public Vector getDescendents(Artifact artifact, String[] cprels) {
		return new Vector(getDescendentsRecursive(artifact, cprels, new ArrayList()));
	}

	private Vector getDescendentsRecursive(Artifact artifact, String[] cprels, List artifactsSeenSoFar) {
		if (artifactsSeenSoFar.contains(artifact)) {
			return new Vector(); //prevents infinite loops
		}
		artifactsSeenSoFar.add(artifact);
		Vector children = getChildren(artifact, cprels);
		Vector descendents = new Vector(children);
		// get the descendants of each child, and add it to the descendants list
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Artifact child = (Artifact) iter.next();
			descendents.addAll(getDescendentsRecursive(child, cprels, artifactsSeenSoFar));
		}
		return descendents;
	}

	/**
	 * @see DataBean#getAncestors(ca.uvic.csr.shrimp.DataBean.Artifact, java.lang.String[])
	 */
	public Vector getAncestors(Artifact artifact, String[] cprels) {
		Vector ancestors = new Vector();
		getAncestorsRecursive(artifact, cprels, ancestors, new HashSet());
		return ancestors;
	}

	private void getAncestorsRecursive(Artifact artifact, String[] cprels, Vector ancestors, Set seenArtifacts) {
		if (seenArtifacts.contains(artifact)) {
			return; //prevents infinite loops
		}

		seenArtifacts.add(artifact);
		Vector parents = getParents(artifact, cprels);
		// add all the parents to the list of ancestors first, so we can keep an ordered list
		for (int i = 0; i < parents.size(); i++) {
			Artifact parent = (Artifact) parents.elementAt(i);
			if (!ancestors.contains(parent)) {
				ancestors.add(parent);
			}
		}
		// now recursively add the parents' parents
		for (int i = 0; i < parents.size(); i++) {
			Artifact parent = (Artifact) parents.elementAt(i);
			getAncestorsRecursive(parent, cprels, ancestors, seenArtifacts);
		}
	}

	/**
	 * @see DataBean#getArtifacts(boolean)
	 */
	public Vector getArtifacts(boolean lookInBackEnd) {
		// already created artifacts
		Set artifactsSet = new HashSet(bufferArtifactsCreated.values());
		if (lookInBackEnd) {
			if (bufferAllBackEndArtifacts == null) {
				// @tag Creole.freeze : this caused Eclipse to freeze once in Creole (March 5th, 2007)
				ProgressDialog.showProgress();
				ProgressDialog.setSubtitle("Getting all items from back-end...");
				bufferAllBackEndArtifacts = findAllArtifactsInBackEnd();
				if (ProgressDialog.isCancelled()) {
					artifactsSet.addAll(bufferAllBackEndArtifacts);
					bufferAllBackEndArtifacts = null;
				}
				ProgressDialog.tryHideProgress();
			}
			if (bufferAllBackEndArtifacts != null) {
				artifactsSet.addAll(bufferAllBackEndArtifacts);
			}
		}
		return new Vector(artifactsSet);
	}

	/**
	 * Returns every single artifact from the back end.
	 * Sometimes this is a necessary, although expensive, call to make.
	 * @return A Vector of Artifacts
	 */
	abstract protected Vector findAllArtifactsInBackEnd();

	/**
	 * @see DataBean#getArtifactTypes(boolean, boolean)
	 */
	public Vector getArtifactTypes(boolean includeOutsideDataFilters, boolean lookInBackEnd) {
		Set allTypesSet = new HashSet(bufferArtifactTypes.keySet());
		if (lookInBackEnd) {
			if (bufferBackEndArtTypes == null) {
				bufferBackEndArtTypes = findArtifactTypesInBackEnd();
			}
			allTypesSet.addAll(bufferBackEndArtTypes);
		}
		Set typesSet = new HashSet(allTypesSet.size());
		for (Iterator iter = allTypesSet.iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			if (includeOutsideDataFilters || !isArtTypeFiltered(type)) {
				typesSet.add(type);
			}
		}
		Vector types = new Vector(typesSet);
		Collections.sort(types, String.CASE_INSENSITIVE_ORDER);
		return types;
	}

	/**
	 * Returns all the artifact types that the back-end knows about.
	 * @return A Vector of Strings
	 */
	abstract protected Vector findArtifactTypesInBackEnd();

	/**
	 * @see DataBean#getRelationshipTypes(boolean, boolean)
	 */
	public Vector getRelationshipTypes(boolean includeOutsideDataFilters, boolean lookInBackEnd) {
		Set allTypesSet = new HashSet();
		Set existingTypes = bufferRelationshipTypes.keySet();
		allTypesSet.addAll(existingTypes);
		if (lookInBackEnd) {
			if (bufferBackEndRelTypes == null) {
				bufferBackEndRelTypes = findRelationshipTypesInBackEnd();
			}
			allTypesSet.addAll(bufferBackEndRelTypes);
		}

		Set validTypesSet = new HashSet(allTypesSet.size());
		for (Iterator iter = allTypesSet.iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			if (includeOutsideDataFilters || !isRelTypeFiltered(type)) {
				validTypesSet.add(type);
			}
		}
		Vector types = new Vector(validTypesSet);
		Collections.sort(types, String.CASE_INSENSITIVE_ORDER);
		return types;
	}

	/**
	 * Returns all the relationship types that the back-end knows about.
	 * @return A Vector of Strings
	 */
	abstract protected Vector findRelationshipTypesInBackEnd();

	/**
	 * @see DataBean#getHierarchicalRelationshipTypes(boolean, boolean)
	 */
	public Vector getHierarchicalRelationshipTypes(boolean includeOutsideDataFilters, boolean lookInBackEnd) {
		return getRelationshipTypes(includeOutsideDataFilters, lookInBackEnd);
	}

	/**
	 * Gets all the unique relationships.
	 * @see DataBean#getIncomingAndOutgoingRelationships(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	public Vector getIncomingAndOutgoingRelationships(Artifact artifact) {
		HashSet incomingAndOutgoingRels = new HashSet();
		incomingAndOutgoingRels.addAll(getIncomingRelationships(artifact));
		incomingAndOutgoingRels.addAll(getOutgoingRelationships(artifact));
		return new Vector(incomingAndOutgoingRels);
	}

	private Vector getRelationshipsEitherWay(Artifact artifactOfInterest, boolean outgoing) {
		Map bufferBackendRels = outgoing ? bufferOutgoingBackendRelsForArtifact : bufferIncomingBackendRelsForArtifact;
		Map bufferWaitingRels = outgoing ? bufferWaitingOutgoingRelsForArtifact : bufferWaitingIncomingRelsForArtifact;
		Set rels = (Set) bufferBackendRels.get(artifactOfInterest);
		// if no rels buffered then look to back end for rels
		if (rels == null) {
			rels = new HashSet();
			bufferBackendRels.put(artifactOfInterest, rels);

			Vector backendRels = outgoing ? findOutgoingRelationshipsInBackEnd(artifactOfInterest) :
										findIncomingRelationshipsInBackEnd(artifactOfInterest);
			rels.addAll(backendRels);

			// the artifact at the other end of the relationship may not know
			// that it is involved in this relationship so we keep a list for this "waiting" artifact
			// of the relationships that we know it's involved in.
			for (Iterator iter = backendRels.iterator(); iter.hasNext();) {
				Relationship rel = (Relationship) iter.next();
				Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
				Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
				Artifact waitingArtifact = outgoing ? destArt : srcArt;
				Set waitingRels = (Set) bufferWaitingRels.get(waitingArtifact);
				if (waitingRels == null) {
					waitingRels = new HashSet();
					bufferWaitingRels.put(waitingArtifact, waitingRels);
				}
				waitingRels.add(rel);
			}
		}

		// we need to check if the are any rels waiting for this artifact to come along
		if (bufferWaitingRels.get(artifactOfInterest) != null) {
			rels.addAll((Set) bufferWaitingRels.get(artifactOfInterest));
		}

		return new Vector(rels);
	}

	public Vector getIncomingRelationships(Artifact artifact) {
		return getRelationshipsEitherWay(artifact, false);
	}

	public Vector getOutgoingRelationships(Artifact artifact) {
		return getRelationshipsEitherWay(artifact, true);
	}

	/**
	 * @see DataBean#addArtifact(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	public boolean addArtifact(Artifact artifact) {
		if (findArtifactByExternalId(artifact.getExternalId()) != null) {
			System.err.println(ApplicationAccessor.getApplication().getName() + " Warning! Artifact already exists in databean: " + artifact);
			return true;
		}
		if (addArtifactToBackEnd(artifact)) {
			clearBufferedData(); //TODO have a more streamlined way of updating buffered data so we don't lose it all
			addArtifactReference(artifact);
			fireArtifactAddEvent(artifact);
			return true;
		}
		return false;
	}

	protected abstract boolean addArtifactToBackEnd(Artifact artifact);

	/**
	 * @see DataBean#removeArtifact(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	public boolean removeArtifact(Artifact artifact) {
		// if the artifact is not in the bean already, do nothing
		//if (!contains (artifact))
		//return;

		// save the current state of the artifact
		Artifact artifactFrozenInTime = (Artifact) artifact.clone();

		// turn the firing event off for a moment
		boolean firing = isFiringEvents();
		setFiringEvents(false);

		// get the id of the artifact before any removing happens
		//String id = (String)artifact.getID();

		// remove the relationships associated with this artifact
		Vector relationships = artifact.getRelationships();
		Vector relsToDelete = new Vector();
		//Relationship toParentRelationship = null;	// this must be deleted last!
		for (int i = 0; i < relationships.size(); i++) {
			// if the relationship is the one between the artifact and its parent, store it and delete it last
			// note: the element 1 is the relationship's artifacts is the child of the relationship
			Relationship rel = (Relationship) relationships.elementAt(i);
			//if (rel.getType().equals (cprel) && rel.getArtifacts().elementAt(1).equals (artifact))
			//toParentRelationship = rel;

			// Remove the relationship.  Since there must be two artifacts in every relationship,
			// the entire relationship can be removed
			//else
			relsToDelete.add(rel);
			//removeRelationship (rel);
		}
		removeRelationships(relsToDelete);
		//if (toParentRelationship != null)
		//removeRelationship (toParentRelationship);

		// refresh to make sure we're in sync with back-end
		clearBufferedData();

		// Before we're done: check to see if it was the last node of its type.
		// If it is, remove the type
		String artifactTypeName = artifact.getType();
		Vector artifactType = (Vector) bufferArtifactTypes.get(artifactTypeName);
		artifactType.remove(artifact);
		if (artifactType.size() == 0) {
			bufferArtifactTypes.remove(artifactTypeName);
		}

		// let everyone know that the data has changed by sending them the frozen artifact
		fireArtifactRemoveEvent(artifactFrozenInTime);

		// turn the firing events back to original setting
		setFiringEvents(firing);
		return true;
	}

	/**
	 * Essentially, "buffers" the given artifact in this data bean.
	 */
	protected void addArtifactReference(Artifact artifact) {
		bufferArtifactsCreated.put(new Long(artifact.getID()), artifact);
		bufferExternalToInternalIdMap.put(artifact.getExternalId(), new Long(artifact.getID()));
		// add the type ref
		String artType = artifact.getType();
		Vector artsWithType = (Vector) bufferArtifactTypes.get(artType);
		if (artsWithType == null) {
			artsWithType = new Vector();
			bufferArtifactTypes.put(artType, artsWithType);
			fireDataTypesChangeEvent(new DataTypesChangeEvent());
		}
		artsWithType.addElement(artifact);
	}

	/**
	 * Essentially, "unbuffers" the given artifact from this data bean.
	 */
	protected void removeArtifactReference(Artifact artifact) {
		bufferArtifactsCreated.remove(new Long(artifact.getID()));
	}

	protected Artifact getArtifactReference(Long internalId) {
		return (Artifact) bufferArtifactsCreated.get(internalId);
	}

	protected Artifact getArtifactReference(long internalId) {
		return (Artifact) bufferArtifactsCreated.get(new Long(internalId));
	}

	/* ************************* Relationship methods ******************* */

	/**
	 * @see DataBean#getRelationship(long)
	 */
	public Relationship getRelationship(long id) {
		// TODO implement a faster way to do this
		Relationship rel = null;
		for (Iterator iter = getRelationships(false).iterator(); iter.hasNext();) {
			Relationship tmpRel = (Relationship) iter.next();
			if (tmpRel.getID() == id) {
				rel = tmpRel;
				break;
			}
		}
		return rel;
	}

	/**
	 *
	 * @see DataBean#getRelationships(boolean)
	 */
	public Vector getRelationships(boolean lookInBackEnd) {
		Vector rels = new Vector();
		if (lookInBackEnd) {
			if (bufferAllBackEndRels == null) {
				bufferAllBackEndRels = new HashSet();
				Vector arts = getArtifacts(true);
				boolean showProgress = arts.size() > 100;
				if (showProgress) {
					ProgressDialog.showProgress();
				}
				for (int i = 0; i < arts.size() && (!showProgress || !ProgressDialog.isCancelled()); i++) {
					Artifact artifact = (Artifact) arts.elementAt(i);
					if (showProgress) {
						ProgressDialog.setNote("Getting all relationships " + "(" + i + " of " + arts.size() + ") ...");
					}
					bufferAllBackEndRels.addAll(getIncomingAndOutgoingRelationships(artifact));
				}
				// @tag Shrimp.bufferRelationships : need to buffer relationships for composite arcs to work
				bufferRelationshipTypes.clear();
				for (Iterator iter = bufferAllBackEndRels.iterator(); iter.hasNext();) {
					Relationship rel = (Relationship) iter.next();
					String type = rel.getType();
					if (!bufferRelationshipTypes.containsKey(type)) {
						bufferRelationshipTypes.put(type, new Vector());
					}
					Vector relsOfType = (Vector) bufferRelationshipTypes.get(type);
					relsOfType.add(rel);
				}
				if (showProgress) {
					ProgressDialog.tryHideProgress();
				}
			}
			rels.addAll(bufferAllBackEndRels);

		} else {
			for (Iterator iter = bufferRelationshipTypes.values().iterator(); iter.hasNext();) {
				Vector relsOfType = (Vector) iter.next();
				rels.addAll(relsOfType);
			}
		}
		return rels;
	}

	/**
	 * Returns the relationships between the given artifacts.  A relationship
	 * may be a direct or composite relationship between the artifacts.
	 * An empty vector is returned if there isn't a single relationship between all of the
	 * given artifacts.
	 * @param artifacts The artifacts of interest.
	 */
	protected Vector getRelationships(Vector artifacts) {
		Vector relationships = new Vector();
		if (artifacts.size() > 0) {
			// get the relationships for all vectors and compare
			Artifact artifact = (Artifact) artifacts.elementAt(0);
			relationships = (Vector) getIncomingAndOutgoingRelationships(artifact).clone();
			for (int i = 1; i < artifacts.size(); i++) {
				Artifact anotherArtifact = (Artifact) artifacts.elementAt(i);
				Vector anotherRel = getIncomingAndOutgoingRelationships(anotherArtifact);
				for (int j = 0; j < relationships.size(); j++) {
					if (!anotherRel.contains(relationships.elementAt(j))) {
						relationships.removeElementAt(j);
						j--;
					}
				}
			}
		}
		return relationships;
	}

	/**
	 * Registers a new relationship with this Bean. If it is the first relationship
	 * of its type, the type will be added to the type list.
	 *
	 * Note: If the relationship is already in the list, it will not be added
	 * Note: If less than or more than two artifacts are specified, the request will be ignored.
	 *
	 * @param relationship the relationship to be added.
	 * If it is <code>null</code>, no operation is done to this DataBean.
	 *
	 * Rob's blurb (Jun 24,2002): This method seems to be for adding a "child-parent" relationship to this
	 * databean only? The parent of this relationship should be in the databean already
	 * but the child shouldn't be and will be added by calling this method.
	 */
	public boolean addRelationship(Relationship relationship) {
		Vector artifacts = relationship.getArtifacts();

		// make sure that all artifact are added to the databean before adding the relationship
		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
			Artifact artifact = (Artifact) iter.next();
			if (findArtifactByExternalId(artifact.getExternalId()) == null) {
				boolean artifactAdded = addArtifact(artifact);
				if (!artifactAdded) {
					return false;
				}
			}
		}

		if (addRelationshipToBackEnd(relationship)) { // returns true if successful
			/////////////////// update all shortcut references /////////////////////
			dataIsDirty();
			clearBufferedData(); //TODO have a more streamlined way of updating buffered data so we don't lose it all

			// Add the relationship type
			Vector type = (Vector) bufferRelationshipTypes.get(relationship.getType());
			if (type == null) {
				type = new Vector();
				bufferRelationshipTypes.put(relationship.getType(), type);
			}
			if (!type.contains(relationship)) {
				type.addElement(relationship);
			}

			// fire event
			fireRelationshipAddEvent(relationship);
			return true;
		}
		return false;
	}

	/**
	 * Adds the relationship to the backend domain data.
	 * NOTE: This is the only method of adding relationships AND artifacts to the back-end data.
	 *
	 * @return Whether or not the relationship was added successfully to the back-end
	 */
	abstract protected boolean addRelationshipToBackEnd(Relationship relationship);

	/**
	 * Removes a relationship from this Bean.  If it is the last relationship
	 * of its type, the type will be removed from the type list.
	 *
	 * Transaction:
	 *      relationships = relationships - {relationship}
	 *      if (last relationship of relationshipType)
	 *             relationshipTypes = relationshipTypes - {relationshipType}
	 *
	 * @param relationship the relationship to be removed.
	 */
	public boolean removeRelationship(Relationship relationship) {
		// turn the firing event off for a moment - allows one event to be fired instead of 5 or 6.
		boolean firing = isFiringEvents();
		setFiringEvents(false);

		// if the arc isn't in the data bean, return
		//if (!contains(relationship))
		//return;

		// Remove the composite relationship
		//removeAllCompositeRelationships (relationship);

		if (removeRelationshipFromBackEnd(relationship)) { // if true, the child was removed
			// remove the reference to the artifact
			removeArtifactReference((Artifact) relationship.getArtifacts().elementAt(1));
		}

		/////////////////// update all shortcut references /////////////////////

		// recalculate the links
		clearBufferedData();

		// remove relationship from list of created relationships
		//if (relationshipsCreated.contains (relationship))
		//relationshipsCreated.remove (relationship);

		// Before we're done: check to see if it was the last relationship of its type.
		// If it is, remove the type
		Vector type = getRelationshipsOfType(relationship.getType(), true);
		type.removeElement(relationship);
		if (type.size() == 0) {
			bufferRelationshipTypes.remove(relationship.getType());
		}

		// let everyone know that the data has changed - fire event
		fireRelationshipRemoveEvent(relationship);
		// turn the firing events back to original setting
		setFiringEvents(firing);

		return true;
	}

	// Removes a vector of relationships
	private void removeRelationships(Vector relationships) {
		Vector removed = new Vector();
		// turn the firing event off for a moment - allows one event to be fired instead of 5 or 6.
		boolean firing = isFiringEvents();
		setFiringEvents(false);

		for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
			Relationship relationship = (Relationship) iterator.next();

			// if the arc isn't in the data bean, return
			//if (!contains(relationship))
			//return;

			// Remove the composite relationship
			//removeAllCompositeRelationships(relationship);

			if (removeRelationshipFromBackEnd(relationship)) { // if true, the child was removed
				// remove the reference to the artifact
				removeArtifactReference((Artifact) relationship.getArtifacts().elementAt(1));
			}
			// remove relationship from list of created relationships
			//if (relationshipsCreated.contains (relationship))
			//relationshipsCreated.remove (relationship);

			// Before we're done: check to see if it was the last relationship of its type.
			// If it is, remove the type
			Vector type = getRelationshipsOfType(relationship.getType(), true);
			type.removeElement(relationship);
			if (type.size() == 0) {
				bufferRelationshipTypes.remove(relationship.getType());
			}

			// let everyone know that the data has changed
			removed.addElement(relationship);
			fireRelationshipRemoveEvent(relationship);
		}

		// recalculate the links
		clearBufferedData();
		// turn the firing events back to original setting
		setFiringEvents(firing);
	}

	/**
	 * Removes the relationship from the back-end.
	 *
	 *	If only visualizing the back end, don't worry about this method.
	 *	Fill this method in to remove the data from the domain.
	 *	Note: An event is already been fired for the relationship removed in the method that calls this one.
	 *
	 * @return The return for this method is true if the relationship was removed
	 */
	abstract protected boolean removeRelationshipFromBackEnd(Relationship relationship);

	/* *********************** Type methods *********************** */

	/**
	 *
	 * @see DataBean#getArtifactsOfType(java.lang.String, boolean)
	 */
	public Vector getArtifactsOfType(String type, boolean lookInBackEnd) {
		if (type == null) {
			return new Vector();
		}

		Vector ret = new Vector();
		if (lookInBackEnd) {
			getArtifacts(lookInBackEnd); // this will create all artifacts from the back end
		}
		if (bufferArtifactTypes.containsKey(type)) {
			ret = (Vector) bufferArtifactTypes.get(type);
		}
		return ret;
	}

	/**
	 * @see DataBean#setArtifactTypeIcon(String, Icon)
	 */
	public void setArtifactTypeIcon(String type, Icon icon) {
		artTypeToIcon.put(type, icon);
	}

	/**
	 * @see DataBean#getArtifactTypeIcon(String)
	 */
	public Icon getArtifactTypeIcon(String type) {
		return (Icon) artTypeToIcon.get(type);
	}

	/**
	 * @see DataBean#getArtifactIcon(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	public Icon getArtifactIcon(Artifact artifact) {
		return null;
	}

	/**
	 * @see DataBean#getRelationshipsOfType(java.lang.String, boolean)
	 */
	public Vector getRelationshipsOfType(String type, boolean lookInBackEnd) {
		Vector relationshipsOfType = new Vector();
		if (lookInBackEnd) {
			//Vector fromBackEnd = findRelationshipsOfTypeInBackEnd(type);
			// this will create all relationships from the back end and populate bufferRelationshipTypes
			getRelationships(true);
		}
		if (bufferRelationshipTypes.containsKey(type)) {
			relationshipsOfType = new Vector((Vector) bufferRelationshipTypes.get(type));
		}
		return relationshipsOfType;
	}

	//protected abstract Vector findRelationshipsOfTypeInBackEnd (String type);

	/* **************************** Listeners **************************** */

	/**
	 * Sets the ability to fire events.  If true is indicated, this bean
	 * will fire events.  If false is indicated, this bean will not fire
	 * events.
	 * Note: If this is turned off, the events will collect.  When it is
	 *       turned on again, all of the events will be joined and fired
	 *       in one large event.  Only the oldest value, and the newest
	 *       value of each artifact will be used.
	 *
	 * @param on Whether or not firing events is turned on
	 */
	public void setFiringEvents(boolean on) {
		firingEvents = on;
		if (firingEvents) {
			if (delayedFireDataTypesChangeEvent) {
				fireDataTypesChangeEvent(new DataTypesChangeEvent());
				delayedFireDataTypesChangeEvent = false;
			}
		}
	}

	/**
	 * @see DataBean#isFiringEvents()
	 */
	public boolean isFiringEvents() {
		return firingEvents;
	}

	/**
	 * @see DataBean#addDataTypesChangeListener(ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener)
	 */
	public void addDataTypesChangeListener(DataTypesChangeListener dtcl) {
		if (!dataTypesChangeListeners.contains(dtcl)) {
			dataTypesChangeListeners.add(dtcl);
		}
	}

	/**
	 * @see DataBean#removeDataTypesChangeListener(DataTypesChangeListener)
	 */
	public void removeDataTypesChangeListener(DataTypesChangeListener dtcl) {
		dataTypesChangeListeners.remove(dtcl);
	}

	/**
	 * @see DataBean#fireDataTypesChangeEvent(DataTypesChangeEvent)
	 */
	public void fireDataTypesChangeEvent(DataTypesChangeEvent dtce) {
		if (isFiringEvents()) {
			for (Iterator iter = dataTypesChangeListeners.iterator(); iter.hasNext();) {
				DataTypesChangeListener dtcl = (DataTypesChangeListener) iter.next();
				dtcl.dataTypesChange(dtce);
			}
		} else {
			delayedFireDataTypesChangeEvent = true;
		}
	}

	/**
	 * @see DataBean#addArtifactAddListener(ArtifactAddListener)
	 */
	public void addArtifactAddListener(ArtifactAddListener aal) {
		artifactAddListeners.add(aal);
	}

	/**
	 * @see DataBean#removeArtifactAddListener(ArtifactAddListener)
	 */
	public void removeArtifactAddListener(ArtifactAddListener aal) {
		artifactAddListeners.remove(aal);
	}

	/**
	 * @see DataBean#addArtifactRemoveListener(ArtifactRemoveListener)
	 */
	public void addArtifactRemoveListener(ArtifactRemoveListener arl) {
		artifactRemoveListeners.add(arl);
	}

	/**
	 * @see DataBean#removeArtifactRemoveListener(ArtifactRemoveListener)
	 */
	public void removeArtifactRemoveListener(ArtifactRemoveListener arl) {
		artifactRemoveListeners.remove(arl);
	}

	/**
	 * @see DataBean#addRootArtifactsChangeListener(RootArtifactsChangeListener)
	 */
	public void addRootArtifactsChangeListener(RootArtifactsChangeListener racl) {
		rootArtifactsChangeListeners.add(racl);
	}

	/**
	 * @see DataBean#removeRootArtifactsChangeListener(RootArtifactsChangeListener)
	 */
	public void removeRootArtifactsChangeListener(RootArtifactsChangeListener racl) {
		rootArtifactsChangeListeners.remove(racl);
	}

	/**
	 * @see DataBean#fireArtifactAddEvent(Artifact)
	 */
	public void fireArtifactAddEvent(Artifact artifact) {
		ArtifactAddEvent aae = new ArtifactAddEvent(artifact);
		for (int i = 0; i < artifactAddListeners.size(); i++) {
			ArtifactAddListener aal = (ArtifactAddListener) artifactAddListeners.elementAt(i);
			aal.addArtifact(aae);
		}
	}

	/**
	 * @see DataBean#fireArtifactRemoveEvent(Artifact)
	 */
	public void fireArtifactRemoveEvent(Artifact artifact) {
		ArtifactRemoveEvent are = new ArtifactRemoveEvent(artifact);
		for (int i = 0; i < artifactRemoveListeners.size(); i++) {
			ArtifactRemoveListener arl = (ArtifactRemoveListener) artifactRemoveListeners.elementAt(i);
			arl.removeArtifact(are);
		}
	}

	/**
	 * @see DataBean#fireRootArtifactsChangeEvent(Set)
	 */
	public void fireRootArtifactsChangeEvent(Set rootArtifacts) {
		RootArtifactsChangeEvent rce = new RootArtifactsChangeEvent(rootArtifacts);
		for (int i = 0; i < rootArtifactsChangeListeners.size(); i++) {
			RootArtifactsChangeListener racl = (RootArtifactsChangeListener) rootArtifactsChangeListeners.elementAt(i);
			racl.rootArtifactsChange(rce);
		}
	}

	/**
	 * @see DataBean#addRelationshipAddListener(RelationshipAddListener)
	 */
	public void addRelationshipAddListener(RelationshipAddListener ral) {
		relationshipAddListeners.add(ral);
	}

	/**
	 * @see DataBean#removeRelationshipAddListener(RelationshipAddListener)
	 */
	public void removeRelationshipAddListener(RelationshipAddListener ral) {
		relationshipAddListeners.remove(ral);
	}

	/**
	 * @see DataBean#addRelationshipRemoveListener(RelationshipRemoveListener)
	 */
	public void addRelationshipRemoveListener(RelationshipRemoveListener rrl) {
		relationshipRemoveListeners.remove(rrl);
	}

	/**
	 * @see DataBean#removeRelationshipRemoveListener(RelationshipRemoveListener)
	 */
	public void removeRelationshipRemoveListener(RelationshipRemoveListener rrl) {
		relationshipRemoveListeners.remove(rrl);
	}

	/**
	 * @see DataBean#addRelationshipChangeListener(RelationshipChangeListener)
	 */
	public void addRelationshipChangeListener(RelationshipChangeListener rcl) {
		relationshipChangeListeners.remove(rcl);
	}

	/**
	 * @see DataBean#removeRelationshipChangeListener(RelationshipChangeListener)
	 */
	public void removeRelationshipChangeListener(RelationshipChangeListener rcl) {
		relationshipChangeListeners.remove(rcl);
	}

	/**
	 * @see DataBean#fireRelationshipAddEvent(Relationship)
	 */
	public void fireRelationshipAddEvent(Relationship relationship) {
		RelationshipAddEvent rae = new RelationshipAddEvent(relationship);
		for (int i = 0; i < relationshipAddListeners.size(); i++) {
			RelationshipAddListener ral = (RelationshipAddListener) relationshipAddListeners.elementAt(i);
			ral.addRelationship(rae);
		}
	}

	/**
	 * @see DataBean#fireRelationshipRemoveEvent(Relationship)
	 */
	public void fireRelationshipRemoveEvent(Relationship relationship) {
		RelationshipRemoveEvent rre = new RelationshipRemoveEvent(relationship);
		for (int i = 0; i < relationshipRemoveListeners.size(); i++) {
			RelationshipRemoveListener rrl = (RelationshipRemoveListener) relationshipRemoveListeners.elementAt(i);
			rrl.removeRelationship(rre);
		}
	}

	/**
	 * @see DataBean#fireRelationshipChangeEvent(Relationship)
	 */
	public void fireRelationshipChangeEvent(Relationship relationship) {
		RelationshipChangeEvent rce = new RelationshipChangeEvent(relationship);
		for (int i = 0; i < relationshipChangeListeners.size(); i++) {
			RelationshipChangeListener rcl = (RelationshipChangeListener) relationshipChangeListeners.elementAt(i);
			rcl.changeRelationship(rce);
		}
	}

	protected String createRelName(Artifact srcArt, Artifact destArt, String relType) {
		String name = relType + ": \"" + srcArt.getName() + "\" " + " to " + " \"" + destArt.getName() + "\"";
		return name;
	}

	protected Relationship getExistingEquivalentRel(Object externalId, Artifact srcArt, Artifact destArt, String relType, String name) {
		Object key = createUniqueRelKey(externalId, srcArt, destArt, relType, name);
		Relationship existingRel = (Relationship) bufferRelationshipsCreated.get(key);
		return existingRel;
	}

	/* If there is no external id, we'll consider a relationship equivalent if it has the same src, dest, type, and name
	 * TODO is there a better way to do this?
	 */
	private Object createUniqueRelKey(Object externalId, Artifact srcArt, Artifact destArt, String relType, String name) {
		Object key = externalId != null ? externalId : srcArt.getID() + destArt.getID() + relType + name;
		return key;
	}

	/**
	 * Handles the details of creating a new relationship
	 * Gives the relationship a default name
	 * @see #createRel(Object, Artifact, Artifact, String, String)
	 */
	protected Relationship createRel(Object externalId, Artifact srcArt, Artifact destArt, String relType) {
		return createRel(externalId, srcArt, destArt, relType, createRelName(srcArt, destArt, relType));
	}

	/**
	 * Handles the details of creating a new relationship
	 */
	protected Relationship createRel(Object externalId, Artifact srcArt, Artifact destArt, String relType, String name) {
		// Should we print out a warning here?  Jambalaya has re-ifed relationships that are filtered
		//if (isRelTypeFiltered(relType)) {
			//System.err.println("Trying to create a relationship where relType is filtered: " + relType);
		//}

		// see if an equivalent relationship exists already
		Relationship existingRel = getExistingEquivalentRel(externalId, srcArt, destArt, relType, name);
		if (existingRel != null) {
			return existingRel;
		}

		Vector artifacts = new Vector();
		artifacts.addElement(srcArt);
		artifacts.addElement(destArt);

		Relationship rel = createEmptyRelationship(externalId, name, relType, artifacts);
		Object key = createUniqueRelKey(externalId, srcArt, destArt, relType, name);
		bufferRelationshipsCreated.put(key, rel);
		Vector relationshipsOfType = (Vector) bufferRelationshipTypes.get(rel.getType());
		if (relationshipsOfType == null) {
			relationshipsOfType = new Vector();
			bufferRelationshipTypes.put(rel.getType(), relationshipsOfType);
			fireDataTypesChangeEvent(new DataTypesChangeEvent());
		}
		//System.out.println("created rel: " + rel);
		relationshipsOfType.addElement(rel);

		return rel;
	}

	protected long nextID() {
		return currID++;
	}

	public boolean isRelTypeFiltered(String type) {
		return fireIsFilteredRequest(type, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE);
	}

	public boolean isArtTypeFiltered(String type) {
		return fireIsFilteredRequest(type, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE);
	}

	/**
	 * A value is considered ordinal if it is a java.lang.Number or java.util.Date
	 * otherwise it is considered a nominal attribute.
	 * @param attrValue
	 * @return ORDINAL_ATTRIBUTE or NOMINAL_ATTRIBUTE
	 */
	protected int getAttributeType(Object attrValue) {
		if (attrValue instanceof Number || attrValue instanceof Date) {
			return ORDINAL_ATTRIBUTE;
		}
		return NOMINAL_ATTRIBUTE;
	}

	/**
	 * Iterates through artifacts and maps attribute names to all the values that they have
	 * @param lookInBackEnd
	 * @param attrType AbstractDataBean.ORDINAL_ATTRIBUTE or AbstractDataBean.NOMINAL_ATTRIBUTE
	 * @param targetType DataBean.ARTIFACT_TYPE or DataBean.RELATIONSHIP_TYPE
	 */
	private Map getAttributeValues(boolean lookInBackEnd, int attrType, int targetType) {
		Vector objects = new Vector();
		if (targetType == ARTIFACT_TYPE) {
			objects = getArtifacts(lookInBackEnd);
			Collections.sort(objects, new Comparator() {

				public int compare(Object o1, Object o2) {
					return ((Artifact) o1).getName().compareToIgnoreCase(((Artifact) o2).getName());
				}
			});
		} else if (targetType == RELATIONSHIP_TYPE) {
			objects = getRelationships(lookInBackEnd);
			Collections.sort(objects, new Comparator() {

				public int compare(Object o1, Object o2) {
					return ((Relationship) o1).getName().compareToIgnoreCase(((Relationship) o2).getName());
				}
			});
		} else {
			return new HashMap();
		}
		Map attrNameToValuesMap = new HashMap();
		for (Iterator iter = objects.iterator(); iter.hasNext();) {
			Object object = iter.next();
			//System.out.println("getting all attributes for " + object + " ...");
			Vector attrNames = targetType == ARTIFACT_TYPE ? ((Artifact) object).getAttributeNames() : ((Relationship) object).getAttributeNames();
			for (Iterator iterator = attrNames.iterator(); iterator.hasNext();) {
				String attrName = (String) iterator.next();
				// don't bother using "name" or "id"...there are way too many values for large projects
				// don't bother using "openable", it is not of interest to user
				// definitely don't bother with source code
				//TODO there needs to be a better way to specify which attributes should be returned
				if (!attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_ID) && !attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_NAME)
						&& !attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_DISPLAY_TEXT)
						&& !attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME)
						&& !attrName.equals(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI)
						&& !attrName.equals(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE) && !attrName.equals(AttributeConstants.NOM_ATTR_OPENABLE)
						&& !attrName.equals(AttributeConstants.NOM_ATTR_REL_DISPLAY_TEXT)
						&& !attrName.equals(AttributeConstants.NOM_ATTR_REL_SHORT_DISPLAY_TEXT)) {
					Object attrValue = targetType == ARTIFACT_TYPE ? ((Artifact) object).getAttribute(attrName) : ((Relationship) object)
							.getAttribute(attrName);
					if (attrValue instanceof Comparable) {
						if (getAttributeType(attrValue) == attrType) {
							SortedSet attrValues = (SortedSet) attrNameToValuesMap.get(attrName);
							if (attrValues == null) {
								attrValues = new TreeSet();
								attrNameToValuesMap.put(attrName, attrValues);
							}
							attrValues.add(attrValue);
						}
					}
				}
			}
		}
		return attrNameToValuesMap;
	}

	/**
	 * Iterates through artifacts and maps attribute names to all the values that they have
	 * @param lookInBackEnd
	 * @param attrType AbstractDataBean.ORDINAL_ATTRIBUTE or AbstractDataBean.NOMINAL_ATTRIBUTE
	 * @param targetType DataBean.ARTIFACT_TYPE or DataBean.RELATIONSHIP_TYPE
	 */
	public Map getNodeDisplayAttrValues(boolean lookInBackEnd) {
		Vector objects = getArtifacts(lookInBackEnd);
		Collections.sort(objects, new Comparator() {

			public int compare(Object o1, Object o2) {
				return ((Artifact) o1).getName().compareToIgnoreCase(((Artifact) o2).getName());
			}
		});
		Map attrNameToValuesMap = new HashMap();
		for (Iterator iter = objects.iterator(); iter.hasNext();) {
			Object object = iter.next();
			//System.out.println("getting all attributes for " + object + " ...");
			Vector attrNames = ((Artifact) object).getAttributeNames();
			for (Iterator iterator = attrNames.iterator(); iterator.hasNext();) {
				String attrName = (String) iterator.next();
				if (attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_DISPLAY_TEXT)
						|| attrName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME)
						|| attrName.equals(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE)) {
					Object attrValue = ((Artifact) object).getAttribute(attrName);
					if (attrValue instanceof Comparable) {
						SortedSet attrValues = (SortedSet) attrNameToValuesMap.get(attrName);
						if (attrValues == null) {
							attrValues = new TreeSet();
							attrNameToValuesMap.put(attrName, attrValues);
						}
						attrValues.add(attrValue);
					}
				}
			}
		}
		return attrNameToValuesMap;
	}

	/**
	 * Overwrite this method to add more nominal attribute values for a
	 * specific domain. Make sure to call super if you want to include
	 * the default nominal attributes.
	 * @see DataBean#getNominalAttrValues(String[], boolean, boolean, int)
	 */
	public Map getNominalAttrValues(String[] cprels, boolean lookInBackEnd, boolean inverted, int targetType) {
		return getAttributeValues(lookInBackEnd, NOMINAL_ATTRIBUTE, targetType);
	}

	/**
	 * Overwrite this method to add more ordinal attribute values for a
	 * specific domain. Make sure to call super if you want to include
	 * the default ordinal attributes.
	 * @see DataBean#getOrdinalAttrValues(String[], boolean, boolean, int)
	 */
	public Map getOrdinalAttrValues(String[] cprels, boolean lookInBackEnd, boolean inverted, int targetType) {
		Map values = getAttributeValues(lookInBackEnd, ORDINAL_ATTRIBUTE, targetType);
		//we should have most values at this point,
		// but need to add a few more that we don't want to calculate until
		// we really need to, like now
		if (targetType == ARTIFACT_TYPE) {
			Vector artifacts = getArtifacts(lookInBackEnd);
			SortedSet numChildrenValues = new TreeSet();
			SortedSet numDescendentsValues = new TreeSet();
			String key = ShrimpUtils.cprelsToKey(cprels);
			for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
				Artifact artifact = (Artifact) iter.next();
				Integer numChildren = new Integer(inverted ? artifact.getParentsCount(cprels) : artifact.getChildrenCount(cprels));
				numChildrenValues.add(numChildren);
				if (inverted) {
					artifact.setAttribute(AttributeConstants.ORD_ATTR_NUM_CHILDREN + key + (inverted ? " (inverted)" : ""),
							numChildren);
				}
				if (lookInBackEnd) {
					// if we are looking to the backend then get the descendent info
					// otherwise it is too expensive to calculate
					Integer numDescendents = new Integer(inverted ? artifact.getAncestors(cprels).size() : artifact.getDescendentsCount(cprels, true));
					numDescendentsValues.add(numDescendents);
					if (inverted) {
						artifact.setAttribute(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS + key + (inverted ? " (inverted)" : ""),
								numDescendents);
					}
				}
			}
			values.put(AttributeConstants.ORD_ATTR_NUM_CHILDREN + key + (inverted ? " (inverted)" : ""), numChildrenValues);
			if (lookInBackEnd) {
				values.put(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS + key + (inverted ? " (inverted)" : ""), numDescendentsValues);
			}
		}
		return values;
	}

	/**
	 * @see DataBean#getCompositeRelationshipsByType(String, String[], String, boolean)
	 */
	public Vector getCompositeRelationshipsByType(String type, String[] cprels, String compositeType, boolean inverted) {
		Vector compRels = new Vector();
		Vector relsOfType = getRelationshipsOfType(type, true);
		List cprelsList = Arrays.asList(cprels);
		for (Iterator iter = relsOfType.iterator(); iter.hasNext(); ) {
			Relationship rel = (Relationship) iter.next();
			if (!cprelsList.contains(rel)) {
				Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
				Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
				long[] srcAncestorIDs = getAncestorIDs(srcArt, cprels, inverted);
				long[] destAncestorIDs = getAncestorIDs(destArt, cprels, inverted);
				CompositeRelationship compRel = new CompositeRelationship(compositeType, srcAncestorIDs, destAncestorIDs, rel);
				compRels.add(compRel);
			}
		}
		return compRels;
	}

	private long[] getAncestorIDs(Artifact art, String[] cprels, boolean inverted) {
		Vector ancestors = inverted ? getDescendents(art, cprels) : getAncestors(art, cprels);
		long[] ancestorIDs = new long[ancestors.size()];
		int i = 0;
		for (Iterator iter = ancestors.iterator(); iter.hasNext();) {
			Artifact ancestor = (Artifact) iter.next();
			ancestorIDs[i++] = ancestor.getID();
		}
		return ancestorIDs;
	}

	/**
	 * @see DataBean#findArtifacts(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	public Collection findArtifacts(String attrName, Object attrValue, String[] cprels) {
		Set foundArtifacts = new HashSet();
		for (Iterator iter = getRootArtifacts(cprels).iterator(); iter.hasNext();) {
			Artifact rootArtifact = (Artifact) iter.next();
			findArtifactsRecursive(rootArtifact, foundArtifacts, new HashSet(), attrName, attrValue, cprels, false);
		}
		return foundArtifacts;
	}

	/**
	 * @see DataBean#findFirstArtifact(java.lang.String, java.lang.Object, java.lang.String[])
	 */
	public Artifact findFirstArtifact(String attrName, Object attrValue, String[] cprels) {
		Artifact artifact = null;
		Set foundArtifacts = new HashSet();
		for (Iterator iter = getRootArtifacts(cprels).iterator(); iter.hasNext() && foundArtifacts.isEmpty();) {
			Artifact rootArtifact = (Artifact) iter.next();
			findArtifactsRecursive(rootArtifact, foundArtifacts, new HashSet(), attrName, attrValue, cprels, true);
		}
		if (!foundArtifacts.isEmpty()) {
			artifact = (Artifact) foundArtifacts.iterator().next();
		}
		return artifact;
	}

	private void findArtifactsRecursive(Artifact artifact, Set foundArtifacts, Set seenArtifacts, String attrNameToFind, Object attrValueToFind,
			String[] cprels, boolean findFirstOnly) {
		if (seenArtifacts.contains(artifact)) {
			return;
		}
		seenArtifacts.add(artifact);
		Object attrValue = artifact.getAttribute(attrNameToFind);
		if (attrValue != null && attrValue.equals(attrValueToFind)) {
			foundArtifacts.add(artifact);
			if (findFirstOnly) {
				return;
			}
		}
		for (Iterator iter = artifact.getChildren(cprels).iterator(); iter.hasNext();) {
			Artifact child = (Artifact) iter.next();
			findArtifactsRecursive(child, foundArtifacts, seenArtifacts, attrNameToFind, attrValueToFind, cprels, findFirstOnly);
			// if only looking for first found, don't bother looking at the other children if we've found an artifact
			if (findFirstOnly && !foundArtifacts.isEmpty()) {
				return;
			}
		}
	}

	/**
	 * @see DataBean#dataIsDirty()
	 */
	public boolean dataIsDirty() {
		return isDirty;
	}

	/**
	 * @see DataBean#setDataIsDirty()
	 */
	public void setDataIsDirty() {
		isDirty = true;
	}

	/**
	 * @see DataBean#getShortestPath(Artifact, Artifact, List)
	 * A default implementation of getShortestPath...nothing fancy, just a simple n^2 implementation.
	 */
	public List getShortestPath(Artifact srcArtifact, Artifact destArtifact, Collection relTypes) {
		shortestPathSoFar = new ArrayList();
		getShortestPathRecursive(srcArtifact, destArtifact, new ArrayList(), relTypes);
		return shortestPathSoFar;
	}

	private void getShortestPathRecursive(Artifact currentArtifact, Artifact destArtifact, List thisPathSoFar, Collection relTypes) {
		if (thisPathSoFar.contains(currentArtifact)) {
			//we have a cycle
		} else {

			if (currentArtifact.equals(destArtifact)) {

				// we are at the end of a path
				// see if it the shortest
				if (shortestPathSoFar.isEmpty() || (thisPathSoFar.size() + 1 < shortestPathSoFar.size())) {
					shortestPathSoFar = new ArrayList(thisPathSoFar);
					shortestPathSoFar.add(destArtifact);
				}
			} else {
				thisPathSoFar.add(currentArtifact);
				Vector rels = currentArtifact.getRelationships();
				for (Iterator iter = rels.iterator(); iter.hasNext();) {
					Relationship rel = (Relationship) iter.next();
					if (relTypes.contains(rel.getType())) {
						Artifact parentArt = (Artifact) rel.getArtifacts().elementAt(0);
						Artifact childArt = (Artifact) rel.getArtifacts().elementAt(1);
						if (parentArt.equals(currentArtifact) && !childArt.equals(currentArtifact)) {
							getShortestPathRecursive(childArt, destArtifact, thisPathSoFar, relTypes);
						}
					}
				}
				thisPathSoFar.remove(currentArtifact);
			}
		}
	}

	public Collection getConnectedArtifacts(Collection srcArtifacts, Collection artTypes, Collection relTypes, int incomingDistance,
			int outgoingDistance) {
		List connectedArtifacts = new ArrayList();
		for (Iterator iter = srcArtifacts.iterator(); iter.hasNext();) {
			Artifact srcArtifact = (Artifact) iter.next();
			// INCOMING
			getConnectedArtifactsRecursive(srcArtifact, artTypes, relTypes, (incomingDistance > 0), incomingDistance, false, new HashSet(),
					connectedArtifacts, 1);
			// OUTGOING
			getConnectedArtifactsRecursive(srcArtifact, artTypes, relTypes, (outgoingDistance > 0), outgoingDistance, true, new HashSet(),
					connectedArtifacts, 1);
		}
		return connectedArtifacts;
	}

	private void getConnectedArtifactsRecursive(Artifact currentArtifact, Collection artifactTypes, Collection relTypes, boolean restrictDistance,
			int maxDistance, boolean outgoing, Collection seenArtifacts, List connectedArtifacts, int currentDistance) {

		if (ProgressDialog.isCancelled()) {
			return;
		}
		if (restrictDistance && (currentDistance > maxDistance)) {
			return;
		}
		if (!artifactTypes.contains(currentArtifact.getType())) {
			return;
		}
		if (seenArtifacts.contains(currentArtifact)) {
			Integer level = (Integer) currentArtifact.getAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE);
			if (level.intValue() > currentDistance) {
				currentArtifact.setAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE, new Integer(currentDistance));
			}
			return;
		}

		seenArtifacts.add(currentArtifact);
		currentArtifact.setAttribute(AttributeConstants.ORD_ATTR_PATH_DISTANCE, new Integer(currentDistance));

		//System.out.println(currentArtifact);
		if (!connectedArtifacts.contains(currentArtifact)) {
			connectedArtifacts.add(currentArtifact);
		}
		Vector rels = (outgoing ? getOutgoingRelationships(currentArtifact) : getIncomingRelationships(currentArtifact));
		for (Iterator iter = rels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			if (!relTypes.contains(rel.getType())) {
				continue;
			}
			Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
			Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
			if (!srcArt.equals(destArt)) {
				Artifact nextArtifact = (outgoing ? destArt : srcArt);
				getConnectedArtifactsRecursive(nextArtifact, artifactTypes, relTypes, restrictDistance, maxDistance, outgoing, seenArtifacts,
						connectedArtifacts, currentDistance + 1);
			}
		}

	}

	/* (non-Javadoc)
	 * @see DataBean#getDefaultGroupForRelationshipType(java.lang.String)
	 */
	public String getDefaultGroupForRelationshipType(String relType) {
		return ShrimpConstants.DEFAULT_GROUP;
	}

	/* (non-Javadoc)
	 * @see DataBean#setWorkingSet(java.lang.Object)
	 */
	public void setWorkingSet(Object newWorkingSet) {
		// do nothing...overwrite this method to set the scope of the data within your databean
	}

	/* (non-Javadoc)
	 * @see DataBean#dataFiltersHaveChanged()
	 *
	 * Overwrite this method to make it more efficient
	 */
	public void dataFiltersHaveChanged() {
		//System.out.println("dataFiltersChanged");
		setDataIsDirty();
		clearBufferedData();
	}

	public void addFilterRequestListener(DataFilterRequestListener listener) {
		if (!filterRequestListeners.contains(listener)) {
			filterRequestListeners.add(listener);
		}
	}

	public void removeFilterRequestListener(DataFilterRequestListener listener) {
		filterRequestListeners.remove(listener);
	}

	protected boolean fireIsFilteredRequest(String type, String targetFilterType) {
		boolean filtered = false;
		for (Iterator iter = filterRequestListeners.iterator(); iter.hasNext() && !filtered;) {
			DataFilterRequestListener listener = (DataFilterRequestListener) iter.next();
			filtered = listener.isFiltered(type, targetFilterType);
		}
		return filtered;
	}

	public boolean isExportingData() {
		return isExportingData;
	}

	public void setExportingData(boolean isExportingData) {
		this.isExportingData = isExportingData;
	}

}
