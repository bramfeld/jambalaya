/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewConfiguration;
import ca.uvic.csr.shrimp.util.BuildProperties;


/**
 * Simple application for the standalone QueryView.
 *
 * @author Chris Callendar
 * @date 11-Oct-07
 */
public class QueryViewApplication extends StandAloneApplication {

	public QueryViewApplication() {
		this(null, ShrimpConstants.TOOL_QUERY_VIEW);
	}

	public QueryViewApplication(String propertiesFileName) {
		this(propertiesFileName, ShrimpConstants.TOOL_QUERY_VIEW);
	}

	public QueryViewApplication(String propertiesFileName, String defaultAppName) {
		this(propertiesFileName, defaultAppName, 2);
	}

	public QueryViewApplication(String propertiesFileName, String defaultAppName, int maxOpenProjects) {
		super(propertiesFileName, defaultAppName, maxOpenProjects);
	}

	protected void createApplicationSpecificActions() {
		// do nothing
	}

	protected void createMenuActions() {
		// do nothing
	}

	public void setDefaultUserControls(boolean loadFromProperties) {
		// do nothing
	}

	protected void addDesktopFrame(ShrimpProject project, ShrimpViewConfiguration config) {
		// do nothing - don't want to create ShrimpView
	}

	protected void loadBuildProperties(String defaultAppName, String buildPropertiesFilename) {
		this.buildProperties = new BuildProperties(defaultAppName);
		// use the StandAloneApplication's build file
		this.buildProperties.loadBuildProperties(buildPropertiesFilename, StandAloneApplication.class);
	}

}
