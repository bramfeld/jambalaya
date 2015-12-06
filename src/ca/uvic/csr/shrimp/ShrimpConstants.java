/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

import java.awt.Color;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;

/**
 * This class collects constants to be used for scripting in Shrimp.
 * @author Nasir Rather, Chris Callendar
 */
public class ShrimpConstants {

	/** Default background color (chisel blue). */
	public static final Color SHRIMP_BACKGROUND_COLOR = new Color(0, 46, 123);

	/** Zoom mode - ZOOM */
	public static final String ZOOM = DisplayConstants.ZOOM;

	/** Zoom mode - MAGNIFY */
	public static final String MAGNIFY = DisplayConstants.MAGNIFY;

	/** Zoom mode - FISHEYE */
	public static final String FISHEYE = DisplayConstants.FISHEYE;

	// label modes

	/** Label mode - Above Node (fixed) */
	public static final String FIXED = DisplayConstants.LABEL_MODE_FIXED;

	/** Label mode - On Node */
	public static final String SCALE_BY_NODE_SIZE = DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE;

	/** Label mode - Above Node (level) */
	public static final String SCALE_BY_LEVEL = DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL;

	// layout names

	/**
	 * Layout method - Grid layout alphabetically
	 */
	public static final String GRID_BY_ALPHA = LayoutConstants.LAYOUT_GRID_BY_ALPHA;

	/**
	 * Layout method - Grid layout by number of children
	 */
	public static final String GRID_BY_NUM_CHILDREN = LayoutConstants.LAYOUT_GRID_BY_NUM_CHILDREN;

	/**
	 * Layout method - Grid layout by number of relationships
	 */
	public static final String GRID_BY_NUM_RELS = LayoutConstants.LAYOUT_GRID_BY_NUM_RELS;

	/**
	 * Layout method - Grid layout by artifact type
	 */
	public static final String GRID_BY_TYPE = LayoutConstants.LAYOUT_GRID_BY_TYPE;

	/**
	 * Layout method - Grid layout by attribute
	 */
	public static final String GRID_BY_ATTRIBUTE = LayoutConstants.LAYOUT_GRID_BY_ATTRIBUTE;

	/**
	 * Layout method - Spring layout
	 */
	public static final String SPRING = LayoutConstants.LAYOUT_SPRING;

	/**
	 * Layout method - Tree layout horizontal
	 */
	public static final String TREE_HORIZONTAL = LayoutConstants.LAYOUT_TREE_HORIZONTAL;

	/**
	 * Layout method - Tree layout vertical
	 */
	public static final String TREE_VERTICAL = LayoutConstants.LAYOUT_TREE_VERTICAL;

	/**
	 * Layout method - Radial layout
	 */
	public static final String RADIAL = LayoutConstants.LAYOUT_RADIAL;

	/**
	 * Layout method - Treemap layout
	 */
	public static final String TREEMAP = LayoutConstants.LAYOUT_TREEMAP;

	/**
	 * Layout method - LAYOUT_UML layout
	 */
	public static final String UML = LayoutConstants.LAYOUT_UML;

	// panel modes

	/**
	 * Panel mode - CLOSED
	 */
	public static final String CLOSED = PanelModeConstants.CLOSED;

	/**
	 * Panel mode - Show children
	 */
	public static final String CHILDREN = PanelModeConstants.CHILDREN;

	/**
	 * The default arc group for arcs with no other group specified.
	 */
	public static final String DEFAULT_GROUP = "Default Group";

	// MENU CONSTANTS
	public static final String ACTION_NAME_SHOW = "Show";
	public static final String ACTION_NAME_SELECT = "Select";
	public static final String ACTION_NAME_SELECT_INVERSE = "Select Inverse";
	public static final String ACTION_NAME_INVERSE_SIBLINGS = "Inverse Siblings";
	public static final String ACTION_NAME_NONE = "None";
	public static final String ACTION_NAME_LABELS = "Labels";
	public static final String ACTION_NAME_GRID = "Grid";
	public static final String ACTION_NAME_ARRANGE_CHILDREN = "Arrange Children";
	public static final String ACTION_NAME_ARRANGE_SELECTED_ITEMS = "Arrange Selected Items";
	public static final String ACTION_NAME_ATTACH_DOCUMENT = "Attach A Document";
	public static final String ACTION_NAME_ATTACH_URL = "Attach A URL";
	public static final String ACTION_NAME_VIEW_DOCUMENTS = "View Documents & Annotations";
	public static final String ACTION_NAME_REMOVE_DOCUMENTS = "Remove Documents";
	public static final String ACTION_NAME_REMOVE_ALL_DOCUMENTS = "Remove All Documents";
	public static final String ACTION_NAME_CHANGE_LABEL_ICON = "Change Label Icon";
	public static final String ACTION_NAME_REMOVE_LABEL_ICON = "Remove Label Icon";
	public static final String ACTION_NAME_CHANGE_OVERLAY_ICON = "Change Overlay Icon";
	public static final String ACTION_NAME_REMOVE_OVERLAY_ICON = "Remove Overlay Icon";

