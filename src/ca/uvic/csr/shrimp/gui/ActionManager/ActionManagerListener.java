/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;


/**
 * Listens for adding, removing, and modifying of actions in the ActionManager.
 * 
 * @author Rob Lintern
 */
public interface ActionManagerListener {
	
	/** Called if new actions have been added to the ActionManager */
	public void actionsAdded (ActionsAddedEvent event);
	
	/** Called if actions removed from the ActionManager */
	public void actionsRemoved (ActionsRemovedEvent event);
	
	/** Called if any changes made to multiple actions in the ActionManager */
	public void actionsModified (ActionsModifiedEvent event);
}
