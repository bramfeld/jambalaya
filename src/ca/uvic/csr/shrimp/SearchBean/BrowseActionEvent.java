/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.io.Serializable;
import java.util.EventObject;
import java.util.Vector;


/**
 * BrowseActionEvent is fired by a search strategy, through which
 * a user performs some kind of browsing action. Such an action will
 * cause a selected object or a set of selected objects to be browsed
 * in other tools.
 *
 * @author Jingwei Wu.
 */
public class BrowseActionEvent extends EventObject implements Serializable {

	private Vector objects;

	/**
	 * Constructs a new BrowseActionEvent.
	 * @param s the search strategy which fires this event.
	 * @param o the object contained in this event. Browsing
	 * this object in other tools is requested by the search
	 * strategy <code>s</code>.
	 */
	public BrowseActionEvent(SearchStrategy s, Object o) {
		super(s);
		objects = new Vector();
		if (o != null)
			objects.addElement(o);
	}

	/**
	 * Constructs a new BrowseActionEvent.
	 * @param s the search strategy which fires this event.
	 * @param objs the objects contained in this event. Browsing
	 * thess object in other tools is requested by the search
	 * strategy <code>s</code>.
	 */
	public BrowseActionEvent(SearchStrategy s, Vector objs) {
		super(s);
		if (objs == null) {
			objects = new Vector();
		} else {
			objects = (Vector) objs.clone();
		}
	}

	/**
	 * Gets the search strategy which fires this event.
	 * @return the event source which in fact is the search strategy.
	 */
	public SearchStrategy getSearchStrategy() {
		return (SearchStrategy) getSource();
	}

	/**
	 * Gets all the objects contained in this event.
	 * @return all the objects in this event.
	 */
	public Vector getObjects() {
		return objects;
	}

} //class BrowseActionEvent
