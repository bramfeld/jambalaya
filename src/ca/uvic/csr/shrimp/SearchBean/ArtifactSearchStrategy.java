/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.SimpleArtifact;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ProgressDialog;
import ca.uvic.csr.shrimp.util.TableSorter;

/**
 * This search strategy provides a threaded facility
 * searching for software artifacts.
 *
 * @author Jingwei Wu
 */
public class ArtifactSearchStrategy extends SearchStrategy implements Serializable {

	public static final String NAME = "Node Search";

	private JComboBox typeBox;
	private JCheckBox regChk;
	private JCheckBox incrChk;
	private JTable table;
	private JScrollPane scrlResults;
	private JToggleButton filterBtn;
	private JButton selectBtn;
	private JLabel selectLabel;
	private JLabel resultLabel;
	private JPanel inputPanel;
	private JPanel outputPanel;

	private URI presentDir;

	// search mode (exact or regular).
	private int searchMode;

	// whether the search is incremental.
	private boolean incremental;

	// whether the search is case sensitive.
	private boolean caseSensitive;

	//private Vector selectedArtifactTypes;

	//private String artifactNamePattern;


	// The thread deals with searching.
	//private SearchThread searching = null;

	private boolean isSearching = false;

	public ArtifactSearchStrategy() {
		this(null);
	}

	public ArtifactSearchStrategy(URI presentDir) {
		super(NAME);
		setSelectedDataReflector(new ArtifactNameReflector());
		createComponentUI();
		this.presentDir = presentDir;
	}

	public void setArtifactTypes(Vector artifactTypes) {
		typeBox.removeAllItems();
		Collections.sort(artifactTypes, String.CASE_INSENSITIVE_ORDER);

		//Allow the user to search all types
		artifactTypes.add(0, "All");

		for (int i = 0; i < artifactTypes.size(); i++) {
			typeBox.addItem(artifactTypes.elementAt(i));
		}
	}

	/**
	 * Creates the component UI supporting this SearchStrategy.
	 * @return the component UI for this SearchStrategy.
	 */
	protected JComponent createComponentUI() {
		JPanel pnlMain = new JPanel();
		pnlMain.setLayout(new BorderLayout());
		pnlMain.add(getInputPanel(), BorderLayout.NORTH);
		pnlMain.add(getOutputPanel(), BorderLayout.CENTER);
		compUI = pnlMain;
		return compUI;
	}

	/**
	 * Creates the input panel.
	 */
	private JPanel getInputPanel() {
		if (inputPanel == null) {
			inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));

			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			inputPanel.add(topPanel);
			inputPanel.add(bottomPanel);

			typeBox = new JComboBox();
			typeBox.setEditable(false);

			Dimension d = new Dimension(100, 16);
			JLabel lbl = new JLabel("Search pattern: ");
			lbl.setPreferredSize(d);
			topPanel.add(lbl);
			topPanel.add(getSearchBox());
			topPanel.add(getSearchButton());

