/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import org.protege.editor.owl.ui.transfer.OWLObjectDataFlavor;

import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.gui.ShrimpTransferHandler;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;

/**
 * Handles drag and drop transfers from Protege to Jambalaya
 *
 * @author Rob Lintern, Chris Callendar
 */
public class JambalayaTransferHandler extends ShrimpTransferHandler {

	private ShrimpProject project;

	public JambalayaTransferHandler(ShrimpProject project) {
		this.project = project;
	}

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
     */
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        boolean canImport = false;
        for (int i = 0; i < transferFlavors.length && !canImport; i++) {
            DataFlavor flavor = transferFlavors[i];
            if (flavor.equals(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR)) {
                canImport = true;
            }
        }
        return canImport;
    }

    /* (non-Javadoc)
     * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
     */
    public boolean importData(JComponent comp, Transferable t) {
        if (t.isDataFlavorSupported(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR)) {
            try {
                Collection sources = (Collection) t.getTransferData(OWLObjectDataFlavor.OWL_OBJECT_DATA_FLAVOR);
                Set rootClses = new HashSet();
                for (Object obj : sources) {
                    if (obj instanceof OWLClass)
                        rootClses.add((OWLClass) obj);
                }
                if (!rootClses.isEmpty()) {
                    try {
                        if (project != null) {
	                        ProtegeDataBean dataBean = (ProtegeDataBean) project.getBean(ShrimpProject.DATA_BEAN);
	                        dataBean.setRootClses(rootClses);

	                        if (comp instanceof JButton) {
	                        	// this will cause the quick view action to run
	                            ((JButton)comp).doClick();
	                        }
                        }
                    } catch (BeanNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
      //always return false so that no data gets "moved" from Proteges
        return false;
    }

}
