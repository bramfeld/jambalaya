/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Properties;

import javax.swing.text.JTextComponent;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.event.AnnotationPanelLoadEvent;
import ca.uvic.csr.shrimp.DataBean.event.AnnotationPanelSaveEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;

/**
 * @author Rob Lintern, Chris Callendar
 */
public class AnnotationPanelActionAdapter implements CustomizedPanelActionListener {

	private static final String KEY_ANNOTATION = "annotation.";

	private Properties properties;

	public AnnotationPanelActionAdapter(Properties projectProperties) {
		this.properties = projectProperties;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener#actionPerformed(CustomizedPanelActionEvent)
	 */
	public void actionPerformed(CustomizedPanelActionEvent e) {
		if (e instanceof AnnotationPanelSaveEvent) {
			AnnotationPanelSaveEvent apse = (AnnotationPanelSaveEvent) e;
			String artifactID = apse.getArtifactID();
			String annotation = apse.getAnnotation();
			setArtifactAnnotation(properties, artifactID, annotation);
		} else if (e instanceof AnnotationPanelLoadEvent) {
			AnnotationPanelLoadEvent aple = (AnnotationPanelLoadEvent) e;
			String artifactID = aple.getArtifactID();
			String annotation = getArtifactAnnotation(properties, artifactID);
			JTextComponent textbox = aple.getTextComponent();
			textbox.setText(annotation);
		}
	}

	public static String getArtifactAnnotation(Properties props, String artifactID) {
		String propValue = "";
		if (artifactID != null) {
			String key = KEY_ANNOTATION + artifactID;
			if (props.containsKey(key)) {
				propValue = props.getProperty(key, "").trim();
				// clean up any empty annotations
				if (propValue.length() == 0) {
					props.remove(key);
				}
			}
		}
		return propValue;
	}

	public static void setArtifactAnnotation(Properties props, String artifactID, String annotation) {
		if (artifactID != null) {
			annotation = annotation.trim();
			String key = KEY_ANNOTATION + artifactID;
			if ((annotation != null) && (annotation.length() > 0)) {
				//System.out.println("Setting annotation: " + artifactID + " : " + annotation);
				props.setProperty(key, annotation);
			} else {
				props.remove(key);
			}
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener#getCustomizedPanelType()
	 */
	public String getCustomizedPanelType() {
		return PanelModeConstants.PANEL_ANNOTATION;
	}

}
