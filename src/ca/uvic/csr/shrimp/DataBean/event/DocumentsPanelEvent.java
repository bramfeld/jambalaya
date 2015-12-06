/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import java.util.List;

import ca.uvic.csr.shrimp.PanelModeConstants;

/**
 * @author Chris Callendar
 * @date 28-Sep-07
 */
public class DocumentsPanelEvent implements CustomizedPanelActionEvent {

	public static final int DOCUMENTS_ADDED = 0;
	public static final int DOCUMENTS_REMOVED = 1;

	private String artifactID;
	// the current documents for the artifact
	private List/*<NodeDocument>*/ artifactDocuments;
	private int type;

	public DocumentsPanelEvent(String artifactID, List/*<NodeDocument>*/ artifactDocuments, int type) {
		this.artifactID = artifactID;
		this.artifactDocuments = artifactDocuments;
		this.type = type;
	}

	public String getArtifactID() {
		return artifactID;
	}

	public List/*<NodeDocument>*/ getDocuments() {
		return artifactDocuments;
	}

	public int getType() {
		return type;
	}

	/**
	 * @return the number of documents that the artifact has.
	 */
	public int getDocumentsCount() {
		return artifactDocuments.size();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent#getPanelType()
	 */
	public String getPanelType() {
		return PanelModeConstants.PANEL_DOCUMENTS;
	}

	public String toString() {
		return "DocumentsPanelEvent: " + (type == DOCUMENTS_ADDED ? "[docs added] " : "[docs removed] ") +
			getDocumentsCount() + " documents now";
	}

}
