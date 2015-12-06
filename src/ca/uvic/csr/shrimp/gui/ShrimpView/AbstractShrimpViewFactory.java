/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.Container;
import java.io.File;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectAdapter;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.gui.LoadPreviousViewDialog;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.gui.quickview.DefaultViewAction;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewManager;
import ca.uvic.csr.shrimp.usercontrols.UserAction;

/**
 * This abstract class provides some common code for create and disposing of a Shrimp View.
 *
 * @author Rob Lintern, Chris Callendar
 */
public abstract class AbstractShrimpViewFactory implements ShrimpViewFactory {

	protected Container shrimpViewContainer;
	protected ShrimpView shrimpView;
	protected ShrimpProject project;

	/**
	 * @see ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewFactory#createShrimpView(ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject, java.awt.Container)
	 */
	public ShrimpView createShrimpView(final ShrimpProject project, Container shrimpViewContainer) {
		return createShrimpView(project, shrimpViewContainer, ShrimpViewConfiguration.ALL_ON);
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewFactory#createShrimpView(ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject, java.awt.Container)
	 */
	public ShrimpView createShrimpView(final ShrimpProject project, Container shrimpViewContainer,
			ShrimpViewConfiguration config) {
		this.project = project;
		this.shrimpViewContainer = shrimpViewContainer;

		if (config == null) {
			config = ShrimpViewConfiguration.ALL_ON; // default to turn on all options
		}
		//create a shrimpView object, initialize the beans, then call init
		shrimpView = new ShrimpView(project, config);
		shrimpView.createShrimpViewBeansForProject();
		shrimpView.init();
		shrimpView.initShrimpViewBeansForProject();

		projectSpecificCreateShrimpView(config);
		project.addProjectListener(new ShrimpProjectAdapter() {
			public void projectClosing(ShrimpProjectEvent event) {
				// must be done before the shrimp view is disposed
				takeClosingSnapShot(event.getProject());
				disposeShrimpView();
				project.removeProjectListener(this);
			}
		});

		boolean useDefault = true;
		if (config.tryLastView) {
			String fileName =  ShrimpSnapShot.getSnapShotName(project);
			try {
				File snapShotFile = new File(fileName);
	            if (snapShotFile.exists()) {
	            	LoadPreviousViewDialog loadPreviousViewDialog = new LoadPreviousViewDialog(project, snapShotFile);
	            	loadPreviousViewDialog.showDialog();
	            	if (loadPreviousViewDialog.getLoadPreviousView()) {
	            		startAtSnapShotState(loadPreviousViewDialog.getSnapShot());
	            		useDefault = false;
	            	}
	            }
	        } catch (Throwable e) {
	        }
        }
		if (useDefault) {
			// invoke later to make sure Shrimp properly sizes the root node to fit the window
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					startAtDefaultState();
				}
			});
		}
		return shrimpView;
	}

	/**
	 * Takes a closing snapshot.
	 */
	protected void takeClosingSnapShot(ShrimpProject project) {
		ShrimpSnapShot.takeSnapShot(project, shrimpView);
	}

	/**
	 * Defines the default actions to take on first loading the ShrimpView.
	 * Can be overriden if other actions are useful in different projects.
	 */
	protected void startAtDefaultState() {
	    QuickViewManager mgr = project.getQuickViewManager();
		UserAction defaultViewAction = mgr.getQuickViewAction(DefaultViewAction.ACTION_NAME);
	    if (defaultViewAction == null) {
	    	defaultViewAction = mgr.createDefaultView(project);
	    }
        defaultViewAction.startAction();
	}

	private void startAtSnapShotState(final ShrimpSnapShot snapShot)  {
		snapShot.revertViewToSnapShotState(project, shrimpView);
	}

	/**
	 * Disposes of the Shrimp View and removes it from its container.
	 */
	protected void disposeShrimpView() {
		projectSpecificDisposeShrimpView();
		//shrimpView.disposeTool();	// already done when project closing event is fired
		shrimpViewContainer.removeAll();
		shrimpViewContainer = null;
	}

	/**
	 * Implement this method to provide any domain or project specific initialization of the Shrimp View.
	 * @param config
	 */
	protected abstract void projectSpecificCreateShrimpView(ShrimpViewConfiguration config);

	/**
	 * Implement this method to provide any domain or project specific "finalization" of the Shrimp View.
	 */
	protected abstract void projectSpecificDisposeShrimpView();


}