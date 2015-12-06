/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.ActivityManager;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.SwingToolTipManager;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivityScheduler;

/**
 * @author Rob Lintern
 */
public class PFlatDisplayBean extends PNestedDisplayBean {

	private static final int DEFAULT_LABEL_LEVELS = 99;
	private static final int ACC_BY_SIZE = 0;

	private int currentAccentuateMode = ACC_BY_SIZE;
	private int unaccentuatedNodeWidth = 3;
	private int unaccentuatedNodeHeight = 3;

	// a list of currently collapsed nodes
	private Vector collapseParents;
	// a list of all nodes made invisible because of collapsed parents
	private Vector allCollapsedDescendents;
	// a mapping from a collapsed parent to the nodes it caused to be invisible
	private Hashtable collapseParentToDescendents;
	// the node that the view has been pruned on
	private ShrimpNode pruneParent;
	// a list of nodes made invisible because they've been pruned from tree
	private Vector prunedNodes;
	// a list of accentuated nodes
	private Vector accentuatedNodes;

	/**
	 * @param cprels
	 */
	public PFlatDisplayBean(ShrimpProject project, String[] cprels, DataDisplayBridge dataDisplayBridge) {
		super(project, cprels, dataDisplayBridge);
		lastLayoutMode = LayoutConstants.LAYOUT_TREE_VERTICAL;
		SwingToolTipManager.sharedInstance().unregisterComponent(pCanvas);

		// TODO horizontal and vertical arcs don't show up unless arrowheads rendered !? don't really want arrowheads in HV
		usingArrowHeads = true;

		collapseParents = new Vector();
		allCollapsedDescendents = new Vector();
		collapseParentToDescendents = new Hashtable();
		pruneParent = null;
		prunedNodes = new Vector();
		accentuatedNodes = new Vector();

		defaultLabelMode = DEFAULT_LABEL_MODE;
		labelLevels = DEFAULT_LABEL_LEVELS;

		getPCanvas().setBackground(Color.white);
		labelBackgroundOpaque = true;
	}

	public void clear() {
		super.clear();
		collapseParents.clear();
		allCollapsedDescendents.clear();
		collapseParentToDescendents.clear();
		pruneParent = null;
		prunedNodes.clear();
		accentuatedNodes.clear();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean#addLayers()
	 */
	protected void addLayers() {
		//note the last added is the last drawn
		getPCanvas().getLayer().addChild (arcLayer); // arc layer is on the bottom in flat display
		getPCanvas().getLayer().addChild (nodeLayer);
		getPCanvas().getLayer().addChild (topLayer);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#addShrimpNode(ca.uvic.csr.shrimp.DisplayBean.ShrimpNode)
	 */
	public void addShrimpNode(ShrimpNode node) {
		if (node instanceof PShrimpNode) {
			nodeLayer.addChild ((PShrimpNode)node);
			ShrimpNode parentNode = node.getParentShrimpNode();
			if (parentNode != null) {
				setTransformOf(node, getTransformOf(parentNode), false, false);
			}
		}
	}

	public void addShrimpLabel(final ShrimpLabel label) {
		if (label instanceof PShrimpNodeLabel) {
			nodeLayer.addChild((PShrimpNodeLabel)label);
			((PShrimpNodeLabel)label).displayObjectPositionChanged();
			((PShrimpNodeLabel)label).updateVisibility();
			// fixed labels don't show up at with correct transform initially so place them again
			// don't know why this works but it does
			if (getDefaultLabelMode().equals(DisplayConstants.LABEL_MODE_FIXED)) {
			    ((PShrimpNodeLabel)label).displayObjectPositionChanged();
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.DisplayBean#setVisible(java.lang.Object, boolean, boolean)
	 */
	public void setVisible(Object obj, boolean visible, boolean assignDefaultPosition) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection)obj).iterator(); iter.hasNext();) {
				setVisible(iter.next(), visible, assignDefaultPosition);
			}
			return;
		}

		// make sure that this object can be set visible
		if (visible) {
			if (obj instanceof ShrimpNode) {
				// can't set node visible if artifact associated with this node is filtered
				// or this node has been pruned from the tree or has a collapsed ancestor
				ShrimpNode node = (ShrimpNode)obj;
				visible = !isFiltered (node.getArtifact()) && !node.hasBeenPrunedFromTree() && !node.hasCollapsedAncestor();
			} else if (obj instanceof ShrimpArc) {
				// can't set arc visible if its end nodes are not visible or in the display
				ShrimpArc arc = (ShrimpArc) obj;
				ShrimpNode srcNode = arc.getSrcNode();
				ShrimpNode destNode = arc.getDestNode();
				boolean srcNodeInDisplay = nodeIsInDisplay(srcNode);
				boolean destNodeInDisplay = nodeIsInDisplay(destNode);
				if (!srcNodeInDisplay || !isVisible (srcNode) || !destNodeInDisplay || !isVisible(destNode) || (arc.getRelationship() != null && isFiltered (arc.getRelationship()))) {
					visible = false;
				}
			} else if (obj instanceof ShrimpLabel) {
				// cant set label visible if its node is not visible
				ShrimpLabel label = (ShrimpLabel) obj;
				if (!isVisible(label.getLabeledObject())) {
					visible = false;
				}
			}
		}

		if (obj instanceof ShrimpDisplayObject){
			setDisplayObjectVisible((ShrimpDisplayObject)obj, visible, assignDefaultPosition);
		}
	}

