/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ActionManager.ShrimpAction;
import ca.uvic.csr.shrimp.util.BrowserLauncher;
import ca.uvic.csr.shrimp.util.ButtonEditor;
import ca.uvic.csr.shrimp.util.ButtonRenderer;
import ca.uvic.csr.shrimp.util.TableSorter;

/**
 * This is the {@link TableModel} used to display {@link NodeDocument}s that are attached
 * to {@link Artifact}s.  It displays the file icon, filename, directory, and a remove button to
 * remove the document from the artifact (and the table).
 *
 * @author Chris Callendar
 * @date 28-Sep-07
 */
class NodeDocumentsTableModel extends AbstractTableModel {

	private static final String REMOVE = "Remove";
	private static final String OPEN = "Open";

	private static boolean IS_WINDOWS = false;
	private static boolean IS_MAC = false;
	static {
		String osName = System.getProperty("os.name", "");
		IS_WINDOWS = (osName.toLowerCase().indexOf("windows") != -1);
		IS_MAC = !IS_WINDOWS && (osName.toLowerCase().indexOf("mac") != -1);
	}

	private static final int COL_ICON = 0;
	private static final int COL_FILENAME = 1;
	private static final int COL_DIRECTORY = 2;
	private static final int COL_OPEN = 3;
	private static final int COL_REMOVE = 4;
	private static final String NO_CONTENT = "[no annotation]";

	private String[] columns = new String[] { ""/* icon */, "Name", "Description", "Open", "Remove?" };

	private List/*<NodeDocument>*/ allDocuments;
	private List/*<NodeDocument>*/ fileDocuments;
	private List/*<NodeDocument>*/ annotationDocuments;
	private HashSet/*<NodeDocument>*/ unloadableDocs;
	private NodeDocument selectedDocument;

	// save changes to the selected node document when the text editor loses focus
	// but only if the document is editable
	private FocusListener focusListener = new FocusAdapter() {
		public void focusLost(FocusEvent e) {
			if ((e.getComponent() instanceof JTextComponent) && (selectedDocument != null) &&
					selectedDocument.canEdit()) {
				String content = ((JTextComponent)e.getComponent()).getText();
				if (!NO_CONTENT.equals(content)) {
					selectedDocument.setContent(content);
				}
			}
		}
	};

	private HyperlinkListener hyperlinkListener = null;

	public NodeDocumentsTableModel() {
		this.allDocuments = new ArrayList/*<NodeDocument>*/();
		this.fileDocuments = new ArrayList/*<NodeDocument>*/();
		this.annotationDocuments = new ArrayList/*<NodeDocument>*/();
		this.unloadableDocs = new HashSet/*<NodeDocument>*/();
	}

	/**
	 * Returns the file documents - Don't EDIT.
	 */
	public List getFileDocuments() {
		return fileDocuments;
	}

	public List getAnnotationDocuments() {
		return annotationDocuments;
	}

	/**
	 * Checks if there are any annotations that have content.
	 * @return
	 */
	public boolean hasAnnotations() {
		boolean has = false;
		for (Iterator iter = annotationDocuments.iterator(); iter.hasNext(); ) {
			NodeDocument doc = (NodeDocument) iter.next();
			if (doc.hasContent()) {
				has = true;
				break;
			}
		}
		return has;
	}

	public boolean addDocument(NodeDocument doc) {
		if (!allDocuments.contains(doc)) {
			allDocuments.add(doc);
			if (doc.isAnnotation()) {
				annotationDocuments.add(doc);
			} else {
				fileDocuments.add(doc);
			}
			fireTableDataChanged();
			return true;
		}
		return false;
	}

	/**
	 * Adds the file to the table if it doesn't already exist.
	 * @param file the file to add
	 * @return true if the file was added, false if it already exists
	 */
	public boolean addDocument(String file) {
		// check if we already have this file attached - don't want duplicates
		for (Iterator iter = allDocuments.iterator(); iter.hasNext(); ) {
			NodeDocument doc = (NodeDocument) iter.next();
			if (file.equalsIgnoreCase(doc.getPath())) {
				return false;
			}
		}
		NodeDocument doc = new NodeDocument(file);
		return addDocument(doc);
	}

