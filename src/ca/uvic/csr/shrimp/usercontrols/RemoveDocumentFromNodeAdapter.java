/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.NodeDocument;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This UserAction adapter lets the user remove an attached document from the selected node.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.DocumentManager
 */
public class RemoveDocumentFromNodeAdapter extends DefaultToolAction {

	private NodeDocument document;

    public RemoveDocumentFromNodeAdapter(ShrimpTool tool, NodeDocument doc) {
    	super(doc.getFilename(), doc.getIcon(), tool);
		mustStartAndStop = false;
		this.document = doc;
    }

    public void startAction() {
    	ShrimpNode node = getSelectedNode();
		if (node != null) {
			Artifact artifact = node.getArtifact();
			artifact.removeDocument(document);
		}
    }
}
