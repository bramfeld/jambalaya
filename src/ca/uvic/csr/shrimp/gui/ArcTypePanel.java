/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.FilterConstants;
import ca.uvic.csr.shrimp.VisVarConstants;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
* @author Neil Ernst, Rob Lintern, Nasir Rather, Chris Callendar
*/
public class ArcTypePanel extends JPanel implements DragSourceListener, DragGestureListener {

	private ArcFilterPalette advancedPalette;
	private String type;
	private ArcStyle style;
	private Color color;

	private JButton btnStyle;
	private JLabel lblType;
	private JCheckBox chkDisplayFilter;
	private MouseListener chkDisplayFilterListener;

	private boolean filteredInDisplay;

	public ArcTypePanel(ArcFilterPalette palette, final String arcType, Color arcColor, ArcStyle arcStyle, int numArcs, boolean enabled) {
		super();
		this.advancedPalette = palette;
		this.type = arcType;
		this.style = arcStyle;
		this.color = arcColor;
		this.setEnabled(enabled);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		setBorder(BorderFactory.createMatteBorder(0,1,1,1, Color.gray));

		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 1) {
					typePanelClicked(e);
				}
			}
		});

		String typeStr = type;
		if (numArcs > 0) {
			typeStr += " (" + numArcs + ")";
		}
		lblType = new JLabel(typeStr);
		lblType.setEnabled(enabled);

		btnStyle = new JButton();
		btnStyle.setMargin(new Insets(6, 6, 0, 6));
		btnStyle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStyle.setEnabled(enabled);
		final JComponent styleComponent = style.getThumbnail(enabled ? color : Color.gray);
		styleComponent.setOpaque(false);
		btnStyle.add(styleComponent);
		Dimension dim = styleComponent.getPreferredSize();
		dim.setSize(dim.width + 20, dim.height + 16);
		btnStyle.setPreferredSize(dim);
		btnStyle.addMouseListener(new MouseAdapter () {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (!advancedPalette.getSelectedTypePanels().contains(ArcTypePanel.this) && e.getClickCount() == 1){
						typePanelClicked(e);
					}
					stylePanelClicked();
				}
			}
		});
		c.weightx = 0;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 1.0;
		c.insets = new Insets(1, 5, 1, 1);
		add(btnStyle, c);

		chkDisplayFilter = new JCheckBox();
		chkDisplayFilter.setEnabled(enabled);
		chkDisplayFilter.setOpaque(false);
		chkDisplayFilter.setBackground(Color.WHITE);
		chkDisplayFilterListener = new MouseAdapter () {
    		public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (!advancedPalette.getSelectedTypePanels().contains(ArcTypePanel.this) && e.getClickCount() == 1){
						typePanelClicked(e);
					}
					advancedPalette.filterSelectedTypesInDisplay(!filteredInDisplay);
				}
			}
         };
        chkDisplayFilter.addMouseListener(chkDisplayFilterListener);
		add(chkDisplayFilter, c);

		c.weightx = 1.0;
		add(lblType, c);

		filteredInDisplay = this.advancedPalette.filterBean.isNominalAttrValueFiltered(AttributeConstants.NOM_ATTR_REL_TYPE, String.class, FilterConstants.RELATIONSHIP_FILTER_TYPE, type);
		setDisplayFiltered(filteredInDisplay);
		highlight(false);

		setToolTipText(type);
	}

	public boolean equals(Object obj) {
		return (obj instanceof ArcTypePanel && ((ArcTypePanel) obj).getType().equals(getType()));
	}

	private void typePanelClicked(MouseEvent e) {
		if (!isEnabled()) {
			return;
		}
		if (advancedPalette.getSelectedGroupPanel() != null) {
			advancedPalette.getSelectedGroupPanel().highlight(false);
			advancedPalette.setSelectedGroupPanel(null);
		}

		if (!e.isControlDown()) {
			for (Iterator iterator = advancedPalette.getSelectedTypePanels().iterator(); iterator.hasNext();) {
				ArcTypePanel p = (ArcTypePanel) iterator.next();
				p.highlight(false);
			}
			advancedPalette.getSelectedTypePanels().removeAllElements();
		}

		if (e.isShiftDown()) {
			boolean foundLastAddedPanel = false;
			boolean foundPanel = false;
			int lastAddedPanelIndex = 0;
			int panelIndex = 0;

			Component[] components = getParent().getComponents();
			for (int i = 0; i < components.length; i++) {
				Component component = components[i];

				if (component instanceof ArcTypePanel) {
					if (!foundLastAddedPanel && component == advancedPalette.getLastSelectedTypePanel()) {
						foundLastAddedPanel = true;
						lastAddedPanelIndex = i;
					}

					if (!foundPanel && component == this) {
						foundPanel = true;
						panelIndex = i;
					}
				}
			}

			int l, m;
			if (panelIndex < lastAddedPanelIndex) {
				l = panelIndex;
				m = lastAddedPanelIndex;
			} else {
				m = panelIndex;
				l = lastAddedPanelIndex;
			}

			for (int i = l; i <= m; i++) {
				Component component = components[i];

				if (component instanceof ArcTypePanel) {
					((ArcTypePanel) component).highlight(true);
					advancedPalette.getSelectedTypePanels().add(component);
				}
			}

		} else {
			highlight(true);
			advancedPalette.getSelectedTypePanels().add(this);
			advancedPalette.setLastSelectedTypePanel(this);
		}
		advancedPalette.enableDisableButtons();
	}

	public void showProperties() {
		stylePanelClicked();
	}

	private void stylePanelClicked() {
		if (!isEnabled()) {
			return;
		}
		Frame parent = ApplicationAccessor.getParentFrame();
		Vector arcStyles = advancedPalette.displayBean.getArcStyles();
		Color defaultColor = (Color) advancedPalette.attrToVisVarBean.getDefaultNominalVisualVariable(
				AttributeConstants.NOM_ATTR_REL_TYPE, VisVarConstants.VIS_VAR_ARC_COLOR, type, color);
		ArcPresentationDialog dialog = new ArcPresentationDialog(parent, arcStyles, style, color, defaultColor, btnStyle);
		if (dialog.accepted()) {
			ArcStyle newStyle = dialog.getArcStyle();
			Color newColor = dialog.getColor();
			if (!newStyle.equals(style) || !newColor.equals(color)) {
				btnStyle.removeAll();
				JComponent newStyleComponent = newStyle.getThumbnail(newColor);
				btnStyle.add(newStyleComponent);
				btnStyle.revalidate();
				btnStyle.repaint();
				if (!newStyle.equals(style)) {
					this.style = newStyle;
					advancedPalette.attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE,
							VisVarConstants.VIS_VAR_ARC_STYLE, type, newStyle);
				}
				if (!newColor.equals(color)) {
					this.color = newColor;
					advancedPalette.attrToVisVarBean.setNominalVisualVariableValue(AttributeConstants.NOM_ATTR_REL_TYPE,
							VisVarConstants.VIS_VAR_ARC_COLOR, type, newColor);
				}
			}
		}
	}

	public void highlight(boolean highlight) {
		if (highlight) {
			setBackground(ArcFilterPalette.SELECTION_BACKGROUND_COLOR);
			setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
			lblType.setForeground(ArcFilterPalette.SELECTION_FOREGROUND_COLOR);
		} else {
			setBackground(ArcFilterPalette.TEXT_BACKGROUND_COLOR);
			setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
			lblType.setForeground(ArcFilterPalette.TEXT_FOREGROUND_COLOR);
		}
	}

	public String getType() {
		return type;
	}

	public void setStyle(ArcStyle style) {
		this.style = style;
		btnStyle.removeAll();
		JComponent component = style.getThumbnail(color);
		component.setOpaque(false);
		btnStyle.add(component);
		validate();
	}

	public Color getColor() {
		return this.color;
	}

	public ArcStyle getStyle() {
		return this.style;
	}

	public void setColor(Color color) {
		this.color = color;
		btnStyle.removeAll();
		JComponent component = style.getThumbnail(color);
		component.setOpaque(false);
		btnStyle.add(component);
		validate();
	}

	public void setDisplayFiltered(boolean displayFiltered) {
		if (isEnabled()) {
			this.filteredInDisplay = displayFiltered;
			this.advancedPalette.filterSingleTypeInDisplay(ArcTypePanel.this.type, displayFiltered);
			//lblDisplayFilter.setIcon( displayFiltered ? AdvancedArcFilterPalette.ICON_DISPLAY_FILTER_FILTERED : AdvancedArcFilterPalette.ICON_DISPLAY_FILTER_UNFILTERED);
			chkDisplayFilter.setSelected(!displayFiltered);
		}
	}

	public void dragEnter(DragSourceDragEvent e) {
		e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
	}

	public void dragOver(DragSourceDragEvent e) {
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent e) {
		e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	final StringSelection dummyTransferable = new StringSelection("");

	public void dragGestureRecognized(DragGestureEvent e) {
		Cursor cursor = DragSource.DefaultMoveNoDrop;
		e.getDragSource().startDrag(e, cursor, dummyTransferable, this);
	}


}