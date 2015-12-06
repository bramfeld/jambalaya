/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpTool;

import java.awt.Color;


/**
 * Interface for {@link ShrimpTool} objects that support navigating to objects and mouse modes.
 * Also supports displaying output text somewhere in the view.
 *
 * @author Jeff Michaud, Chris Callendar
 */
public interface ViewTool extends ShrimpTool {

	public void navigateToObject(Object destObject);
	public String getMouseMode();

	/**
	 * Displays the output text somewhere in the view.
	 * The text will be displayed in the default color.
	 * @param text the text to display.
	 */
	public void setOutputText(String text);

	/**
	 * Displays the output text somewhere in the view.
	 * @param text the text to display.
	 * @param color the color for the text.
	 */
	public void setOutputText(String text, Color color);

	/**
	 * Clears the output text in the view immediately.
	 */
	public void clearOutputText();

	/**
	 * Clears the output text in the view after some delay.
	 * @param delay
	 */
	public void clearOutputText(long delay);

}
