/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataDisplayBridge;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.ImageObserver;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArrowHead;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CompositeArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpTerminal;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.AnnotationPanelActionAdapter;
import ca.uvic.csr.shrimp.adapter.DocumentsPanelActionAdapter;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.util.GraphicsUtils;

/**
 * Bridges data objects (artifacts and relationships) with
 * display objects (nodes, arcs, and labels).
 *
 * This class has a few purposes including factory methods to
 * create display objects from given data objects. It also acts as
 * a storage area for created display objects and contains mappings from
 * child to parent nodes, nodes to labels, artifacts to nodes, and relationships
 * to arcs.
 *
 * One relationships can be associated with many arcs.
 * One artifact can be associated with many nodes.
 * One artifact can be associated with many labels.
 * In a nested view (ShrimpView):
 *		a node has exactly one parent node (except the root which has no parent)
 *		a node has zero to many child nodes
 * In a flat view (HierarchicalView):
 *		a node has one to many parent nodes (except the root which has no parent)
 *		a node has zero to many child nodes
 *
 * @author Rob Lintern, Chris Callendar
 */
public class DataDisplayBridge {

	/** Avoid allocating this each time a node is rendered.*/
	public final static ImageObserver IMAGE_OBSERVER = new JPanel();

	private DataBean dataBean;
	private DisplayBean displayBean;
	private AttrToVisVarBean attrToVisVarBean;

    /** Maps an artifact's id to a vector of ShrimpNodes */
    private Hashtable artifactIDToShrimpNodes;

    /**
     * Maps a node id to the node it identifies.
     */
    private Map nodeIDToNodeMap;

    /**
     * Maps a node id to its label.
     */
    private Hashtable nodeIDToShrimpNodeLabel;
    private Map arcIDToShrimpArcLabel;

    /** Maps a node to its children */
    private Map parentToChildrenMap;

    /**
     * Maps a relationship to the ShrimpArcs that represent it.
     */
    private Hashtable relToShrimpArcs;

	/**
	 * Maps node to the arcs that that it participates in.
	 */
	private Hashtable shrimpNodeToShrimpArcs;

	/**
	 * Maps node to the arcs that that it participates in.
	 */
	private Hashtable nodeToDescedentsArcs;

	/**
	 * Maps src node and dest node id to the arcs that go between them.
	 */
	private Hashtable srcNodeDestNodeToShrimpArcs;

	/**
	 * Maps a node to its parent nodes
	 * Note: In the ShrimpDisplayBean there is always one parent, but
	 * in the PFlatDisplayBean there may be more than one.
	 */
	private Map childToParentsMap;

    private ViewTool tool;
    private Vector customizedPanelListeners;
    private Vector createdArcs;
    private Vector rootNodes;
    private CompositeArcsManager compositeArcsManager;

	public DataDisplayBridge(ViewTool tool) {
		this.tool = tool;

		artifactIDToShrimpNodes = new Hashtable();
		nodeIDToNodeMap = new Hashtable();
		nodeIDToShrimpNodeLabel = new Hashtable();
		arcIDToShrimpArcLabel = new HashMap();
		relToShrimpArcs = new Hashtable();
		shrimpNodeToShrimpArcs = new Hashtable();
		parentToChildrenMap = new HashMap();
		srcNodeDestNodeToShrimpArcs = new Hashtable();
		nodeToDescedentsArcs = new Hashtable();
		childToParentsMap = new HashMap();
		customizedPanelListeners = new Vector(10, 10);
		createdArcs = new Vector();
		compositeArcsManager = new CompositeArcsManager();
		rootNodes = new Vector();
	}


	public void refresh() {
		ShrimpProject project = tool.getProject();
	    if (project != null) {
		    // save rel groups and composites before getting rid of them
			compositeArcsManager.save(project);
			clear();
			try {
			    this.dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				this.attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
			try {
				this.displayBean = (DisplayBean)tool.getBean(ShrimpTool.DISPLAY_BEAN);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
			if(!(displayBean instanceof PFlatDisplayBean)) {
				compositeArcsManager.init(project, dataBean, displayBean, attrToVisVarBean);
			}
	    }
	}

	/**
	 * Clears all the data stored in this bridge
	 */
	private void clear() {
		compositeArcsManager.clear();

		Vector createdNodes = getShrimpNodes();	//.clone() - why clone?
		for (Iterator iter = createdNodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			node.dispose();
		}
		for (Iterator iter = getShrimpArcs().iterator(); iter.hasNext();) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			arc.dispose();
		}
		Vector createdLabels = getShrimpNodeLabels(); 	// .clone() - why clone?
		for (Iterator iter = createdLabels.iterator(); iter.hasNext();) {
			ShrimpLabel label = (ShrimpLabel) iter.next();
			label.dispose();
		}
		artifactIDToShrimpNodes.clear();
		nodeIDToNodeMap.clear();
		nodeIDToShrimpNodeLabel.clear();
		arcIDToShrimpArcLabel.clear();
		relToShrimpArcs.clear();
		shrimpNodeToShrimpArcs.clear();
		parentToChildrenMap.clear();
		srcNodeDestNodeToShrimpArcs.clear();
		nodeToDescedentsArcs.clear();
		childToParentsMap.clear();
		createdArcs.clear();
		rootNodes.clear();

	}

	public void removeShrimpNode(ShrimpNode sn) {
		/* TODO uncomment later
		Vector nodes;
		// remove it from its relationships with its parents, parent could still exist
		if(node.getParentShrimpNode() != null) { // root dont have any parents
			nodes = (Vector) parentToChildShrimpNodes.get(node.getParentShrimpNode());
			if(nodes != null) {
				nodes.remove(node);
				if(nodes.isEmpty()) {
					parentToChildShrimpNodes.remove(node.getParentShrimpNode());
				}
			}
		}

		childToParentShrimpNodes.remove(node);

		nodes = (Vector) artifactIDToShrimpNodes.get(node.getArtifact().getID());
		if(nodes != null) {
			nodes.remove(node);
			if(nodes.isEmpty()) {
				artifactIDToShrimpNodes.remove(node.getArtifact().getID());
			}

		}

		artifactIDAndKeyParentIDsToShrimpNode.remove(node.getID());
		*/
	}

	public void removeShrimpArc(ShrimpArc arc) {
		Vector arcs = (Vector) srcNodeDestNodeToShrimpArcs.get("" + arc.getSrcNode().getID() + arc.getDestNode().getID());
		if(arcs != null) {
			arcs.remove(arc);
			if(arcs.isEmpty()) {
				srcNodeDestNodeToShrimpArcs.remove("" + arc.getSrcNode().getID() + arc.getDestNode().getID());
			}
		}

		arcs = (Vector) shrimpNodeToShrimpArcs.get(arc.getSrcNode());
		if(arcs != null) {
			arcs.remove(arc);
			if(arcs.isEmpty()) {
				shrimpNodeToShrimpArcs.remove(arc.getSrcNode());
			}
		}

		arcs = (Vector) shrimpNodeToShrimpArcs.get(arc.getDestNode());
		if(arcs != null) {
			arcs.remove(arc);
			if(arcs.isEmpty()) {
				shrimpNodeToShrimpArcs.remove(arc.getDestNode());
			}
		}

		Relationship rel = arc.getRelationship();
		if (rel != null) {
			arcs = (Vector) relToShrimpArcs.get(rel);
			if (arcs != null) {
				arcs.remove(arc);
				if (arcs.isEmpty()) {
					relToShrimpArcs.remove(rel);
				}
			}
		}

		createdArcs.remove(arc);
	}

	public void dispose() {
		//displayBean.removeObject(rootNode);
		clear();
	}

	public Vector createDefaultRootNodes() {
	    Vector rootArtifacts = displayBean.isInverted() ? dataBean.getLeafArtifacts(displayBean.getCprels()) : dataBean.getRootArtifacts(displayBean.getCprels());
	    Vector rootNodes = new Vector();
	    for (Iterator iter = rootArtifacts.iterator(); iter.hasNext();) {
			Artifact rootArtifact = (Artifact) iter.next();
			rootNodes.add(createShrimpNode(rootArtifact, null));
		}
	    return rootNodes;
	}

	/**
	 * Returns all nodes that have been created.
	 * Returns an empty vector if no nodes found.
	 */
    public Vector getShrimpNodes() {
		Vector nodes = new Vector(nodeIDToNodeMap.size(),10);
		nodes.addAll(nodeIDToNodeMap.values());
		return nodes;
    }

	/**
	 * Returns the ShrimpNode associated with the given artifact and key parent IDs.
	 * Returns null if no ShrimpNode found.
	 * @param artifact The artifact to be converted.
	 * @param keyParentIDs The IDs of the artifacts key parents.
	 */
    public ShrimpNode getShrimpNode(Artifact artifact, List keyParentIDs, boolean createIfNotFound) {
        // TODO figure out how to find a particular node based on a list of parent ids
        // for now just return the first one that we find
    	ShrimpNode node = null;
    	// this will get all the nodes that this artifact represents
		Vector allNodes = getShrimpNodes(artifact, createIfNotFound);
        if (!allNodes.isEmpty()) {
        	node = (ShrimpNode) allNodes.firstElement();
        }
    	return node;
    }


	/**
	 * Returns all the ShrimpNodes associated with the given artifact
	 * Returns an empty vector if no ShrimpNodes found
	 * @param artifact The artifact to be converted.
	 * @param createIfNoneFound If true and no nodes found, all nodes that represent this artifact will
	 * 			be created and returned. Note: All ancestors of these new nodes will also be created.
	 */
	public Vector getShrimpNodes(Artifact artifact, boolean createIfNoneFound) {
		Vector nodes = (Vector) artifactIDToShrimpNodes.get(new Long(artifact.getID()));
		if (nodes == null && createIfNoneFound) {
			nodes = new Vector();
			String[] cprels = displayBean.getCprels();
			boolean inverted = displayBean.isInverted();
			Vector ancestorArts = inverted ? artifact.getDescendents(cprels) : artifact.getAncestors(cprels);
			if (ancestorArts.isEmpty()) {
				// this is a root artifact, so create a single node for this artifact with no parent node
				ShrimpNode rootNode = createShrimpNode(artifact, null);
				nodes.add(rootNode);
			} else {
				// this is not a root artifact, so find all paths from the roots to this artifact
				Vector allPaths = new Vector();
				Vector currentPath = new Vector();
				allPaths.add(currentPath);
				createPathsToRootRecursive(allPaths, currentPath, artifact);

				for (Iterator iterAllPaths = allPaths.iterator(); iterAllPaths.hasNext();) {
					Vector pathToRoot = (Vector) iterAllPaths.next();
					// go down path creating nodes until a node associated with the given artifact is created
					Vector rootNodes = getRootNodes();
					for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
                        ShrimpNode rootNode = (ShrimpNode) iter.next();
    					ShrimpNode currentParentNode = rootNode;
    					for (int j = pathToRoot.size() - 1; j > -1; j--) {
    						Vector childNodes = getChildNodes(currentParentNode, true);
    						// find the childNode that is associated with the next artifact in the path
    						Artifact nextChildArtInPath = (Artifact) pathToRoot.elementAt(j);
    						for (Iterator iterChildNodes = childNodes.iterator(); iterChildNodes.hasNext(); ) {
    							ShrimpNode childNode = (ShrimpNode) iterChildNodes.next();
    							if (childNode.getArtifact().equals(nextChildArtInPath)) {
    								currentParentNode = childNode; // this child node will become the next parent node
    								break;
    							}
    						}
    					}
                    }
				}
				return getShrimpNodes(artifact, false);
			}

		}
		return (nodes == null) ? new Vector(): (Vector)nodes.clone();
	}

