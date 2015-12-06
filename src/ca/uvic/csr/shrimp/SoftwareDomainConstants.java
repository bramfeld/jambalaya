/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

/**
 * A list of constants to be used in the "software" domain.
 * 
 * @author Rob Lintern
 */
public class SoftwareDomainConstants {
	
    public static final String PANEL_UML = "UML";
    public static final String UML = "UML";
    public final static String PANEL_CODE = "Code";    
    
    /** Artifact's source code URI, nominal attribute (for the Software domain) **/
    public static final String NOM_ATTR_SOURCE_CODE_URI = "Source Code URI";
    /** Artifact's source code (for the Software domain) **/
    public static final String NOM_ATTR_SOURCE_CODE = "Source Code";

    public final static String ATTR_CVS_AUTHOR_LAST = "CVS Author (Last Commit)";
    public final static String ATTR_CVS_AUTHOR_FIRST = "CVS Author (First Commit)";
    public final static String ATTR_CVS_AUTHOR_MOST_COMMITS = "CVS Author (Most Commits)";
    public final static String ATTR_CVS_COMMIT_DATE_OLDEST = "CVS Date (First Commit)";
    public final static String ATTR_CVS_COMMIT_DATE_NEWEST = "CVS Date (Last Commit)";
    public final static String ATTR_CVS_NUM_COMMITS = "CVS Number of Commits";
    
    public final static String ATTR_LINES_OF_CODE = "Lines of Code";
    
    public final static String PATH = "path";
    public static final String DATATYPE_ART_TYPE = "Datatype";
    public static final String CONSTANT_ART_TYPE = "Constant";
    public static final String VARIABLE_ART_TYPE = "Variable";
    public static final String FUNCTION_ART_TYPE = "Function";

    // @tag Shrimp(grouping)
    public static final String NOM_ATTR_GROUPED = "grouped";
    public static final String NOM_ATTR_SUMMARY = "summary";
    public  static final String SUMMARY_PROPERTY_PREFIX = "summary."; // for use in properties file
    public  static final String GROUPED_PROPERTY_PREFIX = "grouped."; // for use in properties file
	public  static final String SUMMARY_COUNT_PROPERTY = "summaryCount";
	public  static final String NAME_PROPERTY_PREFIX = "name.";
	
    // @tag Shrimp(SourceAutoDisplay)
	// Set to true if source should automatically be displayed on a node
	public static final String SOURCE_AUTO_DISPLAY_PROPERTY = "sourceAutoDisplay"; 

}
