/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.util.GeometryUtils;


/**
 * The <code>FisheyeZoomHandler</code> uses an orthogonal
 * zoom algorithm to browse a hierarchical graph structure.
 * The middle mouse button is reserved for shrinking, and
 * the right mouse button is reserved for magnifying.
 *
 * @author  Jingwei Wu
 */
public class PFisheyeZoomHandler extends MouseAdapter  {
	
	/***************************************************************
	 * Note:                                                       *
	 * For each fisheye zooming action(step), there will be a focal*
	 * node to which the fisheye zooming action is applied. All of *
	 * its siblings will behave accordingly.                       *
	 ***************************************************************/

	/**
	 * The default scale value to shrink the focal node.
	 */
	public static final double DEFAULT_SHRINK_SCALE = 0.8f;

	/**
	 * The default scale value to magnify the focal node.
	 */
	public static final double DEFAULT_MAGNIFY_SCALE = 1.25f;

	/**
	 * The default fisheye magnifying limit value for zooming the focal node.
	 */
	public static final double DEFAULT_MAGNIFY_LIMIT = 0.95f;
	//public static final double DEFAULT_MAGNIFY_LIMIT = 0.6f;

	/**
	 * Determines if the fisheye zooming action in the current level will be
	 * propagated to the upper level. The default value is <code>true</code>.
	 */
	protected boolean propagateToUpperLevel = false;

	/**
	 * The scale value to magnify the focal node. The preferred value
	 * is the {@link #DEFAULT_MAGNIFY_SCALE DEFAULT_MAGNIFY_SCALE}.
	 */
	protected double magnifyScale = DEFAULT_MAGNIFY_SCALE;

	/**
	 * The scale value to shrink the focal node. The preferred value
	 * is the {@link #DEFAULT_SHRINK_SCALE DEFAULT_SHRINK_SCALE}.
	 */
	protected double shrinkScale = DEFAULT_SHRINK_SCALE;

	/**
	 * The fisheye magnifying limit value. The ratio of the focal node's size
	 * to its parent's size should not be greater than this limit value when
	 * the focal node is being magnified. The suggested value for this field
	 * is {@link DEFAULT_MAGNIFY_LIMIT DEFAULT_MAGNIFY_LIMIT}.
	 * <p>
	 * Once this limit is reached, fisheye zooming will be propagated to the
	 * upper level if {@link #propagateToUpperlevel propagateToUpperLevel} is
	 * <code>true</code>. 
	 * @see #DEFAULT_MAGNIFY_LIMIT
	 */
	protected double magnifyLimit = DEFAULT_MAGNIFY_LIMIT;

	/**
	 * The focal node.
	 */
	private ShrimpNode focalNode = null;

	/**
	 * The focal node's real parent.  
	 * This will be null for flat graphs, or when the root node is the focal node in a nested graph. 
	 */
	private ShrimpNode focalParent = null;

	/**
	 * The focal node's restriction.
	 */
	private ShrimpNode focalRestrict = null;

	/**
	 * The anchor point relative to the current real estate bounds
	 * for the focal node
	 */
	private Point2D focalAnchor = new Point2D.Double();

	/**
	 * The focal node's previous bounds before the current fisheye
	 * zooming action is applied.
	 */
	private Rectangle2D.Double focalPrevBounds = new Rectangle2D.Double();

	/**
	 * The focal node's current bounds after the current fisheye
	 * zooming action is applied.
	 */
	private Rectangle2D.Double focalCurrBounds = new Rectangle2D.Double();

	/**
	 * The real estate bounds available for fisheye zooming. This
	 * bounds is determined by the focal node's parent and other
	 * user requirements.
	 */
	private Rectangle2D.Double realEstateBounds = new Rectangle2D.Double();

	/**
	 * Determines if the fisheye zooming action should continue.
	 */
	private boolean zooming = false;

	/**
	 * The current fisheye zooming choice. Its value must be either
	 * {@link #ZOOMIN ZOOMIN} or {@link #ZOOMOUT ZOOMOUT}.
	 */
	private boolean zoomChoice;

	/**
	 * Indicates the fisheye zooming action is magnifying(zooming in)
	 * the focal node.
	 * @see #zoomChoice
	 */
	private static final boolean ZOOMIN = true;

