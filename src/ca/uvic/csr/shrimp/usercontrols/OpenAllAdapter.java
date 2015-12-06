/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ProgressDialog;

/**
 * This OpenAll adapter opens and closes nodes and relationships in the selected nodes.
 *
 * @author YiLing Lu
 * @date Feb 7, 2001
 */
public class OpenAllAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_EXPAND_ALL_DESCENDANTS;
	public static final String TOOLTIP = "Expands (opens) all descendants in a nested graph.\nWarning - this might take a while.";

	private Collection nodeTypesToOpen;

	//progress variables
	private int toOpen = 0;
	private int numOpened = 0;

	static int threadCount = 1;

    public OpenAllAdapter(ShrimpProject project, ShrimpTool tool, Collection nodeTypesToOpen) {
    	super(ACTION_NAME, project, tool);
    	setToolTip(TOOLTIP);
		this.mustStartAndStop = false;
		this.nodeTypesToOpen = nodeTypesToOpen;
    }

    public OpenAllAdapter(ShrimpProject project, ShrimpTool tool) {
    	this(project, tool, null);
    }

	/**
	 * perform the action
	 */
    public void startAction() {
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			DataBean dataBean = (DataBean) getBean(ShrimpProject.DATA_BEAN);

	    	final Vector targetNodes = getSelectedNodes();
			// if there are no selected nodes then open all on root
			if (targetNodes.isEmpty()) {
				targetNodes.addAll(displayBean.getDataDisplayBridge().getRootNodes());
			}

			boolean displayVisible = displayBean.isVisible();
			//boolean displayEnabled = displayBean.isEnabled();
			boolean dataBeanFiringEvents = dataBean.isFiringEvents();
			ApplicationAccessor.waitCursor();
			try {
				// change to wait cursor
				//displayBean.setEnabled(false);
				displayBean.setVisible(false);
				dataBean.setFiringEvents(false);

				// For the progress dialog: count number of descendents that will be added to the display
				toOpen = 0;
				ProgressDialog.showProgress();
				ProgressDialog.setSubtitle("Showing All Descendents - Collecting Data ...");

				Set artifactsSoFar = new HashSet ();
				for (int i = 0; i<targetNodes.size(); i++) {
					ShrimpNode targetNode = (ShrimpNode)targetNodes.elementAt(i);
					countToOpen(displayBean, targetNode, artifactsSoFar);
				}

				ProgressDialog.setSubtitle("Showing All Descendents - Updating Display ...");
				ProgressDialog.setNote("");
				numOpened = 0;
				for (int i = 0; i < targetNodes.size(); i++) {
					ShrimpNode targetNode = (ShrimpNode)targetNodes.elementAt(i);
					List pathToThisNode = new ArrayList ();
					openNodeRecursively(displayBean, targetNode, pathToThisNode);
				}

			} catch (Exception e){
				e.printStackTrace();
			} finally {
				ApplicationAccessor.defaultCursor();
				//displayBean.setEnabled(displayEnabled);
				displayBean.setVisible(displayVisible);
				dataBean.setFiringEvents(dataBeanFiringEvents);
				ProgressDialog.tryHideProgress();
			}
		} catch (BeanNotFoundException bnfe) {
		  	bnfe.printStackTrace();
		}
	}

	/**
	 *
	 * @param currentNode
	 * @param pathToThisNode The purpose of this set is to stop infinite loops from occuring.
	 */
	private void openNodeRecursively(DisplayBean displayBean, ShrimpNode currentNode, List pathToThisNode) {
		if (ProgressDialog.isCancelled()) {
			return;
		}
		if (pathToThisNode.contains(currentNode.getArtifact())) {
			return;
		}

		// don't bother opening a filtered artifact
		if (displayBean.isFiltered(currentNode.getArtifact())) {
			return;
		}

		// open target in children mode, if it has any
		Vector children = displayBean.getDataDisplayBridge().getChildNodes(currentNode, true);

		boolean openBasedOnNumChildren = children.size() > 0;
		boolean openBasedOnType;
		if (nodeTypesToOpen != null) {
		    openBasedOnType = false;
			boolean oneChildHasValidType = false; // dont open a node unless one of its children has a valid type
			for (Iterator iter = children.iterator(); iter.hasNext();) {
				ShrimpNode childNode = (ShrimpNode) iter.next();
				String type = childNode.getArtifact().getType();
				if (nodeTypesToOpen.contains(type)) {
					oneChildHasValidType = true;
					break;
				}
			}
			openBasedOnType = oneChildHasValidType;
		} else {
		    openBasedOnType = true;
		}

		if (openBasedOnNumChildren && openBasedOnType) {
			if  (!displayBean.getPanelMode (currentNode).equals (PanelModeConstants.CHILDREN)) {
				displayBean.setPanelMode (currentNode, PanelModeConstants.CHILDREN);
			}
			numOpened++;
			//if (numOpened % 10 == 0) {
			    ProgressDialog.setNote (numOpened + " of " + toOpen + " opened.");
			//}

			// recursively open descendents
			pathToThisNode.add(currentNode.getArtifact());
			for (int i = 0; i < children.size(); i++) {
				ShrimpNode childNode = (ShrimpNode) children.elementAt(i);
				openNodeRecursively(displayBean, childNode, pathToThisNode);
			}
			pathToThisNode.remove(currentNode.getArtifact());
		}
	}


	// counts number of nodes that will be opened during open all
	private void countToOpen(DisplayBean displayBean, ShrimpNode currentNode, Set pathSoFar) {
		if (ProgressDialog.isCancelled()) {
			return;
		}

		if (pathSoFar.contains(currentNode.getArtifact())) {
			return;
		}

		if (nodeTypesToOpen == null || nodeTypesToOpen.contains(currentNode.getArtifact().getType())) {
			pathSoFar.add(currentNode.getArtifact());
			Vector children = displayBean.getDataDisplayBridge().getChildNodes(currentNode, true);
			if (children.size() > 0) {
				toOpen += 1;
			    ProgressDialog.setNote ("found "  + toOpen + (toOpen > 1? " nodes" : " node")+ " to open so far - (" + currentNode.getName() /*+ ": + has " + children.size() + (children.size() > 1 ? " children" : " child") */ + ")");
				for (int i=0; i<children.size() && !ProgressDialog.isCancelled(); i++) {
					ShrimpNode child = (ShrimpNode) children.elementAt(i);
					if (!displayBean.isFiltered(child.getArtifact())) {
						countToOpen(displayBean, child, pathSoFar);
					}
				}
			}
			pathSoFar.remove(currentNode.getArtifact());
		}
	}

}

