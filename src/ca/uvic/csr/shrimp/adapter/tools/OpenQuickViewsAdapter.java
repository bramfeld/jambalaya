/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.gui.quickview.QuickViews;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the Quick Views dialog.
 *
 * @author Chris Callendar
 */
public class OpenQuickViewsAdapter extends AbstractOpenApplicationToolAdapter {

	private static final Rectangle BOUNDS = new Rectangle(DOCK_CENTER, DOCK_CENTER, 800, 750);

    public OpenQuickViewsAdapter() {
        super(ShrimpProject.QUICK_VIEWS, ResourceHandler.getIcon("icon_new.gif"), BOUNDS);
    }

    protected void createTool() {
    	ShrimpProject p = getProject();
    	QuickViewManager qvm = (p != null ? p.getQuickViewManager() : null);
   		tool = new QuickViews(p, qvm);
    }

}
