/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import edu.umd.cs.treemap.MapItem;
import edu.umd.cs.treemap.MapLayout;
import edu.umd.cs.treemap.Mappable;
import edu.umd.cs.treemap.Rect;
import edu.umd.cs.treemap.SimpleMapModel;
import edu.umd.cs.treemap.SquarifiedLayout;

/**
 * @author Jeff Michaud, Rob Lintern
 */
public class ComponentizeLayoutUtil {

	private boolean[] visited; // visited flag for determining components
	private Vector connectedComps; // a vector of ConnectedComponents
	private Vector validChildren; // valid nodes for this layout
	private DisplayBean displayBean;
	private Rectangle2D.Double bounds;

	public ComponentizeLayoutUtil(DisplayBean displayBean, Vector validChildren) {
		this.displayBean = displayBean;
		this.validChildren = validChildren;
		this.bounds = null;
	}

	public void setBoundsForAll(Rectangle2D.Double bounds) {
		this.bounds = bounds;
	}

	/**
	 * Determines the connected components that exist between the children.
	 */
	public void computeComponents() {
		visited = new boolean[validChildren.size()]; // visited flag
		connectedComps = new Vector ();

		// initialize
		for (int i = 0; i < validChildren.size(); i++) {
			visited[i] = false;
		}

		for (int i = 0; i < validChildren.size(); i++) {
			if (!visited[i]) {
				ConnectedComponent connectedComp = new ConnectedComponent ();
				connectedComps.add (connectedComp);
				dfs_visit (i, connectedComp);
			}
		}
	}

	public Vector getComponents() {
		return connectedComps;
	}

	public int getNumComponents() {
		return (connectedComps == null) ? 0 : connectedComps.size();
	}


	public void setComponents(Vector connectedComps) {
		this.connectedComps = connectedComps;
	}


	public void computeComponentBounds() {
		// count total number of nodes in all connected components
		int conNodeCount = 0;
		for (int i = 0; i < connectedComps.size(); i++) {
			ConnectedComponent connectedComp = (ConnectedComponent) connectedComps.elementAt(i);
			Vector nds = connectedComp.getNodes();
			conNodeCount += nds.size();
		}

		Comparator comparator = new Comparator () {
			public int compare(Object o1, Object o2) {
				ConnectedComponent comp1 = (ConnectedComponent) o1;
				ConnectedComponent comp2 = (ConnectedComponent) o2;
				return (comp1.getNodes().size() + comp1.getArcs().size()) - (comp2.getNodes().size() + comp2.getArcs().size());
			}
		};

		MapLayout mapLayout = new SquarifiedLayout ();

		// Sort the child nodes
		Collections.sort(connectedComps, comparator);

		//populate the map
		Mappable items[];
		SimpleMapModel map = new SimpleMapModel();

		items = new MapItem[connectedComps.size()];

		for (int i = 0; i < connectedComps.size(); i++) {
			ConnectedComponent comp = (ConnectedComponent)connectedComps.elementAt(i);
			int size = comp.getNodes().size() + comp.getArcs().size();
			items[i] = new MapItem();
			items[i].setSize(size);
		}

		map.setItems(items);

		Rect rect = new Rect();
		rect.h = bounds.height - 5;
		rect.w = bounds.width - 10;
		rect.x = bounds.x + 5;
		rect.y = bounds.y - 5;

		mapLayout.layout(map, rect);

		for (int i=0; i < connectedComps.size(); i++) {
			Rectangle2D.Double bounds = new Rectangle2D.Double(items[i].getBounds().x, items[i].getBounds().y, items[i].getBounds().w ,items[i].getBounds().h);
			ConnectedComponent comp = (ConnectedComponent)connectedComps.elementAt(i);
			comp.setBounds(bounds);
		}
	}

	/**
	 * Depth First Search
	 * @param i : index of node visiting
	 * @param connectedComp: the connected component of node visiting
	 */
	private void dfs_visit(int i, ConnectedComponent connectedComp) {
		ShrimpNode visitingNode = (ShrimpNode) validChildren.elementAt(i);
		connectedComp.addNode(visitingNode);
		visited[i] = true;

		// find adjacent nodes and visit them
		Vector allArcs = displayBean.getDataDisplayBridge().getShrimpArcs(visitingNode);
		//Vector compRelations =  displayBean.getComposites (visitingNode);
		//allArcs.addAll(compRelations);
		for (int j = 0; j < allArcs.size(); j++) {
			ShrimpArc arc = (ShrimpArc) allArcs.elementAt(j);
			if (isValidArc (arc)) {
				connectedComp.addArc(arc);
				// the "adjacent" node could be the source or destination of this
				// arc
				ShrimpNode src = arc.getSrcNode();
				ShrimpNode dest = arc.getDestNode();

				ShrimpNode adjNode = (visitingNode.equals(src)) ? dest: src;
				int adjIndex = validChildren.indexOf(adjNode);
				// visit the destination
				if (!visited[adjIndex]) {
					dfs_visit (adjIndex, connectedComp);
				}
			}

		}
	}

	/**
	 * A arc is valid for the spring layout
	 * if it is between two (different) children and is currently visible.
	 */
	private boolean isValidArc(ShrimpArc arc) {
		boolean valid = false;
		ShrimpNode src = arc.getSrcNode();
		ShrimpNode dest = arc.getDestNode();
		valid = validChildren.contains(src) &&
				validChildren.contains(dest) &&
				displayBean.isVisible (arc) &&
				(src != dest);
		return valid;
	}

}
