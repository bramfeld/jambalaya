/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpTool;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.HierarchicalView.HierarchicalView;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;


/**
 * Base class for {@link ViewTool} like the {@link ShrimpView}, {@link QueryView}, and {@link HierarchicalView}.
 *
 * @author Chris Callendar
 * @date 22-Oct-07
 */
public abstract class AbstractViewTool extends AbstractShrimpTool implements ViewTool {

	/** Default output text color. */
	protected static final Color OUTPUT_COLOR = Color.gray;

	private JLabel outputLabel;

	public AbstractViewTool(String toolName, ShrimpProject project) {
		super(toolName, project);
	}

	protected JLabel getOutputLabel() {
		if (outputLabel == null) {
			outputLabel = new JLabel("", JLabel.RIGHT);
			outputLabel.setPreferredSize(new Dimension(160, 18));
			outputLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
			outputLabel.setForeground(OUTPUT_COLOR);
		}
		return outputLabel;
	}

    /**
     * Sets the text into the output label.
     * @param text
     */
	public void setOutputText(String text) {
		setOutputText(text, OUTPUT_COLOR);
	}

	/**
     * Sets the text into the output label and the color.
     * @param text
     */
	public void setOutputText(String text, Color color) {
		synchronized (outputLabel) {
			outputLabel.setText(text);
			outputLabel.setToolTipText(text);
			outputLabel.setForeground(color);
			outputLabel.repaint();
		}
	}

	public void clearOutputText() {
		clearOutputText(0);
	}

    public void clearOutputText(final long delay) {
		if (delay > 0) {
			final String text = outputLabel.getText();
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {}
					synchronized (outputLabel) {
						if (text.equals(outputLabel.getText())) {
							outputLabel.setText("");
							outputLabel.setToolTipText("");
						}
					}
				}
			}).start();
		} else {
			setOutputText("");
		}
    }

}
