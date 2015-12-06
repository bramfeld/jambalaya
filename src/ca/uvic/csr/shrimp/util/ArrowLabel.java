/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


/**
 * Extends {@link JLabel} to paint an up, left, down, or right arrow on the left or right side of the label.
 *
 * @author Chris Callendar
 * @date 3-Nov-06
 */
public class ArrowLabel extends JLabel {

	public static final int UP_ARROW = 0;
	public static final int LEFT_ARROW = 1;
	public static final int DOWN_ARROW = 2;
	public static final int RIGHT_ARROW = 3;

	private static final Color CIRCLE_OUTLINE_COLOR = new Color(139, 148, 172);
	private static final Color CIRCLE_BG_COLOR = Color.white;
	private static final Color DEFAULT_COLOR = new Color(33, 89, 201);
	private static final Color DEFAULT_COLOR_OVER = new Color(47, 127, 255);

	private static final Color CIRCLE_OUTLINE_COLOR2 = new Color(96, 130, 186);
	private static final Color ARROW_COLOR = new Color(0, 1, 87);
	private static final Color ARROW_COLOR_OVER = new Color(47, 127, 255);
	private static final Color FILL_START_COLOR = Color.white;
	private static final Color FILL_END_COLOR = new Color(140, 193, 225);

	private static final int DEFAULT_FONT_HEIGHT = 16;
	private static final int ARROW_SEPARATION = 4;

	public static final String CLICK_EVENT = "Click";
	public static final String CLOSE_EVENT = "Close";

	private int arrowDirection = UP_ARROW;
	private Color color = DEFAULT_COLOR;
	private Color hoverColor = DEFAULT_COLOR_OVER;
	private int fontHeight = DEFAULT_FONT_HEIGHT;
	private ArrowBorder arrowBorder;
	private CloseBorder closeBorder;
	private GradientPainter circlePainter = new GradientPainter(GradientPainter.TOP_TO_BOTTOM);
	private boolean showCloseButton = true;	// it will be set to false in initialize()
	private boolean mouseDragged = false;

	private ArrayList listeners = new ArrayList(1);

	public ArrowLabel() {
		super();
		initialize();
	}

