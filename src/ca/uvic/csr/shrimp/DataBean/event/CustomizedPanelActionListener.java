/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;


/**
 * This interface describes the action that should be performed when
 * a customized panel throws an event.
 *
 * @author Casey Best
 * @date Oct 29, 2000
 */
 public interface CustomizedPanelActionListener {

	/*
	 * Perform the action associated with this event
	 */
	public void actionPerformed(CustomizedPanelActionEvent e);

	/**
	 * Returns the type of panel this listener is meant for
	 */
	public String getCustomizedPanelType();

}