	public static final String MENU_FILE = "File";
	public static final String MENU_EDIT = "Edit";
	public static final String MENU_NODE = "Node";
	public static final String MENU_ARC = "Arc";
	public static final String MENU_NAVIGATE = "Navigate";
	public static final String MENU_TOOLS = "Tools";
	public static final String MENU_HELP = "Help";
	public static final String MENU_WINDOW = "Window";
	public static final String MENU_DEMO = "Demo";
	public static final String MENU_MORETOOLS = "More Tools";
	public static final String MENU_SCRIPT = "Script";
	public static final String MENU_LANGUAGE = "Language";
	public static final String MENU_QUICK_VIEWS = "Quick Views";
	public static final String MENU_ARRANGE = "Arrange";
	public static final String MENU_OPEN_RECENT = "Open Recent";
	public static final String MENU_TOOLS_MORETOOLS = MENU_TOOLS + "/" + MENU_MORETOOLS;
	public static final String MENU_NODE_SHOW = MENU_NODE + "/" + ACTION_NAME_SHOW;
	public static final String MENU_NODE_SELECT = MENU_NODE + "/" + ACTION_NAME_SELECT;
	public static final String MENU_NODE_LABELS = MENU_NODE + "/" + ACTION_NAME_LABELS;
	public static final String MENU_NODE_ARRANGE_CHILDREN = MENU_NODE + "/" + ACTION_NAME_ARRANGE_CHILDREN;
	public static final String MENU_NODE_ARRANGE_CHILDREN_GRID = MENU_NODE_ARRANGE_CHILDREN + "/" + ACTION_NAME_GRID;
	public static final String MENU_NODE_ARRANGE_SELECTED_ITEMS = MENU_NODE + "/" + ACTION_NAME_ARRANGE_SELECTED_ITEMS;
	public static final String MENU_NODE_VIEW_DOCUMENTS = MENU_NODE + "/" + ACTION_NAME_VIEW_DOCUMENTS;
	public static final String MENU_NODE_REMOVE_DOCUMENTS = MENU_NODE + "/" + ACTION_NAME_REMOVE_DOCUMENTS;
	public static final String MENU_FILE_OPEN_RECENT = MENU_FILE + "/" + MENU_OPEN_RECENT;

	// ACTION NAME CONSTANTS
	public static final String ACTION_NAME_GRID_LAYOUT = "Grid Layout";
	public static final String ACTION_NAME_SPRING_LAYOUT = "Spring Layout";
	public static final String ACTION_NAME_SPRING_LAYOUT_ADVANCED = "Spring Layout (advanced)";
	public static final String ACTION_NAME_RADIAL_LAYOUT = "Radial Layout";
	public static final String ACTION_NAME_RADIAL_LAYOUT_INVERTED = "Radial Layout (Inverted)";
	public static final String ACTION_NAME_TREEMAP_LAYOUT = "TreeMap Layout";
	public static final String ACTION_NAME_TOUCHGRAPH_LAYOUT = "TouchGraph Layout";
	public static final String ACTION_NAME_FORCE_DIRECTED_LAYOUT = "Force Directed Layout";
	public static final String ACTION_NAME_TREE_LAYOUT_HORIZONTAL = "Tree Layout - Horizontal";
	public static final String ACTION_NAME_TREE_LAYOUT_HORIZONTAL_INVERTED = "Tree Layout - Horizontal (Inverted)";
	public static final String ACTION_NAME_TREE_LAYOUT_VERTICAL = "Tree Layout - Vertical";
	public static final String ACTION_NAME_TREE_LAYOUT_VERTICAL_INVERTED = "Tree Layout - Vertical (Inverted)";
	public static final String ACTION_NAME_HORIZONTAL_LAYOUT = "Horizontal Layout";
	public static final String ACTION_NAME_HORIZONTAL_LAYOUT_NO_OVERLAP = "Horizontal Layout (no overlapping labels)"; // @tag
																														// Shrimp(HorizontalLayout(NoOverlap))
	public static final String ACTION_NAME_VERTICAL_LAYOUT = "Vertical Layout";
	public static final String ACTION_NAME_UML_LAYOUT = "LAYOUT_UML Layout";
	public static final String ACTION_NAME_CASCADE_LAYOUT = "Cascade Layout";
	public static final String ACTION_NAME_ORTHOGONAL_LAYOUT = "Orthogonal Layout";
	public static final String ACTION_NAME_HIERACHICAL_LAYOUT = "Hierarchical Layout";
	// @tag Shrimp.sugiyama
	public static final String ACTION_NAME_SUGIYAMA_LAYOUT = "Sugiyama Layout";

