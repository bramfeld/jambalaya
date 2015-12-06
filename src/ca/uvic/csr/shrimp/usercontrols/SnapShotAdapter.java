/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.event.ActionListener;
import java.io.Serializable;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.adapter.tools.OpenFilmStripAdapter;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.FilmStrip.FilmStrip;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Take a snapshot
 *
 * @author Casey Best
 * @date Oct 22, 2001
 */
public class SnapShotAdapter extends DefaultProjectAction implements ActionListener, Serializable {

	public static String ACTION_NAME = "Take a Snapshot";
	public static String TOOLTIP = "Takes a snapshot of the current view.\nSnapshots can be saved and loaded. \nSee the Filmstrip tool for a view of your snapshots.";

	private String toolName;

    public SnapShotAdapter(ShrimpProject project, String toolName) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_snapshot.gif"), project);
		setToolTip(TOOLTIP);
    	this.toolName = toolName;
		mustStartAndStop = false;
    }

	/**
	 * Takes snapshot
	 */
    public void startAction() {
		takeSnapShot(true);
	}

    public void setProject(ShrimpProject project) {
    	super.setProject(project);
    	setEnabled(project != null);
    }

	public void takeSnapShot(boolean askForComments) {
		if (getProject() == null) {
			return;
		}

		ShrimpApplication app = ApplicationAccessor.getApplication();
		try {
			// see if filmstrip has been created yet
			app.getTool(ShrimpApplication.FILMSTRIP);
		} catch (ShrimpToolNotFoundException stnfe) {
			OpenFilmStripAdapter action = new OpenFilmStripAdapter();
			action.setProject(getProject());
			action.startAction();
		}

		String spdNote = ProgressDialog.getNote();
		String spdSubtitle = ProgressDialog.getSubtitle();
	    app.waitCursor();
		try {
			ProgressDialog.showProgress();
			ProgressDialog.setSubtitle("Creating Snapshot...");
			ProgressDialog.setNote("");
			FilmStrip filmStrip = (FilmStrip) app.getTool(ShrimpApplication.FILMSTRIP);
			ViewTool view = getViewTool(toolName);
			if (view != null) {
				String comment = null;
				boolean add = true;
				if (askForComments) {
					comment = ShrimpSnapShot.askForComments();
					if (comment == null) {
						add = false;
					}
				}
				if (add) {
					ShrimpSnapShot snapShot = new ShrimpSnapShot(getProject(), view, comment);
					filmStrip.addSnapShot(snapShot);
				}
			}
		} catch (ShrimpToolNotFoundException e) {
			e.printStackTrace();
		} finally {
		    app.defaultCursor();
			ProgressDialog.setSubtitle(spdSubtitle);
			ProgressDialog.setNote(spdNote);
		    ProgressDialog.tryHideProgress();
		}
	}
}

