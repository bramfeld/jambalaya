/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.FilterBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * Filter Bean: This is an interface describing a filtering system for
 * Artifacts and Relationships.  Extra entities can be filtered if the
 * Filters added to this Bean can handle that type of entity.
 *
 * @author Casey Best, Rob Lintern
 * @date June 16, 2000
 */
public class FilterBean implements Serializable {

	private Vector filters;
	private Vector filterChangedListeners;
	private FilterChangedEvent filterChangedEventToFire;
	private boolean isFiringEvents;

	/**
	 * Creates a new Filter Bean
	 *
	 * Transaction:
	 *      filters = {}
	 *      filterChangedListeners = {}
	 */
	public FilterBean() {
		filters = new Vector();
		filterChangedListeners = new Vector();
		isFiringEvents = true;
	}

	/**
	 *  Sets whether or not this filter bean will fire events to its listeners.
	 *  @param fireEvents Whether or not this filter bean should fire events.
	 *  When setting the filter bean to fire events, the filter bean will fire
	 *  any events that have been collected while not firing events.
	 */
	 public void setFiringEvents(boolean fireEvents) {
	 	if (isFiringEvents == fireEvents) {
	 		return;
	 	}

	 	if (fireEvents) {
	 		if (filterChangedEventToFire != null) {
	 			fireFilterChangedEvent(filterChangedEventToFire);
	 		}
	 	} else {
	 		filterChangedEventToFire = new FilterChangedEvent(new Vector(), new Vector(), new Vector());
	 	}
	 	isFiringEvents = fireEvents;
	 }

	 public boolean isFiringEvents() {
	 	return isFiringEvents;
	 }


	/**
	 * Returns all of the registered filters for this Filter Bean.
	 */
	public Vector getFilters() {
		return (Vector)filters.clone();
	}

	/**
	 * Returns the filters of a specific type for this Filter Bean.
	 *
	 * @param filterType The type of filters to return.
	 */
	public Vector getFiltersOfType(String filterType, String targetType) {
		Vector ret = new Vector();
		for (int i = 0; i < filters.size(); i++) {
			Filter filter = (Filter)filters.elementAt(i);
			if (filter.getFilterType().equals(filterType) && filter.getTargetType().equals(targetType)) {
				ret.add(filters.elementAt(i));
			}
		}

		return ret;
	}

	/**
	 * Examines if this FilterBean contains the specifed <code>filter</code>.
	 * @param filter the Filter to be checked.
	 */
	public boolean contains(Filter filter) {
		return filter != null && filters.contains(filter);
	}

	/**
	 * Registers a new Filter with this Bean.
	 * Transaction:
	 *      filters = filters + {filter}
	 * Note:
	 *      If the filter is already in the list, it will be added again
     * @param filter the filter to be added.
     * If it is <code>null</code>, no operation is done to this FilterBean.
	 */
	public void addFilter(Filter filter) {
		if (filter != null) {
			filters.addElement(filter);
			Vector addedFilters = new Vector();
			addedFilters.add(filter);
			if (isFiringEvents) {
				filterChangedEventToFire = new FilterChangedEvent(addedFilters, new Vector(), new Vector());
				fireFilterChangedEvent(filterChangedEventToFire);
			} else {
				filterChangedEventToFire.addMoreAddedFilters(addedFilters);
			}
		}
	}

	/**
	 * If the filter is already in this {@link FilterBean} then a filter event is fired.
	 * If the filter isn't already in this {@link FilterBean} then it is added which also
	 * causes a filter event to fire.
	 * @param filter
	 */
	public void applyFilter(Filter filter) {
		if (filter != null) {
			if (!filters.contains(filter)) {
				addFilter(filter);
			} else {
				Vector addedFilters = new Vector();
				addedFilters.add(filter);
				if (isFiringEvents) {
					filterChangedEventToFire = new FilterChangedEvent(addedFilters, new Vector(), new Vector());
					fireFilterChangedEvent(filterChangedEventToFire);
				} else {
					filterChangedEventToFire.addMoreAddedFilters(addedFilters);
				}
			}
		}
	}

