/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataDisplayBridge;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.CompositeRelationship;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CompositeArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpTerminal;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectAdapter;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.XMLSerializerUtil;
import edu.umd.cs.piccolo.PInputManager;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class manages composite, or "high-level" arcs.
 * It is not totally clear how all this code works (now that Nasir is gone) but
 * here are some notes:
 * <p>
 * TODO - write some comments on composite arcs, add constants for attributes!
 *
 * @author Nasir Rather, Rob Lintern, Chris Callendar
 * @see ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup
 */
public class CompositeArcsManager {

    public static final String REL_TYPE_GROUPS_KEY = "DataDisplayBridge::relTypeGroups";
	public static final String COMPOSITE_RELATIONSHIPS_KEY = "DataDisplayBridge::compositeRelationships";

	private Map compositeArcToCompositeRelsMap = new HashMap();
	private Map topSrcIdToCompositeRelsMap = new HashMap();

	//Map<ShrimpNode (src), Map<ShrimpNode (dest), Map<String (composite type), ShrimpCompositeArc>>>
	private Map srcNodeToDestToCompositeTypeToCompositeArcMap = new HashMap();
	//Map<ShrimpNode (dest), Map<ShrimpNode (src), Map<String (composite type), ShrimpCompositeArc>>>
	private Map destNodeToSrcToCompositeTypeToCompositeArcMap = new HashMap();

	private Map relTypeToRelTypeGroupNameMap = new HashMap();
	private Map relTypeGroupNameToRelTypeGroupMap = new HashMap();
	private Map relTypeToCompositeRelsMap = new HashMap();

	private DataBean dataBean;
	private DisplayBean displayBean;
	private DataDisplayBridge dataDisplayBridge;
	private AttrToVisVarBean attrToVisVarBean;
	private ShrimpProject project;

	private ShrimpProjectAdapter projectListener = new ShrimpProjectAdapter() {
		public void projectClosing(ShrimpProjectEvent event) {
			save(event.getProject());
		}
	};

	void init(ShrimpProject project, DataBean dataBean, DisplayBean displayBean, AttrToVisVarBean attrToVisVarBean) {
		this.project = project;
		this.dataBean = dataBean;
		this.displayBean = displayBean;
		this.dataDisplayBridge = displayBean.getDataDisplayBridge();
		this.attrToVisVarBean = attrToVisVarBean;

		// load the rel type groups saved previously
		load();
		// quick fix, we want to make sure that the advanced filter palette refreshes with these newly loaded rel type groups
		try {
			ApplicationAccessor.getApplication().getTool(ShrimpApplication.ARC_FILTER).refresh();
		} catch (ShrimpToolNotFoundException e) {
			// do nothing
		}

		// make sure that rel type groups are saved when project is closed
		project.addProjectListener(projectListener);
	}

	public void clear() {
		compositeArcToCompositeRelsMap.clear();
		topSrcIdToCompositeRelsMap.clear();
		srcNodeToDestToCompositeTypeToCompositeArcMap.clear();
		destNodeToSrcToCompositeTypeToCompositeArcMap.clear();
		relTypeToRelTypeGroupNameMap.clear();
		relTypeGroupNameToRelTypeGroupMap.clear();
		relTypeToCompositeRelsMap.clear();
	}


	/**
	 * Returns the RelTypeGroup identified by groupname. Returns null if group not found.
	 * @param groupName
	 * @return RelTypeGroup
	 */
	public RelTypeGroup getRelTypeGroup(String groupName) {
		return (RelTypeGroup) relTypeGroupNameToRelTypeGroupMap.get(groupName);
	}

	/**
	 * Returns the RelTypeGroup that contains the relType.
	 * If the given relationship type has not been assigned to a
	 * group already, it will be put in the DEFAULT_GROUP.
	 * @param relType
	 * @return RelTypeGroup
	 */
	public RelTypeGroup getRelTypeGroupForType(String relType) {
		String groupName = (String) relTypeToRelTypeGroupNameMap.get(relType);
		if (groupName == null) {
		    String defaultGroupName = dataBean.getDefaultGroupForRelationshipType(relType);
		    groupName = defaultGroupName != null ? defaultGroupName : ShrimpConstants.DEFAULT_GROUP;
			RelTypeGroup relTypeGroup = getRelTypeGroup(groupName);
			if (getRelTypeGroup(groupName) == null) {
				relTypeGroup = createRelTypeGroup(groupName);
			}
			addRelTypeToGroup(relType, relTypeGroup);
		}
		return getRelTypeGroup(groupName);
	}

	/**
	 * Returns all existing RelTypeGroups.
	 */
	public Vector getRelTypeGroups() {
		return new Vector(relTypeGroupNameToRelTypeGroupMap.values());
	}

	/**
	 * Creates, if neccessary, and returns a RelTypeGroup with a name of groupName.
	 * If a group with the given name exists already, it will be returned and may contain arc types already.
	 * If a group with the given name does not exist, it will be created and returned and will contain no arc types.
	 *
	 * @param groupName
	 * @return RelTypeGroup
	 */
	public RelTypeGroup createRelTypeGroup(String groupName) {
		RelTypeGroup relTypeGroup = (RelTypeGroup) relTypeGroupNameToRelTypeGroupMap.get(groupName);
		if (relTypeGroup == null) {
			relTypeGroup = new RelTypeGroup(groupName);
			relTypeGroupNameToRelTypeGroupMap.put(groupName, relTypeGroup);
		}

		return relTypeGroup;
	}

	/**
	 * This will dispose of the group identified by groupname.
	 *
	 * @param groupName The name of the group to get rid of.
	 */
	public void disposeRelTypeGroup(String groupName) {
		RelTypeGroup relTypeGroup = (RelTypeGroup) relTypeGroupNameToRelTypeGroupMap.get(groupName);
		if (relTypeGroup != null) {

			if (relTypeGroup.areCompositesEnabled()) {
				setCompositesEnabled(relTypeGroup, false);
			}

			relTypeGroupNameToRelTypeGroupMap.remove(groupName);
		}
	}

