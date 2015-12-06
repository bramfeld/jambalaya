/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.AutoCompleteDocument;


/**
 * SearchStrategy is the base class for any search strategy to
 * be added to the SearchBean. A SearchStrategy is composed of
 * a set of {@link DataReflector}s and a user interface produced
 * by the method {@link #getComponentUI()}). How to search for
 * objects and display the search results is determined by its
 * subclasses.
 *
 * @author Jingwei Wu
 */
public abstract class SearchStrategy implements Serializable {

	protected static final Insets BUTTON_INSETS = new Insets(3, 5, 2, 4);

	/**
	 * The name of this SearchStrategy.
	 */
	private String name;

	/**
	 * The data reflectors used by this SearchStrategy.
	 */
	private Vector dataReflectors;

	/**
	 * The data reflector selected by this SearchStrategy.
	 */
	private DataReflector selDataReflector = null;

	/**
	 * The listeners which listen to the seach requests
	 * made by this SearchStrategy.
	 */
	private transient Vector requestListeners;

	/**
	 * The listeners which listen to the browsing action
	 * performed on a selected search result.
	 */
	private transient Vector browseListeners;

	/**
	 * The listeners which listen for the filter results action
	 * performed on selected search results.
	 */
	private transient Vector filterResultsListeners;

	/**
	 * The listeners which listen for the select results action
	 * performed on selected search results.
	 */
	private transient Vector selectResultsListeners;

	/**
	 * The storage for the matched data, which is often
	 * referred to as the search results.
	 */
	protected transient Vector searchResults;

	/**
	 * The component UI supporting this SearchStrategy.
	 */
	protected transient JComponent compUI;

	/**
	 *  A list of search completed listeners
	 */
	protected Vector searchCompletedListeners;

	private JButton browseBtn;
	private JCheckBox caseChk;
	private JComboBox patBox;

	private JButton searchBtn;

	/**
	 * Constructs a new SearchStrategy.
	 * @param name the name of this SearchStrategy.
	 */
	public SearchStrategy(String name) {
		this.name = name;
		dataReflectors = new Vector();
		requestListeners = new Vector();
		browseListeners = new Vector();
		filterResultsListeners = new Vector();
		selectResultsListeners = new Vector();
		searchResults = new Vector();
		searchCompletedListeners = new Vector();
	}

	/**
	 * Gets the name of this SearchStrategy.
	 * @return the name of this SearchStrategy.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns whether or not this strategy has a search running
	 */
	public abstract boolean isSearching();

	/**
	* Gets search results.
	* @return a reference to a vector of matched data
	*/
	protected Vector getSearchResultsReference() {
		return searchResults;
	}

	public Vector getSearchResults() {
		return new Vector (searchResults);
	}

	/**
	 * Gets all the data reflectors added to this SearchStrategy,
	 * @return a vector of data relfectors.
	 */
	public Vector getAllDataReflectors() {
		return dataReflectors;
	}

	/**
	 * Adds a data reflector to this SearchStrategy. The added
	 * reflector can be used to reflect information of objects
	 * in the raw data to be processed.
	 * @param reflector the reflector to be added.
	 * @see DataReflector
	 */
	public void addDataReflector(DataReflector reflector) {
		if (reflector != null) {
			if (!dataReflectors.contains(reflector)) {
				dataReflectors.add(reflector);
			}
		}
	}

	/**
	 * Removes a data reflector from this SearchStrategy. The
	 * removed reflector can be used to reflect information of
	 * objects in the raw data to be processed.
	 * @param reflector the reflector to be removed.
	 * @see DataReflector
	 */
	public void removeDataReflector(DataReflector reflector) {
		if (reflector != null) {
			dataReflectors.remove(reflector);
		}
	}

	/**
	 * Gets the selected data reflector of this SearchStrategy.
	 * This reflector is used to reflect information of objects
	 * in the raw data to be processed.
	 * @return the selected data reflector.
	 * @see DataReflector
	 */
	public DataReflector getSelectedDataReflector() {
		if (selDataReflector == null) {
			selDataReflector = new DefaultDataReflector();
		}
		return selDataReflector;
	}

	/**
	 * Sets the selected reflector for this SearchStrategy.
	 * If the specified <code>reflector</code> is <code>null</code>,
	 * a {@link DefaultDataReflector} will be returned by the method
	 * of {@link #getSelectedDataReflector}.
	 * @param reflector the data reflector to be selected.
	 * @see DataReflector
	 * @see DefaultDataReflector
	 */
	public void setSelectedDataReflector(DataReflector reflector) {
		if (reflector != null) {
			selDataReflector = reflector;
			if (!dataReflectors.contains(reflector)) {
				dataReflectors.add(reflector);
			}
		}
	}