	protected void setDisplayObjectVisible(ShrimpDisplayObject sdo, boolean visible, boolean assignDefaultPosition) {
		//sdo.setVisible(visible);
		// now check if it's a node or arc or label
		if (sdo instanceof ShrimpNode) {
			ShrimpNode sn = (ShrimpNode) sdo;
			sn.setVisible(visible);
			if (visible && !sn.getHasBeenTransformedOnce() && assignDefaultPosition) {
				setNewNodePosition(sn);
			}

			// update visibility of  arcs attached to this node
			Vector arcs = getDataDisplayBridge().getShrimpArcs(sn);
			for (Iterator iter = arcs.iterator(); iter.hasNext();) {
                ShrimpArc arc = (ShrimpArc) iter.next();
                arc.updateVisibility();
                ShrimpLabel sl = getDataDisplayBridge().getShrimpArcLabel(arc, false);
                if (sl != null) {
                    sl.updateVisibility();
                }
            }

			// If this ShrimpNode has a label in the
			// tree then update it's visibility
			ShrimpLabel sl = getDataDisplayBridge().getShrimpNodeLabel(sn, false);
			if (sl != null) {
				sl.updateVisibility();
			}
		}  else {
		    System.err.println("should be using updateVisibility(?)");
		}
	}

	/**
	  * Change the display mode of the panels of the Object
	  * If the Object is a Vector, change the display mode
	  * of all the Objects in the Vector.
	  * @param obj The Object whose panelMode is modified
	  * @param newMode The new panelMode
	  */
	 public void setPanelMode(Object obj, String newMode) {
		 // do nothing
	 }

	 public String getPanelMode(Object obj) {
		 return PanelModeConstants.CLOSED;
	 }

