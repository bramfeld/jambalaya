/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * Creates a dialog for choosing the color and style of a node or arc.
 *
 * @see NodePresentationDialog
 * @see ArcPresentationDialog
 * @author Chris Callendar
 */
public abstract class StylePresentationDialog extends EscapeDialog implements PropertyChangeListener {

	protected static final int ROW_HEIGHT = 20;
	protected static final int LABEL_WIDTH = 105;

	private boolean accepted;
	private Color color;

	private ColorLabel lblColor;
	private JButton btnOK;
	private JPanel mainPanel;
	protected JPanel pnlPreview;

	/**
	 * Initializes this dialog.  Subclasses should call {@link StylePresentationDialog#createGUI(Component)}
	 * after the call to super.
	 * @param owner The owner of this dialog.
	 * @param title the title for this dialog
	 */
	protected StylePresentationDialog(Frame owner, String title, Color color) {
		super(owner, title, true);
		this.color = color;
		this.accepted = false;
	}

	/**
	 * Attempts to place this dialog beside (left or right of) the component.
	 */
	private void setBestLocation(Component relativeTo) {
		Window parent = SwingUtilities.windowForComponent(relativeTo);
		Point desiredLocation = relativeTo.getLocationOnScreen();

		// first try the left of the window
		desiredLocation.x = parent.getX() - getWidth();
		if (desiredLocation.x < 0) {
			// now try the right side
			desiredLocation.x = parent.getX() + parent.getWidth();
		}
		//desiredLocation.y -= 30;	// move the dialog titlbar above the component
		desiredLocation.y = parent.getY();	// move the dialog to line up vertically with the parent window

		// make sure not off screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (desiredLocation.getX() + getWidth() > screenSize.getWidth()) {
			desiredLocation.setLocation((int)(screenSize.getWidth() - getWidth()), (int)desiredLocation.getY());
		}
		if (desiredLocation.getY() + getHeight() > screenSize.getHeight()) {
			desiredLocation.setLocation((int)desiredLocation.getX(), (int)(screenSize.getHeight() - getHeight()));
		}
		setLocation(desiredLocation);
	}

	/**
	 * Creates and adds GUI widgets and their listeners.
	 * This method will call {@link StylePresentationDialog#addRows()} which is
	 * where subclasses should add their code to contribute to the gui.
	 * @see StylePresentationDialog#addRows()
	 * @param relativeTo the component used to help position this dialog
	 */
	protected final void createGUI(Component relativeTo) {
		// initialize the layout to have 1 row, this will be changed later when more rows are added
		this.mainPanel = new GradientPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		// let the subclasses create rows
		addRows();

		// fill the remaining space
		mainPanel.add(Box.createVerticalGlue());

		createButtonsPanel();

		pack();
		setDefaultButton(btnOK);
		setBestLocation(relativeTo);
		setVisible(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		setColor(lblColor.getColor());
		updateThumbnail();
	}

	protected void createPreviewPanel() {
		pnlPreview = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
		pnlPreview.setOpaque(true);

		addRow("Preview", pnlPreview, 24);
	}

	protected ColorLabel createColorChooserLabel() {
		lblColor = new ColorLabel(getColor());
		lblColor.addPropertyChangeListener(this);
		return lblColor;
	}

	private void createButtonsPanel() {
		// create the buttons panel
		JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnl.setOpaque(true);
		btnOK = new JButton(new AbstractAction("  OK  ") {
			public void actionPerformed(ActionEvent e) {
				accepted = true;
		    	dispose();
			}
		});
		pnl.add(btnOK);
		JButton btnCancel = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				accepted = false;
		    	dispose();
			}
		});
		pnl.add(btnCancel);
		JButton btnDefault = new JButton(new AbstractAction("Defaults") {
			public void actionPerformed(ActionEvent e) {
				setDefaults();
				updateThumbnail();
			}
		});
		btnDefault.setToolTipText("Reset back to the default style");
		pnl.add(btnDefault);
		getContentPane().add(pnl, BorderLayout.SOUTH);
	}

	/**
	 * This gets called when the rows should be created
	 */
	protected abstract void addRows();

	protected abstract void setDefaults();

	protected abstract void updateThumbnail();

	protected void addRow(String text, JComponent component) {
		addRow(text, component, ROW_HEIGHT);
	}

	protected void addRow(String text, final JComponent component, int height) {
		JPanel pnl = new TransparentPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		JLabel lbl = new JLabel(text);
		lbl.setPreferredSize(new Dimension(LABEL_WIDTH, ROW_HEIGHT));
		pnl.add(lbl);
		pnl.add(component);

		// if the label is clicked then click the button
		if (component instanceof AbstractButton) {
			final AbstractButton btn = (AbstractButton) component;
			lbl.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					btn.doClick();
				}
			});
		}

		int width = lbl.getPreferredSize().width + component.getPreferredSize().width + ROW_HEIGHT;
		pnl.setPreferredSize(new Dimension(width, height + 10));

		mainPanel.add(pnl);
	}

	/** Returns whether or not this dialog was accepted by the user. */
	public boolean accepted() {
		return accepted;
	}

	public Color getColor() {
		return color;
	}

	protected void setColor(Color c) {
		if (c != null) {
			color = c;
			lblColor.setColor(c);
			updateThumbnail();
		}
	}

}
