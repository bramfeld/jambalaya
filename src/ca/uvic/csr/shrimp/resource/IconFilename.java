/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.resource;

import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 * Wraps an icon and its filename.
 *
 * @author Chris Callendar
 * @date 3-Aug-06
 */
public class IconFilename implements Comparable {

	private String filename;
	private Icon icon;

	public IconFilename(String filename, Icon icon) {
		this.filename = (filename != null ? filename : "");
		this.icon = icon;
	}

	public int compareTo(Object obj) {
		if (obj instanceof IconFilename) {
			IconFilename iconFilename = (IconFilename) obj;
			return getFilename().compareToIgnoreCase(iconFilename.getFilename());
		}
		return 0;
	}

	public boolean equals(Object obj) {
		if (obj instanceof IconFilename) {
			IconFilename iconFilename = (IconFilename) obj;
			return getFilename().equals(iconFilename.getFilename());
		}
		return false;
	}

	public String toString() {
		return "IconFilename: " + getFilename();
	}

	/**
	 * Returns the filename (not null)
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the icon
	 */
	public Icon getIcon() {
		return icon;
	}

	/**
	 * Sets the icon using {@link ResourceHandler} and returns it.
	 * Assumes that the icon exists in the resources/icons directory.
	 * @return Icon
	 */
	public Icon reloadResourceIcon() {
		icon = null;
		if (filename.length() > 0) {
			try {
				icon = ResourceHandler.getIcon(filename);
			} catch (Exception e) {
			}
		}
		return icon;
	}

	/**
	 * Reloads the icon, assumes that the filename is an absolute or relative path
	 * (relative to the java working directory).
	 * @return the loaded icon or null
	 */
	public Icon reloadIcon() {
		icon = null;
		if (filename.length() > 0) {
			try {
				icon = new ImageIcon(filename);
			} catch (RuntimeException e) {
			}
		}
		return icon;
	}

}
