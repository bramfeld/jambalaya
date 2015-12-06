/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.event;


/**
 * Listens or navigation events in the display.
 * Ex. The display has just magnified from one node to another.
 * 
 * @author Rob Lintern
 */
public interface NavigationListener {

    /**
     * Called just before display is going to magnify from one object to another.
     * @param e
     */
    public void beforeMagnify (MagnifyEvent e);
    
    /**
     * Called after display has magnified from one object to another.
     * @param e
     */
    public void afterMagnify (MagnifyEvent e);
}
