/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter.mouse;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAndKeyAdapter;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * Takes care of node selection and moving of nodes.
 */
public class MouseSelectAndMoveAdapter extends ShrimpMouseAndKeyAdapter {

	private static final Color SEL_RECT_COLOR = Color.black;
	private static final float SEL_RECT_ABS_WIDTH = 1.0f;
	private static Stroke SEL_RECT_STROKE;
	static {
	    try {
	        SEL_RECT_STROKE = new PFixedWidthStroke(SEL_RECT_ABS_WIDTH);
	    } catch (SecurityException e ) { // thrown when shrimp is an applet
	        //e.printStackTrace();
	        SEL_RECT_STROKE = new BasicStroke(1.0f);
	    }
	}
	private SelectionBox selectionBox;
	private ShrimpNode parentOfSelected;

	private Point2D mousePressedPnt;
	private boolean inMultiSelectMode = false;

    /* JavaBeans associated with this adapter*/
    private SelectorBean selectorBean;
    private DisplayBean displayBean;
    private ShrimpTool tool;

    private boolean dragged;

	private boolean isCtrlPressed = false;
	private boolean isShiftPressed = false;
	protected boolean isAltPressed = false;

	private Vector selectedNodes;

    public MouseSelectAndMoveAdapter(ShrimpTool tool) {
    	this.tool = tool;
		selectedNodes = new Vector();
	    dragged = false;
    }

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mousePressed(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mousePressed(ShrimpMouseEvent e) {
		if (!handleAnyMouseEvent(e)) {
			return;
		}
		Object targetObj = e.getTarget();

		if (isShiftPressed) {
		    parentOfSelected = targetObj instanceof ShrimpNode ? (ShrimpNode) targetObj : null;
			// put a temporary box on the screen
			mousePressedPnt = new Point2D.Double(e.getX(), e.getY());
			addSelectionBoxToGUI();
			inMultiSelectMode = true;
		} else if (targetObj instanceof ShrimpNode) {
			ShrimpNode targetNode = (ShrimpNode) targetObj;

			if (selectedNodes.contains(targetNode)) {
				// if the artifact is selected already, just move
				dragged = false;
			} else {
				// not selected already
				if (isCtrlPressed) {
					// just add to selected if ctrl is down
					addTargetToSelected(targetNode);
					dragged = true;
				} else {
					// clear the selected nodes
					selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector(1));
					addTargetToSelected(targetNode);
					dragged = true;
				}
			}
			// do the action
			selectedNodes = (Vector)((Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES)).clone();
			if (selectedNodes.size() > 0) {
				displayBean.startMovingWithMouse(selectedNodes);
			}
		} else if (targetObj instanceof PCamera) {
		    //user has clicked on canvas, so deselect all selected nodes
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
		    parentOfSelected = null;
			// put a temporary box on the screen
			mousePressedPnt = new Point2D.Double(e.getX(), e.getY());
			addSelectionBoxToGUI();
			inMultiSelectMode = true;
		} else if (targetObj instanceof PShrimpArcLabel) {
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector());
		    parentOfSelected = null;
		    Vector selectedArcLabels = new Vector(1);
		    selectedArcLabels.add(targetObj);
		    ((PShrimpArcLabel)targetObj).setHighlighted(true);
		    selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARC_LABELS, selectedArcLabels);
		    Vector selectedArcs = new Vector(1);
		    selectedArcs.add(((PShrimpArcLabel)targetObj).getLabeledObject());
		    selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, selectedArcs);
		    displayBean.startMovingWithMouse(selectedArcLabels);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mouseReleased(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseReleased(ShrimpMouseEvent e) {
		if (!handleAnyMouseEvent(e)) {
			return;
		}
		Object targetObj = e.getTarget();

		if (inMultiSelectMode) {
			// see what nodes the selection box intersects
			// TODO do this while dragging (this code was in mouseDragged but it was way too slow)
			// idea: see if the bounds of the nodes of any of the children intersect with
			// the selection rectangle
			Rectangle2D selectionRectBounds = selectionBox.getGlobalFullBounds();
			Vector selectedNodes = new Vector();
			Vector possibleNodes = new Vector();
			if (displayBean instanceof PFlatDisplayBean) {
				possibleNodes = displayBean.getVisibleNodes();
			} else if (displayBean instanceof PNestedDisplayBean) {
				if (parentOfSelected == null) {
					possibleNodes = new Vector();
					possibleNodes.addAll(displayBean.getDataDisplayBridge().getRootNodes());
				} else {
					possibleNodes = displayBean.getDataDisplayBridge().getChildNodes(parentOfSelected);
				}
			}
			for (Iterator iter = possibleNodes.iterator(); iter.hasNext();) {
				ShrimpNode childNode = (ShrimpNode) iter.next();
				Rectangle2D childBounds = childNode.getGlobalOuterBounds();
				if (childBounds.intersects(selectionRectBounds)) {
					selectedNodes.addElement(childNode);
				}
			}
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);

			//remove temporary selection box
			removeSelectionBoxFromGUI();
			inMultiSelectMode = false;
		} else {
			if (targetObj instanceof ShrimpNode) {
				ShrimpNode targetNode = (ShrimpNode) targetObj;
				if (selectedNodes.size() > 0) {
					displayBean.stopMovingWithMouse();
					if (!dragged) {
						if (isCtrlPressed) {
							removeTargetFromSelected(targetNode);
						} else {
							// @tag Shrimp.rightClickSelect : don't lose the selection on a right click
							// unless the clicked node is not already selected
							if (!e.isRightMouseButton() || !selectedNodes.contains(targetNode)) {
								// clear the selected nodes
								selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, new Vector(1));
							}
							addTargetToSelected(targetNode);
						}
					}
					selectedNodes = new Vector();
				}
			} else if (targetObj instanceof PShrimpArcLabel) {
			    Vector selectedArcLabels = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_ARC_LABELS);
			    if (selectedArcLabels != null && !selectedArcLabels.isEmpty()) {
			        displayBean.stopMovingWithMouse();
			        selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARC_LABELS, new Vector(0));
				    ((PShrimpArcLabel)targetObj).setHighlighted(false);
				    selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector(0));
			    }
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener#mouseDragged(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseDragged(ShrimpMouseEvent e) {
		if (!handleAnyMouseEvent(e)) {
			return;
		}

		//@tag Shrimp(sequence)
		if (displayBean.isNodeEdgeMovementAllowed()) {
			if (inMultiSelectMode) {
				Point2D newMouseLocation = new Point2D.Double(e.getX(), e.getY());
				double dx = newMouseLocation.getX() - mousePressedPnt.getX();
				double dy = newMouseLocation.getY() - mousePressedPnt.getY();
				double newX = mousePressedPnt.getX();
				if (dx < 0) {
					newX = newX - Math.abs(dx);
				}
				double newY = mousePressedPnt.getY();
				if (dy < 0) {
					newY = newY - Math.abs(dy);
				}
				double newW = Math.abs(dx);
				double newH = Math.abs(dy);
				selectionBox.setPathToRectangle((float)newX, (float)newY, (float)newW, (float)newH);
			} else {
				if (selectedNodes.size() > 0) {
					dragged = true;
					displayBean.continueMovingWithMouse(selectedNodes);
				}
			    Vector selectedArcLabels = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_ARC_LABELS);
			    if (selectedArcLabels != null && !selectedArcLabels.isEmpty()) {
			        displayBean.continueMovingWithMouse(selectedArcLabels);
			    }
			}
		}
	}

    /* Check the shrimpMouseEvent and react accordingly*/
    /**
     * Checks the validity of the given ShrimpMouseEvent
     * @return false if the given ShrimpMouseEvent is invalid and should be ignored.
     */
    private boolean handleAnyMouseEvent(ShrimpMouseEvent e){
		// allow either mouse click
    	if (!e.isLeftMouseButton() && !e.isRightMouseButton()) {
    		return false;
    	}

		try {
			displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
		    selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
			Object targetObj = e.getTarget();

			// if not in drag mode, check to see if the target is invalid
			if (!dragged && !inMultiSelectMode) {
			    if (targetObj instanceof ShrimpArc) {
			    	return false;
			    }
			}
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
			return false;
		}
		return true;
	}

    /*
     * Key has been pressed
     */
    public void keyPressed(ShrimpKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			isCtrlPressed = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			isShiftPressed = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_ALT) {
			isAltPressed = true;
		}
    }

    /*
     * Key has been released
     */
    public void keyReleased(ShrimpKeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
			isCtrlPressed = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			isShiftPressed = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_ALT) {
			isAltPressed = false;
		}
		if (inMultiSelectMode && e.getKeyCode() == KeyEvent.VK_CONTROL) {
			removeSelectionBoxFromGUI();
			inMultiSelectMode = false;
		}
    }

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpKeyListener#resetKeys()
	 */
	public void resetKeys() {
		isCtrlPressed = false;
		isShiftPressed = false;
		isAltPressed = false;
	}

	private void addSelectionBoxToGUI() {
		Shape shape = new Rectangle2D.Double(mousePressedPnt.getX(), mousePressedPnt.getY(), 1.0, 1.0);
		selectionBox = new SelectionBox(shape, SEL_RECT_STROKE);
		selectionBox.setPickable(false);
		PLayer layer = ((PNestedDisplayBean)displayBean).getPCanvas().getLayer();
		layer.addChild(selectionBox);
	}

	private void removeSelectionBoxFromGUI() {
		PLayer layer = ((PNestedDisplayBean)displayBean).getPCanvas().getLayer();
		if (selectionBox != null) {
			layer.removeChild(selectionBox);
		}
	}

	private void removeTargetFromSelected(ShrimpNode target) {
		Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		if (selectedNodes.contains(target)) {
			selectedNodes.removeElement(target);
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);
		}
	}

	/**
	 * Adds a node to the selected nodes.
	 * Also uses motion to highlight the selected nodes and their neighbors if
	 * ALT is pressed and if the application property is set to true.
	 */
	private void addTargetToSelected(ShrimpNode targetNode) {
		Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
		if (!selectedNodes.contains(targetNode)) {
			selectedNodes.addElement(targetNode);
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);
		}

		// @tag Shrimp.MotionLayout : wobble the selected nodes (and their neighbors) if the property is set
		if (ApplicationAccessor.isApplicationSet()) {
			Properties properties = ApplicationAccessor.getProperties();
			boolean useMotion = "true".equals(properties.getProperty(DisplayBean.PROPERTY_KEY__USE_MOTION, "true"));
			if (isAltPressed && useMotion) {
				displayBean.setLayoutMode(selectedNodes, LayoutConstants.LAYOUT_MOTION, false, true);
			}
		}
	}

	private class SelectionBox extends PPath {
		private SelectionBox(Shape shape, Stroke stroke) {
			super(shape, stroke);
			setStrokePaint(SEL_RECT_COLOR);
			setPaint(null);
		}
	}

}
