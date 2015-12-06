/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.util.BrowserLauncher;
import ca.uvic.csr.shrimp.util.GradientPainter;
import ca.uvic.csr.shrimp.util.GradientPanel;

/**
 * This pane handles the layouts of the internal windows for the
 * shrimp frame.
 */
public class ScrollDesktopPane extends JDesktopPane {

    private DeskLayoutManager deskLayoutManager = null;
    private final GradientPainter painter;

	private Icon brandingIcon;
	private JLabel imageLabel;
	private int imageWidth;
	private int imageHeight;

    public ScrollDesktopPane(String version, Image brandingImage, Icon brandingIcon) {
		deskLayoutManager = new DeskLayoutManager(this);
		this.brandingIcon = brandingIcon;
		this.painter = new GradientPainter(GradientPanel.BG_START, GradientPanel.BG_END, GradientPainter.TOP_TO_BOTTOM);

		// Get the icon image
		Icon icon = new ImageIcon(brandingImage);
		imageLabel = new JLabel(version, icon, SwingConstants.CENTER);
		imageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		imageLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		imageLabel.setForeground(new Color(204, 255, 255));
		imageLabel.setFont(imageLabel.getFont().deriveFont(Font.BOLD).deriveFont(14f));
		imageWidth = icon.getIconWidth();
		imageHeight = icon.getIconHeight();
		imageLabel.setSize(imageWidth + 10, imageHeight + 50);

		// @tag Shrimp.logo : add a click listener on the shrimp logo which directs to what's new website
		imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		imageLabel.setToolTipText(version + " (click to visit homepage)");
		imageLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				try {
					BrowserLauncher.openURL(ShrimpConstants.SHRIMP_WHATSNEW_WEBSITE);
				} catch (Throwable t) {
					imageLabel.setCursor(Cursor.getDefaultCursor());
					imageLabel.removeMouseListener(this);
				}
			}
		});

		this.add(imageLabel, -10); // -1 sends it behind the DEFAULT_LAYER
	}

	public DesktopInternalFrame addFrame(String title) {
		DesktopInternalFrame frame = new DesktopInternalFrame(this, title, brandingIcon);

		int parentWidth = this.getWidth();
		int parentHeight = this.getHeight();
		int width = (int)(parentWidth * 0.85);
		int height = (int)(parentHeight * 0.85);
		int xpos;
		int ypos;
		if(getSelectedFrame() != null) {
			try {
				getSelectedFrame().setMaximum(false);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}

			xpos = getSelectedFrame().getX() + 30;
			ypos = getSelectedFrame().getY() + 30;
			if((xpos > (parentWidth - 40)) || (ypos > (parentHeight-40))) {
				xpos = (parentWidth - width) / 2;
				ypos = (parentHeight - height) / 2;
			}
		} else {
			// center the frame
			xpos = (parentWidth - width) / 2;
			ypos = (parentHeight - height) / 2;
		}

		frame.setBounds(xpos, ypos, width, height);
		this.add(frame, DEFAULT_LAYER);
		return frame;
	}

    /**
     * Gets the DeskLayoutManager of this ScrollDesktopPane.
     */
    public DeskLayoutManager getDeskLayoutManager() {
 		if(deskLayoutManager == null) {
		    deskLayoutManager = new DeskLayoutManager(this);
		}
		return deskLayoutManager;
    }

    public void maximizeFirstFrame() {
    	deskLayoutManager.maximizeFirstFrame();
    }
    
    public void maximizeLastFrame() {
    	deskLayoutManager.maximizeLastFrame();
    }
    
    public void maximizeAllFrames() {
    	deskLayoutManager.maximizeAllFrames();
    }
    
    public void minimizeAllFrames() {
    	deskLayoutManager.minimizeAllFrames();
    }

    /**
     * Tiles all the internal frames of this ScrollDesktopPane.
     */
    public void tileAllFrames() {
		deskLayoutManager.tile();
    }

    /**
     * Cascades all the internal frames of this ScrollDesktopPane.
     */
    public void cascadeAllFrames() {
		deskLayoutManager.cascade();
    }

    /**
     * Standardizes all the internal frames of this ScrollDesktopPane
     * to the specified size.
     */
    public void standardizeAllFrames(Dimension size) {
		deskLayoutManager.standardizeAll(size);
    }

    /**
     * Closes all the internal frames of this ScrollDesktopPane.
     */
    public void closeAllFrames() {
		deskLayoutManager.closeAll();
    }

    /**
     * Deiconizes all the internal frames of this ScrollDesktopPane.
     */
    public void deiconizeAllFrames() {
		deskLayoutManager.deiconizeAll();
    }

    /**
     * Iconizes all the internal frames of this ScrollDesktopPane.
     */
    public void iconizeAllFrames() {
		deskLayoutManager.iconizeAll();
    }

	/**
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		painter.fillRect(g, 0, 0, getWidth(), getHeight());

    	int x = (this.getWidth() - imageWidth) / 2;
		int y = (this.getHeight() - imageHeight) / 2;
		if ((x != imageLabel.getX()) && (y != imageLabel.getY())) {
			imageLabel.setLocation(x, y);
		}
	}

}
