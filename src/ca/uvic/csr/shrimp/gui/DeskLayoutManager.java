/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * The DeskLayoutManager class provides some basic utilities such as tiling,
 * cascading the internal frames in a desktop pane.
 * 
 * @version  1.0, 07/05/1999
 * @author   Jingwei Wu
 */
public class DeskLayoutManager {

	private JDesktopPane desktop = null;

	public static final int DEFAULT_CASCADE_WIDTH = 27;
	private int cascadeWidth = DEFAULT_CASCADE_WIDTH;
	private int cascadeHeight = DEFAULT_CASCADE_WIDTH;

	/////////////////////////////////
	// Construct DeskLayoutManager //
	/////////////////////////////////

	/**
	 * DeskLayoutManager's Constructor.
	 */
	public DeskLayoutManager() {
		super();
	}

	/**
	 * DeskLayoutManager's Constructor.
	 */
	public DeskLayoutManager(JDesktopPane desktop) {
		super();
		this.desktop = desktop;
	}

	//////////////////////////////
	// Basic pairs: set and get //
	//////////////////////////////

	/**
	 * Sets a new JDesktopPane instance. The specified destop can
	 * be null reference.
	 */
	public void setDesktopPane(JDesktopPane desktop) {
		this.desktop = desktop;
	}

	/**
	 * Gets the JDesktopPane instance from this DeskLayoutManager.
	 */
	public JDesktopPane gerDesktoppane() {
		return desktop;
	}

	/**
	 * Sets the cascade width.
	 */
	public void setCascadeWidth(int width) {
		cascadeWidth = width;
	}

	/**
	 * Gets the cascade width.
	 */
	public int getCascadeWidth() {
		return cascadeWidth;
	}

	/**
	 * Sets the cascade height.
	 */
	public void setCascadeHeight(int height) {
		cascadeHeight = height;
	}

	/**
	 * Gets the cascade height.
	 */
	public int getCascadeHeight() {
		return cascadeHeight;
	}

	/**
	 * This method cascades all the internal frames existing in
	 * the desktop pane controlled by this DeskLayoutManager.
	 */
	public void cascade() {
		int avgWidth;
		int avgHeight;
		Dimension size;
		Component[] jifs = null;

		if (desktop == null) {
			return;
		}

		//int layer;
		//layer = JLayeredPane.DEFAULT_LAYER.intValue();
		//jifs = desktop.getComponentsInLayer(layer);
		jifs = desktop.getAllFrames();
		if (jifs.length == 0) {
			return;
		}

		avgWidth = 0;
		avgHeight = 0;
		for (int i = 0; i < jifs.length; i++) {
			size = jifs[i].getSize();
			avgWidth += size.width;
			avgHeight += size.height;
		}

		avgWidth = avgWidth / jifs.length;
		avgHeight = avgHeight / jifs.length;

		for (int i = 0; i < jifs.length; i++) {
			jifs[i].setSize(new Dimension(avgWidth, avgHeight));
			jifs[i].setLocation((jifs.length - 1 - i) * cascadeWidth, (jifs.length - 1 - i) * cascadeHeight);
		}
	}

	/**
	 * This method tiles all the internal frames existing in
	 * the desktop pane controlled by this DeskLayoutManager.
	 */
	public void tile() {
		if (desktop == null) {
			return;
		}
		
		tile(desktop.getBounds());
	}

