/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


/**
 * A {@link JPanel} that can be collapsed.
 * It has a title bar which has a string and a collapse/expand button.
 *
 * @author Chris Callendar
 * @date 1-Nov-06
 */
public class CollapsiblePanel extends TransparentPanel {

	/**
     * Used when generating {@link PropertyChangeEvent}s for the "collapsed" property.
     * The new and old values will be {@link Boolean} objects.
     */
	public static final String COLLAPSED_CHANGED_KEY = "collapsed";

	/**
     * Used when generating {@link PropertyChangeEvent}s for the "closed" property
     * The old and new values will be {@link Boolean} objects, and will always be
     * false and true.
     */
	public static final String CLOSED_KEY = "closed";

	private static final Color BG_MAIN = new Color(214, 223, 247);
	private static final Color TITLE_START = new Color(245, 247, 253);
	private static final Color TITLE_END = new Color(199, 212, 247);

	private static final int TITLE_HEIGHT = 24;
	private static final int PADDING = 10;
	private static final int DEFAULT_ARC_WIDTH = 4;

	private JComponent mainComponent;
	private JPanel mainPanel;
	private JPanel innerPanel;
	private JPanel titlePanel;
	private ArrowLabel titleLabel;
	private GradientPainter painter;
	private boolean collapsed;
	private int arcWidth = DEFAULT_ARC_WIDTH;

	private ArrayList listeners;
	private ActionListener closeListener;

	private int innerPadding;

	private Border innerBorder;

	public CollapsiblePanel(JComponent mainComponent) {
		this("", mainComponent);
	}

	public CollapsiblePanel(String title, JComponent mainComponent) {
		this(title, null, mainComponent);
	}

	public CollapsiblePanel(String title, Icon icon, JComponent mainComponent) {
		super(new BorderLayout(0, 0), true);
		this.mainComponent = mainComponent;
		this.collapsed = false;
		this.painter = new GradientPainter(TITLE_START, TITLE_END, GradientPainter.LEFT_TO_RIGHT);
		this.painter.setBorderPainted(false);
		this.listeners = new ArrayList(2);
		this.closeListener = null;
		initialize();
		setTitle(title);
		setTitleIcon(icon);
	}

	private void initialize() {
		mainPanel = new TransparentPanel(new BorderLayout());
		mainPanel.add(getTitlePanel(), BorderLayout.NORTH);

		innerPanel = new JPanel(new BorderLayout());
		innerPanel.setBackground(BG_MAIN);
		innerPanel.add(mainComponent, BorderLayout.CENTER);
		mainComponent.setOpaque(false);
		mainPanel.add(innerPanel, BorderLayout.CENTER);
		add(mainPanel, BorderLayout.NORTH);

		//setOuterPadding(PADDING);
		setInnerPadding(PADDING);
	}

	public JComponent getMainComponent() {
		return mainComponent;
	}

