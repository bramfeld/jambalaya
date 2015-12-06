/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ImageFileFilter;

/**
 * This adapter lets the user choose an image which will be used as the node label icon.
 * This icon appears to the left of the node label.
 * It has contains come useful methods for setting the icon on any {@link ShrimpNode}.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.ChangeNodeIcon
 */
public class ChangeNodeLabelIconAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_CHANGE_LABEL_ICON;
	public static final String TOOLTIP = "Changes the icon for the given node.  The icon will appear to the left of the node label.";

	private ShrimpNode selectedNode;
	private JFileChooser fileChooser;

	/**
	 * Convenience constructor.  Only use this if you don't plan to start the action.
	 */
	public ChangeNodeLabelIconAdapter() {
		this(ACTION_NAME, (ShrimpTool)null);
	}

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public ChangeNodeLabelIconAdapter(ShrimpTool tool) {
    	this(ACTION_NAME, tool);
    }

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public ChangeNodeLabelIconAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, ResourceHandler.getIcon("icon_node_label_add.gif"), tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;
		this.selectedNode = null;
    }

    /**
     * Use this constructor if you already have access to the node.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected node will be retrieved.
     */
    public ChangeNodeLabelIconAdapter(ShrimpNode node) {
    	this(ACTION_NAME, node);
    }

    /**
     * Use this constructor if you already have access to the node.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected node will be retrieved.
     */
    public ChangeNodeLabelIconAdapter(String actionName, ShrimpNode node) {
    	this(actionName, (ShrimpTool)null);
    	this.selectedNode = node;
    }

    public boolean canStart() {
    	return super.canStart() || (selectedNode != null);
    }

    public void startAction() {
    	ShrimpNode node = selectedNode;
    	if (node == null) {
    		node = getSelectedNode();
    	}
    	if (node != null) {
    		String file = chooseFile(node);
    		if (file != null) {
    			setNodeIcon(node, file);
    		}
		}
    }

	/**
	 * Loads the icon and sets it for all the {@link ShrimpNode} objects in the {@link Vector}.
	 */
    public void setNodeIcon(Vector nodes, String iconPath) {
    	ImageIcon icon = null;
		if ((iconPath != null) && (iconPath.length() > 0)) {
			icon = new ImageIcon(iconPath);
		}
		setNodeIcon(nodes, icon);
    }

	/**
	 * Sets the same icon for all the {@link ShrimpNode} objects in the {@link Vector}.
	 */
	public void setNodeIcon(Vector nodes, Icon icon) {
		for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
			Object obj = iter.next();
			if (obj instanceof ShrimpNode) {
				ShrimpNode node = (ShrimpNode) obj;
				setNodeIcon(node, icon);
			}
		}
	}

    /**
     * Loads the icon for the given file and sets it onto the selected target node.
     * @param iconPath the icon to load
     */
    public void setSelectedNodeIcon(String iconPath) {
    	ShrimpNode node = selectedNode;
    	if (node == null) {
    		node = getSelectedNode();
    	}
    	if (node != null) {
			setNodeIcon(node, iconPath);
		}
    }

    /**
     * Sets the icon onto the selected target node.
     * @param icon the icon to set
     */
    public void setSelectedNodeIcon(Icon icon) {
    	ShrimpNode node = selectedNode;
    	if (node == null) {
    		node = getSelectedNode();
    	}
    	if (node != null) {
   			setNodeIcon(node, icon);
		}
    }

    /**
     * Sets the icon for the given file but doesn't save that information in the properties file.
     */
	public void setNodeIcon(ShrimpNode node, String path) {
		setNodeIcon(node, path, false);
	}

    /**
     * Sets the icon for the given file and possibly saves that information in the properties file.
     */
	public void setNodeIcon(ShrimpNode node, String path, boolean saveInProperties) {
		try {
			ImageIcon icon = null;
			if ((path != null) && (path.length() > 0)) {
				icon = new ImageIcon(path);
			}
			setNodeIcon(node, icon);

			if (saveInProperties) {
				// TODO
			}
		} catch (Exception ex) {
			String msg = ApplicationAccessor.getAppName() + ": Error loading icon - " + ex.getMessage();
			System.err.println(msg);
		}
	}

	public void setNodeIcon(ShrimpNode node, Icon icon) {
		if (node != null) {
			// TODO save existing icon?
			node.setIcon(icon);

			// update the node label too
			try {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				ShrimpNodeLabel label = displayBean.getDataDisplayBridge().getShrimpNodeLabel(node, false);
				if (label != null) {
					label.setIcon(icon);
				}
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Removes the icon for the given node.
	 */
	public void removeNodeIcon(ShrimpNode node) {
		setNodeIcon(node, (Icon)null);
	}

	protected String chooseFile(ShrimpNode node) {
		String path = null;
		JFileChooser chooser = getFileChooser();
		chooser.setDialogTitle("Choose an icon for '" + node.getName() + "'");
		int rv = chooser.showOpenDialog(ApplicationAccessor.getParentFrame());
		if (rv == JFileChooser.APPROVE_OPTION) {
			path = chooser.getSelectedFile().getAbsolutePath();
		}
		return path;
    }

    protected JFileChooser getFileChooser() {
    	if (fileChooser == null) {
    		fileChooser = new JFileChooser();
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setFileFilter(new ImageFileFilter());
    	}
    	return fileChooser;
    }

}
