/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

/**
 * A syntax highlighting document.
 * Has support for highlighting numbers, strings (double quotes only), comments, and keywords.
 *
 * @author Chris Callendar
 * @date 4-Apr-07
 */
public class CodeDocument extends DefaultStyledDocument {

	private SimpleAttributeSet keywordStyle = new SimpleAttributeSet();
	private SimpleAttributeSet stringStyle = new SimpleAttributeSet();
	private SimpleAttributeSet normalStyle = new SimpleAttributeSet();
	private SimpleAttributeSet numberStyle = new SimpleAttributeSet();
	private SimpleAttributeSet commentsStyle = new SimpleAttributeSet();

	private Pattern numberPattern;
	private Pattern doubleQuotesPattern;

	private Collection keywords;

	private int startLineOffset;
	private int endLineOffset;

	private JTextComponent textComponent = null;

	public CodeDocument() {
		this.keywords = new HashSet();

		//set the bold attribute
		StyleConstants.setBold(keywordStyle, true);
		StyleConstants.setForeground(keywordStyle, Color.blue);
		StyleConstants.setForeground(stringStyle, Color.magenta);
		StyleConstants.setForeground(numberStyle, new Color(128, 0, 96));
		StyleConstants.setForeground(commentsStyle, new Color(0, 128, 0));
		//StyleConstants.setItalic(comments, true);

		numberPattern = Pattern.compile("[0-9]+");
		doubleQuotesPattern = Pattern.compile("\"[^\"\n\r]*(\")?");

		setTabSize(8, null);
	}

	public CodeDocument(Collection keywords) {
		this();
		setKeywords(keywords);
	}
	public CodeDocument(JTextComponent textComponent) {
		this();
		setTextComponent(textComponent);
	}

	public CodeDocument(JTextComponent textComponent, Collection keywords) {
		this();
		setTextComponent(textComponent);
		setKeywords(keywords);
	}

	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		super.insertString(offs, str, normalStyle);

