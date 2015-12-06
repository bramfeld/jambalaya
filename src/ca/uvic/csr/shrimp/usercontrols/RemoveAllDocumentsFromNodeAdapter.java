/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This UserAction adapter removes all attached documents from the selected node.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.DocumentManager
 */
public class RemoveAllDocumentsFromNodeAdapter extends DefaultToolAction {

	private Artifact selectedArtifact;

	/**
	 * This constructor will get the selected artifact from the tool's {@link SelectorBean}.
	 */
	public RemoveAllDocumentsFromNodeAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, ResourceHandler.getIcon("icon_attachment_delete.gif"), tool);
    	setToolTip("Removes all documents from the selected node (annotations are not removed)");
		mustStartAndStop = false;
		this.selectedArtifact = null;
    }

	/**
	 * This constructor will get the selected artifact from the tool's {@link SelectorBean}.
	 */
	public RemoveAllDocumentsFromNodeAdapter(ShrimpTool tool) {
    	this(ShrimpConstants.ACTION_NAME_REMOVE_ALL_DOCUMENTS, tool);
    }

	/**
	 * Use this constructor only if you already have the selected artifact.
	 */
    public RemoveAllDocumentsFromNodeAdapter(String actionName, Artifact artifact) {
    	this(actionName, (ShrimpTool)null);
    	this.selectedArtifact = artifact;
    }

    public void startAction() {
    	Artifact artifact = selectedArtifact;
    	// get the artifact from the SelectorBean
    	if (artifact == null) {
    		ShrimpNode node = getSelectedNode();
    		if (node != null) {
				artifact = node.getArtifact();
			}
    	}
		if (artifact != null) {
			artifact.removeAllDocuments();
		}
    }


}
