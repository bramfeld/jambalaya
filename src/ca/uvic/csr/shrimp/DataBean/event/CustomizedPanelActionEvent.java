/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

/**
 * This is an interface for a standard event thrown when a customized
 * panel requests an action
 *
 * @author Casey Best
 * @date Oct 29, 2000
 */
public interface CustomizedPanelActionEvent {

    public String getPanelType();

}