	/**
	 * A helper method to determine whether or not a specific nominal attribute value is filtered by this filter bean.
	 * @param attrName The name of the nominal attribute
	 * @param attrType The class of the nominal attribute
	 * @param targetType The type of object that the filter acts upon (DataBean.ARTIFACT_TYPE or DataBean.RELATIONSHIP_TYPE)
	 * @param attrValue The nominal value of interest.
	 * @return Whether or not a specific nominal attribute value is filtered by this filter bean.
	 */
	public boolean isNominalAttrValueFiltered(String attrName, Class attrType, String targetType, Object attrValue) {
		boolean filtered = false;
		Vector nomFilters = getFiltersOfType(NominalAttributeFilter.NOMINAL_ATTRIBUTE_FILTER, targetType);
		for (Iterator iter = nomFilters.iterator(); iter.hasNext() && !filtered;) {
			NominalAttributeFilter nomFilter = (NominalAttributeFilter) iter.next();
			if (nomFilter.getAttributeType().equals(attrType) && nomFilter.getAttributeName().equals(attrName)) {
				Collection filteredValues = nomFilter.getFilteredValuesReference();
				filtered = filteredValues.contains(attrValue);
			}
		}
		return filtered;
	}

	/**
	 * A helper method to remove a nominal attribute filter.
	 * @param attrName The name of the nominal attribute
	 * @param attrType The class of the nominal attribute
	 * @param targetType The type of object that the filter acts upon (DataBean.ARTIFACT_TYPE or DataBean.RELATIONSHIP_TYPE)
	 * @return Whether or not any changes were made to this filterBean.
	 */
	public boolean removeNominalAttrFilter(String attrName, Class attrType, String targetType) {
		boolean changed = false;
		Vector nomFilters = getFiltersOfType(NominalAttributeFilter.NOMINAL_ATTRIBUTE_FILTER, targetType);
		for (Iterator iter = nomFilters.iterator(); iter.hasNext() && !changed;) {
			NominalAttributeFilter nomFilter = (NominalAttributeFilter) iter.next();
			if (nomFilter.getAttributeType().equals(attrType) && nomFilter.getAttributeName().equals(attrName)) {
				try {
					removeFilter(nomFilter);
					changed = true;
				} catch (FilterNotFoundException e) {
					// do nothing
				}
			}
		}
		return changed;
	}

	/**
	 * A helper method to add/remove values to/from a nominal filters in this data bean.
	 * If adding new values and a nominal attribute filter does not exist for the given attribute,
	 * a new nominal attribute filter will be created and added to this filter bean.
	 *
	 * @param attrName The name of the nominal attribute
	 * @param attrType The class of the nominal attribute
	 * @param targetType The type of object that the filter acts upon (DataBean.ARTIFACT_TYPE or DataBean.RELATIONSHIP_TYPE)
	 * @param attrValues The nominal values to filter
	 * @param add Whether to add or remove the given values.
	 * @return Whether or not any changes were made to this filterBean.
	 */
	public boolean addRemoveNominalAttrValues(String attrName, Class attrType, String targetType, Collection attrValues, boolean add) {
		if (attrValues.isEmpty()) {
			return false;
		}
		boolean nomFilterExists = false;
		boolean changed = false;
		Filter changedFilter = null;
		Vector nomFilters = getFiltersOfType(NominalAttributeFilter.NOMINAL_ATTRIBUTE_FILTER, targetType);
		for (Iterator iter = nomFilters.iterator(); iter.hasNext() && !changed;) {
			NominalAttributeFilter nomFilter = (NominalAttributeFilter) iter.next();
			if (nomFilter.getAttributeType().equals(attrType) && nomFilter.getAttributeName().equals(attrName)) {
				nomFilterExists = true;
				Collection currentlyFilteredValues = nomFilter.getFilteredValuesReference();
				for (Iterator iterator = attrValues.iterator(); iterator.hasNext();) {
					Object attrValue = iterator.next();
					if (add) {
						if (!currentlyFilteredValues.contains(attrValue)) {
							if (currentlyFilteredValues.add(attrValue)) {
								changed = true;
							}
						}
					} else {
						if (currentlyFilteredValues.remove(attrValue)) {
							changed = true;
						}
					}
				}
				if (changed) {
					changedFilter = nomFilter;
				}
			}
		}
		if (!nomFilterExists && add) {
			// create and add a new nominal attribute filter
			Collection filteredValues = new ArrayList();
			filteredValues.addAll(attrValues);
			NominalAttributeFilter nomFilter = new NominalAttributeFilter(attrName, attrType, targetType, filteredValues);
			addFilter(nomFilter);
		} else if (changed){
			// fire appropriate event
			Vector changedFilters = new Vector();
			changedFilters.add(changedFilter);
			if (isFiringEvents) {
				filterChangedEventToFire = new FilterChangedEvent(new Vector(), new Vector(), changedFilters);
				fireFilterChangedEvent(filterChangedEventToFire);
			} else {
				filterChangedEventToFire.addMoreChangedFilters(changedFilters);
			}
		}
		return changed;
	}

