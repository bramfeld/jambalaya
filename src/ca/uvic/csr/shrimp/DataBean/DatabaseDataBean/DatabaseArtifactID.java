/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.DatabaseDataBean;


/**
 * An object that uniquely identifies an "artifact" in the database.
 * Perhaps a table name and a primary key?
 *
 * @author Rob Lintern
 */
public class DatabaseArtifactID {

	private String uniqueID = "";
	private String artifactType = "";
	private String primaryKey = "";

	public DatabaseArtifactID(String uniqueID) {
		int index = uniqueID.indexOf("|");
		this.uniqueID = uniqueID;
    	this.artifactType = uniqueID.substring(0,index-1);
    	this.primaryKey = uniqueID.substring(index+1);
    }

	public DatabaseArtifactID(String uniqueID, String artifactType, String primaryKey) {
    	this.uniqueID = uniqueID;
    	this.artifactType = artifactType;
    	this.primaryKey = primaryKey;
    }

	/**
	 * @return Returns the artifaceType.
	 */
	public String getArtifactType() {
		return artifactType;
	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey() {
		return primaryKey;
	}
	/**
	 * @return Returns the uniqueID.
	 */
	public String getUniqueID() {
		return uniqueID;
	}

	public String toString() {
		String s = uniqueID;
		return s;
	}

	public boolean equals (Object obj) {
		boolean equal = false;
		if (obj instanceof DatabaseArtifactID) {
			DatabaseArtifactID that = (DatabaseArtifactID)obj;
			equal = this.getUniqueID().equals(that.getUniqueID());
			if (!equal && that.hashCode() == this.hashCode()) {
				System.err.println("\nobjects NOT EQUAL and hash codes same");
				System.err.println("this.getArtifactType(): " + this.getArtifactType());
				System.err.println("that.getArtifactType(): " + that.getArtifactType());
				System.err.println("this.hashCode(): " + this.hashCode());
				System.err.println("that.hashCode(): " + that.hashCode());
				//(new Exception ("not equal and hash codes same")).printStackTrace();
			} else if (equal && that.hashCode() != this.hashCode()) {
				System.err.println("\nobjects EQUAL and hash codes different");
				System.err.println("this.getArtifactType(): " + this.getArtifactType());
				System.err.println("that.getArtifactType(): " + that.getArtifactType());
				System.err.println("this.hashCode(): " + this.hashCode());
				System.err.println("that.hashCode(): " + that.hashCode());
			}
		}
		return equal;
	}

	public int hashCode(){
		return uniqueID.hashCode();
	}

}
