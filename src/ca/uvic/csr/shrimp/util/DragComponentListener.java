/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.gui.QuickStartComponent;


/**
 * Moves a {@link Component} based on drag events.
 * Requires the component's parent's layout to be null since the component's location
 * is adjusted based on the mouse drag events.
 * <p>
 * Also optionally can constrain the component within the parent's bounds.
 *
 * @author Chris Callendar
 * @date 11-Apr-07
 */
public class DragComponentListener extends MouseAdapter implements MouseMotionListener {

	private Component comp;
	private Point srcStart;
	private boolean constrain;

	public DragComponentListener(Component comp, boolean constrainToParentBounds) {
		this.comp = comp;
		this.constrain = constrainToParentBounds;
		this.srcStart = new Point();
	}

	public void mousePressed(MouseEvent e) {
		srcStart = new Point(e.getPoint());
	}

	public void mouseDragged(MouseEvent e) {
		moveComponent(e.getPoint());
	}

	public void mouseMoved(MouseEvent e) {
	}

	private void moveComponent(Point src) {
		int dx = src.x - srcStart.x;
		int dy = src.y - srcStart.y;
		Point pos = new Point(comp.getX() + dx, comp.getY() + dy);
		Container parent = comp.getParent();
		if (constrain && (parent != null)) {
			if ((pos.x + comp.getWidth()) > parent.getWidth()) {
				pos.x = parent.getWidth() - comp.getWidth();
			}
			if ((pos.y + comp.getHeight()) > parent.getHeight()) {
				pos.y = parent.getHeight() - comp.getHeight();
			}
			pos.x = Math.max(0, pos.x);
			pos.y = Math.max(0, pos.y);
		}
		comp.setLocation(pos);
	}


	public static void main(String[] args) {
		JDialog dlg = new JDialog();
		dlg.setModal(true);
		dlg.setTitle("");
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel pnl = new JPanel(new FlowLayout());
		JButton btn = new JButton("Test");
		pnl.add(btn);
		pnl.setBackground(Color.blue);
		dlg.getContentPane().add(pnl, BorderLayout.CENTER);

		dlg.getGlassPane().setVisible(true);
		final JComponent glassPane = (JComponent) dlg.getGlassPane();
		glassPane.setLayout(null);

		QuickStartComponent showQS = new QuickStartComponent("http://www.google.ca");
		showQS.setPreferredSize(new Dimension(390, 350));
		final CollapsiblePanel cp = new CollapsiblePanel("Test", showQS);
		cp.setBounds(20, 40, 400, 400);
		cp.setShowCloseButton(true);
		glassPane.add(cp);

		DragComponentListener dcl = new DragComponentListener(cp, true);
		cp.getTitleLabel().addMouseListener(dcl);
		cp.getTitleLabel().addMouseMotionListener(dcl);

		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cp.getParent() == null) {
					glassPane.add(cp);
					cp.setBounds(20, 40, 400, 400);
				}
			}
		});

		dlg.pack();
		dlg.setLocation(400, 200);
		dlg.setSize(600, 600);
		dlg.setVisible(true);
	}

}
