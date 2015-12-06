/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;


/**
 * Default action for Shrimp.  The {@link DefaultShrimpAction#actionPerformed(ActionEvent)}
 * method does nothing.  Often used as the action for {@link JMenu} objects.
 *
 * @author Chris Callendar
 * @date 14-Aug-06
 */
public class DefaultShrimpAction extends AbstractAction implements ShrimpAction {

	// override the default mnemonic which will be the first unused letter
	private char mnemonic = 'a';
	private boolean hasMnemonic = false;

	public DefaultShrimpAction() {
		super();
	}

	public DefaultShrimpAction(String name, String tooltip) {
		this(name);
		setToolTip(tooltip);
	}

	public DefaultShrimpAction(String name, Icon icon, String tooltip) {
		this(name, icon);
		setToolTip(tooltip);
	}

	public DefaultShrimpAction(String name, Icon icon) {
		super(name, icon);
	}

	public DefaultShrimpAction(Icon icon) {
		super("", icon);
	}

	public DefaultShrimpAction(Icon icon, String tooltip) {
		this("", icon, tooltip);
	}

	public DefaultShrimpAction(String name) {
		super(name);
	}

	public String getActionName() {
		return getValue(Action.NAME) != null ? (String) getValue(Action.NAME) : "no name action!";
	}

	protected void setActionName(String name) {
		putValue(Action.NAME, name);
	}

	public String getText() {
		return getActionName();
	}

	public String getToolTip() {
		String tt = (String) getValue(SHORT_DESCRIPTION);
		if ((tt == null) || (tt.length() == 0)) {
			tt = getActionName();
		}
		return tt;
	}

	public void setToolTip(String tt) {
		putValue(SHORT_DESCRIPTION, tt);
	}

	public Icon getIcon() {
		return (Icon) getValue(SMALL_ICON);
	}

	public void setIcon(Icon icon) {
		putValue(SMALL_ICON, icon);
	}

	public void actionPerformed(ActionEvent e) {
		// do nothing
	}

	public void dispose() {

	}

	public String toString() {
		return getActionName();
	}

	/**
	 * @return true if a valid mnemonic has been set.
	 */
	public boolean hasMnemonic() {
		return hasMnemonic;
	}

	public char getMnemonic() {
		return mnemonic;
	}

	/**
	 * Sets the character to use as a mnemonic.  Must be a letter character.
	 */
	public void setMnemonic(char c) {
		if (Character.isLetter(c)) {
			this.hasMnemonic = true;
			this.mnemonic = c;
		} else {
			this.hasMnemonic = false;
			this.mnemonic = 'a';
		}
	}

}
