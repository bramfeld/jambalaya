/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ActivityManager;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.MagnifyZoomHandler;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.SwingToolTipManager;
import ca.uvic.csr.shrimp.util.GeometryUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivityScheduler;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PUtil;

/**
 * @author Rob Lintern
 */
public class PNestedDisplayBean extends AbstractDisplayBean {

	private static final Border FOCUS_LOST_BORDER = BorderFactory.createEmptyBorder(); //LineBorder(Color.BLACK, 1);
	private static final Border FOCUS_GAINED_BORDER = BorderFactory.createLineBorder(Color.GREEN.darker(), 3);

	/** The Piccolo picture associated with this bean*/
	protected PCanvas pCanvas;

	private FocusListener focusListener;
	//private ShrimpMouseListener returnComponentFocusAdapter;

	/** These are the layers that are the parents to the certain sections */
	protected PLayer topLayer;
	protected PLayer nodeLayer;
	protected PLayer arcLayer;
	protected PLayer arcLabelLayer;
	protected PLayer nodeLabelLayer;

	/* Modules that handle shrimpEvents*/
	protected PFisheyeZoomHandler fisheyeHandler;
	protected PNormalZoomHandler zoomHandler;
	protected PPanHandler panHandler;

	/** These contain the types for the arcs */
	protected Map arcTypeLayers; //values are vectors of layers of this type, key is the arcType

	protected PropertyChangeListener viewTransformChangeListener;
	protected ComponentListener rootResizedListener;

	private int currentStaticRenderQuality = PPaintContext.HIGH_QUALITY_RENDERING;
	private int currentDynamicRenderQuality = PPaintContext.LOW_QUALITY_RENDERING;

	/** The bounds of the last focused item. */
	private Rectangle2D lastBounds = null;

	private Set addedArcs = new HashSet();

	/**
	 * @param cprels
	 */
	public PNestedDisplayBean(ShrimpProject project, String[] cprels, DataDisplayBridge dataDisplayBridge) {
		super(cprels, dataDisplayBridge, project);

		arcLayer = new PLayer();
		arcLabelLayer = new PLayer();
		nodeLabelLayer = new PLayer();
		nodeLayer = new PLayer();
		//labelLayer = new PLayer();
		topLayer = new PLayer();

		arcTypeLayers = new HashMap();

		//Create a blank picture
		pCanvas = new PNestedCanvas(project);
		addLayers();

		// remove Piccolo's default zoom and pan handlers
		pCanvas.removeInputEventListener(pCanvas.getZoomEventHandler());
		pCanvas.removeInputEventListener(pCanvas.getPanEventHandler());

		//register for tooltips
		SwingToolTipManager.sharedInstance().registerComponent(pCanvas);

		defaultLabelMode = DEFAULT_LABEL_MODE;

		// start listening for mouse and key events
		displayInputManager = new PShrimpDisplayInputManager(this);
		displayInputManager.setActive(false);

		// when the display bean is unfocused, add a visual cue
		focusListener = new FocusListener() {
			public void focusGained(FocusEvent e) {
				pCanvas.setBorder(FOCUS_GAINED_BORDER);
				setEnabled(true);
			}

			public void focusLost(FocusEvent e) {
				pCanvas.setBorder(FOCUS_LOST_BORDER);
				setEnabled(false);
			}
		};
		pCanvas.addFocusListener(focusListener);
//		pCanvas.addMouseListener(new MouseAdapter () {
//            public void mouseEntered(MouseEvent arg0) {
//                pCanvas.requestFocus();
//            }
//        });
		//create handler classes for methods
		fisheyeHandler = new PFisheyeZoomHandler(this);
		zoomHandler = new PNormalZoomHandler(this);
		panHandler = new PPanHandler(this);

		//returnComponentFocusAdapter = new ReturnComponentFocusAdapter();
		//addShrimpMouseListener(returnComponentFocusAdapter);

		//setEnabled(true);

		usingArrowHeads = true; // use arrow heads in nested view

		viewTransformChangeListener = new PropertyChangeListener () {
			public void propertyChange(PropertyChangeEvent evt) {
				viewTransformChanged();
			}
		};
		pCanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, viewTransformChangeListener);

