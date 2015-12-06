/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * GenericNode is a temporary data structure for storing node information.
 * The nodeLabel, nodeID, and nodeType are the three mandatory attributes.
 * All other attributes are optional and are customized by users.
 * Assume that a node can have multiple children but only one parent
 *
 * @author Anton An
 * @date August 10, 2000
 */
public class GenericRigiNode {
	private Hashtable attributeList;
	private String nodeLabel;
	private String nodeID;
	private String nodeType;
	//private String uri;
	private boolean isDefaultCprelRoot;

	public GenericRigiNode() {
		attributeList = new Hashtable();
		nodeType = "unknown";
		isDefaultCprelRoot = false;
	}

	public String getNodeLabel() {
		return nodeLabel;
	}

	public void setNodeLabel(String str) {
		nodeLabel = str;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String str) {
		nodeID = str;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String str) {
		nodeType = str;
	}

	//public String getURI() {
		//return uri;
	//}

	//public void setURI(String uri) {
		//this.uri = uri;
	//}

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
		if (data != null) {
			attributeList.put(key, data);
		} else {
			attributeList.remove(key);
		}
	}

	/**
	 * Returns all of the names for the Custom Data
	 */
	public Vector getCustomizedDataKeys() {
		Vector names = new Vector();
		Enumeration keys = attributeList.keys();
		while (keys.hasMoreElements()) {
			names.addElement(keys.nextElement());
		}
		return names;
	}

	/**
	 * @return True if this node should be considered as a root of the default hierarchy
	 */
	public boolean isDefaultCprelRoot() {
		return isDefaultCprelRoot;
	}

	/**
	 * @param b
	 */
	public void setDefaultCprelRoot(boolean b) {
		isDefaultCprelRoot = b;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "GenericRigiNode: [" + getNodeID() + "] " + getNodeLabel();
    }

}
