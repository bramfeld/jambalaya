/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import edu.umd.cs.piccolo.PCamera;


/**
 * This {@link UserAction} adapter will handle magnifying out
 *
 * @author Casey Best, Chris Callendar
 * @date Jan 30, 2001
 */
public class MagnifyOutAdapter extends ZoomAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_MAGNIFY_OUT;
	public static final String TOOLTIP = "Zooms out to the level of the selected node.";

    public MagnifyOutAdapter(ShrimpTool tool) {
    	super(ACTION_NAME, tool, ZOOM_DIRECTION_OUT, TOOLTIP);
    }

	public boolean canStart() {
		String zoomMode = getZoomMode();
		boolean canStart = DisplayConstants.MAGNIFY.equals(zoomMode);
		return canStart;
	}

	/**
	 * Starts magnifying out
	 */
    public void startAction() {
        if (tool == null) {
        	return;
        }
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
            // use the magnify in adapter to do the work by specifying a new target object
            // this would either be the parent node of a target node
            // or "everything" (MagnifyInAdapter looks for a target of PCamera for this)
            Object oldTarget = getTargetObject();
            Object newTarget = null;
            if (oldTarget != null) {
                if (oldTarget instanceof ShrimpNode) {
                    newTarget = ((ShrimpNode)oldTarget).getParentShrimpNode();
                    if (newTarget == null && displayBean instanceof PNestedDisplayBean) {
                        newTarget =((PNestedDisplayBean)displayBean).getPCanvas().getCamera();
                    }
                } else if (oldTarget instanceof PCamera) {
                    newTarget = oldTarget;
                }
            }

            if (newTarget != null) {
                setTargetObject(newTarget);
                MagnifyInAdapter tempMIA = new MagnifyInAdapter(tool);
                tempMIA.startAction();
                setTargetObject(oldTarget);
            }
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}

    }

 }
