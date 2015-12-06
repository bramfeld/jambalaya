/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.io.Serializable;
import java.util.Vector;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This {@link UserAction} adapter will handle fisheyeing in.
 *
 * @author Casey Best, Chris Callendar
 * @date Jan 30, 2001
 */
public class FisheyeInAdapter extends FisheyeAdapter implements Serializable {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_FISHEYE_IN;
	public static final String TOOLTIP = "Zooms in using a fisheye lens.\n" +
			"The selected node(s) will enlarge and the surrounding nodes will shrink.";

    public FisheyeInAdapter(ShrimpTool tool) {
    	super(ACTION_NAME, tool, ZOOM_DIRECTION_IN, TOOLTIP);
    }

	/**
	 * Starts fisheyeing
	 */
    public void startAction() {
	    try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			ShrimpNode target = getSelectedNode();
			Vector targets = new Vector();
			targets.addElement(target);
			displayBean.startFisheyeingIn(targets);
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

}

