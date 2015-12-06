/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 *
 * Created on Nov 30, 2002
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.awt.Dimension;
import java.awt.Insets;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.layout.ForceDirectedLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.OrthogonalLayout;
import ca.uvic.csr.shrimp.DisplayBean.layout.SugiyamaLayout;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewCprelsChangedEvent;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewListener;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewMouseModeChangedEvent;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Configures the {@link JToolBar} shown in {@link ShrimpView}.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class ToolBarConfigurator implements ShrimpViewListener {

    protected static final int DEFAULT_SEPARATOR_SIZE = 15;

	private ActionManager actionManager;
	private ShrimpView shrimpView;

	private ButtonGroup mouseModebuttonGroup;
	private JToggleButton selectToggleButton;
	private JToggleButton zoomInToggleButton;
	private JToggleButton zoomOutToggleButton;

	public ToolBarConfigurator(ShrimpView shrimpView, ActionManager actionManager) {
		this.shrimpView = shrimpView;
		this.actionManager = actionManager;
	}

	public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event) {
	}

	public void shrimpViewMouseModeChanged(ShrimpViewMouseModeChangedEvent event) {
		String mode = event.getMouseMode();
		if (mode.equals(DisplayConstants.MOUSE_MODE_SELECT)) {
			mouseModebuttonGroup.setSelected(selectToggleButton.getModel(), true);
		} else if (mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_IN)) {
			mouseModebuttonGroup.setSelected(zoomInToggleButton.getModel(), true);
		}
		else if (mode.equals(DisplayConstants.MOUSE_MODE_ZOOM_OUT)) {
			mouseModebuttonGroup.setSelected(zoomOutToggleButton.getModel(), true);
		}
	}

	private JSeparator getSeparator() {
		// Passing in a dimension in the constructor makes the separator horizontal!
		// It works okay in Swing, but in SWT it looks bad
		JToolBar.Separator sep = new JToolBar.Separator();
		sep.setOrientation(JSeparator.VERTICAL);
		sep.setPreferredSize(new Dimension(DEFAULT_SEPARATOR_SIZE, DEFAULT_SEPARATOR_SIZE));
		return sep;
	}

	private JToggleButton getToggleButton(Action action) {
		JToggleButton button = new JToggleButton(action);
		button.setText(null);
		button.setToolTipText((String) action.getValue(Action.NAME));
		button.setMargin(new Insets(1,1,1,1));

		return button;
	}

	private JButton getButton(Action action) {
		JButton button = new JButton(action);
		button.setText(null);
		button.setToolTipText((String) action.getValue(Action.NAME));
		button.setMargin(new Insets(0,0,1,1));

		return button;
	}

	private ListButton getListButton(Icon icon) {
		return new ListButton(icon);
	}

	private void addButton(String actionName, String path, JToolBar toolBar) {
		Action action = actionManager.getAction(actionName, path);
		if (action != null) {
			JButton button = getButton(action);
			toolBar.add(button);
		}
	}

	private void addToListButton(String actionName, String path, boolean selected, ListButton listButton) {
		Action action = actionManager.getAction(actionName, path);
		listButton.add(action, selected);
	}

	public JToolBar createToolBar(Collection extraActions) {
		JToolBar toolbar = new JToolBar();

		Action actionSelect = actionManager.getAction(ShrimpConstants.ACTION_NAME_SELECT_TOOL, ShrimpConstants.MENU_NAVIGATE);
		Action actionZoomIn = actionManager.getAction(ShrimpConstants.ACTION_NAME_ZOOM_IN_TOOL, ShrimpConstants.MENU_NAVIGATE);
		Action actionZoomOut = actionManager.getAction(ShrimpConstants.ACTION_NAME_ZOOM_OUT_TOOL, ShrimpConstants.MENU_NAVIGATE);

		if ((actionSelect != null) && (actionZoomIn != null) && (actionZoomOut != null)) {
			mouseModebuttonGroup = new ButtonGroup();
			selectToggleButton = getToggleButton(actionSelect);
			toolbar.add(selectToggleButton);
			mouseModebuttonGroup.add(selectToggleButton);

			zoomInToggleButton = getToggleButton(actionZoomIn);
			toolbar.add(zoomInToggleButton);
			mouseModebuttonGroup.add(zoomInToggleButton);

			zoomOutToggleButton = getToggleButton(actionZoomOut);
			toolbar.add(zoomOutToggleButton);
			mouseModebuttonGroup.add(zoomOutToggleButton);

			shrimpView.addShrimpViewListener(this);	// won't add duplicates
			toolbar.add(getSeparator());
		}

		addButton(ShrimpConstants.ACTION_NAME_BACK, ShrimpConstants.MENU_NAVIGATE, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_FORWARD, ShrimpConstants.MENU_NAVIGATE, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_HOME, ShrimpConstants.MENU_NAVIGATE, toolbar);
        addButton(ShrimpConstants.ACTION_NAME_REFRESH, ShrimpConstants.MENU_EDIT, toolbar);

		toolbar.add(getSeparator());

		addButton(ShrimpConstants.TOOL_SEARCH, ShrimpConstants.MENU_EDIT, toolbar);

		toolbar.add(getSeparator());

		ListButton listButton = getListButton(ResourceHandler.getIcon("icon_grid_layout.gif"));
		listButton.setToolTipText(ShrimpConstants.ACTION_NAME_GRID_LAYOUT);
		addToListButton(ShrimpConstants.ACTION_NAME_GRID_ALPHABETICAL, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN_GRID, true, listButton);
		addToListButton(ShrimpConstants.ACTION_NAME_GRID_BY_CHILDREN, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN_GRID, false, listButton);
		addToListButton(ShrimpConstants.ACTION_NAME_GRID_BY_RELATIONSHIPS, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN_GRID, false, listButton);
		addToListButton(ShrimpConstants.ACTION_NAME_GRID_BY_TYPE, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN_GRID, false, listButton);
		addToListButton(ShrimpConstants.ACTION_NAME_GRID_BY_ATTRIBUTE, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN_GRID, false, listButton);
		if (listButton.getActionCount() > 0) {
			toolbar.add(listButton);
		}

		addButton(ShrimpConstants.ACTION_NAME_RADIAL_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_SPRING_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);

		if (OrthogonalLayout.isLoaded()) {
			addButton(ShrimpConstants.ACTION_NAME_HIERACHICAL_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
			addButton(ShrimpConstants.ACTION_NAME_ORTHOGONAL_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		}

		// only add the force directed if the prefuse jar file exists in the classpath
		if (ForceDirectedLayout.isPrefuseInstalled()) {
			addButton(ShrimpConstants.ACTION_NAME_FORCE_DIRECTED_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		}

		// @tag Shrimp.sugiyama
		if (SugiyamaLayout.isInstalled()) {
			addButton(ShrimpConstants.ACTION_NAME_SUGIYAMA_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		}

		//addButton(ShrimpConstants.ACTION_NAME_TOUCHGRAPH_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolBar);

		addButton(ShrimpConstants.ACTION_NAME_TREE_LAYOUT_VERTICAL, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_TREE_LAYOUT_HORIZONTAL, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);
		// hide for now to unclutter the toolbar
		//addButton(ShrimpConstants.ACTION_NAME_TREEMAP_LAYOUT, ShrimpConstants.MENU_NODE_ARRANGE_CHILDREN, toolbar);

		toolbar.add(getSeparator());

		addButton(ShrimpConstants.ACTION_NAME_NODE_FILTER, ShrimpConstants.MENU_TOOLS, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_ARC_FILTER, ShrimpConstants.MENU_TOOLS, toolbar);
		// hide for now to unlutter toolbar
		//addButton(ShrimpConstants.TOOL_FILTERS, ShrimpConstants.MENU_TOOLS, toolbar);
//		addButton(ShrimpConstants.ACTION_NAME_ATTRIBUTES_PANEL, ShrimpConstants.MENU_TOOLS, toolbar);
		addButton(ShrimpConstants.ACTION_NAME_FILMSTRIP, ShrimpConstants.MENU_TOOLS, toolbar);
		//addButton(ShrimpConstants.TOOL_SCRIPTING, ShrimpConstants.MENU_TOOLS, toolbar);
//		addButton(ShrimpConstants.ACTION_NAME_HIERARCHICAL_VIEW, ShrimpConstants.MENU_TOOLS, toolbar);
		addButton(ShrimpConstants.TOOL_QUERY_VIEW, ShrimpConstants.MENU_TOOLS, toolbar);
//		addButton(ShrimpConstants.ACTION_NAME_THUMBNAIL_VIEW, ShrimpConstants.MENU_TOOLS, toolbar);

		toolbar.add(getSeparator());

		addButton(ShrimpConstants.ACTION_NAME_SEND_FEEDBACK, ShrimpConstants.MENU_HELP, toolbar);

		toolbar.add(getSeparator());

		// add any extra actions
		if (extraActions != null) {
			for (Iterator iter = extraActions.iterator(); iter.hasNext(); ) {
				Object obj = iter.next();
				if (obj instanceof Action) {
					toolbar.add(getButton((Action)obj));
				}
			}
		}

		// fill up the remaining space?
		//toolbar.add(Box.createHorizontalGlue());

		return toolbar;
	}

}