	// recursively goes up the tree, creating a path to the root
	private void createPathsToRootRecursive(Vector allPaths, Vector currentPath, Artifact currentArtifact) {
	 	if (currentPath.contains(currentArtifact)) {
	 		return; //prevent infinite loops
	 	}
		 // add current artifact to current path
		 currentPath.add (currentArtifact);
		 String[] cprels = displayBean.getCprels();
		 boolean inverted = displayBean.isInverted();
		 Vector parents = inverted ? currentArtifact.getChildren(cprels) : currentArtifact.getParents(cprels);
		 if (parents.size() == 1) {
			 Artifact parent = (Artifact) parents.iterator().next();
			 createPathsToRootRecursive(allPaths, currentPath, parent);
		 } else if (parents.size() > 1) {
			 // create a new path for each parent
			 for (Iterator iter = parents.iterator(); iter.hasNext(); ) {
				 Artifact parent = (Artifact) iter.next();
				 // let the last parent be on the current path
				 if (!iter.hasNext()) {
					 createPathsToRootRecursive(allPaths, currentPath, parent);
				 } else {
					 Vector newPath = new Vector();
					 newPath.addAll(currentPath);
					 allPaths.add(newPath);
					 createPathsToRootRecursive(allPaths, newPath, parent);
				 }
			 }
		 }
		 // if here then there are no parents, so go no further (base case)
	 }

	/**
	 * Returns all the ShrimpNodes associated with the given vector of artifacts.
	 * Returns an empty vector if no ShrimpNodes found
	 * @param artifacts The artifacts to be converted.
	 * @param createIfNoneFound If true, nodes associated with the given artifact will be created.
	 */
    public Vector getShrimpNodes(Vector artifacts, boolean createIfNoneFound) {
    	Vector nodes = new Vector();
    	for (Iterator iter = artifacts.iterator(); iter.hasNext(); ) {
			Artifact art = (Artifact) iter.next();
			nodes.addAll(getShrimpNodes(art, createIfNoneFound));
		}
		return nodes;
    }

    /**
     * Returns a {@link Vector} of the immediate neighbouring nodes including the given node.
     * @param node the starting node
     * @return {@link Vector}
     */
    public Vector getNeighborhood(ShrimpNode node) {
    	return getNeighborhood(node, 1, 1);
    }

    /**
     * Returns a {@link Vector} of the neighbouring nodes including the given node.
     * @param node the starting node
     * @param outgoingLevels the number of arcs to traverse in the outgoing direction
     * @param incomingLevels the number of arcs to traverse in the incoming direction
     * @return {@link Vector}
     */
    public Vector getNeighborhood(ShrimpNode node, int outgoingLevels, int incomingLevels) {
    	HashSet nodes = new HashSet();
    	nodes.add(node);
    	traverseArcs(node, nodes, (outgoingLevels > 0), (incomingLevels > 0), outgoingLevels, incomingLevels, true);
    	return new Vector(nodes);
    }

    /**
     * Returns all the {@link ShrimpNode} objects connected to the given node (with visible arcs).
     * If BOTH outgoing and incoming are true it will return all visible nodes.
     * Here are the steps:
     * 1. add the node
     * 2. get all the arcs connected to the node
     * 3. traverse each arc that is outgoing or incoming (depending on those parameters)
     * 4. repeat for each node
     * @param node the node in question
	 * @param outgoing if outgoing arcs should be traversed
	 * @param incoming if incoming arcs should be traversed
     * @return a {@link Vector} of {@link ShrimpNode} including the given node
     */
    public Vector getShrimpNodeSubgraph(ShrimpNode node, boolean outgoing, boolean incoming) {
    	return getShrimpNodeSubgraph(node, outgoing, incoming, true);
    }

    /**
     * Returns all the {@link ShrimpNode} objects connected to the given node.
     * If BOTH outgoing and incoming are true it will return all visible nodes.
     * Here are the steps:
     * 1. add the node
     * 2. get all the arcs connected to the node
     * 3. traverse each arc that is outgoing or incoming (depending on those parameters)
     * 4. repeat for each node
     * @param node the node in question
	 * @param outgoing if outgoing arcs should be traversed
	 * @param incoming if incoming arcs should be traversed
	 * @param arcsMustBeVisible if false then invisible arcs will be traversed as well
     * @return a {@link Vector} of {@link ShrimpNode} including the given node
     */
    public Vector getShrimpNodeSubgraph(ShrimpNode node, boolean outgoing, boolean incoming, boolean arcsMustBeVisible) {
    	// instead of traversing the entire tree - just return visible nodes
    	if (outgoing && incoming) {
    		return getShrimpNodes();
    	} else {
    		HashSet nodes = new HashSet();
    		nodes.add(node);
    		traverseArcs(node, nodes, outgoing, incoming, -1, -1, arcsMustBeVisible);
    		return new Vector(nodes);
    	}
    }

	/**
	 * Traverses all the arcs connected to the given node.  The arcs can be invisible if arcsMustBeVisible is false.
	 * @param node the node in question
	 * @param nodes the nodes seen so far
	 * @param outgoing if outgoing arcs should be traversed
	 * @param incoming if incoming arcs should be traversed
	 * @param outgoingLevel the outgoing level, if zero then this method returns
	 * @param incomingLevel the incoming level, if zero then this method returns
	 * @param arcsMustBeVisible if false the invisible arcs are traversed too
	 */
	private void traverseArcs(ShrimpNode node, HashSet nodes, boolean outgoing, boolean incoming, int outgoingLevel, int incomingLevel, boolean arcsMustBeVisible) {
		if (outgoing && !incoming && (outgoingLevel == 0)) {
			return;
		}
		if (incoming && !outgoing && (incomingLevel == 0)) {
			return;
		}

		Vector arcs = getShrimpArcs(node, false, true);
		for (Iterator iter = arcs.iterator(); iter.hasNext(); ) {
			ShrimpArc arc = (ShrimpArc) iter.next();
			if (!arcsMustBeVisible || arc.isVisible()) {
				boolean isSrc = node.equals(arc.getSrcNode());
				ShrimpNode nextNode = (isSrc ? arc.getDestNode() : arc.getSrcNode());
				// recurse on the next node if we are following arcs of that direction
				boolean srcOK = isSrc && outgoing && (outgoingLevel != 0);
				boolean destOK = !isSrc && incoming && (incomingLevel != 0);
				if (!nodes.contains(nextNode) && (srcOK || destOK)) {
					nodes.add(nextNode);
					traverseArcs(nextNode, nodes, outgoing, incoming, outgoingLevel - 1, incomingLevel - 1, arcsMustBeVisible);
				}
			}
		}
	}

