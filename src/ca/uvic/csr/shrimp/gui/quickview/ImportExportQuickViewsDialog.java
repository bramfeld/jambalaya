/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.JButtonList;
import ca.uvic.csr.shrimp.util.ShrimpUtils;


/**
 * Displays the quick views as a checkbox list for importing or exporting.
 * This lets the user choose which quick views to import/export.
 *
 * @author Chris Callendar
 * @date 16-Apr-07
 */
public class ImportExportQuickViewsDialog extends EscapeDialog implements ActionListener {

	private QuickViewManager manager;
	private boolean importing;
	private File lastDirectory;

	private JButtonList quickViewsList;
	private JPanel topPanel;
	private JPanel bottomPanel;
	private JButton okButton;
	private JButton cancelButton;

	public ImportExportQuickViewsDialog(Dialog parent, QuickViewManager manager, boolean importing) {
		super(parent, true);
		initialize(manager, importing);
	}

	public ImportExportQuickViewsDialog(Frame parent, QuickViewManager manager, boolean importing) {
		super(parent, true);
		initialize(manager, importing);
	}

	private void initialize(QuickViewManager manager, boolean importing) {
		this.manager = manager;
		this.importing = importing;

		initialize();

		boolean ok;
		if (importing) {
			setTitle("Importing Quick Views");
			ok = importFromFile();
		} else {
			setTitle("Exporting Quick Views");
			ok = exportFromManager();
		}

		if (ok) {
			ShrimpUtils.centerWindowOnParent(this, getParent());
			setVisible(true);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dispose();
				}
			});
		}
	}

	private void initialize() {
		getContentPane().add(getTopPanel(), BorderLayout.NORTH);
		getContentPane().add(new JScrollPane(getQuickViewsList()), BorderLayout.CENTER);
		getContentPane().add(getBottomPanel(), BorderLayout.SOUTH);

		getQuickViewsList().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int checkCount = getQuickViewsList().getCheckedCount();
				getOKButton().setEnabled(checkCount > 0);
			}
		});
		getOKButton().setEnabled(false);

		pack();
		setPreferredSize(new Dimension(250, 400));
		getRootPane().setDefaultButton(getOKButton());
	}

	private JPanel getTopPanel() {
		if (topPanel == null) {
			topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JToolBar toolbar = new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setBorder(null);
			toolbar.add(new JButton(new AbstractAction("Check All ", ResourceHandler.getIcon("icon_checked.gif")) {
				public void actionPerformed(ActionEvent e) {
					getQuickViewsList().checkAll();
				}
			}));
			toolbar.add(new JButton(new AbstractAction("Check None ", ResourceHandler.getIcon("icon_unchecked.gif")) {
				public void actionPerformed(ActionEvent e) {
					getQuickViewsList().checkNone();
				}
			}));
			topPanel.add(toolbar);
		}
		return topPanel;
	}

	protected JButtonList getQuickViewsList() {
		if (quickViewsList == null) {
			quickViewsList = new JButtonList();
			quickViewsList.setSingleClickSelectionChange(false);
			quickViewsList.setButtonClass(JCheckBox.class);
		}
		return quickViewsList;
	}

	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			bottomPanel.add(getOKButton());
			bottomPanel.add(getCancelButton());
		}
		return bottomPanel;
	}

	protected JButton getOKButton() {
		if (okButton == null) {
			okButton = new JButton("  OK  ");
			okButton.addActionListener(this);
			okButton.setActionCommand("OK");
		}
		return okButton;
	}

	protected JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			cancelButton.setActionCommand("Cancel");
		}
		return cancelButton;
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("OK".equals(cmd)) {
			if (importing) {
				importToManager();
			} else {
				exportToFile();
			}
			setVisible(false);
		} else if ("Cancel".equals(cmd)) {
			setVisible(false);
		}
	}

	/**
	 * Reads a quick views properties file and parses it for quick views.
	 * The loaded quick views are put into the checkbox list (all checked).
	 */
	private boolean importFromFile() {
		boolean imported = false;
		JFileChooser chooser = createFileChooser();
		chooser.setDialogTitle("Import Quick Views");
		int choice = chooser.showOpenDialog(this);
		if (choice == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastDirectory = chooser.getCurrentDirectory();
			try {
				Collection quickViews = manager.readQuickViewsFromFile(file);
				QuickViewAction[] quickViewsArray = (QuickViewAction[])quickViews.toArray(new QuickViewAction[quickViews.size()]);
				Arrays.sort(quickViewsArray, new QuickViewComparator());
				getQuickViewsList().setListData(quickViewsArray);
				getQuickViewsList().checkAll();
				imported = true;
			} catch (IOException e) {
				error("Error importing quick views from the file '" + file + "': " + e.getMessage());
			}
		}
		return imported;
	}

	/**
	 * Adds the checked quick views to the {@link QuickViewManager}.
	 */
	private void importToManager() {
		Collection checkedQuickViews = getQuickViewsList().getCheckedItemsCollection();
		if (checkedQuickViews.size() > 0) {
			int importCount = manager.importQuickViews(checkedQuickViews, false);
			String msg = importCount + " quick view" + (importCount != 1 ? "s" : "") + " imported";
			info(msg, "Success");
		}
	}

	/**
	 * Loads the current quick views into the checkbox list (all checked).
	 */
	private boolean exportFromManager() {
		boolean exported = false;
		QuickViewAction[] sortedQuickViews = manager.getSortedQuickViews();
		if (sortedQuickViews.length > 0) {
			getQuickViewsList().setListData(sortedQuickViews);
			getQuickViewsList().checkAll();
			exported = true;
		}
		return exported;
	}

	/**
	 * Opens a save file chooser and saves the checked quick views to the chosen properties file.
	 */
	private void exportToFile() {
		Collection checkedQuickViews = getQuickViewsList().getCheckedItemsCollection();
		if (checkedQuickViews.size() > 0) {
			JFileChooser chooser = createFileChooser();
			chooser.setDialogTitle("Export Quick Views");
			int choice = chooser.showSaveDialog(this);
			if (choice == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				lastDirectory = chooser.getCurrentDirectory();

				if (file.exists()) {
					String msg = "File (" + file.getName() + ") already exists, overwrite?";
					choice = JOptionPane.showConfirmDialog(this, msg, "Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (choice == JOptionPane.NO_OPTION) {
						return;
					}
				}
				try {
					int exportCount = manager.writeQuickViewsToFile(file, checkedQuickViews);
					String msg = exportCount + " quick view" + (exportCount != 1 ? "s" : "") + " exported";
					info(msg, "Success");
				} catch (IOException e) {
					error("Error exporting quick views to the file '" + file + "': " + e.getMessage());
				}
			}
		}
	}

	private JFileChooser createFileChooser() {
		JFileChooser chooser = new JFileChooser(lastDirectory);
		// choose an default filename for the quick views properties file
		String title = "";
		if ((manager != null) && (manager.getProject() != null)) {
			title = manager.getProject().getTitle();
			int dot = title.indexOf(".");
			if (dot >= 0) {
				title = title.substring(0, dot);
			}
			title = title + "_";
		}
		chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), title + "quickviews.properties"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new PropertiesFileFilter());
		return chooser;
	}

	private void error(String errorMsg) {
		System.err.println(errorMsg);
		JOptionPane.showMessageDialog(this, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	private void info(String infoMsg, String title) {
		JOptionPane.showMessageDialog(this, infoMsg, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Accepts directories and <b>.properties</b> files.
	 * @author Chris Callendar
	 * @date 16-Apr-07
	 */
	class PropertiesFileFilter extends FileFilter {
		public boolean accept(File f) {
			return f.isDirectory() ||
				(f.isFile() && f.getName().toLowerCase().endsWith(".properties"));
		}
		public String getDescription() {
			return "Quick View Property Files (*.properties)";
		}
	}

}
