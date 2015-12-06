/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;

import org.eclipse.mylar.zest.layouts.LayoutBendPoint;
import org.eclipse.mylar.zest.layouts.LayoutEntity;
import org.eclipse.mylar.zest.layouts.constraints.LabelLayoutConstraint;
import org.eclipse.mylar.zest.layouts.constraints.LayoutConstraint;
import org.eclipse.mylar.zest.layouts.dataStructures.BendPoint;

import ca.uvic.cs.seqlayout.algorithms.internal.SequenceEdgeLayoutConstraint;
import ca.uvic.csr.shrimp.AttributeConstants;
import ca.uvic.csr.shrimp.DataBean.Relationship;
import ca.uvic.csr.shrimp.DisplayBean.AbstractDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArc;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpArcLabel;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpTerminal;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.AbstractLineArrowArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArrowHead;
import ca.uvic.csr.shrimp.DisplayBean.ArcStyle.LineArrowArcStyle;
import ca.uvic.csr.shrimp.DisplayBean.event.ShrimpDisplayObjectListener;
import ca.uvic.csr.shrimp.util.GeometryUtils;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * A holder in piccolo for an arc.
 * A ShrimpArc is associated with one relationship in the databean; however
 * one relationship can be represented by several arcs.
 * @author Rob Lintern
 */
public class PShrimpArc extends PNode implements ShrimpArc {

	/**
	 * This value is looked at in the {@link PShrimpArc#getVisible()} method.
	 * If set to true then the getVisible() method will always return true.
	 * This is done to ensure that our {@link PShrimpArc#paint(PPaintContext)} method gets called
	 * when printing the entire canvas to an image (see the ExportToImageFileAction class)
	 */
	public static boolean PRINTING_ENTIRE_CANVAS = false;
	
	/** The default colour for this VisualArc. */
	public static final Color DEFAULT_COLOR = Color.blue;

	private static final int MIN_NODE_DIM_FOR_CONNECTORS = 12;

	/** The style this arc will use to render */
	private ArcStyle arcStyle;

	/** The colour of the arc to be drawn */
	private Color arcColor;

	protected boolean visibilitySetOnce = false;

	/** The style of this arc's arrow head */
	private String arrowHeadStyle = ArrowHead.DEFAULT_STYLE;

	/**
	 * Angle of the line (in radians) between the centre of the source node and the
	 * centre of the target node.
	 */
	private double centreAngle = Double.NaN; // NaN indicates that the centreAngle has not been set yet

	/**
	 * The relationship that this arc represents
	 */
	private Relationship relationship;

	protected ShrimpNode srcNode;
	protected ShrimpNode destNode;

	/** The source terminal that this arc is connected to. */
	private ShrimpTerminal srcTerminal;

	/** The destination terminal that this arc is connected to. */
	private ShrimpTerminal destTerminal;

	protected DisplayBean displayBean;

	protected boolean isInView = true;

	protected boolean hasBendPoints = false;

	private long id;

	private static long nextId = 1;

	/**
	 *
	 * @param displayBean
	 * @param relationship
	 * @param srcNode
	 * @param destNode
	 * @param srcTerminal
	 * @param destTerminal
	 * @param arcStyle
	 * @param curveFactor
	 * @param useArrowHead
	 * @param weight
	 * @param arrowHeadStyle
	 */
	public PShrimpArc(DisplayBean displayBean, Relationship relationship, ShrimpNode srcNode,
			ShrimpNode destNode, ShrimpTerminal srcTerminal, ShrimpTerminal destTerminal, ArcStyle arcStyle,
			int curveFactor, boolean useArrowHead, String arrowHeadStyle, double weight) {
		super();
		id = nextId++;
		this.displayBean = displayBean;
		this.relationship = relationship;
		this.srcNode = srcNode;
		this.destNode = destNode;
		this.arcColor = DEFAULT_COLOR;

		this.srcTerminal = srcTerminal;
		this.destTerminal = destTerminal;

		srcTerminal.attachArc(this);
		destTerminal.attachArc(this);

		srcNode.addShrimpDisplayObjectListener(this);
		destNode.addShrimpDisplayObjectListener(this);

		this.arcStyle = (ArcStyle) arcStyle.clone();
		this.arrowHeadStyle = arrowHeadStyle;
		if (this.arcStyle instanceof LineArrowArcStyle) {
			setUsingArrowHead(useArrowHead, arrowHeadStyle);
		}
		this.arcStyle.setWeight(weight);
		this.arcStyle.setCurveFactor(curveFactor);
		double mag = ((PNestedDisplayBean) this.displayBean).getPCanvas().getCamera().getViewScale();
		if (Double.isNaN(mag) || (Math.abs(mag) < 0.01)) {
			mag = 1.0;
		}
		this.arcStyle.setViewMagnification(mag);
	}

