/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.GradientPainter;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;


/**
 *
 *
 * @author Chris Callendar
 * @date 31-Oct-06
 */
public class SnapShotsPanel extends GradientPanel {

	private static final Color END_COLOR = new Color(54, 130, 191);
	private static final Color START_COLOR = new Color(0, 4, 24);
	private static final Color BORDER_COLOR = Color.black;
	private static final Color WHITE_BOX_COLOR = new Color(224, 224, 224);
	private static final Color BG = new Color(57, 131, 193);
	private static final Color LABEL_COLOR = new Color(192, 255, 255);
	private static final int BORDER_HEIGHT = 24;
	private static final int WHITE_BOX_SIZE = 10;
	private static final int LABEL_HEIGHT = 16;


	// these constants decide how many frames and how far apart to display them
	public static final int DIVIDER_WIDTH = 5;
	private static final int ARC = DIVIDER_WIDTH * 2;
	private static final int MIN_NUM_SNAP_SHOTS = 6;

	public static final int IMAGE_WIDTH = 100;
	public static final int IMAGE_HEIGHT = IMAGE_WIDTH;

	// Filmstrip overall height and width
	private static final int MIN_FILM_STRIP_WIDTH = ((IMAGE_WIDTH + (2 * DIVIDER_WIDTH)) * MIN_NUM_SNAP_SHOTS);
	// Fixed height - add a little extra for the horizontal scrollbar
	private static final int FILM_STRIP_HEIGHT = IMAGE_HEIGHT + (2 * BORDER_HEIGHT) + (4 * DIVIDER_WIDTH) + LABEL_HEIGHT;

	private JPanel mainPanel;

	private Vector snapShots;
	private Vector/*<SnapShotImagePanel>*/ imagePanels;
	private boolean hasChanged;
	private boolean locked = false;
	private final FilmStrip filmStrip;
	private Frame parent;
	private GradientPainter painter;

	public SnapShotsPanel(FilmStrip filmStrip, Frame parent) {
		super();
		this.filmStrip = filmStrip;
		this.snapShots = new Vector(MIN_NUM_SNAP_SHOTS);
		this.imagePanels = new Vector/*<SnapShotImagePanel>*/(MIN_NUM_SNAP_SHOTS);
		this.hasChanged = false;
		this.parent = parent;
		this.painter = new GradientPainter(START_COLOR, END_COLOR);
		this.painter.setAntiAliasing(true);
		this.painter.setBorderPainted(false);
		
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());

