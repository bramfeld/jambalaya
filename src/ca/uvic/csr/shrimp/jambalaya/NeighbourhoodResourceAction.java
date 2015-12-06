/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Component;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Rob Lintern
 * @author Chris Callendar
 */
public class NeighbourhoodResourceAction extends ShowInQueryViewResourceAction {

    public NeighbourhoodResourceAction() {
        super(new NeighbourhoodAction("Show Neighbourhood (Jambalaya)..."), "Jambalaya", false,
        		"Show how this resource relates to all others...");
    }

    /*
     * (non-Javadoc)
     * @see edu.stanford.smi.protegex.owl.ui.actions.ResourceAction#isSuitable(java.awt.Component,
     *      edu.stanford.smi.protegex.owl.model.RDFResource)
     */
    public boolean isSuitable(Component component, OWLEntity resource) {
    	// TODO do we really want to do this?  It makes this resource action appear in weird places in tabs...
    	// for example it appears in the SWRL tab which doesn't list classes or instances
//        if (resource instanceof OWLHasValue) {
//            OWLHasValue hr = (OWLHasValue) resource;
//            Object hasValue = hr.getHasValue();
//            if (hasValue != null && (hasValue instanceof RDFSNamedClass || hasValue instanceof RDFIndividual)) {
//                return true;
//            }
//        } else if (resource instanceof OWLQuantifierRestriction) {
//            OWLQuantifierRestriction qr = (OWLQuantifierRestriction) resource;
//            if (qr.getFiller() instanceof RDFSNamedClass) {
//                RDFSNamedClass quantCls = (RDFSNamedClass) qr.getFiller();
//                if (quantCls != null) {
//                    return true;
//                }
//            }
//        } else
        if (resource instanceof OWLClass || resource instanceof OWLNamedIndividual) {
            return true;
        }
        return false;
    }

}