	public ArrowLabel getTitleLabel() {
		if (titleLabel == null) {
			titleLabel = new ArrowLabel("", (collapsed ? ArrowLabel.DOWN_ARROW : ArrowLabel.UP_ARROW));
			titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
			titleLabel.addListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (ArrowLabel.CLOSE_EVENT.equals(e.getActionCommand())) {
						closePanel();
					} else {
						toggleCollapsed();
					}
				}
			});
		}
		return titleLabel;
	}

	private JPanel getTitlePanel() {
		if (titlePanel == null) {
			titlePanel = new TransparentPanel() {
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					Shape shape = GradientPainter.createRoundedTopRectangle(0, 0, this.getWidth(), this.getHeight(), arcWidth);
					painter.paint(g, shape);
					Graphics2D g2 = (Graphics2D) g;
					g2.setColor(TITLE_END);
					g2.draw(shape);
				}
			};
			titlePanel.setBorder(BorderFactory.createEmptyBorder(arcWidth, arcWidth * 2, arcWidth, arcWidth));
			titlePanel.setPreferredSize(new Dimension(200, TITLE_HEIGHT));
			titlePanel.setMinimumSize(new Dimension(50, TITLE_HEIGHT));
			titlePanel.setMaximumSize(new Dimension(5000, TITLE_HEIGHT));
			titlePanel.setLayout(new BorderLayout());
			titlePanel.add(getTitleLabel(), BorderLayout.CENTER);
		}
		return titlePanel;
	}

	public void addChangeListener(PropertyChangeListener pcl) {
		if (!listeners.contains(pcl)) {
			listeners.add(pcl);
		}
	}

	public void removeChangeListener(PropertyChangeListener pcl) {
		listeners.remove(pcl);
	}

	protected void fireChangeEvent(PropertyChangeEvent evt) {
		if (listeners.size() > 0) {
			for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
				PropertyChangeListener pcl = (PropertyChangeListener) iter.next();
				pcl.propertyChange(evt);
			}
		}
	}

	public int getArcWidth() {
		return arcWidth;
	}

	public void setArcWidth(int arcWidth) {
		if (this.arcWidth != arcWidth) {
			this.arcWidth = arcWidth;
			getTitlePanel().repaint();
		}
	}

	/**
	 * @return true if this label is collapsed
	 */
	public boolean isCollapsed() {
		return collapsed;
	}

	/**
	 * Sets whether this panel is collapsed or expanded.
	 * Also fires a {@link PropertyChangeEvent} for any {@link PropertyChangeListener}s
	 * if the collapsed value is different from the current state.
	 * @see CollapsiblePanel#COLLAPSED_CHANGED_KEY
	 * @param collapsed
	 */
	public void setCollapsed(boolean collapsed) {
		if (isCollapsed() != collapsed) {
			setCollapsedPrivate(collapsed);
			PropertyChangeEvent evt = new PropertyChangeEvent(this, COLLAPSED_CHANGED_KEY,
					new Boolean(!collapsed), new Boolean(collapsed));
			fireChangeEvent(evt);
		}
	}

	/**
	 * Doesn't fire an event, and doesn't check if collapsed is different from current value.
	 */
	private void setCollapsedPrivate(boolean collapsed) {
		this.collapsed = collapsed;
		titleLabel.setArrowDirection((collapsed ? ArrowLabel.DOWN_ARROW : ArrowLabel.UP_ARROW));
		int height = TITLE_HEIGHT;
		if (collapsed) {
			mainPanel.remove(innerPanel);
		} else {
			mainPanel.add(innerPanel, BorderLayout.CENTER);
			height += (innerPanel.getHeight() <= 0 ? innerPanel.getPreferredSize().height : innerPanel.getHeight());
		}
		setPreferredSize(new Dimension(mainPanel.getWidth(), height));
		if (getParent() != null) {
			getParent().invalidate();
			getParent().validate();
		}
	}

	private void toggleCollapsed() {
		setCollapsed(!isCollapsed());
	}

	private void closePanel() {
		fireChangeEvent(new PropertyChangeEvent(this, CLOSED_KEY, new Boolean(false), new Boolean(true)));

		if (closeListener != null) {
			closeListener.actionPerformed(new ActionEvent(this, 0, CLOSED_KEY));
		} else {
			// default close operation - remove this panel from its parent
			Container parent = getParent();
			parent.remove(this);
			parent.invalidate();
			parent.validate();
			parent.repaint();
		}
	}

	/**
	 * Sets the titlebar gradient colors.
	 */
	public void setTitleGradientColors(Color gradientStart, Color gradientEnd) {
		painter.setGradientColors(gradientStart, gradientEnd);
		getTitlePanel().repaint();
	}

	public Color getTitleGradientStartColor() {
		return painter.getStartColor();
	}

	public Color getTitleGradientEndColor() {
		return painter.getEndColor();
	}

	/**
	 * Sets the colors for the titlebar.
	 * @see CollapsiblePanel#setTitleGradientColors(Color, Color)
	 * @see CollapsiblePanel#setTitleTextColors(Color, Color)
	 */
	public void setTitleColors(Color gradientStart, Color gradientEnd, Color text, Color textRollover) {
		setTitleGradientColors(gradientStart, gradientEnd);
		setTitleTextColors(text, textRollover);
		getTitlePanel().repaint();
	}

	public void setTitleTextColors(Color normal, Color rollover) {
		getTitleLabel().setColors(normal, rollover);
	}

	public void setTitleFont(Font font) {
		getTitleLabel().setFont(font);
	}

	public Font getTitleFont() {
		return getTitleLabel().getFont();
	}

	public void setTitle(String title) {
		getTitleLabel().setText(title);
	}

	public String getTitle() {
		return getTitleLabel().getText();
	}

	public void setTitleIcon(Icon icon) {
		getTitleLabel().setIcon(icon);
	}

	public Icon getTitleIcon() {
		return getTitleLabel().getIcon();
	}

	public void setInnerBackgroundColor(Color c) {
		innerPanel.setBackground(c);
	}

	public Color getInnerBackgroundColor() {
		return innerPanel.getBackground();
	}

	/**
	 * Sets an empty border on the inner panel with the given padding on each side.
	 */
	public void setInnerPadding(int padding) {
		this.innerPadding = padding;
		if (innerBorder == null) {
			innerPanel.setBorder(BorderFactory.createEmptyBorder(innerPadding, innerPadding, innerPadding, innerPadding));
		} else {
			innerPanel.setBorder(BorderFactory.createCompoundBorder(innerBorder,
					BorderFactory.createEmptyBorder(innerPadding, innerPadding, innerPadding, innerPadding)));
		}
	}

	public void setInnerBorder(Border border) {
		this.innerBorder = border;
		innerPanel.setBorder(BorderFactory.createCompoundBorder(innerBorder,
				BorderFactory.createEmptyBorder(innerPadding, innerPadding, innerPadding, innerPadding)));
	}

	/**
	 * Sets an empty border with the given padding on each side.
	 * Don't use if you have already set a border on this panel.
	 */
	public void setOuterPadding(int padding) {
		setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
	}

	/**
	 * Sets the gradient direction.
	 * @param direction the direction
	 * @see GradientPainter#TOP_TO_BOTTOM
	 * @see GradientPainter#LEFT_TO_RIGHT
	 * @see GradientPainter#TOP_LEFT_TO_BOTTOM_RIGHT
	 * @see GradientPainter#BOTTOM_LEFT_TO_TOP_RIGHT
	 */
	public void setTitleBarGradientDirection(int direction) {
		painter.setGradientDirection(direction);
		getTitlePanel().repaint();
	}

	/**
	 * Gets the title bar gradient direction.
	 * @see GradientPainter#TOP_TO_BOTTOM
	 * @see GradientPainter#LEFT_TO_RIGHT
	 * @see GradientPainter#TOP_LEFT_TO_BOTTOM_RIGHT
	 * @see GradientPainter#BOTTOM_LEFT_TO_TOP_RIGHT
	 */
	public int getTitleBarGradientDirection() {
		return painter.getGradientDirection();
	}

	public void setShowCloseButton(boolean showCloseButton) {
		getTitleLabel().setShowCloseButton(showCloseButton);
	}

	public void setCloseListener(ActionListener closeListener) {
		this.closeListener = closeListener;
	}

	public String toString() {
		return "CollapsiblePanel: " + getTitle();
	}

	public static void main(String[] args) {
		EscapeDialog dlg = new EscapeDialog();
		dlg.setPreferredSize(new Dimension(400, 600));
		dlg.setModal(true);
		dlg.setLocation(400, 200);

		JPanel pnl = new JPanel();
		pnl.add(new JLabel("Test"));
		pnl.add(new JButton("Click"));
		CollapsiblePanel colPnl = new CollapsiblePanel("Test", pnl);

		GradientPanel gp = new GradientPanel(new BorderLayout());
		gp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		gp.add(colPnl, BorderLayout.NORTH);
		dlg.getContentPane().add(gp, BorderLayout.CENTER);
		dlg.setVisible(true);
		System.exit(0);
	}

}
