/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.Collator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import ca.uvic.csr.shrimp.ShrimpColorConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.SearchBean.Matcher;
import ca.uvic.csr.shrimp.SearchBean.SearchResult;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.AutoCompleteDocument;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPainter;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;


/**
 * A panel which lets the user perform searches on a {@link ShrimpView}.  It is a similar idea
 * to what the {@link QueryView} does, but instead of adding and removing objects from the
 * {@link DisplayBean} it just filters the nodes that aren't matched in the search.
 *
 * @author Chris Callendar
 * @date 30-Mar-07
 */
public class QuickSearchPanel extends JRootPane implements KeyListener, FocusListener {

	private static final String CONTAINS 	= "contains";
	private static final String STARTS_WITH = "starts with";
	private static final String ENDS_WITH	= "ends with";
	private static final String EXACT_MATCH = "exact match";
	private static final String REGEXP 		= "regexp";
	private static final String[] MODES = { CONTAINS, STARTS_WITH, ENDS_WITH, EXACT_MATCH, REGEXP };
	private static final int PANEL_HEIGHT = 22;
	private static final int PADDING = 2;

	public static final String KEY_SEARCH_PANEL_VISIBLE = "Quick Search Visible";
	private static final String PROP_SEARCH_MODE = "Quick Search Mode";
	private static final String PROP_AUTOCOMPLETE = "Quick Search Autocomplete On";

	private final Icon lightbulb;
	private final Icon lightbulbDisabled;

	private JPanel mainPanel;
	private JComboBox searchBox;
	private AutoCompleteDocument autoCompleteDocument;
	private JComboBox searchModes;
	private JButton searchBtn;
	private JButton closeBtn;
	private JButton clearBtn;
	private JButton configBtn;
	private JToggleButton autoCompleteBtn;
	private JLabel searchResultsLabel;

	private QuickSearchHelper searchHelper;
	private Properties projectProperties;
	private boolean autocomplete;
	private boolean opaqueButtons = false;

	public QuickSearchPanel(QuickSearchHelper searchHelper) {
		super();
		this.searchHelper = searchHelper;
		this.projectProperties = searchHelper.getShrimpView().getProject().getProperties();
		this.lightbulb = ResourceHandler.getIcon("icon_lightbulb.gif");
		this.lightbulbDisabled = ResourceHandler.getIcon("icon_lightbulb_disabled.gif");

		// buttons look better when they are not opaque on Windows
		this.opaqueButtons = (UIManager.getLookAndFeel().getName().indexOf("Windows") == -1);

		initialize();
		addActions();
		addSearchArtifacts();
		showSearchPanel(isVisibleInProperties());
	}

	private ShrimpView getShrimpView() {
		return searchHelper.getShrimpView();
	}

	public QuickSearchHelper getQuickSearchHelper() {
		return searchHelper;
	}

