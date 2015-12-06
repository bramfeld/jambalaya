/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Displays a dialog window outlining quick ways to get up and running with Shrimp.
 *
 * @author Neil Ernst, Chris Callendar
 */
public class QuickStartComponent extends JPanel {

	private JEditorPane editor;
	private JCheckBox dontShowQuickStartCheckBox;
	private JButton dismissButton;

	/**
	 * @param fileURL the URL of the quick start help page
	 */
	public QuickStartComponent(final URL fileURL) {
		super(new BorderLayout());
		initialize();
		loadProperties();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				loadURL(fileURL);
			}
		});
	}

	public QuickStartComponent(String url) {
		this(createURL(url));
	}

	private static URL createURL(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void initialize() {
		JScrollPane sp = new JScrollPane(getEditorPane());
		sp.setPreferredSize(new Dimension(600, 565));
		sp.setWheelScrollingEnabled(true);
		add(sp, BorderLayout.CENTER);

		JPanel pnlButtons = new TransparentPanel(new BorderLayout());
		pnlButtons.add(getDontShowQuickStartCheckBox(), BorderLayout.WEST);
		pnlButtons.add(getDismissButton(), BorderLayout.EAST);
		add(pnlButtons, BorderLayout.SOUTH);
	}

	private void loadURL(URL fileURL) {
		try {
			if (fileURL != null) {
				getEditorPane().setPage(fileURL);
			} else {
				throw new NullPointerException("Invalid URL");
			}
		} catch (Exception e) {
			getEditorPane().setText("Couldn't load HTML file " + fileURL + ".\n" + e.getMessage());
		}
	}

	public JButton getDismissButton() {
		if (dismissButton == null) {
			dismissButton = new JButton("Dismiss");
		}
		return dismissButton;
	}

	/**
	 * Loads the content in off the web
	 */
    public JEditorPane getEditorPane() {
    	if (editor == null) {
    		editor = new JEditorPane();
    		editor.setEditable(false);
    		editor.setPreferredSize(new Dimension(480, 565));
    		editor.setText("Loading...");
    	}
		return editor;
    }

    public JCheckBox getDontShowQuickStartCheckBox() {
    	if (dontShowQuickStartCheckBox == null) {
    		dontShowQuickStartCheckBox = new JCheckBox ("Do not show this again.");
    		dontShowQuickStartCheckBox.setPreferredSize(new Dimension(200, 35));
    		dontShowQuickStartCheckBox.setOpaque(false);
    		dontShowQuickStartCheckBox.addItemListener(new ItemListener() {
    			public void itemStateChanged(ItemEvent e) {
    				saveProperties();
    			}
    		});
    	}
    	return dontShowQuickStartCheckBox;
    }

    protected void loadProperties() {
    	if (ApplicationAccessor.isApplicationSet()) {
			Properties props = ApplicationAccessor.getProperties();
			String val = props.getProperty(ShrimpApplication.SHOW_QUICKSTART_DIALOG_KEY, "true");
			boolean dontShow = !"true".equals(val);
			getDontShowQuickStartCheckBox().setSelected(dontShow);
    	}
    }

    protected void saveProperties() {
    	if (ApplicationAccessor.isApplicationSet()) {
			Properties props = ApplicationAccessor.getProperties();
			boolean dontShow = getDontShowQuickStartCheckBox().isSelected();
			String val = "" + !dontShow;
			props.setProperty(ShrimpApplication.SHOW_QUICKSTART_DIALOG_KEY, val);
    	}
    }

	/**
	 * Shows the quick start component in a model dialog.
	 * It doesn't check the app preferences, but does update them after the
	 * dialog is closed.
	 */
	public static void showDialog(URL fileURL) {
		Frame frame = ApplicationAccessor.getParentFrame();
		String appName = ApplicationAccessor.getAppName();
		final EscapeDialog dialog = new EscapeDialog(frame, "Getting Started in " + appName);
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().setLayout(new BorderLayout());

		final QuickStartComponent qsc = new QuickStartComponent(fileURL);
		dialog.getContentPane().add(qsc, BorderLayout.CENTER);

		qsc.getDismissButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dialog.dispose();
			}
		});

		// put dialog in center of screen
		dialog.setPreferredSize(new Dimension(400, 600));
		ShrimpUtils.centerOnScreen(dialog);
		dialog.pack();
		dialog.getRootPane().setDefaultButton(qsc.getDismissButton());

		dialog.setVisible(true);	// blocks here
	}

	public static void main(String[] args) throws MalformedURLException {
		QuickStartComponent.showDialog(new URL("http://www.google.ca"));
	}
}
