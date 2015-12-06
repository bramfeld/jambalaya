/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import javax.swing.Action;
import javax.swing.Icon;


/**
 * Base class for actions used in Shrimp.
 *
 * @author Chris Callendar
 * @date 18-Oct-06
 */
public interface ShrimpAction extends Action {

	/** Cleans up any resources used by the action. */
	public void dispose();

	/**
	 * Returns the name of the action
	 * For example, the name could be "Open Node"
	 */
	public String getActionName();

	/**
	 * @return the name of the action
	 */
	public String getText();

	/**
	 * Returns the tooltip for this action.
	 * If it isn't defined then the action name is returned.
	 */
	public String getToolTip();

	/**
	 * Sets the tooltip for this action.
	 */
	public void setToolTip(String tooltip);

	/**
	 * Returns the icon (if defined) for this action.
	 */
	public Icon getIcon();

	/**
	 * Sets the icon for this action.
	 */
	public void setIcon(Icon icon);

}
