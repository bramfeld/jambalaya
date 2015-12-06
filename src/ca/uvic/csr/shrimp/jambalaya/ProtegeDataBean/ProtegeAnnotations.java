/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.jambalaya.JambalayaView;
import ca.uvic.csr.shrimp.jambalaya.ProtegeAnnotationNodeDocument;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 *
 *
 * @author Chris Callendar
 * @date 2008-11-26
 */
public class ProtegeAnnotations {

	public ProtegeAnnotations() {
	}

	public Collection loadAnnotationDocuments(OWLEntity e, OWLOntology o, OWLModelManager mm) {
		Collection documents = new ArrayList/*<ProtegeAnnotationNodeDocument>*/();
		try {
			for (OWLAnnotation a : e.getAnnotations(o)) {
				ProtegeAnnotationNodeDocument doc = new ProtegeAnnotationNodeDocument(e.toStringID(),
						mm.getRendering(a.getValue()), mm.getRendering(e), null, null, false);
				documents.add(doc);
				//System.out.println("Added protege annotation to " + e.getBrowserText());

				// add replies after
				for (OWLAnnotation a2 : a.getAnnotations()) {
					ProtegeAnnotationNodeDocument doc2 = new ProtegeAnnotationNodeDocument(e.toStringID(),
							mm.getRendering(a2.getValue()), mm.getRendering(e), null, null, true);
					documents.add(doc2);
				}
			}
		} catch (Throwable t) {
			System.err.println("Error loading protege annotation: " + t.getMessage());
		}
		return documents;
	}

	public boolean addAnnotation(OWLEntity e, OWLOntology o) {
		try {
			OWLOntologyManager om = o.getOWLOntologyManager();
			OWLDataFactory df = om.getOWLDataFactory();
			OWLAnnotationProperty prp = df.getOWLAnnotationProperty(IRI.create("rdfs:comment"));
			OWLAnnotation a = df.getOWLAnnotation(prp, df.getOWLLiteral(":"));
			displayAnnotationWindow(a);
		} catch (RuntimeException ex) {
			System.err.println("Error creating annotation: " + ex.getMessage());
			return false;
		}
		return true;
	}

	private boolean displayAnnotationWindow(OWLAnnotation a) {
		JambalayaView view = JambalayaView.globalInstance();
		if (view != null) {
			OWLEditorKit ek = view.getOWLEditorKit();
			if (ek != null) {
				OWLAnnotationEditor ed = new OWLAnnotationEditor(ek);
				ed.setEditedObject(a);
				Object[] options = {"OK", "Cancel"};
				int ret = JOptionPane.showOptionDialog(ApplicationAccessor.getParentFrame(),
						ed.getEditorComponent(), "OWL Annotation",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
						ResourceHandler.getIcon("icon_annotation.gif"),
						options, options[0]);
				ed.dispose();
				return (ret == JOptionPane.OK_OPTION);
			}
		}
		return false;
	}

}
