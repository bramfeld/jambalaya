/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the Query View.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class OpenQueryViewAdapter extends AbstractOpenApplicationToolAdapter {
	
	private static final Rectangle BOUNDS = new Rectangle(DOCK_LEFT_INSIDE, DOCK_BOTTOM_INSIDE, 900, 850);
	
    public OpenQueryViewAdapter() {
        super(ShrimpProject.QUERY_VIEW, ResourceHandler.getIcon("icon_query_view.gif"), BOUNDS);
    }
    
    protected void createTool() {
   		tool = new QueryView(getProject(), true);
    }
    
}
