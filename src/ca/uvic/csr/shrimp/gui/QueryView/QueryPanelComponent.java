/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.FilterBean.Filter;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedEvent;
import ca.uvic.csr.shrimp.FilterBean.FilterChangedListener;
import ca.uvic.csr.shrimp.FilterBean.NominalAttributeFilter;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.AutoCompleteDocument;
import ca.uvic.csr.shrimp.util.JButtonList;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * This class contains the components used in the query view.
 *
 * @author Chris Callendar
 */
public class QueryPanelComponent extends TransparentPanel implements ChangeListener, FilterChangedListener {

	private static final String CONTAINS 	= "contains";
	private static final String STARTS_WITH = "starts with";
	private static final String ENDS_WITH	= "ends with";
	private static final String EXACT_MATCH = "exact match";
	private static final String REGEXP 		= "regexp";
	private static final String[] MODES = { CONTAINS, STARTS_WITH, ENDS_WITH, EXACT_MATCH, REGEXP };

	private static final String NEIGHBOURHOOD = "neighbourhood";
	private static final String PARENTS = "parents";
	private static final String CHILDREN	= "children";
	private static final String HIERARCHY_TO_ROOT = "hierarchy to root";
	private static final String[] LEVELS = { NEIGHBOURHOOD, PARENTS, CHILDREN, HIERARCHY_TO_ROOT };

	private static final int MAX_ROWS = 8;
	private static final int QUERY_ROW_HEIGHT = 24;

	private QueryView queryView;
	private QueryHelper queryHelper;
	private boolean updateGraphOnLevelsChange = false;

	private JRootPane rootPane;
	private JButtonList arcTypesList;
	private JButtonList nodeTypesList;
	private JButton btnQuery;
	private JSpinner incomingLevels;
	private JSpinner outgoingLevels;
	private JComboBox searchBox;
	private JComboBox searchModes;
	private JComboBox searchLevels;
	private JPanel pnlSearchButtons;
	private AutoCompleteDocument autoCompleteDocument;

	private JPanel searchPanel;
	private JPanel nodesPanel;
	private JPanel arcsPanel;

	private boolean ignoreFilterEvent = false;

	public QueryPanelComponent(QueryView queryView) {
		super();
		this.queryView = queryView;
		this.queryHelper = (queryView != null ? queryView.getQueryHelper() : new QueryHelper(null));	// for testing

		createGUI();
	}

	public void dispose() {
		this.queryView = null;
		this.queryHelper = null;
	}

	private void createGUI() {
		setLayout(new BorderLayout(0, 0));
		rootPane = new JRootPane();
		((JComponent)rootPane.getContentPane()).setOpaque(false);
		add(rootPane, BorderLayout.CENTER);
		rootPane.getContentPane().add(getSearchPanel(), BorderLayout.NORTH);

		JPanel mainPanel = new TransparentPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.add(getNodesPanel());
		mainPanel.add(Box.createHorizontalStrut(4));
		mainPanel.add(getArcsPanel());
		rootPane.getContentPane().add(mainPanel, BorderLayout.CENTER);

		updateValues();

		rootPane.setDefaultButton(btnQuery);
		getSearchBox().requestFocus();
		this.invalidate();
	}

	/**
	 * Adds or removes an attribute filter.
	 * @param attributeName the attribute name (see {@link AttributeConstants})
	 * @param filterType the filter type (see (see {@link FilterConstants})
	 * @param e the checkbox event
	 */
	private void filter(String attributeName, String filterType, ItemEvent e) {
		if ((queryView != null) && !ignoreFilterEvent) {
			try {
				FilterBean filterBean = (FilterBean) queryView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
				String item = e.getItem().toString();
				filterBean.removeFilterChangedListener(this);
				filterBean.addRemoveSingleNominalAttrValue(attributeName, String.class, filterType, item, !selected);
				filterBean.addFilterChangedListener(this);
			} catch (BeanNotFoundException bnfe) {
				System.err.println("Couldn't find filter bean");
			}
		}
	}

