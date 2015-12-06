/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.AbstractShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.adapter.FileOpenCommandAdapter;
import ca.uvic.csr.shrimp.adapter.tools.OpenNodeFilterAdapter;
import ca.uvic.csr.shrimp.gui.DesktopInternalFrame;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.gui.ScrollDesktopPane;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewConfiguration;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewFactory;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.RenameSelectedArtifactsAdapter;
import ca.uvic.csr.shrimp.util.ShowInBrowserAction;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;


/**
 * Implements a standalone Shrimp application.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class StandAloneApplication extends AbstractShrimpApplication {

	public static final String APPLICATION_TITLE = "Shrimp";
	protected static final String OPEN_DIRECTORY_PREF_KEY = "directory of most recently opened file";
	protected static final String SAVE_DIRECTORY_PREF_KEY = "directory of most recently saved file";
	private static final String RECENT_FILES_PREF_KEY = "recently opened files";
	private static final int MAX_RECENT_FILES = 10;

	private List recentlyOpenedFiles;

	// Demo filenames
	public static final String DEMO_HANGMAN = "demo/hangman/hangman-fri.prj";
	public static final String DEMO_BINGO = "demo/bingo/bingo.prj";
    public static final String DEMO_NEWSPAPER_PPRJ_LOCAL = "demo/newspaper/newspaper.pprj";
    public static final String DEMO_WINES_PPRJ_LOCAL = "demo/wines/wines.pprj";
    public static final String DEMO_PIZZA_OWL_LOCAL = "demo/pizza/pizza.owl";
    public static final String DEMO_PIZZA_OWL_REMOTE = "http://smi-protege.stanford.edu/repos/protege/owl/trunk/examples/pizza.owl";
    public static final String DEMO_TRAVEL_OWL_REMOTE = "http://smi-protege.stanford.edu/repos/protege/owl/trunk/examples/travel.owl";
    public static final String DEMO_OBO_LOCAL = "demo/obo/testfile.obo";

	public static final String[][] DEMOS = {
            {"BINGO! (Java Program)", DEMO_BINGO, ShrimpConstants.GROUP_A},
            {"Hangman (c Program)", DEMO_HANGMAN, ShrimpConstants.GROUP_B},
            {"Newspaper Ontology (Protege)", DEMO_NEWSPAPER_PPRJ_LOCAL, ShrimpConstants.GROUP_C},
            {"Wines Ontology (Protege)", DEMO_WINES_PPRJ_LOCAL, ShrimpConstants.GROUP_C},
            {"Pizza Ontology (Protege OWL)", DEMO_PIZZA_OWL_LOCAL, ShrimpConstants.GROUP_C},
            {"Test OBO File", DEMO_OBO_LOCAL, ShrimpConstants.GROUP_D},
	};

	// Filename where properties for the application would be stored
	private static final String PROPERTIES_FILE_NAME = "Shrimp.properties";

	// identifiers to retrieve and set main frame location and size
	private static final String MAINFRAME_X = "mainFrame_x";
	private static final String MAINFRAME_Y = "mainFrame_y";
	private static final String MAINFRAME_WIDTH = "mainFrame_width";
	private static final String MAINFRAME_HEIGHT = "mainFrame_height";
	private static final int MIN_FRAME_SIZE = 150;

	// Custom desktop for this application
	protected ScrollDesktopPane desktop;

	// The bottom message panel
	protected JPanel messagePanel;

	protected Hashtable projectsToFrames;	// maps projects to internal desktop frames

	// currently active project
	protected StandAloneProject activeProject;

	/***
	 * Creates a StandAloneApplication.
	 * You must call one of the 3 methods {@link #initialize()}, {@link #initialize(JApplet)}, or
	 * {@link #initialize(JFrame)} to initialize Shrimp.
	 */
    public StandAloneApplication() {
		this(PROPERTIES_FILE_NAME, APPLICATION_TITLE, MAX_OPEN_PROJECTS);
    }

    public StandAloneApplication(String propertiesFile, String appName, int maxOpenProjects) {
    	super(propertiesFile, appName, maxOpenProjects);

		brandingImage = ResourceHandler.getResourceImage("shrimplogo_bevel.png");
		brandingIcon = ResourceHandler.getIcon("icon_shrimp.gif");
    }

    /**
     * @return the created main Shrimp frame.
     */
    public JFrame createParentFrame() {
		// initialize the main frame
		JFrame frame = new JFrame(buildProperties.toShortString());

		// Get the last known position and size of the main frame, or use defaults
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int filterWidth = OpenNodeFilterAdapter.FILTER_PALETTE_WIDTH;
		int taskbarHeight = 64;	// spacing for double height taskbar
		int defaultWidth = (int) (screenSize.getWidth() - filterWidth);
		int defaultHeight = (int) (screenSize.getHeight() - taskbarHeight);
		int x = Integer.parseInt(properties.getProperty(MAINFRAME_X, "" + filterWidth));
		int y = Integer.parseInt(properties.getProperty(MAINFRAME_Y, "0"));
		int width = Math.max(MIN_FRAME_SIZE, Integer.parseInt(properties.getProperty(MAINFRAME_WIDTH, "" + defaultWidth)));
		int height = Math.max(MIN_FRAME_SIZE, Integer.parseInt(properties.getProperty(MAINFRAME_HEIGHT, "" + defaultHeight)));

		// ensure that Shrimp doesn't appear off the right side of the screen
		if (!ShrimpUtils.isVisibleOnScreen(x, y)) {
			x = filterWidth;	// leave room for node/arc filter dialogs
			y = 0;
		}

		frame.setLocation(x, y);
		frame.setSize(width, height);
		//frame.setPreferredSize(new Dimension(width, height));	// @tag Shrimp.Java5.setPreferredSize

		// Make sure the application's close() method gets called
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				// close this application
				close();
			}
		});

		if (brandingIcon instanceof ImageIcon) {
			Image image = ((ImageIcon)brandingIcon).getImage();
			frame.setIconImage(image);
		}

		return frame;
    }

    /**
     * Call this method to initialize Shrimp (calls {@link #initialize()} method) and add the
     * UI components to the applet content pane.
     * @param applet
     */
    public void initialize(JApplet applet) {
    	// find the parent frame from the applet
    	Container parent = applet.getParent();
        while (parent != null) {
        	if (parent instanceof Frame) {
        		parentFrame = (Frame) parent;
        		break;
        	}
            parent = parent.getParent();
        }

    	initialize();

        applet.setJMenuBar(menuBar);
		applet.getContentPane().add(desktop, BorderLayout.CENTER);
		applet.getContentPane().add(messagePanel, BorderLayout.SOUTH);

		// send an application started event
		fireApplicationStartedEvent();
    }

    /**
     * Call this method initialize Shrimp and add the UI components to the frame content pane.
     * @param frame
     */
    public void initialize(JFrame frame) {
    	this.parentFrame = frame;

    	initialize();

		frame.setJMenuBar(menuBar);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(desktop, BorderLayout.CENTER);
		frame.getContentPane().add(messagePanel, BorderLayout.SOUTH);

		// send an application started event
		fireApplicationStartedEvent();
    }

    /**
     * Call this method if you don't want to associate any {@link Frame} or {@link JApplet}
     * with Shrimp.
     */
    public void initialize() {
    	this.projectsToFrames = new Hashtable();

		// initialize the message panel
		messagePanel = new JPanel();
		messagePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		messagePanel.setLayout(new BorderLayout());
		setMessagePanelMessage(" For help click on Help >> Manual");

		// initialize the custom desktop
		String version = getBuildInfo().toShortString();
		desktop = new ScrollDesktopPane(version, brandingImage, brandingIcon);
		//desktop.setBackground(ShrimpColorConstants.SHRIMP_BACKGROUND);

    	// create a progress dialog
		ProgressDialog.createProgressDialog(parentFrame, getName() + " Progress ...");

		// @tag Shrimp.createApplicationActions
		// create the menus for this application
		createApplicationActions();
    }

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication#setMessagePanelMessage(java.lang.String)
	 */
	public void setMessagePanelMessage(String message) {
		messagePanel.removeAll();
		messagePanel.add(new JLabel(message));
	}

	/**
	 * Returns the custom desktop this application is using.
	 * @return ScrollDesktopPane this application is using.
	 */
	public ScrollDesktopPane getDesktop() {
		return this.desktop;
	}

	/**
	 * Call this when you want this application to close.
	 */
	public void close() {
		// @tag Shrimp(ToolDialog) : this is done here to save the position and visibility of the tool dialogs
		fireApplicationDeactivatedEvent();

		super.close();

		desktop.setSelectedFrame(null);
		parentFrame.dispose();
	}


	public StandAloneProject getActiveProject() {
		return activeProject;
	}

	/**
	 * Sets the active project.
	 * @param project
	 */
	public void setActiveProject(StandAloneProject project) {
		if ((project == null) && (getProjectCount() > 0)) {
			project = (StandAloneProject) getFirstProject();
		}
		if (activeProject != project) {
			this.activeProject = project;
			fireProjectActivatedEvent(project);
		}
	}

	public void closeProject(ShrimpProject project) {
		if (project == activeProject) {
			activeProject = null;
		}
		DesktopInternalFrame frame = (DesktopInternalFrame) projectsToFrames.remove(project);

		super.closeProject(project);

		// dispose the frame it if necessary
		if (frame != null) {
			// doesn't fire a closing event which is what we want
			// the closing event calls closeProject which would cause a loop
			frame.dispose();
		}
	}

	public void saveProperties() {
		// save location and size of parent
		if (parentFrame != null) {
			properties.setProperty(MAINFRAME_X, "" + parentFrame.getLocation().x);
			properties.setProperty(MAINFRAME_Y, "" + parentFrame.getLocation().y);
			properties.setProperty(MAINFRAME_WIDTH, "" + Math.max(parentFrame.getSize().width, MIN_FRAME_SIZE));
			properties.setProperty(MAINFRAME_HEIGHT, "" + Math.max(parentFrame.getSize().height, MIN_FRAME_SIZE));
			// TEMP - typo in the old y property key
			properties.remove("mainFramep_y");
		}
		super.saveProperties();
	}

	/**
	 * Opens a new ShrimpProject.
	 * @param uri A URI that points to project data.
	 * @return the opened project or null
	 */
	public StandAloneProject openProject(URI uri) {
		return openProject(uri, ShrimpViewConfiguration.ALL_ON);
	}

	/**
	 * Opens a new ShrimpProject.
	 * @param uri A URI that points to project data.
	 * @param config the {@link ShrimpView} configuration
	 * @return the opened project or null
	 */
	public StandAloneProject openProject(URI uri, ShrimpViewConfiguration config) {
	    if (uri == null) {
	        return null;
	    }
	    StandAloneProject project = null;
		boolean firingEvents = actionManager.getFiringEvents();

		String uriStr = uri.toString();
		System.out.println("Opening project: " + uriStr);

	    waitCursor();
	    long start = System.currentTimeMillis();
	    //System.out.println("Loading " + uriStr);
		try {
			actionManager.setFiringEvents(false);

			project = new StandAloneProject(this, uri);
			addProject(project);	// fires project created event

			// create and add the DesktopInternalFrame
			addDesktopFrame(project, config);

			// happens after the ShrimpView has been created
			setActiveProject(project);
			
			// need to update the search strategies now, after the shrimp view has been created
			project.updateSearchStrategies();

			// @tag Shrimp.grouping
			// update node names from properties file
			try {
				ShrimpView view = (ShrimpView)this.activeProject.getTool(ShrimpProject.SHRIMP_VIEW);
				new RenameSelectedArtifactsAdapter(this.activeProject, view).updateNodeNames();
			} catch (ShrimpToolNotFoundException e) {
			}

			// maximize the frame if only one project is allowed
			if (getMaxOpenProjects() == 1) {
				desktop.maximizeFirstFrame();
			}

			addRecentlyOpenedFileName(uriStr);
		} catch (Exception e) {
			e.printStackTrace();
			removedRecentlyOpenedFileName(uriStr);

			// clean up any garbage
			System.runFinalization();
			System.gc();
		} finally {
		    defaultCursor();
			actionManager.setFiringEvents(firingEvents);
			System.out.println("Done loading project in " + (System.currentTimeMillis() - start) + " milliseconds.");
		}
		return project;
	}

	protected void addDesktopFrame(ShrimpProject project, ShrimpViewConfiguration config) {
		// for this new project, create a shrimp view (and a container to put it in)
		DesktopInternalFrame frame = getDesktop().addFrame(project.getTitle());
		projectsToFrames.put(project, frame);
		ShrimpViewFactory factory = new StandAloneShrimpViewFactory(this);
		factory.createShrimpView(project, frame, config);
	}

	/**
	 * Returns a unique title for a project if a project with similar title already exists.
	 * @param title String default title of the project.
	 * @return String unique title for the project.
	 */
	public String checkForDuplicateTitle(String title) {
		return getUniqueTitle(title, title, 1);
	}

	/**
	 * Returns a unique title by adding '(#)' to the end of existing duplicate title. Recursive method.
	 */
	private String getUniqueTitle(String originalTitle, String title, int copyNumber) {
		ShrimpProject[] projects = getProjects();
		for (int i = 0; i < projects.length; i++) {
			// if a similar title is found, change the title by adding a number at the end.
			if (title.equals(((StandAloneProject)projects[i]).getTitle())) {
				return getUniqueTitle(originalTitle, originalTitle + " (" + copyNumber + ")", copyNumber + 1);
			}
		}
		return title;
	}

	/**
	 * Adds actions specific to StandAlone Shrimp to the {@link ActionManager}.
	 */
	protected void createApplicationSpecificActions() {
		super.createApplicationSpecificActions();

		// File -> Open
		//Set the filters
		List fileFilters = new ArrayList(6);
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_PRJ, "Shrimp Project"));
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_PPRJ, "Protege Project"));
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_OWL, "Protege OWL Project"));
		fileFilters.add(new ShrimpFileFilter (StandAloneProject.EXT_GXL, "GXL File"));
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_XML, "XML File"));
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_XMI, "XMI File"));
		fileFilters.add(new ShrimpFileFilter(StandAloneProject.EXT_OBO, "OBO File"));
		String[] allFormats = { StandAloneProject.EXT_PRJ, StandAloneProject.EXT_PPRJ, StandAloneProject.EXT_OWL,
			StandAloneProject.EXT_GXL, StandAloneProject.EXT_XML, StandAloneProject.EXT_XMI, StandAloneProject.EXT_OBO };
		fileFilters.add(new ShrimpFileFilter(allFormats, "All Shrimp formats"));
		// map file extensions to icons
		Map extsToIconsMap = new HashMap(3);
		extsToIconsMap.put(StandAloneProject.EXT_PRJ, ResourceHandler.getIcon("icon_shrimp.gif"));
		extsToIconsMap.put(StandAloneProject.EXT_PPRJ, ResourceHandler.getIcon("icon_protege.gif"));
		extsToIconsMap.put(StandAloneProject.EXT_OWL, ResourceHandler.getIcon("icon_protege.gif"));
		extsToIconsMap.put(StandAloneProject.EXT_XML, ResourceHandler.getIcon("icon_xml.gif"));
		extsToIconsMap.put(StandAloneProject.EXT_OBO, ResourceHandler.getIcon("icon_obo.gif"));

		// File -> Open...
		ShrimpAction action = new FileOpenCommandAdapter(fileFilters, extsToIconsMap);
		action.setEnabled(true);
		addApplicationAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_A, 1);

		// File -> Open Recent
		action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_OPEN_RECENT, ResourceHandler.getIcon("icon_open.gif"));
		addApplicationAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_A, 2);

		//add actions for recently opened files
		createRecentlyOpenedFileActions();

		// File -> Save
