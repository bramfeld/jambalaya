/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ScriptingBean;

import ca.uvic.csr.shrimp.gui.FilmStrip.FilmStrip;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;


/**
 * This is the interface of the scripting support for the main {@link ShrimpView}.
 * It exposes the following operations:
 * <ul>
 * <li>layouts</li>
 * <li>selecting nodes</li>
 * <li>navigation (zooming and panning)
 * <li>setting the default label mode</li>
 * <li>choosing the nesting hierarchy</li>
 * <li>filtering nodes and arcs</li>
 * <li>pruning and collapsing nodes</li>
 * <li>taking snapshots</li>
 * <li>group and ungroup nodes</li>
 * <li>running a quick view</li>
 * <li>running a quick search</li>
 * <li>moving and resizing nodes</li>
 * <li>changing node colors, shapes, and borders for a node type</li>
 * <li>changing arc colors and styles for an arc type</li>
 * <li>changing the node label and overlay icon
 * </ul>
 *
 * @see ScriptingConstants
 * @author Chris Callendar, Chris Bennett
 */
public interface MainViewScriptingBean {

	/**
	 * Selects the nodes identified by <code>name</code>.
	 *
	 * @param name String name of the node.
	 * @param selectAllOccurances whether to select all occurances of nodes with this name or just the first one
	 * @param clearPreviouslySelected boolean whether to clear previous selection.
	 * @param exactMatch whether or not match the name exactly
	 * @param caseSensitive whether or not to consider case in the match.
	 */
	public void selectNodesByName(String name, boolean selectAllOccurances, boolean clearPreviouslySelected, boolean exactMatch, boolean caseSensitive);

	/**
	 * Selects the root nodes.
	 */
	public void selectRoots();

	/**
	 * Selects all nodes of a specific type
	 * @param type see the node filter dialog to see the available node types
	 * @param clearPreviouslySelected clears the previously selected nodes
	 */
	public void selectNodesByType(String type, boolean clearPreviouslySelected);

	/**
	 * De-selects the currently selected nodes, and selects their unselected siblings instead.
	 */
	public void selectInverseSiblings();

	/**
	 * Shows the children (if any) of the selected node(s).
	 */
	public void openSelectedNodes();

	/**
	 * Closes the selected node(s) (hides the children if showing).
	 */
	public void closeSelectedNodes();

	/**
	 * Open all descendants of the selected node(s).
	 */
	public void openAllDescendents();

	/**
	 * Close all descendants of the selected node(s).
	 */
	public void closeAllDescendents();

	/**
	 * Change the child-parent relationships.
	 * @param cprels new child-parent relationships which defines the nested hierarchy
	 */
	public void setHierarchy(String[] cprels);

	/**
	 * Change the display of labels.
	 * <p> {@link ScriptingConstants#SCALE_BY_NODE_SIZE} produces the fastest drawing time
	 * @param mode one of {@link ScriptingConstants#FIXED},
	 * {@link ScriptingConstants#SCALE_BY_NODE_SIZE}, or {@link ScriptingConstants#SCALE_BY_LEVEL}
	 */
	public void setLabelMode(String mode);

	/**
	 * Sets the label style for a node type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param style the label style
	 * @see ScriptingConstants#LABEL_STYLE_FULL
	 * @see ScriptingConstants#LABEL_STYLE_HIDE
	 * @see ScriptingConstants#LABEL_STYLE_ELIDE_LEFT
	 * @see ScriptingConstants#LABEL_STYLE_ELIDE_RIGHT
	 */
	public void setLabelStyleByType(String nodeType, String style);

	/**
	 * Take a snapShot of the MainView and add it to the {@link FilmStrip}.
	 * @param askForComments whether to ask for comments.
	 */
	public void takeSnapShot(boolean askForComments);

	/**
	 * Select all the children of the selected node(s).
	 */
	public void selectAllChildren();

	/**
	 * Select all the descendants of the selected node(s).
	 */
	public void selectAllDescendents();

	/**
	 * Layout children of the selected node(s).
	 * @param newLayoutMode String new layout mode
	 * @see ScriptingConstants
	 * @see ScriptingConstants#GRID_BY_TYPE
	 * @see ScriptingConstants#SPRING
	 * @see ScriptingConstants#TREE_HORIZONTAL
	 * @see ScriptingConstants#TREE_VERTICAL
 	 * @see ScriptingConstants#TREEMAP
	 * @see ScriptingConstants#RADIAL
	 */
	public void layoutChildren(String newLayoutMode);

