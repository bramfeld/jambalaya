/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A terminal is the point at which arcs attach to a node.
 * A terminal can have multiple arcs attached to it. 
 * A terminal can be visible or invisible.
 * 
 * The position of a terminal, on the edge of a node, is based on an "average" of 
 * the angles of the arcs attached to it.
 * 
 * @author Derek Rayside, Rob Lintern
 */
public class PShrimpTerminal extends PPath implements ShrimpTerminal {
	
	/** the default type of terminal is UNKNOWN */
	private final static String DEFAULT_TYPE = UNKNOWN_TYPE;
	
	private final static Color DEFAULT_COLOR = new Color (191,191,159);
	//private final static Color IN_COLOR = DEFAULT_COLOR;
	//private final static Color OUT_COLOR = DEFAULT_COLOR;
	//private final static Color UNKNOWN_COLOR = DEFAULT_COLOR;
	private final static Color FAULT_COLOR = new Color (255,63,63);
	
	protected final static float ABSOLUTE_TERMINAL_WIDTH = 12.0f;
	protected final static float ABSOLUTE_TERMINAL_HEIGHT = ABSOLUTE_TERMINAL_WIDTH*(2.0f/3.0f);
	
	/** The node this terminal belongs to. */
	private ShrimpNode node;
	
	private Color color = DEFAULT_COLOR;
	
	private String name = "";
	
	private boolean show;

	/** Where arcs should attach to this terminal **/
	private final Point2D.Double arcAttachPointGlobal;

	/** The angle of the line from the centre of the node to the terminalAttachPoint. */
	private double terminalPositionAngle = 0.0d;
	
	/** The number of arcs connected to this terminal with a positive angle. */
	private int arcCountPos = 0;

	/** The number of arcs connected to this terminal with a negative angle. */
	private int arcCountNeg = 0;
	
	/** The average centre angle of all arcs with positive angles. */
	private double avgPosAngles = 0.0d;
	
	/** The average centre angle of all arcs with negative angles. */
	private double avgNegAngles = 0.0d;
	
	/** The type of this terminal, in, out, err, or unknown */
	private String type = DEFAULT_TYPE;
	
	/** The id of this terminal **/
	private final String id;
	
	private Vector attachedArcs = new Vector (); 
	
	private DisplayBean displayBean;

	private double currentViewScale = 1.0d;
	
	private static final GeneralPath outTerminalShape = new GeneralPath ();
	private static final GeneralPath inTerminalShape = new GeneralPath ();
	static {
		//create the out shape of this terminal
		float [] xp = new float [6];
		float [] yp = new float [6];
		float half_th = ABSOLUTE_TERMINAL_HEIGHT/2.0f;
		
		
		/*
		 * 
		 * ABSOLUTE_TERMINAL_WIDTH
		 *   |                  |
		 *     
		 *    --------------        -
		 *   |               \ 
		     |                 \        ABSOLUTE_TERMINAL_HEIGHT
		 *   |                 /
		 *   |               /
		 *    --------------        -
		 * 
		 * 
		 */
		xp[0] = 0.0f; 
		yp[0] = 0.0f;
		
		xp[1] = ABSOLUTE_TERMINAL_WIDTH-half_th; 
		yp[1] = 0;
		
		xp[2] = ABSOLUTE_TERMINAL_WIDTH; 
		yp[2] = half_th;
		
		xp[3] = ABSOLUTE_TERMINAL_WIDTH-half_th; 
		yp[3] = ABSOLUTE_TERMINAL_HEIGHT;
		
		xp[4] = 0; 
		yp[4] = ABSOLUTE_TERMINAL_HEIGHT;
		
		//xp[5] = half_th/2.0f; 
		//yp[5] = half_th;

		xp[5] = 0; 
		yp[5] = 0;
		
		outTerminalShape.moveTo(xp[0], yp[0]);
		for (int i = 1; i < xp.length; i++) {
			outTerminalShape.lineTo(xp[i], yp[i]);
		}
	}
	
		
	static {
		//create the in shape of this terminal
		float [] xp = new float [6];
		float [] yp = new float [6];
		float half_th = ABSOLUTE_TERMINAL_HEIGHT/2.0f;
	
	
		/*
		 * 
		 * ABSOLUTE_TERMINAL_WIDTH
		 * |                  |
		 *     
		 *      --------------        -
		 *    /               |  
		 *  /                 |           ABSOLUTE_TERMINAL_HEIGHT
		 *  \                 |    
		 *    \               |   
		 *      --------------        -
		 * 
		 * 
		 */
		 
		xp[0] = 0.0f;
		yp[0] = half_th;
		
		xp[1] = half_th; 
		yp[1] = 0;
	
		xp[2] = ABSOLUTE_TERMINAL_WIDTH; 
		yp[2] = 0;
	
		xp[3] = ABSOLUTE_TERMINAL_WIDTH; 
		yp[3] = ABSOLUTE_TERMINAL_HEIGHT;
	
		xp[4] = half_th; 
		yp[4] = ABSOLUTE_TERMINAL_HEIGHT;
	
		//xp[5] = half_th/2.0f; 
		//yp[5] = half_th;

		xp[5] = 0.0f; 
		yp[5] = half_th;
	
		inTerminalShape.moveTo(xp[0], yp[0]);
		for (int i = 1; i < xp.length; i++) {
			inTerminalShape.lineTo(xp[i], yp[i]);
		}
	}

	
	/**
	 * 
	 * @param displayBean
	 * @param sn
	 */
	public PShrimpTerminal (DisplayBean displayBean, ShrimpNode sn) {
		this (displayBean, sn, "", DEFAULT_TYPE, "", false);
	}
	
