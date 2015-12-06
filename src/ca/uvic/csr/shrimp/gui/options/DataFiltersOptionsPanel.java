/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;

/**
 * A panel that allows the user to choose which artifact and relationship types
 * should be brought into SHriMP.
 * 
 * @author Rob Lintern
 */
public class DataFiltersOptionsPanel extends JPanel implements ShrimpOptions {	
	
	/** maps a relationship type to whether or not it should be imported into shrimp */
	private Map relTypeToShouldBeFilteredMap;
	
	/** maps an artifact type to whether or not it should be imported into shrimp */
	private Map artTypeToShouldBeFilteredMap;
	
	private ShrimpProject shrimpProject;
    private FilterBean dataFilterBean;
	
	public DataFiltersOptionsPanel (ShrimpProject shrimpProject, DataBean dataBean, FilterBean dataFilterBean) {		
		this.shrimpProject = shrimpProject;
        this.dataFilterBean = dataFilterBean;
		
		artTypeToShouldBeFilteredMap = new HashMap();
		relTypeToShouldBeFilteredMap = new HashMap();
		JLabel label = new JLabel("To increase performance, choose node and arc types to filter out of the data.");
		add(label, BorderLayout.NORTH);
		
		JPanel bothTypesPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		JPanel artTypesPanel = new DataFiltersPanel("Node", artTypeToShouldBeFilteredMap, dataBean, dataFilterBean);
		JPanel relTypesPanel = new DataFiltersPanel("Arc", relTypeToShouldBeFilteredMap, dataBean, dataFilterBean);
		bothTypesPanel.setPreferredSize(new Dimension(450, 400));
		bothTypesPanel.add(artTypesPanel);
		bothTypesPanel.add(relTypesPanel);
		add(bothTypesPanel, BorderLayout.CENTER);
	}

		
	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#ok()
	 */
	public void ok() {		
		if (!artTypeToShouldBeFilteredMap.isEmpty() || !relTypeToShouldBeFilteredMap.isEmpty()) {
			//reset databean and displaybean
            boolean firingEvents = dataFilterBean.isFiringEvents();
            dataFilterBean.setFiringEvents(false);
            for (Iterator iter = artTypeToShouldBeFilteredMap.keySet().iterator(); iter.hasNext();) {
                String type = (String) iter.next();
                boolean shouldFilter = ((Boolean)artTypeToShouldBeFilteredMap.get(type)).booleanValue(); 
                dataFilterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, String.class, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE, type, shouldFilter);
            }
            for (Iterator iter = relTypeToShouldBeFilteredMap.keySet().iterator(); iter.hasNext();) {
                String type = (String) iter.next();
                boolean shouldFilter = ((Boolean)relTypeToShouldBeFilteredMap.get(type)).booleanValue(); 
                dataFilterBean.addRemoveSingleNominalAttrValue(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE, type, shouldFilter);
            }
            dataFilterBean.setFiringEvents(firingEvents);
			shrimpProject.refresh();
			
			try {
				ShrimpView shrimpView = (ShrimpView) shrimpProject.getTool(ShrimpProject.SHRIMP_VIEW);
				shrimpView.addDefaultRootNodes(true);
			} catch (ShrimpToolNotFoundException e1) {
				e1.printStackTrace();
			} 
		}		
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.gui.options.ShrimpOptions#cancel()
	 */
	public void cancel() {
	}
	
}
