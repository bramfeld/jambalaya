/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.ShrimpProject.AbstractShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.adapter.tools.OpenArcFilterAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenAttributePanelAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenFilmStripAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenFilterToolAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenHierarchicalViewAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenNodeFilterAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenQueryViewAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenQuickViewsAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenScriptingToolAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenSearchToolAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenThumbnailViewAdapter;
import ca.uvic.csr.shrimp.gui.AboutDialog;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManagerListener;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsAddedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsModifiedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionsRemovedEvent;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.options.GeneralOptionsPanel;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.AttachDocumentToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeLabelIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.ChangeNodeOverlayIconAdapter;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserAction;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserEvent;
import ca.uvic.csr.shrimp.usercontrols.FilterSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.FisheyeInAdapter;
import ca.uvic.csr.shrimp.usercontrols.FisheyeOutAdapter;
import ca.uvic.csr.shrimp.usercontrols.FocusOnHomeAdapter;
import ca.uvic.csr.shrimp.usercontrols.GroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.MagnifyInAdapter;
import ca.uvic.csr.shrimp.usercontrols.MagnifyOutAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenAllAdapter;
import ca.uvic.csr.shrimp.usercontrols.OpenCloseNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanEastAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanNorthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanSouthAdapter;
import ca.uvic.csr.shrimp.usercontrols.PanWestAdapter;
import ca.uvic.csr.shrimp.usercontrols.PruneSubgraphAdapter;
import ca.uvic.csr.shrimp.usercontrols.RedoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllChildrenAdapter;
import ca.uvic.csr.shrimp.usercontrols.SelectAllDescendantsAdapter;
import ca.uvic.csr.shrimp.usercontrols.SnapShotAdapter;
import ca.uvic.csr.shrimp.usercontrols.SpaceInvadersAdapter;
import ca.uvic.csr.shrimp.usercontrols.ToggleLongToolTipAdapter;
import ca.uvic.csr.shrimp.usercontrols.UndoActionAdapter;
import ca.uvic.csr.shrimp.usercontrols.UnfilterAllByIdAdapter;
import ca.uvic.csr.shrimp.usercontrols.UngroupSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.UserEvent;
import ca.uvic.csr.shrimp.usercontrols.ViewDocumentsAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomInAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomInZoomModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutAnyModeAdapter;
import ca.uvic.csr.shrimp.usercontrols.ZoomOutZoomModeAdapter;
import ca.uvic.csr.shrimp.util.BuildProperties;
import ca.uvic.csr.shrimp.util.ShowInBrowserAction;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.SortedProperties;
import edu.umd.cs.piccolo.PCanvas;

/**
 * A base implementation for ShrimpApplication. Classes wanting to implement a
 * ShrimpApplication should ideally extend this class. This class includes most common operations.
 *
 * @author Rob Lintern, Nasir Rather, Chris Callendar
 */
public abstract class AbstractShrimpApplication implements ShrimpApplication {

	private static final String BUILD_PROPERTIES_FILE_NAME = "build.properties";

    /** default help email address */
    private static final String HELP_EMAIL = "chisel-support@cs.uvic.ca";

	// Collection of Projects that have been opened.
	private Vector projects;

	private int maxOpenProjects = MAX_OPEN_PROJECTS;

	// A map of application-level ShimpTools.
	protected Map tools;

	// application-level properties
	protected Properties properties;

	// application's actionManager
	protected ActionManager actionManager;
	private Vector applicationActions;

	// Tells if the application is active or not
	private boolean active = true;

	// collection of listeners of this application
	private Vector listeners;

	// collection of the customized actions for this application
	private Map dummyUserActions;
	// stores the user events to check for duplicates
	private Set dummyUserEvents;

	private String propertiesFileName;
	protected BuildProperties buildProperties;
	protected Icon brandingIcon;
	protected Image brandingImage;
	protected String helpEmail = HELP_EMAIL;

	private Cursor defaultAppCursor = DEFAULT_CURSOR;
	private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

    protected Frame parentFrame;

	// The application's menubar
	protected JMenuBar menuBar;

