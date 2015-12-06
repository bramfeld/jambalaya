/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. 
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


/**
 * 
 * 
 * @author Chris Callendar
 */
public class JIntegerComboBoxEditor extends BasicComboBoxEditor implements FocusListener {

	private JTextField textbox;
	private final int defaultInt;
	
	public JIntegerComboBoxEditor() {
		this(0);
	}
	
	public JIntegerComboBoxEditor(int defaultInt) {
		super();
		this.defaultInt = defaultInt;
		textbox = (JTextField) getEditorComponent();
		textbox.addFocusListener(this);
		textbox.setDocument(new NumberDocument());
		
		if (textbox.getText().length() == 0) {
			textbox.setText("" + defaultInt);
		}
	}
	
	public void focusGained(FocusEvent e) {}
	
	public void focusLost(FocusEvent e) {
		if (textbox.getText().length() == 0) {
			textbox.setText("" + defaultInt);
		}
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