	public static final String ACTION_NAME_EXIT = "Exit";
	public static final String ACTION_NAME_CASCADE = "Cascade";
	public static final String ACTION_NAME_TILE = "Tile";
	public static final String ACTION_NAME_CLOSE_ALL = "Close All";
	public static final String ACTION_NAME_CLOSE = "Close";
	public static final String ACTION_NAME_OPEN = "Open...";
	public static final String ACTION_NAME_OPEN_RECENT = "Open Recent";
	public static final String ACTION_NAME_SAVE = "Save";
	public static final String ACTION_NAME_SAVE_AS = "Save As...";

	public static final String ACTION_NAME_BACK = "Back";
	public static final String ACTION_NAME_FORWARD = "Forward";
	public static final String ACTION_NAME_HOME = "Home";
	public static final String ACTION_NAME_REFRESH = "Refresh";
	public static final String ACTION_NAME_CHANGE_ROOT_CLASSES = "Change Root Classes...";

	public static final String ACTION_NAME_SELECT_TOOL = "Select Tool";
	public static final String ACTION_NAME_ZOOM_IN_TOOL = "Zoom In Tool";
	public static final String ACTION_NAME_ZOOM_OUT_TOOL = "Zoom Out Tool";
	public static final String ACTION_NAME_ZOOM_MODE = "Zoom Mode";
	public static final String ACTION_NAME_ZOOM_IN_ZOOM_MODE = "Zoom In (Zoom Mode)";
	public static final String ACTION_NAME_ZOOM_IN_ANY_MODE = "Zoom In (Any Mode)";
	public static final String ACTION_NAME_ZOOM_OUT_ZOOM_MODE = "Zoom Out (Zoom Mode)";
	public static final String ACTION_NAME_ZOOM_OUT_ANY_MODE = "Zoom Out (Any Mode)";
	public static final String ACTION_NAME_MAGNIFY_MODE = "Magnify Mode";
	public static final String ACTION_NAME_MAGNIFY_IN = "Magnify In";
	public static final String ACTION_NAME_MAGNIFY_OUT = "Magnify Out";
	public static final String ACTION_NAME_FISHEYE_MODE = "Fisheye Mode";
	public static final String ACTION_NAME_FISHEYE_IN = "Fisheye In";
	public static final String ACTION_NAME_FISHEYE_OUT = "Fisheye Out";

	public static final String ACTION_NAME_PAN_NORTH = "Pan North";
	public static final String ACTION_NAME_PAN_EAST = "Pan East";
	public static final String ACTION_NAME_PAN_SOUTH = "Pan South";
	public static final String ACTION_NAME_PAN_WEST = "Pan West";

	// Tools
	public static final String TOOL_HIERARCHICAL_VIEW = "Hierarchical View";
	public static final String TOOL_THUMBNAIL_VIEW = "Thumbnail View";
	public static final String TOOL_SCRIPTING = "Scripting";
	public static final String TOOL_SEARCH = "Search";
	public static final String ACTION_NAME_ARC_FILTER = "Arc Filter";
	public static final String ACTION_NAME_NODE_FILTER = "Node Filter";
	public static final String TOOL_FILTERS = "Filters";
	public static final String ACTION_NAME_FILMSTRIP = "Filmstrip";
	public static final String TOOL_NODE_ATTRIBUTE_PANEL = "Node Attribute Panel";
	public static final String TOOL_ARC_ATTRIBUTE_PANEL = "Arc Attribute Panel";
	public static final String TOOL_SHRIMP_VIEW = "ShrimpView";
	public static final String TOOL_QUERY_VIEW = "Query View";
	public static final String TOOL_QUICK_VIEWS = "Quick Views";

	public static final String ACTION_NAME_ATTRIBUTES_PANEL = "Attributes Panel";
	public static final String ACTION_NAME_SEND_FEEDBACK = "Send Feedback!";

