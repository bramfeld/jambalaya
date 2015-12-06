/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ActionHistoryBean;


/** 
 * The Action interface is used to keep track of the methods to undo and redo
 * any actions that are fired to the DisplayBean.
 *
 * @author Polly Allen
 * @date June 28, 2000
 */
public interface HistoryAction {
    
    /** 
     * Undo method.  This method will be empty in all
     * Action implementation classes, and will be implemented
     * by the system adapters.
     */
    public void undo();

   /** 
     * Redo method.  This method will be empty in all
     * Action implementation classes, and will be implemented
     * by the system adapters.
     */
    public void redo();

}











