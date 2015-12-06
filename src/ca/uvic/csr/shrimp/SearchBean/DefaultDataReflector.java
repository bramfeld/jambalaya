/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.io.Serializable;
import java.net.URL;

/**
 * DefaultDataReflector is a default implementation for
 * {@link DataReflector}.
 *
 * @author Jingwei Wu
 */
public class DefaultDataReflector implements DataReflector, Serializable {

	/**
	 * Constructs a new DefaultDataReflector.
	 */
	public DefaultDataReflector() {
		super();
	}

	/**
	 * Tests if the specified object <code>o</code> is
	 * acceptable to this DataReflector or not.
	 * @return <code>true</code> always for a non<code>null</code>
	 * object, and <code>false</code> for a <code>null</code> object.
	 */
	public boolean accept(Object o) {
		if (o == null) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the name of the specified object <code>o</code>.
	 * This method is dependent on {@link #reflect(Object)}.
	 * @param o any object.
	 * @return the name of the sepecified object <code>o</code>.
	 * @throws java.lang.IllegalArgumentException if the specified
	 * object <code>o</code> is not acceptable to this reflector.
	 */
	public String getName(Object o) {
		return reflect(o);
	}

	/**
	 * Reflects the string information about the specified
	 * object <code>o</code>.
	 * @param o the object to be reflected.
	 * @return the <code>Object.toString()</code> information.
	 * @throws java.lang.IllegalArgumentException if the specified
	 * object <code>o</code> is not acceptable to this reflector.
	 */
	public String reflect(Object o) {
		if (!accept(o)) {
			throw new IllegalArgumentException("The argument is not acceptable!");
		}
		return o.toString();
	}

	/**
	 * Reflects the URL information about the specified
	 * object <code>o</code>.
	 * @param o the object to be reflected.
	 * @return <code>null</code> always.
	 * @throws java.lang.IllegalArgumentException if the specified
	 * object <code>o</code> is not acceptable to this reflector.
	 */
	public URL reflectURL(Object o) {
		if (!accept(o)) {
			throw new IllegalArgumentException("The argument is not acceptable!");
		}
		return null;
	}

}

