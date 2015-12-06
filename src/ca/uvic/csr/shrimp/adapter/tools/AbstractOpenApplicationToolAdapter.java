/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationAdapter;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplicationEvent;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProjectEvent;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Abstract class implementing default way to open a ShrimpTool.
 * Contains the common stuff for all tool adapters.
 *
 * @author Nasir Rather, Chris Callendar
 */
public abstract class AbstractOpenApplicationToolAdapter extends CheckBoxAction {

	private static final int DOCK_MIN = -10004;
	private static final int DOCK_MAX = -10000;

	/** Attempts to dock this dialog along the inside left hand side its parent window. */
	public static final int DOCK_LEFT_INSIDE = -10000;
	/** Attempts to dock this dialog outside and to the left of its parent window. */
	public static final int DOCK_LEFT_OUTSIDE = -10001;
	/** Attempts to dock this dialog along the inside right hand side its parent window. */
	public static final int DOCK_RIGHT_INSIDE = -10002;
	/** Attempts to dock this dialog outside and to the right of its parent window. */
	public static final int DOCK_RIGHT_OUTSIDE = -10003;

	/** Attempts to align the top of this dialog with the top of its parent window. */
	public static final int DOCK_TOP_INSIDE = DOCK_LEFT_INSIDE;
	/** Attempts to position this dialog above its parent window. */
	public static final int DOCK_TOP_OUTSIDE = DOCK_LEFT_OUTSIDE;
	/** Attempts to align the bottom of this dialog with the bottom of its parent window. */
	public static final int DOCK_BOTTOM_INSIDE = DOCK_RIGHT_INSIDE;
	/** Attempts to position this dialog below its parent window. */
	public static final int DOCK_BOTTOM_OUTSIDE = DOCK_RIGHT_OUTSIDE;
	/** Docks this dialog in the center (either horizontally or vertically) of its parent window. */
	public static final int DOCK_CENTER = -10004;

	private Rectangle defaultBounds;
	private String toolName;
	private JDialog toolContainer;
	private ToolApplicationListener listener;

	public AbstractOpenApplicationToolAdapter(String toolName, Icon icon, Rectangle defaultBounds) {
		super(toolName, icon, (ShrimpProject) null);
		this.toolName = toolName;
		this.defaultBounds = defaultBounds;
		this.listener = new ToolApplicationListener();

		// listen for application events such as project activated, project closed etc
		// when a project is activated it is set as the project for this adapter
		ApplicationAccessor.getApplication().addApplicationListener(listener);
	}

	private Frame getShrimpFrame() {
		return ApplicationAccessor.getParentFrame();
	}

	protected boolean getDefaultVisibility() {
		return false;
	}

	/**
	 * @return the 16x16 image to be displayed in the tool window.
	 */
	protected Image getToolImage() {
		Icon icon = getIcon();
		if (icon instanceof ImageIcon) {
			return ((ImageIcon) icon).getImage();
		}
		System.err.println("Warning - no tool icon found for " + toolName);
		return null;
	}

	public void setProject(ShrimpProject project) {
		if ((project != null) && (project == getProject())) {
			return;
		}

		super.setProject(project);

		// if the dialog is already visible then update its contents
		if (project != null) {
			// case #1: first time showing, saved visibility = true -> create dialog and display contents
			if ((toolContainer == null) && getSavedVisibility()) {
				createContainer();
			}
			// case #2: first time showing, saved visibility = false -> do nothing
			else if ((toolContainer == null) && !getSavedVisibility()) {
				// do nothing
			}

			// case #3: container already created -> update container contents
			if (toolContainer != null) {
				if (tool == null) {
					// keep the dialog visible as it was
					createUpdateAddTool(toolContainer.isVisible());
				} else {
					tool.setProject(getProject());
					updateContainerContents(false);
				}
			}
		} else {
			//  no project - just update the container
			updateContainerContents(true);
		}
	}

	protected boolean isContainerVisible() {
		return ((toolContainer != null) && toolContainer.isVisible());
	}

