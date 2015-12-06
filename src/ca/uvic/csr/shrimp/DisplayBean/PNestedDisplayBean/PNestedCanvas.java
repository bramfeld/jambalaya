/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JToolTip;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.SwingToolTip;
import ca.uvic.csr.shrimp.jambalaya.JambalayaApplication;
import ca.uvic.csr.shrimp.util.GraphicsUtils;
import edu.umd.cs.piccolo.PCanvas;

/**
 * @author Rob Lintern
 *
 */
public class PNestedCanvas extends PCanvas {

	private ShrimpProject project;
    private Color tooltipForegroundColor = Color.black;
    private Color tooltipBackgroundColor = Color.white;

    private Image canvasImage;

    public PNestedCanvas(ShrimpProject project) {
        super();
        this.project = project;
        setBackground(DisplayConstants.CANVAS_COLOUR);
    }

    public void removeNotify() {
    	// make an image of the canvas if Jambalaya - must be done here to fix a bug in Protege where in
    	// JambalayaTab.beforeHide() the canvas is already disposed.
    	if (ApplicationAccessor.getApplication() instanceof JambalayaApplication) {
    		int width = getWidth();
    		int height = getHeight();
    		canvasImage = createImage(width, height);
    	}

    	// this disposes the component, and makes it impossible for us to make an image from the canvas
    	super.removeNotify();
    }

    /**
     * Returns thet image of the canvas that was taken just before the canvas was disposed.
     * Only applicable in Jambalaya.
     */
    public Image getCanvasImage() {
    	return canvasImage;
    }

    /** Sets the canvas image to null. */
    public void clearCanvasImage() {
    	canvasImage = null;
    }

    private String getNodeToolTipText(ShrimpNode node) {
        String tooltipText = (String) node.getArtifact().getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_DISPLAY_TEXT);

        // a test to get fully qualified names
        if (tooltipText == null || tooltipText.equals("")) {
            tooltipText = (String) node.getArtifact().getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_LONG_NAME);
        }
        // end test

        if (tooltipText == null || tooltipText.equals("")) {
            tooltipText = node.getName();
        }

        // check and see if another attribute besides name is assigned to tooltip for nodes
        // ***** hack starts
        if (project != null) {
            try {
                AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
                Attribute attr = attrToVisVarBean.getMappedAttribute(VisVarConstants.VIS_VAR_TOOLTIP_TEXT);
                if (attr != null) {
                    Object attrValue = node.getArtifact().getAttribute(attr.getName());
                    if (attrValue != null) {
                        String tooltipVisVarText = (String) attrToVisVarBean.getVisVarValue(attr.getName(), VisVarConstants.VIS_VAR_TOOLTIP_TEXT, attrValue);
                        tooltipText += "\n    " + attr.getName() + " = " + tooltipVisVarText;
                    } else {
                        tooltipText += "\n    " + attr.getName() + " = " + "(no value)";
                    }
                }
            } catch (BeanNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        // ***** hack ends
        return tooltipText;
    }

    /**
     *
     * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
     */
    public String getToolTipText(MouseEvent e) {
        String tooltipText = "";
        if (e != null) {
            Object obj = getCamera().pick(e.getX(), e.getY(), 1).getPickedNode();
            if (obj instanceof ShrimpLabel) {
                obj = ((ShrimpLabel) obj).getLabeledObject();
            }
            if (obj instanceof ShrimpNode) {
                ShrimpNode node = (ShrimpNode) obj;
                tooltipText = getNodeToolTipText(node);
                Color c = node.getColor();
                tooltipBackgroundColor = c;
                tooltipForegroundColor = GraphicsUtils.getTextColor(c);
            } else if (obj instanceof ShrimpArc) {
                ShrimpArc arc = (ShrimpArc) obj;
                Relationship rel = arc.getRelationship();
                String arcType = (rel == null) ? "" : rel.getType();
                tooltipText = (rel == null) ? "" : (String) rel.getAttribute(AttributeConstants.NOM_ATTR_REL_DISPLAY_TEXT);
                if (tooltipText == null || tooltipText.equals("")) {
                    tooltipText = arc.getSrcNode().getName() + " ---" + arcType + "---> " + arc.getDestNode().getName();
                }
                if (arc instanceof ShrimpCompositeArc && ((ShrimpCompositeArc) arc).isAtHighLevel()) {
                    ShrimpCompositeArc compArc = (ShrimpCompositeArc) arc;
                    //tooltipText += "\nCount: " + compArc.getArcCount();
                    Collection rels = compArc.getRelationships();
                    int relIndex = 0;
                    int maxNumHiddenRelsToShow = 5;
                    List hiddenRelTexts = new ArrayList();
                    for (Iterator iter = rels.iterator(); iter.hasNext() && relIndex < maxNumHiddenRelsToShow;) {
                        Relationship hiddenRel = (Relationship) iter.next();
                        Artifact srcArtifact = (Artifact) hiddenRel.getArtifacts().elementAt(0);
                        Artifact destArtifact = (Artifact) hiddenRel.getArtifacts().elementAt(1);
                        String hiddenRelText = (String) hiddenRel.getAttribute(AttributeConstants.NOM_ATTR_REL_DISPLAY_TEXT);
                        if (hiddenRelText == null || hiddenRelText.equals("")) {
                            hiddenRelText = "\n " + srcArtifact.getName() + " ---" + hiddenRel.getType() + "---> " + destArtifact.getName();

                            // a test
                            //hiddenRelText = "\n   " + srcArtifact.getAttribute(Artifact.NOM_ATTR_ARTIFACT_LONG_NAME) + " ---" + hiddenRel.getType() + "---> " + destArtifact.getAttribute(Artifact.NOM_ATTR_ARTIFACT_LONG_NAME);
                        } else {
                            hiddenRelText = "\n   " + hiddenRelText;
                        }
                        hiddenRelTexts.add(hiddenRelText);
                        relIndex++;
                    }
                    Collections.sort(hiddenRelTexts);
                    for (Iterator iter = hiddenRelTexts.iterator(); iter.hasNext();) {
                        String hiddenRelText = (String) iter.next();
                        tooltipText += hiddenRelText;
                    }
                    if (rels.size() > maxNumHiddenRelsToShow) {
                        int numRelsNotShown = rels.size() - maxNumHiddenRelsToShow;
                        tooltipText += "\n  ... " + numRelsNotShown + " more";
                    }
                }
                Color c = arc.getColor();
                tooltipBackgroundColor = c;
                tooltipForegroundColor = GraphicsUtils.getTextColor(c);
            }
        }
        return tooltipText;
    }

    /**
     *
     * @see javax.swing.JComponent#createToolTip()
     */
    public JToolTip createToolTip() {
        return new SwingToolTip(tooltipForegroundColor, tooltipBackgroundColor);
    }

}