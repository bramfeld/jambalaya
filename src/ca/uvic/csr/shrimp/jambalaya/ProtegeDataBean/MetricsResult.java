/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.jambalaya.ProtegeDataBean;

import java.io.Serializable;

/**
 * @author Neil Ernst
 *
 * A set of metrics for a particular protege cls.
 */
public class MetricsResult implements Serializable {

	private int [] metricValues;
	//see edu.stanford.smi.protege.model.FrameID for discussion of what 
	//is a unique id for Protege frames.  IDs change in text files.
	private String frameName;  
	
	public MetricsResult () {
		metricValues = new int [ProtegeMetrics.NUM_METRICS];
	}
	
	public int getMetricValue (int metric) {
		return metricValues[metric];
	}
	
	public void setMetricValue (int metric, int value) {
		metricValues[metric] = value;
	}
	
	public String getFrameName() {
		return frameName;
	}
	
	public void setFrameName(String frameName) {
		this.frameName = frameName;
	}
}
