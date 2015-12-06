/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import net.sourceforge.gxl.GXLAtomicValue;
import net.sourceforge.gxl.GXLAttr;
import net.sourceforge.gxl.GXLAttributedElement;
import net.sourceforge.gxl.GXLBag;
import net.sourceforge.gxl.GXLBool;
import net.sourceforge.gxl.GXLCompositeValue;
import net.sourceforge.gxl.GXLDocument;
import net.sourceforge.gxl.GXLEdge;
import net.sourceforge.gxl.GXLEnum;
import net.sourceforge.gxl.GXLFloat;
import net.sourceforge.gxl.GXLGXL;
import net.sourceforge.gxl.GXLGraph;
import net.sourceforge.gxl.GXLGraphElement;
import net.sourceforge.gxl.GXLInt;
import net.sourceforge.gxl.GXLLocator;
import net.sourceforge.gxl.GXLNode;
import net.sourceforge.gxl.GXLSeq;
import net.sourceforge.gxl.GXLSet;
import net.sourceforge.gxl.GXLString;
import net.sourceforge.gxl.GXLTup;
import net.sourceforge.gxl.GXLType;
import net.sourceforge.gxl.GXLTypedElement;
import net.sourceforge.gxl.GXLValue;
import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiArc;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * @author Rob Lintern, Chris Callendar
 */
public class GXLPersistentStorageBean implements PersistentStorageBean {

	private static final String CONTAINS_NODE = "containsNode";
	private static final String CONTAINS_GRAPH = "containsGraph";
	
	private Vector psbListeners;
	private Hashtable nodes;
	private Hashtable arcs;
	int nextID = 1;

	// contains GenericRigiArc objects that will be added AFTER the entire graph has been loaded
	// these arcs require a generated id and we want to ensure that the id will be unique
	// and won't be the same as an arc id defined in the GXL file
	private Vector delayedArcsToAdd;

