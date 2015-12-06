/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

import java.awt.Color;

/**
 * Constants used in the Java domain.
 *
 * @author Rob Lintern
 */
public class JavaDomainConstants extends SoftwareDomainConstants {

    public static final String CONTAINS_REL_TYPE = "contains";
    public static final String ACCESSES_REL_TYPE = "accesses";
    public static final String CALLS_REL_TYPE = "calls";
    public static final String CALLS_STATICALLY_REL_TYPE = "calls statically";
    public static final String CASTS_TO_TYPE_REL_TYPE = "casts to type"; //TODO find out the correct terminology here: 'casts' or 'checks' ?
    public static final String CREATES_REL_TYPE = "creates";
    public static final String EXTENDED_BY_REL_TYPE = "extended by";
    public static final String HAS_PARAMETER_TYPE_REL_TYPE = "has parameter type";
    public static final String HAS_RETURN_TYPE_REL_TYPE = "has return type";
    public static final String IMPLEMENTED_BY_REL_TYPE = "implemented by";
    public static final String INTERFACE_EXTENDED_BY_REL_TYPE = "extended by (interface)";
    public static final String IS_OF_TYPE_REL_TYPE = "is of type";
    public static final String OVERRIDDEN_BY_REL_TYPE = "overridden by";

    //@tag Shrimp(sequence)
    public static final String METHOD_CALL_REL_TYPE = "MethodCall";
    public static final String RETURN_VALUE_REL_TYPE = "ReturnValue";

    public static final String PROJECT_ART_TYPE = "Project";
    public static final String PACKAGE_ROOT_ART_TYPE = "Package Root";
    public static final String PACKAGE_ART_TYPE = "Package";
    public static final String PACKAGE_FRAGMENT_ART_TYPE = "Package Fragment";
    public static final String PACKAGE_DECLARATION_ART_TYPE = "Package Delaration";
    public static final String JAVA_FILE_ART_TYPE = ".java File";
    //public static final String IMPORT_CONTAINER_ART_TYPE = "Import Container"; // we don't bother showing these
    //public static final String IMPORT_DECLARATION_ART_TYPE = "Import Declaration"; // we don't bother showing these
    public static final String CLASS_ART_TYPE = "Class";
    public static final String INTERFACE_ART_TYPE = "Interface";
    public static final String FIELD_ART_TYPE = "Field";
    public static final String CONSTANT_ART_TYPE = "Constant (static final)";
    public static final String METHOD_ART_TYPE = "Method";
    public static final String CONSTRUCTOR_ART_TYPE = "Constructor";
    public static final String INITIALIZER_ART_TYPE = "Initializer";
    public static final String CLASS_FILE_ART_TYPE = ".class File";

    //@tag Shrimp(sequence)
    public static final String ACTOR_ART_TYPE = "Actor";
    public static final String OBJECT_ART_TYPE = "Object";
    public static final String METHOD_EXEC_ART_TYPE = "MethodExecution";
    public static final String SUMMARY_ART_TYPE = "Summary";

    public static final String JAVADOC = "JavaDoc";
    public final static String PANEL_JAVADOC = "JavaDoc";
    public final static String PANEL_CODE_AND_JAVADOC = "Code and JavaDoc";

    public static final Color COLOR_INTERFACE_ART_TYPE = new Color (227, 212, 234);
    public static final Color COLOR_CLASS_ART_TYPE = new Color (206, 235, 206);
    public static final Color COLOR_JAVA_FILE_ART_TYPE = new Color (216, 211, 196);
    public static final Color COLOR_PACKAGE_FRAGMENT_ART_TYPE = new Color (239, 224, 177);
    public static final Color COLOR_PACKAGE_ROOT_ART_TYPE = new Color (241, 232, 207);
    public static final Color COLOR_PROJECT_ART_TYPE = new Color (178, 207, 237);

    public static final String ABSTRACT_MODIFIER = "modifier abstract";
    public static final String DEPRECATED_MODIFIER = "modifier deprecated";
    public static final String FINAL_MODIFIER = "modifier final";
    public static final String NATIVE_MODIFIER = "modifier native";
    public static final String PRIVATE_MODIFIER = "modifier private";
    public static final String PROTECTED_MODIFIER = "modifier protected";
    public static final String PUBLIC_MODIFIER = "modifier public";
    public static final String PACKAGE_VISIBILITY_MODIFIER = "modifier (package)";
    public static final String STATIC_MODIFIER = "modifier static";
    public static final String SYNCRONIZED_MODIFIER = "modifier synchronized";
    public static final String SYNTHETIC_MODIFIER = "modifier synthetic";
    public static final String TRANSIENT_MODIFIER = "modifier transient";
    public static final String VOLATILE_MODIFIER = "modifier volatile";

    public static final String [] MODIFIERS = new String [] {
        ABSTRACT_MODIFIER,
        DEPRECATED_MODIFIER,
        FINAL_MODIFIER,
        NATIVE_MODIFIER,
        PRIVATE_MODIFIER,
        PROTECTED_MODIFIER,
        PUBLIC_MODIFIER,
        PACKAGE_VISIBILITY_MODIFIER,
        STATIC_MODIFIER,
        SYNCRONIZED_MODIFIER,
        SYNTHETIC_MODIFIER,
        TRANSIENT_MODIFIER,
        VOLATILE_MODIFIER
    };

    public static final String JAVA_QUICK_VIEW_CLASS_HIERARCHY = "Class Hierarchy";
    public static final String JAVA_QUICK_VIEW_INTERFACE_HIERARCHY = "Interface Hierarchy";
    public static final String JAVA_QUICK_VIEW_CLASS_INTERFACE_HIERARCHY = "Class & Interface Hierarchy";
    public static final String JAVA_QUICK_VIEW_CALL_GRAPH = "Call Graph";
    public static final String JAVA_QUICK_VIEW_CONTROL_FLOW_GRAPH = "Control Flow Graph";
    //@tag Shrimp.sequence
    public static final String JAVA_QUICK_VIEW_SEQUENCE_DIAGRAM = "Sequence Diagram";
    public static final String JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_ACCESSES = "Package Dependencies via Field Accesses";
    public static final String JAVA_QUICK_VIEW_ACCESSES_REL_GROUP_NAME = "Accesses";
    public static final String JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS = "Package Dependencies via Method Calls";
    public static final String JAVA_QUICK_VIEW_CALLS_REL_GROUP_NAME = "Calls";
    public static final String JAVA_QUICK_VIEW_PACKAGE_DEPENDENCIES_CALLS_ACCESSES = "Package Dependencies via Method Calls & Field Accesses";
    public static final String JAVA_QUICK_VIEW_CALLS_ACCESSES_REL_GROUP_NAME = "Calls & Accesses";
    public static final String JAVA_QUICK_VIEW_FAN_IN_OUT = "Fan In/Out";
    public static final String JAVA_QUICK_VIEW_QUERY_VIEW = "Query View";

}
