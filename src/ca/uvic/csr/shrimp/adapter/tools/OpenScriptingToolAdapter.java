/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.ScriptingBean.ScriptingTool;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/** 
 * Opens the Scripting Tool UI which allows users to edit and run custom scripts.
 * 
 * @see ScriptingTool
 * @author Nasir Rather
 */
public class OpenScriptingToolAdapter extends AbstractOpenApplicationToolAdapter {

	private static final Rectangle BOUNDS = new Rectangle(DOCK_LEFT_INSIDE, DOCK_BOTTOM_INSIDE, 550, 300);

	public OpenScriptingToolAdapter() {
		super(ShrimpProject.SCRIPTING_TOOL, ResourceHandler.getIcon("icon_script_manager.gif"), BOUNDS);
	}
		
	/**
	 * @see ca.uvic.csr.shrimp.adapter.tools.AbstractOpenApplicationToolAdapter#createTool()
	 */
	protected void createTool() {
		tool = new ScriptingTool(getProject());		
	}
	
}