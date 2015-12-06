/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.gui.ArcFilterPalette;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the ArcFilter tool.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class OpenArcFilterAdapter extends AbstractOpenApplicationToolAdapter {

	public static final int FILTER_PALETTE_WIDTH = OpenNodeFilterAdapter.FILTER_PALETTE_WIDTH;
	public static final int FILTER_PALETTE_HEIGHT = 400;

	private static final Rectangle BOUNDS = new Rectangle(DOCK_LEFT_OUTSIDE, DOCK_TOP_INSIDE,
														FILTER_PALETTE_WIDTH, FILTER_PALETTE_HEIGHT);

	private String linkedToolName;

	public OpenArcFilterAdapter(String linkedToolName) {
		this(ShrimpApplication.ARC_FILTER, linkedToolName);
	}

	public OpenArcFilterAdapter(String actionName, String linkedToolName) {
		this(actionName, ResourceHandler.getIcon("icon_arc_filter.gif"), linkedToolName);
	}

	public OpenArcFilterAdapter(String actionName, Icon icon, String linkedToolName) {
		super(actionName, icon, BOUNDS);
		this.linkedToolName = linkedToolName;
	}

	protected boolean getDefaultVisibility() {
		return true;
	}

	/** Display below the node filter palette. */
	protected int getVerticalDockOffset() {
		return OpenNodeFilterAdapter.FILTER_PALETTE_HEIGHT;
	}

	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool()  {
		tool = new ArcFilterPalette(getProject(), linkedToolName);
	}

}
