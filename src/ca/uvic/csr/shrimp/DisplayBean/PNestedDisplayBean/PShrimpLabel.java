/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.util.GraphicsUtils;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * A PShrimpLabel is a piccolo component that displays a single "sticky" label.
 * The size of this label remains constant as the camera zooms in and out.
 * The coordinates of this label are updated when its associated
 * display object is relocated. This is done by implementing the interface ShrimpDisplayObjectListener.
 * This component also listens for level changed events from the display bean and
 * updates its size and/or opacity according to its level.
 *
 * A PShrimpLabel is associated with one artifact in the databean; however
 * an artifact could have many PShrimpLabels in the display, because of
 * multiple inheritence.
 *
 * @author Rob Lintern
 */
public abstract class PShrimpLabel extends PNode implements ShrimpLabel  {

	protected static final double PADDING = 2.0;
	private static final double ICON_GAP = 0.0; // pixels
	private static final Color BORDER_HIGHLIGHT_COLOR = Color.BLUE;
	private static final double BORDER_HIGHLIGHT_WIDTH = 2.0;
	private static final String ELLIPSIS = "...";

	/** The display object that this label identifies */
	protected ShrimpDisplayObject displayObject;
	protected DisplayBean displayBean;

	private boolean backgroundOpaque;

	protected PCanvas canvas;
	private PImage iconImage;
	protected PText pText;
	protected String text;
    private Color backgroundColor;
    private Color textColor = Color.BLACK;
    protected boolean highlighted = false;

    private boolean hovering = false;
    private Color backupTextColor = null;
    private Color backupBGColor = null;
    private boolean backupOpaque = false;
    private float backupTransparency = 1f;

	/**
	 * Constructs a new label with a reference to the display object that it labels.
	 * @param displayBean The display that this label is shown on.
	 * @param displayObject The display object (node, arc, etc) that this label labels.
	 * @param font The font to be used for this label.
	 * @param text The text of this label.
	 */
	public PShrimpLabel(DisplayBean displayBean, ShrimpDisplayObject displayObject, Font font, String text) {
		this.displayBean = displayBean;
		this.canvas = ((PNestedDisplayBean)displayBean).getPCanvas();
		this.displayObject = displayObject;

		pText = new PText();
		addChild(pText);
		pText.setFont(font);
		setText(text);
		setPaint(null);
	}

	/**
	 * Elide to show first n characters
	 * @param nChars
	 */
	protected void elideLeft(int nChars) {
		if (text.length() > nChars) {
			setText( text.substring(0, nChars) + ELLIPSIS);
		}
	}

	/**
	 * Elide to show last n characters
	 * @param nChars
	 */
	protected void elideRight(int nChars) {
		if (text.length() > nChars) {
			setText(ELLIPSIS + text.substring(text.length()-nChars, text.length()));
		}
	}

	/**
	 * Restore displayed label text to original
	 */
	protected void hideLabel() {
		pText.setText("");
	}

	/**
	 * Restore displayed label text to original
	 */
	protected void restoreLabel() {
		pText.setText(this.text);
	}

