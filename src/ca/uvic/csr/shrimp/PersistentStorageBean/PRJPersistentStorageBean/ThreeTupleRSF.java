/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Reads RSF in the ThreeTupleRSF format.
 * @author Derek Rayside
 */
class ThreeTupleRSF extends RSF {

    // kinds of tokens
    private final static int VERB = 1;
    private final static int SUBJECT = 2;
    private final static int OBJECT = 3;
    private static final boolean HIDE_PROTOTYPES_SELF_LOOPS = true;

    private final Factory factory = new Factory();
    private final Set nodeTypes = new TreeSet();
    private final Set arcTypes = new TreeSet();
    private final Set nodeAttributeTypes = new TreeSet();
    private final Set arcAttributeTypes = new TreeSet();

    ThreeTupleRSF() {
        super();
    }

    private void setDefaultNodeAttributeTypes () {
        // set some default node attribute types
        nodeAttributeTypes.add("image");
        nodeAttributeTypes.add(AttributeConstants.NOM_ATTR_CLOSED_IMAGE);
        nodeAttributeTypes.add("url");
        nodeAttributeTypes.add("nodeurl");
        nodeAttributeTypes.add(JavaDomainConstants.JAVADOC);
        nodeAttributeTypes.add(SoftwareDomainConstants.UML);
        nodeAttributeTypes.add("tagged");
    }

    protected void extract() throws IOException {
        final URI domainDir = prjFile.getRSFDomain();

        // Riginode
        try {
            final URI uri = new URI(domainDir + "/Riginode");
            final BufferedReader reader = ResourceHandler.getReader(uri);
            if (reader != null) {
                //System.out.println("\n known node types: ");
                parseRiginodeOrRigiarc(reader, nodeTypes);
                reader.close();
            }
        } catch (Exception e) {
            System.err.println("Exception reading Riginode: " + e.getMessage());
        }

        // Rigiarc
        try {
            final URI uri = new URI(domainDir + "/Rigiarc");
            final BufferedReader reader = ResourceHandler.getReader(uri);
            if (reader != null) {
                //System.out.println("\n known arc types: ");
                parseRiginodeOrRigiarc(reader, arcTypes);
                reader.close();
            }
        } catch (Exception e) {
            System.err.println("Exception reading Rigiarc: " + e.getMessage());
        }

        // Rigiattr
        try {
            final URI uri = new URI(domainDir + "/Rigiattr");
            final BufferedReader reader = ResourceHandler.getReader(uri);
            if (reader != null) {
                //System.out.println("\n known attribute types: ");
                parseRigiattr(reader);
                reader.close();
            } else {
                setDefaultNodeAttributeTypes();
            }
        } catch (Exception e) {
            System.err.println("Exception reading Rigiattr: " + e.getMessage());
            setDefaultNodeAttributeTypes();
        }

        // read the rsf file
		final URI uri = prjFile.getRSFFile();
        final BufferedReader reader = ResourceHandler.getReader(uri);
        parseRSF(reader);
        factory.fireAddDataEvent(storageBean);
		reader.close();
	}

    private void parseRiginodeOrRigiarc(final Reader reader, final Set types)  {
		final StreamTokenizer t = new StreamTokenizer(reader);
        setSyntax(t);

		try {
			t.nextToken();
			while (t.ttype != StreamTokenizer.TT_EOF) {

				// get the next token
                final String token;
                switch (t.ttype) {
					case StreamTokenizer.TT_WORD:   case '"':
						token = t.sval;
						break;
					case StreamTokenizer.TT_NUMBER:
						token = Double.toString(t.nval);
						break;
                    case StreamTokenizer.TT_EOL:
                        // end of line, reset everything
                        token = null;
                        break;
					default:
                        token = null;
						System.err.println("exception in RSF ThreeTuple domain parser ...");
                        System.err.println("t.ttype==" + t.ttype
                        + "\t t.sval==" + t.sval);
				}

                // move on to the next ...
				t.nextToken();

                // some tokens just change the state,
                // and are otherwise ignored
                if (token == null) {
					continue;
				}

                // if we got this far we've got something ...
                types.add(token);
                //System.out.println(types);

            } // end while

		} catch (IOException e) {
			System.err.println("IOException tokenizing RSF on line:  " + t.lineno());
			System.err.println(e);
			e.printStackTrace(System.err);
		}
    }

