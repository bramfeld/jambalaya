/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;


/**
 *
 *
 * @author Chris Callendar
 * @date 18-Apr-07
 */
public class TextComponentContentAssist {

	private JWindow window;
	private JComboBox combo;
	private JTextComponent textComponent;
	private JTextComponent editorComponent;
	private HashMap keywordMap;

	private long delay;
	private boolean keyPressed;
	private boolean enabled;

	private int startOffset = 0;
	private int dotOffset = 0;
	private int caretOffset = 0;

	public TextComponentContentAssist(JTextComponent textComponent) {
		this.textComponent = textComponent;
		this.combo = new JComboBox();
		this.keywordMap = new HashMap();
		this.delay = -1;
		this.keyPressed = false;
		this.enabled = true;

		initializeTextComponent();
		initializeComboBox();
	}

	public void dispose() {
		getWindow().dispose();
		textComponent = null;
		keywordMap = null;
		combo = null;
	}

	/**
	 * Enables or disables content assist.
	 * No popups will be displayed if it is disabled.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the delay before the content assist popups up after a period.
	 * Set to -1 to turn off.
	 * @param delay the number of milliseconds before showing the content assist
	 */
	public void setPopupDelay(long delay) {
		this.delay = Math.max(-1, delay);
	}

	public long getPopupDelay() {
		return delay;
	}

	public void addContentAssistTerms(String keyword, Collection terms) {
		if ((keyword != null) && (keyword.length() > 0)) {
			if (keyword.endsWith(".")) {
				keyword = keyword.substring(0, keyword.length() - 1);
			}
			Vector v = (terms instanceof Vector ? (Vector)terms : new Vector(terms));
			DefaultComboBoxModel model = new DefaultComboBoxModel(v);
			keywordMap.put(keyword, model);
		}
	}


	private void initializeTextComponent() {
		this.textComponent.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				hideComboWindow();
			}
		});
		this.textComponent.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (getWindow().isVisible()) {
					setTextAndHideWindow(editorComponent.getText());
				}
			}
		});
		this.textComponent.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPress(e);
			}
		});
	}

	private void initializeComboBox() {
		Font font = textComponent.getFont();
		FontMetrics metrics = textComponent.getFontMetrics(font);
		int comboHeight = metrics.getHeight() + 4;
		combo.setFont(font);
		combo.setPreferredSize(new Dimension(200, comboHeight));
		combo.setBounds(0, 0, 200, comboHeight);
		combo.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				hideComboWindow();
			}
		});
		combo.setEditable(true);

		AutoCompleteDocument doc = new AutoCompleteDocument(combo);
		editorComponent = (JTextComponent) combo.getEditor().getEditorComponent();
		editorComponent.setDocument(doc);
	}

	private JWindow getWindow() {
		if (window == null) {
			window = new EscapeWindow(textComponent) {
				protected void performEnter(KeyEvent e) {
					setTextAndHideWindow(editorComponent.getText());
				}
			};
			window.setBounds(0, 0, 200, combo.getPreferredSize().height);
			window.setLayout(new BorderLayout());
			window.setVisible(false);
			window.add(combo, BorderLayout.CENTER);
		}
		return window;
	}

	protected void handleKeyPress(KeyEvent e) {
		keyPressed = true;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SPACE :
				if (e.isControlDown()) {
					showContentAssist();
				}
				break;
			case KeyEvent.VK_ESCAPE :
				hideComboWindow();
				break;
			case KeyEvent.VK_PERIOD :
				showContentAssistDelayed();
				break;
		}
	}

	/**
	 * Displays the content assist popup after the delay.
	 * If a key is pressed during the delay then the popup is not displayed.
	 */
	private void showContentAssistDelayed() {
		if (isEnabled() && (delay >= 0)) {
			keyPressed = false;
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
					}
					if (!keyPressed) {
						showContentAssist();
					}
				}
			}).start();
		}
	}

	private void showContentAssist() {
		if (!isEnabled()) {
			return;
		}

		caretOffset = textComponent.getCaretPosition();
		String word = getWordText(caretOffset);
		startOffset = caretOffset - word.length();
		int dot = word.indexOf(".");
		if (dot == -1) {
			dotOffset = -1;
		} else {
			dotOffset = startOffset + dot;
		}
		String[] split = splitWord(word);
		String keyword = split[0];
		final String text = split[1];
		if (keywordMap.containsKey(keyword)) {
			DefaultComboBoxModel model = (DefaultComboBoxModel) keywordMap.get(keyword);
			combo.setModel(model);
			if ((dotOffset != -1) && ((dotOffset + 1) != caretOffset)) {
				textComponent.setCaretPosition(dotOffset + 1);
			}
			// the caret position on screen doesn't get updated until it is repainted,
			// so we have to wait
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showComboWindow(text);
				}
			});
		}
	}

	private void showComboWindow(String text) {
		Point p = textComponent.getCaret().getMagicCaretPosition();
		if (isEnabled() && (p != null)) {
			p = new Point(p.x, p.y - 2);	// must use a new point!
			SwingUtilities.convertPointToScreen(p, textComponent);
			getWindow().setLocation(p.x, p.y);
			getWindow().setVisible(true);
			editorComponent.requestFocus();
			editorComponent.setText(text);

			// the popup doesn't seem to display unless it is run later
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					combo.showPopup();
				}
			});
		}
	}

	protected void hideComboWindow() {
		if (getWindow().isVisible()) {
			combo.hidePopup();
			getWindow().setVisible(false);
			textComponent.requestFocus();
		}
	}

	private void setTextAndHideWindow(String text) {
		try {
			Document doc = textComponent.getDocument();
			int afterDotOffset = (dotOffset == -1 ? caretOffset : (dotOffset + 1));
			int length = caretOffset - afterDotOffset;
			if (length > 0) {
				doc.remove(afterDotOffset, length);
			}
			doc.insertString(afterDotOffset, text, null);
			textComponent.setCaretPosition(afterDotOffset + text.length());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		hideComboWindow();
	}

	private String[] splitWord(String word) {
		String[] split = new String[2];
		split[0] = "";
		split[1] = "";
		if (word.length() > 0) {
			int dot = word.lastIndexOf(".");
			if (dot != -1) {
				split[0] = word.substring(0, dot);
				split[1] = word.substring(dot + 1);
			}
		}
		return split;
	}

	private String getWordText(int offset) {
		String text = "";
		if (offset > 0) {
			try {
				int start = Utilities.getPreviousWord(textComponent, offset);
				Document doc = textComponent.getDocument();
				text = doc.getText(start, offset - start);
				if (".".equals(text)) {
					start = Utilities.getPreviousWord(textComponent, offset - 1);
					text = doc.getText(start, offset - start);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return text;
	}

}