	public void filterChanged(final FilterChangedEvent fce) {
		// wait for all other events to finish
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				boolean updateNodeTypes = false;
				boolean updateArcTypes = false;
				Vector changedFilters = new Vector(fce.getAddedFilters());
				changedFilters.addAll(fce.getRemovedFilters());
				changedFilters.addAll(fce.getChangedFilters());
				//Syncrhonize checkboxes with new artifact filters
				for (Iterator iter = changedFilters.iterator(); iter.hasNext(); ) {
					Filter filter = (Filter) iter.next();
					if (filter instanceof NominalAttributeFilter){
						NominalAttributeFilter attrFilter = (NominalAttributeFilter) filter;
						if (AttributeConstants.NOM_ATTR_ARTIFACT_TYPE.equals(attrFilter.getAttributeName())) {
							updateNodeTypes = true;
						} else if (AttributeConstants.NOM_ATTR_REL_TYPE.equals(attrFilter.getAttributeName())) {
							updateArcTypes = true;
						}
					}
				}

				if (updateNodeTypes) {
					updateCheckedArtifactTypes(false);
				}
				if (updateArcTypes) {
					updateCheckedRelationshipTypes(false);
				}
			}
		});
	}

	private void updateValues() {
		updateLevels();
		searchBox.setSelectedItem(queryHelper.getSrcArtifactName());
	}

	private int getStringMatchingMode() {
		String mode = (String) getSearchModesComboBox().getSelectedItem();
		if (CONTAINS.equals(mode)) {
			return QueryHelper.STRING_MATCH_CONTAINS_MODE;
		} else if (EXACT_MATCH.equals(mode)) {
			return QueryHelper.STRING_MATCH_EXACT_MODE;
		} else if (STARTS_WITH.equals(mode)) {
			return QueryHelper.STRING_MATCH_STARTS_WITH_MODE;
		} else if (ENDS_WITH.equals(mode)) {
			return QueryHelper.STRING_MATCH_ENDS_WITH_MODE;
		} else /* if (REGEXP.equals(mode)) */ {
			return QueryHelper.STRING_MATCH_REGEXP_MODE;
		}
	}

	private void doQuery() {
		if (queryView != null) {
			// collect all information from gui widgets
			collectQueryParamsFromGUI();
			queryView.doQuery(true);
		}
	}

	private void collectQueryParamsFromGUI() {
		queryHelper.setSearchMode(QueryHelper.SEARCH_MODE_BY_NAME);
		JTextComponent editorComponent = (JTextComponent) searchBox.getEditor().getEditorComponent();
		String text = editorComponent.getText().trim();
		queryHelper.setSrcArtifactName(text);
		queryHelper.setStringMatchingMode(getStringMatchingMode());

		queryHelper.setArtifactTypes(nodeTypesList.getCheckedItemsCollection());
		queryHelper.setRelationshipTypes(arcTypesList.getCheckedItemsCollection());

		int incomingLevels = 0, outgoingLevels = 0;
		String level = (String) getSearchLevelsComboBox().getSelectedItem();
		if (NEIGHBOURHOOD.equals(level)) {
			incomingLevels = outgoingLevels = 1;
		} else if (PARENTS.equals(level)) {
			incomingLevels = 1;
		} else if (CHILDREN.equals(level)) {
			outgoingLevels = 1;
		} else if (HIERARCHY_TO_ROOT.equals(level)) {
			incomingLevels = -1;
		}
		queryHelper.setLevels(incomingLevels, outgoingLevels);
	}

	private void setChecked(Collection types, JButtonList lst) {
		List indicesList = new ArrayList();
		lst.checkNone();
		for (int i = 0; i < lst.getModel().getSize(); i++) {
			String artType = (String) lst.getModel().getElementAt(i);
			if (types.contains(artType)) {
				indicesList.add(new Integer(i));
			}
		}
		int[] indices = new int[indicesList.size()];
		int i = 0;
		for (Iterator iter = indicesList.iterator(); iter.hasNext();) {
			Integer indexInt = (Integer) iter.next();
			indices[i] = indexInt.intValue();
			i++;
		}
		lst.setCheckedIndices(indices);
	}


	//////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////

	public void stateChanged(ChangeEvent e) {
		// update the incoming and outgoing levels
		int incoming = ((Integer)incomingLevels.getValue()).intValue();
		int outgoing = ((Integer)outgoingLevels.getValue()).intValue();
		if ((incoming != queryHelper.getIncomingLevels()) || (outgoing != queryHelper.getOutgoingLevels())) {
			queryHelper.setLevels(incoming, outgoing);
			if (updateGraphOnLevelsChange) {
				doQuery();
			}
		}
	}

	public JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
			searchPanel.add(getSearchBox());
			searchPanel.add(getExtraSearchButtonsPanel());
			searchPanel.add(getSearchModesComboBox());
			searchPanel.add(getSearchLevelsComboBox());
			searchPanel.add(getQueryButton());
		}
		return searchPanel;
	}

	public JComboBox getSearchModesComboBox() {
		if (searchModes == null) {
			searchModes = new JComboBox(MODES);
			searchModes.setOpaque(false);
			searchModes.setToolTipText("Search modes");
			searchModes.setSelectedIndex(0);
			searchModes.setPreferredSize(new Dimension(105, QUERY_ROW_HEIGHT));
		}
		return searchModes;
	}

	public JComboBox getSearchLevelsComboBox() {
		if (searchLevels == null) {
			searchLevels = new JComboBox(LEVELS);
			searchLevels.setOpaque(false);
			searchLevels.setToolTipText("Search levels");
			searchLevels.setSelectedIndex(0);
			searchLevels.setPreferredSize(new Dimension(125, QUERY_ROW_HEIGHT));
		}
		return searchLevels;
	}

	public JComboBox getSearchBox() {
		if (searchBox == null) {
			searchBox = new JComboBox(new DefaultComboBoxModel());
			searchBox.setOpaque(false);
			searchBox.setEditable(true);
	        autoCompleteDocument = new AutoCompleteDocument(searchBox);
	        JTextComponent editor = (JTextComponent) searchBox.getEditor().getEditorComponent();
			editor.setDocument(autoCompleteDocument);

			searchBox.setPreferredSize(new Dimension(200, QUERY_ROW_HEIGHT));
			searchBox.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent e) {
					// pressing enter when textbox is focussed runs the query
					rootPane.setDefaultButton(btnQuery);
				}
			});
		}
		return searchBox;
	}

	public JButton getQueryButton() {
		if (btnQuery == null) {
			btnQuery = new JButton(new DefaultShrimpAction(" Query ", ResourceHandler.getIcon("icon_search.gif"), "Run the query") {
				public void actionPerformed(ActionEvent e) {
					doQuery();
				}
			});
			btnQuery.setPreferredSize(new Dimension(100, QUERY_ROW_HEIGHT));
		}
		return btnQuery;
	}

	public JPanel getNodesPanel() {
		if (nodesPanel == null) {
			nodesPanel = new TransparentPanel(new BorderLayout());
			nodesPanel.setBorder(BorderFactory.createTitledBorder("Node Types"));
			nodesPanel.setToolTipText("Node types filter panel");
			nodeTypesList = new JButtonList();
			nodeTypesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			nodeTypesList.setListData(new Object[0]);
			nodesPanel.add(new JScrollPane(nodeTypesList), BorderLayout.CENTER);
			nodeTypesList.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, FilterConstants.ARTIFACT_FILTER_TYPE, e);
				}
			});
		}
		return nodesPanel;
	}

	public JPanel getArcsPanel() {
		if (arcsPanel == null) {
			arcsPanel = new TransparentPanel(new BorderLayout());
			arcsPanel.setBorder(BorderFactory.createTitledBorder("Arc Types"));
			arcsPanel.setToolTipText("Arc types filter panel");
			arcTypesList = new JButtonList();
			arcTypesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			arcTypesList.setListData(new Object[0]);
			arcsPanel.add(new JScrollPane(arcTypesList), BorderLayout.CENTER);
			arcTypesList.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					filter(AttributeConstants.NOM_ATTR_REL_TYPE, FilterConstants.RELATIONSHIP_FILTER_TYPE, e);
				}
			});
		}
		return arcsPanel;
	}

    /**
     * Returns the search by name panel.
     * Holds any extra buttons used in searching (e.g. in Jambalaya).
     */
    public JPanel getExtraSearchButtonsPanel() {
    	if (pnlSearchButtons == null) {
			pnlSearchButtons = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    	}
    	return pnlSearchButtons;
    }

	public void setSearchItems(Collection items, boolean clearExisting) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) getSearchBox().getModel();
		if (clearExisting) {
			model.removeAllElements();
		}
		for (Iterator iter = items.iterator(); iter.hasNext(); ) {
			model.addElement(iter.next());
		}
	}

	public void addSearchItem(Object item) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) getSearchBox().getModel();
		model.addElement(item);
	}

	public void addSearchItem(int index, Object item) {
		DefaultComboBoxModel model = (DefaultComboBoxModel) getSearchBox().getModel();
		if ((index >= 0) && (index < model.getSize())) {
			model.insertElementAt(item, index);
		}
	}

	public void updateSearchMode() {
		int mode = queryHelper.getSearchMode();
		String strMode = (String) getSearchModesComboBox().getSelectedItem();
		if (QueryHelper.STRING_MATCH_CONTAINS_MODE == mode) {
			strMode = CONTAINS;
		} else if (QueryHelper.STRING_MATCH_EXACT_MODE == mode) {
			strMode = EXACT_MATCH;
		} else if (QueryHelper.STRING_MATCH_STARTS_WITH_MODE == mode) {
			strMode = STARTS_WITH;
		} else if (QueryHelper.STRING_MATCH_ENDS_WITH_MODE == mode) {
			strMode = ENDS_WITH;
		} else if (QueryHelper.STRING_MATCH_REGEXP_MODE == mode) {
			strMode = REGEXP;
		}
		getSearchModesComboBox().setSelectedItem(strMode);
	}

	public void updateLevels() {
		String level;
		int incoming = queryHelper.getIncomingLevels();
        int outgoing = queryHelper.getOutgoingLevels();
        if ((incoming == 1) && (outgoing == 0)) {
        	level = PARENTS;
        } else if ((incoming == 0) && (outgoing == 1)) {
        	level = CHILDREN;
        } else if ((incoming == -1) && (outgoing == 0)) {
        	level = HIERARCHY_TO_ROOT;
        } else { // if ((incoming == 1) && (outgoing == 1)) {
        	level = NEIGHBOURHOOD;
        }
        getSearchLevelsComboBox().setSelectedItem(level);
	}

	public void updateArtifactTypes() {
		nodeTypesList.setListData(new Vector(queryHelper.getArtTypes()));
        setChecked(queryHelper.getArtTypes(), nodeTypesList);
	}

	/**
	 * Updates the checked artifacts - possibly firing a filter event.
	 */
	public void updateCheckedArtifactTypes(boolean fireEvent) {
		if (!fireEvent) {
			ignoreFilterEvent = true;
		}
		if (queryView != null) {
			// check the artifact types that aren't filtered
			try {
				FilterBean filterBean = (FilterBean) queryView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				Collection artTypes = queryHelper.getArtTypes();
				Vector checkedArtTypes = new Vector();
				for (Iterator iter = artTypes.iterator(); iter.hasNext(); ) {
					String artType = (String) iter.next();
					boolean filtered = filterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE,
												String.class, FilterConstants.ARTIFACT_FILTER_TYPE, artType);
					if (!filtered) {
						checkedArtTypes.add(artType);
					}
				}
				setChecked(checkedArtTypes, nodeTypesList);
			} catch (BeanNotFoundException bnfe) {
				System.err.println("Couldn't find filter bean");
			}
		}
		if (!fireEvent) {
			ignoreFilterEvent = false;
		}
	}

	public void updateRelationshipTypes() {
		arcTypesList.setListData(new Vector(queryHelper.getRelTypes()));
        setChecked(queryHelper.getRelTypes(), arcTypesList);
	}

	/**
	 * Updates the checked relationships - possibly firing a filter event.
	 */
	public void updateCheckedRelationshipTypes(boolean fireEvent) {
		if (!fireEvent) {
			ignoreFilterEvent = true;
		}
		if (queryView != null) {
			// check the relationship types that aren't filtered
			try {
				FilterBean filterBean = (FilterBean) queryView.getBean(ShrimpTool.DISPLAY_FILTER_BEAN);
				Collection relTypes = queryHelper.getRelTypes();
				Vector checkedRelTypes = new Vector();
				for (Iterator iter = relTypes.iterator(); iter.hasNext(); ) {
					String relType = (String) iter.next();
					boolean filtered = filterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_REL_TYPE,
												String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, relType);
					if (!filtered) {
						checkedRelTypes.add(relType);
					}
				}
				setChecked(checkedRelTypes, arcTypesList);
			} catch (BeanNotFoundException bnfe) {
				System.err.println("Couldn't find filter bean");
			}
		}
		if (!fireEvent) {
			ignoreFilterEvent = false;
		}
	}

	public void updateSrcArtifactName() {
		searchBox.setSelectedItem(queryHelper.getSrcArtifactName());
		JTextComponent textbox = (JTextComponent)searchBox.getEditor().getEditorComponent();
		// this is to stop the UI from freezing when the popup shows
		autoCompleteDocument.setShowPopup(false);
		textbox.setText(queryHelper.getSrcArtifactName());
		autoCompleteDocument.setShowPopup(true);
	}

	public void focusSearchBox() {
		JTextComponent editorComponent = (JTextComponent) searchBox.getEditor().getEditorComponent();
		editorComponent.requestFocus();
		editorComponent.selectAll();
	}

	/**
	 * If update is true then whenever the incoming or outgoing levels change value
	 * the graph is updated to display the new levels.
	 * @param update
	 */
	public void setUpdateGraphWhenLevelsChange(boolean update) {
		this.updateGraphOnLevelsChange = update;
	}

	/**
	 * Tries to make the height better depending on what components are shown
	 * and how many items are in the lists.
	 */
	public void adjustHeight() {
		int height = 0;
		if (getSearchPanel().isVisible()) {
			height += 60;
		}
		int middleHeight = 80;
		int rows = 0;
		if (getNodesPanel().isVisible()) {
			rows = Math.min(MAX_ROWS, nodeTypesList.getModel().getSize());
		}
		if (getArcsPanel().isVisible()) {
			rows = Math.min(MAX_ROWS, Math.max(rows, arcTypesList.getModel().getSize()));
		}
		middleHeight = Math.max(middleHeight, 16 + (rows * 18));
		height += middleHeight;
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, height));
	}

	public static void main(String[] args) {
		QueryPanelComponent qpc = new QueryPanelComponent(null);

		JFrame frame = new JFrame("QueryPanelComponent");
		frame.setSize(850, 500);
		//	frame.setPreferredSize(new Dimension(750, 500));	// @tag Shrimp.Java5.setPreferredSize
		frame.setLocation(400, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(qpc, BorderLayout.NORTH);
		frame.pack();
		frame.setVisible(true);
	}

}