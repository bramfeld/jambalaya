/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.gui.NodeFilterPalette;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the NodeFilter tool.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class OpenNodeFilterAdapter extends AbstractOpenApplicationToolAdapter {

	public static final int FILTER_PALETTE_WIDTH = 300;
	public static final int FILTER_PALETTE_HEIGHT = 350;
	private static final Rectangle BOUNDS = new Rectangle(DOCK_LEFT_OUTSIDE, DOCK_TOP_INSIDE,
														FILTER_PALETTE_WIDTH, FILTER_PALETTE_HEIGHT);

	private String linkedToolName;

	public OpenNodeFilterAdapter(String linkedToolName) {
		this(ShrimpApplication.NODE_FILTER, linkedToolName);
	}

	public OpenNodeFilterAdapter(String actionName, String linkedToolName) {
		this(actionName, ResourceHandler.getIcon("icon_node_filter.gif"), linkedToolName);
	}

	public OpenNodeFilterAdapter(String actionName, Icon icon, String linkedToolName) {
		super(actionName, icon, BOUNDS);
		this.linkedToolName = linkedToolName;
	}

	protected boolean getDefaultVisibility() {
		return true;
	}

	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new NodeFilterPalette(getProject(), linkedToolName);
	}

}
