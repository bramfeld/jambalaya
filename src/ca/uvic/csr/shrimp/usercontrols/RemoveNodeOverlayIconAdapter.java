/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This UserAction adapter removes the ndoe overlay icon from the selected node.
 *
 * @author Chris Callendar
 * @date 26-Oct-07
 * @tag Shrimp.ChangeNodeIcon
 */
public class RemoveNodeOverlayIconAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_REMOVE_OVERLAY_ICON;
	public static final String TOOLTIP = "Removes the overlay icon for the given node.";

	private ShrimpNode selectedNode;

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public RemoveNodeOverlayIconAdapter(ShrimpTool tool) {
    	this(ACTION_NAME, tool);
    }

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public RemoveNodeOverlayIconAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, ResourceHandler.getIcon("icon_node_overlay_remove.gif"), tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;
		this.selectedNode = null;
    }

    /**
     * Use this constructor if you already have access to the node.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected node will be retrieved.
     */
    public RemoveNodeOverlayIconAdapter(String actionName, ShrimpNode node) {
    	this(actionName, (ShrimpTool)null);
    	this.selectedNode = node;
    }

    public boolean canStart() {
    	return super.canStart() || (selectedNode != null);
    }

    public void startAction() {
    	ShrimpNode node = selectedNode;
    	if (node == null) {
			node = getSelectedNode();
    	}
    	if ((node != null) && (node.getOverlayIconProvider() != null)) {
    		new ChangeNodeOverlayIconAdapter(tool).removeOverlayIcon(node);
		}
    }

}
