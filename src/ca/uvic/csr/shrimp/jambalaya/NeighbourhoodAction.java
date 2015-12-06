/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * This action uses the QueryView to show the neighborhood of the source artifacts.
 *
 * @author Chris Callendar
 */
public class NeighbourhoodAction extends ShowInQueryViewAction {

	/**
	 * Initializes this with the default "Show Neighbourhood" name and the default Jambalaya icon.
	 */
	public NeighbourhoodAction() {
		this("Show Neighbourhood");
	}

	/**
	 * Initializes this with the given name and the default Jambalaya icon.
	 */
	public NeighbourhoodAction(String name) {
		super(name, ResourceHandler.getIcon("icon_jambalaya.gif"));
	}

	public NeighbourhoodAction(String name, Icon icon) {
		super(name, icon);
	}

	/**
	 * @param srcArtifacts the artifacts to query
	 */
	protected void setupQuery(DataBean dataBean, QueryView queryView, Vector srcArtifacts) {
        queryView.setLevels(1, 1);

        // parameters for source artifacts
       	queryView.setSrcArtifacts(srcArtifacts);

        // parameters for relationship types
        List relTypes = dataBean.getRelationshipTypes(true, true);
        // TODO should we remove the protege dependency here?
        relTypes.remove(ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE);
        relTypes.remove(ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE);
        queryView.setRelationshipTypes(relTypes);

        // parameters for layout
        queryView.setLayoutMode(LayoutConstants.LAYOUT_SPRING);

        // parameters for transparency
        queryView.getQueryHelper().setChangeTransparency(true);
	}

}
