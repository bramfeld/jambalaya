/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.awt.Color;


/**
 * Defines the nominal visual variable of arc colour.
 * 
 * @author Rob Lintern
 */
public class NominalArcColorVisualVariable extends NominalColorVisualVarible {
	
	/**
	 * Constructs a new arc color nominal visual variable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public NominalArcColorVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	protected void loadDefaultColors() {
		defaultNominalColors = new Color[12];
		defaultNominalColors[0] =  new Color(205,  92,  92); //indianred
		defaultNominalColors[1] =  new Color( 70, 130, 180); //steelblue        
		defaultNominalColors[2] =  new Color(186,  85, 211); //mediumorchid
		defaultNominalColors[3] =  new Color(210, 105,  30); //chocolate
		defaultNominalColors[4] =  new Color(255, 215,   0); //gold
		defaultNominalColors[5] =  new Color(205, 133,  63); //peru
		defaultNominalColors[6] =  new Color(128, 128, 128); //grey
		defaultNominalColors[7] =  new Color( 50, 205,  50); //limegreen
		defaultNominalColors[8] =  new Color(148,   0, 211); //darkviolet
		defaultNominalColors[9] =  new Color(128, 128,   0); //olive        
		defaultNominalColors[10] = new Color(189, 183, 107); //darkkahki                        
		defaultNominalColors[11] = new Color(255, 140,   0); //darkorange        
	}

}