	/**
	 * Layout selected node(s).
	 * @param newLayoutMode String new layout mode
	 * @see ScriptingConstants
	 * @see ScriptingConstants#GRID_BY_TYPE
	 * @see ScriptingConstants#SPRING
	 * @see ScriptingConstants#TREE_HORIZONTAL
	 * @see ScriptingConstants#TREE_VERTICAL
 	 * @see ScriptingConstants#TREEMAP
	 * @see ScriptingConstants#RADIAL
	 */
	public void layoutSelected(String newLayoutMode);

	/**
	 * Filter the selected node(s).
	 */
	public void filterSelectedNodes();

	/**
	 * Hides the selected node(s).
	 * This does the same thing as {@link MainViewScriptingBean#filterSelectedNodes()}.
	 */
	public void hideSelectedNodes();

	/**
	 * Groups the selected node(s).
	 * Grouped nodes are visually combined into one node (usually with a new name).
	 * All incoming and outgoing relationships are re-routed to this new node.
	 * @see MainViewScriptingBean#ungroupSelectedNodes()
	 */
	//@tag Shrimp.grouping
	public void groupSelectedNodes();

	/**
	 * Ungroup the selected node(s).
	 * @see MainViewScriptingBean#groupSelectedNodes()
	 */
	//@tag Shrimp.grouping
	public void ungroupSelectedNodes();

	/**
	 * Renames the selected node.
	 */
	public void renameSelectedNode();

	/**
	 * Filters or unfilters all arcs from the display
	 * @param filter show or hide all arcs
	 */
	public void filterAllArcs(boolean filter);

	/**
	 * Filters or unfilters all nodes from the display
	 * @param filter show or hide all nodes
	 */
	public void filterAllNodes(boolean filter);

	/**
	 * Filters or unfilters arcs of String type from the display
	 * @param type String type of the arcs - see the arc filter dialog for the available arc types
	 * @param filter boolean whether to show or hide the arcs.
	 */
	public void filterArcsByType(String type, boolean filter);

	/**
	 * Filters or unfilters nodes of String type from the display.
	 *
	 * @param type type of the nodes - see the node filter dialog for the available arc types
	 * @param filter whether to show or hide the nodes.
	 */
	public void filterNodesByType(String type, boolean filter);

	/**
	 * Filters or unfilters artifacts of a specific type from the data.
	 * Nodes of the same type will be filtered or unfiltered from the display.
	 * @param type the artifact type
	 * @param filter show or hide artifacts of the given type
	 */
	public void filterArtifactDataType(String type, boolean filter);

	/**
	 * Filters or unfilters relationships of a specific type from the data.
	 * Arcs of the same type will be filtered or unfiltered from the display.
	 * @param type the relationship type
	 * @param filter show or hide relationships of the given type
	 */
	public void filterRelationshipDataType(String type, boolean filter);

	/**
	 * Filters or unfilters all artifact types from the data.
	 * All nodes will be filtered or unfiltered from the display.
	 * @param filter show or hide all artifacts
	 */
	public void filterAllArtifactDataTypes(boolean filter);

	/**
	 * Filters or unfilters all relationship types from the data.
	 * All arcs will be filtered or unfiltered from the display.
	 * @param filter show or hide all relationships
	 */
	public void filterAllRelationshipDataTypes(boolean filter);

	/**
	 * Unfilter all the previously filtered artifacts.
	 */
	public void unFilterAllArtifacts();

	/**
	 * Shows all hidden nodes.
	 * This does the same as the {@link MainViewScriptingBean#unFilterAllArtifacts()} method.
	 */
	public void showAllHiddenNodes();

	/**
	 * Prunes the subgraph for each of the selected nodes.
	 * A subgraph is determined be following all the outgoing relationships
	 * for each node.  For a simple tree, pruning a single node will hide that
	 * node and all its descendants (children, grandchildren etc).
	 */
	public void pruneSelectedNodes();

	/**
	 * Prompts the user to select one or more files to attach
	 * to the selected node.
	 */
	public void attachDocumentToSelectedNode();

	/**
	 * Views the documents for the first selected node.
	 * This will focus on (zoom in) the selected node and display
	 * the documents in a table for viewing.
	 */
	public void viewSelectedNodeDocuments();

