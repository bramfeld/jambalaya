/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.SearchBean;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;


/**
 * This search strategy provides a threaded facility similar to
 * searching tools embedded in text editors and/or file finders.
 *
 * @author Jingwei Wu, Jeff Michaud
 */
public class GrepSearchStrategy extends SearchStrategy implements Serializable, FilenameFilter {

	public static final String NAME = "Source Code Search";

	private HashSet fileExtensions = null;

	private JTable table;
	private JScrollPane scroller;

	private JLabel selectLabel;
	private JLabel resultLabel;

	private JPanel inputPanel;
	private JPanel outputPanel;

	private Vector searchFiles;
	private Vector searchResults;
	private URI searchDir;
	private URI presentDir;

	// whether the search is case sensitive.
	private boolean caseSensitive;

	// The thread deals with searching.
	private SearchThread searching = null;

	private static final int COL_FILENAME = 0;
	private static final int COL_LINE_NUMBER = 1;
	private static final int COL_LINE_STRING = 2;

	/**
	 * Constructs a simple grep search strategy.
	 * @param searchDir
	 * @param presentDir
	 */
	public GrepSearchStrategy(URI searchDir, URI presentDir) {
		super(NAME);
		createComponentUI();
		this.searchDir = searchDir;
		this.presentDir = presentDir;
	}

	public void setDirectories(URI searchDir, URI presentDir) {
		this.searchDir = searchDir;
		this.presentDir = presentDir;
	}
	
	public URI getSearchDir() {
		return searchDir;
	}
	
	public URI getPresentDir() {
		return presentDir;
	}

