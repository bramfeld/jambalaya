/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.mouse;

import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * Responsible for selecting/highlighting arcs.
 *
 * @author Polly Allen, Rob Lintern, Chris Callendar
 */
public class MouseHighlightArcAdapter extends ShrimpMouseAdapter {
	
    private ShrimpTool tool;
    
    public MouseHighlightArcAdapter(ShrimpTool tool) {
		this.tool = tool;
    }
    
    /**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mouseMoved(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mouseMoved(ShrimpMouseEvent e) {
		handleShrimpMouseEvent(e);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseAdapter#mousePressed(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent)
	 */
	public void mousePressed(ShrimpMouseEvent e) {
		//handleShrimpMouseEvent(e);
	}
	
	private void handleShrimpMouseEvent(ShrimpMouseEvent e) {
		if (e == null) {
			return;
		}
		
		if (e.getTarget() instanceof ShrimpArc) {
			handleShrimpArc((ShrimpArc)e.getTarget());
		} else {
			handleNonShrimpArc();
		}
	}
	
	private void handleNonShrimpArc() {
		try {
			SelectorBean selectorBean = (SelectorBean)tool.getBean(ShrimpTool.SELECTOR_BEAN);
			selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, new Vector());
		} catch (BeanNotFoundException bnfe) {
			//bnfe.printStackTrace();
		}
	}
	
	private void handleShrimpArc(ShrimpArc arc) {
		try {
			SelectorBean selectorBean = (SelectorBean)tool.getBean(ShrimpTool.SELECTOR_BEAN);
			
			// if this arc is not already selected, then select it.
			Vector selectedArcs = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_ARCS);
			if (!selectedArcs.contains(arc)) {
				selectedArcs = new Vector();
				selectedArcs.add(arc);
				selectorBean.setSelected(SelectorBeanConstants.SELECTED_ARCS, selectedArcs);
			}
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
		}
	}

}