	/**
	 * Prompts the user to choose an image to use as the icon for the selected node.
	 */
	public void changeSelectedNodeLabelIcon();

	/**
	 * Displays a dialog to the user allowing him/her to change the overlay icon
	 * and the position for the icon.
	 */
	public void changeSelectedNodeOverlayIcon();

	/**
	 * Expands or collapses the selected node(s).
	 * If a node is collapsed then it will be expanded and vice versa.
	 * Collapsing a node causes the its descendants to be hidden.
	 */
	public void expandCollapseSelectedNodes();

	/**
	 * Zooms back to the root node.
	 */
	public void focusOnHome();

	/**
	 * Zooms in on the first selected node.
	 */
	public void focusOnSelectedNode();

	/**
	 * Redo the action that was previously undone.
	 */
	public void redoAction();

	/**
	 * Undo an action. NOTE: not all actions a user performs can be undone.
	 */
	public void undoAction();

	/**
	 * Sets the zoom mode for the MainView. This mode determines what happens when a user tries to zoom
	 * using the proper input (X, Z keys on the keyboard or the middle mouse button are the defaults).
	 * @param newMode the new zoom mode
	 * @see ScriptingConstants#ZOOM
	 * @see ScriptingConstants#MAGNIFY
	 * @see ScriptingConstants#FISHEYE
	 */
	public void changeZoomMode(String newMode);

	/**
	 * Causes the view to navigate to the node with the given name.
	 * If there are multiple nodes in the display with the same name,
	 * the first one found will be used.
	 * @param name the name of the node to navigate to
	 */
	public void navigateToNode(String name);

	/**
	 * Displays a non-modal dialog with the given message.
	 * @param message the message to display
	 */
	public void showDialog(String message);

	/**
	 * Displays a non-modal dialog with a message and title
	 * @param message the message to display
	 * @param title	title for the dialog
	 */
	public void showDialog(String message, String title);

	/**
	 * Displays a modal dialog with no title
	 * @param message the message to display
	 */
	public void alert(String message);

	/**
	 * Displays a modal dialog with a message.
	 * @param message String new message to display
	 * @param title	String the title of the dialog
	 */
	public void alert(String message, String title);

	/**
	 * Displays a modal dialog with an input field.
	 * @param message the message to display
	 * @param title	the title of the dialog
	 * @return the value the user typed
	 */
	public String prompt(String message, String title);

	/**
	 * Pauses any further script operation for <code>time</code> ms.
	 * @param time the pause time in milliseconds.
	 */
	public void pause(long time);

	/**
	 * Causes the display to refresh.
	 */
	public void refresh();

	/**
	 * Selects all nodes that are connected to the currently selected nodes.
	 */
	public void selectConnectedNodes();

	/**
	 * Creates composite arcs for the given types.
	 * @param arcTypes the art types to form composite arcs from
	 */
	public void createComposites(String[] arcTypes);

	/**
	 * Removes the composite arcs.
	 */
	public void clearComposites();

	/**
	 * Runs the quick view with the given name.
	 * See the {@link ScriptingConstants} class for most of the quick view names.
	 * Note that not all quick views are necessarily available for the open project.
	 * If a quick views doesn't exist an exception is thrown.
	 * If a quick view exists but isn't valid then an exception is also thrown.
	 * @see ScriptingConstants#QUICK_VIEW_DEFAULT
	 * @see MainViewScriptingBean#runDefaultQuickView()
	 * @param name
	 */
	public void runQuickView(String name);

	/**
	 * Runs the default quick view.
	 * @see ScriptingConstants#QUICK_VIEW_DEFAULT
 	 * @see MainViewScriptingBean#runDefaultQuickView()
	 */
	public void runDefaultQuickView();

	/**
	 * Performs a quick search.  Any nodes with a name
	 * that <b>contains</b> the given search text will be
	 * selected. Any nodes not in the neighborhood of matching nodse
	 * will be filtered.
	 * @param searchText the text to search
	 * @see ScriptingConstants#CONTAINS
	 */
	public void quickSearch(String searchText);

