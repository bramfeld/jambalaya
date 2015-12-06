/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.AttrToVisVarBean;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * Allows a specific Icons to be mapped to specific attribute values.
 * @author Rob Lintern
 * 
 */
public class IconVisualVariable extends NominalVisualVariable {

	/**
	 * @param attrToVisVarBean
	 * @param name
	 */
	public IconVisualVariable(AttrToVisVarBean attrToVisVarBean, String name) {
		super(attrToVisVarBean, name);
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.NominalVisualVariable#getNextDefaultNomVisVarValue(java.lang.Object)
	 */
	protected Object getNextDefaultNomVisVarValue(Object attributeValue) {
		return null; 
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getVisVarValueFromString(java.lang.String)
	 */
	public Object getVisVarValueFromString(String s) {
        ImageIcon imageIcon = null;   
        // an empty string will cause bad problems since the URL will still be a valid path to the root folder
        if ((s != null) && !"".equals(s)) {
	        // if this is a url to a icon in a jar file then we just need the part after the '!'
	        URL url = ResourceHandler.getFileURL(s);
	        if (url == null && s.startsWith("jar:file")) {
	            int i = s.lastIndexOf('!');
	            if (i != -1 && s.length() > i + 1) {
	                String newS = s.substring(i + 1);
	                url = ResourceHandler.getFileURL(newS);
	            }
	        }
	        if (url != null) {
	            imageIcon = new ImageIcon(url, s);
	        }
        }
		return imageIcon; 
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.AttrToVisVarBean.VisualVariable#getStringFromVisVarValue(java.lang.Object)
	 */
	public String getStringFromVisVarValue(Object visVarValue) {
		String s = "";
		if (visVarValue != null && visVarValue instanceof Icon) {
			Icon icon = (Icon) visVarValue;
			if (icon instanceof ImageIcon) {
				s = ((ImageIcon)icon).getDescription();
			} 
			if (s == null || s.equals("")) {
				s = icon.getClass().getName();
			}
		}
		//System.out.println(s);
		return s;
	}
	
}
