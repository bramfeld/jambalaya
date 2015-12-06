/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;

/**
 * An action that opens the given url in the system web browser.
 *
 * @author Rob Lintern
 */
public class ShowInBrowserAction extends DefaultShrimpAction {

	private String url;

    public ShowInBrowserAction(String title, String url, Icon icon) {
        super(title, icon);
        this.url = url;
    }

    public void setUrl(String url) {
    	this.url = url;
    }

    public String getUrl() {
    	return url;
    }

    public void actionPerformed(ActionEvent event) {
        try {
        	BrowserLauncher.openURL(url);
		} catch (Exception e) {
		    String message = "Sorry, there was a problem automatically displaying a page in your web browser.\n" +
		    			"Please manually direct your browser to " + url + "\n\n(Error Message: " + e.getMessage() + ")";
		    Frame parent = ApplicationAccessor.getParentFrame();
			String name = ApplicationAccessor.getAppName();
			JOptionPane.showMessageDialog(parent, message, name, JOptionPane.ERROR_MESSAGE);
		}
    }
}