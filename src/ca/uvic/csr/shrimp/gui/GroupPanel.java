/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.ShrimpConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.DataDisplayBridge.RelTypeGroup;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.PopupListener;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Displays the group name, plus/minus, and possibly the composite arcs graphic.
 *
 * @author Rob Lintern
 */
class GroupPanel extends TransparentPanel implements DropTargetListener {

	protected final static Icon ICON_CHECKED = NodeFilterPalette.ICON_CHECKED;
	protected final static Icon ICON_UNCHECKED = NodeFilterPalette.ICON_UNCHECKED;

	private final ArcFilterPalette advancedPalette;
	private RelTypeGroup relTypeGroup;

	private JLabel lblTypeCount;
	private JLabel lblPlusMinus;
	private JLabel lblName;
	private JPanel pnlHeader;
	private JPanel pnlBody;
	private JLabel lblDisplayFilter;
	private JPanel pnlStyleThumb;
	private JComponent styleThumbnail;
	private JPopupMenu popup;

	// this contains types, which are arranged alphabetically by the treeset
	private TreeSet panelTypes = new TreeSet();

	public GroupPanel(ArcFilterPalette palette, RelTypeGroup relTypeGroup) {
		super();
		this.advancedPalette = palette;
		this.relTypeGroup = relTypeGroup;

		ArcStyle groupStyle = relTypeGroup.getCompositeStyle();
		Color groupColor = relTypeGroup.getCompositeColor();

		new DropTarget(this, this);
		setLayout(new GridBagLayout());

		lblPlusMinus = new JLabel(ArcFilterPalette.ICON_PLUS);
		lblPlusMinus.addMouseListener(new MouseAdapter () {
			public void mouseClicked(MouseEvent e) {
				collapseExpandGroup();
			}
		});

		lblTypeCount = new JLabel();
		setLabelTypeCountText();
		lblName = new JLabel(relTypeGroup.getGroupName());
		pnlHeader = new JPanel();
		pnlBody = new TransparentPanel();

		lblDisplayFilter = new JLabel(); //AdvancedArcFilterPalette.ICON_DISPLAY_FILTER_ENABLED);
		lblDisplayFilter.setBorder(BorderFactory.createEmptyBorder());
		final Icon icon = ArcFilterPalette.ICON_DISPLAY_FILTER_FILTERED;
		lblDisplayFilter.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));

		//add stylePanel
		pnlStyleThumb = new TransparentPanel();
		styleThumbnail = groupStyle.getThumbnail(groupColor);
		styleThumbnail.setOpaque(false);
		pnlStyleThumb.add(styleThumbnail);
		pnlStyleThumb.setVisible(relTypeGroup.areCompositesEnabled());

		pnlHeader.setBorder(BorderFactory.createLineBorder(Color.gray));
		pnlHeader.setBackground(ArcFilterPalette.TEXT_BACKGROUND_COLOR);
		pnlHeader.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
		pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.X_AXIS));

		pnlHeader.add(lblPlusMinus);
		pnlHeader.add(lblDisplayFilter);
		pnlHeader.add(pnlStyleThumb);
		pnlHeader.add(lblName);
		pnlHeader.add(lblTypeCount);
		pnlHeader.add(Box.createHorizontalGlue());	// align right
		pnlHeader.add(createFilterToolBar());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.weightx = 1.0;
		add(pnlHeader, gridBagConstraints);

		gridBagConstraints.insets = new Insets(0, 10, 0, 10);
		pnlBody.setLayout(new GridBagLayout());
		add(pnlBody, gridBagConstraints);

		pnlHeader.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				for (Iterator iterator = advancedPalette.getSelectedTypePanels().iterator(); iterator.hasNext();) {
					ArcTypePanel p = (ArcTypePanel) iterator.next();
					p.highlight(false);
				}
				advancedPalette.getSelectedTypePanels().removeAllElements();

				if (advancedPalette.getSelectedGroupPanel() == null) {
					advancedPalette.setSelectedGroupPanel(GroupPanel.this);
					advancedPalette.getSelectedGroupPanel().highlight(true);
				} else {
					advancedPalette.getSelectedGroupPanel().highlight(false);
					advancedPalette.setSelectedGroupPanel(GroupPanel.this);
					advancedPalette.getSelectedGroupPanel().highlight(true);
				}
				advancedPalette.enableDisableButtons();

				if (e.getClickCount() == 2) {
					collapseExpandGroup();
					showGroupProperties();
				}
			}
		});

		popup = new JPopupMenu();
		popup.add(new JMenuItem(new AbstractAction(ShrimpConstants.ACTION_NAME_PROPERTIES) {
			public void actionPerformed(ActionEvent e) {
				showGroupProperties();
			}
		}));
		pnlHeader.addMouseListener(new PopupListener(popup));

		boolean showTypeCount = false; //advancedPalette.displayBean.getCprels().length == 0;
		Vector relTypes = relTypeGroup.getRelTypes();
		String[] cprels = advancedPalette.displayBean.getCprels();
		// add all the cprels to a hashset for quicker lookup
		HashSet cprelsSet = new HashSet((int)(cprels.length / 0.75));
		for (int i = 0; i < cprels.length; i++) {
            String cprel = cprels[i];
            cprelsSet.add(cprel);
        }
		for (Iterator iterator = relTypes.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			if (!advancedPalette.dataBean.isArtTypeFiltered(type)) {
				int typeCount = - 1;
				if (showTypeCount) {
					typeCount = advancedPalette.dataBean.getRelationshipsOfType(type, true).size();
				}
				Color color = (Color) advancedPalette.attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, type);
				ArcStyle arcStyle = (ArcStyle) advancedPalette.attrToVisVarBean.getVisVarValue(AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_STYLE, type);
				// disable any types that are in the cprels hashset
				boolean enabled = !cprelsSet.contains(type);
				ArcTypePanel typePanel = new ArcTypePanel(advancedPalette, type, color, arcStyle, typeCount, enabled);
				advancedPalette.typePanels.put(type, typePanel);
				add(typePanel);
			}
		}
		cprelsSet.clear();

		if (advancedPalette.filterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, relTypeGroup.getGroupName())) {
			setVisibilityStatus(false);
		}

		pnlBody.setVisible(false);
	}

	private JToolBar createFilterToolBar() {
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setOpaque(false);
		JButton btn = toolbar.add(new DefaultShrimpAction("", ICON_CHECKED, "Show all the arc types in this group") {
			public void actionPerformed(ActionEvent e) {
				advancedPalette.filterSelectedGroupTypesInDisplay(GroupPanel.this, false);
			}
		});
		btn.setOpaque(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btn = toolbar.add(new DefaultShrimpAction("", ICON_UNCHECKED, "Hide all the arc types in this group") {
			public void actionPerformed(ActionEvent e) {
				advancedPalette.filterSelectedGroupTypesInDisplay(GroupPanel.this, true);
			}
		});
		btn.setOpaque(false);
		btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return toolbar;
	}

	private void setLabelTypeCountText() {
		Vector relTypes = relTypeGroup.getRelTypes();
		int relTypeCount = 0;
		for (Iterator iterator = relTypes.iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			if (!advancedPalette.dataBean.isRelTypeFiltered(type)) {
				relTypeCount++;
			}
		}
		String s = " (" + relTypeCount + (relTypeCount == 1 ? " type" : " types");
		boolean showTypeCount = false;
		if (showTypeCount) {
			int groupArcCount = 0;
			for (Iterator iterator = relTypes.iterator(); iterator.hasNext();) {
				String type = (String) iterator.next();
				int typeCount = - 1;
				if (showTypeCount) {
					typeCount = advancedPalette.dataBean.getRelationshipsOfType(type, true).size();
					groupArcCount += typeCount;
				}
			}
			s+= ", " + groupArcCount + " arcs";
		}
		s+= ")";
		lblTypeCount.setText(s);
	}

	void showGroupProperties() {
		Window parent = SwingUtilities.windowForComponent(this);
		EscapeDialog dialog = EscapeDialog.createDialog(parent, "Arc Group Properties", true);
		GroupOptionsPanel groupOptionsPanel = new GroupOptionsPanel(this, advancedPalette.attrToVisVarBean,
				advancedPalette.dataDisplayBridge, advancedPalette.dataBean);
		groupOptionsPanel.refresh();
		dialog.getContentPane().add(groupOptionsPanel, BorderLayout.CENTER);
		dialog.pack();
		dialog.setDefaultButton(groupOptionsPanel.getCloseButton());
		groupOptionsPanel.getCloseButton().requestFocus();

		int y = parent.getY();
		int x = (int) parent.getBounds().getMaxX();
		// make sure not off screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (x + dialog.getWidth() > screenSize.getWidth()) {
			x = (int)(screenSize.getWidth() - dialog.getWidth());
		}
		if (y + dialog.getHeight() > screenSize.getHeight()) {
			y = (int)(screenSize.getHeight() - dialog.getHeight());
		}
		dialog.setLocation(Math.max(0, x), Math.max(0, y));
		dialog.setVisible(true);
	}

	protected void collapseExpandGroup() {
		pnlBody.setVisible(!pnlBody.isVisible());
		if (pnlBody.isVisible()) {
			lblPlusMinus.setIcon(ArcFilterPalette.ICON_MINUS);
		} else {
			lblPlusMinus.setIcon(ArcFilterPalette.ICON_PLUS);
		}
	}

	public void setVisibilityStatus(boolean visible) {
		lblDisplayFilter.setIcon(visible ? ArcFilterPalette.ICON_DISPLAY_FILTER_UNFILTERED : ArcFilterPalette.ICON_DISPLAY_FILTER_FILTERED);
	}

	public Collection getTypePanels() {
		Collection typePanels = new HashSet();
		for (Iterator iter = panelTypes.iterator(); iter.hasNext();) {
			String panelType = (String) iter.next();
			ArcTypePanel typePanel = (ArcTypePanel) advancedPalette.typePanels.get(panelType);
			if (typePanel != null) {
				typePanels.add(typePanel);
			}
		}
		return typePanels;
	}

	public Component add(ArcTypePanel typePanel) {
		panelTypes.add(typePanel.getType());

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = GridBagConstraints.RELATIVE;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.NORTHEAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(0, 2, 0, 2);
		gridBagConstraints.ipady = 5;

		pnlBody.removeAll();
		for (Iterator iterator = panelTypes.iterator(); iterator.hasNext();) {
			String panelType = (String) iterator.next();
			pnlBody.add((Component) advancedPalette.typePanels.get(panelType), gridBagConstraints);
		}

		setLabelTypeCountText ();
		return typePanel;
	}

	public void remove(ArcTypePanel comp) {
		panelTypes.remove(comp.getType());
		pnlBody.remove(comp);
		setLabelTypeCountText ();
	}

	public void highlight(boolean highlight) {
		if (highlight) {
			pnlHeader.setBackground(ArcFilterPalette.SELECTION_BACKGROUND_COLOR);
			pnlHeader.setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
			lblName.setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
			lblTypeCount.setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
		} else {
			pnlHeader.setBackground(ArcFilterPalette.TEXT_BACKGROUND_COLOR);
			pnlHeader.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
			lblName.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
			lblTypeCount.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
		}
	}

	public void setCompositeArcsEnabled(boolean enabled) {
	    advancedPalette.dataDisplayBridge.getCompositeArcsManager().setCompositesEnabled(relTypeGroup, enabled);
		lblDisplayFilter.setVisible(enabled);
		pnlStyleThumb.setVisible(enabled);
		pnlStyleThumb.revalidate();
	}

	public void setCompositeArcsColor(Color color) {
		pnlStyleThumb.remove(styleThumbnail);
		styleThumbnail = relTypeGroup.getCompositeStyle().getThumbnail(color);
		styleThumbnail.setOpaque(false);
		pnlStyleThumb.add(styleThumbnail);
		pnlStyleThumb.revalidate();
	}

	public void setCompositeArcsStyle(ArcStyle style) {
		Color color = relTypeGroup.getCompositeColor();
		pnlStyleThumb.remove(styleThumbnail);
		styleThumbnail = style.getThumbnail(color);
		styleThumbnail.setOpaque(false);
		pnlStyleThumb.add(styleThumbnail);
		pnlStyleThumb.revalidate();
	}

	//TODO make a button to enable renaming of group
	public void setGroupName(String name) {
		relTypeGroup.setGroupName(name);
		lblName.setText(name);
	}

	public void setFilter(boolean addFilter) {
		advancedPalette.filterSingleTypeInDisplay(relTypeGroup.getGroupName(), addFilter);
		setVisibilityStatus(!addFilter);
	}

	public void dragEnter(DropTargetDragEvent e) {
		pnlHeader.setBackground(ArcFilterPalette.SELECTION_BACKGROUND_COLOR);
		pnlHeader.setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
	}

	public void dragOver(DropTargetDragEvent e) {
		Vector relTypes = relTypeGroup.getRelTypes();
		for (Iterator iterator = advancedPalette.getSelectedTypePanels().iterator(); iterator.hasNext();) {
			ArcTypePanel typePanel = (ArcTypePanel) iterator.next();

			if (relTypes.contains(typePanel.getType())) {
				e.rejectDrag();
				return;
			}
		}

		e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent e) {
		pnlHeader.setBackground(ArcFilterPalette.TEXT_BACKGROUND_COLOR);
		pnlHeader.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
	}

	public void drop(DropTargetDropEvent e) {
		for (Iterator iterator = advancedPalette.getSelectedTypePanels().iterator(); iterator.hasNext();) {
			ArcTypePanel selectedPanel = (ArcTypePanel) iterator.next();
			GroupPanel oldGroupPanel = (GroupPanel) selectedPanel.getParent().getParent();

			RelTypeGroup oldGroup = oldGroupPanel.relTypeGroup;
			RelTypeGroup newGroup = this.relTypeGroup;
			advancedPalette.dataDisplayBridge.getCompositeArcsManager().removeRelTypeFromGroup(selectedPanel.getType(), oldGroup);
			advancedPalette.dataDisplayBridge.getCompositeArcsManager().addRelTypeToGroup(selectedPanel.getType(), newGroup);

			oldGroupPanel.remove(selectedPanel);
			this.add(selectedPanel);
		}

		this.getParent().validate();

		pnlHeader.setBackground(ArcFilterPalette.TEXT_BACKGROUND_COLOR);
		pnlHeader.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
	}
    public RelTypeGroup getRelTypeGroup() {
        return relTypeGroup;
    }

    public String toString() {
    	return "GroupPanel: " + relTypeGroup.getGroupName() + " [" + panelTypes.size() + " types]";
    }

}