		if (str.trim().length() > 0) {
			// get each line
			int endOffset = offs + str.length();
			int startLine = getDefaultRootElement().getElementIndex(offs);
			int endLine = getDefaultRootElement().getElementIndex(endOffset);
			for (int line = startLine; line <= endLine; line++) {
				Element element = getDefaultRootElement().getElement(line);
				startLineOffset = element.getStartOffset();
				endLineOffset = element.getEndOffset();
				// trim to remove carriage returns?
				String lineStr = getText(startLineOffset, endLineOffset - startLineOffset).trim();
				parseLine(lineStr);
			}
		}
	}

	private void parseLine(String line) {
		if (line.length() > 0) {
			// save the caret position
			int caretPosition = -1;
			if (textComponent != null) {
				caretPosition = textComponent.getCaretPosition();
			}

			// first remove the line and add it back to remove any styles
			try {
				remove(startLineOffset, line.length());
				super.insertString(startLineOffset, line, normalStyle);
			} catch (BadLocationException e1) {
			}

			// 1. Match numbers
			matchLine(line, numberPattern, numberStyle);

			// 2. Match keywords
			for (Iterator iter = keywords.iterator(); iter.hasNext(); ) {
				String key = (String) iter.next();
				try {
					Pattern keywordPattern = Pattern.compile(key);
					matchLine(line, keywordPattern, keywordStyle);
				} catch (PatternSyntaxException e) {
					System.err.println("Invalid keyword (can't parse):" + key);
					iter.remove();
				}
			}

			// 3. Match quoted strings
			matchLine(line, doubleQuotesPattern, stringStyle);

			// 4. Match comments
			matchCommentLine(line);

			// restore the caret position
			if ((caretPosition >= 0) && (caretPosition < getLength())) {
				textComponent.setCaretPosition(caretPosition);
			}
		}
	}

	private boolean matchLine(String line, Pattern p, AttributeSet style) {
		boolean matched = false;
		Matcher m = p.matcher(line);
		int start = 0;
		while ((start < line.length()) && m.find(start)) {
			matched = true;
			start = m.start();
			String group = m.group();
			insertStyledString(group, start, style);
			start = m.end() + 1;
		}
		return matched;
	}

	private void matchCommentLine(String line) {
		boolean insideMultiComment = false;
		boolean insideQuotes = false;
		boolean done = false;
		int multiStart = -1;

		char c2 = ' ';	// previous character
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
				case '/' :
					if ((c2 == '/') && !insideQuotes && !insideMultiComment) {
						int start = i - 1;
						String commentString = line.substring(start).trim();
						insertStyledString(commentString, start, commentsStyle);
						done = true;
						break;
					} else if ((c2 == '*') && !insideQuotes) {
						int start = (multiStart == -1 ? 0 : multiStart);
						String commentString = line.substring(start, i+1).trim();
						insertStyledString(commentString, start, commentsStyle);
						insideMultiComment = false;
					}
					break;
				case '*' :
					if ((c2 == '/') && !insideQuotes) {
						insideMultiComment = true;
						multiStart = i - 1;
					}
					break;
				case '\"' :
					if (!insideMultiComment) {
						insideQuotes = !insideQuotes;
					}
					break;
			}
			c2 = c;
			if (done) {
				break;
			}
		}

		if (insideMultiComment) {
			int start = (multiStart == -1 ? 0 : multiStart);
			String commentString = line.substring(start).trim();
			insertStyledString(commentString, start, commentsStyle);
		}
	}

	private void insertStyledString(String str, int relativeOffset, AttributeSet style) {
		try {
			//remove the old word and formatting (changes the caret position!)
			int pos = startLineOffset + relativeOffset;
			this.remove(pos, str.length());
			super.insertString(pos, str, style);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Sets the text component that this document is associated with.
	 * It is need to ensure that the cursor/caret position remains the same.
	 */
	public void setTextComponent(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}

	public JTextComponent getTextComponent() {
		return textComponent;
	}

	public Collection getKeywords() {
		return this.keywords;
	}

	public void setKeywords(Collection keywords) {
		if (keywords != null) {
			this.keywords = keywords;
		}
	}

	public void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}

	public void addKeywords(Collection add) {
		this.keywords.addAll(add);
	}

	public void removeKeywords(Collection remove) {
		this.keywords.removeAll(remove);
	}

	public void clearKeywords() {
		this.keywords = new HashSet();
	}

	/**
	 * @return the default set of javascript keywords
	 */
	public static Collection getJavaScriptKeywords() {
		HashSet jsKeywords = new HashSet();
		// keywords
		jsKeywords.add("function");
		jsKeywords.add("try");
		jsKeywords.add("catch");
		jsKeywords.add("var");
		jsKeywords.add("new");
		jsKeywords.add("if");
		jsKeywords.add("else");
		jsKeywords.add("while");
		jsKeywords.add("for");
		jsKeywords.add("break");
		jsKeywords.add("continue");
		jsKeywords.add("return");
		jsKeywords.add("null");
		// booleans
		jsKeywords.add("true");
		jsKeywords.add("false");
		// functions
		//jsKeywords.add("alert");
		//jsKeywords.add("confirm");
		//jsKeywords.add("parseInt");
		// Objects
		//jsKeywords.add("Array");
		//jsKeywords.add("Date");

		return jsKeywords;
	}

	/**
	 * @return the default set of javascript keywords
	 */
	public static Collection getJavaKeywords() {
		HashSet javaKeywords = new HashSet();
		// keywords
		javaKeywords.add("public");
		javaKeywords.add("protected");
		javaKeywords.add("private");
		javaKeywords.add("static");
		javaKeywords.add("final");
		javaKeywords.add("void");
		javaKeywords.add("try");
		javaKeywords.add("catch");
		javaKeywords.add("finally");
		javaKeywords.add("new");
		javaKeywords.add("if");
		javaKeywords.add("else");
		javaKeywords.add("while");
		javaKeywords.add("do");
		javaKeywords.add("for");
		javaKeywords.add("break");
		javaKeywords.add("continue");
		javaKeywords.add("return");
		javaKeywords.add("this");
		javaKeywords.add("super");
		// booleans
		javaKeywords.add("true");
		javaKeywords.add("false");
		// primitives
		javaKeywords.add("byte");
		javaKeywords.add("short");
		javaKeywords.add("int");
		javaKeywords.add("long");
		javaKeywords.add("float");
		javaKeywords.add("double");
		javaKeywords.add("char");
		javaKeywords.add("null");
		// Objects
		//javaKeywords.add("String");

		return javaKeywords;
	}

	/**
	 * Sets the tab size for this document.
	 * @param tabSize the tab size, this is the number of spaces (e.g. 4 or 8)
	 */
	public void setTabSize(int tabSize) {
		setTabSize(tabSize, null);
	}

	/**
	 * Sets the tab size for this document.
	 * @param tabSize the tab size, defaults to 4
	 * @param fm the font metrics, null is allowed
	 */
	public void setTabSize(int tabSize, FontMetrics fm) {
		int charWidth = 3;
		if (fm != null) {
			charWidth = fm.charWidth(' ');
		}
		int tabWidth = charWidth * tabSize;
		TabStop[] tabs = new TabStop[10];
		for (int i = 0; i < tabs.length; i++) {
			tabs[i] = new TabStop((i + 1) * tabWidth);
		}
		TabSet tabset  = new TabSet(tabs);
		StyleContext sc = StyleContext.getDefaultStyleContext();
	    AttributeSet attributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.TabSet, tabset);
		setParagraphAttributes(0, getLength(), attributes, false);
	}


	public static void main(String[] args) {
		JDialog dlg = new JDialog();
		dlg.setModal(true);
		dlg.setTitle("");
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel pnl = new JPanel(new BorderLayout());

		CodeDocument doc = new CodeDocument();
		JTextPane editor = new JTextPane(doc);
		doc.setTextComponent(editor);
		doc.addKeyword("constants");

		pnl.add(editor, BorderLayout.CENTER);
		dlg.getContentPane().add(pnl);

		dlg.pack();
		dlg.setLocation(400, 200);
		dlg.setSize(400, 300);
		dlg.setVisible(true);
	}

}
