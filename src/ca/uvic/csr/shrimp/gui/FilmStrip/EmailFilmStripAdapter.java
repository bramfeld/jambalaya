/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.Frame;
import java.io.File;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.util.EmailUtil;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Sends the current filmstrip as an email.
 *
 * @see ca.uvic.csr.shrimp.util.EmailUtil
 * @author Chris
 * @date 30-Nov-07
 */
public class EmailFilmStripAdapter extends DefaultProjectAction {

	// this class is included in the mail.jar file
	private static final String MAIL_CLASS = "javax.mail.Session";

	// this class is included in the activation.jar file, and now also in the Java 6 JRE
	private static final String ACTIVATION_CLASS = "javax.activation.DataSource";

	private FilmStrip filmstrip;
	private String lastEmail;
	
	public EmailFilmStripAdapter(String actionName, Icon icon, ShrimpProject project, FilmStrip filmstrip) {
		super(actionName, icon, project);
		this.filmstrip = filmstrip;
		this.lastEmail = ShrimpSnapShot.getEmailAddressFromProperties(project);
	}

	public void startAction() {
		int count = filmstrip.getSnapShotPanel().getSnapShotCount();
		if (count > 0) {
			final String appName = ApplicationAccessor.getAppName();
			final String title = (getProject() != null ? getProject().getTitle() : "");
			// Save the filmstrip to a temporary directory, then email
			try {
				File tempDir = ShrimpUtils.createTempDir("ShrimpFilmstrip", true);
				File file = new File(tempDir, appName + (title.length() > 0 ? "_" + title : "") + ".filmstrip");
				final String filename = file.getAbsolutePath();
				filmstrip.save(filename);
				final String toEmail = promptForToEmailAddress();
				if (toEmail != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							String body = appName + " filmstrip\nProject: " + title + "\nDate: " + (new Date()).toString();
							try {
								EmailUtil.sendChiselAttachmentEmail(toEmail, filename, body, appName + " filmstrip");
							} catch (Exception e) {
								JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
									"Error emailing the filmstrip: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
					"Error emailing the filmstrip: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), "There are no snapshots to save.",
				"No SnapShots", JOptionPane.WARNING_MESSAGE);
		}
	}

	private String promptForToEmailAddress() {
		Frame frame = ApplicationAccessor.getParentFrame();
		String to = (String) JOptionPane.showInputDialog(frame, "Please enter your email address: ",
												"Email", JOptionPane.QUESTION_MESSAGE, null, null, lastEmail);
		if (to != null) {
			lastEmail = to;
			if ((to.length() > 0) && (to.indexOf("@") == -1)) {
				JOptionPane.showMessageDialog(frame, "Invalid email address", "Error", JOptionPane.ERROR_MESSAGE);
				promptForToEmailAddress();
			} else if (to.length() > 0) {
				ShrimpSnapShot.saveEmailAddressToProperties(getProject(), to);
			}
		}
		return to;
	}

	public static boolean canEmail() {
		return hasMailJar();
	}

	public static boolean hasMailJar() {
		try {
			Class cls = Class.forName(MAIL_CLASS);
			return (cls != null);
		} catch (Throwable t) {
			return false;
		}
	}

	public static boolean hasActivationJar() {
		try {
			Class cls = Class.forName(ACTIVATION_CLASS);
			return (cls != null);
		} catch (Throwable t) {
			return false;
		}
	}
}