		mainPanel = new JPanel(null) {
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, width, FILM_STRIP_HEIGHT);
			}
		};
		mainPanel.setBorder(new FilmStripBorder());
		mainPanel.setPreferredSize(new Dimension(MIN_FILM_STRIP_WIDTH, FILM_STRIP_HEIGHT));
		mainPanel.setBackground(BG);
		
		JScrollPane scroll = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setOpaque(false);
		scroll.setBorder(null);
		scroll.getViewport().setOpaque(false);
		add(scroll, BorderLayout.NORTH);
		
		updateImagePanels(false);
		
		revalidate();
	}
	
	public boolean hasChanged() {
		return hasChanged;
	}

	public void setChanged(boolean b) {
		hasChanged = false;
	}

	public Vector getSnapShots() {
		return new Vector(snapShots);
	}

	public int getSnapShotCount() {
		return snapShots.size();
	}

	private void updateImagePanels(boolean fireChange) {
		if (!locked) {
			for (int i = 0; i < snapShots.size(); i++) {
				SnapShot snapShot = null;
				if (i < snapShots.size()) {
					snapShot = (SnapShot) snapShots.get(i);
					SnapShotImagePanel panel;
					if (i < imagePanels.size()) {
						panel = (SnapShotImagePanel) imagePanels.get(i);
					} else {
						panel = new SnapShotImagePanel();
						imagePanels.add(panel);
						mainPanel.add(panel);
					}
					panel.setSnapShot(snapShot);
					int x = DIVIDER_WIDTH + (i * panel.getWidth()) + (i * (2*DIVIDER_WIDTH));
					int y = DIVIDER_WIDTH + BORDER_HEIGHT;
					panel.setLocation(x, y);
				}
			}
			while (imagePanels.size() > snapShots.size()) {
				int pos = imagePanels.size() - 1;
				imagePanels.remove(pos);
				mainPanel.remove(pos);
			}
			mainPanel.revalidate();
			mainPanel.repaint();
			if (fireChange && (filmStrip != null)) {
				filmStrip.snapShotsChanged();
			}
		}
	}
	
	/**
	 * Adds the given snapshot at the given position.
	 * @param snapShot the snapshot to add
	 * @param pos the position to add the snapshot at
	 */
	private void addSnapShot(SnapShot snapShot, int pos) {
		locked = true;
		snapShots.insertElementAt(snapShot, pos);
		locked = false;
		updateImagePanels(true);
		hasChanged = true;
	}

	/**
	 * Adds the given snapshot at the beginning of the filmstrip
	 * @param snapShot the snapshot to add
	 */
	public void addSnapShot(SnapShot snapShot) {
		addSnapShot(snapShot, 0);
	}

	public void addSnapShots(Vector snapShotsToAdd) {
		locked = true;
		// add all the snapshots to the front in reverse order
		for (int i = snapShotsToAdd.size() - 1; i >= 0; i--) {
			SnapShot snapShot = (SnapShot) snapShotsToAdd.get(i);
			snapShots.insertElementAt(snapShot, 0);
		}
		locked = false;
		// update the panels
		updateImagePanels(true);
		hasChanged = true;
	}

	/**
	 * Removes all the snapshots in the filmstrip
	 */
	public void removeAllSnapShots() {
		locked = true;
		while (snapShots.size() > 0) {
			removeSnapShot(0);
		}
		locked = false;
		updateImagePanels(true);
	}


	/**
	 * Removes the given snapshot.
	 */
	private void removeSnapShot(SnapShot snapShot) {
		if (snapShot != null) {
			int index = snapShots.indexOf(snapShot);
			if (index != -1) {
				removeSnapShot(index);
			} else {
				System.err.println("Warning - no snap shot found to remove");
			}
		}
	}

	/**
	 * Removes the snapshot at the given position
	 * @param pos the position of the snapshot to remove
	 */
	private void removeSnapShot(int pos) {
		SnapShot snapShot = (SnapShot) snapShots.elementAt(pos);
		snapShot.cleanUp();
		snapShots.removeElementAt(pos);
		hasChanged = true; // update the flag
		updateImagePanels(true);
	}

	/**
	 * Creates the filmstrip ticker-tape border.
	 *
	 * @author Chris Callendar
	 * @date 1-Nov-06
	 */
	class FilmStripBorder extends EmptyBorder {

		private final int WHITE_BOX_SIZE_HALF = (WHITE_BOX_SIZE / 2);
		private final int MID = (BORDER_HEIGHT / 2) - WHITE_BOX_SIZE_HALF;

		public FilmStripBorder() {
			super(BORDER_HEIGHT, 0, BORDER_HEIGHT, 0);
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			int startX = x + DIVIDER_WIDTH;
			int startY = y + BORDER_HEIGHT + DIVIDER_WIDTH;
			while (startX < (x + width)) {
				paintBackground(g, startX, startY);
				startX += DIVIDER_WIDTH + IMAGE_WIDTH + DIVIDER_WIDTH;
			}
			
			// draw a 1 pixel black border around the whole filmstrip
			g.setColor(BORDER_COLOR);
			g.drawRect(x, y, width - 1, height - 1);

			// paint the top strip and white boxes
			g.fillRect(x, y, width, BORDER_HEIGHT);
			paintWhiteBoxes(g, x + WHITE_BOX_SIZE_HALF, y + MID, WHITE_BOX_SIZE, x + width);

			// paint the bottom strip and white boxes
			g.setColor(BORDER_COLOR);
			g.fillRect(x, y + height - BORDER_HEIGHT, width, BORDER_HEIGHT);
			paintWhiteBoxes(g, x + WHITE_BOX_SIZE_HALF, y + height - BORDER_HEIGHT + MID, WHITE_BOX_SIZE, x + width);
			
		}

		/**
		 * Paints the background gradient.
		 */
		private void paintBackground(Graphics g, int startX, int startY) {
			painter.paint(g, new RoundRectangle2D.Float(startX, startY, IMAGE_WIDTH + DIVIDER_WIDTH, IMAGE_HEIGHT + DIVIDER_WIDTH, ARC, ARC));
		}
		
		private void paintWhiteBoxes(Graphics g, int startX, int y, int w, int maxX) {
			g.setColor(WHITE_BOX_COLOR);
			int x = startX;
			while (x < maxX) {
				g.fillRect(x, y, w, w);
				x += (2 * w);
			}
		}

	}

	/**
	 * @author Chris Callendar
	 * @date 1-Nov-06
	 */
	class SnapShotImagePanel extends TransparentPanel implements ActionListener {

		private SnapShot snapShot;
		private Image image;
		private JButton btnRun;
		private JButton btnEdit;
		private JButton btnClose;
		private JLabel lblComment;

		public SnapShotImagePanel() {
			initialize();
			updateVisibility();
		}

		private void initialize() {
			setLayout(new BorderLayout(0, 0));
			Rectangle r = new Rectangle(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT + LABEL_HEIGHT + (2*DIVIDER_WIDTH));
			setBounds(r);
			add(createTopPanel(), BorderLayout.NORTH);
			add(Box.createVerticalGlue(), BorderLayout.CENTER);
			add(createBottomLabel(), BorderLayout.SOUTH);
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if ((e.getClickCount() >= 2) && (snapShot != null)) {
						btnRun.doClick();
					}
				}
			});
		}

		private JLabel createBottomLabel() {
			lblComment = new JLabel("", JLabel.CENTER);
			lblComment.setForeground(LABEL_COLOR);
			Font font = lblComment.getFont();
			lblComment.setFont(font.deriveFont(font.getSize2D() - 1f));
			lblComment.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createMatteBorder(1, 1, 0, 0, Color.gray),
					BorderFactory.createMatteBorder(0, 0, 1, 1, Color.lightGray)));
			Dimension dim = new Dimension(IMAGE_WIDTH, 16);
			lblComment.setMinimumSize(dim);
			lblComment.setPreferredSize(dim);
			lblComment.setMaximumSize(dim);
			return lblComment;
		}

		private JPanel createTopPanel() {
			JPanel pnl = new TransparentPanel(new FlowLayout(FlowLayout.RIGHT, 0, DIVIDER_WIDTH));	// right align buttons
			// add the edit/close button only if an image is present
			btnRun = createButton("icon_forward_small.gif", "Load this snapshot", "Run");
			btnEdit = createButton("icon_edit.gif", "Annotate this snapshot", "Edit");
			btnClose = createButton("icon_close_gray.gif", "icon_close_red.gif", "Delete this snapshot", "Close");

			pnl.add(btnRun);
			pnl.add(Box.createHorizontalStrut(1));
			pnl.add(btnEdit);
			pnl.add(Box.createHorizontalStrut(1));
			pnl.add(btnClose);
			pnl.add(Box.createHorizontalStrut(DIVIDER_WIDTH));	// to align horizontally with the image
			return pnl;
		}

		private JButton createButton(String iconPath, String rolloverIconPath, String tooltip, String actionCmd) {
			JButton btn = createButton(iconPath, tooltip, actionCmd);
			btn.setRolloverEnabled(true);
			btn.setRolloverIcon(ResourceHandler.getIcon(rolloverIconPath));
			return btn;
		}

		private JButton createButton(String iconPath, String tooltip, String actionCmd) {
			JButton btn = new JButton(ResourceHandler.getIcon(iconPath));
			btn.setToolTipText(tooltip);
			btn.setActionCommand(actionCmd);
			btn.addActionListener(this);
			Dimension size = new Dimension(18, 18);
			btn.setPreferredSize(size);
			btn.setMaximumSize(size);
			btn.setMinimumSize(size);
			btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return btn;
		}

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if ("Run".equals(cmd)) {
				filmStrip.applyLayout(snapShot);
			} else if ("Edit".equals(cmd) && (snapShot != null)) {
				boolean changed = snapShot.promptUserToChangeComment(parent);
				if (changed) {
					hasChanged = changed;
					updateVisibility();
				}
			} else if ("Close".equals(cmd)) {
				SnapShot old = snapShot;
				setSnapShot(null);	// make sure the image is removed and buttons hidden
				removeSnapShot(old);
			}
		}

		public void setSnapShot(SnapShot snapShot) {
			this.snapShot = snapShot;
			image = (snapShot != null ? snapShot.getPreviewShot() : null);
			updateVisibility();
		}

		private void updateVisibility() {
			String txt = "";
			txt = (snapShot != null ? snapShot.getComment() : "");
			txt = (txt == null ? "" : txt);
			lblComment.setVisible(txt.length() > 0);
			String t = txt.replace('\n', ' ');
			lblComment.setText(t);
			lblComment.setToolTipText(t);
			boolean vis = (image != null);
			btnRun.setVisible(vis);
			btnEdit.setVisible(vis);
			btnClose.setVisible(vis);
			setToolTipText(txt);
			repaint();
		}

		/**
		 * Draws the image if one exists.
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int x = 0;
			int y = 0;
			// paint image after the background paint and center in the round rectangle
			if (image != null) {
				x += DIVIDER_WIDTH;
				y += DIVIDER_WIDTH;
				g.setColor(Color.white);
				g.fillRect(x, y, IMAGE_WIDTH, IMAGE_HEIGHT);
				g.drawImage(image, x, y, this);
			}
		}


	}

}