	/**
	 * Gets the component UI supporting this SearchStrategy.
	 * @return the component UI for this SearchStrategy.
	 */
	public JComponent getComponentUI() {
		if (compUI == null) {
			compUI = createComponentUI();
		}
		return compUI;
	}

	/**
	 * Creates the component UI supporting this SearchStrategy.
	 * @return the component UI for this SearchStrategy.
	 */
	protected abstract JComponent createComponentUI();

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
	public abstract void processUpdate(Collection data);

	/**
	 * Processes the given <code>data</code> that is going to
	 * be displayed on the compoent UI of this SearchStrategy.
	 * The new search results will be appended after the old
	 * search results.
	 * @param data the data to be examined. Each object in the
	 * collection is treated as raw data and must be checked by
	 * the data reflector in this SearchStrategy.
	 * @see #getSelectedDataReflector()
	 */
	public abstract void processAppend(Collection data);

	/**
	 * Adds a {@link SearchRequestEvent} listener to this SearchStrategy.
	 * @param l the listener to be added.
	 */
	public void addSearchRequestListener(SearchRequestListener l) {
		if (requestListeners == null) {
			requestListeners = new Vector();
		}
		if (l != null) {
			requestListeners.addElement(l);
		}
	}

	/**
	 * Removes a {@link SearchRequestEvent} listener to this SearchStrategy.
	 * @param l the listener to be removed.
	 */
	public void removeSearchRequestListener(SearchRequestListener l) {
		if (requestListeners == null) {
			requestListeners = new Vector();
		}
		if (l != null) {
			requestListeners.removeElement(l);
		}
	}

	/**
	 * Fires an SearchRequestEvent to the listeners.
	 * @param e the event to be fired.
	 */
	protected void fireSearchRequestEvent(SearchRequestEvent e) {
		for (int i = 0; i < ((Vector)requestListeners.clone()).size(); i++) {
			((SearchRequestListener) requestListeners.elementAt(i)).requestMade(e);
		}
	}

	/**
	 * Adds a {@link BrowseActionEvent} listener to this SearchStrategy.
	 * @param l the listener to be added.
	 */
	public void addBrowseActionListener(BrowseActionListener l) {
		if (l != null) {
			browseListeners.addElement(l);
		}
	}

	/**
	 * Removes a {@link BrowseActionEvent} listener to this SearchStrategy.
	 * @param l the listener to be removed.
	 */
	public void removeBrowseActionListener(BrowseActionListener l) {
		if (l != null) {
			browseListeners.removeElement(l);
		}
	}

	/**
	 * Fires a BrowseActionEvent to the listeners.
	 * @param e the event to be fired.
	 */
	public void fireBrowseActionEvent(BrowseActionEvent e) {
		for (int i = 0; i < ((Vector)browseListeners.clone()).size(); i++) {
			((BrowseActionListener) browseListeners.elementAt(i)).browse(e);
		}
	}

	/**
	 * Adds a SearchCompletedListener to this search strategy, if not already present.
	 */
	public void addSearchCompletedListener(SearchCompletedListener scl) {
		if (!searchCompletedListeners.contains(scl)) {
			searchCompletedListeners.add(scl);
		}
	}

	/**
	 * Remove a SearchCompletedListener to this search strategy, if present.
	 */
	public void removeSearchCompletedListener(SearchCompletedListener scl) {
		if (searchCompletedListeners.contains(scl)) {
			searchCompletedListeners.remove(scl);
		}
	}

	/**
	 * Fires a searchCompletedEvent to all SearchCompletedListeners added
	 * to this search strategy.
	 */
	protected void fireSearchCompletedEvent() {
		SearchCompletedEvent sce = new SearchCompletedEvent(getSearchResultsReference(), this);
		for (Enumeration e = searchCompletedListeners.elements(); e.hasMoreElements();) {
			SearchCompletedListener scl = (SearchCompletedListener) e.nextElement();
			scl.searchCompleted(sce);
		}
	}

	/**
	 * Adds a FilterResultsActionListener to this search strategy, if not already present.
	 */
	public void addFilterResultsActionListener(FilterResultsActionListener fral) {
		if (!filterResultsListeners.contains(fral)) {
			filterResultsListeners.add(fral);
		}
	}

	/**
	 * Remove a FilterResultsActionListener to this search strategy, if present.
	 */
	public void removeFilterResultsActionListener(FilterResultsActionListener fral) {
		if (filterResultsListeners.contains(fral)) {
			filterResultsListeners.remove(fral);
		}
	}

