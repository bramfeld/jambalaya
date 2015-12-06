/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * File filter for images: JPG, BMP, GIF, and PNG.
 *
 * @author Chris Callendar
 * @date 12-Oct-07
 */
public class ImageFileFilter extends FileFilter {

	public boolean accept(File f) {
		if (f != null) {
			String name = f.getName().toLowerCase();
			boolean isImg = name.endsWith(".jpg") || name.endsWith(".jpeg") ||
				name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".png");
			return isImg || f.isDirectory();
		}
		return false;
	}

	public String getDescription() {
		return "Image Files (*.jpg, *.gif, *.png, *.bmp)";
	}

}
