/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.io.Serializable;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;

/**
 * This is an event thrown when a Protege panel is double clicked
 *
 * @author Casey Best
 * @date June 29, 2001
 */
public class ProtegeDoubleClickEvent implements CustomizedPanelActionEvent, Serializable {

	private Artifact target;
	private Artifact source;

	public ProtegeDoubleClickEvent(Artifact target, Artifact source) {
		this.target = target;
		this.source = source;
	}

	/**
	 * Returns the target to focus on
	 */
	public Artifact getTarget() {
		return target;
	}

	/**
	 * Returns the source that was double clicked on
	 */
	public Artifact getSource() {
		return source;
	}

	/**
	 * @see CustomizedPanelActionEvent#getPanelType()
	 */
	public String getPanelType() {
		return ProtegeArtifact.PANEL_FORM;
	}

}
