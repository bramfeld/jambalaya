/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter;

import java.awt.event.ActionEvent;

import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.FilmStrip.FilmStrip;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This adapter loads the given film strip from a file
 *
 * @author Casey Best
 * @date Oct 24, 2001
 */
public class LoadFilmStripAdapter extends DefaultShrimpAction {

	public static final String ACTION_NAME = "Load Filmstrip...";

	private FilmStrip filmStrip;

	public LoadFilmStripAdapter(FilmStrip filmStrip) {
		super (ACTION_NAME, ResourceHandler.getIcon("icon_open.gif"));
		this.filmStrip = filmStrip;
	}

	public void actionPerformed(ActionEvent e) {
		// if the user chooses "cancel" then we don't want to continue
		if (filmStrip.saveIfNeeded()) {
			filmStrip.loadFilmStrip();
		}
	}

}