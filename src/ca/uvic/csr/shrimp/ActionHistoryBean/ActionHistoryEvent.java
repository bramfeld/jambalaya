/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ActionHistoryBean;

 
/**
 * Carries any needed details for redoing an action
 *
 * @author Casey Best
 * @date August 1, 2000
 */
public class ActionHistoryEvent {
	
	ActionHistoryBean actionHistoryBean;
	
	public ActionHistoryEvent(ActionHistoryBean actionHistoryBean) {
		this.actionHistoryBean = actionHistoryBean;
	}
	
	public ActionHistoryBean getActionHistoryBean() {
		return actionHistoryBean;
	}
		
}