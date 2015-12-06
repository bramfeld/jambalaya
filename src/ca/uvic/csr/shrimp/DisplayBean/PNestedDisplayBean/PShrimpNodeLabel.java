/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayConstants;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNodeLabel;
import ca.uvic.csr.shrimp.DisplayBean.PFlatDisplayBean.PFlatDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.event.MagnifyEvent;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;
import edu.umd.cs.piccolo.util.PAffineTransform;

/**
 * A fixed-size or scaled (by node size) label for a node.
 * @author Rob Lintern
 * @author Chris Bennett
 */
public class PShrimpNodeLabel extends PShrimpLabel implements ShrimpNodeLabel {

    /**
     * @param displayBean
     * @param node
     * @param font
     * @param text
     */
    public PShrimpNodeLabel(DisplayBean displayBean, ShrimpNode node, Font font, String text) {
        super(displayBean, node, font, text);
        node.addShrimpDisplayObjectListener(this);
		setBackgroundColor(node.getColor());
		displayBean.addNavigationListener(this);
    }

	/* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#dispose()
     */
    public void dispose() {
		displayBean.removeNavigationListener(this);
		displayObject.removeShrimpDisplayObjectListener(this);
		super.dispose();
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#updateVisibility(java.util.Vector)
     */
    public void updateVisibility(Vector currentFocusedOnObjects)  {
		double opacity = calculateOpacity(currentFocusedOnObjects);
    	boolean labeledObjectVisible = displayBean.isVisible(getLabeledObject());
		boolean newVisibility = (getParent() != null) && (opacity > 0 && opacity <= 1.0) &&
						labeledObjectVisible && isInAreaOfInterest(currentFocusedOnObjects);

		if (newVisibility != isVisible()) {
			setVisible(newVisibility);
			if (newVisibility) {
				if (displayBean.getLabelFadeOut()) {
					setTransparency((float)opacity);
				} else {
					setTransparency(1.0f);
				}
			}
			displayObjectPositionChanged(currentFocusedOnObjects);
		}
	}

    public void displayObjectPositionChanged(Vector currentFocusedOnObjects) {
 		if (!isVisible()) {
			return;
		}

 		setLabelStyle();

		Rectangle2D nodeLocalBounds = ((PShrimpNode)displayObject).localToParent(displayObject.getOuterBounds());
		double labelLocalHeight = getHeight() - PADDING;
		double labelLocalWidth = getWidth();
		AffineTransform nodeLocalToGlobalTx = new AffineTransform();
		PShrimpNode parentNode = (PShrimpNode) ((ShrimpNode)displayObject).getParentShrimpNode();
		if (parentNode != null && !(displayBean instanceof PFlatDisplayBean)) {
			nodeLocalToGlobalTx = parentNode.getLocalToGlobalTransform(new PAffineTransform());
		} else {
			nodeLocalToGlobalTx.setToIdentity();
		}

		double nodeScale = nodeLocalToGlobalTx.getScaleX();

		// Put label on top of the visual node in the middle
		AffineTransform newLabelTx = new AffineTransform();

		String labelMode = displayBean.getDefaultLabelMode();
		double viewScale = canvas.getCamera().getViewTransform().getScale();
		double labelScale = 1.0/(viewScale*nodeScale);
		double levelScale = 1.0;
		if (labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)) {
			// scale to the level of this label's node
			int displayLevel = 1;
			if (currentFocusedOnObjects.size() == 1 && currentFocusedOnObjects.firstElement() instanceof ShrimpNode) {
			    displayLevel = ((ShrimpNode)currentFocusedOnObjects.firstElement()).getLevel();
			}
			int thisLevel = ((ShrimpNode)displayObject).getLevel();
			int maxSize = ((Font)displayBean.getLabelFont()).getSize();
			// Decrease label by 20% for each level away from current,
			// but not less than minimum size
			double newSize = Math.max(DisplayConstants.MIN_FONT_SIZE, (maxSize * Math.pow(0.80, thisLevel - displayLevel)));
			levelScale = newSize / maxSize;
		}

		//x, put middle of label in middle of node
		double labelXWRTParent = nodeLocalBounds.getX() + nodeLocalBounds.getWidth()/2.0 - (labelLocalWidth/2.0 - PADDING)*levelScale*labelScale ;
		// y, put bottom of label on top of node
		double labelYWRTParent = nodeLocalBounds.getY() - labelLocalHeight*levelScale*labelScale;

		newLabelTx.translate(labelXWRTParent, labelYWRTParent);
		newLabelTx.scale (labelScale*levelScale, labelScale*levelScale);
		setTransform(newLabelTx);
	}

