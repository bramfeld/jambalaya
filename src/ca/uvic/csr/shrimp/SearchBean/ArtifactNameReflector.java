/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.io.Serializable;
import java.net.URL;

import ca.uvic.csr.shrimp.DataBean.Artifact;

/**
 * ArtifactNameReflector is used to reflect the name information
 * of any {@link ca.uvic.csr.shrimp.DataBean.Artifact}s. Since
 * only the name is considered, {@link #reflectURL(Object)}
 * always returns <code>null</code>.
 *
 * @author Jingwei Wu
 */
public final class ArtifactNameReflector implements DataReflector, Serializable {

	/**
	 * Constructs a new ArtifactNameReflector.
	 */
	public ArtifactNameReflector() {
		super();
	}

	/**
	 * Tests if the specified object <code>o</code> is
	 * acceptable to this reflector or not.
	 * @param o the object to be tested. Only non<code>null</code>
	 * {@link ca.uvic.csr.shrimp.DataBean.Artifact} is acceptable
	 * to this reflector.
	 * @return <code>true</code> if <code>o</code> is acceptable
	 * to this reflector. Otherwise, <code>false</code>.
	 */
	public boolean accept(Object o) {
		if (o == null)
			return false;
		return (o instanceof Artifact);
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
	 * Reflects the name information about the specified
	 * object <code>o</code>.
	 * @param o the object to be reflected.
	 * @throws java.lang.IllegalArgumentException if the
	 * specified object <code>o</code> is not acceptable to
	 * this reflector. 
	 */
	public String reflect(Object o) {
		if (!accept(o)) {
			throw new IllegalArgumentException("The argument is not acceptable!");
		}
		return ((Artifact) o).getName();
	}

	/**
	 * Reflects the URL information about the specified
	 * object <code>o</code>. 
	 * @param o the object to be reflected.
	 * @return <code>null</code> always.
	 * @throws java.lang.IllegalArgumentException if the
	 * specified object <code>o</code> is not acceptable
	 * to this reflector. 
	 */
	public URL reflectURL(Object o) {
		if (!accept(o)) {
			throw new IllegalArgumentException("The argument is not acceptable!");
		}
		return null;
	}

} //class ArtifactNameReflector
