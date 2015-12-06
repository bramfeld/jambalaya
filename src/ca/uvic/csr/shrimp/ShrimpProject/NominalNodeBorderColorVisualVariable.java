/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpProject;

import java.awt.Color;

import ca.uvic.csr.shrimp.AttrToVisVarBean.AttrToVisVarBean;
import ca.uvic.csr.shrimp.AttrToVisVarBean.NominalColorVisualVarible;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.NodeBorder;

/**
 *
 * @author Chris Bennett
 */
public class NominalNodeBorderColorVisualVariable extends NominalColorVisualVarible {

	/**
	 * Constructs a new node border color nominal visual variable.
	 * @param attrToVisVarBean The bean that this visual variable is to be registered with.
	 * @param name The name to be given to this visual variable.
	 */
	public NominalNodeBorderColorVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	protected void loadDefaultColors() {
		defaultNominalColors = new Color [1];
		defaultNominalColors[0] =  NodeBorder.DEFAULT_UNHIGHLIGHT_COLOR;
	}


}
