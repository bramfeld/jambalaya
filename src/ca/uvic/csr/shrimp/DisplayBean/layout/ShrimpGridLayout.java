/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.algorithms.GridLayoutAlgorithm;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.gui.ChooseAttributeDialog;
import ca.uvic.csr.shrimp.util.NodeAttributeComparator;
import ca.uvic.csr.shrimp.util.NodeNameComparator;

/**
 * This layout will place the visible children in a grid layout. The nodes will be sorted using a comparator, defaults to sorting by node name.
 * Sorting by relationships, children and artifact name is also allowed.
 * @author Jingwei Wu, Chris Callendar
 * @date Apr 5, 2001
 */
public class ShrimpGridLayout extends AbstractLayout {

	private Comparator comparator;

	public ShrimpGridLayout(DisplayBean displayBean) {
		this(displayBean, NodeNameComparator.NODE_NAME_COMPARATOR, "temporary grid layout", new GridLayoutAlgorithm());
	}

	public ShrimpGridLayout(DisplayBean displayBean, Comparator comparator, String name, GridLayoutAlgorithm gridAlgorithm) {
		super(displayBean, name, gridAlgorithm);
		this.comparator = comparator;
		gridAlgorithm.setComparator(comparator);
	}

	/*
	 * (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#applyLayout(java.util.Vector, java.awt.geom.Rectangle2D.Double, java.util.Vector, boolean,
	 *      boolean)
	 */
	public void setupAndApplyLayout(Vector nodes, Rectangle2D.Double bounds, Vector nodesToExclude, boolean showDialog, boolean animate,
			boolean separateComponents) {
		Vector nodesToInclude = new Vector(nodes.size());
		// Vector addedArcs = new Vector();

		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			ShrimpNode node = (ShrimpNode) iter.next();
			if (displayBean.isVisible(node)) {
				nodesToInclude.add(node);
			} else {
				if (!nodesToExclude.contains(node)) {
					nodesToExclude.add(node);
				}
			}
		}

		// if we are comparing by attribute, ask the user to choose an attribute
		if (comparator instanceof NodeAttributeComparator && showDialog) {
			// find a frame to "own" dialog
			ChooseAttributeDialog cad = new ChooseAttributeDialog(nodesToInclude);
			if (cad.isAccepted()) {
				String selectedAttribute = cad.getSelectedAttribute();
				if (selectedAttribute == null) {
					selectedAttribute = "";
				}
				((NodeAttributeComparator) comparator).setAttribute(selectedAttribute);
				for (Iterator iter = ((Vector) nodesToInclude.clone()).iterator(); iter.hasNext();) {
					ShrimpNode node = (ShrimpNode) iter.next();
					if (node.getArtifact().getAttribute(selectedAttribute) == null) {
						nodesToInclude.remove(node);
						nodesToExclude.add(node);
					}
				}
			} else {
				return;
			}
		}
		if (nodesToInclude.isEmpty() && nodesToExclude.isEmpty()) {
			return;
		}

		displayBean.getStructuralGroupingManager().handleNodeGrouping(nodes);

		layoutAlgorithm.setEntityAspectRatio(((Dimension) displayBean.getCanvasDimension()).getWidth()
				/ ((Dimension) displayBean.getCanvasDimension()).getHeight());
		if (displayBean.getSwitchLabelling() && nodesToInclude.size() >= displayBean.getSwitchAtNum()) {
			displayBean.setLabelMode(nodesToInclude, DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
		} else {
			displayBean.setLabelMode(nodesToInclude, displayBean.getDefaultLabelMode());
			boolean atLeastOneInFixedSizeMode = false;
			for (Iterator iter = nodesToInclude.iterator(); iter.hasNext() && !atLeastOneInFixedSizeMode;) {
				ShrimpNode node = (ShrimpNode) iter.next();
				String labelMode = displayBean.getLabelMode(node);
				atLeastOneInFixedSizeMode = labelMode.equals(DisplayConstants.LABEL_MODE_FIXED)
						|| labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL);
			}
			if (atLeastOneInFixedSizeMode) {
				Font font = (Font) displayBean.getLabelFont();
				((GridLayoutAlgorithm) layoutAlgorithm).setRowPadding(displayBean.getFontHeightOnCanvas(font));
			} else {
				((GridLayoutAlgorithm) layoutAlgorithm).setRowPadding(0);
			}
		}

		positionIncludedAndExcludedNodes(nodesToInclude, new Vector(), nodesToExclude, bounds, animate);
	}

}
