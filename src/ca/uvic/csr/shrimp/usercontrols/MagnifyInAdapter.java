/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ActionHistoryBean.ActionHistoryBean;
import ca.uvic.csr.shrimp.ActionHistoryBean.HistoryAction;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import edu.umd.cs.piccolo.PCamera;

/**
 * This UserAction adapter will handle magnifying
 *
 * @author Casey Best, Chris Callendar
 * @date Jan 30, 2001
 */
public class MagnifyInAdapter extends ZoomAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_MAGNIFY_IN;
	public static final String TOOLTIP = "Zooms in to the level of the selected node.";

	public MagnifyInAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool, ZOOM_DIRECTION_IN, TOOLTIP);
	}

	public boolean canStart() {
		Object targetObject = getTargetObject();
		boolean canStart = (targetObject instanceof PCamera || targetObject instanceof ShrimpNode);
		return canStart;
	}

	/**
	 * Starts magnifying
	 */
	public void startAction() {
		ApplicationAccessor.waitCursor();
		try {
			final DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			ActionHistoryBean actionHistoryBean = (ActionHistoryBean) tool.getBean(ShrimpTool.ACTION_HISTORY_BEAN);
			Object targetObject = getTargetObject();

			// shouldn't need this - the action won't start if this is the case
			if (targetObject == null || targetObject instanceof ShrimpArc) {
				return;
			}

			if ((targetObject instanceof PCamera) || (targetObject instanceof ShrimpNode)) {
				boolean toExtents = (targetObject instanceof PCamera);
				// create the undo
				Vector currentObjs = displayBean.getCurrentFocusedOnObjects();
				boolean fromExtents = currentObjs == null || currentObjs.isEmpty();

				Vector path = new Vector();
				if (fromExtents && !toExtents) {
				    path = displayBean.getPathBetweenObjects(((ShrimpNode) targetObject).getRootShrimpNode(), targetObject);
				} else if (toExtents && !fromExtents) {
				    path = displayBean.getPathBetweenObjects(currentObjs, ((ShrimpNode)currentObjs.firstElement()).getRootShrimpNode());
				} else if (toExtents && fromExtents) {
				    path = new Vector();
				} else {
				    path = displayBean.getPathBetweenObjects(currentObjs, targetObject);
				}

				UndoMagnifyInFocus action = new UndoMagnifyInFocus(path, fromExtents, toExtents, displayBean);
				actionHistoryBean.addAction(action);
				action.redo();
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}

	private class UndoMagnifyInFocus implements HistoryAction {

		private Vector redoPath;
		private boolean fromExtents;
		private boolean toExtents;
		private DisplayBean displayBean;

		public UndoMagnifyInFocus(Vector path, boolean fromExtents, boolean toExtents, DisplayBean displayBean) {
			this.redoPath = path;
			this.fromExtents = fromExtents;
			this.toExtents = toExtents;
			this.displayBean = displayBean;
		}

		public void undo() {
			if (fromExtents) {
			    displayBean.focusOnExtents(true);
			} else {
			    Vector undoPath = new Vector (redoPath);
			    Collections.reverse(undoPath);
			    goDownPath(undoPath);
			}
		}

		private void goDownPath(Vector path) {
		    if (path.isEmpty()) {
		    	return;
		    }
			if (path.size() > 1) {
				// If the first and second elements in the path are nodes, and the second
				// element is a child of the first element make sure that the
				// first element is switched to children mode before heading down the path
				Object obj1 = path.elementAt(0);
				Object obj2 = path.elementAt(1);
				boolean refreshLayout = false;
				if ((obj1 instanceof ShrimpNode) && (obj2 instanceof ShrimpNode)) {
					ShrimpNode node1 = (ShrimpNode) obj1;
					ShrimpNode node2 = (ShrimpNode) obj2;
					ShrimpNode parentOfObj2 = node2.getParentShrimpNode();
					if (parentOfObj2 != null && parentOfObj2.equals(obj1)) {
						displayBean.setPanelMode(obj1, PanelModeConstants.CHILDREN);
					}
					if (((node1.getLabelMode().equals(DisplayConstants.LABEL_MODE_FIT_TO_NODE)) ||
						(node1.getLabelMode().equals(DisplayConstants.LABEL_MODE_WRAP_TO_NODE))) &&
							node2.equals(node1.getParentShrimpNode())) {
						refreshLayout = true;
					}
				}

				// if we are just going from one root node to another then we must focus on extents first
				Collection rootNodes = displayBean.getDataDisplayBridge().getRootNodes();
				if (path.size() == 2 && rootNodes.contains(obj1) && rootNodes.contains(obj2)) {
			        displayBean.focusOnExtents(true);
				}

				// always start at second element, since the first is already focused on
				for (int i = 1; i < path.size(); i++) {
					Object obj = path.elementAt(i);
					// objects along the way should be opened in children view
					if (displayBean.isVisible(obj)) {
						changeSelectedObjectsTo(obj);

						if (i < path.size() - 1 && obj instanceof ShrimpNode) {
							displayBean.focusOnNode((ShrimpNode)obj, PanelModeConstants.CHILDREN);
							// if we've just focused on a root node, and the next object to focus on is also a root node,
							// then we must focus on extends so that we can properly see the transition from one root node to another
							if (rootNodes.contains(obj)) {
							    Object nextObj = path.elementAt(i+1);
							    if (rootNodes.contains(nextObj)) {
							        displayBean.focusOnExtents(true);
							    }
							}
						} else {
							displayBean.focusOn(obj);
						}
					}
				}

				// this is an attempt to fix the problem in the fit to node and wrap to node label modes
				// when zooming out the children start to overlap the parents because the inner bounds haven't been updated
				if (refreshLayout) {
					// run the last layout on the previous target node
					ShrimpNode node1 = (ShrimpNode) obj1;
					Vector children = displayBean.getDataDisplayBridge().getChildNodes(node1, false);
					if (!children.isEmpty()) {
						boolean animate = false;
						displayBean.refreshLayout(children, animate);
						for (Iterator iter = children.iterator(); iter.hasNext(); ) {
							ShrimpNode child = (ShrimpNode) iter.next();
							if (child.isOpen()) {
								Vector grandChildren = displayBean.getDataDisplayBridge().getChildNodes(child, false);
								if (!grandChildren.isEmpty() ) {
									displayBean.refreshLayout(grandChildren, animate);
								}
							}
						}
					}
				}
			} else if (path.size() == 1) {
				Object dest = path.lastElement();
				displayBean.focusOn(dest);
				changeSelectedObjectsTo(dest);
			}
		}

		public void redo() {
			if (toExtents) {
				displayBean.focusOnExtents(true);
			} else {
				goDownPath(redoPath);
			}
		}

		/**
		 *  changes the highlighted objects to the given node or arc
		 */
		private void changeSelectedObjectsTo(Object object) {
			Vector newSelected = new Vector();
			newSelected.addElement(object);
			if (object instanceof ShrimpNode) {
				setSelectedNodes(newSelected);
			} else if (object instanceof ShrimpArc) {
				setSelectedArcs(newSelected);
			} else {
				(new Exception("Can't handle object: " + object)).printStackTrace();
			}
		}
	}

}
