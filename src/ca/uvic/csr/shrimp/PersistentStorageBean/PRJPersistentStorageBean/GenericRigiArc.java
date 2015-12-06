/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * GenericArc is a temporary data structure for storing arc information. 
 * The arcLabel, arcID, sourceID, destID, and arcType are the mandatory fields.
 * All other attributes are optional and are customized by users.
 * 
 * @author Anton An
 * @date August 11, 2000
 */
public class GenericRigiArc {
	
	private Hashtable attributeList;
	private String arcLabel = null;
	private String arcID = null;
	private String sourceID = null;
	private String destID = null;
	private String destNodeLabel = null;
	private String arcType = null;
	private boolean directed = true;

	public GenericRigiArc() {
		attributeList = new Hashtable();
		arcType = "unknown";
	}

	public String getArcLabel() {
		return arcLabel;
	}

	public void setArcLabel(String str) {
		arcLabel = str;
	}

	public String getArcID() {
		return arcID;
	}

	public void setArcID(String str) {
		arcID = str;
	}

	public String getArcType() {
		return arcType;
	}

	public void setArcType(String str) {
		arcType = str;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String str) {
		sourceID = str;
	}

	public String getDestID() {
		return destID;
	}

	public void setDestID(String str) {
		destID = str;
	}

	public String getDestNodeLabel() {
		return destNodeLabel;
	}

	public void setDestNodeLabel(String str) {
		destNodeLabel = str;
	}

	/**
	 * Returns a piece of custom information for a data model.
	 */
	public Object getCustomizedData(String key) {
		return attributeList.get(key);
	}

	/**
	 * Sets a piece of custom information for a data model.
	 *
	 * @param key The string name of the data
	 * @param data The data to be stored
	 */
	public void setCustomizedData(String key, Object data) {
		if (data != null)
			attributeList.put(key, data);
		else
			attributeList.remove(key);
	}

	/**
	 * Returns all of the names for the Custom Data
	 */
	public Vector getCustomizedDataKeys() {
		Vector names = new Vector();
		Enumeration keys = attributeList.keys();
		while (keys.hasMoreElements())
			names.addElement(keys.nextElement());
		return names;
	}
	
	/**
	 * @return True if this arc is directed
	 */
	public boolean isDirected() {
		return directed;
	}

	/**
	 * @param b
	 */
	public void setDirected(boolean b) {
		directed = b;
	}

	public String toString() {
		return "GenericRigiArc: [" + getArcID() + "] " + getArcLabel() + " (" + getSourceID() + (directed ? " -> " : " - ") + getDestID() + ")";
	}
}
