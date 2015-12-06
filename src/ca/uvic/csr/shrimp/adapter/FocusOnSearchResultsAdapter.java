/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Vector;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SearchBean.BrowseActionEvent;
import ca.uvic.csr.shrimp.SearchBean.BrowseActionListener;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;
 
/**
 * This class catches a request to focus on an artifact found in the
 * search bean, and passes it to the display bean to handle the request.
 *
 * @author Casey Best
 * date: Nov 7, 2000
 */ 
public class FocusOnSearchResultsAdapter implements BrowseActionListener {

	private ShrimpProject project;
	private String toolName;
	private DisplayBean displayBean;
	private ViewTool view;

	public FocusOnSearchResultsAdapter(ShrimpProject project, String toolName) {
		this.project = project;
		this.toolName = toolName;		
	}
	
	/**
	 * Sends a request to the display bean to focus on the objects in this
	 * event.
	 * @param e Contains the objects to focus on.
	 */
    public void browse(BrowseActionEvent e) {
		try {
			view = (ViewTool) project.getTool(toolName);
			displayBean = (DisplayBean) view.getBean(ShrimpTool.DISPLAY_BEAN);		
		   	Vector objects = e.getObjects();
			Object obj = objects.elementAt(0);
			ShrimpNode target = null;
			if (obj instanceof ShrimpNode) {
				target = (ShrimpNode) obj;
			} else if (obj instanceof Artifact) {				
				Artifact art = (Artifact)obj;
				Vector nodes = displayBean.getDataDisplayBridge().getShrimpNodes(art, true);
				if (!nodes.isEmpty()) {				
				    target = (ShrimpNode) nodes.firstElement();
				}
			}
			if (target != null) {
				view.navigateToObject(target);
			}
		} catch (BeanNotFoundException bnfe) { 
			bnfe.printStackTrace();
		} catch (ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		}
     }
    
}