/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.event.AnnotationPanelSaveEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.adapter.AnnotationPanelActionAdapter;


/**
 *
 *
 * @author Chris Callendar
 * @date 2008-11-25
 */
public class ShrimpAnnotationNodeDocument extends NodeDocument implements CustomizedPanelActionListener {

	private Properties projectProperties;
	private String artifactID;
	private boolean hasContent;

	protected ShrimpAnnotationNodeDocument(String nodeName) {
		super();
		filename = ApplicationAccessor.getAppName() + " Annotation";
		directory = "The annotation for " + nodeName;
		path = directory;
		type = TYPE_ANNOTATION;
		icon = getIconForType(type);
		projectProperties = null;
		artifactID = null;
		hasContent = false;
	}

	public ShrimpAnnotationNodeDocument(String nodeName, String artifactID, Properties props) {
		this(nodeName);
		this.artifactID = artifactID;
		this.projectProperties = props;
		this.hasContent = (getContent().length() > 0);
	}

	public boolean exists() {
		return true;
	}

	public boolean canRemove() {
		// can't remove this document, it is always present
		return false;
	}

	public void removeAnnotation() {
		if ((projectProperties != null) && (artifactID != null)) {
			AnnotationPanelActionAdapter.setArtifactAnnotation(projectProperties, artifactID, null);
		}
	}

	public boolean canOpen() {
		// can't open, no need since the content is already displayed
		return false;
	}

	public boolean canEdit() {
		// allow edits
		return true;
	}

	public void setContent(String newContent) {
		if ((projectProperties != null) && (artifactID != null)) {
			hasContent = ((newContent != null) && (newContent.length() > 0));
			AnnotationPanelActionAdapter.setArtifactAnnotation(projectProperties, artifactID, newContent);
		}
	}

	public String getContent() {
		if ((projectProperties != null) && (artifactID != null)) {
			return AnnotationPanelActionAdapter.getArtifactAnnotation(projectProperties, artifactID);
		}
		return "";
	}

	public boolean hasContent() {
		return hasContent;
	}

	public URL getURL() throws MalformedURLException {
		return null;
	}

	public boolean saveInProperties() {
		// don't save these, they are saved separately
		return false;
	}

	public void actionPerformed(CustomizedPanelActionEvent e) {
		if (e instanceof AnnotationPanelSaveEvent) {
			AnnotationPanelSaveEvent apse = (AnnotationPanelSaveEvent) e;
			String annotation = apse.getAnnotation();
			hasContent = (annotation.trim().length() > 0);
		}
	}

	public String getCustomizedPanelType() {
		return PanelModeConstants.PANEL_ANNOTATION;
	}

}
