/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.io.File;

import javax.swing.JFileChooser;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
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
public class AttachDocumentToNodeAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_ATTACH_DOCUMENT;
	public static final String TOOLTIP = "Attaches a document to the selected node.  The document can be viewed externally or internally depending on the content type.";

	private Artifact selectedArtifact;
	private JFileChooser fileChooser;

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected artifact will be retrieved from the {@link SelectorBean}.
	 */
    public AttachDocumentToNodeAdapter(ShrimpTool tool) {
    	this(ACTION_NAME, tool);
    }

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected artifact will be retrieved from the {@link SelectorBean}.
	 */
    public AttachDocumentToNodeAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, tool);
    	setToolTip(TOOLTIP);
    	setIcon(ResourceHandler.getIcon("icon_attachment.gif"));
		mustStartAndStop = false;
		this.selectedArtifact = null;
    }

    /**
     * Use this constructor if you already have access to the artifact.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected artifact will be retrieved.
     */
    public AttachDocumentToNodeAdapter(String actionName, Artifact artifact) {
    	this(actionName, (ShrimpTool)null);
    	this.selectedArtifact = artifact;
    }

    public boolean canStart() {
    	return super.canStart() || (selectedArtifact != null);
    }

    public void startAction() {
    	Artifact artifact = selectedArtifact;
    	if (artifact == null) {
			ShrimpNode node = getSelectedNode();
			if (node != null) {
				artifact = node.getArtifact();
			}
    	}
    	if (artifact != null) {
    		chooseFile(artifact);
		}
    }

    protected void chooseFile(Artifact artifact) {
		JFileChooser chooser = getFileChooser();
		chooser.setDialogTitle("Choose files to attach to '" + artifact.getName() + "'");
		int rv = chooser.showOpenDialog(ApplicationAccessor.getParentFrame());
		if (rv == JFileChooser.APPROVE_OPTION) {
			File[] files = chooser.getSelectedFiles();
			for (int i = 0; i < files.length; i++) {
				artifact.attachDocument(files[i].getAbsolutePath(), true);
			}
		}
    }

    private JFileChooser getFileChooser() {
    	if (fileChooser == null) {
    		fileChooser = new JFileChooser();
			fileChooser.setMultiSelectionEnabled(true);
    	}
    	return fileChooser;
    }

}
