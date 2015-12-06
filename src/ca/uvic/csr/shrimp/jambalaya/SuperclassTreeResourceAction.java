/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Component;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * OWL ResourceAction for showing the superclass tree in the QueryView.
 * @author Rob Lintern
 * @author Chris Callendar
 */
public class SuperclassTreeResourceAction extends ShowInQueryViewResourceAction {

    public SuperclassTreeResourceAction() {
        super(new SuperclassTreeAction("Show Superclass Tree (Jambalaya)..."), "Jambalaya", false, 
        		"Show how this resource relates to all it superclasses...");
    }
 
    /*
     * (non-Javadoc)
     * @see edu.stanford.smi.protegex.owl.ui.actions.ResourceAction#isSuitable(java.awt.Component,
     *      edu.stanford.smi.protegex.owl.model.RDFResource)
     */
    public boolean isSuitable(Component component, OWLEntity resource) {
        return resource instanceof OWLClass;
    }

}
