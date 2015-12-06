/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.mylar.zest.layouts.LayoutStyles;

import ca.uvic.cs.seqlayout.algorithms.SequenceLayoutAlgorithm;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArrowHead;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.util.NodeNameComparator;

/**
 * Sequence diagram layout
 * @tag Shrimp(sequence)
 * @author Chris Bennett
 */
public class SequenceLayout extends AbstractLayout {

	private LifelineGroupingManager lifeLineGroupingManager;
	private  MethodExecGroupingManager methodExecGroupingManager;


	public SequenceLayout(DisplayBean displayBean, String name) {
		super(displayBean, name, new SequenceLayoutAlgorithm(LayoutStyles.NONE));
		lifeLineGroupingManager = displayBean.getLifelineGroupingManager();
		methodExecGroupingManager = displayBean.getMethodExecGroupingManager();
		setSequenceApplicationProperties();
	}

	/**
	 * Set the default sequence application properties to ensure correct display
	 * of sequence charts.
	 */
	private void setSequenceApplicationProperties() {
		Properties properties = ApplicationAccessor.getProperties();
		if (properties.getProperty("attrToVisVarMap|Node Type|node shape|Object") == null) {
			properties.setProperty("attrToVisVarMap|Node Type|node shape|Object",
				"ObjectLifeLine");
		}
		if (properties.getProperty("attrToVisVarMap|Node Type|node color|Object") == null) {
		properties.setProperty("attrToVisVarMap|Node Type|node color|Object",
			"204,204,255");
		}
		if (properties.getProperty("attrToVisVarMap|Node Type|label style|MethodExecution") == null) {
			properties.setProperty("attrToVisVarMap|Node Type|label style|MethodExecution",
				"Hide");
			}
		if (properties.getProperty("attrToVisVarMap|Node Type|node shape|Actor") == null) {
			properties.setProperty("attrToVisVarMap|Node Type|node shape|Actor",
				"ObjectLifeLine");
		}
		if (properties.getProperty("attrToVisVarMap|Arc Type|arrow head style|MethodCall") == null) {
			properties.setProperty("attrToVisVarMap|Arc Type|arrow head style|MethodCall",
				"Full_Filled");
		}
		if (properties.getProperty("attrToVisVarMap|Arc Type|arc style|ReturnValue") == null) {
			properties.setProperty("attrToVisVarMap|Arc Type|arc style|ReturnValue",
				"Dotted Line - Straight");
		}
		if (properties.getProperty("attrToVisVarMap|Arc Type|arc color|ReturnValue") == null) {
			properties.setProperty("attrToVisVarMap|Arc Type|arc color|ReturnValue",
				"205,92,92");
		}
		if (properties.getProperty("attrToVisVarMap|Arc Type|arrow head style|ReturnValue") == null) {
			properties.setProperty("attrToVisVarMap|Arc Type|arrow head style|ReturnValue",
				"Open");
		}
		if (properties.getProperty("attrToVisVarMap|Node Type|node color|MethodExecution") == null) {
			properties.setProperty("attrToVisVarMap|Node Type|node color|MethodExecution",
				"204,204,255");
		}
		ApplicationAccessor.getApplication().saveProperties();
	}

	public SequenceLayout(PFlatDisplayBean displayBean, String name,
			boolean inverted, NodeNameComparator comparator) {
		this(displayBean, name);
		layoutAlgorithm.setComparator(comparator);
		setReturnValueArrowHeads(displayBean);
	}

	/**
	 * Reset this layout's group filters to avoid problem with refiltering nodes
	 */
	public void reset() {
		lifeLineGroupingManager.clearGroupedNodeFilter();
		methodExecGroupingManager.clearGroupedNodeFilter();
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.layout.Layout#applyLayout(java.util.Vector, java.awt.geom.Rectangle2D.Double, java.util.Vector, boolean, boolean)
	 */
	public void setupAndApplyLayout(Vector nodes, Rectangle2D.Double bounds,
			Vector nodesToExclude, boolean showDialog, boolean animate,
			boolean separateComponents) {
		HashMap groupedProperties = new HashMap();

		lifeLineGroupingManager.handleNodeGrouping(nodes, groupedProperties);

		methodExecGroupingManager.handleNodeGrouping(nodes, groupedProperties);


		// This is needed because alternatively grouping and ungrouping
		// object lifelines and method execs can leave behind unused properties
		methodExecGroupingManager.cleanupGroupedProperties(groupedProperties);


		super.setupAndApplyLayout(nodes, bounds, nodesToExclude,
				showDialog, animate, separateComponents);
		methodExecGroupingManager.initDisplayFilters();
		displayBean.setNodeEdgeMovement(false);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.DisplayBean#setUsingArrowHeads(boolean)
	 * @tag Shrimp(sequence):
	 */
	public void setReturnValueArrowHeads(DisplayBean displayBean) {
		Vector arcs = displayBean.getAllArcs();
		for (int i = 0; i < arcs.size(); i++) {
			ShrimpArc arc = (ShrimpArc) arcs.elementAt(i);
			if (arc.getRelationship().getType().equals(
					JavaDomainConstants.RETURN_VALUE_REL_TYPE)) {
				((ShrimpArc) arcs.elementAt(i)).setUsingArrowHead(true, ArrowHead.OPEN);
			}
		}
	}
}