	/**
	 * Informs this CompositeArcsManager that the given relationship type
	 * has been removed from the group with given name.
	 * @param relType
	 * @param group
	 */
	public void removeRelTypeFromGroup(String relType, RelTypeGroup group) {
	    if (!group.getRelTypes().contains(relType)) {
			return;
		}

	    group.remove(relType);

        relTypeToRelTypeGroupNameMap.remove(relType);

        // if composites are enabled for this group we must update
        // all the mappings and display accordingly
        if (group.areCompositesEnabled()) {
            for (Iterator iter = getCompositeArcs().iterator(); iter.hasNext();) {
                ShrimpCompositeArc cArc = (ShrimpCompositeArc) iter.next();
                Vector cRelsToRemove = new Vector();
                Collection cRels = (Collection) compositeArcToCompositeRelsMap.get(cArc);
                for (Iterator iterator = cRels.iterator(); iterator.hasNext();) {
                    CompositeRelationship cRel = (CompositeRelationship) iterator.next();
                    if (cRel.getRelationship().getType().equals(relType)) {
                        cRelsToRemove.add(cRel);
                    }
                }
                cRels.removeAll(cRelsToRemove);

                if (cRels.isEmpty()) {
                    removeCompositeArc(cArc, relType);
                }
            }

            for (Iterator iter = getTopSrcIds().iterator(); iter.hasNext();) {
                Object id = iter.next();
                Vector cRelsToRemove = new Vector();
                Collection collection = (Collection) topSrcIdToCompositeRelsMap.get(id);
                for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
                    CompositeRelationship cRel = (CompositeRelationship) iterator.next();
                    if (cRel.getRelationship().getType().equals(relType)) {
                        cRelsToRemove.add(cRel);
                    }
                }
                collection.removeAll(cRelsToRemove);
                if (collection.isEmpty()) {
                    topSrcIdToCompositeRelsMap.remove(id);
                }
            }

            showRegularShrimpArcs(relType);
        }
    }

	/**
	 * Informs this CompositeArcsManager that the given relationship type
	 * has been added to the group with given name.
	 * @param relType
	 * @param group
	 */
	public void addRelTypeToGroup(String relType, RelTypeGroup group) {
	    if (group.getRelTypes().contains(relType)) {
	    	return;
	    }
	    group.add(relType);

		relTypeToRelTypeGroupNameMap.put(relType, group.getGroupName());

		// if composites are enabled for this group then create
		// new composite relationships and update display
		if (group.areCompositesEnabled()) {
			String compositeType = group.getGroupName();
			createCompositeRelationshipsOfType(relType, compositeType);
			addCompositeArcsOfRootNodes();
			/*
			Collection rootNodes = dataDisplayBridge.getRootNodes();
			for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
	            ShrimpNode rootNode = (ShrimpNode) iter.next();
	            addCompositeArcsOfChildren(rootNode);
	        }
	        */
		}
	}

	/**
	 * Returns all existing composite arcs.
	 * @return A collection of CompositeArcs
	 */
	public Collection getCompositeArcs() {
	    Collection compositeArcs = new HashSet(compositeArcToCompositeRelsMap.keySet());
	    return compositeArcs;
	}

	private Collection getTopSrcIds() {
	    Collection topSrcIds = new HashSet(topSrcIdToCompositeRelsMap.keySet());
	    return topSrcIds;
	}

	private void enableComposites(RelTypeGroup relTypeGroup) {
		// The group name will be our composite type
		String compositeType = relTypeGroup.getGroupName();

		// Now go through all the types in this group and create CompositeRelationships for relationships
		// of this type. Then show the composite arcs.
		Vector relTypes = relTypeGroup.getRelTypes();
		for (int i = 0; i < displayBean.getCprels().length; i++) {
            String cprel = displayBean.getCprels()[i];
            relTypes.remove(cprel);
        }
		for (Iterator iterator = relTypeGroup.getRelTypes().iterator(); iterator.hasNext();) {
			String relType = (String) iterator.next();
			createCompositeRelationshipsOfType(relType, compositeType);
		}
		addCompositeArcsOfRootNodes();
		/*
		Collection rootNodes = dataDisplayBridge.getRootNodes();
		for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
            ShrimpNode rootNode = (ShrimpNode) iter.next();
            addCompositeArcsOfChildren(rootNode);
    		// we need to add composites to all visible descendents of the root nodes
           //Vector descendents = dataDisplayBridge.getDescendentNodes(rootNode, false);
           // for (Iterator iterator = descendents.iterator(); iterator.hasNext();) {
             //   ShrimpNode descendent = (ShrimpNode) iterator.next();
               // addCompositeArcsOfChildren(descendent);
           // }
        }
        */

	}

	private void createCompositeRelationshipsOfType(String relType, String compositeType) {
		Vector compositeRels = new Vector();
		compositeRels.addAll(getCompositeRelationshipsByType(compositeType, relType, displayBean.getCprels(), displayBean.isInverted()));

		for (Iterator iterator = compositeRels.iterator(); iterator.hasNext();) {
			CompositeRelationship cRel = (CompositeRelationship) iterator.next();
			if (cRel.getSrcAncestorIDs().length > 0) {
				long rootSrcArtifactID = cRel.getSrcAncestorIDs()[cRel.getSrcAncestorIDs().length - 1];
		        Artifact rootSrcArtifact = dataBean.getArtifact(rootSrcArtifactID);
		        if (rootSrcArtifact != null) {
		            //System.out.println("rootSrcArtifact: " + rootSrcArtifact);
		            mapTopSrcToCompositeRel(new Long(rootSrcArtifact.getID()), cRel);
		        } else {
		           System.err.println("no root artifact for composite rel! rootSrcArtifactID = " + rootSrcArtifactID);
		        }
			} else {
		           System.err.println("no src ancestor IDs for composite rel! crel = " + cRel);
			}
		}

		hideRegularShrimpArcs(relType);
	}

	/**
     * @param relType
     */
    private void hideRegularShrimpArcs(String relType) {
        Vector rels = dataBean.getRelationshipsOfType(relType, true);
		for (Iterator iterator = rels.iterator(); iterator.hasNext();) {
			Relationship rel = (Relationship) iterator.next();
			Vector arcs = dataDisplayBridge.getShrimpArcs(rel, false);
			displayBean.removeObject(arcs);
		}
    }

    private Vector getCompositeRelationshipsByType(String compositeType, String relType, String[] cprels, boolean inverted) {
		Vector compositeRels = (Vector) relTypeToCompositeRelsMap.get(relType);
		if (compositeRels == null) {
			compositeRels = dataBean.getCompositeRelationshipsByType(relType, cprels, compositeType, inverted);
			relTypeToCompositeRelsMap.put(relType, compositeRels);
		}

		return compositeRels;
	}


	private void mapTopSrcToCompositeRel(Object topSrcId, CompositeRelationship compositeRel) {
		Vector compositeRels = (Vector) topSrcIdToCompositeRelsMap.get(topSrcId);
		if (compositeRels == null) {
			compositeRels = new Vector();
			topSrcIdToCompositeRelsMap.put(topSrcId, compositeRels);
		}

		if (!compositeRels.contains(compositeRel)) {
			compositeRels.add(compositeRel);
		}
	}

	public void setCompositesEnabled(RelTypeGroup group, boolean enable) {
	    if (group.areCompositesEnabled() != enable) {
	        if (enable) {
	            enableComposites(group);
	        } else {
	            disableComposites(group);
	        }
		    group.setCompositesEnabled(enable);
	    }
	}

	/**
	 * Disables composite arcs for the given group.
	 * @param group
	 */
	private void disableComposites(RelTypeGroup group) {
		String compositeType = group.getGroupName();

		for (Iterator iter = getCompositeArcs().iterator(); iter.hasNext();) {
            ShrimpCompositeArc cArc = (ShrimpCompositeArc) iter.next();
 			Vector cRels = (Vector) compositeArcToCompositeRelsMap.get(cArc);
			// crels contains at least 1 crel
			CompositeRelationship rel = (CompositeRelationship) cRels.firstElement();

			if (rel.getCompositeType().equals(compositeType)) {
				removeCompositeArc(cArc, cArc.isAtHighLevel() ? compositeType : rel.getRelationship().getType());
			}
		}

		for (Iterator topSrcIter = getTopSrcIds().iterator(); topSrcIter.hasNext();) {
            Object id = topSrcIter.next();
			Vector remove = new Vector();
			Collection cRels = (Collection) topSrcIdToCompositeRelsMap.get(id);
			for (Iterator cRelsIter = cRels.iterator(); cRelsIter.hasNext();) {
				CompositeRelationship cRel = (CompositeRelationship) cRelsIter.next();
				if (cRel.getCompositeType().equals(compositeType)) {
					remove.add(cRel);
				}
			}
			cRels.removeAll(remove);

			if (cRels.isEmpty()) {
				topSrcIdToCompositeRelsMap.remove(id);
			}
		}

		Vector relTypes = group.getRelTypes();
		for (Iterator iterator = relTypes.iterator(); iterator.hasNext();) {
			String relType = (String) iterator.next();
			showRegularShrimpArcs(relType);
		}
	}

	/**
	 * Removes the composite arc.
	 * @param compositeType	for high level arcs this is {@link CompositeRelationship#getCompositeType()}.
	 * For arcs that aren't at a high level this should be the {@link Relationship#getType()}
	 */
	private void removeCompositeArc(ShrimpCompositeArc cArc, String compositeType) {
		//System.out.println("Removing " + cArc + " from group '" + compositeType + "'...");

		compositeArcToCompositeRelsMap.remove(cArc);
		displayBean.removeObject(cArc);

		ShrimpNode srcNode = cArc.getSrcNode();
		ShrimpNode destNode = cArc.getDestNode();

		// remove this arc from the map of composite type to composite arc (which is inside a map inside a map)
		Map destNodeToCompositeTypeToCompositeArc = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(srcNode);
		if (destNodeToCompositeTypeToCompositeArc != null) {
			Map compositeTypeToCompositeArc = (Map) destNodeToCompositeTypeToCompositeArc.get(destNode);
			if (compositeTypeToCompositeArc != null) {
				Object removed = compositeTypeToCompositeArc.remove(compositeType);
				if ((removed == null) && compositeTypeToCompositeArc.containsValue(cArc)) {
					// shouldn't get here!
					System.out.println("** Composite arc still exists! (Type removed was " + compositeType + ")");
				}
				if (compositeTypeToCompositeArc.isEmpty()) {
					destNodeToCompositeTypeToCompositeArc.remove(destNode);
					if (destNodeToCompositeTypeToCompositeArc.isEmpty()) {
						srcNodeToDestToCompositeTypeToCompositeArcMap.remove(srcNode);
					}
				}
			}
		}
		Map srcNodeToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(destNode);
		if (srcNodeToCompositeTypeToCompositeArc != null) {
			Map compositeTypeToCompositeArc = (Map) srcNodeToCompositeTypeToCompositeArc.get(srcNode);
			if (compositeTypeToCompositeArc != null) {
				Object removed = compositeTypeToCompositeArc.remove(compositeType);
				if ((removed == null) && compositeTypeToCompositeArc.containsValue(cArc)) {
					// shouldn't get here!
					System.out.println("** Composite arc still exists! (Type removed was " + compositeType + ")");
				}
				if (compositeTypeToCompositeArc.isEmpty()) {
					srcNodeToCompositeTypeToCompositeArc.remove(srcNode);
					if (srcNodeToCompositeTypeToCompositeArc.isEmpty()) {
						destNodeToSrcToCompositeTypeToCompositeArcMap.remove(destNode);
					}
				}
			}
		}

		cArc.dispose();
	}

