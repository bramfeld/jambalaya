/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.util.Collection;
import java.util.Vector;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.DisplayBean.event.DisplayFilterRequestListener;
import ca.uvic.csr.shrimp.DisplayBean.event.NavigationListener;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyListener;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.LifelineGroupingManager;
import ca.uvic.csr.shrimp.DisplayBean.layout.MethodExecGroupingManager;
import ca.uvic.csr.shrimp.DisplayBean.layout.StructuralGroupingManager;
import edu.umd.cs.piccolo.PCanvas;

/**
 * The DisplayBean is the bean responsible for displaying
 * the visual representations of data entities . It also
 * receives input from the mouse and the keyboard and
 * fires events accordingly.
 */
public interface DisplayBean {

    /** Property key for default layout mode **/
	public static final String PROPERTY_KEY__DEFAULT_LAYOUT_MODE = "ShrimpDisplayBean::Layout Algorithm";
	/** Default value for default layout mode **/
	public static final String PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE = LayoutConstants.LAYOUT_GRID_BY_TYPE;

	/** Property key for using animation*/
	public static final String PROPERTY_KEY__USE_ANIMATION = "ShrimpDisplayBean::Use Animation";
	/** Default value for using animation*/
	public static final boolean DEFAULT_USING_ANIMATION = true;
	/** Property key for number of nodes needed before disable animation*/
	public static final String PROPERTY_KEY__ANIMATION_THRESHOLD = "ShrimpDisplayBean::Animation threshold";
	/** Default value for number of nodes needed before disable animation*/
	public static final String DEFAULT_ANIMATION_THRESHOLD = "200";

	/** Property key for number of children needed to show "too many children" warning.*/
	public static final String PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING_THRESHOLD = "ShrimpDisplayBean::show many children warning threshold";
	/** Default value for number of children needed to show "too many children" warning.*/
	public static final String DEFAULT_SHOW_MANY_CHILDREN_WARNING_THRESHOLD = "200";
	/** Property key for showing "too many children" warning at all. */
	public static final String PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING = "ShrimpDisplayBean::show many children warning";
	/** Default value for showing "too many children" warning at all. */
	public static final String DEFAULT_SHOW_MANY_CHILDREN_WARNING = "true";

    public static final String DEFAULT_SHOW_NO_ROOT_NODES_WARNING = "true";
    public static final String PROPERTY_KEY__SHOW_NO_ROOT_NODES_WARNING = "show no root nodes warning";

    public static final String DEFAULT_SHOW_EXPRESS_VIEW_WARNING = "true";
    public static final String PROPERTY_KEY__SHOW_EXPRESS_VIEW_WARNING = "show express view warning";

	public static final String PROPERTY_KEY__USE_MOTION = "use motion highlighting";
	public static final boolean DEFAULT_USE_MOTION = true;

	/** The motion path length key. */
	public static final String PROPERTY_KEY__DISPLACEMENT = "motion highlighting path length";
	/** The default motion displacement (integer). */
	public static final String DEFAULT_DISPLACEMENT = "1";

	/** The motion radius key. */
	public static final String PROPERTY_KEY__MOTION_RADIUS = "motion highlighting radius";
	/** The default motion radius String (double). */
	public static final String DEFAULT_MOTION_RADIUS = "2.0";

	/** The motion time in milliseconds key. */
	public static final String PROPERTY_KEY__MOTION_TIME = "motion highlighting time milliseconds";
	/** The default motion time String (long milliseconds). */
	public static final String DEFAULT_MOTION_TIME = "1000";

	/** The motion speed property key. */
	public static final String PROPERTY_KEY__MOTION_SPEED = "motion highlighting speed";
	/** The default motion speed (integer). */
	public static final String DEFAULT_MOTION_SPEED = "5";

	/** The node border width multiplier key. */
	public static final String PROPERTY_KEY__BORDER_WIDTH_MULTIPLIER = "ShrimpDisplayBean::Border Width";;	/** The default node border width multiplier. */
	public static final String DEFAULT_BORDER_WIDTH_MULTIPLIER = "1";

