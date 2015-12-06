/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.algorithms.MotionLayoutAlgorithm;
import org.eclipse.mylar.zest.layouts.progress.ProgressEvent;
import org.eclipse.mylar.zest.layouts.progress.ProgressListener;

import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.ActivityManager;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.util.DoubleDimension;


/**
 * Uses the {@link MotionLayoutAlgorithm} to move nodes and arcs in a
 * circular pattern for a few seconds.
 *
 * @author Chris Callendar
 */
public class MotionLayout extends AbstractLayout implements ProgressListener {

	public static final int DEFAULT_DISPLACEMENT = 1;
	public static final int DEFAULT_SPEED = 5;

	private Vector nodes = new Vector(0);
	private int displacement = DEFAULT_DISPLACEMENT;
	private FadeThread fadeThread = null;

	/**
	 * Initializes this layout.
	 * Loads the path length, motionradius, and time from the application properties.
	 */
	public MotionLayout(DisplayBean displayBean, String name) {
		super(displayBean, name, new MotionLayoutAlgorithm());
		loadProperties();
	}

	/**
	 * Loads the displacement, motion radius, and motion time from the Shrimp Application properties.
	 */
	private void loadProperties() {
		int displ = DEFAULT_DISPLACEMENT;
		double radius = MotionLayoutAlgorithm.DEFAULT_RADIUS;
		int speed = DEFAULT_SPEED;
		long time = MotionLayoutAlgorithm.DEFAULT_TIME;
		try {
			Properties props = ApplicationAccessor.getProperties();
			displ = Integer.parseInt(props.getProperty(DisplayBean.PROPERTY_KEY__DISPLACEMENT, DisplayBean.DEFAULT_DISPLACEMENT));
			radius = Double.parseDouble(props.getProperty(DisplayBean.PROPERTY_KEY__MOTION_RADIUS, DisplayBean.DEFAULT_MOTION_RADIUS));
			speed = Integer.parseInt(props.getProperty(DisplayBean.PROPERTY_KEY__MOTION_SPEED, DisplayBean.DEFAULT_MOTION_SPEED));
			time = Long.parseLong(props.getProperty(DisplayBean.PROPERTY_KEY__MOTION_TIME, DisplayBean.DEFAULT_MOTION_TIME));
		} catch (Exception ignore) {}

		setDisplacement(displ);
		setMotionRadius(radius);
		setMotionSpeed(speed);	// set the speed (sleep time) before the total time
		setMotionTime(time);
	}

	/**
	 * When a node is selected and ALT is pressed, it and its neighborhood of connected
	 * nodes and arcs are moved in a small circular motion to highlight the connected graph.
	 * For example, if the displacement value is 1, then the selected node and its immediate neighbors
	 * are moved.  If the displacement value is 0, only the selected node is moved.
	 * @param displacement the number of arc lengths away from the selected node
	 */
	public void setDisplacement(int displacement) {
		this.displacement = Math.max(0, displacement);
	}

	/**
	 * Updates the motion radius value.
	 * @param radius
	 */
	public void setMotionRadius(double radius) {
		((MotionLayoutAlgorithm)layoutAlgorithm).setRadius(radius);
	}

	/**
	 * Sets the total run time for the motion.
	 * @param time the time in milliseconds.
	 */
	public void setMotionTime(long time) {
		((MotionLayoutAlgorithm)layoutAlgorithm).setTotalRunTime(time);
	}

	/**
	 * Sets the speed of the motion.
	 * This actually controls how long the algorithm sleeps between iterations.
	 * @param speed the speed between 0 (sleep for 100ms) and 10 (sleep for 0ms).
	 */
	public void setMotionSpeed(int speed) {
		if (speed >= 0 && speed <= 10) {
			long ms = 100 - (speed * 10);
			((MotionLayoutAlgorithm)layoutAlgorithm).setSleepTime(ms);
		}
	}

	protected boolean showProgress() {
		return false;
	}

	protected boolean animateIterations(int nodeCount, int arcCount) {
		return true;
	}

	public void setupAndApplyLayout(Vector nodes, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents) {
		// a layout is already running
		if ((fadeThread != null) && fadeThread.isRunning()) {
			System.out.println("Already running");
			return;
		}

		this.nodes = nodes;
		if (nodes.size() == 0) {
			return;
		}

		// add all the connected nodes and arcs
		Vector nodesToMove = new Vector();
		Vector addedArcs = new Vector();
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			ShrimpNode node = (ShrimpNode) iter.next();
			nodesToMove.add(node);
			// add all the connected arcs and nodes up to a certain path length away
			addConnectedNodesAndArcs(node, nodesToMove, addedArcs, displacement);
		}

		HashMap labelsMap = new HashMap(nodesToMove.size());
		// save the state of the labels
		saveLabelState(nodesToMove, labelsMap);

		positionIncludedAndExcludedNodes(nodesToMove, addedArcs, new Vector(0), bounds, animate);

