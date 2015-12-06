/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

import ca.uvic.csr.shrimp.FilterBean.Filter;

/**
 * Constants used with a {@link Filter}.
 */
public class FilterConstants {

    /** represents a filter that affects Artifacts */
    public static final String ARTIFACT_FILTER_TYPE = "artifact filter";

    /** represents a filter that affects Relationships */
    public static final String RELATIONSHIP_FILTER_TYPE = "relationship filter";

    public static final String STRING_FILTER_TYPE = "artifact type string filter";

    /** represents a filter that affects artifact types (as Strings) */
    public static final String ARTIFACT_TYPE_STRING_FILTER_TYPE = "artifact type string filter";

    /** represents a filter that affects relationship types (as Strings) */
    public static final String RELATIONSHIP_TYPE_STRING_FILTER_TYPE = "relationship type string filter";

    /** represents a filter that hides nodes for a collapsed node. */
    public static final String COLLAPSED_NODE_FILTER_TYPE = "collapsed node filter";

	public static final String GROUPED_NODE_FILTER_TYPE = "grouped node filter";

	public static final String DEGREE_OF_INTEREST_HIGHLIGHTER_TYPE = "degree of interest highlighter";
	public static final String DEGREE_OF_INTEREST_FILTER_TYPE = "degree of interest filter";

	/** represents a filter that hides all artifacts that aren't part of the search results. */
	public static final String QUICK_SEARCH_FILTER_TYPE = "search results: ";

}
