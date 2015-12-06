/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.ShrimpLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyListener;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener;
import edu.umd.cs.piccolo.PInputManager;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Rob Lintern
 *
 */
public class PShrimpDisplayInputManager extends PInputManager implements ShrimpDisplayInputManager {

	private PNestedDisplayBean pNestedDisplayBean;
	private boolean active;
	private Vector shrimpKeyListeners;
	private Vector shrimpMouseListeners;
	private AWTEventListener awtEventListener;
	
	private ShrimpMouseEvent latestMouseEvent;
	private ShrimpMouseEvent previousMouseEvent;
	private Point2D lastKnownMousePosition;
	private Object lastKnownTargetObject;
	
	/* we need to save the last mouse event while inactive,
	 * so that we know that the display has been clicked on to bring it back into focus.
	 */
	//private PInputEvent latestMousePressedEventWhileInactive;

	public PShrimpDisplayInputManager(PNestedDisplayBean pNestedDisplayBean) {
		this.pNestedDisplayBean = pNestedDisplayBean;
		active = false;
		shrimpMouseListeners = new Vector();
		shrimpKeyListeners = new Vector();
		lastKnownMousePosition = new Point2D.Double(0, 0);
			
		//Add the keyboard listener to always capture the Ctrl, Shift, and Alt keys
		awtEventListener = new AWTEventListener() {
			public void eventDispatched(AWTEvent e) {
				if(e instanceof KeyEvent) {
					KeyEvent ke = (KeyEvent) e;
					if (!handleAnyKeyEvent(ke)) {
						return;
					}
					if (e.getID() == KeyEvent.KEY_PRESSED) {
						for (Iterator iter = shrimpKeyListeners.iterator(); iter.hasNext();) {
							ShrimpKeyListener skl = (ShrimpKeyListener) iter.next();
							skl.keyPressed(createShrimpKeyEvent(ke));
						}
					} else if (e.getID() == KeyEvent.KEY_RELEASED) {
						for (Iterator iter = shrimpKeyListeners.iterator(); iter.hasNext();) {
							ShrimpKeyListener skl = (ShrimpKeyListener) iter.next();
							skl.keyReleased(createShrimpKeyEvent(ke));
						}
					}
				}
			}
		};
		pNestedDisplayBean.getPCanvas().addInputEventListener(this);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#setActive(boolean)
	 */
	public void setActive(boolean activate) {
        if (activate && !active) {
    		try {
    		    pNestedDisplayBean.getPCanvas().getToolkit().addAWTEventListener(awtEventListener, AWTEvent.KEY_EVENT_MASK);
            } catch (SecurityException e) { //thrown when shrimp is an applet
              //  e.printStackTrace();
            }
        	// makes sure the key listeners are not holding out of date info about keys pressed after display comes back into have focues
        	for (Iterator iter = shrimpKeyListeners.iterator(); iter.hasNext();) {
        		ShrimpKeyListener skl = (ShrimpKeyListener) iter.next();
        		skl.resetKeys();
        	}
        	active = activate;
        } else if (!activate && active) {
    		try {
        		// we dont want to listen to any key events when inactive
        		pNestedDisplayBean.getPCanvas().getToolkit().removeAWTEventListener(awtEventListener);
            } catch (SecurityException e) { //thrown when shrimp is an applet
               // e.printStackTrace();
            }
        	active = activate;
        }
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#isActive()
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#getLatestMouseEvent()
	 */
	public ShrimpMouseEvent getLatestMouseEvent() {
		return latestMouseEvent;
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#getPreviousMouseEvent()
	 */
	public ShrimpMouseEvent getPreviousMouseEvent() {
		return previousMouseEvent;
	}

	private ShrimpMouseEvent createShrimpMouseEvent(PInputEvent e) {
		Object targetObj = e.getPickedNode();
		if (targetObj instanceof PShrimpNodeLabel) {
			targetObj = ((ShrimpLabel)targetObj).getLabeledObject();
		} else if (targetObj instanceof PShrimpNodeBoundsHandle) {
		    targetObj = ((PShrimpNodeBoundsHandle)targetObj).getPShrimpNode();
		}

		// @tag Shrimp.Piccolo.Determinant0 : this lines spits out a NonInvertableTransformError sometimes
		Point2D position = e.getPosition();
		ShrimpMouseEvent sme = null;
		if (position != null) {
			// @tag Shrimp.MouseWheel
			int wheelRotation = (e.isMouseWheelEvent() ? e.getWheelRotation() : 0);
			sme = new ShrimpMouseEvent(targetObj, position.getX(), position.getY(), e.getModifiers(), e.getClickCount(), wheelRotation);
		}
		return sme;
	}
	
	
	/* Listens for a mouse pressed event*/
	public void mousePressed(PInputEvent e){
		//if (!active) {
			//latestMousePressedEventWhileInactive = e;
		//}
		if (!handleAnyMouseEvent(e, false)) {
			return;
		}
		
		for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
			ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
			sml.mousePressed(latestMouseEvent);
		}
	}
    
	 /* Listens for a mouse dragged event*/
	public void mouseDragged(PInputEvent e){
		if (!handleAnyMouseEvent(e, false)) {
			return;
		}
		for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
			ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
			sml.mouseDragged(latestMouseEvent);
		}
	}

	 /* Listens for a mouse released event*/
	public void mouseReleased(PInputEvent e){
		if (!handleAnyMouseEvent(e, false)) {
			return;
		}
		for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
			ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
			sml.mouseReleased(latestMouseEvent);
		}
	}

