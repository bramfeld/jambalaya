/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.QueryView;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.usercontrols.DefaultUserAction;

/**
 * Base action class for {@link QueryView} actions.
 *
 * @author Chris
 * @date   18-Oct-07
 */
abstract class QueryViewMenuAction extends DefaultUserAction {

	protected boolean allowMultipleSelections;
	protected QueryView queryView;

	public QueryViewMenuAction(String name, Icon icon, QueryView queryView) {
		super(name, icon);
		this.queryView = queryView;
		this.allowMultipleSelections = true;
	}

	protected ShrimpProject getProject() {
		return queryView.getProject();
	}

	public void startAction() {
		if (getProject() != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) queryView.getBean(ShrimpTool.SELECTOR_BEAN);
				if (allowMultipleSelections) {
					Vector selectedNodes = (Vector) selectorBean.getSelected(SelectorBeanConstants.SELECTED_NODES);
					for (Iterator iter = selectedNodes.iterator(); iter.hasNext(); ) {
						ShrimpNode node = (ShrimpNode) iter.next();
						DisplayBean displayBean = (DisplayBean) queryView.getBean(ShrimpTool.DISPLAY_BEAN);
						doAction(displayBean, node);
					}
				} else {
					Object targetObj = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
					if (targetObj instanceof ShrimpNode) {
						ShrimpNode node = (ShrimpNode) targetObj;
						DisplayBean displayBean = (DisplayBean) queryView.getBean(ShrimpTool.DISPLAY_BEAN);
						doAction(displayBean, node);
						// keep the target node selected
						selectorBean.setSelected(SelectorBeanConstants.TARGET_OBJECT, node);
					}
				}
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	protected DataBean getDataBean() {
		try {
			return (DataBean) getProject().getBean(ShrimpProject.DATA_BEAN);
		} catch (BeanNotFoundException e) {
			return null;
		}
	}

	/**
	 * Expands or collapses of the immediate neighbors around the given node.
	 * @param displayBean
	 * @param node
	 */
	protected abstract void doAction(DisplayBean displayBean, ShrimpNode node);

}
