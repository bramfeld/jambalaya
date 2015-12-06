/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Only allows integers in the textfield.  Can't be blank.
 * @author Chris Callendar
 */
public class JIntegerTextField extends JTextField implements FocusListener {

	private final int defaultInt;

	public JIntegerTextField() {
		this(0);
	}

	/**
	 * @param intValue default/starting integer value
	 */
	public JIntegerTextField(int intValue) {
		super(Integer.toString(intValue));
		setDocument(new NumberDocument());
		this.defaultInt = intValue;
		this.addFocusListener(this);
		setIntegerText(intValue);
	}

	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent e) {
		if ("".equals(getText())) {
			setText("" + defaultInt);
		}
	}

	public int getIntegerText() {
		return (new Integer(getText()).intValue());
	}

	public void setIntegerText(int intValue) {
		setText(Integer.toString(intValue));
	}

	class NumberDocument extends PlainDocument {
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			for(int i = 0; i < str.length(); i++) {
		        if(!Character.isDigit(str.charAt(i))) {
		            return;
		        }
		     }
			super.insertString(offs, str, a);
		}
	}

}
