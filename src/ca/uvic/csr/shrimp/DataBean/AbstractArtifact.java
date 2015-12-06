/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLEditorKit;

import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.PanelModeConstants;
import ca.uvic.csr.shrimp.SoftwareDomainConstants;
import ca.uvic.csr.shrimp.DataBean.event.AnnotationPanelLoadEvent;
import ca.uvic.csr.shrimp.DataBean.event.AnnotationPanelSaveEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionEvent;
import ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener;
import ca.uvic.csr.shrimp.DataBean.event.DocumentsPanelEvent;
import ca.uvic.csr.shrimp.DataBean.event.URLRequestEvent;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.gui.ActionManager.DefaultShrimpAction;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.usercontrols.AttachDocumentToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.AttachURLToNodeAdapter;
import ca.uvic.csr.shrimp.usercontrols.RemoveAllDocumentsFromNodeAdapter;
import ca.uvic.csr.shrimp.util.EscapeDialog;
import ca.uvic.csr.shrimp.util.GradientPanel;
import ca.uvic.csr.shrimp.util.ShrimpUtils;
import ca.uvic.csr.shrimp.util.TableSorter;
import ca.uvic.csr.shrimp.util.TransparentPanel;

/**
 * The AbstractArtifact contains some default implementations for Artifact in all domiains.
 * Some of the default implementations should be overwritten to increase efficiency, add
 * attributes etc for different domains.
 *
 * It also defines some abstract methods to be overwritten for Artifacts of different domains.
 *
 * @author Casey Best, Rob Lintern, Chris Callendar
 */
public abstract class AbstractArtifact implements Artifact {

	protected long id;
	protected Hashtable customizedPanels;
	protected Hashtable attributes;
	protected DataBean dataBean;
	protected Hashtable customizedPanelListeners;
	private NodeDocumentsTableModel documentModel;
	private boolean marked = false;
    private Object externalId;

