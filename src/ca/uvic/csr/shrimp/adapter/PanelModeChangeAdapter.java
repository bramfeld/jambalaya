/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Properties;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.usercontrols.DefaultToolAction;

/**
 * This adapter handles requests to change the panel mode of the
 * currently selected nodes.
 *
 * @author Casey Best, Chris Callendar
 * @date July 27, 2000
 */
public class PanelModeChangeAdapter extends DefaultToolAction {

	public final static String WARNING_KEY = "PanelModeChangeWarningKey";

	public PanelModeChangeAdapter(String actionName, Icon icon, ShrimpTool tool) {
		super(actionName, icon, tool);
	}

	public PanelModeChangeAdapter(String actionName, ShrimpTool tool) {
	    super(actionName, tool);
	}

	/**
	 * Processes the request to change the zoom mode
	 */
	public void startAction() {
		String newMode = (String) getValue(Action.NAME);
		changePanelMode(newMode);
	}

	public void changePanelMode(String newMode) {
		DisplayBean displayBean = null;
		SelectorBean selectorBean = null;
		try {
			displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			selectorBean = (SelectorBean) tool.getBean(ShrimpTool.SELECTOR_BEAN);
		} catch (BeanNotFoundException bnfe) {
			bnfe.printStackTrace();
			return;
		}

		Vector targets = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);

		//warn users about mass panel-mode change
		// 10? don't know why 10 - could make it 20 if you like
		if(targets.size() > 10) {
			boolean showWarning = true;
			Properties props = ApplicationAccessor.getProperties();
			String showWarningStr = props.getProperty(WARNING_KEY);

			if (showWarningStr != null) {
				showWarning = Boolean.valueOf(showWarningStr).booleanValue();
			}
			if (showWarning) {
				Object[] options = { "OK", "CANCEL" };
				String msg = "You are trying to look at the " + newMode + " panels of many nodes.\n" +
							"This may require a lot of resources. Click OK to continue";
				JOptionPane pane = new JOptionPane(msg, JOptionPane.WARNING_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
				pane.setInitialValue(options[0]);

				JDialog	dialog = pane.createDialog(ApplicationAccessor.getParentFrame(), "Show " + newMode);
				pane.selectInitialValue();
				JCheckBox checkBox = new JCheckBox ("Do not show this warning again.");
				dialog.getContentPane().add(checkBox, BorderLayout.SOUTH);
				dialog.pack();
				dialog.setVisible(true);

				showWarning = !checkBox.isSelected();
				props.setProperty(WARNING_KEY, "" + showWarning);
				Object selectedValue = pane.getValue();
				if (selectedValue == options[1]) {
					return;
				}
			}
		}

		Collections.sort(targets, Collections.reverseOrder());
		for (int i = 0; i < targets.size(); i++) {
			Object t = targets.elementAt(i);
			if (t instanceof ShrimpNode) {
				ShrimpNode sn = (ShrimpNode) t;
				if(sn.getArtifact().getCustomizedPanelNames().contains(newMode)) {
					// focus on the node first
					displayBean.focusOn(sn);
					displayBean.setPanelMode(sn, newMode);
				}
			}
		}
	}

}