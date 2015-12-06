/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import org.protege.editor.owl.ui.OWLIcons;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * The pupose of this class is to collect a few methods that require the Protege-OWL plugin to be installed.
 * Calling any method in this class will result in an exception if the Protege-OWL plugin is not installed
 * so just wrap your code with a try catch when calling any of the methods in this class.
 * 
 * @author Rob Lintern
 */
public class OWLSpecificUtils {
    /**
     * Creates the visvar attributes for the OWL Icons.
     * NoClassDefFoundError will occur if OWL isn't installed. 
     * @param attrToVisVarBean
     */
    public static void setOWLIcons(AttrToVisVarBean attrToVisVarBean) {
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.PRIMITIVE_CLASS_TYPE, OWLIcons.getIcon("class.primitive.png"));      
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.DEFINED_CLASS_TYPE, OWLIcons.getIcon("class.defined.png"));
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.INDIVIDUAL_TYPE, OWLIcons.getIcon("individual.png"));
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.LOGICAL_OPERATION_TYPE, OWLIcons.getIcon("OWLUnionClass.gif"));
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.RESTRICTION_TYPE, OWLIcons.getIcon("OWLRestriction.gif"));
        attrToVisVarBean.setDefaultNominalVisualVariableValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, ProtegeDataBean.ENUMERATION_CLASS_TYPE, OWLIcons.getIcon("OWLEnumeratedClass.gif"));
    }

}
