/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.AttributePanel;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * @author Xiaomin Wu, Chris Callendar
 */
public class OpenAttributePanelAdapter extends AbstractOpenApplicationToolAdapter {
	
	private static final Rectangle BOUNDS = new Rectangle(DOCK_CENTER, DOCK_CENTER, 400, 400);

	private int targetType;
	
    public OpenAttributePanelAdapter(int targetType) {
    	super(determineName(targetType), ResourceHandler.getIcon("icon_attribute_panel.gif"), BOUNDS);
    	this.targetType = targetType;
    }

	private static String determineName(int targetType) {
		return targetType == DataBean.ARTIFACT_TYPE ? ShrimpProject.NODE_ATTRIBUTE_PANEL : ShrimpProject.ARC_ATTRIBUTE_PANEL;
	}
	
	protected void createTool() {
		tool = new AttributePanel(getProject(), targetType);

		// add the Close button (don't want this in Creole)
		final JComponent gui = ((JComponent)tool.getGUI());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton closeButton = new JButton(new AbstractAction(" Close ") {
			public void actionPerformed(ActionEvent e) {
				Window window = SwingUtilities.getWindowAncestor(gui);
				window.dispose();
			}
		});
		buttonPanel.add(closeButton);
		gui.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	protected boolean beforeActionHasRun() {
		String msg = "This process needs to analyze the project and could take a few moments.\nDo you want to continue?";
		int choice = JOptionPane.showConfirmDialog(null, msg, "Please Confirm...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		return (choice == JOptionPane.OK_OPTION);	
	}
	
}