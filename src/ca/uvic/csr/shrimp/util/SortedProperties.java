/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;


/**
 * This class extends properties to return a sorted {@link Enumeration} of the keys.
 * It compares the String key values using a to determine the sort order. 
 * 
 * @author Chris Callendar
 * @date 8-Aug-06
 */
public class SortedProperties extends Properties {

	public SortedProperties() {
		super();
	}

	public SortedProperties(Properties defaults) {
		super(defaults);
	}

	/**
	 * Returns a sorted enumeration.
	 */
	public synchronized Enumeration keys() {
		Enumeration keys = super.keys();
		if (keys.hasMoreElements()) {
			List list = Collections.list(keys);
			Collections.sort(list, new StringComparator());
			keys = Collections.enumeration(list);
		}
		return keys;
	}
	
	class StringComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			if ((o1 instanceof String) && (o2 instanceof String)) {
				return ((String)o1).compareTo((String)o2);
			}
			return 0;
		}
	}
	
}
