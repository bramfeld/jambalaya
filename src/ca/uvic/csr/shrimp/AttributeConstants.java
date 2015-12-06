/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;


public class AttributeConstants {

    /** Number of Descendents ordinal attribute */
    public static final String ORD_ATTR_NUM_DESCENDENTS = "Number of Descendents";

    /** Number of Children ordinal attribute */
    public static final String ORD_ATTR_NUM_CHILDREN = "Number of Children";

    /** Number of Parents ordinal attribute */
    public static final String ORD_ATTR_NUM_PARENTS = "Number of Parents";

    // @tag Shrimp(TreeMap) : number of relationships [author = ccallendar; date = 21/02/06]
    /** Number of Relationships ordinal attribute */
    public static final String ORD_ATTR_NUM_RELATIONSHIPS = "Number of Relationships";

    /** Artifact name nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_NAME = "Artifact Name";

    /** Artifact color nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_COLOR = "color";

    /** Artifact border outer color nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_OUTER_BORDER_COLOR = "OuterBorderColor";

    /** Artifact border inner color nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_INNER_BORDER_COLOR = "InnerBorderColor";

    /** Artifact outer border style nominal attribute */
	public static final String NOM_ATTR_ARTIFACT_OUTER_BORDER_STYLE = "OuterBorderStyle";

    /** Artifact inner border style nominal attribute */
	public static final String NOM_ATTR_ARTIFACT_INNER_BORDER_STYLE = "InnerBorderStyle";

	/* Artifact attribute for signalling if a node is collapsed. */
    //public static final String NOM_ATTR_COLLAPSED = "collapsed";

    public static final String NOM_ATTR_CLOSED_IMAGE = "ClosedImage";

    /**
     * The artifact attribute key for degree of interest.  The value of this attribute
     * will be a boolean (true/false) indicating if the artifact/node is interesting.
     */
    public static final String NOM_ATTR_ARTIFACT_INTERESTING = "Interest";

    /** The boolean value (true/false) for landmark (very interesting) nodes. */
    public static final String NOM_ATTR_ARTIFACT_LANDMARK = "Landmark";

    /**
     * Artifact long name nominal attribute
     * A longer name used to describe the artifact (ex. a fully qualified name for a java method)
     */
    public static final String NOM_ATTR_ARTIFACT_LONG_NAME = "Artifact Long Name";

    /**
     * Artifact type nominal attribute
     * NOTE: "Node" used instead of "Artifact" because this string is displayed to the user
    */
    public static final String NOM_ATTR_ARTIFACT_TYPE = "Node Type";

    /** Artifact id nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_ID = "Artifact id";

    /** Artifact display text nominal attribute */
    public static final String NOM_ATTR_ARTIFACT_DISPLAY_TEXT = "artifact display text";

    /** Artifact change type nominal attribute */
    public static final String NOM_ATTR_CHANGE_TYPE = "Diff";

    /** "added" change type */
    public static final String NOM_ATTR_CHANGE_VALUE_ADDED = "added";

    /** "changed" change type */
    public static final String NOM_ATTR_CHANGE_VALUE_CHANGED = "changed";

    /** "unchanged" change type */
    public static final String NOM_ATTR_CHANGE_VALUE_UNCHANGED = "unchanged";

    /** "removed" change type */
    public static final String NOM_ATTR_CHANGE_VALUE_REMOVED = "removed";

    /** an "internal" attribute for recording distance along path from a certain source node */
    public static final String ORD_ATTR_PATH_DISTANCE = "path distance";

    /**
     * The relationship type nominal attribute
     * NOTE: "Arc" is used here instead of "relationship" because this is what user sees
    */
    public static final String NOM_ATTR_REL_TYPE = "Arc Type";

    /** Relationship id nominal attribute */
    public static final String NOM_ATTR_REL_ID = "Relationship id";

    /** The relationship display text nominal attribute **/
    public static final String NOM_ATTR_REL_DISPLAY_TEXT = "relationship display text";

    /** The relationship display text nominal attribute **/
    public static final String NOM_ATTR_REL_SHORT_DISPLAY_TEXT = "relationship short display text";

    /** The name of the terminal that a relationship comes from */
    public static final String NOM_ATTR_TARGET_TERMINAL_ID = "TargetTerminal";

    /** The name of the terminal that a relationship goes to */
    public static final String NOM_ATTR_SOURCE_TERMINAL_ID = "SourceTerminal";

    /** The weight of a relationship **/
    public static final String ORD_ATTR_REL_WEIGHT = "Arc Weight";

    /**
	 * The display nominal attribute - used to flag if a graph should be
	 * displayed or not. Its values are boolean ('true' or 'false')
	 */
    public static final String NOM_ATTR_DISPLAY = "display";

    /**
	 * The layout nominal attribute - used to define a custom layout for a a graph
	 * Its possible values are defined in LayoutConstants. @see ca.uvic.csr.shrimp.LayoutConstants
	 */
    public static final String NOM_ATTR_LAYOUT = "layout";

    /** Attribute indicating if a node is openable.  */
    public static final String NOM_ATTR_OPENABLE = "openable";

    /** Attribute for the order of a node or edge.  */
    public static final String NOM_ATTR_ORDER = "order";

}