	/**
	 * This method tiles all the internal frames existing in
	 * the desktop pane controlled by this DeskLayoutManager.
	 * @param boundingArea java.awt.Rectangle
	 */
	public void tile(Rectangle boundingArea) {
		int sqrt, rows, cols;
		int avgWidth, avgHeight;
		Rectangle bounds = null;
		Component[] jifs = null;

		if (desktop == null) {
			return;
		}

		//int layer;
		//layer = JLayeredPane.DEFAULT_LAYER.intValue();
		//jifs = desktop.getComponentsInLayer(layer);
		jifs = desktop.getAllFrames();
		if (jifs.length == 0) {
			return;
		}

		// Calculate the number of rows and columns.
		sqrt = (int) (Math.sqrt(jifs.length));
		if (sqrt * sqrt == jifs.length) {
			rows = sqrt;
		} else {
			rows = sqrt + 1;
		}
		if ((sqrt + 1) * sqrt < jifs.length) {
			cols = sqrt + 1;
		} else {
			cols = sqrt;
		}

		// Get the bouding area.
		if (boundingArea != null) {
			bounds = boundingArea;
		} else {
			bounds = desktop.getBounds();
		}

		// Calculate average_width and average_height.
		if (cols != 0) {
			avgWidth = bounds.width / cols;
		} else {
			avgWidth = bounds.width;
		}
		if (rows != 0) {
			avgHeight = bounds.height / rows;
		} else {
			avgHeight = bounds.height;
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if ((i * cols + j) < jifs.length) {
					jifs[i * cols + j].setSize(avgWidth, avgHeight);
					jifs[i * cols + j].setLocation(bounds.x + j * avgWidth, bounds.y + i * avgHeight);
				} // End if
			} // End for
		} // End for
	}

	/**
	 * This method standardize all the internal frames to the
	 * specified size.
	 * @param size java.awt.Dimension
	 */
	public void standardizeAll(Dimension size) {
		JInternalFrame[] jifs = null;

		// No need to standardize all frames.
		if (size == null) {
			return;
		}
		if (desktop == null) {
			return;
		}

		jifs = desktop.getAllFrames();
		if (jifs.length == 0) {
			return;
		}

		for (int i = 0; i < jifs.length; i++) {
			jifs[i].setSize(size);
		}
	}

	public void iconizeAll() {
		JInternalFrame[] jifs = null;

		if (desktop == null) {
			return;
		}

		jifs = desktop.getAllFrames();
		if (jifs.length == 0) {
			return;
		}

		for (int i = 0; i < jifs.length; i++) {
			if (!jifs[i].isIcon()) {
				try {
					jifs[i].setIcon(true);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void deiconizeAll() {
		JInternalFrame[] jifs = null;

		if (desktop == null) {
			return;
		}

		jifs = desktop.getAllFrames();
		if (jifs.length == 0) {
			return;
		}

		for (int i = 0; i < jifs.length; i++) {
			if (jifs[i].isIcon()) {
				try {
					jifs[i].setIcon(false);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method close all the internal frames existing in
	 * the desktop pane controlled by this DeskLayoutManager.
	 */
	public void closeAll() {
		JInternalFrame[] jifs = null;

		if (desktop == null) {
			return;
		}

		jifs = desktop.getAllFrames();
		for (int i = 0; i < jifs.length; i++) {
			try {
				jifs[i].setClosed(true);
			} catch (PropertyVetoException pve) {
				pve.printStackTrace();
			}
		}
		desktop.repaint();
	}
	
	/**
	 * Maximizes the first frame.
	 */
	public void maximizeFirstFrame() {
		if (desktop == null) {
			return;
		}
		JInternalFrame[] jifs = desktop.getAllFrames();
		if (jifs.length >= 1) {
			try {
				jifs[0].setMaximum(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Maximizes the last frame.
	 */
	public void maximizeLastFrame() {
		if (desktop == null) {
			return;
		}
		JInternalFrame[] jifs = desktop.getAllFrames();
		if (jifs.length >= 1) {
			try {
				jifs[jifs.length - 1].setMaximum(true);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void maximizeAllFrames() {
		if (desktop == null) {
			return;
		}
		JInternalFrame[] jifs = desktop.getAllFrames();
		if (jifs.length >= 1) {
			for (int i = 0; i < jifs.length; i++) {
				try {
					jifs[i].setMaximum(true);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void minimizeAllFrames() {
		if (desktop == null) {
			return;
		}
		JInternalFrame[] jifs = desktop.getAllFrames();
		if (jifs.length >= 1) {
			for (int i = 0; i < jifs.length; i++) {
				try {
					jifs[i].setMaximum(false);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
} //class DeskLayoutManager
