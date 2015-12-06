/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.ScriptingBean;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.CurvedSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightDottedLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.StraightSolidLineArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.DiamondNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.DropShadowRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.EllipseNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.InvertedTriangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RectangleTriangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.RoundedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.StackedRectangleNodeShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.TriangleNodeShape;
import ca.uvic.csr.shrimp.SearchBean.Matcher;
import ca.uvic.csr.shrimp.gui.quickview.DefaultViewAction;
import ca.uvic.csr.shrimp.gui.quickview.NestedTreemapViewAction;
import ca.uvic.csr.shrimp.jambalaya.JambalayaApplication;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * This class collects constants to be used for scripting in Shrimp.
 *
 * @see MainViewScriptingBean
 * @author Chris Callendar
 */
public final class ScriptingConstants {

	// protected to hide from javadoc
	protected ScriptingConstants() {
	}

	///////////////////////////
	// Zoom Modes
	///////////////////////////

	/** Zoom mode - ZOOM */
	public static final String ZOOM = DisplayConstants.ZOOM;

	/** Zoom mode - MAGNIFY */
	public static final String MAGNIFY = DisplayConstants.MAGNIFY;

	/** Zoom mode - FISHEYE */
	public static final String FISHEYE = DisplayConstants.FISHEYE;


	///////////////////////////
	// Label Modes
	///////////////////////////

	/** Label mode - Above Node (fixed) */
	public static final String FIXED = DisplayConstants.LABEL_MODE_FIXED;

	/** Label mode - On Node */
	public static final String SCALE_BY_NODE_SIZE = DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE;

	/** Label mode - Above Node (level) */
	public static final String SCALE_BY_LEVEL = DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL;

	/** Label style - full label */
	public static final String LABEL_STYLE_FULL = DisplayConstants.LABEL_STYLE_FULL;
	/** Label style - hidden */
	public static final String LABEL_STYLE_HIDE = DisplayConstants.LABEL_STYLE_HIDE;
	/** Label style - elide left */
	public static final String LABEL_STYLE_ELIDE_LEFT = DisplayConstants.LABEL_STYLE_ELIDE_LEFT;
	/** Label style - elide right */
	public static final String LABEL_STYLE_ELIDE_RIGHT = DisplayConstants.LABEL_STYLE_ELIDE_RIGHT;


	///////////////////////////
	// Layout Constants
	///////////////////////////

	/** Layout method - Grid layout alphabetically */
	public static final String GRID_BY_ALPHA = LayoutConstants.LAYOUT_GRID_BY_ALPHA;

	/** Layout method - Grid layout by number of children */
	public static final String GRID_BY_NUM_CHILDREN = LayoutConstants.LAYOUT_GRID_BY_NUM_CHILDREN;

	/** Layout method - Grid layout by number of relationships */
	public static final String GRID_BY_NUM_RELS = LayoutConstants.LAYOUT_GRID_BY_NUM_RELS;

	/** Layout method - Grid layout by artifact type */
	public static final String GRID_BY_TYPE = LayoutConstants.LAYOUT_GRID_BY_TYPE;

	/** Layout method - Grid layout by attribute */
	public static final String GRID_BY_ATTRIBUTE = LayoutConstants.LAYOUT_GRID_BY_ATTRIBUTE;

	/** Layout method - Spring layout */
	public static final String SPRING = LayoutConstants.LAYOUT_SPRING;

	/** Layout method - Tree layout horizontal */
	public static final String TREE_HORIZONTAL = LayoutConstants.LAYOUT_TREE_HORIZONTAL;

	/** Layout method - Tree layout vertical */
	public static final String TREE_VERTICAL = LayoutConstants.LAYOUT_TREE_VERTICAL;

	/** Layout method - Radial layout */
	public static final String RADIAL = LayoutConstants.LAYOUT_RADIAL;

	/** Layout method - Treemap layout (require treemap.jar file) */
	public static final String TREEMAP = LayoutConstants.LAYOUT_TREEMAP;

	/** Layout method - LAYOUT_UML layout */
	public static final String UML = LayoutConstants.LAYOUT_UML;

	/** Layout method - orthogonal layout (control flow diagram). */
	public static final String ORTHOGONAL = LayoutConstants.LAYOUT_ORTHOGONAL;

	/** Layout method - sugiyama hierarchical layout (requires sugiyama.jar file) */
	public static final String SUGIMAYA = LayoutConstants.LAYOUT_SUGIYAMA;

	/** Layout method - force directed (requires extra jar file). */
	public static final String FORCE_DIRECTED = LayoutConstants.LAYOUT_FORCE_DIRECTED;

	/** Layout method - uses a circular motion to highlight the selected node(s) and connected nodes. */
	public static final String MOTION = LayoutConstants.LAYOUT_MOTION;


	///////////////////////////
	// Panel Modes
	///////////////////////////

	/**
	 * Panel mode - CLOSED
	 */
	public static final String CLOSED = PanelModeConstants.CLOSED;

	/**
	 * Panel mode - Show children
	 */
	public static final String CHILDREN = PanelModeConstants.CHILDREN;