	public static final String PROPERTY_KEY__OVERRIDE_DEFAULT_NODE_BORDER_WIDTH = "override default node border width";

	public static final String DEFAULT_OVERRIDE_DEFAULT_NODE_BORDER_WIDTH = "false";

	/**
	 * Add a shrimpMouseListener to the DisplayBean
	 * @param sml
	 */
	public void addShrimpMouseListener(ShrimpMouseListener sml);

	/**
	 * Remove a shrimpMouseListener from the DisplayBean
	 * @param sml
	 */
	public void removeShrimpMouseListener(ShrimpMouseListener sml);

	/**
	 * Add a shrimpKeyListener to the DisplayBean
	 * @param skl
	 */
	public void addShrimpKeyListener(ShrimpKeyListener skl);

	/**
	 * Remove a shrimpKeyListener from the DisplayBean
	 * @param skl
	 */
	public void removeShrimpKeyListener(ShrimpKeyListener skl);

   /**
     * Turns the display bean and its listeners on and off
     * Specifically if it the display bean is not enabled, it will not receive
     * any input from the keyboard, mouse or any other input device.
     * NOTE: This only disables at the user level.  Any calls to the methods
     *       in this bean will still work normally.
     *
     * @param enabled true if the display bean is to become enabled
     */
    public void setEnabled(boolean enabled);

    /**
     * @return if the display bean is enabled
     */
    public boolean isEnabled();


	/**
	 * Adds a navigation listener.
	 * @param listener The object that will handle the navigation event.
	 */
	public void addNavigationListener(NavigationListener listener);

	/**
	 * Remove a navigation listener.
	 * @param listener The object that will handle the navigation event.
	 */
	public void removeNavigationListener(NavigationListener listener);

	/**
	 * Decides whether or not to use composites.
	 * @param useComposites Set true to use composite relationships
	 */
	public void setUsingComposites(boolean useComposites);

	/**
	 * @return whether or not the display is showing arrow heads on arcs
	 */
	public boolean getUsingArrowHeads();

	/**
	 * Set whether or not the display should show arrow heads on arcs
	 */
	public void setUsingArrowHeads(boolean usingArrrowHeads);

	/**
	 * Returns whether or not this display bean is displaying composite arcs.
	 */
	public boolean getUsingComposites();


   /**
	* Sets the tooltip to be enabled/disabled
	* @param enabled Whether or not the tooltip is on/off
	*/
   public void setToolTipEnabled(boolean enabled);

   /**
	* returns whether or not the tool tip is on
	*/
   public boolean isToolTipEnabled();

   /**
	* Sets whether or not the display should use long tooltips.
	*/
   public void setUseLongTooltips(boolean useLongTooltips);

   /**
	* Returns whether or not the display should use long tooltips.
	*/
   public boolean getUseLongTooltips();

    /**
     * Set the visual representation (artifact/relationship) corresponding
     * to the Object visible/invisible
     * If The object passed in is a Vector, all of the objects in
     * that Vector will be set visible/invisible
     *@param obj The object to be set visible/invisible
     *@param visible Choosing between visible and invisible
     */
    public void setVisible(Object obj, boolean visible, boolean assignDefaultPosition);

//    /**
//     * Set all of the artifacts/relationships with the given attribute condition
//     * to either visible or not visible.  For example, to set all of the artifacts
//     * of type "proc" to invisible, use the method in the following way:
//     *
//     *        setVisible("type", "equals", "proc", false);
//     *
//     * The available attributes can be found in the Artifact interface or its subclasses
//     * The available boolean operations are "less than", "equals", "greater than"
//     * The value can be any Object such as String, Integer, ...
//     *
//     * @param attributeName The name of the attribute to base the comparison on
//     * @param booleanOperation The boolean operation to perform on the attribute's value
//     * @param value The value the artifact must have in order to be affected.
//     * @param visible Whether or not the found artifacts should be set visibile or invisible
//     */
//    public void setVisible(String attributeName, String booleanOperation, Object value, boolean visible);

   	/**
   	 * Returns a list of all the visible artifacts/relationships in the display bean.
   	 */
   	public Vector getVisibleObjects();

