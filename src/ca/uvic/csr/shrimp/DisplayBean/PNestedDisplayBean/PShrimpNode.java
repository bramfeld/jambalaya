/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.eclipse.mylar.zest.layouts.constraints.BasicEntityConstraint;
import org.eclipse.mylar.zest.layouts.constraints.LabelLayoutConstraint;
import org.eclipse.mylar.zest.layouts.constraints.LayoutConstraint;

import ca.uvic.cs.seqlayout.algorithms.internal.SequenceNodeLayoutConstraint;
import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpColorConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.DiamondNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RoundedRectangleTriangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.StackedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.GeometryUtils;
import ca.uvic.csr.shrimp.util.GraphicsUtils;
import ca.uvic.csr.shrimp.util.HyperlinkHandCursorAdapter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PInputManager;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.util.PBoundsLocator;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * A holder in the piccolo tree for a "shrimp" node.
 * A PShrimpNode is associated with a single artifact in the data bean; however, due to multiple parents
 * an artifact can be represented by multiple nodes in the display.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class PShrimpNode extends PNode implements Comparable, ShrimpNode {

	//private static final String ATTR_ABOVE_NODE_LABEL = "Above Node Label Attribute";
	private static final Icon ATTACHMENT_ICON = ResourceHandler.getIcon("icon_attachment.gif");
	private static final Icon ANNOTATION_ICON = ResourceHandler.getIcon("icon_annotation.gif");

	/** Avoid allocating this each time a node is rendered.*/
	private final static JPanel RENDER_JPANEL = new JPanel();
	private final static boolean LIMIT_IMAGE_SCALE = false;
	private final static NodeShape COLLAPSE_NODE_SHAPE = new RoundedRectangleTriangleNodeShape();
	private final static NodeShape PRUNE_ROOT_NODE_SHAPE = new DiamondNodeShape();
	private final static NodeShape GROUPED_NODE_SHAPE = new StackedRectangleNodeShape();
	private final static double CUSTOMIZED_PANEL_IMAGE_SCALE = 0.5;
	public static boolean DEFAULT_ANIMATE_DOORS = false;
	public static boolean DEFAULT_SHOW_RESIZE_HANDLES = true;
	/** Determines if a plus icon is rendered on closed nodes that have children */
	public static boolean RENDER_PLUS_ICON = true;
	public static boolean RENDER_ATTACHMENT_ICON = true;

	// @tag Shrimp.fitToNodeLabelling
	/** Minimum allowable font height */
	private static final int MINIMUM_CANVAS_FONT_HEIGHT = 15;
	protected static final double PADDING = 2.0;

	private boolean isOpen;

	private boolean labelVisible;

	/** a unique id for this node */
	private long id;
	private static long nextId = 1;

	/** the display that this node is on */
	private DisplayBean displayBean;

	/** The artifact represented by this node */
	private Artifact artifact;

	/** the panel mode of this node (ex. showing children, closed, etc) */
	private String panelMode;

	// keeps track of whether or not this node has been placed on the screen
	private boolean hasBeenTransformedOnce = false;

	private boolean isCollapsed;
	private boolean hasCollapsedAncestor;
	private boolean isPruneRoot;
	private boolean hasBeenPrunedFromTree;
	private boolean isGrouped;

	private boolean marked;
	private boolean childrenAdded = false;

	private Component customizedPanel = null;
	private Image customizedPanelImage = null;

	private Map panelModeToPanel;
	private NodeShape currentNodeShape;
	private NodeShape defaultNodeShape;
	private NodeShape beforeCollapseNodeShape;	// saved before a collapse
	private NodeShape beforeGroupNodeShape;
	private Color closedColor;
	private NodeImage nodeImage;
	private Color outerBorderColor;
	private String outerBorderStyle;
	private Color innerBorderColor;
	private String innerBorderStyle;

	private boolean focusedOn = false;

	private boolean isOpenable = true;
	private boolean showAttachments = true;

	/** Stores the interested ShrimpNodeListeners. */
	private Vector shrimpDisplayObjectListeners;

	/** The visual centre of this node. */
	private transient Point2D.Double centrePoint;

	//private Image collapsedImage;
	//private Image pruneRootImage;
	private boolean highlighted;
	private boolean mouseOver;

	private String name;
	private String labelMode;
	private Icon icon;

	private boolean resizable = true;
	private double magnification = 1.0;

	/**
	 * The image to display when this node is in the "closed" state.
	 * May be null, to indicate that no image should be drawn.
	 */
	private Image displayImage;

	private Rectangle2D.Double innerBounds;
	private Rectangle2D.Double labelBounds;

	private Vector terminals = new Vector();

	private int level;

	private PInputEventListener mouseOverListener;
	private Collection boundsHandles = new ArrayList();

    private boolean equivalentNodeSelected = false;

    private boolean currentlyAnimatingDoors = false;
    private Rectangle2D.Double leftDoor = new Rectangle2D.Double(0,0,ShrimpNode.DEFAULT_NODE_DIMENSION/2.0, ShrimpNode.DEFAULT_NODE_DIMENSION);
    private Rectangle2D.Double rightDoor = new Rectangle2D.Double(ShrimpNode.DEFAULT_NODE_DIMENSION/2.0,0,ShrimpNode.DEFAULT_NODE_DIMENSION/2.0, ShrimpNode.DEFAULT_NODE_DIMENSION);

    // provides an overlay icon and position
    private IconProvider iconProvider;

	/**
	 * Constructs a new PShrimpNode with a reference to the artifact it represents.
	 * @param nodeShape The shape of this node.
	 * @param labelStyle The style of label (e.g., hidden, full, elided right)
	 * @param artifact The artifact this node represents
	 * @param displayBean The display that this node is part of.
	 * @param nodeImage An image to be rendered on the node (with extra information).
	 */
	public PShrimpNode(NodeShape nodeShape, String labelStyle, Artifact artifact, DisplayBean displayBean, NodeImage nodeImage) {
		super();
		this.artifact = artifact;
		this.displayBean = displayBean;
		this.displayImage = null;
		this.nodeImage = nodeImage;
		this.currentNodeShape = nodeShape;
		this.defaultNodeShape = nodeShape;
		this.beforeCollapseNodeShape = nodeShape;
		this.beforeGroupNodeShape = nodeShape;
		this.labelStyle = labelStyle;
		this.iconProvider = null;

		setName((artifact != null ? artifact.getName() : "null"));

		id = nextId++;

		panelMode = PanelModeConstants.CLOSED;
		panelModeToPanel = new HashMap ();
		shrimpDisplayObjectListeners = new Vector(10);
		centrePoint = new Point2D.Double();
		highlighted = false;

		// listen for when this node has the mouse over it
		mouseOverListener = new PInputManager() {
			public void mouseEntered(PInputEvent e) {
				super.mouseEntered(e);
				// @tag Shrimp.mouseover : do we want to only show mouseover when the displaybean is enabled?
				// this happens when the ShrimpView canvas is in focus
				if (e.getPickedNode().equals(PShrimpNode.this)/* && PShrimpNode.this.displayBean.isEnabled()*/) {
					PShrimpNode.this.setIsMouseOver(true);
				}
			}

			public void mouseExited(PInputEvent e) {
				super.mouseExited(e);
				// if setIsMouseOver(false) only gets called when the DisplayBean is enabled then a bug happens
				// when you right click on a node and then left click outside the node - it stays mouseovered
				if (e.getPickedNode().equals(PShrimpNode.this)/* && PShrimpNode.this.displayBean.isEnabled()*/) {
					PShrimpNode.this.setIsMouseOver(false);
				}
			}
		};
		this.addInputEventListener(mouseOverListener);
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#raiseAboveSiblings()
	 */
	public void raiseAboveSiblings() {
		// raise this node and all its descendents to the front of its siblings
		Vector ancestors = displayBean.getDataDisplayBridge().getAncestorNodes(this);
		ancestors.add(this);
		for (Iterator iter = ancestors.iterator(); iter.hasNext();) {
			PShrimpNode psn = (PShrimpNode) iter.next();
			psn.moveAboveSiblingNodes();
			ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(psn, false);
			if (label != null && label.isVisible()) {
				label.raiseAboveSiblings();
			}
		}
	}

	// move this node above its sibling nodes, but not above its sibling labels
	private void moveAboveSiblingNodes () {
		moveToFront();
	}


	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getLevel()
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setLevel(int)
	 */
	public void setLevel(int level) {
		this.level = level;
	}


	/**
	 * Gets the magnification.  This value is the scale value
	 * that is set from the {@link PPaintContext} each time the node is rendered.
	 * It is a value that indicates how big the node is relative to the view bounds.
	 */
	public double getMagnification() {
		return magnification;
	}

	public boolean shouldRender(PPaintContext paintContext) {
		if (!isVisible()) {
			(new Exception ("shouldn't be rendering")).printStackTrace();
			return false;
		}

		//Don't bother rendering this node if one of its children takes up the whole screen
		Rectangle2D.Double viewBounds = paintContext.getCamera().getViewBounds();
		for (Iterator iter = getChildrenIterator(); iter.hasNext();) {
			PNode pNode = (PNode) iter.next();
			if (pNode instanceof PShrimpNode) {
				PBounds childGlobalFullBounds = pNode.getGlobalFullBounds();
				if (childGlobalFullBounds.contains(viewBounds)) {
				    return false;
				}
			}
		}

		// Don't render this node if it occludes its parent's label
		return !occludesParent(paintContext);
	}

	/**
	 * Render the node, first rendering its shape and then the
	 * background and image as required
	 * @param paintContext
	 */
	protected void renderNode(PPaintContext paintContext) {
	    if (!shouldRender(paintContext)) {
	    	return;
	    }

	    recomputeInnerBounds();

		// 1. render the node shape
		magnification = paintContext.getScale();
   		Graphics2D g2 = paintContext.getGraphics();
		NodeBorder border = new NodeBorder(magnification,
				this.outerBorderColor, this.outerBorderStyle,
				this.innerBorderColor, this.innerBorderStyle,
				highlighted, mouseOver, equivalentNodeSelected);
		currentNodeShape.render(getOuterBounds(), g2, closedColor, border, nodeImage);

		// @tag Shrimp.fitToNodeLabelling
		// 2. render the label, if neccessary
		// NOTE: g2.drawString is a time consuming task, so we check to see if we really need to draw it
		if (labelVisible && !labelStyle.equals(DisplayConstants.LABEL_STYLE_HIDE)) {
			renderOnNodeLabel(g2, border);
		}

		// 3. render the content area background (this does not include the header)
		if (isOpen()) {
			g2.setPaint(Color.WHITE); //getOpenColor());
			g2.fill(innerBounds);
			// @tag Shrimp.node.drawInnerBorder
			if (currentNodeShape.drawContentBorder()) {
				g2.setPaint(NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR);
				g2.setStroke(new PFixedWidthStroke(1f));
				g2.draw(innerBounds);
			}
		}

		// 4. render the closed image if there is one
		if (displayImage != null && displayImage.getWidth(RENDER_JPANEL) != -1
				&& displayImage.getHeight(RENDER_JPANEL) != -1
				&& !getPanelMode().equals(PanelModeConstants.CHILDREN)
				&& getOnNodeText() == null) { // don't display if we are rendering on node text
			renderImage(paintContext);
		}

		// 5. render a small paperclip or annotation icon if this node has one or more documents/annotations
		if (shouldRenderDocumentIcon()) {
			renderDocumentIcon(g2, ATTACHMENT_ICON);
		} else if (shouldRenderAnnotationIcon()) {
			renderDocumentIcon(g2, ANNOTATION_ICON);
		}

		// 6. render an overlay icon if there is one
		if (shouldRenderOverlayIcon()) {
			renderOverlayIcon(g2);
		}

		// 7. render a '+' icon in the top left if the node
		if (shouldRenderPlusIcon()) {
			renderPlusIcon(g2);
		}
	}

	/**
	 * Calculates the actual size of this node as it will be rendered by the graphics object.
	 * It multiplies the node's outer bounds by the transform's scaling factors.
	 * @return the bounds of the this node as it will be rendered
	 */
	protected Rectangle2D.Double getActualBounds(Graphics2D g2) {
		Double b = getOuterBounds();
		AffineTransform t = g2.getTransform();
		int x = 0;
		int y = 0;
		int w = (int)(b.width * t.getScaleX());
		int h = (int)(b.height * t.getScaleY());
		return new Rectangle2D.Double(x, y, w, h);
	}

	/**
	 * @return true if the overlay icon should be rendered.
	 */
	protected boolean shouldRenderOverlayIcon() {
		boolean yes = (iconProvider != null);
		if (yes) {
			if (isOpen() && (iconProvider.getRenderOption() == IconProvider.RENDER_CLOSED_NODES)) {
				yes = false;
			} else if (!isOpen() && (iconProvider.getRenderOption() == IconProvider.RENDER_OPEN_NODES)) {
				yes = false;
			}
		}
		return yes;
	}

	protected void renderOverlayIcon(Graphics2D g2) {
		Icon overlayIcon = iconProvider.getIcon();
		if (overlayIcon != null) {
			int mode = iconProvider.getScaleMode();
			int iconWidth = overlayIcon.getIconWidth();
			int iconHeight = overlayIcon.getIconHeight();
			int iconSize = Math.max(iconWidth, iconHeight);

			Rectangle2D bounds;
			if (IconProvider.SCALE_NONE == mode) {
				bounds = getActualBounds(g2);
			} else {
				bounds = getBounds();
			}
			double size = Math.min(bounds.getWidth(), bounds.getHeight());
			Point2D p = iconProvider.getIconPosition(bounds);
			if (IconProvider.SCALE_NODE == mode) {
				if (size > iconSize) {
					overlayIcon.paintIcon(RENDER_JPANEL, g2, (int)p.getX(), (int)p.getY());
				}
			} else if ((IconProvider.SCALE_FIT_TO_NODE == mode) ||
					(IconProvider.SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO == mode) ||
					(IconProvider.SCALE_TILE == mode)) {
					Image img;
					if (overlayIcon instanceof ImageIcon) {
						img = ((ImageIcon)overlayIcon).getImage();
					} else {
						img = new BufferedImage(iconWidth, iconHeight, BufferedImage.TYPE_INT_ARGB);
						overlayIcon.paintIcon(RENDER_JPANEL, img.getGraphics(), iconWidth, iconHeight);
					}
					boolean tile = (IconProvider.SCALE_TILE == mode);
					boolean keepAspectRatio = (IconProvider.SCALE_FIT_TO_NODE_KEEP_ASPECT_RATIO == mode);
					if (tile) {
						int padding = (iconProvider instanceof DefaultIconProvider ? ((DefaultIconProvider)iconProvider).getIconPadding() : 0);
						GraphicsUtils.drawTiledImage(bounds, g2, img, RENDER_JPANEL, iconWidth, iconHeight, padding);
					} else {
						GraphicsUtils.drawStretchedImage(bounds, g2, img, RENDER_JPANEL, keepAspectRatio);
					}
			} else {	// NO SCALING
				if (size > iconSize) {
					AffineTransform t = g2.getTransform();	// save the old transform
					// use a new transformation - only translate, no scaling (icon will always be the same size)
					AffineTransform nt = new AffineTransform();
					// need to round to an integer otherwise the image gets scaled
					nt.setToTranslation(Math.round(t.getTranslateX()), Math.round(t.getTranslateY()));
					g2.setTransform(nt);
					overlayIcon.paintIcon(RENDER_JPANEL, g2, (int)p.getX(), (int)p.getY());
					g2.setTransform(t);	// restore original settings
				}
			}
		}
	}

	/**
	 * Returns true if this node will have a document icon.  This will be the case if
	 * this node's artifact has attached documents.
	 * Also the user can turn off attachment icons in the general options.
	 * @return true if this node will have a document icon
	 */
	public boolean shouldRenderDocumentIcon() {
		return RENDER_ATTACHMENT_ICON && (ATTACHMENT_ICON != null) && !isOpen() && isShowAttachments() &&
			(getArtifact() != null) && getArtifact().hasDocuments();
	}

	/**
	 * Returns true if this node will have an annotation icon. This will be the case if
	 * this node's artifact has annotations (either Shrimp or Protege annotations).
	 * Also the user can turn off attachment icons in the general options.
	 * @return true if this node will have an annotation icon
	 */
	public boolean shouldRenderAnnotationIcon() {
		return RENDER_ATTACHMENT_ICON && (ANNOTATION_ICON != null) && !isOpen() && isShowAttachments() &&
			(getArtifact() != null) && getArtifact().hasAnnotations();
	}

	/**
	 * Renders a small document icon in the bottom left corner of the node only if the node
	 * has attached documents.
	 */
	private void renderDocumentIcon(Graphics2D g2, Icon icon) {
		Rectangle2D bounds = getActualBounds(g2);
		double size = Math.min(bounds.getWidth(), bounds.getHeight());
		if (size > 32) {
			AffineTransform t = g2.getTransform();	// save the graphics settings
			// have to change the transformation - we only want to translate, no scaling
			// this way the icon is always the same size
			AffineTransform nt = new AffineTransform();
			// need to round to an integer otherwise the image gets scaled
			nt.setToTranslation(Math.round(t.getTranslateX()), Math.round(t.getTranslateY()));
			g2.setTransform(nt);

			int x = (int)bounds.getWidth() - 18;
			int y = (int)bounds.getHeight() - 20;
			icon.paintIcon(RENDER_JPANEL, g2, x, y);

			g2.setTransform(t);	// restore original settings
		}
	}

	/**
	 * Returns true if this node will have a plus icon. This will happen if this node has
	 * children and if it's closd and if the display bean isn't inverted.
	 * Also the user can turn off plus icons in the general options.
	 * @return true if this node will have a plus icon
	 */
	public boolean shouldRenderPlusIcon() {
		// if the cprels are inverted then don't render since it is backwards!
		return RENDER_PLUS_ICON && !isOpen() && isOpenable() && !displayBean.isInverted();
	}

	/**
	 * Renders a plus icon in the top left corner of the node only if the node
	 * is closed and if it has children and its global size is larger than the icon.
	 */
	private void renderPlusIcon(Graphics2D g2) {
		Rectangle2D bounds = getActualBounds(g2);
		double size = Math.min(bounds.getWidth(), bounds.getHeight());
		if (size >= MIN_GLOBAL_NODE_SIZE) {
			AffineTransform t = g2.getTransform();	// save the graphics settings

			// have to change the transformation - we only want to translate, no scaling
			// this way the icon is always the same size
			AffineTransform nt = new AffineTransform();
			// need to round to an integer otherwise the image gets scaled
			nt.setToTranslation(Math.round(t.getTranslateX()), Math.round(t.getTranslateY()));
			g2.setTransform(nt);

			boolean large = size >= 100;
			int w = (large ? 12 : 10);
			int h = w;
			// half outside top left corner
			int x = -(w/2);
			int y = -(h/2);
			renderPlus(g2, x, y, w, h);

			g2.setTransform(t);	// restore original settings
		}
	}

	/**
	 * Renders a plus sign.
	 */
	private static void renderPlus(Graphics2D g2, int x, int y, int w, int h) {
		int w2 = w - 6;
		int x2 = x + 3, y2 = y + 3;
		int xm = x2 + (w2 / 2);
		int ym = y2 + (w2 / 2);

		// save the graphics settings
		Paint paint = g2.getPaint();
		Object antiAliasing = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Composite comp = g2.getComposite();
		Stroke stroke = g2.getStroke();
		// set the values for the plus icon
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		g2.setStroke(new BasicStroke(1));

		Shape shape;
		if (ShrimpColorConstants.USE_ECLIPSE_COLORS) {
			shape = new Ellipse2D.Double(x + 1, y + 1, w - 2, w - 2);
		} else {
			shape = new Rectangle2D.Double(x + 1, y + 1, w - 2, w - 2);
		}
		// fill the shape
		g2.setPaint(new GradientPaint(x + 1, y + 1, ShrimpColorConstants.getPlusStartColor(),
									  x + w - 1, y + w - 1, ShrimpColorConstants.getPlusEndColor()));
		g2.fill(shape);
		// draw the plus
		g2.setColor(ShrimpColorConstants.getPlusColor());
		g2.drawLine(x2, ym, x2 + w2, ym);
		g2.drawLine(xm, y2, xm, y2 + w2);
		// draw the border
		g2.setColor(ShrimpColorConstants.getPlusBorderColor());
		g2.draw(shape);

		// restore the graphics settings
		g2.setPaint(paint);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliasing);
		g2.setComposite(comp);
		g2.setStroke(stroke);
	}

	/**
	 * Gets on-node text to display or null if this option is not enabled
	 * or there is not text in the specified attribute
	 */
	private String getOnNodeText() {
		String onNodeText = null;
		if (isGrouped) {
			onNodeText = displayBean.getDataDisplayBridge().getAnnotationProperty(artifact);
		}
		else {
			String displayTextAttributeName = getOnNodeDisplayAttribute();
			if (displayTextAttributeName != null) {
				String displayText = (String)getArtifact().getAttribute(displayTextAttributeName);
				if (displayText != null && !"".equals(displayText)) {
					onNodeText = displayText;
				}
			}
		}
		return onNodeText;
	}

	private static final double X_LABEL_PADDING = PADDING * 3;

	/**
	 * Render a label on this node (off node labelling is done by the node label object)
	 * @param g2
	 * @param border
	 */
	private void renderOnNodeLabel(Graphics2D g2, NodeBorder border) {
		Point2D.Double labelLocation = null;
		Rectangle2D labelBounds = getLabelBounds();
		g2.setPaint(GraphicsUtils.getTextColor(getColor()));

		// @tag Shrimp.labelling
		// Use the label's (potentially elided) text if it is available
		String text = this.name;
		PShrimpNodeLabel label = (PShrimpNodeLabel) displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, false);
		if (label != null) {
			text = label.getText();
		}
		Font font;
		double labelX = 0;
		double labelY = 0;
		double textStartY = 0;

		if (labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE)) {
			font = getLabelFont();
			labelX = innerBounds.getX() + 0.5*innerBounds.getWidth() - 0.5*labelBounds.getWidth(); // center label horizontally
			labelY = innerBounds.getY() - 0.5*labelBounds.getHeight(); // center label vertically
			if (labelY < innerBounds.getMaxY()) {
				g2.setFont(font);
				g2.drawString(text, (float)labelX, (float)labelY);
				labelLocation = new Point2D.Double(labelX, labelY);
			}
			textStartY = labelY + labelBounds.getHeight();
		} else if (DisplayConstants.LABEL_MODE_FIT_TO_NODE.equals(labelMode)) {
			font = getAdjustedLabelFont(this.magnification, g2);
			text = adjustText(g2, font, text);
			Rectangle2D stringBounds = g2.getFontMetrics(font).getStringBounds(text, g2);
			Rectangle2D.Double outerBounds = getOuterBounds();
			double textHeight = stringBounds.getHeight();
			double oldY = innerBounds.y;
			innerBounds.y = outerBounds.y + (1.5 * textHeight);
			innerBounds.height -= (innerBounds.y - oldY);
			labelX = innerBounds.getX() + 0.5*innerBounds.getWidth() - 0.5*stringBounds.getWidth();
			labelY = innerBounds.getY() - 0.5*textHeight;
			if (labelY < innerBounds.getMaxY()) {
				g2.setFont(font);
				g2.drawString(text, (float) labelX, (float) labelY);
				labelLocation = new Point2D.Double(labelX, labelY);
			}
			textStartY = labelY + labelBounds.getHeight();
		} else { // labelMode.equals(DisplayConstants.LABEL_MODE_WRAP_TO_NODE)
			font = getAdjustedLabelFont(this.magnification, g2);
			String[] textLines = splitText(g2, font, text);
			Rectangle2D stringBounds = g2.getFontMetrics(font).getStringBounds(text, g2);
			Rectangle2D.Double outerBounds = getOuterBounds();
			double textHeight = stringBounds.getHeight();
			double oldY = innerBounds.y;
			innerBounds.y = outerBounds.y + (1.5 * textHeight);
			innerBounds.height -= (innerBounds.y - oldY);
			// have to shift the text to the right by the icon width
			double iconWidth = (icon != null ? icon.getIconWidth() : 0);
			labelX = innerBounds.getX() + X_LABEL_PADDING + iconWidth;
			g2.setFont(font);
			for (int i = 0; i< textLines.length; i++) {
				double offset = textHeight*i - (0.25*textHeight);
				labelY = innerBounds.getY() + offset;
				if (labelY < innerBounds.getMaxY()) {
					g2.drawString(textLines[i], (float)labelX, (float)labelY);
					if (i == 0) {
						labelLocation = new Point2D.Double(labelX, labelY);
					}
				} else {
					break;
				}
			}
			textStartY = innerBounds.getY() + stringBounds.getHeight()*(textLines.length) - PADDING;
		}

		// @tag Shrimp.SourceAutoDisplay
		// TODO Should this be displayed even for off-node labels?
		String onNodeText = getOnNodeText();
		if (onNodeText != null && onNodeText.length() > 0) {
			renderOnNodeText(g2, font, textStartY, onNodeText);
		}

		// render an icon if there is one
		if ((icon != null) && (labelLocation != null)) {
			float fontHeight = font.getSize2D();
			double w = icon.getIconWidth();
			double h = icon.getIconHeight();
			int x = (int) (labelLocation.getX() - w - 4);
			int y = (int) (labelLocation.getY() - fontHeight + (fontHeight - h) / 2);

			if (DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE.equals(labelMode)) {
				icon.paintIcon(RENDER_JPANEL, g2, x, y);
			} else {
				// change the transformation to make large nodes have smaller icons and small nodes have larger icons
				AffineTransform t = g2.getTransform();
				Rectangle2D newBounds = t.createTransformedShape(new Rectangle2D.Double(x, y, w, h)).getBounds2D();
				double scaleX = Math.sqrt(t.getScaleX());
				double scaleY = Math.sqrt(t.getScaleY());
				// need to round to an integer otherwise the image gets scaled
				int transX = (int)(newBounds.getMaxX() - (w * scaleX));
				int transY = (int)(newBounds.getY() + (newBounds.getHeight() - (h * scaleY)) / 2);
				AffineTransform nt = new AffineTransform(scaleX, 0, 0, scaleY, transX, transY);
				g2.setTransform(nt);
				icon.paintIcon(RENDER_JPANEL, g2, 0, 0);
				g2.setTransform(t);
			}
		}
	}

	/**
	 * Return true of the property is set to true for auotmatic display of source code
	 */
	private String getOnNodeDisplayAttribute() {
		Properties props = ApplicationAccessor.getProperties();
		return props.getProperty(ShrimpProject.PROPERTY_KEY_NODE_DISPLAY_TEXT_ATTRIBUTE_TYPE, null);
	}

	/**
	 * Render a block of text on a node. This assumes that the text is formated
	 * using HTML markup (e.g. <BR> for line breaks)
	 */
	private void renderOnNodeText(Graphics2D g2, Font font, double startY, String text) {
		String[] textLines = splitHtmlText(text);
		Font newFont = calculateNewFont(font, textLines, g2);
		Rectangle2D stringBounds = g2.getFontMetrics(newFont).getStringBounds(textLines[0], g2);
		g2.setFont(newFont);
		double labelX = innerBounds.getX() + X_LABEL_PADDING;
		boolean offNode = false;
		if (startY < innerBounds.getMaxY()) { // don't render off node
			double offset = -PADDING;
			for (int i=0; i< textLines.length && !offNode; i++) {
				offset += stringBounds.getHeight();
				double labelY = startY + offset;
				if ((labelY+stringBounds.getHeight()) < innerBounds.getMaxY()) {
					g2.drawString(textLines[i], (float)(labelX), (float)(labelY));
				} else {
					if (i > 0 && textLines[i].trim().length() > 0) {
						g2.drawString("...", (float)(labelX), (float)(labelY));
					}
					offNode = true;
				}
			}
		}
	}

	/**
	 * Calculate a font size that will allow textLines to fit
	 * within the bounds of the current node. This only shrinks
	 * the font, never expands it.
	 * @param currentFont
	 * @param textLines
	 * @param g2
	 */
	private Font calculateNewFont(Font currentFont, String[] textLines, Graphics2D g2) {
		double adjustmentRatio = 1.0;
		Font newFont = currentFont;

		for (int i=0; i< textLines.length; i++) {
			Rectangle2D stringBounds = g2.getFontMetrics(currentFont).getStringBounds(textLines[i], g2);
			double newRatio = this.getWidth()/stringBounds.getWidth();
			if (newRatio < adjustmentRatio) {
				adjustmentRatio = newRatio;
			}
		}
		if (adjustmentRatio < 1.0) {
			int newPointSize = (int)(currentFont.getSize()*adjustmentRatio);
			newFont = new Font(currentFont.getName(), currentFont.getStyle(), newPointSize);
		}
		return newFont;
	}


	/**
	 * Split text based on HTML line breaks
	 * @param text
	 */
	private String[] splitHtmlText(String text) {
		ArrayList lines = new ArrayList();
		while (text.length() > 0 && text.indexOf("<br>") > 0) {
			int endIndex = text.indexOf("<br>");
			lines.add(text.substring(0, endIndex).trim());
			int startIndex = endIndex+4;
			if (startIndex < text.length()) {
				text = text.substring(endIndex+4);
			}
			else {
				text = "";
			}
		}

		if (text.length() > 0) {
			lines.add(text.trim());
		}
		return (String[])lines.toArray(new String[lines.size()]);
	}

	/**
	 * Split the specified text into lines to fit within the node bounds
	 * @param g2
	 * @param font
	 * @param text
	 */
	private String[] splitText(Graphics2D g2, Font font, String text) {
		ArrayList textLines = new ArrayList();
		String remainingText = text;
		Rectangle2D stringBounds = g2.getFontMetrics(font).getStringBounds(remainingText, g2);
		int charsInLine = (int)(text.length()*(innerBounds.getWidth()-X_LABEL_PADDING)/stringBounds.getWidth());
		if (charsInLine > 0) {
			boolean finished = stringBounds.getWidth() <= innerBounds.getWidth();
			while (!finished) {
				String lineText = getNextLine(remainingText, charsInLine);
				if (lineText.length() < remainingText.length()) { // not the last line
					textLines.add(lineText);
					remainingText = remainingText.substring(lineText.length());
				}
				else {
					finished = true;
				}
			}
			textLines.add(remainingText);
		} else {
			textLines.add(""); // node not large enough for a label
		}
		return (String[])textLines.toArray(new String[textLines.size()]);
	}


	/**
	 * Get a line of text from fullText, splitting the line at non-lowercase characters
	 * @param fullText
	 * @param charsInLine
	 */
	private String getNextLine(String fullText, int charsInLine) {
		if (fullText.length() > charsInLine) {
			String line = fullText.substring(0, charsInLine);
			for (int i=line.length()-1; i>0; i--) {
				char ch = line.charAt(i);
				if (!Character.isLowerCase(ch)) {
					for (int j=i-1; j>0; j--) {
						ch = line.charAt(j);
						if (Character.isLowerCase(ch)) {
							return line.substring(0, j+1);
						}
					}
				}
			}
			return line; // no suitable line break found
		} else {
			return fullText;
		}
 	}

	/**
	 * Adjust text to ensure it fits inside this node
	 * @tag Shrimp.fitToNodeLabelling
	 * @param g2
	 * @param font
	 * @param text
	 */
	private String adjustText(Graphics2D g2, Font font, String text) {
		Rectangle2D stringBounds = g2.getFontMetrics(font).getStringBounds(text, g2);
		String newText = text;
		if (stringBounds.getWidth() > innerBounds.getWidth()) {
			int length = (int)(text.length()*innerBounds.getWidth()/stringBounds.getWidth());
			if (length > 5) {
				newText = text.substring(0, length-3) + "...";
			} else if (length > 0){
				newText = text.substring(0, length);
			}
		}
		return newText;
	}

	public int getAdjustedFontHeightOnCanvas(Object font) {
		int height = displayBean.getFontHeightOnCanvas(font);
		return height < MINIMUM_CANVAS_FONT_HEIGHT ? MINIMUM_CANVAS_FONT_HEIGHT : height;
	}

	/**
	 * Adjusted to ensure a minimum font display size
	 */
	private Font getAdjustedLabelFont(double magnification, Graphics2D g2) {
		Font font = getLabelFont();
		if (font == null) {
			font = (Font)displayBean.getLabelFont();
		}
		int height = displayBean.getFontHeightOnCanvas(font);
		float multiplier = ((float)MINIMUM_CANVAS_FONT_HEIGHT)/height;
		if (magnification == 0) {
			magnification = 1.0;
		}
		return font.deriveFont((float)((font.getSize()*multiplier)/magnification));
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#pickAfterChildren(edu.umd.cs.piccolo.util.PPickPath)
	 */
	protected boolean pickAfterChildren(PPickPath pickPath) {
		return currentNodeShape.getShape(getOuterBounds()).intersects(pickPath.getPickBounds());
	}

	/**
	 * @see edu.umd.cs.piccolo.nodes.PPath#paint(edu.umd.cs.piccolo.util.PPaintContext)
	 */
	protected void paint(PPaintContext paintContext) {
		renderNode(paintContext);
	}

	/* (non-Javadoc)
     * @see edu.umd.cs.piccolo.PNode#paintAfterChildren(edu.umd.cs.piccolo.util.PPaintContext)
     */
    protected void paintAfterChildren(PPaintContext paintContext) {
	    if (!shouldRender(paintContext)) {
			return;
		}
        super.paintAfterChildren(paintContext);
        //System.out.println("currentlyAnimatingDoors: " + currentlyAnimatingDoors);
        if (currentlyAnimatingDoors) {
        	Graphics2D g2 = paintContext.getGraphics();
        	g2.setColor(getColor());
        	g2.fillRect((int)leftDoor.x, (int)leftDoor.y, (int)leftDoor.width, (int)leftDoor.height);
        	g2.fillRect((int)rightDoor.x, (int)rightDoor.y, (int)rightDoor.width, (int)rightDoor.height);
    		Color borderColor = Color.GRAY;
    		float absoluteStrokeWidth = 1.0f;
        	g2.setColor(borderColor);
        	g2.setStroke(new BasicStroke (absoluteStrokeWidth));
        	g2.drawRect((int)leftDoor.x, (int)leftDoor.y, (int)leftDoor.width, (int)leftDoor.height);
        	g2.drawRect((int)rightDoor.x, (int)rightDoor.y, (int)rightDoor.width, (int)rightDoor.height);
        }
    }

    private void renderImage(PPaintContext paintContext) {
		double mag = paintContext.getScale();
		Graphics2D g2 = paintContext.getGraphics();
		double innerX = innerBounds.getX();
		double innerY = innerBounds.getY();
		double innerW = innerBounds.getWidth();
		double innerH = innerBounds.getHeight();

		// The node is too big for the image when the node's width or height on screen
		// is greater than maxImageScale times the image's original width or height.
		// If this is the case, we must draw the image smaller than the node.
		double nodeWOnScreen = innerW * mag;
		double nodeHOnScreen = innerH * mag;

		double origImageWidth = displayImage.getWidth(RENDER_JPANEL);
		double origImageHeight = displayImage.getHeight(RENDER_JPANEL);

		double imageWScaled = innerW;
		double imageHScaled = innerH;
		double originalImageHToWRatio = origImageHeight/origImageWidth;
		double nodeHToWRatio = innerH/innerW;
		if (originalImageHToWRatio > nodeHToWRatio) {
			// the image size will be restricted by height first
			imageHScaled = innerH;
			// if the node is taller than the thumbnail then get the full size image
			if (nodeHOnScreen > origImageHeight) {
				origImageWidth = displayImage.getWidth(RENDER_JPANEL);
				origImageHeight = displayImage.getHeight(RENDER_JPANEL);
			}
			if (LIMIT_IMAGE_SCALE && (nodeHOnScreen > origImageHeight)) {
				imageHScaled = origImageHeight / mag;
			}
			imageWScaled = imageHScaled / originalImageHToWRatio;
		} else {
			// the image size will be restricted by width first
			imageWScaled = innerW;
			// if the node is wider than the thumbnail then get the full size image
			if (nodeWOnScreen > origImageWidth) {
				origImageWidth = displayImage.getWidth(RENDER_JPANEL);
				origImageHeight = displayImage.getHeight(RENDER_JPANEL);
			}
			if (LIMIT_IMAGE_SCALE && (nodeWOnScreen > origImageWidth)) {
				imageWScaled = origImageWidth / mag;
			}
			imageHScaled = imageWScaled * originalImageHToWRatio;
		}
		double imageXScaled = innerX + (innerW - imageWScaled)/2.0d;
		double imageYScaled = innerY + (innerH - imageHScaled)/2.0d;
		g2.drawImage(displayImage, (int)imageXScaled, (int)imageYScaled, (int)imageWScaled, (int)imageHScaled, RENDER_JPANEL);
	}

	/**
	 * Compute label and inner bounds
	 */
	protected void recomputeInnerBounds() {
		try {
			// @tag Shrimp.labelling
			// Use the label's (potentially elided) text if it is available
			String text = this.name;
			ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, false);
			if (label != null) {
				text = label.getText();
			}

			Graphics2D graphics = (Graphics2D)getPNestedDisplayBean().getPCanvas().getGraphics();
			Font font;
			if (getLabelMode() != null &&
					(getLabelMode().equals(DisplayConstants.LABEL_MODE_FIT_TO_NODE) ||
		     	     getLabelMode().equals(DisplayConstants.LABEL_MODE_WRAP_TO_NODE))) {
				font = getAdjustedLabelFont(magnification, graphics);
			} else {
				font = getLabelFont();
			}
			labelBounds = getLabelBounds(text, font, graphics);

			// Adjust for maximum border width
			// this happens when the node is highlighted
			float borderWidth = new NodeBorder(magnification,
					this.outerBorderColor, this.outerBorderStyle,
					this.innerBorderColor, this.innerBorderStyle,
					true, mouseOver, equivalentNodeSelected).getMaximumWidth();

			Rectangle2D.Double shapeInnerBounds = currentNodeShape.getInnerBounds(getOuterBounds());
			double x = shapeInnerBounds.getX() + borderWidth;
			double y = shapeInnerBounds.getY() + labelBounds.getHeight() + borderWidth;
			double w = shapeInnerBounds.getWidth() - borderWidth*2;
			double h = shapeInnerBounds.getHeight() - labelBounds.getHeight() - borderWidth*2;

			innerBounds = new Rectangle2D.Double(x, y, w, h);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the label bounds based on the specified text and current label font.
	 * Note that this is independent of the available inner bounds space.
	 * @param text
	 * @return
	 */
	private Rectangle2D.Double getLabelBounds(String text, Font font, Graphics2D g2) {
		double labelX = 0;
		double labelY = 0;
		double labelW = 0;
		double labelH = 20;

		if (g2 != null && font != null && g2.getFontMetrics(font) != null) {
			Rectangle2D stringBounds = g2.getFontMetrics(font).getStringBounds(text, g2);
			labelX = stringBounds.getX(); //
			labelY = stringBounds.getY() + 3; // a negative number, y=0 is at the baseline of the string
			labelW = stringBounds.getWidth();
			labelH = stringBounds.getHeight();
			if (icon != null) {
				labelW = labelW + icon.getIconWidth();
				labelH = Math.max(icon.getIconHeight(), labelH) + 3;
			}
		}
		return new Rectangle2D.Double(labelX, labelY, labelW, labelH);
	}

	public Image getDisplayImage() {
        return displayImage;
    }

	public void setDisplayImage(Image image) {
		if (displayImage != null) {
			displayImage.flush();
		}
		displayImage = image;
	}

	public NodeImage getNodeImage() {
        return nodeImage;
    }

	public void addTerminal(ShrimpTerminal t) {
		terminals.add(t);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#removeTerminal(ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpTerminal)
	 */
	public void removeTerminal(PShrimpTerminal terminal) {
		terminals.remove(terminal);
	}


	/**
	 * @param id
	 * @return The terminal attached to this node with the given id (null is returned if there isn't one)
	 */
	public ShrimpTerminal getTerminal(String id) {
		for (int i = 0; i < terminals.size(); ++i) {
			final ShrimpTerminal terminal = (ShrimpTerminal) terminals.get(i);
			String terminalId = terminal.getId();
			if (terminalId != null && id.equals(terminalId)) {
				return terminal;
			}
		}
		return null;
	}

	public Vector getTerminals() {
		return terminals;
	}

	public double getTerminalAttachPoint(double terminalPositionAngle, Point2D.Double attachPoint) {
		double terminalTheta = currentNodeShape.getTerminalAttachPoint(terminalPositionAngle, getGlobalOuterBounds(), getLocalToGlobalTransform(new PAffineTransform()), attachPoint);
		return terminalTheta;
	}


	/**
	 * Notifies the arcs and labels attached to this node that they need to update the location they are drawn at
	 */
	public void firePositionChangedEvent() {
		firePositionChangedEventRecursive(new HashSet());
	}

	private void firePositionChangedEventRecursive(Set nodesVisitedSoFar) {
		if (nodesVisitedSoFar.contains(this)) {
			return; // to stop infinite loops
		}
		nodesVisitedSoFar.add(this);
		for (int i = 0; i < shrimpDisplayObjectListeners.size(); i++) {
			ShrimpDisplayObjectListener l = (ShrimpDisplayObjectListener)shrimpDisplayObjectListeners.elementAt(i);
			l.displayObjectPositionChanged();
		}

		//fire Position Changed Event for each child
		if (!(displayBean instanceof PFlatDisplayBean)) { // dont bother doing this if using flat display bean
			Vector childNodes = displayBean.getDataDisplayBridge().getChildNodes(this);
			for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
				PShrimpNode childNode = (PShrimpNode) iter.next();
				childNode.firePositionChangedEventRecursive(nodesVisitedSoFar);
			}
		}
	}

	public void recomputeCentrePoint() {
		recomputeCentrePointRecursive(new HashSet());
	}

	public void recomputeCentrePointRecursive(Set nodesVisitedSoFar) {
		if (nodesVisitedSoFar.contains(this)) {
			return; // to stop infinite loops
		}
		nodesVisitedSoFar.add(this);
		Rectangle2D.Double globalBounds = getGlobalOuterBounds();
		double x = globalBounds.getX();
		double y = globalBounds.getY();
		double w = globalBounds.getWidth();
		double h = globalBounds.getHeight();

		centrePoint.setLocation(x + w / 2.0, y + h / 2.0);

		// update "arcless" terminals if any
		// terminals with arcs will be notified by the arcs attached to them
		for (Iterator iter = terminals.iterator(); iter.hasNext();) {
			ShrimpTerminal st = (ShrimpTerminal) iter.next();
			if (st.hasNoArcs()) {
				st.computeTerminalPosition();
			}
		}

		//Update the centre point of child nodes, only if not using flat display bean
		if (!(displayBean instanceof PFlatDisplayBean)) {
			Vector childNodes = displayBean.getDataDisplayBridge().getChildNodes(this);
			for (Iterator iter = childNodes.iterator(); iter.hasNext();) {
				PShrimpNode childNode = (PShrimpNode) iter.next();
				childNode.recomputeCentrePointRecursive(nodesVisitedSoFar);
			}
		}
	}

	/** @return The centre point of this visual node. */
	public Point2D.Double getCentrePoint() {
		return centrePoint;
	}

	public void dispose () {
		if (customizedPanel != null) {
			removeCustomizedPanel();
		}
		for (Iterator iter = ((Vector)terminals.clone()).iterator(); iter.hasNext();) {
			ShrimpTerminal terminal = (ShrimpTerminal) iter.next();
			terminal.dispose();
		}
		for (Iterator iter = boundsHandles.iterator(); iter.hasNext();) {
			PShrimpNodeBoundsHandle handle = (PShrimpNodeBoundsHandle) iter.next();
			handle.dispose();
		}
		boundsHandles.clear();
		removeAllChildren();
		removeFromParent();
		removeInputEventListener(mouseOverListener);
		shrimpDisplayObjectListeners.clear();
		terminals.clear();

		displayBean = null;
		artifact = null;
		terminals = null;
		icon = null;
		customizedPanel = null;
		if (customizedPanelImage != null) {
			customizedPanelImage.flush();
			customizedPanelImage = null;
		}
		if (displayImage != null) {
			displayImage.flush();
			displayImage = null;
		}
		nodeImage.flush();

		iconProvider = null;

		panelModeToPanel.clear();
		panelModeToPanel = null;

		currentNodeShape = null;
		defaultNodeShape = null;
	}

	public void setHasBeenTransformedOnce(boolean hasBeenTransformedOnce) {
		this.hasBeenTransformedOnce = hasBeenTransformedOnce;
	}

	public boolean getHasBeenTransformedOnce() {
		return hasBeenTransformedOnce;
	}

	/**
	 * Sets the labelVisible.
	 * @param labelVisible The new visibility
	 */
	private void setLabelVisible(boolean labelVisible) {
		this.labelVisible = labelVisible;
		recomputeInnerBounds();
		repaint();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setLabelMode(java.lang.String)
	 */
	public void setLabelMode(String newMode) {
		if (labelMode == null || !labelMode.equals(newMode)) {
			this.labelMode = newMode;
			//@tag shrimp(fitToNodeLabelling)
			setLabelVisible(DisplayConstants.isLabelOnNode(newMode));
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getLabelMode()
	 */
	public String getLabelMode() {
		return labelMode;
	}


	/**
	 * Change the font of the label of this node
	 */
	public void setLabelFont(Font font) {
		if (font != null) {
			ShrimpNodeLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, false);
			if (label != null) {
				label.setFont(font);
			}
		}
		recomputeInnerBounds();
		repaint();
	}

	/**
	 * Gets the font for the label.
	 * @return Font or null
	 */
	private Font getLabelFont() {
		ShrimpNodeLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, true);
		return label.getFont();
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#setBounds(double, double, double, double)
	 */
	public boolean setBounds(double x, double y, double width, double height) {
		boolean changed = super.setBounds(x,y,width,height);
		if (changed) {
			recomputeInnerBounds();
		}
		return changed;
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#setBounds(java.awt.geom.Rectangle2D)
	 */
	public boolean setBounds(Rectangle2D newBounds) {
		return setBounds (newBounds.getX(), newBounds.getY(), newBounds.getWidth(), newBounds.getHeight());
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setOuterBounds(java.awt.geom.Rectangle2D.Double)
	 */
	public void setOuterBounds(Rectangle2D.Double newBounds) {
		setBounds(newBounds);
	}

	public Rectangle2D.Double getOuterBounds() {
		return super.getBounds();
	}

	public boolean occludesParent(PPaintContext paintContext) {
		Rectangle2D.Double parentInnerBounds = new Rectangle2D.Double();
		Rectangle2D.Double outerBounds = super.getBounds();
		PAffineTransform localToGlobalTransform = new PAffineTransform();
		try {
			PShrimpNode parent = (PShrimpNode)getParent();
			if (parent == null) {
				return false;
			}
			else if (parent.occludesParent(paintContext)) {
				return true;
			}
			parent.getLocalToGlobalTransform(localToGlobalTransform);
			localToGlobalTransform.transform(parent.getInnerBounds(), parentInnerBounds);
		} catch (ClassCastException cse) {
			return false;
		}

		this.getLocalToGlobalTransform(localToGlobalTransform);
		localToGlobalTransform.transform(getOuterBounds(), outerBounds);

		return outerBounds.height > parentInnerBounds.height;
	}

	/**
	 * Returns the artifact associated with this node.
	 */
	public Artifact getArtifact() {
		return artifact;
	}

	/**
	 * @return The shape of this node.
	 */
	public NodeShape getNodeShape() {
		return currentNodeShape;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setNodeShape(ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape)
	 */
	public void setNodeShape(NodeShape nodeShape) {
		this.currentNodeShape = nodeShape;
		recomputeInnerBounds();
		firePositionChangedEvent();
		repaint();
	}


	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getGlobalOuterBounds()
	 */
	public Rectangle2D.Double getGlobalOuterBounds() {
		Rectangle2D.Double b = getOuterBounds();
		b = GeometryUtils.transform(b, getLocalToGlobalTransform(new PAffineTransform()));
		return b;
	}

	/**
	 *
	 * @return The usable area inside this node.
	 */
	public Rectangle2D.Double getInnerBounds() {
		if (innerBounds == null || innerBounds.isEmpty()) {
			recomputeInnerBounds();
		}
		return (Rectangle2D.Double)innerBounds.clone();
 	}

	public Rectangle2D.Double getLabelBounds() {
		if (labelBounds == null || labelBounds.isEmpty()) {
			recomputeInnerBounds();
		}
		return labelBounds;
	}

	/**
	 * Returns the concatenation of the id's of this node's artifact and its parent nodes' artifacts
	 * Note: This will be a unique id.
	 */
	public long getID() {
		return id;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getParentShrimpNode()
	 */
	public ShrimpNode getParentShrimpNode() {
		ShrimpNode parentNode = null;
		Vector parents = new Vector(0);
		if (displayBean != null) {
			parents = displayBean.getDataDisplayBridge().getParentShrimpNodes(this);
		} else {
			// @tag Shrimp.PShrimpNode.NullPointerException : display bean is sometimes null?
			(new NullPointerException("** Warning - DisplayBean is null!")).printStackTrace();
		}

		if (parents.size() == 1) {
			parentNode = (ShrimpNode) parents.firstElement();
		} else if (parents.size() > 1) {
			// TODO deal with nodes that have more than one parent
			// for now we just return the first parent
		    System.out.println("PShrimpNode: more than one parent node");
			parentNode = (ShrimpNode) parents.firstElement();
		}
		return parentNode;
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getRootShrimpNode()
     */
    public ShrimpNode getRootShrimpNode() {
        ShrimpNode rootNode = null;
        if (this.getParentShrimpNode() == null) {
        	// this node is a root already if it has no parent
            rootNode = this;
        } else {
            ShrimpNode currentNode = this;
	        while (currentNode.getParentShrimpNode() != null) {
	            currentNode = currentNode.getParentShrimpNode();
	        }
	        // at this point the current node must be a root (ie. has no parent)
	        rootNode = currentNode;
        }
        return rootNode;
    }

	public String toString() {
		return getName();
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#setTransform(java.awt.geom.AffineTransform)
	 */
	public void setTransform(AffineTransform newTransform) {
		super.setTransform(newTransform);
		recomputeInnerBounds();
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setHasBeenPrunedFromTree(boolean hasBeenPrunedFromTree) {
		this.hasBeenPrunedFromTree = hasBeenPrunedFromTree;
	}

	public boolean hasBeenPrunedFromTree() {
		return hasBeenPrunedFromTree;
	}

	public boolean isPruneRoot() {
		return isPruneRoot;
	}

	public void setIsPruneRoot(boolean isPruneRoot) {
		this.isPruneRoot = isPruneRoot;
		setNodeShape(isPruneRoot ? PRUNE_ROOT_NODE_SHAPE : defaultNodeShape);
	}

	public void setIsGrouped(boolean isGrouped) {
		this.isGrouped = isGrouped;
		if (isGrouped) {
			beforeGroupNodeShape = getNodeShape();
			setNodeShape(GROUPED_NODE_SHAPE);
		} else {
			setNodeShape(beforeGroupNodeShape);
		}
	}

	public boolean isGrouped() {
		return isGrouped;
	}

	public void setHasCollapsedAncestor(boolean hasCollapsedAncestor) {
		this.hasCollapsedAncestor = hasCollapsedAncestor;
	}

	public boolean hasCollapsedAncestor() {
		return hasCollapsedAncestor;
	}

	public boolean isCollapsed() {
		return isCollapsed;
	}

	public void setIsCollapsed(boolean isCollapsed) {
		this.isCollapsed = isCollapsed;
		if (isCollapsed) {
			// TODO set in artifact attributes here?
			// artifacts are shared between ShrimpView, HV, QueryView...
			//artifact.setAttribute(AttributeConstants.NOM_ATTR_COLLAPSED, "true");
			// @tag Shrimp.collapsed.NodeShape
			beforeCollapseNodeShape = getNodeShape();
			setNodeShape(COLLAPSE_NODE_SHAPE);
		} else {
			//artifact.setAttribute(AttributeConstants.NOM_ATTR_COLLAPSED, null);
			setNodeShape(beforeCollapseNodeShape);
		}
	}

	private Component getPanel(String panelMode) {
		Component panel = (Component)panelModeToPanel.get(panelMode);
		if (panel == null) {
			panel = artifact.getCustomizedPanel(panelMode);
			if (panel != null) {
				panelModeToPanel.put(panelMode, panel);
			}
		}
		return panel;
	}

	/**
	 * Nodes are ordered first by decreasing level, then by parent id's,
	 * then by name.
	 * The objective here is to ensure that nodes lower in the tree
	 * get acted upon first, which is essential in some cases.
	 *
	 * @see java.lang.Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) {
		ShrimpNode that = (ShrimpNode) o;
		int thatLevel = that.getLevel();
		int thisLevel = this.getLevel();
		if (this.equals(that)) {
			return 0;
		} else if (thisLevel > thatLevel) {
			return -1;
		} else if (thisLevel < thatLevel) {
			return 1;
		}

		ShrimpNode thisParent = this.getParentShrimpNode();
		ShrimpNode thatParent = that.getParentShrimpNode();
		int parentIDCompare = 0;
		if (thisParent != null && thatParent != null) {
			parentIDCompare = (int) (thisParent.getID() - thatParent.getID());
		}
		if (parentIDCompare != 0) {
			return parentIDCompare;
		}
		return this.getName().compareTo(that.getName());
	}

	/**
	 *
	 * @see edu.umd.cs.piccolo.PNode#setVisible(boolean)
	 */
	public void setVisible(boolean newVisibility) {
		super.setVisible(newVisibility);
		setPickable(newVisibility);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isVisible()
	 */
	public boolean isVisible() {
		return getVisible();
	}


	/**
	 * Two PShrimpNodes are equal if they have the same id
	 */
	public boolean equals(Object obj) {
	    if (obj instanceof ShrimpNode) {
            return getID() == ((ShrimpNode)obj).getID();
	    }
        return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	    if (id > Integer.MAX_VALUE) {
	        System.err.println("Warning: Converting Node id to int even though it is greater than Integer.MAX_VALUE");
	    }
		return (int) getID();
	}

	public String getPanelMode() {
		return panelMode;
	}

	/**
	 * Sets the customizedPanel for the current node to the given mode.
	 * Note: This method will look in the corresponding Artifact for customized (Domain specific) panels.
	 * @param newMode The new customizedPanel mode.
	 */
	public void setPanelMode(String newMode) {
		if (PanelModeConstants.CLOSED.equals(newMode)) {
			if (!PanelModeConstants.CLOSED.equals(panelMode)) {
				setClosedMode();
			}
		} else if (PanelModeConstants.CHILDREN.equals(newMode)) {
			if (!PanelModeConstants.CHILDREN.equals(panelMode)) {
				setChildrenMode(/*getRelTypeToChildren(), isRelTypeToChildrenInverted()*/);
			}
		} else {
			setCustomizedPanelMode(newMode);
		}
	}

	private void setClosedMode() {
		openCloseDoors(false);
		removeCurrentMode();
		panelMode = PanelModeConstants.CLOSED;
		afterSettingPanelMode();
	}

	/**
	 * Centers the customized panel (if showing) within the PShrimpNode.
	 * @param onlyIfVisible if true and the panel is not visible nothing happens
	 */
	public void centerCustomizedPanelOnNode(boolean onlyIfVisible) {
		if (customizedPanel != null) {
			if (onlyIfVisible && !customizedPanel.isVisible()) {
				return;
			}
			PCamera camera = getPNestedDisplayBean().getPCanvas().getCamera();
			AffineTransform cameraTx = camera.getViewTransform();
			Rectangle2D.Double innerBounds = getInnerBounds();
			//transform these local inner bounds to global coordinates
			AffineTransform localToGlobalTx = getLocalToGlobalTransform(new PAffineTransform ());
			innerBounds = GeometryUtils.transform(innerBounds, localToGlobalTx);
			innerBounds = GeometryUtils.transform(innerBounds, cameraTx);

			customizedPanel.setBounds((int)innerBounds.getX(), (int)innerBounds.getY(),
							(int)innerBounds.getWidth(), (int)innerBounds.getHeight());
		}
	}

	private void setCustomizedPanelMode(String newMode) {
		if (!focusedOn && (customizedPanelImage == null)) {
			return;
		}
		Component newPanel = getPanel(newMode);
		if (newPanel == null) {
			return;
		}

		removeCurrentMode();
		panelMode = newMode;
		customizedPanel = newPanel;

		// Hack to ensure that a hand cursor shows up over hypertext links inside JEditorPanes.
		// Setting the cursor of the JEditorPane doesn't work so here we set the
		// cursor of the displayBean instead.
		if (customizedPanel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) customizedPanel).getViewport();
			Component component = viewport.getView();
			if (component instanceof JEditorPane) {
				JEditorPane editorPane = (JEditorPane) component;
				editorPane.addHyperlinkListener(new HyperlinkHandCursorAdapter());
			}
		}

		if (focusedOn) {
			// add the panel to the display, exactly where the inner bounds of this node are on the screen
			centerCustomizedPanelOnNode(false);
			setDisplayImage(null);
			customizedPanel.setVisible(true);
			getPNestedDisplayBean().getPCanvas().add(customizedPanel);
		} else {
		    setDisplayImage(customizedPanelImage);
		}

		afterSettingPanelMode();
		getPNestedDisplayBean().getPCanvas().revalidate();
		displayBean.repaint();

        // Rob's Comment
        // This is a fix for Creole, which solves the problem of the cursor not changing properly to a hyperlink "hand" cursor
        // Not sure exactly how all this works but it solves the problem and took a few hours to figure out!
		if (focusedOn) {
		    if (getPNestedDisplayBean().getPCanvas().hasFocus()) {
		        getPNestedDisplayBean().getPCanvas().transferFocus();
		    }
	        SwingUtilities.invokeLater(new Runnable () {
                public void run() {
    			    getPNestedDisplayBean().getPCanvas().requestFocus();
                }
            });
		}
	}

    public boolean isCustomPanelShowing() {
        return (customizedPanel != null) && customizedPanel.isVisible();
    }

	public void setCustomizedPanelImage(Image image) {
	    customizedPanelImage = image;
	}

	public Image getCusomizedPanelImage() {
	    return customizedPanelImage;
	}

	public void setHasFocus(boolean focusedOn) {
		if (this.focusedOn == focusedOn) {
			return;
		}
		this.focusedOn = focusedOn;

		if (!focusedOn) {
			focusLost();
		} else {
			focusGained();
		}
	}

    public boolean hasFocus() {
        return focusedOn;
    }


	// turn our panel image back into an interactive panel
	private void focusGained() {
		if (!PanelModeConstants.CHILDREN.equals(panelMode) && !PanelModeConstants.CLOSED.equals(panelMode)) {
			if (getPanel(panelMode) != null) {
				setCustomizedPanelMode(panelMode);
			}
		}
	}

	// if a panel is showing on this node then turn it into an image on the node
	// and take the panel out of the display
	private void focusLost() {
		if (!PanelModeConstants.CHILDREN.equals(panelMode) && !PanelModeConstants.CLOSED.equals(panelMode)) {
			if (customizedPanel != null) {
				customizedPanel.setVisible(false);
                createImage();
			}
		}
	}

    private void createImage() {
        int width = customizedPanel.getWidth();
        int height = customizedPanel.getHeight();
        if (customizedPanelImage != null) {
            customizedPanelImage.flush();
        }
        int reducedWidth = (int) Math.max(1.0, width*CUSTOMIZED_PANEL_IMAGE_SCALE); //must be at least 1 pixel wide
        int reducedHeight = (int) Math.max(1.0, height*CUSTOMIZED_PANEL_IMAGE_SCALE); //must be at least 1 pixel tall
        customizedPanelImage = customizedPanel.createImage(reducedWidth, reducedHeight);
        Graphics2D g = (Graphics2D)customizedPanelImage.getGraphics();
        AffineTransform scale = AffineTransform.getScaleInstance(CUSTOMIZED_PANEL_IMAGE_SCALE, CUSTOMIZED_PANEL_IMAGE_SCALE);
        g.transform(scale);

        // Calling customizedPanel.paint(g) sometimes causes Creole to freeze
        // instead call paint on the viewport component and it seems to fix the problem
        //customizedPanel.paint(g); // paint the customized panel to the image
		if (customizedPanel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) customizedPanel).getViewport();
			Component component = viewport.getView();
			component.paint(g);
		}

		// don't need the panel anymore
        removeCustomizedPanel();

        // create a grayed-out version of the panel
        customizedPanelImage = GraphicsUtils.createFadedImage(customizedPanelImage);

        setDisplayImage(customizedPanelImage);
        //repaint();
        displayBean.repaint();
    }

	private void removeCustomizedPanelImage() {
		if (customizedPanelImage != null) {
			customizedPanelImage.flush();
			customizedPanelImage = null;
			setDisplayImage(null);
		}
	}

	private void removeCustomizedPanel() {
		if (customizedPanel != null) {
            customizedPanel.setVisible(false);
			getPNestedDisplayBean().getPCanvas().remove(customizedPanel);
			customizedPanel = null;
		}
	}

	private void afterSettingPanelMode() {
		setIsOpen(panelMode.equals(PanelModeConstants.CHILDREN));
        repaint();
	}

	public void setChildrenMode(/*String relTypeToChildren, boolean inverted*/) {
		int numChildren = displayBean.getDataDisplayBridge().getChildNodesCount(this);
		if (numChildren == 0) {
			return;
		}

		boolean showProgress = numChildren > 100;

		boolean displayVisible = displayBean.isVisible();
		String oldSubtitle = ProgressDialog.getSubtitle();
		String oldNote = ProgressDialog.getNote();
		String oldPanelMode = panelMode;

		ApplicationAccessor.waitCursor();
		try {
			// if there are many children, show progress monitor and hide display
			if (showProgress) {
			    ProgressDialog.showProgress();
			    ProgressDialog.setSubtitle("Showing children of " + getName());
				displayBean.setVisible(false);
				ProgressDialog.setNote("Creating children...");
			}

			Vector childNodes = displayBean.getDataDisplayBridge().getChildNodes(this, true);
			removeCurrentMode();
			panelMode = PanelModeConstants.CHILDREN;

			int count = 0;
			Vector addedChildren = new Vector(childNodes.size());
			Vector childrenSetVisible = new Vector(childNodes.size());
			for (Iterator iter = childNodes.iterator(); iter.hasNext() && !ProgressDialog.isCancelled();) {
				ShrimpNode childNode = (ShrimpNode) iter.next();
				//childNode.setRelTypeToParents(relTypeToChildren, inverted);
				if (!childrenAdded) {
					displayBean.addObject(childNode);
					addedChildren.add(childNode);
				}
				displayBean.setVisible(childNode, true, false);

				childrenSetVisible.add(childNode);
				count++;
				if (showProgress && count % 10 == 0) {
				    ProgressDialog.setNote(count + " of " + numChildren + " children added.");
				}
			}

			// if progress is cancelled
			if (showProgress && ProgressDialog.isCancelled()) {
				panelMode = oldPanelMode;
				ProgressDialog.setSubtitle("Removing children ...");
				// hide any nodes set visible
				for (Iterator iter = childrenSetVisible.iterator(); iter.hasNext();) {
					ShrimpNode childNode = (ShrimpNode) iter.next();
					displayBean.setVisible(childNode, false, false);
				}
				//remove any nodes added
				// remove any arcs added as a result of adding the nodes
				for (Iterator iter = addedChildren.iterator(); iter.hasNext();) {
					ShrimpNode childNode = (ShrimpNode) iter.next();
					displayBean.removeObject(childNode);
				}
			}

			if (!showProgress || !ProgressDialog.isCancelled()) {
				if (showProgress) {
				    ProgressDialog.setNote("Laying out children...");
				}

				Properties props = ApplicationAccessor.getProperties();
				if (!childrenAdded) {
					String customLayoutMode = (String) getArtifact().getAttribute(AttributeConstants.NOM_ATTR_LAYOUT);
					if (customLayoutMode != null) {
						displayBean.setLayoutMode(childNodes, customLayoutMode, false, false);
					} else {
						String defaultLayoutMode = props.getProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE,
								DisplayBean.PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);
						displayBean.setLayoutMode(childNodes, defaultLayoutMode, false, false);
					}
				}

				childrenAdded = true;
				afterSettingPanelMode();
			}

			if (showProgress) {
	            ProgressDialog.setSubtitle(oldSubtitle);
	            ProgressDialog.setNote(oldNote);
			    ProgressDialog.tryHideProgress();
				displayBean.setVisible(displayVisible);
			}
		} finally {
			ApplicationAccessor.defaultCursor();
		}
		openCloseDoors(true);
	}

	// an experiment
	private void openCloseDoors(boolean open) {
	    if (!DEFAULT_ANIMATE_DOORS) {
	    	return;
	    }
	    if (currentlyAnimatingDoors) {
	    	return;
	    }

		currentlyAnimatingDoors = true;
		Rectangle2D.Double innerBounds = getInnerBounds();
		double closedDoorWidth = innerBounds.width/2.0;
		double initialDoorWidth = open ? closedDoorWidth : 1.0;
		leftDoor.setFrame(innerBounds.x, innerBounds.y, initialDoorWidth, innerBounds.height);
		rightDoor.setFrame(innerBounds.width - initialDoorWidth, innerBounds.y, initialDoorWidth, innerBounds.height);
		double numSteps = 5.0;
		double stepSize = closedDoorWidth / numSteps;
		int stepDuration = 30; //ms
		double newWidth = initialDoorWidth;
        Rectangle2D.Double repaintBounds = GeometryUtils.transform(getGlobalOuterBounds(),
        		getPNestedDisplayBean().getPCanvas().getCamera().getViewTransform());
        PCanvas canvas = getPNestedDisplayBean().getPCanvas();
		while (newWidth > 0 && newWidth <= closedDoorWidth) {
            leftDoor.setFrame(leftDoor.x, leftDoor.y, newWidth, leftDoor.height);
            rightDoor.setFrame(innerBounds.width - newWidth, rightDoor.y, newWidth, rightDoor.height);
            // eventually causes paintAfterChildren to be called where doors are actually drawn on screen
            canvas.paintImmediately((int)repaintBounds.x, (int)repaintBounds.y,
            		(int)repaintBounds.width, (int)repaintBounds.height);
		    try {
                Thread.sleep(stepDuration);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                break;
            }
            newWidth = leftDoor.getWidth() + (open ? -stepSize : stepSize);
 		}
		currentlyAnimatingDoors = false;
	}

	private void removeCurrentMode() {
		if (PanelModeConstants.CHILDREN.equals(panelMode)) {
			Vector children = displayBean.getDataDisplayBridge().getChildNodes(this);
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				ShrimpNode child = (ShrimpNode) iter.next();
				displayBean.setVisible(child, false, false);
			}
		} else {
			removeCustomizedPanel();
			removeCustomizedPanelImage();
		}
	}

	public boolean haveChildrenBeenAdded() {
		return childrenAdded;
	}

	public void setChildrenAdded(boolean b) {
		childrenAdded = b;
	}

	public void setIsHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	 	if (DEFAULT_SHOW_RESIZE_HANDLES && isResizable() && highlighted) {
			addBoundsHandlesTo();
		} else {
			removeBoundsHandlesFrom();
		}
	}


	public boolean isHighlighted() {
		return highlighted;
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setEquivalentNodeSelected(boolean)
     */
    public void setEquivalentNodeSelected(boolean equivalentNodeSelected) {
        this.equivalentNodeSelected = equivalentNodeSelected;
        repaint();
    }

	/**
	 * @see ShrimpNode#setIsMouseOver(boolean)
	 */
	public void setIsMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
		showHideAboveNodeLabel();
		repaint();
	}

	/**
	 * This method will display the {@link PShrimpNodeLabel} for this node
	 * if the mouse is currently over this node and the label mode is above node and the
	 * label isn't visible.  When the mouse moves off this node the label will be re-hidden.
	 */
	private void showHideAboveNodeLabel() {
		// Show the label when in "On Node" mode and the node is small
		boolean onNodeOK = DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE.equals(labelMode) && (getMagnification() < 0.55);
		// Or if the model is above node fixed/level
		boolean labelModeOK = !DisplayConstants.isLabelOnNode(labelMode) || onNodeOK;
		if (labelModeOK) {
			PShrimpLabel label = (PShrimpLabel) displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, true);
			if (label != null) {
				if (mouseOver && !label.isVisible()) {
					// display the label and show it
					displayBean.addObject(label);
					label.setVisible(true);
					// must call this afer setVisible is called
					label.mouseOver(getColor());
					// this correctly positions the label
					label.displayObjectPositionChanged(ShrimpUtils.toVector(this));
				} else if (!mouseOver && label.isVisible()) {
					// this line causes the label to be hidden permanently - we don't want this
					//displayBean.removeObject(label);
					if (label.isHovering()) {
						label.mouseOut();
						label.setVisible(false);
					}
				}
			}
		}
	}

	private void addBoundsHandlesTo() {
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createEastLocator(this), displayBean, this));
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createWestLocator(this), displayBean, this));
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createNorthLocator(this), displayBean, this));
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createSouthLocator(this), displayBean, this));
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createNorthEastLocator(this), displayBean, this));
		// this handle hides the plus icon
		if (!shouldRenderPlusIcon()) {
			boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createNorthWestLocator(this), displayBean, this));
		}
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createSouthEastLocator(this), displayBean, this));
		boundsHandles.add(new PShrimpNodeBoundsHandle(PBoundsLocator.createSouthWestLocator(this), displayBean, this));

		for (Iterator iter = boundsHandles.iterator(); iter.hasNext();) {
			PBoundsHandle boundsHandle = (PBoundsHandle) iter.next();
			this.addChild(boundsHandle);
		}
		repaint();
	}

	public void removeBoundsHandlesFrom() {
		if ((boundsHandles != null) && !boundsHandles.isEmpty()) {
			this.removeChildren(boundsHandles);
			repaint();
			boundsHandles.clear();
		}
	}

	protected void setIsOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public void setColor(Color color) {
		closedColor = color;
		repaint();
	}

	public Color getColor() {
		return closedColor;
	}

	protected Color getOpenColor() {
		return GraphicsUtils.fadeColor(closedColor);
	}

	public void setOuterBorderColor(Color color) {
		this.outerBorderColor = color;
	}

	public void setOuterBorderStyle(String style) {
		this.outerBorderStyle = style;
	}

	public void setInnerBorderColor(Color color) {
		this.innerBorderColor = color;
	}

	public void setInnerBorderStyle(String style) {
		this.innerBorderStyle = style;
	}

	/**
	 * Adds a ShrimpNodeListener to the objects
	 * 'interested' in the state of this visual node
	 * @param l The listener to be added.
	 */
	public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener l) {
		shrimpDisplayObjectListeners.addElement(l);
	}

	/**
	 * Removes a specified ShrimpNodeListener from this visual node.
	 */
	public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener l) {
		shrimpDisplayObjectListeners.removeElement(l);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getIcon()
	 */
	public Icon getIcon() {
		return this.icon;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setIcon(javax.swing.Icon)
	 */
	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getOverlayIconProvider()
	 */
	public IconProvider getOverlayIconProvider() {
		return iconProvider;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setOverlayIconProvider(IconProvider)
	 */
	public void setOverlayIconProvider(IconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setName(java.lang.String)
	 */
	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			System.err.println("Warning - null node name!");
			name = "null";
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#setResizable(boolean)
	 */
	public void setResizable(boolean b) {
		resizable = b;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpNode#isResizable()
	 */
	public boolean isResizable() {
		return resizable;
	}

    public boolean isOpenable() {
        return isOpenable;
    }

    public void setOpenable(boolean isOpenable) {
		this.isOpenable = isOpenable;
    }

    public boolean isShowAttachments() {
    	return showAttachments;
    }

    public void setShowAttachments(boolean canShowAttachments) {
    	this.showAttachments  = canShowAttachments;
    }

    public boolean isInDisplay() {
        return getParent() != null;
    }

    public boolean hasPreferredLocation() {
        return false;
    }

    private double xInLayout = 0;
    private double yInLayout = 0;
    private double wInLayout = 0;
    private double hInLayout = 0;
    private Object layoutInformation;

	private Stack savedNames = new Stack(); // A stack of names that this node was previously known by

	private String labelStyle = DisplayConstants.LABEL_STYLE_FULL;

    public double getXInLayout() {
        return xInLayout;
    }

    public double getYInLayout() {
        return yInLayout;
    }

    public double getWidthInLayout() {
        return wInLayout;
    }

    public double getHeightInLayout() {
        return hInLayout;
    }

    public void setLocationInLayout(double x, double y) {
        xInLayout = x;
        yInLayout = y;
    }

    public void setSizeInLayout(double width, double height) {
        // @tag Shrimp.minNodeSize: Commented this because it overrides layout resize parameter
    	//wInLayout = Math.max(MIN_GLOBAL_NODE_SIZE, width);
    	//hInLayout = Math.max(MIN_GLOBAL_NODE_SIZE, height);
        wInLayout = width;
        hInLayout = height;
    }

    public Object getLayoutInformation() {
        return layoutInformation;
    }

    public void setLayoutInformation(Object layoutInformation) {
        this.layoutInformation = layoutInformation;
    }

	/**
	 * Populate the specified layout constraint
	 */
	public void populateLayoutConstraint(LayoutConstraint constraint) {
		try {
			if (constraint instanceof LabelLayoutConstraint) {
				LabelLayoutConstraint labelConstraint = (LabelLayoutConstraint) constraint;
				labelConstraint.label = this.name;
				String displayText = getOnNodeText();
				// split text up to strip out leading and trailing whitespace on each line
				if (displayText != null && !displayText.equals("")) {
					String[] displayLines = splitHtmlText(displayText);
					for (int i=0; i<displayLines.length; i++) {
						labelConstraint.label += "\n" + displayLines[i];
					}
				}
				else {
					// ensure a minimum height node (otherwise Shrimp resizes to min
					// height and we get edges overlapping nodes
					labelConstraint.label += "\n\n";
				}
				labelConstraint.pointSize = 18;
			}
			else if (constraint instanceof BasicEntityConstraint) {
				BasicEntityConstraint basicEntityConstraint = (BasicEntityConstraint) constraint;
				if (this.hasPreferredLocation()) {
					basicEntityConstraint.hasPreferredLocation = true;
					basicEntityConstraint.preferredX = this.getX();
					basicEntityConstraint.preferredY = this.getY();
				}
			}
			else if ( constraint instanceof SequenceNodeLayoutConstraint ) {
				SequenceNodeLayoutConstraint nodeConstraint = (SequenceNodeLayoutConstraint) constraint;
				nodeConstraint.type =
					(String)getArtifact().getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE);
				nodeConstraint.order = (String)getArtifact().getAttribute(AttributeConstants.NOM_ATTR_ORDER);
			}
		} catch (NoClassDefFoundError ignore) {}
	}

    /**
     * Rename a node (usually used for annotating a summary node in a grouping operation)
     * @tag Shrimp.grouping
     * @param newName
     */
    public void rename(String newName) {
    	this.savedNames.push(this.name);
    	this.name = newName;
		ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, false);
		if (label != null) {
			label.setText(newName);
		}
    }

    /**
     * Restore the most recent name for this node
     * @tag Shrimp.grouping
     */
    public void restoreName() {
    	if (!this.savedNames.isEmpty()) {
	    	this.name = (String)this.savedNames.pop();
			ShrimpLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(this, false);
			if (label != null) {
				label.setText(this.name);
			}
    	}
    }

    /**
     * Get a list of previously saved names (created by renaming)
     */
	public String[] getSavedNames() {
		return (String[])(this.savedNames.toArray(new String[savedNames.size()]));
	}

    /**
     * Set the style of label for this node (e.g., full size, hidden, elided left...)
     * @param labelStyle
     */
	public void setLabelStyle(String labelStyle) {
		this.labelStyle = labelStyle;
		firePositionChangedEvent();
		repaint();
	}

    /**
     * Set the style of label for this node (e.g., full size, hidden, elided left...)
     * @param labelStyle
     */
	public String getLabelStyle() {
		return this.labelStyle;
	}

	protected PNestedDisplayBean getPNestedDisplayBean() {
		return (PNestedDisplayBean) displayBean;
	}

}