	/**
	 * Creates the component UI supporting this SearchStrategy.
	 * @return the component UI for this SearchStrategy.
	 */
	protected JComponent createComponentUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getInputPanel(), BorderLayout.NORTH);
		panel.add(getOutputPanel(), BorderLayout.CENTER);
		compUI = panel;
		return panel;
	}

	/**
	 * Creates the input panel.
	 */
	private JPanel getInputPanel() {
		if (inputPanel == null) {
			inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

			inputPanel.add(new JLabel("Search pattern: "));
			inputPanel.add(getSearchBox());
			inputPanel.add(getCaseCheckBox());
			inputPanel.add(getSearchButton());
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

			JPanel north;
			north = new JPanel();
			north.setLayout(new GridLayout(1, 2));

			JPanel panel;
			panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.LEFT));
			panel.add(getBrowseButton());
			panel.add(new JLabel("Item:"));
			selectLabel = new JLabel("");
			panel.add(selectLabel);
			north.add(panel);

			panel = new JPanel();
			panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			resultLabel = new JLabel("");
			panel.add(resultLabel);
			north.add(panel);

			scroller = new JScrollPane(new JList());
			outputPanel.add(north, BorderLayout.NORTH);
			outputPanel.add(scroller, BorderLayout.CENTER);
		}
		return outputPanel;
	}

	/**
	 * Performs the required search.
	 */
	protected void doSearch() {
		String input = getSearchText();
		if (input == null) {
			return;
		}

		getSearchBox().addItem(input);
		if (searching != null) {
			searching.tryStop();
		}
		searchFiles = new Vector();
		getFiles(searchDir);
		searching = new SearchThread(input, searchFiles);
		searching.start();
	}

	protected void doBrowse() {
		//Build the URL from what we know
		int index = table.getSelectedRow();
		String uriStr = (String) table.getValueAt(index, 0);
		//String line = (String) table.getValueAt(index, 1);
		// I don't understand what adding "/~default~" does?!?  It makes the URI invalid anyways
//		if (uriStr.lastIndexOf('/') <= 1) {
//			uriStr = "/~default~" + uriStr;
//		}
		// are we expecting html files?!?
//		uriStr = uriStr.replace('.', '_');
//		uriStr = presentDir.toString() + uriStr + ".html" + "#" + line;

		// try loading the uri this way?!?
		uriStr = searchDir.toString() + uriStr;
		try {
			URI uri = new URI(uriStr);
			BrowseActionEvent bae = new BrowseActionEvent(GrepSearchStrategy.this, uri);
			fireBrowseActionEvent(bae);
		} catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
	}
	
	/**
	 * Displays search results.
	 */
	private void displaySearchResults() {
		String text;
		if (searchResults == null || searchResults.size() == 0) {
			text = "Found nothing!";
			selectLabel.setText("");
			getBrowseButton().setEnabled(false);
		} else {
			int size = searchResults.size();
			text = "Found " + size + " item" + (size == 1 ? "!" : "s!");
		}
		resultLabel.setText(text);

		Object obj;
		String name;
		Vector names = new Vector();
		for (int i = 0; i < searchResults.size(); i++) {
			obj = searchResults.elementAt(i);
			try {
				name = getSelectedDataReflector().getName(obj);
				names.add(name);
			} catch (IllegalArgumentException illegal) {
				illegal.printStackTrace();
			}
		}

		TableModel dataModel = new AbstractTableModel() {
			public int getRowCount() {
				return searchResults.size();
			}
			public int getColumnCount() {
				return 3;
			}
			public String getColumnName(int col) {
				if (col == COL_FILENAME) {
					return "File";
				}
				if (col == COL_LINE_NUMBER) {
					return "Line Number";
				}
				if (col == COL_LINE_STRING) {
					return "Line";
				}
				return null;
			}
			public Object getValueAt(int row, int col) {
				Vector result = (Vector) searchResults.elementAt(row);

				// File - set it to the relative path of the source directory
				if (col == COL_FILENAME) {
					File file = (File) result.elementAt(col);
					return getPartialPath(file);
				}
				// Line Number
				if (col == COL_LINE_NUMBER) {
					return result.elementAt(col);
				}
				// Line
				if (col == COL_LINE_STRING) {
					String line = (String) result.elementAt(col);
					return line.trim();
				}
				return null;
			}
			private String getPartialPath(File file) {
				String path = file.getName();
				File sourceDir = new File(searchDir);
				while ((file.getParentFile() != null) && !file.getParentFile().equals(sourceDir)) {
					file = file.getParentFile();
					path = file.getName() + "/" + path;
				}
				return "/" + path;
			}
		};

		table = new JTable(dataModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				getBrowseButton().setEnabled(true);
			}
		});

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					getBrowseButton().doClick();
				}
				selectLabel.setText("" + (table.getSelectedRow() + 1));
			}
		});

		outputPanel.remove(scroller);
		scroller = new JScrollPane(table);
		outputPanel.add(scroller, BorderLayout.CENTER);
		outputPanel.validate();
		outputPanel.repaint();
	}

	/**
	 * Processes the given <code>data</code> that is going to
	 * be displayed on the compoent UI of this SearchStrategy.
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
		searchResults.clear();
		processAppend(data);
	}

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
	public void processAppend(Collection data) {
		String input = getSearchText();
		if (input == null) {
			return; // Do nothing.
		}

		if (searching != null) {
			searching.tryStop();
		}
		searching = new SearchThread(input, searchFiles);
		searching.start();
	}

	/**
	 * Returns whether or not this strategy has a search running
	 */
	public boolean isSearching() {
		return (searching != null && !searching.isDone());
	}
	
	/** FilenameFilter method */
	public boolean accept(File dir, String name) {
		boolean accept = false;
		File file = new File(dir, name);
		if (file.isFile()) {
			int dot = name.lastIndexOf(".");
			if (dot != -1) {
				String ext = name.substring(dot + 1).toLowerCase();
				if (fileExtensions == null) {
					fileExtensions = new HashSet();
					// all the programming language file types... probably overkill!
					fileExtensions.add("java");
					fileExtensions.add("js");
					fileExtensions.add("c");
					fileExtensions.add("cpp");
					fileExtensions.add("h");
					fileExtensions.add("hpp");
					fileExtensions.add("cs");
					fileExtensions.add("txt");
					fileExtensions.add("text");
					fileExtensions.add("pl");
					fileExtensions.add("cgi");
					fileExtensions.add("asp");
					fileExtensions.add("aspx");
					fileExtensions.add("html");
					fileExtensions.add("htm");
					fileExtensions.add("php");
					fileExtensions.add("php3");
					fileExtensions.add("php4");
					fileExtensions.add("svg");
				}
				accept = fileExtensions.contains(ext);
			}
		} else if (file.isDirectory()) {
			accept = true;
		}
		return accept;
	}
	
	/**
	* Retrieves the java files to be searched
	*/
	private void getFiles(URI uri) {
		try {
			File dir = new File(uri);
			if (dir.exists() && dir.isDirectory()) {
				getFilesRecursively(dir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getFilesRecursively(File parent) {
		String[] children = parent.list(/* FilenameFilter */this);
		for (int i = 0; i < children.length; i++) {
			File file = new File(parent, children[i]);
			if (file.isDirectory()) {
				getFilesRecursively(file);
			} else {
				searchFiles.add(file);
			}
		}
	}
	
	
	/**
	 * This class is created for threaded searching.
	 */
	class SearchThread extends Thread {
		private Vector files;
		private boolean isDone = false;
		private String pattern;

		public SearchThread(String pattern, Vector files) {
			this.files = files;
			this.pattern = pattern;
		}

		public void run() {
			isDone = false;

			// set the cursor to busy
			Container container = getSearchButton().getTopLevelAncestor();
			Cursor cursor = container.getCursor();
			container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			//"grep" files for pattern
			searchResults = new Vector();
			for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                boolean match = false;
                int lineNumber = 1;
                String matchingLine = "";
        		try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                	    match = Matcher.match(line, pattern, Matcher.CONTAINS_MODE, caseSensitive);
                	    if (match) {
                	        matchingLine = line;
                	        break;
                	    }
                	    lineNumber++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (match) {
                    Vector result = new Vector(3);
                    result.add(COL_FILENAME, file);
                    result.add(COL_LINE_NUMBER, "" + lineNumber);
                    result.add(COL_LINE_STRING, matchingLine);
                    searchResults.add(result);
                }
            }

			//Display results
			displaySearchResults();

			// inform listeners that search is complete
			fireSearchCompletedEvent();
			// set the cursor back
			container.setCursor(cursor);

			isDone = true;
		}

		public boolean isDone() {
			return isDone;
		}

		public void tryStop() {
			searching = null;
		}
	}

}
