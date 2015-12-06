/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.Icon;

import org.eclipse.mylar.zest.layouts.LayoutEntity;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.IconProvider;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpTerminal;

/**
 * Interface for all Shrimp nodes.
 *
 * @author Rob Lintern, Chris Callendar
 */
public interface ShrimpNode extends ShrimpDisplayObject, LayoutEntity  {

	public static final Color DEFAULT_NODE_COLOR = Color.WHITE;
	public static final NodeShape DEFAULT_NODE_SHAPE  = new RectangleNodeShape();
	public static final int DEFAULT_NODE_DIMENSION = 500;
	public static final double MAX_NODE_WH_RATIO = 1.5;
	public static final double MIN_NODE_WH_RATIO = 1.0/MAX_NODE_WH_RATIO;

	// degree of interest transparency constants
	public static final float NOT_INTERESTING = 0.7f;
	public static final float INTERESTING = 1f;

    /** TODO base the min node size on the current magnification level of the view */
    public static final double MIN_GLOBAL_NODE_SIZE = 16.0;

    /**
     * @return the custom overlay icon provider for the node
     */
	public IconProvider getOverlayIconProvider();

	/**
	 * Sets a custom overlay icon provider for the node.  The provider will
	 * provide the icon, the icon position, and whether or not the icon should be scaled.
	 * @param overlayIconProvider the provider, null is allowed
	 * @see IconProvider
	 */
	public void setOverlayIconProvider(IconProvider overlayIconProvider);

	public void setVisible(boolean newVisibility);
	public boolean isVisible();

	/**
	* Raises this object above its siblings in the display
	*/
	public void raiseAboveSiblings();

	public int getLevel();

	/**
	 * Sets the level in the nested graph hierarchy for this node.
	 * @param level the level in the hierarchy
	 * occupied by this node.
	 */
	public void setLevel(int level);

	public void setHasBeenTransformedOnce(boolean hasBeenTransformedOnce);
	public boolean getHasBeenTransformedOnce();

	public void setLabelMode(String newMode);
	public String getLabelMode();

	/**
	 * Change the font of the label of this node
	 */
	public void setLabelFont(Font font);

	/**
	 * Sets the outer bounds of this node.
	 * @param newBounds
	 */
	public void setOuterBounds(Rectangle2D.Double newBounds);

	/**
	 * Returns the artifact associated with this node.
	 */
	public Artifact getArtifact();

	/**
	 * @return The shape of this node.
	 */
	public NodeShape getNodeShape();

	/**
	 * @return The usable area inside this node.
	 */
	public Rectangle2D.Double getInnerBounds();

	/**
	 * @return The bounds of this node's label
	 */
	public Rectangle2D.Double getLabelBounds();

	/**
	 * Note: This will be a unique id.
	 */
	public long getID();
	//public void setID(long id);

	public String toString();

	public void setMarked(boolean marked);
	public boolean isMarked();

	//public void setHasBeenExpanded(boolean hasBeenExpanded);
	//public boolean getHasBeenExpanded();

	public void setHasBeenPrunedFromTree(boolean hasBeenPrunedFromTree);
	public boolean hasBeenPrunedFromTree();

	/**
	 * Sets if this node has become the root node.
	 * This is the opposite of being pruned.
	 * If true this node will have a special node shape (diamond).
	 * @param isPruneRoot true if this is the visible root node
	 */
	public void setIsPruneRoot(boolean isPruneRoot);

	/**
	 * @return true if this node is the prune root
	 */
	public boolean isPruneRoot();

	/**
	 * Sets if the node has been groups.
	 */
	public void setIsGrouped(boolean isGrouped);

	/**
	 * @return true if this node is grouped
	 */
	public boolean isGrouped();

	public void setHasCollapsedAncestor(boolean hasCollapsedAncestor);
	public boolean hasCollapsedAncestor();

	public void setIsCollapsed(boolean isCollapsed);
	public boolean isCollapsed();

	public int compareTo(Object o);

	public String getPanelMode();

	/**
	 * Sets the customizedPanel for the current node to the given mode. Note: This method
	 * will look in the corresponding Artifact for customized (Domain specific) panels.
	 * @param newMode The new customizedPanel mode.
	 */
	public void setPanelMode(final String newMode);
    public boolean isCustomPanelShowing();

	public void setChildrenMode();

	/**
	 * Returns whether or not the children of this node have been added
	 */
	public boolean haveChildrenBeenAdded();
	public void setChildrenAdded(boolean b);

	public void setColor(Color color);
	public Color getColor();

	public ShrimpNode getParentShrimpNode();
    public ShrimpNode getRootShrimpNode();

    public void setHasFocus(boolean hasFocus);
	public boolean hasFocus();

	public boolean isHighlighted();
	public void setIsHighlighted(boolean highlighted);
	public void setIsMouseOver(boolean mouseOver);
	public void setEquivalentNodeSelected(boolean equivalentNodeSelected);

	public boolean isOpen();
	public double getMagnification();

	/**
	 * @return The centre point of this node.
	 */
	public Point2D.Double getCentrePoint();

	public void addTerminal(ShrimpTerminal terminal);
	public void removeTerminal(PShrimpTerminal terminal);
	public Vector getTerminals();

	/**
	 * @param id
	 * @return The terminal attached to this node with the given id (null is returned if there isn't one)
	 */
	public ShrimpTerminal getTerminal(String id);

	/**
	 * Determines the point at which a terminal should attached to the outer edge of this node based on an
	 * angle from this node's center.
	 * @param terminalPositionAngle The angle from this node's center that the terminal will be at.
	 * @param attachPoint Overwritten and returned with the appropriate x and y values filled in. NOTE: These values
	 * will be in reference to the node's center in the GLOBAL coordinate system.
	 * @return The angle that the terminal should be drawn at in order to match "nicely" with the edge of this node
	 */
	public double getTerminalAttachPoint(double terminalPositionAngle, Point2D.Double attachPoint);

	public void recomputeCentrePoint();

	/**
	 * Notifies the terminals, arcs, and labels attached to this node that they need to update
	 * the location they are drawn at.
	 */
	public void firePositionChangedEvent();

	/**
	 * @param nodeShape
	 */
	public void setNodeShape(NodeShape nodeShape);

    /**
     * Set the style of label for this node (e.g., full size, hidden, elided left...)
     * @param labelStyle
     */
	public void setLabelStyle(String labelStyle);

	public void setIcon(Icon icon);
	public Icon getIcon();

	public String getName();
	public void setName(String name);

	public boolean isResizable();
	public void setResizable(boolean b);

	public Image getDisplayImage();
	public void setDisplayImage(Image image);

	public NodeImage getNodeImage();

    public boolean isOpenable();
    public void setOpenable(boolean isOpenable);

    public boolean isShowAttachments();
    public void setShowAttachments(boolean showAttachments);

    /**
     * Rename this node to the specified name
     * @tag Shrimp.grouping
     * @param newName
     */
    public void rename(String newName);

    /**
     * Restore the name of this node
     * @tag Shrimp.grouping
     */
    public void restoreName();

    /**
     * Get a list of previously saved names (created during renaming)
     * @tag Shrimp.grouping
     * @return an array of the nodes previously names
     */
    public String[] getSavedNames();

    public void repaint();

	public void setOuterBorderColor(Color color);

	public void setOuterBorderStyle(String style);

	public void setInnerBorderColor(Color color);

	public void setInnerBorderStyle(String style);
}