		rootResizedListener = new ComponentAdapter () {
			public void componentResized(ComponentEvent e) {
				if (getCurrentFocusedOnObjects().size() >= 1 && ((Dimension)getCanvasDimension()).width > 0 && ((Dimension)getCanvasDimension()).height > 0) {
					//if (getCurrentFocusedOnObjects().equals(dataDisplayBridge.getRootNodes()))
						//dataDisplayBridge.setRootNodeSize(dataDisplayBridge.getRootNodes(), true);
					//else
						focusOn(getCurrentFocusedOnObjects());
				}
			}
		};
		pCanvas.addComponentListener(rootResizedListener);

	}

	protected void viewTransformChanged () {
		for (Iterator iter = getAllArcs().iterator(); iter.hasNext();) {
			PShrimpArc arc = (PShrimpArc) iter.next();
			arc.viewTransformChanged();
		}
		for (Iterator iter = getAllLabels().iterator(); iter.hasNext();) {
			PShrimpLabel label = (PShrimpLabel) iter.next();
			label.viewTransformChanged();
		}
	}

	protected void addLayers() {
	    //note the last added is the last drawn
		pCanvas.getLayer().addChild(nodeLayer);
		pCanvas.getLayer().addChild(arcLayer);
		pCanvas.getLayer().addChild(arcLabelLayer);
		pCanvas.getLayer().addChild(nodeLabelLayer);
		pCanvas.getLayer().addChild(topLayer);
	}


	public PCanvas getPCanvas() {
		return pCanvas;
	}

	/**
	 * Gives focus to the canvas.
	 */
	public void requestFocus() {
		if (pCanvas != null) {
			pCanvas.requestFocus();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#setToolTipEnabled(boolean)
	 */
	public void setToolTipEnabled(boolean enabled) {
		super.setToolTipEnabled(enabled);
		SwingToolTipManager.sharedInstance().setEnabled(enabled);
	}

	/**
	 * @see DisplayBean#startZoomingIn()
	 */
	public void startZoomingIn() {
		resetCurrentFocusedOnObject();
		zoomHandler.startZoomingIn(displayInputManager.getLatestMouseEvent());
	}

	public void startZoomingIn(double x, double y) {
		resetCurrentFocusedOnObject();
		zoomHandler.startZoomingIn(x, y);
	}

	public void startZoomingIn(double x, double y, double magnification) {
		resetCurrentFocusedOnObject();
		zoomHandler.startZoomingIn(x, y, magnification);
	}

	/**
	 * @see DisplayBean#startZoomingOut()
	 */
	public void startZoomingOut() {
		resetCurrentFocusedOnObject();
		zoomHandler.startZoomingOut(displayInputManager.getLatestMouseEvent());
	}

	/**
	 * @see DisplayBean#stopZooming()
	 */
	public void stopZooming() {
		zoomHandler.stopZooming(displayInputManager.getLatestMouseEvent());
	}

	/**
	 * @see DisplayBean#startPanning(java.lang.String)
	 */
	public void startPanning(String direction) {
		resetCurrentFocusedOnObject();
		panHandler.startPanning(displayInputManager.getLatestMouseEvent(), direction);
	}

	/**
	 * @see DisplayBean#stopPanning()
	 */
	public void stopPanning() {
		panHandler.stopPanning(displayInputManager.getLatestMouseEvent());
	}

	/**
	 * @see DisplayBean#startFisheyeingIn(java.util.Vector)
	 */
	public void startFisheyeingIn(Vector nodes) {
		resetCurrentFocusedOnObject();
		fisheyeHandler.startFisheyeing(nodes);
	}

	/**
	 * @see DisplayBean#stopFisheyeing()
	 */
	public void stopFisheyeing() {
		fisheyeHandler.stopFisheyeing();
	}

	/**
	 * @see DisplayBean#startFisheyeingOut(java.util.Vector)
	 */
	public void startFisheyeingOut(Vector nodes) {
		resetCurrentFocusedOnObject();
		fisheyeHandler.startFisheyeingOut(nodes);
	}

	private void resetCurrentFocusedOnObject() {
		if (currentFocusedOnObject != null && currentFocusedOnObject instanceof ShrimpNode) {
			((ShrimpNode) currentFocusedOnObject).setHasFocus(false);
            currentFocusedOnObject = null;
		}
	}

	/**
	 *
	 * @param objs
	 * @param checkWithinParent
	 */
	protected void continueMovingWithMouse(Collection objs, boolean checkWithinParent) {
//		if (currentFocusedOnObject != null && currentFocusedOnObject instanceof ShrimpNode) {
//			((ShrimpNode)currentFocusedOnObject).setIsFocusedOn(false);
//            //currentFocusedOnObject = null;
//		}
		ShrimpMouseEvent currEvent = displayInputManager.getLatestMouseEvent();
		ShrimpMouseEvent prevEvent = displayInputManager.getPreviousMouseEvent();

		if (currEvent == null || prevEvent == null) {
			return;
		}
		if (objs.size() == 0) {
			return;
		}

		Object firstObject = objs.iterator().next();
		if (firstObject instanceof PShrimpNode) {
		    continueMovingNodesWithMouse(objs, checkWithinParent, currEvent, prevEvent);
		} else if (firstObject instanceof PShrimpArcLabel){
		    continueMovingArcLabelsWithMouse(objs, currEvent, prevEvent);
		}
	}

	/**
     * @param objs
     * @param currEvent
     * @param prevEvent
     */
    private void continueMovingArcLabelsWithMouse(Collection objs, ShrimpMouseEvent currEvent, ShrimpMouseEvent prevEvent) {
		PShrimpLabel first = (PShrimpLabel) objs.iterator().next();
		Point2D prev = new Point2D.Double(prevEvent.getX(), prevEvent.getY());
		Point2D curr = new Point2D.Double(currEvent.getX(), currEvent.getY());
		// change these screen coords to child's local coords
		first.globalToLocal(prev);
		first.globalToLocal(curr);
		double dx = curr.getX() - prev.getX();
		double dy = curr.getY() - prev.getY();
		// now translate the nodes
		for (Iterator iter = objs.iterator(); iter.hasNext();) {
			PShrimpArcLabel label = (PShrimpArcLabel) iter.next();
			label.moveOffset(dx, dy);
		}
    }

    private void continueMovingNodesWithMouse(Collection objs, boolean checkWithinParent,
    										  ShrimpMouseEvent currEvent, ShrimpMouseEvent prevEvent) {
		// first calculate the translation based on the first node
		PShrimpNode firstChild = (PShrimpNode) objs.iterator().next();
		Point2D prev = new Point2D.Double(prevEvent.getX(), prevEvent.getY());
		Point2D curr = new Point2D.Double(currEvent.getX(), currEvent.getY());
		// change these screen coords to child's local coords
		firstChild.globalToLocal(prev);
		firstChild.globalToLocal(curr);
		double dx = curr.getX() - prev.getX();
		double dy = curr.getY() - prev.getY();

		if (checkWithinParent) {
			// adjust the distance if needed to keep all of the nodes within their parent
			for (Iterator iter = objs.iterator(); iter.hasNext();) {
				PShrimpNode node = (PShrimpNode) iter.next();
				// here we get the bounds of the child and parent in the child's coordinate system
				Rectangle2D childOuterBounds = (Rectangle2D)node.getOuterBounds().clone(); // clone is neccessary here
				PShrimpNode parentNode = (PShrimpNode) node.getParentShrimpNode();
				if (parentNode != null) {
					Rectangle2D parentInnerBounds = (Rectangle2D)parentNode.getInnerBounds().clone(); // clone is neccessary here
					parentNode.localToGlobal(parentInnerBounds);
					node.globalToLocal(parentInnerBounds);

					// if the child will be outside the parent, we adjust the distance to
					// keep the child within the parent
					childOuterBounds.setRect(childOuterBounds.getX() + dx, childOuterBounds.getY() + dy, childOuterBounds.getWidth(), childOuterBounds.getHeight());
					if (!parentInnerBounds.contains(childOuterBounds)) {
						// adjust dx based on the side that is incorrect
						if (childOuterBounds.getX() < parentInnerBounds.getX()) {
							dx += parentInnerBounds.getX() - childOuterBounds.getX();
						} else if (childOuterBounds.getX() + childOuterBounds.getWidth() > parentInnerBounds.getX() + parentInnerBounds.getWidth()) {
							dx -= (childOuterBounds.getX() + childOuterBounds.getWidth()) - (parentInnerBounds.getX() + parentInnerBounds.getWidth());
						}

						// adjust dy based on the side that is incorrect
						if (childOuterBounds.getY() < parentInnerBounds.getY()) {
							dy += parentInnerBounds.getY() - childOuterBounds.getY();
						} else if (childOuterBounds.getY() + childOuterBounds.getHeight() > parentInnerBounds.getY() + parentInnerBounds.getHeight()) {
							dy -= (childOuterBounds.getY() + childOuterBounds.getHeight()) - (parentInnerBounds.getY() + parentInnerBounds.getHeight());
						}
					}
				}
			}
		}

		// now translate the nodes
		for (Iterator iter = objs.iterator(); iter.hasNext();) {
			PShrimpNode node = (PShrimpNode) iter.next();
			// translate
			AffineTransform nodeTx = node.getTransform();
			nodeTx.translate(dx, dy);
			setTransformOf(node, nodeTx);

			// this is done to remove the trails left behind on the plus icons
			if (node.shouldRenderPlusIcon()) {
				node.getParent().repaint();
			}
		}
	}

	/**
	 *
	 * @see DisplayBean#continueMovingWithMouse(java.util.Collection)
	 */
	public void continueMovingWithMouse(Collection nodes) {
		continueMovingWithMouse(nodes, true);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		pCanvas.setEnabled(enabled);
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#setTransformsOfNodesWithAnimation(java.util.List, java.util.List)
	 */
	protected void setTransformsOfNodesWithAnimation (List nodes, List transforms) {
		PActivityScheduler scheduler = pCanvas.getRoot().getActivityScheduler();

		ArrayList activities = new ArrayList(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			final PShrimpNode pNode = (PShrimpNode) nodes.get(i);
			AffineTransform destTransform = (AffineTransform) transforms.get(i);
			AffineTransform srcTransform = pNode.getTransform();
			if (srcTransform.equals(destTransform)) {
				continue;
			}

			PTransformActivity.Target t = new PTransformActivity.Target() {
				public void setTransform(AffineTransform aTransform) {
					pNode.setTransform(aTransform);
				}
				public void getSourceMatrix(double[] aSource) {
					pNode.getTransformReference(true).getMatrix(aSource);
				}
			};

			PActivity activity = new PTransformActivity(1500, PUtil.DEFAULT_ACTIVITY_STEP_RATE, t, destTransform) {
				// at every step, make sure arcs etc know that a node has changed position
				protected void activityStep(long elapsedTime) {
					super.activityStep(elapsedTime);
					pNode.recomputeCentrePoint();
					pNode.firePositionChangedEvent();
				}

				/**
				 * @see edu.umd.cs.piccolo.activities.PInterpolatingActivity#activityFinished()
				 */
				protected void activityFinished() {
					super.activityFinished();
					pNode.recomputeCentrePoint();
					pNode.firePositionChangedEvent();
				}

			};
			activities.add(activity);
		}
		ActivityManager finishedListener = new ActivityManager(scheduler, activities);
		// wait until all nodes have finished moving
		waitForActivitiesToFinish(scheduler, finishedListener);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#setBoundsOfShrimpNodeWithAnimation(ca.uvic.csr.shrimp.DisplayBean.ShrimpNode, java.awt.geom.Rectangle2D.Double)
	 */
	protected void setBoundsOfShrimpNodeWithAnimation(ShrimpNode node, Rectangle2D.Double newBounds) {
		PActivityScheduler scheduler = pCanvas.getRoot().getActivityScheduler();
		final PBounds dst = new PBounds(newBounds);
		final PShrimpNode psn = (PShrimpNode) node;

		PInterpolatingActivity ta = new PInterpolatingActivity(500, PUtil.DEFAULT_ACTIVITY_STEP_RATE) {
			private PBounds src;

			protected void activityStarted() {
				src = psn.getBounds();
				super.activityStarted();
			}

			public void setRelativeTargetValue(float zeroToOne) {
				psn.setBounds(src.x + (zeroToOne * (dst.x - src.x)),
									 src.y + (zeroToOne * (dst.y - src.y)),
									 src.width + (zeroToOne * (dst.width - src.width)),
									 src.height + (zeroToOne * (dst.height - src.height)));
			}
		};
		ActivityManager finishedListener = new ActivityManager(scheduler, ta);
		// wait until all nodes have finished moving
		waitForActivitiesToFinish(scheduler, finishedListener);
	}



	/**
	 * @see DisplayBean#clear()
	 */
	public void clear() {
		boolean isVisible = isVisible();
		setVisible(false); // TODO check faster when not visible
		super.clear();
		//if (dataDisplayBridge != null) {
			//removeObject(dataDisplayBridge.getRootNodes()); // removing the root should cause all its descendents and all its arcs to be removed //TODO this takes a long time when many nodes
		//}
		nodeLayer.removeAllChildren();
		arcLayer.removeAllChildren();
		arcLabelLayer.removeAllChildren();
		topLayer.removeAllChildren();
		nodeLabelLayer.removeAllChildren();
		arcTypeLayers.clear();
		setVisible(isVisible);
		addedArcs.clear();
	}

	/**
	 * @see DisplayBean#getScreenCoordinates()
	 */
	public Object getScreenCoordinates() {
		return pCanvas.getCamera().getViewBounds();
	}

	/**
	 * @see DisplayBean#getTransformOf(java.lang.Object)
	 */
	public Object getTransformOf(Object obj) {
		AffineTransform at = new AffineTransform();
		if (obj instanceof PShrimpNode) {
			PShrimpNode node = (PShrimpNode) obj;
			at = (AffineTransform) node.getTransform().clone();
		}
		return at;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#setTransformOfNode(ca.uvic.csr.shrimp.DisplayBean.ShrimpNode, java.awt.geom.AffineTransform)
	 */
	protected void setTransformOfNode(ShrimpNode node, AffineTransform at) {
        if (currentFocusedOnObject != null && currentFocusedOnObject.equals(node)) {
            node.setHasFocus(false);
        }
		if (node instanceof PShrimpNode) {
			PShrimpNode pNode = (PShrimpNode)node;
			pNode.setTransform(at);
		}
	}

	/**
	 * @see DisplayBean#focusOnCoordinates(java.util.Vector, boolean)
	 */
	public void focusOnCoordinates(Vector coordinates, boolean animate) {
		Rectangle2D.Double bounds = (Rectangle2D.Double) coordinates.elementAt(0);
		resetCurrentFocusedOnObject();
		moveViewToCenterBounds (bounds, true, MagnifyZoomHandler.DEFAULT_ANIMATION_TIME, animate);
	}

	/**
	 * @see DisplayBean#isVisible()
	 */
	public boolean isVisible() {
		return pCanvas.isVisible();
	}

	/**
	 * @see DisplayBean#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		pCanvas.setVisible(visible);
	}

	/**
	 * @see DisplayBean#repaint()
	 */
	public void repaint() {
		pCanvas.repaint();
	}

	/**
	 * @return The static rendering quality of the display, that is when the user is not
	 * dragging, zooming, or panning.
	 */
	public int getStaticRenderingQuality() {
		return currentStaticRenderQuality;
	}

	public void setStaticRenderingQuality (int quality) {
		pCanvas.setDefaultRenderQuality(quality);
		currentStaticRenderQuality = quality;
	}

	public int getDynamicRenderingQuality() {
		return currentDynamicRenderQuality;
	}

	public void setDynamicRenderingQuality(int quality) {
		pCanvas.setAnimatingRenderQuality(quality);
		pCanvas.setInteractingRenderQuality(quality);
		currentDynamicRenderQuality = quality;
	}

	public int getDefaultStaticRenderingQuality() {
		return PPaintContext.HIGH_QUALITY_RENDERING;
	}

	public int getDefaultDynamicRenderingQuality() {
		return PPaintContext.LOW_QUALITY_RENDERING;
	}

	/**
	 * @see DisplayBean#setInteracting(boolean)
	 */
	public void setInteracting(boolean interacting) {
		pCanvas.setInteracting(interacting);
	}

	/**
	 * @see DisplayBean#isInteracting()
	 */
	public boolean isInteracting() {
		return pCanvas.getInteracting();
	}

	/**
	 * @see DisplayBean#getFontHeightOnCanvas(Object)
	 */
	public int getFontHeightOnCanvas(Object font) {
		int height = labelFont.getSize();
		Graphics g = pCanvas.getGraphics();
		if (g != null && font instanceof Font) {
			FontMetrics fm = g.getFontMetrics((Font)font);
			height = fm.getHeight();
		}
		return height;
	}

	/**
	 * @see DisplayBean#getCanvasDimension()
	 */
	public Object getCanvasDimension() {
		return new Dimension (pCanvas.getWidth(), pCanvas.getHeight());
	}

	/**
	 * Tests if the picture has the named layer.
	 * @param layerName The name of the layer.
	 * @return <code>true</code> if the named layer is found. Otherwise, return <code>false</code>.
	 */
	private boolean hasArcLayer(String layerName) {
		return arcTypeLayers.containsKey(layerName);
	}

	/**
	 * Creates a new arc layer for a specific type of arc
	 * For example, the new layer may be for "composite" arcs
	 * @param type The type of relationships to be added to this layer
	 */
	private void createArcLayer(String type) {
		if (!hasArcLayer(type)) {
			PLayer newLayer = new PLayer();
			arcLayer.addChild(newLayer);
			arcTypeLayers.put(type, newLayer);
		}
	}

	/**
	 * @param type The type of relationship whose layer you require.
	 */
	private PLayer getArcLayer(String type) {
		return (PLayer) arcTypeLayers.get(type);
	}

	/**
	 * Gets the layer containing nodes.
	 * @return The PLayer that contains nodes
	 */
	public PLayer getNodeLayer() {
		return nodeLayer;
	}

	/**
	 * Sets the coordinates for displaying a new node.  These coordinates are based
	 * on the coordinates of the parent.
	 */
	protected void setNewNodePosition(ShrimpNode node) {
		ShrimpNode parentSn = node.getParentShrimpNode();
		if (parentSn != null) {
			Rectangle2D.Double parentBounds = parentSn.getInnerBounds();
			Vector objects = new Vector(1);
			objects.add(node);

			//figure out a new dimension for the new node
			Vector dimensions = new Vector(1);
			double minDim = Math.min(parentBounds.getWidth(), parentBounds.getHeight());
			minDim = Math.max(minDim / 2.0d, 5.0d);
			Dimension dim = new Dimension((int) minDim, (int) minDim);
			dimensions.add(dim);

			// fiure out a good position for the new node
			Vector positions = new Vector(1);
			Point2D position = new Point2D.Double(parentBounds.getX() + 0.5 * parentBounds.getWidth(), parentBounds.getY() + 0.5 * parentBounds.getHeight());
			positions.add(position);

			// now move and resize it
			// @tag Shrimp.Bendpoints : TODO - add relationships here?
			setPositionsAndSizes(objects, new Vector() /*relationships */, positions, dimensions, false);

			node.raiseAboveSiblings();
		}
	}

	public void bringToTopLayer(Object obj) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection)obj).iterator(); iter.hasNext();) {
				bringToTopLayer (iter.next());

			}
		} else if (obj instanceof PShrimpArc) {
			PShrimpArc arc = (PShrimpArc) obj;
			arc.removeFromParent();
			topLayer.addChild(arc);
		}
	}

	public void returnToNormalLayer(Object obj) {
		if (obj instanceof Collection) {
			for (Iterator iter = ((Collection)obj).iterator(); iter.hasNext();) {
				returnToNormalLayer (iter.next());
			}
		} else if (obj instanceof PShrimpArc) {
			PShrimpArc arc = (PShrimpArc) obj;
			arc.removeFromParent();
			addShrimpArc(arc);
		}
	}

	/**
	 * Set the visual representation corresponding
	 * to the relationship type visible/invisible
	 *
	 * DFR Note:  this is never called!
	 *
	 * Note: If the object passed in is filtered, it will not be set visible
	 * Note: If the endpoints of the arc are not visible, the arc will not
	 *       be set visible
	 *
	 *@param type The type of relationships to make visible/invisible
	 *@param visible Choosing between visible and invisible
	 */
	public void setArcTypeVisible(String type, boolean visible) {
		final PLayer layer = getArcLayer(type);
		if (layer == null) {
			return;
		}

		layer.setVisible(visible);
	}

	public void addShrimpLabel(ShrimpLabel label) {
		if (label instanceof PShrimpLabel) {
			// add the label as a sibling of its node's
			PShrimpLabel psl = (PShrimpLabel) label;

			if (psl instanceof PShrimpNodeLabel) {
				PShrimpNode parentNode = (PShrimpNode)((ShrimpNode)psl.getLabeledObject()).getParentShrimpNode();
				if (parentNode != null) {
				    int index = Math.max (0, parentNode.getChildrenCount() - 1);
			        parentNode.addChild(index , psl); // add label to end of children to make it draw on top
				} else {
				    nodeLabelLayer.addChild(psl);
					//nodeLayer.addChild(0, psl);
				}
				psl.displayObjectPositionChanged();

			    psl.updateVisibility();
				// fixed labels don't show up at with correct transform initially so place them again
				// don't know why this works but it does
				if (getDefaultLabelMode().equals(DisplayConstants.LABEL_MODE_FIXED)) {
				    psl.displayObjectPositionChanged();
				}
			} else if (psl instanceof PShrimpArcLabel) {
			    arcLabelLayer.addChild(psl);
			    psl.displayObjectPositionChanged();
			    psl.updateVisibility();
				// arc labels don't show up at with correct transform initially so place them again
				// don't know why this works but it does
				psl.displayObjectPositionChanged();
			}
		}
	}

	public void removeShrimpLabel(ShrimpLabel sl) {
		if (sl instanceof PShrimpLabel) {
		    PShrimpLabel psl = (PShrimpLabel) sl;
		    if (psl instanceof ShrimpNodeLabel) {
		        removeNavigationListener((ShrimpNodeLabel)psl);
		    }
			psl.removeFromParent();
			psl.updateVisibility();
		}
	}

	public void addShrimpNode(ShrimpNode node) {
		if (node instanceof PShrimpNode) {
			PShrimpNode parentNode = (PShrimpNode)node.getParentShrimpNode();
			if (parentNode != null) {
			    //if (parentNode.isRootNode()) {
					//nodeLayer.addChild((PShrimpNode)node);
			    //} else {
					// add node to beginning of children so that it draws below sticky labels
					parentNode.addChild(0, (PShrimpNode)node);
			    //}
			} else { // its a root
				nodeLayer.addChild((PShrimpNode)node);
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#addShrimpTerminal(ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal)
	 */
	protected void addShrimpTerminal(ShrimpTerminal terminal) {
		ShrimpNode parentNode = terminal.getShrimpNode().getParentShrimpNode();
		if (parentNode != null && (parentNode instanceof PShrimpNode) && (terminal instanceof PShrimpTerminal)) {
			((PShrimpNode)parentNode).addChild(((PShrimpTerminal)terminal));
		}
	}

	/**
	 * Stop displaying an node and any children under it.
	 * Also removes any direct relationships, displaying the composites
	 * instead.  Also adjusts any composites attached to this artifact
	 */
	public void removeShrimpNode(ShrimpNode sn) {
		// first remove the children
		Vector children = dataDisplayBridge.getChildNodes(sn);
		removeObject(children);

		//remove all relationships attached to this node
		Vector arcs = dataDisplayBridge.getShrimpArcs(sn);
		removeObject(arcs);

		dataDisplayBridge.removeShrimpNode(sn);
		if (sn instanceof PShrimpNode) {
			PShrimpNode psn = (PShrimpNode) sn;
			setVisible(psn, false, false);
			psn.removeFromParent();

			// If labels are fixed or scaled by level, remove label for this
			// artifact from the display. If labels are scaled by level,
			// remove label from the visual pane
			if (getDefaultLabelMode().equals(DisplayConstants.LABEL_MODE_FIXED) || getDefaultLabelMode().equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
				ShrimpLabel sl = getDataDisplayBridge().getShrimpNodeLabel(psn, false);
				if (sl != null) {
					removeObject(sl);
				}
			}
			// remove terminals of this node, if any
			PShrimpNode parentSn = (PShrimpNode)psn.getParentShrimpNode();
			if (parentSn != null) {
				for (Iterator iter = psn.getTerminals().iterator(); iter.hasNext();) {
					PShrimpTerminal st = (PShrimpTerminal) iter.next();
					st.removeFromParent();
				}
			}
		}

	}

	/**
	 * Adds a ShrimpArc to the display.
	 */
	public void addShrimpArc(ShrimpArc sa) {
		if (addedArcs.contains(sa)) {
			// TODO figure out why this happens with composite arcs!
			// @tag Shrimp.CompositeArcs : duplicate arcs are added to display bean
			//System.out.println("trying to add arc more than once: " + sa);
			return;
		}
		if (sa instanceof PShrimpArc) {
			PShrimpArc psa = (PShrimpArc) sa;
			ShrimpNode srcNode = sa.getSrcNode();
			ShrimpNode destNode = sa.getDestNode();
			boolean srcNodeInDisplay = nodeIsInDisplay(srcNode);
			boolean destNodeInDisplay = nodeIsInDisplay(destNode);
			if (!srcNodeInDisplay || !destNodeInDisplay) {
				return;
			}

			// add all arcs of the same type to the same layer
			String type = (psa.getRelationship() != null) ? psa.getRelationship().getType() : "";
			if (!hasArcLayer(type)) {
				createArcLayer(type);
			}
			getArcLayer(type).addChild(psa);
			addedArcs.add(sa);
			if (showArcLabels) {
				ShrimpLabel sl = dataDisplayBridge.getShrimpArcLabel(sa, true);
				addShrimpLabel(sl);
			}
			//arc.updateVisibility();
			arrangeArcs(psa.getSrcNode(), psa.getDestNode());
			psa.updateVisibility();
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean#nodeIsInDisplay(ca.uvic.csr.shrimp.DisplayBean.ShrimpNode)
	 */
	protected boolean nodeIsInDisplay (ShrimpNode node) {
		boolean inDisplay = false;
		if (node instanceof PShrimpNode) {
			PShrimpNode psn = (PShrimpNode) node;
			inDisplay = psn.getParent() != null;
		}
		return inDisplay;
	}


	/**
	 * Stop displaying an arc and remove all references to it.
	 * @param arc The ShrimpArc to turn off
	 */
	protected void removeShrimpArc(ShrimpArc arc) {
		ShrimpLabel label = dataDisplayBridge.getShrimpArcLabel(arc, false);
		if (label != null) {
		    removeObject(label);
		}
		addedArcs.remove(arc);
		dataDisplayBridge.removeShrimpArc(arc);
		if (arc instanceof PShrimpArc) {
			PShrimpArc psa = (PShrimpArc) arc;
			psa.setVisible(false);
			psa.removeFromParent();
			psa.getSrcNode().removeShrimpDisplayObjectListener(psa);
			psa.getDestNode().removeShrimpDisplayObjectListener(psa);
		}
	}

	public void focusOnExtents(boolean animate) {
        if (getVisibleObjects().isEmpty()) {
            System.err.println("no visible objects to focus on");
            return;
        }
        try {
			ApplicationAccessor.waitCursor();
			Object fromObj = currentFocusedOnObject;
			Object toObj = null;
			fireBeforeMagnifyEvent(fromObj, toObj);
			double[] extents = getExtents();
			Rectangle2D.Double extentsRect = new Rectangle2D.Double(extents[0], extents[1], extents[2], extents[3]);
			Vector coords = new Vector();
			coords.add(extentsRect);
			focusOnCoordinates(coords, animate);
			currentFocusedOnObject = null;
			fireAfterMagnifyEvent(fromObj, toObj);
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}


    public double[] getExtents() {
        double x = 0;
        double y = 0;
        double w = 0;
        double h = 0;

		Vector visibleObjects = getVisibleObjects();
		if (visibleObjects.size() > 0) {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = Double.MIN_VALUE;
			double maxY = Double.MIN_VALUE;

			for (int i = 0; i < visibleObjects.size(); i++) {
				ShrimpDisplayObject sdo = (ShrimpDisplayObject) visibleObjects.elementAt(i);
				if (sdo != null) {
					Rectangle2D.Double bounds = sdo.getGlobalOuterBounds();
					if (bounds.getX() < minX) {
						minX = bounds.getX();
					}
					if (bounds.getY() < minY) {
						minY = bounds.getY();
					}
					if (bounds.getX() + bounds.getWidth() > maxX) {
						maxX = bounds.getX() + bounds.getWidth();
					}
					if (bounds.getY() + bounds.getHeight() > maxY) {
						maxY = bounds.getY() + bounds.getHeight();
					}
				}
			}
			x = minX;
			y = minY;
			w = maxX - minX; //Math.max(getDisplayBounds().width, maxX - minX);
			h = maxY - minY; //Math.max(getDisplayBounds().height, maxY - minY);
		} else {
		    System.err.println("no visible objects to focus on");
		    x = getDisplayBounds().x;
		    y = getDisplayBounds().y;
		    w = getDisplayBounds().width;
		    h = getDisplayBounds().height;
		}

		// make extents 10% bigger
		double borderWidth = w * 0.1;
		double borderHeight = h * 0.1;
		x -= 0.5 * borderWidth;
		y -= 0.5 * borderHeight;
		w += borderWidth;
		h += borderHeight;
        return new double[] {x, y, w, h};
    }

    public Rectangle2D.Double getDisplayBounds() {
		Rectangle bounds = getPCanvas().getBounds();
		if (bounds == null || bounds.width < 0 || bounds.height < 0) {
			bounds.setBounds(0,0,1000,1000);
			getPCanvas().setBounds(bounds);
		}
		return new Rectangle2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
	}


	/**
	 *
	 * @param globalBounds
	 * @param shouldScale
	 * @param duration
	 * @return a PActivity that will animiate the view to the center
	 */
	private PActivity getAnimateViewToCenterBoundsActivity(final Object globalBounds, final boolean shouldScale, final long duration) {
		Rectangle2D viewBounds = getPCanvas().getCamera().getBounds();
		if (viewBounds.isEmpty()) {
			// @tag Shrimp.Piccolo.Determinant0 : if the view bounds are null then the transform will be zero
			System.err.println("PNestedDisplayBean: warning - viewing bounds is empty!");
		}
		double s = 1;

		if (shouldScale) {
			s = Math.min(viewBounds.getWidth() / ((Rectangle2D)globalBounds).getWidth(), viewBounds.getHeight() / ((Rectangle2D)globalBounds).getHeight());
		}

		AffineTransform destination = new AffineTransform();
		double transX = viewBounds.getCenterX() + (-((Rectangle2D)globalBounds).getCenterX() * s);
		double transY = viewBounds.getCenterY() + (-((Rectangle2D)globalBounds).getCenterY() * s);
		destination.translate(transX, transY);
		destination.scale(s, s);

		if (duration == 0) {
			getPCanvas().getCamera().setViewTransform(destination);
            return null;
		}

		PTransformActivity.Target t = new PTransformActivity.Target() {
			public void setTransform(AffineTransform aTransform) {
				getPCanvas().getCamera().setViewTransform(aTransform);
			}
			public void getSourceMatrix(double[] aSource) {
				getPCanvas().getCamera().getViewTransformReference().getMatrix(aSource);
			}
		};

		PTransformActivity ta = new PTransformActivity(duration, PUtil.DEFAULT_ACTIVITY_STEP_RATE, t, destination);
		return ta;
	}

	public void moveViewToCenterBounds(Object globalBounds, boolean shouldScale, long duration, boolean animate) {

		// check if the bounds are the same as the last time, if so return to save time
		if (globalBounds instanceof Rectangle2D) {
			Rectangle2D bounds = (Rectangle2D)globalBounds;
			if (GeometryUtils.compareBounds(bounds, lastBounds, 2D)) {
				return;
			}
			lastBounds = bounds;
		}

        // turn off the animation if too many objects on screen
        int animationThreshold = 2000; //TODO this should be a user preference
        animate = animate && getVisibleNodes().size() < animationThreshold;
        if (!animate) {
            duration = 0;
        }

		PActivity activity = getAnimateViewToCenterBoundsActivity (globalBounds, shouldScale, duration);
        if (activity != null) {
            PRoot r = getPCanvas().getCamera().getRoot();
    		PActivityScheduler scheduler = r.getActivityScheduler();
    		ActivityManager finishedListener = new ActivityManager(scheduler, activity);
    		waitForActivitiesToFinish(scheduler, finishedListener);
        }
	}

	/**
	 * Waits for the activities to be finished, painting the canvas until they are done.
	 * @param scheduler
	 * @param finishedListener
	 */
	protected void waitForActivitiesToFinish (PActivityScheduler scheduler, ActivityManager activityManager) {
		while (!activityManager.isFinished()) {
			try {
				scheduler.processActivities(System.currentTimeMillis());
				if (pCanvas != null && pCanvas.getParent() != null) {
					// TODO are we painting too much here?
					pCanvas.paintImmediately(pCanvas.getParent().getBounds());
				}
			} catch (Exception e) {
				e.printStackTrace();
				activityManager.setFinished(true);
			}
		}
	}

	public boolean isFlat() {
		return (getCprels().length == 0);
	}
}