	/**
	 * Indicates the fisheye zooming action is shrinking(zooming out)
	 * the focal node.
	 * @see #zoomChoice
	 */
	private static final boolean ZOOMOUT = false;

	/**
	 * The scale value to magnify/shrink the focal node. This variable
	 * will be assigned the value of {@link #magnifyScale magnifyScale}
	 * or {@link #shrinkScale shrinkScale}.
	 */
	private double focalScale;

	/**
	 * The scale value to zoom in/out the focal node's siblings.
	 */
	private double siblingScale;

	/**
	 * The area code for the top-left partition relative to the focal node.   
	 */
	private static final int TOP_LEFT = 0;

	/**
	 * The area code for the top partition relative to the focal node.   
	 */
	private static final int TOP = 1;

	/**
	 * The area code for the top-right partition relative to the focal
	 * node.   
	 */
	private static final int TOP_RIGHT = 2;

	/**
	 * The area code for the top-left partition relative to the focal node.   
	 */
	private static final int LEFT = 3;

	/**
	 * The area code for the center partition relative to the focal node.   
	 */
	private static final int CENTER = 4;

	/**
	 * The area code for the right partition relative to the focal node.   
	 */
	private static final int RIGHT = 5;

	/**
	 * The area code for the bottom-left partition relative to the focal
	 * node. 
	 */
	private static final int BOT_LEFT = 6;

	/**
	 * The area code for the bottom partition relative to the focal node.   
	 */
	private static final int BOT = 7;

	/**
	 * The area code for the bottom-right partition relative to the focal
	 * node. 
	 */
	private static final int BOT_RIGHT = 8;

	/**
	 * The area code for the areas outside the nine standard partitions.
	 */
	private static final int IRRELEVANT = 9;

	/**
	 * The array to store the nine standard areas(partitions). Each area
	 * is a dynamically defined bounds in the global coordinates.
	 */
	private Rectangle2D.Double[] areaBounds = new Rectangle2D.Double[9];

	/**
	 * The <code>Hashtable</code> to store the collided siblings and their
	 * area codes. For a specific sibling of the focal node, its reference
	 * is the key, and its area code is the value. A sibling and its area
	 * code will be dynamically stored into this hashtable if and only if
	 * it collides with the focal node that is being fisheye zoomed. 
	 */
	private Hashtable siblingCodes = new Hashtable();

	/**
	 * Marks if this event handler is active or not.
	 */
	private boolean active = false;

	/**
	 * The DisplayBean this event handler is associated with.
	 */
	private DisplayBean displayBean = null;

	/** The previous state of the tooltip */
	private boolean toolTipStateBeforeAction;


