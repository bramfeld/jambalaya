/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Action that shows the superclass tree of the selected node(s).
 * 
 * @author Chris Callendar
 */
public class SuperclassTreeAction extends ShowInQueryViewAction {

	public SuperclassTreeAction() {
		this("Show Superclass Tree");
	}
	
	public SuperclassTreeAction(String name) {
		super(name, ResourceHandler.getIcon("icon_jambalaya.gif"));
	}

	public SuperclassTreeAction(String name, Icon icon) {
		super(name, icon);
	}
	
   /**
     * @param dataBean
     * @param queryView
     * @param srcArtifacts the artifacts to query
     */
    protected void setupQuery(DataBean dataBean, final QueryView queryView, Vector srcArtifacts) {
        // parameters for artifact types
        List artTypes = new ArrayList(3);
        artTypes.add(ProtegeDataBean.CLASS_ART_TYPE);
        artTypes.add(ProtegeDataBean.PRIMITIVE_CLASS_TYPE);
        artTypes.add(ProtegeDataBean.DEFINED_CLASS_TYPE);
        queryView.setArtTypes(artTypes);

        // parameters for neighbours
        queryView.setLevels(-1, 0);	// show all ancestors

        // parameters for source artifacts
        queryView.setSrcArtifacts(srcArtifacts);

        // parameters for relationship types
        List relTypes = new ArrayList(1);
        relTypes.add(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
        queryView.setRelationshipTypes(relTypes);
        
        // parameters for layout
        queryView.getQueryHelper().setLayoutMode(LayoutConstants.LAYOUT_TREE_HORIZONTAL);

        // parameters for transparency
        queryView.getQueryHelper().setChangeTransparency(true);
    }

}
