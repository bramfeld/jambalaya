/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalAttribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalNodeColorVisualVariable;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.DisplayBean.layout.TreeMapLayout;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * @author Rob Lintern
 *
 * Configures the view to show CVS attributes.
 */
public class CVSExpressViewConfigurator extends ExpressViewConfigurator {

    public CVSExpressViewConfigurator(ShrimpProject project) {
        super(project);
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.gui.ExpressViewConfigurator#setupAttrToVisVarBean()
     */
    protected void setupAttrToVisVarBean() {
		String attrName = SoftwareDomainConstants.ATTR_CVS_AUTHOR_MOST_COMMITS;
		NominalAttribute attr = (NominalAttribute) attrToVisVarBean.getAttr(attrName);
		if (attr == null) {
			attr = new NominalAttribute (attrToVisVarBean, attrName, String.class);
			attrToVisVarBean.addAttr(attr);
		}
		VisualVariable visVar = attrToVisVarBean.getVisVar(VisVarConstants.VIS_VAR_NODE_COLOR);
		if (visVar == null || !(visVar instanceof NominalNodeColorVisualVariable)) {
			if (visVar != null) {
				attrToVisVarBean.removeVisVar(visVar);
			}
			visVar = new NominalNodeColorVisualVariable (attrToVisVarBean, VisVarConstants.VIS_VAR_NODE_COLOR);
			attrToVisVarBean.addVisVar(visVar);
		}
		attrToVisVarBean.mapAttrToVisVar(attrName, VisVarConstants.VIS_VAR_NODE_COLOR);
    }

    protected void getToolsAndBeans() throws ShrimpToolNotFoundException, BeanNotFoundException {
    	super.getToolsAndBeans();
        dataBean.clearBufferedData();
    }

    public void configureView(String configDescription) {
    	super.configureView(configDescription);

        Vector visibleNodes = displayBean.getVisibleNodes();
        Vector nodes = new Vector();
        for (Iterator iter = visibleNodes.iterator(); iter.hasNext();) {
        	ShrimpNode node = (ShrimpNode) iter.next();
        	if( node.getArtifact().getType().equals(JavaDomainConstants.JAVA_FILE_ART_TYPE) ) {
        		nodes.add(node);
        	}
        }
        Vector layouts = displayBean.getLayouts();
        TreeMapLayout treeMapLayout = null;
        for (Iterator iter = layouts.iterator(); iter.hasNext() && (treeMapLayout == null);) {
            Layout layout = (Layout) iter.next();
            if (layout instanceof TreeMapLayout) {
                treeMapLayout = (TreeMapLayout) layout;
            }
        }
        if (treeMapLayout != null) {
            treeMapLayout.setOrderField(SoftwareDomainConstants.ATTR_CVS_NUM_COMMITS);
            treeMapLayout.setSizeField(SoftwareDomainConstants.ATTR_CVS_NUM_COMMITS);
        }
        displayBean.setLayoutMode(nodes, LayoutConstants.LAYOUT_TREEMAP, false, false);
        displayBean.setPanelMode(nodes, PanelModeConstants.CLOSED);
    }

}