	//////////////////////////////
	// Quick View Names
	//////////////////////////////

	/** The default nested quick view. */
	public static final String QUICK_VIEW_DEFAULT = DefaultViewAction.ACTION_NAME;

	/** The nested treemap quick view. */
	public static final String QUICK_VIEW_TREEMAP = NestedTreemapViewAction.ACTION_NAME;

	/** Quick view which shows the classes hierarchy (Protege projects). */
	public static final String QUICK_VIEW_CLASS_TREE = JambalayaApplication.QUICK_VIEW_CLASS_TREE;
	/** Quick view which shows the classes and instances hierarchy (Protege projects). */
	public static final String QUICK_VIEW_CLASS_INSTANCE_TREE = JambalayaApplication.QUICK_VIEW_CLASS_INSTANCE_TREE;
	/** Quick view which shows the classes and individuals tree (Protege OWL projects). */
	public static final String QUICK_VIEW_CLASS_INDIVIDUAL_TREE = JambalayaApplication.QUICK_VIEW_CLASS_INDIVIDUAL_TREE;
	/** Quick view which shows the domain/range view (Protege OWL projects). */
	public static final String QUICK_VIEW_DOMAIN_RANGE = JambalayaApplication.QUICK_VIEW_DOMAIN_RANGE;

	/** Quick view which displays a class hierarchy. */
	public static final String QUICK_VIEW_CLASS_HIERARCHY = JavaDomainConstants.JAVA_QUICK_VIEW_CLASS_HIERARCHY;
	/** Quick view which displays a class and interface hierarchy. */
	public static final String QUICK_VIEW_CLASS_INTERFACE_HIERARCHY = JavaDomainConstants.JAVA_QUICK_VIEW_CLASS_INTERFACE_HIERARCHY;
	/** Quick view which displays a interface hierarchy. */
	public static final String QUICK_VIEW_INTERFACE_HIERARCHY = JavaDomainConstants.JAVA_QUICK_VIEW_INTERFACE_HIERARCHY;

	/** Quick view which displays a call graph. */
	public static final String QUICK_VIEW_CALL_GRAPH = JavaDomainConstants.JAVA_QUICK_VIEW_CALL_GRAPH;
	/** Quick view which displays a control flow graph (requires extra jar files and dlls). */
	public static final String QUICK_VIEW_CONTROL_FLOW_GRAPH = JavaDomainConstants.JAVA_QUICK_VIEW_CONTROL_FLOW_GRAPH;
	/** Quick view which displays a graph as a sequence diagram (requires an extra jar file). */
	public static final String QUICK_VIEW_SEQUENCE_DIAGRAM = JavaDomainConstants.JAVA_QUICK_VIEW_SEQUENCE_DIAGRAM;
	/** Quick view which displays a fan in/out graph. */
	public static final String QUICK_VIEW_FAN_IN_OUT = JavaDomainConstants.JAVA_QUICK_VIEW_FAN_IN_OUT;

	/** Quick view which displays a packaged dependencies access graph. */
	public static final String QUICK_VIEW_PACKAGE_DEPENDENCIES_ACCESSES = JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_ACCESSES;
	/** Quick view which displays a packaged dependencies call graph. */
	public static final String QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS = JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS;
	/** Quick view which displays a packaged dependencies call and access graph. */
	public static final String QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS_ACCESSES = JavaDomainConstants.JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS_ACCESSES;


	//////////////////////////////
	// String Matching Modes
	//////////////////////////////

	/** String matching mode that searches based on containment */
	public static final int CONTAINS = Matcher.CONTAINS_MODE;
	/** String matching mode that searches from the start of a word */
	public static final int STARTS_WITH = Matcher.STARTS_WITH_MODE;
	/** String matching mode that searches from the end of a word */
	public static final int ENDS_WITH = Matcher.ENDS_WITH_MODE;
	/** String matching mode that returns only exact matches */
	public static final int EXACT_MATCH = Matcher.EXACT_MATCH_MODE;
	/** String matching mode that searches using regular expressions */
	public static final int REGEXP = Matcher.REGEXP_MODE;

	///////////////////////
	// Node border styles
	///////////////////////

	/** Default node border style - a solid border */
	public static final String BORDER_STYLE_PLAIN = NodeBorder.PLAIN;
	/** Dashed node border style */
	public static final String BORDER_STYLE_DASHED = NodeBorder.DASHED;


	///////////////////////
	// Node shapes
	///////////////////////

