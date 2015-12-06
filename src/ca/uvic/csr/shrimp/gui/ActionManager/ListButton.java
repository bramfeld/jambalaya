/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ActionManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * @author Nasir Rather
 * 
 * Defines ListButton class.
 */
class ListButton extends JComponent {
	private JButton button;
	private JPopupMenu popupMenu;
	private JPanel badgePanel;
	
	private final int BADGE_WIDTH = 12;
	
	private String toolTipPrefix = "";
	private Icon icon;
	
	private ButtonGroup buttonGroup;
	
	// if multiple checks are not allowed, menuItemList should only have one item in it
	private ArrayList menuItemList;
	
	private Action action;
	
	private boolean allowMultipleChecked = false;

	/**
	 * Creates a ListButton and the Icon <code>icon</code> will be fixed as the icon for this button.
	 * The popupMenu will contains checkBoxMenuItems to let the user know which menuItem is selected.
	 * 
	 * @param icon Icon for this button.
	 */
	public ListButton(Icon icon) {
		super();		
		this.icon = icon;		
		init();
	}
	
	/**
	 * Creates a ListButton and the Icon <code>icon</code> will be fixed as the icon for this button.
	 * The popupMenu will contains checkBoxMenuItems to let the user know which menuItem is selected.
	 * 
	 * @param icon Icon for this button.
	 */
	public ListButton(Icon icon, boolean allowMultipleChecked) {
		super();
		
		this.icon = icon;
		this.allowMultipleChecked = allowMultipleChecked;
		
		init();
	}
	
	// Internal initializations
	private void init() {
		menuItemList = new ArrayList();
		
		popupMenu = new JPopupMenu();
		
		action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				for (Iterator iter = menuItemList.iterator(); iter.hasNext();) {
					JMenuItem menuItem = (JMenuItem) iter.next();
					
					if(menuItem.isSelected()) {
						menuItem.getAction().actionPerformed(e);
					}
				}
			}
		};
		
		button = new JButton(action) {
			public void paint(Graphics g) {
				super.paint(g);
				
				// we want badgePanel always on top
				badgePanel.repaint();
			}
		};
		button.setIcon(icon);
		button.setText(null);
		button.setMargin(new Insets(1, 0-BADGE_WIDTH/2, 1, 5));
		button.setFocusPainted(false);
		
		// this is the little arrow to the right, its just a JPanel that paints a little arrow
		badgePanel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);
				
				int triangleWidth = 7;
				int triangleHeight = 4;

				int left = getWidth() / 2 - triangleWidth / 2 -1;
				int top = getHeight() / 2 - triangleHeight / 2;

				Polygon triangle = new Polygon();
				triangle.addPoint(left, top);
				triangle.addPoint(left + triangleWidth, top);
				triangle.addPoint(left + triangleWidth / 2, top + triangleHeight);

				g.setColor(Color.black);
				g.fillPolygon(triangle);
			}
		};
		
		// we want to popup the menu whenever badgePanel is clicked on
		badgePanel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				popupMenu.show(button, 0, button.getHeight());
			}
		});		
		
		setLayout(null);
		add(badgePanel);
		add(button);		
		
		adjustSize();
	}
	
	/**
	 * @see javax.swing.JComponent#setToolTipText(String)
	 */
	public void setToolTipText(String arg0) {
		toolTipPrefix = arg0;
	}
	
	/**
	 * Adds a new action to the list of actions this buttons has.
	 * The first action is setSelected by default.
	 * 
	 * @param action Action to be added. 
	 * @param isInitiallySelected Whether or not this acton should be selected initially.
	 */
	public void add (Action action, boolean isInitiallySelected) {
		if (action == null)
			return;
		
		final JMenuItem menuItem = new JCheckBoxMenuItem(action);		
		
		action.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				// If the action is a checkbox action, synchronize the action and the menuItem
				if(evt.getSource() instanceof CheckBoxAction && evt.getPropertyName().equals(CheckBoxAction.CHECKED)){
					menuItem.setSelected(((CheckBoxAction)evt.getSource()).isChecked());
				}
				
				if(!allowMultipleChecked && menuItem.isSelected() && evt.getPropertyName().equals("enabled")) {
					button.getAction().setEnabled(menuItem.getAction().isEnabled());
				}
			}
		});
		
		popupMenu.add(menuItem);
		
		if(!allowMultipleChecked) {
			if(buttonGroup == null) {
 				buttonGroup = new ButtonGroup();
			}
			
			buttonGroup.add(menuItem);
			
			if(isInitiallySelected) {
				menuItem.setSelected(true);
				button.getAction().setEnabled(menuItem.getAction().isEnabled());
			}
		}
		
		if (isInitiallySelected)
			setButtonAttributes(menuItem);
		
		menuItemList.add(menuItem);
		
		// if a menuItem was clicked
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setButtonAttributes((JMenuItem) e.getSource());
			}
		});
	}
	
	// this ones sets the appropriate properties of the button whenever a new item is
	// selected from the menuItem
	private void setButtonAttributes(JMenuItem menuItem) {
		button.setToolTipText(toolTipPrefix + " - " + menuItem.getAction().getValue(Action.NAME));
		button.setIcon(menuItem.getIcon());
	}
	
	// This is needed whenever an icon is added, in short whenever button size changes
	private void adjustSize() {
		badgePanel.setSize(new Dimension(BADGE_WIDTH, button.getPreferredSize().height-6));
		badgePanel.setLocation(button.getPreferredSize().width+2, (button.getPreferredSize().height- (button.getPreferredSize().height-6))/2);
		
		button.setSize(new Dimension(button.getPreferredSize().width+BADGE_WIDTH+5, button.getPreferredSize().height));
		
		setMinimumSize(new Dimension(button.getPreferredSize().width+BADGE_WIDTH+5, button.getPreferredSize().height));
		setPreferredSize(new Dimension(button.getPreferredSize().width+BADGE_WIDTH+5, button.getPreferredSize().height));
		setMaximumSize(new Dimension(button.getPreferredSize().width+BADGE_WIDTH+5, button.getPreferredSize().height));
	}
	
	/** Return the number of actions added to this list button **/
	public int getActionCount() {
		return popupMenu.getComponentCount();
	} 
}
