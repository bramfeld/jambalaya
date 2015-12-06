/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import ca.uvic.csr.shrimp.DataBean.SimpleDataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiArc;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiNode;

/**
 *
 */
public class XMIPersistentStorageBean implements PersistentStorageBean{

    private Vector psbListeners;

	private long id;

	Hashtable xmlNodeToRigiNode = new Hashtable();
	Hashtable xmiIdToRigiNode = new Hashtable();
	GenericRigiNode rootRigiNode = null;
	Map pathToRigiNode;

	Hashtable rigiNodesTable = new Hashtable();
	Hashtable rigiArcsTable = new Hashtable();
	Hashtable dataTypes = new Hashtable();

	/** Canonical output. */
	protected boolean canonical;

    public XMIPersistentStorageBean() {
        psbListeners = new Vector();
		id = 100;
		canonical = true;
    }

 	/**
	 * fire the event adding the Artifacts and Relationships
	 * @param artifacts The artifacts to be added
	 * @param relationships The relationships to be added
	 */
	protected void fireDataLoadedEvent(Hashtable rigiNodes, Hashtable rigiArcs) {
		RSFDataLoadedEvent dataLoadedEvent = new RSFDataLoadedEvent(rigiNodes, rigiArcs);
		for (int i = 0; i < ((Vector)psbListeners.clone()).size(); i++) {
			((PersistentStorageBeanListener) psbListeners.elementAt(i)).dataLoaded(dataLoadedEvent);
		}
	}

    public void addPersistentStorageBeanListener(PersistentStorageBeanListener listener) {
        psbListeners.addElement(listener);
    }

