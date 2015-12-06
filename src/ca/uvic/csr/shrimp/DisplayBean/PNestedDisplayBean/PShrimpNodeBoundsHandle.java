/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.SwingConstants;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.util.DoubleDimension;
import ca.uvic.csr.shrimp.util.GeometryUtils;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.handles.PBoundsHandle;
import edu.umd.cs.piccolox.util.PBoundsLocator;

/**
 * @author Rob Lintern
 */
public class PShrimpNodeBoundsHandle extends PBoundsHandle {

	private static final float HANDLE_SIZE = 4.0f;

	private DisplayBean displayBean;
	private PShrimpNode pShrimpNode;

	private double size = -1;

	/**
	 * @param aLocator
	 * @param displayBean
	 */
	public PShrimpNodeBoundsHandle(PBoundsLocator aLocator, DisplayBean displayBean, PShrimpNode node) {
		super(aLocator);
		this.displayBean = displayBean;
		this.pShrimpNode = node;
		setPaint(Color.darkGray);
		setStrokePaint(Color.darkGray);
	}

	public PShrimpNode getPShrimpNode() {
		return pShrimpNode;
	}

	protected void paint(PPaintContext paintContext) {
		if (size == -1) {
			adjustSize(paintContext);
			return;
		}
		super.paint(paintContext);
	}

	private void adjustSize(PPaintContext paintContext) {
		double scale = paintContext.getScale();
		if (scale > 0) {
			double currentSize = getPathReference().getBounds2D().getWidth();
			double newSize = Math.min(100d, HANDLE_SIZE / scale);
			double shift = (currentSize - newSize) / 2;
			double x = getX() + shift;
			double y = getY() + shift;
			size = newSize;
			setPathToRectangle((float)x, (float)y, (float)size, (float)size);
		}
	}

	public void dragHandle(PDimension aLocalDimension, PInputEvent aEvent) {
		PBoundsLocator l = (PBoundsLocator) getLocator();

		PNode n = l.getNode();
		PBounds b = n.getBounds();

		PNode parent = getParent();
		if (parent != n && parent instanceof PCamera) {
			((PCamera) parent).localToView(aLocalDimension);
		}

		localToGlobal(aLocalDimension);
		n.globalToLocal(aLocalDimension);

		double dx = aLocalDimension.getWidth();
		double dy = aLocalDimension.getHeight();

		switch (l.getSide()) {
			case SwingConstants.NORTH:
				b.setRect(b.x, b.y + dy, b.width, b.height - dy);
				break;
			case SwingConstants.SOUTH:
				b.setRect(b.x, b.y, b.width, b.height + dy);
				break;
			case SwingConstants.EAST:
				b.setRect(b.x, b.y, b.width + dx, b.height);
				break;
			case SwingConstants.WEST:
				b.setRect(b.x + dx, b.y, b.width - dx, b.height);
				break;
			case SwingConstants.NORTH_WEST:
				b.setRect(b.x + dx, b.y + dy, b.width - dx, b.height - dy);
				break;
			case SwingConstants.SOUTH_WEST:
				b.setRect(b.x + dx, b.y, b.width - dx, b.height + dy);
				break;
			case SwingConstants.NORTH_EAST:
				b.setRect(b.x, b.y + dy, b.width + dx, b.height - dy);
				break;
			case SwingConstants.SOUTH_EAST:
				b.setRect(b.x, b.y, b.width + dx, b.height + dy);
				break;
		}

		boolean flipX = false;
		boolean flipY = false;

		if (b.width < 0) {
			flipX = true;
			b.width = -b.width;
			b.x -= b.width;
		}

		if (b.height < 0) {
			flipY = true;
			b.height = -b.height;
			b.y -= b.height;
		}

		if (flipX || flipY) {
			flipSiblingBoundsHandles(flipX, flipY);
		}

		updateNodeTransform(n, b);

	}

	private void updateNodeTransform(PNode n, PBounds newLocalBounds) {
		PAffineTransform at = n.getTransform();
		Rectangle2D.Double newBoundsWRTParent = GeometryUtils.transform(newLocalBounds, at);
		ShrimpNode parentNode = ((ShrimpNode) n).getParentShrimpNode();
		if (parentNode != null && !parentNode.getInnerBounds().contains(newBoundsWRTParent)) {
			return;
		}

		double newWidthWRTParent = newBoundsWRTParent.getWidth();
		double newHeightWRTParent = newBoundsWRTParent.getHeight();
		if (newWidthWRTParent < 10.0 || newHeightWRTParent < 10.0) {
			return;
		}
		double newXWRTParent = newBoundsWRTParent.getX();
		double newYWRTParent = newBoundsWRTParent.getY();
		Point2D.Double newPosition = new Point2D.Double(newXWRTParent + 0.5 * newWidthWRTParent, newYWRTParent + 0.5 * newHeightWRTParent);
		DoubleDimension newDim = new DoubleDimension(newWidthWRTParent, newHeightWRTParent);

		Vector nodes = new Vector(1);
		Vector positions = new Vector(1);
		Vector dimensions = new Vector(1);

		nodes.add(n);
		positions.add(newPosition);
		dimensions.add(newDim);

		displayBean.setPositionsAndSizes(nodes, positions, dimensions, false);
	}

	public void dispose() {
		removeAllChildren();
		removeFromParent();
		displayBean = null;
	}

}
