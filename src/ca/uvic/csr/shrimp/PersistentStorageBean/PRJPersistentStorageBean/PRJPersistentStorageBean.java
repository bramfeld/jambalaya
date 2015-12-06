/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Hashtable;
import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener;
import ca.uvic.csr.shrimp.PersistentStorageBean.RSFDataLoadedEvent;

/**
 * PRJPersistentStorageBean: This is a storage system for Rigi domain data.
 * It is responsible for extracting information from RSF files,
 * and possibly writing to RSF files.
 * 
 * @author Anton An, Derek Rayside
 */

public class PRJPersistentStorageBean implements Serializable, PersistentStorageBean {

	private static final String PRJ = ".prj";

	private Hashtable artifacts = new Hashtable();

	private Hashtable relationships = new Hashtable();

	private Vector psbListeners = new Vector();

	/* PRJ File for the project */
	private PRJFile prjFile;

	public PRJPersistentStorageBean() {
	}

	/**
	 * load data from a file or a database table 
	 * @param uri The location of the "prj" data
	 */
	public void loadData(URI uri) {
		if (uri.toString().endsWith(PRJ)) {
			prjFile = new PRJFile(uri);
			final RSF rsf = prjFile.getRSFStyle();
			rsf.setStorageBean(this);
			try {
				rsf.extract();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 
	 * fire the event adding the Artifacts and Relationships
	 * @param artifacts The artifacts to be added
	 * @param relationships The relationships to be added
	 */
	protected void fireDataLoadedEvent(Hashtable artifacts, Hashtable relationships) {
		RSFDataLoadedEvent dataLoadedEvent = new RSFDataLoadedEvent(artifacts, relationships);
		for (int i = 0; i < ((Vector)psbListeners.clone()).size(); i++) {
			((PersistentStorageBeanListener) psbListeners.elementAt(i)).dataLoaded(dataLoadedEvent);
		}
	}

	/** 
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#addPersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener)
	 */
	public void addPersistentStorageBeanListener(PersistentStorageBeanListener listener) {
		psbListeners.addElement(listener);
	}

	/** 
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#removePersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener)
	 */
	public void removePersistentStorageBeanListener(PersistentStorageBeanListener listener) {
		psbListeners.removeElement(listener);
	}

	public URI getSourceDirectory() {
		return prjFile.getSourceDirectory();
	}

	public URI getDocDirectory() {
		return prjFile.getDocDirectory();
	}

	public URI getUmlDirectory() {
		return prjFile.getUmlDirectory();
	}

	public URI getCodeDirectory() {
		return prjFile.getCodeDirectory();
	}

	public void setArtifact(GenericRigiNode node, Artifact art) {
		artifacts.put(node, art);
	}

	public Artifact getArtifact(GenericRigiNode node) {
		return (Artifact) artifacts.get(node);
	}

	public void setRelationship(GenericRigiArc arc, Relationship art) {
		relationships.put(arc, art);
	}

	public Relationship getRelationship(GenericRigiArc arc) {
		return (Relationship) relationships.get(arc);
	}

	public Hashtable getArtifacts() {
		return artifacts;
	}

	/** 
	 * Write stuff out in Structured RSF.
	 * @param filename the name of the file or the database table to store the data
	 * @param artifacts the collection of artifacts
	 * @param relationships the collection of relationships
	 */
	public void saveData(String filename, Vector artifacts, Vector relationships) {
	    try {
	        File file = new File (filename);
            URI uri = file.toURI();
            PRJFile prjFile = new PRJFile(uri);
            StructuredWriter.saveData(filename, artifacts, relationships, prjFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
