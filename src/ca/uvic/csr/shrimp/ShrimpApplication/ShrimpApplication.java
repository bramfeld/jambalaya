/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.util.BuildProperties;

/**
 * Defines the interface for a Shrimp visualization application.
 *
 * @author Nasir Rather
 */
public interface ShrimpApplication {

	// ************************ APPLICATION PROPERTY KEYS ********************

	/**
	 * Property used to indicate whether the prvious view dialog should be shown
	 */
    public static final String SHOW_RETURN_TO_PREVIOUS_DIALOG_KEY = "Show Return to Previous Dialog";

    /**
     * Property key used to indicate if the quick start dialog should be shown
     */
    public static final String SHOW_QUICKSTART_DIALOG_KEY = "Show QuickStart Dialog";

    /**
     * Property key used to indicate if a scrollpane should be used in the main Shrimp view.
     */
    public static final String USE_SCROLLPANE = "Use scrollpane";

    /**
     * Property key used to indicate whether plus icons should be rendered on closed nodes that have children.
     */
    public static final String SHOW_PLUS_ICONS = "Show plus icons";

    /**
     * Property key used to indicate whether attachment icons (paperclip) should be rendered on nodes that have attached files.
     */
    public static final String SHOW_ATTACHMENT_ICONS = "Show attachment icons";

    /**
     * Property key used to indicate whether resize handles should be shown on nodes.
     */
    public static final String SHOW_RESIZE_HANDLES = "Show node resize handles";

    /**
     * Property key used to indicate whether opening and closing nodes use the open/close door animation.
     */
    public static final String ANIMATE_OPEN_CLOSE_DOORS = "Animate opening and closing nodes";

    /**
     * The property key for the Shrimp Options bounds.
     */
	public static final String OPTIONS_DIALOG_BOUNDS = "Shrimp options dialog bounds";


	// ************************ APPLICATION TOOLS ********************

	/**
	 * Use this identifier to retrieve and set the filmStrip.
	 *
	 * @see ShrimpApplication#getTool(String name)
	 */
	public static final String FILMSTRIP = "Filmstrip";

	/**
	 * Use this identifier to retrieve and set the nodeFilter.
	 */
	public static final String NODE_FILTER = "Node Filter";

	/**
	 * Use this identifier to retrieve and set the arcFilter.
	 */
	public static final String ARC_FILTER = "Arc Filter";

	public static final int MAX_OPEN_PROJECTS = 5;

	/**
	 * Returns the properties of this application.
	 * These are application settings that are saved between sessions.
	 *
	 * @return Properties of this application.
	 */
	public Properties getProperties();

	/**
	 * Returns the action manager that this application uses.
	 *
	 * @return ActionManager The action manager that this application uses.
	 */
	public ActionManager getActionManager();

	/**
	 * Returns a ShrimpTool from this application.
	 *
	 * @param name String name identifying a ShrimpTool.
	 *
	 * @return ShrimpTool from this application.
	 *
	 * @throws ShrimpToolNotFoundException Thrown if the requested tool is not found.
	 */
	public ShrimpTool getTool(String name) throws ShrimpToolNotFoundException;

	/**
	 * Returns the tools in this application.
	 */
	public ShrimpTool[] getTools();

	/**
	 * Adds a ShrimpTool to this application.
	 *
	 * @param name String name to identify the tool.
	 * @param tool ShrimpTool tool to be added.
	 */
	public void addTool(String name, ShrimpTool tool);

	/**
	 * Removes a ShrimpTool from this application.
	 *
	 * @param name String name of the ShrimpTool to be added.
	 */
	public void removeTool(String name);

	/**
	 * Adds a listener to this application.
	 *
	 * @param listener ShrimpApplicationListener to be added.
	 */
	public void addApplicationListener(ShrimpApplicationListener listener);

	/**
	 * Removes a listener that was previously added to this application.
	 *
	 * @param listener ShrimpApplicationListener to be removed.
	 */
	public void removeApplicationListener(ShrimpApplicationListener listener);

	/**
	 * Fires userControlsChanged event.
	 */
	public void fireUserControlsChangedEvent();

	/**
	 * This should get fired AFTER the shrimp view has been created for a project.
	 * It notifies the project listeners that the project is activated.
	 * @param project the active project
	 */
	public void fireProjectActivatedEvent(ShrimpProject project);

	/**
	 * Closes the given project. This should only be called from the
	 * {@link ShrimpProject#disposeProject()} method.
	 * @param project
	 */
	public void closeProject(ShrimpProject project);

	/**
	 * @return All open projects.
	 */
	public ShrimpProject[] getProjects();

	/**
	 * Adds a project to the list of open projects.
	 * @param project the project to add
	 */
	public void addProject(ShrimpProject project);

	/**
	 * Returns the first project or null.
	 */
	public ShrimpProject getFirstProject();

	/**
	 * Returns the maximum number of open projects.
	 * @return int the max number of projects between 1 and MAX_OPEN_PROJECTS.
	 */
	public int getMaxOpenProjects();

	/**
	 * @return A vector of user actions that this application can handle.
	 */
	public Vector getUserActions();

	/**
	 * @param action
	 * @return The user events associated with the given user action.
	 */
	public Vector getUserEvents(UserAction action);

	/**
	 * Saves the propeties of this application to a file.
	 */
	public void saveProperties();

	/**
	 * Returns the top level frame of this application.
	 * @return top level Frame of this application.
	 */
	public Frame getParentFrame();

	/**
	 * Sets the parent (top level) frame for this application.
	 * @param frame
	 */
	public void setParentFrame(Frame frame);

	/**
	 * Returns the number of open ShrimpProjects.
	 *
	 * @return int number of projects open in this application.
	 */
	public int getProjectCount();

	/**
	 * Tells if this application is active or not.
	 *
	 * @return boolean if this application is active.
	 */
	public boolean isActive();

	/**
	 * Displays the given message on "status" panel.
	 * It is up to each implementation of this interface to decide what
	 * kind of panel this is and where it is placed.
	 *
	 * @param message The message to display.
	 */
	public void setMessagePanelMessage(String message);

	/**
	 * Call this when you want this application to close.
	 */
	public void close();

	/**
	 * @return The name of this application (ex SHriMP, Jambalaya, Creole, etc)
	 */
	public String getName();

	/**
	 * @return A description of this application's build infomation
	 *  including name, build version, build date, and build number.
	 */
	public BuildProperties getBuildInfo();

	/**
	 * @return The "branding" icon for this application.
	 */
	public Icon getIcon();

	/**
	 * @return The "branding" image for this application.
	 */
	public Image getImage();

    public String getHelpEmailAddress();

    /**
     * Sets both the app cursor and the display bean cursor.
     * @param cursor
     */
	public void setCursor(Cursor cursor);

	/**
	 * Sets just the display bean cursor.
	 * @param newCursor
	 */
	public void setDisplayBeanCursor(Cursor newCursor);
	/**
	 * Sets just the app cursor.
	 * @param cursor
	 */
	public void setAppCursor(Cursor cursor);

	/**
	 * Sets the app and display bean cursor to be the wait cursor.
	 */
	public void waitCursor();

	/**
	 * Sets the app and display bean cursor to the default.
	 */
	public void defaultCursor();

	/**
	 * Sets the default application cursor.
	 * @param cursor
	 */
	public void setDefaultAppCursor(Cursor cursor);

	/**
	 * Maps user actions to the events that trigger them.
	 * @param loadFromProperties if true each action checks the properties file for saved settings
	 *  if false then the default shortcut keys are used.
	 */
	public void setDefaultUserControls(boolean loadFromProperties);

}