   	/**
   	 * Returns a list of all the invisible artifacts/relationships in the display bean.
   	 */
   	public Vector getInvisibleObjects();

    /**
     * Returns whether or not the given artifact/relationship is visible
	 *
     * @param obj The artifact/relationship to be retrieve the info on
     */
    public boolean isVisible(Object obj);

    /**
     * Add the visual representation (node/arc) corresponding
     * to the Object (artifact/relationship) to the display
     * If the object passed in is a Vector, all of the objects in
     * the Vector will be added to the display
     *
     * @param obj The object to be added
     */
    public void addObject(Object obj);

   /**
     * Remove the visual representation (node/arc) corresponding
     * to the Object from the display
     * If the Object passed in is a Vector, all of the objects in
     * that Vector will be removed from the display
     * If the object passed in is not represented in the display,
     * nothing will happen.
     *
     *@param obj The object to be removed
     */
    public void removeObject(Object obj);

	/**
	 * Clear the display.
	 * Removes all objects.
	 */
   	public void clear();

   /**
     * Change the display mode of the panels of the Object
     * If the Object is a Vector, change the display mode
     * of all the Objects in the Vector.
     *
     *@param obj The Object whose panelMode is modified
     *@param newMode The new panelMode
     */
    public void setPanelMode(Object obj, String newMode);

    /**
     * Returns the panel mode of the given object.
     * @param obj
     */
    public String getPanelMode(Object obj);

   /**
     * Change the default display mode of labels.
     *
     *@param newMode The new defaultLabelMode
     */
    public void setDefaultLabelMode(String newMode);

    /**
     * Returns the default label mode of the display.
     */
    public String getDefaultLabelMode();

	/**
	 * Sets the label mode of a particular object
	 * @param object
	 * @param newMode
	 */
	public void setLabelMode(Object object, String newMode);

	/**
	 *
	 * @param object
	 * @return The label mode of the given object.
	 */
	public String getLabelMode(Object object);

   /**
     * Change the font of labels in the display.
     *@param newFont The new font for labels. Is an Object so no AWT in this interface.
     */
    public void setLabelFont(Object newFont);

    /**
     * Returns the font used for labels in the display.
     * @return An Object, since we don't want any AWT in this interface.
     */
    public Object getLabelFont();

    /**
     * Change whether we should switch labelling mode.
     *
     *@param switchLabelling Whether labels fade out by level.
     */
    public void setSwitchLabelling(boolean switchLabelling);

    /**
     * Return whether we should switch labelling mode.
     */
    public boolean getSwitchLabelling();

   /**
     * Change the number of nodes at which labelling switches to on node
     *
     *@param num the number of nodes to switch labelling mode at
     */
    public void setSwitchAtNum(int num);

    /**
     * Returns the number of nodes at which labelling switches to on node
     */
    public int getSwitchAtNum();

    /**
     * Change the number of levels of labels to show.
     *
     *@param numLevels The new number of levels to show
     */
    public void setLabelLevels(int numLevels);

    /**
     * Returns the number of levels of labels showing
     */
    public int getLabelLevels();

    /**
     * Change whether labels fade out by level.
     *
     *@param fadeOut Whether labels fade out by level.
     */
    public void setLabelFadeOut(boolean fadeOut);

    /**
     * Return whether labels fade out by level.
     */
    public boolean getLabelFadeOut();

	/**
	 * Method getLabelBackgroundOpaque.
	 * @return boolean
	 */
	boolean getLabelBackgroundOpaque();

	void setLabelBackgroundOpaque(boolean labelOpaque);

	void setShowArcLabels(boolean showArcLabels);

	boolean getShowArcLabels();

   /**
     * Change the layout mode of the Object
     * If the Object is a Vector, change the layout mode of all the Objects in the Vector.
     * @param obj The Object whose layoutMode is modified
     * @param newMode The new layoutMode
     * @param showLayoutDialog Whether or not to prompt the user with a layout dialog.
     * @param animate Whether or not to animate the layout.
     */
    public void setLayoutMode(Object obj, String newMode, boolean showLayoutDialog, boolean animate);