	public GXLPersistentStorageBean() {
		psbListeners = new Vector();
		delayedArcsToAdd = new Vector();
	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#loadData(URI)
	 */
	public void loadData(URI uri) {
		nodes = new Hashtable();
		arcs = new Hashtable();
		try {
			URL url = uri.toURL();
			GXLDocument gxlDoc = new GXLDocument(url);
			nextID = 1;
			convertGXLDocToRSF(gxlDoc);
			fireDataLoadedEvent();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = "Sorry, there was a problem reading the GXL file.\nThe error message is '" + e.getMessage() + "'";
			JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), msg,
					ApplicationAccessor.getAppName(), JOptionPane.ERROR_MESSAGE);
			nodes = new Hashtable();
			arcs = new Hashtable();
			fireDataLoadedEvent();
		}
	}

	/**
	 * @return the next available unused id
	 */
	private String getNextID() {
		return "" + nextID++;
	}

	private void convertGXLDocToRSF(GXLDocument gxlDoc) {
		GXLGXL gxlGXL = gxlDoc.getDocumentElement();
		int graphCount = gxlGXL.getGraphCount();
		for (int i = 0; i < graphCount; i++) {
			GXLGraph gxlGraph = gxlGXL.getGraphAt(i);
			handleGXLGraph(gxlGraph, null /* no parent node */);
		}
	}

	/**
	 * Process a glxGraph element, converting this to a Rigi Node and processing its child elements
	 * @param graph the GXL graph to process
	 * @param parentNode the parent node of this graph
	 */
	private GenericRigiNode handleGXLGraph(GXLGraph gxlGraph, GenericRigiNode parentNode) {
		boolean allowsHyperGraphs = gxlGraph.getAllowsHyperGraphs();
		if (allowsHyperGraphs) {
			System.err.println("Warning - Shrimp cannot handle hypergraphs.");
		}
		String role = gxlGraph.getRole();

		// Determine if we should display the graph or skip and just display its
		// child elements directly
		GenericRigiNode rigiNode = null;
		boolean display = true; // default is to display the graph element as a node
		String displayStr = getAttributedElementValue(gxlGraph, AttributeConstants.NOM_ATTR_DISPLAY);
		if (displayStr != null) {
			display = !"false".equalsIgnoreCase(displayStr);
		}

		if (display || parentNode == null) { // must display if there is no parentNode (root graph)
			// turn this graph element into a
			rigiNode = (GenericRigiNode) nodes.get(gxlGraph.getID());
			if (rigiNode == null) {
				rigiNode = new GenericRigiNode();
				rigiNode.setNodeID(gxlGraph.getID());

				// we'll set this to something better below if we come across a "name" or "label" attribute
				rigiNode.setNodeLabel(gxlGraph.getID());
				Map attrs = handleAttributedElement(gxlGraph);
				for (Iterator iter = attrs.keySet().iterator(); iter.hasNext();) {
					String attrName = (String) iter.next();
					rigiNode.setCustomizedData(attrName, attrs.get(attrName));
					if ("name".equalsIgnoreCase(attrName) || "label".equalsIgnoreCase(attrName) ||
							AttributeConstants.NOM_ATTR_ARTIFACT_NAME.equals(attrName)) {
						rigiNode.setNodeLabel(attrs.get(attrName).toString());
					}
				}
				String type = handleTypedElement(gxlGraph);
				if ("unknown".equals(type)) {
					type = "unknown graph type";
				}
				rigiNode.setNodeType(type);
				if (role != null && !role.equals("")) {
					rigiNode.setCustomizedData("role", role);
				}
				nodes.put(rigiNode.getNodeID(), rigiNode);
			}
		} else { // use the parent instead, if not displaying the graph
			rigiNode = parentNode;
		}

		for (int i = 0; i < gxlGraph.getGraphElementCount(); i++) {
			GXLGraphElement graphElement = gxlGraph.getGraphElementAt(i);
			if (graphElement instanceof GXLNode) {
				GXLNode childGXLNode = (GXLNode) graphElement;
				GenericRigiNode childRigiNode = handleGXLNode(childGXLNode);
				
				// add a "containsNode" arc between this graph element and its child node elements
				GenericRigiArc levelArc = new GenericRigiArc();
				levelArc.setArcType(CONTAINS_NODE);
				levelArc.setSourceID(rigiNode.getNodeID());
				levelArc.setDestID(childRigiNode.getNodeID());
				// this arc will be added later (and given a unique id)
				delayedArcsToAdd.add(levelArc);
			} else if (graphElement instanceof GXLEdge) {
				handleGXLEdge((GXLEdge) graphElement);
			} else {
				System.err.println("Warning: can't handle graph element: " + graphElement);
			}
		}
		
		// now add all the "containsNode" and "containsGraph" arcs
		// this is done last to ensure that the generated id is unique
		for (Iterator iter = delayedArcsToAdd.iterator(); iter.hasNext(); ) {
			GenericRigiArc arc = (GenericRigiArc) iter.next();
			// get a unique ID
			String id = getNextID();
			while (arcs.containsKey(id)) {
				//System.out.println(id + " is already in use!");
				id = getNextID();
			}
			arc.setArcID(id);
			arcs.put(id, arc);
		}
		
		return rigiNode;
	}

	/**
	 * Creates a {@link GenericRigiArc} from the {@link GXLEdge}.
	 * @param gxlEdge
	 */
	private GenericRigiArc handleGXLEdge(GXLEdge gxlEdge) {
		GenericRigiArc rigiArc = null;
		String gxlEdgeID = gxlEdge.getID();
		if (gxlEdgeID == null || gxlEdgeID.equals("")) {
			gxlEdgeID = getNextID();
		} else {
			rigiArc = (GenericRigiArc) arcs.get(gxlEdgeID);
		}
		if (rigiArc == null) {
			rigiArc = new GenericRigiArc();
			rigiArc.setArcID(gxlEdgeID);
			rigiArc.setSourceID(gxlEdge.getSourceID());
			rigiArc.setDestID(gxlEdge.getTargetID());
			rigiArc.setDirected(gxlEdge.isDirected());
			if (!gxlEdge.isDirected()) {
				System.err.println("Warning: cant properly handle undirected edges: " + gxlEdge);
			}
			Map attrs = handleAttributedElement(gxlEdge);
			for (Iterator iter = attrs.keySet().iterator(); iter.hasNext();) {
				String attrName = (String) iter.next();
				rigiArc.setCustomizedData(attrName, attrs.get(attrName));
			}
			String type = handleTypedElement(gxlEdge);
			if ("unknown".equals(type)) {
				type = "unknown edge type";
			}
			rigiArc.setArcType(type);
			arcs.put(gxlEdgeID, rigiArc);
		} else {
			System.err.println("Warning - GXL edge with id " + gxlEdgeID + " already exists!");
		}
		if (gxlEdge.getGraphCount() > 0) {
			System.err.println("Warning. Shrimp can't properly handle a graph that is a child of an edge.");
		}
		
		return rigiArc;
	}

	/**
	 * Process a GXL node, creating a RIGI Node and processing subgraphs
	 * @param gxlNode
	 * @return a GenericRigiNode mirroring the given gxlNode
	 */
	private GenericRigiNode handleGXLNode(GXLNode gxlNode) {
		String nodeID = gxlNode.getID();
		GenericRigiNode rigiNode = (GenericRigiNode) nodes.get(nodeID);
		if (rigiNode == null) {
			rigiNode = new GenericRigiNode();
			rigiNode.setNodeID(nodeID);
			rigiNode.setNodeLabel(nodeID);
			Map attrs = handleAttributedElement(gxlNode);
			for (Iterator iter = attrs.keySet().iterator(); iter.hasNext();) {
				String attrName = (String) iter.next();
				rigiNode.setCustomizedData(attrName, attrs.get(attrName));
				if ("name".equalsIgnoreCase(attrName) || "label".equalsIgnoreCase(attrName) ||
						AttributeConstants.NOM_ATTR_ARTIFACT_NAME.equals(attrName)) {
					rigiNode.setNodeLabel(attrs.get(attrName).toString());
				}
			}
			String type = handleTypedElement(gxlNode);
			if ("unknown".equals(type)) {
				type = "unknown node type";
			}
			rigiNode.setNodeType(type);
			nodes.put(rigiNode.getNodeID(), rigiNode);
			// look to see if there are any sub graphs
			for (int j = 0; j < gxlNode.getGraphCount(); j++) {
				GXLGraph gxlSubGraph = gxlNode.getGraphAt(j);
				GenericRigiNode childRigiNode = handleGXLGraph(gxlSubGraph, rigiNode);

				if (childRigiNode == rigiNode) { // if graph is not displayed, add layout attribute to parent node
					String customLayout = getAttributedElementValue(gxlSubGraph, AttributeConstants.NOM_ATTR_LAYOUT);
					if (customLayout != null) {
						rigiNode.setCustomizedData(AttributeConstants.NOM_ATTR_LAYOUT, customLayout);
					}
				} else {
					// skip arc creation if no graph node generated
					// add a "containsGraph" arc between this node element and its child graph elements
					GenericRigiArc levelArc = new GenericRigiArc();
					levelArc.setArcType(CONTAINS_GRAPH);
					levelArc.setSourceID(rigiNode.getNodeID());
					levelArc.setDestID(childRigiNode.getNodeID());
					// this arc will be added later and given a unique id
					delayedArcsToAdd.add(levelArc);
				}
			}
		} else {
			System.err.println("Warning - GXL node with id " + nodeID + " already exists!");
		}

		return rigiNode;
	}

	/**
	 *
	 * @param elem
	 * @return A String representation, possible decoded, of the given elem
	 */
	private String handleTypedElement(GXLTypedElement elem) {
		String type = "unknown";
		GXLType gxlType = elem.getType();
		if (gxlType != null) {
			URI typeURI = gxlType.getURI();
			type = typeURI.getFragment();
			if (type == null) {
				type = typeURI.toString();
			}
			try {
				String encodedType = new String(type);
				type = URLDecoder.decode(encodedType, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return type;
	}

	/**
	 * Get the string value of an attributed element. This currently returns values only for
	 * Strings and Booleans.
	 * @param elem Element containing the attribute sub-element
	 * @param name name of the attribute element - should name a boolean or string attribute
	 * @return a String value for the attribute identified by name of the element elem. Returns null
	 * if a value cannot be found.
	 */
	private String getAttributedElementValue(GXLAttributedElement elem, String name) {
		String value = null;
		GXLAttr gxlAttr = elem.getAttr(name);
		if (gxlAttr != null) {
			GXLValue gxlValue = gxlAttr.getValue();
			if (gxlValue instanceof GXLString) {
				value = ((GXLString) gxlValue).getValue();
			} else if (gxlValue instanceof GXLBool) {
				value = ((GXLBool) gxlValue).getValue();
			}
		}
		return value;
	}

	/**
	 * @return a Map (attr name -> attr value) of the attributes of elem
	 */
	private Map handleAttributedElement(GXLAttributedElement elem) {
		Map attrs = new HashMap();
		int attrCount = elem.getAttrCount();
		for (int i = 0; i < attrCount; i++) {
			GXLAttr gxlAttr = elem.getAttrAt(i);
			String attrName = gxlAttr.getName();
			String attrKind = gxlAttr.getKind();
			if (attrKind != null) {
				System.err.println("Warning: can't properly handle gxlAttr.getKind(): " + attrKind);
			}
			GXLValue gxlValue = gxlAttr.getValue();
			Object attrValue = handleGXLValue(gxlValue);
			attrs.put(attrName, attrValue);
		}
		return attrs;
	}

	/**
	 * @return elem converted to a generic object such as Integer, String, Boolean, etc
	 */
	private Object handleGXLValue(GXLValue elem) {
		Object value = null;
		if (elem instanceof GXLAtomicValue) {
			if (elem instanceof GXLBool) {
				value = new Boolean(((GXLBool) elem).getBooleanValue());
			} else if (elem instanceof GXLEnum) {
				value = ((GXLEnum) elem).getValue();
				//System.err.println(indent + "Warning: may not handle GXLEnum nicely: " + elem);
			} else if (elem instanceof GXLFloat) {
				value = new Float(((GXLFloat) elem).getFloatValue());
			} else if (elem instanceof GXLInt) {
				value = new Integer(((GXLInt) elem).getIntValue());
			} else if (elem instanceof GXLString) {
				value = ((GXLString) elem).getValue();
			}
		} else if (elem instanceof GXLCompositeValue) {
			List values = handleGXLCompositeValue((GXLCompositeValue) elem);
			if (elem instanceof GXLBag) {
				value = values;
				System.err.println("Warning: may not handle GXLBag nicely: " + elem);
			} else if (elem instanceof GXLSeq) {
				value = values;
			} else if (elem instanceof GXLSet) {
				value = new HashSet(values);
			} else if (elem instanceof GXLTup) {
				System.err.println("Warning: may not handle GXLTup nicely: " + elem);
				value = values;
			}
		} else if (elem instanceof GXLLocator) {
			URI uri = ((GXLLocator) elem).getURI();
			value = uri;
		}
		return value;
	}

	private List handleGXLCompositeValue(GXLCompositeValue composite) {
		List values = new ArrayList(composite.getValueCount());
		for (int i = 0; i < composite.getValueCount(); i++) {
			GXLValue value = composite.getValueAt(i);
			Object valueObj = handleGXLValue(value);
			values.add(valueObj);
		}
		return values;

	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#saveData(java.lang.String, java.util.Vector, java.util.Vector)
	 */
	public void saveData(String filename, Vector artifacts, Vector relationships) {
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error! Could not create file \"" + filename + ".\"  GXL not saved!");
				return;
			}
		}
		GXLDocument gxlDocument = new GXLDocument();
		GXLGXL gxlgxl = gxlDocument.getDocumentElement();
		GXLGraph gxlGraph = new GXLGraph(filename);
		gxlgxl.add(gxlGraph);
		long nextID = 1;
		Map artToIDMap = new HashMap();
		for (Iterator iter = artifacts.iterator(); iter.hasNext();) {
			Artifact artifact = (Artifact) iter.next();
			String id = "" + nextID++;
			GXLNode gxlNode = new GXLNode(id); //artifact.getID().toString());
			artToIDMap.put(artifact, id);
			URI typeURI = createTypeURI(artifact.getType());
			gxlNode.setType(typeURI);
			// TODO need a way for the user to specify which attributes to save
			if (artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_NAME) != null) {
				Object attrVal = artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_NAME);
				gxlNode.add(createGXLAttr("name", attrVal));
			}
			if (artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME) != null) {
				Object attrVal = artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME);
				gxlNode.add(createGXLAttr("qualified_name", attrVal));
			}
			if (artifact.getAttribute(JavaDomainConstants.ATTR_LINES_OF_CODE) != null) {
				Object attrVal = artifact.getAttribute(JavaDomainConstants.ATTR_LINES_OF_CODE);
				gxlNode.add(createGXLAttr(JavaDomainConstants.ATTR_LINES_OF_CODE, attrVal));
			}
			if (artifact.getAttribute(SoftwareDomainConstants.PATH) != null) {
				Object attrVal = artifact.getAttribute(SoftwareDomainConstants.PATH);
				gxlNode.add(createGXLAttr(SoftwareDomainConstants.PATH, attrVal));
			}

			//temporary
			if (artifact.getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE) != null) {
				Object attrVal = artifact.getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE);
				gxlNode.add(createGXLAttr(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE, attrVal));
			}

			for (int i = 0; i < JavaDomainConstants.MODIFIERS.length; i++) {
				String modifier = JavaDomainConstants.MODIFIERS[i];
				Object attrVal = artifact.getAttribute(modifier);
				if (attrVal != null && attrVal.equals(Boolean.TRUE)) {
					gxlNode.add(createGXLAttr(modifier, attrVal));
				}
			}
			gxlGraph.add(gxlNode);
		}
		for (Iterator iter = relationships.iterator(); iter.hasNext();) {
			Relationship relationship = (Relationship) iter.next();
			Vector arts = relationship.getArtifacts();
			Artifact srcArt = (Artifact) arts.elementAt(0);
			Artifact destArt = (Artifact) arts.elementAt(1);
			if (!artifacts.contains(srcArt)) {
				System.err.println("Warning!: Source artifact \"" + srcArt.getName() +
								"\" not in artifacts list! For relationship \"" + relationship + ".\"");
				continue;
			}
			if (!artifacts.contains(destArt)) {
				System.err.println("Warning!: Destination artifact \"" + destArt.getName() +
								"\" not in artifacts list! For relationship \"" + relationship + ".\"");
				continue;
			}
			GXLEdge gxlEdge = new GXLEdge(artToIDMap.get(srcArt).toString(), artToIDMap.get(destArt).toString());
			String id = "" + nextID++;
			gxlEdge.setID(id);
			URI typeURI = createTypeURI(relationship.getType());
			gxlEdge.setType(typeURI);
			gxlGraph.add(gxlEdge);
		}
		if (gxlDocument.getDanglingTentacleCount() > 0) {
			System.err.println("Error! GXL document has dangling tentacles. GXL not saved!");
		} else {
			if (file.exists()) {
				try {
					gxlDocument.write(file);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Error! There was a problem writing GXL to file. GXL not saved!");
				}
			}
		}
	}

	/**
	 * @param type
	 * @param typeURI
	 */
	private URI createTypeURI(String type) {
		URI typeURI;
		try {
			String unencodedType = new String(type);
			String encodedType = URLEncoder.encode(unencodedType, "UTF-8");
			typeURI = new URI(encodedType);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			typeURI = URI.create("unknown");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			typeURI = URI.create("unknown");
		}
		return typeURI;
	}

	/**
	 * @param attrName
	 * @param attrVal
	 */
	private GXLAttr createGXLAttr(String attrName, Object attrVal) {
		GXLValue gxlValue;
		if (attrVal instanceof Boolean) {
			gxlValue = new GXLBool(((Boolean) attrVal).booleanValue());
		} else if (attrVal instanceof Number) {
			if (attrVal instanceof Integer || attrVal instanceof Long || attrVal instanceof Short || attrVal instanceof Byte) {
				gxlValue = new GXLInt(((Number) attrVal).intValue());
			} else {
				gxlValue = new GXLFloat(((Number) attrVal).floatValue());
			}
		} else {
			gxlValue = new GXLString(attrVal.toString());
		}
		GXLAttr gxlAttr = new GXLAttr(attrName, gxlValue);
		return gxlAttr;
	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#addPersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener)
	 */
	public void addPersistentStorageBeanListener(PersistentStorageBeanListener listener) {
		if (!psbListeners.contains(listener)) {
			psbListeners.add(listener);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#removePersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener)
	 */
	public void removePersistentStorageBeanListener(PersistentStorageBeanListener listener) {
		psbListeners.remove(listener);
	}

	/**
	 * fire the event adding the Artifacts and Relationships
	 * @param artifacts The artifacts to be added
	 * @param relationships The relationships to be added
	 */
	protected void fireDataLoadedEvent() {
		RSFDataLoadedEvent dataLoadedEvent = new RSFDataLoadedEvent(nodes, arcs);
		for (int i = 0; i < ((Vector) psbListeners.clone()).size(); i++) {
			((PersistentStorageBeanListener) psbListeners.elementAt(i)).dataLoaded(dataLoadedEvent);
		}
	}

}
