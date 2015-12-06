/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.SearchBean.SearchTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * @author Rob Lintern
 */
public class OpenSearchToolAdapter extends AbstractOpenApplicationToolAdapter {
	
	private static final Rectangle BOUNDS = new Rectangle(DOCK_CENTER, DOCK_CENTER, 600, 350);

	public OpenSearchToolAdapter() {
		super(ShrimpConstants.TOOL_SEARCH, ResourceHandler.getIcon("icon_search.gif"), BOUNDS);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new SearchTool(getProject());
	}
	
}
