/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Opens the HierarchicalView tool.
 * At the moment this opens the HierarchicalView in a new dialog for each open project.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class OpenHierarchicalViewAdapter extends AbstractOpenApplicationToolAdapter {

	private static final Rectangle BOUNDS = new Rectangle(DOCK_LEFT_INSIDE, DOCK_TOP_INSIDE, 400, 400);

	public OpenHierarchicalViewAdapter() {
		super(ShrimpProject.HIERARCHICAL_VIEW, ResourceHandler.getIcon("icon_hierarchical_view.gif"), BOUNDS);
	}

	protected void createTool() {
		tool = new HierarchicalView(getProject());
	}

	private HierarchicalView getHierarchicalView() {
		return (HierarchicalView) tool;
	}

	protected void afterActionHasRun() {
		if ((tool != null) && !getHierarchicalView().isDisplayBeanPopulated() && isContainerVisible()) {
			getHierarchicalView().populateDisplayBean();
		}
	}

	protected void updateContainerContents(boolean refreshTool) {
   		super.updateContainerContents(refreshTool);
		if ((tool != null) && !getHierarchicalView().isDisplayBeanPopulated() && isContainerVisible()) {
			getHierarchicalView().populateDisplayBean();
		}
	}

}
