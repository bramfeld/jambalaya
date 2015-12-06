/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewCprelsChangedEvent;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewListener;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewMouseModeChangedEvent;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.CollapsiblePanel;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * FilterPalette serves as a container for filter GUIs. FilterPalette
 * contains two JPanels. The top JPanel
 * holds a list of the available filters for the selected
 * picture. the bottom JPanel is populated with appropritate GUI widgets
 * by the selected filter.
 *
 * @author Nasir Rather, Chris Callendar
 */
public class ArcFilterPalette extends AbstractShrimpTool
	implements FilterChangedListener, AttrToVisVarChangeListener, ShrimpViewListener {

	protected final static Color SELECTION_FOREGROUND_COLOR = UIManager.getColor("Tree.selectionForeground");
	protected final static Color SELECTION_BACKGROUND_COLOR = UIManager.getColor("Tree.selectionBackground");
	protected final static Color TEXT_FOREGROUND_COLOR = UIManager.getColor("Tree.textForeground");
	protected final static Color TEXT_BACKGROUND_COLOR = UIManager.getColor("Tree.textBackground");

	protected final static Icon ICON_PLUS = ResourceHandler.getIcon("icon_plus.gif");
	protected final static Icon ICON_MINUS = ResourceHandler.getIcon("icon_minus.gif");
	protected final static Icon ICON_DISPLAY_FILTER_FILTERED = ResourceHandler.getIcon("icon_display_filter_filtered.gif");
	protected final static Icon ICON_DISPLAY_FILTER_UNFILTERED = ResourceHandler.getIcon("icon_display_filter_unfiltered.gif");
	protected final static Icon ICON_DATA_FILTER_FILTERED = ResourceHandler.getIcon("icon_data_filter_filtered.gif");
	protected final static Icon ICON_DATA_FILTER_UNFILTERED = ResourceHandler.getIcon("icon_data_filter_unfiltered.gif");


	private JPanel gui;
	private JPanel mainPanel;
	private JPanel filtersPanel;
	private AutoScrollPane filtersPane;

	/** The title pf this palette */
	protected String title;
	protected String viewToolName;
	protected DataBean dataBean;
	protected FilterBean filterBean;
	protected DisplayBean displayBean;
	protected SelectorBean selectorBean;
	protected AttrToVisVarBean attrToVisVarBean;
	protected DataDisplayBridge dataDisplayBridge;

	protected HashMap groupPanels = new HashMap();
	protected HashMap typePanels = new HashMap();

	private GroupPanel selectedGroupPanel = null;
	private ArcTypePanel lastSelectedTypePanel = null;
	private Vector selectedTypePanels = new Vector();
	private DefaultShrimpAction newGroupAction;
	private DefaultShrimpAction deleteGroupAction;
	private DefaultShrimpAction propertiesAction;

	public ArcFilterPalette(ShrimpProject project, String viewToolName) {
		super(ShrimpApplication.ARC_FILTER, project);
		this.viewToolName = viewToolName;
		this.gui = new GradientPanel();
		createGUI();
	}

	private ShrimpTool getViewTool() throws ShrimpToolNotFoundException {
		if (project != null) {
			return project.getTool(viewToolName);
		} else {
			throw new ShrimpToolNotFoundException(viewToolName);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getBean(String)
	 * The Filter Palette uses all the beans of its parent tool
	 */
	public Object getBean(String name) throws BeanNotFoundException {
		try {
			ShrimpTool tool = getViewTool();
			return tool.getBean(name);
		} catch (ShrimpToolNotFoundException e) {
			throw new BeanNotFoundException(name);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool#getGUI()
	 */
	public Component getGUI() {
		return gui;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool#disposeTool()
	 */
	public void disposeTool() {
		try {
			ShrimpTool tool = getViewTool();
			if (tool instanceof ShrimpView) {
				((ShrimpView) tool).removeShrimpViewListener(this);
			}
		} catch (Exception ignore) {
		}
		if (filterBean != null) {
			filterBean.removeFilterChangedListener(this);
		}
		if (attrToVisVarBean != null) {
			attrToVisVarBean.removeVisVarValuesChangeListener(this);
		}

		clear();
		enableDisableButtons();
		dataBean = null;
		filterBean = null;
		displayBean = null;
		selectorBean = null;
		attrToVisVarBean = null;
		dataDisplayBridge = null;
		selectedGroupPanel = null;
		lastSelectedTypePanel = null;
		filtersPane = null;
		selectedTypePanels = new Vector();
		groupPanels = new HashMap(1);
		typePanels = new HashMap(1);
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		disposeTool();
		updateFilterPanel(true);
	}

	/**
	 * Sets the filter choices for the current view and
	 * sets up the GUI widgets for the default filter for that window.
	 */
	private void createGUI() {
		createActions();

		gui.removeAll();
		gui.setLayout(new BorderLayout());
		mainPanel = new GradientPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		gui.add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(createHelpPanel(), BorderLayout.NORTH);

		updateFilterPanel(false);

		enableDisableButtons();

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pnl.add(createToolBar());
		gui.add(pnl, BorderLayout.SOUTH);

		gui.validate();
		gui.repaint();
	}

	private JToolBar createToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(new JButton(newGroupAction));
		toolbar.addSeparator(new Dimension(15, 20));
		toolbar.add(new JButton(deleteGroupAction));
		toolbar.addSeparator(new Dimension(15, 20));
		toolbar.add(new JButton(propertiesAction));
		return toolbar;
	}

	private void createActions() {
		newGroupAction = new DefaultShrimpAction("Group", ResourceHandler.getIcon("icon_new2.gif"), "Create a new arc type group") {
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog(gui, "Enter a name for the group.", generateUniqueGroupName("New Group "));
				if (name == null) {
					return;
				}
				if (!groupNameIsUnique(name)) {
					String oldName = name;
					name = generateUniqueGroupName(name);
					String msg = "The name \"" + oldName + "\" is already in use.  Using \"" + name + "\" instead.";
					JOptionPane.showMessageDialog(gui, msg);
				}
				addGroupPanel(dataDisplayBridge.getCompositeArcsManager().createRelTypeGroup(name));
				mainPanel.validate();
			}
		};
		deleteGroupAction = new DefaultShrimpAction("Group", ResourceHandler.getIcon("icon_delete.gif"), "Delete the selected group") {
			public void actionPerformed(ActionEvent e) {
				if (selectedGroupPanel != null) {
					if (selectedGroupPanel.getRelTypeGroup().getRelTypes().size() == 0) {
						removeGroupPanel(selectedGroupPanel);
						dataDisplayBridge.getCompositeArcsManager().disposeRelTypeGroup(selectedGroupPanel.getRelTypeGroup().getGroupName());
						selectedGroupPanel = null;
						enableDisableButtons();
					} else {
						JOptionPane.showMessageDialog(gui, "This group is not empty.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(gui, "Please select an empty group.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		propertiesAction = new DefaultShrimpAction("Properties", ResourceHandler.getIcon("icon_filter.gif"), "Displays the group or arc type properties dialog") {
			public void actionPerformed(ActionEvent e) {
				if (selectedGroupPanel != null) {
					selectedGroupPanel.showGroupProperties();
				} else if (selectedTypePanels.size() == 1) {
					((ArcTypePanel)selectedTypePanels.firstElement()).showProperties();
				}
			}
		};
	}

	private JPanel createHelpPanel() {

		JPanel pnl = new JPanel(new GridLayout(4, 1, 0, 2));
		pnl.add(createLabel("Filter arc types using the checkboxes below."));
		pnl.add(createLabel("Click on an arc type icon below to change the style."));
		pnl.add(createLabel("Double-click on a group to turn on high level arcs."));
		pnl.add(createLabel("Arc types can be dragged between groups."));
		final CollapsiblePanel helpPanel = new CollapsiblePanel("Help", ResourceHandler.getIcon("icon_help.gif"), pnl);

		// restore the collapsed state (before the listener is added)
		String str = ApplicationAccessor.getProperties().getProperty(viewToolName+".help.visible", "false");
		helpPanel.setCollapsed(!"true".equalsIgnoreCase(str));

		// listen for expand/collapse of the help panel, save in project properties
		helpPanel.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (CollapsiblePanel.COLLAPSED_CHANGED_KEY.equals(evt.getPropertyName())) {
					boolean collapsed = ((Boolean)evt.getNewValue()).booleanValue();
					Properties props = ApplicationAccessor.getProperties();
					props.setProperty(viewToolName + ".help.visible", "" + !collapsed);
				}
			}
		});
		return helpPanel;
	}

	private void updateFilterPanel(boolean repaint) {
		if (filtersPane != null) {
			mainPanel.remove(filtersPane);
			filtersPane.removeAll();
		}
		if (loadBeans()) {
			filtersPane = new AutoScrollPane(createFiltersPanel());
			filtersPane.setOpaque(false);
			filtersPane.getViewport().setOpaque(false);
			filtersPane.setBorder(null);
			mainPanel.add(filtersPane, BorderLayout.CENTER);
		}
		if (repaint) {
			mainPanel.validate();
			mainPanel.repaint();
		}
	}

	private boolean loadBeans() {
		boolean loaded = false;
		if (project != null) {
			try {
				ShrimpTool tool = getViewTool();
	            if (tool instanceof ShrimpView) {
	            	((ShrimpView)tool).addShrimpViewListener(this);	// won't add duplicates
	            }
			} catch (ShrimpToolNotFoundException e) {
			}
			try {
	            dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
	            attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
	            attrToVisVarBean.addVisVarValuesChangeListener(this);
	            displayBean = (DisplayBean) getBean(DISPLAY_BEAN);
	            dataDisplayBridge = displayBean.getDataDisplayBridge();
	            selectorBean = (SelectorBean) getBean(SELECTOR_BEAN);
	            filterBean = (FilterBean) getBean(DISPLAY_FILTER_BEAN);
	            filterBean.addFilterChangedListener(this);
		        loaded = true;
	        } catch (BeanNotFoundException e) {
	        }
		}
		return loaded;
	}

	private JLabel createLabel(String txt) {
		JLabel lbl = new JLabel(txt);
		lbl.setToolTipText(txt);
		return lbl;
	}

	/**
	 * Filters or unfilters all the arc types in the given {@link ca.uvic.csr.shrimp.gui.GroupPanel}.
	 * @param filter if false all types are checked/shown, if true all are unchecked/hidden.
	 */
	public void filterSelectedGroupTypesInDisplay(GroupPanel groupPanel, boolean filter) {
		boolean isFiringEvents = filterBean.isFiringEvents();
		filterBean.removeFilterChangedListener(this);
		try {
			filterBean.setFiringEvents(false);
			if (groupPanel != null) {
				for (Iterator iterator = groupPanel.getTypePanels().iterator(); iterator.hasNext();) {
					ArcTypePanel panel = (ArcTypePanel) iterator.next();
					panel.setDisplayFiltered(filter);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			filterBean.setFiringEvents(isFiringEvents);
			filterBean.addFilterChangedListener(this);
		}
		enableDisableButtons();
	}

	void filterSelectedTypesInDisplay(boolean displayFilter) {
		boolean isFiringEvents = filterBean.isFiringEvents();
		filterBean.removeFilterChangedListener(this);
		try {
			filterBean.setFiringEvents(false);
			for (Iterator iterator = selectedTypePanels.iterator(); iterator.hasNext();) {
				ArcTypePanel panel = (ArcTypePanel) iterator.next();
				panel.setDisplayFiltered(displayFilter);
			}

			if(selectedGroupPanel != null) {
				selectedGroupPanel.setFilter(displayFilter);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			filterBean.setFiringEvents(isFiringEvents);
			filterBean.addFilterChangedListener(this);
		}
		enableDisableButtons();
	}

	private boolean groupNameIsUnique(String groupName) {
		Collection usedNames = getUsedGroupNames();
		return !usedNames.contains(groupName);
	}

	private String generateUniqueGroupName(String prefix) {
		int x = 1;
		String uniqueGroupName = null;
		Collection usedNames = getUsedGroupNames();
		while (uniqueGroupName == null) {
			String groupName = prefix + x;
			if (!usedNames.contains(groupName)) {
				uniqueGroupName = groupName; // we have found a unique group name
			}
			x++;
		}
		return uniqueGroupName;
	}

	private Collection getUsedGroupNames() {
		Collection usedNames = new HashSet();	// set of all used names already
		if (dataDisplayBridge != null) {
			Collection relTypeGroups = dataDisplayBridge.getCompositeArcsManager().getRelTypeGroups();
			for (Iterator iterator = relTypeGroups.iterator(); iterator.hasNext();) {
				RelTypeGroup relTypeGroup = (RelTypeGroup) iterator.next();
				usedNames.add(relTypeGroup.getGroupName());
			}
			// we dont want groups to have the same name as a type of relationship
			Collection relTypes = dataBean.getRelationshipTypes(false, true);
			usedNames.addAll(relTypes);
		}
		return usedNames;
	}


	/**
	 * Returns the bottom part of the filter's dialog box.
	 */
	private JPanel createFiltersPanel() {
		JPanel returnPanel = new TransparentPanel(new GridBagLayout());

		filtersPanel = new TransparentPanel();
		filtersPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		returnPanel.add(filtersPanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;

		JPanel panel = new TransparentPanel();
		returnPanel.add(panel, c);

		Vector relTypeGroups = dataDisplayBridge.getCompositeArcsManager().getRelTypeGroups();
		Collections.sort(relTypeGroups);
		for (Iterator iterator = relTypeGroups.iterator(); iterator.hasNext();) {
			RelTypeGroup relTypeGroup = (RelTypeGroup) iterator.next();
			addGroupPanel(relTypeGroup);
		}

		return returnPanel;
	}

	void enableDisableButtons() {
		boolean groupTypeIsSelected = (selectedGroupPanel != null);
		boolean normalTypeIsSelected = (selectedTypePanels != null) && !selectedTypePanels.isEmpty();
		//boolean compositesEnabled = selectedGroupPanel != null && selectedGroupPanel.relTypeGroup.areCompositesEnabled();
		boolean allFiltered = true;
		boolean allNotFiltered = true;
		if (groupTypeIsSelected) {
			String type = selectedGroupPanel.getRelTypeGroup().getGroupName();
			allFiltered = filterBean.isNominalAttrValueFiltered (AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, type);
			allNotFiltered = !allFiltered;
		} else if (normalTypeIsSelected) {
			for (Iterator iter = selectedTypePanels.iterator(); iter.hasNext();) {
				ArcTypePanel typePanel = (ArcTypePanel) iter.next();
				String type = typePanel.getType();
				boolean filtered = filterBean.isNominalAttrValueFiltered (AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, type);
				allFiltered = allFiltered && filtered;
				allNotFiltered = allNotFiltered && !filtered;
			}
		}

		deleteGroupAction.setEnabled(groupTypeIsSelected);
		newGroupAction.setEnabled((dataDisplayBridge != null));
		propertiesAction.setEnabled(groupTypeIsSelected || (normalTypeIsSelected && (selectedTypePanels.size() == 1)));
	}

	private void addGroupPanel(RelTypeGroup relTypeGroup) {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.weightx = 1.0;

		GroupPanel groupPanel = new GroupPanel(this, relTypeGroup);
		groupPanel.collapseExpandGroup();
		groupPanels.put(relTypeGroup.getGroupName(), groupPanel);
		filtersPanel.add(groupPanel, gridBagConstraints);
	}

	private void removeGroupPanel(GroupPanel groupPanel) {
		filtersPanel.remove(groupPanel);
		groupPanels.remove(groupPanel.getRelTypeGroup().getGroupName());
		filtersPanel.getParent().validate();
	}

	public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event) {
		updateFilterPanel(true);
	}

	public void shrimpViewMouseModeChanged(ShrimpViewMouseModeChangedEvent event) {
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.FilterBean.FilterChangedListener#filterChanged(ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent)
	 */
	public void filterChanged(FilterChangedEvent fce) {
		Vector filters = new Vector(20);
		filters.addAll(fce.getAddedFilters());
		filters.addAll(fce.getChangedFilters());
		filters.addAll(fce.getRemovedFilters());

		boolean changed = false;
		for (Iterator iter = filters.iterator(); iter.hasNext(); ) {
			Filter filter = (Filter) iter.next();
			if (FilterConstants.RELATIONSHIP_FILTER_TYPE.equals(filter.getTargetType())) {
				changed = true;
				break;
			}
		}

		if (changed) {
			updateFilterPanel(true);
		}
	}

	/** turns the relationship type filters on or off */
	protected void filterSingleTypeInDisplay(String relType, boolean addFilter) {
		filterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, relType, addFilter);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener#valuesChanged(ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent)
	 */
	public void valuesChanged(AttrToVisVarChangeEvent e) {
		Attribute attr = e.getAttribute();
		if (attr.getName().equals(AttributeConstants.NOM_ATTR_REL_TYPE)) {
			List visVars = e.getVisualVariables();
			for (Iterator iter = visVars.iterator(); iter.hasNext();) {
				VisualVariable visVar = (VisualVariable) iter.next();
				if (visVar.getName().equals(VisVarConstants.VIS_VAR_ARC_COLOR)) {
					updateColors ();
				} else if (visVar.getName().equals(VisVarConstants.VIS_VAR_ARC_STYLE)) {
					updateStyles();
				}
			}
		}
	}

	private void updateColors() {
		for (Iterator iter = typePanels.keySet().iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			ArcTypePanel typePanel = (ArcTypePanel) typePanels.get(type);
			Color color = (Color)attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, type);
			typePanel.setColor(color);
		}
	}

	private void updateStyles() {
		for (Iterator iter = typePanels.keySet().iterator(); iter.hasNext();) {
			String type = (String) iter.next();
			ArcTypePanel typePanel = (ArcTypePanel) typePanels.get(type);
			ArcStyle arcStyle = (ArcStyle) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, type);
			typePanel.setStyle(arcStyle);
		}
	}

    public GroupPanel getSelectedGroupPanel() {
        return selectedGroupPanel;
    }

    public void setSelectedGroupPanel(GroupPanel groupPanel) {
        this.selectedGroupPanel = groupPanel;
    }

    public Vector getSelectedTypePanels() {
        return selectedTypePanels;
    }

    public ArcTypePanel getLastSelectedTypePanel() {
        return lastSelectedTypePanel;
    }

    public void setLastSelectedTypePanel(ArcTypePanel lastSelectedTypePanel) {
        this.lastSelectedTypePanel = lastSelectedTypePanel;
    }

    public void clear() {
    	//if (dataBean != null) {
    		// @tag Creole.CompositeArcs : this was causing problems in Creole with composite arcs
    		// if the databean is cleared then the artifacts are re-created and the composite arcs contain the wrong artifacts
    		//dataBean.clearBufferedData();
    	//}
		if (filtersPane != null) {
			mainPanel.remove(filtersPane);
			filtersPane.removeAll();
		}
    }

	// this panel implements autoscroll for DND purposes. DropTargetListner really does nothing.
	private class AutoScrollPane extends JScrollPane implements Autoscroll, DropTargetListener {

		public AutoScrollPane(Component view) {
			super(view, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			new DropTarget(this, this);
			//setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4,4,4,4), getBorder()));
		}

		public Insets getAutoscrollInsets() {
			return new Insets(30,30,30,30);
		}

		public void autoscroll(Point cursorLocn) {
			Rectangle r = new Rectangle(cursorLocn.x-10, cursorLocn.y-10, 1, 1);
			viewport.scrollRectToVisible(r);
		}

		public void dragEnter(DropTargetDragEvent e) {
		}

		public void dragOver(DropTargetDragEvent e) {
			e.rejectDrag();
		}

		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

		public void dragExit(DropTargetEvent dte) {
		}

		public void drop(DropTargetDropEvent dtde) {
		}
	}
}
