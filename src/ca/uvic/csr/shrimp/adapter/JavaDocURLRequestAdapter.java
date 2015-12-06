/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;

/**
 * This adapter handles requests caused by clicking on a URL
 *
 * @author Casey Best, Chris Callendar
 * date: Sept 21, 2000
 */
public class JavaDocURLRequestAdapter extends URLRequestAdapter {
	
	public JavaDocURLRequestAdapter(ViewTool tool) {
	    super(tool, JavaDomainConstants.PANEL_JAVADOC, JavaDomainConstants.JAVADOC);
	}
		
	/**
	 * Returns the type of panel this listener is meant for
	 */
	public String getCustomizedPanelType() {
		return JavaDomainConstants.PANEL_JAVADOC;
	}
	
}