	/**
	 * Takes care of necessary initializations.
	 * This has to be called by all subclasses.
	 * Sets the max open projects to MAX_OPEN_PROJECTS.
	 */
	public AbstractShrimpApplication(String propertiesFileName, String defaultAppName) {
		this(propertiesFileName, defaultAppName, MAX_OPEN_PROJECTS);
	}

	/**
	 * Takes care of necessary initializations.
	 * This has to be called by all subclasses.
	 * @param maxOpenProjects the maximum number of open projects
	 */
	public AbstractShrimpApplication(String propertiesFileName, String defaultAppName, int maxOpenProjects) {
		// initialize members
		ApplicationAccessor.setApplication(this);
		this.propertiesFileName = propertiesFileName;
		setMaxOpenProjects(maxOpenProjects);
		properties = new SortedProperties();	// sorts the keys which makes it easier to read
		actionManager = new ActionManager();
		projects = new Vector(maxOpenProjects);
		tools = new HashMap();
		listeners = new Vector();
		applicationActions = new Vector();
		dummyUserActions = new HashMap();
		dummyUserEvents = new HashSet();
		menuBar = new JMenuBar();

		loadBuildProperties(defaultAppName, BUILD_PROPERTIES_FILE_NAME);
		System.out.println("\n" + buildProperties.toString() + "\nCHISEL Group, University of Victoria, " + helpEmail + "\n");

		// Load the application properties
		loadProperties();

		loadOptionsFromProperties();

		setDefaultUserControls(true);
	}

	/**
	 * Loads the options that are saved in the Shrimp.properties file.
	 * These are the options set in the {@link GeneralOptionsPanel}.
	 */
	protected void loadOptionsFromProperties() {
		// show plus icons
		String shownStr = properties.getProperty(ShrimpApplication.SHOW_PLUS_ICONS, "true");
		PShrimpNode.RENDER_PLUS_ICON = ShrimpUtils.parseBoolean(shownStr, PShrimpNode.RENDER_PLUS_ICON);

		// show attachment icons
		shownStr = properties.getProperty(ShrimpApplication.SHOW_ATTACHMENT_ICONS, "true");
		PShrimpNode.RENDER_ATTACHMENT_ICON = ShrimpUtils.parseBoolean(shownStr, PShrimpNode.RENDER_ATTACHMENT_ICON);

		// show resize handles
		shownStr = properties.getProperty(ShrimpApplication.SHOW_RESIZE_HANDLES, "true");
		PShrimpNode.DEFAULT_SHOW_RESIZE_HANDLES = ShrimpUtils.parseBoolean(shownStr, PShrimpNode.DEFAULT_SHOW_RESIZE_HANDLES);

		// animate doors
		shownStr = properties.getProperty(ShrimpApplication.ANIMATE_OPEN_CLOSE_DOORS, "false");
		PShrimpNode.DEFAULT_ANIMATE_DOORS = ShrimpUtils.parseBoolean(shownStr, PShrimpNode.DEFAULT_ANIMATE_DOORS);
	}

	/**
	 * Loads the build properties from the given properties file.
	 * CreoleApplication overrides this method to set the plugin build version.
	 * @param defaultAppName
	 * @param buildPropertiesFilename the filename for the properties file (relative to the class), can be null
	 */
	protected void loadBuildProperties(String defaultAppName, String buildPropertiesFilename) {
		this.buildProperties = new BuildProperties(defaultAppName);
		this.buildProperties.loadBuildProperties(buildPropertiesFilename, getClass());
	}

	class DummyUserAction extends DefaultUserAction {

		public DummyUserAction(String name) {
			super(name);
		}

		public void startAction() {
			System.err.println("Dummy UserAction: Should never do anything");
		}

	}

