/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * A default implementation of the AttrToVisVarBean interface.
 * @see AttrToVisVarBean
 *
 * @author Rob Lintern
 */
public class DefaultAttrToVisVarBean implements AttrToVisVarBean {

	private Map visVarsMap;
	private Map attrsMap;

	// key = attribute name, value = a list of visual variables names
	private Map attrToVisVarsMap;

	private Collection visVarValuesChangeListeners;
	private boolean isFiringEvents;

	private ShrimpProject project;

	public DefaultAttrToVisVarBean(ShrimpProject project) {
		this.project = project;
		attrToVisVarsMap = new HashMap();
		visVarValuesChangeListeners = new HashSet();
		visVarsMap = new HashMap();
		attrsMap = new HashMap();
		isFiringEvents = true;
	}

	public ShrimpProject getProject() {
		return project;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#clear()
	 */
	public void clear() {
		attrToVisVarsMap.clear();
		visVarValuesChangeListeners.clear();
		visVarsMap.clear();
		attrsMap.clear();
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#getVisVar(java.lang.String)
	 */
	public VisualVariable getVisVar(String visVarName) {
		VisualVariable visVar = (VisualVariable) visVarsMap.get(visVarName);
		if (visVar == null) {
			//System.err.println("vis var '" + visVarName + "' not in AttrToVisVarBean!");
		}
		return visVar;
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#getAttr(java.lang.String)
	 */
	public Attribute getAttr(String attrName) {
		Attribute attr = (Attribute) attrsMap.get(attrName);
		if (attr == null) {
			//System.err.println ("attribute '" + attrName + "' not in AttrToVisVarBean!");
		}
		return attr;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#unmapAttrToVisVar(java.lang.String, java.lang.String)
	 */
	public boolean unmapAttrToVisVar(String attrName, String visVarName) {
		boolean changed = false;
		if ((getAttr(attrName) != null) && (getVisVar(visVarName) != null)) {
		VisualVariable visVar = getVisVar(visVarName);
		Attribute attr = getAttr(attrName);
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
			if (visVarNames != null) {
				changed = visVarNames.remove(visVarName);
				if (changed) {
					fireVisVarValuesChangeEvent(attr, visVar);
				}
			}
		}
		return changed;
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#mapAttrToVisVar(java.lang.String, java.lang.String)
	 */
	public void mapAttrToVisVar(String attrName, String visVarName) {
		if (getAttr(attrName) == null) {
			return;
		}
		if (getVisVar(visVarName) == null) {
			return;
		}

		VisualVariable visVar = getVisVar(visVarName);
		Attribute attr = getAttr(attrName);
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
		if (visVarNames == null) {
			visVarNames = new ArrayList();
		}
		if (visVarNames.contains(visVarName)) {
			//System.err.println("attribute '" + attrName + "' already mapped to vis var '" + visVarName + "'");
			//fireVisVarValuesChangeEvent(attr, visVar); // should this be done?
		} else {
			visVarNames.add(visVarName);
			//System.err.println("new mapping: attribute '" + attrName + "' mapped to vis var '" + visVarName + "'");
			attrToVisVarsMap.put(attrName, visVarNames);
			fireVisVarValuesChangeEvent(attr, visVar);
		}

		// remove mapping of visVar to other attributes
		Set attrNames = attrToVisVarsMap.keySet();
		for (Iterator iter = attrNames.iterator(); iter.hasNext();) {
			String tmpAttrName = (String) iter.next();
			List tmpVisVarNames = (List) attrToVisVarsMap.get(tmpAttrName);
			if (!tmpAttrName.equals(attrName) && tmpVisVarNames != null && tmpVisVarNames.contains(visVarName)) {
				tmpVisVarNames.remove(visVarName);
				//System.err.println("unmapping: attribute '" + tmpAttrName + "' unmapped from vis var '" + visVarName + "'");
				attrToVisVarsMap.put(tmpAttrName, tmpVisVarNames);
			}
		}
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#getVisVarValue(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public Object getVisVarValue(String attrName, String visVarName, Object attrValue) {
		if ((getAttr(attrName) == null) || (getVisVar(visVarName) == null)) {
			return null;
		}

		Attribute attr = getAttr(attrName);
		Object visVarValue = null;
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
		if (visVarNames != null) {
			if (visVarNames.contains(visVarName)) {
				VisualVariable visVar = getVisVar(visVarName);
				visVarValue = visVar.getVisVarValue(attr, attrValue);
			} else {
				//System.err.println("Warning! '" + attrName + "' not mapped to vis var '" + visVarName + "'");
			}
		} else {
			//System.err.println("Warning! '" + attrName + "' not mapped to vis var '" + visVarName + "'");
		}
		return visVarValue;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#setNominalVisualVariableValue(java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void setNominalVisualVariableValue(String attrName, String visVarName, Object attrValue, Object visVarValue) {
		if ((getAttr(attrName) == null) || (getVisVar(visVarName) == null)) {
			return;
		}

		Attribute attr = getAttr(attrName);
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
		if (visVarNames != null) {
			if (visVarNames.contains(visVarName)) {
				VisualVariable visVar = getVisVar(visVarName);
				((NominalVisualVariable)visVar).setNomVisVarValue(attr, attrValue, visVarValue);
			} else {
				//System.err.println("Warning! '" + attrName + "' not mapped to vis var '" + visVarName + "'");
			}
		} else {
			//System.err.println("Warning! '" + attrName + "' not mapped to vis var '" + visVarName + "'");
		}
	}

	/**
	 * @tag Shrimp.SetDefaultNominalVisVar
	 * @see AttrToVisVarBean#setDefaultNominalVisualVariableValue(String, String, String, Object)
	 */
	public void setDefaultNominalVisualVariableValue(String attrName, String visVarName, String attrValue, Object defaultVisVarValue) {
		if ((getAttr(attrName) == null) || (getVisVar(visVarName) == null)) {
			return;
		}

		Attribute attr = getAttr(attrName);
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
		if (visVarNames != null) {
			if (visVarNames.contains(visVarName)) {
				VisualVariable visVar = getVisVar(visVarName);
				((NominalVisualVariable)visVar).setDefaultNomVisVarValue(attr, attrValue, defaultVisVarValue);
				// @tag Shrimp.DefaultVisVars : save the default value - this never gets changed except in this method
				((NominalVisualVariable)visVar).setDefaultNomVisVarValue(attr, attrValue + ".default", defaultVisVarValue);
				//System.out.println("Set default VisVar: " + attrName + ", " + attrValue + ",  " + defaultVisVarValue);
			}
		}
	}

	/**
	 * Gets the default nominal visual variable.
	 * If a default isn't found, the given defaultVisVarValue is set and also returned.
	 * @param defaultVisVarValue only used if a default value isn't found, in which cause this value is set as the default
	 * @return Object
	 */
	public Object getDefaultNominalVisualVariable(String attrName, String visVarName, String attrValue, Object defaultVisVarValue) {
		Object var = null;
		List visVarNames = (List) attrToVisVarsMap.get(attrName);
		if (visVarNames != null) {
			if (visVarNames.contains(visVarName)) {
				VisualVariable visVar = getVisVar(visVarName);
				// @tag Shrimp.DefaultVisVars : load the default value
				var = ((NominalVisualVariable)visVar).getDefaultNomVisVarValue(attrName, attrValue + ".default");
			}
		}
		if (var == null) {
			//System.err.println("Warning - no default VisVar for " + attrName + ", " + visVarName + ", " + attrValue);
			setDefaultNominalVisualVariableValue(attrName, visVarName, attrValue, defaultVisVarValue);
			var = defaultVisVarValue;
		}
		return var;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#addVisVarValuesChangeListener(ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener)
	 */
	public void addVisVarValuesChangeListener (AttrToVisVarChangeListener l) {
		if (!visVarValuesChangeListeners.contains(l)) {
			visVarValuesChangeListeners.add(l);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#removeVisVarValuesChangeListener(ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener)
	 */
	public void removeVisVarValuesChangeListener (AttrToVisVarChangeListener l) {
		visVarValuesChangeListeners.remove(l);
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#fireVisVarValuesChangeEvent(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute, java.util.List)
	 */
	public void fireVisVarValuesChangeEvent (Attribute attr, List visVars) {
		if (isFiringEvents) {
			AttrToVisVarChangeEvent e = new AttrToVisVarChangeEvent (visVars, attr);
			for (Iterator iter = visVarValuesChangeListeners.iterator(); iter.hasNext();) {
				AttrToVisVarChangeListener l = (AttrToVisVarChangeListener) iter.next();
				l.valuesChanged(e);
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#fireVisVarValuesChangeEvent(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute, ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable)
	 */
	public void fireVisVarValuesChangeEvent(Attribute attr, VisualVariable visVar) {
		List visVars = new ArrayList(1);
		visVars.add(visVar);
		fireVisVarValuesChangeEvent(attr, visVars);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#getVisVars(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute)
	 */
	public List getVisVars(Attribute attr) {
		List visVars = (List) attrToVisVarsMap.get(attr);
		return (visVars != null) ? visVars : new ArrayList();
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#getMappedAttribute(java.lang.String)
	 */
	public Attribute getMappedAttribute (String visVarName) {
		if (getVisVar(visVarName) == null) {
			return null;
		}

		Attribute attr = null;
		Set attrNames = attrToVisVarsMap.keySet();
		for (Iterator iter = attrNames.iterator(); iter.hasNext();) {
			String tmpAttrName = (String) iter.next();
			List visVarNames = (List) attrToVisVarsMap.get(tmpAttrName);
			if (visVarNames != null && visVarNames.contains(visVarName)) {
				attr = getAttr(tmpAttrName);
				if (attr != null) {
					break;
				}
			}
		}
		return attr;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#isFiringEvents()
	 */
	public boolean isFiringEvents() {
		return isFiringEvents;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#setFiringEvents(boolean)
	 */
	public void setFiringEvents(boolean fireEvents) {
		this.isFiringEvents = fireEvents;
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#addAttr(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute)
	 */
	public void addAttr(Attribute attr) {
		attrsMap.put(attr.getName(), attr);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#removeAttr(ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute)
	 */
	public void removeAttr(Attribute attr) {
		attrsMap.remove(attr.getName());
		attrToVisVarsMap.remove(attr.getName());
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#addVisVar(ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable)
	 */
	public void addVisVar(VisualVariable visVar) {
		visVarsMap.put(visVar.getName(), visVar);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean#removeVisVar(ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable)
	 */
	public void removeVisVar(VisualVariable visVar) {
		visVarsMap.remove(visVar.getName());
		for (Iterator iter = attrToVisVarsMap.values().iterator(); iter.hasNext();) {
			List visVarNames = (List) iter.next();
			visVarNames.remove(visVar.getName());
		}
	}

}