	public static final String ACTION_NAME_GRID_ALPHABETICAL = "Alphabetical Order";
	public static final String ACTION_NAME_GRID_BY_CHILDREN = "By Num of Children";
	public static final String ACTION_NAME_GRID_BY_RELATIONSHIPS = "By Num of Relationships";
	public static final String ACTION_NAME_GRID_BY_TYPE = "By Type";
	public static final String ACTION_NAME_GRID_BY_ATTRIBUTE = "By Other Attribute...";

	public static final String ACTION_NAME_LABEL_OPTIONS = "Label Options";

	public static final String ACTION_NAME_NODE_PROPERTIES = "Properties";
	public static final String ACTION_NAME_ARC_PROPERTIES = "Properties";
	public static final String ACTION_NAME_OPEN_CLOSE_NODE = "Open/Close Node";
	public static final String ACTION_NAME_UNFILTER = "Unfilter";
	public static final String ACTION_NAME_SHOW_ALL_NODES = "Show All Hidden Nodes";
	public static final String ACTION_NAME_SHOW_HIDDEN_ARCS = "Show Hidden Arcs";
	public static final String ACTION_NAME_PRUNE = "Prune";
	public static final String ACTION_NAME_UNPRUNE = "Unprune";
	public static final String ACTION_NAME_HIDE = "Hide";
	public static final String ACTION_NAME_HIDE_ARC = "Hide Arc";
	public static final String ACTION_NAME_HIDE_ANCESTORS = "Hide Ancestors (incoming arcs)";
	public static final String ACTION_NAME_HIDE_DESCENDANTS = "Hide Descendants (outgoing arcs)";
	public static final String ACTION_NAME_FOCUS_ON = "Focus On";
	public static final String ACTION_NAME_RESTORE_ROOT_NODE = "Restore Original Root Node";
	public static final String ACTION_NAME_MAKE_ROOT_NODE = "Make Root Node";
	public static final String ACTION_NAME_COLLAPSE = "Collapse";
	public static final String ACTION_NAME_EXPAND_X_LEVELS = "Expand <X> Levels ...";
	public static final String ACTION_NAME_EXPAND = "Expand";
	public static final String ACTION_NAME_EXPAND_ALL_DESCENDANTS = "Expand All Descendants";
	public static final String ACTION_NAME_COLLAPSE_ALL_DESCENDANTS = "Collapse All Descendants";
	public static final String ACTION_NAME_EXPAND_COLLAPSE_SUBGRAPH = "Expand/Collapse Subgraph";
	public static final String ACTION_NAME_GROUP = "Group"; // @tag Shrimp(grouping)
	public static final String ACTION_NAME_UNGROUP = "Ungroup";
	public static final String ACTION_NAME_RENAME = "Rename";
	public static final String ACTION_NAME_LONG_TOOLTIPS = "Use Long Tooltips";
	public static final String ACTION_NAME_SELECT_ALL_CHILDREN = "All Children";
	public static final String ACTION_NAME_SELECT_ALL_DESCENDANTS = "All Descendants";

	public static final String ACTION_NAME_ABOUT = "About";
	public static final String ACTION_NAME_ONLINE_MANUAL = "Online Manual";
	public static final String SHRIMP_MANUAL_WEBSITE = "http://www.thechiselgroup.org/shrimp_manual";
	public static final String ACTION_NAME_SHRIMP_WEBSITE = "Shrimp Website";
	public static final String SHRIMP_WEBSITE = "http://www.thechiselgroup.org/shrimp";
	public static final String SHRIMP_QUICKVIEWS_WEBSITE = "http://www.thechiselgroup.org/shrimp_manual_quickviews";
	public static final String SHRIMP_WHATSNEW_WEBSITE = "http://www.thechiselgroup.org/shrimp_new";
	public static final String ACTION_NAME_JAMBALAYA_WEBSITE = "Jambalaya Website";
	public static final String JAMBALAYA_WEBSITE = "http://www.thechiselgroup.org/jambalaya";
	public static final String JAMBALAYA_MANUAL_WEBSITE = "http://www.thechiselgroup.org/jambalaya_manual";
	public static final String ACTION_NAME_CREOLE_WEBSITE = "Creole Website";
	public static final String CREOLE_WEBSITE = "http://www.thechiselgroup.org/creole";

	public static final String ACTION_NAME_PROPERTIES = "Properties...";
	public static final String ACTION_NAME_OPTIONS = "Options";
	public static final String ACTION_NAME_SNAPSHOT = "Snapshot";

	public static final String GROUP_A = "Group-A";
	public static final String GROUP_B = "Group-B";
	public static final String GROUP_C = "Group-C";
	public static final String GROUP_D = "Group-D";
	public static final String GROUP_E = "Group-E";
	public static final String GROUP_F = "Group-F";

}