	/**
	  * Change the layout mode of the hierarchical view
	  * If the Object is a Vector, change the layout mode
	  * of all the Objects in the Vector.
	  * @param obj A Vector of artifacts to layout
	  * @param newMode The new layoutMode
	  * @param showDialog Whether or not to prompt the user with a layout dialog.
	  * @param animate Whether or not to animate the layout.
	  */
	 public void setLayoutMode(Object obj, String newMode, boolean showDialog, boolean animate) {
		 Layout layout = getLayout(newMode);
		 if (layout != null && obj instanceof Vector) {
			 lastLayoutMode = newMode;
			 Vector nodesToInclude = (Vector)obj;
			 layout.setupAndApplyLayout(nodesToInclude, getDisplayBounds(), new Vector(), false, animate, false);
		 }
	 }

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.DisplayBean#setPositionsAndSizes(java.util.Vector, java.util.Vector, java.util.Vector, boolean)
	 */
	 public void setPositionsAndSizes(Vector objects, Vector relationships, Vector positions, Vector dimensions, boolean animate) {
		setPositionsAndSizes(objects, relationships, positions, dimensions, animate, false);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.DisplayBean#continueMovingWithMouse(java.util.Collection)
	 */
	public void continueMovingWithMouse(Collection nodes) {
		continueMovingWithMouse(nodes, false);
	}

	/** Determines the size of nodes that are not accentuated. */
	private void setUnaccentuatedNodeSize() {
		// get first visible artifact in display and see how big it is
		ShrimpNode node = null;
		for (Iterator iter = getAllNodes().iterator(); iter.hasNext(); ) {
			node = (ShrimpNode) iter.next();
			if (!node.isVisible()) {
				node = null;
				break;
			}
		}
		if (node == null) {
			return;
		}
		Rectangle2D.Double bounds = node.getOuterBounds();

		AffineTransform tx = (AffineTransform) getTransformOf(node);
		unaccentuatedNodeWidth = (int) (bounds.getWidth() * tx.getScaleX());
		unaccentuatedNodeHeight = (int) (bounds.getHeight() * tx.getScaleY());
	}

	/**
	 * Unaccentuates all accentuated artifacts
	 */
	public void clearAccentuated() {
		accentuate(accentuatedNodes.clone(), false);
	}

	/**
	 * Accentuates (or unaccenuates) the passed in object. If the object is a vector
	 * all objects in the vector will be accentuated. If the object is a node,
	 * this node will be accentuated
	 */
	public void accentuate(Object obj, boolean on) {
		if (obj instanceof Vector) {
			Vector objs = (Vector)((Vector)obj).clone();
			for (Iterator iter = objs.iterator(); iter.hasNext();) {
				accentuate (iter.next(), on);
			}
		} else if (obj instanceof ShrimpNode) {
			accentuate ((ShrimpNode) obj, on);
		}
	}

	/**
	 * Accentuates (or unaccenuates) given the node.
	 */
	private void accentuate(ShrimpNode node, boolean on) {
		// accentuate only if not accentuated already
		// and unaccentuate only if accentuated already
		boolean proceed = false;
		if (on && !accentuatedNodes.contains(node)) {
			accentuatedNodes.add(node);
			proceed = true;
		} else if (!on && accentuatedNodes.contains(node)) {
			accentuatedNodes.remove(node);
			proceed = true;
		}
		if (proceed) {
			switch (currentAccentuateMode) {
				case ACC_BY_SIZE: accentuateBySize(node, on); break;
				default: // do nothing
			}
		}
	}

	/**
	 * Makes accentuated node twice as big, or at least MIN_ACCENTUATED_NODE_WIDTH wide
	 * and MIN_ACCENTUATED_NODE_HEIGHT tall, but not bigger than 80% of parent's size
	 */
	public void accentuateBySize(ShrimpNode node, boolean on){
		if (node == null) {
			return;
		}

		Rectangle2D.Double scaledBounds = node.getGlobalOuterBounds(); //global = scaled bounds
		double oldWidth = scaledBounds.getWidth();
		double oldHeight = scaledBounds.getHeight();
		double oldCenterX = scaledBounds.getX() + 0.5 * oldWidth; // this is the position of the center of the node
		double oldCenterY = scaledBounds.getY() + 0.5 * oldHeight; // this is the position of the center of the node

		double displayWidth = getDisplayBounds().getWidth();
		double displayHeight = getDisplayBounds().getHeight();
		double newWidth;
		double newHeight;
		if (on) {
			newWidth = Math.min (displayWidth * 0.8, Math.max (oldWidth * 2.0, displayWidth/10.0));
			newHeight = Math.min (displayHeight * 0.8, Math.max (oldHeight * 2.0, displayHeight/10.0));
			//fade the accentuated node
			node.setTransparency(0.5f);
		} else {
			newWidth = unaccentuatedNodeWidth;
			newHeight = unaccentuatedNodeHeight;
			//unfade the accentuated node
			node.setTransparency(1.0f);
		}

		Vector objects = new Vector();
		objects.add(node);
		Vector positions = new Vector();
		positions.add(new Point2D.Double (oldCenterX, oldCenterY));
		Vector dimensions = new Vector();
		int newDim = (int) Math.min(newWidth, newHeight);
		dimensions.add(new Dimension (newDim, newDim));
		setPositionsAndSizes(objects, new Vector(), positions, dimensions, false);
	}


	/**
	 * @param node
	 * @param on
	 */
	public void accentuateMouseOver(ShrimpNode node, boolean on) {
		// note: we do this via the activity scheduler so that the hierarchical view updates
		// while the shrimp view is busy zooming around
		PRoot r = getPCanvas().getCamera().getRoot();
		PActivity activity = getAccentuateActivity(node, on);
		PActivityScheduler scheduler = r.getActivityScheduler();
		ActivityManager finishedListener = new ActivityManager(scheduler, activity);
		waitForActivitiesToFinish(scheduler, finishedListener);
	}

	private PActivity getAccentuateActivity(final ShrimpNode node, final boolean on) {
		PActivity activity = new PActivity(-1) {
			protected void activityStep(long elapsedTime) {
				super.activityStep(elapsedTime);
				accentuateMouseOverInternal(node, on);
				terminate();
			}
		};
		return activity;
	}

	/**
	 * Takes care of accentuating a node when the mouse is over it.
	 * All the labels of its ancestors are shown
	 */
	private void accentuateMouseOverInternal(ShrimpNode node, boolean on) {
		if (node == null) {
			return;
		}
		Vector ancestors = dataDisplayBridge.getAncestorNodes(node);
		ancestors.add(node);
	    Vector nodes = new Vector(1);
	    if (on) {
	        nodes.add(node);
	    }
		for (Iterator iterator = ancestors.iterator(); iterator.hasNext(); ) {
			ShrimpNode ancestor = (ShrimpNode) iterator.next();
			ShrimpNodeLabel label = dataDisplayBridge.getShrimpNodeLabel(ancestor, true);
			label.updateVisibility(nodes);
		}
	}

	/**
	 * Sets the mode for accentuating nodes (ex. accentuate by size)
	 */
	public void setNewAccentuateMode(int newAccentuateMode) {
		// unnaccentuate all accentuated nodes in old mode
		clearAccentuated();

		// unnaccentuate all nodes in new mode
		currentAccentuateMode = newAccentuateMode;
		Vector nodes = getAllNodes();
		accentuate(nodes.clone(), false);
	}

	private boolean reapplyLayoutRequired() {
		//TODO not always true
		return true;
	}

   /**
	 * Change the display mode of labels of all objects.
	 *
	 *@param newMode The new label mode
	 */
	public void setDefaultLabelMode(String newMode) {
		String oldMode = defaultLabelMode;
		defaultLabelMode = newMode; // set label mode before doing anything else

		//@tag shrimp(fitToNodeLabelling)
		if 	((!DisplayConstants.isLabelOnNode(oldMode)) &&
				DisplayConstants.isLabelOnNode(newMode)) {
			//when switching from sticky labels to labels on nodes
			// show the labels on the nodes and remove sticky labels from the display
 	 		// @tag Shrimp(labelling)
			Vector nodes = getAllNodes();
			for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
				ShrimpNode node = (ShrimpNode) iterator.next();
				//remove sticky label
				ShrimpLabel ssl = dataDisplayBridge.getShrimpNodeLabel(node, false);
				if (ssl != null) {
					removeObject (ssl);
				}
			}
		} else {
			//when switching from one type of sticky label to another
			// just update the existing labels
			Vector labels = getAllLabels();
			for (Iterator iterator = labels.iterator(); iterator.hasNext();) {
				ShrimpLabel label = (ShrimpLabel) iterator.next();
				label.displayObjectPositionChanged();
				if (label instanceof ShrimpNodeLabel) {
				    ((ShrimpNodeLabel)label).updateVisibility();
				    ((ShrimpNodeLabel)label).displayObjectPositionChanged();
				}
			}

		}
	}

