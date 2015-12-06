/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.io.File;
import java.net.URL;

import javax.swing.event.HyperlinkEvent;


/**
 * Shows a hyperlink in the default browser.
 * Works for <code>mailto:</code> links as well.
 * 
 * @author Chris Callendar
 * @date 18-Aug-06
 */
public class HyperlinkAdapter extends HyperlinkHandCursorAdapter {

	public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        	hyperlinkActivated(e.getURL());
        }
	}
	
	public void hyperlinkActivated(URL url) {
		if (url != null) {
			openURL(url.toString());
		}
	}
	
	/**
	 * Opens a URL using {@link BrowserLauncher}.
	 * @param url
	 */
	public static void openURL(String url) {
        try {
            if (!url.startsWith("http:") && !url.startsWith("file:") && !url.startsWith("mailto:")) {
                url = new File(url).toURI().toURL().toString();
            }
            BrowserLauncher.openURL(url);
        } catch (Exception ex) {
        }
	}

}
