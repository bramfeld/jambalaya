/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 *
 * Created on Nov 5, 2002
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.LayoutConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.layout.Layout;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * Displays options for changing the default layout
 *
 * @author Nasir Rather, Chris Callendar
 * @date Nov 5, 2002
 */
public class LayoutOptionsPanel extends JPanel implements ShrimpOptions {

	private DisplayBean displayBean;
	private Properties properties;

	// these represent the properites we want to ask user for
	private String layoutMode;
	private boolean resetLayout;

	public LayoutOptionsPanel(DisplayBean db) {
		this.displayBean = db;
		this.properties = ApplicationAccessor.getProperties();

		this.resetLayout = false;
		this.layoutMode = properties.getProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE,
				DisplayBean.PROPERTY_DEFAULT_VALUE__DEFAULT_LAYOUT_MODE);

		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));


		// get all available layouts
		Vector layouts = displayBean.getLayouts();
		Vector layoutNames = new Vector(layouts.size());
		for (Iterator iter = layouts.iterator(); iter.hasNext();) {
			Layout layout = (Layout) iter.next();
			layoutNames.add(layout.getName());
		}
		// don't want users to choose the motion layout as the default
		layoutNames.remove(LayoutConstants.LAYOUT_MOTION);
		 // sort names alphabetically
		Collections.sort(layoutNames, String.CASE_INSENSITIVE_ORDER);

		final JComboBox comboBox = new JComboBox(layoutNames);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layoutMode = comboBox.getSelectedItem().toString();
			}
		});
		comboBox.setSelectedItem(layoutMode);

		final JCheckBox checkBox = new JCheckBox("Reset layouts of the nodes I have already visited in the current graph?");
		checkBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetLayout = checkBox.isSelected();
			}
		});

		JPanel first = new JPanel(new FlowLayout(FlowLayout.CENTER));
		first.add(new JLabel(" Default layout algorithm: "));
		first.add(comboBox);

		JPanel second = new JPanel(new FlowLayout(FlowLayout.CENTER));
		second.add(checkBox);

		JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.add(first);
		panel.add(second);
		this.add(panel, BorderLayout.NORTH);
	}

	private void applySettings() {
		// save settings to properties
		properties.setProperty(DisplayBean.PROPERTY_KEY__DEFAULT_LAYOUT_MODE, layoutMode);
		if (resetLayout) {
			Collection rootNodes = displayBean.getDataDisplayBridge().getRootNodes();
			for (Iterator iter = rootNodes.iterator(); iter.hasNext();) {
                ShrimpNode rootNode = (ShrimpNode) iter.next();
    			displayBean.setLayoutMode(displayBean.getDataDisplayBridge().getDescendentNodes(rootNode, false),
    					layoutMode, false, false);
            }
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#ok()
	 */
	public void ok() {
		applySettings();
	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#cancel()
	 */
	public void cancel() {
	}

}
