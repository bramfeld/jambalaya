/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.AbstractArtifact;
import ca.uvic.csr.shrimp.DataDisplayBridge.DataDisplayBridge;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;

/**
 * This adapter handles requests to rename the currently selected artifacts. It
 * displays a dialog box for each selected artifacts and allows the user to
 * enter a new name. Names are updated in the selected nodes and also in the
 * associated properties file.
 * Note that this class also serves as a manager for renaming nodes in
 * general and is called at startup and refresh times to initialize the node
 * name stacks from the properties file.
 * @tag Shrimp(grouping)
 * @author Chris Bennett
 * @author Chris Callendar
 */
public class RenameSelectedArtifactsAdapter extends DefaultToolAction {

	public static final String ACTION_NAME = ShrimpConstants.ACTION_NAME_RENAME;
	public static final String TOOLTIP = "Renames the selected node(s).\nDisplays a dialog which lets you enter in a new name for each node.";

	public static final int RENAME_CANCELLED = JOptionPane.CANCEL_OPTION;
	public static final int RENAME_RESTORED  = JOptionPane.NO_OPTION;
	public static final int RENAME_CHANGED   = JOptionPane.YES_OPTION;

	private static final String NAME_PROPERTY_PREFIX = "name.";
	private static final String ANNOTATION_PROPERTY_PREFIX = "annotation";

	private JPanel renamePanel = null;
	private JTextField renameText = null;
	private JTextArea annotateText = null;

	/**
	 * Constructs a new RenameSelectedArtifactsAdapter
	 * @param tool the tool that this adapter acts upon.
	 */
	public RenameSelectedArtifactsAdapter(ShrimpTool tool) {
		super(ACTION_NAME, tool);
		setToolTip(TOOLTIP);
		mustStartAndStop = false;
	}

	/**
	 * Constructs a new RenameSelectedArtifactsAdapter
	 * @param tool the tool that this adapter acts upon.
	 */
	public RenameSelectedArtifactsAdapter(ShrimpProject project, ShrimpTool tool) {
		super(ACTION_NAME, project, tool);
		mustStartAndStop = false;
	}

	public boolean isEnabled() {
    	Vector selectedNodes = getSelectedNodes();
    	return super.isEnabled() && (selectedNodes.size() > 0);
	}

