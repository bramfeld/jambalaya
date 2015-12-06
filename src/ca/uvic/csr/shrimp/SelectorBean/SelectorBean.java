/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */ 
package ca.uvic.csr.shrimp.SelectorBean;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;

/** 
 * SelectorBean contains a hashtable of values that are indexed by Strings.  
 * These values are Objects or a collection of Objects, stored in a Vector.  
 * The values are used to keep track of variables that are global to the system.  
 * The String key of each value should indicate the use of each variable.  
 * 
 * @author Polly Allen
 * @date July 4, 2000
 */
public class SelectorBean {

	private Hashtable selected;
	private PropertyChangeSupport propertyChangeSupport;

	public SelectorBean() {
		selected = new Hashtable();
		propertyChangeSupport = new PropertyChangeSupport(this);
	}

	public void dispose() {
		selected.clear();
		selected = null;
		propertyChangeSupport = null;
	}
	
	/** 
	 * Retreives some information from the SelectorBean.  If no value is mapped 
	 * to this key, the function returns null. If the information is a Vector, 
	 * a clone of the Vector will be returned.
	 * @param key A string indicating what the user wants to retrieve
	 */
	public Object getSelected(String key) {
		if (selected.containsKey (key)) {
			Object selectedObj = selected.get(key);
			if (selectedObj instanceof Vector) {
				Object selectedVector = ((Vector)selectedObj).clone();
				return selectedVector;
			}
			return selectedObj;
		}
		return null;
	}  

	/**
	 * Sets some information in the SelectorBean.  If there is no key of this name 
	 * in the hashtable, the key is created with the associated object.
	 * @param key A string indicating what the user wants to set
	 * @param newValue The object to be associated with that key
	 */
	public void setSelected(String key, Object newValue) {
		Object oldValue = selected.get(key);	
		selected.put(key, newValue);
		if (oldValue != newValue) {	
			if (key.equals(SelectorBeanConstants.SELECTED_NODES)) {
				Vector newValueVector = (Vector) newValue;
				Set newValueSet = new HashSet(newValueVector);
				if (newValueVector.size() != newValueSet.size()) {
					System.err.println("SelectorBean: warning - duplicate selections");
				}
			}
			propertyChangeSupport.firePropertyChange (key, oldValue, newValue);		
		}
	}  

	/** 
	 * Removes the key and the associated value from the hashtable.
	 * If there is no key of this name in the hashtable, nothing happens.
	 *
	 * @param key The key string indicating which value to reset
	 */
	public void clearSelected(String key) {
		Object oldValue = selected.get(key);
		selected.remove(key);
		if (oldValue != null) {
			propertyChangeSupport.firePropertyChange(key, oldValue, null);
		}		
	}   
	
	/** 
	 * Add a PropertyChangeListener for all properties.
	 * @param listener The PropertyChangeListener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}   

	/** 
	 * Add a PropertyChangeListener for a specific property.
	 * @param listener The PropertyChangeListener to add.
	 * @param propertyName The name of the property to listen on.
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}
	
	/** 
	 * Removes a PropertyChangeListener registered for all properties.
	 * @param listener The PropertyChangeListener to remove.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}   
	
	/** 
	 * Removes a PropertyChangeListener for a specific property.
	 * @param listener The PropertyChangeListener to remove.
	 * @param propertyName The name of the property that was listened on.

	 */
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}   
	
}