	public void removeDocument(NodeDocument docToRemove) {
		// now check if we can remove it from the table
		if (docToRemove.canRemove()) {
			// now remove it from the table
			boolean found = false;
			int row = 0;
			for (Iterator iter = allDocuments.iterator(); iter.hasNext(); ) {
				NodeDocument doc = (NodeDocument) iter.next();
				if (doc.equals(docToRemove)) {
					iter.remove();
					found = true;
					if (doc.isAnnotation()) {
						annotationDocuments.remove(doc);
					} else {
						fileDocuments.remove(doc);
					}
					break;
				}
				row++;
			}
			if (found) {
				fireTableDataChanged();
			}
		}
	}

	/**
	 * Remove all documents that can removed, but leaves the annotation documents.
	 */
	public void removeAllFileDocuments() {
		for (Iterator iter = allDocuments.iterator(); iter.hasNext(); ) {
			NodeDocument doc = (NodeDocument) iter.next();
			if (doc.canRemove() && !doc.isAnnotation()) {
				iter.remove();
				fileDocuments.remove(doc);
			}
		}
		fireTableDataChanged();
	}

	public NodeDocument getDocument(int index) {
		NodeDocument doc = null;
		if ((index >= 0) && (index < size())) {
			doc = (NodeDocument) allDocuments.get(index);
		}
		return doc;
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
	    return columns[column];
	}

	public Object getValueAt(int row, int column) {
		Object value = null;
		if ((column >= 0) && (column < getColumnCount()) && (row >= 0) && (row < getRowCount())) {
			NodeDocument doc = (NodeDocument) this.allDocuments.get(row);
			switch (column) {
				case COL_ICON :
					value = doc.getIcon();
					break;
				case COL_FILENAME :
					value = doc.getFilename();
					break;
				case COL_DIRECTORY :
					value = doc.getDirectory();
					break;
				case COL_OPEN :
					value = OPEN;
					break;
				case COL_REMOVE :
					value = REMOVE;
					break;
			}
		}
		return value;
	}

	public Class getColumnClass(int columnIndex) {
		switch (columnIndex) {
			case COL_ICON :
				return Icon.class;
			case COL_OPEN :
			case COL_REMOVE :
				return JButton.class;
			default :
				return String.class;
		}
	}

	public int getRowCount() {
		return size();
	}

	public int size() {
		return allDocuments.size();
	}

	public boolean isCellEditable(int row, int col) {
		boolean canEdit = (col == COL_OPEN) || (col == COL_REMOVE);
		return canEdit;
	}

	/**
	 * Initializes and returns the table which displays the documents.
	 */
	public JTable createDocumentsTable(JEditorPane editor) {
		TableSorter sorter = new TableSorter(this);
		final JTable table = new JTable(sorter);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.getColumnModel().getColumn(COL_ICON).setPreferredWidth(20);
		table.getColumnModel().getColumn(COL_ICON).setMaxWidth(20);
		table.getColumnModel().getColumn(COL_FILENAME).setPreferredWidth(100);
		table.getColumnModel().getColumn(COL_DIRECTORY).setPreferredWidth(300);

		ShrimpAction openAction = createOpenDocumentAction(editor, table);
		table.getColumnModel().getColumn(COL_OPEN).setPreferredWidth(60);
		table.getColumnModel().getColumn(COL_OPEN).setCellRenderer(new ButtonRenderer());
		table.getColumnModel().getColumn(COL_OPEN).setCellEditor(new ButtonEditor(new JCheckBox(), openAction));

		ShrimpAction removeAction = createRemoveDocumentAction(editor, table);
		table.getColumnModel().getColumn(COL_REMOVE).setPreferredWidth(70);
		table.getColumnModel().getColumn(COL_REMOVE).setCellRenderer(new RemoveButtonRenderer());
		table.getColumnModel().getColumn(COL_REMOVE).setCellEditor(new RemoveButtonEditor(new JCheckBox(), removeAction));

		table.getSelectionModel().addListSelectionListener(createTableSelectionListener(editor, table));
		table.addMouseListener(createTableDoubleClickListener(openAction));
		table.setRowHeight(20);
		sorter.addMouseListenerToHeaderInTable(table);

		return table;
	}

