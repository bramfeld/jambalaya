/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleNodeShape;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * @author Rob Lintern, Chris Callendar
 */
public class AttrToVisVarChangeAdapter implements AttrToVisVarChangeListener {

	private ShrimpProject project;
	private ShrimpTool tool;

	public AttrToVisVarChangeAdapter(ShrimpProject project, ShrimpTool tool) {
		this.project = project;
		this.tool = tool;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener#valuesChanged(ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent)
	 */
	public void valuesChanged(AttrToVisVarChangeEvent e) {
		Attribute attr = e.getAttribute();
		List visVars = e.getVisualVariables();
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
			for (Iterator iter = visVars.iterator(); iter.hasNext();) {
				VisualVariable visVar = (VisualVariable) iter.next();
				String visVarName = visVar.getName();
				if (VisVarConstants.VIS_VAR_NODE_COLOR.equals(visVarName)) {
					// recolor all nodes
					updateNodeColors(attrToVisVarBean, displayBean, attr.getName(), visVarName);
				} else if (VisVarConstants.VIS_VAR_NODE_SHAPE.equals(visVarName)) {
					// reshape all nodes
					updateNodeShapes(attrToVisVarBean, displayBean, attr.getName(), visVarName);
				} else if (VisVarConstants.VIS_VAR_NODE_IMAGE.equals(visVarName)) {
					// update the image and node borders (the on/off border flags are stored here) for each node
					updateNodeImages(attrToVisVarBean, displayBean, attr.getName(), visVarName);
					updateNodeBorderColors(attrToVisVarBean, displayBean, attr.getName(),
							VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR,
							AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_COLOR);
					updateNodeBorderColors(attrToVisVarBean, displayBean, attr.getName(),
							VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR,
							AttributeConstants.NOM_ATTR_ARTIFACT_INNER_BORDER_COLOR);
				} else if (VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR.equals(visVarName)) {
						// recolor all nodes
					updateNodeBorderColors(attrToVisVarBean, displayBean, attr.getName(), visVarName,
							AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_COLOR);
				} else if (VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR.equals(visVarName)) {
					// recolor all nodes
					updateNodeBorderColors(attrToVisVarBean, displayBean, attr.getName(), visVarName,
							AttributeConstants.NOM_ATTR_ARTIFACT_INNER_BORDER_COLOR);
				} else if (VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE.equals(visVarName)) {
					// recolor all nodes
					updateNodeBorderStyles(attrToVisVarBean, displayBean, attr.getName(), visVarName,
							AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_STYLE);
				} else if (VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE.equals(visVarName)) {
					// recolor all nodes
					updateNodeBorderStyles(attrToVisVarBean, displayBean, attr.getName(), visVarName,
							AttributeConstants.NOM_ATTR_ARTIFACT_INNER_BORDER_STYLE);
				} else if (VisVarConstants.VIS_VAR_ARC_COLOR.equals(visVarName)) {
					// recolor all arcs
					updateArcColors(attrToVisVarBean, displayBean, attr.getName(), visVarName);
				} else if (VisVarConstants.VIS_VAR_ARC_STYLE.equals(visVarName)) {
					// restyle all arcs
					updateArcStyle(attrToVisVarBean, displayBean, attr.getName(), visVarName);
				} else if (VisVarConstants.VIS_VAR_LABEL_STYLE.equals(visVarName)) {
					// restyle all arcs
					updateLabelStyle(attrToVisVarBean, displayBean, attr.getName(), visVarName);
				} else if (VisVarConstants.VIS_VAR_TOOLTIP_TEXT.equals(visVarName)) {
					// do nothing
				} else {
					//((new Exception ("cant handle vis var:" + visVar))).printStackTrace();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	/**
	 * Update the label style for all related nodes
	 * @param attrToVisVarBean
	 * @param displayBean
	 * @param attrName
	 * @param visVarName
	 */
	private void updateLabelStyle(AttrToVisVarBean attrToVisVarBean,
			DisplayBean displayBean, String attrName, String visVarName) {
		Vector nodes = displayBean.getAllNodes();
		for (int i=0; i<nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			Artifact artifact = node.getArtifact();
			Object attrValue = (artifact != null) ? artifact.getAttribute(attrName) : null;
			String labelStyle = DisplayConstants.LABEL_STYLE_FULL;
			if (attrValue != null) {
				labelStyle= (String) attrToVisVarBean.getVisVarValue(attrName, visVarName, attrValue);
			}
			node.setLabelStyle(labelStyle);
		}
	}

	private void updateNodeColors(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean, String attrName, String visVarName) {
		// update the display bean
		Vector nodes = displayBean.getAllNodes();
		SelectorBean selector = null;
		try {
			selector = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException e) {
		}

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			ShrimpNodeLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(node, false);
			Artifact artifact = node.getArtifact();
			Color color = null;
			// update the node and label color, font, and transparency based on the artifact attributes
			if (artifact != null) {
				//@tag Shrimp.GXL_Colors: check for color attribute
				// Color override - check for the color attribute (e.g. "#00ff00" or "green")
				color = displayBean.getDataDisplayBridge().loadNodeColorFromArtifact(artifact, null);
				// if the node is interesting then select it
				boolean interesting = displayBean.getDataDisplayBridge().updateNodeTransparencyBasedOnInterest(node, artifact);
				if (interesting && (selector != null)) {
					Vector selectedNodes = (Vector) selector.getSelected(SelectorBeanConstants.SELECTED_NODES);
					if (!selectedNodes.contains(node)) {
						selectedNodes.add(node);
						selector.setSelected(SelectorBeanConstants.SELECTED_NODES, selectedNodes);
					}
				}
			}

			node.setColor((color != null ? color: ShrimpNode.DEFAULT_NODE_COLOR));
			if (label != null) {
				label.setBackgroundColor(color);
			}
		}
	}

	private void updateNodeShapes(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean, String attrName, String visVarName) {
		// update the display bean
		Vector nodes = displayBean.getAllNodes();

		for (int i=0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			Artifact artifact = node.getArtifact();
			Object attrValue = (artifact != null) ? artifact.getAttribute(attrName) : null;
			NodeShape nodeShape = new RectangleNodeShape();
			if (attrValue != null) {
				nodeShape = (NodeShape) attrToVisVarBean.getVisVarValue(attrName, visVarName, attrValue);
			}
			node.setNodeShape(nodeShape);
		}
	}

	private void updateNodeImages(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean, String attrName, String visVarName) {
		Vector nodes = displayBean.getAllNodes();
		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			Artifact artifact = node.getArtifact();
			Object attrValue = (artifact != null ? artifact.getAttribute(attrName) : null);
			NodeImage nodeImage = node.getNodeImage();
			if (attrValue != null) {
				String value = (String) attrToVisVarBean.getVisVarValue(attrName, visVarName, attrValue);
				if (value != null) {
					// updates the node's image and properties
					NodeImage.parseAndUpdate(nodeImage, value);
					node.repaint();
				}
			}
		}
	}

	private void updateNodeBorderColors(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean,
					String attrName, String visVarName, String borderColorAttributeName) {
		// update the display bean
		Vector nodes = displayBean.getAllNodes();

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			Artifact artifact = node.getArtifact();
			Color color = null;
			// update the node border color based on the artifact attributes
			if (artifact != null) {
				//@tag Shrimp.GXL_Colors : check for color attribute
				// Color override - check for the color attribute (e.g. "#00ff00" or "green")
				color = displayBean.getDataDisplayBridge().loadNodeBorderColorFromArtifact(artifact,
								null, borderColorAttributeName, visVarName);
			}
			if (borderColorAttributeName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_COLOR)) {
				node.setOuterBorderColor(color);
			}
			else {
				node.setInnerBorderColor(color);
			}
		}
	}

