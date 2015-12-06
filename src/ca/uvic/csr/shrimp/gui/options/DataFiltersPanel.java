/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui.options;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.FilterBean.FilterBean;

/**
 * This is a very quick implementation to filter out artifacts and relationships before they get
 * into shrimp. It is basically a copy of the FilterPalette code.
 * 
 * @author Rob Lintern
 */
public class DataFiltersPanel extends JPanel {

	/** The mode of this palette. Will be "Arcs" or "Nodes" */
	protected String mode;

	/**
	 * Used to get the currently selected data bean - retrieves the appropriate types
	 */
	protected DataBean dataBean;
    
    protected FilterBean dataFilterBean;
	
	protected Vector types = null;

	/** The vector containing all the check boxes */
	protected Vector typeBoxes = null;
	
	private Map typeToShouldBeFilteredMap;
	
	public DataFiltersPanel(String mode, Map typeShouldBeFilteredMap, DataBean dataBean, FilterBean dataFilterBean) {
		if (mode.startsWith("Node")) {
			this.mode = "Nodes";
		} else {
			this.mode = "Arcs";
		}	
		this.typeToShouldBeFilteredMap = typeShouldBeFilteredMap;
		this.dataBean = dataBean;		
        this.dataFilterBean = dataFilterBean;
		setFiltersForCurrentWindow();
	}
	
	/**
	 * Sets the filter choices for the current picture and
	 * sets up the GUI widgets for the default filter for that window.
	 * Called by the parent frame when a new picture is created
	 * or made the forward window while a FilterPalette is open. 
	 */
	public void setFiltersForCurrentWindow(){
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)));
		
		// reset the vector containing all the check boxes
		typeBoxes = new Vector();

		//the filter panel
		JPanel filtersPanel = getGUIWidgets();
		
		add(new JScrollPane(filtersPanel), BorderLayout.CENTER);
		
		// create the buttons for displaying or hiding all of the items
		JPanel buttonsPanel = new JPanel();
		JButton displayAll = new JButton("Select All");
		JButton hideAll = new JButton("Select None");

		// add the listeners to the buttons
		displayAll.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				for (int i = 0; i < typeBoxes.size(); i++) {
					JCheckBox box = (JCheckBox) typeBoxes.elementAt(i);
					if(!box.isSelected()) {
						box.setSelected(true);
					}
				}
			}
		});
		
		hideAll.addActionListener(new ActionListener() {
			public void actionPerformed (ActionEvent e) {
				for (int i = 0; i < typeBoxes.size(); i++) {
					JCheckBox box = (JCheckBox) typeBoxes.elementAt(i);
					if(box.isSelected()) {
						box.setSelected (false);
					}
				}
			}
		});
		
		buttonsPanel.add(displayAll);
		buttonsPanel.add(hideAll);
		
		add(buttonsPanel, BorderLayout.NORTH);
	}

	/**
	 * Returns the bottom part of the filter's dialog box.
	 */
	protected JPanel getGUIWidgets() {
		int numTypes;
		String type;

		if(mode.equals("Arcs")) {
			types = dataBean.getRelationshipTypes(true, true);
		} else if(mode.equals("Nodes")) {
			types = dataBean.getArtifactTypes(true, true);
		}
		numTypes = types.size();

		JPanel panel = new JPanel();
		panel.setLayout (new BoxLayout(panel, BoxLayout.Y_AXIS));		

		// add the filter types
		for (int i = 0; i < numTypes; i++) {
			type = (String)types.elementAt(i);
			SingleFilterPanel filterPanel = new SingleFilterPanel(type);
			panel.add(filterPanel);
		}
		panel.validate();

		return panel;
	}
	
    class SingleFilterPanel extends JPanel {
    	Cursor oldCursor;
		public SingleFilterPanel(String label) {
		    super();
		    
		    GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gridbag);
		    setBorder(BorderFactory.createEtchedBorder());
          
			// add the check box
		    JCheckBox chkBox = new JCheckBox(label);
		    boolean isFiltered = mode.equals("Nodes") ? dataFilterBean.isFiltered(label, FilterConstants.ARTIFACT_TYPE_STRING_FILTER_TYPE) : dataFilterBean.isFiltered(label, FilterConstants.RELATIONSHIP_TYPE_STRING_FILTER_TYPE) ;
		    chkBox.setSelected(!isFiltered);
		    
		    c.weightx = 1.0;
		    c.gridx = 0; 
		    c.gridy = 0;
		    c.anchor = GridBagConstraints.WEST;		    
		    c.insets = new Insets(2,2,2,2);		    
		    c.fill = GridBagConstraints.HORIZONTAL;
		    
			gridbag.setConstraints(chkBox, c);
		    add(chkBox);

			// add the box to the vector of boxes
			typeBoxes.addElement (chkBox);
									
			// add the listener for a change of event.
		    chkBox.addItemListener(new ModifyFilterAdapter());
		}
    }

	class ModifyFilterAdapter implements ItemListener {
		public void itemStateChanged (ItemEvent e) {
			String type = ((JCheckBox)e.getItemSelectable()).getText();
			Boolean shouldFilter = new Boolean(e.getStateChange() != ItemEvent.SELECTED);			
            typeToShouldBeFilteredMap.put(type, shouldFilter);			
		}
	}
	
}
