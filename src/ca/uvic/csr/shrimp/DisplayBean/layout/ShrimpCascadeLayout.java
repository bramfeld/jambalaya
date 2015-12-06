/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.util.NodeNameComparator;

/**
 * @author Rob Lintern
 */
public class ShrimpCascadeLayout implements Layout {

	private final static double BORDER_WIDTH_PERCENT = 0.05;
	private final static double CHILD_WIDTH_PERCENT = 0.50;
	private final static double CHILD_HEIGHT_PERCENT = 0.50;

	private DisplayBean displayBean;

	/**
	 * @param displayBean
	 */
	public ShrimpCascadeLayout(DisplayBean displayBean) {
		this.displayBean = displayBean;
	}

	/**
	 * Not required for this type of layout
	 */
	public void resetLayout() {
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#getName()
	 */
	public String getName() {
		return "Cascade";
	}

	public boolean includeArc(ShrimpArc arc) {
		return true;
	}

	public void setArcTypes(Collection arcTypes) {
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#setupAndApplyLayout(java.util.Vector, java.awt.geom.Rectangle2D.Double, java.util.Vector, boolean, boolean, boolean)
	 */
	public void setupAndApplyLayout(Vector nodes, Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate,
			boolean separateComponents) {
		Vector visibleNodes = new Vector(nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			if (displayBean.isVisible(nodes.elementAt(i))) {
				visibleNodes.addElement(nodes.elementAt(i));
			}
		}

		Collections.sort(visibleNodes, NodeNameComparator.NODE_NAME_COMPARATOR);

		// leave 10% for unconnected nodes, if any
		double conHeight = 0;
		double unconHeight = 0;
		if (!nodesToExclude.isEmpty()) {
			conHeight = bounds.getHeight() * 0.9;
			unconHeight = bounds.getHeight() - conHeight;
			bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), conHeight);
		}

		boolean layoutApplied = true;
		if (visibleNodes.size() > 0) {
			layoutApplied = applyLayoutTo(visibleNodes, new Vector(), bounds, animate);
		}

		//now place any unconnected nodes in a grid
		if (layoutApplied && !nodesToExclude.isEmpty()) {
			//create a temporary grid layout to do the work
			ShrimpGridLayout grid = new ShrimpGridLayout(displayBean);
			bounds.setRect(bounds.getX(), bounds.getY() + conHeight, bounds.getWidth(), unconHeight * 0.9);
			grid.setupAndApplyLayout(nodesToExclude, bounds, new Vector(), false, animate, false);
		}
	}

	/**
	 *
	 * @param nodes
	 * @param arcs
	 * @param bounds
	 * @param animate
	 * @return True if the layout was completed successfully
	 */
	public boolean applyLayoutTo(Vector nodes, Vector arcs, Rectangle2D.Double bounds, boolean animate) {
		int numChildren = nodes.size();
		if (numChildren < 1) {
			return true;
		}

		double borderWidth = bounds.getWidth() * BORDER_WIDTH_PERCENT;
		double borderHeight = bounds.getHeight() * BORDER_WIDTH_PERCENT;
		double borderWH = Math.max(borderWidth, borderHeight);
		double newX = bounds.getX() + borderWH;
		double newY = bounds.getY() + borderWH;
		double newW = Math.max(5, bounds.getWidth() - 2.0d * borderWH);
		double newH = Math.max(5, bounds.getHeight() - 2.0d * borderWH);

		Rectangle2D.Double boundsClone = new Rectangle2D.Double(newX, newY, newW, newH);

		double nodeWidth = Math.max(5, boundsClone.width * CHILD_WIDTH_PERCENT);
		double nodeHeight = Math.max(5, boundsClone.height * CHILD_HEIGHT_PERCENT);

		String fontMode = displayBean.getDefaultLabelMode();
		if (fontMode.equals(DisplayConstants.LABEL_MODE_FIXED) || fontMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
			Font font = (Font) displayBean.getLabelFont();
			int fontHeight = displayBean.getFontHeightOnCanvas(font);
			if (fontHeight > 0) {
				nodeHeight = Math.max(5, nodeHeight - fontHeight);
			}
		}

		double horizOffset = (boundsClone.width - nodeWidth) / ((double) numChildren - 1);
		double vertOffset = (boundsClone.height - nodeHeight) / ((double) numChildren - 1);

		double offsetX = nodeWidth / 2; // horizontal distance from corner of child to middle of child
		double offsetY = nodeHeight / 2; // vertical distance from corner of child to middle of child

		Vector positions = new Vector(nodes.size());
		Vector dimensions = new Vector(nodes.size());
		Dimension dim = new Dimension((int) nodeWidth, (int) nodeHeight);
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode childNode = (ShrimpNode) nodes.elementAt(i);
			childNode.raiseAboveSiblings();
			double xmove = boundsClone.x + i * horizOffset + offsetX;
			double ymove = boundsClone.y + i * vertOffset + offsetY;
			positions.add(new Point2D.Double(xmove, ymove));
			dimensions.add(dim);
		}
		displayBean.setPositionsAndSizes(nodes, positions, dimensions, animate);

		return true;
	}

}