	/**
	 * @see ca.uvic.csr.shrimp.usercontrols.UserAction#startAction()
	 */
	public final void startAction() {
		boolean run = beforeActionHasRun();
		if (run) {
			// case #1: container does not exist -> Show it
			if (toolContainer == null) {
				createContainer();
				createUpdateAddTool(true);
			}
			// case #2: container exists and is not visible -> Show it  (toggle checkbox action)
			else if (!toolContainer.isVisible()) {
				// create (if it doesn't already exist) and update the dialog
				createUpdateAddTool(true);
			}
			// case #3: container exists and is visible -> Hide it (toggle checkbox action)
			else {
				// container is already visible, so hide it (unchecks this action)
				toolContainer.setVisible(false);
			}

			afterActionHasRun();
		}
	}

	/**
	 * This gets called before {@link AbstractOpenApplicationToolAdapter#startAction()} is called and returns true.
	 * Subclasses can override this method to perform operations before the action starts.
	 * @return true if the action should run
	 */
	protected boolean beforeActionHasRun() {
		return true;
	}

	/**
	 * Subclasses can override this method to perform operations after the action has been run.
	 * This does nothing.
	 */
	protected void afterActionHasRun() {
		// do nothing
	}

	// Creates the container.
	private void createContainer() {
		toolContainer = new JDialog(getShrimpFrame(), toolName, false);
		toolContainer.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		toolContainer.setContentPane(new JPanel(new BorderLayout()));
		toolContainer.addComponentListener(new ComponentAdapter() {
			public void componentHidden(ComponentEvent e) {
				setChecked(false);
				saveBounds();
				if (tool != null) {
					updateOutputText(false);
					tool.disposeTool();
					// this also removes the tool from the project
					tool.setProject(null);
					ApplicationAccessor.getApplication().removeTool(toolName);
					tool = null;
				}
			}
			public void componentShown(ComponentEvent e) {
				updateOutputText(true);
				setChecked(true);
				positionContainer();
			}

			private void updateOutputText(boolean shown) {
				if ((getProject() != null) && (tool != null)) {
					try {
						ShrimpView shrimpView = (ShrimpView) getProject().getTool(ShrimpProject.SHRIMP_VIEW);
						shrimpView.setOutputText((shown ? "Opening " : "Closing ") + tool.getName());
						shrimpView.clearOutputText(1000);
					} catch (ShrimpToolNotFoundException ignore) {}
				}
			}
		});
		positionContainer();
	}

	/**
	 * Implement this method to instantiate a specific tool
	 */
	protected abstract void createTool();

	/**
	 * Override this method to provide some initialization for a specific tool
	 */
	protected void initTool() {
        // by default, do nothing
	}

	/**
	 * Creates the {@link ShrimpTool} if it doesn't already exist in the application's list of tools.
	 * Then the container is populated with the tool's contents.
	 * @param visible if the container should be visible
	 */
	private void createUpdateAddTool(boolean visible) {
		// Create the tool if it doesn't already exist in the application
		try {
			tool = ApplicationAccessor.getApplication().getTool(toolName);
		} catch (ShrimpToolNotFoundException e) {
			if (tool == null) {
				createTool();
				initTool();
			}
			if (tool != null) {
				ApplicationAccessor.getApplication().addTool(toolName, tool);
			} else {
				ApplicationAccessor.getApplication().removeTool(toolName);
			}
		}
		updateContainerContents(true);
		toolContainer.setVisible(visible);
	}

	private boolean isDock(int z) {
		return ((z >= DOCK_MIN) && (z <= DOCK_MAX));
	}

	/**
	 * Positions/resizes the container according to the previously set positions/dimensions.
	 */
	private void positionContainer() {
		if (toolContainer != null) {
			Rectangle bounds = getSavedBounds();
			int x = bounds.x;
			int y = bounds.y;
			int w = Math.max(10, bounds.width);
			int h = Math.max(10, bounds.height);

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			// if x or y is < 10000 this indicates that the dialog should be docked
			if (isDock(x)) {
				x = dockX(x, screenSize.width, w);
			}
			if (isDock(y)) {
				y = dockY(y, screenSize.height, h);
			}

			// special case if this dialog is hiding the parent's titlebar - shift it down
			Frame shrimpFrame = getShrimpFrame();
			int parentY = (shrimpFrame != null ? shrimpFrame.getY() : 0);
			int parentX = (shrimpFrame != null ? shrimpFrame.getX() : 0);
			int parentW = (shrimpFrame != null ? shrimpFrame.getWidth() : 0);
			if ((y == parentY) && (x == (parentX + parentW - w))) {
				y += 30;
			}

			if (x > screenSize.width) {
				x = screenSize.width - w;
			}
			if (y > screenSize.height) {
				y = screenSize.height - h;
			}
			// last check - make sure this point is displayable on the screen
			boolean ok = ShrimpUtils.isVisibleOnScreen(x, y);
			if (!ok) {
				x = 0;
				y = 0;
			}

			//toolContainer.setPreferredSize(new Dimension(w, h));	// @tag Shrimp.Java5.setPreferredSize
			toolContainer.setBounds(x, y, w, h);
		}
	}

