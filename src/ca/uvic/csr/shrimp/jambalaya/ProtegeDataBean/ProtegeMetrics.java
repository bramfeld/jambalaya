/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.net.URI;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.protege.editor.owl.model.OWLModelManagerImpl;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.jambalaya.MetricsThread;

/**
 * The main metrics class.  All calls to generate metrics should go
 * through here.  A thread is created on project load which generates metrics
 * if they aren't saved to a file.  Otherwise, metrics should 
 * 
 * @author Rob Lintern
 * @author Neil Ernst (Strahler metrics and threading)
 */
public class ProtegeMetrics {
	
	public static final int NUM_METRICS = 4;
	public static final int NUM_INSTANCES = 0;
	public static final int NUM_INHERITORS = 1;
	public static final int TREE_DEPTH = 2;
	private static final int STRAHLER = 3;
	
	protected OWLOntology project;
	private Map clsToMetricResultMap;
	
	public ProtegeMetrics(OWLOntology o) {
		project = o;
		clsToMetricResultMap = new HashMap();
	}
	
	public void clear() {
		clsToMetricResultMap.clear();
	}
	
	public void calculateMetrics(OWLEntity e) {
	    //long start = System.currentTimeMillis();
		calculateMetricsRecursive(e);
		//long end = System.currentTimeMillis();
	    //System.out.println("calculating metrics for: " + f.getBrowserText() + ": " + (end - start));
	}
	
	private void calculateMetricsRecursive(OWLEntity e) {
		if (!(e instanceof OWLClass)) {
			return;
		}
		
		OWLClass currentCls = (OWLClass) e;
		MetricsResult currentClsResult = (MetricsResult) clsToMetricResultMap.get(currentCls);
		if (currentClsResult != null) {
			return; // this cls has already been visited
		}
		
		//otherwise set it as visited and calculate (must be done pre-order for cycles)
		currentClsResult = new MetricsResult();
		clsToMetricResultMap.put(currentCls, currentClsResult);
		
		int maxSubTreeDepth = 0;
		int sumNumSubClsInstances = 0;
		int sumNumSubClsInheritors = 0;
		Set<OWLClassExpression> directSubClasses = currentCls.getSubClasses(project);
		int cnt = 0;
		for (OWLClassExpression exp : directSubClasses) {
			if (! exp.isAnonymous()) {
				OWLClass directSubCls = exp.asOWLClass();
				// 1. find maximum tree depth
				int subTreeDepth = getMetricValue(directSubCls, TREE_DEPTH);
				if (subTreeDepth > maxSubTreeDepth)
					maxSubTreeDepth = subTreeDepth;			
				// 2. calc number of subcls instances
				sumNumSubClsInstances += getMetricValue(directSubCls, NUM_INSTANCES);
				
				// 3. calc number of subcls inheritors
				sumNumSubClsInheritors += getMetricValue(directSubCls, NUM_INHERITORS);
				cnt++;
			}
		}
		int treeDepth = maxSubTreeDepth + 1; // the tree depth of this cls is the maximum tree depth of its children plus one	
		int numInstances = sumNumSubClsInstances + currentCls.getIndividuals(project).size(); // the number of instances of this cls, is the number of its own instances plus the sum of the number of instances of all its children	
		int numInheritors = sumNumSubClsInheritors + cnt; // the number of inheritors of this cls, is the number of its own inheritors plus the sum of the number of inheritors of all its childre
		
		currentClsResult.setMetricValue(NUM_INSTANCES, numInstances);
		currentClsResult.setMetricValue(NUM_INHERITORS, numInheritors);
		currentClsResult.setMetricValue(TREE_DEPTH, treeDepth);
		//indicate that node was visited, but need to calc strahler
		currentClsResult.setMetricValue(STRAHLER, -99); 
		currentClsResult.setFrameName(currentCls.toStringID());
		
		clsToMetricResultMap.put(currentCls, currentClsResult);
	}
	
	private int getMetricValue(OWLClass cls, int metric) {
		int result = 0;
		MetricsResult mr = (MetricsResult) clsToMetricResultMap.get(cls);
		if (mr == null) {
			calculateMetrics(cls);
			mr = (MetricsResult) clsToMetricResultMap.get(cls);
		}
		if (mr != null) {
			result = mr.getMetricValue(metric);
		}
		return result;
	}
		
