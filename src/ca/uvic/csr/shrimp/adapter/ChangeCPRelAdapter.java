/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.adapter;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.JOptionPane;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ChildParentChooserWindow;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.util.CollectionUtils;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * This adapter displays a dialog allowing the user to choose a new hierarchy to
 * base the shrimp view on, and choose whether or not to show composites in the shrimp view.
 *
 * @author Casey Best, Rob Lintern
 */
public class ChangeCPRelAdapter extends DefaultProjectAction {

	public static final String ACTION_NAME = "Change Hierarchy...";
	
	// these bounds are used during the current sesesion to keep the dialog the same size
	private Rectangle bounds = new Rectangle();

	public ChangeCPRelAdapter(ShrimpProject project) {
		super(ACTION_NAME, ResourceHandler.getIcon("icon_hierarchy.gif"), project);
	}

	public void startAction() {
		Frame parentFrame = ApplicationAccessor.getParentFrame();
		try {
			ShrimpProject project = getProject();
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean (ShrimpTool.DISPLAY_BEAN);
			ChildParentChooserWindow chooser = new ChildParentChooserWindow(parentFrame, displayBean, dataBean, bounds);
			if (chooser.accepted()) {
			    String[] chosenCprels = chooser.getCprels();
                boolean chosenInverted = chooser.isInverted();
			    if (shouldChangeCprels(chosenCprels, chosenInverted, dataBean.getDefaultCprels(), dataBean.getDefaultCprelsInverted())) {
			        if (hasRootArtifacts(chosenCprels, chosenInverted, dataBean)) {
			            if (shouldShowManyRoots(chosenCprels, chosenInverted, dataBean)) {
							boolean dataBeanFiringEvents = dataBean.isFiringEvents();
							dataBean.setFiringEvents(false);
							shrimpView.setCprels(chosenCprels, chosenCprels.length > 0 && chosenInverted, true);
							shrimpView.addDefaultRootNodes(true);
							//shrimpView.navigateToObject(displayBean.getDataDisplayBridge().getRootNodes());
							dataBean.setFiringEvents(dataBeanFiringEvents);
			            }
				    }
			    }
			}
		} catch (ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		} catch (BeanNotFoundException bnfe) {
		    bnfe.printStackTrace();
		}
	}

	private String cprelsToString(String[] cprels) {
		String s = "";
		for (int i = 0; i < cprels.length; i++) {
			String cprel = cprels[i];
			if (i > 0) {
				s += ", ";
			}
			s += cprel;
		}
		return s;
	}

	private boolean shouldShowManyRoots(String[] cprels, boolean inverted, DataBean dataBean) {
	    Vector artifacts = cprels.length > 0 && inverted ? dataBean.getLeafArtifacts(cprels) : dataBean.getRootArtifacts(cprels);
	    return ShrimpUtils.shouldShowManyRoots(artifacts.size());
	}

	private boolean hasRootArtifacts(String[] cprels, boolean inverted, DataBean dataBean) {
	    Vector artifacts = cprels.length > 0 && inverted ? dataBean.getLeafArtifacts(cprels) : dataBean.getRootArtifacts(cprels);
	    if (artifacts.isEmpty()) {
	        // dont bother changing the hierarchy if there are no root artifacts.
			Object message = "The hierachy of \"" + cprelsToString(cprels) + "\" has no root nodes!" +
			"\nThe hierarchy will not be changed.";
	        JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), message,
	        		ApplicationAccessor.getAppName(), JOptionPane.INFORMATION_MESSAGE);
	    }
	    return !artifacts.isEmpty();
	}

    private boolean shouldLoadAllData() {
		Component parentComp = ApplicationAccessor.getParentFrame();
		String title = ApplicationAccessor.getAppName();
		String message = "This action will cause all data to be loaded into memory.\n" +
						"For large projects  this could take a long time and use up a lot of memory.\n" +
						"Are you sure you want to proceed?";
		int result = JOptionPane.showConfirmDialog(parentComp, message, title, JOptionPane.YES_NO_OPTION);
		return (result == JOptionPane.OK_OPTION);
    }

	private boolean shouldChangeCprels(String[] cprels, boolean inverted, String[] defaultCprels, boolean defaultCprelsInverted) {
	    // check if user wants to load all data, if not using the default hierarchy
		// **NOTE: there is an assumption made here that choosing the default hierarchy doesn't
		// require all data to be loaded from the back-end. So far (Sept 30, 2004), this is the case but
		// may not always be so for any new domains.
	    if (!CollectionUtils.haveSameElements(cprels, defaultCprels) || (inverted != defaultCprelsInverted)) {
	        return shouldLoadAllData();
	    }
	    return true;
	}

}
