/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.util.Set;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;

import ca.uvic.csr.shrimp.DataBean.ShrimpAnnotationNodeDocument;
import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Protege annotation (changes).
 *
 * @author Chris Callendar
 * @date 2008-11-25
 */
public class ProtegeAnnotationNodeDocument extends ShrimpAnnotationNodeDocument implements HyperlinkListener {

	private String content;

	public ProtegeAnnotationNodeDocument(String frameName, String content, String subject, String author, String date, boolean reply) {
		super(frameName);
		this.content = content;
		filename = "Protege Annotation";// + (reply ? " Reply" : "");
		directory = subject + " [" + author + ", " + date + "]";
		// must set the path since it is used for comparison
		path = directory;
		icon = ResourceHandler.getIcon("icon_annotation_protege.gif");
	}

	public String getContentType() {
		return "text/html";
	}

	public boolean canEdit() {
		// can't edit protege annotations
		return false;
	}

	public void setContent(String newContent) {
		// should never happen, content doesn't change
	}

	public String getContent() {
		return content;
	}

	/**
	 * @return true if there is non blank content
	 */
	public boolean hasContent() {
		String c = getContent();
		return ((c != null) && (c.length() > 0));
	}

	public boolean canRemove() {
		return false;
	}

	public HyperlinkListener getHyperlinkListener() {
		// displays any hyperlinked instances in a popup window
		return this;
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			Element src = e.getSourceElement();
			int start = src.getStartOffset();
			int end = src.getEndOffset();
			try {
				// will be something like: @'http://www.co-ode.org/ontologies/pizza/2005/10/18/pizza.owl#Pizza'
				String instanceName = src.getDocument().getText(start, end-start);
				// strip off the @'...'
				if (instanceName.startsWith("@'") && instanceName.endsWith("'")) {
					instanceName = instanceName.substring(2, instanceName.length() - 1);
					JambalayaView view = JambalayaView.globalInstance();
					if (view != null) {
						OWLEditorKit ek = view.getOWLEditorKit();
						if (ek != null) {
							Set<OWLEntity> fnd = ek.getOWLModelManager().getOWLEntityFinder().getEntities(IRI.create(instanceName));
							if (! fnd.isEmpty())
								ek.getOWLWorkspace().displayOWLEntity(fnd.iterator().next());
						}
					}
				}
			} catch (Throwable t) {
				//t.printStackTrace();
			}
		}
	}

}