	 /* Listens for a mouse clicked event*/
	public void mouseClicked(PInputEvent e) {
		if (handleAnyMouseEvent(e, false)) {
			for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
				ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
				sml.mouseClicked(latestMouseEvent);
			}
		}
	}

	 /* Listens for a mouse moved event*/
	public void mouseMoved(PInputEvent e) {
		if (!active) {
			return;
		}
		Point2D position = e.getPosition();
		if (position != null) {
			lastKnownMousePosition = new Point2D.Double(position.getX(), position.getY());
		}
		if (handleAnyMouseEvent(e, true)) {
			for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
				ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
				sml.mouseMoved(latestMouseEvent);
			}
		}
	}
	
	/**
	 * @see edu.umd.cs.piccolo.PInputManager#mouseEntered(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void mouseEntered(PInputEvent event) {
		if (!active) {
			return;
		}
		super.mouseEntered(event);
	}
	
	/**
	 * @see edu.umd.cs.piccolo.PInputManager#mouseExited(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void mouseExited(PInputEvent event) {
		if (!active) {
			return;
		}
		super.mouseExited(event);
	}
	
	public void mouseWheelRotated(PInputEvent event) {
		if (handleAnyMouseEvent(event, false)) {
			for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
				ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
				sml.mouseWheelMoved(latestMouseEvent);
			}
		}
		super.mouseWheelRotated(event);
	}
	
	public void mouseWheelRotatedByBlock(PInputEvent event) {
		if (handleAnyMouseEvent(event, false)) {
			for (Iterator iter = shrimpMouseListeners.iterator(); iter.hasNext();) {
				ShrimpMouseListener sml = (ShrimpMouseListener) iter.next();
				sml.mouseWheelMoved(latestMouseEvent);
			}
		}
		super.mouseWheelRotatedByBlock(event);
	}
	
	private boolean handleAnyMouseEvent(PInputEvent e, boolean mouseMoved) {
		Object targetObj = e.getPickedNode();
		if (targetObj == null) {
			return false;
		}
		if (targetObj instanceof ShrimpLabel) {
			targetObj = ((ShrimpLabel)targetObj).getLabeledObject();
		} else if (targetObj instanceof PShrimpNodeBoundsHandle) {
		    targetObj = ((PShrimpNodeBoundsHandle)targetObj).getPShrimpNode();
		}
		lastKnownTargetObject = targetObj;
		
		if (targetObj instanceof ShrimpNode) {
			ShrimpNode node = (ShrimpNode) targetObj;
			// don't allow the event if the target is a node showing a custom panel
            if (node.isCustomPanelShowing()) {
                return false;
            }
		}
		previousMouseEvent = latestMouseEvent;
		latestMouseEvent = createShrimpMouseEvent(e);
		return true;
	}
	
	/**
	 * @see edu.umd.cs.piccolo.PInputManager#keyPressed(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyPressed(PInputEvent e) {
		//taken care of by awtEventListener
	}
	
	/**
	 * @see edu.umd.cs.piccolo.PInputManager#keyReleased(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyReleased(PInputEvent e) {
		//taken care of by awtEventListener
	}

	/**
	 * @see edu.umd.cs.piccolo.PInputManager#keyTyped(edu.umd.cs.piccolo.event.PInputEvent)
	 */
	public void keyTyped(PInputEvent e) {
		//taken care of by awtEventListener
	}
	
	public boolean handleAnyKeyEvent(KeyEvent e) {
		Object targetObj = lastKnownTargetObject;
		int code = e.getKeyCode();
		// always allow ctrl, shift, and alt through
		if (code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_ALT) {
			return true;
		}
		
		// don't allow the event if the target is a focused on node showing a custom panel
		if (targetObj instanceof ShrimpLabel) {
			targetObj = ((ShrimpLabel)targetObj).getLabeledObject();
		}
		if (targetObj instanceof ShrimpNode) {
            ShrimpNode node = (ShrimpNode) targetObj;
            // don't allow the event if the target is a node showing a custom panel
            if (node.isCustomPanelShowing()) {
                return false;
            }
		}
		
		return true;
	}
	
	private ShrimpKeyEvent createShrimpKeyEvent(KeyEvent e) {
		Vector coords = new Vector(2);
		coords.addElement (new Double(lastKnownMousePosition.getX()));
		coords.addElement (new Double(lastKnownMousePosition.getY()));
		ShrimpKeyEvent ske = new ShrimpKeyEvent(lastKnownTargetObject, coords, e.getKeyCode(), e.getModifiers());
		return ske;
	}


	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#addShrimpMouseListener(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener)
	 */
	public void addShrimpMouseListener(ShrimpMouseListener sml) {
		shrimpMouseListeners.addElement(sml);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#removeShrimpMouseListener(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpMouseListener)
	 */
	public void removeShrimpMouseListener(ShrimpMouseListener sml) {
		shrimpMouseListeners.removeElement(sml);
	}

	
	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#dispose()
	 */
	public void dispose() {
		pNestedDisplayBean.getPCanvas().getToolkit().removeAWTEventListener(awtEventListener);
		pNestedDisplayBean.getPCanvas().removeInputEventListener(this);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#addShrimpKeyListener(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyListener)
	 */
	public void addShrimpKeyListener(ShrimpKeyListener skl) {
		shrimpKeyListeners.add(skl);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayInputManager#removeShrimpKeyListener(ca.uvic.csr.shrimp.DisplayBean.event.ShrimpKeyListener)
	 */
	public void removeShrimpKeyListener(ShrimpKeyListener skl) {
		shrimpKeyListeners.remove(skl);
	}

}