	/** Reapplies the current layout */
	public void refreshLayout(boolean animate){
		if (reapplyLayoutRequired()) {
			super.refreshLayout(getVisibleNodes(), animate);
		}
		// make sure accentuated nodes are still accentuated after applying layout
		setUnaccentuatedNodeSize();
		reAccentuate();
	}

	public void reAccentuate() {
		Vector accentuatedNodesCopy = (Vector) accentuatedNodes.clone();
		accentuate(accentuatedNodes, false);
		accentuate(accentuatedNodesCopy, true);
	}

	/** Applies the radial layout */
	public void applyRadialLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_RADIAL, false, animate);
		setUnaccentuatedNodeSize();
	}

	/** Applies the hierarchical layout */
	public void applyHierarchicalLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_HIERARCHICAL, false, animate);
		setUnaccentuatedNodeSize();
	}

	/** Applies the orthogonal layout */
	public void applyOrthogonalLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_ORTHOGONAL, false, animate);
		setUnaccentuatedNodeSize();
	}

	/** Applies the sequence layout */
	public void applySequenceLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_SEQUENCE, false, animate);
		setUnaccentuatedNodeSize();
	}

	/**
	 * Applies the Sugiyamalayout
	 * @tag Shrimp.sugiyama
	 */
	public void applySugiyamaLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_SUGIYAMA, false, animate);
		setUnaccentuatedNodeSize();
	}

	/** Applies the tree layout */
	public void applyVerticalTreeLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_TREE_VERTICAL, false, animate);
		setUnaccentuatedNodeSize();
	}

	/** Applies the tree layout */
	public void applyHorizontalTreeLayout(boolean animate){
		setLayoutMode(getVisibleNodes(), LayoutConstants.LAYOUT_TREE_HORIZONTAL, false, animate);
		setUnaccentuatedNodeSize();
	}

	/**
	 *  Collapse a single node.  Hides all descendants.
	 */
	private void collapseNode(ShrimpNode node) {
		// if this node is already collapsed, don't collapse it again
		if (node.isCollapsed()) {
			return;
		}
		// if no children then don't collapse this node
		if (getDataDisplayBridge().getChildNodesCount(node) == 0) {
			return;
		}
		// hide all descendents of current node, if not already hidden by another collapse
		Vector allDescendents = dataDisplayBridge.getDescendentNodes(node, false);
		Vector hiddenDescendents = new Vector();
		for (Iterator iter = allDescendents.iterator(); iter.hasNext(); ) {
			ShrimpNode descendent = (ShrimpNode) iter.next();
			descendent.setHasCollapsedAncestor(true);
			if (!allCollapsedDescendents.contains(descendent)) {
				setVisible (descendent, false, true);
				allCollapsedDescendents.add (descendent);
				hiddenDescendents.add(descendent);
			}
			// record a map from the newly collapsed node, to the descendents
			// hidden from the collapse
			if (!hiddenDescendents.isEmpty()) {
				collapseParentToDescendents.put(node, hiddenDescendents);
			}
		}
		collapseParents.add(node);
		node.setIsCollapsed(true);
	}

	/**
	 *  Collapse a vector of nodes.
	 */
	public void collapseNodes(Collection nodes) {
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			collapseNode((ShrimpNode) iter.next());
		}
	}

	/**
	 *  Expand all collapsed nodes.
	 */
	public void expandAll() {
		expandNodes((Vector)collapseParents.clone());
	}

	/**
	 * Expand the sub tree.
	 * Can be restricted to x number of levels, or can expand entire subtree.
	 * @param node The node to expand
	 * @param restrictToLevel Whether or not to restrict the expansion to a certain number of levels.
	 * @param levels If restrictToLevel is true, this is the number of levels to restrict the expansion to.
	 * If restrictToLevel is false, this parameter is ignored.
	 */
	public void expandSubTree(ShrimpNode node, boolean restrictToLevel, int levels) {
		boolean displayBeanVisible = isVisible();
		ProgressDialog.showProgress();
		String subtitle = restrictToLevel ? levels + " Levels" : " All Descendents";
		ProgressDialog.setSubtitle("Expanding " + subtitle + " - Updating Display...");
		ProgressDialog.setNote ("");
		Vector descendents = dataDisplayBridge.getDescendentNodes(node, true, true, false, restrictToLevel, levels);
		node.setMarked(false);
		int numDescendents = descendents.size();
		try {
			expandSubTreeRecursive(node, numDescendents + 1, restrictToLevel, levels, 0);
			refreshLayout(false);
			focusOnExtents(true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    ProgressDialog.tryHideProgress();
			setVisible(displayBeanVisible);
		}
	}

	/**
	 * @param node node to expand on
	 * @param numDescendents number of descendents to iterate
	 * @restrictToLevel whether or not to restrict further expansion based on levelsToGo
	 * @param levelsToGo levels left to descend
	 * @param numLevelsExpanded a simple count of the number of nodes expanded so far
	 */
	private void expandSubTreeRecursive(ShrimpNode node, int numDescendents, boolean restrictToLevel, int levelsToGo, int numNodesExpanded) {
        if (ProgressDialog.isCancelled()) {
            return;
        }
        if (node.isMarked()) {
            return;
        }
        node.setMarked(true);
        expandNode(node); // TODO shouldn't keep expanding descendents unless we really have to
        numNodesExpanded++;
        if (numNodesExpanded % 10 == 0) {
            ProgressDialog.setNote(numNodesExpanded + " of " + numDescendents + " expanded - " + node.getArtifact().getName());
        }
        if (!restrictToLevel || levelsToGo > 1) {
            Vector children = dataDisplayBridge.getChildNodes(node, true);
            for (Iterator iter = children.iterator(); iter.hasNext();) {
                ShrimpNode child = (ShrimpNode) iter.next();
                expandSubTreeRecursive(child, numDescendents, restrictToLevel, levelsToGo - 1, numNodesExpanded);
            }
        }
	}

	/**
	 * @param node
	 * @param collapseNewNodes
	 */
	private boolean expandNode(ShrimpNode node) {
		// don't bother expanding if node not collapsed
		if (!node.isCollapsed()) {
			return false;
		}
		if (!node.haveChildrenBeenAdded()) {
			//this node has never been expanded before so just add its children
			Vector children = dataDisplayBridge.getChildNodes(node, true);
            Vector childrenToAdd = new Vector (children.size());
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				ShrimpNode childNode = (ShrimpNode) iter.next();
                // because of multiple parents, a child node may be in the display already because of expanding another parent
                if (!childNode.isInDisplay()) {
                    childrenToAdd.add(childNode);
                    childNode.setIsCollapsed(dataDisplayBridge.getChildNodesCount(childNode) > 0); // make child node collapsed by default, if it has children
                }
			}
			addObject(childrenToAdd);
			setVisible(childrenToAdd, true, true);
			node.setChildrenAdded(true);
		} else {
			// show all descendents that have been hidden because of the current node but only if not pruned
			Vector hiddenDescendents = (Vector)collapseParentToDescendents.get(node);
			if (hiddenDescendents != null) {
				for (Iterator iter = hiddenDescendents.iterator(); iter.hasNext(); ) {
					ShrimpNode descendent = (ShrimpNode) iter.next();
					descendent.setHasCollapsedAncestor(false);
					if (!prunedNodes.contains(descendent)) {
						setVisible(descendent, true, true);
                    }
					allCollapsedDescendents.remove(descendent);
				}
			}
			collapseParentToDescendents.remove(node);
		}

		collapseParents.remove(node);
		node.setIsCollapsed(false);
    	return true;
	}

	/**
	 * Expand a list of collapsed nodes.
	 */
	public void expandNodes(Collection nodes) {
		boolean oneNodeExpanded = false;
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			ShrimpNode node = (ShrimpNode) iter.next();
			if (node.isCollapsed() && dataDisplayBridge.getChildNodesCount(node) > 0) {
				oneNodeExpanded = true;
			}
			expandNode(node);
		}
		if (oneNodeExpanded) {
			refreshLayout(true);
		}
	}

	/**
	 * Makes the node into the root node.
	 * Hide every node except the selected one and its descendents.
	 */
	public void makeRootNode(ShrimpNode current) {
		Vector descendents = dataDisplayBridge.getDescendentNodes(current, false);
		Vector nodes = getAllNodes();
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode)nodes.elementAt(i);
			if (!node.equals(current) && !descendents.contains(node)) {
				setVisible(node, false, false);
				node.setHasBeenPrunedFromTree(true);
				prunedNodes.add(node);
			}
		}

		pruneParent = current;
		current.setIsPruneRoot(true);

		// is it worth doing a layout now?
	}

	/**
	 * Show all artifacts that have been pruned, if not collapsed.
	 */
	public void restoreOriginalRoot() {
		for (int i = 0; i < prunedNodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) prunedNodes.elementAt(i);
			node.setHasBeenPrunedFromTree(false);
			if (!allCollapsedDescendents.contains(node)) {
				setVisible(node, true, true);
			}
		}
		prunedNodes = new Vector();
		pruneParent.setIsPruneRoot(false);
		pruneParent = null;
		refreshLayout(false);
	}

	/**
	 * Returns nodes that have been collapsed
	 */
	public Vector getCollapsedNodes() {
		return (Vector) collapseParents.clone();
	}

	/**
	 * Returns node that tree has been pruned to.
	 * Returns null if the tree has not been pruned.
	 */
	public ShrimpNode getPrunedToNode() {
		return pruneParent;
	}

	/**
	 * Start zooming in
	 */
	public void startZoomingIn(){
		//We override this method to tell the zoom handler the maximum magnification
		//Should use a better guide than the number of nodes
		zoomHandler.setMaxMagnification(this.getVisibleNodes().size()/5.0);
		super.startZoomingIn();
	}

	/**
	 * Start zooming out.
	 */
	public void startZoomingOut(){
		//We override this method to tell the zoom handler the minimum magnification
		this.zoomHandler.setMinMagnification(0.5);
		super.startZoomingOut();
	}

	public boolean isFlat() {
		return true;
	}

}