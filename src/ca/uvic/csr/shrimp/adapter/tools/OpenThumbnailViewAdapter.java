/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ThumbnailView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the ThumbnailView tool.
 * 
 * @author Nasir Rather, Chris Callendar
 */
public class OpenThumbnailViewAdapter extends AbstractOpenApplicationToolAdapter {
	
	private static final Rectangle BOUNDS = new Rectangle(DOCK_RIGHT_INSIDE, DOCK_TOP_INSIDE, 300, 300);

	public OpenThumbnailViewAdapter() {
		super(ShrimpProject.THUMBNAIL_VIEW, ResourceHandler.getIcon("icon_thumbnail_view.gif"), BOUNDS);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new ThumbnailView(getProject());
	}
	
}