    public void removePersistentStorageBeanListener(PersistentStorageBeanListener listener) {
        psbListeners.removeElement(listener);
    }

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#loadData(URI)
	 */
	public void loadData(URI uri) {
		xmlNodeToRigiNode = new Hashtable();
		rootRigiNode = null;

		rigiNodesTable = new Hashtable();
		rigiArcsTable = new Hashtable();
		pathToRigiNode = new HashMap();

		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(uri.toString(), new XMINodeHandler(uri.toString()));

			replaceTypeIdsWithTypeNames();
			updateOperationSignatures();

			parser.parse(uri.toString(), new XMIArcHandler());
			fireDataLoadedEvent(rigiNodesTable, rigiArcsTable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Returns a sorted list of attributes. */
	protected Attr[] sortAttributes(NamedNodeMap attrs) {
		int len = ((attrs != null) ? attrs.getLength() : 0);
		Attr array[] = new Attr[len];
		for (int i = 0; i < len; i++) {
			array[i] = (Attr) attrs.item(i);
		}
		for (int i = 0; i < len - 1; i++) {
			String name = array[i].getNodeName();
			int index = i;
			for (int j = i + 1; j < len; j++) {
				String curName = array[j].getNodeName();
				if (curName.compareTo(name) < 0) {
					name = curName;
					index = j;
				}
			}
			if (index != i) {
				Attr temp = array[i];
				array[i] = array[index];
				array[index] = temp;
			}
		}

		return (array);

	}

	/**
	 * Creates a generic rigi arc between a parent rigi node and a child rigi node
	 */
	private void createChildParentArc(GenericRigiNode parentRigiNode, GenericRigiNode rigiNode) {
		if (parentRigiNode == null) {
			return;
		}
		//System.out.println("parentRigiNode: " + parentRigiNode);
		GenericRigiArc rigiArc = new GenericRigiArc();
		rigiArc.setArcID(String.valueOf(id++));
		rigiArc.setArcLabel(SimpleDataBean.PRIMARY_DEFAULT_CPREL + "(" + parentRigiNode.getNodeLabel() + " to " + rigiNode.getNodeLabel() + ")");
		rigiArc.setArcType(SimpleDataBean.PRIMARY_DEFAULT_CPREL);
		rigiArc.setDestID(rigiNode.getNodeID());
		rigiArc.setDestNodeLabel(rigiNode.getNodeLabel());
		rigiArc.setSourceID(parentRigiNode.getNodeID());
		rigiArcsTable.put(rigiArc.getArcID(), rigiArc);
	}

	/**
	 * Creates a generic rigi arc between a two nodes
	 */
	private GenericRigiArc createArc(GenericRigiNode parentRigiNode, GenericRigiNode rigiNode, String arcType) {
		if (parentRigiNode == null) {
			return null;
		}
		//System.out.println("parentRigiNode: " + parentRigiNode);
		GenericRigiArc rigiArc = new GenericRigiArc();
		rigiArc.setArcID(String.valueOf(id++));
		rigiArc.setArcLabel(arcType + "(" + parentRigiNode.getNodeLabel() + " to " + rigiNode.getNodeLabel() + ")");
		rigiArc.setArcType(arcType);
		rigiArc.setDestID(rigiNode.getNodeID());
		rigiArc.setDestNodeLabel(rigiNode.getNodeLabel());
		rigiArc.setSourceID(parentRigiNode.getNodeID());
		rigiArcsTable.put(rigiArc.getArcID(), rigiArc);
		return rigiArc;
	}

	private void replaceTypeIdsWithTypeNames() {
		Enumeration nodes = rigiNodesTable.elements();
		while (nodes.hasMoreElements()) {
			GenericRigiNode node = (GenericRigiNode) nodes.nextElement();

			String nodeTypeId = (String) node.getCustomizedData("type");
			if (nodeTypeId != null) {
				String typeName = (String) dataTypes.get(nodeTypeId);

				if (typeName != null) {
					node.setCustomizedData("type", typeName);
				}
			}

			//label attributes with their type and visibility
			if (node.getNodeType().equalsIgnoreCase("Attribute")) {
				node.setNodeLabel(node.getCustomizedData("visibility") + " " + node.getNodeLabel() + " : " + node.getCustomizedData("type"));
			}

			//label parameters with their type
			if (node.getNodeType().equalsIgnoreCase("Parameter")) {
				node.setNodeLabel(node.getCustomizedData("type") + " " + node.getNodeLabel());
			}

			//label return types with their type
			if (node.getNodeType().equalsIgnoreCase("Return")) {
				node.setNodeLabel("return " + node.getCustomizedData("type"));
			}
		}
	}

	private void updateOperationSignatures() {
		Enumeration nodes = rigiNodesTable.elements();
		while (nodes.hasMoreElements()) {

			GenericRigiNode node = (GenericRigiNode) nodes.nextElement();

			if (node.getNodeType().equalsIgnoreCase("Operation")) {
				StringBuffer signature = new StringBuffer();
				GenericRigiNode returnNode = (GenericRigiNode) node.getCustomizedData("return");
				Vector params = (Vector) node.getCustomizedData("params");

				signature.append(node.getCustomizedData("visibility") + " ");
				signature.append(node.getNodeLabel() + "(");

				for (Iterator iter = params.iterator(); iter.hasNext();) {
					GenericRigiNode param = (GenericRigiNode) iter.next();
					signature.append(param.getNodeLabel() + ", ");
				}

				if (params.size()>0) {
					signature.delete(signature.length()-2, signature.length()+1);
				}

				signature.append(") : ");
				signature.append(returnNode.getCustomizedData("type"));

				node.setNodeLabel(signature.toString());
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#saveData(String, Vector, Vector)
	 */
	public void saveData(String filename, Vector artifacts, Vector relationships) {
	}


	private class XMINodeHandler extends DefaultHandler {
		private long numStarts = 0;
		private long numEnds = 0;
		private Map levelToRigiNode = new HashMap();
		private Vector opParams = new Vector();
		private GenericRigiNode opReturn;
		private GenericRigiNode lastOperation;


		public XMINodeHandler(String fileName) {
			super();
			//Add the root node for SHriMP
			GenericRigiNode rigiNode = new GenericRigiNode();
			rigiNode.setNodeID("1");
			rigiNode.setNodeType("Document");
			rigiNode.setNodeLabel(fileName);
			rigiNodesTable.put(rigiNode.getNodeID(), rigiNode);
			levelToRigiNode.put(new Integer(0), rigiNode);
		}

		private void setAttributes(GenericRigiNode rigiNode, Attributes atts) {
			for (int i = 0; i < atts.getLength(); i++) {
				String attTypeName = atts.getQName(i);
				String attValue = atts.getValue(i);
				if (attTypeName.equals("name")) {
					rigiNode.setNodeLabel(attValue);
				} else if (attTypeName.equals("xmi.id")) {
					xmiIdToRigiNode.put(attValue, rigiNode);
				}
				rigiNode.setCustomizedData(attTypeName, attValue);

			}
		}

		private String getAttribute(Attributes atts, String attName) {
			for (int i = 0; i < atts.getLength(); i++) {
				String attTypeName = atts.getQName(i);
				String attValue = atts.getValue(i);
				if (attTypeName.equals(attName)) {
					return attValue;
				}
			}
			return "";
		}

		public void startElement(String uri, String name, String qName, Attributes atts) {
			System.out.println("Node: " + uri + ", " + name + ", " + qName);
			for (int i = 0; i < atts.getLength(); i++) {
				System.out.println("  -> " + atts.getURI(i) + ", " + atts.getLocalName(i) + ", " + atts.getQName(i) + ", " + atts.getType(i) + ":" + atts.getValue(i));
			}
			System.out.println();

			if (name.endsWith("Class") || name.endsWith("Operation") || name.endsWith("Attribute") || name.endsWith("Interface")) {
				numStarts++;
				long level = numStarts - numEnds;

				GenericRigiNode rigiNode = new GenericRigiNode();
				rigiNode.setNodeID(String.valueOf(id++));
				rigiNode.setNodeType(name);
				rigiNode.setNodeLabel(qName);
				setAttributes(rigiNode, atts);

				if (name.toLowerCase().indexOf("class") != -1 || name.toLowerCase().indexOf("Interface") != -1) {
					dataTypes.put(getAttribute(atts, "xmi.id"), getAttribute(atts, "name"));
					//rigiNode.setCustomizedData("LAYOUT_UML", "True");
				}

				//need to keep track of last operation so that we can
				//assemble signature after we visit parameter and return elements
				if (name.endsWith("Operation")) {
					lastOperation = rigiNode;
				}


				rigiNodesTable.put(rigiNode.getNodeID(), rigiNode);

				GenericRigiNode parentNode = (GenericRigiNode) levelToRigiNode.get(new Long(level - 1));
				if (parentNode != null) {
					createChildParentArc(parentNode, rigiNode);
				}
				levelToRigiNode.put(new Long(level), rigiNode);

			} else if (name.endsWith("DataType")) { //Datatypes like long, void, etc
				dataTypes.put(getAttribute(atts, "xmi.id"), getAttribute(atts, "name"));
			} else if (name.endsWith("Parameter")) { //Parameter tags include parameters and return types
				String type;
				numStarts++;
				long level = numStarts - numEnds;


				GenericRigiNode rigiNode = new GenericRigiNode();

				if ((getAttribute(atts, "kind")).equalsIgnoreCase("return")) {
					type = "Return";
					opReturn = rigiNode;
				} else {
					type = "Parameter";
					opParams.add(rigiNode);
				}

				rigiNode.setNodeID(String.valueOf(id++));
				rigiNode.setNodeType(type);
				rigiNode.setNodeLabel(qName);
				setAttributes(rigiNode, atts);

				rigiNodesTable.put(rigiNode.getNodeID(), rigiNode);

				GenericRigiNode parentNode = (GenericRigiNode) levelToRigiNode.get(new Long(level - 1));
				if (parentNode != null) {
					createChildParentArc(parentNode, rigiNode);
				}
				levelToRigiNode.put(new Long(level), rigiNode);
			}
		}

		public void endElement(String uri, String name, String qName) {
			if (name.endsWith("Class") || name.endsWith("Operation") || name.endsWith("Attribute") || name.endsWith("Interface") || name.endsWith("Parameter")) {
				numEnds++;
			}

			if (name.endsWith("Operation")) {
				lastOperation.setCustomizedData("return", opReturn);
				lastOperation.setCustomizedData("params", opParams.clone());
				opParams.clear();
			}

		}
	}

	private class XMIArcHandler extends DefaultHandler {
		Vector associations;
		Vector multiplicities;

		public XMIArcHandler() {
			super();
		}

		private String getAttributeValue(String att, Attributes atts) {
			for (int i = 0; i < atts.getLength(); i++) {
				String attTypeName = atts.getQName(i);
				String attValue = atts.getValue(i);
				if (attTypeName.equalsIgnoreCase(att)) {
					return attValue;
				}
			}
			return "";
		}

		public void startElement(String uri, String name, String qName, Attributes atts) {
			//System.out.println("Arc: " + uri + ", " + name + ", " + qName + ", " + atts);
			if (name.endsWith("Association")) {
				associations = new Vector();
				multiplicities = new Vector();
			}

			if (name.endsWith("AssociationEnd")) {
				String endId = getAttributeValue("type", atts);
				associations.add(xmiIdToRigiNode.get(endId));
			}

			if (name.endsWith("MultiplicityRange")) {
				String lower = getAttributeValue("lower", atts);
				String upper = getAttributeValue("upper", atts);
				multiplicities.add(lower);
				multiplicities.add(upper);
			}

			if (name.endsWith("Generalization")) {
				String child = getAttributeValue("child", atts);
				String parent = getAttributeValue("parent", atts);
				createArc((GenericRigiNode) xmiIdToRigiNode.get(child), (GenericRigiNode) xmiIdToRigiNode.get(parent), "Extends");
			}

			if (name.endsWith("Abstraction")) {
				String supplier = getAttributeValue("supplier", atts);
				String client = getAttributeValue("client", atts);
				createArc((GenericRigiNode) xmiIdToRigiNode.get(client), (GenericRigiNode) xmiIdToRigiNode.get(supplier), "Implements");
			}

		}

		public void endElement(String uri, String name, String qName) {
			if (name.endsWith("Association")) {
				GenericRigiArc arc = createArc((GenericRigiNode) associations.elementAt(0), (GenericRigiNode) associations.elementAt(1), "Association");
				if (arc != null) {
					arc.setCustomizedData("Parent_Multiplicity_Lower", multiplicities.elementAt(0));
					arc.setCustomizedData("Parent_Multiplicity_Upper", multiplicities.elementAt(1));
					arc.setCustomizedData("Child_Multiplicity_Lower", multiplicities.elementAt(2));
					arc.setCustomizedData("Child_Multiplicity_Upper", multiplicities.elementAt(3));
					StringBuffer multiplicitiesStr = new StringBuffer(" ");
					multiplicitiesStr.append((String)multiplicities.elementAt(0));
					multiplicitiesStr.append("..");
					multiplicitiesStr.append((String)multiplicities.elementAt(1));
					multiplicitiesStr.append(" to ");
					multiplicitiesStr.append((String)multiplicities.elementAt(2));
					multiplicitiesStr.append("..");
					multiplicitiesStr.append((String)multiplicities.elementAt(3));
					arc.setArcLabel(arc.getArcLabel().replaceFirst("zzzMzzz", multiplicitiesStr.toString()));
				}
			}
		}
	}

}



