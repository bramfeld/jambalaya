/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import javax.swing.text.JTextComponent;

import ca.uvic.csr.shrimp.PanelModeConstants;

/**
 * @author Rob Lintern
 */
public class AnnotationPanelLoadEvent implements CustomizedPanelActionEvent {

	private String artifactID;
	private JTextComponent textbox;

	public AnnotationPanelLoadEvent(String artifactExternalID, JTextComponent textbox) {
		this.artifactID = artifactExternalID;
		this.textbox = textbox;
	}

	public String getArtifactID() {
		return artifactID;
	}

	public JTextComponent getTextComponent() {
		return textbox;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent#getPanelType()
	 */
	public String getPanelType() {
		return PanelModeConstants.PANEL_ANNOTATION;
	}

}