	/**
	 * Listens for double clicks on a row and opens the document associated with that row.
	 * @param openAction the action which performs the open.
	 */
	private MouseListener createTableDoubleClickListener(final ShrimpAction openAction) {
		MouseListener listener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable table = (JTable) e.getSource();
					int row = table.getSelectedRow();
					int col = table.getSelectedColumn();
					// we don't want to do anything if a button columns was clicked
					if ((row != -1) && (col != COL_OPEN) && (col != COL_REMOVE)) {
						openAction.actionPerformed(new ActionEvent(e.getSource(), 0, "Open"));
					}
				}
			}
		};
		return listener;
	}

	/**
	 * Creates and returns a listener than handles viewing documents.
	 */
	private ListSelectionListener createTableSelectionListener(final JEditorPane editor, final JTable table) {
		// attempts to view the document, if the document can't be loaded it won't be attempted again.
		ListSelectionListener listener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					NodeDocument doc = getDocument(table.getSelectedRow());
					viewDocument(editor, doc);
				}
			}
		};
		return listener;
	}


	private ShrimpAction createOpenDocumentAction(final JEditorPane editor, final JTable table) {
		DefaultShrimpAction openAction = new DefaultShrimpAction() {
			public void actionPerformed(ActionEvent e) {
				NodeDocument doc = getDocument(table.getSelectedRow());
				if ((doc != null) && doc.exists()) {
					try {
						if (doc.canOpen()) {
							editor.setForeground(Color.black);
							editor.setText("Attempting to opening document '" + doc.getPath() + "'...");
							openDocument(doc);
						}
					} catch (Exception ex) {
						editor.setForeground(Color.red);
						editor.setText("Error opening document '" + doc.getPath() + "':\n" + ex.getMessage());
					}
				}
			}
		};
		return openAction;
	}

	private ShrimpAction createRemoveDocumentAction(final JEditorPane editor, final JTable table) {
		DefaultShrimpAction removeAction = new DefaultShrimpAction() {
			public void actionPerformed(ActionEvent e) {
				NodeDocument doc = getDocument(table.getSelectedRow());
				if (doc != null)  {
					removeDocument(doc);
					table.clearSelection();
					editor.setText("");
				}
			}
		};
		return removeAction;
	}


	/**
	 * Attempts to open the file in an external viewer.  If the file is a PDF or a web document (html, etc)
	 * then the {@link BrowserLauncher} is used, otherwise a {@link Process} is used.
	 */
	protected void openDocument(NodeDocument doc) throws Exception {
		if ((doc.getType() == NodeDocument.TYPE_PDF) || (doc.getType() == NodeDocument.TYPE_WEB) ||
				(doc.getType() == NodeDocument.TYPE_URL)) {
			BrowserLauncher.openURL(doc.getURL().toString());
		} else {
			// try to open the file using a process - OS dependent
			// see http://www.rgagnon.com/javadetails/java-0014.html for more info
			String file = doc.getPath();
			if (IS_WINDOWS) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler \"" + file + "\"");
			} else if (IS_MAC) {
				Runtime.getRuntime().exec("open " + file);
			} else {
				// Assume Linux?
				String[] cmd = {"/bin/sh", "-c", file};
				Runtime.getRuntime().exec(cmd);
			}
		}
	}

	/**
	 * Attempts to load a document into the editor.
	 * If the document can't be loaded then it will not be attempted again.
	 */
	protected void viewDocument(JEditorPane editor, NodeDocument doc) {
		if ((doc != null) && !unloadableDocs.contains(doc)) {
			boolean loaded = setDocumentToEditor(editor, doc);
			if (!loaded) {
				unloadableDocs.add(doc);
			}
		} else {
			editor.setEditorKit(editor.getEditorKitForContentType("text/plain"));
			editor.setText("");
		}
	}

	/**
	 * Sets the document into the editor.
	 * If an exception occurs the stack trace is put inside the editor with red text.
	 */
	private boolean setDocumentToEditor(JEditorPane editor, NodeDocument doc) {
		selectedDocument = doc;
		boolean ok = true;
		try {
			if (editor.isEditable()) {
				editor.setEditable(false);
				editor.removeFocusListener(focusListener);
			}
			if (hyperlinkListener != null) {
				editor.removeHyperlinkListener(hyperlinkListener);
				hyperlinkListener = null;
			}

			String type = doc.getType();
			if (!doc.exists()) {
				throw new FileNotFoundException("The document '" + doc.getPath() + " does not exist.");
			}

    		editor.setEditorKit(editor.getEditorKitForContentType("text/plain"));
			editor.setText("");
			editor.setForeground(Color.black);

			// check if it is an annotation or some other kind of text document (not a file)
			String content = doc.getContent();
			if (content != null) {
				String contentType = doc.getContentType();
				if (contentType != null) {
		    		editor.setEditorKit(editor.getEditorKitForContentType(contentType));
				}
				editor.setText((content.length() == 0 ? NO_CONTENT : content));
				if (doc.canEdit()) {
					editor.setEditable(true);
					editor.addFocusListener(focusListener);
					editor.selectAll();
					editor.requestFocus();
				}
				hyperlinkListener = doc.getHyperlinkListener();
				if (hyperlinkListener != null) {
					editor.addHyperlinkListener(hyperlinkListener);
				}
			}
			// assume it is a file
			else {
				URL url = doc.getURL();
				// TODO do we want to display URLs?  Some webpages are pretty complicated...
				if (NodeDocument.TYPE_TEXT.equals(type) || NodeDocument.TYPE_WEB.equals(type)) {
		    		editor.setPage(url);
		    	} else if (NodeDocument.TYPE_IMAGE.equals(type)) {
		    		// embed the image inside HTML, not sure if this is the easiest way to do it...
		    		editor.setEditorKit(new HTMLEditorKit());
		    		String html = "<html><body><img src=\"" + url.toString() + "\"></body></html>";
		    		editor.setText(html);
		    	} else if (NodeDocument.TYPE_RTF.equals(type)) {
		    		editor.setEditorKit(new RTFEditorKit());
		    		editor.setPage(url);
		    	} else if (NodeDocument.TYPE_PDF.equals(type)) {
		    		editor.setText("Click the Open button to load the PDF file in your browser.");
		    		// I don't think we want to always open the PDF file when the table selection changes
		    		//BrowserLauncher.openURL(url.toString());
		    	} else if (NodeDocument.TYPE_URL.equals(type)) {
		    		editor.setText("Click the Open button to open the website in your browser.");
		    		// I don't think we want to always open the website when the table selection changes
		    		//BrowserLauncher.openURL(url.toString());
		    	} else {
		    		editor.setText("This document is not a text document and cannot be displayed here.");
		    		editor.setForeground(Color.gray);
		    	}
			}


			// set the scroll bars positions
			if ((editor.getParent().getParent() instanceof JScrollPane)) {
				JScrollPane scroll = (JScrollPane)editor.getParent().getParent();
				scroll.getVerticalScrollBar().setValue(0);
				scroll.getHorizontalScrollBar().setValue(0);
			}

			editor.repaint();
		} catch (Exception ex) {
			ok = false;
			editor.setForeground(Color.red);
			editor.setText(ex.getMessage());
			/* Stack trace:
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(new BufferedWriter(writer)));
			String stack = writer.toString();
			editor.setText(stack); */
			selectedDocument = null;
		}
		return ok;
	}

	protected boolean canRemove(Object documentObject) {
		boolean removable = true;
		if (documentObject instanceof NodeDocument) {
			NodeDocument doc = (NodeDocument) documentObject;
			removable = doc.canRemove();
		}
		return removable;
	}

	class RemoveButtonRenderer extends ButtonRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			// update the enablement
			comp.setEnabled(canRemove(getDocument(row)));
			return comp;
		}

	}

	class RemoveButtonEditor extends ButtonEditor {

		public RemoveButtonEditor(JCheckBox checkBox, ShrimpAction removeAction) {
			super(checkBox, removeAction);
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			Component comp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
			// update the enablement
			comp.setEnabled(canRemove(getDocument(row)));
			return comp;
		}

	}

}