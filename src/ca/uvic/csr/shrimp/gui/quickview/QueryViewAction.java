/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.QueryView.QueryViewConfigurator;

/**
 * An action that configures the view to show the results
 * of a query, that is nodes of interest and their neighbours.
 *
 * @author Rob Lintern
 */
public class QueryViewAction extends QuickViewAction {

	/**
	 * Don't use this constructor.  It is used to instantiate this class using reflection.
	 */
	public QueryViewAction() {
		super();
	}

    protected QueryViewAction(String actionName, String iconFilename, ShrimpProject project,
    		Collection nodeTypes, Collection arcTypes, String layoutMode, String labelMode,
    		int incomingLevels, int outgoingLevels) {
        super(actionName, iconFilename, project);
        config.setNodeTypesOfInterest(nodeTypes);
        config.setArcTypesOfInterest(arcTypes);
        config.setLayoutMode(layoutMode);
        config.setLabelMode(labelMode);
        config.setCprels(new String[0]);
        getQueryViewConfigurator().setLevels(incomingLevels, outgoingLevels);
    }

    protected ExpressViewConfigurator createExpressViewConfigurator(ShrimpProject project) {
    	return new QueryViewConfigurator(project);
    }

    private QueryViewConfigurator getQueryViewConfigurator() {
    	return (QueryViewConfigurator) config;
    }

    public void startAction() {
        if (!getSrcArtifacts().isEmpty()) {
    	    super.startAction();
        } else {
        	StringBuffer buffer = new StringBuffer();
        	Collection types = config.getNodeTypesOfInterest();
        	for (Iterator iter = types.iterator(); iter.hasNext(); ) {
        		String type = (String) iter.next();
        		if (buffer.length() == 0) {
                	buffer.append("Please select a node that is of type:\n");
        		} else if (iter.hasNext()) {
        			buffer.append(", ");
        		} else {
        			buffer.append(" or ");
        		}
        		buffer.append(type);
        	}
        	JOptionPane.showMessageDialog(null, buffer.toString(), getActionName() + " Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setSrcExternalArtifactIDs(Collection srcArtifactExternalIDs) {
    	getQueryViewConfigurator().setSrcExternalArtifactIDs(srcArtifactExternalIDs);
    }

    /**
     * Overridden to verify if any src external artifact IDs have been set.
     */
    protected boolean isValid() {
    	// && (getQueryViewConfigurator().getSrcExternalArtifactIDs().size() > 0);
    	// we can't set this to be invalid because it will never show up in the quick view panel
    	// instead an error message will be displayed if a method or constructor isn't selected
    	return super.isValid();
    }

    public void applyDataFilters() {
        config.applyDataFilters();
    }

    public Vector getSrcArtifacts() {
        return getQueryViewConfigurator().getSrcArtifacts();
    }

}
