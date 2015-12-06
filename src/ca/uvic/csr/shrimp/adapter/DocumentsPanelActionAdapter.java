/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.adapter;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.NodeDocument;
import ca.uvic.csr.shrimp.DataBean.ShrimpAnnotationNodeDocument;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;
import ca.uvic.csr.shrimp.DataBean.event.DocumentsPanelEvent;
import ca.uvic.csr.shrimp.util.ShrimpUtils;

/**
 * Handles adding and removing documents from the project properties file.
 *
 * @author Chris Callendar
 * @date 28-Sep-07
 */
public class DocumentsPanelActionAdapter implements CustomizedPanelActionListener {

	private static final String KEY = "documents.";

	private Properties properties;

	public DocumentsPanelActionAdapter(Properties projectProperties) {
		this.properties = projectProperties;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener#getCustomizedPanelType()
	 */
	public String getCustomizedPanelType() {
		return PanelModeConstants.PANEL_DOCUMENTS;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener#actionPerformed(CustomizedPanelActionEvent)
	 */
	public void actionPerformed(CustomizedPanelActionEvent e) {
		if (e instanceof DocumentsPanelEvent) {
			DocumentsPanelEvent dpe = (DocumentsPanelEvent) e;
			String artifactID = dpe.getArtifactID();
			List/*<NodeDocument>*/ documents = dpe.getDocuments();

			// first remove all the documents from the properties file
			removeAllDocuments(properties, artifactID);

			// now add all the current documents to the properties file
			if (documents.size() > 0) {
				int count = 0;
				for (Iterator iter = documents.iterator(); iter.hasNext(); ) {
					NodeDocument doc = (NodeDocument) iter.next();
					if (doc.saveInProperties()) {
						String key2 = KEY + artifactID + "." + count;
						properties.setProperty(key2, doc.getPath());
						count++;
					}
				}
				properties.setProperty(KEY + artifactID + ".count", String.valueOf(count));
			}
		}
	}

	/**
	 * Removes all of this artifact's documents from the properties file.
	 */
	public static void removeAllDocuments(Properties props, String artifactID) {
		if (artifactID != null) {
			String key = KEY + artifactID;
			String key2 = key + ".count";
			if (props.containsKey(key2)) {
				int count = ShrimpUtils.parseInt(props.getProperty(key2, "0"), 0);
				// don't want to keep this property either
				props.remove(key2);
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						key2 = key + "." + i;
						props.remove(key2);
					}
				}
			}
		}
	}

	/**
	 * Loads all the documents from the properties file and adds them
	 * to the artifact.
	 */
	public static void loadDocuments(Properties props, Artifact artifact) {
		String id = artifact.getExternalIdString();
		if (id != null) {
			String key = KEY + id;
			String key2 = key + ".count";
			if (props.containsKey(key2)) {
				int count = ShrimpUtils.parseInt(props.getProperty(key2, "0"), 0);
				if (count > 0) {
					for (int i = 0; i < count; i++) {
						key2 = key + "." + i;
						String file = props.getProperty(key2, "");
						if (file.trim().length() > 0) {
							artifact.attachDocument(file, false);
						}
					}
				}
			}

			// Also load the Shrimp annotation, even if it's blank
			final ShrimpAnnotationNodeDocument annotDoc = new ShrimpAnnotationNodeDocument(
								artifact.getName(), artifact.getExternalIdString(), props);
			artifact.attachDocument(annotDoc);
			// needs to listen for annotation changes to keep the hasContent flag in sync
			artifact.addCustomizedPanelListener(annotDoc);
		}
	}

}