	public void startAction() {
		Vector selectedNodes = getSelectedNodes();
		for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
			changeName((ShrimpNode) iterator.next());
		}
	}

	/**
	 * Change a nodes name using a user dialog
	 * @return one of {@link RenameSelectedArtifactsAdapter#RENAME_CANCELLED},
	 * {@link RenameSelectedArtifactsAdapter#RENAME_CHANGED},
	 * {@link RenameSelectedArtifactsAdapter#RENAME_RESTORED}.
	 */
	public int changeName(ShrimpNode node) {
		boolean showRestore = (node.getSavedNames().length > 0);
		String[] options = (showRestore ? new String[]{ "OK", "Cancel", "Restore" } : new String[]{ "OK", "Cancel" });
		getRenameTextField().setText(node.getName());
		getAnnotateTextArea().setText(getAnnotationProperty(node));
		int result = JOptionPane.showOptionDialog((tool != null ? tool.getGUI() : null),
										getRenamePanel(), "Rename Node", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (result == RENAME_CHANGED) {
			node.rename(renameText.getText().trim());
			saveProperties(node);

		} else if (result == RENAME_RESTORED) {
			node.restoreName();
			removeNameProperty(node);
		}
		return result;
	}

	/**
	 * Return a complete panel for display in the dialog box
	 */
	private JPanel getRenamePanel() {
		if (renamePanel == null) {
			renamePanel = new JPanel(new BorderLayout(5, 5));

			Dimension d = new Dimension(70, 16);
			JLabel lbl = new JLabel("Name: ");
			lbl.setPreferredSize(d);
			JPanel namePanel = 	new JPanel(new BorderLayout());
			namePanel.add(lbl, BorderLayout.WEST);
			namePanel.add(getRenameTextField(), BorderLayout.CENTER);
			renamePanel.add(namePanel, BorderLayout.NORTH);

			lbl = new JLabel("Comments: ");
			lbl.setPreferredSize(d);
			JPanel annotatePanel = 	new JPanel(new BorderLayout());
			annotatePanel.add(lbl, BorderLayout.WEST);
			annotatePanel.add(new JScrollPane(getAnnotateTextArea()), BorderLayout.CENTER);

			renamePanel.add(annotatePanel, BorderLayout.CENTER);
			renamePanel.setPreferredSize(new Dimension(225, 90));
		}
		return renamePanel;
	}

	/**
	 * Return the name text field
	 * @return
	 */
	private JTextField getRenameTextField() {
		if (renameText == null) {
			renameText = new JTextField();
		}
		return renameText;
	}

	/**
	 * Return the annotations text area
	 * @return
	 */
	private JTextArea getAnnotateTextArea() {
		if (annotateText == null) {
			annotateText = new JTextArea();
			// ensure this is the same as text field's font
			annotateText.setFont(getRenameTextField().getFont());
			annotateText.setLineWrap(true);
			annotateText.setWrapStyleWord(true);
		}
		return annotateText;
	}

	/**
	 * Update all the nodes' names based on what is in the related
	 * property file's name properties
	 */
	public void updateNodeNames() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			DataDisplayBridge bridge = displayBean.getDataDisplayBridge();
			Vector nodes = getAllNodes(bridge);
			for (int i = 0; i < nodes.size(); i++) {
				ShrimpNode node = (ShrimpNode)nodes.get(i);
				String propertyName = makeNamePropertyName(getNodeId(node));
				Vector names = getCSVPropertyValues(propertyName);
				for (int j = 0; j < names.size(); j++) { // process names
					String newName = (String) names.get(j);
					node.rename(newName);
				}
			}
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get all the shrimp nodes, adding child nodes if needed
	 * @param bridge
	 * @return
	 */
	private Vector getAllNodes(DataDisplayBridge bridge) {
		Vector nodes = bridge.getShrimpNodes();
		if (nodes.size() == 1) { // only a root node, so get children
			ShrimpNode parent = (ShrimpNode)nodes.get(0);
			nodes.addAll(bridge.getChildNodes(parent, true));
		}
		return nodes;
	}

	/**
	 * Save the name and annotation properties
	 * @param node
	 */
	private void saveProperties(ShrimpNode node) {
		Properties properties = getProject().getProperties();
		String namePropertyName = makeNamePropertyName(getNodeId(node));
		Vector values = getCSVPropertyValues(namePropertyName);
		values.add(node.getName());
		properties.put(namePropertyName, makeStringFromValues(values));
		properties.put(makeAnnotationPropertyName(node), annotateText.getText());

		Component annotationPanel = node.getArtifact().getCustomizedPanel(PanelModeConstants.PANEL_ANNOTATION);
		if (annotationPanel != null) {
			AbstractArtifact.updateAnnotationPanel(annotationPanel, annotateText.getText());
		}
		getProject().saveProperties();
	}

	/**
	 * Return the annotation string for the specified node from the properties file
	 * TBD - refactor to use DataDisplayBridge getAnnotationProperty
	 * @param node
	 */
	private String getAnnotationProperty(ShrimpNode node) {
		Properties properties = getProject().getProperties();
		String propertyName = makeAnnotationPropertyName(node);
		return properties.getProperty(propertyName);
	}

	/**
	 * Remove the current node's name from the name property
	 *
	 * @param node
	 */
	public void removeNameProperty(ShrimpNode node) {
		String propertyName = makeNamePropertyName(getNodeId(node));
		Vector values = getCSVPropertyValues(propertyName);
		if (values.size() <= 1) {
			clearProperty(propertyName);
		} else { // prune list of summary numbers
			values.remove(values.size() - 1); // assume the names match
			getProject().getProperties().put(propertyName,
					makeStringFromValues(values));
		}
	}

	/**
	 * Get a list of values associated with the specified property. Assumes that
	 * the property value is a comma separated list property (may be empty)
	 *
	 * @param summaryPropertyName
	 * @return
	 */
	private Vector getCSVPropertyValues(String propertyName) {
		Vector values = new Vector();
		Properties properties = getProject().getProperties();
		String currentSummaryValue = properties.getProperty(propertyName, null);
		if (currentSummaryValue != null) {
			StringTokenizer tokenizer = new StringTokenizer(
					currentSummaryValue, ",");
			while (tokenizer.hasMoreTokens()) {
				values.add(tokenizer.nextToken());
			}
		}
		return values;
	}

	/**
	 * Make a comma separated string from a list of values
	 *
	 * @param values
	 * @return
	 */
	private Object makeStringFromValues(Vector values) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < values.size(); i++) {
			sb.append(values.get(i));
			if (i < values.size() - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private String getNodeId(ShrimpNode node) {
		return node.getArtifact().getExternalId().toString();
	}

	/**
	 * Clear the group property for the selected property name
	 * @param propertyName
	 */
	private void clearProperty(String propertyName) {
		Properties properties = getProject().getProperties();
		properties.remove(propertyName);
	}

	/**
	 * Make a summary property name for the specified node
	 * @param node
	 * @return
	 */
	private String makeNamePropertyName(String nodeId) {
		return NAME_PROPERTY_PREFIX + nodeId;
	}

	/**
	 * Make a summary property name for the specified node
	 * @param node
	 * @return
	 */
	private String makeAnnotationPropertyName(ShrimpNode node) {
		return ANNOTATION_PROPERTY_PREFIX + node.getArtifact().getID();
	}


}