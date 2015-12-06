/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

/**
 * This action moves the cursor/caret (or updates the selection) to the next or previous
 * character that is of a different case.
 * It will move the cursor to the next character that has a different case than the previous one,
 * or to the next non-letter or digit character (whitespace, symbol etc).
 * The cursor is moved past space and tab characters.
 *
 * This implementation is designed to be similar to Eclipse's Java Editor.
 *
 * @see Utilities
 * @see DefaultEditorKit
 * @author Chris Callendar
 * @date 18-Apr-07
 */
public class CamelCaseWordAction extends TextAction {

	private boolean select;
	private boolean forwards;
	private Action fallBackAction;

	/**
	 * @param name the name for the action
	 * @param select if true the text will be selected, if false the cursor is moved
	 * @param forwards if true then the cursor moves forwards, otherwise the cursor moves backwards
	 */
	public CamelCaseWordAction(String name, boolean select, boolean forwards) {
		this(name, select, forwards, null);
	}

	/**
	 * @param name the name for the action
	 * @param select if true the text will be selected, if false the cursor is moved
	 * @param forwards if true then the cursor moves forwards, otherwise the cursor moves backwards
	 * @param fallBackAction the action to use in case of an error (might be the default action)
	 */
	public CamelCaseWordAction(String name, boolean select, boolean forwards, Action fallBackAction) {
		super(name);
		this.select = select;
		this.forwards = forwards;
		this.fallBackAction = fallBackAction;
	}

	private String getText(JTextComponent target, int offs) throws BadLocationException {
		Element line = Utilities.getParagraphElement(target, offs);
		Document doc = line.getDocument();
		int lineStart = line.getStartOffset();
		int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
		if ((offs > lineEnd) || (offs < lineStart)) {
		    throw new BadLocationException("No more words", offs);
		}

		return (forwards ? doc.getText(offs, lineEnd - offs) : doc.getText(lineStart, offs - lineStart));
	}

	private char charAt(int index, String text) {
		return (forwards ? text.charAt(index) : text.charAt(text.length() - index - 1));
	}

	private int checkChar(int index, char c, char previous) {
		int delta = 0;
		boolean first = (index == 0);
		boolean lc = Character.isLowerCase(c) || Character.isDigit(c);
		boolean uc = Character.isUpperCase(c);
		//boolean ws = Character.isWhitespace(c);
		boolean space = (c == ' ') || (c == '\t');
		boolean symbol = !Character.isLetterOrDigit(c) && !Character.isWhitespace(c);

		if (space) {
			// space or tab, move past it
			index++;
			delta = (forwards ? index : -index);
		} else if (lc) {
			// Lowercase or digit
			if (!first && !Character.isLetterOrDigit(previous)) {
				delta = (forwards ? index : -index);
			} else if (!forwards && Character.isUpperCase(previous)) {
				delta = -index;
			}
		} else if (uc) {
			// uppercase or other non-letter or digit character
			if (!Character.isUpperCase(previous)) {
				delta = (forwards ? index : 0);
			}
		} else if (symbol) {
			if (c == previous) {
				// skip repeated symbols
			} else {
				delta = (forwards ? index : -index);
			}
		}

		return delta;
	}

	public void actionPerformed(ActionEvent e) {
		JTextComponent target = getTextComponent(e);
		if (target != null) {
			int offs = target.getCaretPosition();
			int oldOffs = offs;
			boolean failed = false;
			try {
		        String text = getText(target, offs);
		        boolean stoped = false;
		        char previous = ' ';
		        int textLength = text.length();
				if (textLength > 0) {
			        for (int i = 0; i < textLength; i++) {
			        	char c = charAt(i, text);
			        	int delta = checkChar(i, c, previous);
			        	if (delta != 0) {
			        		offs += delta;
			        		stoped = true;
			        		break;
			        	}
			        	previous = c;
			        }
			        if (!stoped) {
			        	offs += (forwards ? textLength : -textLength);
			        }
		        } else {
		        	// if we are at the start of a line - move back one character
		        	if (!forwards && (offs > 0)) {
		        		offs--;
		        	}
		        }
			} catch (BadLocationException bl) {
				failed = true;
			}
			if (!failed) {
				if (offs != oldOffs) {
					if (select) {
						target.moveCaretPosition(offs);
					} else {
						target.setCaretPosition(offs);
					}
				}
			} else if (fallBackAction != null) {
				fallBackAction.actionPerformed(e);
			} else {
				UIManager.getLookAndFeel().provideErrorFeedback(target);
			}
		}
	}

}
