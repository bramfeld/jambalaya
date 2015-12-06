/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.Frame;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.NodeDocument;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This UserAction adapter lets the user choose a document to attach to the selected node.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.DocumentManager
 */
public class AttachURLToNodeAdapter extends AttachDocumentToNodeAdapter {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_ATTACH_URL;
	public static final String TOOLTIP = "Attaches a website URL to the selected node.  The website can be viewed externally or internally.";

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected artifact will be retrieved from the {@link SelectorBean}.
	 */
    public AttachURLToNodeAdapter(ShrimpTool tool) {
    	this(ACTION_NAME, tool);
    }

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected artifact will be retrieved from the {@link SelectorBean}.
	 */
    public AttachURLToNodeAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, tool);
    	setToolTip(TOOLTIP);
    	setIcon(ResourceHandler.getIcon("icon_file_web.gif"));
    }

    /**
     * Use this constructor if you already have access to the artifact.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected artifact will be retrieved.
     */
    public AttachURLToNodeAdapter(String actionName, Artifact artifact) {
    	super(actionName, artifact);
    	setToolTip(TOOLTIP);
    	setIcon(ResourceHandler.getIcon("icon_file_web.gif"));
    }

    protected void chooseFile(Artifact artifact) {
    	Frame parent = ApplicationAccessor.getParentFrame();
		Object response = JOptionPane.showInputDialog(parent,
    			"Enter the URI/URL of the website", "Attach a URL", JOptionPane.QUESTION_MESSAGE,
    			ResourceHandler.getIcon("icon_file_web.gif"), null, "http://");
		if (response != null) {
			String url = response.toString().trim();
			if (NodeDocument.isURL(url) || url.toLowerCase().startsWith("file:")) {
				artifact.attachDocument(url, true);
			} else {
				JOptionPane.showMessageDialog(parent, "Only valid website addresses are allowed.",
						"Invalid URL", JOptionPane.ERROR_MESSAGE);
			}
		}
    }

}
