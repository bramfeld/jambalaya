/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;


/**
 * Extends {@link JDialog} to detect when the escape key is pressed and hide the dialog.
 * Also sets up the default close operation to be dispose, and sets the content pane to use
 * a {@link BorderLayout}.
 *
 * @author Chris Callendar
 * @date 17-Apr-07
 */
public class EscapeWindow extends JWindow implements KeyListener, ContainerListener {

	public EscapeWindow(Frame owner) {
		super(owner);
		initialize();
	}

	public EscapeWindow(Window owner) {
		super(owner);
		initialize();
	}

	public EscapeWindow(Component c) {
		super(SwingUtilities.windowForComponent(c));
		initialize();
	}

	private void initialize() {
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

}
