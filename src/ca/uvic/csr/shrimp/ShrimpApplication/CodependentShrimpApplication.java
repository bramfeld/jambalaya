/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import ca.uvic.csr.shrimp.ScriptingBean.DefaultMainViewScriptingBean;
import ca.uvic.csr.shrimp.ScriptingBean.ScriptingBean;
import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ScrollDesktopPane;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewConfiguration;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * An application that can be run as part of another Java application or,
 * using JNI, a non-java app. Refer to the main() method for an example of
 * how to use this. See also {@link ShrimpViewConfiguration}
 * @author Chris Bennett
 */
public class CodependentShrimpApplication extends StandAloneApplication {

	private ShrimpViewConfiguration config = ShrimpViewConfiguration.ALL_ON; // default to all options enabled

	public CodependentShrimpApplication(ShrimpViewConfiguration config) {
		super();
		this.config = config;
		// Could allow flexible branding, here by passing these values in the config
		// or as parameters
		brandingImage = ResourceHandler.getResourceImage("shrimplogo_bevel.png");
		brandingIcon = ResourceHandler.getIcon("icon_shrimp.gif");
	}

	public void openProject(String fileName) {
	    final URI uri = ResourceHandler.getFileURI(fileName);
	    if (uri != null) {
			SwingUtilities.invokeLater(new Runnable () {
				public void run() {
					openProject(uri, config);
				}
			});
	    } else {
	        System.err.println("File '" + fileName + "' does not exist.");
	    }
	}

    /**
     * Call this method initialize Shrimp and add the UI components to the frame content pane.
     * @param frame
     */
    public void initialize(JFrame frame) {
    	this.parentFrame = frame;
    	initialize(config.showMessagePanel);

    	if (config.showMenuBar) {
    		frame.setJMenuBar(menuBar);
    	}

		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(desktop, BorderLayout.CENTER);

    	if (config.showMessagePanel) {
    		frame.getContentPane().add(messagePanel, BorderLayout.SOUTH);
    	}

		// send an application started event
		fireApplicationStartedEvent();
    }

    /**
     * Call this method if you don't want to associate any {@link Frame} or {@link JApplet}
     * with Shrimp.
     * @param showMessagePanel
     */
    public void initialize( boolean showMessagePanel) {
    	this.projectsToFrames = new Hashtable();

    	if (showMessagePanel) {
			// initialize the message panel
			messagePanel = new JPanel();
			messagePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			messagePanel.setLayout(new BorderLayout());
			setMessagePanelMessage(" For help click on Help >> Manual");
    	}

		// initialize the custom desktop
		String version = getBuildInfo().toShortString();
		desktop = new ScrollDesktopPane(version, brandingImage, brandingIcon);
		//desktop.setBackground(ShrimpColorConstants.SHRIMP_BACKGROUND);

    	// create a progress dialog
		ProgressDialog.createProgressDialog(parentFrame, getName() + " Progress ...");

		// create the menus for this application
		createApplicationActions();   		// @tag Shrimp.createApplicationActions
    }



	/**
	 * Example usage
	 */
	public static void main(String[] args) {

		// Turn off everything we can
		ShrimpViewConfiguration config = ShrimpViewConfiguration.ALL_OFF;

		// But, turn this back on to allow mouse manipulation...
		config.enableUserControls = true;

		final CodependentShrimpApplication application = new CodependentShrimpApplication(config);

		// Either use the default application frame...
//        JFrame frame = application.createParentFrame();

		// Or create your own: e.g.,
		JFrame frame = new JFrame();
		frame.setSize(1024, 768);
		//frame.setPreferredSize(new Dimension(1024, 768));	// @tag Shrimp.Java5.setPreferredSize

		// Initialize the application with the specified frame and menu bar/msg panel display options
        application.initialize(frame);
        frame.setVisible(true);

        // Open a project file (modify this to point to a valid GXL file)
        // This could also be passed in, of course...
        String fileName = "C:/Documents and Settings/Chris Bennett/Desktop/DND/Graphs/March12007demo.gxl";
	    application.openProject(fileName);

	    // Wait a bit so that the application and project can load
	    try {Thread.sleep(5000);} catch (InterruptedException e) {}

	    // Get a scripting bean so that you can manipulate and control the current project
	    StandAloneProject project = application.getActiveProject();

 		try { // Get and use the scripting bean
			ScriptingBean sb = (ScriptingBean) project.getBean(ShrimpProject.SCRIPTING_BEAN);
			DefaultMainViewScriptingBean svsb = sb.getShrimpViewScriptingBean();

			// Use the scripting bean here...
			svsb.selectNodesByName(".str", true, false, false, false);
		    svsb.groupSelectedNodes();
		    // etc...
		} catch (BeanNotFoundException e1) {
			e1.printStackTrace();
		}

	    // Make sure not to leave the process running after the window is closed
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent evt) {
				application.close();
				System.exit(0);
			}
		});
	}
}
