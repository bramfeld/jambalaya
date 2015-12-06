/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean;

import java.awt.Color;
import java.awt.geom.Point2D;

import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;

/**
 * The points at which arcs attach to nodes.
 * An arc will attach to one terminal at each end.
 * A node can have 0 to many terminals.
 * A terminal should be attached to a node.
 * A terminal will be attached to 0 or more arcs.
 * 
 * @author Rob Lintern
 *
 */
public interface ShrimpTerminal extends ShrimpDisplayObject, ShrimpDisplayObjectListener {
	
	/** constant that represents an error or fault terminal */
	public final static String FAULT = "FaultTerminal";

	/** constant that represents an in terminal */
	public final static String IN = "InTerminal";
	
	/** constant that represents an out terminal */
	public final static String OUT = "OutTerminal";
	
	/** constant that represents a terminal with an unknown type */
	public final static String UNKNOWN_TYPE = "UnknownTerminal";

	/** Includes an angle of an arc in the average angle of all arcs attached to this terminal */
	public void addArcAngle (double newAngle);	
	
	public void attachArc (ShrimpArc arc);
	public void detachArc (ShrimpArc arc);

	/**
	 * Called when multiple arcs attached to this terminal and one of them changes its angle.
	 * Arcs call this to notify the terminal that their angle has changed.
	 * @param oldAngle The old angle of the arc.
	 * @param newAngle The new angle of the arc.
	 */
	public void changeAnArcAngle(final double oldAngle, final double newAngle); //TODO maybe should be elsewhere

	/**
	 * Computes where this terminal should be placed on the edge of its node.
	 * The position is based on the average of the angles of this terminal's attached arcs.
	 */
	public void computeTerminalPosition(); //TODO maybe should be elsewhere

	/** Returns the position of this terminal on its node. */
	public Point2D.Double getArcAttachPoint();
	
	/**
	 * 
	 * @param color
	 */
	public void setColor(Color color);
	
	public Color getColor();
	
	public String getId();

	/** 
	 * Returns the name of this terminal.
	 */
	public String getName();
	
	public ShrimpNode getShrimpNode ();
	
	/** 
	 * Returns the type of this terminal, in, out, or error.
	 */
	public String getType();

	/**
	 * @return true if this terminal has no arcs connected to it; false otherwise
	 */
	public boolean hasNoArcs();
	
	/** Removes an angle of an arc in the average angle of all arcs attached to this terminal */
	public void removeArcAngle (double oldAngle);
	
	/**
	 * 
	 * @param name
	 */
	public void setName(String name);
	
}