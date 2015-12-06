/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

/**
 * Thrown from ShrimpProject or ShrimpTool when a requested bean cannot be found.
 * 
 * @author Nasir Rather
 */
public class BeanNotFoundException extends Exception {
	
	/**
	 * 
	 * @param beanName The name of the bean that could not be found.
	 */
	public BeanNotFoundException (String beanName) {
	    super("Bean not found: " + beanName);
	}
	
}

