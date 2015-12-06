/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.DiamondNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.EllipseNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RoundedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.TriangleNodeShape;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Defines a node shape visual variable, meaning that this visual variable will have
 * discrete values of type NodeShape.
 *
 * @author Rob Lintern
 */
public class NodeShapeVisualVariable extends NominalVisualVariable {

	public final static NodeShape DEFAULT_NODE_SHAPE = new RoundedRectangleNodeShape();

	/**
	 * Constructs a new node shape visual variable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public NodeShapeVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
	 */
	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		return DEFAULT_NODE_SHAPE;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
	 */
	public Object getVisVarValueFromString(String s) {
		Object visVarValue = null;

		ShrimpProject project = attrToVisVarBean.getProject();
		if (project != null) {
			try {
				DisplayBean displayBean = (DisplayBean) project.getTool(ShrimpProject.SHRIMP_VIEW).getBean(ShrimpTool.DISPLAY_BEAN);
				visVarValue = displayBean.getNodeShape(s);
			} catch (BeanNotFoundException e) {
			} catch (ShrimpToolNotFoundException e) {
			}
		}
		if (visVarValue == null) {
			// for testing
			if (s.equals(RectangleNodeShape.NAME)) {
				visVarValue = new RectangleNodeShape();
			} else if (s.equals(RoundedRectangleNodeShape.NAME)) {
				visVarValue = new RoundedRectangleNodeShape();
			} else if (s.equals(EllipseNodeShape.NAME)) {
				visVarValue = new EllipseNodeShape();
			} else if (s.equals(TriangleNodeShape.NAME)) {
				visVarValue = new TriangleNodeShape();
			} else if (s.equals(DiamondNodeShape.NAME)) {
				visVarValue = new DiamondNodeShape();
			} else {
				visVarValue = DEFAULT_NODE_SHAPE;
				System.err.println("NodeShapeVisualVariable: warning - unknown node shape: " + s);
			}
		}
		return visVarValue;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
	 */
	public String getStringFromVisVarValue(Object visVarValue) {
		return ((NodeShape)visVarValue).getName();
	}

	/**
	 * Don't save the default node shape.
	 */
	protected void saveNomVisVarValue(String attrName, Object attrValue, Object visVarValue) {
		boolean sameAsDefault= ShrimpUtils.equals(DEFAULT_NODE_SHAPE, visVarValue);
		if (sameAsDefault) {
			// clears the property
			visVarValue = null;
		}
		super.saveNomVisVarValue(attrName, attrValue, visVarValue);
	}

}
