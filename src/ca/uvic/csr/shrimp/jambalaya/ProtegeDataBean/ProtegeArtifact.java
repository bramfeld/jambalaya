/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import org.protege.editor.owl.model.OWLModelManager;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.AbstractArtifact;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.NodeDocument;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * The ProtegeArtifact is a container for a single entity of
 * information in the Shrimp system.  It represents a class
 * or instance in a Protege {@link KnowledgeBase}.
 *
 * @author Casey Best, Rob Lintern
 */
public class ProtegeArtifact extends AbstractArtifact {

	/** These constants are customized panels that this artifact knows how to handle */
	public final static String PANEL_FORM = "Form";
	public final static String PANEL_METRICS = "Metrics";
	//public final static String PANEL_SEARCH_CRITERIA = "Search Panel";

	/** These constants are for customized data metrics */
    protected final static String INHERITORS_KEY = "Metrics - Inheritors";
	protected final static String INSTANCES_KEY = "Metrics - Instances";
	protected final static String DEPTH_KEY = "Metrics - Tree Depth";
	protected final static String STRAHLER_KEY = "Metrics - Strahler";

	// The frame this artifact represents
	protected OWLModelManager modelManager = null;
	protected OWLOntology project = null;
	protected OWLEntity entity;

	protected ProtegeArtifact(DataBean db, String name, String type, Object externalId){
		super (db, name, type, externalId);
	}

	public ProtegeArtifact(ProtegeDataBean db, OWLEntity e) {
		this (db, db.frameToArtifactName(e), db.frameToArtifactType(e), db.frameToExternalArtifactID(e));
		  this.modelManager = db.getModelManager();
		  this.project = db.getProject();
        this.entity = e;

        // check if we need to load the protege annotations
		loadProtegeAnnotations();

	}

	/**
	 * Returns the Protege frame this artifact represents.
	 */
	public OWLEntity getEntity() {
		return entity;
	}

	/**
	 * This method has been overridden to ask the frame for specific slot info if
	 * there isn't already data for the given attribute name.
	 * NOTE: If there is data in the attributes hash table, the slot WON'T be checked.
	 * @param attrName The name of the attribute or slot you want the value for.
	 */
	 
	/*
	public Object getAttribute(String attrName) {
		Object attrValue = super.getAttribute(attrName);
		if ((attrName != null) && (attrValue == null) && (protegeFrame != null)) {
			Project project = ((ProtegeDataBean)dataBean).getProject();
			if (project != null) {
				KnowledgeBase knowledgeBase = project.getKnowledgeBase();
				Slot slot = knowledgeBase.getSlot(attrName);
				if (slot != null) {
					attrValue = protegeFrame.getOwnSlotValue(slot);
				}
			}
		}
		return attrValue;
	}
	*/

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getDefaultPanelModeOrder()
	 */
	public String[] getDefaultPanelModeOrder() {
		String[] ret = new String[2];
		ret[0] = PanelModeConstants.CHILDREN;
		ret[1] = PanelModeConstants.PANEL_DEFAULT;
		return ret;
	}

	/**
	 * Creates the panel for the panel mode passed in.  Returns null if there isn't
	 * a panel for the mode.
	 * @param mode The panel mode to create a panel for.
	 */
	protected Component createPanel(String mode) {
		if (PanelModeConstants.PANEL_DEFAULT.equals(mode)) {
			addWidgetPanel();
		} else if (PANEL_FORM.equals(mode)) {
			addWidgetPanel();
		} else if (PanelModeConstants.PANEL_ANNOTATION.equals(mode)) {
			addAnnotationPanel();
		} else if (PANEL_METRICS.equals(mode)) {
			addMetricsPanel();
		} else if (PanelModeConstants.PANEL_ATTRIBUTES.equals(mode)) {
		    addAttributesPanel();
		} else if (PanelModeConstants.PANEL_DOCUMENTS.equals(mode)) {
			addDocumentsPanel();
		}

		JComponent panel = (JComponent) customizedPanels.get(mode);
		return panel;
	}

	/**
	 * Returns the available customized panels for this artifact.
	 */
	public Vector getCustomizedPanelNames() {
		Vector names = new Vector();
		names.addElement(PanelModeConstants.PANEL_ANNOTATION);
 		if (entity != null) {
			names.addElement(PANEL_FORM);
			names.addElement(PANEL_METRICS);
			names.addElement(PanelModeConstants.PANEL_ATTRIBUTES);
		}
 		names.addElement(PanelModeConstants.PANEL_DOCUMENTS);
		return names;
	}

	public void setCustomizedPanel(String mode, Component panel) {
		super.setCustomizedPanel(mode, panel);

		// OLD WAY, not efficient, now instead there is a refresh button
		// add a component listener which will reload the protege annotations
//		if (PanelModeConstants.PANEL_DOCUMENTS.equals(mode)) {
//			panel.addComponentListener(new ComponentAdapter() {
//				public void componentShown(ComponentEvent e) {
//					// reload the protege annotations?
//					loadProtegeAnnotations();
//				}
//			});
//		}
	}


	/********************* Methods from Object ***********************/

	/**
	 * Overrides the Object method with the same name.  Used to index
	 * hashTables correctly.
	 */
	public int hashCode() {
		/* ***** NOTE: We dont want to use id.hashCode here because id is a string, and
		 * if the String's length exceeds 16 characters, String.hashCode() samples only a
		 * portion of the String, which may result in duplicate hashcodes.
		 */
		int hashCode = (entity == null) ? 0 : entity.toStringID().hashCode();
		return hashCode;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.AbstractArtifact#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		// TODO should take into account hashcode returned above
		return super.equals(obj);
	}