	/**
	 * Fires a FilterResultsActionEvent to all FilterResultsActionListeners added
	 * to this search strategy.
	 */
	protected void fireFilterResultsActionEvent(FilterResultsActionEvent frae) {
		for (Enumeration e = filterResultsListeners.elements(); e.hasMoreElements();) {
			FilterResultsActionListener fral = (FilterResultsActionListener) e.nextElement();
			fral.filterResults(frae);
		}
	}

	/**
	 * Adds a SelectResultsActionListener to this search strategy, if not already present.
	 */
	public void addSelectResultsActionListener(SelectResultsActionListener sral) {
		if (!selectResultsListeners.contains(sral)) {
			selectResultsListeners.add(sral);
		}
	}

	/**
	 * Remove a SelectResultsActionListener to this search strategy, if present.
	 */
	public void removeSelectResultsActionListener(SelectResultsActionListener sral) {
		if (selectResultsListeners.contains(sral)) {
			selectResultsListeners.remove(sral);
		}
	}

	/**
	 * Fires a SelectResultsActionEvent to all SelectResultsActionListeners added
	 * to this search strategy.
	 */
	protected void fireSelectResultsActionEvent(SelectResultsActionEvent srae) {
		for (Enumeration e = selectResultsListeners.elements(); e.hasMoreElements();) {
			SelectResultsActionListener fral = (SelectResultsActionListener) e.nextElement();
			fral.selectResults(srae);
		}
	}
	
	protected abstract void doBrowse();
	protected abstract void doSearch();
	

	/**
	 * Creates the browse button that cause a BrowseActionEvent
	 * be fired to listeners.
	 */
	protected JButton getBrowseButton() {
		if (browseBtn == null) {
			browseBtn = new JButton("Browse");
			browseBtn.setToolTipText("Navigates to the selected node in the main view");
			browseBtn.setEnabled(false);
			browseBtn.setMargin(BUTTON_INSETS);
			browseBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doBrowse();
				}
			});
		}
		return browseBtn;
	}

	/**
	 * Creates the search button.
	 */
	protected JButton getSearchButton() {
		if (searchBtn == null) {
			searchBtn = new JButton(new AbstractAction("Search", ResourceHandler.getIcon("icon_search.gif")) {
				public void actionPerformed(ActionEvent e) {
					doSearch();
				}
			});
			searchBtn.setToolTipText("Performs the search");
		}
		return searchBtn;
	}

	/**
	 * Creates the case-sensitive checkbox.
	 */
	protected JCheckBox getCaseCheckBox() {
		if (caseChk == null) {
			caseChk = new JCheckBox("Case Sensitive");
			caseChk.setToolTipText("Check this box if you want to perform case sensitive searches");
		}
		return caseChk;
	}
	
	protected boolean isCaseSensitive() {
		return getCaseCheckBox().isSelected();
	}
	
	/**
	 * @param caseSensitive The caseSensitive to set.
	 */
	public void setCaseSensitive(boolean caseSensitive) {
		getCaseCheckBox().setSelected(caseSensitive);
	}
	protected JComboBox getSearchBox() {
		if (patBox == null) {
			patBox = new JComboBox();
			patBox.setOpaque(false);
			patBox.setEditable(true);
			patBox.setMaximumRowCount(10);
			patBox.setMinimumSize(new Dimension(100, 16));
			patBox.setPreferredSize(new Dimension(220, 20));
	
			// we want to start searching immediately if the user pushing the Enter key
			JTextComponent editorComponent = (JTextComponent) patBox.getEditor().getEditorComponent();
			editorComponent.addKeyListener(new KeyAdapter () {
			    public void keyReleased(KeyEvent e) {
			        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			            getSearchButton().doClick();
			        }
			    }
			});
			
			// autocomplete on all the node names in the databean
			AutoCompleteDocument doc = new AutoCompleteDocument(patBox);
			editorComponent.setDocument(doc);
		}
		return patBox;
	}
	
	public void setSearchText(String text) {
		getSearchBox().setSelectedItem(text);
		JTextComponent editor = (JTextComponent)getSearchBox().getEditor().getEditorComponent();
		editor.setText(text);
		editor.selectAll();
		editor.requestFocus();
		getSearchBox().setPopupVisible(false);
	}
	
	protected String getSearchText() {
		String text = "";
		if (patBox.getSelectedItem() != null) {
			text = patBox.getSelectedItem().toString();
		}
		return text;
	}

	public void setSearchItems(Collection items) {
		DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(items));
		getSearchBox().setModel(model);
		((JTextComponent)getSearchBox().getEditor().getEditorComponent()).setText("");
	}

}
