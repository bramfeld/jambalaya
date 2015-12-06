/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.FilterBean.FilterTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Creates and displays the {@link FilterTool} inside a dialog.
 * 
 * @see FilterTool
 * @author Chris Callendar
 */
public class OpenFilterToolAdapter extends AbstractOpenApplicationToolAdapter {
	
	private static final Rectangle BOUNDS = new Rectangle(DOCK_RIGHT_INSIDE, DOCK_TOP_INSIDE, 350, 400);

	public OpenFilterToolAdapter() {
		super(ShrimpConstants.TOOL_FILTERS, ResourceHandler.getIcon("icon_filter.gif"), BOUNDS);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new FilterTool(getProject());
	}
	
}
