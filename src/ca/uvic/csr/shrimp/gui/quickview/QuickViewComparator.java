/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.Comparator;



/**
 * Compares {@link QuickViewAction}s.  The {@link DefaultViewAction} always comes first, then other nested actions, 
 * and then composite actions.  Sorting is done by name for actions of the same type.
 * 
 * @author Chris Callendar
 * @date 3-Aug-06
 */
public class QuickViewComparator implements Comparator {

	public int compare(Object qv1, Object qv2) {
		if ((qv1 instanceof QuickViewAction) && (qv2 instanceof QuickViewAction)) {
			QuickViewAction action1 = (QuickViewAction) qv1;
			QuickViewAction action2 = (QuickViewAction) qv2;
	
			// DefaultViewActions comes first
			if (action1 instanceof DefaultViewAction)
				return -1;
			if (action2 instanceof DefaultViewAction)
				return 1;
			
			String name1 = action1.getActionName(), name2 = action2.getActionName();
			boolean nested1 = action1.isNested(), nested2 = action2.isNested();
			boolean comp1 = action1.isComposite(), comp2 = action2.isComposite();

			// if both nested and NOT composite - compare by name
			if (nested1 && nested2 && !comp1 && !comp2) {
				return name1.compareToIgnoreCase(name2);
			}
			// if both composite AND nested - compare by name
			if (nested1 && nested2 && comp1 && comp2) {
				return name1.compareToIgnoreCase(name2);
			}
			// if both are flat graphs - just compare by name
			if (!nested1 && !nested2) {
				return name1.compareToIgnoreCase(name2);
			}
			// if both nested then only one is composite - the composite comes last
			if (nested1 && nested2) {
				return (comp1 ? 1 : -1);
			}
			// if we get here then only one is nested - it comes first
			return (nested1 ? -1 : 1);
		}
		return 0;
	}

}