//		action = new MenuAction(ShrimpConstants.ACTION_NAME_SAVE) {
//			public void actionPerformed(ActionEvent e) {
//				if (activeProject != null) {
//					activeProject.save();
//				}
//			}
//		};
//		addApplicationAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_B, 1);

		// File -> Save As
		//action = new MenuAction(ShrimpConstants.ACTION_NAME_SAVE_AS);	// not implemented yet
		//addApplicationAction(action, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_B, 2);

		// File -> Exit
		DefaultShrimpAction exitAction = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_EXIT) {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		};
		exitAction.setMnemonic('x');
		addApplicationAction(exitAction, ShrimpConstants.MENU_FILE, ShrimpConstants.GROUP_E, 3);

		// Window Menu
		addApplicationAction(new DefaultShrimpAction(ShrimpConstants.MENU_WINDOW), "", "", 8);

		// Window -> Cascade
		action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_CASCADE) {
			public void actionPerformed(ActionEvent e) {
				getDesktop().cascadeAllFrames();
			}
		};
		addApplicationAction(action, ShrimpConstants.MENU_WINDOW, ShrimpConstants.GROUP_A, 1);

		// Window -> Tile
		action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_TILE) {
			public void actionPerformed(ActionEvent e) {
				getDesktop().tileAllFrames();
			}
		};
		addApplicationAction(action, ShrimpConstants.MENU_WINDOW, ShrimpConstants.GROUP_A, 2);

		// Window -> Close all
		action = new DefaultShrimpAction(ShrimpConstants.ACTION_NAME_CLOSE_ALL) {
			public void actionPerformed(ActionEvent e) {
				getDesktop().closeAllFrames();
			}
		};
		addApplicationAction(action, ShrimpConstants.MENU_WINDOW, ShrimpConstants.GROUP_A, 3);

		// Demo Menu
		addApplicationAction(new DefaultShrimpAction(ShrimpConstants.MENU_DEMO), "", "", 7);

		String[][] demos = DEMOS;
		for (int i = 0; i < demos.length; i++) {
            String demoTitle = demos[i][0];
            String demoFilename = demos[i][1];
            String demoGroup = demos[i][2];
            final URI demoURI = ResourceHandler.getFileURI(demoFilename);
		    if (demoURI != null) {
	    		action = new DefaultShrimpAction(demoTitle) {
	    		    public void actionPerformed(ActionEvent e) {
						openProject(demoURI);
	    		    }
	    		};
	    		addApplicationAction(action, ShrimpConstants.MENU_DEMO, demoGroup, (i+1));
		    } else {
		        System.out.println("couldn't find demo file: " + demoFilename);
		    }
        }

		//Help -> Online Manual
		action = new ShowInBrowserAction(ShrimpConstants.ACTION_NAME_ONLINE_MANUAL, ShrimpConstants.SHRIMP_MANUAL_WEBSITE, ResourceHandler.getIcon("icon_help.gif"));
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 1);

		//Help -> Website
		action = new ShowInBrowserAction(ShrimpConstants.ACTION_NAME_SHRIMP_WEBSITE, ShrimpConstants.SHRIMP_WEBSITE, ResourceHandler.getIcon("icon_shrimp.gif"));
		addApplicationAction(action, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 2);

		// @tag Shrimp.ParseWebPage
		//action = new ParseWebPageAction();
		//addApplicationAction(action, "Test", ShrimpConstants.GROUP_A, 0);

	}

	/** Returns the list of names of files that have been recently opened **/
	private List getRecentlyOpenedFiles() {
		if (recentlyOpenedFiles == null) {
			recentlyOpenedFiles = new ArrayList(MAX_RECENT_FILES);
			String fileNamesStr = properties.getProperty(RECENT_FILES_PREF_KEY);
			if (fileNamesStr != null) {
				StringTokenizer st = new StringTokenizer(fileNamesStr, ";");
				while (st.hasMoreTokens()) {
					try {
						URI uri = new URI(st.nextToken());
						if ("file".equalsIgnoreCase(uri.getScheme())) {
							File file = new File(uri);
							if (!file.exists()) {
								continue;
							}
						}
						recentlyOpenedFiles.add(uri.toString());
					} catch (Exception e) {
					}
				}
			}
		}
		return recentlyOpenedFiles;
	}

	/** Returns the list of names of files that have been recently opened **/
	private void storeRecentlyOpenedFiles() {
		if (recentlyOpenedFiles !=  null) {
			String s = "";
			for (Iterator iter = recentlyOpenedFiles.iterator(); iter.hasNext();) {
				String file = (String) iter.next();
				s += file;
				if (iter.hasNext()) {
					s += ";";
				}
			}
			properties.setProperty(RECENT_FILES_PREF_KEY, s);
		}
	}

	/**
	 * Prepends a name to the list of names of files that have been recently opened
	 * The list only holds MAX_RECENT_FILES file names; the oldest one will be thrown away if necessary.
	 **/
	protected void addRecentlyOpenedFileName(String fileName) {
		List files = getRecentlyOpenedFiles();
		// if this file is already in the list, pull it out of the list
		// and add it to the beginning of the list
		if (files.contains(fileName)) {
			files.remove(fileName);
		}

		files.add (0, fileName);
		if (files.size() > MAX_RECENT_FILES) {
			files = files.subList(0, MAX_RECENT_FILES - 1);
		}
		refreshRecentlyOpenFileActions();

	}

	private void refreshRecentlyOpenFileActions() {
		// update the action manager
		removeRecentlyOpenedFileActions();
		storeRecentlyOpenedFiles();
		createRecentlyOpenedFileActions();
	}

	/**
	 * Removes a name from the list of names of files that have been recently opened
	 **/
	protected void removedRecentlyOpenedFileName(String fileName) {
		List files = getRecentlyOpenedFiles();
		files.remove(fileName);

		refreshRecentlyOpenFileActions();
	}

	/**
	 * Removes the recently opened file actions from the action manager
	 **/
	private void removeRecentlyOpenedFileActions() {
		List files = getRecentlyOpenedFiles();
		for (Iterator iter = files.iterator(); iter.hasNext();) {
			String fileName = (String) iter.next();
			actionManager.removeAction(fileName, ShrimpConstants.MENU_FILE_OPEN_RECENT, ActionManager.IGNORE_PARENT);
		}
	}

	/**
	 * Adds the recently opened file actions to the action manager
	 **/
	protected void createRecentlyOpenedFileActions() {
		List fileNames = getRecentlyOpenedFiles();
		int i = 0;
		for (Iterator iter = fileNames.iterator(); iter.hasNext();) {
			final String fileName = (String) iter.next();
			Action action = new AbstractAction(fileName) {
				public void actionPerformed(ActionEvent e) {
				    try {
                        URI uri = new URI(fileName);
                        openProject (uri);
                    } catch (URISyntaxException e1) {
                    	removedRecentlyOpenedFileName(fileName);
                    }
				}
			};
			actionManager.addAction(action, ShrimpConstants.MENU_FILE_OPEN_RECENT, "", i++);
		}
	}

	public void createMenus() {
		super.createMenus();
		 // @tag Shrimp.menu.shortcuts : adds shortcut keys (Alt) to the main menus (StandAlone Shrimp only)
		addMnemonics();
	}

	/**
	 * Adds shortcut keys (mnemonics) to all the main menus (File, Edit, Help etc) and the submenus
	 * of each main menu.  Only goes one level down.
	 * @tag Shrimp.menu.shortcuts : adds shortcut keys (Alt) to the main menus
	 */
	private void addMnemonics() {
		// maps the shortcut mnemonic keys used so far to their MenuAction
		HashMap keysToActions = new HashMap(menuBar.getMenuCount()+2);

		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			if (menu != null) {
				addMnemonic(menu, keysToActions);

				HashMap subKeys = new HashMap(menu.getMenuComponentCount()+2);
				for (int j = 0; j < menu.getMenuComponentCount(); j++) {
					Component c = menu.getMenuComponent(j);
					if (c instanceof JMenuItem) {
						JMenuItem menuItem = (JMenuItem) c;
						addMnemonic(menuItem, subKeys);
					}
				}
			}
		}
	}

	/**
	 * Adds a mnemonic character for the given menu. It first checks if the action has a predefined mnemonic,
	 * if so it uses that. Otherwise is calls {@link StandAloneApplication#addMnemonicFromText(JMenuItem, HashMap)}
	 * which uses as a mnemonic the first available letter.  If all the letters in the text are
	 * already used then no shortcut mnemonic will be used.
	 * @param menu the menu to add a shortcut to
	 * @param keysToActions the map of existing shortcut keys to the JMenuItem
	 */
	private void addMnemonic(JMenuItem menu, HashMap keysToActions) {
		boolean addedPredefined = false;
		// check for a predefined mnemonic
		if (menu.getAction() instanceof DefaultShrimpAction) {
			DefaultShrimpAction action = (DefaultShrimpAction) menu.getAction();
			if (action.hasMnemonic()) {
				char c = action.getMnemonic();
				String key = "" + Character.toLowerCase(c);

				// menu that was previously mapped to this mnemonic
				JMenuItem previous = (JMenuItem) keysToActions.get(key);

				keysToActions.put(key, menu);
				menu.setMnemonic(c);
				// clear the previous mnemonic
				if (previous != null) {
					previous.setMnemonic(0);
					// find a new mnemonic for this
					addMnemonicFromText(previous, keysToActions);
				}
				addedPredefined = true;
			}
		}
		// add the mnemonic based on the menu text
		if (!addedPredefined) {
			addMnemonicFromText(menu, keysToActions);
		}
	}

	/**
	 * Looks at the menu text and finds the first unused letter and uses
	 * that as the mnemonic.
	 * @param menu
	 * @param keysToActions
	 */
	private void addMnemonicFromText(JMenuItem menu, HashMap keysToActions) {
		// use the menu text to find the first available letter
		String text = menu.getText();
		int length = text.length();
		for (int i = 0; i < length; i++) {
			char c = text.charAt(i);
			String key = "" + Character.toLowerCase(c);
			if (Character.isLetter(c) && !keysToActions.containsKey(key)) {
				keysToActions.put(key, menu);
				// only works with letters
				menu.setMnemonic(c);
				break;
			}
		}
	}

}