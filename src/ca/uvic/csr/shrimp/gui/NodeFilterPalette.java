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
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent;
import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener;
import ca.uvic.csr.shrimp.AttrToVisVarBean.Attribute;
import ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeImage;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeShape;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.FilterBean.NominalAttributeFilter;
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
 * FilterPalette serves as a container for filter GUIs.
 * The main JPanel is populated with appropritate GUI widgets by the selected filter.
 *
 * @author Jamie Pettit/Rob Lintern, Chris Callendar
 */
public class NodeFilterPalette extends AbstractShrimpTool
	implements FilterChangedListener, AttrToVisVarChangeListener, ShrimpViewListener {

	protected final static Icon ICON_CHECKED = ResourceHandler.getIcon("icon_checked.gif");
	protected final static Icon ICON_UNCHECKED = ResourceHandler.getIcon("icon_unchecked.gif");

	private JPanel gui;
	private JPanel container;
	private JScrollPane filterScrollPane;

	/** The title for this palette */
	protected String title;

	protected Vector types = null;

	/** The vector containing all the check boxes */
	protected HashMap typeBoxes = null;

	protected String cprel = "";

	// this is the width for all palettes created after the first
	protected static int widthForPalette = -1;

	private String viewToolName;

	public NodeFilterPalette(ShrimpProject project, String linkedToolName) {
		super(ShrimpApplication.NODE_FILTER, project);
		this.viewToolName = linkedToolName;
		this.gui = new JPanel();

		createGUI();
	}

	private ShrimpTool getViewTool() throws ShrimpToolNotFoundException {
		if (project == null) {
			throw new ShrimpToolNotFoundException(viewToolName);
		}
		return project.getTool(viewToolName);
	}
	/**

	 * The Filter Palette uses all the beans of its parent tool
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#getBean(String)
	 */
	public Object getBean(String name) throws BeanNotFoundException {
		try {
			ShrimpTool viewTool = getViewTool();
			return viewTool.getBean(name);
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
		if (project != null) {
			try {
				ShrimpTool tool = getViewTool();
				if (tool instanceof ShrimpView) {
					((ShrimpView)tool).removeShrimpViewListener(this);
				}
			} catch (ShrimpToolNotFoundException e) {
			}
			try {
				AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
				attrToVisVarBean.removeVisVarValuesChangeListener(this);
				FilterBean filterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				filterBean.removeFilterChangedListener(this);
			} catch (BeanNotFoundException e2) {
			}
		}
		clear();
		types = null;
		typeBoxes = null;
	}

	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		disposeTool();
		updateFilterPanel(true);
	}

	/**
	 * Creates the node filter GUI.
	 * This should only be called once from the constructor.
	 * For all other changes the {@link NodeFilterPalette#updateFilterPanel(boolean)} should be called.
	 */
	private void createGUI() {
		gui.removeAll();
		gui.setLayout(new BorderLayout());

		container = new GradientPanel(new BorderLayout());
		container.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		container.add(createHelpPanel(), BorderLayout.NORTH);

		// this will create the filter panel
		updateFilterPanel(false);

		gui.add(container, BorderLayout.CENTER);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pnl.add(createToolBar());
		gui.add(pnl, BorderLayout.SOUTH);
		gui.validate();
		gui.repaint();
	}

	private JPanel createHelpPanel() {
		JPanel pnl = new JPanel(new GridLayout(3, 1, 0, 2));
		pnl.add(createLabel("Filter node types using the checkboxes below."));
		pnl.add(createLabel("Filtered (unchecked) node types will be hidden."));
		pnl.add(createLabel("Click on a node type icon below to change the style."));

		final CollapsiblePanel helpPanel = new CollapsiblePanel("Help", ResourceHandler.getIcon("icon_help.gif"), pnl);

		// possibly collapse the help panel (do this before the listener is added
		String str = ApplicationAccessor.getProperties().getProperty(viewToolName+".help.visible", "false");
		helpPanel.setCollapsed(!"true".equalsIgnoreCase(str));

		// listen for expand/collapse of the help panel, save in application properties
		helpPanel.addChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (CollapsiblePanel.COLLAPSED_CHANGED_KEY.equals(evt.getPropertyName())) {
					boolean collapsed = ((Boolean)evt.getNewValue()).booleanValue();
					Properties props = ApplicationAccessor.getProperties();
					props.setProperty(viewToolName+".help.visible", "" + !collapsed);
				}
			}
		});

		return helpPanel;
	}

	private JToolBar createToolBar() {
		// create the buttons for displaying or hiding all of the items
		JButton showAll = new JButton(new DefaultShrimpAction("Show All", ICON_CHECKED, "Show all the node types") {
			public void actionPerformed(ActionEvent e) {
				showHideAll(true);
			}
		});
		showAll.setOpaque(false);

		JButton hideAll = new JButton(new DefaultShrimpAction("Hide All", ICON_UNCHECKED, "Hide all the node types") {
			public void actionPerformed(ActionEvent e) {
				showHideAll(false);
			}
		});
		hideAll.setOpaque(false);

		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setOpaque(false);
		toolbar.add(showAll);
		toolbar.addSeparator(new Dimension(15, 20));
		toolbar.add(hideAll);
		return toolbar;
	}

	private void showHideAll(boolean show) {
		FilterBean filterBean = null;
		try {
			filterBean = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
		} catch (BeanNotFoundException e) {
			return;
		}

		// remove this as a filter changed listener while changing filterbean
		filterBean.removeFilterChangedListener(NodeFilterPalette.this);
		//Cursor paletteCursor = ApplicationAccessor.getApplication().getCursor();
		boolean firingEvents = filterBean.isFiringEvents();
		ApplicationAccessor.waitCursor();
		try {
			filterBean.setFiringEvents(false);

			Collection types = typeBoxes.keySet();
			for (Iterator iter = types.iterator(); iter.hasNext();) {
				String type = (String) iter.next();
				JCheckBox chkbox = (JCheckBox) typeBoxes.get(type);
				if(chkbox.isSelected() != show && chkbox.isEnabled()) {
					// remove listener from check box to prevent events from firing
					ItemListener il = chkbox.getItemListeners()[0];
					chkbox.removeItemListener(il);
					chkbox.setSelected (show);
					filter(filterBean, type, !show);
					chkbox.addItemListener(il);
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			ApplicationAccessor.defaultCursor();
			filterBean.setFiringEvents(firingEvents);
			filterBean.addFilterChangedListener(NodeFilterPalette.this);
		}
	}

	private JLabel createLabel(String txt) {
		JLabel lbl = new JLabel(txt);
		lbl.setToolTipText(txt);
		return lbl;
	}

	private void updateFilterPanel(boolean repaint) {
		if (filterScrollPane != null) {
			filterScrollPane.removeAll();
			container.remove(filterScrollPane);
		}

		types = new Vector(0);
		typeBoxes = new HashMap(0);
		if (project != null) {
			JPanel holder = new TransparentPanel(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridy = GridBagConstraints.RELATIVE;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;

			JPanel filterPanel = new TransparentPanel();
			filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
			holder.add(filterPanel, c);

			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1.0;
			JPanel pnl = new TransparentPanel();
			holder.add(pnl, c);

			filterScrollPane = new JScrollPane(holder, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			filterScrollPane.setOpaque(false);
			filterScrollPane.setBorder(null);
			filterScrollPane.getViewport().setOpaque(false);
			container.add(filterScrollPane, BorderLayout.CENTER);

			try {
				// get view tool and beans, and add listeners
				ShrimpTool viewTool = project.getTool(viewToolName);
			    if (viewTool instanceof ShrimpView) {
			    	((ShrimpView) viewTool).addShrimpViewListener(this);
			    }
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				AttrToVisVarBean attrToVisVarBean = (AttrToVisVarBean) project.getBean(ShrimpProject.ATTR_TO_VIS_VAR_BEAN);
				attrToVisVarBean.addVisVarValuesChangeListener(this);
				FilterBean fb = (FilterBean) getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				fb.addFilterChangedListener(this);

				// add the filter types
				types = dataBean.getArtifactTypes(false, true);
				for (Iterator iter = types.iterator(); iter.hasNext(); ) {
					String type = (String) iter.next();
					boolean showTypeCount = false;
					int typeCount = 0;
					if (showTypeCount) {
						// TODO warning! causes all data to be collected from backend, which is ok if hiearchy is flat
						typeCount = dataBean.getArtifactsOfType(type, true).size();
					}

					Color color = (Color) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_COLOR, type);
					Color outerBorderColor = (Color) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR, type);
					String outerBorderStyle = (String) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE, type);
					Color innerBorderColor = (Color) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR, type);
					String innerBorderStyle = (String) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE, type);
					NodeShape nodeShape = (NodeShape) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_SHAPE, type);
					String nodeImageStr = (String) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_IMAGE, type);
					Icon icon = (Icon) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_NODE_ICON, type);
					String labelStyle = (String) attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, VisVarConstants.VIS_VAR_LABEL_STYLE, type);

					NodeImage nodeImage = new NodeImage(nodeImageStr);

					NodeTypePanel panel = new NodeTypePanel(this, type, color,
							outerBorderColor, outerBorderStyle, innerBorderColor, innerBorderStyle,
							nodeImage, icon, nodeShape, labelStyle, typeCount, showTypeCount, viewTool);
					filterPanel.add(panel);
				}
			} catch (BeanNotFoundException e) {
			} catch (ShrimpToolNotFoundException e) {
			}
		}
		if (repaint) {
			container.validate();
			container.repaint();
		}
	}

	/**
	 * Added to allow synchronization of the palette with changes in the filterbean
	 */
	public void filterChanged(FilterChangedEvent fce) {
		boolean setFilters = false;
		Vector changedFilters = new Vector(fce.getAddedFilters());
		changedFilters.addAll(fce.getRemovedFilters());
		changedFilters.addAll(fce.getChangedFilters());
		//Syncrhonize checkboxes with new artifact filters
		for (Iterator iter = changedFilters.iterator(); iter.hasNext(); ) {
			Filter filter = (Filter) iter.next();
			if (filter instanceof NominalAttributeFilter){
				NominalAttributeFilter attrFilter = (NominalAttributeFilter) filter;
				if (AttributeConstants.NOM_ATTR_ARTIFACT_TYPE.equals(attrFilter.getAttributeName())) {
					setFilters = true;
					break;
				}
			}
		}

		if (setFilters) {
			// TODO make this more efficient - just update checkboxes?
			updateFilterPanel(true);
		}
	}

	void filter(FilterBean filterBean, String type, boolean filter) {
		filterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_FILTER_TYPE, type, filter);
	}

	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeListener#valuesChanged(ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarChangeEvent)
	 */
	public void valuesChanged(AttrToVisVarChangeEvent e) {
		Attribute attr = e.getAttribute();
		if (attr.getName().equals(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE)) {
			// these values require the filter panel to be updated
			HashSet needChanging = new HashSet();
			needChanging.add(VisVarConstants.VIS_VAR_NODE_COLOR);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_SHAPE);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_IMAGE);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_INNER_BORDER_COLOR);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_COLOR);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_OUTER_BORDER_STYLE);
			needChanging.add(VisVarConstants.VIS_VAR_NODE_INNER_BORDER_STYLE);

			boolean update = false;
			List visVars = e.getVisualVariables();
			for (Iterator iter = visVars.iterator(); iter.hasNext();) {
				VisualVariable visVar = (VisualVariable) iter.next();
				if (needChanging.contains(visVar.getName())) {
					update = true;
					break;
				}
			}
			if (update) {
				updateFilterPanel(true);
			}
		}
	}

	public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event) {
		updateFilterPanel(true);
	}

	public void shrimpViewMouseModeChanged(ShrimpViewMouseModeChangedEvent event) {
	}

    public void clear() {
		if (filterScrollPane != null) {
			filterScrollPane.removeAll();
			container.remove(filterScrollPane);
		}
    	types = null;
    	typeBoxes = null;
    }

}
