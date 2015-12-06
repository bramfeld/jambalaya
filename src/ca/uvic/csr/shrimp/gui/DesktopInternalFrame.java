/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;

public class DesktopInternalFrame extends JInternalFrame {

	private ScrollDesktopPane deskTopPane;
	private boolean disposed = false;

	public DesktopInternalFrame(final ScrollDesktopPane deskTopPane, String title, Icon icon) {
		super(title, true, true, true, true);
		this.deskTopPane = deskTopPane;

		if (icon != null) {
			setFrameIcon(icon);

			try {
				// HACK add a double click close listener to the icon
				// TODO system context menu still shows...
				InternalFrameUI ui = getUI();
				if (ui instanceof BasicInternalFrameUI) {
					BasicInternalFrameUI bfui = (BasicInternalFrameUI) ui;
					JComponent np = bfui.getNorthPane();
					if (np.getComponentCount() > 0) {
						final int width = icon.getIconWidth() + 4;
						final int height = icon.getIconHeight() + 4;
						np.getComponent(0).addMouseListener(new MouseAdapter() {

							public void mouseClicked(MouseEvent e) {
								if ((e.getClickCount() >= 2) && (e.getPoint().x <= width) && (e.getPoint().y <= height)) {
									DesktopInternalFrame.this.doDefaultCloseAction();
								}
							}
						});
					}
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @see javax.swing.JInternalFrame#dispose()
	 */
	public void dispose() {
		if (disposed) {
			return;
		}

		JInternalFrame[] frames = this.deskTopPane.getAllFrames();

		// make sure to remove this as the selected frame
		if (deskTopPane.getSelectedFrame() == this) {
			boolean foundAnotherFrame = false;
			// select another frame if possible
			for (int i = 0; i < frames.length; i++) {
				if (frames[i] != this) {
					deskTopPane.setSelectedFrame(frames[i]);
					foundAnotherFrame = true;
					break;
				}
			}
			// no other frame, so clear selected frame
			if (!foundAnotherFrame) {
				deskTopPane.setSelectedFrame(null);
			}
		}

		disposed = true;
		super.dispose();

		if (frames.length > 0) {
			try {
				// hack around.. cause toFront() doesn't work
				frames[0].setMaximum(true);
				frames[0].setMaximum(false);
			} catch (Exception e) {
			}
		}
	}

	public void select() {
		deskTopPane.getDesktopManager().activateFrame(this);
		deskTopPane.getDesktopManager().deiconifyFrame(this);
	}

}