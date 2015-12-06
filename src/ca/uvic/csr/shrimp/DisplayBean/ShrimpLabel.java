/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import javax.swing.Icon;

import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;


/**
 * 
 * @author Rob Lintern
 */
public interface ShrimpLabel extends ShrimpDisplayObject, ShrimpDisplayObjectListener{
	
	public void setFont(Font font);
	public Font getFont();
	
	public void updateVisibility(); 	
	public void updateVisibility(Vector currentFocusedOnObjects);
	
	public void setBackgroundOpaque(boolean backgroundOpaque);
	
	public String getText();
	public void setText(String s);

	public void setBackgroundColor(Color color);
	public void setTextColor(Color color);
	
	public void setIcon(Icon icon);

	public void raiseAboveSiblings();
	
	public ShrimpDisplayObject getLabeledObject();
	
	public void setHighlighted(boolean highlighted);
	
}