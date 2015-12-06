/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeArtifact;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeDataBean;
import ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean.ProtegeMetrics;

/**
 * This class creates a thread which runs, at low priority, and collects
 * information on 
 * need another thread which listens for Frame changes in Protege and adds them in.
 * 
 * @author Neil Ernst
 */
public class MetricsThread extends Thread {
	
	private ShrimpProject p;
	private ProtegeDataBean pdb;
	private ProtegeMetrics pm;
	private String[] cprels = new String[] {ProtegeDataBean.DIRECT_INSTANCE_SLOT_TYPE, ProtegeDataBean.DIRECT_SUBCLASS_SLOT_TYPE};
	
	public MetricsThread(String name, ShrimpProject p) {
		super(name);
		this.p = p;
	}
	
	public void run() {
		//low priority
		setPriority(2);
		//find the root class and start gathering metrics
		if (p == null) {
			System.out.println("no project");
			return;
		} 
		if (!(p.getApplication() instanceof JambalayaApplication)) {
			System.out.println("must use Jambalaya with this thread");
			return;
		}
		try {
			pdb = (ProtegeDataBean) p.getBean(ShrimpProject.DATA_BEAN);
			pm = pdb.getMetrics();
		} catch (BeanNotFoundException e) {
			e.printStackTrace();
		}
		//note: we assume the serialized metrics objects are consistent with the current state of the db
		//this may not be true if someone has changed the db and not had jambalaya loaded.  
		File metricsFile = getMetricsFile();
		if (metricsFile != null && metricsFile.canRead()) {
			pm.loadMetricsFromFile(metricsFile);
		} else {
			traverseDB();
			if (metricsFile != null) {
			    pm.saveMetricsToFile(metricsFile);
			}
		}
	}

	/**
	 * if no metrics are pre-loaded, find them
	 */
	private void traverseDB() {
		for (Object obj : pdb.getRootArtifacts(cprels)) {
			if (obj instanceof ProtegeArtifact) {
				OWLEntity e = ((ProtegeArtifact) obj).getEntity();
				if (e != null)
					pm.calculateMetrics(e);			
			}
		}
	}

	/**
	 * @return the file to use
	 */
	private File getMetricsFile() {
	    File metricsFile = null;
		URI projectURI = pdb.getProject().getOntologyID().getOntologyIRI().toURI();
		if (projectURI != null) {
			try {
			    URI metricsFileURI = new URI(projectURI.toString() + "/__Jambalaya.metrics");
				metricsFile = new File(metricsFileURI);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
		return metricsFile;
	}
	
}
