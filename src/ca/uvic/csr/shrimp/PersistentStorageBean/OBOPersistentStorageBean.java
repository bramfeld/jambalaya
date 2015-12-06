/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.geneontology.dataadapter.DataAdapterException;
import org.geneontology.dataadapter.FileAdapterConfiguration;
import org.geneontology.dataadapter.IOOperation;
import org.geneontology.oboedit.dataadapter.OBOFileAdapter;
import org.geneontology.oboedit.datamodel.LinkedObject;
import org.geneontology.oboedit.datamodel.OBORestriction;
import org.geneontology.oboedit.datamodel.OBOSession;

import ca.uvic.csr.shrimp.DataBean.OBODataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiArc;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiNode;


/**
 * Bean capable of reading OBO files.  It creates {@link GenericRigiArc} and {@link GenericRigiNode}
 * for each term and relationship and then passes them to the {@link OBODataBean}. 
 * Saving OBO files is not supported.
 * 
 * @author Chris Callendar
 */
public class OBOPersistentStorageBean implements PersistentStorageBean {

	private static final String OBO = ".obo";

	private Vector psbListeners = null;
	
	private Hashtable nodes;
	private Hashtable arcs;
	private long id;
	
	private Hashtable arcsToTypes;
	
	public OBOPersistentStorageBean() {
		psbListeners = new Vector();
		nodes = new Hashtable();
		arcs = new Hashtable();
		arcsToTypes = new Hashtable();
		id = 100;
	}
    
 	/** 
	 * fire the event adding the nodes and arcs
	 * @param rigiNodes The nodes to be added
	 * @param rigiArcs The relationships to be added
	 */
	protected void fireDataLoadedEvent(Hashtable rigiNodes, Hashtable rigiArcs) {
		RSFDataLoadedEvent dataLoadedEvent = new RSFDataLoadedEvent(rigiNodes, rigiArcs);
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

	public void loadData(URI uri) {
		String str = uri.toString();
		if (str.toLowerCase().endsWith(OBO)) {
			OBOSession oboSession = loadOBOFile(str);
			if (oboSession == null) {
				throw new Error("Couldn't read the OBO file.");
			}
			loadData(oboSession);
		}
	}
	
	public void loadData(OBOSession oboSession) {
		Set roots = oboSession.getRoots();
		loadTerms(roots);
		fireDataLoadedEvent(nodes, arcs);
	}

	/** Recursively load the terms into the tree. */
	private void loadTerms(Set terms) {
		for (Iterator iter = terms.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof LinkedObject) {
				LinkedObject term = (LinkedObject) obj;
				if (term.getName().toLowerCase().startsWith("obo:")) {
					continue;
				}
				// check for duplicate nodes
				if (!nodes.containsKey(term.getID())) {
					createNode(term);
					// load the children
					loadTerms(term.getChildren());
				} else {
					//System.err.println("Already contains node "+term.getName());
				}
			} else if (obj instanceof OBORestriction) {
				OBORestriction rest = (OBORestriction) obj;
				createArc(rest);
				
				LinkedObject term = rest.getChild();
				if (!nodes.containsKey(term.getID())) {
					createNode(term);
					// load the child
					loadTerms(term.getChildren());
				} else {
					//System.out.println("Already contains node " +term.getName());
				}
			} else {
				//System.out.println("Other: " + obj.getClass() + " " + obj.toString());
			}
		}		
	}
	
	private GenericRigiArc createArc(OBORestriction rest) {
		GenericRigiArc arc = new GenericRigiArc();
		arc.setArcID(String.valueOf(id++));
		arc.setArcLabel(rest.toString());
		String id = rest.getType().getID();
		if (id.endsWith(OBODataBean.IS_A_REL_TYPE)) {
			arc.setArcType(OBODataBean.IS_A_REL_TYPE);	
		} else if (OBODataBean.PART_OF_REL_TYPE.equals(id)) {
			arc.setArcType(OBODataBean.PART_OF_REL_TYPE);
		} else if (OBODataBean.DEVELOPS_FROM_REL_TYPE.equals(id)) {
			arc.setArcType(OBODataBean.DEVELOPS_FROM_REL_TYPE);
		} else {
			arc.setArcType(id);
		}		
		
		LinkedObject child = rest.getChild();
		String destID = child.getID();
		arc.setDestID(destID);
		arc.setDestNodeLabel(child.getName());
		LinkedObject parent = rest.getParent();
		String srcID = parent.getID();
		arc.setSourceID(srcID);

		// check if this arc (direction and arc type) already exists
		// TODO why am I getting duplicate arcs??
		String key = srcID + " -> " + destID;
		boolean exists = arcsToTypes.containsKey(key) && arc.getArcType().equals(arcsToTypes.get(key));
		if (!exists) {
			arcsToTypes.put(key, arc.getArcType());
			//System.out.println("Created an arc from parent " + parent + "  --->  " + child);
			arcs.put(arc.getArcID(), arc);
			return arc;
		}
		return null;
	}

	private GenericRigiNode createNode(LinkedObject term) {
		GenericRigiNode node = new GenericRigiNode();
		node.setNodeID(term.getID());
		node.setNodeLabel(term.getName());
		node.setNodeType(term.getType().getID());
		//System.out.println("Created a node " + node);
		nodes.put(node.getNodeID(), node);
		return node;
	}
	
	
	/**
	 * Reads an obo file and returns the OBOSession object or null.
	 * @param oboFile
	 * @return OBOSession or null if an error occurred.
	 */
	public static OBOSession loadOBOFile(String oboFile) {
		OBOSession os = null;
		OBOFileAdapter fa = new OBOFileAdapter();
		FileAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
		Collection fileList = new ArrayList();
		fileList.add(oboFile);
		cfg.setReadPaths(fileList);
		//System.out.println("cfg: " + cfg);
		try {
			os = (OBOSession)fa.doOperation(IOOperation.READ, cfg, null);
		} catch (DataAdapterException e) {
			System.out.println("Data adapter exception: " + e);
		}
		return os;
	}	
	
	public void saveData(String filename, Vector artifacts, Vector relationships) {
		throw new Error("OBOPersistentStorage.saveData() is not implemented yet.");
	}

}
