/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterNotFoundException;
import ca.uvic.csr.shrimp.FilterBean.NominalAttributeFilter;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to filter the currently selected arcs.
 * An id filter is added to the FilterBean for each selected relationship.
 *
 * @tag Shrimp.filterAllArcs
 * @author Chris Callendar
 * @date Feb 6, 2007
 */
public class FilterSelectedArcsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_HIDE_ARC;
	public static final String TOOLTIP = "Hides the selected arc.";

	private ShrimpArcIDFilter filter;

	private Vector hiddenCompositeArcs;

	/**
	 * Constructs a new FilterSelectedArcsAdapter
	 * @param tool The tool that this adapter acts upon.
	 */
	public FilterSelectedArcsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		this.mustStartAndStop = false;
		this.filter = new ShrimpArcIDFilter();
		this.hiddenCompositeArcs = new Vector();
	}

	public void startAction() {
	    try {
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
	    	Vector selectedArcs = getSelectedArcs();
			Vector idsToFilter = new Vector(selectedArcs.size());
			for (Iterator iterator = selectedArcs.iterator(); iterator.hasNext();) {
				ShrimpArc arc = (ShrimpArc) iterator.next();
				if (arc instanceof ShrimpCompositeArc) {
					ShrimpCompositeArc compositeArc = (ShrimpCompositeArc) arc;
					compositeArc.setVisible(false);
					hiddenCompositeArcs.add(compositeArc);
				} else {
					Relationship rel = arc.getRelationship();
					if (rel != null) {
						idsToFilter.add(new Long(rel.getID()));
					}
				}
			}
			if (!idsToFilter.isEmpty()) {
				filter.addIDs(idsToFilter);
				if (filterBean.hasFilter(filter)) {
					try {
						filterBean.removeFilter(filter);
					} catch (FilterNotFoundException e) {}
				}
				filterBean.addFilter(filter);
			}
		} catch (BeanNotFoundException stnfe) {
			stnfe.printStackTrace();
	    }
	}

	public boolean isEnabled() {
    	Vector selectedArcs = getSelectedArcs();
		return super.isEnabled() && (selectedArcs.size() > 0);
	}

	/**
	 * Removes the filter and clears the ids.
	 */
	public void clearFilter() {
		 try {
			filter.clearIDs();
			FilterBean filterBean = (FilterBean) tool.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
			if (filterBean.hasFilter(filter)) {
				try {
					filterBean.removeFilter(filter);
				} catch (FilterNotFoundException e) {}
			}
		} catch (BeanNotFoundException stnfe) {
			stnfe.printStackTrace();
	    }
		for (Iterator iter = hiddenCompositeArcs.iterator(); iter.hasNext();) {
			ShrimpCompositeArc compositeArc = (ShrimpCompositeArc) iter.next();
			compositeArc.setVisible(true);
		}
		hiddenCompositeArcs.clear();
	}

	/**
	 * @return true if the filter has at least one relationship id to hide.
	 */
	public boolean hasFilteredIDs() {
		return (filter.getSize() > 0) || (hiddenCompositeArcs.size() > 0);
	}

	/**
	 * A filter which hides {@link Relationship}s by id.
	 * @author Chris Callendar
	 * @date 6-Feb-07
	 */
	class ShrimpArcIDFilter extends NominalAttributeFilter {

		public ShrimpArcIDFilter() {
			super(AttributeConstants.NOM_ATTR_REL_ID, Long.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, new HashSet());
		}

		public void addIDs(Collection/*<Long>*/ idsToAdd) {
			getFilteredValuesReference().addAll(idsToAdd);
		}

		public void clearIDs() {
			getFilteredValuesReference().clear();
		}

		public int getSize() {
			return getFilteredValuesReference().size();
		}

		public boolean isFiltered(Object object) {
			boolean filtered = false;
			if (object instanceof Relationship) {
				Relationship rel = (Relationship) object;
				Long id = new Long(rel.getID());
				if (getFilteredValuesReference().contains(id)) {
					filtered = true;
				}
			}
			return filtered;
		}

	}

}