	private void updateNodeBorderStyles(AttrToVisVarBean attrToVisVarBean,
			DisplayBean displayBean, String name, String visVarName, String
			borderStyleAttributeName) {
		// update the display bean
		Vector nodes = displayBean.getAllNodes();

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpNode node = (ShrimpNode) nodes.elementAt(i);
			Artifact artifact = node.getArtifact();
			String style = null;
			// update the node border colorbased on the artifact attributes
			if (artifact != null) {
				// Style override - check for the style attribute (e.g. "plain" or "dashed")
				style = displayBean.getDataDisplayBridge().loadNodeBorderStyleFromArtifact(artifact, null,
						borderStyleAttributeName, visVarName);
			}
			if (borderStyleAttributeName.equals(AttributeConstants.NOM_ATTR_ARTIFACT_OUTER_BORDER_STYLE)) {
				node.setOuterBorderStyle((style != null ? style: NodeBorder.DEFAULT_BORDER_STYLE));
			}
			else {
				node.setInnerBorderStyle((style != null ? style: NodeBorder.DEFAULT_BORDER_STYLE));
			}
		}
	}

	private void updateArcColors(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean, String attrName, String visVarName) {
		// update the display bean
		Vector arcs = displayBean.getAllArcs();

		for (int i = 0; i < arcs.size(); i++) {
			ShrimpArc arc = (ShrimpArc) arcs.elementAt(i);
			Relationship rel = arc.getRelationship();
			if (rel != null) {
				// @tag Shrimp(GXL_Colors): arc colors
				Color color = displayBean.getDataDisplayBridge().getArcColorFromRelationship(rel, attrToVisVarBean, null);
				arc.setColor((color != null ? color : ShrimpArc.DEFAULT_ARC_COLOR));
				ShrimpArcLabel label = displayBean.getDataDisplayBridge().getShrimpArcLabel(arc, false);
				if (label != null) {
				    label.setTextColor(color);
				}
			}
		}
	}

	private void updateArcStyle(AttrToVisVarBean attrToVisVarBean, DisplayBean displayBean, String attrName, String visVarName) {
		// update the display bean
		Vector nodes = displayBean.getAllArcs();

		for (int i = 0; i < nodes.size(); i++) {
			ShrimpArc arc = (ShrimpArc) nodes.elementAt(i);
			Relationship rel = arc.getRelationship();
			ArcStyle arcStyle = new StraightSolidLineArcStyle ();
			if (rel != null) {
				Object attrValue = arc.getRelationship().getAttribute(attrName);
				if (attrValue != null) {
					arcStyle = (ArcStyle) attrToVisVarBean.getVisVarValue(attrName, visVarName, attrValue);
				}
				arc.setStyle(arcStyle);
			}
		}
	}
}


