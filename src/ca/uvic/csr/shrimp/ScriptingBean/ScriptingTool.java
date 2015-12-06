/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ScriptingBean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.CamelCaseWordAction;
import ca.uvic.csr.shrimp.util.CodeDocument;
import ca.uvic.csr.shrimp.util.ShowInBrowserAction;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TextComponentContentAssist;

/**
 * The scripting tool allows users to write javascript code to control the main {@link ShrimpView}.
 * See the <a href="http://www.cs.uvic.ca/~chisel/projects/shrimp/scripting-api/ca/uvic/csr/shrimp/ScriptingBean/MainViewScriptingBean.html">API</a>
 * for more details.
 *
 * @author  Nasir Rather, Chris Callendar
 * @date July 2, 2003, 3:24 PM
 */
public class ScriptingTool extends AbstractShrimpTool implements UndoableEditListener {

	private final static String PROP_LAST_DIR = "scripting.directory";
	private static final String PROP_CONTENT_ASSIST = "scripting.contentAssist";
	private static final String SCRIPT = "Script";
	private static final String SCRIPTING_API =
		"http://www.cs.uvic.ca/~chisel/projects/shrimp/scripting-api/ca/uvic/csr/shrimp/ScriptingBean/MainViewScriptingBean.html";
	private static final Color NO_PROJECT = new Color(212, 212, 212);

	private JPanel mainPanel;
    private JRootPane rootPane;
	private JLabel statusLabel;
	private JTextPane editor;
	private UndoManager undoManager;
	private DefaultShrimpAction undoAction;
	private DefaultShrimpAction redoAction;
	private DefaultShrimpAction runAction;
	private DefaultShrimpAction openAction;
	private DefaultShrimpAction saveAction;
	private DefaultShrimpAction exitAction;

	// @tag Shrimp.Scripting.ContentAssist
	private TextComponentContentAssist contentAssist;

	private ScriptingBean scriptingBean;
	private File scriptFile;

	private String lastDirectory;
	private CheckBoxAction contentAssistAction;

	/** Creates new form ScriptingTool */
	public ScriptingTool(ShrimpProject project) {
		super(ShrimpProject.SCRIPTING_TOOL, project);
		rootPane = new JRootPane();
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
		scriptFile = null;
		undoManager = new UndoManager();

		boolean contentAssistOn = true;
		String userDir = System.getProperty("user.dir");
		if (ApplicationAccessor.isApplicationSet()) {
			Properties props = ApplicationAccessor.getProperties();
			lastDirectory = props.getProperty(PROP_LAST_DIR, userDir);
			contentAssistOn = "true".equalsIgnoreCase(props.getProperty(PROP_CONTENT_ASSIST, "true"));
		} else {
			lastDirectory = userDir;
		}

		createActions();
		addKeyStrokes();

		contentAssist = new TextComponentContentAssist(getEditor());
		contentAssist.setPopupDelay(500);
		contentAssist.setEnabled(contentAssistOn);
		contentAssistAction.setChecked(contentAssistOn);
		populateContentAssist();
	}

	/**
	 * Populates the content assist with the methods from the {@link MainViewScriptingBean} and the static
	 * fields in {@link ScriptingConstants}.
	 * @tag Shrimp.Scripting.ContentAssist
	 */
	private void populateContentAssist() {
		// load the MainViewScriptingBean methods
		Vector methods = ShrimpUtils.getMethodNames(MainViewScriptingBean.class, false);
		Collections.sort(methods, Collator.getInstance());
		contentAssist.addContentAssistTerms("mainView", methods);

		// load the ScriptingConstants fields
		Vector fields = ShrimpUtils.getFields(ScriptingConstants.class);
		contentAssist.addContentAssistTerms("constants", fields);
	}

	public void undoableEditHappened(UndoableEditEvent e) {
        //Remember the edit and update the menus
        undoManager.addEdit(e.getEdit());
    }

	public void setProject(ShrimpProject project) {
		super.setProject(project);
		updateActions();
	}

