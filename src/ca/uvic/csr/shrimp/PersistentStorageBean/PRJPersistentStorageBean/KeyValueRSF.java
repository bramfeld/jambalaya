/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URI;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Reads RSF in the Key/Value format.
 * 
 * @author Derek Rayside
 */
class KeyValueRSF extends RSF {

    // kinds of tokens
    private final static int ID_TOKEN = 1;

    private final static int KEY_TOKEN = 2;

    private final static int VALUE_TOKEN = 3;

    KeyValueRSF() {
        super();
    }

    protected void extract() throws IOException {
        final URI rsfURI = prjFile.getRSFFile();
        final BufferedReader reader = ResourceHandler.getReader(rsfURI);
        if (reader == null) {
            return;
        }
        final Factory factory = parse(reader);
        factory.fireAddDataEvent(storageBean);
        reader.close();
    }

    private Factory parse(Reader reader)  {

        final StreamTokenizer t = new StreamTokenizer(reader);
        setSyntax(t);
        t.ordinaryChar('=');

        final Factory factory = new Factory();
        final IDFactory idFactory = new IDFactory(factory, prjFile);

        ID id = null;
        String key = ""; // should these be null by default?
        String value = "";
        int state = ID_TOKEN; // what we're expecting the next token to be

        try {
            t.nextToken();
            while (t.ttype != StreamTokenizer.TT_EOF) {

                // get the next token
                final String token;
                switch (t.ttype) {
                case StreamTokenizer.TT_WORD:
                case '"':
                    token = t.sval;
                    break;
                case StreamTokenizer.TT_NUMBER:
                    token = Double.toString(t.nval);
                    break;
                case '=':
                    token = null;
                    //state = VALUE_TOKEN;
                    break;
                case StreamTokenizer.TT_EOL:
                    // end of line, reset everything
                    token = null;
                    id = null;
                    key = "";
                    value = "";
                    state = ID_TOKEN;
                    //System.out.println(" line " +
                    //    Integer.toString(t.lineno()));
                    break;
                default:
                    token = null;
                    System.err.println("error in RSF KeyValue parser ...");
                    System.err.println("t.ttype==" + t.ttype + "\t t.sval==" + t.sval);
                }

                // move on to the next ...
                t.nextToken();

                // some tokens just change the state,
                // and are otherwise ignored
                if (token == null) {
					continue;
				}

                // do something with the token,
                // depending on our expectation of it
                switch (state) {
                case ID_TOKEN:
                    id = idFactory.makeID(token);
                    state = KEY_TOKEN;
                    break;
                case KEY_TOKEN:
                    key = token;
                    state = VALUE_TOKEN;
                    break;
                case VALUE_TOKEN:
                    value = token;
                    id.addKeyValuePair(key, value);
                    key = "";
                    value = "";
                    state = KEY_TOKEN;
                    break;
                default:
                    System.err.println("unknown state in RSF KeyValue parser ...");
                }

            }
        } catch (IOException e) {
            System.err.println("IOException tokenizing RSF on line:  " + t.lineno());
            System.err.println(e);
            e.printStackTrace(System.err);
        }

        return factory;
    }

    ///////////////////// INNER CLASSES ////////////////////////////

    /** @see ShrimpDisplayTree#addArtifact(Artifact) */
    private class IDFactory {

        private final static char NODE_ID_CHAR = 'n';

        private final static char ARC_ID_CHAR = 'a';

        private final static char TERMINAL_ID_DELIMITER = '!';

        private Factory factory;

        private PRJFile prjFile;

        IDFactory(final Factory f, final PRJFile p) {
            factory = f;
            prjFile = p;
        }

        /**
         * Checks the first character to determine what kind of id it is. If it
         * looks like a node id, then it checks if it's actually a terminal id.
         * Removes the leading character, since RigiArtifact and
         * RigiRelationship have Long id's.
         */
        ID makeID(final String sID) {
            if (sID.length() < 1) {
				return null;
			}
            switch (sID.charAt(0)) {
            case NODE_ID_CHAR:
                return makeNodeOrTerminalID(sID);
            case ARC_ID_CHAR:
                return new ArcID(this, factory.makeRigiArc(sID.substring(1)));
            default:
                return null;
            }
        }

