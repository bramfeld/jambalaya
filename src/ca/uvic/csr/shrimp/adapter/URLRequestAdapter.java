/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.net.URI;
import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.URLRequestEvent;
import ca.uvic.csr.shrimp.DataBean.event.URLRequestListener;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.SearchBean.BrowseActionEvent;
import ca.uvic.csr.shrimp.SearchBean.BrowseActionListener;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;

/**
 * This adapter handles requests caused by clicking on a URL
 *
 * @author Casey Best
 * date: Sept 21, 2000
 */
public abstract class URLRequestAdapter implements URLRequestListener, BrowseActionListener {

	protected ViewTool tool;
	protected String attrName;
	protected String panelType;
	
	protected boolean requestedBySearch;

	public URLRequestAdapter(ViewTool tool, String panelType, String attrName) {
		this.tool = tool;
		this.attrName = attrName;
		this.panelType = panelType;
	}
	
	/**
	 * Processes the request to when a URL is clicked
	 * 
	 * @param event Carries the URL clicked on
	 */
	public void actionPerformed(CustomizedPanelActionEvent event) {
		if (event instanceof URLRequestEvent) {
			actionPerformed ((URLRequestEvent)event);
		}
	}

	/**
	 * Enables support for browsing a url from searches
	 * @param e
	 */
	public void browse(BrowseActionEvent e) {
		requestedBySearch = true;
		Vector v = e.getObjects();
		if (v.size() > 0) {
			URI uri = (URI) v.elementAt(0);
			openURL(uri);
		}
	}
		
	/**
	 * Passes on the request when a URL is clicked
	 * @param event Carries the URL clicked on
	 */
	public void actionPerformed(URLRequestEvent event) {
		requestedBySearch = false;
		URI uri = event.getURI();
		if (uri != null) {
			openURL(uri);
		} else {
		    String artifactExternalIDStr = event.getURIDescription();
		    Object artifactExternalID = artifactExternalIDStr;
		    // use the databean to turn this id string into an id object
		    ShrimpProject project = tool.getProject();
			if (project != null) {
				try {
					DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
                    artifactExternalID = dataBean.getArtifactExternalIDFromString(artifactExternalIDStr);
				} catch (BeanNotFoundException e) {
				    e.printStackTrace();
				}
			}
		    if (artifactExternalID != null && !artifactExternalID.equals("")) {
		        openURL (artifactExternalID);
			}
		}
	}
	
	/**
	 * Does the animation and loading involved with a URL
	 * @param URL url to be loaded
	 */
	private void openURL(URI uri) {
	    ShrimpProject project = tool.getProject();
        if (project == null) {
			return;
		}
        
        Vector artifacts = new Vector();
		try {
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			artifacts = dataBean.getArtifacts(true);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
		
		Artifact foundArtifact = null;
		for (int i = 0; i < artifacts.size(); i++) {
		    Artifact art = (Artifact)artifacts.elementAt(i);
		    URI artURI = (URI)art.getAttribute(attrName);
		    //System.out.println("artUIR: " + artURI);
		    if ((artURI != null) && uri.equals(artURI)) {
		    	foundArtifact = art;
		        break;
		    }
		}
		
		// try to find the base artifact another way - using the filename
		if (foundArtifact == null) {
		    String uriStr = uri.toString();
		    // extract the filename without the extension
		    int slash = uriStr.lastIndexOf("/");
		    int dot = uriStr.lastIndexOf(".");
		    if ((slash != -1) && (dot > slash)) {
		    	String artName = uriStr.substring(slash + 1, dot);
		    	for (int i = 0; i < artifacts.size(); i++) {
		    		Artifact art = (Artifact)artifacts.elementAt(i);
				    if (artName.equals(art.getName())) {
				    	foundArtifact = art;
				        break;
				    }
				}
		    }
		}
		
		if (foundArtifact == null) {
		    JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
		    	"Couldn't find the artifact for the URI: " + uri, "Error", JOptionPane.ERROR_MESSAGE);
		} else {
			openURL(foundArtifact.getExternalId());
		}
	}
	
	/**
	 * Does the animation and loading involved with a URL
	 * 
	 * @param String url to be loaded
	 */
	private void openURL(Object externalId) {
		// The url is an artifact id
	    ShrimpProject project = tool.getProject();
		if (project == null) {
			return;
		}
		
		try {
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			Artifact artTarget = dataBean.findArtifactByExternalId(externalId);
			if (artTarget == null) {
				JOptionPane.showMessageDialog(tool.getGUI(), "There isn't a destination node for that hyperlink!");
			} else {
				Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(artTarget, true);
				if (nodes.isEmpty()) {
					JOptionPane.showMessageDialog(tool.getGUI(), "There isn't a destination node for that hyperlink!");
				} else {
					tool.navigateToObject(nodes.firstElement());
					if (panelType != null && !panelType.equals("")) {
					    displayBean.setPanelMode(nodes, panelType);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the type of panel this listener is meant for
	 */
	public String getCustomizedPanelType() {
		return panelType;
	}
	
}