		// restore the state of the labels - fade out any previously hidden labels
		fadeThread = new FadeThread(labelsMap, true);
		fadeThread.start();
	}

	/**
	 * Saves the labels visibility, color, and transparency before the motion occurs.
	 * @param nodes Vector of ShrimpNode objects
	 * @param labelsMap the map to save label information in
	 */
	private void saveLabelState(Vector nodes, HashMap labelsMap) {
		DataDisplayBridge dataDisplayBridge = displayBean.getDataDisplayBridge();
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			PShrimpNode node = (PShrimpNode) iter.next();
			PShrimpLabel label = (PShrimpLabel)dataDisplayBridge.getShrimpNodeLabel(node, false);
			if (label != null) {
				// save the original state of the label
				Object[] vals = new Object[] { new Boolean(label.isVisible()),
											  label.getTextColor(),
											  new Float(label.getTransparency()) };
				labelsMap.put(label, vals);

				// set the label to be visible, blue, and fully opaque
				if (!label.isVisible()) {
					label.setVisible(true);
				}
				label.setTextColor(Color.BLUE);
				label.setTransparency(1f);
				label.repaint();
			}
		}
	}


	/**
	 * Recursively adds the connected nodes and arcs up to the given path length away.
	 * @param node the node in question
	 * @param nodes connected nodes are added to this list
	 * @param addedArcs connected arcs are added to this list
	 * @param pathLength the path length away to iterate from the given node
	 */
	private void addConnectedNodesAndArcs(ShrimpNode node, Vector nodes, Vector addedArcs, int pathLength) {
		if (pathLength <= 0) {
			return;
		}

		if (displayBean.isVisible(node)) {
			Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(node);
			for (Iterator iter = arcs.iterator(); iter.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iter.next();
				if (displayBean.isVisible(arc)) {
		            ShrimpNode srcNode = arc.getSrcNode();
		            ShrimpNode destNode = arc.getDestNode();
					if ((srcNode != destNode) && !addedArcs.contains(arc)) {
						addedArcs.add(arc);
					}
		            if (srcNode != node) {
		        		nodes.add(srcNode);
	            		addConnectedNodesAndArcs(srcNode, nodes, addedArcs, pathLength - 1);
		            } else if (destNode != node) {
		        		nodes.add(destNode);
	            		addConnectedNodesAndArcs(destNode, nodes, addedArcs, pathLength - 1);
		            }
				}
			}
		}
	}

	public void progressStarted(ProgressEvent e) {
	}

	public void progressUpdated(ProgressEvent e) {
		updateGUIImmediately(nodes);
	}

	public void progressEnded(ProgressEvent e) {
		layoutAlgorithm.removeProgressListener(this);
		updateGUIImmediately(nodes);
	}

	protected void positionAndSizeNodesAfterLayout(Vector nodes, Vector positions, Vector dimensions) {
		  for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			PShrimpNode node = (PShrimpNode) iter.next();
			// these bounds are relative to the parent
			// we have to use these instead of the layout bounds because they
			// don't get updated when the user moves or resizes a node
			Rectangle2D.Double bounds = node.getFullBounds();
			double w = bounds.width; // node.getWidthInLayout();
			double h = bounds.height; // node.getHeightInLayout();
			// @tag Shrimp.Bendpoints
			if (resizeEntitiesAfterLayout) {
				// ok to resize things for this layout, so ensure min size
				w = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, w);
				h = Math.max(ShrimpNode.MIN_GLOBAL_NODE_SIZE, h);
			}

			// Restore node position to centre point (layout positions at top left corner)
			double x = bounds.x + (w / 2.0); // node.getXInLayout()
			double y = bounds.y + (h / 2.0); // node.getYInLayout()
			positions.add(new Point2D.Double(x, y));
			dimensions.add(new DoubleDimension(w, h));
		}

		// subclasses like HorizontalLayout override this method
		adjustPositionsAndSizesAfterLayout(nodes, positions, dimensions);
	}


	class FadeThread extends Thread {

		private HashMap labelsMap;
		private boolean fadeLabels = true;
		private boolean isRunning = false;

		public FadeThread(HashMap labelsMap, boolean fadeLabels) {
			super("FadeThread");
			this.labelsMap = labelsMap;
			this.fadeLabels = fadeLabels;
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void run() {
			if (labelsMap.size() > 0) {
				isRunning = true;
				try {
					try {
						Thread.sleep(400);
					} catch (InterruptedException ignore) {}
					// fade out the labels that will be hidden
					if (fadeLabels) {
						Vector acts = new Vector();
						for (Iterator iter = labelsMap.keySet().iterator(); iter.hasNext(); ) {
							PShrimpLabel label = (PShrimpLabel) iter.next();
							Object[] vals = (Object[]) labelsMap.get(label);
							boolean vis = ((Boolean)vals[0]).booleanValue();
							if (!vis) {
								acts.add(label.animateToTransparency(0f, 1000));
							}
						}
						if (acts.size() > 0) {
							ActivityManager activityManager = new ActivityManager(acts);
							activityManager.waitUntilFinished();
						}
					}
				} catch (Throwable t) {
				}

				// restore the label state
				try {
					for (Iterator iter = labelsMap.keySet().iterator(); iter.hasNext(); ) {
						PShrimpLabel label = (PShrimpLabel) iter.next();
						Object[] vals = (Object[]) labelsMap.get(label);
						boolean vis = ((Boolean)vals[0]).booleanValue();
						Color c = (Color) vals[1];
						float trans = ((Float)vals[2]).floatValue();
						label.setVisible(vis);
						label.setTextColor(c);
						label.setTransparency(trans);
						label.repaint();
					}
				} catch (Throwable t) {
				}
				// layout is finished
				isRunning = false;
			}
		}
	};

}
