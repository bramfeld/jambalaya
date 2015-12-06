/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.caida.libsea.AttributeCreator;
import org.caida.libsea.DuplicateObjectException;
import org.caida.libsea.Graph;
import org.caida.libsea.GraphBuilder;
import org.caida.libsea.GraphFactory;
import org.caida.libsea.GraphFileWriter;
import org.caida.libsea.QualifierCreator;
import org.caida.libsea.ValueType;

import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * The purpose of this class is to export to the LibSea graph format, specifially the format that
 * can be read by the Walrus 3-D graph visualization engine...its cool!<br>
 * <a href="http://www.caida.org/tools/visualization/walrus/">Walrus - Graph Visualization Tool</a><br>
 * <a href ="http://www.caida.org/tools/visualization/libsea/">The LibSea Graph File Format and Java Graph Library</a> 
 * 
 * @author Rob Lintern
 */
public class LibSeaPersistentStorageBean implements PersistentStorageBean {
    
	private List psbListeners = new ArrayList();
    private Map artToIdMap = new HashMap();
    private Map relToIdMap = new HashMap();
    private int nextArtId = 0;
    //private int nextRelId = 0;
    private Vector fakeRootRels = new Vector ();
    private String fakeRoot = "";
    private ShrimpProject project;
    
    public LibSeaPersistentStorageBean(ShrimpProject project) {
        super();
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#loadData(java.net.URI)
     */
    public void loadData(URI uri) {
        throw new Error ("Sorry, LibSeaPersistentStorageBean.loadData(uri) is not implemented yet!");
    }
    
    private void setId(Object rel, int id) {
        relToIdMap.put(rel, new Integer(id));
        //nextRelId = id+1;
    }
    
    private int getRelId(Object rel) {
        Integer id = (Integer) relToIdMap.get(rel);
        return id == null ? -1 : id.intValue();
    }
    
    private int getArtId(Object art, boolean createIfNotFound) {
        Integer id = (Integer) artToIdMap.get(art);
        if (id == null && createIfNotFound) {
            id = new Integer(nextArtId++);
            artToIdMap.put(art, id);
        }
        return id == null ? -1 : id.intValue();
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#saveData(java.lang.String,
     *      java.util.Vector, java.util.Vector)
     */
    public void saveData(String filename, Vector artifacts, Vector relationships) {
        if (artifacts.isEmpty() || relationships.isEmpty()) {
            System.err.println("no artifacts, or no relationships to save");
            return;
        }
        fakeRootRels = new Vector ();
        fakeRoot = "";
        Graph graph = null;
        GraphBuilder builder = GraphFactory.makeImmutableGraph();
        try {
	        builder.setGraphDescription(filename);
	        builder.setGraphName(filename);

	        DataBean dataBean = ((Artifact)artifacts.firstElement()).getDataBean();
	        Vector rootArtifacts = dataBean.getRootArtifacts(dataBean.getDefaultCprels());
	        System.out.println("rootArtifacts.size(): " + rootArtifacts.size());
	        int numNodes = artifacts.size();
	        if (rootArtifacts.size() > 1) {
	            numNodes++;
	        }
	        int numLinks = relationships.size();
	        if (rootArtifacts.size() > 1) {
	            numLinks += rootArtifacts.size();
	        }
	        System.out.println("numNodes: " + numNodes);
	        System.out.println("numLinks: " + numLinks);
	        builder.allocateNodes(numNodes);
	        builder.allocateLinks(numLinks);
	        builder.allocatePaths(0);
	        builder.allocatePathLinks(0);
	        if (rootArtifacts.size() > 1) { 
	            createSingleFakeRootAndLinks(builder, dataBean, rootArtifacts);
	        }
	        for (Iterator iter = relationships.iterator(); iter.hasNext();) {
                Relationship rel = (Relationship) iter.next();
                Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
                Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
                int relId = builder.addLink(getArtId(srcArt, true), getArtId(destArt, true));
                setId(rel, relId);
            }
	        
	        saveAttributes(builder, artifacts, true);
	        saveAttributes(builder, relationships, false);
	        
	        saveSpanningTreeForWalrus (builder, artifacts, relationships);

        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        } 
        try {
	        graph = builder.endConstruction();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
	        Writer writer = new FileWriter (new File (filename));
	        GraphFileWriter fileWriter = new GraphFileWriter(writer, graph, true, true);
            fileWriter.write();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    private void createSingleFakeRootAndLinks(GraphBuilder builder, DataBean dataBean, Vector rootArtifacts) {
        fakeRoot = "fake_root";
        int fakeRootId = getArtId(fakeRoot, true);
        for (Iterator iter = rootArtifacts.iterator(); iter.hasNext();) {
            Artifact realRootArt = (Artifact) iter.next();
            String fakeRootRel = "fake_root" + realRootArt.getName(); 
            int linkId = builder.addLink(fakeRootId, getArtId(realRootArt, true));
            setId(fakeRootRel, linkId);
            fakeRootRels.add(fakeRootRel);
        }
    }
    
    /**
     * @param builder
     * @param artifacts
     * @param relationships
     * @throws DuplicateObjectException
     */
    private void saveSpanningTreeForWalrus(GraphBuilder builder, Vector artifacts, Vector relationships) throws DuplicateObjectException {
        DataBean dataBean = ((Artifact)artifacts.firstElement()).getDataBean();
        Vector rootArtifacts = dataBean.getRootArtifacts(dataBean.getDefaultCprels());

        int rootAttr = builder.addAttributeDefinition ("root", ValueType.BOOLEAN, -1, null);
        int rootId = getArtId(rootArtifacts.size() > 1 ? fakeRoot : rootArtifacts.firstElement(), false); 
        builder.addNodeAttribute(rootId).addBooleanValue(true);
        
        int treeLinkAttr = builder.addAttributeDefinition("tree_link", ValueType.BOOLEAN, -1, null);
        if (rootArtifacts.size() > 1) {
           for (Iterator iter = fakeRootRels.iterator(); iter.hasNext();) {
               String fakeRootRel = (String) iter.next();
               builder.addLinkAttribute(getRelId(fakeRootRel)).addBooleanValue(true);
           } 
        }
        List seenArts = new ArrayList();
        List spanningRels = new ArrayList ();        
        List cprels = Arrays.asList(dataBean.getDefaultCprels());
        for (Iterator iter = rootArtifacts.iterator(); iter.hasNext();) {
            Artifact rootArt = (Artifact) iter.next();
            saveSpanningTreeForWalrusRecursive(rootArt, cprels, spanningRels, seenArts);
        }
        
        for (Iterator iter = spanningRels.iterator(); iter.hasNext();) {
            Relationship rel = (Relationship) iter.next();
            builder.addLinkAttribute(getRelId(rel)).addBooleanValue(true);
        }
        QualifierCreator qualifierCreator = builder.addQualifier("spanning_tree", "is_a_Hierarchy", "The is-a hierarchy");
        qualifierCreator.associateAttribute(rootAttr, "root");
        qualifierCreator.associateAttribute(treeLinkAttr, "tree_link");
    }

    private void saveSpanningTreeForWalrusRecursive(Artifact art, List cprels, List spanningRels, List seenArts) {
        if (seenArts.contains(art)) {
            return;
        }
        seenArts.add(art);
        Vector rels = art.getRelationships();
        for (Iterator iter = rels.iterator(); iter.hasNext();) {
            Relationship rel = (Relationship) iter.next();
            if (rel.getArtifacts().elementAt(0).equals(art)) {
                Artifact child = (Artifact) rel.getArtifacts().elementAt(1);
                if (!seenArts.contains(child)) {
                    spanningRels.add(rel);
                    saveSpanningTreeForWalrusRecursive(child, cprels, spanningRels, seenArts);
                }
            }
        }        
    }
    private void saveColorAttributes(GraphBuilder builder, Vector objs, boolean areArtifacts) {
        try {
            //hard coded color attribute values for artifact and rel type 
            // TODO generate color attributes for all attributes
            AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
            String visVarName = areArtifacts ? VisVarConstants.VIS_VAR_NODE_COLOR : VisVarConstants.VIS_VAR_ARC_COLOR;           
            Attribute attr = attrToVisVarBean.getMappedAttribute(visVarName);
	        try {
	            String validAttrName = getValidAttrName(attr.getName() + "_colour");
                builder.addAttributeDefinition(validAttrName, ValueType.FLOAT3, -1, null);
            } catch (DuplicateObjectException e) {
                e.printStackTrace();
                return;
            }
            if (attr != null) {
                for (Iterator iter = objs.iterator(); iter.hasNext();) {
                    Object obj = iter.next();
                    if (!(obj instanceof Artifact) && !(obj instanceof Relationship)) {
                        continue;
                    }
                    String type = areArtifacts ? ((Artifact)obj).getType() : ((Relationship)obj).getType();
                    Color colour = (Color) attrToVisVarBean.getVisVarValue(attr.getName(), visVarName, type);
                    AttributeCreator creator = areArtifacts ? builder.addNodeAttribute(getArtId(obj, true)) : builder.addLinkAttribute(getRelId(obj));
                    creator.addFloat3Value(colour.getRed()/255.0f, colour.getGreen()/255.0f, colour.getBlue()/255.0f);
                }
            } else {
                System.err.println("no attribute for " + visVarName);
            }
        } catch (BeanNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    private String getValidAttrName(String attrName) {
        String validAttrName = attrName.replaceAll(" ", "_"); // antlr/walrus doesn't like spaces
        validAttrName = validAttrName.replaceAll("\\(", "_"); // or bracket (
        validAttrName = validAttrName.replaceAll("\\)", "_"); // or bracket )
        validAttrName = validAttrName.replaceAll(",", "_");   // or commas
        // TODO what other characters are not allowed by antlr/walrus?
        return validAttrName;
    }

    private void saveAttributes(GraphBuilder builder, Vector objs, boolean areArtifacts) {
        saveColorAttributes(builder, objs, areArtifacts);
        Set allAttrNames = new HashSet ();
        for (Iterator iter = objs.iterator(); iter.hasNext();) {
            Object obj = iter.next();
            if (!(obj instanceof Artifact) && !(obj instanceof Relationship)) {
                continue;
            }
            allAttrNames.addAll(areArtifacts ? ((Artifact)obj).getAttributeNames() : ((Relationship)obj).getAttributeNames());
        }
        for (Iterator iter = allAttrNames.iterator(); iter.hasNext();) {
            String attrName = (String) iter.next();
            ValueType valueType = null;
            for (Iterator iterator = objs.iterator(); iterator.hasNext() && valueType == null;) {
                Object obj = iterator.next();
                if (!(obj instanceof Artifact) && !(obj instanceof Relationship)) {
                    continue;
                }
                Object attrValue = areArtifacts ? ((Artifact)obj).getAttribute(attrName) : ((Relationship)obj).getAttribute(attrName);                
                if (attrValue == null) {
                    //System.err.println("Warning. There is no value for attr '" + attrName + "' of object " + obj);
                    continue;
                }
                valueType = getValueType(attrValue);
            }
            if (valueType == null) {
                continue;
            }
	        try {
	            String validAttrName = getValidAttrName(attrName);
                builder.addAttributeDefinition(validAttrName, valueType, -1, null);
            } catch (DuplicateObjectException e) {
                e.printStackTrace();
                continue;
            }
            for (Iterator iterator = objs.iterator(); iterator.hasNext();) {
                Object obj = iterator.next();
                Object attrValue = areArtifacts ? ((Artifact)obj).getAttribute(attrName) : ((Relationship)obj).getAttribute(attrName);
                if (attrValue == null) {
                    //System.err.println("Warning. There is no value for attr '" + attrName + "' of object " + obj);
                    continue;
                }
                AttributeCreator creator = areArtifacts ? builder.addNodeAttribute(getArtId(obj, true)) : builder.addLinkAttribute(getRelId(obj));
                if (valueType.equals(ValueType.BOOLEAN) && attrValue instanceof Boolean) {
                    creator.addBooleanValue(((Boolean)attrValue).booleanValue());
                } else if (valueType.equals(ValueType.INTEGER) && attrValue instanceof Integer) {
                    creator.addIntegerValue(((Integer)attrValue).intValue());
                } else if (valueType.equals(ValueType.FLOAT) && attrValue instanceof Float) {
                    creator.addFloatValue(((Float)attrValue).floatValue());
                } else if (valueType.equals(ValueType.DOUBLE) && attrValue instanceof Double) {
                    creator.addDoubleValue(((Double)attrValue).doubleValue());
                } else if (valueType.equals(ValueType.STRING) && attrValue instanceof String) {
                    creator.addStringValue((String)attrValue);
                } else {
                    System.err.println("Warning. Attribute '" + attrName + "' is not right type for object: '" + obj);
                }
            }
        }
        
    }

    private ValueType getValueType(Object obj) {
        ValueType valueType = null;
        if (obj instanceof Boolean) {
            valueType = ValueType.BOOLEAN;
        } else if (obj instanceof Integer) {
            valueType = ValueType.INTEGER;
        } else if (obj instanceof Double) {
            valueType = ValueType.DOUBLE;
        } else if (obj instanceof Float) {
            valueType = ValueType.FLOAT;
        } else if (obj instanceof String) {
            valueType = ValueType.STRING;
        } else {
            System.err.println("Warning: Can't determine ValueType for object: " + obj);
        }
        return valueType;
    }
    /*
     * (non-Javadoc)
     * 
     * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#addPersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.listener.PersistentStorageBeanListener)
     */
    public void addPersistentStorageBeanListener(PersistentStorageBeanListener listener) {
        psbListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean#removePersistentStorageBeanListener(ca.uvic.csr.shrimp.PersistentStorageBean.listener.PersistentStorageBeanListener)
     */
    public void removePersistentStorageBeanListener(PersistentStorageBeanListener listener) {
        psbListeners.remove(listener);
    }

    protected void fireDataLoadedEvent(Object loadedData) {
        DataLoadedEvent event = new DataLoadedEvent(loadedData);
        for (Iterator iter = psbListeners.iterator(); iter.hasNext();) {
            PersistentStorageBeanListener listener = (PersistentStorageBeanListener) iter.next();
            listener.dataLoaded(event);
        }
    }

}