			lbl = new JLabel("Node type: ");
			lbl.setPreferredSize(d);
			bottomPanel.add(lbl);
			bottomPanel.add(typeBox);
			bottomPanel.add(getCaseCheckBox());
			bottomPanel.add(getRegCheckBox());
			bottomPanel.add(getIncrCheckBox());
		}
		return inputPanel;
	}

	/**
	 * Creates the output panel.
	 */
	private JPanel getOutputPanel() {
		if (outputPanel == null) {
			outputPanel = new JPanel();
			outputPanel.setLayout(new BorderLayout());

			JPanel pnlNorth = new JPanel();
			pnlNorth.setLayout(new BorderLayout());

			JPanel pnlNorthCenter = new JPanel();
			pnlNorthCenter.setLayout(new FlowLayout(FlowLayout.LEFT));
			pnlNorthCenter.add(getBrowseButton());
			pnlNorthCenter.add(getFilterButton());
			pnlNorthCenter.add(getSelectButton());
			pnlNorthCenter.add(new JLabel("Item(s):"));
			selectLabel = new JLabel("");
			pnlNorthCenter.add(selectLabel);
			pnlNorth.add(pnlNorthCenter, BorderLayout.CENTER);

			JPanel pnlNorthEast = new JPanel();
			pnlNorthEast.setLayout(new FlowLayout(FlowLayout.RIGHT));
			resultLabel = new JLabel("");
			pnlNorthEast.add(resultLabel);
			pnlNorth.add(pnlNorthEast, BorderLayout.EAST);

			scrlResults = new JScrollPane(new JList());

			outputPanel.add(pnlNorth, BorderLayout.NORTH);
			outputPanel.add(scrlResults, BorderLayout.CENTER);
		}
		return outputPanel;
	}

	/**
	 * Performs the required search.
	 */
	public void doSearch() {
		String selectedType = (String) typeBox.getSelectedItem();
		if (selectedType == null || selectedType.equals("")) {
			selectedType.equals("All");
		}
		Vector selectedArtifactTypes = new Vector ();
		selectedArtifactTypes.add(selectedType);

		String input = getSearchText();
		if (input == null) {
			return; // Do nothing.
		}

		Vector rawData = null;
		if (incremental) {
			rawData = (Vector) searchResults.clone();
			searchResults.clear(); // Clear results.
		} else {
			SearchRequestEvent e;
			rawData = new Vector();
			e = new SearchRequestEvent(this, rawData);
			fireSearchRequestEvent(e);
			searchResults.clear();
		}
		run(rawData, selectedArtifactTypes, input);
		displaySearchResults();

	}
	
	protected void doBrowse() {
		int index = table.getSelectedRow();
		if (index >= 0) {
			Object dest = getSearchResultsReference().elementAt(index);
			BrowseActionEvent browseAction = new BrowseActionEvent(ArtifactSearchStrategy.this, dest);
			fireBrowseActionEvent(browseAction);
		}
	}
	
	private void doFilter() {
		boolean filterOn = filterBtn.isSelected();
		filterBtn.setText(filterOn ? "Unfilter" : "  Filter  ");
		filterBtn.setToolTipText(filterOn ? "Unfilters (shows) the nodes from the main view" :
											"Filters (hides) the nodes from the main view");
		
		Vector selectedResults = new Vector();
		int[] rows = table.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			int value = rows[i];
			selectedResults.add(getSearchResultsReference().elementAt(value));
		}
		FilterResultsActionEvent event = new FilterResultsActionEvent(ArtifactSearchStrategy.this,
												selectedResults, filterOn);
		fireFilterResultsActionEvent(event);
	}

	/**
	 * Displays search results.
	 */
	private void displaySearchResults() {
		String text;
		if (searchResults == null || searchResults.size() == 0) {

			if (!isSearching()) {
				text = "Found nothing!";
			} else {
				text = "Found nothing yet... searching";
			}
			selectLabel.setText("");
			incrChk.setEnabled(false);
			incrChk.setSelected(incremental);
			getBrowseButton().setEnabled(false);
		} else if (searchResults.size() == 1) {
			if (!isSearching()) {
				text = "Found 1 item!";
			} else {
				text = "Found 1 item...searching";
			}
			incrChk.setEnabled(true);
			incrChk.setSelected(incremental);
		} else {

			incrChk.setEnabled(true);
			incrChk.setSelected(incremental);
			if (!isSearching()) {
				text = "Found " + searchResults.size() + " items!";
			} else {
				text = "Found " + searchResults.size() + " items...searching";
			}
		}
		resultLabel.setText(text);

		TableModel dataModel = new AbstractTableModel() {
			public int getRowCount() {
				return getSearchResultsReference().size();
			}
			public int getColumnCount() {
				return (presentDir != null ? 3 : 2);
			}
			public String getColumnName(int col) {
				if (col == 0) {
					return "Node Name";
				}
				if (col == 1) {
					return "Type";
				}
				if (col == 2) {
					return "URL";
				}
				return null;
			}
			public Object getValueAt(int row, int col) {
				Artifact art = (Artifact) getSearchResultsReference().elementAt(row);
				String value = "";
				if (col == 0) {
					value = " " + art.getName();
				} else if (col == 1) {
					value = art.getType();
				} else if (col == 2) {
					if (art instanceof SimpleArtifact) {
						SimpleArtifact rigiArt = (SimpleArtifact) art;
						if (rigiArt.getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI) != null) {
							value = rigiArt.getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI).toString().substring(presentDir.toString().length());
						}
					}
				}
				return value;
			}
		};
		TableSorter sorter = new TableSorter(dataModel);
		table = new JTable(sorter);
		sorter.addMouseListenerToHeaderInTable(table);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ListSelectionModel selectionModel = table.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int[] rows = table.getSelectedRows();
				String labelText = "";
				for (int i = 0; i < rows.length; i++) {
					int value = rows[i];
					labelText = (i > 0) ? labelText + ", " + (value + 1) : "" + (value + 1);
				}
				selectLabel.setText(labelText);
				getBrowseButton().setEnabled(table.getSelectedRowCount() == 1);
				getFilterButton().setEnabled(table.getSelectedRowCount() > 0);
				getSelectButton().setEnabled(table.getSelectedRowCount() > 0);
			}
		});

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					getBrowseButton().doClick();
				}
			}
		});

		outputPanel.remove(scrlResults);
		scrlResults = new JScrollPane(table);
		outputPanel.add(scrlResults, BorderLayout.CENTER);
		outputPanel.validate();
		outputPanel.repaint();
	}

	/**
	 * Creates the regular-expression checkbox.
	 */
	private JCheckBox getRegCheckBox() {
		if (regChk == null) {
			regChk = new JCheckBox("Regular expression");
			regChk.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int state = e.getStateChange();
					if (state == ItemEvent.SELECTED) {
						searchMode = Matcher.REGEXP_MODE;
					} else {
						searchMode = Matcher.CONTAINS_MODE;
					}
				}
			});
		}
		return regChk;
	}

	/**
	 * Creates the incremental-search checkbox.
	 */
	private JCheckBox getIncrCheckBox() {
		if (incrChk == null) {
			incrChk = new JCheckBox("Search within results");
			incrChk.setEnabled(false);
			incrChk.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					int state = e.getStateChange();
					if (state == ItemEvent.SELECTED) {
						incremental = true;
					} else {
						incremental = false;
					}
				}
			});
		}
		return incrChk;
	}

	/**
	 * Creates a button that filters the selected search results
	 */
	private JToggleButton getFilterButton() {
		if (filterBtn == null) {
			filterBtn = new JToggleButton("  Filter  ", false);
			filterBtn.setToolTipText("Filters (hides) the nodes from the main view");
			filterBtn.setEnabled(false);
			filterBtn.setMargin(BUTTON_INSETS);
			filterBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doFilter();
				}
			});
		}
		return filterBtn;
	}
	/**
	 * Creates a button that filters the selected search results
	 */
	private JButton getSelectButton() {
		if (selectBtn == null) {
			selectBtn = new JButton("Select");
			selectBtn.setToolTipText("Selects the nodes in the main view");
			selectBtn.setEnabled(false);
			selectBtn.setMargin(BUTTON_INSETS);
			selectBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Vector selectedResults = new Vector();
					int[] rows = table.getSelectedRows();
					for (int i = 0; i < rows.length; i++) {
						int value = rows[i];
						selectedResults.add(getSearchResultsReference().elementAt(value));
					}
					SelectResultsActionEvent srae = new SelectResultsActionEvent(ArtifactSearchStrategy.this, selectedResults);
					fireSelectResultsActionEvent(srae);
				}
			});
		}
		return selectBtn;
	}

	/**
	 * Processes the given <code>data</code> that is going to
	 * be displayed on the component UI of this SearchStrategy.
	 * The new search results replaces the old search results.
	 * @param data the data to be examined. Each object in the
	 * collection is treated as raw data and must be checked by
	 * the data reflector in this SearchStrategy. If the data
	 * is <code>null</code> or does not contain any object, all
	 * the search results displayed in the component UI must be
	 * cleaned properly.
	 * @see #getSelectedDataReflector()
	 */
	public void processUpdate(Collection data) {
		processAppend(data);
	}

	/**
	 * Processes the given <code>data</code> that is going to
	 * be displayed on the component UI of this SearchStrategy.
	 * The new search results will be appended after the old
	 * search results.
	 * @param data the data to be examined. Each object in the
	 * collection is treated as raw data and must be checked by
	 * the data reflector in this SearchStrategy.
	 * @see #getSelectedDataReflector()
	 */
	public void processAppend(Collection data) {
		String selectedType = (String) typeBox.getSelectedItem();
		if (selectedType == null || selectedType.equals("")) {
			selectedType.equals("All");
		}
		Vector selectedArtifactTypes = new Vector ();
		selectedArtifactTypes.add(selectedType);

		String input = getSearchText();
		if (input == null) {
			return; // Do nothing.
		}

		run(new Vector(data), selectedArtifactTypes, input);
	}

	public void run(Vector rawData, Vector types, String pattern) {
        // check that the regexp is a valid pattern
        if (searchMode == Matcher.REGEXP_MODE) {
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                String message = "Sorry, the regular expression entered is not a valid pattern.\n\n" + e.getMessage();
                JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message,
                		ApplicationAccessor.getAppName(), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

		isSearching = true;


		ProgressDialog.showProgress();
		ProgressDialog.setSubtitle("Performing Search...");
        // set the cursor to busy
		ApplicationAccessor.waitCursor();
		try {
			DataReflector ref = getSelectedDataReflector();
			Vector searchResultsRef = getSearchResultsReference();
			searchResultsRef.clear();

			// TODO should we check for progress dialog cancelled here
			for (int i = 0; i < rawData.size() /*&& !ProgressDialog.isCancelled()*/; i++) {
				Object o = rawData.elementAt(i);
				if (!ref.accept(o)) {
					continue;
				}
				if (! (types.size() == 1 && types.firstElement().equals("All"))) {
					if (!types.contains(((Artifact) o).getType())) {
						continue;
					}
				}

				String info = ref.reflect(o);
				if (Matcher.match(info, pattern, searchMode, !caseSensitive)) {
					searchResultsRef.add(o);
					if (searchResultsRef.size() > 0 && (searchResultsRef.size() % 10 == 0 || i % 500 == 0)) {
					    ProgressDialog.setNote("Found " + searchResultsRef.size() + (searchResultsRef.size() == 1 ? " item." : " items."));
					}
				}

			}
			ProgressDialog.tryHideProgress();
			isSearching = false;
			fireSearchCompletedEvent();
		} finally {
			ApplicationAccessor.defaultCursor();
		}
	}

	/**
	 * @param file
	 */
	public void setPresentDir(URI file) {
		presentDir = file;
	}

	/**
	 * @param incremental The incremental to set.
	 */
	public void setIncremental(boolean incremental) {
		this.incremental = incremental;
	}

	public boolean isSearching() {
		return isSearching;
	}

	/**
	 * @param searchMode The searchMode to set.
	 */
	public void setSearchMode(int searchMode) {
		this.searchMode = searchMode;
	}

}
