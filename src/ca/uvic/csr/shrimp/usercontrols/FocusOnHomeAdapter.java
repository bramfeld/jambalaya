/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Focuses on the root node.
 *
 * @author Casey Best, Jeff Michaud, Chris Callendar
 * @date Aug 18, 2000
 */
public class FocusOnHomeAdapter extends DefaultToolAction {
	
	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_HOME;
	public static final String TOOLTIP = "Focuses on the root (top-level) node.";
	
	public FocusOnHomeAdapter(ShrimpTool tool) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_home.gif"), tool);
		setToolTip(TOOLTIP);
	    mustStartAndStop = false;
	}

	public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);		
			displayBean.focusOnExtents(true);
		} catch (BeanNotFoundException bnfe) { 
			bnfe.printStackTrace();
		}
	}
	
}