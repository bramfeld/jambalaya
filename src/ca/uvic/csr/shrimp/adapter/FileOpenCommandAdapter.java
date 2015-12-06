/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JFileChooser;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserAction;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpFileView;

/**
 * @author Rob Lintern
 */
public class FileOpenCommandAdapter extends DefaultUserAction {

	private static final String ACTION_NAME = "Open...";

	private List fileFilters;
	private Map extsToIconsMap;
	private File lastDirectory = null;

	public FileOpenCommandAdapter() {
		this(new ArrayList(0), new HashMap(0));
	}

	public FileOpenCommandAdapter(List fileFilters, Map extsToIconsMap) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_open.gif"));
		this.fileFilters = fileFilters;
		this.extsToIconsMap = extsToIconsMap;
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public void startAction() {
		// Set the icons
		ShrimpFileView fileView = new ShrimpFileView();
		for (Iterator iter = extsToIconsMap.keySet().iterator(); iter.hasNext();) {
			String ext = (String) iter.next();
			Icon icon = (Icon) extsToIconsMap.get(ext);
			fileView.putIcon(ext, icon);
		}

		// Create and display a FileChooser
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Select A Data File");
		File dir = (lastDirectory == null ? new File(System.getProperty("user.dir")) : lastDirectory);
		chooser.setCurrentDirectory(dir);
		chooser.setAcceptAllFileFilterUsed(true);

		for (Iterator iter = fileFilters.iterator(); iter.hasNext();) {
			ShrimpFileFilter fileFilter = (ShrimpFileFilter) iter.next();
			chooser.addChoosableFileFilter(fileFilter);
		}
		chooser.setFileView(fileView);

		int state = chooser.showOpenDialog(ApplicationAccessor.getParentFrame());
		File file = chooser.getSelectedFile();
		if (state == JFileChooser.APPROVE_OPTION) {
			ShrimpApplication app = ApplicationAccessor.getApplication();
			((StandAloneApplication) app).openProject(file.toURI());
			lastDirectory = file.getParentFile();
		}
	}

}