	/** Default node shape - a rectangle */
	public static final String NODE_SHAPE_DEFAULT = RectangleNodeShape.NAME;
	/** Rectangular node shape */
	public static final String NODE_SHAPE_RECTANGLE = RectangleNodeShape.NAME;
	/** Round rectangular node shape */
	public static final String NODE_SHAPE_ROUNDED_RECTANGLE = RoundedRectangleNodeShape.NAME;
	/** Elliptical node shape */
	public static final String NODE_SHAPE_ELLIPSE = EllipseNodeShape.NAME;
	/** Triangular node shape */
	public static final String NODE_SHAPE_TRIANGLE = TriangleNodeShape.NAME;
	/** Inverted triangular node shape */
	public static final String NODE_SHAPE_TRIANGLE_INVERTED = InvertedTriangleNodeShape.NAME;
	/** Diamond node shape */
	public static final String NODE_SHAPE_DIAMOND = DiamondNodeShape.NAME;
	/** Drop shadow rectangular node shape */
	public static final String NODE_SHAPE_DROP_SHADOW = DropShadowRectangleNodeShape.NAME;
	/** Rectangular node shape with a small triangle overlay (used for collapsed nodes) */
	public static final String NODE_SHAPE_RECTANGLE_TRIANGLE = RectangleTriangleNodeShape.NAME;
	/** Stacked rectangle node shape (used for grouped nodes) */
	public static final String NODE_SHAPE_STACKED_RECTANGLE = StackedRectangleNodeShape.NAME;


	//////////////////////////////////////////////////////////
	// Some Default Node Types for Java and Protege projects
	//////////////////////////////////////////////////////////

	/** Java class node type */
	public static final String NODE_TYPE_CLASS = JavaDomainConstants.CLASS_ART_TYPE;
	/** Java interface node type */
	public static final String NODE_TYPE_INTERFACE = JavaDomainConstants.INTERFACE_ART_TYPE;
	/** Java field node type */
	public static final String NODE_TYPE_FIELD = JavaDomainConstants.FIELD_ART_TYPE;
	/** Java constant (static final) node type */
	public static final String NODE_TYPE_CONSTANT = JavaDomainConstants.CONSTANT_ART_TYPE;
	/** Java constructor node type */
	public static final String NODE_TYPE_CONSTRUCTOR = JavaDomainConstants.CONSTRUCTOR_ART_TYPE;
	/** Java method node type */
	public static final String NODE_TYPE_METHOD = JavaDomainConstants.METHOD_ART_TYPE;
	/** Java class file (.class) node type */
	public static final String NODE_TYPE_CLASS_FILE = JavaDomainConstants.CLASS_FILE_ART_TYPE;
	/** Java package fragment node type */
	public static final String NODE_TYPE_PACKAGE_FRAGMENT = JavaDomainConstants.PACKAGE_FRAGMENT_ART_TYPE;
	/** Java package fragment node type */
	public static final String NODE_TYPE_PACKAGE = JavaDomainConstants.PACKAGE_ART_TYPE;

	/** Protege class node type */
	public static final String NODE_TYPE_CLS = ProtegeDataBean.CLASS_ART_TYPE;
	/** Protege instance node type */
	public static final String NODE_TYPE_INSTANCE = ProtegeDataBean.INSTANCE_ART_TYPE;


	//////////////////////////////////////////////////////////
	// Some Default Arc Types for Java and Protege projects
	//////////////////////////////////////////////////////////

	/** Java accesses arc type */
	public static final String ARC_TYPE_ACCESSES = JavaDomainConstants.ACCESSES_REL_TYPE;
	/** Java calls arc type */
	public static final String ARC_TYPE_CALLS = JavaDomainConstants.CALLS_REL_TYPE;
	/** Java contains arc type */
	public static final String ARC_TYPE_CONTAINS = JavaDomainConstants.CONTAINS_REL_TYPE;
	/** Java extended by arc type */
	public static final String ARC_TYPE_EXTENDED_BY = JavaDomainConstants.EXTENDED_BY_REL_TYPE;
	/** Java implemented by arc type */
	public static final String ARC_TYPE_IMPLEMENTED_BY = JavaDomainConstants.IMPLEMENTED_BY_REL_TYPE;
	/** Java method call arc type */
	public static final String ARC_TYPE_METHOD_CALL = JavaDomainConstants.METHOD_CALL_REL_TYPE;

	/** Protege direct instance slot arc type */
	public static final String ARC_TYPE_HAS_INSTANCE = ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE;
	/** Protege direct subclass slot arc type */
	public static final String ARC_TYPE_HAS_SUBCLASS = ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE;


	////////////////
	// Arc Styles
	////////////////

	/** Arc style that is a solid straight line. */
	public static final String ARC_STYLE_STRAIGHT_SOLID = StraightSolidLineArcStyle.NAME;
	/** Arc style that is a dashed straight line. */
	public static final String ARC_STYLE_STRAIGHT_DASHED = StraightDottedLineArcStyle.NAME;
	/** Arc style that is a curved solid line. */
	public static final String ARC_STYLE_CURVED_SOLID = CurvedSolidLineArcStyle.NAME;
	/** Arc style that is a curved dashed line. */
	public static final String ARC_STYLE_CURVED_DASHED = CurvedDottedLineArcStyle.NAME;
	/** Default arc style - solid straight line. */
	public static final String ARC_STYLE_DEFAULT = ARC_STYLE_STRAIGHT_SOLID;

    /**
     * The default arc group for arcs with no other group specified.
     */
    protected static final String DEFAULT_GROUP = "Default Group";



}
