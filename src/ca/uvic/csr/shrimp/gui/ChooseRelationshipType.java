/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import ca.uvic.csr.shrimp.DataBean.DataBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.util.EscapeDialog;


/**
 * Displays a list of relationships for the user to choose.
 *
 * @author Chris Callendar
 * @date 11-Aug-06
 */
public class ChooseRelationshipType extends EscapeDialog {

	private ShrimpProject project;
	private boolean allowMultiple;
	private List selectedRelationships;
	private Collection relationshipsToHide;
	private boolean okPressed = false;

	private JList lstRels;
	private JButton btnOK;
	private JPanel pnlButtons;

	public ChooseRelationshipType(Frame owner, ShrimpProject project, boolean allowMultiple) throws HeadlessException {
		this(owner, project, allowMultiple, Collections.EMPTY_LIST);
	}

	public ChooseRelationshipType(Frame owner, ShrimpProject project, boolean allowMultiple, Collection relationshipsToHide) throws HeadlessException {
		super(owner, true);
		this.project = project;
		this.allowMultiple = allowMultiple;
		this.selectedRelationships = Collections.EMPTY_LIST;
		this.relationshipsToHide = relationshipsToHide;
		initialize();
	}

	public Collection getSelectedRelationships() {
		return selectedRelationships;
	}

	public String getSelectedRelationship() {
		String rel = null;
		if (selectedRelationships.size() > 0) {
			rel = (String) selectedRelationships.get(0);
		}
		return rel;
	}

	public boolean isOKPressed() {
		return okPressed;
	}

	protected void okPressed() {
		okPressed = true;
		selectedRelationships = getSelectionFromList(getRelationshipsList());
	}

	protected void cancelPressed() {
		okPressed = false;
		selectedRelationships = Collections.EMPTY_LIST;
	}

	private List getSelectionFromList(JList lst) {
		Object[] sel = lst.getSelectedValues();
		ArrayList list = new ArrayList(sel.length);
		for (int i = 0; i < sel.length; i++) {
			list.add(sel[i]);
		}
		return list;
	}

	private void loadRelationships() {
		if (project != null) {
			try {
				DataBean dataBean = (DataBean) project.getBean(ShrimpProject.DATA_BEAN);
				Vector rels = dataBean.getRelationshipTypes(true, true);
				if (relationshipsToHide.size() > 0) {
					for (Iterator iter = relationshipsToHide.iterator(); iter.hasNext(); ) {
						rels.remove(iter.next());
					}
				}
				getRelationshipsList().setListData(rels);
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void initialize() {
		getContentPane().setLayout(new BorderLayout(5, 5));

		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		pnl.add(new JScrollPane(getRelationshipsList()), BorderLayout.CENTER);

		getContentPane().add(pnl, BorderLayout.CENTER);
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		String title = (allowMultiple ? "Select relationships" : "Select a relationship type");
		setTitle(title);
		loadRelationships();

		pack();

		setPreferredSize(new Dimension(250, 350));
		Point p = (getParent() == null ? new Point(0, 0) : getParent().getLocation());
		p.translate(200, 100);
		setLocation(p);

		setDefaultButton(btnOK);
		setVisible(true);
	}

	private Component getButtonsPanel() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
			btnOK = new JButton(createOKAction());
			JButton btnCancel = new JButton(createCancelAction());
			pnlButtons.add(btnOK);
			pnlButtons.add(btnCancel);
		}
		return pnlButtons;
	}

	private JList getRelationshipsList() {
		if (lstRels == null) {
			lstRels = new JList(new DefaultListModel());
			lstRels.setSelectionMode(allowMultiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
		}
		return lstRels;
	}

}