	public int getNumInstances(OWLEntity e) {
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			return getMetricValue(cls, NUM_INSTANCES);
	    }
		return 0;
	}
	
	public int getNumInheritors(OWLEntity e) {
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			return getMetricValue(cls, NUM_INHERITORS);
	    }
		return 0;
	}
	
	public int getTreeDepth(OWLEntity e) {
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			return getMetricValue(cls, TREE_DEPTH);
	    }
		return 0;
	}
	
	/**
	 * A Strahler metric is "... a good measure of the branching structure of hierarchical networks
	 * (trees)." (Herman, I. and Marshall, M.S. and Melan?on, G. and Duke, D.J. and Delest, M. and Domenger, J.-P. 	
	 * Skeletal images as visual cues in graph visualization, Proceedings of the Joint Eurographics - 
	 * IEEE TCCG Symposium on Visualization, Springer Verlag, 1999)
	 * Essentially it is calculated as 0, if the node is a leaf;
	 * 									the max of the children's Strahler numbers + num Children - 1 if all equal
	 * 									the max of children's Strahler #s + num children - 2 otherwise
	 * You can also add a weight factor.  In our case the weight may be defined as say size of node e.g. LOC or number of 
	 * properties.
	 * @param f - the Protege frame to calculate from.
	 * @return the metric value for this frame.
	 */
	public int getStrahler(OWLEntity e) {
		if (e instanceof OWLClass) {
			OWLClass cls = (OWLClass) e;
			return getStrahlerRecursive(cls);
	    }
	    //System.out.println("was instance");
		return 0;
	}
	
	/**
	 * @see ProtegeMetrics#getStrahler(Frame)
	 */
	private int getStrahlerRecursive(OWLClass cls) {
		//the weight is = number of direct slots you have - 'interestingness' metric
		//int weight = cls.getDirectTemplateSlots().size();
		int weight = 0;
		int sumStrahlerNum = 0;
		int strahlerConst = 1;
		int max = 0;
		int changeCount = 0;
		Set<OWLClassExpression> directSubClasses = cls.getSubClasses(project);
		// Base cases - already visited or leaf.  Should always have a MetricsResult obj 
		// since other metrics are called earlier
		MetricsResult currentClsResult = (MetricsResult) clsToMetricResultMap.get(cls);
		if (currentClsResult == null) {
			return 0;
		}
		
		if(currentClsResult.getMetricValue(STRAHLER) != -99) {
			return currentClsResult.getMetricValue(STRAHLER);
		}
		if(directSubClasses.size() == 0) {
			int value = weight + cls.getIndividuals(project).size() - 1;
			if (value < 1) value = 0;
			return value;
		}
		
		for (OWLClassExpression exp : directSubClasses) {
			if (! exp.isAnonymous()) {
				OWLClass directSubCls = exp.asOWLClass();
				//4. calc Strahler metric
				currentClsResult.setMetricValue(STRAHLER, sumStrahlerNum);
				clsToMetricResultMap.put(cls, currentClsResult);
				sumStrahlerNum = getStrahlerRecursive(directSubCls);
				//always need the max
				if(sumStrahlerNum > max) {
					changeCount++; //we've made x changes
					max = sumStrahlerNum;
				}
				//set to num children + max Strahler -2 if any difference
				if(changeCount > 0 && directSubClasses.size() > 1)
					strahlerConst = 2;
				sumStrahlerNum = directSubClasses.size() + max - strahlerConst;
				currentClsResult.setMetricValue(STRAHLER, sumStrahlerNum);
				clsToMetricResultMap.put(cls, currentClsResult);
			}
		}
		return sumStrahlerNum;
	}

	public void printMetrics(OWLOntology o) {
		project = o;
		for (OWLClass cls : project.getClassesInSignature()) {
			printMetricsRecursive(cls, new ArrayList ());
		}
	}
	
	private void printMetricsRecursive(OWLClass cls, Collection seenClses) {
		if (seenClses.contains(cls)) {
			return;
		}
		
		seenClses.add(cls);
		System.out.println("\nCls: " + cls.toStringID());
		//System.out.println("\tRank: " + getRank(cls));
		System.out.println("\tNum Instances: " + getNumInstances(cls));
		System.out.println("\tNum Inheritors: " + getNumInheritors(cls));
		System.out.println("\tTree Depth: " + getTreeDepth(cls));
		System.out.println("\tStrahler: " + getStrahler(cls));
		Set<OWLClassExpression> subClasses = cls.getSubClasses(project);
		for (OWLClassExpression exp : subClasses) {
			if (! exp.isAnonymous()) {
				OWLClass subCls = exp.asOWLClass();
				printMetricsRecursive(subCls, seenClses);
			}	
		}	
	}
	
	/**
	 * Starts a low priority thread to calculate the metrics
	 */
	public void startThread(ShrimpProject activeProject) {
		new MetricsThread("collect metrics", activeProject).run();
	}
	
	/**
	 * @param f file to load from
	 */
	public void loadMetricsFromFile(File f) {
		clear();  //purge existing metrics
		FileInputStream fis = null;
		try {
			long millis = System.currentTimeMillis();
			fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ArrayList al = (ArrayList) ois.readObject();
			Iterator it = al.iterator();
			while(it.hasNext()) {
				MetricsResult m = (MetricsResult) it.next();
				clsToMetricResultMap.put(m.getFrameName(), m);
			}
			millis -= System.currentTimeMillis();
			System.out.println("Loaded Metrics from file in " + millis + " ms");
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Save the metrics for this project to a file
	 * @param f the filename
	 */
	public void saveMetricsToFile(File f){
		try {
			long millis = System.currentTimeMillis();
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			Iterator it = clsToMetricResultMap.keySet().iterator();
			ArrayList al = new ArrayList();
			while(it.hasNext()) {
				MetricsResult m = (MetricsResult) clsToMetricResultMap.get(it.next());
				al.add(m);
			}
			oos.writeObject(al);
			System.out.println("Saved metrics to file in " + (System.currentTimeMillis() - millis) + " ms");
			oos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String fileName = "C:\\Program Files\\Protege_3.2.1\\examples\\newspaper\\newspaper.pprj";
		OWLModelManagerImpl mm = new OWLModelManagerImpl();
		if (mm != null && mm.loadOntologyFromPhysicalURI(URI.create(fileName))) {
			OWLOntology prj = mm.getActiveOntology();
			ProtegeMetrics metrics = new ProtegeMetrics(prj);
			metrics.printMetrics(prj);	
		}
	}
}
