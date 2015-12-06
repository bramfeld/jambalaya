/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JFrame;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DataBean.OBODataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.OBOPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.adapter.PRJPersistentStorageBeanAdapter;


/**
 *
 *
 * @author Chris Callendar
 * @date 5-Dec-06
 */
public class OBOViewer {

	private QueryView queryView;
	private HashMap sessionsMap;
	private StandAloneApplication app;
	private boolean showQueryPanel;

	public OBOViewer(boolean showQueryPanel) {
		this.showQueryPanel = showQueryPanel;
		this.sessionsMap = new HashMap();
        this.app = new StandAloneApplication();
        app.initialize();

        queryView = new QueryView(null, showQueryPanel);
        queryView.setLayoutMode(LayoutConstants.LAYOUT_SUGIYAMA);
	}

	/**
	 * Loads an {@link OBOSession} and caches it.
	 * @param oboSession
	 */
	public void loadOBOSession(OBOSession oboSession) {
        OBOPersistentStorageBean psb = new OBOPersistentStorageBean();
        OBODataBean dataBean = new OBODataBean();

		PersistentStorageBeanListener psbListener = new PRJPersistentStorageBeanAdapter(dataBean);
		psb.addPersistentStorageBeanListener(psbListener);
        psb.loadData(oboSession);
		psb.removePersistentStorageBeanListener(psbListener);

        ApplicationAccessor.setApplication(app);
        StandAloneProject project = new StandAloneProject(app, "Shrimp OBO Viewer", psb, dataBean);
        app.addProject(project);
        app.setActiveProject(project);

		sessionsMap.put(oboSession, project);
	}

	/**
	 * Executes a query for the given {@link OBOClass}.
	 * @param session
	 * @param cls
	 */
	public void query(OBOSession session, OBOClass cls) {
		query(session, cls, false);
	}

	public void query(OBOSession session, OBOClass cls, boolean animate) {
		query(session, cls.getName(), animate);
	}

	public void query(OBOSession session, final String termName, final boolean animate) {
		if (!sessionsMap.containsKey(session)) {
			loadOBOSession(session);	// lazy load
		}
		StandAloneProject project = (StandAloneProject) sessionsMap.get(session);

		// first time - set up the query view
		boolean firstTime = (queryView.getProject() == null);
		queryView.setProject(project);

        if (firstTime && showQueryPanel) {
        	queryView.setStringMatchingMode(QueryHelper.STRING_MATCH_EXACT_MODE);
        	/*
        	queryView.getQueryPanel().getSearchPanel().setVisible(false);
        	queryView.getQueryPanel().getNodesPanel().setVisible(false);
        	queryView.getQueryPanel().setUpdateGraphWhenLevelsChange(true);
        	*/
        	//queryView.getQueryPanel().getIncomingLevelsLabel().setText("Incoming ");
        	//queryView.getQueryPanel().getOutoingLevelsLabel().setText("Outgoing ");
        }
        //queryView.setToolBarShown(false);

		queryView.query(termName, animate);
	}

	public Component getView() {
		return queryView.getGUI();
	}

	public QueryView getQueryView() {
		return queryView;
	}

	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame("OBO Test");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			final OBOViewer viewer = new OBOViewer(true);
			frame.getContentPane().add(viewer.getView());

			final OBOSession session = OBOPersistentStorageBean.loadOBOFile(StandAloneApplication.DEMO_OBO_LOCAL.toString());
			//final OBOSession session = OBOPersistentStorageBean.loadOBOFile("demo/obo/fly_anatomy.obo");
			viewer.loadOBOSession(session);

			frame.pack();
			frame.setSize(600, 600);
			frame.setLocation(400, 200);
			frame.setVisible(true);

			viewer.query(session, "Lysosome", false);
		} catch (RuntimeException e1) {
			e1.printStackTrace();
		}
	}

}
