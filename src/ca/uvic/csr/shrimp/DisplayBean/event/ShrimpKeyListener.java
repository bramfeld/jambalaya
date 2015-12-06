/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.DisplayBean.event;


public interface ShrimpKeyListener {

    /* 
     * Key has been typed
     */
    public void keyTyped (ShrimpKeyEvent e);

    /* 
     * Key has been pressed
     */
    public void keyPressed (ShrimpKeyEvent e);

    /* 
     * Key has been released
     */
    public void keyReleased (ShrimpKeyEvent e);
    
    public void resetKeys();
    
}
