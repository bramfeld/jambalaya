/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntology;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.AbstractShrimpApplication;
import ca.uvic.csr.shrimp.adapter.HelpQuickStartAdapter;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ShowInBrowserAction;

/**
 * Implements Shrimp as a plugin for Protege, also known as "Jambalaya"
 * The JambalayaApplication has at most one JambalayaProject at a given time,
 * unlike stand-alone Shrimp which can have many.
 *
 * @author Nasir Rather, Rob Lintern, Chris Callendar
 */
public class JambalayaApplication extends AbstractShrimpApplication {

	/**Title of the Jambalaya application. */
	public static final String APPLICATION_TITLE = "Jambalaya";

	/** Filename where properties for the Jambalaya application will be stored */
	private static final String PROPERTIES_FILE_NAME = "jambalaya.properties";

    public final static String QUICK_VIEW_CLASS_TREE = "Class Tree";
    public final static String QUICK_VIEW_CLASS_INSTANCE_TREE = "Class & Instance Tree";
    public final static String QUICK_VIEW_CLASS_INDIVIDUAL_TREE = "Class & Individual Tree";
    public final static String QUICK_VIEW_DOMAIN_RANGE = "Domain/Range";
    public final static String QUICK_VIEW_PART_OF_TREE = "Part_of Tree";
    public final static String QUICK_VIEW_NESTED_COMPOSITE_VIEW = "Nested Composite View";

	private OWLModelManager modelManager = null;
	private OWLEditorKit editorKit = null;
	
	/**
	 * Creates the Jambalaya application.
	 */
	public JambalayaApplication() {
		super(PROPERTIES_FILE_NAME, APPLICATION_TITLE, 1);
		JambalayaView view = JambalayaView.globalInstance();
		if (view != null) {
			modelManager = view.getOWLModelManager();
			editorKit = view.getOWLEditorKit();
		}
		createApplicationActions();
	}

    public JambalayaApplication(OWLModelManager mm, OWLEditorKit ek) {
		super(PROPERTIES_FILE_NAME, APPLICATION_TITLE, 1);
        modelManager = mm;
        editorKit = ek;
        createApplicationActions();
    }

    public JambalayaProject getJambalayaProject() {
    	return (JambalayaProject) getFirstProject();
    }

    /**
     * For the applets.
     */
    public JMenuBar getMenuBar() {
    	return menuBar;
    }

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.AbstractShrimpApplication#createMenus()
	 */
	public void createMenus() {
		JambalayaView view = JambalayaView.globalInstance();
		if (view != null) {
			view.createMenus();
		} else if (menuBar != null){
            super.createMenus();
        }
	}

	// Add actions for this application to the actionManager.
	protected void createApplicationSpecificActions() {
		super.createApplicationSpecificActions();

		//Help -> Online Manual
		ShrimpAction action = new ShowInBrowserAction(ShrimpConstants.ACTION_NAME_ONLINE_MANUAL, ShrimpConstants.JAMBALAYA_MANUAL_WEBSITE, ResourceHandler.getIcon("icon_help.gif"));
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 1);

		//Help -> Website
		action = new ShowInBrowserAction(ShrimpConstants.ACTION_NAME_JAMBALAYA_WEBSITE, ShrimpConstants.JAMBALAYA_WEBSITE, ResourceHandler.getIcon("icon_jambalaya.gif"));
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 2);

		// Help -> Show Quick Start
		action = new HelpQuickStartAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 3);
	}

	public void close() {
		super.close();
	}

	public void fireApplicationActivatedEvent() {
		super.fireApplicationActivatedEvent();
	}

	public void fireApplicationDeactivatedEvent() {
		super.fireApplicationDeactivatedEvent();
	}

	public void fireApplicationStartedEvent() {
		super.fireApplicationStartedEvent();
	}

	/**
	 * Creates a Protege Project from the URI, and then creates the JambalayaProject for that project.
	 * @param sourceURI
	 * @return JambalayaProject or null
	 */
	public JambalayaProject createJambalayaProject(URI sourceURI) {
      //System.out.println("createJambalayaProject");
		JambalayaProject project = null;
		OWLOntology ontology = createProtegeProject(sourceURI);
		if (ontology != null)
			project = createJambalayaProject(ontology, sourceURI);
		return project;
	}

	public OWLOntology createProtegeProject(URI sourceURI) {
		OWLOntology ontology = null;
		try {
			if (modelManager != null)
				ontology = modelManager.createNewOntology(new OWLOntologyID(IRI.create(sourceURI)), sourceURI);
		}
		catch (Exception ex) {
		}
		return ontology;
	}

	/**
	 * Creates and returns a new JambalayaProject.
	 */
	public JambalayaProject createJambalayaProject(OWLOntology ontology) {
		return createJambalayaProject(ontology, ontology.getOntologyID().getOntologyIRI().toURI());
	}

	/**
	 * Creates and returns a new JambalayaProject.
	 */
	public JambalayaProject createJambalayaProject(OWLOntology ontology, URI sourceURI) {
		JambalayaProject project = new JambalayaProject(this, modelManager, editorKit, ontology, sourceURI);
		addProject(project);
		return project;
	}

}