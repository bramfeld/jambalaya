/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import ca.uvic.csr.shrimp.PanelModeConstants;

/**
 * @author Rob Lintern
 */
public class AnnotationPanelSaveEvent implements CustomizedPanelActionEvent {

	private String artifactID;
	private String annotation;

	public AnnotationPanelSaveEvent(String artifactExternalID, String annotation) {
		this.artifactID = artifactExternalID;
		this.annotation = annotation;
	}

	public String getArtifactID() {
		return artifactID;
	}

	public String getAnnotation() {
		return annotation;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent#getPanelType()
	 */
	public String getPanelType() {
		return PanelModeConstants.PANEL_ANNOTATION;
	}
}
