/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

/**
 * Thrown from ShrimpApplication or ShrimpProject when a tool cannot be found. 
 * @author Nasir Rather
 */
public class ShrimpToolNotFoundException extends Exception {
    
    /**
     * 
     * @param toolName The name of the tool that could not be found
     */
	public ShrimpToolNotFoundException (String toolName) {
		super("Tool not found: " + toolName);
	}
	
}

