/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.util.StringTokenizer;



/**
 * Defines an ordinal color visual variable, meaning that values of this visual variable will
 * be in a continuous range of type Color.
 * @see OrdinalVisualVariable
 * 
 * @author Rob Lintern
 */
public class OrdinalColorVisualVariable extends OrdinalVisualVariable {
	public static final Color DEFAULT_MIN_COLOR = new Color (130, 255, 130); // medium green
	public static final Color DEFAULT_MAX_COLOR = new Color (130, 130, 255); // medium blue
	
	private static Raster raster;
	
	/**
	 * 
	 * @param attrToVisVarBean The bean that this visual variable is to registered with.
	 * @param name The name of this visual variable.
	 */
	public OrdinalColorVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
		minVisVarValue = DEFAULT_MIN_COLOR;
		maxVisVarValue = DEFAULT_MAX_COLOR;
		Color minColor = (Color) minVisVarValue;
		Color maxColor = (Color) maxVisVarValue;
		updateRaster(minColor, maxColor);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable#setMinVisVarValue(java.lang.Object)
	 */
	public void setMinVisVarValue(Object minVisVarValue) {
		Color minColor = (Color) minVisVarValue;
		Color maxColor = (Color) maxVisVarValue;
		updateRaster(minColor, maxColor);
		super.setMinVisVarValue(minVisVarValue);
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable#setMaxVisVarValue(java.lang.Object)
	 */
	public void setMaxVisVarValue(Object maxVisVarValue) {
		Color minColor = (Color) minVisVarValue;
		Color maxColor = (Color) maxVisVarValue;
		updateRaster(minColor, maxColor);
		super.setMaxVisVarValue(maxVisVarValue);
	}

	
	private void updateRaster (Color minColor, Color maxColor) {
		// we use a gradient paint here to help us determine the proper color
		Rectangle bounds = new Rectangle (0, 0, 100, 1);
		GradientPaint gp = new GradientPaint (0.0f, 0.0f, minColor, 100.0f, 0.0f, maxColor);
		ColorModel cm = Toolkit.getDefaultToolkit().getColorModel();
		PaintContext pc = gp.createContext(cm, bounds, bounds, new AffineTransform (), null);
		raster = pc.getRaster(0,0,100,1);
	}

	
	/**
	 *  
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.OrdinalVisualVariable#getVisVarValue(java.lang.Object, double)
	 */
	protected Object getVisVarValue(Object attrValue, double position) {
		Color colorValue = null;
		int positionInt = (int)(position*99.0); // a position from 0 to 99 inclusive
		try {
			// we use a gradient paint here to help us determine the proper color
			int rPixel = raster.getSample(positionInt, 0, 0); // band 0 is red
			int gPixel = raster.getSample(positionInt, 0, 1); // band 1 is green
			int bPixel = raster.getSample(positionInt, 0, 2); // band 2 is blue
			colorValue = new Color (rPixel, gPixel, bPixel);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return colorValue;
		
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
