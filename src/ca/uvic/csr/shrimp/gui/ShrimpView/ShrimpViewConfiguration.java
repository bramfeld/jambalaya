/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

/**
 * Contains a set of parameters to configure a SHriMP view
 * @author Chris Bennett
 */
public class ShrimpViewConfiguration {

	public static ShrimpViewConfiguration ALL_ON =
		new ShrimpViewConfiguration(true, true, true, true, true, true, true, true);

	/**
	 * All options off
	 */
	public static ShrimpViewConfiguration ALL_OFF =
		new ShrimpViewConfiguration(false, false, false, false, false, false, false, false);

	/**
	 * Default constructor - all fields are set to true.
	 */
	public ShrimpViewConfiguration() {
		this(true, true, true, true, true, true, true, true);
	}

	/**
	 * Convenience constructor to set all fields
	 * @param tryLastView
	 */
	public ShrimpViewConfiguration(boolean showMenuBar, boolean showMessagePanel,
			boolean tryLastView, boolean showQuickSearchPanel,
			boolean createToolBar, boolean enableUserCOntrols,
			boolean showQuickViewPanel, boolean showModePanel) {
		this.showMenuBar = showMenuBar;
		this.showMessagePanel = showMessagePanel;
		this.tryLastView = tryLastView;
		this.showQuickSearchPanel = showQuickSearchPanel;
		this.createToolBar = createToolBar;
		this.enableUserControls = enableUserCOntrols;
		this.showQuickViewPanel = showQuickViewPanel;
		this.showModePanel = showModePanel;
	}

	// Application UI
	public boolean showMenuBar;
	public boolean showMessagePanel;

	// Project UI
	public boolean tryLastView;
	public boolean showQuickSearchPanel;
	public boolean createToolBar;
	public boolean enableUserControls;
	public boolean showQuickViewPanel;
	public boolean showModePanel;
}
