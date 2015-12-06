/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;

import org.semanticweb.owlapi.model.OWLEntity;

import org.protege.editor.owl.ui.action.SelectedOWLEntityAction;

/**
 * Pops up the Query View and shows the selected resource and its neighbours.
 * Passes everything on to the {@link ShowInQueryViewAction}.
 * 
 * @author Rob Lintern
 * @author Chris Callendar
 */
public abstract class ShowInQueryViewResourceAction extends SelectedOWLEntityAction {

	protected ShowInQueryViewAction action = null;

    public ShowInQueryViewResourceAction(ShowInQueryViewAction action, String group, boolean inToolBar, String shortDescription) {
        super();
        this.action = action;
		  putValue(Action.NAME, (String)action.getValue(Action.NAME));
		  putValue(Action.SMALL_ICON, (Icon)action.getValue(Action.SMALL_ICON));
        putValue(Action.SHORT_DESCRIPTION, shortDescription);
    }

    public void actionPerformed(OWLEntity e) {
    	if (e != null) {
    		Vector frames = new Vector(1);
    		frames.add(e);
			action.actionPerformed(frames);
    	}
    }


    protected void disposeAction() throws Exception {
    }
}
