/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.FilmStrip.FilmStrip;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;

/**
 * This adapter saves the given film strip to a file
 *
 * @author Casey Best
 * @date Oct 24, 2001
 */
public class SaveFilmStripAdapter extends DefaultShrimpAction {

	public static final String ACTION_NAME = "Save Filmstrip...";

	private FilmStrip filmStrip;
	private Component parent;

	public SaveFilmStripAdapter (FilmStrip filmStrip, Component parent) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_save.gif"));
		this.filmStrip = filmStrip;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		save();
	}

	public void save() {
		//Create and display a FileChooser
		//Get the user selected file
		JFileChooser chooser = new JFileChooser();
		ShrimpFileFilter filter = new ShrimpFileFilter();
		filter.addExtension("filmstrip");
		filter.setDescription("Shrimp Filmstrip");
		chooser.setFileFilter(filter);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Save Filmstrip");
		chooser.setApproveButtonText ("Save");
		Properties props = ApplicationAccessor.getProperties();
		String filmStripDir = props.getProperty(FilmStrip.FILMSTIP_DIRECTORY_KEY);
		if ((filmStripDir != null) && (filmStripDir.length() > 0)) {
			chooser.setCurrentDirectory(new File(filmStripDir));
		}

		int state = chooser.showSaveDialog(parent);
		if (state == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				// prompt to overwrite
				state = JOptionPane.showConfirmDialog(parent, "Overwrite existing file?", "Overwrite?",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (state == JOptionPane.NO_OPTION) {
					return;
				}
			}
			// make sure it has the right extension
			String fileName = file.getPath();
			if (!fileName.endsWith (".filmstrip")) {
				fileName += ".filmstrip";
			}
			props.setProperty(FilmStrip.FILMSTIP_DIRECTORY_KEY, file.getParent());

			filmStrip.save(fileName);
		}
	}
}