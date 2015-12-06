/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.awt.Color;


/**
 * Defines the nominal visual variable of node colour.
 * 
 * @author Rob Lintern, Chris Callendar
 */
public class NominalNodeColorVisualVariable extends NominalColorVisualVarible {

	/**
	 * Constructs a new node color nominal visual variable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public NominalNodeColorVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	protected void loadDefaultColors() {
		defaultNominalColors = new Color [12];
		defaultNominalColors[0] =  new Color(165,195,210); //darker blue
		defaultNominalColors[1] =  new Color(219,193,181); //light browny   
		defaultNominalColors[2] =  new Color(169,192,177); //darker green
		defaultNominalColors[3] =  new Color(255,251,204); //cream
		defaultNominalColors[4] =  new Color(184,183,204); //darker purple    
		defaultNominalColors[5] =  new Color(203,195,122); //greeny yellow
		defaultNominalColors[6] =  new Color(224,222,239); //lighter purple
		defaultNominalColors[7] =  new Color(212,208,179); //tan
		defaultNominalColors[8] =  new Color(212,239,252); //lighter blue
		defaultNominalColors[9] =  new Color(252,211,193); //just peachy
		defaultNominalColors[10] = new Color(204,231,211); //lighter green
		defaultNominalColors[11] = new Color(217,194,206); //pinky purply
	}
	
}
