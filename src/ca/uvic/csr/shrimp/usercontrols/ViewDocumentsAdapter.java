/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This UserAction adapter lets the user choose a document to attach to the selected node.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.DocumentManager
 */
public class ViewDocumentsAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_VIEW_DOCUMENTS;
	public static final String TOOLTIP = "Zooms in on the selected node and displays the attached documents and annotations in a table.  The documents can be viewed internally or externally depending on the content type.";

    public ViewDocumentsAdapter(ShrimpTool tool) {
    	super(ACTION_NAME, tool.getProject(), tool);
    	setToolTip(TOOLTIP);
    	setIcon(ResourceHandler.getIcon("icon_file.gif"));
		mustStartAndStop = false;
    }

    public void startAction() {
		ShrimpNode node = getSelectedNode();
		if (node != null) {
			try {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				displayBean.focusOn(node);
				displayBean.setPanelMode(node, PanelModeConstants.PANEL_DOCUMENTS);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
    }

}
