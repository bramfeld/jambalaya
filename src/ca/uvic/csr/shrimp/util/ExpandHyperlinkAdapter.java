/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import ca.uvic.csr.shrimp.DataBean.Artifact;
import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpNode;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * Not used at the moment.
 *
 * @author Rob Lintern
 * @date Aug-03
 */
public class ExpandHyperlinkAdapter implements HyperlinkListener {

	private PShrimpNode node;
	private DisplayBean displayBean;

	public ExpandHyperlinkAdapter(PShrimpNode node, DisplayBean displayBean) {
		this.node = node;
		this.displayBean = displayBean;
	}

	protected Component addLinksPanel() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html");

		Vector rels = node.getArtifact().getRelationships();
		// sort into incoming and outgoing, then group by type
		Map outgoingTypeMap = new HashMap();
		Map incomingTypeMap = new HashMap();
		Vector incomingRels = new Vector();
		Vector outgoingRels = new Vector();
		for (Iterator iter = rels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			Vector artifacts = rel.getArtifacts();
			Artifact parentArt = (Artifact) artifacts.elementAt(0);
			Artifact childArt = (Artifact) artifacts.elementAt(1);
			if (parentArt.equals(node.getArtifact()) /*&& !childArt.isRoot(rel.getType())*/) {
				// this artifact is the parent or source
				outgoingRels.add(rel);
			} else if (childArt.equals(node.getArtifact()) /*&& !parentArt.isRoot(rel.getType())*/) {
				// this artifact is the child or destination
				incomingRels.add(rel);
			}
		}
		for (Iterator iter = outgoingRels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			Vector relsOfType = (Vector) outgoingTypeMap.get(rel.getType());
			if (relsOfType == null) {
				relsOfType = new Vector();
				outgoingTypeMap.put(rel.getType(), relsOfType);
			}
			relsOfType.add(rel);
		}
		for (Iterator iter = incomingRels.iterator(); iter.hasNext();) {
			Relationship rel = (Relationship) iter.next();
			Vector relsOfType = (Vector) incomingTypeMap.get(rel.getType());
			if (relsOfType == null) {
				relsOfType = new Vector();
				incomingTypeMap.put(rel.getType(), relsOfType);
			}
			relsOfType.add(rel);
		}
		StringBuffer pageHTML = new StringBuffer();
		pageHTML.append("<html><head></head><body>");
		pageHTML.append("\n<h2>Outgoing Arcs</h2>\n");
		ArrayList outgoingRelTypes = new ArrayList(outgoingTypeMap.keySet());
		Collections.sort(outgoingRelTypes);
		for (Iterator iter = outgoingRelTypes.iterator(); iter.hasNext();) {
			String relType = (String) iter.next();
			pageHTML.append("<h3>" + "<a href=\"" + "outgoing" + "^" + relType + "\">" + relType + "</a></h3>\n");
			Vector relsOfType = (Vector) outgoingTypeMap.get(relType);
			for (Iterator iterator = relsOfType.iterator(); iterator.hasNext();) {
				Relationship rel = (Relationship) iterator.next();
				Artifact targetArt = (Artifact) rel.getArtifacts().elementAt(1);
				//if (!targetArt.isRoot(relType)) {
				pageHTML.append("<a href=\"" + "outgoing" + "^" + relType + "^" + targetArt.getID() + "\">" + targetArt.getName() + "</a>");
				if (iterator.hasNext()) {
					pageHTML.append(", \n");
				} else {
					pageHTML.append("<br>\n");
				}
				//}
			}
		}
		pageHTML.append("\n<h2>Incoming Arcs</h2>\n");
		ArrayList incomingRelTypes = new ArrayList(incomingTypeMap.keySet());
		Collections.sort(incomingRelTypes);
		for (Iterator iter = incomingRelTypes.iterator(); iter.hasNext();) {
			String relType = (String) iter.next();
			pageHTML.append("<h3>" + "<a href=\"" + "incoming" + "^" + relType + "\">" + relType + "</a></h3>\n");
			Vector relsOfType = (Vector) incomingTypeMap.get(relType);
			for (Iterator iterator = relsOfType.iterator(); iterator.hasNext();) {
				Relationship rel = (Relationship) iterator.next();
				Artifact targetArt = (Artifact) rel.getArtifacts().elementAt(0);
				//if (!targetArt.isRoot(relType)) {
				pageHTML.append("<a href=\"" + "incoming" + "^" + relType + "^" + targetArt.getID() + "\">" + targetArt.getName() + "</a>");
				if (iterator.hasNext()) {
					pageHTML.append(", \n");
				} else {
					pageHTML.append("<br>\n");
				}
				//}
			}
		}
		pageHTML.append("</body></html>");

		editorPane.setText(pageHTML.toString());
		editorPane.addHyperlinkListener(this);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		scrollPane.getVerticalScrollBar().setValue(0);
		scrollPane.getHorizontalScrollBar().setValue(0);
		return scrollPane;
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			ApplicationAccessor.waitCursor();
			try {
				StringTokenizer tokenizer = new StringTokenizer(e.getDescription(), "^");
				if (tokenizer.hasMoreTokens()) {
					// we want to focus on the child node that has this id
					String targetChildArtIdStr = tokenizer.nextToken();
					DataBean dataBean = node.getArtifact().getDataBean();
					Object targetChildArtID = dataBean.getArtifactExternalIDFromString(targetChildArtIdStr);
					//System.out.println("targetIdStr: " + targetChildArtIdStr);
					Vector childNodes = displayBean.getDataDisplayBridge().getChildNodes(node);
					for (Iterator iter = childNodes.iterator(); iter.hasNext(); ) {
						final ShrimpNode childNode = (ShrimpNode) iter.next();
						Artifact childArt = childNode.getArtifact();
						if (childArt.getExternalId().equals(targetChildArtID)) {
							// this is the node we want to focus on, put in a different thread so we can wait a bit first
							new Thread(new Runnable() {
								public void run() {
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {}
									displayBean.focusOn(childNode);
								}
							}, "expand link focus on thread").start();
							break;
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				ApplicationAccessor.defaultCursor();
			}
		}
	}
}