	protected void updateContainerContents() {
		updateContainerContents(true);
	}

	/**
	 * The dialog's contents are removed and the tool's gui is added.  Then the tool is refreshed.
	 * The dialog's title is also updated, and the dialog is revalidated and repainted.
	 */
	protected void updateContainerContents(boolean refreshTool) {
		if (toolContainer != null) {
			if (toolContainer.getContentPane().getComponentCount() == 1) {
				toolContainer.getContentPane().remove(0);
			}
			if (tool != null) {
				toolContainer.getContentPane().add(tool.getGUI(), BorderLayout.CENTER);
				if (refreshTool) {
					tool.refresh();
				}
			}
			updateTitle();
			toolContainer.invalidate();
			toolContainer.validate();
			toolContainer.repaint();
		}
	}

	private void updateTitle() {
		String title = toolName;
		if (getProject() != null) {
			title += " - " + getProject().getTitle();
		}
		toolContainer.setTitle(title);
	}

	/**
	 *  Save the size, location and visibility of the container to the application properties.
	 */
	private void saveBounds() {
		if (toolContainer != null) {
			Properties properties = ApplicationAccessor.getProperties();
			properties.setProperty(toolName + ".x", "" + toolContainer.getX());
			properties.setProperty(toolName + ".y", "" + toolContainer.getY());
			properties.setProperty(toolName + ".width", "" + toolContainer.getWidth());
			properties.setProperty(toolName + ".height", "" + toolContainer.getHeight());
		}
	}

	/**
	 * Gets the saved bounds of the tool container from the application properties.
	 */
	private Rectangle getSavedBounds() {
		Properties properties = ApplicationAccessor.getProperties();
		defaultBounds.x = Integer.parseInt(properties.getProperty(toolName + ".x", "" + defaultBounds.x));
		defaultBounds.y = Integer.parseInt(properties.getProperty(toolName + ".y", "" + defaultBounds.y));
		defaultBounds.width = Integer.parseInt(properties.getProperty(toolName + ".width", "" + defaultBounds.width));
		defaultBounds.height = Integer.parseInt(properties.getProperty(toolName + ".height", "" + defaultBounds.height));
		return defaultBounds;
	}

	/**
	 * Saves the visibility of the tool container to the application properties.
	 */
	private void saveVisibility() {
		Properties properties = ApplicationAccessor.getProperties();
		properties.setProperty(toolName + ".visibility", "" + isContainerVisible());
	}

	/**
	 * Returns the visibility of the container from the application properties.
	 * @return boolean visibility.
	 */
	private boolean getSavedVisibility() {
		Properties properties = ApplicationAccessor.getProperties();
		return Boolean.valueOf(properties.getProperty(toolName + ".visibility", "" + getDefaultVisibility())).booleanValue();
	}

	/**
	 *Attempts find the x location to dock the dialog beside the parent dialog.
	 */
	private int dockX(int initialX, int screenWidth, int dlgWidth) {
		int x = initialX;
		Frame frame = getShrimpFrame();
		int parentX = (frame != null ? frame.getX() : 0);
		int parentWidth = (frame != null ? frame.getWidth() : screenWidth);
		if (x == DOCK_LEFT_OUTSIDE) {
			x = parentX - dlgWidth;
			if (x < 0) {
				x = DOCK_RIGHT_OUTSIDE;
			}
		}
		if (x == DOCK_RIGHT_OUTSIDE) {
			x = parentX + parentWidth;
			if ((x < 0) || ((x + dlgWidth) > screenWidth)) {
				x = DOCK_LEFT_INSIDE;
			}
		}
		if (x == DOCK_LEFT_INSIDE) {
			x = Math.max(0, parentX);
		}
		if (x == DOCK_RIGHT_INSIDE) {
			x = Math.max(0, parentX + parentWidth - dlgWidth);
		}
		if (x == DOCK_CENTER) {
			x = Math.max(0, parentX + (parentWidth / 2) - (dlgWidth / 2));
		}

		// shift the docking position left or right
		x += getHorizontalDockOffset();
		return x;
	}

