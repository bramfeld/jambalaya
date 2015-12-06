/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.util.List;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;


/**
 * The purpose of this bean is to explicitly map visual variables to data attributes.
 * For example, we may want to map node colour (a visual variable) to the number of 
 * children that this node has (a data attribute)
 * 
 * Attributes and visual variables are currently considered to be one of two types: 
 * nominal or ordinal
 * "Ordinal" means having a range of values between a minimum and maximum, 
 * whereas "nominal" means having a discrete set of values.
 * 
 * Attributes and visual variables must be added to this bean before they can be 
 * mapped together.
 * 
 * @author Rob Lintern
 *
 */
public interface AttrToVisVarBean {
	
	/**
	 * @return the {@link ShrimpProject} associated with this bean.
	 */
	public ShrimpProject getProject();
	
	/**
	 * Clears all the mappings in this bean.
	 */
	public void clear();
	
	/**
	 * Adds an attribute to this bean.
	 * @param attr
	 */
	public void addAttr(Attribute attr);
	
	/**
	 * Adds a visual variable to this bean.
	 * @param visVar
	 */
	public void addVisVar(VisualVariable visVar);
	
	/**
	 * Removes an attribute from this bean.
	 * @param attr
	 */
	public void removeAttr(Attribute attr);

	/**
	 * Removes a visual variable from this bean.
	 * @param visVar
	 */
	public void removeVisVar(VisualVariable visVar);
	
	/**
	 * @param visVarName
	 * @return The visual variable represented by the passed in name. 
	 * If this bean does not contain a visual variable of that name, null is returned.
	 */
	public VisualVariable getVisVar(String visVarName);

	/**
	 * 
	 * @param attrName
	 * @return The attribute represented by the passed in name. 
	 * If this bean does not contain an attribute of that name, null is returned.
	 */
	public Attribute getAttr(String attrName);
	
	/**
	 * Maps an attribute to a visual variable. 
	 * If this bean does not contain the passed in attribute or visual variable, nothing will happen.
	 * @param attrName The name of the attribute to be mapped.
	 * @param visVarName The name of the visual variable to be mapped
	 */
	public void mapAttrToVisVar(String attrName, String visVarName);
	
	/**
	 * Unmaps an attribute from a visual variable. 
	 * If this bean does not contain the passed in attribute or visual variable, nothing will happen.
	 * @param attrName The name of the attribute to be unmapped.
	 * @param visVarName The name of the visual variable to be unmapped
	 * @return Whether or not any changes we made to this bean.
	 */
	public boolean unmapAttrToVisVar(String attrName, String visVarName);
	
	/**
	 * 
	 * @param attrName The name of the attribute of interest.
	 * @param visVarName The name of the visual variable of interest.
	 * @param attrValue The value of the attribute of interest.
	 * @return The value of the passed in visual variable based upon the passed in attribute value.
	 * Returns null if this bean does not contain the given visual variable or attribute, or 
	 * if the given attribute and visual variable are not mapped to each other.
	 */
	public Object getVisVarValue(String attrName, String visVarName, Object attrValue);
	
	/**
	 * Sets the value of a nominal visual variable
	 * @param attrName
	 * @param visVarName
	 * @param attrValue
	 * @param visVarValue
	 */
	public void setNominalVisualVariableValue(String attrName, String visVarName, Object attrValue, Object visVarValue);
	
	/**
	 * Set the default nominal vis var value to be used for the given nominal attribute
	 * @param attrName the attribute name - see ({@link AttributeConstants}
	 * @param visVarName the name of the visual variable - see {@link VisVarConstants}
	 * @param attrValue the attribute value
	 * @param defaultVisVarValue the default value to set
	 */
	public void setDefaultNominalVisualVariableValue(String attrName, String visVarName, String attrValue, Object defaultVisVarValue);

	/**
	 * Gets the default nominal vis var value to be used for the given nominal attribute
	 * @param attrName the attribute name - see ({@link AttributeConstants}
	 * @param visVarName the name of the visual variable - see {@link VisVarConstants}
	 * @param attrValue the attribute value
	 * @param defaultVisVarValue this value is only used if a default can't be found, 
	 * 	in which case this value is set as the default
	 * @return Object the default value, or if not found the defaultVisVarValue is returned
	 */
	public Object getDefaultNominalVisualVariable(String attrName, String visVarName, String attrValue, Object defaultVisVarValue);
	
	/**
	 * Returns a list of visual variables for the given attribute.
	 * @param attr
	 * @return A list of visual variables mapped to the given attribute
	 */
	public List getVisVars (Attribute attr);
	
	/**
	 * Gets an {@link Attribute} for the visual variable name. 
	 * @param visVarName
	 * @return The attribute mapped to the given visual variable.
	 */
	public Attribute getMappedAttribute(String visVarName);
	
	/**
	 * Registers a listener for changes to the mappings of this bean.
	 * @param l
	 */
	public void addVisVarValuesChangeListener(AttrToVisVarChangeListener l);
	
	/**
	 * Deregisters a listener for changes to the mappings of this bean.
	 * @param l
	 */
	public void removeVisVarValuesChangeListener(AttrToVisVarChangeListener l);
	
	/**
	 * Informs listeners of changes to the mappings of this bean.
	 * @param attr The attribute newly mapped
	 * @param visVars The visual variables newly mapped to attr.
	 */
	public void fireVisVarValuesChangeEvent(Attribute attr, List visVars);
	
	/**
	 * Informs listeners of changes to the mappings of this bean.
	 * @param attr The attribute newly mapped
	 * @param visVar The visual variables newly mapped to attr.
	 */
	public void fireVisVarValuesChangeEvent(Attribute attr, VisualVariable visVar);
	
	/**
	 * @return Whether or not this bean is currently firing events.
	 */
	public boolean isFiringEvents();
	
	/**
	 * Sets whether or not this bean should fire events.
	 * @param fireEvents
	 */
	public void setFiringEvents(boolean fireEvents);

}