//	// debug method
//	private static void ensureRemovedFromMap(ShrimpCompositeArc cArc, Map map) {
//		for (Iterator iter = map.values().iterator(); iter.hasNext(); ) {
//			Object obj = iter.next();
//			if (obj instanceof Map) {
//				ensureRemovedFromMap(cArc, (Map) obj);
//			} else if (obj == cArc) {
//				System.out.println("ARG!  " + cArc);
//			}
//		}
//	}
//
//	// debug method
//	private static void printMap(Map map) {
//		for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
//			Object key = iter.next();
//			Object val = map.get(key);
//			System.out.println(key + " : " + val);
//		}
//		System.out.println();
//	}

	/**
	 * Put the original arcs associated with these composite arcs back
	 * @param type
	 */
	private void showRegularShrimpArcs(String type) {
		// don't show the arcs if the type is in our hierarchy
		String[] cprels = displayBean.getCprels();
		for (int i = 0; i < cprels.length; i++) {
			if (type.equals(cprels[i])) {
				//System.out.println("In hierarchy");
				return;
			}
		}

		Vector rels = dataBean.getRelationshipsOfType(type, true);
		for (Iterator relsIter = rels.iterator(); relsIter.hasNext();) {
			Relationship relationship = (Relationship) relsIter.next();

			Vector arcs = new Vector();
			Artifact srcArt = (Artifact) relationship.getArtifacts().elementAt(0);
			Artifact destArt = (Artifact) relationship.getArtifacts().elementAt(1);
			Vector srcNodes = dataDisplayBridge.getShrimpNodes(srcArt, false);
			Vector destNodes = dataDisplayBridge.getShrimpNodes(destArt, false);
			//add an arc between all of these nodes
			for (Iterator srcIter = srcNodes.iterator(); srcIter.hasNext();) {
				ShrimpNode srcNode = (ShrimpNode) srcIter.next();
				for (Iterator destIter = destNodes.iterator(); destIter.hasNext();) {
					ShrimpNode destNode = (ShrimpNode) destIter.next();
					ShrimpArc arc = dataDisplayBridge.createShrimpArc(relationship, srcNode, destNode);
					arcs.add(arc);
				}
			}

			displayBean.addObject(arcs);
			displayBean.setVisible(arcs, true, false);
		}
	}

	private ShrimpCompositeArc createCompositeArc(ShrimpNode srcNode, ShrimpNode destNode, ArcStyle arcStyle, Color color) {
		// see if this arc should attach to already created terminals
		// or if we should create new terminals for this arc
		ShrimpTerminal srcTerminal = new PShrimpTerminal(displayBean, srcNode);
		ShrimpTerminal destTerminal = new PShrimpTerminal(displayBean, destNode);

		Vector nodes = new Vector();
		nodes.add(srcNode);
		nodes.add(destNode);

		ShrimpCompositeArc arc = new PShrimpCompositeArc(displayBean, srcNode, destNode,
									/*displayBean.getClosestCommonAncestor(nodes),*/
									srcTerminal, destTerminal, arcStyle, 0,
									displayBean.getUsingArrowHeads(), 1.0);
		arc.setColor(color);

		return arc;
	}

	/**
     * @param topSrcNode
     * @param descendants
     * @param descendant
     */
    private void showIncomingComposites(ShrimpNode topSrcNode, Vector descendants, ShrimpNode descendant) {
        Map srcNodeToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(descendant);
        if (srcNodeToCompositeTypeToCompositeArc == null) {
			return; // no composites
		}

        Set srcNodes = new HashSet(srcNodeToCompositeTypeToCompositeArc.keySet());
        for (Iterator srcNodesIter = srcNodes.iterator(); srcNodesIter.hasNext();) {
            ShrimpNode node = (ShrimpNode) srcNodesIter.next();

            // TODO: it will be faster to search using the hierarchical structure
            if (descendants.contains(node)) {
				continue;
			}

            Vector compositeRels = new Vector();
            Map compositeTypeToCompositeArc = (Map) srcNodeToCompositeTypeToCompositeArc.get(node);
            if (compositeTypeToCompositeArc == null) {
                System.err.println("showIncomingComposites: compositeTypeToCompositeArc == null");
            } else {
                Collection arcs = compositeTypeToCompositeArc.values();
                for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
                    ShrimpCompositeArc arc = (ShrimpCompositeArc) arcsIter.next();
                    compositeRels.addAll((Collection) compositeArcToCompositeRelsMap.get(arc));
                    displayBean.removeObject(arc);
                    compositeArcToCompositeRelsMap.remove(arc);
                }
            }

            Map destNodeToCompositeTypeToCompositeArc = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(node);
            if (destNodeToCompositeTypeToCompositeArc == null) {
                System.err.println("showIncomingComposites: destNodeToCompositeTypeToCompositeArc == null");
            } else {
	            destNodeToCompositeTypeToCompositeArc.remove(descendant);
	            if (destNodeToCompositeTypeToCompositeArc.isEmpty()) {
	                srcNodeToDestToCompositeTypeToCompositeArcMap.remove(node);
	            }
            }

            srcNodeToCompositeTypeToCompositeArc.remove(node);
            for (Iterator iterator = compositeRels.iterator(); iterator.hasNext();) {
                CompositeRelationship cRel = (CompositeRelationship) iterator.next();
                addCompositeArcToDisplay(node, topSrcNode, cRel);
            }
         }

        if (srcNodeToCompositeTypeToCompositeArc.isEmpty()) {
            srcNodeToDestToCompositeTypeToCompositeArcMap.remove(descendant);
        }
    }

    /**
     * @param topSrcNode
     * @param descendants
     * @param descendant
     */
    private void showOutgoingComposites(ShrimpNode topSrcNode, Vector descendants, ShrimpNode descendant) {
        Map destNodeToCompositeTypeToCompositeArc = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(descendant);
        if (destNodeToCompositeTypeToCompositeArc == null) {
			return; // no composites
		}

        Set destNodes = new HashSet(destNodeToCompositeTypeToCompositeArc.keySet());
        for (Iterator iter = destNodes.iterator(); iter.hasNext();) {
            ShrimpNode node = (ShrimpNode) iter.next();

            // TODO: it will be faster to search using the hierarchical structure
            if (descendants.contains(node)) {
				continue;
			}

            Vector compositeRels = new Vector();
            Map compositeTypeToCompositeArc = (Map) destNodeToCompositeTypeToCompositeArc.get(node);
            if (compositeTypeToCompositeArc == null) {
                System.err.println("showOutgoingComposites: compositeTypeToCompositeArc == null");
            } else {
                Collection arcs = compositeTypeToCompositeArc.values();
                for (Iterator iterator2 = arcs.iterator(); iterator2.hasNext();) {
                    ShrimpCompositeArc arc = (ShrimpCompositeArc) iterator2.next();
                    Collection cRels = (Collection) compositeArcToCompositeRelsMap.get(arc);
                    if (cRels == null) {
                        System.err.println("shouldn't be happening (?)");
                    } else {
                        compositeRels.addAll(cRels);
                        compositeArcToCompositeRelsMap.remove(arc);
                    }
                    displayBean.removeObject(arc);
                }
            }

            Map srcNodeToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(node);
            if (srcNodeToCompositeTypeToCompositeArc == null) {
                System.err.println("showOutgoingComposites: srcNodeToCompositeTypeToCompositeArc == null");
            } else {
	            srcNodeToCompositeTypeToCompositeArc.remove(descendant);
	            if (srcNodeToCompositeTypeToCompositeArc.isEmpty()) {
	                destNodeToSrcToCompositeTypeToCompositeArcMap.remove(node);
	            }
            }

            destNodeToCompositeTypeToCompositeArc.remove(node);
            for (Iterator iterator = compositeRels.iterator(); iterator.hasNext();) {
                CompositeRelationship cRel = (CompositeRelationship) iterator.next();
                addCompositeArcToDisplay(topSrcNode, node, cRel);
            }
        }

        if (destNodeToCompositeTypeToCompositeArc.isEmpty()) {
            srcNodeToDestToCompositeTypeToCompositeArcMap.remove(descendant);
        }
    }

	/**
	 *
	 * @param topSrcNode
	 */
	public void collapseCompositeArcs(ShrimpNode topSrcNode) {
		Vector descendants = dataDisplayBridge.getDescendentNodes(topSrcNode, false);

		for (Iterator iterator = descendants.iterator(); iterator.hasNext();) {
			ShrimpNode descendant = (ShrimpNode) iterator.next();

			// outgoing arcs
            showOutgoingComposites(topSrcNode, descendants, descendant);
			// incoming arcs
            showIncomingComposites(topSrcNode, descendants, descendant);
		}
	}

	private void addCompositeArcsOfRootNodes() {
	    Collection compositeRelsVectors = new Vector (topSrcIdToCompositeRelsMap.values());
		topSrcIdToCompositeRelsMap.clear();
	    for (Iterator iter = compositeRelsVectors.iterator(); iter.hasNext();) {
            Vector compositeRels = (Vector) iter.next();
            for (Iterator iterator = compositeRels.iterator(); iterator.hasNext();) {
                CompositeRelationship cRel = (CompositeRelationship) iterator.next();
                long [] srcAncestorIDs = cRel.getSrcAncestorIDs();
                if (srcAncestorIDs.length == 0) {
                    System.err.println("no src ancestor IDs");
                    continue;
                }
    			long rootSrcArtifactID = srcAncestorIDs[srcAncestorIDs.length-1];
    	        Artifact rootSrcArtifact = dataBean.getArtifact(rootSrcArtifactID);
    	        if (rootSrcArtifact == null) {
    	           System.err.println("no root artifact for composite rel! rootSrcArtifactID = " + rootSrcArtifactID);
    	           continue;
    	        }
    	        long [] srcDestAncestorIDs = cRel.getDestAncestorIDs();
                if (srcDestAncestorIDs.length == 0) {
                    System.err.println("no dest ancestor IDs");
                    continue;
                }
    			long rootDestArtifactID = srcDestAncestorIDs[srcDestAncestorIDs.length-1];
    	        Artifact rootDestArtifact = dataBean.getArtifact(rootDestArtifactID);
    	        if (rootDestArtifact == null) {
    	           System.err.println("no root artifact for composite rel! rootDestArtifactID = " + rootDestArtifactID);
    	           continue;
    	        }
    	        Vector rootSrcNodes = dataDisplayBridge.getShrimpNodes(rootSrcArtifact, false);
    	        if (rootSrcNodes.size() != 1) {
     	           	System.err.println("rootSrcNodes.size() != 1 - rootSrcNodes = " + rootSrcNodes);
    	            continue;
    	        }
    	        Vector rootDestNodes = dataDisplayBridge.getShrimpNodes(rootDestArtifact, false);
    	        if (rootDestNodes.size() != 1) {
     	           	System.err.println("rootDestNodes.size() != 1 - rootDestNodes = " + rootDestNodes);
     	           continue;
    	        }
    	        ShrimpNode rootSrcNode = (ShrimpNode) rootSrcNodes.firstElement();
    	        ShrimpNode rootDestNode = (ShrimpNode) rootDestNodes.firstElement();
    			showCompositeArcsAlongBothTrees(rootSrcNode, rootDestNode, cRel, true);
            }
        }
	}

	/**
	 * Show composites arcs of the children of the given node.
	 * This will create and add composites arcs to the display,
	 * if there are any between the children of the given node.
	 * @param node
	 */
	public void addCompositeArcsOfChildren (ShrimpNode node) {
		Long artifactID = new Long(node.getArtifact().getID());
		Vector compositeRels = (Vector) topSrcIdToCompositeRelsMap.get(artifactID);
		if (compositeRels != null && !compositeRels.isEmpty()) {
			topSrcIdToCompositeRelsMap.remove(artifactID);
			for (Iterator iter = compositeRels.iterator(); iter.hasNext();) {
                CompositeRelationship compositeRel = (CompositeRelationship) iter.next();
    			showCompositeArcsAlongBothTrees(node, node, compositeRel, true);
            }
		}
	}

	private void explodeCompositeArc(String compositeType, ShrimpCompositeArc oldCompositeArc) {
		ShrimpNode arcSrcNode = oldCompositeArc.getSrcNode();
		ShrimpNode arcDestNode = oldCompositeArc.getDestNode();

		Vector compositeRels = (Vector) compositeArcToCompositeRelsMap.get(oldCompositeArc);

		// cleanup stuff from this oldComposite arc
		displayBean.removeObject(oldCompositeArc);
		compositeArcToCompositeRelsMap.remove(oldCompositeArc);

		Map destNodeToCompositeTypeToCompositeArc = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(arcSrcNode);
		Map compositeTypeToCompositeArc = (Map) destNodeToCompositeTypeToCompositeArc.get(arcDestNode);
		compositeTypeToCompositeArc.remove(compositeType);
		if (compositeTypeToCompositeArc.isEmpty()) {
			destNodeToCompositeTypeToCompositeArc.remove(arcDestNode);
			if (destNodeToCompositeTypeToCompositeArc.isEmpty()) {
				srcNodeToDestToCompositeTypeToCompositeArcMap.remove(arcSrcNode);
			}
		}
		Map srcNodeToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(arcDestNode);
		compositeTypeToCompositeArc = (Map) srcNodeToCompositeTypeToCompositeArc.get(arcSrcNode);
		compositeTypeToCompositeArc.remove(compositeType);
		if (compositeTypeToCompositeArc.isEmpty()) {
			srcNodeToCompositeTypeToCompositeArc.remove(arcSrcNode);
			if (srcNodeToCompositeTypeToCompositeArc.isEmpty()) {
				destNodeToSrcToCompositeTypeToCompositeArcMap.remove(arcDestNode);
			}
		}

		for (Iterator iter = compositeRels.iterator(); iter.hasNext();) {
            CompositeRelationship compositeRel = (CompositeRelationship) iter.next();
			showCompositeArcsAlongBothTrees(arcSrcNode, arcDestNode, compositeRel, false);
        }
	}

    /**
	 * Finds the nodes that the composite arcs should go on.
	 * TODO make this method work with multiple root nodes
	 * this method assumes that there is one root (ie. assumes all nodes have a common ancestor node)
	 * @param currentNode
	 * @param targetID
	 * @param ancestorArtifactIds
	 * @param deep
	 */
	private ShrimpNode getNodeToAttachToRecursive(ShrimpNode currentNode, long targetID, long[] ancestorArtifactIds, boolean deep) {
		long currentNodeID = currentNode.getArtifact().getID();
		if (currentNodeID == targetID) {
		    return currentNode;
		} else if (CollectionUtils.contains(ancestorArtifactIds, currentNodeID)) {
			if (!displayBean.getPanelMode(currentNode).equals(PanelModeConstants.CHILDREN)) {
			    return currentNode;
			}
			Vector childNodes = dataDisplayBridge.getChildNodes(currentNode, false);
			if (deep) {
				for (Iterator iterator = childNodes.iterator(); iterator.hasNext(); ) {
					ShrimpNode childNode = (ShrimpNode) iterator.next();
					ShrimpNode foundNode = getNodeToAttachToRecursive(childNode, targetID, ancestorArtifactIds, deep);
					if (foundNode != null) {
						return foundNode;
					}
				}
			} else {
				for (Iterator iterator = childNodes.iterator(); iterator.hasNext();) {
					ShrimpNode childNode = (ShrimpNode) iterator.next();
					long childNodeID = childNode.getArtifact().getID();
					if (childNodeID == targetID || CollectionUtils.contains(ancestorArtifactIds, childNodeID)) {
					    return childNode;
					}
				}
			}
		}
		return null;
	}
	/**
	 *
	 * @param topSrcNode
	 * @param topDestNode
	 * @param compositeRel
	 * @param deep
	 */
	private void showCompositeArcsAlongBothTrees(ShrimpNode topSrcNode, ShrimpNode topDestNode, CompositeRelationship compositeRel, boolean deep) {
		long[] srcAncestorArtifactIds = compositeRel.getSrcAncestorIDs();
		long[] destAncestorArtifactIds = compositeRel.getDestAncestorIDs();

		// find the appropriate src and dest nodes in the display that should be connected by
		// a composite arc that represents the given composite relationship
		ShrimpNode srcNode = getNodeToAttachToRecursive(topSrcNode, compositeRel.getSrcID(), srcAncestorArtifactIds, deep);
		ShrimpNode destNode = getNodeToAttachToRecursive(topDestNode, compositeRel.getDestID(), destAncestorArtifactIds, deep);

		if (srcNode != null && destNode != null) {
		    addCompositeArcToDisplay(srcNode, destNode, compositeRel);
		} else {
			// TODO should never happen
			if (srcNode == null || destNode == null) {
				System.err.println("Couldn't find composite arc for " + compositeRel);
			}
		}
	}

	/**
	 * This is the method that actually creates a composite arc and adds it to the display
	 *
	 * @param srcNode Source of the composite arc
	 * @param destNode Destination of the composite arc
	 * @param compositeRel The composite relationship that the arc represents.
	 */
	private void addCompositeArcToDisplay(ShrimpNode srcNode, ShrimpNode destNode, CompositeRelationship compositeRel) {
		if (srcNode != destNode) {
			Vector arts = new Vector();
			Artifact srcArtifact = srcNode.getArtifact();
			Artifact destArtifact = destNode.getArtifact();
			arts.add(srcArtifact);
			arts.add(destArtifact);

			Map destNodesToCompositeTypeToCompositeArc = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(srcNode);
			if (destNodesToCompositeTypeToCompositeArc == null) {
				destNodesToCompositeTypeToCompositeArc = new HashMap();
				srcNodeToDestToCompositeTypeToCompositeArcMap.put(srcNode, destNodesToCompositeTypeToCompositeArc);
			}

			// we have found the src and dest of the relationship enclosed in the CompositeRelationship, show the actual arc.
			Relationship rel = compositeRel.getRelationship();
			if (srcArtifact.getID() == compositeRel.getSrcID() && destArtifact.getID() == compositeRel.getDestID()) {
				// TODO: use a regular arc instead of a composite
				ArcStyle arcStyle = (ArcStyle) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, rel.getAttribute(AttributeConstants.NOM_ATTR_REL_TYPE));
				// @tag Shrimp.GXL_Colors: arc colors
				Color color = displayBean.getDataDisplayBridge().getArcColorFromRelationship(rel, attrToVisVarBean);

				ShrimpCompositeArc arc = createCompositeArc(srcNode, destNode, arcStyle, color);
				arc.addRelationship(rel);
				arc.setAtHighLevel(false);

				Map compositeTypeToCompositeArc1 = (Map) destNodesToCompositeTypeToCompositeArc.get(destNode);
				if (compositeTypeToCompositeArc1 == null) {
					compositeTypeToCompositeArc1 = new HashMap();
					destNodesToCompositeTypeToCompositeArc.put(destNode, compositeTypeToCompositeArc1);
				}

				// two nodes can have only one arc of one type between them
				compositeTypeToCompositeArc1.put(rel.getType(), arc);

				Map srcNodesToSrcToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(destNode);
				if (srcNodesToSrcToCompositeTypeToCompositeArc == null) {
					srcNodesToSrcToCompositeTypeToCompositeArc = new HashMap();
					destNodeToSrcToCompositeTypeToCompositeArcMap.put(destNode, srcNodesToSrcToCompositeTypeToCompositeArc);
				}
				Map compositeTypeToCompositeArc2 = (Map) srcNodesToSrcToCompositeTypeToCompositeArc.get(srcNode);
				if (compositeTypeToCompositeArc2 == null) {
					compositeTypeToCompositeArc2 = new HashMap();
					srcNodesToSrcToCompositeTypeToCompositeArc.put(srcNode, compositeTypeToCompositeArc2);
				}
				compositeTypeToCompositeArc2.put(rel.getType(), arc);

				Vector rels = (Vector) compositeArcToCompositeRelsMap.get(arc);
				if (rels == null) {
					rels = new Vector();
					compositeArcToCompositeRelsMap.put(arc, rels);
				}

				rels.add(compositeRel);
				displayBean.addObject(arc);
				arc.updateVisibility();

			} else {
				// show the composite arc between the two nodes, if there is already one of the same type then just use that
				final String compositeType = compositeRel.getCompositeType();
				RelTypeGroup relTypeGroup = getRelTypeGroup(compositeType);
				if (relTypeGroup == null) {
					(new Exception("This should not happen.")).printStackTrace();
					return;
				}

				Map compositeTypeToCompositeArc1 = (Map) destNodesToCompositeTypeToCompositeArc.get(destNode);
				if (compositeTypeToCompositeArc1 == null) {
					compositeTypeToCompositeArc1 = new HashMap();
					destNodesToCompositeTypeToCompositeArc.put(destNode, compositeTypeToCompositeArc1);
				}
				ShrimpCompositeArc arc = (ShrimpCompositeArc) compositeTypeToCompositeArc1.get(compositeType);
				if (arc == null) {
					arc = createCompositeArc(srcNode, destNode, relTypeGroup.getCompositeStyle(), relTypeGroup.getCompositeColor());
					arc.setAtHighLevel(true);
					arc.setArcCount(1);

					((PShrimpCompositeArc) arc).addInputEventListener(new PInputManager() {
						public void mouseClicked(PInputEvent event) {
							// TODO move this listener elsewhere
							if (event.getClickCount() == 2) {
							    explodeCompositeArc(compositeType, (ShrimpCompositeArc) event.getPickedNode());
							}
						}
					});

					compositeTypeToCompositeArc1.put(compositeType, arc);

					Map srcNodeToCompositeTypeToCompositeArc = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(destNode);
					if (srcNodeToCompositeTypeToCompositeArc == null) {
						srcNodeToCompositeTypeToCompositeArc = new HashMap();
						destNodeToSrcToCompositeTypeToCompositeArcMap.put(destNode, srcNodeToCompositeTypeToCompositeArc);
					}

					Map compositeTypeToCompositeArc2 = (Map) srcNodeToCompositeTypeToCompositeArc.get(srcNode);
					if (compositeTypeToCompositeArc2 == null) {
						compositeTypeToCompositeArc2 = new HashMap();
						srcNodeToCompositeTypeToCompositeArc.put(srcNode, compositeTypeToCompositeArc2);
					}

					compositeTypeToCompositeArc2.put(compositeType, arc);
				} else {
					arc.setArcCount(arc.getArcCount() + 1);
				}
				arc.addRelationship(rel);

				Vector rels = (Vector) compositeArcToCompositeRelsMap.get(arc);
				if (rels == null) {
					rels = new Vector();
					compositeArcToCompositeRelsMap.put(arc, rels);
				}

				rels.add(compositeRel);

				// TODO this line sometimes adds an arc more than once...  why?
				displayBean.addObject(arc);
				arc.updateVisibility();
				//displayBean.setVisible(arc, true, true);
			}
		} else {
			// if we are here, this means the node should have children unless a node has a relationship with itself
			// if the node is open, we should go one level deeper
			if (displayBean.getPanelMode(srcNode).equals(PanelModeConstants.CHILDREN)) {
				showCompositeArcsAlongBothTrees(srcNode, srcNode, compositeRel, false);
			} else { 	// the node is not open, store these composite rels, they will be shown later when the node is opened
				long srcNodeID = srcNode.getArtifact().getID();
				mapTopSrcToCompositeRel(new Long(srcNodeID), compositeRel);
			}
		}
	}

	/**
	 * Returns the incoming and outgoing composite arcs that the given node connects to.
	 * @param node
	 */
	Collection getCompositeArcs(ShrimpNode node) {
		Vector arcs = new Vector();

		Map destToCompositeTypeToCompositeArcs = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(node);
		if (destToCompositeTypeToCompositeArcs != null) {
			Collection collection = destToCompositeTypeToCompositeArcs.values();
			for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
				Map compositeTypeToCompositeArcs = (Map) iterator.next();
				arcs.addAll(compositeTypeToCompositeArcs.values());
			}
		}

		Map srcToCompositeTypeToCompositeArcs = (Map) destNodeToSrcToCompositeTypeToCompositeArcMap.get(node);
		if (srcToCompositeTypeToCompositeArcs != null) {
			Collection collection = srcToCompositeTypeToCompositeArcs.values();
			for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
				Map compositeTypeToCompositeArcs = (Map) iterator.next();
				arcs.addAll(compositeTypeToCompositeArcs.values());
			}
		}

		for (Iterator iter = arcs.iterator(); iter.hasNext(); ) {
			ShrimpCompositeArc arc = (ShrimpCompositeArc) iter.next();
			if ((arc.getSrcNode() == null) || (arc.getDestNode() == null)) {
				System.out.println("Removing null arc - " + arc);
				iter.remove();
			}
		}
		return arcs;
	}

	/**
	 * Returns the composite arcs that the given source and destination nodes participate in.
	 * @param srcNode
	 * @param destNode
	 */
	Collection getCompositeArcs(ShrimpNode srcNode, ShrimpNode destNode) {
		Vector retArcs = new Vector();

		Map destToCompositeTypeToCompositeArcs = (Map) srcNodeToDestToCompositeTypeToCompositeArcMap.get(srcNode);
		if (destToCompositeTypeToCompositeArcs != null) {
			Map compositeTypeToCompositeArcs = (Map) destToCompositeTypeToCompositeArcs.get(destNode);
			if (compositeTypeToCompositeArcs != null) {
				retArcs.addAll(compositeTypeToCompositeArcs.values());
			}
		}
		return retArcs;
	}

	/**
	 * Loads the relTypeGroup settings from the application properties.
	 */
	private void load() {
		String str = project.getProperties().getProperty(REL_TYPE_GROUPS_KEY);
		if (str != null) {
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new InputSource(new StringReader(str)));

				RelTypeGroup defaultGroup = getRelTypeGroup(ShrimpConstants.DEFAULT_GROUP);
				Element groups = document.getDocumentElement();
				if (groups.getTagName().equals("Groups")) {
					NodeList list = groups.getElementsByTagName("Group");
					for (int i = 0; i < list.getLength(); i++) {
						Element group = (Element) list.item(i);

						String groupName = group.getAttribute("name");
						RelTypeGroup relTypeGroup = createRelTypeGroup(groupName);
						String compositeEnabled = group.getAttribute("compositeEnabled");
						String compositeColor = group.getAttribute("compositeColor");

						NodeList list2 = group.getElementsByTagName("Type");
						for (int j = 0; j < list2.getLength(); j++) {
							Element type = (Element) list2.item(j);
							String typeName = type.getAttribute("name");

							if(typeName != null && (defaultGroup == null || !defaultGroup.hasRelType(typeName) || defaultGroup.remove(typeName))) {
								addRelTypeToGroup(typeName, relTypeGroup);
							}
						}

						if (compositeColor != null) {
							relTypeGroup.setCompositeColor(new Color(Integer.parseInt(compositeColor)));
						}

						if (compositeEnabled != null) {
							relTypeGroup.setCompositesEnabled(Boolean.valueOf(compositeEnabled).booleanValue());
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Saves the RelTypeGroup settings to the application properties.
	 */
	void save(ShrimpProject project) {
        try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument(); // Create from whole cloth
			Element groups = document.createElement("Groups");
			document.appendChild(groups);

			for (Iterator iter = relTypeGroupNameToRelTypeGroupMap.keySet().iterator(); iter.hasNext();) {
			    String groupName = (String) iter.next();
				RelTypeGroup relTypeGroup = (RelTypeGroup) relTypeGroupNameToRelTypeGroupMap.get(groupName);
				Element group = document.createElement("Group");
				groups.appendChild(group);
				group.setAttribute("name", groupName);
				group.setAttribute("compositeEnabled", "" + relTypeGroup.areCompositesEnabled());
				group.setAttribute("compositeColor", "" + relTypeGroup.getCompositeColor().getRGB());

				for (Iterator iterator = relTypeGroup.getRelTypes().iterator(); iterator.hasNext();) {
					String relType = (String) iterator.next();
					Element type = document.createElement("Type");
					group.appendChild(type);
					type.setAttribute("name", relType);
				}
			}

        	// Use a Transformer for output (it is not actually transforming anything but this is the suggest way)
            OutputStream outputStream = new ByteArrayOutputStream();
            try {
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				DOMSource source = new DOMSource(document);
				StreamResult result = new StreamResult(outputStream);
				// @tag Shrimp.Java6 : this sometimes throws an AbstractMethodError in Java 1.6
				transformer.transform(source, result);
        	} catch (AbstractMethodError e) {
        		// Java 6 error, use alternate xml serialization method
        		XMLSerializerUtil.serialize(document, outputStream);
        	}
			project.getProperties().setProperty(REL_TYPE_GROUPS_KEY, outputStream.toString());
		} catch (Throwable t) {
			System.err.println(ApplicationAccessor.getAppName() + " - error saving composite arcs to xml.");
			t.printStackTrace();
		}
 	}


	// The purpose of the code below is to stop composite arcs from getting too large on the screen.
	//The "width" or "weight" of each visible composite arc is scaled according to the
	// composite arc on the screen that represents the most arcs.
	private static Map visibleCompositeArcCountsMap = new HashMap();
	private static int maxVisibleCompositeArcCount = Integer.MIN_VALUE;
	private final static double MIN_WEIGHT = 1.0;
	private final static double MAX_WEIGHT = 30.0;

	/**
	 * Informs this composite arc manager of a change in the visibility of the given composite arc.
	 * The appropriate weights of all visible composite arcs will be recalculated and arcs will be redrawn.
	 * @param arcCount
	 * @param cArc
	 * @param add
	 */
	public static void updateVisibleCompositeArcCounts(ShrimpCompositeArc cArc, int arcCount, boolean add) {
		if (!cArc.isAtHighLevel()) {
			return;
		}
		if (arcCount == 0) {
			System.err.println("whooa! cant update arc count of 0!");
			return;
		}
		boolean changed = false;
		Integer arcCountInt = new Integer (arcCount);
		Set compositesWithThisCount = (Set) visibleCompositeArcCountsMap.get(arcCountInt);
		if (compositesWithThisCount == null) {
			if (add) {
				compositesWithThisCount = new HashSet();
				visibleCompositeArcCountsMap.put(arcCountInt, compositesWithThisCount);
			} else {
				System.err.println("whooa! cant remove what's not there!");
				return;
			}
		}

		if (add) {
			boolean added = compositesWithThisCount.add(cArc);
			if (!added) {
				System.err.println("whooa! arc not added!");
			}
			changed = added;
		} else {
			boolean removed = compositesWithThisCount.remove(cArc);
			if (!removed) {
				System.err.println("whooa! arc not removed!");
			}
			changed = removed;
		}

		if (compositesWithThisCount.isEmpty()) {
			visibleCompositeArcCountsMap.remove(arcCountInt);
		}
		if (changed) {
			computeMaxVisibleCompositeArcCount();
		}
	}

	private static void computeMaxVisibleCompositeArcCount() {
		int newMax = Integer.MIN_VALUE;
		for (Iterator iter = visibleCompositeArcCountsMap.keySet().iterator(); iter.hasNext();) {
			Integer arcCountInt = (Integer) iter.next();
			newMax = Math.max(arcCountInt.intValue(), newMax);
		}
		if (newMax != maxVisibleCompositeArcCount) {
			maxVisibleCompositeArcCount = newMax;
			updateVisibleComposites();
		}
	}

	/**
	 * Returns an appropriate weight for a composite arcs that represents the given number of arcs.
	 */
	public static double calculateWeight(int arcCount) {
		double weight = MIN_WEIGHT;
		if (maxVisibleCompositeArcCount <= MAX_WEIGHT) {
			weight = arcCount;
		} else {
			weight = Math.max(MIN_WEIGHT, (MAX_WEIGHT / maxVisibleCompositeArcCount) * arcCount);
		}
		return weight;
	}

	private static void updateVisibleComposites() {
		for (Iterator iter = visibleCompositeArcCountsMap.values().iterator(); iter.hasNext();) {
			Set compositesWithThisCount = (Set) iter.next();
			for (Iterator iterator = compositesWithThisCount.iterator(); iterator.hasNext();) {
				PShrimpCompositeArc compArc = (PShrimpCompositeArc) iterator.next();
				ArcStyle arcStyle = compArc.getStyle();
				if (arcStyle instanceof CompositeArcStyle) {
					((CompositeArcStyle)arcStyle).recreateArc();
					compArc.setBounds(arcStyle.getBounds());
				}
			}
		}
	}

}