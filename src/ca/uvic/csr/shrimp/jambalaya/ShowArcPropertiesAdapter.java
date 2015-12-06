/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import ca.uvic.csr.shrimp.SelectorBeanConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpCompositeArc;
import ca.uvic.csr.shrimp.SelectorBean.SelectorBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.usercontrols.DefaultToolAction;
import ca.uvic.csr.shrimp.usercontrols.UserAction;
import ca.uvic.csr.shrimp.usercontrols.UserEvent;

/**
 * This {@link UserAction} adapter displays the properties for the selected arc.
 *
 * @author Chris Callendar
 * @date April 10, 2007
 */
public class ShowArcPropertiesAdapter extends DefaultToolAction {

	public static String ACTION_NAME = ShrimpConstants.ACTION_NAME_ARC_PROPERTIES;
	public static final String TOOLTIP = "Displays the properties of the selected arc.\n" +
		"In Jambalaya the Protege Property Editor is displayed.";

	private String toolName = null;

    public ShowArcPropertiesAdapter(ShrimpProject project, ShrimpTool tool) {
    	super(ACTION_NAME, project, tool);
    	setToolTip(TOOLTIP);
		mustStartAndStop = false;

		// add the event - double click left mouse button
		addDefaultUserEvent(true, UserEvent.DOUBLE_CLICK__LEFT_MOUSE_BUTTON, false, false, false);
    }

    public ShowArcPropertiesAdapter(ShrimpProject project, String toolName) {
    	this(project, (ShrimpTool) null);
    	this.toolName = toolName;
    }

    public ShrimpTool getTool() {
    	if ((tool == null) && (toolName != null) ) {
    		try {
				tool = getProject().getTool(toolName);
			} catch (ShrimpToolNotFoundException e) {
			}
    	}
    	return tool;
    }

    public void startAction() {
    	if (getTool() != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) getTool().getBean(ShrimpTool.SELECTOR_BEAN);
				Object targetObject = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
				if ((targetObject instanceof ShrimpArc) && !(targetObject instanceof ShrimpCompositeArc)) {
					Relationship rel = ((ShrimpArc) targetObject).getRelationship();
					// rel will be null for composite arcs
					if (rel != null) {
						String relType = rel.getType();
						if (rel.getDataBean() instanceof ProtegeDataBean) {
							displayProtegePropertyEditor((ProtegeDataBean) rel.getDataBean(), relType);
						}
					}
				}
			} catch (BeanNotFoundException bnfe) {
			  	bnfe.printStackTrace();
			}
    	} else {
    		setEnabled(false);
    	}
    }

    public boolean canStart() {
    	boolean canStart = false;
    	if (getTool() != null) {
			try {
				SelectorBean selectorBean = (SelectorBean) getTool().getBean(ShrimpTool.SELECTOR_BEAN);
				Object targetObject = selectorBean.getSelected(SelectorBeanConstants.TARGET_OBJECT);
				if ((targetObject instanceof ShrimpArc) && !(targetObject instanceof ShrimpCompositeArc)) {
					canStart = true;
				}
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return canStart && super.canStart();
    }

	/**
	 * Opens the default Protege property editor window for the double clicked relationship.
	 * @param dataBean
	 * @param relType
	 */
	private void displayProtegePropertyEditor(ProtegeDataBean dataBean, String relType) {
	/*
		Slot slot = dataBean.relationshipTypeToSlot(relType);
		if (slot != null) {
			final JFrame frame = dataBean.getKnowledgeBase().getProject().show(slot);

			// need to make the display bean enabled again
			// it loses focus when the dialog pops up
			frame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					try {
						DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
						displayBean.setEnabled(true);
						displayBean.requestFocus();
						displayBean.repaint();
					} catch (BeanNotFoundException e1) {
					}
					frame.removeWindowListener(this);
				}
			});
		} else {
			System.err.println("Couldn't find slot for relationship type '" + relType + "'");
		}
	*/
	}

}