	/**
	 * Maps user actions to the events that trigger them.
	 * @param loadFromProperties if true each action checks the properties file for saved settings
	 *  if false then the default shortcut keys are used.
	 */
	public void setDefaultUserControls(boolean loadFromProperties) {
		// clear all the existing actions
		dummyUserActions.clear();
		dummyUserEvents.clear();

		// SNAPSHOT
		DummyUserAction ua = addUserAction(SnapShotAdapter.ACTION_NAME, SnapShotAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_S, false, true, false);
		// PAN EAST
		ua = addUserAction(PanEastAdapter.ACTION_NAME, PanEastAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_LEFT, false, false, false);
		// PAN WEST
		ua = addUserAction(PanWestAdapter.ACTION_NAME, PanWestAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_RIGHT, false, false, false);
		// PAN NORTH
		ua = addUserAction(PanNorthAdapter.ACTION_NAME, PanNorthAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_DOWN, false, false, false);
		// PAN SOUTH
		ua = addUserAction(PanSouthAdapter.ACTION_NAME, PanSouthAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_UP, false, false, false);
		// ZOOM IN ANY MODE (magnify, zoom, fisheye)
		ua = addUserAction(ZoomInAnyModeAdapter.ACTION_NAME, ZoomInAnyModeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(true, UserEvent.MIDDLE_MOUSE_BUTTON, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_X, false, false, false);
		// ZOOM OUT ANY MODE (magnify, zoom, fisheye)
		ua = addUserAction(ZoomOutAnyModeAdapter.ACTION_NAME, ZoomOutAnyModeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(true, UserEvent.MIDDLE_MOUSE_BUTTON, false, true, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_Z, false, false, false);
		// ZOOM IN ZOOM MODE
		ua = addUserAction(ZoomInZoomModeAdapter.ACTION_NAME, ZoomInZoomModeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_EQUALS, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_EQUALS, false, true, false);
		// ZOOM OUT ZOOM MODE
		ua = addUserAction(ZoomOutZoomModeAdapter.ACTION_NAME, ZoomOutZoomModeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_MINUS, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_MINUS, false, true, false);
		// MAGNIFY IN
		ua = addUserAction(MagnifyInAdapter.ACTION_NAME, MagnifyInAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_ADD, false, false, false);
		// MAGNIFY OUT
		ua = addUserAction(MagnifyOutAdapter.ACTION_NAME, MagnifyOutAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_SUBTRACT, false, false, false);
		// FISHEYE IN
		ua = addUserAction(FisheyeInAdapter.ACTION_NAME, FisheyeInAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_F, false, false, false);
		// FISHEYE OUT
		ua = addUserAction(FisheyeOutAdapter.ACTION_NAME, FisheyeOutAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_F, false, true, false);
		// OPEN/CLOSE NODE
		ua = addUserAction(OpenCloseNodeAdapter.ACTION_NAME, OpenCloseNodeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(true, UserEvent.DOUBLE_CLICK__LEFT_MOUSE_BUTTON, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_SPACE, false, false, false);
		// OPEN ALL
		ua = addUserAction(OpenAllAdapter.ACTION_NAME, OpenAllAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_O, false, false, false);
		// TOGGLE LONG TOOLTIPS
		ua = addUserAction(ToggleLongToolTipAdapter.ACTION_NAME, ToggleLongToolTipAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_T, false, false, false);
		// GROUP SELECTED
		// @tag Shrimp.grouping
		ua = addUserAction(GroupSelectedArtifactsAdapter.ACTION_NAME, GroupSelectedArtifactsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_G, false, false, false);
		// UNGROUP SELECTED
		ua = addUserAction(UngroupSelectedArtifactsAdapter.ACTION_NAME, UngroupSelectedArtifactsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_G, false, true, false);
		// RENAME
		ua = addUserAction(RenameSelectedArtifactsAdapter.ACTION_NAME, RenameSelectedArtifactsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_F2, false, false, false);
		// FOCUS ON HOME
		ua = addUserAction(FocusOnHomeAdapter.ACTION_NAME, FocusOnHomeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_H, false, false, false);
		// UNDO
		ua = addUserAction(UndoActionAdapter.ACTION_NAME, UndoActionAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_COMMA, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_Z, true, false, false);
		// REDO
		ua = addUserAction(RedoActionAdapter.ACTION_NAME, RedoActionAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_PERIOD, false, false, false);
		ua.addDefaultUserEvent(false, KeyEvent.VK_Y, true, false, false);
		// SELECT ALL CHILDREN
		ua = addUserAction(SelectAllChildrenAdapter.ACTION_NAME, SelectAllChildrenAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_A, true, false, false);
		// SELECT ALL DESCENDANTS
		ua = addUserAction(SelectAllDescendantsAdapter.ACTION_NAME, SelectAllDescendantsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_D, true, false, false);
		// FILTER SELECTED
		ua = addUserAction(FilterSelectedArtifactsAdapter.ACTION_NAME, FilterSelectedArtifactsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_DELETE, false, false, false);
		// UNFILTER ALL
		ua = addUserAction(UnfilterAllByIdAdapter.ACTION_NAME, UnfilterAllByIdAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_DELETE, false, true, false);
		// PRUNE
		ua = addUserAction(PruneSubgraphAdapter.ACTION_NAME, PruneSubgraphAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_P, false, false, false);

		// ATTACH and VIEW DOCUMENTS
		// @tag Shrimp.DocumentManager : user control defaults
		ua = addUserAction(AttachDocumentToNodeAdapter.ACTION_NAME, AttachDocumentToNodeAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_A, false, false, false);
		ua = addUserAction(ViewDocumentsAdapter.ACTION_NAME, ViewDocumentsAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_D, false, false, false);

		// Change node label and overlay icon
		ua = addUserAction(ChangeNodeLabelIconAdapter.ACTION_NAME, ChangeNodeLabelIconAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_N, true, true, false);
		ua = addUserAction(ChangeNodeOverlayIconAdapter.ACTION_NAME, ChangeNodeOverlayIconAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_O, true, true, false);

		// @tag Shrimp.SpaceInvaders
		ua = addUserAction(SpaceInvadersAdapter.ACTION_NAME, SpaceInvadersAdapter.TOOLTIP, loadFromProperties);
		ua.addDefaultUserEvent(false, KeyEvent.VK_I, true, true, false);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getParentFrame()
	 */
	public Frame getParentFrame() {
		return parentFrame;
	}

    public void setParentFrame(Frame frame) {
        this.parentFrame = frame;
    }

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getUserActions()
	 */
	public Vector getUserActions() {
		return new Vector(dummyUserActions.values());
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getUserEvents(ca.uvic.csr.shrimp.usercontrols.UserAction)
	 */
	public Vector getUserEvents(UserAction action) {
		UserAction dummyAction = (UserAction) dummyUserActions.get(action.getActionName());
		if (dummyAction == null) {
			return action.getUserEvents(); // return its own userEvents
		}
		String userEventsString = DefaultUserEvent.userEventsToPropertiesString(dummyAction.getUserEvents());
		Vector userEvents = DefaultUserEvent.propertiesStringToUserEvents(userEventsString, action);
		return userEvents;
	}

	protected DummyUserAction addUserAction(String name, String description, boolean loadFromProperties) {
		DummyUserAction ua = addUserAction(name, loadFromProperties);
		ua.setToolTip(description);
		return ua;
	}

	protected DummyUserAction addUserAction(String name, boolean loadFromProperties) {
		DummyUserAction ua = new DummyUserAction(name);

		// try to load the default events from the properties file
		if (loadFromProperties) {
			String propertyValue = properties.getProperty(DefaultProjectAction.userActionToPropertiesKeyString(ua));
			if (propertyValue != null) {
				Vector userEvents = DefaultUserEvent.propertiesStringToUserEvents(propertyValue, ua);
				// check for duplicates
				for (Iterator iter = userEvents.iterator(); iter.hasNext(); ) {
					UserEvent event = (UserEvent) iter.next();
					if (dummyUserEvents.contains(event)) {
						iter.remove();
					} else {
						dummyUserEvents.add(event);
					}
				}
				ua.setUserEvents(userEvents);
			}
		}

		dummyUserActions.put(name, ua);
		return ua;
	}

	private void removeUserActions() {
		for (Iterator iter  = dummyUserActions.values().iterator(); iter.hasNext(); ) {
			UserAction action = (UserAction) iter.next();
			action.dispose();
		}
		dummyUserActions.clear();
	}

	/**
	 * Displays the menus of the application.
	 * Each subclass will have to implement this method in order to use the actionManager.
	 */
	public void createMenus() {
		menuBar.removeAll();

		// add the menus from the action manager
		JMenu allMenus = actionManager.createMenus("ApplicationMenus");
		Component[] menuComponents = allMenus.getMenuComponents();

		for (int i = 0; i < menuComponents.length; i++) {
			JMenu menu = null;
			if (menuComponents[i] instanceof JMenu) {
				menu = (JMenu) menuComponents[i];
			} else if (menuComponents[i] instanceof JMenuItem) {
				// turn this menuItem into an empty menu (another hack!)
				JMenuItem item = (JMenuItem)menuComponents[i];
				menu = new JMenu(item.getAction());
			} else {
				// we dont want to add anything that is not a menu
			}
			if (menu != null) {
				menuBar.add(menu);
			}
		}

		menuBar.add(Box.createHorizontalGlue()); // fills the rest of the space in
		menuBar.revalidate();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#setMessagePanelMessage(java.lang.String)
	 */
	public void setMessagePanelMessage(String message) {
		// do nothing
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getProjects()
	 */
	public ShrimpProject[] getProjects() {
		return (ShrimpProject[]) projects.toArray(new ShrimpProject[projects.size()]);
	}

	/**
	 * Returns the first project or null.
	 */
	public ShrimpProject getFirstProject() {
		return (projects.size() > 0 ? (ShrimpProject)projects.get(0) : null);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getProperties()
	 */
	public Properties getProperties() {
		return this.properties;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getActionManager()
	 */
	public ActionManager getActionManager() {
		return this.actionManager;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getTool(String)
	 */
	public ShrimpTool getTool(String name) throws ShrimpToolNotFoundException {
		if (!this.tools.containsKey(name)) {
			throw new ShrimpToolNotFoundException(name);
		}
		return (ShrimpTool) this.tools.get(name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getTools()
	 */
	public ShrimpTool[] getTools() {
		ShrimpTool [] toolArray = new ShrimpTool [tools.size()];
		Collection coll = tools.values();
		coll.toArray(toolArray);
		return toolArray;
	}


	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#addTool(String, ShrimpTool)
	 */
	public void addTool(String name, ShrimpTool tool) {
		if (name != null && tool != null) {
			this.tools.put(name, tool);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#removeTool(String)
	 */
	public void removeTool(String name) {
		if (this.tools.containsKey(name)) {
			this.tools.remove(name);
		}
	}

	/**
	 * Saves the properties to a file.  Creates the file if it doesn't exist.
	 * @see ShrimpApplication#saveProperties()
	 */
	public void saveProperties() {
        String fileName = propertiesFileName;
	    if (fileName == null || fileName.length() == 0) {
	    	return;
	    }

		if (properties != null) {
			try {
				File file = new File(fileName);
				if (!file.exists()) {
					if (file.getParentFile() != null) {
						file.getParentFile().mkdirs();
					}
					file.createNewFile();
				}
				FileOutputStream out = new FileOutputStream(file);
				properties.store(out, "--- " + getName() + " Properties ---");
				out.close();
			} catch (Exception e) {
                System.err.println(getName() + " Warning: Could not save properties to '" + fileName + ".'" + " Reason: " + e.getMessage());
			}
		}
	}

	/**
	 * Loads the propeties of this application from a file.
	 * If the properties file is not found nothing happens (doesn't create it).
	 * @param fileName String name of the file.
	 */
	private void loadProperties() {
        String fileName = propertiesFileName;
	    if (fileName == null || fileName.length() == 0) {
	    	return;
	    }

		try {
			File file = new File(fileName);
			FileInputStream in = new FileInputStream(file);
			this.properties.load(in);
			in.close();
		} catch (FileNotFoundException e) {
			// do nothing
		} catch (Exception e) {
            System.err.println(getName() + " Warning: Could not load application properties file at '" + fileName + ".'" + " Reason: " + e.getMessage());
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getProjectCount()
	 */
	public int getProjectCount() {
		return this.projects.size();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#isActive()
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * First creates the default menu actions, then creates the application specific menu actions.
	 * @tag Shrimp.createApplicationActions
	 */
	protected final void createApplicationActions() {
		// Add a listener to the menumanger, tells it what to do when actions change
		// Note: This should go after the createMenus() call, we do not want to go through any events
		// cause this is not the actual subclass constructor
		actionManager.addActionManagerListener(new ActionManagerListener() {
			public void actionsAdded(ActionsAddedEvent event) {
				createMenus();
			}
			public void actionsRemoved(ActionsRemovedEvent event) {
				createMenus();
			}
			public void actionsModified(ActionsModifiedEvent event) {
				createMenus();
			}
		});

		// stop firing actions (don't recreate menus until the end)
		boolean firing = actionManager.getFiringEvents();
		actionManager.setFiringEvents(false);

		// Create the default menus: File, Edit, Node, Arc, Navigate, Tools, Help
		createMenuActions();

		// create the application specific actions - this should be the only place
		// this method gets called
		createApplicationSpecificActions();

		// start firing events again - creates the menus
		actionManager.setFiringEvents(firing);
	}

	/**
	 * Creates the application specific actions.
	 * Creole, Jambalaya and StandAlone Shrimp will override this method to create actions
	 * specific to each application.
	 */
	protected void createApplicationSpecificActions() {

		//Edit -> Search
		ShrimpAction action = new OpenSearchToolAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_F, 1);

		int i = 1;
		// Tools -> Node Filter
		action = new OpenNodeFilterAdapter(ShrimpProject.SHRIMP_VIEW);
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);
		// Tools -> Arc Filter
		action = new OpenArcFilterAdapter(ShrimpProject.SHRIMP_VIEW);
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		// Tools -> FilmStrip
		action = new OpenFilmStripAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		// Tools -> ThumbnailView
		action = new OpenThumbnailViewAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		// Tools -> Query View
		action = new OpenQueryViewAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		// Tools -> Quick Views
		action = new OpenQuickViewsAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		// Tools->Extras Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_MORETOOLS);
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_C, i++);

		//Tools -> Extras -> Filter	@tag Shrimp.FilterTool
		action = new OpenFilterToolAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS_MORETOOLS, ShrimpConstants.GROUP_A, 1);

		// Tools -> Extras -> Attribute Filter
		action = new OpenAttributePanelAdapter(DataBean.ARTIFACT_TYPE);
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS_MORETOOLS, ShrimpConstants.GROUP_A, 2);
		//action = new OpenAttributePanelAdapter(DataBean.RELATIONSHIP_TYPE);
		//addApplicationAction(action, ShrimpConstants.MENU_TOOLS, ShrimpConstants.GROUP_A, 3);

		// Tools -> Extras -> Hierarchical View
		action = new OpenHierarchicalViewAdapter();
		addApplicationAction(action, ShrimpConstants.MENU_TOOLS_MORETOOLS, ShrimpConstants.GROUP_A, 4);

		// Tools -> Extras -> Scripting
		if (ShrimpUtils.isScriptingToolInstalled()) {
			action = new OpenScriptingToolAdapter();
			addApplicationAction(action, ShrimpConstants.MENU_TOOLS_MORETOOLS, ShrimpConstants.GROUP_A, 5);
		}

		//Help -> About
		action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_ABOUT) {
			public void actionPerformed(ActionEvent e) {
				new AboutDialog(getParentFrame());
			}
		};
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 1);

		//Help -> Feedback
        String url = "mailto:" + helpEmail + "?subject=" + getName() + "%20Feedback";
        action = new ShowInBrowserAction(ShrimpConstants.ACTION_NAME_SEND_FEEDBACK, url, ResourceHandler.getIcon("icon_feedback.gif"));
        addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_B, 1);
	}

	/**
	 * Creates the common menu actions: File, Edit, Node, Arc, Navigate, Tools, and Help.
	 */
	protected void createMenuActions() {
		int i = 1;
		// File Menu
		ShrimpAction action = new DefaultShrimpAction(ShrimpConstants.MENU_FILE);
		action.setEnabled(true);
		addApplicationAction(action, "", "", i++);
		// Edit Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_EDIT);
		addApplicationAction(action, "", "", i++);
		// Node Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_NODE);
		action.setEnabled(false);
		addApplicationAction(action, "", "", i++);
		// Arc Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_ARC);
		action.setEnabled(false);
		addApplicationAction(action, "", "", i++);
		// Navigate Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_NAVIGATE);
		action.setEnabled(false);
		addApplicationAction(action, "", "", i++);
		// Tools Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_TOOLS);
		addApplicationAction(action, "", "", i++);

		// Help Menu
		action = new DefaultShrimpAction(ShrimpConstants.MENU_HELP);
		addApplicationAction(action, "", "", i++);

	}

	protected void addApplicationAction(ShrimpAction action, String path, String group, int position) {
		actionManager.addAction(action, path, group, position);
		applicationActions.add(action);
	}

	/** Remove actions from this application to the actionManager. */
	private void removeApplicationActions() {
		for (int i = 0; i < applicationActions.size(); i++) {
			ShrimpAction action = (ShrimpAction) applicationActions.get(i);
			actionManager.removeAction(action, ActionManager.IGNORE_PARENT);
			action.dispose();
		}
		applicationActions.clear();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#addApplicationListener(ShrimpApplicationListener)
	 */
	public void addApplicationListener(ShrimpApplicationListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#removeApplicationListener(ShrimpApplicationListener)
	 */
	public void removeApplicationListener(ShrimpApplicationListener listener) {
		this.listeners.remove(listener);
	}

	protected void fireApplicationActivatedEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		active = true;
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpApplicationListener) cloneListeners.get(i)).applicationActivated(new ShrimpApplicationEvent(this));
		}
	}

	protected void fireApplicationDeactivatedEvent() {
		active = false;
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpApplicationListener) cloneListeners.get(i)).applicationDeactivated(new ShrimpApplicationEvent(this));
		}
	}

	protected void fireApplicationClosingEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpApplicationListener) cloneListeners.get(i)).applicationClosing(new ShrimpApplicationEvent(this));
		}
	}