    /**
     * Adds a newly created relationship to the display, in other words,
     * add any new nodes or arcs to the display based on the new relationship
     * and the artifacts that it joins.
     * Note: There is no addArtifact method since an artifact/node
     * will only ever be in the display because it is involved in some relationship/arc.
     */
    public void addRelationship(Relationship relationship) {
        //TODO fully implement addRelationship
        //boolean isCprel = Arrays.asList(displayBean.getCprels()).contains(relationship);
        Vector artifacts = relationship.getArtifacts();
        if (artifacts.size() != 2) {
            System.err.println("DataDisplayBridge.addRelationship: can only handle relationships with 2 artifacts (a source and a destination) !");
            return;
        }
        Artifact srcArtifact = (Artifact)artifacts.elementAt(0);
        Artifact destArtifact = (Artifact)artifacts.elementAt(1);
        if (!inCurrentHierarchy(srcArtifact) || !inCurrentHierarchy(destArtifact)) {
            System.err.println("DataDisplayBridge.addRelationship: either the source or destination artifact is not in the current hierarchy");
            return; // if one of the artifacts is not in the current hierarchy this new rel would never show up
        }
        for (Iterator iter1 = artifacts.iterator(); iter1.hasNext();) {
            Artifact artifact = (Artifact) iter1.next();
            //Vector existingNodes = getShrimpNodes(artifact, false); //NOTE: the presence of existing nodes does not mean that all nodes have been created for this artifact yet.
            Vector parentArts = displayBean.isInverted() ? artifact.getChildren(displayBean.getCprels()) : artifact.getParents(displayBean.getCprels());
            for (Iterator iter2 = parentArts.iterator(); iter2.hasNext();) {
                Artifact parentArt = (Artifact) iter2.next();
                Vector existingParentNodes = getShrimpNodes(parentArt, false);
                for (Iterator iter3 = existingParentNodes.iterator(); iter3.hasNext();) {
                    ShrimpNode existingParentNode = (ShrimpNode) iter3.next();
                    //Vector existingChildNodes = getChildNodes(existingParentNode, false);
                    Set existingChildNodes = (Set) parentToChildrenMap.get(new Long(existingParentNode.getID()));

                    if (existingChildNodes != null) {
                        // see if this artifact is missing from the artifacts represented by the currently displayed child nodes
                        boolean missing = true;
                        for (Iterator iter4 = existingChildNodes.iterator(); iter4.hasNext() && missing;) {
                            ShrimpNode existingChildNode = (ShrimpNode) iter4.next();
                            Artifact childArt = existingChildNode.getArtifact();
                            if (childArt.equals(artifact)) {
                                missing = false;
                            }
                        }
                        if (missing) {
                            ShrimpNode newNode = createShrimpNode(artifact, existingParentNode);
    	                    if (existingParentNode.isVisible()) {
    	                        if (existingParentNode.getPanelMode().equals(PanelModeConstants.CHILDREN)) {
    	                            displayBean.addObject(newNode);
    	                            displayBean.setVisible(newNode, true, true);
    	                        }
    	                    }
                        }
                    }
                }
            }
        }
    }

    // an artifact should be in the currently displayed hierarchy if it has some ancestor that is a root node
    private boolean inCurrentHierarchy(Artifact artifact) {
        boolean inHierarchy = false;
        Vector rootNodes = getRootNodes();
        Vector ancestors = displayBean.isInverted() ? artifact.getDescendents(displayBean.getCprels()) : artifact.getAncestors(displayBean.getCprels());
        ancestors.add(artifact);
        for (Iterator iter1 = ancestors.iterator(); iter1.hasNext() && !inHierarchy;) {
            Artifact ancestorArt = (Artifact) iter1.next();
            for (Iterator iter2 = rootNodes.iterator(); iter2.hasNext() &&!inHierarchy;) {
                ShrimpNode rootNode = (ShrimpNode) iter2.next();
                inHierarchy = rootNode.getArtifact().equals(ancestorArt);
            }
        }
        return inHierarchy;
    }


	/**
	 * Creates and returns a ShrimpNode.
	 * @param artifact The artifact that the created node will represent.
	 * @param parentNode The parent node of the node to be created.
	 */
	private ShrimpNode createShrimpNode(Artifact artifact, ShrimpNode parentNode) {
		NodeShape nodeShape = loadNodeShape(artifact);
		String labelStyle = loadLabelStyle(artifact);
		Color color = loadNodeColorFromArtifact(artifact);
		Color outerBorderColor = loadNodeBorderColorFromArtifact(artifact,
				AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_COLOR,
				VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR);
		String outerBorderStyle = loadNodeBorderStyleFromArtifact(artifact,
				AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_STYLE,
				VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE);
		Color innerBorderColor = loadNodeBorderColorFromArtifact(artifact,
				AttributeConstants.NOM_ATTR_ARTIFACT_INNER_BORDER_COLOR,
				VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR);
		String innerBorderStyle = loadNodeBorderStyleFromArtifact(artifact,
				AttributeConstants.NOM_ATTR_ARTIFACT_INNER_BORDER_STYLE,
				VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE);
		NodeImage nodeImage = loadNodeImage(artifact);

	    final ShrimpNode node = new PShrimpNode(nodeShape, labelStyle, artifact, displayBean, nodeImage);
		// level of this node is one more than its parent's level
		int level = parentNode == null ? 1 : parentNode.getLevel() + 1;
		node.setLevel(level);
		node.setColor(color);
		node.setOuterBorderColor(nodeImage.isDrawOuterBorder() ? outerBorderColor : null);
		node.setOuterBorderStyle(outerBorderStyle);
		node.setInnerBorderColor(nodeImage.isDrawInnerBorder() ? innerBorderColor : null);
		node.setInnerBorderStyle(innerBorderStyle);
		node.setLabelFont((Font)displayBean.getLabelFont());
		node.setLabelMode(displayBean.getDefaultLabelMode());

		// @tag Shrimp.Interest : update transparency based on degree of interest (set as artifact attribute)
		updateNodeTransparencyBasedOnInterest(node, artifact);

		Icon icon = dataBean.getArtifactIcon(artifact);
		if (icon != null) {
		    node.setIcon(icon);
		    ShrimpNodeLabel label = getShrimpNodeLabel(node, false);
		    if (label != null) {
		    	label.setIcon(icon);
		    }
		}

		// add terminals, if any
		if (!(displayBean instanceof PFlatDisplayBean)) {
			Vector dataKeys = artifact.getAttributeNames();
			for (int i = 0; i < dataKeys.size(); i++) {
				Object key = dataKeys.get(i).toString();
				if (key instanceof String && ((String)key).startsWith("Terminal")) {
					String terminalId = (String)key;
					Map terminalProperties = (Map) artifact.getAttribute(terminalId);
					String terminalName = (String) terminalProperties.get(terminalId + "_Name");
					String terminalType = (String) terminalProperties.get(terminalId + "_Type");
					new PShrimpTerminal(displayBean, node, terminalName, terminalType, terminalId, true);
				}
			}
		}

		// add the appropriate customized panel listeners
		for (int i = 0; i < customizedPanelListeners.size(); i++) {
			CustomizedPanelActionListener listener = (CustomizedPanelActionListener) customizedPanelListeners.elementAt (i);
			artifact.addCustomizedPanelListener(listener);
		}

		boolean openable = (displayBean instanceof PFlatDisplayBean ? false : true);
		Boolean artifactOpenable = (Boolean) artifact.getAttribute(AttributeConstants.NOM_ATTR_OPENABLE);
		if (artifactOpenable != null) {
		    openable = artifactOpenable.booleanValue();
		}
		if (openable) {
			// only want to be able to open nodes that have children (flat graphs have no cprels)
			openable = !displayBean.isFlat() && (dataBean.getChildrenCount(artifact, displayBean.getCprels()) > 0);
		}
		node.setOpenable(openable);

		// only show the attachments icon if it is the ShrimpView
		boolean showAttachments = (tool instanceof ShrimpView);
		node.setShowAttachments(showAttachments);

		if (displayBean instanceof PFlatDisplayBean) {
			node.setResizable(false);
		}

		putShrimpNode(node);
		if (parentNode != null) {
		    associateChildParentShrimpNodes(node, parentNode);
		} else {
		    rootNodes.add(node);
		}

		// check if this node has any documents attached to it (loaded from the project properties)
		addDocumentsToArtifact(artifact);

		// check if this node has a special overlay icon provider
		loadOverlayIcon(node);

	    return node;
	}

