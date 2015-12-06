/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Properties;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.DefaultIconProvider;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.IconProvider;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.OverlayIconDialog;
import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * This adapter lets the user choose an image which will be used as the node label icon.
 * This icon appears to the left of the node label.
 * It has contains come useful methods for setting the icon on any {@link ShrimpNode}.
 *
 * @author Chris Callendar
 * @date Sep 26, 2007
 * @tag Shrimp.ChangeNodeIcon
 */
public class ChangeNodeOverlayIconAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_CHANGE_OVERLAY_ICON;
	public static final String TOOLTIP = "Displays a dialog which lets the user choose an overlay icon for the selected node.  The icon can be positioned anywhere on the node.";

	private static final String ICON_KEY = "node.overlay.icon.";

	private ShrimpNode selectedNode;

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public ChangeNodeOverlayIconAdapter(ShrimpTool tool) {
    	this(ACTION_NAME, tool);
    }

	/**
	 * Use this constructor unless you already have the selected artifact.
	 * The selected node will be retrieved from the {@link SelectorBean}.
	 */
    public ChangeNodeOverlayIconAdapter(String actionName, ShrimpTool tool) {
    	super(actionName, ResourceHandler.getIcon("icon_node_overlay_add.gif"), tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;
		this.selectedNode = null;
    }

    /**
     * Use this constructor if you already have access to the node.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected node will be retrieved.
     */
    public ChangeNodeOverlayIconAdapter(ShrimpNode node) {
    	this(ACTION_NAME, node);
    }

    /**
     * Use this constructor if you already have access to the node.
     * Otherwise pass in the {@link ShrimpTool} to the other constructor and the
     * selected node will be retrieved.
     */
    public ChangeNodeOverlayIconAdapter(String actionName, ShrimpNode node) {
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
			try {
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
	    		OverlayIconDialog dlg = new OverlayIconDialog(tool.getGUI(), node, displayBean);
	    		if (dlg.isOKPressed()) {
	    			setOverlayIcon(node, dlg.getIconProvider());
	    		}
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
    }

	/**
	 * Sets the overlay icon provider for the given node.  Also saves this information
	 * in the project properties file if the icon provider is a {@link DefaultIconProvider}.
	 * @param node the node to set the icon on
	 * @param iconProvider the icon provider
	 */
	public void setOverlayIcon(ShrimpNode node, IconProvider iconProvider) {
		if (node == null) {
			return;
		}

		// no point in saving a provider if no image was chosen
		if ((iconProvider != null) && (iconProvider.getIcon() == null)) {
			// remove the icon provider
			iconProvider = null;
		}

		node.setOverlayIconProvider(iconProvider);
		node.repaint();

		// update properties
		if ((tool != null) && (tool.getProject() != null)) {
			Properties props = tool.getProject().getProperties();
			DefaultIconProvider dip = (iconProvider instanceof DefaultIconProvider ? (DefaultIconProvider) iconProvider : null);
			saveNodeOverlayIcon(node, dip, props);
		}
	}

	/**
	 * Removes the overlay icon provider from the given node if one exists.  Also removes it
	 * from the properties file.
	 * @param node
	 */
	public void removeOverlayIcon(ShrimpNode node) {
		setOverlayIcon(node, null);
	}

	/**
	 * Saves the icon provider into the properties file.  If the icon provider is null then the node overlay icon
	 * property is removed from the properties file.
	 * @param node
	 * @param iconProvider
	 * @param props
	 */
	private void saveNodeOverlayIcon(ShrimpNode node, DefaultIconProvider iconProvider, Properties props) {
		String key = ICON_KEY + node.getArtifact().getID();
		if (iconProvider == null) {
			props.remove(key);
		} else {
			String value = iconProvider.getPropertyValue();
			if (value.length() > 0) {
				props.setProperty(key, value);
			} else {
				props.remove(key);
			}
		}
	}

	/**
	 * Attempts to load the overlay icon provider for this node.
	 * @param node
	 * @param props
	 */
	public static void loadNodeOverlayIcon(ShrimpNode node, Properties props) {
		String key = ICON_KEY + node.getArtifact().getID();
		if (props.containsKey(key)) {
			String value = props.getProperty(key);
			if (value.length() > 0) {
				DefaultIconProvider dip = new DefaultIconProvider();
				dip.parsePropertyValue(value);
				node.setOverlayIconProvider(dip);
			}
		}
	}

}
