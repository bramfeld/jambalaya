/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.gui.FilmStrip.FilmStrip;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * @author Rob Lintern
 */
public class OpenFilmStripAdapter extends AbstractOpenApplicationToolAdapter {

	private static final Rectangle BOUNDS = new Rectangle(DOCK_CENTER, DOCK_BOTTOM_INSIDE, 728, 243);

	public OpenFilmStripAdapter() {
		super(ShrimpApplication.FILMSTRIP, ResourceHandler.getIcon("icon_filmstrip.gif"), BOUNDS);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new FilmStrip(getProject());
	}

	protected void initTool() {
		super.initTool();
		
		FilmStrip filmstrip = (FilmStrip) tool;
		filmstrip.loadTemporaryFilmstrip();
	}
	
}
