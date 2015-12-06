/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/**
 * Base class for Fisheye In and Out actions.
 *
 * @see FisheyeInAdapter
 * @see FisheyeOutAdapter
 * @author Chris Callendar
 * @date June 25th, 2006
 */
public abstract class FisheyeAdapter extends ZoomAction {

	public FisheyeAdapter(String actionName, ShrimpTool tool, String direction, String tooltip) {
		super(actionName, tool, direction, tooltip);
	}

    public boolean canStart() {
    	boolean canStart = (getSelectedNode() != null);
		return canStart;
    }

	/**
	 * Stops fisheyeing
	 */
	public void stopAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.stopFisheyeing();
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

}
