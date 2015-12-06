/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Component;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * OWL ResourceAction class for showing the subclass tree in the query view.
 * 
 * @author Rob Lintern
 * @author Chris Callendar
 */
public class SubclassTreeResourceAction extends ShowInQueryViewResourceAction {

    public SubclassTreeResourceAction() {
        super(new SubclassTreeAction("Show Subclass Tree (Jambalaya)..."), "Jambalaya", false, 
        		"Show how this resource relates to all it subclasses...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.stanford.smi.protegex.owl.ui.actions.ResourceAction#isSuitable(java.awt.Component,
     *      edu.stanford.smi.protegex.owl.model.RDFResource)
     */
    public boolean isSuitable(Component component, OWLEntity resource) {
        return resource instanceof OWLClass;
    }

}