    /**
     * Returns the screen coordinates
     * @return An Object so that no AWT in this interface
     */
    public Object getScreenCoordinates();

    /**
     * start zooming in
     */
    public void startZoomingIn();

    /**
     * start zooming out
     */
    public void startZoomingOut();

    /**
     * stop zooming
     */
    public void stopZooming();

    /**
     * start panning
     */
    public void startPanning(String direction);

    /**
     * stop panning
     */
    public void stopPanning();

    /**
     * Starts moving the passed in objects with any mouse movement
     */
    public void startMovingWithMouse(Collection objs);

    /**
     * Stop moving object along with mouse movement
     * @see #startMovingWithMouse(Collection)
     */
    public void stopMovingWithMouse();

    /**
     * Continues moving passed in objects with any mouse movement
     * @see #startMovingWithMouse(Collection)
     */
    public void continueMovingWithMouse(Collection objs);

	/**
	 * Move and resize the given objects
	 *
	 * Note: the center of object[i] will be placed at position[i] and resized to dimension[i]
	 *
	 * @param objects The ojects to move and resize.
	 * @param positions The new positions of centers of these objects. These must be java.awt.geom.Point2D objects.
	 * @param dimensions The new dimensions of the nodes. These must be java.awt.Dimension2D objects .
	 * @param animate Whether or not to animate the moving and resizing.
	 */
	public void setPositionsAndSizes(Vector objects, Vector positions, Vector dimensions, boolean animate);
	public void setPositionsAndSizes(Vector objects, Vector relationships, Vector positions, Vector dimensions, boolean animate);

	/**
	 * Transform the given objects by translating and scaling the
	 * objects from 0,0 using the given transformations.
	 *
	 * Note: The transform at position i is only applied to the object at position i
	 *
	 * @param objects The objects to move
	 * @param transforms The tranformations to use on the objects
	 */
    public void setTransformOf(Vector objects, Vector transforms);

	/**
	 * Transform the given object by translating and scaling the
	 * object using the given transformation.
	 *
	 * @param obj The object to transform.
	 * @param transform The tranformation to use on the object. Is an Object so that no AWT in this interface.
	 */
	public void setTransformOf(Object obj, Object transform);

	/**
	 * Transform the given object by translating and scaling the
	 * object using the given transformation.
	 *
	 * @param obj The object to move
	 * @param transform The tranformation to use on obj. Is an Object so that no AWT in this interface.
	 * @param firePositionChangedEvent Whether or not to fire a position changed event after transforming the object
	 * @param animate Whether or not to animate the transformation process.
	 */
	public void setTransformOf(Object obj, Object transform, boolean firePositionChangedEvent, boolean animate);

	/**
	 * Returns the transformation currently being used by obj.
	 * @param obj The object you wish to retrieve the transformation for
	 * @return the AffineTransform - it is an Object so that no AWT in this interface
	 */
	public Object getTransformOf(Object obj);


    /**
     * start fisheye zoom on the node corresponding to the artifacts
     */
    public void startFisheyeingIn(Vector nodes);

    /**
     * stop fisheye-zooming in
     */
    public void stopFisheyeing();

    /**
     * start fisheye-zooming out on the node corresponding to the artifacts
     */
    public void startFisheyeingOut(Vector nodes);

    /**
     * open a composite relationship
     */
    //public void openComposite(Relationship rel);

    /**
     * magnify the node corresponding to the artifacts
     *
     * @param object The object(s) to focus on.  This could be a vector
     *               or a single artifact/relationship.
     */
    public void focusOn(Object object);


    /**
     * Ensures that every visible object can be seen in the display.
     * @param animate Whether or not to animate the transition.
     */
    public void focusOnExtents(boolean animate);

    public double [] getExtents();

    /**
     * magnify onto the area given by the given node, and open the node into the given panel mode.
     *
     * @param node The node to magnify on.
     * @param panelMode The panel that will be displayed when the node is focused on.
     */
    public void focusOnNode(ShrimpNode node, String panelMode);

