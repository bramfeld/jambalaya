/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.net.URL;

/**
 * DataReflector is an information extractor which is used
 * to extract specific descriptive or URL information from
 * any acceptable objects.
 * 
 * @author Jingwei Wu
 */
public interface DataReflector {

    /**
     * Tests if the specified object <code>o</code> is
     * acceptable to this DataReflector or not.
     * @param o the object to be tested.
     * @return <code>true</code> if <code>o</code> is acceptable.
     * Otherwise, <code>false</code>.
     */
    public boolean accept(Object o);

    /**
     * Gets the name of the specified object <code>o</code>.
     * An object's name is expected to be unique in a certain
     * name space.
     * @param o any object.
     * @return the name of the sepecified object <code>o</code>.
     * @throws java.lang.IllegalArgumentException if the specified
     * object <code>o</code> is not acceptable to this reflector. 
     */
    public String getName(Object o);

    /**
     * Reflects descriptive information about the specified
     * object <code>o</code>.
     * @param o the object to be reflected.
     * @return descriptive information about the sepecified
     * object <code>o</code>.
     * @throws java.lang.IllegalArgumentException if the specified
     * object <code>o</code> is not acceptable to this reflector. 
     */
    public String reflect(Object o);

    /**
     * Reflects the URL information about the specified
     * object <code>o</code>.
     * @param o the object to be reflected.
     * @return the associated URL object.
     * @throws java.lang.IllegalArgumentException if the specified
     * object <code>o</code> is not acceptable to this reflector. 
     */
    public URL reflectURL(Object o);

}//interface DataReflector
