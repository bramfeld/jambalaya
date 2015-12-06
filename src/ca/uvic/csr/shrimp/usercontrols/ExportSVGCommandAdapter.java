/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.BrowserLauncher;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;

/**
 * Saves the current view to an SVG file.
 *
 * @author Yiling Lu
 */
public class ExportSVGCommandAdapter extends DefaultProjectAction {

	public static String ACTION_NAME = "Export to HTML/SVG Snapshot";

	private Frame parentFrame;
	private String toolName;
	private ShrimpSnapShot snapShot;
	private JFileChooser chooser;

	private String htmlFileName;
	private String svgFileName;

	public ExportSVGCommandAdapter(ShrimpProject project, String toolName) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_export.gif"), project);
		this.toolName = toolName;
		mustStartAndStop = false;
	}

	public void actionPerformed(ActionEvent e) {
		parentFrame = ApplicationAccessor.getParentFrame();
		ViewTool view = getViewTool(toolName);
		if (view != null) {
			String comment = ShrimpSnapShot.askForComments();
			if (comment != null) {
				snapShot = new ShrimpSnapShot(getProject(), view, comment);
				save();
			}
		}
	}

	public void save() {
		JFileChooser chooser = getFileChooser();
		int state = chooser.showSaveDialog(parentFrame);
		if (state == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				htmlFileName = file.getCanonicalPath();
			} catch (Exception e) {
				return;
			}
			String lc = htmlFileName.toLowerCase();
			if (!lc.endsWith(".htm") && !lc.endsWith(".html")) {
				htmlFileName += ".html";
			}

			int dotPos = htmlFileName.lastIndexOf(".");
			svgFileName = htmlFileName.substring(0, dotPos);

			if (!svgFileName.endsWith(".svg")) {
				svgFileName += ".svg";
			}
			snapShot.saveDOMs(svgFileName, htmlFileName);
			promptForOpening();
		}
	}

	private JFileChooser getFileChooser() {
		if (chooser == null) {
			chooser = new JFileChooser();
			ShrimpFileFilter filter = new ShrimpFileFilter();
			filter.addExtension("htm");
			filter.addExtension("html");
			filter.setDescription("HTML File");
			chooser.setFileFilter(filter);
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle("Export HTML/SVG Snapshot");
			chooser.setApproveButtonText("Save");
			chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		}
		return chooser;
	}

	public void startAction() {
		actionPerformed(new ActionEvent("", 0, ""));
	}

	private void promptForOpening() {
		String msg = " A HTML/SVG snapshot has been successfully saved.\n\n Would you like to view this snapshot now?";
		int n = JOptionPane.showConfirmDialog(parentFrame, msg, htmlFileName, JOptionPane.YES_NO_OPTION);

		if (n == JOptionPane.YES_OPTION) {
			try {
				BrowserLauncher.openURL(htmlFileName);
			} catch (IOException e) {
				String message = "Sorry, there was a problem automatically displaying a page in your web browser.\n" +
					"Please manually direct your browser to " + htmlFileName + "\n\n" + e.getMessage();
				JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message,
						ApplicationAccessor.getAppName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
