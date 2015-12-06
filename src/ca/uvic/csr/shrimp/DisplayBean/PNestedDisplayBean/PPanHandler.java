/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;


import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * NoramlPanHandler supports the basic panning over a nested hierarchy.
 *
 * David Perrin Oct 12, 2001
 */
public class PPanHandler implements Runnable {
    /**
     * The default panning scale value as a portion of the visible zCanvas.
     */
    public static final double PANNING_SCALE = 0.01f;

    /**
     * Constants for the direction we want to pan.
     */
    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int EAST  = 3;
    public static final int WEST  = 4;
    
    /**
     * The default pause between panning steps
     */
    public static final int DEFAULT_PAN_PAUSE = 0;

	// The amount to pause between panning steps
    public static int panPause = DEFAULT_PAN_PAUSE;
    
    // The amount to pan by.
    private double incrementNS;
    private double incrementEW;

    // True when event handlers are attached to a node.
    private boolean active = false;

    // The camera we are panning within.
    private PCamera camera = null;

    // The node the event has been pressed on
    //private ShrimpDisplayObject pathNode = null;

    // Event coords of mouse press (in object space)
    private Point2D pressObjPt;

    // True while panning 
    private boolean panning = false;
    
	// The display bean this zoom handler works inside.
	private DisplayBean displayBean;

    private int direction;
    
	/** The previous state of the tooltip */
	private boolean toolTipStateBeforeAction;

    /**
     * Constructs a new NormalPanHandler.
     */
    public PPanHandler(DisplayBean displayBean) {
		pressObjPt = new Point2D.Double();
		this.displayBean = displayBean;
		// a bit of a hack here
		if (displayBean instanceof PNestedDisplayBean) {
			camera = ((PNestedDisplayBean)displayBean).getPCanvas().getCamera();
		}
    }
    
    /**
     * Sets this event handler active or not.
     * @param active <code>true</code> to make this event handler active.
     */
    public void setActive(boolean active) {
		if (this.active && !active) {
		    // Turn off event handlers.
		    this.active = false;
		} else if (!this.active && active) {
		    // Turn on event handlers.
		    this.active = true;
		}
    }

    public boolean isActive(){
		return active;
    }

    /**
     *  Start panning
     */
    public void startPanning(ShrimpMouseEvent e, String direction){
		displayBean.setInteracting(true);

		//Set the panning rate
		Rectangle2D.Double rect = (Rectangle2D.Double)displayBean.getScreenCoordinates();
		incrementNS = PANNING_SCALE * rect.height;
		incrementEW = PANNING_SCALE * rect.width;

		pressObjPt.setLocation(e.getX(), e.getY());
		camera.viewToLocal(pressObjPt);
		camera.localToGlobal(pressObjPt);

		toolTipStateBeforeAction = displayBean.isToolTipEnabled();
		displayBean.setToolTipEnabled(false);
		
		if ( direction.equalsIgnoreCase("north")) {
			startPanning(NORTH);
		} else if ( direction.equalsIgnoreCase("south")) {
			startPanning(SOUTH);
		} else if ( direction.equalsIgnoreCase("east")) {
			startPanning(EAST);
		} else if (direction.equalsIgnoreCase("west")) {
			startPanning(WEST);
		}
    }

    /**
     *  Stop panning
     */
    public void stopPanning(ShrimpMouseEvent e){
		stopPanning();
		displayBean.setInteracting(false);

		displayBean.setToolTipEnabled (toolTipStateBeforeAction);
    }

    /**
     * Start animated panning.
     */
    public void startPanning(int direction) {
		panning = true;
		this.direction = direction;
		panOneStep();
    }

    /**
     * Stop animated panning.
     */
    public void stopPanning() {
		panning = false;
    }

    /**
     * Set the pan speed.
     * @param pause The amount to pause between pan steps. Should be between 0(fast) and 100(slow).
     */
    public static void setPanSpeed(int pause) {
	    if (pause < 0) 
	    	panPause = 0;
	    else if (pause > 100)
	    	panPause = 100;
	    else
			panPause = pause;
    }

    /**
     * Get the pan speed.
     * @return The pan speed.
     */
    public static int getPanSpeed() {
		return panPause;
    }

    /**
     * Do one basic panning step and schedule the next panning step.
     */
    private void panOneStep() {
		if (panning) {
			long startTime = System.currentTimeMillis();
			
			// @tag Shrimp.Bugs_Fixed.Panning : panning causes parts of graph to be cutoff
			// Calling camera.translate(x,y) causes the visible nodes to be cutoff
			// changed to translateView(x,y) like Piccolo's PPanEventHandler class does
			PDimension delta = new PDimension(0, 0);
			
		    // Decide which way to pan
		    if (direction == NORTH) {
		    	//camera.translate( 0, -incrementNS );	// causes nodes to be cutoff...
		    	delta.setSize(0, -incrementNS);
		    } else if (direction == SOUTH){
		    	//camera.translate( 0, incrementNS );
		    	delta.setSize(0, incrementNS);
		    } else if (direction == EAST){
		    	//camera.translate( incrementEW, 0 );
		    	delta.setSize(incrementEW, 0);
		    } else if (direction == WEST){
		    	//camera.translate( -incrementEW, 0 );
		    	delta.setSize(-incrementEW, 0);
		    }
		    
		    camera.localToView(delta);
		    if ((delta.width != 0) || (delta.height != 0)) {
		    	camera.translateView(delta.width, delta.height);
		    }
            
            long finishTime = System.currentTimeMillis();
            long sleepTime = panPause - (finishTime - startTime);
            // don't want to pan too fast, so take a little nap if needed 
            if (sleepTime > 0) {         
	            try {
	            	Thread.sleep(sleepTime);
	            } catch (Exception e) {
	            	e.printStackTrace();
	            }
	        }
			SwingUtilities.invokeLater(this);	// calls the run method
		}
    }

    public void run() {
		PPanHandler.this.panOneStep();
    }
        
}