	public ArrowLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		initialize();
	}

	public ArrowLabel(Icon image) {
		super(image);
		initialize();
	}

	public ArrowLabel(String text) {
		this(text, UP_ARROW);
	}

	public ArrowLabel(String text, int arrowDirection) {
		super(text);
		setArrowDirection(arrowDirection);
		initialize();
	}

	private void initialize() {
		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				setForeground(hoverColor);
			}
			public void mouseExited(MouseEvent e) {
				setForeground(color);
			}
			public void mouseClicked(MouseEvent e) {
				if (!mouseDragged && SwingUtilities.isLeftMouseButton(e)) {
					fireClickEvent(e.getPoint());
				}
			}
			public void mousePressed(MouseEvent e) {
				mouseDragged = false;
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				mouseDragged = true;
			}
		});

		setForeground(color);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseMotionListener(getArrowBorder());
		setShowCloseButton(false);

		circlePainter.setAntiAliasing(true);
		circlePainter.setBorderPainted(true);
		circlePainter.setBorderColor(CIRCLE_OUTLINE_COLOR2);
		circlePainter.setGradientColors(FILL_START_COLOR, FILL_END_COLOR);
	}

	private ArrowBorder getArrowBorder() {
		if (arrowBorder == null) {
			arrowBorder = new ArrowBorder();
		}
		return arrowBorder;
	}

	private CloseBorder getCloseBorder() {
		if (closeBorder == null) {
			closeBorder = new CloseBorder();
		}
		return closeBorder;
	}

	public void addListener(ActionListener al) {
		if (!listeners.contains(al)) {
			listeners.add(al);
		}
	}

	public void removeListener(ActionListener al) {
		listeners.remove(al);
	}

	protected void fireClickEvent(Point p) {
		ArrayList clone = new ArrayList(listeners);
		String cmd = CLICK_EVENT;
		if (showCloseButton && getCloseBorder().contains(p)) {
			cmd = CLOSE_EVENT;
		}

		//System.out.println("Event: " + cmd);
		ActionEvent actionEvent = new ActionEvent(this, 0, cmd);
		for (Iterator iter = clone.iterator(); iter.hasNext(); ) {
			ActionListener al = (ActionListener) iter.next();
			al.actionPerformed(actionEvent);
		}
	}

	/**
	 * Sets the arrow direction
	 * @param direction one of: up, down, left, right
	 * @see ArrowLabel#UP_ARROW
	 * @see ArrowLabel#DOWN_ARROW
	 * @see ArrowLabel#LEFT_ARROW
	 * @see ArrowLabel#RIGHT_ARROW
	 * @throws IllegalArgumentException if the direction isn't one of up, down, left, or right
	 */
	public void setArrowDirection(int direction) throws IllegalArgumentException {
		if (direction != this.arrowDirection) {
			switch (direction) {
				// fall through valid directions
				case UP_ARROW :
				case LEFT_ARROW :
				case DOWN_ARROW :
				case RIGHT_ARROW :
					this.arrowDirection = direction;
					repaint();
					break;
				default :
					throw new IllegalArgumentException("Invalid arrow direction: " + direction);
			}
		}
	}

	public int getArrowDirection() {
		return arrowDirection;
	}

	/**
	 * Warning - if you call this method the collapse/expand icon will not longer be painted.
	 */
	public void setBorder(Border border) {
		super.setBorder(border);
	}

	public void setFont(Font font) {
		fontHeight = getFontMetrics(font).getHeight() + 2;
		getArrowBorder().updatePadding();
		getCloseBorder().updatePadding();
		super.setFont(font);
	}

	/**
	 * Sets whether arrow icon should be on the right (the default).
	 * @param alignRight
	 */
	public void setArrowOnRight(boolean alignRight) {
		ArrowBorder cp = getArrowBorder();
		if (cp.isRightAlign() != alignRight) {
			cp.setRightAlign(alignRight);
			repaint();
		}
	}

	/**
	 * @return whether arrow icon should be on the right (the default).
	 */
	public boolean isArrowOnRight() {
		return getArrowBorder().isRightAlign();
	}

	/**
	 * Sets the color for this label.
	 * @param normal the color when the mouse isn't over this label
	 * @param hover the hover color when the mosue is over this label
	 */
	public void setColors(Color normal, Color hover) {
		this.color = normal;
		this.hoverColor = hover;
	}

	public void setShowCloseButton(boolean show) {
		if (this.showCloseButton != show) {
			this.showCloseButton = show;
			Border border;
			if (showCloseButton) {
				addMouseMotionListener(getCloseBorder());
				border = BorderFactory.createCompoundBorder(getCloseBorder(), getArrowBorder());
			} else {
				removeMouseMotionListener(getCloseBorder());
				border = getArrowBorder();
			}
			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2), border));
		}
	}

	public boolean isShowCloseButton() {
		return showCloseButton;
	}

	/**
	 * Paints the arrow on the left or right side of the label.
	 * @author Chris Callendar
	 * @date 3-Nov-06
	 */
	private class ArrowBorder extends EmptyBorder implements MouseMotionListener {

		private boolean rightAlign;
		private boolean isOver;
		private Ellipse2D circle;

		public ArrowBorder() {
			this(true);
		}

		public ArrowBorder(boolean rightAlign) {
			super(0, 0, 0, 0);
			this.circle = new Ellipse2D.Double();
			this.isOver = false;
			setRightAlign(rightAlign);
		}

		public boolean isRightAlign() {
			return rightAlign;
		}

		public void setRightAlign(boolean rightAlign) {
			this.rightAlign = rightAlign;
			updatePadding();
		}

		protected int getDefaultPadding() {
			return top;
		}

		/** Update the amount of horizontal padding based on the text size. */
		public void updatePadding() {
			int padding = fontHeight + 2 + (2 * getDefaultPadding());
			this.left = (rightAlign ? getDefaultPadding() : padding);
			this.right = (rightAlign ? padding : getDefaultPadding());
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// use the font size to determine how big the collapse icon is (to match the text)
			int size = Math.min(fontHeight, height);
			int startX = x + getDefaultPadding();
			int startY = y + (height / 2) - (size / 2);	// vertically align in the middle
			if (rightAlign) {
				startX = x + width - (size + getDefaultPadding());
			}

			size -= 1;	// make the circle 1 pixel smaller
			if (size % 2 == 1) {
				// even - the actual circle drawn will have a width/height of 1 greater than this
				size--;
			}

			circle = new Ellipse2D.Float(startX, startY, size, size);
			circlePainter.paint(g2, circle);

			g2.setColor(isOver ? ARROW_COLOR_OVER : ARROW_COLOR);

			paintShape(g2, startX, startY, size);
		}

		/**
		 * Fills an up, down, left, or right arrow in the center of the circle.
		 * @param startX the left edge of the circle
		 * @param startY the top edge of the circle
		 * @param diameter the diameter of the surrounding circle
		 */
		protected void paintShape(Graphics2D g2, int startX, int startY, int diameter) {
			// no antialiasing!
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

			// inside diameter (doesn't count the border)
			diameter--;	// always ODD

			int x = startX + 1;	// add 1 for the border
			int y = startY + 1;

			int longShift, longSide, shortSide, shortShift, longMid;
			int half = diameter / 2;
			int half2 = diameter - half;
			if (half % 2 == 0) {
				// EVEN
				longShift = half / 2;	// EVEN
				longSide = half2; 		// ODD
			} else {
				longShift = half2 / 2;	// EVEN
				longSide = half;		// ODD
			}
			shortSide = (int) Math.ceil(longSide / 2f);
			shortShift = (diameter - shortSide) / 2;
			longMid = longSide / 2;

			switch (arrowDirection) {
				case UP_ARROW :
					x += longShift;
					y += shortShift;
					for (int i = 0; i < shortSide; i++) {
						g2.drawLine(x + longMid - i, y + i, x + longMid + i, y + i);
					}
					break;
				case LEFT_ARROW :
					x += shortShift;
					y += longShift;
					for (int i = 0; i < shortSide; i++) {
						g2.drawLine(x + i, y + longMid - i, x + i, y + longMid + i);
					}
					break;
				case DOWN_ARROW :
					x += longShift;
					y += shortShift;
					if (shortSide % 2 == 0) {
						y++;	// looks better when shifted down one
					}
					for (int i = 0; i < shortSide; i++) {
						g2.drawLine(x + i, y + i, x + longSide - 1 - i, y + i);
					}
					break;
				case RIGHT_ARROW :
					x += shortShift;
					y += longShift;
					if (shortSide % 2 == 0) {
						x++;	// looks better when shifted right one
					}
					for (int i = 0; i < shortSide; i++) {
						g2.drawLine(x + i, y + i, x + i, y + longSide - i);
					}
					break;
			}
		}

		/**
		 * Paints two arrows (two "V" shapes) in the center of the square.
		 */
		protected void paintArrows(Graphics2D g2, int x, int y, int width, int height) {
			// use the font size to determine how big the collapse icon is (to match the text)
			int size = Math.min(fontHeight, height);
			int startX = x + getDefaultPadding();
			int startY = y + (height / 2) - (size / 2);	// vertically align in the middle
			if (rightAlign) {
				startX = x + width - (size + getDefaultPadding());
			}

			size -= 1;	// make the circle 1 pixel smaller
			g2.setColor(CIRCLE_BG_COLOR);
			g2.fillOval(startX, startY, size, size);
			g2.setColor(CIRCLE_OUTLINE_COLOR);
			g2.drawOval(startX, startY, size, size);

			drawArrows(g2, startX, startY, size);
		}

		/**
		 * Draws two up, down, left, or right arrows (V-shaped) in the center of the circle.
		 * @param startX the left edge of the circle
		 * @param startY the top edge of the circle
		 * @param diameter the diameter of the surrounding circle
		 */
		protected void drawArrows(Graphics2D g2, int startX, int startY, int diameter) {
			// calculate the top left point for the upper arrow
			int shift = (diameter / 2) - 3;
			int x = startX + shift;
			int y = startY + shift;

			// determine the three points for the arrow defining the "V" shape
			int[] xx = { 0, 0, 0 };
			int[] yy = { 0, 0, 0 };
			switch (arrowDirection) {
				case UP_ARROW :
					xx = new int[] { x, x + 3, x + 6 };
					yy = new int[] { y + 3, y, y + 3 };
					break;
				case LEFT_ARROW :
					xx = new int[] { x + 6, x + 3, x + 6 };
					yy = new int[] { y, y + 3, y + 6 };
					break;
				case DOWN_ARROW :
					xx = new int[] { x, x + 3, x + 6 };
					yy = new int[] { y, y + 3, y };
					break;
				case RIGHT_ARROW :
					xx = new int[] { x, x + 3, x };
					yy = new int[] { y, y + 3, y + 6 };
					break;
			}

			g2.setColor(getForeground());
			// draw the first arrow
			g2.drawLine(xx[0], yy[0], xx[1], yy[1]);
			g2.drawLine(xx[1], yy[1], xx[2], yy[2]);

			// shift for the next arrow
			if ((arrowDirection == UP_ARROW) || (arrowDirection == DOWN_ARROW)) {
				shiftArray(yy, ARROW_SEPARATION);
			} else {
				shiftArray(xx, (arrowDirection == LEFT_ARROW ? -ARROW_SEPARATION : ARROW_SEPARATION));
			}

			// draw the second arrow
			g2.drawLine(xx[0], yy[0], xx[1], yy[1]);
			g2.drawLine(xx[1], yy[1], xx[2], yy[2]);
		}

		private void shiftArray(int[] array, int d) {
			for (int i = 0; i < array.length; i++) {
				array[i] += d;
			}
		}

		public boolean contains(Point p) {
			return circle.contains(p);
		}

		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
		}

		public void mouseMoved(MouseEvent e) {
			boolean old = isOver;
			isOver = contains(e.getPoint());
			if (isOver != old) {
				repaint();
			}
		}

	}

	private class CloseBorder extends ArrowBorder {

		protected void paintShape(Graphics2D g2, int startX, int startY, int size) {
			// use antialiasing
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int half = size / 2;
			int quarter = size / 4;
			if ((half % 2) == 1) {
				quarter++;
				half--;
			}
			startX += quarter;
			startY += quarter;

			g2.drawLine(startX, startY, startX + half, startY + half);
			g2.drawLine(startX + half, startY, startX, startY + half);
		}

	}

	/**
	 * Tests the label.
	 */
	public static void main(String[] args) {
		EscapeDialog dlg = new EscapeDialog();
		dlg.setPreferredSize(new Dimension(200, 300));
		dlg.setModal(true);
		dlg.setLocation(400, 200);

		int rows = 4;
		JPanel pnl = new JPanel(new GridLayout(rows, 1, 0, 4));
		Font font = pnl.getFont().deriveFont(8f);
		for (int i = 0; i < rows; i++) {
			font = font.deriveFont(font.getSize2D() + 2);
			final ArrowLabel lbl = new ArrowLabel("Label (" + font.getSize2D() + ")");
			lbl.setOpaque(true);
			lbl.setBackground(Color.cyan);
			lbl.setArrowDirection(i % rows);
			lbl.setFont(font);
			pnl.add(lbl);
			lbl.addListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String cmd = e.getActionCommand();
					if (CLICK_EVENT.equals(cmd)) {
						// toggle the arrow direction
						lbl.setArrowDirection((lbl.getArrowDirection() + 2) % 4);
					} else if (CLOSE_EVENT.equals(cmd)) {
						Container parent = lbl.getParent();
						parent.remove(lbl);
						parent.validate();
						parent.repaint();
					}
				}
			});
			lbl.setShowCloseButton(true);
		}

		dlg.getContentPane().add(pnl, BorderLayout.CENTER);
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dlg.setVisible(true);
		System.exit(0);
	}

}