    /**
     * Move the screen camera to the given coordinates
     * @param animate Whether or not to animate the transition.
     */
    public void focusOnCoordinates(Vector coordinates, boolean animate);

    /**
     * Returns the current objects the screen is focused on.
     * These objects can be artifacts, relationships, or a combination
     * of both.
     */
    public Vector getCurrentFocusedOnObjects();

    /**
     * add a FilterListener to the DisplayBean
     */
    public void addFilterRequestListener(DisplayFilterRequestListener frl);

	/**
	 * Remove a filterListener to the DisplayBean
	 */
	public void removeFilterRequestListener(DisplayFilterRequestListener frl);


	/**
	 * Highlights/dehighlights the given artifact/relationship
	 *
	 * @param object The object to modify
	 * @param on Whether or not to highligh the object (false = unhighlight)
	 */
    public void highlight(Object object, boolean on);

	/**
	 * Returns the object that bridges the gap between the display and the data.
	 */
	public DataDisplayBridge getDataDisplayBridge();

	/**
	 * Returns the closest path between two sets of objects.  The path goes from the source object(s)
	 * to the common ancestor, then from the common ancestor to destination object(s). Note that the
	 * source object(s) are always added to the path at the beginning, and the dest object(s) are
	 * always added to the path at the end.
	 *
	 * @param source The source of the path.  This could be an node, arc, or vector of both
	 * @param dest The destination of the path.  This could be an node, arc, or vector of both
	 *
	 * @return A vector of objects along the path, beginning at the source and ending at the dest.
	 * The elements in the path could be a nodes or arcs.
	 */
	public Vector getPathBetweenObjects(Object source, Object dest);

	/**
	 * Returns the first common node from all of the given nodes' ancestor trees. In other words,
	 * this method will get all of the ancestors for each node passed in.  The ancestors will then be
	 * searched for the lowest (ie. closest to the passed in node) ancestor in each tree.  If there
	 * isn't a common node between the given nodes, null is returned.
	 *
	 * Note: If one node is an ancestor of the others, it will be returned
	 *
	 * @param nodes The nodes to be searched
	 */
	public ShrimpNode getClosestCommonAncestor(Vector nodes);

	/**
	 * Returns whether or not the given object is filtered in the display.
	 * @param object
	 */
	public boolean isFiltered(Object object);

	/**
	 * Returns whether or not the whole display is visible.
	 */
	public boolean isVisible();

	/**
	 * Sets whether or not the whole display is visible
	 * @param visible
	 */
	public void setVisible(boolean visible);

	/**
	 * @param node
	 */
	public boolean isNodeOpen(ShrimpNode node);
	/**
	 * @param node
	 */
	public void openNode(ShrimpNode node);

	/**
	 * @param node
	 */
	public void closeNode(ShrimpNode node);

	/**
	 * Returns all nodes that have been created.
	 * Returns an empty vector if no nodes found.
	 */
    public Vector getAllNodes();
	/**
	 * Returns all arcs that have been created.
	 * Returns an empty vector if no arcs found.
	 */
	public Vector getAllArcs();

	/**
	 * Returns all arcs that have been created and are visible.
	 * Returns an empty vector if no arcs found.
	 */
	public Vector getVisibleArcs();

	/**
	 * Returns all nodes that have been created and are visible.
	 * Returns an empty vector if no nodes found.
	 */
	public Vector getVisibleNodes();

	/**
	 * Returns all the ShrimpLabels
	 * Returns an empty vector if no ShrimpLabels found
	 */
	public Vector getAllLabels();

	/**
	 * Returns all labels that have been created and are visible.
	 * Returns an empty vector if no labels found.
	 */
	public Vector getVisibleLabels();

