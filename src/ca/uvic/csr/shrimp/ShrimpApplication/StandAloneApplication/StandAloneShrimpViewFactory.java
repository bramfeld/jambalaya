/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication.StandAloneApplication;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.ShrimpProject.StandAloneProject.StandAloneProject;
import ca.uvic.csr.shrimp.gui.DesktopInternalFrame;
import ca.uvic.csr.shrimp.gui.ActionManager.ActionManager;
import ca.uvic.csr.shrimp.gui.ActionManager.CheckBoxAction;
import ca.uvic.csr.shrimp.gui.ShrimpView.AbstractShrimpViewFactory;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpViewConfiguration;
import ca.uvic.csr.shrimp.gui.quickview.QuickViewPanel;

class StandAloneShrimpViewFactory extends AbstractShrimpViewFactory {

	private ShrimpInternalFrameAdapter internalFrameAdapter;
	private StandAloneApplication standAloneApplication;

	public StandAloneShrimpViewFactory(StandAloneApplication standAloneApplication) {
		super();
		this.standAloneApplication = standAloneApplication;
	}

	protected void projectSpecificCreateShrimpView(ShrimpViewConfiguration config) {
		// Add the new frame to the window menu
		CheckBoxAction chkAction = new CheckBoxAction(project.getTitle()) {
			public void startAction() {
				// show this frame and bring it to the front
				((DesktopInternalFrame)shrimpViewContainer).select();
			}
		};
		standAloneApplication.getActionManager().addAction(chkAction, ShrimpConstants.MENU_WINDOW, ShrimpConstants.GROUP_B, 1);

		internalFrameAdapter = new ShrimpInternalFrameAdapter((StandAloneProject)project, chkAction);
		((DesktopInternalFrame)shrimpViewContainer).addInternalFrameListener(internalFrameAdapter);
		((DesktopInternalFrame)shrimpViewContainer).setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		((DesktopInternalFrame)shrimpViewContainer).getContentPane().add(shrimpView.getGUI(), BorderLayout.CENTER);

		if (config.showQuickViewPanel) {
			QuickViewPanel pnlQuickViews = new QuickViewPanel(project, null);
			((DesktopInternalFrame)shrimpViewContainer).getContentPane().add(pnlQuickViews.getQuickViewPanel(), BorderLayout.WEST);
		}

		shrimpViewContainer.setVisible(true);

	}

	/**
	 * @see ca.uvic.csr.shrimp.gui.ShrimpView.AbstractShrimpViewFactory#projectSpecificDisposeShrimpView()
	 */
	public void projectSpecificDisposeShrimpView() {
		standAloneApplication.getActionManager().removeAction(project.getTitle(), ShrimpConstants.MENU_WINDOW, ActionManager.DISABLE_PARENT);

		((DesktopInternalFrame)shrimpViewContainer).removeInternalFrameListener(internalFrameAdapter);
		standAloneApplication.getDesktop().remove(shrimpViewContainer);
		standAloneApplication.getDesktop().repaint();

		((DesktopInternalFrame)shrimpViewContainer).getContentPane().removeAll();
		internalFrameAdapter = null;
	}
}