	/**
	 * Constructs a new orthogonal FisheyeEventHandler.
	 */
	public PFisheyeZoomHandler(DisplayBean displayBean) {
		this.displayBean = displayBean;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	/**
	 * Determines if the fisheye zooming actions in the current level
	 * will be automatically propagated to the upper level.
	 * @param propagate If <code>true</code>, propagate
	 * to the upper level. Otherwise, stop propagation.
	 * @see #isPropagatedToUpperLevel
	 */
	public void setPropagatedToUpperLevel(boolean propagate) {
		//propagateToUpperLevel = propagate;
		propagateToUpperLevel = false;
	}

	/**
	 * Examines if the fisheye zooming actions will be automatically
	 * propagated to the upper level.
	 * @see #setPropagatedToUpperLevel(boolean)
	 * @return <code>true</code> if the propagation is allowed.
	 */
	public boolean isPropagatedToUpperLevel() {
		return propagateToUpperLevel;
	}

	/**
	 * Sets the scale for magnifying the focal node.
	 * @param sc The scale value for magnifying. The value
	 * must be between 1.11f and 1.33f. Once the magnifying
	 * value is changed, the shrinking value will be changed
	 * automatically.
	 * @see #DEFAULT_MAGNIFY_SCALE
	 * @see #setShrinkScale(double)
	 */
	public void setMagnifyScale(double sc) {
		if (sc > 1.33f)
			magnifyScale = 1.33f;
		else if (sc < 1.11f)
			magnifyScale = 1.11f;
		else
			magnifyScale = sc;
		shrinkScale = 1.0f / magnifyScale;
	}

	/**
	 * Gets the scale for magnifying the focal node.
	 * @return The value of <code>magnifyScale<code>.
	 */
	public double getMagnifyScale() {
		return magnifyScale;
	}

	/**
	 * Sets the scale for shrinking the focal node.
	 * @param sc The scale value for shrinking. The value
	 * must be between 0.75f and 0.90f. Once the shrinking
	 * value is changed, the magnifying value will be changed
	 * automatically.
	 * @see #DEFAULT_SHRINK_SCALE
	 * @see #setMagnifyScale(double)
	 */
	public void setShrinkScale(double sc) {
		if (sc >= 0.90f)
			shrinkScale = 0.90f;
		else if (sc <= 0.75f)
			shrinkScale = 0.75f;
		else
			shrinkScale = sc;
		magnifyScale = 1.0f / shrinkScale;
	}

	/**
	 * Gets the scale for shrinking the focal node.
	 * @return The value of <code>shrinkScale<code>.
	 */
	public double getShrinkScale() {
		return shrinkScale;
	}

	/**
	 * Sets the limit to stop magnifying the focal node.
	 * @param limit The limit to stop magnifying. The value
	 * must be between 0.60f and 0.90f.
	 * @see #DEFAULT_MAGNIFY_LIMIT
	 */
	public void setMagnifyLimit(double limit) {
		if (limit >= 0.9f)
			magnifyLimit = 0.9f;
		else if (limit <= 0.6f)
			magnifyLimit = 0.6f;
		else
			magnifyLimit = limit;
	}

	/**
	 * Gets the limit to stop magnifying the focal node.
	 * @return The value of <code>magnifyLimit<code>.
	 */
	public double getMagnifyLimit() {
		return magnifyLimit;
	}

	public void startFisheyeing(Vector nodes) {
		// If no "focalNode", return.
		focalNode = (ShrimpNode) nodes.firstElement();
		if (focalNode == null)
			return;

		// @tag Shrimp(Fisheye) : focalParent will be null for root node or flat graphs
		focalParent = focalNode.getParentShrimpNode();
		zooming = true;
		zoomChoice = ZOOMIN;
		focalScale = magnifyScale;

		toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		displayBean.setToolTipEnabled(false);

		// Start fisheye zooming interaction.
		displayBean.setInteracting(true);
		startFisheyeZooming();
	}

	public void startFisheyeingOut(Vector nodes) {
		// If no "focalNode", return.
		focalNode = (ShrimpNode) nodes.firstElement();
		if (focalNode == null)
			return;

		// @tag Shrimp(Fisheye) : focalParent will be null for root node or flat graphs
		focalParent = focalNode.getParentShrimpNode();
		zooming = true;
		zoomChoice = ZOOMOUT;
		focalScale = shrinkScale;

		toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		displayBean.setToolTipEnabled(false);

		// Start fisheye zooming interaction.
		displayBean.setInteracting(true);
		startFisheyeZooming();
	}

	public void stopFisheyeing() {
		// Stop fisheye zooming interaction.
		stopFisheyeZooming();
		displayBean.setInteracting(false);

		displayBean.setToolTipEnabled(toolTipStateBeforeAction);
	}

	/**
	 * Starts the animated fisheye zooming.
	 */
	private void startFisheyeZooming() {
		// Repaint the drawing surface of the displayBean.
		displayBean.repaint();

		// Compute the real estate bounds for fisheye zooming.
		computeRealEstate();

		// Compute the zooming anchor point of the focal node.
		// This anchor point ensures that the focal node will
		// fit within its parent's global bounds.
		computeFocalAnchor();

		// Compute the nine standard areas for fisheye zooming.
		computeAreaBounds();

		// Compute all the siblings' area codes.
		siblingCodes.clear();
		computeSiblingCodes();

		// Zoom one step. The method will generate animation.
		zoomOneStep();
	}

	/**
	 * Stop the animated fisheye zooming.
	 */
	private void stopFisheyeZooming() {
		zooming = false; // Set fisheye zooming false to stop.
		focalNode = null; // Must be null once fisheye zooming stops.
		focalParent = null; // Must be null once fisheye zooming stops.
		focalRestrict = null; // Must be null once fisheye zooming stops.
		siblingCodes.clear();
	}

	/**
	 * Do one fisheye zooming step, sleep a short time, and schedule
	 * the next fisheye zooming step.
	 */
	private void zoomOneStep() {
		// Safety checking. 
		if (focalNode == null)
			return;
		
		checkZoomingConditions();

		if (zooming) {
			// Be Called in order.
			computeSiblingScale();
			fisheyeZoomFocalNode();
			fisheyeZoomAllSiblings();

			displayBean.repaint();

			if (zoomChoice == ZOOMOUT && focalScale != shrinkScale) {
				zooming = false;
				return;
			}

			try {
				// Give the primary thread some time to respond to events.
				Thread.sleep(50);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PFisheyeZoomHandler.this.zoomOneStep();
					}
				});
			} catch (InterruptedException e) {
				// If interrupted, stop the fisheye zooming.
				zooming = false;
			}
		}
	}

	/**
	 * Gets a node component's bounds which is transformed by the
	 * edit group, ZTransformGroup. 
	 */
	private Rectangle2D.Double getTransformedBoundsFor(ShrimpNode node) {
		// Get the visual component bounds.
		Rectangle2D.Double nodeBounds = node.getOuterBounds();

		// Get the Affine transform and then do transformation.
		AffineTransform tx = (AffineTransform) displayBean.getTransformOf(node);
		Rectangle2D.Double rect = new Rectangle2D.Double(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
		rect = GeometryUtils.transform(rect, tx);

		return rect;
	}

	/**
	 * Computes the real estate bounds. If the visual component of the
	 * focal parent is {@link ShrimpNode}, the
	 * real estate bounds will be the focal parent's visual inner bounds.
	 * Otherwise, the real estate bounds will be the focal parent's back
	 * visual component bounds or subtree bounds.
	 */
	private void computeRealEstate() {
		Rectangle2D rect;
		// @tag Shrimp(Fisheye) : If focal parent is null use the canvas size instead of focal parent's inner bounds
		if (focalParent == null) {
			Dimension canvasSize = (Dimension) displayBean.getCanvasDimension();
			rect = new Rectangle2D.Double(0, 0, canvasSize.width, canvasSize.height);
		} else {
			rect = focalParent.getInnerBounds(); 
		}
		realEstateBounds.setRect(rect);
	}

	/**
	 * Computes the anchor point of the focal node. By zooming around
	 * this point located in the focal node's local coordinate system,
	 * the relative position of the focal node to its parent will not
	 * be changed.
	 */
	private void computeFocalAnchor() {
		Rectangle2D.Double focal; // The focal node's component bounds transformed
		// by applying its edit group: ZTransformGroup.
		Rectangle2D.Double estat; // The real estate bounds for the focal node.
		double x, y;
		double val1, val2;
		double xratio, yratio;

		// Set the "focalPrevBounds" and "focalCurrBounds".
		focalPrevBounds.setRect(getTransformedBoundsFor(focalNode));
		focalCurrBounds.setRect(focalPrevBounds);
		focal = focalCurrBounds;
		estat = realEstateBounds;

		// Begin calculation.
		val1 = (estat.width * focal.x) - (focal.width * estat.x);
		val2 = (estat.width - focal.width);
		x = val1 / val2;
		val1 = (estat.height * focal.y) - (focal.height * estat.y);
		val2 = (estat.height - focal.height);
		y = val1 / val2;

		xratio = (x - focal.x) / focal.width;
		yratio = (y - focal.y) / focal.height;
		// End calculation.

		// Calculate the focal node's anchor point that is
		// relative to the focal node's local coordsystem.
		Rectangle2D.Double local = focalNode.getOuterBounds();
		x = local.x + local.width * xratio;
		y = local.y + local.height * yratio;
		focalAnchor.setLocation(x, y);
	}

	/**
	 * Computes the area bounds for each standard area(partition) that is
	 * determined by the current status of the focal node.
	 */
	private void computeAreaBounds() {
		Rectangle2D.Double focal; // The focal node's component bounds transformed
		// by applying its edit group: ZTransformGroup.
		Rectangle2D.Double estat; // The real estate bounds for the focal node.
		double x, y, width, height;

		focal = focalPrevBounds;
		estat = realEstateBounds;

		x = estat.x;
		y = estat.y;
		width = focal.x - estat.x;
		height = focal.y - estat.y;
		areaBounds[TOP_LEFT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[TOP_LEFT] = new ZBounds(x, y, (width+focal.width/4), (height+focal.height/4));

		x = focal.x;
		y = estat.y;
		width = focal.width;
		height = focal.y - estat.y;
		areaBounds[TOP] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[TOP] = new ZBounds((x+focal.width/4), y, (width-focal.width/2), (height+focal.height/4));

		x = focal.x + focal.width;
		y = estat.y;
		width = (estat.x + estat.width) - (focal.x + focal.width);
		height = focal.y - estat.y;
		areaBounds[TOP_RIGHT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[TOP_RIGHT] = new ZBounds((x-focal.width/4), y, (width+focal.width/4), (height+focal.height/4));

		x = estat.x;
		y = focal.y;
		width = focal.x - estat.x;
		height = focal.height;
		areaBounds[LEFT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[LEFT] = new ZBounds(x, (y+focal.height/4), (width+focal.width/4), (height+focal.height/4));

		x = focal.x;
		y = focal.y;
		width = focal.width;
		height = focal.height;
		areaBounds[CENTER] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[CENTER] = new ZBounds((x+width/4), (y+height/4), width/2, height/2);

		x = focal.x + focal.width;
		y = focal.y;
		width = (estat.x + estat.width) - (focal.x + focal.width);
		height = focal.height;
		areaBounds[RIGHT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[RIGHT] = new ZBounds((x-focal.width/4), (y+focal.height/4), (width+focal.width/4), (height-focal.height/4));

		x = estat.x;
		y = focal.y + focal.height;
		width = focal.x - estat.x;
		height = (estat.y + estat.height) - (focal.y + focal.height);
		areaBounds[BOT_LEFT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[BOT_LEFT] = new ZBounds(x, (y-focal.height/4), (width+focal.width/4), (height+focal.height/4));                 

		x = focal.x;
		y = focal.y + focal.height;
		width = focal.width;
		height = (estat.y + estat.height) - (focal.y + focal.height);
		areaBounds[BOT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[BOT] = new ZBounds((x+focal.width/4), (y-focal.height/4), (width-focal.width/2), (height+focal.height/4));        

		x = focal.x + focal.width;
		y = focal.y + focal.height;
		width = (estat.x + estat.width) - (focal.x + focal.width);
		height = (estat.y + estat.height) - (focal.y + focal.height);
		areaBounds[BOT_RIGHT] = new Rectangle2D.Double(x, y, width, height);
		//areaBounds[BOT_RIGHT] = new ZBounds((x-focal.width/4), (y-focal.height/4), (width+focal.width/4), (height+focal.height/4));                   

	}

	/**
	 * Computes the area codes for all the siblings, including the
	 * focalNode.
	 */
	private void computeSiblingCodes() {
		// Set focalRestrict to focalNode, and
		// calculate the minimum restrict area.
		focalRestrict = focalNode;
		Rectangle2D.Double sibTr = getTransformedBoundsFor(focalRestrict);
		double minArea = sibTr.width * sibTr.height;

		Vector allSiblings;
		// @tag Shrimp(Fisheye) : if focal parent is null get all root nodes
		if (focalParent == null) {
			allSiblings = displayBean.getDataDisplayBridge().getRootNodes();
		} else {
			allSiblings = displayBean.getDataDisplayBridge().getChildNodes(focalParent);
		}

		// Compute the area codes for all the siblings.
		for (Iterator iter = allSiblings.iterator(); iter.hasNext();) {
			ShrimpNode sibling = (ShrimpNode) iter.next();
			
			sibTr = getTransformedBoundsFor(sibling);
			int code = computeAreaCode(sibTr);
			siblingCodes.put(sibling, new Integer(code));

			// Update the focalRestrict.
			if (zoomChoice == ZOOMOUT) {
				double sibArea = sibTr.width * sibTr.height;
				if (sibArea < minArea)
					focalRestrict = sibling;
			}
		}
	}

	/**
	 * Computes the area code that is determined by the specified point.
	 */
	private int computeAreaCode(Rectangle2D.Double bounds) {
		int code;
		double x, y;

		code = IRRELEVANT;
		x = bounds.x + 0.5*bounds.width;
		y = bounds.y + 0.5*bounds.height;

		for (int i = 0; i < 9; i++) {
			if (areaBounds[i].contains(x, y)) {
				code = i;
				break;
			}
		}
		return code;
	}

	/**
	 * Examines if the next zooming action should be done(whether magnifying
	 * or shrinking).
	 */
	private void checkZoomingConditions() {
		Rectangle2D.Double focal; // The focal node's component bounds transformed
		// by applying its edit group: ZTransformGroup.
		Rectangle2D.Double estat; // The real estate bounds for the focal node.
		double xratio, yratio;

		focal = focalCurrBounds;
		estat = realEstateBounds;
		xratio = (focal.width * focalScale) / estat.width;
		yratio = (focal.height * focalScale) / estat.height;

		if (zoomChoice == ZOOMIN) {
			// Fisheye zoomin
			if ((xratio > magnifyLimit) || (yratio > magnifyLimit)) {
				if (propagateToUpperLevel) {
					// If allowing propagation to the upper level,
					// change the focal node and its real parent.
					focalNode = focalParent;
					// @tag Shrimp(Fisheye) : if focal parent is null then don't propagate fisheye to upper level
					if ((focalNode == null) || (focalNode.getParentShrimpNode() == null)) {
						zooming = false;
						return;
					}
					focalParent = focalNode.getParentShrimpNode();
					// Repaint the drawing surface
					displayBean.repaint();

					computeRealEstate();
					computeFocalAnchor();
					computeAreaBounds();
					siblingCodes.clear();
					computeSiblingCodes();

					// Recheck conditions.
					checkZoomingConditions();
				} else {
					zooming = false;
				}
			}
		} else {
			// Fisheye zoomout
			if (focalNode == focalRestrict) {
				zooming = false;
			} else {
				Rectangle2D.Double restrictBounds;
				restrictBounds = getTransformedBoundsFor(focalRestrict);

				double focalArea;
				focalArea = focalCurrBounds.width * focalCurrBounds.height;
				focalArea = focalArea * focalScale * focalScale;

				double restrictArea;
				computeSiblingScale();
				restrictArea = restrictBounds.width * restrictBounds.height;
				restrictArea = restrictArea * siblingScale * siblingScale;

				if (restrictArea > focalArea) {
					focalScale = convertCurrentFocalScale(focalScale, estat, focal, restrictBounds);
				}
			}
		}
	}

	/**
	 * Computes the scale value that will be applied to the focal node's
	 * siblings that will collide with the focal node.
	 */
	private void computeSiblingScale() {
		Rectangle2D.Double focal; // The focal node's component bounds transformed
		// by applying its edit group: ZTransformGroup.
		Rectangle2D.Double estat; // The real estate bounds for the focal node.

		focal = focalCurrBounds;
		estat = realEstateBounds;

		// The "siblingScale" depends on the "focalScale", the focal node's
		// global size and its parent's global size. So, it is not constant.
		double siblingScale_w = (estat.width - focal.width * focalScale);
		double siblingScale_h = (estat.height - focal.height * focalScale);

		//siblingScale = siblingScale/(estat.width - focal.width);
		siblingScale_w = siblingScale_w / (estat.width - focal.width);
		siblingScale_h = siblingScale_h / (estat.height - focal.height);

		//siblingScale = (siblingScale_w + siblingScale_h)/2;

		siblingScale = siblingScale_h;
	}

	/**
	 * Fisheye zooms the focal node.
	 */
	private void fisheyeZoomFocalNode() {
		// Store the previous bounds of the focal node.
		focalPrevBounds.setRect(getTransformedBoundsFor(focalNode));

		// Apply scale transformation before any other transformations.
		AffineTransform sx = getScaleTransform(focalScale, focalAnchor.getX(), focalAnchor.getY());
		AffineTransform at = (AffineTransform) displayBean.getTransformOf(focalNode);
		at.concatenate(sx);
		displayBean.setTransformOf(focalNode, at);

		// Store the current bounds of the focal node.
		focalCurrBounds.setRect(getTransformedBoundsFor(focalNode));
	}

	/**
	 * Fisheye zooms all the focal node's siblings.
	 */
	private void fisheyeZoomAllSiblings() {
		for (Iterator iter = siblingCodes.keySet().iterator(); iter.hasNext();) {
			ShrimpNode sibling = (ShrimpNode) iter.next();
			int code = ((Integer) siblingCodes.get(sibling)).intValue();

			// The focal node has been fisheye zoomed before.
			if (sibling == focalNode)
				continue;

			// Policy: No need to fisheye zoom the nodes outside
			// the focal node's parent.
			if (code == IRRELEVANT)
				continue;

			// Policy: No need to fisheye zoom the nodes inside
			// the focal node.
			if (code == CENTER)
				continue;

			// Policy: Fisheye zoom other siblings of the focal node.
			if (zoomChoice == ZOOMIN) {
				fisheyeZoomSibling(sibling, siblingScale);
			} else {
				fisheyeZoomSibling(sibling, siblingScale);
			}
		}
	}

	/**
	 * Fisheye zooms a specific sibling of the focal node.
	 */
	private void fisheyeZoomSibling(ShrimpNode sibling, double zoomVal) {
		Rectangle2D.Double sibVb = sibling.getOuterBounds(); // The sibling's visual component bounds.
		AffineTransform at = (AffineTransform) displayBean.getTransformOf(sibling);

		// Apply local scale transformation.
		double x = sibVb.x + sibVb.width / 2.0f;
		double y = sibVb.y + sibVb.height / 2.0f;
		AffineTransform sx = getScaleTransform(zoomVal, x, y);
		at.concatenate(sx);

		// Apply global translating transformaton.
		Point2D pt = computeSiblingMove(sibling, zoomVal);
		AffineTransform tx = getTranslateTransform(pt.getX(), pt.getY());
		at.preConcatenate(tx);

		// Apply the transformation to the sibling.
		displayBean.setTransformOf(sibling, at);
	}

	/**
	 * Computes the global move of a specific sibling of the focal node.
	 */
	private Point2D computeSiblingMove(ShrimpNode sibling, double zoomVal) {
		int code;
		Point2D pt;
		double dx, dy;
		Rectangle2D.Double estat; // The real estate bounds.
		Rectangle2D.Double sibTr; // The sibling node's component bounds transformed
		// by applying its edit group: ZTransformGroup.
		dx = 0;
		dy = 0;
		pt = new Point2D.Double();
		estat = realEstateBounds;
		sibTr = getTransformedBoundsFor(sibling);

		code = ((Integer) siblingCodes.get(sibling)).intValue();

		if (code == TOP_LEFT) {
			dx = (sibTr.x + sibTr.width / 2.0f) - estat.x;
			dx = dx * (zoomVal - 1.0f);
			dy = (sibTr.y + sibTr.height / 2.0f) - estat.y;
			dy = dy * (zoomVal - 1.0f);
		}

		if (code == TOP) {
			Point2D.Double focalCurrCenterPoint = new Point2D.Double(focalCurrBounds.x + 0.5*focalCurrBounds.width, focalCurrBounds.y + 0.5*focalCurrBounds.height);
			Point2D.Double focalPrevCenterPoint = new Point2D.Double(focalPrevBounds.x + 0.5*focalPrevBounds.width, focalPrevBounds.y + 0.5*focalPrevBounds.height);
			dx = focalCurrCenterPoint.getX() - focalPrevCenterPoint.getX();
			dy = (sibTr.y + sibTr.height / 2.0f) - estat.y;
			dy = dy * (zoomVal - 1.0f);
		}

		if (code == TOP_RIGHT) {
			dx = (sibTr.x + sibTr.width / 2.0f) - (estat.x + estat.width);
			dx = dx * (zoomVal - 1.0f);
			dy = (sibTr.y + sibTr.height / 2.0f) - estat.y;
			dy = dy * (zoomVal - 1.0f);
		}

		if (code == LEFT) {
			Point2D.Double focalCurrCenterPoint = new Point2D.Double(focalCurrBounds.x + 0.5*focalCurrBounds.width, focalCurrBounds.y + 0.5*focalCurrBounds.height);
			Point2D.Double focalPrevCenterPoint = new Point2D.Double(focalPrevBounds.x + 0.5*focalPrevBounds.width, focalPrevBounds.y + 0.5*focalPrevBounds.height);
			dx = (sibTr.x + sibTr.width / 2.0f) - estat.x;
			dx = dx * (zoomVal - 1.0f);
			dy = focalCurrCenterPoint.getY() - focalPrevCenterPoint.getY();
		}

		if (code == CENTER) {
			// It is just a policy.
			dx = 0;
			dy = 0;
		}

		if (code == RIGHT) {
			Point2D.Double focalCurrCenterPoint = new Point2D.Double(focalCurrBounds.x + 0.5*focalCurrBounds.width, focalCurrBounds.y + 0.5*focalCurrBounds.height);
			Point2D.Double focalPrevCenterPoint = new Point2D.Double(focalPrevBounds.x + 0.5*focalPrevBounds.width, focalPrevBounds.y + 0.5*focalPrevBounds.height);
			dx = (sibTr.x + sibTr.width / 2.0f) - (estat.x + estat.width);
			dx = dx * (zoomVal - 1.0f);
			dy = focalCurrCenterPoint.getY() - focalPrevCenterPoint.getY();
		}

		if (code == BOT_LEFT) {
			dx = (sibTr.x + sibTr.width / 2.0f) - estat.x;
			dx = dx * (zoomVal - 1.0f);
			dy = (sibTr.y + sibTr.height / 2.0f) - (estat.y + estat.height);
			dy = dy * (zoomVal - 1.0f);
		}

		if (code == BOT) {
			Point2D.Double focalCurrCenterPoint = new Point2D.Double(focalCurrBounds.x + 0.5*focalCurrBounds.width, focalCurrBounds.y + 0.5*focalCurrBounds.height);
			Point2D.Double focalPrevCenterPoint = new Point2D.Double(focalPrevBounds.x + 0.5*focalPrevBounds.width, focalPrevBounds.y + 0.5*focalPrevBounds.height);
			dx = focalCurrCenterPoint.getX() - focalPrevCenterPoint.getX();
			dy = (sibTr.y + sibTr.height / 2.0f) - (estat.y + estat.height);
			dy = dy * (zoomVal - 1.0f);
		}

		if (code == BOT_RIGHT) {
			dx = (sibTr.x + sibTr.width / 2.0f) - (estat.x + estat.width);
			dx = dx * (zoomVal - 1.0f);
			dy = (sibTr.y + sibTr.height / 2.0f) - (estat.y + estat.height);
			dy = dy * (zoomVal - 1.0f);
		}

		pt.setLocation(dx, dy);
		return pt;
	}

	/**
	 * Gets the scale transformation matrix around a specific position.
	 */
	private AffineTransform getScaleTransform(double dz, double x, double y) {
		AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
		tx.scale(dz, dz);
		tx.translate(-x, -y);
		return tx;
	}

	/**
	 * Gets the translate transformation matrix.
	 */
	private AffineTransform getTranslateTransform(double x, double y) {
		AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
		return tx;
	}

	/**
	 * Convert the the current focalScale to make the focal node
	 * to the same size of the the focalRestrict. 
	 */
	private double convertCurrentFocalScale(double minVal, Rectangle2D.Double estate, Rectangle2D.Double focalBounds, Rectangle2D.Double restrictBounds) {
		double estatW;
		double focalW;
		double focalArea;
		double restrArea;

		estatW = estate.width;
		focalW = focalBounds.width;
		focalArea = focalW * focalBounds.height;
		restrArea = restrictBounds.width * restrictBounds.height;

		double a, b, c;
		a = focalArea * (estatW - focalW) * (estatW - focalW) / restrArea - focalW * focalW;
		b = 2.0f * estatW * focalW;
		c = - (estatW * estatW);

		double root;
		root = getEquationRootLessThanOne(a, b, c, minVal);
		return root;
	}

	/**
	 * Solve an equation to find the root to meet the requirements. 
	 */
	private double getEquationRootLessThanOne(double a, double b, double c, double min) {
		// Solve the equation
		double sqrt = Math.sqrt(b * b - 4 * a * c);
		double x1 = (-b + sqrt) / (2 * a);
		double x2 = (-b - sqrt) / (2 * a);

		double x = min;
		if (min <= x1 && x1 <= 1.0f)
			x = x1;
		if (min <= x2 && x2 <= 1.0f)
			x = x2;
		if (x == min)
			x = min * 1.001f;
		return x;
	}

} //class FisheyeZoomHandler
