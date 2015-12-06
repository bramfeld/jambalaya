/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;

import org.eclipse.mylar.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.mylar.zest.layouts.LayoutAlgorithm;
import org.eclipse.mylar.zest.layouts.LayoutEntity;
import org.eclipse.mylar.zest.layouts.LayoutRelationship;
import org.eclipse.mylar.zest.layouts.progress.ProgressEvent;
import org.eclipse.mylar.zest.layouts.progress.ProgressListener;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.gui.ProgressDialog;

/**
 *
 * @author Rob Lintern, Chris Callendar
 */
public abstract class AbstractLayout implements Layout {

	private static final double EXCLUDED_HEIGHT_PERCENT = 0.10d; // the percentage of the height of the layout bounds to be used for excluded nodes

	private String name;
	protected DisplayBean displayBean;
	protected LayoutAlgorithm layoutAlgorithm;
	protected boolean inverted;

	// Child classes can set to false to retain node shapes and sizes
	protected boolean resizeEntitiesAfterLayout = true;

	private Collection arcTypes;

	public AbstractLayout(DisplayBean displayBean, String name, LayoutAlgorithm layoutAlgorithm) {
	    this(displayBean, name, layoutAlgorithm, false);
	}

	public AbstractLayout(DisplayBean displayBean, String name, LayoutAlgorithm layoutAlgorithm, boolean inverted) {
		this.displayBean = displayBean;
		this.name = name;
		this.layoutAlgorithm = layoutAlgorithm;
		this.inverted = inverted;
		this.arcTypes = Collections.EMPTY_LIST;

		// @tag Shrimp(sequence) : Reset to true by default
		displayBean.setNodeEdgeMovement(true);
	}

	/**
	 * Main reset method - uses template pattern to call more
	 * specific reset method
	 */
	public void resetLayout() {
		displayBean.getStructuralGroupingManager().clearGroupedNodeFilter();
		reset();
	}

	/**
	 * Child classes can implement this method to reset state prior
	 * to processing. This method is called as part of the standard
	 * layout processing by the display bean prior to calculating
	 * node lists.
	 * Usage example: The SequenceLayout class sets some
	 * node filters for node grouping, which must be reset prior to
	 * calculating node lists.
	 */
	public void reset() {
		// Override this method in child classes if needed
	}

	/**
	 * Sets the arc types of interest.
	 * If null or empty then all arcs are included when the layout is run.
	 */
	public void setArcTypes(Collection arcTypes) {
		this.arcTypes = (arcTypes == null ? Collections.EMPTY_LIST : arcTypes);
	}

