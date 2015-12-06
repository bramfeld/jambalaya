/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TransparentPanel;


/**
 * Displays all option panels as a set of tabs.
 * Pressing OK or Cancel buttons calls the respective routines in each of the panels (tabs).
 *
 * @author Jeff Michaud, Chris Callendar
 */
public class ShrimpOptionsDialog extends EscapeDialog {

	private JTabbedPane tabbedPane;
	private Vector tabs;
	private JButton btnCancel;
	private JButton btnOK;

	public ShrimpOptionsDialog(Frame parent, boolean modal) {
		super(parent, "Shrimp Options", modal);
		this.tabs = new Vector();
		initComponents();
	}

	public int getSelectedTabIndex() {
		return tabbedPane.getSelectedIndex();
	}

	public void setSelectedTabIndex(int index) {
		tabbedPane.setSelectedIndex(index);
	}

    private void initComponents() {
    	GradientPanel contentPane = new GradientPanel(new BorderLayout());

    	this.tabbedPane = new JTabbedPane();
    	tabbedPane.setOpaque(false);

    	TransparentPanel mainPanel = new TransparentPanel(new BorderLayout());
    	mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
    			BorderFactory.createEmptyBorder(5, 10, 10, 10)));
    	mainPanel.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(mainPanel, BorderLayout.CENTER);

		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnl.add(getOKButton());
		pnl.add(getCancelButton());
		contentPane.add(pnl, BorderLayout.SOUTH);
		setContentPane(contentPane);

		pack();
		setSize(600, 525);
		setDefaultButton(getOKButton());
		ShrimpUtils.centerOnScreen(this);
    }

	private JButton getCancelButton() {
		if (btnCancel == null) {
			btnCancel = new JButton(new AbstractAction("Cancel") {
				public void actionPerformed(ActionEvent e) {
					for (Iterator iter = tabs.iterator(); iter.hasNext();) {
						ShrimpOptions element = (ShrimpOptions) iter.next();
						element.cancel();
					}
					dispose();
				}
			});
		}
		return btnCancel;
	}

	private JButton getOKButton() {
		if (btnOK == null) {
			btnOK = new JButton(new AbstractAction("  OK  ") {
				public void actionPerformed(ActionEvent e) {
					for (Iterator iter = tabs.iterator(); iter.hasNext();) {
						ShrimpOptions element = (ShrimpOptions) iter.next();
						element.ok();
					}
					dispose();
				}
			});
		}
		return btnOK;
	}

	/**
	 * Adds an option tab to this options dialog
	 * @param tabName
	 * @param tab
	 */
    public void addTab(String tabName, JPanel tab) {
    	this.tabbedPane.addTab(tabName, tab);
    	tabs.add(tab);
    }

}