	/**
	 *
	 * @param db
	 * @param name
	 * @param type
	 * @param externalId
	 */
	public AbstractArtifact(DataBean db, String name, String type, Object externalId) {
		this.dataBean = db;
		this.externalId = externalId;
		id = ((AbstractDataBean)db).nextID();
		customizedPanels = new Hashtable();
		attributes = new Hashtable();
		customizedPanelListeners = new Hashtable();
		documentModel = new NodeDocumentsTableModel();
		setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, type);
		setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_NAME, name);
		setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_ID, new Long(id));
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getID()
	 */
	public long getID() {
		return id;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#setID(long)
	 */
	//public void setID(long id) {
	//	id = id;
	//}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getName()
	 */
	public String getName() {
		return (String) getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_NAME);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#setName(java.lang.String)
	 */
	public void setName(String name) {
		setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_NAME, name);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getType()
	 */
	public String getType() {
		return (String) getAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#setType(String)
	 */
	public void setType(String type) {
		setAttribute(AttributeConstants.NOM_ATTR_ARTIFACT_TYPE, type);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getDataBean()
	 */
	public DataBean getDataBean() {
		return dataBean;
	}

	public String getExternalIdString() {
		if (dataBean != null) {
			return dataBean.getStringFromExternalArtifactID(getExternalId());
		}
		return null;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getParents(java.lang.String[])
	 */
	public Vector getParents(String [] cprels) {
		return dataBean.getParents(this, cprels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getChildren(java.lang.String[])
	 */
	public Vector getChildren(String [] cprels) {
		return dataBean.getChildren(this, cprels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getSiblings(java.lang.String[])
	 */
	public Vector getSiblings(String [] cprels) {
		return dataBean.getSiblings(this, cprels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getDescendents(java.lang.String[])
	 */
	public Vector getDescendents(String [] cprels) {
		return dataBean.getDescendents(this, cprels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getAncestors(java.lang.String[])
	 */
	public Vector getAncestors(String [] cprels) {
		return dataBean.getAncestors(this, cprels);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getChildrenCount(java.lang.String[])
	 */
	public int getChildrenCount(String [] cprels) {
		return dataBean.getChildrenCount(this, cprels);
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#getParentsCount(java.lang.String[])
     */
    public int getParentsCount(String[] cprels) {
        return dataBean.getParentsCount(this, cprels);
    }

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getDescendentsCount(java.lang.String[], boolean)
	 */
	public int getDescendentsCount(String[] cprels, boolean countArtifactMultipleTimes) {
		return dataBean.getDescendentsCount(this, cprels, countArtifactMultipleTimes);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getRelationships()
	 */
	public Vector getRelationships() {
		return dataBean.getIncomingAndOutgoingRelationships(this);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attrName) {
		if (attrName == null) {
			System.err.println("Warning - null attribute name!");
		}
		return (attrName != null ? attributes.get(attrName) : null);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String attrName, Object attrValue) {
		if (attrName != null) {
			if (attrValue != null) {
				attributes.put(attrName, attrValue);
			} else {
				attributes.remove(attrName);
			}
		} else {
			System.err.println("Warning - trying to set a null attribute name");
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#hasAttribute(java.lang.String)
	 */
	public boolean hasAttribute(String attrName) {
		return (attrName != null ? attributes.containsKey(attrName) : false);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getAttributeNames()
	 */
	public Vector getAttributeNames() {
		return new Vector(attributes.keySet());
	}

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#getCustomizedPanel(java.lang.String)
	 */
	public Component getCustomizedPanel(String mode) {
		Component component = (Component) customizedPanels.get(mode);
		if (component == null) {
			component = createPanel(mode);
			setCustomizedPanel(mode, component);
		}
		return component;
	}

	/**
	 * Creates the panel for the given panel name.
	 *
	 * @param panelName The name of the panel to create.
	 */
	protected abstract Component createPanel(String panelName);

	/**
	 *
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#setCustomizedPanel(java.lang.String, java.awt.Component)
	 */
	public void setCustomizedPanel(String mode, Component panel) {
		if (panel != null) {
			customizedPanels.put(mode, panel);
		} else {
			customizedPanels.remove(mode);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#addCustomizedPanelListener(ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener)
	 */
	public void addCustomizedPanelListener(CustomizedPanelActionListener listener) {
		Vector listeners = (Vector) customizedPanelListeners.get(listener.getCustomizedPanelType());
		if (listeners == null) {
			listeners = new Vector();
			customizedPanelListeners.put(listener.getCustomizedPanelType(), listeners);
		}
		if (!listeners.contains(listener)) {
			listeners.addElement(listener);
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#removeCustomizedPanelListener(ca.uvic.csr.shrimp.DataBean.event.CustomizedPanelActionListener)
	 */
	public void removeCustomizedPanelListener(CustomizedPanelActionListener listener) {
		Vector listeners = (Vector) customizedPanelListeners.get(listener.getCustomizedPanelType());
		if (listeners != null) {
			if (listeners.contains(listener)) {
				listeners.removeElement(listener);
			}
			if (listeners.size() == 0) {
				customizedPanelListeners.remove(listener.getCustomizedPanelType());
			}
		}
	}

	/**
	 * Fires a CustomizedPanelActionEvent
	 */
	protected void fireCustomizedPanelEvent(CustomizedPanelActionEvent customizedPanelActionEvent) {
	    String panelType = customizedPanelActionEvent.getPanelType();
		if (customizedPanelListeners.get(panelType) == null) {
			return;
		}

		Vector listeners = (Vector) ((Vector) customizedPanelListeners.get(panelType)).clone();
		if (listeners != null) {
			for (int i = 0; i < listeners.size(); i++) {
				CustomizedPanelActionListener listener = (CustomizedPanelActionListener) listeners.elementAt(i);
				listener.actionPerformed(customizedPanelActionEvent);
			}
		}
	}

	/**
	 * The default hashCode of an artifact is getID().hashCode()
	 * @return The hashCode of this artifact
	 */
	public int hashCode() {
	    if (id > Integer.MAX_VALUE) {
	        System.err.println("Watch out! The id of this artifact is greater than Integer.MAX_VALUE!");
	    }
		return (int)id; //TODO what to do if the id cannot be converted to an int?!
	}

	/**
	 * Two artifacts are considered equal if they have the same id
	 * @param obj The object to compare this artifact to
	 * @return True if the passed in object is an artifact and it has an id equal
	 * to the id of this artifact.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Artifact) {
			return ((Artifact)obj).getID() == getID();
		}
		return false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#equivalent(Artifact)
	 */
	public boolean equivalent(Artifact artifact) {
		return equals(artifact);
	}

	/**
	 * Returns the name of this artifact.
	 */
	public String toString() {
		return getName(); // + " (Artifact, id=\"" + getID() + "\", type=\"" + getType() + "\")";
	}

	/**
	 * Adds the LAYOUT_UML panel
	 */
	protected void addUMLPanel(URI umlURI) {
		JPanel pane = new JPanel();
		pane.setBackground(Color.white);
		int widest = 0;
		int width = 0;
		int height = 0;
		Rectangle lastFeature = new Rectangle();

		//Get the children and sort them according attribute / operation
		Vector attributes = new Vector();
		Vector operations = new Vector();
		String [] cprels = dataBean.getDefaultCprels();
        boolean inverted = dataBean.getDefaultCprelsInverted();
		Vector features = inverted ? getParents(cprels) : getChildren(cprels);
		for (Iterator featureIter = features.iterator(); featureIter.hasNext();) {
			AbstractArtifact child = (AbstractArtifact) featureIter.next();
			if (child.getType().equalsIgnoreCase("Attribute") || child.getType().equalsIgnoreCase("field")) {
				attributes.addElement(child);
			}
			if (child.getType().equalsIgnoreCase("Operation") || child.getType().equalsIgnoreCase("method") || child.getType().equalsIgnoreCase("constructor")) {
				operations.addElement(child);
			}
		}

		pane.setLayout(null);

		//present the attributes
		int i, j;
		for (i = 0; i < attributes.size(); i++) {
			AbstractArtifact element = (AbstractArtifact) attributes.elementAt(i);

			//visibility
			String visibility = (String) element.getAttribute("visibility");
			if (visibility == null) {
				visibility = "";
			}

			//type
			String type = (String) element.getAttribute("type");
			if (type == null) {
				type = "";
			}

			//draw text
			JLabel attLabel = new JLabel(element.getName());

			//caclulate size of text
			FontMetrics fm = pane.getFontMetrics(attLabel.getFont());
			width = fm.stringWidth(element.getName()) + 11;
			height = fm.getAscent() + 3;

			pane.add(attLabel);
			attLabel.setBounds(5, 5 + (i * fm.getAscent()), width, height);
			if (widest < width) {
				widest = width;
			}

			lastFeature = attLabel.getBounds();

		}
		i++;

		int lineEntry = (int) (lastFeature.getBounds().getHeight() + lastFeature.getY());

		for (j = 0; j < operations.size(); j++) {
			AbstractArtifact element = (AbstractArtifact) operations.elementAt(j);

			JLabel opLabel = new JLabel(element.getName());
			FontMetrics fm = pane.getFontMetrics(opLabel.getFont());
			width = fm.stringWidth(element.getName()) + 11;
			height = fm.getAscent() + 3;

			pane.add(opLabel);
			opLabel.setBounds(5, 5 + ((i + j) * fm.getAscent()), width, height);
			if (widest < width) {
				widest = width;
			}

			lastFeature = opLabel.getBounds();
		}

		//make the node big enough to display the node label.  Assume for now
		//that the label is on the node and is the same font size as these labels
		JLabel test = new JLabel(this.getName());
		FontMetrics fm = pane.getFontMetrics(test.getFont());
		width = fm.stringWidth(this.getName());
		if (widest < width) {
			widest = width + 20;
		}

		//Draw a line in between attributes and operations
		JPanel line = new JPanel();
		line.setBackground(Color.BLACK);
		line.setBounds(0, lineEntry + 5, widest + 30, 1);
		pane.add(line);

		//store the dimensions of the uml text box so that we can draw
		//a node that is _just_ big enough
		Dimension dim = new Dimension(widest + 20, lastFeature.height + lastFeature.y + 30);
		setAttribute("UMLDimension", dim);
		setCustomizedPanel(SoftwareDomainConstants.PANEL_UML, pane);

	}

	/**
	 * Add the annotation panel (a {@link JEditorPane}).
	 */
	protected void addAnnotationPanel() {
		final JTextArea textbox = new JTextArea();
		textbox.setBorder(null);
		textbox.setEditable(true);
		textbox.setWrapStyleWord(true);
		textbox.setLineWrap(true);
		textbox.setTabSize(4);
		textbox.setFont(textbox.getFont().deriveFont(textbox.getFont().getSize2D() + 2f));
		AnnotationPanelLoadEvent aple = new AnnotationPanelLoadEvent(getExternalIdString(), textbox);
		fireCustomizedPanelEvent(aple);
		textbox.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				AnnotationPanelSaveEvent apse = new AnnotationPanelSaveEvent(getExternalIdString(), textbox.getText());
				fireCustomizedPanelEvent(apse);
			}
			public void focusGained(FocusEvent e) {
				AnnotationPanelLoadEvent aple = new AnnotationPanelLoadEvent(getExternalIdString(), textbox);
				fireCustomizedPanelEvent(aple);
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createHeaderPanel(PanelModeConstants.PANEL_ANNOTATION), BorderLayout.NORTH);
		panel.add(new JScrollPane(textbox), BorderLayout.CENTER);
		setCustomizedPanel(PanelModeConstants.PANEL_ANNOTATION, panel);
	}

	/**
	 * Adds the attributes panel (contains a table of the name/value pairs).
	 */
	protected void addAttributesPanel() {
		JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        List attrNames = new ArrayList(attributes.keySet());
        Collections.sort(attrNames);

        // create the table model
        DefaultTableModel model = new DefaultTableModel(new String[] { "Name", "Value" }, attrNames.size()) {
        	public boolean isCellEditable(int row, int column) {
        		return false;
        	}
        };
        int row = 0;
		for (Iterator iter = attrNames.iterator(); iter.hasNext();) {
            String attrName = (String) iter.next();
            Object attrValue = attributes.get(attrName);
            model.setValueAt(attrName, row, 0);
            model.setValueAt(String.valueOf(attrValue), row, 1);
            row++;
        }

		// create the sortable table
		TableSorter sorter = new TableSorter(model);
		JTable table = new JTable(sorter);
		sorter.addMouseListenerToHeaderInTable(table);
        table.setAutoCreateColumnsFromModel(true);
        table.setColumnSelectionAllowed(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(18);
        table.setShowGrid(true);
        pane.add(new JScrollPane(table), BorderLayout.CENTER);

        // put an attributes label at the top of the panel
		pane.add(createHeaderPanel("Attributes"), BorderLayout.NORTH);

		setCustomizedPanel(PanelModeConstants.PANEL_ATTRIBUTES, pane);
	}

	/**
	 * Update the annotation panel text. This allows an external
	 * editor to make annotation changes, which will be reflected
	 * in a currently displayed annotation panel.
	 */
	public static void updateAnnotationPanel(Component component, String annotationtext) {
		JPanel pnlHeaderAndEditor = (JPanel)component;
		JTextArea textBox = (JTextArea)((JScrollPane)pnlHeaderAndEditor.getComponent(1)).getViewport().getView();
		textBox.setText(annotationtext);
	}

	/**
	 * Add the documents panel.
	 */
	protected void addDocumentsPanel() {
		final JEditorPane editor = new JEditorPane();
		editor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		editor.setEditable(false);

		final JTable documentsTable = documentModel.createDocumentsTable(editor);

		JPanel header = createHeaderPanel("Documents & Annotations", ResourceHandler.getIcon("icon_file.gif"));
		TransparentPanel btns = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		btns.setEmptyBorder(3, 0, 3, 4);
		createHeaderButtons(btns);
		header.add(btns, BorderLayout.EAST);

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				new JScrollPane(documentsTable), new JScrollPane(editor));
		split.setDividerLocation(120);

		JPanel panel = new GradientPanel(new BorderLayout());
		panel.add(header, BorderLayout.NORTH);
		panel.add(split, BorderLayout.CENTER);

		panel.addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent e) {
				// reset the selection, this will reload the annotation text
				int selectedRow = documentsTable.getSelectedRow();
				if (selectedRow != -1) {
					documentsTable.clearSelection();
					documentsTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
				}
			}
		});

		setCustomizedPanel(PanelModeConstants.PANEL_DOCUMENTS, panel);
	}

	protected void createHeaderButtons(JPanel panel) {
		panel.add(createHelpDocumentButton());
		panel.add(createAddDocumentButton());
		panel.add(createAddURLButton());
		//panel.add(createRemoveAllDocumentsButton(editor));
	}

	private JButton createHelpDocumentButton() {
		DefaultShrimpAction action = new DefaultShrimpAction(ResourceHandler.getIcon("icon_help.gif")) {
			public void actionPerformed(ActionEvent e) {
				Frame parent = ApplicationAccessor.getParentFrame();
				EscapeDialog dlg = new EscapeDialog(parent, "Node Documents Help", true);
				dlg.setContentPane(new GradientPanel(new BorderLayout()));

				JPanel btns = new TransparentPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
				JButton closeBtn = new JButton(dlg.createCloseAction());
				btns.add(closeBtn);
				dlg.getContentPane().add(btns, BorderLayout.SOUTH);

				TransparentPanel pnl = new TransparentPanel(new GridLayout(0, 1, 0, 4));
				dlg.getContentPane().add(pnl, BorderLayout.CENTER);
				pnl.setEmptyBorder(10);
				JEditorPane editor = new JEditorPane();
				editor.setEditable(false);
				editor.setEditorKit(new HTMLEditorKit());
				editor.setContentType("text/html");
				pnl.add(new JScrollPane(editor), BorderLayout.CENTER);
				pnl.setPreferredSize(new Dimension(650, 350));
				StringBuffer buffer = new StringBuffer();
				buffer.append("<html><body>");
				buffer.append("<h3>This view allows you to attach documents (files/images/web sites,...) to this node</h3>");
				buffer.append("<li>click the <b>Add File</b> button to attach a file from your computer to this node</li>");
				buffer.append("<li>click the <b>Add URL</b> button to attach a URL (e.g. http://www.google.com) to this node</li>");
				// not used for now
				//buffer.append("<li>click the <b>Remove Documents</b> button to remove every file or URL from this node</li>");
				buffer.append("<li>double click on a document or click the <b>Open</b> button to view the file or URL in an external viewer</li>");
				buffer.append("<li>click on a file's <b>Remove</b> button to remove it from this node</li>");
				buffer.append("<li>only text, image, and local html files can be viewed using the internal viewer</li>");
				buffer.append("<li>no modifications are made to the documents</li>");
				buffer.append("<li>nodes with attached documents will have a small paperclip icon the bottom right corner of the node</li>");
				buffer.append("<li>nodes with an annotation and no attached documents will have the annotation icon instead</li>");
				buffer.append("<li>you can also edit the <b>" + ApplicationAccessor.getAppName() + "</b> Annotation for this node</li>");
				if (AbstractArtifact.this instanceof ProtegeArtifact) {
					buffer.append("<li>any Protege annotations (from the Collaboration panel) will show up in the list too</li>");
					buffer.append("<li>you can add new Protege annotations by clicking the \"Add Annotations\" button at the top</li>");
				}
				buffer.append("</body></html>");
				editor.setText(buffer.toString());

				dlg.pack();
				dlg.setDefaultButton(closeBtn);
				ShrimpUtils.centerWindowOnParent(dlg, parent);
				dlg.setVisible(true);
			}
		};
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(22, 22));
		return btn;
	}

	private JButton createAddDocumentButton() {
		AttachDocumentToNodeAdapter action = new AttachDocumentToNodeAdapter("Add File", this);
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(95, 22));
		return btn;
	}

	private JButton createAddURLButton() {
		AttachDocumentToNodeAdapter action = new AttachURLToNodeAdapter("Add URL", this);
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(95, 22));
		return btn;
	}

	// not used for now
	protected JButton createRemoveAllDocumentsButton(final JEditorPane editor) {
		RemoveAllDocumentsFromNodeAdapter action = new RemoveAllDocumentsFromNodeAdapter("Remove Documents", this) {
			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				editor.setText("");
			}
		};
		JButton btn = new JButton(action);
		btn.setPreferredSize(new Dimension(135, 22));
		return btn;
	}

	private JPanel createHeaderPanel(String text) {
		return createHeaderPanel(text, null);
	}

	private JPanel createHeaderPanel(String text, Icon icon) {
		JPanel panel = new GradientPanel(new BorderLayout());
		panel.add(createHeaderLabel(text, icon), BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(200, 26));
		return panel;
	}

	private JLabel createHeaderLabel(String text, Icon icon) {
		JLabel lbl = new JLabel(text);
		if (icon != null) {
			lbl.setIcon(icon);
		}
		lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		lbl.setFont(lbl.getFont().deriveFont(Font.BOLD).deriveFont(14f));
		lbl.setForeground(Color.WHITE);
		return lbl;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#mark()
	 */
	public void mark() {
		marked = true;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#unmark()
	 */
	public void unmark() {
		marked = false;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DataBean.Artifact#isMarked()
	 */
	public boolean isMarked() {
		return marked;
	}

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#setExternalId()
     */
    //public void setExternalId(Object externalId) {
        //this.externalId = externalId;
    //}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#getExternalId()
     */
    public Object getExternalId() {
        return externalId;
    }

	/**
     * @see ca.uvic.csr.shrimp.DataBean.Artifact#clone()
     */
    public abstract Object clone();

    public boolean hasDocuments() {
    	// files only, no annotations
    	return (documentModel.getFileDocuments().size() > 0);
    }

    public boolean hasAnnotations() {
    	return documentModel.hasAnnotations();
    }

    public List/*<NodeDocument>*/ getDocuments() {
    	// files only, no annotations
    	return documentModel.getFileDocuments();
    }

    public void removeDocument(NodeDocument doc) {
    	documentModel.removeDocument(doc);
    	// must fire the event after the document has been removed
		DocumentsPanelEvent evt = new DocumentsPanelEvent(getExternalIdString(), getDocuments(),
				DocumentsPanelEvent.DOCUMENTS_REMOVED);
		fireCustomizedPanelEvent(evt);
    }

    public void removeAllDocuments() {
    	documentModel.removeAllFileDocuments();
    	// must fire the event after all the documents have been removed
		DocumentsPanelEvent evt = new DocumentsPanelEvent(getExternalIdString(), getDocuments(),
				DocumentsPanelEvent.DOCUMENTS_REMOVED);
		fireCustomizedPanelEvent(evt);
    }

    public void attachDocument(String file, boolean fireEvent) {
    	boolean added = documentModel.addDocument(file);
    	// must fire the event after the document has been added
		if (added && fireEvent) {
			DocumentsPanelEvent evt = new DocumentsPanelEvent(getExternalIdString(), getDocuments(),
					DocumentsPanelEvent.DOCUMENTS_ADDED);
			fireCustomizedPanelEvent(evt);
		}
    }

    public void attachDocument(NodeDocument document) {
    	documentModel.addDocument(document);
    }


	protected class PanelHyperlinkAdapter implements HyperlinkListener {
		private Artifact artifact;
		private String panelName;

		/**
		 * Constructs a new hyperlink adapter for the given artifact and panel
		 * @param artifact The artifact to associate this adapter with
		 * @param panelName The name of the panel that this adapter will listen to
		 */
		public PanelHyperlinkAdapter(Artifact artifact, String panelName) {
			this.artifact = artifact;
			this.panelName = panelName;
		}

		/**
		 *
		 * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
		 */
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				ApplicationAccessor.waitCursor();
				try {
					URL url = e.getURL(); // url is null if the href is not a valid URL
					if (url != null) {
						fireCustomizedPanelEvent(new URLRequestEvent(URI.create(url.toString()), artifact, panelName));
					} else {
					    String urlDescription = e.getDescription();
					    if (urlDescription != null) {
					        fireCustomizedPanelEvent(new URLRequestEvent(e.getDescription(), artifact, panelName));
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

}