	/**
	 * Returns a clone of this artifact
	 */
	public Object clone() {
		ProtegeArtifact art = new ProtegeArtifact ((ProtegeDataBean) dataBean, entity);
		Enumeration keys = getCustomizedPanelNames().elements();
		while (keys.hasMoreElements()) {
			String name = (String)keys.nextElement();
			art.setCustomizedPanel(name, getCustomizedPanel(name));
		}

		keys = getAttributeNames().elements();
		while (keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			art.setAttribute(name, getAttribute(name));
		}

		return art;
	}

	private JComponent getWidgetPanel(OWLEntity e) {
		JComponent widget = null;
		if (dataBean instanceof ProtegeDataBean) {
			widget = ((ProtegeDataBean) dataBean).getClsWidget(e);
		}
		return widget;
	}

	/**
	 * Add the widget pane
	 */
	protected void addWidgetPanel() {
		JComponent widget = getWidgetPanel(entity);
		if (widget != null) {
			JScrollPane scrollPane = new JScrollPane(widget);
			setCustomizedPanel (PanelModeConstants.PANEL_DEFAULT, scrollPane);
			setCustomizedPanel(PANEL_FORM, scrollPane);
		}
	}

	protected void addMetricsPanel() {
		JPanel pane = new JPanel();

	    String inheritorsString = "# Inheritors: ";
	    String instancesString = "# Instances: ";
	    String depthString = "Tree Depth: ";
	    String strahlerString = "Strahler: ";

		ProtegeMetrics metrics = ((ProtegeDataBean)dataBean).getMetrics();
        setAttribute(INHERITORS_KEY, new Integer (metrics.getNumInheritors(entity)));
        setAttribute(INSTANCES_KEY, new Integer (metrics.getNumInstances(entity)));
        setAttribute(DEPTH_KEY, new Integer (metrics.getTreeDepth(entity)));
        setAttribute(STRAHLER_KEY, new Integer (metrics.getStrahler(entity)));

		strahlerString += getAttribute(STRAHLER_KEY);
       	inheritorsString += getAttribute(INHERITORS_KEY);
        instancesString += getAttribute(INSTANCES_KEY);
        depthString += getAttribute(DEPTH_KEY);

	    JLabel inheritorsLabel;
	    JLabel instancesLabel;
	    JLabel depthLabel;
	    JLabel strahlerLabel;
        inheritorsLabel = new JLabel(inheritorsString);
        instancesLabel = new JLabel(instancesString);
        depthLabel = new JLabel(depthString);
        strahlerLabel = new JLabel(strahlerString);
        pane.setLayout(new GridLayout(0, 1));
        pane.add(inheritorsLabel);
        pane.add(instancesLabel);
        pane.add(depthLabel);
        pane.add(strahlerLabel);
		setCustomizedPanel (PANEL_METRICS, pane);
	}

    public int getTreeDepth() {
        int depth = 0;
        ProtegeMetrics pm = ((ProtegeDataBean)dataBean).getMetrics();
        if (entity instanceof OWLClass) {
            OWLClass cls = (OWLClass) entity;
            depth = pm.getTreeDepth(cls);
        }
        return depth;
    }

	/**
	 * Loads the protege annotations.
	 * First checks if the ChAOUtil class from the collab changes plug-in is present.
	 * If so it calls the ProtegeAnnotationsLoader class to load the annotations for the frame.
	 * Then it creates a ProtegeAnnotationNodeDocument for each annotation and adds
	 * them to this artifact.
	 */
	private void loadProtegeAnnotations() {
		Collection documents = new ProtegeAnnotations().loadAnnotationDocuments(entity, project, modelManager);
		for (Object obj : documents) {
			if (obj instanceof NodeDocument)
				attachDocument((NodeDocument) obj);
		}
	}

	protected void createHeaderButtons(JPanel panel) {
		super.createHeaderButtons(panel);
		// add after the help button
		panel.add(createRefreshDocumentsButton(), 1);
		panel.add(createAddAnnotationButton());
	}


	private JButton createRefreshDocumentsButton() {
		DefaultShrimpAction action = new DefaultShrimpAction(ResourceHandler.getIcon("icon_refresh.gif"),
															 "Refresh the documents and annotations") {
			public void actionPerformed(ActionEvent e) {
				loadProtegeAnnotations();
			}
		};
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(22, 22));
		return btn;
	}

	protected JButton createAddAnnotationButton() {
		DefaultShrimpAction action = new DefaultShrimpAction("Add Annotation",
				ResourceHandler.getIcon("icon_annotation_protege.gif"),
				"Add a Protege Annotation to the Changes Ontology") {
			public void actionPerformed(ActionEvent e) {
				createAnnotation();
			}
		};
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(120, 22));
		return btn;
	}

	/**
	 * Loads the protege annotations.
	 * First checks if the ChAOUtil class from the collab changes plug-in is present.
	 * If so it calls the ProtegeAnnotationsLoader class to load the annotations for the frame.
	 * Then it creates a ProtegeAnnotationNodeDocument for each annotation and adds
	 * them to this artifact.
	 */
	protected void createAnnotation() {
		boolean added = new ProtegeAnnotations().addAnnotation(entity, project);
		if (added) {
			// reload the annotations to get the new one.
			loadProtegeAnnotations();
		}
	}

}
