/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.awt.Rectangle;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import ca.uvic.csr.shrimp.gui.options.DataFiltersOptionsPanel;
import ca.uvic.csr.shrimp.gui.options.GeneralOptionsPanel;
import ca.uvic.csr.shrimp.gui.options.LabelOptionsPanel;
import ca.uvic.csr.shrimp.gui.options.LayoutOptionsPanel;
import ca.uvic.csr.shrimp.gui.options.RenderingOptionsPanel;
import ca.uvic.csr.shrimp.gui.options.ShrimpOptionsDialog;
import ca.uvic.csr.shrimp.gui.options.UserControlOptions;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Initializes the Shrimp Options Dialog.  This includes initializing all the tabs: general options,
 * labels, layouts, user controls, data filters, and rendering.
 *
 * @author Jeff Michaud
 */
public class OpenShrimpOptionsAdapter extends DefaultProjectAction {

	private ShrimpOptionsDialog shrimpOptionsDialog;
	private int selectedTabIndex = 0;

	public OpenShrimpOptionsAdapter(String actionName, ShrimpProject project) {
		super(actionName, ResourceHandler.getIcon("icon_options.gif"), project);
	}

	public void startAction() {
		try {
			ShrimpProject project = getProject();
			ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
			DisplayBean displayBean = (DisplayBean) shrimpView.getBean(ShrimpTool.DISPLAY_BEAN);
			DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
            FilterBean dataFilterBean = (FilterBean) project.getBean(ShrimpProject.DATA_FILTER_BEAN);
			SelectorBean selectorBean = (SelectorBean) shrimpView.getBean(ShrimpTool.SELECTOR_BEAN);

			ShrimpApplication app = ApplicationAccessor.getApplication();

			shrimpOptionsDialog = new ShrimpOptionsDialog(app.getParentFrame(), true);
			shrimpOptionsDialog.addTab("General", new GeneralOptionsPanel(project, displayBean, shrimpView));
			shrimpOptionsDialog.addTab("Labels", new LabelOptionsPanel(displayBean, selectorBean));
			shrimpOptionsDialog.addTab("Layouts", new LayoutOptionsPanel(displayBean));
			shrimpOptionsDialog.addTab("User Controls", new UserControlOptions(app));
			shrimpOptionsDialog.addTab("Data Filters", new DataFiltersOptionsPanel(project, dataBean, dataFilterBean));
			shrimpOptionsDialog.addTab("Rendering", new RenderingOptionsPanel(ShrimpProject.SHRIMP_VIEW, project));

			// load the selected tab
			shrimpOptionsDialog.setSelectedTabIndex(selectedTabIndex);

			String str = app.getProperties().getProperty(ShrimpApplication.OPTIONS_DIALOG_BOUNDS);
			Rectangle bounds = ShrimpUtils.stringToBounds(str);
			if (bounds != null) {
				if (bounds.y < 0) {
					bounds.y = 0;
				}
				shrimpOptionsDialog.setBounds(bounds);
			}

			// show the dialog (blocks here)
			shrimpOptionsDialog.setVisible(true);

			// save the options dialog bounds
			str = ShrimpUtils.boundsToString(shrimpOptionsDialog.getBounds());
			app.getProperties().setProperty(ShrimpApplication.OPTIONS_DIALOG_BOUNDS, str);

			// save the selected tab
			selectedTabIndex = shrimpOptionsDialog.getSelectedTabIndex();
		} catch(ShrimpToolNotFoundException stnfe) {
			stnfe.printStackTrace();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
	}

}
