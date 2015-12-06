/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.TreeMapLayout;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.usercontrols.OpenAllAdapter;

/**
 * Sets up the {@link ExpressViewConfigurator} to show a nested view using the treemap layout.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class NestedTreemapViewAction extends QuickViewAction {

    public static final String ACTION_NAME = "Nested Treemap";

    /**
	 * Don't use this constructor.  It is used to instantiate this class using reflection.
	 */
	public NestedTreemapViewAction() {
		super();
	}

    protected NestedTreemapViewAction(ShrimpProject project, Collection nodeTypes, String[] cprels, boolean inverted) {
        super(ACTION_NAME, "icon_quick_view_treemap.gif", project);

		config.setArcTypesOfInterest(Collections.EMPTY_LIST);
		config.setNodeTypesOfInterest(nodeTypes);
		config.setLabelMode(DisplayConstants.LABEL_MODE_SCALE_BY_NODE_SIZE);
		config.setLayoutMode(LayoutConstants.LAYOUT_TREEMAP);
		config.setCprels(cprels);
		config.setInverted(inverted);
    }

    public void startAction() {
        if (!hasProject()) {
			return;
		}

        DisplayBean displayBean = null;
        ShrimpTool shrimpView = null;
		try {
			shrimpView = getTool(ShrimpProject.SHRIMP_VIEW);
            displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        List layouts = displayBean.getLayouts();
        TreeMapLayout treeMapLayout = null;
        for (Iterator iter = layouts.iterator(); iter.hasNext();) {
            Layout layout = (Layout) iter.next();
            if (layout instanceof TreeMapLayout) {
                treeMapLayout = (TreeMapLayout) layout;
                break;
            }
        }
        if (treeMapLayout == null) {
			return;
		}

        treeMapLayout.setMapLayoutName(TreeMapLayout.LAYOUT_ORDERED);
        treeMapLayout.setOrderField(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS);
        treeMapLayout.setSizeField(AttributeConstants.ORD_ATTR_NUM_DESCENDENTS);
        treeMapLayout.setBorderField(0);

        Properties props = ApplicationAccessor.getProperties();
        String oldDefaultLayoutMode = props.getProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE, DisplayBean.PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);
        props.setProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE, LayoutConstants.LAYOUT_TREEMAP);

        // configure the express view
        super.startAction();

        OpenAllAdapter openAllAdapter = new OpenAllAdapter(getProject(), shrimpView);
		openAllAdapter.startAction();
		props.setProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE, oldDefaultLayoutMode);
    }

}
