/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.FilmStrip.ShrimpSnapShot;
import ca.uvic.csr.shrimp.gui.FilmStrip.SnapShot;

/**
 * @author Jeff Michaud
 */
public class LoadPreviousViewDialog {

	private ShrimpProject project;
	private File snapShotFile;

	private ShrimpSnapShot snapShot;

	private boolean loadPreviousView;
	private boolean showLoadPreviousDialog;

	public LoadPreviousViewDialog(ShrimpProject project, File snapShotFile) {
		this.project = project;
		this.snapShotFile = snapShotFile;
	}

	public void showDialog() {
		loadPreviousView = false;
		snapShot = null;
		if (snapShotFile != null && snapShotFile.exists()) {
			//Get the properties
			Properties props = ApplicationAccessor.getProperties();
			String showLoadPreviousDialogStr = props.getProperty(ShrimpApplication.SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY, "true");
			showLoadPreviousDialog = "true".equalsIgnoreCase(showLoadPreviousDialogStr);

			if (showLoadPreviousDialog) {
				final Object[] options = { "Yes", "No" };
				final JOptionPane pane = new JOptionPane("Return to previous view?",
						JOptionPane.QUESTION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[1]);
		        final JDialog dialog = pane.createDialog(ApplicationAccessor.getParentFrame(), "Return to Previous View");

				//add checkbox to dialog
				JCheckBox checkBox = new JCheckBox("Remember my answer and do not ask me again.");
				checkBox.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 0));
				dialog.getContentPane().add(checkBox, BorderLayout.SOUTH);

				// add preview image to dialog
				JLabel previewLabel = new JLabel();
				previewLabel.setSize(new Dimension(SnapShot.IMAGE_WIDTH, SnapShot.IMAGE_HEIGHT));
				previewLabel.setMinimumSize(new Dimension(SnapShot.IMAGE_WIDTH, SnapShot.IMAGE_HEIGHT));
				Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5),
												BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
				previewLabel.setBorder(border);

				snapShot = new ShrimpSnapShot(project, snapShotFile, previewLabel);
				Image previewImage = snapShot.getPreviewShot();
				if (previewImage != null) {
					ImageIcon previewIcon = new ImageIcon(previewImage);
					previewLabel.setIcon(previewIcon);
				}
				previewLabel.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
							pane.setValue(options[0]);
							dialog.dispose();
						}
					}
				});
				dialog.getContentPane().add(previewLabel, BorderLayout.EAST);

				dialog.pack();
				dialog.setVisible(true);

	        	Object selectedValue = pane.getValue();
				loadPreviousView = (selectedValue == options[0]);

				props.setProperty(ShrimpApplication.SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY, "" + !checkBox.isSelected());
			}
		}
	}

	public boolean getLoadPreviousView() {
		return loadPreviousView;
	}

	public ShrimpSnapShot getSnapShot() {
		return snapShot;
	}

}