	/**
	 * A helper method to add/remove a single value to/from a nominal filter in this data bean.
	 * Immediately calls addRemoveNominalAttrValues passing in a collection containing attrValue as its only item.
	 * @see FilterBean#addRemoveNominalAttrValues(String, Class, String, Collection, boolean)
	 */
	public boolean addRemoveSingleNominalAttrValue(String attrName, Class attrType, String targetType, Object attrValue, boolean add) {
		if (attrValue instanceof Collection) {
			System.err.println("Warning! Trying to filter a collection as a single value!");
		}
		Vector attrValues = new Vector(1);
		attrValues.add(attrValue);
		return addRemoveNominalAttrValues(attrName, attrType, targetType, attrValues, add);
	}

	/**
	 * Registers multiple new Filters with this Bean.
	 * Transaction:
	 *      filters = filters + {filters}
	 * Note:
	 *      If a filter is already in the list, it will not be added
     * @param filtersToAdd The filters to be added.
	 */
	public void addFilters(Vector filtersToAdd) {
		Vector addedFilters = new Vector();
		for (int i = 0; i < filtersToAdd.size(); i++) {
			Filter filterToAdd = (Filter) filtersToAdd.elementAt(i);
			if (filterToAdd != null /*&& !filters.contains (filter)*/) {
				filters.addElement(filterToAdd);
				addedFilters.add(filterToAdd);
			}
		}
		if (!addedFilters.isEmpty()) {
			if (isFiringEvents) {
				filterChangedEventToFire = new FilterChangedEvent(addedFilters, new Vector(), new Vector());
				fireFilterChangedEvent(filterChangedEventToFire);
			} else {
				filterChangedEventToFire.addMoreAddedFilters(addedFilters);
			}
		}
	}


	/**
	 * Removes a Filter from this FilterBean.
	 * Transaction:
	 *      filters = filters - {filter}
     * @param filter the filter to be removed.
     * @throws FilterNotFoundException if the specified filter is not found
     * in this FilterBean.
	 */
	public void removeFilter(Filter filter) throws FilterNotFoundException {
		if (filters.contains(filter)) {
			filters.removeElement(filter);
			Vector removedFilters = new Vector();
			removedFilters.add(filter);
			if (isFiringEvents) {
				FilterChangedEvent fce = new FilterChangedEvent(new Vector(), removedFilters, new Vector());
				fireFilterChangedEvent(fce);
			} else {
				filterChangedEventToFire.addMoreRemovedFilters(removedFilters);
			}
		} else {
			throw new FilterNotFoundException();
		}
	}

	/**
	 * Returns true if the given filter already exists in this filterbean.
	 */
	public boolean hasFilter(Filter filter) {
		return filters.contains(filter);
	}

	/**
	 * Removes multiple Filters from this FilterBean.
	 *
	 * Transaction:
	 *      filters = filters - {filters}
	 *
     * @param filtersToRemove The filters to be removed.
     * @throws FilterNotFoundException if a filter is not found
     * in this FilterBean.
	 */
	public void removeFilters(Vector filtersToRemove) throws FilterNotFoundException {
		Vector removedFilters = new Vector();
		for (int i = 0; i < filtersToRemove.size(); i++) {
			Filter filter = (Filter) filtersToRemove.elementAt(i);
			if (filters.contains(filter)) {
				filters.removeElement(filter);
				removedFilters.add(filter);
			} else {
				throw new FilterNotFoundException();
			}
		}
		if (!removedFilters.isEmpty()) {
			if (isFiringEvents) {
				FilterChangedEvent fce = new FilterChangedEvent(new Vector(), removedFilters, new Vector());
				fireFilterChangedEvent(fce);
			} else {
				filterChangedEventToFire.addMoreRemovedFilters(removedFilters);
			}
		}
	}

	/**
	 * Returns whether or not an object should be filtered, based on the currently
	 * registered filters.
	 */
	public boolean isFiltered(Object object) {
        return isFiltered(object, null);
	}

