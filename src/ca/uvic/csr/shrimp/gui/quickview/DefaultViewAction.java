/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.Collection;

import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * An action that configures the Shrimp View to its default nested view.
 * The nested arc types (cprels) are determined by the dataBean, and all node and arc types are shown.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class DefaultViewAction extends QuickViewAction {

	public static final String ACTION_NAME = "Nested View (Default)";

	/**
	 * Don't use this constructor.  It is used to instantiate this class using reflection.
	 */
	public DefaultViewAction() {
		super();
	}

	protected DefaultViewAction(ShrimpProject project, Collection nodeTypes, Collection arcTypes,
			String[] cprels, boolean inverted) {
		this(ACTION_NAME, "icon_quick_view_nested.gif", project, nodeTypes, arcTypes, cprels, inverted);
	}

	protected DefaultViewAction(String actionName, String iconFilename, ShrimpProject project,
			Collection nodeTypes, Collection arcTypes, String[] cprels, boolean inverted) {
	    super(actionName, iconFilename, project);

	    config.setNodeTypesOfInterest(nodeTypes);
	    config.setArcTypesOfInterest(arcTypes);
	    // Respect the default label mode set in application properties
		String labelMode = ApplicationAccessor.getProperty(ShrimpView.SV_LABEL_MODE_KEY, DisplayConstants.LABEL_MODE_FIXED);
	    config.setLabelMode(labelMode);
	    config.setCprels(cprels);
	    config.setInverted(inverted);
	}

}
