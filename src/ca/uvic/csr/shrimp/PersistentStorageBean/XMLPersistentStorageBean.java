/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import ca.uvic.csr.shrimp.DataBean.SimpleDataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiArc;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.GenericRigiNode;

/**
 *
 */
public class XMLPersistentStorageBean implements PersistentStorageBean{

    private Vector psbListeners;

	private long id;

	private Hashtable rigiNodesTable = new Hashtable();
	private Hashtable rigiArcsTable = new Hashtable();

	/** Canonical output. */
	protected boolean canonical;

    private URI uri;

    public XMLPersistentStorageBean() {
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

	private class MyContentHandler extends DefaultHandler {
		private long numStarts = 0;
		private long numEnds = 0;
		private Map levelToRigiNode = new HashMap();
		private GenericRigiNode rigiNode;

		public MyContentHandler() {
			super();
			//Add the root node for SHriMP
			GenericRigiNode rigiNode = new GenericRigiNode();
			rigiNode.setNodeID("1");
			rigiNode.setNodeType("Document");
			rigiNode.setNodeLabel(uri.toString());
			rigiNodesTable.put(rigiNode.getNodeID(), rigiNode);
			levelToRigiNode.put(new Long(0), rigiNode);
		}

		/* (non-Javadoc)
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length) {
            String s = new String (ch, start, length);
            s = s.trim();
            if (!s.equals("")) {
                System.out.println("characters = \"" + s + "\"");
            }
        }

		public void startElement(String elementURI, String elementName, String elementQName, Attributes atts) {
		    System.out.println("starting element: " + elementURI + ", " + elementName + ", " + elementQName);
			numStarts++;
			long level = numStarts - numEnds;
			rigiNode = new GenericRigiNode();
			rigiNode.setNodeID(String.valueOf(id++));
			rigiNodesTable.put(rigiNode.getNodeID(), rigiNode);

			// try to find a proper type for the node
			String nodeType = "";
		    if (elementName != null && !elementName.equals("")) {
		        nodeType = elementName;
		    } else if (elementQName != null  || !elementQName.equals("")) {
			    nodeType = elementQName;
			} else if (elementURI != null || !elementURI.equals("")) {
			    nodeType = elementURI;
			} else {
			    nodeType = "unknown element type";
			}

			// try to find a proper name for the node
			String nodeLabel = "";
			String [] possibleNameAtts = new String [] {"name", "id"};
		    for (int i = 0; i < possibleNameAtts.length && nodeLabel.equals(""); i++) {
                String possibleNameAtt = possibleNameAtts[i];
			    String nameFromAtts = atts.getValue(possibleNameAtt);
			    if (nameFromAtts != null && !nameFromAtts.equals("")) {
			        nodeLabel = nameFromAtts;
			    }
            }

			//Add the element's attributes as attributes of the node
			for (int i = 0; i < atts.getLength(); i++) {
			    // get the attribute's name
			    String attName = "";
			    if (atts.getLocalName(i) != null && !atts.getLocalName(i).equals("")) {
			        attName = atts.getLocalName(i);
			    } else if (atts.getQName(i) != null && !atts.getQName(i).equals("")) {
			        attName = atts.getQName(i);
			    } else {
		            attName = "unknown attribute name";
			    }

			    // get the attributes value
			    String attValue = atts.getValue(i);
			    if (attValue == null || attValue.equals("")) {
			        attValue = "unknown attribute value";
			    }

			    // get the attributes type
			    String attType = atts.getType(i);
			    if (attType == null || attType.equals("")) {
			        attType = "unknown attribute type";
			    }
			    rigiNode.setCustomizedData(attName + " (" + attType + ")", attValue);
			}

			if (nodeLabel.equals("")) {
			    nodeLabel = "unknown name (" + nodeType + ")";
			}
			rigiNode.setNodeLabel(nodeLabel);
			rigiNode.setNodeType(nodeType);

			GenericRigiNode parentNode = (GenericRigiNode) levelToRigiNode.get(new Long(level - 1));
			if (parentNode != null) {
				createChildParentArc(parentNode, rigiNode);
			} else {
			    System.out.println(rigiNode + " has no parent: level =  " + level);
			}

			levelToRigiNode.put(new Long(level), rigiNode);
		}

		public void endElement(String elementURI, String elementName, String elementQName) {
		    System.out.println("ending element: " + elementURI + ", " + elementName + ", " + elementQName);
			numEnds++;
			//if (numEnds % 1000 == 0) {
				//System.out.println("numEnds: " + numEnds);
			//}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#loadData(URI)
	 */
	public void loadData(URI uri) {
	    this.uri = uri;

		rigiNodesTable = new Hashtable();
		rigiArcsTable = new Hashtable();

		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(uri.toString(), new MyContentHandler());
			fireDataLoadedEvent(rigiNodesTable, rigiArcsTable);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	 * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#saveData(String, Vector, Vector)
	 */
	public void saveData(String filename, Vector artifacts, Vector relationships) {
	}

}