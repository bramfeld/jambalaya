/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.AbstractShrimpTool;
import ca.uvic.csr.shrimp.gui.ShrimpView.ShrimpView;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PInputManager;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;
 
/**
 * This class represents a thumbnail of the current shrimp project.  It appears
 * in a small window and shows SHriMP as a whole.  A box in the window represents
 * the area of the project being seen in the SHriMP View window.  Moving the box
 * will update the larger window.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class ThumbnailView extends AbstractShrimpTool {
	
	private static final int VIEW_BOX_MIN_DIM = 10;
	private static final float ABS_VIEW_BOX_WIDTH = 4.0f;
	private static Stroke VIEW_BOX_STROKE;
	private static final float ABS_TEMP_BOX_WIDTH = 1.0f;
	private static Stroke TEMP_BOX_STROKE;
	private static final float ABS_RESIZE_BOX_WIDTH = 1.0f;
	private static Stroke RESIZE_BOX_STROKE;
	static {
	    try {
	        VIEW_BOX_STROKE = new PFixedWidthStroke(ABS_VIEW_BOX_WIDTH);
	        TEMP_BOX_STROKE = new PFixedWidthStroke(ABS_TEMP_BOX_WIDTH);
	        RESIZE_BOX_STROKE = new PFixedWidthStroke(ABS_RESIZE_BOX_WIDTH);
	    } catch (SecurityException e) { //thrown when shrimp is an applet
	        VIEW_BOX_STROKE = new BasicStroke(ABS_VIEW_BOX_WIDTH);
	        TEMP_BOX_STROKE = new BasicStroke(ABS_TEMP_BOX_WIDTH);
	        RESIZE_BOX_STROKE = new BasicStroke(ABS_RESIZE_BOX_WIDTH);
	    }	        
	}
	private static final Color VIEW_BOX_COLOR = Color.gray;
	private static final Color TEMP_BOX_COLOR = Color.darkGray;
	private static final Color RESIZE_BOX_COLOR = Color.black;
	private static final int RESIZE_BOX_DIM = 6;

	private JRootPane gui;
	private DisplayBean displayBean;
	private Rectangle2D.Double rootBounds;
	private PCanvas thumbCanvas;
	private PPath viewBox;
	private PPath[] resizeBoxes;
	private PPath tempBox;
	private PLayer viewLayer;
	private PCamera thumbCamera;
	private PCamera mainViewCamera;
	private PCanvas mainViewCanvas;
	private double mousePressedX;
	private double mousePressedY;
	private double viewBoxWhenPressedX;
	private double viewBoxWhenPressedY;
	private double viewBoxWhenPressedWidth;
	private double viewBoxWhenPressedHeight;
	
	private ComponentListener guiResizedListener;
	private ComponentListener displayBeanResizedListener;
	private PropertyChangeListener displayBeanViewChangeListener;

	public ThumbnailView(ShrimpProject project) {
		super(ShrimpProject.THUMBNAIL_VIEW, project);
		gui = new JRootPane();
		init();
	}
	
	private void init() {	
		if (project == null) {
			initializeNullProjectGUI();
		} else {
			try {
				ShrimpView shrimpView = (ShrimpView) project.getTool(ShrimpProject.SHRIMP_VIEW);
				displayBean = (DisplayBean) shrimpView.getBean(ShrimpView.DISPLAY_BEAN);
				mainViewCanvas = ((PNestedDisplayBean)displayBean).getPCanvas();				
				mainViewCamera = mainViewCanvas.getCamera();				
				initializeGUI();
			} catch (ShrimpToolNotFoundException e) {
				//e.printStackTrace();
				initializeNullProjectGUI();
			} catch (BeanNotFoundException e) {
				e.printStackTrace();
			}
		}
		gui.revalidate();
		gui.repaint();
	}
	
	private void initializeNullProjectGUI () {
		gui.add(new JPanel());
	}
	
	private void cleanup() {
		gui.removeAll();
		if (guiResizedListener != null) {
			gui.removeComponentListener(guiResizedListener);
		}
		if (displayBean != null) {
			if (displayBeanResizedListener != null) {
				mainViewCanvas.removeComponentListener(displayBeanResizedListener);	
			}
			if (displayBeanViewChangeListener != null) {
				mainViewCamera.removePropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, displayBeanViewChangeListener);				
			}
		}
	}
	
	private void zoomToRoot() {
		if (displayBean == null) {
			return;
		}
		
    	calculateRootBounds();
		Rectangle2D.Double mainViewBounds = mainViewCamera.getViewBounds();	
		// if view bounds are bigger than root bounds then focus on view bounds
		if (mainViewBounds.contains(rootBounds)) {
			thumbCamera.animateViewToCenterBounds(mainViewBounds, true, 1000);					    			
		} else {
			thumbCamera.animateViewToCenterBounds(rootBounds, true, 1000);					    			
		}
		updateViewBox();
		updateResizeBoxes();
	}
	
	private void initializeGUI() {					
		thumbCanvas = new PCanvas();
		thumbCanvas.setPreferredSize(new Dimension(300, 300));
		
		thumbCanvas.removeInputEventListener(thumbCanvas.getZoomEventHandler());
		thumbCanvas.removeInputEventListener(thumbCanvas.getPanEventHandler());
		
		gui.setLayout(new BorderLayout());
		gui.add(thumbCanvas, BorderLayout.CENTER);
		
		thumbCamera = thumbCanvas.getCamera();
					
		// create a view box, and resize box and add to a layer group
		createViewBox();
		createResizeBoxes();
		viewLayer = new PLayer();
		viewLayer.addChild(viewBox);
		for (int i = 0; i < resizeBoxes.length; i++) {
			viewLayer.addChild(resizeBoxes[i]);
		}
		
		//add the shrimp view's node layer to this camera's list of layers
		thumbCamera.addLayer(((PNestedDisplayBean)displayBean).getNodeLayer());
		// add view layer to the zCanvas' base layer
		thumbCanvas.getLayer().addChild(viewLayer);
		// add view layer to camera's list of layers
		thumbCamera.addLayer(viewLayer);
		
    	//want the resize box to follow the view box
		thumbCamera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateResizeBoxes();
			}
		});
				
		// zoom in on the root if this window resized
		guiResizedListener = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				zoomToRoot();
			}
		};
		gui.addComponentListener(guiResizedListener);
		
		// update view box when displayBean is resized
		displayBeanResizedListener = new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				updateViewBox();
				updateResizeBoxes();
			}
		};
		mainViewCanvas.addComponentListener(displayBeanResizedListener);
		
		displayBeanViewChangeListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				updateViewBox();
				updateResizeBoxes();
			}
		};
		mainViewCamera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, displayBeanViewChangeListener);
		
		viewBox.addInputEventListener( new ViewBoxInputAdapter());		
			
		resizeBoxes[0].addInputEventListener(new ResizeTopLeftAdapter());
		resizeBoxes[1].addInputEventListener(new ResizeTopRightAdapter());
		resizeBoxes[2].addInputEventListener(new ResizeBottomLeftAdapter());
		resizeBoxes[3].addInputEventListener(new ResizeBottomRightAdapter());
	}
	
	public Component getGUI() {
		return gui;
	}
	
	public void disposeTool() {
		cleanup();
	}
	
	/**
	 * @see ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool#refresh()
	 */
	public void refresh() {
		cleanup();
		init();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				zoomToRoot();
			}
		});
	}

	private void createViewBox() {
		viewBox = new PPath(new Rectangle2D.Double());
		viewBox.setPaint(null);
		viewBox.setStroke(VIEW_BOX_STROKE);
		viewBox.setStrokePaint(VIEW_BOX_COLOR);
		viewBox.setTransparency(0.6f);
	} 
	
	private void createTempBox() {
		tempBox = new PPath(viewBox.getBounds());
		tempBox.setPaint(null);
		tempBox.setStroke(TEMP_BOX_STROKE);
		tempBox.setStrokePaint(TEMP_BOX_COLOR);
	} 
	
	private void createResizeBoxes(){
		resizeBoxes = new PPath[4];
		for (int i = 0; i < resizeBoxes.length; i++) {
			resizeBoxes[i] = new PPath(new Rectangle2D.Double());
			resizeBoxes[i].setPaint(RESIZE_BOX_COLOR);
			resizeBoxes[i].setStroke(RESIZE_BOX_STROKE);
			resizeBoxes[i].setStrokePaint(RESIZE_BOX_COLOR);
			resizeBoxes[i].setTransparency(0.7f);
		}
	}
	
	/** Determines bounds of the view box */
	private void updateViewBox() {
		Rectangle2D.Double shrimpViewBounds = mainViewCamera.getViewBounds();	
		double x = shrimpViewBounds.getX();
		double y = shrimpViewBounds.getY();
		double w = shrimpViewBounds.getWidth();
		double h = shrimpViewBounds.getHeight();

		// minimum absolute dimension should be VIEW_BOX_MIN_DIM
		double scale = thumbCamera.getViewScale();
		double oldW = w;
		double oldH = h;
		if (w < h) {
			// scale by width, preserving aspect ratio
			w = Math.max(w, VIEW_BOX_MIN_DIM / scale);
			h = h * w / oldW;
		} else {
			// scale by height, preserving aspect ratio
			h = Math.max(h, VIEW_BOX_MIN_DIM / scale);
			w = w * h / oldH;
		}
		x = Math.max(0, x - (w - oldW) / 2);
		y = Math.max(0, y - (h - oldH) / 2);
		viewBox.setPathTo(new Rectangle2D.Double(x, y, w, h));	
	}
	
	/**
	 * Determines bounds of the resize boxes, will always have an absolute size 
	 * of RESIZE_BOX_DIM by RESIZE_BOX_DIM and be placed in corners of the view box.
	 */
	private void updateResizeBoxes() {
		Rectangle2D.Double viewBoxBounds = viewBox.getBounds();	
		double scale = thumbCamera.getViewScale();
		double w = RESIZE_BOX_DIM / scale;
		double h = RESIZE_BOX_DIM / scale;

		// top left
		double x = viewBoxBounds.getX();
		double y = viewBoxBounds.getY();
		resizeBoxes[0].setPathTo(new Rectangle2D.Double(x, y, w, h));

		// top right
		x = viewBoxBounds.getX() + viewBoxBounds.getWidth() - w;
		y = viewBoxBounds.getY();
		resizeBoxes[1].setPathTo(new Rectangle2D.Double(x, y, w, h));

		// bottom left
		x = viewBoxBounds.getX();
		y = viewBoxBounds.getY() + viewBoxBounds.getHeight() - h;
		resizeBoxes[2].setPathTo(new Rectangle2D.Double(x, y, w, h));

		// bottom right
		x = viewBoxBounds.getX() + viewBoxBounds.getWidth() - w;
		y = viewBoxBounds.getY() + viewBoxBounds.getHeight() - h;
		resizeBoxes[3].setPathTo(new Rectangle2D.Double(x, y, w, h));	
	}

	
	private void calculateRootBounds() {
		// focus on the root artifact and just beyond
		double[] extents = displayBean.getExtents();
		rootBounds = new Rectangle2D.Double(extents[0], extents[1], extents[2], extents[3]);
	    double x = rootBounds.getX();
		double y = rootBounds.getY();
		double w = rootBounds.getWidth();
		double h = rootBounds.getHeight();
		x = x - w * 0.02f;
		y = y - h * 0.02f;
		w = w * 1.04f;
		h = h * 1.04f;
		rootBounds.setRect(x, y, w, h);
	}

    public void clear() {
        // nothing for now
    }
    
	protected void setTempBox(double x, double y, double w, double h) {
		// make sure the bounds stay within the viewing area
		x = Math.max(0, x); 
		y = Math.max(0, y);
		tempBox.setPathTo(new Rectangle2D.Double(x, y, w, h));
	}
	
	
	private class ViewBoxInputAdapter extends PInputManager {
		
		public void mouseEntered(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
		
		public void mouseExited(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		
		public void mousePressed(PInputEvent e) {
			mousePressedX = e.getPosition().getX();
			mousePressedY = e.getPosition().getY();
			viewBoxWhenPressedX = viewBox.getBounds().getX();
			viewBoxWhenPressedY = viewBox.getBounds().getY();
			
    		//put a temporary box where view box will end up
    		createTempBox();
    		viewLayer.addChild(tempBox);
		}
		
		public void mouseReleased(PInputEvent e) {
			mainViewCamera.animateViewToCenterBounds(tempBox.getBounds(), true, 1000);
			tempBox.removeFromParent();
		}	
			
		public void mouseDragged(PInputEvent e) {
			double mouseDraggedX = e.getPosition().getX();
			double mouseDraggedY = e.getPosition().getY();
			double x = viewBoxWhenPressedX - (mousePressedX - mouseDraggedX); 
			double y = viewBoxWhenPressedY - (mousePressedY - mouseDraggedY);
			double w = viewBox.getBounds().getWidth();
			double h = viewBox.getBounds().getHeight();
			setTempBox(x, y, w, h);	
			updateResizeBoxes();			
		}
		
	}
	
	
	// ***** resize box adapters
	private class ResizeBoxInputAdapter extends PInputManager {
		
		public void mouseExited(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getDefaultCursor());
		}
		
		public void mousePressed(PInputEvent e) {
			mousePressedX = e.getPosition().getX();
			mousePressedY = e.getPosition().getY();
			viewBoxWhenPressedWidth = viewBox.getBounds().getWidth();
			viewBoxWhenPressedHeight = viewBox.getBounds().getHeight();
			
    		//put a temporary box where view box will end up
    		createTempBox();
    		viewLayer.addChild(tempBox);
		}
		
		public void mouseReleased(PInputEvent e) {
			mainViewCamera.animateViewToCenterBounds(tempBox.getBounds(), true, 1000);
			tempBox.removeFromParent();
		}
		
	}
	
	private class ResizeTopLeftAdapter extends ResizeBoxInputAdapter {
		
		public void mouseEntered(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		}
		
		public void mouseDragged(PInputEvent e) {
			double diffX = mousePressedX - e.getPosition().getX();
			double diffY = mousePressedY - e.getPosition().getY();
			double w = viewBoxWhenPressedWidth + diffX; 
			double h = viewBoxWhenPressedHeight + diffY;
			double x = viewBox.getBounds().getX() - diffX;
			double y = viewBox.getBounds().getY() - diffY;
			setTempBox(x, y, w, h);				
		}
	}

	private class ResizeTopRightAdapter extends ResizeBoxInputAdapter {
		
		public void mouseEntered(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		}
		
		public void mouseDragged(PInputEvent e) {
			double diffX = mousePressedX - e.getPosition().getX();
			double diffY = mousePressedY - e.getPosition().getY();
			double w = viewBoxWhenPressedWidth - diffX; 
			double h = viewBoxWhenPressedHeight + diffY;
			double x = viewBox.getBounds().getX();
			double y = viewBox.getBounds().getY() - diffY;
			setTempBox(x, y, w, h);				
		}
	}

	private class ResizeBottomLeftAdapter extends ResizeBoxInputAdapter {
		
		public void mouseEntered(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		}
		
		public void mouseDragged(PInputEvent e) {
			double diffX = mousePressedX - e.getPosition().getX();
			double diffY = mousePressedY - e.getPosition().getY();
			double w = viewBoxWhenPressedWidth + diffX; 
			double h = viewBoxWhenPressedHeight - diffY;
			double x = viewBox.getBounds().getX() - diffX;
			double y = viewBox.getBounds().getY();
			setTempBox(x, y, w, h);				
		}
	}

	private class ResizeBottomRightAdapter extends ResizeBoxInputAdapter {
		
		public void mouseEntered(PInputEvent e) {
			thumbCanvas.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
		}
		
		public void mouseDragged(PInputEvent e) {
			double diffX = mousePressedX - e.getPosition().getX();
			double diffY = mousePressedY - e.getPosition().getY();
			double w = viewBoxWhenPressedWidth - diffX; 
			double h = viewBoxWhenPressedHeight - diffY;
			double x = viewBox.getBounds().getX();
			double y = viewBox.getBounds().getY();
			setTempBox(x, y, w, h);				
		}
	}
		
}