	public void viewTransformChanged() {
		if (isVisible()) {
			displayObjectPositionChanged();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#dispose()
	 */
	public void dispose() {
		removeAllChildren();
		removeFromParent();
		disposeIcon();

		pText.removeAllChildren();
		pText.removeFromParent();

		displayObject = null;
		displayBean = null;
		canvas = null;
	}

	/**
	 * Removes the icon from it's parent and disposes it.
	 */
	private void disposeIcon() {
		if (iconImage != null) {
			iconImage.removeAllChildren();
			iconImage.removeFromParent();
			iconImage.getImage().flush();
			iconImage = null;
		}
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel#setHighlighted()
     */
    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    protected void paint(PPaintContext paintContext) {
		if (highlighted || backgroundOpaque) {
			Graphics2D g2 = paintContext.getGraphics();
			double mag = paintContext.getScale();

			int x = (int)getBounds().x;
			int y = (int)getBounds().y;
			int w = (int)getBounds().width;
			int h = (int)getBounds().height;

			// draw background rectangle
			if (backgroundOpaque) {
				g2.setPaint(backgroundColor);
				g2.fillRect(x, y, w, h);
			}

			//draw a thin rectangle around this label
			g2.setStroke(new BasicStroke ((float) ((highlighted ? BORDER_HIGHLIGHT_WIDTH : 1.0)/mag)));
			g2.setPaint(highlighted ? BORDER_HIGHLIGHT_COLOR : Color.black);
			g2.drawRect(x, y, w, h);
		}
    }

	/* (non-Javadoc)
	 * @see edu.umd.cs.piccolo.PNode#pick(edu.umd.cs.piccolo.util.PPickPath)
	 */
	protected boolean pick(PPickPath pickPath) {
		boolean picked = super.pickAfterChildren(pickPath);
		return picked;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getGlobalOuterBounds()
	 */
	public Rectangle2D.Double getGlobalOuterBounds() {
		return getGlobalFullBounds();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getOuterBounds()
	 */
	public Rectangle2D.Double getOuterBounds() {
		return getFullBounds();
	}

	/**
	 * Set the name of this node
	 */
	public void setName(String name) {
		setText(name);
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel#getLabeledObject()
     */
    public ShrimpDisplayObject getLabeledObject() {
         return displayObject;
    }

	public String toString() {
		return "Label of " + displayObject;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isVisible()
	 */
	public boolean isVisible() {
		return getVisible();
	}

	public void setVisible(boolean newVisibility) {
		super.setVisible(newVisibility);
		setPickable(newVisibility);

		if (newVisibility) {
			mouseOut();
		}
	}

	public boolean isHovering() {
		return hovering;
	}

	public void mouseOver(Color bgColor) {
		if (!hovering) {
			// backup the values for bg color, text color, and bg opaque
			backupTextColor = getTextColor();
			backupBGColor = getBackgroundColor();
			backupOpaque = isBackgroundOpaque();
			backupTransparency = getTransparency();

			// change the label properties to be more visible
			setTextColor(GraphicsUtils.getTextColor(bgColor));
			setBackgroundColor(bgColor);
			setBackgroundOpaque(true);
			setTransparency(1f);

			hovering = true;
		}
	}

	public void mouseOut() {
		if (hovering) {
			// restore the old values for text color, bg color, and bg opaque
			setTextColor(backupTextColor);
			setBackgroundColor(backupBGColor);
			setBackgroundOpaque(backupOpaque);
			setTransparency(backupTransparency);

			hovering = false;
		}
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener#displayObjectPositionChanged()
     */
    public void displayObjectPositionChanged() {
    	if (displayBean != null) {
    		displayObjectPositionChanged(displayBean.getCurrentFocusedOnObjects());
    	}
    }

	public void updateVisibility() {
    	if (displayBean != null) {
    		updateVisibility(displayBean.getCurrentFocusedOnObjects());
    	}
	}

	public void setBackgroundOpaque(boolean backgroundOpaque) {
		this.backgroundOpaque = backgroundOpaque;
		if (backgroundOpaque) {
			pText.setPaint(GraphicsUtils.getTextColor(backgroundColor));
		} else {
			pText.setPaint(textColor);
		}
		repaint();
	}

	public boolean isBackgroundOpaque() {
		return backgroundOpaque;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel#getText()
	 */
	public String getText() {
		return pText.getText();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel#setText(String)
	 */
	public void setText(String s) {
		this.text = s; // save this for use when eliding or hiding text
		pText.setText(this.text);
		PBounds textBounds = pText.getBounds();
		setBounds(-PADDING, -PADDING, textBounds.getWidth() + 2*PADDING,
				textBounds.getHeight()+ 2*PADDING);
	}

	/**
	 * Changes the font of the label wrapped by this component (if different from the current font).
	 * @param font The new font for the label.
	 */
	public void setFont(Font font) {
		if ((getFont() != null) && !getFont().equals(font)) {
			pText.setFont(font);
		}
	}

	/**
	 * Gets the font for the label.
	 * @return Font or null
	 */
	public Font getFont() {
		return (pText != null ? pText.getFont() : null);
	}

	/**
	 * @see ShrimpLabel#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color color) {
	    this.backgroundColor = color;
		if (backgroundOpaque) {
			pText.setPaint(GraphicsUtils.getTextColor(color));
		}
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
     * @see ShrimpLabel#setTextColor(Color)
     */
    public void setTextColor(Color color) {
        this.textColor = color;
		if (!backgroundOpaque) {
			pText.setPaint(textColor);
		}
    }

    public Color getTextColor() {
    	return textColor;
    }

	/**
	 * @see ShrimpLabel#setIcon(Icon)
	 */
	public void setIcon(Icon icon) {
		// remove the existing icon (if there is one)
		disposeIcon();

		// add the new icon
		if (icon instanceof ImageIcon) {
			Image image = ((ImageIcon)icon).getImage();
			iconImage = new PImage();
			iconImage.setImage(image);
			ImageObserver observer = new ImageObserver() {
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
					return false;
				}
			};
			int imageWidth = image.getWidth(observer);
			int imageHeight = image.getHeight(observer);
			iconImage.setBounds(0, 0, imageWidth, imageHeight);
			addChild(iconImage);

			pText.setTransform(AffineTransform.getTranslateInstance(imageWidth + ICON_GAP, 0));
			double x = -PADDING;
			double y = -PADDING;
			double w = imageWidth + ICON_GAP + pText.getBounds().getWidth() + 2*PADDING;
			double h = Math.max(imageHeight, pText.getBounds().getHeight()) + 2*PADDING;
			setBounds(x, y, w, h);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel#raiseAboveSiblings()
	 */
	public void raiseAboveSiblings() {
		moveToFront();
	}

	protected double calculateOpacity(Vector currentFocusedOnObjects) {
	    return 1.0;
	}

	/**
	 * check to see if this label is on an object that is inside an area of interest, whatever that may be
	 * @param currentFocusedOnObjects
	 * @return true if this label is the area of interest
	 */
	protected abstract boolean isInAreaOfInterest(Vector currentFocusedOnObjects);

    public abstract void displayObjectPositionChanged(Vector currentFocusedOnObjects);

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isInDisplay()
     */
    public boolean isInDisplay() {
        return (getParent() != null);
    }

}