    private void parseRigiattr(final Reader reader)  {
		final StreamTokenizer t = new StreamTokenizer(reader);
        setSyntax(t);

        final int ATTR = 1;
        final int NODE_OR_ARC = 2;
        final int NODE_ATTRIBUTE = 3;
        final int ARC_ATTRIBUTE = 4;
        final int BAD_LINE = 5;
        int state = ATTR;

		try {
			t.nextToken();
			while (t.ttype != StreamTokenizer.TT_EOF) {

				// get the next token
                final String token;
                switch (t.ttype) {
					case StreamTokenizer.TT_WORD:   case '"':
						token = t.sval;
						break;
					case StreamTokenizer.TT_NUMBER:
						token = Double.toString(t.nval);
						break;
                    case StreamTokenizer.TT_EOL:
                        // end of line, reset everything
                        token = null;
                        state = ATTR;
                        break;
					default:
                        token = null;
						System.err.println("exception in RSF ThreeTuple domain parser ...");
                        System.err.println("t.ttype==" + t.ttype
                        + "\t t.sval==" + t.sval);
				}

                // move on to the next ...
				t.nextToken();

                // some tokens just change the state,
                // and are otherwise ignored
                if (token == null) {
					continue;
				}

                // if we got this far, do something ...
                switch (state) {
                    case ATTR:
                        if (!token.equals("attr")) {
							state = BAD_LINE;
						} else {
							state = NODE_OR_ARC;
						}
                        break;
                    case NODE_OR_ARC:
                        if (token.equals("Node")) {
							state = NODE_ATTRIBUTE;
						} else if (token.equals("Arc")) {
							state = ARC_ATTRIBUTE;
						} else {
							state = BAD_LINE;
						}
                        break;
                    case NODE_ATTRIBUTE:
                        nodeAttributeTypes.add(token);
                        //System.out.println("node attr: " + nodeAttributeTypes);
                        state = ATTR;
                        break;
                    case ARC_ATTRIBUTE:
                        // note that these cannot be used by three-tuple rsf
                        arcAttributeTypes.add(token);
                        //System.out.println("arc attr: " + arcAttributeTypes);
                        state = ATTR;
                        break;
                    case BAD_LINE:
                        // ignore everything ...
                        // wait for reset at TT_EOL
                        break;
                    default:
                        System.err.println("bad state in parsing rsf domain files: " + state);
                }

            } // end while

		} catch (IOException e) {
			System.err.println("IOException tokenizing RSF on line:  " + t.lineno());
			System.err.println(e);
			e.printStackTrace(System.err);
		}

    }

	private void parseRSF(Reader reader) {
		final StreamTokenizer t = new StreamTokenizer(reader);
        setSyntax(t);

        String verb = null;
        String subject = "";        // should these be null by default?
        String object = "";
        int state = VERB;       // what we're expecting the next token to be

		try {
			t.nextToken();
			while (t.ttype != StreamTokenizer.TT_EOF) {

				// get the next token
                final String token;
                switch (t.ttype) {
					case StreamTokenizer.TT_WORD:   case '"':
						token = t.sval;
						break;
					case StreamTokenizer.TT_NUMBER:
						token = Double.toString(t.nval);
						break;
                    case StreamTokenizer.TT_EOL:
                        // end of line, reset everything
                        token = null;
                        verb = null;
                        subject = "";
                        object = "";
                        state = VERB;
                        //System.out.println("    line " +
                        //    Integer.toString(t.lineno()));
                        break;
					default:
                        token = null;
						System.err.println("exception in RSF ThreeTuple parser ...");
                        System.err.println("t.ttype==" + t.ttype
                        + "\t t.sval==" + t.sval);
				}

                // move on to the next ...
				t.nextToken();

                // some tokens just change the state,
                // and are otherwise ignored
                if (token == null) {
					continue;
				}

                switch (state) {
                    case VERB:
                        verb = token;
                        state = SUBJECT;
                        break;
                    case SUBJECT:
                        subject = token;
                        state = OBJECT;
                        break;
                    case OBJECT:
                        object = token;
                        processTuple(verb, subject, object);
                        state = VERB;
                        break;
                    default:
                        System.err.println("unknown state in RSF ThreeTuple parser ...");
                }

			}
		} catch (IOException e) {
			System.err.println("IOException tokenizing RSF on line:  " + t.lineno());
			System.err.println(e);
			e.printStackTrace(System.err);
		}

	}

    private void processTuple(final String verb, final String subject, final String object) {
        if (verb.equals("type")) {
            // node declaration
            final GenericRigiNode n = factory.makeRigiNodeFromName(subject);
            n.setNodeType(object);

        } else if (nodeAttributeTypes.contains(verb)) {
            // attribute declaration
            // note that only nodes may have attributes
            // in three-tuple rsf
            final GenericRigiNode node = factory.makeRigiNodeFromName(subject);
            if (verb.equals("image") || verb.equals("ClosedImage")) {
                node.setCustomizedData("ClosedImage", prjFile.constructImageURI(object));
            } else if (verb.equals("url") || verb.equals("nodeurl")) {
                node.setCustomizedData( SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI, prjFile.constructCodeURI(object));
            } else if (verb.equals(JavaDomainConstants.JAVADOC)) {
                node.setCustomizedData(JavaDomainConstants.JAVADOC, prjFile.constructDocURI(object));
            } else if (verb.equals(SoftwareDomainConstants.UML)) {
                node.setCustomizedData(SoftwareDomainConstants.UML, prjFile.constructUmlURI(object));
            } else if (verb.equals("tagged")) {
                node.setCustomizedData("tagged", new Boolean(true));
            } else {
                node.setCustomizedData(verb, object);
            }

        } else {
            // default:  assume it's an arc
//            if (!arcTypes.contains(verb)) {
//                System.err.println("unknown verb in three-tuple rsf:  " + verb + " --- treating as arc type");
//            }

            // arc declaration
            // a bit of hack to hide 'prototypes' self-loops
            if (verb.equals("prototypes") && HIDE_PROTOTYPES_SELF_LOOPS && subject.equals(object)) {
                System.out.println("skipping");
            } else {
                final GenericRigiNode source = factory.makeRigiNodeFromName(subject);
                final GenericRigiNode target = factory.makeRigiNodeFromName(object);
                if (verb.equals("prototypes") && HIDE_PROTOTYPES_SELF_LOOPS && source.equals(target)) {
                    System.out.println("skipping");
                    // do nothing
                } else {
                    final GenericRigiArc arc = factory.makeRigiArc();
                    arc.setArcType(verb);
                    arc.setSourceID(source.getNodeID());
                    arc.setDestID(target.getNodeID());
                }
            }
        }

    }

}


