/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.quickview;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.DefaultProjectAction;
import ca.uvic.csr.shrimp.usercontrols.ShrimpActionListener;


/**
 * Main class for nested and flat quick views.
 * Most of the quick view configuration is done inside the {@link ExpressViewConfigurator} class.
 * This class has many static methods for creating different kinds of quick views - nested, flat, composite etc.
 *
 * @see DefaultViewAction
 * @see NestedTreemapViewAction
 * @see QueryViewAction
 * @author Chris Callendar
 * @date 1-Aug-06
 */
public class QuickViewAction extends DefaultProjectAction {

	protected ExpressViewConfigurator config;
	private String iconFilename;

	private boolean display = true;

	/**
	 * Don't use this constructor.  It is used to instantiate this class using reflection.
	 */
	public QuickViewAction() {
		this(null, null, null);
	}

	protected QuickViewAction(String actionName, ShrimpProject project) {
		this(actionName, (String)null, project);
	}

	protected QuickViewAction(String actionName, String iconFilename, ShrimpProject project) {
		super(actionName, project);
		setToolTip(actionName);
		setIconFilename(iconFilename);	// also updates the icon inside a try/catch
		this.config = createExpressViewConfigurator(project);
	}

	public void dispose() {
		if (config != null) {
			config.dispose();
			config = null;
		}
		super.dispose();
	}

	protected boolean isValid() {
		return true;
	}

	protected ExpressViewConfigurator createExpressViewConfigurator(ShrimpProject project) {
		return new ExpressViewConfigurator(project);
	}

	public void setProject(ShrimpProject project) {
		config.setProject(project);
		super.setProject(project);
	}

	public void setActionName(String name) {
		super.setActionName(name);
		updateToolTip();
	}

	public void setDisplay(boolean display) {
		this.display = display;
		updateToolTip();
	}

	public boolean isDisplay() {
		return display;
	}

	protected void updateToolTip() {
		String tt = getActionName();
		if (!isDisplay()) {
			tt += " (not displayed)";
		}
		setToolTip(tt);
	}

	/**
	 * Sets up the {@link ExpressViewConfigurator} and calls {@link ExpressViewConfigurator#configureView(String)}.
	 * Nothing will happen if the data bean or project are null.
	 * Before and after the action starts all {@link ShrimpActionListener}s are notified.
	 * @see QuickViewAction#addActionListener(ShrimpActionListener)
	 */
	public void startAction() {
	    fireActionEvent(true);
	    if (!hasProject()) {
	    	System.err.println("No project - can't start quick view " + getActionName());
	        return;
	    }
	    config.configureView(getActionName());
	    fireActionEvent(false);
	}

	public ExpressViewConfigurator getConfigurator() {
		return config;
	}

	public String getIconFilename() {
		return iconFilename;
	}

	/**
	 * Sets the icon filename.  If the filename is not null, then the {@link ResourceHandler}
	 * is used to update the icon.
	 * @param iconFilename
	 */
	public void setIconFilename(String iconFilename) {
		this.iconFilename = iconFilename;
		if (iconFilename != null) {
			try {
				setIcon(ResourceHandler.getIcon(iconFilename));
			} catch (Exception ex) {}
		}
	}

	/**
	 * Checks if any cprels are defined, if so true is returned.
	 * @return true if the quick view is nested.
	 */
	public boolean isNested() {
		return (config.getCprels().length > 0);
	}

	/**
	 * @return true if the view uses composite arcs
	 */
	public boolean isComposite() {
		return (config.getCompositeArcs().size() > 0);
	}


}
