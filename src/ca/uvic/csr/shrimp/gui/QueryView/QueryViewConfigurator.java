/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.quickview.ExpressViewConfigurator;

/**
 * Configures a view to show the results of a query,
 * that is nodes of interest and their neighbours.
 *
 * @author Rob Lintern
 */
public class QueryViewConfigurator extends ExpressViewConfigurator {

    private static final boolean DEFAULT_CASE_SENSITIVE = false;
    private static final int DEFAULT_STRING_MATCHING_MODE = QueryHelper.STRING_MATCH_REGEXP_MODE;
    private static final boolean DEFAULT_CHANGE_TRANSPARENCY = false;
    private static final int DEFAULT_INCOMING_LEVELS = 1;
    private static final int DEFAULT_OUTGOING_LEVELS = 1;

    private Collection srcExternalArtifactIDs = Collections.EMPTY_LIST;
	private int incomingLevels = DEFAULT_INCOMING_LEVELS;
	private int outgoingLevels = DEFAULT_OUTGOING_LEVELS;

    public QueryViewConfigurator(ShrimpProject project) {
        super(project);
    }

    public void dispose() {
    	srcExternalArtifactIDs = Collections.EMPTY_LIST;
    	super.dispose();
    }

    public Vector getSrcArtifacts() {
        Vector srcArtifacts = new Vector();
		if (project == null) {
			return srcArtifacts;
		}

		try {
		    getToolsAndBeans();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
			return srcArtifacts;
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
			return srcArtifacts;
		}
        if ((srcExternalArtifactIDs == null) || srcExternalArtifactIDs.isEmpty()) {
            Vector srcNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
            srcArtifacts = new Vector();
            srcExternalArtifactIDs = new ArrayList(srcNodes.size());
            for (Iterator iter = srcNodes.iterator(); iter.hasNext(); ) {
                ShrimpNode srcNode = (ShrimpNode) iter.next();
                Artifact srcArtifact = srcNode.getArtifact();
                if (!srcArtifacts.contains(srcArtifact) && nodeTypesOfInterest.contains(srcArtifact.getType())) {
                    srcArtifacts.add(srcArtifact);
                    srcExternalArtifactIDs.add(srcArtifact.getExternalId());
                }
            }
        } else {
            for (Iterator iter = srcExternalArtifactIDs.iterator(); iter.hasNext();) {
                Object artifactID = iter.next();
                Artifact srcArtifact = dataBean.findArtifactByExternalId(artifactID);
                if (srcArtifact != null) {
                    if (!srcArtifacts.contains(srcArtifact) && nodeTypesOfInterest.contains(srcArtifact.getType())) {
                        srcArtifacts.add(srcArtifact);
                    }
                } else {
                    System.err.println("can't find artifact for id = " + artifactID);
                }
            }
        }
        return srcArtifacts;
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.gui.ExpressViewConfigurator#addFlatRootNodes()
     */
    protected void addFlatRootNodes() {
        Vector srcArtifacts = getSrcArtifacts();
        if (srcArtifacts.isEmpty()) {
			return;
		}

		// get all relationships first from back-end otherwise incoming arcs will be missing
        dataBean.getRelationships(true);

        // parameters for artifact types
        QueryHelper queryHelper = new QueryHelper(shrimpView);
        queryHelper.setArtifactTypes(new Vector(nodeTypesOfInterest));

        // parameters for neighbours
        queryHelper.setLevels(incomingLevels, outgoingLevels);

        // parameters for source artifacts
        queryHelper.setStringMatchingMode(DEFAULT_STRING_MATCHING_MODE);
        queryHelper.setCaseSensitive(DEFAULT_CASE_SENSITIVE);
        queryHelper.setSrcArtifacts(srcArtifacts);

        // parameters for relationship types
        queryHelper.setRelationshipTypes(new Vector(arcTypesOfInterest));

        queryHelper.setLayoutMode(layoutMode);
        queryHelper.setChangeTransparency(DEFAULT_CHANGE_TRANSPARENCY);

		queryHelper.doQuery();

		Vector srcNodes = dataDisplayBridge.getShrimpNodes(new Vector(srcArtifacts), false);
		selectorBean.setSelected(SelectorBeanConstants.SELECTED_NODES, srcNodes);
    }

    /**
     * @param srcExternalArtifactIDs Sets The IDs of source artifacts.
     */
    public void setSrcExternalArtifactIDs(Collection srcExternalArtifactIDs) {
        this.srcExternalArtifactIDs = (srcExternalArtifactIDs != null ? srcExternalArtifactIDs : Collections.EMPTY_LIST);
    }

    public Collection getSrcExternalArtifactIDs() {
    	return srcExternalArtifactIDs;
    }

    public void setLevels(int incoming, int outgoing) {
    	this.incomingLevels = incoming;
    	this.outgoingLevels = outgoing;
    }


}