        NodeOrTerminalID makeNodeOrTerminalID(final String sID) {
            // find out if it's a terminal id or a node id
            final int index = sID.indexOf(TERMINAL_ID_DELIMITER);
            if (index == -1) {
                // it's a regular node id
                return new NodeID(this, factory.makeRigiNode(sID.substring(1)));
            }
            // it's a terminal id
            return new TerminalID(this, factory.makeRigiNode(sID.substring(1, index)), sID.substring(index + 1));
        }

    }

    private abstract class ID {
        IDFactory idFactory;

        protected ID() {
            super();
        }

        ID(final IDFactory i) {
            idFactory = i;
        }

        abstract void addKeyValuePair(String key, String value);
    } 

    private abstract class NodeOrTerminalID extends ID {
        GenericRigiNode node;

        NodeOrTerminalID(IDFactory i) {
            super(i);
        }

        void setAsArcSource(GenericRigiArc arc) {
            arc.setSourceID(node.getNodeID());
        }

        void setAsArcDest(GenericRigiArc arc) {
            arc.setDestID(node.getNodeID());
        }
    } 

    private class NodeID extends NodeOrTerminalID {
        NodeID(final IDFactory i, final GenericRigiNode n) {
            super(i);
            node = n;
        }

        void addKeyValuePair(final String key, final String value) {
            final String lowerKey = key.toLowerCase();
            if (lowerKey.equals("type")) {
				node.setNodeType(value);
			} else if (lowerKey.equals("label")) {
				node.setNodeLabel(value);
			} else if (lowerKey.equals("image")) {
				node.setCustomizedData(AttributeConstants.NOM_ATTR_CLOSED_IMAGE, idFactory.prjFile.constructImageURI(value));
			} else if (lowerKey.equals("url") || lowerKey.equals("nodeurl")) {
				node.setCustomizedData(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI, idFactory.prjFile.constructCodeURI(value));
			} else if (lowerKey.equals(JavaDomainConstants.JAVADOC)) {
				node.setCustomizedData(JavaDomainConstants.JAVADOC, idFactory.prjFile.constructDocURI(value));
			} else if (lowerKey.equals(SoftwareDomainConstants.UML)) {
				node.setCustomizedData(SoftwareDomainConstants.UML, idFactory.prjFile.constructUmlURI(value));
			} else {
				node.setCustomizedData(key, value);
			}
        }
    } 

    private class TerminalID extends NodeOrTerminalID {
        String name;

        TerminalID(IDFactory i, GenericRigiNode node, String name) {
            super(i);
            this.node = node;
            this.name = name;
            final Object obj = node.getCustomizedData(name);
            if (obj == null) {
                // need to create this terminal on the node
                node.setCustomizedData(name, "Terminal");
            } else {
                // it's already there ... check that it's ok
                if (!obj.toString().endsWith("Terminal")) {
					System.err.println("Problem creating terminal '" + name + "' on node " + node.toString());
				}
            }
        }

        void addKeyValuePair(final String key, final String value) {
            final String lowerKey = key.toLowerCase();
            if (lowerKey.equals("image")) {
                node.setCustomizedData(name + "Image", idFactory.prjFile.getRSFDomain().toString() + File.separator + value);
            } else {
                node.setCustomizedData(name + key, value);
            }
        }

        void setAsArcSource(GenericRigiArc arc) {
            arc.setSourceID(node.getNodeID());
            arc.setCustomizedData(AttributeConstants.NOM_ATTR_SOURCE_TERMINAL_ID, name);
        }

        void setAsArcDest(GenericRigiArc arc) {
            arc.setDestID(node.getNodeID());
            arc.setCustomizedData(AttributeConstants.NOM_ATTR_TARGET_TERMINAL_ID, name);
        }
    } 

    private class ArcID extends ID {
        GenericRigiArc arc;

        ArcID(final IDFactory i, final GenericRigiArc a) {
            super(i);
            arc = a;
        }

        void addKeyValuePair(final String key, final String value) {
            final String lowerKey = key.toLowerCase();
            if (lowerKey.equals("type")) {
				arc.setArcType(value);
			} else if (lowerKey.equals("source")) {
                final NodeOrTerminalID i = idFactory.makeNodeOrTerminalID(value);
                i.setAsArcSource(arc);
            } else if (lowerKey.equals("target")) {
                final NodeOrTerminalID i = idFactory.makeNodeOrTerminalID(value);
                i.setAsArcDest(arc);
            } else {
				arc.setCustomizedData(key, value);
			}
        }
    } 

}

