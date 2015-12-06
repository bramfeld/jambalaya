/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */


package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Hashtable;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Reads RSF in the Structured format (ie, the format Rigi saves in).
 * This is the default RSF style.
 * @author Derek Rayside
 */


class StructuredRSF extends RSF {

    final private Hashtable nodes = new Hashtable();
    final private Hashtable arcs = new Hashtable();


    StructuredRSF() {super();}

   
	protected void extract() throws IOException {
		final URI uri = prjFile.getRSFFile();
		final BufferedReader reader = ResourceHandler.getReader(uri);
		if (reader == null) {
		    return;
		}
		
		String line;
		
		while((line = reader.readLine()) != null) {
			if(line.startsWith("\"a") || line.startsWith("a")) {
				processArcData(line);
			} else if(line.startsWith("\"n") || line.startsWith("n")) {
				processNodeData(line);
			}
		}

		reader.close();
	    storageBean.fireDataLoadedEvent (nodes, arcs);
	}



    /**
     * Parses strings and creates new instances of RigiArc as
     * necessary, and sets attributes for created RigiArcs.
     * @see RSFFileInterpreter#getRSFData()
     */
    private void processArcData(String line)  {
		String[] tokens;
		int exIndex;
		String srcID, arcID, destID, destLabel;
		String firstArg;
		GenericRigiArc arc;
		//GenericRigiNode srcNode;
		GenericRigiNode destNode;
	
		//Extract the arc information
		tokens = getTokens(line);	
		tokens[0] = tokens[0].substring(2); // take out "a!"

		firstArg = tokens[0];

		if (tokens[1].indexOf ('!') != -1) {	// it is not an attribute
			exIndex = tokens[1].indexOf('!');
			srcID = tokens[1].substring(0, exIndex);
			arcID = tokens[1].substring(exIndex + 1);
			exIndex = tokens[2].indexOf('!');
			destID = tokens[2].substring(0, exIndex);
			destLabel = tokens[2].substring(exIndex + 1);	
		
			/* test if this relationship has been previously constructed */
			if(arcExists(arcID))
				arc = (GenericRigiArc)arcs.get(arcID);	
			else {
				arc = new GenericRigiArc();	    
				arc.setArcID(arcID);
				arc.setDestID(destID);
				arc.setSourceID(srcID);
				arc.setDestNodeLabel(destLabel);
				arcs.put(arcID, arc);
				arc.setArcType("unknown");
			}

			/* the destination node might not exist yet */
			if(nodeExists(destID)) {
				destNode = (GenericRigiNode)nodes.get(destID);
			} else {
				destNode = new GenericRigiNode();
				destNode.setNodeID(destID);
				destNode.setNodeLabel(destLabel);
				nodes.put(destID, destNode);
			}

			arc.setArcType(firstArg);
	    } else {
	    	String attribute = tokens[0];
	    	String id = tokens[1];
	    	String value = tokens[2];

			arc = (GenericRigiArc)arcs.get(id);	
			arc.setCustomizedData(attribute, value);
	    }
		
	}
    
    /**
     * Parses Strings and creates new instances of RigiNode as
     *  necessary, and sets attributes for created RigiNodes.
     * @see RSFFileInterpreter#getRSFData()
     */
    private void processNodeData(String line)  {
		GenericRigiNode node;
	
		String [] tokenss = getTokens(line);
		int exIndex = tokenss[1].indexOf('!');
		String nodeID = tokenss[1].substring(0, exIndex);
		String nodeLabel  = tokenss[1].substring(exIndex + 1);	
		
		String attrValue = tokenss[2];
		if (attrValue == null) {
		    System.err.println("attrValue is null: " + line);
		}
		String attrName = tokenss[0].substring(2); //take out the n!

		/* test if this node has been previously constructed */
		if(nodeExists(nodeID))
		    node = (GenericRigiNode)nodes.get(nodeID);
		else{
		    node = new GenericRigiNode();	    
		    nodes.put(nodeID, node);
			node.setNodeID(nodeID);
			node.setNodeLabel(nodeLabel);
			node.setNodeType("unknown");
		}

		//Here we handle the various types of node data
		//Type data...	
		if(attrName.equals("type")) {
		    node.setNodeType(attrValue);
		}  
	
		//Attribute data...
		//Attributes fromthe C domain
		else if(attrName.equals("nodeurl")){
            node.setCustomizedData(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI, prjFile.constructCodeURI(attrValue) );
		}
	
		//Attributes frm the Java domain
		else if(attrName.equals("url")){
            node.setCustomizedData(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI, prjFile.constructCodeURI(attrValue) );
		}
        else if (attrName.equals(JavaDomainConstants.JAVADOC)) {
            node.setCustomizedData(JavaDomainConstants.JAVADOC, 
                prjFile.constructDocURI(attrValue));
        }
        else if (attrName.equals(SoftwareDomainConstants.UML)) {
            node.setCustomizedData(SoftwareDomainConstants.UML, 
                prjFile.constructUmlURI(attrValue));
        }

		// images, added for the flow domain
		// images are to be stored in the rigi domain directory
        else if (attrName.endsWith("Image")) {
            node.setCustomizedData(attrName,
                prjFile.constructImageURI(attrValue));
        }
        
        else if (attrName.equalsIgnoreCase("name")) { // a bit of a hack, makes sure that if there is a name attribute that the node gets this name
            node.setNodeLabel(attrValue);
            //System.out.println(attrValue);
        }

		// just store unknown domain dependent tokens as strings
        else {
            String val = new String (attrValue);
            try {
                val = URLDecoder.decode(val, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.err.println("Warning: " + e.getMessage());
            }
        	node.setCustomizedData(attrName, val);
        }
    }


	private static String[] getTokens(final String line) {
		// the values to be returned
		final String[] tokens = new String[3];
		int i = 0;

		final StreamTokenizer t = new StreamTokenizer(new StringReader(line));
        setSyntax(t);

		try {
			t.nextToken();
			while (t.ttype != StreamTokenizer.TT_EOF) {
				switch (t.ttype) {
					case StreamTokenizer.TT_WORD: 
					case '"': 
						tokens[i++] = t.sval;
						break;
					case StreamTokenizer.TT_NUMBER:
						tokens[i++] = Double.toString(t.nval);
						break;
                    case StreamTokenizer.TT_EOL:
                        break;
					default:
						System.err.println("t.ttype==" + t.ttype + "\t t.sval==" + t.sval);
				} 
				t.nextToken();
			}		
		} catch (IOException e) {
			System.err.println("IOException tokenizing RSF on line:  " + line);
			System.err.println(e);
			e.printStackTrace(System.err);
		}		
		return tokens;
	}
    
    private boolean nodeExists(String nodeID){ 
        return nodes.containsKey(nodeID); 
    }

    private boolean arcExists(String arcID){
		return arcs.containsKey(arcID);
    }

}



