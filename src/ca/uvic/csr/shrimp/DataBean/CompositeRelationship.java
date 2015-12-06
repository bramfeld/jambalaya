/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

/**
 * @author Nasir Rather
 * @date Jan 16, 2003
 */
public class CompositeRelationship {

	private long[] srcAncestorIDs;
	private long[] destAncestorIDs;
	private String compositeType;
	private Relationship rel;

	/**
	 *
	 * @param compositeType
	 * @param srcAncestorIDs
	 * @param destAncestorIDs
	 * @param rel
	 */
	public CompositeRelationship(String compositeType, long[] srcAncestorIDs, long[] destAncestorIDs, Relationship rel) {
		this.compositeType = compositeType;
		this.srcAncestorIDs = srcAncestorIDs;
		this.destAncestorIDs = destAncestorIDs;
		this.rel = rel;
	}

	/**
	 * @return The id of the destination artifact of this composite relationship.
	 */
	public long getDestID() {
		return ((Artifact) rel.getArtifacts().elementAt(1)).getID();
	}

	public String getDestName() {
		return ((Artifact) rel.getArtifacts().elementAt(1)).getName();
	}

	/**
	 * @return The id of the source artifact of this composite relationship.
	 */
	public long getSrcID() {
		return ((Artifact) rel.getArtifacts().elementAt(0)).getID();
	}

	public String getSrcName() {
		return ((Artifact) rel.getArtifacts().elementAt(0)).getName();
	}

	/**
	 * @return The type of this composite relationship.
	 */
	public String getCompositeType() {
		return compositeType;
	}

	/**
	 * @return The ancestors of the destination of this composite relationship.
	 */
	public long[] getDestAncestorIDs() {
		return destAncestorIDs;
	}

	/**
	 * @return The ancestors of the source of this composite relationship.
	 */
	public long[] getSrcAncestorIDs() {
		return srcAncestorIDs;
	}

	/**
	 * Sets the type of this composite relationship.
	 * @param compositeType
	 */
	public void setCompositeType(String compositeType) {
		this.compositeType = compositeType;
	}

	public Relationship getRelationship() {
		return rel;
	}

	public String toString() {
		return "CompositeRelationship {" + getSrcName() + " [" + getSrcID() + "] " +
			compositeType + " " + getDestName() + " [" + getDestID() + "]}";
	}

}