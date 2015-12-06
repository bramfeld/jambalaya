/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.awt.Color;
import java.util.StringTokenizer;

/**
 * Defines a generic nominal colour visual variable, meaning that this visual variable will
 * have discrete Color objects as its values.
 *  
 * @author Rob Lintern
 *
 */
public class NominalColorVisualVarible extends NominalVisualVariable {
	
	protected Color[] defaultNominalColors;
	private int nextColorIndex = 0;
	
	/**
	 * Constructs a new color nominal visual variable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public NominalColorVisualVarible(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
		nextColorIndex = 0;
		loadDefaultColors();
	}
	
	/** 
	 * Sets the default colors.
	 */
	protected void loadDefaultColors() {
		defaultNominalColors = new Color [10];
		defaultNominalColors[0] = new Color(255, 182, 193); //lightpink                
		defaultNominalColors[1] = new Color(135, 206, 250); //lightskyblue                
		defaultNominalColors[2] = new Color(250, 250, 210); //lightgoldenyellow
		defaultNominalColors[3] = new Color(221, 160, 221); //plum        
		defaultNominalColors[4] = new Color(159, 253, 159); //lightgreen
		defaultNominalColors[5] = new Color(211, 211, 211); //lightgrey
		defaultNominalColors[6] = new Color(255, 218, 185); //peachpuff        
		defaultNominalColors[7] = new Color(224, 255, 255); //lightcyan
		defaultNominalColors[8] = new Color(000, 250, 154); //mediumspringgreen        
		defaultNominalColors[9] = new Color(240, 230, 140); //khaki
	}
	
	public Color[] getDefaultColors() {
		return defaultNominalColors;
	}

	/**
	 *  
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
	 */
	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		Color nextColor = defaultNominalColors[nextColorIndex];
		if (nextColorIndex == defaultNominalColors.length - 1) {
			nextColorIndex = 0;
		} else {
			nextColorIndex++;
		}
		return nextColor;
	}

	/**
	 *  
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
	 */
	public Object getVisVarValueFromString(String s) {
		StringTokenizer stringTokenizer = new StringTokenizer(s, ",");
		int r = Integer.parseInt(stringTokenizer.nextToken());
		int g = Integer.parseInt(stringTokenizer.nextToken());
		int b = Integer.parseInt(stringTokenizer.nextToken());
			
		Color color = new Color(r, g, b);
		return color;
	}
	
	/**
	 *  
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
	 */
	public String getStringFromVisVarValue(Object visVarValue) {
		Color color = (Color) visVarValue;
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
	    
		return "" + r + "," + g + "," + b;
	}

}
