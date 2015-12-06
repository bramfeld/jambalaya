/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.algorithms.TreeLayoutAlgorithm;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.util.NodeNameComparator;

/**
 * UML Layout
 *
 * @author Jeff Michaud
 * @date Apr 30, 2003
 */
public class ShrimpUMLLayout extends AbstractLayout {

	public ShrimpUMLLayout(DisplayBean displayBean) {
		super(displayBean, LayoutConstants.LAYOUT_UML, new TreeLayoutAlgorithm());
		layoutAlgorithm.setComparator(NodeNameComparator.NODE_NAME_COMPARATOR);
	}

	public void setupAndApplyLayout(Vector allNodes, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate, boolean separateComponents) {
		if (allNodes.isEmpty()) {
			return;
		}
		// add visible classes and interfaces only
		Vector validNodes = new Vector();
		for (int i = 0; i < allNodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) allNodes.elementAt(i);
			if (node.getArtifact().getType().toLowerCase().indexOf(JavaDomainConstants.CLASS_ART_TYPE) != -1 || node.getArtifact().getType().toLowerCase().indexOf("interface") != -1 && displayBean.isVisible(node)) {				
				validNodes.add(node);
			} else {
				if (!nodesToExclude.contains(node)) {
					nodesToExclude.add(node);
				}
			}
		}

		if (validNodes.isEmpty()) {
			return;
		}
		
		Vector validArcs = new Vector();
		for (Iterator iter = validNodes.iterator(); iter.hasNext();) {
			ShrimpNode childNode = (ShrimpNode) iter.next();
			Vector arcs = displayBean.getDataDisplayBridge().getShrimpArcs(childNode);
			for (Iterator iterator = arcs.iterator(); iterator.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iterator.next();
				if (arc.getRelationship().getType().toLowerCase().indexOf("extends") != -1 || arc.getRelationship().getType().toLowerCase().indexOf("implements") != -1 && displayBean.isVisible(arc)) {
					if (!validArcs.contains(arc)) {
						validArcs.add(arc);
					}
				}
			}
		}
		if (separateComponents) {
		    positionIncludedAndExcludedNodes_Components(validNodes, validArcs, nodesToExclude, bounds, animate);
		} else {
		    positionIncludedAndExcludedNodes(validNodes, validArcs, nodesToExclude, bounds, animate);
		}

	}

	protected boolean applyLayoutTo(Vector nodes, Vector arcs, Double bounds, boolean animate, int componentNum, int numComponents, Vector positions, Vector dimensions) {
	    /*
		double scale;
		double shift;

		//Layout the nodes in a tree		
		TreeLayoutAlgorithm treeAlgorithm = new TreeLayoutAlgorithm();
		treeAlgorithm.setComparator(new NodeNameComparator());

		//add artifacts to tree
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode sn = (ShrimpNode) nodes.elementAt(i);
			treeAlgorithm.add(sn);
		}

		//add relationships to tree
		for (int i = 0; i < arcs.size(); i++) {
			ShrimpArc arc = (ShrimpArc) arcs.elementAt(i);
			
			ShrimpNode srcNode = arc.getSrcNode();
			ShrimpNode destNode = arc.getDestNode();
			// Add unexchangeable relation to the layout			
			treeAlgorithm.addRelation(srcNode, destNode, false);
		}

		Vector transforms = new Vector(nodes.size());
		Vector points = new Vector(nodes.size());

		if (nodes.size() > 1) {

			//add the nodes connected by extends and implements arcs to the algorithm
			treeAlgorithm.add(nodes);

			treeAlgorithm.compute();
			treeAlgorithm.fitWithinBounds(bounds);
			Dimension recSize = treeAlgorithm.queryRecommendedNodeSize();

			for (int i = 0; i < nodes.size(); i++) {
				ShrimpNode sn = (ShrimpNode) nodes.elementAt(i);
				Point2D point = treeAlgorithm.queryPosition(sn);
				points.add(point);
			}

			for (int i = 0; i < nodes.size(); i++) {
				ShrimpNode sn = (ShrimpNode) nodes.elementAt(i);
				displayBean.setPanelMode(sn, SimpleArtifact.PANEL_UML);
				DoubleDimension dim = (DoubleDimension) sn.getArtifact().getAttribute("UMLDimension");
				if (dim == null) {
					dim = new DoubleDimension (recSize.getWidth(), recSize.getHeight());
				}
				Point2D point = (Point2D) points.elementAt(i);
				positions.add(point);
				dimensions.add(dim);
				adjustAllOtherNodes(nodes, points, sn, point, recSize);
			} 
			
			double furthestRight = java.lang.Double.MIN_VALUE;
			double furthestLeft = java.lang.Double.MAX_VALUE;			
			for (int i=0; i<points.size(); i++ ){

				ShrimpNode sn = (ShrimpNode) nodes.elementAt(i);
				Point2D point = (Point2D) points.elementAt(i);

				if (furthestRight < point.getX() + sn.getOuterBounds().width * 2)
					furthestRight = point.getX() + sn.getOuterBounds().width * 2;

				if (furthestLeft > point.getX())
					furthestLeft = point.getX();

			}

			shift = 0;
			if (bounds.width < (furthestRight - furthestLeft)) {
				scale = (bounds.width / (furthestRight - furthestLeft));
				shift = furthestLeft - (furthestLeft * scale);
			} else {
				scale = ((furthestRight - furthestLeft) / bounds.width);
				shift = furthestLeft - (furthestLeft * scale);
			}

			for (int i = 0; i < nodes.size(); i++) {
				Point2D point = (Point2D) points.elementAt(i);
				AffineTransform move = new AffineTransform();
				move.translate(shift + point.getX() * scale, point.getY());
				move.scale(scale, scale);
				transforms.add(move);
			}
		}
		*/

		return true;
	}
	
	/*
	private void adjustAllOtherNodes(Vector nodes, Vector points, ShrimpNode currentNode, Point2D currentPoint, Dimension recSize) {
		Point2D point;
		ShrimpNode sn;

		for (int i = 0; i < nodes.size(); i++) {
			sn = (ShrimpNode) nodes.elementAt(i);

			if (sn == currentNode)
				continue;

			point = (Point2D) points.elementAt(i);

			if (currentPoint.getX() < point.getX() && currentPoint.getY() == point.getY()) {
				double width = currentNode.getOuterBounds().width - recSize.width;

				point.setLocation(point.getX() + width + 2, point.getY());

			}

		}
	}
	*/
}