	/**
	 * 
	 * @param displayBean
	 * @param sn
	 * @param visible
	 */
	public PShrimpTerminal (DisplayBean displayBean, ShrimpNode sn, boolean visible) {
		this (displayBean, sn, "", DEFAULT_TYPE, "", visible);
	}
	
	/**
	 * 
	 * @param displayBean
	 * @param sn
	 * @param name
	 * @param type
	 * @param id
	 * @param show
	 */
	public PShrimpTerminal(DisplayBean displayBean, ShrimpNode sn, String name, String type, String id, boolean show) {
		super();
		this.displayBean = displayBean;
		this.node = sn;
		this.name = name;
		this.type = type;
		this.id = id;
		
		setPickable(true);
		arcAttachPointGlobal = new Point2D.Double(0,0);
		sn.addTerminal(this);

		this.show = show;
		if (show) {
			if (type.equals(OUT)) {
				setPathTo(outTerminalShape);
			} else if (type.equals(IN)) {
				setPathTo(inTerminalShape);
			} else {
				setPathTo(outTerminalShape);
			}
		
			if (type.equals(FAULT)) {
				setPaint(FAULT_COLOR);
				this.color = FAULT_COLOR;
			} else {
				setPaint(DEFAULT_COLOR);	
				this.color = DEFAULT_COLOR;
			}
		}

		currentViewScale = ((PNestedDisplayBean)PShrimpTerminal.this.displayBean).getPCanvas().getCamera().getViewScale();
		//setStroke (new BasicStroke ((float)(1.0/(PShrimpTerminal.this.currentViewScale*getGlobalScale()))));
		setStroke (null);
		
		/*
		((PNestedDisplayBean)displayBean).getPCanvas().getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener () {
			public void propertyChange(PropertyChangeEvent evt) {
				PShrimpTerminal.this.currentViewScale = ((PNestedDisplayBean)PShrimpTerminal.this.displayBean).getPCanvas().getCamera().getViewScale();
				double scale = PShrimpTerminal.this.getGlobalScale();
				//PShrimpTerminal.this.setStroke (new BasicStroke ((float)(1.0/(PShrimpTerminal.this.currentViewScale*scale))));
				if (PShrimpTerminal.this.show) {
					computeTerminalPosition();
				}
			}
		});	
		*/	
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#dispose()
	 */
	public void dispose() {
		node.removeTerminal (this);
		attachedArcs.clear(); 
		attachedArcs = null;	
		displayBean = null;
	}

		
	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isVisible()
	 */
	public boolean isVisible() {
		return getVisible();
	}
	
	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getGlobalOuterBounds()
	 */	
	public Rectangle2D.Double getGlobalOuterBounds() {
		return getGlobalFullBounds();
	}
	
	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getOuterBounds()
	 */
	public Rectangle2D.Double getOuterBounds() {
		return getBounds();
	}

	/** 
	 * Returns the name of this terminal.
	 */
	public String getName() {
		return name;	
	}
	
	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#getId()
	 */
	public String getId() {
		return id;
	}
	
	/** 
	 * Returns the type of this terminal, in, out, or error.
	 */
	public String getType() {
		return type;	
	}

	/** 
	 * "Attaches" an arc to this terminal 
	 */
	 public void attachArc (ShrimpArc arc) {
		 if (!attachedArcs.contains(arc)){
			 attachedArcs.add(arc);
		 }
	 }
	 
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#detachArc(ca.uvic.csr.shrimp.DisplayBean.ShrimpArc)
	 */
	public void detachArc(ShrimpArc arc) {
		if (attachedArcs != null) {
			attachedArcs.remove(arc);
		}
	}

	 
	 /** 
	 * @see edu.umd.cs.piccolo.nodes.PPath#paint(edu.umd.cs.piccolo.util.PPaintContext)
	 */
	protected void paint(PPaintContext paintContext) {
		double mag = paintContext.getScale();
		super.paint(paintContext);
		paintContext.getGraphics().setPaint(getStrokePaint());
		float strokeWidth = (float)(1.0/mag);
		paintContext.getGraphics().setStroke(new BasicStroke (strokeWidth));
		paintContext.getGraphics().draw(getPathReference());
		
	}
	
    /**
     * 
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#getColor()
     */
	public Color getColor() {
		return color;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#setColor(java.awt.Color)
	 */
	public void setColor(Color color) {
		this.color = color;
		setPaint(color);
	}


	/**
	 * 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener#displayObjectPositionChanged()
     */
    public void displayObjectPositionChanged() {
 		computeTerminalPosition();
	}
	
	/** 
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal#getShrimpNode()
	 */
	public ShrimpNode getShrimpNode() {
		return node;
	}

	/**
	 * Computes where this terminal should be placed on the edge of its node.
	 * The position is based on the average of the angles of this terminal's attached arcs.
	 */
	public void computeTerminalPosition() {
		if (hasNoArcs()) {
			// no arcs connected
			if (show) {
				if (type.equals(IN)) {
					terminalPositionAngle = Math.toRadians(180.01d);
				} else {
					terminalPositionAngle = 0.0d;
				}
			} else {
				return; // dont bother with the terminal position if not shown and has no arcs attached to it
			}
		}

		Rectangle2D.Double nodeGlobalBounds = node.getGlobalOuterBounds();
		
		Point2D.Double terminalAttachPointWRTNodeCenterGlobal = new Point2D.Double ();
		double terminalAngle = node.getTerminalAttachPoint(terminalPositionAngle, terminalAttachPointWRTNodeCenterGlobal);
		
		// find out where this terminal attaches w.r.t the upper left corner of the node
		Point2D.Double terminalAttachPointGlobal = new Point2D.Double ();
		double nodeCenterXGlobal = nodeGlobalBounds.getX() + nodeGlobalBounds.getWidth()/2.0;
		double nodeCenterYGlobal = nodeGlobalBounds.getY() + nodeGlobalBounds.getHeight()/2.0;
		terminalAttachPointGlobal.x = nodeCenterXGlobal + terminalAttachPointWRTNodeCenterGlobal.getX();
		terminalAttachPointGlobal.y = nodeCenterYGlobal + terminalAttachPointWRTNodeCenterGlobal.getY();	
		
		// In order to help with terminals being hidden behind nodes...
		// see if there is one arc attached to this terminal then see if the node
		// on the other end of the arc (the end not attached to this terminal), lets call it "thatNode",
		// is above the node that this terminal is attached to, lets call it "thisNode", and see if thatNode's bounds
		// contain the position of this terminal.
		// If so, then we should flip this terminal to the other side of thisNode,
		// in other words, rotate it 180 degrees around the centre of thisNode.
		if (attachedArcs.size() == 1) {
		    /* TODO finish this
			ShrimpArc arc = (ShrimpArc) attachedArcs.iterator().next();
			ShrimpNode destNode = arc.getDestNode();
			ShrimpNode srcNode = arc.getSrcNode();
			ShrimpNode thisNode = getShrimpNode();
			ShrimpNode thatNode = srcNode.equals(thisNode) ? destNode : srcNode;
			int thatNodeLevel = thatNode.getLevel();
			int thisNodeLevel = thisNode.getLevel();
			int thatNodePos = 0;
			int thisNodePos = 0;
			if (thatNodeLevel == thisNodeLevel) {
				
			}
			if (thatNode.getGlobalOuterBounds().contains(terminalAttachPointGlobal)) {
			
			} 
			*/
		}
		

		// if this terminal is not visible the arc will attach at the same point at which the (invisible) terminal attaches
		arcAttachPointGlobal.setLocation(terminalAttachPointGlobal); 

		// if terminal is to be shown we need it to be drawn at the proper location and 
		// with the proper orientation, and make sure that arcs attach to the tip of it
		if (show) {
			// terminals are children of 
			ShrimpNode parentNode = node.getParentShrimpNode();
			
			AffineTransform parentLToGTx = new AffineTransform ();
			AffineTransform parentGtoLTx = new AffineTransform ();
			
			if (parentNode != null) {
				parentLToGTx = ((PShrimpNode)parentNode).getLocalToGlobalTransform(new PAffineTransform ());
				parentGtoLTx = ((PShrimpNode)parentNode).getGlobalToLocalTransform(new PAffineTransform ()); 	
			}
						
			Point2D.Double terminalAttachPointWRTParent = new Point2D.Double ();
			parentGtoLTx.transform(terminalAttachPointGlobal, terminalAttachPointWRTParent);

			double desiredTerminalWidthGlobal = ABSOLUTE_TERMINAL_WIDTH/currentViewScale;
			
			double terminalHeightLocal = getHeight();
			
			double terminalHeightWRTParent = parentLToGTx.getScaleY()*terminalHeightLocal;
			
			double amountToScaleTerminalWRTParent = 1.0/(currentViewScale*parentLToGTx.getScaleX());
			
			/// The location of the upper left corner of this terminal with respect to its parent node
			// we need to shift the location of this terminal up by half its height
			Point2D.Double terminalLocationWRTParent = new Point2D.Double (terminalAttachPointWRTParent.x, terminalAttachPointWRTParent.y - 0.5*terminalHeightWRTParent);
			
			moveToFront();
			AffineTransform newTx = new AffineTransform ();
			newTx.translate (terminalLocationWRTParent.x, terminalLocationWRTParent.y);
			newTx.scale (amountToScaleTerminalWRTParent, amountToScaleTerminalWRTParent);
			newTx.rotate (terminalAngle, 0.0, terminalHeightLocal/2.0);
			setTransform (newTx);	


			// determine where the arc should attach to this terminal
			double arcX = terminalAttachPointGlobal.getX() + Math.cos(terminalAngle) * desiredTerminalWidthGlobal;
			double arcY = terminalAttachPointGlobal.getY() + Math.sin(terminalAngle) * desiredTerminalWidthGlobal;	
			arcAttachPointGlobal.setLocation(arcX, arcY);
			//arcAttachPointGlobal.setLocation(terminalAttachPointGlobal.getX() + desiredTerminalWidthGlobal, terminalAttachPointGlobal.getY());
			//AffineTransform rotateTx = AffineTransform.getRotateInstance(terminalAngle, terminalAttachPointGlobal.getX(), terminalAttachPointGlobal.getY());
			//rotateTx.transform((Point2D.Double)arcAttachPointGlobal.clone(), arcAttachPointGlobal);
		}		
		
		//if there are other arcs attached to this terminal, make sure they get updated as this terminal moves
		//TODO inform other arcs of terminal's movement
		if (attachedArcs.size() > 1) {
			//for (Iterator iter = attachedArcs.iterator(); iter.hasNext();) {
				//ShrimpArc arc = (ShrimpArc) iter.next();
				//arc.nodePositionChanged(node);	
			//} 
		}
	}
	/** Returns the position of this terminal on its node. */
	public Point2D.Double getArcAttachPoint() {
		return arcAttachPointGlobal;
	}
	
	/** Includes an angle of an arc in the average angle of all arcs attached to this terminal */
	public void addArcAngle(double newAngle) {
		if (Double.isNaN(newAngle)) {
			return;
		}
		//make sure that there will not be more arc counts than there are attached arcs
		if (arcCountPos + arcCountNeg + 1 > attachedArcs.size()) {
			return;
		}
		// assimilate newAngle
		if (newAngle > 0) {
			avgPosAngles = ((avgPosAngles * arcCountPos) + newAngle) / (arcCountPos+1);
			arcCountPos++;
		} else {
			avgNegAngles = ((avgNegAngles * arcCountNeg) + newAngle) / (arcCountNeg+1);
			arcCountNeg++;
		}		
	}	
	
	/** Removes an angle of an arc in the average angle of all arcs attached to this terminal */
	public void removeArcAngle(double oldAngle) {
		if (Double.isNaN(oldAngle)) {
			return;
		}
		if (oldAngle > 0) {
			switch (arcCountPos) {
				case 0:  // this shouldn't happen
				case 1:  // necessary to avoid divide by zero
					avgPosAngles = 0.0d;
					arcCountPos = 0;
					break;
				default:
					avgPosAngles = ((avgPosAngles * arcCountPos) - oldAngle) / (arcCountPos-1);
					arcCountPos--;
					break;
			} // end switch
		} else {
			switch (arcCountNeg) {
				case 0:  // this shouldn't happen
				case 1:  // necessary to avoid divide by zero
					avgNegAngles = 0.0d;
					arcCountNeg = 0;
					break;
				default:
					avgNegAngles = ((avgNegAngles * arcCountNeg) - oldAngle) / (arcCountNeg-1);
					arcCountNeg--;
					break;
			} // end switch
		} // end else
	}
	
	/**
	 * Called when multiple arcs attached to this terminal and one of them changes its angle.
	 * Arcs call this to notify the terminal that their angle has changed.
	 * @param oldAngle The old angle of the arc.
	 * @param newAngle The new angle of the arc.
	 */
	public void changeAnArcAngle(final double oldAngle, final double newAngle) {
		// adjust the averages
		removeArcAngle(oldAngle);
		addArcAngle(newAngle);

		// check if there are only positive or only negative arcs
		if (arcCountPos == 0) {
			if (arcCountNeg == 0) {
				// no arcs
				terminalPositionAngle = 0.0d;
			} else {
				// only negative arcs
				terminalPositionAngle = avgNegAngles;
			}
		} else if (arcCountNeg == 0) {
			// only positive arcs
			terminalPositionAngle = avgPosAngles;
		} else {
			
			// both positive and negative arcs
	
			// determine the terminalPositionAngle from the two averages
			// first, find the shortest difference
			// difference crossing 0
			double diff = avgPosAngles - avgNegAngles;
			// if that's not the shortest, use the difference crossing PI/NEG_PI
			if (diff > DisplayConstants.PI) {
				// hard case: diff crossing PI/NEG_PI is shortest
				diff = DisplayConstants.TWO_PI - diff;
				// pro-rate diff 
				diff = diff * ((double)arcCountNeg / (double)(arcCountPos+arcCountNeg));
				terminalPositionAngle = avgPosAngles + diff;
				// ensure terminalPositionAngle is in the range NEG_PI .. PI
				if (terminalPositionAngle > DisplayConstants.PI) terminalPositionAngle -= DisplayConstants.TWO_PI;
			} else {
				// simple case: diff crossing 0 is shortest
				// find the average of all arcs
				terminalPositionAngle = (avgNegAngles*arcCountNeg + avgPosAngles*arcCountPos) / (arcCountNeg+arcCountNeg);
			}
		} // end else			
	}

	/**
	 * @return true if this terminal has no arcs connected to it; false otherwise
	 */
	public boolean hasNoArcs() {
		return (arcCountPos==0 && arcCountNeg==0);
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#addShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
        // implement PShrimpTerminal.addShrimpDisplayObjectListener      
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#removeShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
        // implement PShrimpTerminal.removeShrimpDisplayObjectListener      
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isInDisplay()
     */
    public boolean isInDisplay() {
        return getParent() != null;
    }

}
