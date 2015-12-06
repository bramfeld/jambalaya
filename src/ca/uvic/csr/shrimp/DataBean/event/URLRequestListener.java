/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
 
package ca.uvic.csr.shrimp.DataBean.event;


/**
 * This class listens for events from the display bean, caused by
 * clicking on a hyperlink.
 *
 * @author Casey Best
 * date: Sept 21, 2000
 */
public interface URLRequestListener extends CustomizedPanelActionListener {
    
    /* 
     * Perform the action associated with this event
     */
    public void actionPerformed (URLRequestEvent e);
}
