/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.awt.Component;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import ca.uvic.csr.shrimp.JavaDomainConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * @author Rob Lintern
 */
public class SimpleArtifact extends AbstractArtifact {

    protected Vector changedArt = new Vector();


	/**
	 * Constructor for SimpleArtifact.
	 * A unique id will be automatically assigned to this artifact.
	 * @param db
	 * @param name
	 * @param type
	 */
	public SimpleArtifact(DataBean db, String name, String type){
		this(db, name, type, null);
	}

	/**
	 * Constructor for SimpleArtifact.
	 * @param db
	 * @param name
	 * @param type
	 * @param externalId
	 */
	public SimpleArtifact(DataBean db, String name, String type, Object externalId){
		super(db, name, type, externalId);
	}

	/**
	 * @see AbstractArtifact#createPanel(String)
	 */
	protected Component createPanel(String mode) {
		if (PanelModeConstants.PANEL_DEFAULT.equals(mode)) {
			if (JavaDomainConstants.CLASS_ART_TYPE.equals(getType()) && (getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI) != null || getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE) != null)) {
				addCodePanel((URI) getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI));
			} else {
				Vector possibles = getCustomizedPanelNames();
				return getCustomizedPanel((String) possibles.firstElement());
			}
		} else if (SoftwareDomainConstants.PANEL_UML.equals(mode)) {
			addUMLPanel((URI) getAttribute(SoftwareDomainConstants.UML));
		} else if (SoftwareDomainConstants.PANEL_CODE.equals(mode)) {
			addCodePanel((URI) getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI));
		} else if (PanelModeConstants.PANEL_ANNOTATION.equals(mode)) {
			addAnnotationPanel();
		} else if (JavaDomainConstants.PANEL_JAVADOC.equals(mode)) {
			addJavaDocPanel((URI) getAttribute(JavaDomainConstants.JAVADOC));
		} else if (JavaDomainConstants.PANEL_CODE_AND_JAVADOC.equals(mode)) {
			addCodeAndJavaDocPanel((URI) getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI), (URI) getAttribute(JavaDomainConstants.JAVADOC));
		} else if (PanelModeConstants.PANEL_ATTRIBUTES.equals(mode)) {
		    addAttributesPanel();
		} else if (PanelModeConstants.PANEL_DOCUMENTS.equals(mode)) {
			addDocumentsPanel();
		}

		return (Component) customizedPanels.get(mode);
	}

	/**
	 * @see Object#clone()
	 */
	public Object clone() {
		return new SimpleArtifact(dataBean, getName(), getType(), getExternalId());
	}

	/**
	 * @see Artifact#getDefaultPanelModeOrder()
	 */
	public String[] getDefaultPanelModeOrder() {
		String[] ret = new String[0];
		if (getType().equalsIgnoreCase(JavaDomainConstants.CLASS_ART_TYPE) ||
			getType().equalsIgnoreCase(JavaDomainConstants.INTERFACE_ART_TYPE)) {
			ret = new String[4];
			ret[0] = PanelModeConstants.CHILDREN;
			ret[1] = SoftwareDomainConstants.PANEL_CODE;
			ret[2] = SoftwareDomainConstants.PANEL_UML;
			ret[3] = PanelModeConstants.PANEL_DEFAULT;
		} else if (getType().equals ("Operation") || getType().equals(JavaDomainConstants.METHOD_ART_TYPE)) {
			ret = new String[2];
			ret[0] = SoftwareDomainConstants.PANEL_CODE;
			ret[1] = PanelModeConstants.PANEL_DEFAULT;
		} else {
			ret = new String[3];
			ret[0] = PanelModeConstants.CHILDREN;
			ret[1] = PanelModeConstants.PANEL_ATTRIBUTES;
			ret[2] = PanelModeConstants.PANEL_DEFAULT;
		}
		return ret;
	}

	/**
	 * @see Artifact#getCustomizedPanelNames()
	 */
	public Vector getCustomizedPanelNames() {
		Vector names = new Vector();
        if (getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI) != null ||
        	getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE) != null) {
			names.addElement (SoftwareDomainConstants.PANEL_CODE);
		}
        if (getAttribute(JavaDomainConstants.JAVADOC) != null) {
			names.addElement (JavaDomainConstants.PANEL_JAVADOC);
		}
        if (getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE_URI) != null &&
        	getAttribute(JavaDomainConstants.JAVADOC) != null) {
        	names.addElement (JavaDomainConstants.PANEL_CODE_AND_JAVADOC);
        }
		if (getType().equalsIgnoreCase(JavaDomainConstants.INTERFACE_ART_TYPE) ||
			getType().equalsIgnoreCase(JavaDomainConstants.CLASS_ART_TYPE)) {
			names.addElement(SoftwareDomainConstants.PANEL_UML);
		}

		names.addElement(PanelModeConstants.PANEL_ATTRIBUTES);
        names.addElement(PanelModeConstants.PANEL_ANNOTATION);
 		names.addElement(PanelModeConstants.PANEL_DOCUMENTS);

		return names;
	}


	/* ********** The Panels available in this domain *************** */

	/**
	 * Add the pane for viewing the artifact's code
	 */
	protected void addCodePanel(URI uri) {
		JScrollPane scroller = createCodePanel(uri);
	    setCustomizedPanel (SoftwareDomainConstants.PANEL_CODE, scroller);
	    setCustomizedPanel (PanelModeConstants.PANEL_DEFAULT, scroller);
	}

	/**
	 * Create the pane for viewing the artifact's code
	 */
	protected JScrollPane createCodePanel(URI uri) {
	    String code = "";
	    if (uri == null) {
	        code = (String)getAttribute(SoftwareDomainConstants.NOM_ATTR_SOURCE_CODE);
	        if (code == null) {
	            code = "Sorry, no code found!";
	        }
	    }
		JEditorPane displayPane = new JEditorPane();
		displayPane.setEditable(false);
		displayPane.setBorder(null);
		JScrollPane scroller = new JScrollPane(displayPane);

        try {
            if(uri != null || code != null){
				displayPane.addHyperlinkListener (new PanelHyperlinkAdapter (this, SoftwareDomainConstants.PANEL_CODE));
				displayPane.addPropertyChangeListener (new PageChangeAdapter (displayPane));
				displayPane.setContentType("text/html");
				if (uri != null) {
	                displayPane.setPage(uri.toURL());
				} else {
				    displayPane.setText(code);
				}
        		setAttribute ("Code Pane", displayPane);
            }
        } catch (FileNotFoundException e) {
        	JOptionPane.showMessageDialog (null, "Sorry, couldn't find code file for \"" + this.getName() + ".\"", ApplicationAccessor.getApplication().getName(), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scroller;
	}

	protected void addCodeAndJavaDocPanel(URI codeURI, URI javaDocURI) {
		JScrollPane codeScrollPane = createCodePanel (codeURI);
		JScrollPane javaDocScrollPane = createJavaDocPanel (javaDocURI);
		JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, codeScrollPane, javaDocScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.50);

	    setCustomizedPanel (JavaDomainConstants.PANEL_CODE_AND_JAVADOC, splitPane);
        setAttribute (JavaDomainConstants.PANEL_CODE_AND_JAVADOC, splitPane);
	}

	/**
	 * Add the pane for viewing an artifact's Java Docs
	 */
	protected void addJavaDocPanel(URI javaDocURI) {
		JScrollPane scrollPane = createJavaDocPanel (javaDocURI);
	    setCustomizedPanel (JavaDomainConstants.PANEL_JAVADOC, scrollPane);
	}

	/**
	 * Create the pane for viewing an artifact's Java Docs
	 */
	protected JScrollPane createJavaDocPanel(URI javaDocURI) {
		JEditorPane displayPane = new JEditorPane();
		displayPane.setEditable(false);
		displayPane.setBorder(null);
		JScrollPane scroller = new JScrollPane(displayPane);

        try {
            if(javaDocURI != null){
				displayPane.addHyperlinkListener (new PanelHyperlinkAdapter (this, JavaDomainConstants.PANEL_JAVADOC));
				displayPane.addPropertyChangeListener (new PageChangeAdapter (displayPane));
                displayPane.setPage(javaDocURI.toURL());
        		setAttribute ("JavaDoc Pane", displayPane);
            }
        } catch (FileNotFoundException e) {
        	JOptionPane.showMessageDialog (null, "Sorry, couldn't find the JavaDoc for \"" + this.getName() + ".\"", JavaDomainConstants.JAVADOC, JOptionPane.ERROR_MESSAGE);
        } catch (Exception e3) {
            e3.printStackTrace();
        }

      	return scroller;
	}


	/******************* The adapters for the customized panels *************/

	public class PageChangeAdapter implements PropertyChangeListener {
		private JEditorPane displayPane;

		public PageChangeAdapter (JEditorPane displayPane) {
			this.displayPane = displayPane;
		}

		public void propertyChange(PropertyChangeEvent evt) {

			if (evt.getPropertyName().equalsIgnoreCase("page")) {
				URL currentURL = (URL) evt.getNewValue();
				if (currentURL != null && currentURL.getRef() != null) {
					Rectangle currRect = displayPane.getVisibleRect();
					if (currRect.y > 5) {
						Rectangle newRect = new Rectangle(currRect.x, currRect.y + 905, 0, 0);
						displayPane.scrollRectToVisible(newRect);
					}
				}
			}
		}
	}


}