	/**
	 * Updates the run action and the status bar label
	 * depending on whether the project is loaded.
	 */
	private void updateActions() {
		// update the run Action and status bar
		runAction.setEnabled(project != null);
		if (project == null) {
			setStatusText("> No project", true);
			getEditor().setBackground(NO_PROJECT);
		} else {
			setStatusText(">", false);
			getEditor().setBackground(Color.white);
		}
	}

	private void initComponents() {
		rootPane.getContentPane().removeAll();
		rootPane.getContentPane().setLayout(new BorderLayout());
		rootPane.getContentPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.removeAll();

		JScrollPane scrollPane = new JScrollPane(getEditor());
		scrollPane.setBorder(new EtchedBorder());
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(getStatusLabel(), BorderLayout.SOUTH);

		updateActions();
		addExampleCode();
		recreateMenuBar();
	}

	private JTextPane getEditor() {
		if (editor == null) {
			editor = new JTextPane();
			KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
			editor.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, "runScript2");
			editor.getActionMap().put("runScript2", runAction);
			addEditorKeyStrokeActions();

			// @tag Shrimp.Scripting.SyntaxHighlighting
			CodeDocument doc = new CodeDocument(editor);
			doc.addKeywords(CodeDocument.getJavaScriptKeywords());
			doc.addKeyword("bsf");
			doc.addKeyword("constants");
			doc.addKeyword("mainView");
			doc.setTabSize(8, editor.getFontMetrics(editor.getFont()));
			editor.setDocument(doc);

			// @tag Shrimp.Scripting.Undo
			doc.addUndoableEditListener(this);
		}
		return editor;
	}

	/**
	 * Uses custom actions for the next word and previous word actions.
	 * Also adds undo and redo support.
	 * */
	private void addEditorKeyStrokeActions() {
		Keymap parentMap = editor.getKeymap();
		Keymap childMap = JTextComponent.addKeymap("ScriptingKeymap", parentMap);

		// get all the actions JTextArea provides for us
		Action actionList[] = editor.getActions();
		// put them in a Hashtable so we can retreive them by Action.NAME
		Hashtable lookup = new Hashtable();
		for (int j = 0; j < actionList.length; j++) {
			Action action = actionList[j];
			Object name = action.getValue(Action.NAME);
			lookup.put(name, action);
		}

		// @tag Shrimp.Scripting.CamelCaseWordAction
		Action oldRight = (Action) lookup.get(DefaultEditorKit.nextWordAction);
		CamelCaseWordAction newRight = new CamelCaseWordAction(DefaultEditorKit.nextWordAction, false, true, oldRight);
		KeyStroke ctrlRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK);
		childMap.addActionForKeyStroke(ctrlRight, newRight);

		Action oldSelectRight = (Action) lookup.get(DefaultEditorKit.selectionNextWordAction);
		CamelCaseWordAction newSelectRight = new CamelCaseWordAction(DefaultEditorKit.selectionNextWordAction, true, true, oldSelectRight);
		KeyStroke ctrlShiftRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
		childMap.addActionForKeyStroke(ctrlShiftRight, newSelectRight);

		Action oldLeft = (Action) lookup.get(DefaultEditorKit.previousWordAction);
		CamelCaseWordAction newLeft = new CamelCaseWordAction(DefaultEditorKit.previousWordAction, false, false, oldLeft);
		KeyStroke ctrlLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK);
		childMap.addActionForKeyStroke(ctrlLeft, newLeft);

		Action oldSelectLeft = (Action) lookup.get(DefaultEditorKit.selectionPreviousWordAction);
		CamelCaseWordAction newSelectLeft = new CamelCaseWordAction(DefaultEditorKit.selectionPreviousWordAction, true, false, oldSelectLeft);
		KeyStroke ctrlShiftLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
		childMap.addActionForKeyStroke(ctrlShiftLeft, newSelectLeft);

		// add Undo/Redo keystrokes
		KeyStroke ctrlZ = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
		childMap.addActionForKeyStroke(ctrlZ, undoAction);
		KeyStroke ctrlY = KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK);
		childMap.addActionForKeyStroke(ctrlY, redoAction);

		editor.setKeymap(childMap);
	}

	private JLabel getStatusLabel() {
		if (statusLabel == null) {
			statusLabel = new JLabel(">");
			statusLabel.setBackground(new Color(204, 204, 204));
			statusLabel.setBorder(new EtchedBorder());
		}
		return statusLabel;
	}

	/**
	 * Adds example code to the editor pane.
	 */
	private void addExampleCode() {
		StringBuffer code = new StringBuffer();
		code.append("// Content Assist (Ctrl+Space) is enabled for the constants and mainView variables\n");
		code.append("// To use it type in the variable name and a period\n");
		code.append("constants = bsf.lookupBean(\"scriptingConstants\");\n");
		code.append("mainView = bsf.lookupBean(\"mainViewScriptingBean\");\n\n");
		int position = code.length();
		getEditor().setText(code.toString());
		getEditor().setCaretPosition(position);
	}

	/**
	 * Inserts a try catch block at the cursor position.
	 */
	private void addTryCatchActionPerformed() {
		StringBuffer buffer = new StringBuffer();
		int caretPosition = getEditor().getCaretPosition();

		// put the try/catch before the line where the cursor is located
		try {
			int line = getLineOfOffset(caretPosition);
			caretPosition = getLineStartOffset(line);
			buffer.append("try {\n    ");
			int newPosition = caretPosition + buffer.length();
			buffer.append("\n} catch (e) {\n\tmainView.alert(\"Caught exception: \"+e);\n}\n");
			getEditor().getDocument().insertString(caretPosition, buffer.toString(), null);
			getEditor().setCaretPosition(newPosition);
		} catch (BadLocationException ignore) {
		}
	}

	//////////////////////////////////////////////////////
	// These 4 methods were copied from JTextArea
	//////////////////////////////////////////////////////

    /**
     * Translates an offset into the components text to a
     * line number.
     * @param offset the offset >= 0
     * @return the line number >= 0
     */
	protected int getLineOfOffset(int offset) {
        Element map = getEditor().getDocument().getDefaultRootElement();
        return map.getElementIndex(offset);
    }

    /**
     * Determines the number of lines contained in the area.
     * @return the number of lines > 0
     */
    protected int getLineCount() {
        Element map = getEditor().getDocument().getDefaultRootElement();
        return map.getElementCount();
    }

    /**
     * Determines the offset of the start of the given line.
     *
     * @param line  the line number to translate >= 0
     * @return the offset >= 0
     * @exception BadLocationException thrown if the line is
     * less than zero or greater or equal to the number of
     * lines contained in the document (as reported by
     * getLineCount).
     */
    protected int getLineStartOffset(int line) {
        Element map = getEditor().getDocument().getDefaultRootElement();
        Element lineElem = map.getElement(line);
        return lineElem.getStartOffset();
    }

    /**
     * Determines the offset of the end of the given line.
     * @param line  the line >= 0
     * @return the offset >= 0
     */
    protected int getLineEndOffset(int line) {
        int lineCount = getLineCount();
        Element map = getEditor().getDocument().getDefaultRootElement();
        Element lineElem = map.getElement(line);
        int endOffset = lineElem.getEndOffset();
        // hide the implicit break at the end of the document
        return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
    }


	private void createActions() {
		actionManager = new ActionManager();

		// File->Open
		openAction = new DefaultShrimpAction("Open... (Ctrl+O)") {
			public void actionPerformed(ActionEvent evt) {
				loadActionPerformed();
			}
		};
		openAction.setMnemonic('O');
		actionManager.addAction(openAction, ShrimpConstants.MENU_FILE, ShrimpConstants.MENU_FILE, 1);

		// File->Save
		saveAction = new DefaultShrimpAction("Save    (Ctrl+S)") {
			public void actionPerformed(ActionEvent evt) {
				saveActionPerformed();
			}
		};
		saveAction.setMnemonic('S');
		actionManager.addAction(saveAction, ShrimpConstants.MENU_FILE, ShrimpConstants.MENU_FILE, 2);

		// File->Save As
		DefaultShrimpAction saveAs = new DefaultShrimpAction("Save As...") {
			public void actionPerformed(ActionEvent evt) {
				saveAsActionPerformed();
			}
		};
		saveAs.setMnemonic('A');
		actionManager.addAction(saveAs, ShrimpConstants.MENU_FILE, ShrimpConstants.MENU_FILE, 3);

		// File->Exit
		exitAction = new DefaultShrimpAction("Exit", "Close the Scripting Window") {
			public void actionPerformed(ActionEvent e) {
				exitActionPerformed();
			}
		};
		exitAction.setMnemonic('X');
		actionManager.addAction(exitAction, ShrimpConstants.MENU_FILE, ShrimpConstants.MENU_FILE, 4);

		// Edit->Undo
		undoAction = new DefaultShrimpAction("Undo (Ctrl+Z)") {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.undo();
				} catch (CannotUndoException cue) {
				}
			}
		};
		actionManager.addAction(undoAction, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_A, 1);

		// Edit->Redo
		redoAction = new DefaultShrimpAction("Redo (Ctrl+Y)") {
			public void actionPerformed(ActionEvent e) {
				try {
					undoManager.redo();
				} catch (CannotRedoException cre) {
				}
			}
		};
		actionManager.addAction(redoAction, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_A, 2);

		contentAssistAction = new CheckBoxAction("Content Assist (Ctrl+Space)") {
			public void startAction() {
				toggleContentAssistAction();
			}
		};
		contentAssistAction.setChecked(true);
		actionManager.addAction(contentAssistAction, ShrimpConstants.MENU_EDIT, ShrimpConstants.GROUP_B, 1);

		// Script->Run
		runAction = new DefaultShrimpAction("Run (F5)", "Execute the script") {
			public void actionPerformed(ActionEvent evt) {
				runActionPerformed(evt);
			}
		};
		runAction.setMnemonic('R');
		actionManager.addAction(runAction, SCRIPT, SCRIPT, 1);

		// Script->Reset
		Action resetToExample = new DefaultShrimpAction("Reset", "Resets the textarea back to the default text") {
			public void actionPerformed(ActionEvent e) {
				addExampleCode();
			}
		};
		actionManager.addAction(resetToExample, SCRIPT, SCRIPT, 2);

		// Script->Add Try/Catch
		Action addTryCatch = new AbstractAction("Add a try catch block") {
			public void actionPerformed(ActionEvent evt) {
				addTryCatchActionPerformed();
			}
		};
		actionManager.addAction(addTryCatch, SCRIPT, SCRIPT, 3);

		// Language->Javascript
		CheckBoxAction javascript = new CheckBoxAction("Javascript") {
			public void startAction() {
				if (scriptingBean != null) {
					scriptingBean.setLanguage("javascript");
				}
			}
		};
		javascript.setChecked(true);
		actionManager.addAction(javascript, "Language", "Language", 1);

		// Help->Scripting API
		URL url = null; //ResourceHandler.getFileURL("doc/scripting/index.html");
	    try {
	        // use the online version
            url = new URL(SCRIPTING_API);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
		if (url != null) {
			Action help = new ShowInBrowserAction("Scripting API", url.toString(), ResourceHandler.getIcon("icon_help.gif"));
			actionManager.addAction(help, ShrimpConstants.MENU_HELP, ShrimpConstants.GROUP_A, 1);
		}
	}

	private void addKeyStrokes() {
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = rootPane.getActionMap();

		KeyStroke F5 = KeyStroke.getKeyStroke("F5");
		inputMap.put(F5, "runScript");
		actionMap.put("runScript", runAction);

		KeyStroke ctrlO = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK);
		inputMap.put(ctrlO, "openScript");
		actionMap.put("openScript", openAction);

		KeyStroke ctrlS = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
		inputMap.put(ctrlS, "saveScript");
		actionMap.put("saveScript", saveAction);
	}

	private void saveLastDirectory() {
		if (ApplicationAccessor.isApplicationSet()) {
			Properties props = ApplicationAccessor.getProperties();
			props.setProperty(PROP_LAST_DIR, lastDirectory);
		}
	}

	protected void toggleContentAssistAction() {
		boolean checked = !contentAssistAction.isChecked();
		contentAssistAction.setChecked(checked);
		contentAssist.setEnabled(checked);
		if (ApplicationAccessor.isApplicationSet()) {
			Properties props = ApplicationAccessor.getProperties();
			props.setProperty(PROP_CONTENT_ASSIST, ""+checked);
		}
	}

	private void loadActionPerformed() {
		JFileChooser chooser = new JFileChooser();
		ShrimpFileFilter jsFilter = new ShrimpFileFilter("js", "Javascript");
		chooser.addChoosableFileFilter(jsFilter);

		if (lastDirectory != null) {
			chooser.setCurrentDirectory(new File(lastDirectory));
		}
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setDialogTitle("Open Script");
		int state = chooser.showOpenDialog(rootPane);

		if (state == JFileChooser.APPROVE_OPTION) {
			scriptFile = chooser.getSelectedFile();
			lastDirectory = scriptFile.getParent();
			saveLastDirectory();

			try {
				String lang = BSFManager.getLangFromFilename(scriptFile.getName());
				if (scriptingBean != null) {
					scriptingBean.setLanguage(lang);
				}
				BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
				getEditor().read(reader, new PlainDocument());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BSFException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void saveAsActionPerformed() {
		JFileChooser chooser = new JFileChooser();

		ShrimpFileFilter filter = new ShrimpFileFilter("js", "Javascript");
		//String lang = scriptingBean.getLanguage();

		chooser.addChoosableFileFilter(filter);
		if (lastDirectory != null) {
			chooser.setCurrentDirectory(new File(lastDirectory));
		}
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setDialogTitle("Save Script");
		int state = chooser.showSaveDialog(rootPane);

		if (state == JFileChooser.APPROVE_OPTION) {
			scriptFile = chooser.getSelectedFile();
			lastDirectory = scriptFile.getParent();
			saveLastDirectory();

			if (!scriptFile.getName().toLowerCase().endsWith(".js")) {
				scriptFile = new File(scriptFile.getAbsolutePath() + ".js");
			}
			try {
				getEditor().write(new FileWriter(scriptFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveActionPerformed() {
		if ((scriptFile != null) && scriptFile.exists()) {
			try {
				getEditor().write(new FileWriter(scriptFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			saveAsActionPerformed();
		}
	}

	private void setStatusText(String text) {
		setStatusText(text, false);
	}

	private void setStatusText(String text, boolean error) {
		getStatusLabel().setText(text);
		getStatusLabel().setForeground((error ? Color.red : Color.black));
	}

	private void runActionPerformed(ActionEvent evt) {
		setStatusText("> Running Script...");
		runScript(getEditor().getText());
	}

	private boolean runScript(String scriptAsString) {
		try {
			scriptingBean.runScriptString(scriptAsString);
			setStatusText("> Script finished");
			return true;
		} catch (Throwable t) {
			t.printStackTrace();
			setStatusText("> Script error: " + t.getMessage(), true);
		}
		return false;
    }

	private void exitActionPerformed() {
		Window window = SwingUtilities.windowForComponent(rootPane);
		if (window != null) {
			window.setVisible(false);
		}
	}

	public Component getGUI() {
		return rootPane;
	}

	public void disposeTool() {
		clear();
		if (contentAssist != null) {
			contentAssist.dispose();
			contentAssist = null;
		}
		scriptingBean = null;
	}

	public void clear() {
        mainPanel.removeAll();
        rootPane.getContentPane().removeAll();
    }

	public void refresh() {
		// save existing text
		String text = getEditor().getText();
		if (project != null) {
			try {
				scriptingBean = (ScriptingBean) project.getBean(ShrimpProject.SCRIPTING_BEAN);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}

		initComponents();

		// restore the original text
		if (text.length() > 0) {
			getEditor().setText(text);
		}
	}


    public static void main(String[] args) {
		ScriptingTool tool = new ScriptingTool(null);

    	JDialog dlg = new JDialog();
		dlg.setModal(true);
		dlg.setTitle("Scripting Tool Test");
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.add(tool.getGUI(), BorderLayout.CENTER);
		dlg.getContentPane().add(pnl);

		tool.refresh();

		dlg.pack();
		dlg.setLocation(400, 200);
		dlg.setSize(600, 300);
		dlg.setVisible(true);

		tool.disposeTool();
	}

}
