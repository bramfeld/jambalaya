/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import java.io.Serializable;
import java.net.URI;

import ca.uvic.csr.shrimp.DataBean.Artifact;

/**
 * This is an event thrown when a URL has been clicked on in the display bean.
 *
 * @author Casey Best
 * @date Sept 21, 2000
 */
public class URLRequestEvent implements CustomizedPanelActionEvent, Serializable {

	private String uriDescription; // description of a uri
	private URI uri;
	private Artifact artifact;
	private String panelType;

	public URLRequestEvent(URI uri, Artifact artifact, String panelType) {
		this.uriDescription = null;
		this.uri = uri;
		this.artifact = artifact;
		this.panelType = panelType;
	}

	/**
	 * Creates a request event with a description of a URI
	 * Makes it possible to use hyperlinks that don't have proper URI's
	 * For example: An artifact id could be used instead of a url
	 */
	public URLRequestEvent(String uriDescription, Artifact artifact, String panelType) {
		this.uri = null;
		this.uriDescription = uriDescription;
		this.artifact = artifact;
		this.panelType = panelType;
	}

	/*
	 * Returns the uri.
	 * Note: If returns null, try getURLDescription()
	 */
	public URI getURI() {
		return uri;
	}

	public String getPanelType() {
		return panelType;
	}

	/*
	 * Returns a description of the URL (for a link that does not have a proper URL)
	 */
	public String getURIDescription() {
		return uriDescription;
	}

	/*
	 * Returns the artifact
	 */
	public Artifact getArtifact() {
		return artifact;
	}

}
