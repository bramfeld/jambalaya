/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.Component;

import org.semanticweb.owlapi.model.OWLOntology;


/**
 * Interface for any Jambalaya Component.
 *
 * @author Chris Callendar
 */
public interface IJambalayaContainer {

	JambalayaApplication getJambalayaApplication();

	JambalayaProject getJambalayaProject();

	OWLOntology getProject();

	Component add(Component comp);

	void validate();

	boolean isVisible();

}