	protected void fireApplicationStartedEvent() {
		Vector cloneListeners = (Vector) listeners.clone();

		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpApplicationListener) cloneListeners.get(i)).applicationStarted(new ShrimpApplicationEvent(this));
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#fireUserControlsChangedEvent()
	 */
	public void fireUserControlsChangedEvent() {
		Vector cloneListeners = (Vector) listeners.clone();
		for (int i = 0; i < cloneListeners.size(); i++) {
			((ShrimpApplicationListener) cloneListeners.get(i)).userControlsChanged(new ShrimpApplicationEvent(this));
		}
	}

	private void fireProjectCreatedEvent(ShrimpProject createdProject) {
		Vector clonedListeners = (Vector) listeners.clone();
		ShrimpProjectEvent evt = new ShrimpProjectEvent(createdProject);
		for (int i = 0; i < clonedListeners.size(); i++) {
			((ShrimpApplicationListener) clonedListeners.get(i)).projectCreated(evt);
		}
	}

	public void fireProjectActivatedEvent(ShrimpProject project) {
		if (project != null) {
			((AbstractShrimpProject)project).fireProjectActivatedEvent();
			Vector clonedListeners = (Vector) listeners.clone();
			ShrimpProjectEvent evt = new ShrimpProjectEvent(project);
			for (int i = 0; i < clonedListeners.size(); i++) {
				((ShrimpApplicationListener) clonedListeners.get(i)).projectActivated(evt);
			}
		}
	}

	protected void fireProjectClosedEvent(ShrimpProject project) {
		if (project != null) {
			Vector clonedListeners = (Vector) listeners.clone();
			ShrimpProjectEvent evt = new ShrimpProjectEvent(project);
			for (int i = 0; i < clonedListeners.size(); i++) {
				((ShrimpApplicationListener) clonedListeners.get(i)).projectClosed(evt);
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#close()
	 */
	public void close() {
		fireApplicationClosingEvent();
		disposeProjects();
		removeUserActions();
		removeApplicationActions();
		saveProperties();

		listeners.clear();
		actionManager.clear();

		System.runFinalization();
		System.gc();
	}

	/**
	 * Disposes all the projects.
	 */
	private void disposeProjects() {
		while (projects.size() > 0) {
			ShrimpProject project = (ShrimpProject)projects.remove(projects.size() - 1);
			project.disposeProject();
		}
	}

	/**
	 * Closes the given project. This should only be called from {@link ShrimpProject#disposeProject()}.
	 * @param project
	 */
	public void closeProject(ShrimpProject project) {
		if (project == null) {
			return;
		}

		boolean removed = this.projects.remove(project);
		if (removed) {
			fireProjectClosedEvent(project);
		}

		// clean up any garbage
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.runFinalization();
				System.gc();
			}
		});
	}

	public void addProject(ShrimpProject project) {
		if (project != null) {
			if (projects.contains(project)) {
				return;
			}
			// if too many projects are open, close the first ones
			while (projects.size() >= maxOpenProjects) {
				ShrimpProject closeProject = (ShrimpProject)projects.remove(0);
				if (closeProject != null) {
					closeProject.disposeProject();
				}
			}

			projects.add(project);

			// @tag Shrimp.createProjectActions: this is where the project actions are created
			// this should be the only place that this gets called
			// it must be done after any of the projects above are closed
			// because they remove actions from the ActionManager
			// here those actions get added back
			project.createProjectActions();

			// signals that the project has been created - the ShrimpView has not been created yet though!
			fireProjectCreatedEvent(project);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getBuildInfo()
	 */
	public BuildProperties getBuildInfo() {
		return buildProperties;
	}

	public String toString() {
		return "ShrimpApplication: " + getName();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getName()
	 */
	public String getName() {
		return buildProperties.getName();
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getIcon()
	 */
	public Icon getIcon() {
		return brandingIcon;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#getImage()
	 */
	public Image getImage() {
		return brandingImage;
	}

	public void setCursor(Cursor newCursor) {
	    setAppCursor(newCursor);
	    setDisplayBeanCursor(newCursor);
	}

	public void setAppCursor(Cursor cursor) {
	    Frame frame = getParentFrame();
		if ((frame != null) && (frame.getCursor() != null) && !frame.getCursor().equals(cursor)) {
	        frame.setCursor(cursor);
	    }
	}

	public void setDefaultAppCursor(Cursor defaultAppCursor) {
		this.defaultAppCursor = defaultAppCursor;
	}

	public void setDisplayBeanCursor(Cursor cursor) {
		for (int i = 0; i < projects.size(); i++) {
			ShrimpProject project = (ShrimpProject) projects.get(i);
	        try {
                PCanvas canvas = ((PNestedDisplayBean)project.getTool(ShrimpProject.SHRIMP_VIEW).getBean(ShrimpTool.DISPLAY_BEAN)).getPCanvas();
                if ((canvas.getCursor() != null) && !canvas.getCursor().equals(cursor)) {
                	canvas.setCursor(cursor);
                }
            } catch (BeanNotFoundException e) {
                //e.printStackTrace();
            } catch (ShrimpToolNotFoundException e) {
                 //e.printStackTrace();
            }
	    }
	}

	public void defaultCursor() {
		setAppCursor(defaultAppCursor);
		setDisplayBeanCursor(getDefaultDisplayCursor());
	}

	public void waitCursor() {
		setCursor(WAIT_CURSOR);
	}

	private Cursor getDefaultDisplayCursor() {
        String mode = getProperties().getProperty(ShrimpView.MOUSE_MODE_PROPERTY_KEY, ShrimpView.MOUSE_MODE_DEFAULT);
        Cursor initialDisplayCursor = Cursor.getDefaultCursor();
		if (mode.equals(DisplayConstants.MOUSE_MODE_SELECT)) {
		    initialDisplayCursor = ShrimpView.CURSOR_SELECT;
		} else if(mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_IN)) {
		    initialDisplayCursor = ShrimpView.CURSOR_ZOOM_IN;
		} else if(mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_OUT)) {
		    initialDisplayCursor = ShrimpView.CURSOR_ZOOM_OUT;
		}
		return initialDisplayCursor;
	}

    public String getHelpEmailAddress() {
        return helpEmail;
    }

	public int getMaxOpenProjects() {
		return maxOpenProjects;
	}

	/**
	 * Sets the max open projects.  It is not recommended that you call this method.
	 * Both Jambalaya and Creole only expect one project to be open at a time.
	 * @param maxOpenProjects the max number of open projects (between 1 and {@link AbstractShrimpApplication#MAX_OPEN_PROJECTS})
	 */
	public void setMaxOpenProjects(int maxOpenProjects) {
		this.maxOpenProjects = Math.max(1, Math.min(MAX_OPEN_PROJECTS, maxOpenProjects));
	}

}