	/**
	 * Attempts find the y location to dock the dialog above or below the parent dialog.
	 */
	private int dockY(int initialY, int screenHeight, int dlgHeight) {
		int y = initialY;
		int parentY = (getShrimpFrame() != null ? getShrimpFrame().getY() : 0);
		int parentHeight = (getShrimpFrame() != null ? getShrimpFrame().getHeight() : screenHeight);
		if (y == DOCK_TOP_OUTSIDE) {
			y = parentY - dlgHeight;
			if (y < 0) {
				y = DOCK_TOP_INSIDE;
			}
		}
		if (y == DOCK_BOTTOM_OUTSIDE) {
			y = parentY + parentHeight;
			if ((y < 0) || ((y + dlgHeight) > screenHeight)) {
				y = DOCK_BOTTOM_INSIDE;
			}
		}
		if (y == DOCK_TOP_INSIDE) {
			y = Math.max(0, parentY);
		}
		if (y == DOCK_BOTTOM_INSIDE) {
			y = Math.max(0, parentY + parentHeight - dlgHeight);
		}
		if (y == DOCK_CENTER) {
			y = Math.max(0, parentY + (parentHeight / 2) - (dlgHeight / 2));
		}
		// see if this tool container should be shifted up or down
		y += getVerticalDockOffset();
		return y;
	}

	/**
	 * Returns the number of pixels to shift the tool dialog up or down
	 * relative to the docking position.
	 * This is only used by the arc filter palette to position it below the node filter palette.
	 */
	protected int getVerticalDockOffset() {
		return 0;
	}

	/**
	 * Returns the number of pixels to shift the tool dialog left or right
	 * relative to the docking position. This is not used currently by any tools.
	 */
	protected int getHorizontalDockOffset() {
		return 0;
	}

	/**
	 * Listens for application events and updates the tool container and its contents as necessary.
	 * @author Chris Callendar
	 */
	private class ToolApplicationListener extends ShrimpApplicationAdapter {

		/**
		 * When the application is started, show tool if it was visible when application
		 * closed last session.
		 */
		public void applicationStarted(ShrimpApplicationEvent event) {
			if (getSavedVisibility()) {
				createContainer();
				createUpdateAddTool(true);
			}
		}

		/**
		 * When the application is activated, show tool if it was visible when the
		 * application was deactivated.
		 */
		public void applicationActivated(ShrimpApplicationEvent e) {
			if (toolContainer != null) {
				// check the settings that were stored when the application was deactivated
				if (getSavedVisibility()) {
					positionContainer();
					createUpdateAddTool(true);
				}
			}
		}

		/**
		 * When the application is deactivated (eg. in Protege, when another tab other than the
		 * Jambalaya tab is showing) hide tool, if showing, and save its settings.
		 */
		public void applicationDeactivated(ShrimpApplicationEvent e) {
			if (toolContainer != null) {
				saveBounds();
				// @tag Shrimp.ToolDialog: save visibility of tool dialog
				saveVisibility();
				toolContainer.setVisible(false);
			}
		}

		/**
		 * Just before the application closes, save the visibility and position of the tool
		 * and dispose the tool and tool container.
		 */
		public void applicationClosing(ShrimpApplicationEvent event) {
			ApplicationAccessor.getApplication().removeApplicationListener(this);
			if (toolContainer != null) {
				saveBounds();
				// @tag Shrimp.ToolDialog: don't save visibility - instead it is done on deactivation
				//saveVisibility();
				toolContainer.setVisible(false);	// this will dispose the tool
				tool = null;
				toolContainer = null;
			}
		}

		public void projectActivated(ShrimpProjectEvent event) {
			setProject(event.getProject());
		}

		public void projectCreated(ShrimpProjectEvent event) {
			// Shrimp View is not created yet, wait for the project activated event
			//setProject(event.getProject());
		}

		public void projectClosed(ShrimpProjectEvent event) {
			if (getProject() == event.getProject()) {
				setProject(null);
			}
		}

	}

}
