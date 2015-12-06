/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A few convenience methods for handling collections and arrays.
 *
 * @author Rob Lintern
 */
public class CollectionUtils {


    public static boolean contains(Object [] arr, Object obj) {
        for (int i = 0; i < arr.length; i++) {
            Object object = arr[i];
            if (object.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(long [] arr, long l) {
        for (int i = 0; i < arr.length; i++) {
            long lTemp = arr[i];
            if (lTemp == l) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if the given arrays contain the same elements but not
     * necessarily in the same order.
     * NOTE: This is by no means an "efficient" algorithm, just a convienience.
     * @param arr1
     * @param arr2
     * @return True if arrays have same elements, regardless of order
     */
    public static boolean haveSameElements(Object[] arr1, Object[] arr2) {
    	if ((arr1 == null) && (arr2 == null)) {
    		return true;
    	} else if ((arr1 == null) || (arr2 == null)) {
    		return false;
    	}
        return haveSameElements(Arrays.asList(arr1), Arrays.asList(arr2));
    }

    /**
     * Checks if the given collections contain the same elements but not
     * necessarily in the same order.  If both collections are null then true is returned,
     * if one is null then false is returned.
     * NOTE: This is by no means an "efficient" algorithm, just a convienience.
     * @param c1
     * @param c2
     * @return True if the collections have same elements, regardless of order.
     */
    public static boolean haveSameElements(Collection c1, Collection c2) {
    	if ((c1 == null) && (c2 == null)) {
    		return true;
    	} else if ((c1 == null) || (c2 == null)) {
        	return false;
    	}
		return c1.containsAll(c2) && c2.containsAll(c1);
    }

    public static String arrayToString(Object [] arr) {
        return collectionToString(Arrays.asList(arr));
    }

    public static String collectionToString(Collection c) {
        StringBuffer buff = new StringBuffer();
        int i = 0;
        int size = c.size();
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object object = iter.next();
            if (size > 1 && i == (size - 1)) {
                buff.append ("and ");
            }
            buff.append(object);
            if (i < (size - 1)) {
                if (size > 2) {
                    buff.append(", ");
                } else {
                    buff.append(" ");
                }
            }
            i++;
        }
        return buff.toString();

    }

}