    /**
     * Causes the display to repaint itself.
     */
    public void repaint();


//     /**
//     * Set all of the artifacts/relationships with the given attribute condition
//     * to either visible or not visible.  For example, to set all of the artifacts
//     * of type "proc" to invisible use the method in the following way:
//     *
//     *        setVisible("type", "equals", "proc", false);
//     *
//     * The available attributes for the artifact/relationship are "name", "type", and
//     *     any data stored in the attributes field.
//     * The available boolean operations are "less than", "equals", "greater than"
//     *     Note: "equals" is the only operator used for the attributes "name" and "type"
//     *     Note: "less than" and "greater than" must have values that are either of type String or Double
//     * The value can be any Object such as String, Integer, ...
//     *
//     * @param attributeName The name of the attribute to base the comparison on
//     * @param booleanOperation The boolean operation to perform on the attribute's value
//     * @param value The value the artifact must have in order to be affected.
//     * @param visible Whether or not the found artifacts should be set visibile or invisible
//     */
//    public void setNodesVisible(String attributeName, String booleanOperation, Object value, boolean visible);

//    /**
//     * Set all of the artifacts/relationships with the given attribute condition
//     * to either visible or not visible.  For example, to set all of the artifacts
//     * of type "proc" to invisible use the method in the following way:
//     *
//     *        setVisible("type", "equals", "proc", false);
//     *
//     * The available attributes for the artifact/relationship are "name", "type", and
//     *     any data stored in the attributes field.
//     * The available boolean operations are "less than", "equals", "greater than"
//     *     Note: "equals" is the only operator used for the attributes "name" and "type"
//     *     Note: "less than" and "greater than" must have values that are either of type String or Double
//     * The value can be any Object such as String, Integer, ...
//     *
//     * @param attributeName The name of the attribute to base the comparison on
//     * @param booleanOperation The boolean operation to perform on the attribute's value
//     * @param value The value the artifact must have in order to be affected.
//     * @param visible Whether or not the found artifacts should be set visibile or invisible
//     */
//    public void setArcsVisible(String attributeName, String booleanOperation, Object value, boolean visible);


	/**
	 * @param cprels
	 */
	public void setCprels(String[] cprels);

	/**
	 * Returns the child-parent arc types currently being used for nesting
	 */
	public String[] getCprels();

	/**
	 * Sets whether or not child-parent nesting should be inverted
	 * @param inverted
	 */
	public void setInverted(boolean inverted);

	/**
	 * Returns whether or not child-parent nesting should be inverted
	 */
	public boolean isInverted();

	/**
	 * The static rendering quality of the display, that is when the user is not
	 * dragging, zooming, or panning.
	 */
	public int getStaticRenderingQuality();

	/**
	 * @param quality
	 * @see #getStaticRenderingQuality()
	 */
	public void setStaticRenderingQuality(int quality);

	/**
	 * Returns the default rendering quality
	 * @see #getStaticRenderingQuality()
	 */
	public int getDefaultStaticRenderingQuality();

	/**
	 * The static rendering quality of the display, that is when the user is
	 * dragging, zooming, or panning.
	 */
	public int getDynamicRenderingQuality();

	/**
	 * @param quality
	 * @see #getDynamicRenderingQuality()
	 */
	public void setDynamicRenderingQuality(int quality);

	/**
	 * Returns the default dynamic rendering quality
	 * @see #getDynamicRenderingQuality()
	 */
	public int getDefaultDynamicRenderingQuality();

	/**
	 * Sets whether or not the display is "interacting." Usually set to true when
	 * the display is zooming or animating.
	 * While the display is interacting it renders at a lower quality and therfore
	 * can render faster.
	 * @param interacting
	 */
	public void setInteracting(boolean interacting);

	/**
	 * Returns whether or not the display is "interacting."
	 * @see #setInteracting(boolean)
	 */
	public boolean isInteracting();

	/**
	 * Adds a layout to the DisplayBean, making it available to
	 * the user.  This layout is an algorithm used to organize
	 * the children of an node.
	 *
	 * @param layout The layout to add
	 */
	public void addLayout(Layout layout);

	/**
	 * Checks if this {@link DisplayBean} has the layout with the given name.
	 * @param layoutName the name of the layout, see @link {@link LayoutConstants}
	 * @return true if the layout is found
	 */
	public boolean hasLayout(String layoutName);

	/**
	 * Returns the available layouts in this display bean
	 *
	 * @see #addLayout(Layout)
	 */
	public Vector getLayouts();

