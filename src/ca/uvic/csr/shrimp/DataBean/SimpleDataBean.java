/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiArc;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiNode;

/**
 * A simple implementation of AbstractDataBean.
 *
 * To do a bulk population of the databean use the <code>setData</code> methods.
 * You can pass in <code>Map</code>s of GenericRigiNodes and
 * GenericRigiArcs created from reading an RSF, GXL, or other file format via the RSFPersistantStorageBean
 * or simply pass in lists of <code>Artifact</code>s and <code>Relationship</code>s.
 *
 * In this simple implementation there is no 'backend' data source. Internally this class just
 * keeps simple lists of <code>Artifact</code>s and <code>Relationship</code>s.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class SimpleDataBean extends AbstractDataBean implements Cloneable, Serializable {

	public final static String PRIMARY_DEFAULT_CPREL = "contains"; // some rsf and gxl files have these
	public final static String[] PRIMARY_DEFAULT_CPRELS = { PRIMARY_DEFAULT_CPREL };
	private final static String[] SECONDARY_DEFAULT_CPRELS = { "level" }; //some rsf files have these
	private final static String[] TERTIARY_DEFAULT_CPRELS = { "isInFile", "isDefinedIn" }; //some rsf files have these
	private final static String[] GXL_DEFAULT_CPRELS = { "containsNode", "containsGraph" }; // GXL files have these

	private final static boolean PRIMARY_DEFAULT_CPRELS_INVERTED = false;
	private final static boolean SECONDARY_DEFAULT_CPRELS_INVERTED = false;
	private final static boolean TERTIARY_DEFAULT_CPRELS_INVERTED = true;
	private final static boolean GXL_DEFAULT_CPRELS_INVERTED = false;

	/** The default child-parent relationships */
	protected String[] defaultCprels;

	/** Whether or not the default hiearchy should be inverted */
	protected boolean defaultCprelsInverted;

	/** pseudo backend artifact data */
	private List backEndArts = new ArrayList();

	/** pseudo backend relationship data */
	private List backEndRels = new ArrayList();

	private Map idToArtifactMapBuffer = new HashMap();
	private Map idToRelMapBuffer = new HashMap();

	private Map artToIncomingRelsBuffer = new HashMap();
	private Map artToOutgoingRelsBuffer = new HashMap();

	/**
	 * Constructor for SimpleDataBean.
	 */
	public SimpleDataBean() {
		super();
	}

	public void setData(List artifacts, List rels) {
		this.backEndArts = artifacts;
		this.backEndRels = rels;

		// set up the mappings from id to object and check id's for uniqueness
		for (Iterator iter = backEndArts.iterator(); iter.hasNext();) {
			Artifact art = (Artifact) iter.next();
			idToArtifactMapBuffer.put(art.getExternalId(), art);
		}
		for (Iterator iter = backEndRels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			idToRelMapBuffer.put(rel.getExternalId(), rel);
			Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
			List incomingRels = (List) artToIncomingRelsBuffer.get(destArt);
			if (incomingRels == null) {
				incomingRels = new ArrayList();
				artToIncomingRelsBuffer.put(destArt, incomingRels);
			}
			incomingRels.add(rel);
			Artifact sourceArt = (Artifact) rel.getArtifacts().elementAt(0);
			List outgoingRels = (List) artToOutgoingRelsBuffer.get(sourceArt);
			if (outgoingRels == null) {
				outgoingRels = new ArrayList();
				artToOutgoingRelsBuffer.put(sourceArt, outgoingRels);
			}
			outgoingRels.add(rel);
		}

		// figure out what the default cprel should be based on the passed in data
		Vector types = findRelationshipTypesInBackEnd();
		if (types.contains(PRIMARY_DEFAULT_CPRELS[0])) {
			defaultCprels = PRIMARY_DEFAULT_CPRELS;
			defaultCprelsInverted = PRIMARY_DEFAULT_CPRELS_INVERTED;
		} else if (types.contains(SECONDARY_DEFAULT_CPRELS[0])) {
			defaultCprels = SECONDARY_DEFAULT_CPRELS;
			defaultCprelsInverted = SECONDARY_DEFAULT_CPRELS_INVERTED;
		} else if (types.contains(TERTIARY_DEFAULT_CPRELS[0]) || types.contains(TERTIARY_DEFAULT_CPRELS[1])) {
			defaultCprels = TERTIARY_DEFAULT_CPRELS;
			defaultCprelsInverted = TERTIARY_DEFAULT_CPRELS_INVERTED;
		} else if (types.contains(GXL_DEFAULT_CPRELS[0]) || types.contains(GXL_DEFAULT_CPRELS[1])) {
			defaultCprels = GXL_DEFAULT_CPRELS;
			defaultCprelsInverted = GXL_DEFAULT_CPRELS_INVERTED;
		} else {
			defaultCprels = new String[0];
			defaultCprelsInverted = false;
		}
	}

	/**
	 * Sets the raw ("backend") data to be used by this databean.
	 * A convenience method to convert a bunch of GenericRigiNodes and
	 * GenericRigiArcs into Artifacts and Relationships
	 *
	 * @param nodesTable A table of GenericRigiNodes (key=node id, value = node)
	 * @param arcsTable	A table of GenericRigiArcs (key=arc id, value = arc)
	 */
	public void setData(Hashtable nodesTable, Hashtable arcsTable) {
		// remove isInFile arcs if there is an isDefinedIn arc to take its place
		List isInFileArcs = new ArrayList();
		List isDefinedInArcs = new ArrayList();
		for (Iterator iter = arcsTable.values().iterator(); iter.hasNext();) {
			GenericRigiArc arc = (GenericRigiArc) iter.next();
			if (arc.getArcType().equals("isInFile")) {
				isInFileArcs.add(arc);
			} else if (arc.getArcType().equals("isDefinedIn")) {
				isDefinedInArcs.add(arc);
			}
		}
		for (Iterator iter1 = isInFileArcs.iterator(); iter1.hasNext();) {
			GenericRigiArc isInFileArc = (GenericRigiArc) iter1.next();
			String fileMember = isInFileArc.getSourceID();
			for (Iterator iter2 = isDefinedInArcs.iterator(); iter2.hasNext();) {
				GenericRigiArc isDefinedInArc = (GenericRigiArc) iter2.next();
				String fileMemberMember = isDefinedInArc.getSourceID();
				if (fileMemberMember.equals(fileMember)) {
					//System.out.println("removing: " + isInFileArc);
					arcsTable.remove(isInFileArc.getArcID());
					break;
				}
			}
		}

		Map idToArtifactMap = new HashMap(nodesTable.size());
		List artifacts = new ArrayList(nodesTable.size());
		List rels = new ArrayList(arcsTable.size());
		for (Iterator iter = nodesTable.values().iterator(); iter.hasNext();) {
			GenericRigiNode node = (GenericRigiNode) iter.next();
			Artifact newArt = new SimpleArtifact(this, node.getNodeLabel(), node.getNodeType(), node.getNodeID());
			Vector keys = node.getCustomizedDataKeys();
			for (int j = 0; j < keys.size(); j++) {
				String key = (String) keys.elementAt(j);
				newArt.setAttribute(key, node.getCustomizedData(key));
			}
			idToArtifactMap.put(node.getNodeID(), newArt);
			artifacts.add(newArt);
		}
		for (Iterator iter = arcsTable.values().iterator(); iter.hasNext();) {
			GenericRigiArc arc = (GenericRigiArc) iter.next();
			Artifact srcArt = (Artifact) idToArtifactMap.get(arc.getSourceID());
			Artifact destArt = (Artifact) idToArtifactMap.get(arc.getDestID());
			if (srcArt == null) {
				System.err.println("Warning: Couldn't find source artifact for arc: " + arc);
			}
			if (destArt == null) {
				System.err.println("Warning: Couldn't find destination artifact for arc: " + arc);
			}
			if (srcArt == null || destArt == null) {
				continue;
			}
			Vector arts = new Vector(2);
			arts.add(srcArt);
			arts.add(destArt);
			Relationship newRel = createRel(arc.getArcID(), srcArt, destArt, arc.getArcType(), arc.getArcLabel());
			if (arc.getArcLabel() == null) {
				newRel.setName(createRelName(srcArt, destArt, arc.getArcType()));
			}
			Vector keys = arc.getCustomizedDataKeys();
			for (int j = 0; j < keys.size(); j++) {
				String key = (String) keys.elementAt(j);
				newRel.setAttribute(key, arc.getCustomizedData(key));
			}
			rels.add(newRel);
		}
		setData(artifacts, rels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#createArtifact(Object)
	 */
	protected Artifact createArtifact(Object externalId) {
		Artifact artifact = (Artifact) idToArtifactMapBuffer.get(externalId);
		if (artifact == null) {
			System.err.println("Warning: can't find artifact matching id = " + externalId);
		}
		return artifact;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#createEmptyRelationship(Object, java.lang.String, java.lang.String, java.util.Vector)
	 */
	protected Relationship createEmptyRelationship(Object externalId, String name, String type, Vector artifacts) {
		Relationship rel = (Relationship) idToRelMapBuffer.get(externalId);
		if (rel == null) {
			rel = new SimpleRelationship(this, name, type, artifacts, externalId);
			idToRelMapBuffer.put(externalId, rel);
		}
		return rel;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#addRelationshipToBackEnd(ca.uvic.csr.shrimp.DataBean.Relationship)
	 */
	protected boolean addRelationshipToBackEnd(Relationship relationship) {
		boolean added = false;
		if (!backEndRels.contains(relationship)) {
			added = backEndRels.add(relationship);
		}
		if (added) {
			idToRelMapBuffer.put(relationship.getExternalId(), relationship);
		}
		return added;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#removeRelationshipFromBackEnd(ca.uvic.csr.shrimp.DataBean.Relationship)
	 */
	protected boolean removeRelationshipFromBackEnd(Relationship relationship) {
		boolean removed = backEndRels.remove(relationship);
		if (removed) {
			idToRelMapBuffer.remove(relationship.getExternalId());
		}
		return removed;
	}

	/**
	 * @see AbstractDataBean#findRootArtifactsInBackEnd(String)
	 */
	protected Vector findRootArtifactsInBackEnd(String[] cprels) {
		return defaultFindRootArtifactsInBackEnd(cprels);
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findIncomingRelationshipsInBackEnd(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	protected Vector findIncomingRelationshipsInBackEnd(Artifact artifact) {
		Vector incomingRels = new Vector();
		findRelationshipsInBackEnd(artifact, incomingRels, false);
		return incomingRels;
	}

	/**
	 * @see AbstractDataBean#findRelationshipsInBackEnd(Artifact)
	 */
	protected Vector findOutgoingRelationshipsInBackEnd(Artifact artifact) {
		Vector outgoingRels = new Vector();
		findRelationshipsInBackEnd(artifact, outgoingRels, true);
		return outgoingRels;
	}

	/**
	 * @param artifact
	 * @param outgoingRels
	 */
	private void findRelationshipsInBackEnd(Artifact artifact, Vector rels, boolean outgoing) {
		if (outgoing) {
			List outgoingRels = (List) artToOutgoingRelsBuffer.get(artifact);
			if (outgoingRels != null) {
				rels.addAll(outgoingRels);
			}
		} else {
			List incomingRels = (List) artToIncomingRelsBuffer.get(artifact);
			if (incomingRels != null) {
				rels.addAll(incomingRels);
			}
		}
	}

	/**
	 * @see DataBean#getArtifactExternalIDFromString(String)
	 */
	public Object getArtifactExternalIDFromString(String IDInStringForm) {
		return IDInStringForm;
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DataBean.DataBean#getStringFromExternalArtifactID(java.lang.Object)
	 */
	public String getStringFromExternalArtifactID(Object externalID) {
		return externalID.toString();
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DataBean.DataBean#getStringFromExternalRelationshipID(java.lang.Object)
	 */
	public String getStringFromExternalRelationshipID(Object externalID) {
		return externalID.toString();
	}

	public String[] getDefaultCprels() {
		return defaultCprels;
	}

	public boolean getDefaultCprelsInverted() {
		return defaultCprelsInverted;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findAllArtifactsInBackEnd()
	 */
	protected Vector findAllArtifactsInBackEnd() {
		for (Iterator iter = backEndArts.iterator(); iter.hasNext();) {
			Artifact art = (Artifact) iter.next();
			if (!isArtTypeFiltered(art.getType())) {
				// TODO implement so that don't have to call findArtifactByExternalId
				// this is done so that all mapping in AbstractDataBean are created properly
				findArtifactByExternalId(art.getExternalId());
			}
		}
		return new Vector(backEndArts);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findAllArtifactTypesInBackEnd()
	 */
	protected Vector findArtifactTypesInBackEnd() {
		Vector types = new Vector();
		for (Iterator iter = backEndArts.iterator(); iter.hasNext();) {
			Artifact art = (Artifact) iter.next();
			String type = art.getType();
			if (!types.contains(type)) {
				types.add(type);
			}
		}
		return types;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#findAllRelationshipTypesInBackEnd()
	 */
	protected Vector findRelationshipTypesInBackEnd() {
		Vector types = new Vector();
		for (Iterator iter = backEndRels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			String type = rel.getType();
			if (!types.contains(type)) {
				types.add(type);
			}
		}
		return types;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.DataBean#clearBufferedData()
	 */
	public void clearBufferedData() {
		if (dataIsDirty()) {
			//artToIncomingRelsBuffer.clear();
			//artToOutgoingRelsBuffer.clear();
			super.clearBufferedData();
		}
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractDataBean#addArtifactToBackEnd(ca.uvic.csr.shrimp.DataBean.Artifact)
	 */
	protected boolean addArtifactToBackEnd(Artifact artifact) {
		boolean added = false;
		if (!backEndArts.contains(artifact)) {
			added = backEndArts.add(artifact);
		}
		if (added) {
			idToArtifactMapBuffer.put(artifact.getExternalId(), artifact);
		}
		return added;
	}

}
