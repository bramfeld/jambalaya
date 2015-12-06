/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.layout;

import java.awt.geom.Rectangle2D;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;

public class ConnectedComponent {
		private Vector nodes; //nodes in this connected component
		private Vector arcs; // arcs in this connected component
		private Rectangle2D.Double bounds;
		
    	
		public ConnectedComponent () {
			nodes = new Vector ();
			arcs = new Vector ();
		}
    	
		public void addNode (ShrimpNode node) {
			if (!nodes.contains(node))
				nodes.add(node);
		}
    	
		public void addArc (ShrimpArc arc) {
			if (!arcs.contains(arc))
				arcs.add(arc);
		}
    	
		public Vector getNodes () {
			return nodes;
		}
    	
		public Vector getArcs () {
			return arcs;
		}
    	
    	public void setBounds(Rectangle2D.Double bounds) {
    		this.bounds = bounds;
    	}
    	public Rectangle2D.Double getBounds () {
    		return bounds;
    	}
    	    	
		public String toString() {
			String s = "Connected Component: #nodes: " + arcs.size() +
						", #arcs: " + arcs.size();
			return s;
		}
		
	}