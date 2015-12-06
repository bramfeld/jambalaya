/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.util.ArrayList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Icon;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLClassExpressionSetEditor;
import org.protege.editor.owl.ui.editor.OWLIndividualEditor;
import org.protege.editor.owl.ui.OWLIcons;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.usercontrols.DefaultToolAction;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This {@link UserAction} adapter displays the properties for the selected node.
 *
 * @author Chris Callendar
 * @date April 10, 2007
 */
public class ShowNodePropertiesAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_NODE_PROPERTIES;
	public static final String TOOLTIP = "Displays the properties of the selected node.\n" +
		"In Jambalaya the Protege Property Editor is displayed.";

	private String toolName = null;

    public ShowNodePropertiesAdapter(ShrimpProject project, ShrimpTool tool) {
    	super(ACTION_NAME, project, tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;
    }

    public ShowNodePropertiesAdapter(ShrimpProject project, String toolName) {
    	this(project, (ShrimpTool)null);
    	this.toolName = toolName;
    }

    public void startAction() {
    	if ((tool == null) && (toolName != null) ) {
    		try {
				tool = getProject().getTool(toolName);
			} catch (ShrimpToolNotFoundException e) {
			}
    	}
    	if (tool != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
				Object targetObject = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
				if (targetObject instanceof ShrimpNode) {
					Artifact artifact = ((ShrimpNode) targetObject).getArtifact();
					if (artifact instanceof ProtegeArtifact) {
						displayProtegePropertyEditor((ProtegeArtifact) artifact);
					}
				}
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
		} else {
			setEnabled(false);
		}
    }

	/**
	 * Opens the default Protege property editor window for given artifact.
	 */
	private void displayProtegePropertyEditor(ProtegeArtifact artifact) {
		OWLEntity e = (OWLEntity) artifact.getEntity();
		if (e != null) {
			JambalayaView view = JambalayaView.globalInstance();
			if (view != null) {
				OWLEditorKit ek = view.getOWLEditorKit();
				if (ek != null) {
					if (e instanceof OWLClass) {
						ArrayList exp = new ArrayList(); exp.add((OWLClass) e);
						OWLClassExpressionSetEditor ed = new OWLClassExpressionSetEditor(ek, exp);
						Object[] options = {"OK"};
						JOptionPane.showOptionDialog(ApplicationAccessor.getParentFrame(),
								ed.getEditorComponent(), "OWL Class",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
								OWLIcons.getIcon("class.defined.png"),	options, options[0]);
						ed.dispose();
					} else if (e instanceof OWLNamedIndividual) {
						OWLIndividualEditor ed = new OWLIndividualEditor(ek);
						ed.setEditedObject((OWLNamedIndividual) e);
						Object[] options = {"OK"};
						JOptionPane.showOptionDialog(ApplicationAccessor.getParentFrame(),
								ed.getEditorComponent(), "OWL Individual",
								JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
								OWLIcons.getIcon("individual.png"),	options, options[0]);
						ed.dispose();
					}
				}
			}
		}
	}

}
