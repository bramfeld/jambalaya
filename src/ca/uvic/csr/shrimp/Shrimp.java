/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication.StandAloneApplication;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Optional Command Line Parameter:
 *	<i>filename</i> Opens this file immediately as program starts up.
 */
public class Shrimp {

    /**
     * "The" main method to launch stand-alone shrimp
     * @param args Optional Command Line Parameter: <i>filename</i> Opens this file immediately as program starts up.
     */
    public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Starting Shrimp...");
		System.out.println("java.version=" + System.getProperty("java.version"));
		//System.out.println("java.vm.version=" + System.getProperty("java.vm.version"));
		System.out.println("os.name=" + System.getProperty("os.name"));
		//System.out.println("os.arch=" + System.getProperty("os.arch"));
        //System.out.println("os.version=" + System.getProperty("os.version"));
        //System.out.println("java.io.tmpdir=" + System.getProperty("java.io.tmpdir"));

		final StandAloneApplication application = new StandAloneApplication();
        JFrame frame = application.createParentFrame();
        application.initialize(frame);
        frame.setVisible(true);
		if (args.length > 0) {
		    String fileName = args[0];
		    final URI uri = ResourceHandler.getFileURI(fileName);
		    if (uri != null) {
				SwingUtilities.invokeLater(new Runnable () {
					public void run() {
						application.openProject(uri);
					}
				});
		    } else {
		        System.err.println("File '" + fileName + "' does not exist.");
		    }
		}

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent evt) {
         		// Exit without an error
				System.exit(0);
			}
		});
    }
}