    /**
     * Set the text label based on the style
     *
     */
    private void setLabelStyle() {
    	final int CHARS_TO_ELIDE = 30; // TODO - set this based on font or something sane
    	String labelStyle = ((PShrimpNode)displayObject).getLabelStyle();
    	if (labelStyle.equals(DisplayConstants.LABEL_STYLE_FULL)) {
    		super.restoreLabel();
    	}
    	else if (labelStyle.equals(DisplayConstants.LABEL_STYLE_ELIDE_LEFT)) {
    		super.elideLeft(CHARS_TO_ELIDE);
    	}
    	else if (labelStyle.equals(DisplayConstants.LABEL_STYLE_ELIDE_RIGHT)) {
    		super.elideRight(CHARS_TO_ELIDE);
    	}
    	else if (labelStyle.equals(DisplayConstants.LABEL_STYLE_HIDE)) {
    		super.hideLabel();
    	}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpLabel#isInAreaOfInterest()
	 * This label is in the area of interest if this label's node is in the
	 * descendents, ancestors, or siblings of the currently focused on node,
	 * or is the currently focused on node
	 */
	protected boolean isInAreaOfInterest(Vector currentFocusedOnObjects) {
	    if (displayBean instanceof PFlatDisplayBean) {
	        return isInAreaOfInterestFlatDisplayBean(currentFocusedOnObjects);
	    }
		boolean isInArea = false;
		if (currentFocusedOnObjects.size() == 1) {
			Object focusedOnObj = currentFocusedOnObjects.firstElement();
			if (focusedOnObj instanceof ShrimpNode) {
				ShrimpNode focusedOnNode = (ShrimpNode) focusedOnObj;
				// is this label's node currently focused on?
				if (focusedOnNode.equals(displayObject)) {
					return true;
				}
				// is this label's node a sibling of the currently focused on node?
				Vector parentNodes = displayBean.getDataDisplayBridge().getParentShrimpNodes(focusedOnNode);
				if (parentNodes.size() == 1) {
					Vector siblings = displayBean.getDataDisplayBridge().getChildNodes((ShrimpNode)parentNodes.firstElement(), false);
					siblings.remove(focusedOnNode);
					if (siblings.contains(displayObject)) {
						return true;
					}
				}
				// is this label's node an ancestor of the currently focused on node?
				Vector ancestorNodes = displayBean.getDataDisplayBridge().getAncestorNodes(focusedOnNode);
				if (ancestorNodes.contains(displayObject)) {
					return true;
				}
				// is this label's node a descendent of the currently focused on node?
				Vector descendentNodes = displayBean.getDataDisplayBridge().getDescendentNodes(focusedOnNode, false);
				if (descendentNodes.contains(displayObject)) {
					return true;
				}
			}
		} else if (currentFocusedOnObjects.isEmpty()) {
			return true;
		}
		return isInArea;
	}

    private boolean isInAreaOfInterestFlatDisplayBean(Vector currentFocusedOnObjects) {
        Vector ancestors = new Vector();
        for (Iterator iter = currentFocusedOnObjects.iterator(); iter.hasNext();) {
            ShrimpNode node = (ShrimpNode) iter.next();
            ancestors.addAll(displayBean.getDataDisplayBridge().getAncestorNodes(node));
            ancestors.add(node);
        }
        boolean inAreaOfInterest = ancestors.contains(getLabeledObject());
        return inAreaOfInterest;
    }

    // below 0 is invisible, above is visible
	protected double calculateOpacity(Vector currentFocusedOnObjects) {
	    if (displayBean instanceof PFlatDisplayBean) {
	        return 1.0;
	    }
		int displayLevel = 1;
		if (currentFocusedOnObjects.size() == 1 && currentFocusedOnObjects.firstElement() instanceof ShrimpNode) {
		    displayLevel = ((ShrimpNode)currentFocusedOnObjects.firstElement()).getLevel();
		}
		int thisLevel = ((ShrimpNode)displayObject).getLevel();
		double labelLevels = displayBean.getLabelLevels();
		double levelDiff = thisLevel - displayLevel;
		double opacity = 0;
		if (labelLevels > 0) {
			opacity = (labelLevels - levelDiff)/labelLevels;
		}
		return opacity;
	}

	/**
	 * Changes the font (if different from the current font) of the label wrapped by this component.
	 * @param font The new font for the label.
	 */
	public void setFont(Font font) {
		if ((getFont() != null) && !getFont().equals(font)) {
		    super.setFont(font);

			// Invoke update later to ensure that the label has repainted with its new font.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					displayObjectPositionChanged();
				}
			});
		}
	}

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#addShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#removeShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
     */
    public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
        // do nothing
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.listener.NavigationListener#beforeMagnify(ca.uvic.csr.shrimp.DisplayBean.event.MagnifyEvent)
     */
    public void beforeMagnify(MagnifyEvent e) {
        Object toObject = e.getToObject();
        Object fromObject = e.getFromObject();
        int fromLevel = -1;
        int toLevel = -1;
        if (fromObject == null) {
            fromLevel = 1;
        } else if (fromObject instanceof ShrimpNode) {
            fromLevel = ((ShrimpNode)fromObject).getLevel();
        } else {
            return;
        }
        if (toObject == null) {
            toLevel = 1;
        } else if (toObject instanceof ShrimpNode) {
            toLevel = ((ShrimpNode)toObject).getLevel();
        } else {
            return;
        }
        if (toLevel >= fromLevel) { // zooming in, or across
            levelChange((ShrimpNode)toObject, fromLevel, toLevel);
        }
    }

    /* (non-Javadoc)
     * @see ca.uvic.csr.shrimp.DisplayBean.listener.NavigationListener#afterMagnify(ca.uvic.csr.shrimp.DisplayBean.event.MagnifyEvent)
     */
    public void afterMagnify(MagnifyEvent e) {
        Object toObject = e.getToObject();
        Object fromObject = e.getFromObject();
        int fromLevel = -1;
        int toLevel = -1;
        if (fromObject == null) {
            fromLevel = 1;
        } else if (fromObject instanceof ShrimpNode) {
            fromLevel = ((ShrimpNode)fromObject).getLevel();
        } else {
            return;
        }
        if (toObject == null) {
            toLevel = 1;
        } else if (toObject instanceof ShrimpNode) {
            toLevel = ((ShrimpNode)toObject).getLevel();
        } else {
            return;
        }
        if (toLevel < fromLevel) { // zooming out
            levelChange((ShrimpNode)toObject, fromLevel, toLevel);
        }
    }

    private void levelChange(ShrimpNode nodeInFocus, int fromLevel, int toLevel) {
        Vector currentFocusedOnObjects = new Vector();
        if (nodeInFocus != null) {
            currentFocusedOnObjects.add(nodeInFocus);
        }

		updateVisibility(currentFocusedOnObjects);
		if (isVisible()) {
			String labelMode = displayBean.getLabelMode(displayObject);
			if (labelMode.equals(DisplayConstants.LABEL_MODE_SCALE_BY_LEVEL)){
				//label needs to be placed again so just update
				displayObjectPositionChanged(currentFocusedOnObjects);
			}
		}
    }
}