	/**
	 * Adds a new Arc Style to the displaybean.
	 * @param arcStyle the arc style to add
	 * @param makeDefault Whether or not this style should become the default.
	 */
	public void addArcStyle(ArcStyle arcStyle, boolean makeDefault);

	/**
	 * Removes an arc style from the display bean.  Note that the StraightSolidLineArcStyle
	 * can't be removed.
	 */
	public void removeArcStyle(ArcStyle arcStyle);

	/** Returns the arcstyle with the given name */
	public ArcStyle getArcStyle(String arcStyleName);

	/** Returns all the arc styles currently in this display bean. */
	public Vector getArcStyles();

	/**
	 * @param font a java.awt.Font object (no AWT in this interface)
	 * @return How high, in pixels, the given font will be on the canvas.
	 */
	public int getFontHeightOnCanvas(Object font);

	/**
	 * @return A java.awt.Dimension object (no AWT in this interface)
	 */
	public Object getCanvasDimension();

	/**
	 * Return whether or not the display is animating layouts.
	 */
	public boolean isAnimatingLayouts();

	/**
	 * Returns the node shapes registered with this display bean.
	 */
	public Vector getNodeShapes();

	/**
	 * @param nodeShapeName
	 * @return The node shape with the given name (if there is one)
	 */
	public NodeShape getNodeShape(String nodeShapeName);

	/**
	 * Adds a new node shape to the displaybean.
	 * @param nodeShape the node shape to add
	 * @param makeDefault Whether or not this shape should become the default.
	 */
	public void addNodeShape(NodeShape nodeShape, boolean makeDefault);

	/**
	 * Removes a node shape from this display.
	 * @param nodeShape
	 */
	public void removeNodeShape(NodeShape nodeShape);

	/**
	 * Gets the allowed label styles.
	 * @return Vector of Strings
	 * @see DisplayConstants#LABEL_STYLE_FULL
	 * @see DisplayConstants#LABEL_STYLE_ELIDE_LEFT
	 * @see DisplayConstants#LABEL_STYLE_ELIDE_RIGHT
	 * @see DisplayConstants#LABEL_STYLE_HIDE
	 */
	public Vector getLabelStyles();

	/**
     * @param globalBounds An Object so that no AWT in this interface
	 * @param shouldScale
	 * @param duration
	 * @param animate Whether or not to animate the transition
	 */
	public void moveViewToCenterBounds(Object globalBounds, boolean shouldScale, long duration, boolean animate);

    public void setDefaultArcWeight(double defaultArcWeight);
    public double getDefaultArcWeight();

    /**
     * Requests focus for the {@link PCanvas} used with this DisplayBean.
     */
    public void requestFocus();

    /**
     * @tag Shrimp.sequence : Necessary to prevent node and edge dragging in certain layouts.
     * @return boolean if node edge movement is allowed
     */
    public boolean isNodeEdgeMovementAllowed();

    /**
     * @tag Shrimp.sequence : Necessary to prevent node and edge dragging in certain layouts.
     */
    public void setNodeEdgeMovement(boolean movementAllowed);

    /**
     * @return true if the display bean has no hierarchy (no cprels).
     */
    public boolean isFlat();

    /**
     * Refreshes the target {@link ShrimpNode} using the last run layout.
     * @param targets the target shrimp nodes to layout
     * @param animate if animation should be used
     */
    public void refreshLayout(Vector targets, boolean animate);

    /**
     * Refreshes the target {@link ShrimpNode} using the last run layout, using animation.
     * @param targets the target shrimp nodes to layout
     */
    public void refreshLayout(Vector targets);

    /**
     * Runs the last layout on the root nodes using animation.
     */
    public void refreshLayout();

    /** Runs the last layout on the root nodes possibly using animation. */
    public void refreshLayout(boolean animate);

    /**
     * @return the last used layout
     */
    public String getLastLayoutMode();

    public StructuralGroupingManager getStructuralGroupingManager();

    public LifelineGroupingManager getLifelineGroupingManager();

    public MethodExecGroupingManager getMethodExecGroupingManager();

	public Vector getBorderStyles();;
}