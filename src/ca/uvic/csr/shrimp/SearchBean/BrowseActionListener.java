/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventListener;

/**
 * BrowseActionListener.
 *
 * @author Jingwei Wu
 */
public interface BrowseActionListener extends EventListener {
	
    public void browse(BrowseActionEvent e);

}