	public void viewTransformChanged() {
		Point2D.Double srcPoint = srcTerminal.getArcAttachPoint();
		Point2D.Double destPoint = destTerminal.getArcAttachPoint();
		Rectangle2D.Double viewBounds = ((PNestedDisplayBean) displayBean).getPCanvas().getCamera().getViewBounds();
		boolean srcIsOnScreen = isOnScreen(srcPoint, viewBounds);
		boolean destIsOnScreen = isOnScreen(destPoint, viewBounds);
		boolean newIsInView = srcIsOnScreen || destIsOnScreen;
		if (newIsInView != isInView) {
			isInView = newIsInView;
			updateVisibility();
		}
		if (isVisible() && newIsInView) {
			double mag = ((PNestedDisplayBean) displayBean).getPCanvas().getCamera().getViewScale();
			arcStyle.setViewMagnification(mag);
			setBounds(arcStyle.getBounds());
		}
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#getId()
	 */
	public long getId() {
		return id;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isVisible()
	 */
	public boolean isVisible() {
		return getVisible();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#setActive(boolean)
	 */
	public void setActive(boolean active) {
		arcStyle.setActive(active);
		setBounds(arcStyle.getBounds());
		ShrimpArcLabel label = displayBean.getDataDisplayBridge().getShrimpArcLabel(this, false);
		if (label != null) {
			label.updateVisibility();
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#setTransparency(float)
	 */
	public void setTransparency(float t) {
		super.setTransparency(t);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#isActive()
	 */
	public boolean isActive() {
		return arcStyle.getActive();
	}
		
	public boolean getVisible() {
		if (PRINTING_ENTIRE_CANVAS) {
			return true;
		}
		return super.getVisible();
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
	 */
	protected void paint(PPaintContext paintContext) {
		renderVisualArc(paintContext);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getGlobalOuterBounds()
	 */
	public Rectangle2D.Double getGlobalOuterBounds() {
		return getGlobalFullBounds();
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#getOuterBounds()
	 */
	public Rectangle2D.Double getOuterBounds() {
		return getBounds();
	}

	/**
	 * @see edu.umd.cs.piccolo.PNode#pickAfterChildren(edu.umd.cs.piccolo.util.PPickPath)
	 */
	protected boolean pickAfterChildren(PPickPath pickPath) {
		if (arcStyle == null) {
			return false;
		}
		if (pickPath == null) {
			return false;
		}
		return arcStyle.intersects(pickPath.getPickBounds());
	}

	/**
	 * Sets this arc to either drawing arrow heads or not.
	 * @param useArrowHeads if true, this arc will be rendered with an arrow head.
	 * @param arrowHeadStyle
	 */
	public void setUsingArrowHead(boolean useArrowHeads, String arrowHeadStyle) {
		if (arcStyle instanceof LineArrowArcStyle) {
			((LineArrowArcStyle) arcStyle).setUseArrowHeads(useArrowHeads, arrowHeadStyle);
		}
		setBounds(arcStyle.getBounds());
	}

	/**
	 * @param curveFactor
	 */
	public void setCurveFactor(int curveFactor) {
		arcStyle.setCurveFactor(curveFactor);
		setBounds(arcStyle.getBounds());
	}

	public int getCurveFactor() {
		return arcStyle.getCurveFactor();
	}

	/** @return the source terminal of this arc */
	public ShrimpTerminal getSrcTerminal() {
		return srcTerminal;
	}

	/** @return the destination terminal of this arc */
	public ShrimpTerminal getDestTerminal() {
		return destTerminal;
	}

	/**
	 * Computes the angle of this arc and notifies the source and destination terminals.
	 */
	private void computeAngle() {
		double oldArcAngle = centreAngle;

		// compute the new angle
		Point2D.Double srcPoint = srcNode.getCentrePoint();
		Point2D.Double destPoint = destNode.getCentrePoint();
		double deltaX = destPoint.getX() - srcPoint.getX();
		double deltaY = destPoint.getY() - srcPoint.getY();
		double newArcAngle = Math.atan2(deltaY, deltaX);
		//System.out.println(this + " newArcAngle: " + Math.toDegrees(newArcAngle));

		if (Double.isNaN(oldArcAngle) || oldArcAngle != newArcAngle) {
			double srcNewAngle = newArcAngle;
			double srcOldAngle = oldArcAngle;

			double destNewAngle = srcNode.equals(destNode) ? newArcAngle : GeometryUtils.rotateByPI(newArcAngle);
			double destOldAngle = srcNode.equals(destNode) ? oldArcAngle : GeometryUtils.rotateByPI(oldArcAngle);

			// update the source terminal
			srcTerminal.changeAnArcAngle(srcOldAngle, srcNewAngle);

			// update the destination terminal
			destTerminal.changeAnArcAngle(destOldAngle, destNewAngle);

			centreAngle = newArcAngle;

		}
	}

	/** Sets whether or not this arc is to be drawn highlighted. */
	public void setHighlighted(boolean highlighted) {
		arcStyle.setHighlighted(highlighted);
		setBounds(arcStyle.getBounds());
	}

	/**
	 * @param weight
	 */
	public void setWeight(double weight) {
		arcStyle.setWeight(weight);
		setBounds(arcStyle.getBounds());
	}

	/**
	 * @return The color of this arc.
	 */
	public Color getColor() {
		return arcColor;
	}

	/**
	 * Set the color of this arc.
	 */
	public void setColor(Color c) {
		arcColor = c;
		setPaint(c);
		repaint();
	}

	/**
	 * @return The style of this arc.
	 */
	public ArcStyle getStyle() {
		return arcStyle;
	}

	/**
	 * Set the style of this arc.
	 */
	public void setStyle(ArcStyle newStyle) {
		ArcStyle oldStyle = arcStyle;
		arcStyle = (ArcStyle) newStyle.clone();
		arcStyle.setCurveFactor(oldStyle.getCurveFactor());
		arcStyle.setSrcDestPoints(oldStyle.getSrcPoint(), oldStyle.getDestPoint());
		arcStyle.setHighlighted(oldStyle.getHighlighted());
		if (newStyle instanceof LineArrowArcStyle && oldStyle instanceof LineArrowArcStyle) {
			((LineArrowArcStyle) arcStyle).setUseArrowHeads(((LineArrowArcStyle) oldStyle).isUsingArrowHeads(), this.arrowHeadStyle);
		}
		arcStyle.setViewMagnification(oldStyle.getViewMagnification());
		arcStyle.setWeight(oldStyle.getWeight());
		arcStyle.setVisible(oldStyle.getVisible());
		setBounds(arcStyle.getBounds());
		repaint();
	}

	/**
	 * Returns the relationship that this arc represents.
	 */
	public Relationship getRelationship() {
		return relationship;
	}

	/**
	 * Returns the relationship type.
	 * @see Relationship#getType()
	 */
	public String getType() {
		return (relationship != null ? relationship.getType() : "");
	}

	/**
	 * Returns the node that is the source (or parent) of this arc.
	 */
	public ShrimpNode getSrcNode() {
		return srcNode;
	}

	/**
	 * Returns the node that is the destination (or child) of this arc.
	 */
	public ShrimpNode getDestNode() {
		return destNode;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("PShrimpArc { ");
		//s.append("relationship: " + (relationship == null ? "null" : relationship.getName()));
		s.append("\"" + srcNode.getName() + "\" -- ");
		s.append(getType());
		s.append(" --> \"" + destNode.getName());
		s.append("\" }");
		return s.toString();
	}

	/**
	 * Two arcs are equal if they represent the same relationship and both go
	 * between the same two nodes.
	 */
	public boolean equals(Object obj) {
		//	    boolean equal = false;
		if (obj instanceof ShrimpArc) {
			//	        ShrimpArc that = (ShrimpArc) obj;
			//	        boolean equalByID = this.getId() == that.getId();
			//	        Relationship thatRel = that.getRelationship();
			//	        Relationship thisRel = this.getRelationship();
			//	        boolean equalByRelAndNodes = this.getSrcNode().equals(that.getSrcNode()) && this.getDestNode().equals(that.getDestNode()) && ((thisRel == null && thatRel == null) || thisRel.equals(thatRel));
			//	        if (equalByRelAndNodes && !equalByID) {
			//	            System.err.println("arcs are equal by rel and nodes, but not by id!");
			//	        }
			//	        equal = equalByID;
			return getId() == ((ShrimpArc) obj).getId();
		}
		//		return equal;
		return false;
	}

	public int hashCode() {
		if (id > Integer.MAX_VALUE) {
			System.err.println("Warning: Converting from long to int when id > Integer.MAX_VALUE");
		}
		return (int) id;
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener#displayObjectPositionChanged()
	 */
	public void displayObjectPositionChanged() {
		if (isVisible()) {
			computeAngle();
			srcTerminal.computeTerminalPosition();
			destTerminal.computeTerminalPosition();
			if (hasBendPoints) {
				PAffineTransform bendPointToGlobalTransform = new PAffineTransform();
				if (srcNode.getParentShrimpNode() == null) { // no parent
					this.getLocalToGlobalTransform(bendPointToGlobalTransform);
				} else { // use parent's coordinate system
					((PShrimpNode) srcNode.getParentShrimpNode()).getLocalToGlobalTransform(bendPointToGlobalTransform);
				}
				double srcX = srcNode.getCentrePoint().x - srcNode.getGlobalOuterBounds().width / 2;
				double srcY = srcNode.getCentrePoint().y - srcNode.getGlobalOuterBounds().height / 2;
				double destX = destNode.getCentrePoint().x - destNode.getGlobalOuterBounds().width / 2;
				double destY = destNode.getCentrePoint().y - destNode.getGlobalOuterBounds().height / 2;
				arcStyle.setSrcDestPoints(new Point2D.Double(srcX, srcY), new Point2D.Double(destX, destY), bendPointToGlobalTransform);
			} else {
				arcStyle.setSrcDestPoints(srcTerminal.getArcAttachPoint(), destTerminal.getArcAttachPoint());
			}
			setBounds(arcStyle.getBounds());
			ShrimpArcLabel label = displayBean.getDataDisplayBridge().getShrimpArcLabel(this, false);
			if (label != null) {
				label.displayObjectPositionChanged();
			}
		}
	}

	public void setVisible(boolean newVisibility) {
		boolean currentVisibility = isVisible();
		boolean changeInVisibility = newVisibility != currentVisibility;
		if (changeInVisibility) {
			super.setVisible(newVisibility);
			arcStyle.setVisible(newVisibility);
			setPickable(newVisibility);

			if (newVisibility) {
				double mag = ((PNestedDisplayBean) displayBean).getPCanvas().getCamera().getViewScale();
				arcStyle.setViewMagnification(mag);
				setBounds(arcStyle.getBounds());
			}

			((AbstractDisplayBean) displayBean).arrangeArcs(getSrcNode(), getDestNode());
		}
		// Need to inform the attached terminals that this arc is changing its visibility,
		// The first time we set an arc visible, we want to make sure that it gets put in the right place.
		if (changeInVisibility || (!visibilitySetOnce && isVisible())) {
			if (isVisible()) {
				srcTerminal.addArcAngle(centreAngle);
				destTerminal.addArcAngle(GeometryUtils.rotateByPI(centreAngle));
				displayObjectPositionChanged(); // make sure that this arc is drawn in the right place
			} else {
				srcTerminal.removeArcAngle(centreAngle);
				destTerminal.removeArcAngle(GeometryUtils.rotateByPI(centreAngle));
			}
			visibilitySetOnce = true;
		}
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#dispose()
	 */
	public void dispose() {
		//System.out.println("disposing: " + this);
		srcNode.removeShrimpDisplayObjectListener(this);
		destNode.removeShrimpDisplayObjectListener(this);
		srcTerminal.detachArc(this);
		destTerminal.detachArc(this);

		arcStyle = null;
		relationship = null;
		srcNode = null;
		destNode = null;
		//commonAncestor = null;
		srcTerminal = null;
		destTerminal = null;
		displayBean = null;
	}

	private static boolean isOnScreen(Point2D.Double point, Rectangle2D.Double viewBounds) {
		return viewBounds.contains(point);
		/*
		 double pX = point.getX();
		 double pY = point.getY();
		 double vx1 = viewBounds.getX(); // upper left corner of view
		 double vy1 = viewBounds.getY(); // upper left corner of view
		 double vw = viewBounds.getWidth(); // view width
		 double vh = viewBounds.getHeight(); // view height
		 double vx2 = vx1 + vw; // lower right corner of view
		 double vy2 = vy1 + vh; // lower right corner of view
		 boolean isOnScreen = ((pX >= vx1 && pX <= vx2)) && ((pY >= vy1 && pY <= vy2));
		 return isOnScreen;
		 */
	}
	
	protected void renderVisualArc(PPaintContext paintContext) {
		if (!isVisible()) {
			System.out.println(this + " not visible");
			return;
		}
		if (srcNode == null || destNode == null) {
			System.out.println(this + " null src or dest");
			return;
		}
		if (!srcNode.isVisible() || !destNode.isVisible()) {
			System.out.println(this + " src or dest not visible");
			return;
		}

		// Ensure we do not render arcs to invisible PShrimpNodes
		try {
			PShrimpNode shrimpSrcNode = (PShrimpNode)srcNode;
			PShrimpNode shrimpDestNode = (PShrimpNode)destNode;
			if (!shrimpSrcNode.shouldRender(paintContext)|| !shrimpDestNode.shouldRender(paintContext)) {
				System.out.println(this + " should not render src or dest");
				return;
			}
		} catch (ClassCastException cse) {
			//ignore
		}

		// see if src and dest nodes are on screen and see how big they actually are on screen
		Point2D.Double srcPoint = srcTerminal.getArcAttachPoint();
		Point2D.Double destPoint = destTerminal.getArcAttachPoint();
		Rectangle2D.Double viewBounds = paintContext.getCamera().getViewBounds();
		boolean srcIsOnScreen = isOnScreen(srcPoint, viewBounds);
		boolean destIsOnScreen = isOnScreen(destPoint, viewBounds);
		if (!srcIsOnScreen && !destIsOnScreen) {
			System.err.println(this + " shouldn't be rendering if src and dest not on screen");
			return; // don't bother rendering this arc if its src and dest are not on screen)
		}

		boolean arrowHeadIsOnScreen = false;
		if (arcStyle instanceof AbstractLineArrowArcStyle) {
			Point2D.Double arrowHeadPoint = ((AbstractLineArrowArcStyle) arcStyle).getArrowHeadPoint();
			arrowHeadIsOnScreen = isOnScreen(arrowHeadPoint, viewBounds);
		}
		double srcWidthOnScreen = srcNode.getOuterBounds().getWidth() * ((AffineTransform) displayBean.getTransformOf(srcNode)).getScaleX()
				* paintContext.getCamera().getViewScale();
		double srcHeightOnScreen = srcNode.getOuterBounds().getHeight() * ((AffineTransform) displayBean.getTransformOf(srcNode)).getScaleY()
				* paintContext.getCamera().getViewScale();
		int srcMinDimOnScreen = (int) Math.min(srcWidthOnScreen, srcHeightOnScreen);
		double destWidthOnScreen = destNode.getOuterBounds().getWidth() * ((AffineTransform) displayBean.getTransformOf(destNode)).getScaleX()
				* paintContext.getCamera().getViewScale();
		double destHeightOnScreen = destNode.getOuterBounds().getHeight() * ((AffineTransform) displayBean.getTransformOf(destNode)).getScaleY()
				* paintContext.getCamera().getViewScale();
		int destMinDimOnScreen = (int) Math.min(destWidthOnScreen, destHeightOnScreen);
		boolean showSrcConnector = srcIsOnScreen && !arrowHeadIsOnScreen && srcMinDimOnScreen >= MIN_NODE_DIM_FOR_CONNECTORS;
		boolean showDestConnector = destIsOnScreen && !arrowHeadIsOnScreen && destMinDimOnScreen >= MIN_NODE_DIM_FOR_CONNECTORS;
		int renderingQuality = displayBean.isInteracting() ? displayBean.getDynamicRenderingQuality() : displayBean.getStaticRenderingQuality();
		arcStyle.render(paintContext.getGraphics(), renderingQuality, arcColor, showSrcConnector, showDestConnector);
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#getWeight()
	 */
	public double getWeight() {
		return arcStyle.getWeight();
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#addShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
	 */
	public void addShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
		// implement PShrimpArc.addShrimpDisplayObjectListener
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#removeShrimpDisplayObjectListener(ca.uvic.csr.shrimp.DisplayBean.listener.ShrimpDisplayObjectListener)
	 */
	public void removeShrimpDisplayObjectListener(ShrimpDisplayObjectListener shrimpDisplayObjectListener) {
		// implement PShrimpArc.removeShrimpDisplayObjectListener
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#updateVisibility()
	 */
	public void updateVisibility() {
		boolean newVisibility = srcNode.isVisible() && destNode.isVisible() && isInView && !isFiltered();
		if (newVisibility != isVisible() || !visibilitySetOnce) {
			setVisible(newVisibility);
		}
	}

	protected boolean isFiltered() {
		return displayBean.isFiltered(getRelationship());
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpDisplayObject#isInDisplay()
	 */
	public boolean isInDisplay() {
		return getParent() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#getSourceInLayout()
	 */
	public LayoutEntity getSourceInLayout() {
		return invertedInLayout ? destNode : srcNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#getDestinationInLayout()
	 */
	public LayoutEntity getDestinationInLayout() {
		return invertedInLayout ? srcNode : destNode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#isBidirectionalInLayout()
	 */
	public boolean isBidirectionalInLayout() {
		return false;
	}

	double weightInLayout = 1.0;

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#setWeightInLayout(double)
	 */
	public void setWeightInLayout(double weight) {
		weightInLayout = weight;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#getWeightInLayout()
	 */
	public double getWeightInLayout() {
		return weightInLayout;
	}

	Object layoutInformation;

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#setLayoutInformation(java.lang.Object)
	 */
	public void setLayoutInformation(Object layoutInformation) {
		this.layoutInformation = layoutInformation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.mylar.zest.layouts.LayoutRelationship#getLayoutInformation()
	 */
	public Object getLayoutInformation() {
		return layoutInformation;
	}

	private boolean invertedInLayout = false;

	private Stack savedSrcNodes = new Stack();

	private Stack savedDestNodes = new Stack();

	private Stack savedSrcTerminals = new Stack();

	private Stack savedDestTerminals = new Stack();

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#getInvertedInLayout()
	 */
	public boolean getInvertedInLayout() {
		return invertedInLayout;
	}

	/* (non-Javadoc)
	 * @see ca.uvic.csr.shrimp.DisplayBean.ShrimpArc#setInvertedInLayout(boolean)
	 */
	public void setInvertedInLayout(boolean invertedInLayout) {
		this.invertedInLayout = invertedInLayout;
	}

	public void setBendPoints(LayoutBendPoint[] bendPoints) {
		this.arcStyle.setBendPoints(bendPoints);
		this.hasBendPoints = true;
	}

	public LayoutBendPoint[] getBendPoints() {
		return (hasBendPoints ? arcStyle.getBendPoints() : new LayoutBendPoint[0]);
	}

	public void clearBendPoints() {
		this.arcStyle.setBendPoints(new BendPoint[0]);
		this.hasBendPoints = false;
	}

	public boolean hasBendPoints() {
		return hasBendPoints;
	}

	/**
	 * Populate the specified layout constraint
	 */
	public void populateLayoutConstraint(LayoutConstraint constraint) {
		try {
			// If this is a label constraint, set the label to the edge's label
			if (constraint instanceof LabelLayoutConstraint) {
				LabelLayoutConstraint labelConstraint = (LabelLayoutConstraint) constraint;
				String label = (String) this.getRelationship().getAttribute(AttributeConstants.NOM_ATTR_REL_SHORT_DISPLAY_TEXT);
				if (label != null) {
					labelConstraint.label = label;
					labelConstraint.pointSize = 18;
				}
			} else if (constraint instanceof SequenceEdgeLayoutConstraint) {
				SequenceEdgeLayoutConstraint edgeConstraint = (SequenceEdgeLayoutConstraint) constraint;
				edgeConstraint.type = (String) getRelationship().getAttribute(AttributeConstants.NOM_ATTR_REL_TYPE);
				edgeConstraint.order = (String) getRelationship().getAttribute(AttributeConstants.NOM_ATTR_ORDER);
				// @tag Shrimp(sequence) : Todo implement the grouped attribute (add to constants, etc.)
				//	edgeConstraint.grouped = this.grouped;
			}

			//		else if ( constraint instanceof BasicEdgeConstraints ) {
			//			BasicEdgeConstraints basicEdgeConstraints = (BasicEdgeConstraints) constraint;
			//		}
		} catch (NoClassDefFoundError ignore) {
		}
	}

	/**
	 * Redirect this arc to the specified source and destination. The old source and
	 * destination nodes for later restoration.
	 * @tag Shrimp(grouping)
	 * @param source
	 * @param dest
	 */
	public void redirect(ShrimpNode source, ShrimpNode dest) {
		displayBean.getDataDisplayBridge().delShrimpArcFromMaps(this);
		this.savedSrcNodes.push(this.srcNode);
		this.savedDestNodes.push(this.destNode);
		this.savedSrcTerminals.push(this.srcTerminal);
		this.savedDestTerminals.push(this.destTerminal);
		if (!source.equals(srcNode)) {
			this.srcNode.removeShrimpDisplayObjectListener(this);
			source.addShrimpDisplayObjectListener(this);
			srcTerminal = new PShrimpTerminal(displayBean, source, false);
			srcTerminal.attachArc(this);
		}
		if (!dest.equals(destNode)) {
			this.destNode.removeShrimpDisplayObjectListener(this);
			dest.addShrimpDisplayObjectListener(this);
			destTerminal = new PShrimpTerminal(displayBean, dest, false);
			destTerminal.attachArc(this);
		}
		this.destNode.addShrimpDisplayObjectListener(this);
		this.srcNode = source;
		this.destNode = dest;
		displayBean.getDataDisplayBridge().addShrimpArcToMaps(this);
	}

	/**
	 * Restore a redirected arc to its orginal source and destination nodes.
	 * @tag Shrimp.grouping
	 */
	public void restore() {
		displayBean.getDataDisplayBridge().delShrimpArcFromMaps(this);
		if (!this.savedSrcNodes.isEmpty()) {
			this.srcNode.removeShrimpDisplayObjectListener(this);
			this.srcNode = (ShrimpNode) this.savedSrcNodes.pop();
			this.srcNode.addShrimpDisplayObjectListener(this);
			this.srcTerminal = (ShrimpTerminal) this.savedSrcTerminals.pop();
		}
		if (!this.savedDestNodes.isEmpty()) {
			this.destNode.removeShrimpDisplayObjectListener(this);
			this.destNode = (ShrimpNode) this.savedDestNodes.pop();
			this.destNode.addShrimpDisplayObjectListener(this);
			this.destTerminal = (ShrimpTerminal) this.savedDestTerminals.pop();
		}
		displayBean.getDataDisplayBridge().addShrimpArcToMaps(this);
	}
}