    /**
     * Returns whether or not an object should be filtered, based on the currently registered filters.
     * @param object
     * @param targetType Restricts to considering only filters of this type.
     * 	Pass in <code>null</code> to consider all filters.
     * @return true if filtered
     */
    public boolean isFiltered(Object object, String targetType) {
        if (object instanceof Artifact) {
            return artifactIsFiltered(object);
        } else if (object instanceof Relationship) {
            return relationshipIsFiltered(object);
        } else if (object instanceof String && (targetType != null) &&
        		(FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE.equals(targetType) ||
        		 FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE.equals(targetType))) {
            return stringIsFiltered(object, targetType);
        } else {
            System.err.println(ApplicationAccessor.getApplication().getName() + " Warning: FilterBean doesn't know how to filter this object: " + (object == null ? "null" : object.toString() + " (" + object.getClass().getName() + ")"));
            return false;
        }
    }

	/**
	 * Filters each object in the objects Vector.  If the filters in the
	 * Filter Bean filter (ie. remove) the object, it will be placed in the
	 * filtered vector.  If the object passes through all of the filters
	 * it is placed in the notFiltered vector.
	 *
	 * @param objects The objects to filter
	 * @param filtered An empty vector that will be filled with the filtered objects
	 * @param notFiltered An empty vector that will be filled with the unfiltered objects
	 */
	public void filter(Vector objects, Vector filtered, Vector notFiltered) {
		for (int i = 0; i < objects.size(); i++) {
			Object object = objects.elementAt(i);
			if (isFiltered (object)) {
				filtered.addElement(object);
			} else {
				notFiltered.addElement(object);
			}
		}
	}

	/**
	 * Adds a FilterChangedListener to this FilterBean, if not already present.
	 * Transaction:
	 *      filterChangedListeners = filterChangedListeners + {fcl}
     * @param fcl the FilterChangedListener to be added.
	 */
	public void addFilterChangedListener(FilterChangedListener fcl) {
		if (!filterChangedListeners.contains(fcl)) {
			filterChangedListeners.add(fcl);
		}
	}

	/**
	 * Removes a FilterChangedListener from this FilterBean, if present.
	 * Transaction:
	 *      filterChangedListeners = filterChangedListeners - {fcl}
     * @param fcl the FilterChangedListener to be removed.
	 */
	public void removeFilterChangedListener(FilterChangedListener fcl) {
		if (filterChangedListeners.contains(fcl)) {
			filterChangedListeners.remove(fcl);
		}
	}

	/**
	 * Fires a filterChangedEvent to all filterChangedListeners
	 *
     * @param fce the FilterChangedEvent to be fired.
	 */
	private void fireFilterChangedEvent(FilterChangedEvent fce) {
		for (Enumeration e = filterChangedListeners.elements() ; e.hasMoreElements() ;) {
			FilterChangedListener fcl = (FilterChangedListener) e.nextElement();
			fcl.filterChanged(fce);
		}
	}

	/**
	 * Returns whether or not an artifact should be filtered, based on the currently
	 * registered filters.
	 */
	private boolean artifactIsFiltered(Object object) {
		boolean filtered = false;
		for (int i = 0; i < filters.size(); i++) {
			Filter filter = (Filter)filters.elementAt(i);
			if (FilterConstants.ARTIFACT_FILTER_TYPE.equals(filter.getTargetType()) && filter.isFiltered(object)) {
				filtered = true;
				break;
			}
		}
		return filtered;
	}

    /**
     * Returns whether or not a relationship should be filtered, based on the currently
     * registered filters.
     */
    private boolean relationshipIsFiltered(Object object) {
        boolean filtered = false;
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = (Filter)filters.elementAt(i);
            if (FilterConstants.RELATIONSHIP_FILTER_TYPE.equals(filter.getTargetType()) && filter.isFiltered(object)) {
                filtered = true;
                break;
            }
        }
        return filtered;
    }

    /**
     * Returns whether or not a relationship should be filtered, based on the currently
     * registered filters.
     */
    private boolean stringIsFiltered(Object object, String targetType) {
        boolean filtered = false;

        for (int i = 0; i < filters.size(); i++) {
            Filter filter = (Filter)filters.elementAt(i);
            if (filter.getTargetType().equals(targetType) && filter.isFiltered(object)) {
                filtered = true;
                break;
            }
        }
        return filtered;
    }

}