	/**
	 * Adds actions and loads properties.
	 */
	private void addActions() {
		getAutoCompleteButton().setAction(new DefaultShrimpAction(lightbulb) {
			public void actionPerformed(ActionEvent e) {
				boolean newAutoComplete = !autocomplete;
				setAutoComplete(newAutoComplete);
				projectProperties.setProperty(PROP_AUTOCOMPLETE, "" + newAutoComplete);
			}
		});
		boolean ac = "true".equalsIgnoreCase(projectProperties.getProperty(PROP_AUTOCOMPLETE, "true"));
		setAutoComplete(ac);

		getSearchModesBox().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String searchMode = (String)getSearchModesBox().getSelectedItem();
					projectProperties.setProperty(PROP_SEARCH_MODE, searchMode);
				}
			}
		});
		if (projectProperties.containsKey(PROP_SEARCH_MODE)) {
			String mode = projectProperties.getProperty(PROP_SEARCH_MODE);
			for (int i = 0; i < MODES.length; i++) {
				if (MODES[i].equalsIgnoreCase(mode)) {
					getSearchModesBox().setSelectedItem(mode);
					break;
				}
			}
		}
	}

	private void addSearchArtifacts() {
		ShrimpView shrimpView = getShrimpView();
		try {
			DataBean dataBean = (DataBean) shrimpView.getProject().getBean(ShrimpProject.DATA_BEAN);
	        TreeSet artifactNames = new TreeSet(Collator.getInstance());
        	Vector artifacts = dataBean.getArtifacts(true);
        	for (int i = 0; i < artifacts.size(); i++) {
        		Artifact artifact = (Artifact) artifacts.get(i);
        		artifactNames.add(artifact.getName());
        	}
        	setSearchItems(artifacts);
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initialize() {
		mainPanel = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
		JLabel lbl = new JLabel(" Search: ");
		lbl.setForeground(Color.white);
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
		mainPanel.add(lbl);
		mainPanel.add(getSearchBox());
		mainPanel.add(getAutoCompleteButton());
		mainPanel.add(getSearchModesBox());
		mainPanel.add(getSearchButton());
		mainPanel.add(getClearButton());
		mainPanel.add(getConfigButton());
		
		// @tag Shrimp.QuickSearch : hiding the config button - it is not needed [ccallend, June 27, 2008]
		getConfigButton().setVisible(false);

		mainPanel.setMinimumSize(new Dimension(100, PANEL_HEIGHT));
		mainPanel.setPreferredSize(new Dimension(500, PANEL_HEIGHT));
		mainPanel.setMaximumSize(new Dimension(5000, PANEL_HEIGHT));

		JPanel rightPanel = new TransparentPanel(new BorderLayout());
		rightPanel.add(getSearchResultsLabel(), BorderLayout.CENTER);
		rightPanel.add(getCloseButton(), BorderLayout.EAST);

		GradientPanel contentPane = new GradientPanel(GradientPanel.BG_START, ShrimpColorConstants.SHRIMP_BACKGROUND.darker(), GradientPainter.TOP_TO_BOTTOM);
		contentPane.setLayout(new BorderLayout());
		contentPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.darkGray, 1),
				BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(rightPanel, BorderLayout.EAST);
		setContentPane(contentPane);
	}

	private JLabel getSearchResultsLabel() {
		if (searchResultsLabel == null) {
			searchResultsLabel = new JLabel();
			searchResultsLabel.setOpaque(false);
			searchResultsLabel.setForeground(Color.white);
			searchResultsLabel.setMinimumSize(new Dimension(100, PANEL_HEIGHT));
		}
		return searchResultsLabel;
	}

	/**
	 * Creates and returns the search {@link JComboBox}.
	 * This combo box has an {@link AutoCompleteDocument} which
	 * can be turned on and off.  It is on by default and is populated
	 * with all the names of the nodes in the project.
	 */
	private JComboBox getSearchBox() {
		if (searchBox == null) {
			searchBox = new JComboBox(new DefaultComboBoxModel());
			searchBox.setEditable(true);
			searchBox.setPreferredSize(new Dimension(180, PANEL_HEIGHT));
			searchBox.addFocusListener(this);
	        autoCompleteDocument = new AutoCompleteDocument(searchBox);
	        getSearchTextComponent().setDocument(autoCompleteDocument);
	        getSearchTextComponent().addFocusListener(this);
		}
		return searchBox;
	}

	/**
	 * @return the autcompleting {@link JTextComponent} editor
	 */
	private JTextComponent getSearchTextComponent() {
		return (JTextComponent) getSearchBox().getEditor().getEditorComponent();
	}

	/**
	 * Displays the search modes - contains, starts with, ends with, exact match, and regexp.
	 */
	private JComboBox getSearchModesBox() {
		if (searchModes == null) {
			searchModes = new JComboBox(MODES);
			searchModes.setOpaque(false);
			searchModes.setSelectedIndex(0);
			searchModes.setPreferredSize(new Dimension(86, PANEL_HEIGHT));
			searchModes.addKeyListener(this);
		}
		return searchModes;
	}

	/**
	 * Performs the search.
	 */
	private JButton getSearchButton() {
		if (searchBtn == null) {
			searchBtn = new JButton(new DefaultShrimpAction("Search", ResourceHandler.getIcon("icon_flashlight.gif"), "Perform the search") {
				public void actionPerformed(ActionEvent e) {
					performSearch();
				}
			});
			searchBtn.setOpaque(opaqueButtons);
			searchBtn.setMargin(new Insets(4, 4, 4, 4));
			final Dimension d = new Dimension(80, PANEL_HEIGHT);
			searchBtn.setPreferredSize(d);
			searchBtn.setMinimumSize(d);
		}
		return searchBtn;
	}

	/**
	 * Clears the search text field removes the search results filter.
	 */
	private JButton getClearButton() {
		if (clearBtn == null) {
			clearBtn = new JButton(new DefaultShrimpAction("Clear", ResourceHandler.getIcon("icon_flashlight_clear.gif"), "Clear the search") {
				public void actionPerformed(ActionEvent e) {
					clearSeachText();
				}
			});
			clearBtn.setOpaque(opaqueButtons);
			clearBtn.setMargin(new Insets(4, 4, 4, 4));
			final Dimension d = new Dimension(80, PANEL_HEIGHT);
			clearBtn.setPreferredSize(d);
			clearBtn.setMinimumSize(d);
		}
		return clearBtn;
	}

	/**
	 * Toggles whether autocompletion is turned on.
	 */
	private JButton getConfigButton() {
		if (configBtn == null) {
			configBtn = new JButton(new DefaultShrimpAction(ResourceHandler.getIcon("icon_options.gif"), "Quick Search Options") {
				public void actionPerformed(ActionEvent e) {
					showQuickSearchOptions();
				}
			});
			configBtn.setOpaque(opaqueButtons);
			configBtn.setMargin(new Insets(2, 3, 2, 3));
			final Dimension d = new Dimension(PANEL_HEIGHT, PANEL_HEIGHT);
			configBtn.setPreferredSize(d);
			configBtn.setMinimumSize(d);
		}
		return configBtn;
	}

	/**
	 * Toggles whether autocompletion is turned on.
	 */
	private JToggleButton getAutoCompleteButton() {
		if (autoCompleteBtn == null) {
			autoCompleteBtn = new JToggleButton();
			autoCompleteBtn.setOpaque(opaqueButtons);
			autoCompleteBtn.setMargin(new Insets(2, 3, 2, 3));
			final Dimension d = new Dimension(PANEL_HEIGHT, PANEL_HEIGHT);
			autoCompleteBtn.setPreferredSize(d);
			autoCompleteBtn.setMinimumSize(d);
		}
		return autoCompleteBtn;
	}

	/**
	 * Creates and returns the close button which hides this panel.
	 * The action is added in the constructor.
	 */
	private JButton getCloseButton() {
		if (closeBtn == null) {
			closeBtn = new JButton(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					showSearchPanel(false);
					getShrimpView().reloadToolBar();
				}
			});
			closeBtn.setIcon(ResourceHandler.getIcon("icon_delete_disabled.gif"));
			closeBtn.setRolloverEnabled(true);
			Icon icon = ResourceHandler.getIcon("icon_delete.gif");
			closeBtn.setRolloverIcon(icon);
			closeBtn.setRolloverSelectedIcon(icon);
			closeBtn.setPressedIcon(icon);
			closeBtn.setToolTipText("Hide this search panel");
			closeBtn.setOpaque(opaqueButtons);
			closeBtn.setMargin(new Insets(2, 2, 2, 2));
			closeBtn.setPreferredSize(new Dimension(PANEL_HEIGHT, PANEL_HEIGHT));
		}
		return closeBtn;
	}

	public void focusGained(FocusEvent e) {
		setDefaultButton(getSearchButton());
	}

	public void focusLost(FocusEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			getSearchButton().doClick();
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	private int getStringMatchingMode() {
		String mode = (String) getSearchModesBox().getSelectedItem();
		if (CONTAINS.equals(mode)) {
			return Matcher.CONTAINS_MODE;
		} else if (EXACT_MATCH.equals(mode)) {
			return Matcher.EXACT_MATCH_MODE;
		} else if (STARTS_WITH.equals(mode)) {
			return Matcher.STARTS_WITH_MODE;
		} else if (ENDS_WITH.equals(mode)) {
			return Matcher.ENDS_WITH_MODE;
		} else /* if (REGEXP.equals(mode)) */ {
			return Matcher.REGEXP_MODE;
		}
	}

	private void setStringMatchingMode(int mode) {
		String modeString;
		switch (mode) {
			case Matcher.EXACT_MATCH_MODE :
				modeString = EXACT_MATCH;
				break;
			case Matcher.STARTS_WITH_MODE :
				modeString = STARTS_WITH;
				break;
			case Matcher.ENDS_WITH_MODE :
				modeString = ENDS_WITH;
				break;
			case Matcher.REGEXP_MODE :
				modeString = REGEXP;
				break;
			case Matcher.CONTAINS_MODE :
			default :
				modeString = CONTAINS;
				break;
		}
		getSearchModesBox().setSelectedItem(modeString);
	}

	private void showQuickSearchOptions() {
		final EscapeDialog dlg = EscapeDialog.createDialog(this, "Quick Search Options", true);
		dlg.getContentPane().setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		dlg.getContentPane().add(mainPanel, BorderLayout.CENTER);

		JLabel lbl = new JLabel("Incoming level: ", JLabel.RIGHT);
		lbl.setPreferredSize(new Dimension(80, 16));
		mainPanel.add(lbl);
		final SpinnerNumberModel incomingModel = new SpinnerNumberModel(searchHelper.getIncomingLevel(), QuickSearchHelper.MIN_INCOMING_LEVEL, QuickSearchHelper.MAX_LEVEL, 1);
		mainPanel.add(new JSpinner(incomingModel));

		lbl = new JLabel("Outgoing level: ", JLabel.RIGHT);
		lbl.setPreferredSize(new Dimension(90, 16));
		mainPanel.add(lbl);
		final SpinnerNumberModel outgoingModel = new SpinnerNumberModel(searchHelper.getOutgoingLevel(), QuickSearchHelper.MIN_OUTGOING_LEVEL, QuickSearchHelper.MAX_LEVEL, 1);
		mainPanel.add(new JSpinner(outgoingModel));

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton okBtn = (JButton) pnl.add(new JButton(new AbstractAction("  OK  ") {
			public void actionPerformed(ActionEvent e) {
				searchHelper.setLevels(incomingModel.getNumber().intValue(), outgoingModel.getNumber().intValue());
				searchHelper.saveLevels();
				dlg.setVisible(false);
			}
		}));
		dlg.setDefaultButton(okBtn);
		pnl.add(new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dlg.setVisible(false);
			}
		}));
		pnl.add(new JButton(new AbstractAction("Defaults") {
			public void actionPerformed(ActionEvent e) {
				incomingModel.setValue(new Integer(QuickSearchHelper.DEFAULT_INCOMING_LEVEL));
				outgoingModel.setValue(new Integer(QuickSearchHelper.DEFAULT_OUTGOING_LEVEL));
			}
		}));
		pnl.add(new JButton(new AbstractAction("Help") {
			public void actionPerformed(ActionEvent e) {
				String msg = "Levels restrict how many neighbouring nodes are displayed.\n" +
							"For immediate neighbours choose a value of 1 for both levels.\n" +
							"For the parent hierarchy choose a value of -1 for incoming levels.";
				JOptionPane.showMessageDialog(dlg, msg, "Help", JOptionPane.INFORMATION_MESSAGE);
			}
		}));
		dlg.getContentPane().add(pnl, BorderLayout.SOUTH);

		dlg.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dlg.setVisible(false);
				}
			}
		});

		dlg.pack();
		ShrimpUtils.centerWindowOnParent(dlg, this);
		Dimension d = new Dimension(350, 100);
		dlg.setPreferredSize(d);
		dlg.setSize(d);
		dlg.getRootPane().setDefaultButton(okBtn);
		dlg.setVisible(true);	// blocks
	}

	private void setSearchResults(SearchResult result) {
		getSearchResultsLabel().setText(result.toString() + " ");
		getSearchResultsLabel().setForeground(result.isError() ? Color.red :
			(result.isCancelled() ? Color.gray : Color.white));
	}

	/**
	 * Performs the search using the text in the search text editor component.
	 */
	private void performSearch() {
		String searchText = getSearchTextComponent().getText().trim();
		SearchResult result = searchHelper.performSearch(searchText, getStringMatchingMode());
		setSearchResults(result);
	}

	/**
	 * Performs a search on the given text and using the given string matching mode.
	 * @param searchText
	 * @param matchingMode the matching mode
	 * @see Matcher#CONTAINS_MODE
	 * @see Matcher#STARTS_WITH_MODE
	 * @see Matcher#ENDS_WITH_MODE
	 * @see Matcher#EXACT_MATCH_MODE
	 * @see Matcher#REGEXP_MODE
	 */
	public void search(String searchText, int matchingMode) {
		if (searchText != null) {
			getSearchTextComponent().setText(searchText);
			getSearchBox().hidePopup();
			setStringMatchingMode(matchingMode);
			performSearch();
		}
	}

	public void clearSeachText() {
		getSearchTextComponent().setText("");
		getSearchBox().hidePopup();
		searchHelper.clearSearch();
		getSearchResultsLabel().setText("");
	}

	public void setSearchItems(Vector items) {
		DefaultComboBoxModel model = new DefaultComboBoxModel(items);
		getSearchBox().setModel(model);
		clearSeachText();
	}

	public void setAutoComplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
		autoCompleteDocument.setShowPopup(autocomplete);
		autoCompleteDocument.setAutoComplete(autocomplete);
		getAutoCompleteButton().setIcon(autocomplete ? lightbulb : lightbulbDisabled);
		getAutoCompleteButton().setToolTipText((autocomplete ? "Turn off autocompletion" : "Turn on autocompletion"));
		getAutoCompleteButton().setSelected(autocomplete);
	}

	/**
	 * Shows or hides this panel.
	 */
	public void showSearchPanel(boolean visible) {
		setVisible(visible);
		projectProperties.setProperty(KEY_SEARCH_PANEL_VISIBLE, "" + visible);
		Container parent = getParent();
		if (parent != null) {
			parent.invalidate();
			parent.validate();
			parent.repaint();
		}
	}

	/**
	 * Checks the visible setting in the project properties file.
	 */
	public boolean isVisibleInProperties() {
		return "true".equalsIgnoreCase(projectProperties.getProperty(KEY_SEARCH_PANEL_VISIBLE, "true"));
	}

}
