/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import org.semanticweb.owlapi.model.OWLEntity;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.tools.OpenQueryViewAdapter;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * Base class for Jambalaya actions that involve showing selected nodes in the Query View.
 *
 * @author Chris Callendar
 */
public abstract class ShowInQueryViewAction extends AbstractAction {

	public ShowInQueryViewAction() {
		this(JavaDomainConstants.JAVA_QUICK_VIEW_QUERY_VIEW);
	}

	public ShowInQueryViewAction(String name) {
		super(name);
	}

	public ShowInQueryViewAction(String name, Icon icon) {
		super(name, icon);
	}

	/**
	 * This method gets the selected artifacts from the ShrimpView SelectorBean
	 * and then runs the QueryView action on those artifacts.
	 */
	public void actionPerformed(ActionEvent e) {
        JambalayaView view = JambalayaView.globalInstance();
        if ((view != null) && (view.getJambalayaProject() != null)) {
    		JambalayaProject project = view.getJambalayaProject();
            try {
            	DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
            	ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
            	SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);
                Vector selectedNodes = (Vector)selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
                // get the artifacts from the nodes
                Vector selectedArtifacts = new Vector(selectedNodes.size());
                for (Iterator iter = selectedNodes.iterator(); iter.hasNext();) {
                    ShrimpNode node = (ShrimpNode) iter.next();
                    if (!selectedArtifacts.contains(node.getArtifact())) {
                        selectedArtifacts.add(node.getArtifact());
                    }
                }
                actionPerformed(dataBean, dataFilterBean, selectedArtifacts);
            } catch (BeanNotFoundException e1) {
                e1.printStackTrace();
            } catch (ShrimpToolNotFoundException e2) {
            	e2.printStackTrace();
            }
        }
	}

	/**
	 * Runs the query view action on the given frames.
	 * @param selectedFrames the selected Frame objects
	 */
	public void actionPerformed(Vector selectedFrames) {
        JambalayaView view = JambalayaView.globalInstance();
        if (view != null) {
    		JambalayaProject project = view.getJambalayaProject();
        	if (project == null) {
        		// creates and adds the project, this is needed if the JambalayaTab hasn't been shown yet
        		project = view.getJambalayaApplication().createJambalayaProject(view.getProject());
        		view.setJambalayaProject(project);
        	}
    		if (project != null) {
	        	try {
					ProtegeDataBean dataBean = (ProtegeDataBean) project.getBean(ShrimpProject.DATA_BEAN);
					FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
					Vector artifacts = getArtifactsForFrames(dataBean, selectedFrames);
					actionPerformed(dataBean, dataFilterBean, artifacts);
				} catch (BeanNotFoundException e) {
					e.printStackTrace();
				}
    		}
        }
	}

	/**
	 * Runs the query view action on the given artifacts.
	 * @param dataBean the data bean
	 * @param dataFilterBean the filter bean
	 * @param artifacts the artifacts
	 */
	protected void actionPerformed(DataBean dataBean, FilterBean dataFilterBean, Vector artifacts) {
		final QueryView queryView = getQueryView();
		importAllData (dataBean, dataFilterBean);
		setupQuery(dataBean, queryView, (artifacts != null ? artifacts : new Vector()));

		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	queryView.refresh();
		        queryView.getQueryHelper().doQuery(false);
		    }
		});
	}

	/**
	 * Gets the artifacts for the given frames.
	 * @param c A collection of Frame objects
	 * @return Vector of Artifact objects
	 */
	private Vector getArtifactsForFrames(ProtegeDataBean dataBean, Collection frames) {
		Vector artifacts = new Vector();
		for (Iterator iter = frames.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof OWLEntity) {
				OWLEntity e = (OWLEntity) obj;
				Artifact artifact = dataBean.findArtifact(e);
				if (artifact != null) {
					artifacts.add(artifact);
				}
			}
		}
		return artifacts;
	}

	/**
	 * Sets the query parameters.
	 * @param dataBean
	 * @param queryView
	 * @param srcArtifacts the selected artifacts to query
	 */
    abstract protected void setupQuery(DataBean dataBean, QueryView queryView, Vector srcArtifacts);

    /**
     * Initializes the query view (also creates the GUI) and returns it.
     * @return QueryView
     */
    private QueryView getQueryView() {
        QueryView queryView = null;
        JambalayaView view = JambalayaView.globalInstance();
        if ((view != null) && (view.getJambalayaProject() != null)) {
			JambalayaProject project = view.getJambalayaProject();
			OpenQueryViewAdapter openQueryViewAdapter = new OpenQueryViewAdapter();
			openQueryViewAdapter.setProject(project);
	        openQueryViewAdapter.startAction();
	        try {
	            queryView = (QueryView) project.getTool(ShrimpProject.QUERY_VIEW);
	        } catch (ShrimpToolNotFoundException e) {
	            queryView = new QueryView(project);
	        }
        }
        return queryView;
    }

    private void importAllData(DataBean dataBean, FilterBean dataFilterBean) {
        // make sure that everything available
        ((ProtegeDataBean)dataBean).setDefaultRootClses();
        Vector allArtTypes = dataBean.getArtifactTypes(true, true);
        Vector allRelTypes = dataBean.getRelationshipTypes(true, true);
        dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE, allArtTypes, false);
        dataFilterBean.addRemoveNominalAttrValues(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, allRelTypes, false);
    }


}