	/**
	 * Checks to see if the arc's type is in the arc types collection.
	 * If so (or if the arc types collection is empty) then true is returned.
	 */
	public boolean includeArc(ShrimpArc arc) {
		boolean include = true;
		if (!arcTypes.isEmpty() && (arc != null)) {
			String type = arc.getType();
			if (!arcTypes.contains(type)) {
				include = false;
			}
		}
		return include;
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#applyLayout(java.util.Vector, java.awt.geom.Rectangle2D.Double, java.util.Vector, boolean, boolean)
	 */
	public void setupAndApplyLayout(Vector nodes, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents) {
		// No need to go further.
		if(nodes.size() == 0) {
			return;
		}

		displayBean.getStructuralGroupingManager().handleNodeGrouping(nodes);

		Vector addedChildren = new Vector();
		Vector addedArcs = new Vector();
		for (Iterator nodeIter = nodes.iterator(); nodeIter.hasNext();) {
			ShrimpNode child = (ShrimpNode) nodeIter.next();
			boolean unconnected = true;
			if (displayBean.isVisible(child)) {
				Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(child);
				for (Iterator arcsIter = arcs.iterator(); arcsIter.hasNext();) {
					ShrimpArc arc = (ShrimpArc) arcsIter.next();
					arc.setInvertedInLayout(inverted);
		            ShrimpNode srcNode = inverted ? arc.getDestNode() : arc.getSrcNode();
		            ShrimpNode destNode = inverted ? arc.getSrcNode() : arc.getDestNode();
					if (nodes.contains(srcNode) && nodes.contains(destNode) && displayBean.isVisible(arc) && includeArc(arc)) {
						unconnected = false;
						if ((srcNode != destNode) && !addedArcs.contains(arc)) {
							addedArcs.add(arc);
						}
					}
				}

				// if no relationships added for this artifact then add it to list of
				// nodes to exclude from this layout, else add it to the layout.
				if (unconnected) {
					nodesToExclude.add(child);
				} else {
					addedChildren.add(child);
				}
			}
		}

		if ((addedChildren.size() == 0) && (nodesToExclude.size() == 0)) {
			return;
		}

		if (separateComponents) {
		    if (addedChildren.isEmpty() && !nodesToExclude.isEmpty()) {
			    positionIncludedAndExcludedNodes(nodesToExclude, addedArcs, new Vector(0), bounds, animate);
		    } else {
		        positionIncludedAndExcludedNodes_Components(addedChildren, addedArcs, nodesToExclude, bounds, animate);
		    }
		} else {
		    positionIncludedAndExcludedNodes(addedChildren, addedArcs, nodesToExclude, bounds, animate);
		}

		// @tag Shrimp(SourceAutoDisplay): Zoom so that source is legible
		if (displayBean instanceof PNestedDisplayBean) {
//	        SwingUtilities.invokeLater(new Runnable () {
//                public void run() {
//					// TODO - use last point of focus rather than centre
//    			    ((PNestedDisplayBean)displayBean).startZoomingIn(getCentreX(), getCentreY(), calcLegibleTextMagnification());
//    				((PNestedDisplayBean)displayBean).stopZooming();
//                }
//
//                /**
//                 * Assume that the layout allowed for legible text in its own bounds
//                 */
//				private double calcLegibleTextMagnification() {
//					final double LEGIBLE_SIZE = 5; // This is a heuristic based on the font used in fit or wrap to node
//					int nNodesCounted=0;
//					double magnification = 0;
//					// Sum all magnifications
//					for (Iterator nodeIter = displayBean.getAllNodes().iterator(); nodeIter.hasNext();) {
//						ShrimpNode node= (ShrimpNode) nodeIter.next();
//						if (node.getName().length() > 0) {
//							nNodesCounted++;
//							double textSize = node.getWidthInLayout()/node.getName().length();
//							magnification += LEGIBLE_SIZE/textSize;
//						}
//					}
//					return nNodesCounted > 0 ? magnification/nNodesCounted : 1.0;
//				}
//
//				private double getCentreX() {
//					return ((PNestedDisplayBean)displayBean).getDisplayBounds().getX()+
//					((PNestedDisplayBean)displayBean).getDisplayBounds().getWidth()/2;
//				}
//
//				private double getCentreY() {
//					return ((PNestedDisplayBean)displayBean).getDisplayBounds().getY()+
//					((PNestedDisplayBean)displayBean).getDisplayBounds().getHeight()/2;
//				}
//            });
		}
	}



	/**
	 * Apply layout to a vector of nodes, taking into account the passed in arcs.
	 * @param nodes A vector of ShrimpNodes. The nodes to organize.
	 * @param arcs A vector of ShrimpArcs to include in the layout.
	 * @param bounds The bounds to fit the artifacts into.
	 * @param animate Whether or not to animate this layout.
	 * @param componentNum Indicates which component this is.
	 * @param numComponents Indicates total number of components.
	 * @param positions To be populated with positions for the passed in nodes.
	 * @param dimensions To be populated with the dimensions for the passed in nodes.
	 *
	 * @return Whether or not the layout was performed successfully.
	 */
	private boolean applyLayoutTo(final Vector nodes, final Vector arcs, Rectangle2D.Double bounds,
			int componentNum, int numComponents, Vector positions, Vector dimensions) {
       boolean success = true;
	    if (showProgress()) {
            ProgressDialog.showProgress();
            ProgressDialog.setSubtitle(getName() + " (component " + componentNum + " of " + numComponents + ") ...");
            ProgressDialog.setNote("");
        }
	    List layoutEntities = new ArrayList();
	    List layoutRelationships = new ArrayList();
	    ProgressListener progressListener = new ProgressListener() {
            public void progressUpdated(ProgressEvent e) {
                //System.out.println("progressUpdated");
                if (showProgress()) {
                    if (ProgressDialog.isCancelled()) {
                        // stop the layout algorithm from running anymore
                        layoutAlgorithm.removeProgressListener(this);
                        layoutAlgorithm.stop();
                    } else {
                        String note = e.getStepsCompleted() + " of " + e.getTotalNumberOfSteps() + " steps complete...";
                        ProgressDialog.setNote(note);
                    }
                }
                if (animateIterations(nodes.size(), arcs.size())) {
                    updateGUIImmediately(nodes, arcs);
                }
            }
            public void progressStarted(ProgressEvent e) {
                //System.out.println("progressStarted");
            }
            public void progressEnded(ProgressEvent e) {
                //System.out.println("progressEnded");
            }
	    };

        layoutAlgorithm.addProgressListener(progressListener);

	    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            ShrimpNode shrimpNode = (ShrimpNode) iter.next();
            layoutEntities.add(shrimpNode);
        }

	    for (Iterator iter = arcs.iterator(); iter.hasNext();) {
            ShrimpArc shrimpArc = (ShrimpArc) iter.next();
            if (!layoutRelationships.contains(shrimpArc)) {
                layoutRelationships.add(shrimpArc);
            } else {
                System.err.println("duplicate arc!");
            }
        }

	    try {
            LayoutEntity[] entitiesArr = new LayoutEntity [layoutEntities.size()];
            layoutEntities.toArray(entitiesArr);
            LayoutRelationship[] relationshipsArr = new LayoutRelationship [layoutRelationships.size()];
            layoutRelationships.toArray(relationshipsArr);
            layoutAlgorithm.applyLayout(entitiesArr, relationshipsArr, bounds.x, bounds.y, bounds.width, bounds.height, false, false);
            positionAndSizeNodesAfterLayout(nodes, positions, dimensions);
        } catch (InvalidLayoutConfiguration e1) {
            e1.printStackTrace();
            success = false;
        }
        layoutAlgorithm.removeProgressListener(progressListener);
	    if (showProgress()) {
            ProgressDialog.tryHideProgress();
	    }
        return success;
	}

