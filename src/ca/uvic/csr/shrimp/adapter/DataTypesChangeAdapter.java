/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.event.DataTypesChangeListener;
import ca.uvic.csr.shrimp.DataDisplayBridge.CompositeArcsManager;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter responds to any types added or removed from a data bean.
 *
 * @author Rob Lintern
 * @date Feb 20, 2002
 */
public class DataTypesChangeAdapter implements DataTypesChangeListener {

	private ShrimpProject project;
	private ShrimpTool tool;

	public DataTypesChangeAdapter(ShrimpProject project, ShrimpTool tool) {
		this.project = project;
		this.tool = tool;
	}

	public void dataTypesChange(DataTypesChangeEvent dtce) {
		// update the composite arc manager with any new rel types and groups
		try {
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			CompositeArcsManager manager = displayBean.getDataDisplayBridge().getCompositeArcsManager();
			Vector relTypes = dataBean.getRelationshipTypes(false, false);
			for (Iterator iter = relTypes.iterator(); iter.hasNext();) {
				String relType = (String) iter.next();
				manager.getRelTypeGroupForType(relType);
			}
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
		}

        // refresh the filter palletes
        // done in Swing thread to avoid deadlock (happens occasionally in creole)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    ApplicationAccessor.getApplication().getTool(ShrimpApplication.NODE_FILTER).refresh();
                } catch (ShrimpToolNotFoundException e) {
                    // do nothing
                }
                try {
                    ApplicationAccessor.getApplication().getTool(ShrimpApplication.ARC_FILTER).refresh();
                } catch (ShrimpToolNotFoundException e) {
                    // do nothing
                }
            }
        });
	}
}
