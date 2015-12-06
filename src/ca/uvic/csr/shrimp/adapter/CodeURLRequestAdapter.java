/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;

/**
 * Handles a request to show the source code of a node.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class CodeURLRequestAdapter extends URLRequestAdapter {

    public CodeURLRequestAdapter(ViewTool tool) {
        super(tool, SoftwareDomainConstants.PANEL_CODE, SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI);
    }
    
}