	/**
	 * Iterates through the {@link PShrimpNode} objects and sets the position ({@link Point2D.Double})
	 * and size ({@link Dimension}) for each one in the positions and dimensions {@link Vector}s.
	 * @param nodes the nodes to be positioned and sized
	 * @param positions the positions for each node to be set
	 * @param dimensions the sizes of each node to be set
	 */
	protected void positionAndSizeNodesAfterLayout(Vector nodes, Vector positions, Vector dimensions) {
	    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			PShrimpNode node = (PShrimpNode) iter.next();
			double w = node.getWidthInLayout();
			double h = node.getHeightInLayout();
			// @tag Shrimp.Bendpoints
			if (resizeEntitiesAfterLayout) { // ok to resize things for this layout, so ensure min size
				w = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, w);
				h = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, h);
			}

			// Restore node position to centre point (layout positions at top left corner)
			double x = node.getXInLayout() + (w / 2.0);
			double y = node.getYInLayout() + (h / 2.0);
			positions.add(new Point2D.Double(x, y));
			dimensions.add(new Dimension((int)w, (int)h));
		}

	    // subclasses like HorizontalLayout override this method
	    adjustPositionsAndSizesAfterLayout(nodes, positions, dimensions);
	}

    /**
     * This method does nothing - subclasses can override this method to perform sizing and positioning
     * of nodes after the layout has been completed.
     * @param nodes the nodes in question
     * @param positions the Vector of node positions (already set)
     * @param dimensions the Vector of node sizes (already set)
     */
    protected void adjustPositionsAndSizesAfterLayout(Vector nodes, Vector positions, Vector dimensions) {
    	// subclasses can implement this method
    }

    protected boolean showProgress() {
        return false;
    }

    protected boolean animateIterations(int nodeCount, int arcCount) {
        return false; //nodeCount + arcCount < 200;
    }

    /**
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * --------------
	 * |			|
	 * | included   |
	 * |			|
	 * |			|
	 * --------------
	 * | excluded   |
	 * --------------
	 *
	 * @param nodesToInclude
	 * @param arcsToInclude
	 * @param nodesToExclude
	 * @param entireBounds
	 * @param animate
	 */
	protected void positionIncludedAndExcludedNodes(Vector nodesToInclude, Vector arcsToInclude, Vector nodesToExclude, Rectangle2D.Double entireBounds, boolean animate) {
	    positionIncludedAndExcludedNodes(nodesToInclude, arcsToInclude, nodesToExclude, entireBounds, animate, false);
	}

	protected void positionIncludedAndExcludedNodes(Vector nodesToInclude, Vector arcsToInclude, Vector nodesToExclude, Rectangle2D.Double entireBounds, boolean animate, boolean flipXandY) {
		double excludedHeight = entireBounds.getHeight() * EXCLUDED_HEIGHT_PERCENT;
		double includedHeight = entireBounds.getHeight() - excludedHeight;

		Rectangle2D.Double includedBounds = (!flipXandY) ? 	new Rectangle2D.Double (entireBounds.getX(), entireBounds.getY(), entireBounds.getWidth(), includedHeight) :
															new Rectangle2D.Double (entireBounds.getY(), entireBounds.getX(), includedHeight, entireBounds.getWidth());
		Vector positions = new Vector(nodesToInclude.size());
		Vector dimensions = new Vector(nodesToInclude.size());
		boolean layoutApplied = applyLayoutTo(nodesToInclude, arcsToInclude, includedBounds, 1, 1, positions, dimensions);
		if (layoutApplied) {
		    displayBean.setPositionsAndSizes(nodesToInclude, arcsToInclude, positions, dimensions, animate);
			positionExcludedNodes(nodesToExclude, entireBounds, animate, excludedHeight, includedHeight);
		}
	}

	protected void positionIncludedAndExcludedNodes_Components(Vector nodesToInclude, Vector arcsToInclude, Vector nodesToExclude, Rectangle2D.Double entireBounds, boolean animate) {
		positionIncludedAndExcludedNodes_Components(nodesToInclude, arcsToInclude, nodesToExclude, entireBounds, animate, false);
	}

	protected void positionIncludedAndExcludedNodes_Components(Vector nodesToInclude, Vector arcsToInclude, Vector nodesToExclude, Rectangle2D.Double entireBounds, boolean animate, boolean flipXandY) {
		double excludedHeight = entireBounds.getHeight() * EXCLUDED_HEIGHT_PERCENT;
		double includedHeight = entireBounds.getHeight() - excludedHeight;

		Rectangle2D.Double includedBounds = (!flipXandY) ? new Rectangle2D.Double (entireBounds.getX(), entireBounds.getY(), entireBounds.getWidth(), includedHeight) :
															new Rectangle2D.Double (entireBounds.getY(), entireBounds.getX(), includedHeight, entireBounds.getWidth());

		// Determine connected components
		ComponentizeLayoutUtil compUtil = new ComponentizeLayoutUtil(displayBean, nodesToInclude);
		compUtil.computeComponents();
		Vector connectedComps = compUtil.getComponents();

		// components with 1 element get added to nodesToExlude
		Vector ccsClone = (Vector) connectedComps.clone();
		for (int i = 0; i < ccsClone.size(); i++) {
			ConnectedComponent connectedComp = (ConnectedComponent) ccsClone.elementAt(i);
			Vector singleNodes = connectedComp.getNodes();
			if (singleNodes.size() == 1 && nodesToInclude.size() > 1) {
				nodesToExclude.addAll(singleNodes);
				connectedComps.remove(connectedComp); // take this con comp out of the list
			}
		}

		//Compute space for each component to display in
		compUtil.setBoundsForAll(includedBounds);
		compUtil.setComponents(connectedComps);
		compUtil.computeComponentBounds();

		int componentNum = 1;
		int numComponents = compUtil.getNumComponents();
		Vector allIncludedNodes = new Vector(nodesToInclude.size());
		Vector allPositions = new Vector(nodesToInclude.size());
		Vector allDimensions = new Vector(nodesToInclude.size());
		//Vector allDimensions = new Vector(nodesToInclude.size());
		int minWidth = Integer.MAX_VALUE;
		int minHeight = Integer.MAX_VALUE;
		for (int i = 0; i < connectedComps.size(); i++) {
			ConnectedComponent comp = (ConnectedComponent)connectedComps.elementAt(i);
			Vector nds = comp.getNodes();
			Vector arcs = comp.getArcs();
			// set new position/size for child
			Vector positions = new Vector(nds.size());
			Vector dimensions = new Vector(nds.size());
			boolean layoutApplied = applyLayoutTo(nds, arcs, comp.getBounds(), componentNum, numComponents, positions, dimensions);
			if (layoutApplied) {
				allIncludedNodes.addAll(nds);
				allPositions.addAll(positions);
				// @tag Shrimp.Bendpoints : Will need to flag this so that the layout may
				// specify if the same dimension should be used for all nodes (as in the commented code below)
				allDimensions.addAll(dimensions);
			for (Iterator iter = dimensions.iterator(); iter.hasNext();) {
                    Dimension2D dim = (Dimension2D) iter.next();
                    if (dim.getWidth() < minWidth) {
                        minWidth = (int)dim.getWidth();
                    }
                    if (dim.getHeight() < minHeight) {
                        minHeight = (int)dim.getHeight();
                    }
                }
			}
			componentNum++;
		}

//		 @tag Shrimp.Bendpoints : Need to flag this code based on layout preference
//		Dimension globalDim = new Dimension (minWidth, minHeight);
		//use the same dimensions for all components
//		Vector allDimensions = new Vector (allIncludedNodes.size());
//		for (int i=0; i < allIncludedNodes.size(); i++) {
//		    allDimensions.add(globalDim);
//		}
		displayBean.setPositionsAndSizes(allIncludedNodes, arcsToInclude, allPositions, allDimensions, animate);
		if (displayBean.getSwitchLabelling() &&
			allIncludedNodes.size() >= displayBean.getSwitchAtNum()) {
			displayBean.setLabelMode(allIncludedNodes, DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
		} else {
			displayBean.setLabelMode(allIncludedNodes, displayBean.getDefaultLabelMode());
		}

		Rectangle2D.Double excludedBounds = new Rectangle2D.Double ();
		if (nodesToInclude.isEmpty() && !nodesToExclude.isEmpty()) {
		    excludedBounds = entireBounds;
		} else {
		    excludedBounds.setFrame(entireBounds.getX(), entireBounds.getY() + includedHeight,  entireBounds.getWidth(), excludedHeight);
		}

		positionExcludedNodes(nodesToExclude, excludedBounds, animate, excludedHeight, includedHeight);

	}

	/**
	 * Excluded nodes will be placed in a grid layout at the bottom of the given bounds
	 * @param nodesToExclude
	 * @param entireBounds
	 * @param animate
	 * @param excludedHeight
	 * @param includedHeight
	 */
	protected void positionExcludedNodes(Vector nodesToExclude, Rectangle2D.Double bounds, boolean animate, double excludedHeight, double includedHeight) {
	    // make sure that we are just dealing with visible excluded nodes
	    Vector visibleNodesToExclude = new Vector ();
	    for (Iterator iter = nodesToExclude.iterator(); iter.hasNext();) {
            ShrimpNode node = (ShrimpNode) iter.next();
            if (node.isVisible()) {
                visibleNodesToExclude.add(node);
            }
        }

		//create a temporary grid layout to do the work
		ShrimpGridLayout grid = new ShrimpGridLayout(displayBean);
		grid.setupAndApplyLayout(visibleNodesToExclude, bounds, new Vector(), false, animate, false);
	}

	/**
	 * returns the name of this layout, possibly followed by some paramater descriptions
	 */
	public String toString() {
		return "Layout: " + getName();
	}

	private static long lastUpdate = System.currentTimeMillis();

	protected void updateGUIImmediately (Vector nodes) {
		updateGUIImmediately(nodes, new Vector(0));
	}

	protected void updateGUIImmediately (Vector nodes, Vector arcs) {
	    long newTime = System.currentTimeMillis();
	    if (newTime - lastUpdate < 50) {
	    	return;
	    }
        Vector tmpObjects = new Vector (nodes.size());
        Vector tmpPositions = new Vector (nodes.size());
        Vector tmpDimensions = new Vector (nodes.size());
	    for (Iterator iter = nodes.iterator(); iter.hasNext();) {
            PShrimpNode shrimpNode = (PShrimpNode) iter.next();
            tmpObjects.add(shrimpNode);
            double x = shrimpNode.getXInLayout();
            double y = shrimpNode.getYInLayout();
            // @tag Shrimp.minNodeSize
            double w; double h;
			if (resizeEntitiesAfterLayout) { // ok to resize things for this layout, so ensure min size
				w = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, shrimpNode.getWidthInLayout());
				h = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, shrimpNode.getHeightInLayout());
			} else {
				w = shrimpNode.getWidthInLayout();
				h = shrimpNode.getHeightInLayout();
			}
            tmpPositions.add(new Point2D.Double (x + w/2.0, y + h/2.0));
            tmpDimensions.add(new Dimension ((int)w, (int)h));
        }
        displayBean.setPositionsAndSizes(tmpObjects, arcs, tmpPositions, tmpDimensions, false);
        JComponent canvas = ((PNestedDisplayBean)displayBean).getPCanvas();
        boolean isInteracting = displayBean.isInteracting();
        displayBean.setInteracting(true);
        canvas.paintImmediately(0, 0, canvas.getWidth(), canvas.getHeight());
        displayBean.setInteracting(isInteracting);
        lastUpdate = newTime;
	}

}