	/**
	 * Performs a quick search.
	 * @param searchText the text to search
	 * @param stringMatchingMode the matching mode
	 * @see ScriptingConstants#CONTAINS
	 * @see ScriptingConstants#STARTS_WITH
	 * @see ScriptingConstants#ENDS_WITH
	 * @see ScriptingConstants#EXACT_MATCH
	 * @see ScriptingConstants#REGEXP
	 */
	public void quickSearch(String searchText, int stringMatchingMode);

	/**
	 * Sets the background color for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param color the color - can either be one of Java's 16 predefined colors in (e.g. "red"),
	 * 	or a hex string (e.g. "#ff0000"), or an RGB or RGBA value (e.g. "255,0,0" or "255,0,0,255").
	 * @see ScriptingConstants#NODE_TYPE_CLASS
	 */
	public void setNodeColorByType(String nodeType, String color);

	/**
	 * Sets the outer border color for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param color the color - can either be one of Java's 16 predefined colors in (e.g. "red"),
	 * 	or a hex string (e.g. "#ff0000"), or an RGB or RGBA value (e.g. "255,0,0" or "255,0,0,255").
	 * @see ScriptingConstants#NODE_TYPE_CLASS
	 */
	public void setNodeOuterBorderColorByType(String nodeType, String color);

	/**
	 * Sets the outer border style for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param borderStyle the border style
	 * @see ScriptingConstants#BORDER_STYLE_PLAIN
	 * @see ScriptingConstants#BORDER_STYLE_DASHED
	 */
	public void setNodeOuterBorderStyleByType(String nodeType, String borderStyle);

	/**
	 * Sets the outer border color for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param color the color - can either be one of Java's 16 predefined colors in (e.g. "red"),
	 * 	or a hex string (e.g. "#ff0000"), or an RGB or RGBA value (e.g. "255,0,0" or "255,0,0,255").
	 * @see ScriptingConstants#NODE_TYPE_CLASS
	 */
	public void setNodeInnerBorderColorByType(String nodeType, String color);

	/**
	 * Sets the inner border style for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param borderStyle the border style
	 * @see ScriptingConstants#BORDER_STYLE_PLAIN
	 * @see ScriptingConstants#BORDER_STYLE_DASHED
	 */
	public void setNodeInnerBorderStyleByType(String nodeType, String borderStyle);

	/**
	 * Sets the nod shape for nodes of the given type.
	 * @param nodeType the node type - see {@link ScriptingConstants}
	 * @param shape the node shape
	 * @see ScriptingConstants#NODE_SHAPE_DEFAULT
	 */
	public void setNodeShapeByType(String nodeType, String shape);

	/**
	 * Sets the color for the given arc type.
	 * @param arcType the arc type - see {@link ScriptingConstants}
	 * @param color the color - can either be one of Java's 16 predefined colors in (e.g. "red"),
	 * 	or a hex string (e.g. "#ff0000"), or an RGB or RGBA value (e.g. "255,0,0" or "255,0,0,255").
	 */
	public void setArcColorByType(String arcType, String color);

	/**
	 * Sets the arc style for the given arc type.
	 * @param arcType the arc type - see {@link ScriptingConstants}
	 * @param arcStyle the border style
	 * @see ScriptingConstants#ARC_STYLE_STRAIGHT_SOLID
	 * @see ScriptingConstants#ARC_STYLE_STRAIGHT_DASHED
 	 * @see ScriptingConstants#ARC_STYLE_CURVED_SOLID
 	 * @see ScriptingConstants#ARC_STYLE_CURVED_DASHED
	 */
	public void setArcStyleByType(String arcType, String arcStyle);

	/**
	 * Shifts the selected nodes by the given offsets.
	 * @param dx horizontal shift
	 * @param dy vertical shift
	 */
	public void moveSelectedNodesBy(double dx, double dy);

	/**
	 * Repositions the selected nodes to the new coordinates (relative to parent).
	 * @param x new x location
	 * @param y new y location
	 */
	public void moveSelectedNodesTo(double x, double y);

	/**
	 * Resizes the selected nodes by the given amounts.
	 * @param dw the amount to grow or shrink the selected nodes widths
	 * @param dh the amount to grow or shrink the selected nodes heights
	 */
	public void resizeSelectedNodesBy(double dw, double dh);

	/**
	 * Resizes each of the selected nodes to the given width and height.
	 * @param w the width for each selected node
	 * @param h the height for each selected node
	 */
	public void resizeSelectedNodesTo(double w, double h);

}
