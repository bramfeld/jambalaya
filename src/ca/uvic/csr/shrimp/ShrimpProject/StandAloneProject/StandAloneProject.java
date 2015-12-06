/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject;

import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.OBODataBean;
import ca.uvic.csr.shrimp.DataBean.SimpleDataBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.GXLPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.OBOPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PersistentStorageBeanListener;
import ca.uvic.csr.shrimp.PersistentStorageBean.XMIPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.XMLPersistentStorageBean;
import ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean.PRJPersistentStorageBean;
import ca.uvic.csr.shrimp.SearchBean.ArtifactSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.GrepSearchStrategy;
import ca.uvic.csr.shrimp.SearchBean.SearchBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.CodeURLRequestAdapter;
import ca.uvic.csr.shrimp.adapter.PRJPersistentStorageBeanAdapter;
import ca.uvic.csr.shrimp.adapter.URLRequestAdapter;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * StandAloneProject represents a single project in StandAloneApplication.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class StandAloneProject extends AbstractShrimpProject {

	// The directory where the source is - used by grep search
	protected File sourceDir;
	protected File codeDir;

	public StandAloneProject(StandAloneApplication application)  {
		super(application, PROJECT_TYPE_UNKNOWN);
	}

	/**
	 * Creates a StandAloneProject.
	 *
	 * @param application Reference to the StandAloneApplication that created this project.
	 * @param projectURI A URI that points to the data for this project.
	 */
	public StandAloneProject(StandAloneApplication application, URI projectURI) {
		super(application, determineProjectType(projectURI));
		this.projectURI = projectURI;
		setupAttrVisVarBeanForJavaDomain();

		String projectURIString = projectURI.toString();
		String projectName = new String(projectURIString);
		int index = projectURIString.lastIndexOf('/');
		if (index != -1) {
		    projectName = projectName.substring(index + 1);
		}
		// filename where the properties of this project will be stored
		propertiesFileName = projectURI.getScheme().equals("file") ? projectURI.getPath() : projectName;
		int dot = propertiesFileName.lastIndexOf('.');
		if (dot != -1) {
			propertiesFileName = propertiesFileName.substring(0, dot);
		}
		propertiesFileName += "_" + application.getName().replace(' ', '_') + ".properties";

		// load properties
		loadProperties();

		// set the title of this project
		this.title = application.checkForDuplicateTitle(projectName);

		initDataBean(projectURI, projectURIString);
	}

	/**
	 * Creates a StandAloneProject.
	 * @param application Reference to the StandAloneApplication that created this project.
	 */
	public StandAloneProject(StandAloneApplication application, String projectTitle, PersistentStorageBean psb, DataBean dataBean) {
		super(application, PROJECT_TYPE_UNKNOWN);
		setupAttrVisVarBeanForJavaDomain();
		this.title = application.checkForDuplicateTitle(projectTitle);
		addBean(ShrimpProject.PERSISTENT_STORAGE_BEAN, psb);
		addBean(ShrimpProject.DATA_BEAN, dataBean);
	}

	/**
	 * Initializes the data bean based on the project type.
	 * @param projectURI
	 * @param projectURIString
	 */
	private void initDataBean(URI projectURI, String projectURIString) {
		// load data into the databean
		if (projectType == PROJECT_TYPE_PROTEGE) {
			ProtegeDataBean dataBean = null;
			try {
		    	dataBean = new ProtegeDataBean(projectURI);
				dataBean.setDefaultRootClses();
				addBean(ShrimpProject.DATA_BEAN, dataBean);
			} catch (NoClassDefFoundError e) {
				// Probably no OWL jars found
				String msg = "Couldn't load the Protege Project.\n" +
						"Shrimp can only load Protege projects if the Protege jar files are included on the classpath.\n" +
						"If you are trying to open an OWL project then the OWL jar files are required as well.";
				JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
								msg, "Error", JOptionPane.ERROR_MESSAGE);
				// throw this exception to stop the rest of the load process
				throw new RuntimeException(ApplicationAccessor.getAppName() + ": couldn't load Protege project", e);
			}
		} else if (projectType != PROJECT_TYPE_UNKNOWN) {
			SimpleDataBean dataBean = new SimpleDataBean();
			PersistentStorageBean psb;
			if (projectType == PROJECT_TYPE_XML) {
			    psb = new XMLPersistentStorageBean();
			} else if (projectType == PROJECT_TYPE_XMI){
			    psb = new XMIPersistentStorageBean();
			} else if (projectType == PROJECT_TYPE_GXL){
			    psb = new GXLPersistentStorageBean();
			} else if (projectType == PROJECT_TYPE_OBO) {
				// @tag Shrimp.OBO : use a OBOPersistentStorageBean and OBODataBean
				psb = new OBOPersistentStorageBean();
				dataBean = new OBODataBean();
			} else {
			    psb = new PRJPersistentStorageBean();
			}
			addBean(ShrimpProject.PERSISTENT_STORAGE_BEAN, psb);
			addBean(ShrimpProject.DATA_BEAN, dataBean);
			PersistentStorageBeanListener psbListener = new PRJPersistentStorageBeanAdapter(dataBean);
			psb.addPersistentStorageBeanListener(psbListener);
			psb.loadData(projectURI);
			psb.removePersistentStorageBeanListener(psbListener);
			
			// we can't do this here - the ShrimpView hasn't been created yet!
			//updateSearchStrategies();
		} else {
			System.err.println("Unknown project type");
		}
	}

	public void createDefaultQuickViewActions() {
		super.createDefaultQuickViewActions();

		// TODO support more kinds of quick views
		if (PROJECT_TYPE_PROTEGE.equals(projectType)) {
			createDefaultProtegeQuickViewActions();
		} else if (PROJECT_TYPE_OBO.equals(projectType)) {
			createDefaultOBOQuickViewActions();
		} else {
			createDefaultJavaQuickViewActions();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpProject#createProjectActions()
	 */
	public void createProjectActions() {
		super.createProjectActions();

		// load or create the quick views
		createQuickViewActions();
	}


	/**
	 * Creates and adds a grep search to the search bean
	 * Also, sets code directory of artifact search
	 */
	public void updateSearchStrategies() {
		if (!getProjectType().equals(PROJECT_TYPE_PRJ)) {
			return;
		}

		try {
			// get the source and code directories
			PRJPersistentStorageBean psb = (PRJPersistentStorageBean) getBean(ShrimpProject.PERSISTENT_STORAGE_BEAN);
			SearchBean searchBean = (SearchBean) getBean(SEARCH_BEAN);
			// create a grep search
			URI codeDirectory = psb.getCodeDirectory();
			URI sourceDirectory = psb.getSourceDirectory();
			if (sourceDirectory != null) { // cant do grep search without a source directory
				GrepSearchStrategy grepSearchStrategy = new GrepSearchStrategy(sourceDirectory, codeDirectory);
		    	try {
					ViewTool tool = (ViewTool) getTool(ShrimpProject.SHRIMP_VIEW);
					URLRequestAdapter urlRequestAdapter = new CodeURLRequestAdapter(tool);
					grepSearchStrategy.addBrowseActionListener(urlRequestAdapter);
				} catch (ShrimpToolNotFoundException e) {
					e.printStackTrace();
				}
				searchBean.addStrategy(grepSearchStrategy);
			}
			//update directory of artifact search strategy
			ArtifactSearchStrategy artifactSearchStrategy = (ArtifactSearchStrategy) searchBean.getStrategy(ArtifactSearchStrategy.NAME);
			if (artifactSearchStrategy != null && codeDirectory != null) {
				artifactSearchStrategy.setPresentDir(codeDirectory);
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}


    /**
     * Sets this project to be the active project.
     */
    public void setActive() {
    	((StandAloneApplication)application).setActiveProject(this);
    }

	public void save() {
		System.out.println("do what is neccessary to save here");
	}


}