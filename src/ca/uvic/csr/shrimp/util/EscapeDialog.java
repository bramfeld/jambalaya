/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;


/**
 * Extends {@link JDialog} to detect when the escape key is pressed and hide the dialog.
 * Also sets up the default close operation to be dispose, and sets the content pane to use
 * a {@link BorderLayout}.
 *
 * @author Chris Callendar
 * @date 17-Apr-07
 */
public class EscapeDialog extends JDialog implements KeyListener, ContainerListener {

	public EscapeDialog() {
		super();
		initialize();
	}

	public EscapeDialog(Frame owner) {
		super(owner);
		initialize();
	}

	public EscapeDialog(Dialog owner) {
		super(owner);
		initialize();
	}

	public EscapeDialog(Frame owner, boolean modal) {
		super(owner, modal);
		initialize();
	}

	public EscapeDialog(Frame owner, String title) {
		super(owner, title);
		initialize();
	}

	public EscapeDialog(Dialog owner, boolean modal) {
		super(owner, modal);
		initialize();
	}

	public EscapeDialog(Dialog owner, String title) {
		super(owner, title);
		initialize();
	}

	public EscapeDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		initialize();
	}

	public EscapeDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		initialize();
	}

	public EscapeDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		initialize();
	}

	public EscapeDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		initialize();
	}

	public EscapeDialog(String title, boolean modal) {
		super((Frame)null, title, modal);
		initialize();
	}

	public static EscapeDialog createDialog(Window parent) {
		return createDialog(parent, null, false);
	}

	public static EscapeDialog createDialog(Window parent, String title) {
		return createDialog(parent, title, false);
	}

	public static EscapeDialog createDialog(Window parent, boolean modal) {
		return createDialog(parent, null, modal);
	}

	public static EscapeDialog createDialog(Component component) {
		return createDialog(getWindow(component), null, false);
	}

	public static EscapeDialog createDialog(Component component, String title) {
		return createDialog(getWindow(component), title, false);
	}

	public static EscapeDialog createDialog(Component component, boolean modal) {
		return createDialog(getWindow(component), null, modal);
	}

	public static EscapeDialog createDialog(Component component, String title, boolean modal) {
		return createDialog(getWindow(component), title, modal);
	}

	public static EscapeDialog createDialog(Window parent, String title, boolean modal) {
		EscapeDialog dlg;
		if (parent instanceof Dialog) {
			dlg = new EscapeDialog((Dialog)parent, title, modal);
		} else if (parent instanceof Frame) {
			dlg = new EscapeDialog((Frame)parent, title, modal);
		} else {
			dlg = new EscapeDialog((Frame)null, title, modal);
		}
		return dlg;
	}

	private static Window getWindow(Component component) {
		return (component == null ? null : (component instanceof Window ?
				(Window) component : SwingUtilities.windowForComponent(component)));
	}

	private void initialize() {
		getContentPane().setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addListeners(this);
	}

    private void addListeners(Component c) {
		c.addKeyListener(this);
		if (c instanceof Container) {
			Container cont = (Container) c;
			cont.addContainerListener(this);
			Component[] children = cont.getComponents();
			for (int i = 0; i < children.length; i++) {
				addListeners(children[i]);
			}
		}
	}

    public void keyPressed(KeyEvent e) {
    	int code = e.getKeyCode();
        if(code == KeyEvent.VK_ESCAPE){
        	performEscape(e);
        } else if(code == KeyEvent.VK_ENTER){
           performEnter(e);
        }
    }

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void componentAdded(ContainerEvent e) {
		addListeners(e.getChild());
	}

	public void componentRemoved(ContainerEvent e) {
		addListeners(e.getChild());
	}

	protected void performEscape(KeyEvent e) {
		setVisible(false);
	}

	protected void performEnter(KeyEvent e) {
		// does nothing
	}

	public void setDefaultButton(JButton defaultButton) {
		getRootPane().setDefaultButton(defaultButton);
	}

	public void setPreferredSize(Dimension d) {
		//super.setPreferredSize(d);	// @tag Shrimp.Java5.setPreferredSize
		super.setSize(d);
	}

	public Action createCloseAction() {
		return new AbstractAction("Close") {
			public void actionPerformed(ActionEvent e) {
				closePressed();
			}
		};
	}

	protected void closePressed() {
		dispose();
	}

	public Action createCancelAction() {
		return new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				cancelPressed();
			}
		};
	}

	protected void cancelPressed() {
		dispose();
	}

	public Action createOKAction() {
		return new AbstractAction("  OK  ") {
			public void actionPerformed(ActionEvent e) {
				okPressed();
			}
		};
	}

	protected void okPressed() {
		dispose();
	}

}