	private NodeShape loadNodeShape(Artifact artifact) {
		Attribute nodeShapeAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_SHAPE);
		NodeShape nodeShape = ShrimpNode.DEFAULT_NODE_SHAPE;
		if (nodeShapeAttr != null) {
			Object attrValue = artifact.getAttribute(nodeShapeAttr.getName());
			if (attrValue != null) {
				nodeShape = (NodeShape) attrToVisVarBean.getVisVarValue(nodeShapeAttr.getName(), VisVarConstants.VIS_VAR_NODE_SHAPE, attrValue);
			}
		}
		return nodeShape;
	}


	/**
	 * Loads the label style from the artifact.  Defaults to {@link DisplayConstants#LABEL_STYLE_FULL}.
	 */
	private String loadLabelStyle(Artifact artifact) {
		String labelStyle = DisplayConstants.LABEL_STYLE_FULL;
		Attribute labelStyleAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_LABEL_STYLE);
		if (labelStyleAttr != null) {
			String name = labelStyleAttr.getName();
			Object attrValue = artifact.getAttribute(name);
			if (attrValue != null) {
				labelStyle = (String) attrToVisVarBean.getVisVarValue(name, VisVarConstants.VIS_VAR_LABEL_STYLE, attrValue);
			}
		}
		return labelStyle;
	}

	private NodeImage loadNodeImage(Artifact artifact) {
		NodeImage nodeImage = new NodeImage();
		Attribute nodeImageAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_IMAGE);
		if (nodeImageAttr != null) {
			String name = nodeImageAttr.getName();
			Object attribute = artifact.getAttribute(name);
			if (attribute != null) {
				String value = (String) attrToVisVarBean.getVisVarValue(name, VisVarConstants.VIS_VAR_NODE_IMAGE, attribute);
				if (value != null) {
					NodeImage.parseAndUpdate(nodeImage, value);	// copy the values into the node image
				}
			}
		}
		return nodeImage;
	}

	/**
	 * Gets the node color from the artifact attributes.  If the color attribute doesn't exist
	 * then it will return the default shrimp node color
	 * @see ShrimpNode#DEFAULT_NODE_COLOR
	 * @return Color
	 */
	public Color loadNodeColorFromArtifact(Artifact artifact) {
		return loadNodeColorFromArtifact(artifact, ShrimpNode.DEFAULT_NODE_COLOR);
	}

	/**
	 * Gets the node color from the artifact attributes.  If the color attribute doesn't exist
	 * then it will return the default node color.
	 * @return Color
	 */
	public Color loadNodeColorFromArtifact(Artifact artifact, Color defaultColor) {
		Color color = defaultColor;
		// @tag Shrimp.GXL_Colors : get color from artifact attribute [author = ccallendar; date = 16/03/06]
		// Check for the color attribute in Artifact (e.g. "#00ff00" or "green")
		if (artifact.hasAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_COLOR)) {
			String colorAttr = (String)artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_COLOR);
			color = GraphicsUtils.stringToColor(colorAttr, ShrimpNode.DEFAULT_NODE_COLOR);
		} else {
			// load colors based on node type
			Attribute colorAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_NODE_COLOR);
			if (colorAttr != null) {
			    Object attrValue = artifact.getAttribute(colorAttr.getName());
				if (attrValue != null) {
					color = (Color) attrToVisVarBean.getVisVarValue(colorAttr.getName(), VisVarConstants.VIS_VAR_NODE_COLOR, attrValue);
				}
			}
		}
		return color;
	}

	/**
	 * Gets the node border from the artifact attributes.  If the color attribute doesn't exist
	 * then it will return the default shrimp node border color
	 * @see NodeBorder#DEFAULT_UNHIGHLIGHT_COLOR
	 * @return Color
	 */
	public Color loadNodeBorderColorFromArtifact(Artifact artifact, String attributeName, String visVarConstant) {
		return loadNodeBorderColorFromArtifact(artifact, NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR, attributeName, visVarConstant);
	}

	/**
	 * Gets the node border color (inner or outer) from the artifact attributes.
	 * If the color attribute doesn't exist, it will return the default node color.
	 * @return Color
	 */
	public Color loadNodeBorderColorFromArtifact(Artifact artifact, Color defaultColor, String attributeName, String visVarConstant) {
		Color color = defaultColor;
		// @tag Shrimp.GXL_Colors : get border color from artifact attribute
		// Check for the border color attribute in Artifact (e.g. "#00ff00" or "green")
		if (artifact.hasAttribute(attributeName)) {
			String colorAttr = (String)artifact.getAttribute(attributeName);
			color = GraphicsUtils.stringToColor(colorAttr, NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR);
		} else {
			// load colors based on node type
			Attribute colorAttr = attrToVisVarBean.getMappedAttribute(visVarConstant);
			if (colorAttr != null) {
			    Object attrValue = artifact.getAttribute(colorAttr.getName());
				if (attrValue != null) {
					try {
					color = (Color) attrToVisVarBean.getVisVarValue(colorAttr.getName(), visVarConstant, attrValue);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return color;
	}


	/**
	 * Get the node border style (either inner or outer) from the artifact
	 * @param artifact
	 * @param attributeName
	 * @param visVarConstant
	 * @return
	 */
	private String loadNodeBorderStyleFromArtifact(Artifact artifact,
			String attributeName, String visVarConstant) {
		return loadNodeBorderStyleFromArtifact(artifact, NodeBorder.DEFAULT_BORDER_STYLE,
				attributeName, visVarConstant);
	}

	/**
	 * Get the node border style (either inner or outer) from the artifact
	 * @param artifact
	 * @param defaultStyle
	 * @param attributeName
	 * @param visVarConstant
	 * @return the node border style String
	 */
	public String loadNodeBorderStyleFromArtifact(Artifact artifact, String defaultStyle, String attributeName, String visVarConstant) {
		String style = defaultStyle;
		// Style override - check for the style attribute (e.g. "plain" or "dashed")
		if (artifact.hasAttribute(attributeName)) {
			style = (String)artifact.getAttribute(attributeName);
		} else {
			// load style based on node type
			Attribute styleAttr = attrToVisVarBean.getMappedAttribute(visVarConstant);
			if (styleAttr != null) {
			    Object attrValue = artifact.getAttribute(styleAttr.getName());
				if (attrValue != null) {
					style = (String)attrToVisVarBean.getVisVarValue(
							styleAttr.getName(), visVarConstant, attrValue);
				}
			}
		}
		return style;
	}


	/**
	 * Gets the arc color from the artifact attributes.  If the color attribute doesn't exist
	 * then it will return the default arc color.
	 * @param rel the relationship
	 * @return Color (defaults to the default arc color)
	 */
	public Color getArcColorFromRelationship(Relationship rel, AttrToVisVarBean attrToVisVarBean) {
		return getArcColorFromRelationship(rel, attrToVisVarBean, ShrimpArc.DEFAULT_ARC_COLOR);
	}

	/**
	 * Gets the arc color from the artifact attributes.  If the color attribute doesn't exist
	 * then it will return the default arc color.
	 * @param rel the relationship
	 * @return Color (defaults to the default color)
	 */
	public Color getArcColorFromRelationship(Relationship rel, AttrToVisVarBean attrToVisVarBean, Color defaultColor) {
		Color color = defaultColor;
		Object colorAttr = rel.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_COLOR);
		if (colorAttr != null) {
			color = GraphicsUtils.stringToColor((String)colorAttr, color);
		} else {
			color = (Color) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, rel.getAttribute(AttributeConstants.NOM_ATTR_REL_TYPE));
		}
		return color;
	}


	/**
	 * If the artifact contains the interest attribute, then the Shrimp node's (and label's) transparency are updated
	 * according to the interest value.  Uninteresting nodes are made partially transparent, interesting nodes are
	 * opaque, and landmarks have a bolded label.
	 * @tag Shrimp.Interest : update node and label transparency based on degree of interest (artifact attribute)
	 * @param node the node to update
	 * @param artifact the artifact possibly containing the interest and landmark attributes
	 * @return boolean if the node is interesting
	 */
	public boolean updateNodeTransparencyBasedOnInterest(ShrimpNode node, Artifact artifact) {
		ShrimpNodeLabel label = getShrimpNodeLabel(node, false);
		Font font = (label != null ? label.getFont() : null);
		boolean isInteresting = false;
		if (artifact.hasAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_INTERESTING)) {
			isInteresting = "true".equals(artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_INTERESTING));
			boolean isLandmark = "true".equals(artifact.getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LANDMARK));
			try {
				float zeroToOne = (isInteresting ? ShrimpNode.INTERESTING : ShrimpNode.NOT_INTERESTING);
				node.setTransparency(zeroToOne);
				if (label != null) {
					label.setTransparency(zeroToOne);
				}

				// bold the font for landmarks (if label is null then font is null)
				if (font != null) {
					if (isLandmark) {
						label.setFont(font.deriveFont(Font.BOLD));
					} else {
						label.setFont(font.deriveFont(Font.PLAIN));
					}
				}
			} catch (NumberFormatException ignore) {}
		} else {
			// reset the transparency and font
			node.setTransparency(1);
			if (label != null) {
				label.setTransparency(1);
			}
			if ((font != null) && (font.getStyle() != Font.PLAIN)) {
				label.setFont(font.deriveFont(Font.PLAIN));
			}
		}
		return isInteresting;
	}


    /**
     * Maps the given node's id to the given node
     * Maps the given node to the artifact that it represents.
     */
    private void putShrimpNode(ShrimpNode node) {
    	nodeIDToNodeMap.put(new Long(node.getID()), node);
		Artifact artifact = node.getArtifact();
		if (artifact != null) {
		    Long artID = new Long(node.getArtifact().getID());
			Vector nodes = (Vector) artifactIDToShrimpNodes.get(artID);
			if (nodes == null) {
				nodes = new Vector ();
				artifactIDToShrimpNodes.put(artID, nodes);
			}
			nodes.add(node);
            //System.out.println(nodes);
		}
    }

    private void associateChildParentShrimpNodes(ShrimpNode childNode, ShrimpNode parentNode) {
		// map from child node to all parent nodes
		Long childToParentsKey = new Long(childNode.getID());
		Set existingParents = (Set) childToParentsMap.get(childToParentsKey);
		if (existingParents == null) {
		    existingParents = new HashSet();
			childToParentsMap.put(childToParentsKey, existingParents);
		}
		existingParents.add(parentNode);

		// map from parent node to all child nodes
		Long parentToChildrenKey = new Long(parentNode.getID());
		Set existingChildren = (Set) parentToChildrenMap.get(parentToChildrenKey);
		if (existingChildren == null) {
		    existingChildren = new HashSet();
		    parentToChildrenMap.put(parentToChildrenKey, existingChildren);
		}
		existingChildren.add(childNode);
    }


	/**
	 * Returns all the ShrimpArcs associated with the given relationship
	 * Returns an empty array if no ShrimpArcs found.
	 *
	 * @param rel The relationship to be converted.
	 * @param createIfNoneFound If true, arcs that represent this relationship will be created and returned.
	 */
    public Vector getShrimpArcs(Relationship rel, boolean createIfNoneFound) {
    	Vector arcs = (Vector) relToShrimpArcs.get(rel);
    	if (arcs == null && createIfNoneFound) {
			Artifact srcArt = (Artifact) rel.getArtifacts().elementAt(0);
			Artifact destArt = (Artifact) rel.getArtifacts().elementAt(1);
			Vector srcNodes = getShrimpNodes (srcArt, true);
			Vector destNodes = getShrimpNodes (destArt, true);
			//add an arc between all of these nodes
			for (Iterator iter1 = srcNodes.iterator(); iter1.hasNext();) {
				ShrimpNode srcNode = (ShrimpNode) iter1.next();
				for (Iterator iter2 = destNodes.iterator(); iter2.hasNext();) {
					ShrimpNode destNode = (ShrimpNode) iter2.next();
					ShrimpArc arc = createShrimpArc(rel, srcNode, destNode);
					arcs.add(arc);
				}
			}
    	}
    	return (arcs == null) ? new Vector() : (Vector) arcs.clone();
    }

	/**
	 * Associates an arc with a relationship.
	 */
    private void putShrimpArc(Relationship rel, ShrimpArc arc) {
    	Vector arcs = (Vector) relToShrimpArcs.get(rel);
    	if (arcs == null) {
    		arcs = new Vector ();
    		relToShrimpArcs.put(rel, arcs);
    	}
    	arcs.add(arc);
    }

	/**
	 * Creates and returns a ShrimpArc.
	 * @param rel The relationship that the created arc will represent.
	 * @param srcNode The source node for the created arc.
	 * @param destNode The destination node for the created arc.
	 */
	ShrimpArc createShrimpArc(Relationship rel, ShrimpNode srcNode, ShrimpNode destNode) {
		ShrimpTerminal srcTerminal = null;
		ShrimpTerminal destTerminal = null;

		// see if this arc should attach to already created terminals
		// or if we should create new terminals for this arc
		String srcTerminalName = (String) rel.getAttribute(AttributeConstants.NOM_ATTR_SOURCE_TERMINAL_ID);
		if (srcTerminalName != null) {
			srcTerminal = srcNode.getTerminal(srcTerminalName);
		} else {
			srcTerminal = srcNode.getTerminal("src"); // for testing
		}
		if (srcTerminal == null) {
			//create an invisible terminal
			srcTerminal = new PShrimpTerminal (displayBean, srcNode, false);
		}
		final String destTerminalName = (String) rel.getAttribute(AttributeConstants.NOM_ATTR_TARGET_TERMINAL_ID);
		if (destTerminalName != null) {
			destTerminal = destNode.getTerminal(destTerminalName);
		} else {
			destTerminal = destNode.getTerminal("dest"); // for testing
		}
		if (destTerminal == null) {
			//create an invisible terminal
			destTerminal = new PShrimpTerminal(displayBean, destNode, false);
		}

		Attribute weightAttr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_ARC_WEIGHT);
		double weight = displayBean.getDefaultArcWeight();
		if (weightAttr != null) {
		    Object attrValue = rel.getAttribute(weightAttr.getName());
			if (attrValue != null) {
				Double weightDouble = (Double) attrToVisVarBean.getVisVarValue(weightAttr.getName(), VisVarConstants.VIS_VAR_ARC_WEIGHT, attrValue);
				if (weightDouble != null) {
					weight = weightDouble.doubleValue();
				}
			}
		}

		//TODO this is a hack to get proper line styles on arcs
		// need a better way to get saved line styles if there are any, or use default line styles
		ArcStyle arcStyle = displayBean.getArcStyle(StraightSolidLineArcStyle.NAME);
		if (weight > displayBean.getDefaultArcWeight()) {
			arcStyle = new CompositeArcStyle ();
		} else {
			ArcStyle mappedArcStyle = (ArcStyle) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, rel.getAttribute(AttributeConstants.NOM_ATTR_REL_TYPE));
			if (mappedArcStyle != null) {
				arcStyle = mappedArcStyle;
			}
		}

		ShrimpArc arc = new PShrimpArc (displayBean, rel, srcNode, destNode,
					srcTerminal, destTerminal, arcStyle, 0,
					displayBean.getUsingArrowHeads(), getArrowHeadStyle(rel), weight);
		// @tag Shrimp.GXL_Colors : arc colors [author = ccallendar; date = 16/03/06]
		Color color = getArcColorFromRelationship(rel, attrToVisVarBean);
		arc.setColor(color);
		arc.setActive(false);
		if (CompositeArcStyle.COMPOSITE_STYLE.equals(arc.getStyle().getName())) {
			arc.setTransparency(DisplayConstants.DEFAULT_COMPOSITE_ARC_STYLE_TRANSPARENCY);
		}
		putShrimpArc(srcNode, arc);
		putShrimpArc(destNode, arc);
		putShrimpArc(srcNode, destNode, arc);
		putShrimpArc(rel, arc);

		createdArcs.add(arc);

		return arc;
	}

	/**
	 * Returns the ShrimpLabel associated with the given node.
	 * Returns null if no ShrimpLabel found.
	 * @param node The node to be converted.
	 */
    public ShrimpNodeLabel getShrimpNodeLabel(ShrimpNode node, boolean createIfNotFound) {
    	ShrimpNodeLabel shrimpNodeLabel = (ShrimpNodeLabel) nodeIDToShrimpNodeLabel.get(new Long(node.getID()));
    	if (shrimpNodeLabel == null && createIfNotFound) {
    	    shrimpNodeLabel = createShrimpNodeLabel(node);
    	    if (node.getIcon() != null) {
    	    	shrimpNodeLabel.setIcon(node.getIcon());
    	    }
    	}
    	return shrimpNodeLabel;
    }

	/**
	 * Returns the ShrimpLabel associated with the given arc.
	 * Returns null if no ShrimpLabel found.
	 * @param arc The arc to be converted.
	 */
    public ShrimpArcLabel getShrimpArcLabel(ShrimpArc arc, boolean createIfNotFound) {
    	ShrimpArcLabel shrimpArcLabel = (ShrimpArcLabel) arcIDToShrimpArcLabel.get(new Long(arc.getId()));
    	if (shrimpArcLabel == null && createIfNotFound) {
    	    shrimpArcLabel = createShrimpArcLabel(arc);
    	}
    	return shrimpArcLabel;
    }


	/**
	 * Returns all the ShrimpNodeLabels
	 * Returns an empty vector if no ShrimpNodeLabels found
	 */
    public Vector getShrimpLabels() {
		Vector labels = new Vector(nodeIDToShrimpNodeLabel.size() + arcIDToShrimpArcLabel.size(), 10);
		labels.addAll(nodeIDToShrimpNodeLabel.values());
		labels.addAll(arcIDToShrimpArcLabel.values());
		return labels;
    }

	/**
	 * Returns all the ShrimpNodeLabels
	 * Returns an empty vector if no ShrimpNodeLabels found
	 */
    public Vector getShrimpNodeLabels() {
		Vector labels = new Vector(nodeIDToShrimpNodeLabel.size(), 10);
		labels.addAll(nodeIDToShrimpNodeLabel.values());
		return labels;
    }

	/**
	 * Returns all the ShrimpArcLabels
	 * Returns an empty vector if no ShrimpArcLabels found
	 */
    public Vector getShrimpArcLabels() {
		Vector labels = new Vector(arcIDToShrimpArcLabel.size(), 10);
		labels.addAll(arcIDToShrimpArcLabel.values());
		return labels;
    }

	/**
	 * Creates and returns a ShrimpLabel.
	 */
	private ShrimpNodeLabel createShrimpNodeLabel(ShrimpNode node) {
    	ShrimpNodeLabel label = new PShrimpNodeLabel(displayBean, node, (Font)displayBean.getLabelFont(), node.getName());
		label.setBackgroundOpaque(displayBean.getLabelBackgroundOpaque());

    	// see if there is an icon for this node
		Icon icon = node.getIcon();
		if (icon != null) {
		    label.setIcon(icon);
		}

    	putShrimpNodeLabel(node, label);
    	return label;
	}

	private ShrimpArcLabel createShrimpArcLabel(ShrimpArc arc) {
	    String text = "";
		if (arc instanceof ShrimpCompositeArc && ((ShrimpCompositeArc)arc).isAtHighLevel()) {
		    text += "composite (" + ((ShrimpCompositeArc)arc).getArcCount() + ")";
		} else {
		    Relationship rel = arc.getRelationship();
			String shortText = rel == null ? null : (String) rel.getAttribute(AttributeConstants.NOM_ATTR_REL_SHORT_DISPLAY_TEXT);
			if (shortText == null || shortText.equals("")) {
				String arcType = rel == null ? "no type!" : arc.getRelationship().getType();
			    text = arcType;
			} else {
			    text = shortText;
			}
		}
	    ShrimpArcLabel label = new PShrimpArcLabel (displayBean, arc, (Font)displayBean.getLabelFont(), text);
	   	putShrimpArcLabel (arc, label);
	    return label;
	}

    /**
     * Associates a ShrimpNodeLabel with a particular node
     */
    private void putShrimpNodeLabel(ShrimpNode node, ShrimpNodeLabel snl) {
    	nodeIDToShrimpNodeLabel.put(new Long(node.getID()), snl);
    }

    /**
     * Associates a ShrimpArcLabel with a particular node
     */
    private void putShrimpArcLabel(ShrimpArc arc, ShrimpArcLabel sal) {
    	arcIDToShrimpArcLabel.put(new Long(arc.getId()), sal);
    }

	/**
	 * Adds a customized panel action listeners to each artifact that has a corresponding node in the display
	 * @param customizedPanelActionListener The objects that will handle the action for the panel
	 */
	public void addCustomizedPanelListeners(Vector customizedPanelActionListener) {
		// add to list of listeners
		customizedPanelListeners.addAll(customizedPanelActionListener);

		// get the ids of the artifacts
		for (Iterator iter = artifactIDToShrimpNodes.keySet().iterator(); iter.hasNext();) {
            Long id = (Long) iter.next();
			Artifact artifact = dataBean.getArtifact(id.longValue());
			if(artifact != null) {
				Iterator iterator = customizedPanelActionListener.iterator();
				while(iterator.hasNext()) {
					artifact.addCustomizedPanelListener((CustomizedPanelActionListener) iterator.next());
				}
			}
		}
	}

	/**
	 * Removes a listener for a given customized panel
	 * @param customizedPanelActionListener The object that will handle action for the panel
	 */
	public void removeCustomizedPanelListeners(Vector customizedPanelActionListener) {
		// remove from list of listeners
		customizedPanelListeners.removeAll(customizedPanelActionListener);

		// get the ids of the artifacts
		for (Iterator iter = artifactIDToShrimpNodes.keySet().iterator(); iter.hasNext();) {
            Long id = (Long) iter.next();
			Artifact artifact = dataBean.getArtifact(id.longValue());

			if(artifact != null) {
				Iterator iterator = customizedPanelActionListener.iterator();
				while(iterator.hasNext()) {
					artifact.removeCustomizedPanelListener((CustomizedPanelActionListener) iterator.next());
				}
			}
		}
	}

	/** Returns the customized panel listeners that have been added to this bridge.
	 */
	public Vector getCustomizedPanelListeners() {
		return (Vector)customizedPanelListeners.clone();
	}

	/**
	 * Returns the existing arcs connected to the given node.
	 * Use <code>getShrimpArcs (node, true, false)</code> to create and return all possible arcs for the given node
	 * @param node
	 * @return A Vector of ShrimpArc objects
	 */
	public Vector getShrimpArcs(ShrimpNode node) {
		return getShrimpArcs(node, false, true);
	}

	/**
	 * Returns the arcs associated with a particular node.
	 * Returns an empty vector if no arcs found.
	 * @param node The node to get the incoming and outgoing arcs of
	 * @param createIfNoneFound Whether or not to attempt to create new arcs for this node if none exist already.
	 * @param useExistingOnly Whether or not to just return the arcs for node, or to try to create all arcs for this node.
	 * @return A vector of ShrimpArcs objects.
	 */
	public Vector getShrimpArcs(ShrimpNode node, boolean createIfNoneFound, boolean useExistingOnly) {
		Set allArcsSet = new HashSet();

		//add existing arcs for this node
		Vector existingArcs = (Vector) shrimpNodeToShrimpArcs.get(node);
		if (existingArcs != null) {
			allArcsSet.addAll(existingArcs);
		}

		// add existing composite arcs for this node
		Collection compositeArcs = compositeArcsManager.getCompositeArcs(node);
		allArcsSet.addAll(compositeArcs);

		// if want more than existing, create new arcs for this node and add them
		if (!useExistingOnly || existingArcs == null && createIfNoneFound) {
			Vector newArcs = createArcs(node);
			allArcsSet.addAll(newArcs);
		}

		return new Vector(allArcsSet);
	}

	public Vector getShrimpArcs(ShrimpNode srcNode, ShrimpNode destNode) {
		Vector retArcs = new Vector();

		String key = "" + srcNode.getID() + destNode.getID();
		Vector arcs = (Vector) srcNodeDestNodeToShrimpArcs.get(key);
		if (arcs != null) {
			retArcs.addAll(arcs);
		}

		retArcs.addAll(compositeArcsManager.getCompositeArcs(srcNode, destNode));

		return retArcs;
	}

	/**
	 * Returns all arcs that have been created.
	 * Returns an empty vector if no arcs found.
	 */
	public Vector getShrimpArcs() {
		Vector arcsClone = (Vector)createdArcs.clone();
		arcsClone.addAll(compositeArcsManager.getCompositeArcs());

		return arcsClone;
	}

	/**
	 * Delete the arc from node relationship maps
	 * @param arc
	 */
	public void delShrimpArcFromMaps(ShrimpArc arc) {
		Vector arcs = (Vector) shrimpNodeToShrimpArcs.get(arc.getSrcNode());
		if (arcs != null && arcs.contains(arc)) {
			arcs.remove(arc);
		}
		arcs = (Vector) shrimpNodeToShrimpArcs.get(arc.getDestNode());
		if (arcs != null && arcs.contains(arc)) {
			arcs.remove(arc);
		}
		String key = "" + arc.getSrcNode().getID() + arc.getDestNode().getID();
		arcs = (Vector) srcNodeDestNodeToShrimpArcs.get(key);
		if (arcs != null && arcs.contains(arc)) {
			arcs.remove(arc);
		}
	}

	/**
	 * Add the arc to node relationship maps
	 * @param arc
	 */
	public void addShrimpArcToMaps(ShrimpArc arc) {
		putShrimpArc (arc.getSrcNode(), arc);
		putShrimpArc (arc.getDestNode(), arc);
		putShrimpArc (arc.getSrcNode(), arc.getDestNode(), arc);
	}


	/**
	 * Associates the given node and arc.
	 */
	private void putShrimpArc(ShrimpNode node, ShrimpArc arc) {
		Vector arcs = (Vector) shrimpNodeToShrimpArcs.get(node);
		if (arcs == null) {
			arcs = new Vector();
			shrimpNodeToShrimpArcs.put(node, arcs);
		}
		if (!arcs.contains(arc)) {
			arcs.add(arc);
		}
	}

	/**
	 * Associates the given src node and dest node with the given arc.
	 */
	private void putShrimpArc(ShrimpNode srcNode, ShrimpNode destNode, ShrimpArc arc) {
		String key = "" + srcNode.getID() + destNode.getID();
		Vector arcs = (Vector) srcNodeDestNodeToShrimpArcs.get(key);
		if (arcs == null) {
			arcs = new Vector ();
			srcNodeDestNodeToShrimpArcs.put(key, arcs);
		}
		if (!arcs.contains(arc)) {
			arcs.add(arc);
		}
	}

	/**
	 * Returns the root nodes in the display. These are nodes that have no parents.
	 * Most likely, these will correspond the root artifacts in the dataBean.
	 */
	public Vector getRootNodes() {
	    return new Vector(rootNodes);
	}

	/**
	 *
	 * @param node
	 * @return The number of child nodes (visible and invisible) that the given node will have.
	 */
	public int getChildNodesCount(ShrimpNode node) {
		int numChildren = 0;
		String [] cprels = displayBean.getCprels();
		boolean inverted = displayBean.isInverted();
		if (node.getArtifact() != null) {
			if (!inverted) {
		        numChildren = node.getArtifact().getChildrenCount(cprels);
		    } else {
		        numChildren = node.getArtifact().getParentsCount(cprels);
		    }
		}
		return numChildren;
	}

	/**
	 *
	 * @param node
	 * @return The number of visible/unfiltered children that the given node will have.
	 */
	public int getVisibleChildNodeCount(ShrimpNode node) {
		return getChildNodeCount(node, false);
	}

	/**
	 *
	 * @param node
	 * @return The number of invisible/filtered children that the given node will have
	 */
	public int getInvisibleChildNodeCount(ShrimpNode node) {
		return getChildNodeCount(node, true);
	}

	/**
	 *
	 * @param node
	 * @param countFiltered
	 */
	private int getChildNodeCount(ShrimpNode node, boolean countFiltered) {
		int numChildren = 0;
		String [] cprels = displayBean.getCprels();
		boolean inverted = displayBean.isInverted();
		Vector children = inverted ? node.getArtifact().getParents(cprels) : node.getArtifact().getChildren(cprels);
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Artifact childArt = (Artifact) iter.next();
			if (displayBean.isFiltered(childArt)) {
				if (countFiltered) {
					numChildren++;
				}
			} else {
				if (!countFiltered) {
					numChildren++;
				}
			}
		}
		return numChildren;
	}

	/*
	private ShrimpNode createRootNode () {
		final ShrimpNode rootNode = new PShrimpNode (displayBean.getNodeShape(RectangleNodeShape.RECTANGLE_NODE_SHAPE), null, displayBean, null, null);
		rootNode.setID(createRootNodeID());
		rootNode.setColor(DisplayConstants.DEFAULT_ROOT_COLOUR);
		rootNode.setName(DisplayConstants.ROOT_NAME);
		int level = 0;
		rootNode.setLevel(level);
		rootNode.setLabelFont(displayBean.getLabelFont());
		rootNode.setLabelMode(displayBean.getDefaultLabelMode());
		if (displayBean instanceof PFlatDisplayBean) {
			rootNode.setResizable(false);
		}
		setRootNodeSize(false);
		nodeIDToNodeMap.put(rootNode.getID(), rootNode);

		return rootNode;
	}
	*/

	/**
	 *
	 * @param rootNode
	 * @param fixDescendents
	 */
	/*
	public void setRootNodesSize (boolean fixDescendents) {
		//Set the size of the root node to be the same size as the canvas
		double displayWidth = displayBean.getCanvasDimension().width;
		double displayHeight = displayBean.getCanvasDimension().height;

		double rootWidth = ShrimpNode.DEFAULT_NODE_DIMENSION;
		double rootHeight = ShrimpNode.DEFAULT_NODE_DIMENSION;
		if (displayWidth != 0 && displayHeight != 0) {
			// make sure the root is not too tall or too wide
			double whRatio = displayWidth/displayHeight; //Math.max(0.5, );
			if (whRatio < 1.0) {
				whRatio = Math.max(ShrimpNode.MIN_NODE_WH_RATIO, whRatio); // make sure not too tall
			} else {
				whRatio = Math.min(ShrimpNode.MAX_NODE_WH_RATIO, whRatio); // make sure not too wide
			}
			rootWidth = rootHeight * whRatio;
		}
		if (fixDescendents) {
			Vector objects = new Vector (1);
			objects.add(rootNode);
			Vector positions = new Vector (1);
			positions.add(new Point2D.Double(rootWidth/2.0, rootHeight/2.0));
			Vector sizes = new Vector (1);
			sizes.add(new DoubleDimension (rootWidth, rootHeight));
			displayBean.setPositionsAndSizes(objects, positions, sizes, false);
		} else {
			rootNode.setOuterBounds(new Rectangle2D.Double (0,0,rootWidth, rootHeight));
		}
		double padding = 10;
		Rectangle2D.Double bounds = new Rectangle2D.Double (-padding, -padding, rootWidth+2*padding, rootHeight+2*padding);
		((PNestedDisplayBean)displayBean).getPCanvas().getCamera().animateViewToCenterBounds(bounds, true, 0);
	}

	private String createRootNodeID () {
		String rootID = DisplayConstants.ROOT_ID;
		return rootID;
	}
	*/

	/**
	 * Creates and returns the children of the given node
	 */
	private Vector createChildNodes(ShrimpNode parentNode) {
		Vector childNodes = new Vector();
		Set childArtifacts = new HashSet();
		Artifact parentArtifact = parentNode.getArtifact();
		String [] cprels = displayBean.getCprels();
		boolean inverted = displayBean.isInverted();
	    if (!inverted) {
	        childArtifacts.addAll(parentArtifact.getChildren(cprels));
	    } else {
	        childArtifacts.addAll(parentArtifact.getParents(cprels));
	    }
		for (Iterator iter = childArtifacts.iterator(); iter.hasNext();) {
			Artifact childArtifact = (Artifact) iter.next();
			boolean createNewChildNode = true;
			if (displayBean instanceof PFlatDisplayBean) {
				// look for any nodes that exist for this artifact already
				// we don't want duplicate nodes in the hierarchical view
				Vector existingChildNodes = getShrimpNodes(childArtifact, false);
				if (!existingChildNodes.isEmpty()) {
					childNodes.addAll(existingChildNodes);
					createNewChildNode = false;
				}
			}
			if (createNewChildNode) {
				ShrimpNode childNode = createShrimpNode(childArtifact, parentNode);
				childNodes.add(childNode);
//				long id = childNode.getArtifact().getID().toString();
//				if (parentNode != null) {
//					ID += " " + parentNode.getID();
//				}
//				childNode.setID(ID);
			}
		}
		Collections.sort(childNodes);
		return (Vector) childNodes.clone();
	}

	/**
	 * Returns the parents of the given node
	 * Returns an empty vector if no parents found for a node.
	 */
	public Vector getParentShrimpNodes(ShrimpNode childNode) {
		Vector parents = new Vector();
		Long childToParentsKey = new Long(childNode.getID()); // + childNode.getRelTypeToParents() + childNode.isRelTypeToParentsInverted();
		Set existingParents = (HashSet) childToParentsMap.get(childToParentsKey);
		if (existingParents != null) {
			for (Iterator iterator = existingParents.iterator(); iterator.hasNext();) {
				parents.add(iterator.next());
			}
		}
		return parents;
	}

	/**
	 * Returns the already created child nodes of the given parent.
	 * An empty vector is returned if no child nodes found.
	 *
	 * Use <code>getChildNodes (parentNode, true)</code> to create new child nodes
	 */
	public Vector getChildNodes(ShrimpNode parentNode) {
		return getChildNodes(parentNode, false);
	}

	/**
	 * Returns the child nodes of the given parent.
	 * If createIfNoneFound is true, new children will be created and returned.
	 * An empty vector is returned if no child nodes found
	 */
	public Vector getChildNodes(ShrimpNode parentNode, boolean createIfNoneFound) {
	   // System.out.println("getting child nodes of " + parentNode + parentNode.getLevel());
		Vector childNodes = null;
		if (parentNode == null) {
			(new Exception ()).printStackTrace();
		} else {
			Long parentToChildrenKey = new Long(parentNode.getID());
			Set existingChildNodes = (Set) parentToChildrenMap.get(parentToChildrenKey);
			if (existingChildNodes == null) {
			    if (createIfNoneFound) {
					childNodes = createChildNodes(parentNode);
			    }
			} else {
			    childNodes = new Vector(existingChildNodes);
			}
		}
		return (childNodes == null ? new Vector(): (Vector) childNodes.clone());
	}

	/**
	 * Creates and returns the arcs that will be attached to the given node.
	 */
	private Vector createArcs(ShrimpNode node) {
		Set createdArcs = new HashSet();
		Vector rels = node.getArtifact().getRelationships();
		List cprelsList = Arrays.asList(displayBean.getCprels());

		for (Iterator iter = rels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			if (!(displayBean instanceof PFlatDisplayBean) && displayBean instanceof PNestedDisplayBean) {
				// dont bother adding rels of same type as display's cprel since they will be filtered out anyway
				if (cprelsList.contains(rel.getType())) {
					continue;
				}

				// check if this type has a composite type
				RelTypeGroup relTypeGroup = compositeArcsManager.getRelTypeGroupForType(rel.getType());
				if(relTypeGroup.areCompositesEnabled()){
					continue;
				}
			} else {
				// hierarchical display only uses cprel arcs
				if (!cprelsList.contains(rel.getType())) {
					continue;
				}
			}

			Vector artifacts = rel.getArtifacts();
			Artifact srcArt = (Artifact) artifacts.elementAt(0);
			Artifact destArt = (Artifact) artifacts.elementAt(1);
			if (srcArt.equals(node.getArtifact())) {
				// the passed in node represents the artifact that is the source, or "parent", of this relationship
				ShrimpNode srcNode = node;
				// create arcs to any destination nodes that have been created already
				Vector existingDestNodes = getShrimpNodes (destArt, false);
				for (Iterator iterator = existingDestNodes.iterator(); iterator.hasNext(); ) {
					ShrimpNode destNode = (ShrimpNode) iterator.next();
					ShrimpArc arc = createNewArc (rel, srcNode, destNode);
					if (arc != null) {
						createdArcs.add(arc);
					}
				}
			} else if (destArt.equals(node.getArtifact())) {
				// the passed in node represents the artifact that is the destination, or "child", of this relationship
				ShrimpNode destNode = node;
				// create arcs to any existing src nodes, but only if these arcs have not been created yet
				Vector existingSrcNodes = getShrimpNodes (srcArt, false);
				for (Iterator iterator = existingSrcNodes.iterator(); iterator.hasNext(); ) {
					ShrimpNode srcNode = (ShrimpNode) iterator.next();
					ShrimpArc arc = createNewArc (rel, srcNode, destNode);
					if (arc != null) {
						createdArcs.add(arc);
					}
				}
			}
		}
		return new Vector (createdArcs);
	}

	private ShrimpArc createNewArc(Relationship rel, ShrimpNode srcNode, ShrimpNode destNode) {
		ShrimpArc newArc = null;
		Vector existingSrcDestArcs = getShrimpArcs (srcNode, destNode);
		boolean arcExists = false;
		for (Iterator existingArcIterator = existingSrcDestArcs.iterator(); existingArcIterator.hasNext() && !arcExists;) {
			ShrimpArc existingArc = (ShrimpArc) existingArcIterator.next();
			arcExists = existingArc.getRelationship().equals(rel);
		}
		if (!arcExists) {
			newArc = createShrimpArc(rel, srcNode, destNode);
		}
		return newArc;
	}

	/**
	 * Returns all the descendents of the given node.
	 * @param node The node to find the descendents of.
	 * @param createDescendentsIfNoneFound If true, creates descendent nodes if none found.
	 */
	public Vector getDescendentNodes(ShrimpNode node, boolean createDescendentsIfNoneFound) {
		return getDescendentNodes(node, createDescendentsIfNoneFound, false, false);
	}

	/**
	 * Returns all the descendents of the given node. These will be returned in a depth-first order.
	 * @param node The node to find the descendents of.
	 * @param createDescendentsIfNoneFound If true, creates descendent nodes if none found.
	 * @param markNodes Whether or not to mark these descendents.
	 * @param markValue If markNodes is true, marks these descendents with the value of markValue.
	 */
	public Vector getDescendentNodes(ShrimpNode node, boolean createDescendentsIfNoneFound,
									 boolean markNodes, boolean markValue) {
		return getDescendentNodes(node, createDescendentsIfNoneFound, markNodes, markValue, false, -1);
	}

	/**
	 * @see #getDescendentNodes(ShrimpNode, boolean)
	 * @return a Vector of the ShrimpNodes in tree fashion to depth specified
	 */
	public Vector getDescendentNodes(ShrimpNode node, boolean createDescendentsIfNoneFound, boolean markNodes, boolean markValue, boolean restrictLevels, int numLevels)	{
	    if (restrictLevels && numLevels < 1) {
	        throw new IllegalArgumentException("levels must be greater than zero");
	    }
		Set descendents = new HashSet();
		getDescendentNodesRecursive(restrictLevels, numLevels, 1, node, new HashSet(), descendents, createDescendentsIfNoneFound, markNodes, markValue);
		return new Vector(descendents);
	}

	/**
	 *
	 * @param restrictLevels
	 * @param levels
	 * @param currDepth
	 * @param node
	 * @param nodesSeenSoFar
	 * @param descendents
	 * @param createDescendentsIfNoneFound
	 * @param markNodes
	 * @param markValue
	 */
	private void getDescendentNodesRecursive(boolean restrictLevels, int numLevels, int currLevel, ShrimpNode node, Set nodesSeenSoFar, Set descendents, boolean createDescendentsIfNoneFound, boolean markNodes, boolean markValue) {
		if (nodesSeenSoFar.contains(node)) {
			return ; // to prevent infinite loops
		}
		if (restrictLevels && currLevel > numLevels) {
		    return;
		}
		nodesSeenSoFar.add(node);
		Vector childNodes = getChildNodes(node, createDescendentsIfNoneFound);
		for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
			ShrimpNode child = (ShrimpNode) iter.next();
			descendents.add(child);
			if (markNodes) {
				child.setMarked(markValue);
			}
			getDescendentNodesRecursive(restrictLevels, numLevels, currLevel + 1, child, nodesSeenSoFar, descendents, createDescendentsIfNoneFound, markNodes, markValue);
		}
	}

	/**
	 * Returns all the ancestors of the given node.
	 */
	public Vector getAncestorNodes (ShrimpNode node) {
	    return getAncestorNodes(node, false);
	}

	private Vector getAncestorNodes (ShrimpNode node, boolean useOneParentOnly) {
	    Vector ancestors = new Vector();
	    getAncestorNodesRecursive(node, ancestors, useOneParentOnly);
	    return ancestors;
	}

	private void getAncestorNodesRecursive (ShrimpNode node, Vector ancestors, boolean useOneParentOnly) {
		Vector parents = getParentShrimpNodes(node);
		for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
			ShrimpNode parent = (ShrimpNode) iterator.next();
			if (!ancestors.contains(parent)) {
			    ancestors.add(parent);
			    getAncestorNodesRecursive (parent, ancestors, useOneParentOnly);
			    if (useOneParentOnly) {
			        break;
			    }
			}
		}
	}

	/**
	 * Returns the first path found from the given node to its top-level ancestor (a root node).
	 * @param node
	 */
	public Vector getFirstPathToRoot (ShrimpNode node) {
	    return getAncestorNodes(node, true);
	}

	public boolean isAncestorFiltered (Artifact artifact) {
		Set ancestors = new HashSet ();
		String [] cprels = displayBean.getCprels();
		boolean inverted = displayBean.isInverted();
		if (!inverted) {
		    ancestors.addAll(artifact.getAncestors(cprels));
		} else {
		    ancestors.addAll(artifact.getDescendents(cprels));
		}
        boolean ancestorFiltered = false;
        for (Iterator iter = ancestors.iterator(); iter.hasNext() && !ancestorFiltered;) {
            Artifact ancestor = (Artifact) iter.next();
            ancestorFiltered = displayBean.isFiltered(ancestor);
        }
		return ancestorFiltered;
	}


	public CompositeArcsManager getCompositeArcsManager() {
		return compositeArcsManager;
	}


	public String getArrowHeadStyle(ShrimpArc arc) {
		return  getArrowHeadStyle(arc.getRelationship());
	}

	public String getArrowHeadStyle(Relationship rel) {
		String style = null;
		String type = rel.getType();
		if (type != null) {
			style = (String) attrToVisVarBean.getVisVarValue(
					AttributeConstants.NOM_ATTR_REL_TYPE,
					VisVarConstants.VIS_VAR_ARC_ARROW_HEAD_STYLE, type);
		}
		if (style == null) {
			style = ArrowHead.DEFAULT_STYLE;
		}
		return style;
	}

	/**
	 * Return the annotation string for the specified node from the properties file
	 * @param artifact
	 */
	public String getAnnotationProperty(Artifact artifact) {
		Properties properties = tool.getProject().getProperties();
		return AnnotationPanelActionAdapter.getArtifactAnnotation(properties, artifact.getExternalIdString());
	}

	/**
	 * Load the documents from the properties file for the given artifact.
	 */
	private void addDocumentsToArtifact(Artifact artifact) {
		Properties properties = tool.getProject().getProperties();
		DocumentsPanelActionAdapter.loadDocuments(properties, artifact);
	}

	private void loadOverlayIcon(ShrimpNode node) {
		Properties properties = tool.getProject().getProperties();
		ChangeNodeOverlayIconAdapter.loadNodeOverlayIcon(node, properties);
	}

}