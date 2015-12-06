/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.util.Iterator;
import java.util.Vector;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.ShrimpNode;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.SpaceInvadersBlockerShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.SpaceInvadersBlueShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.SpaceInvadersGreenShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.SpaceInvadersMainShape;
import ca.uvic.csr.shrimp.DisplayBean.NodeShape.SpaceInvadersRedShape;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpApplication.BeanNotFoundException;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpApplication;
import ca.uvic.csr.shrimp.ShrimpApplication.ShrimpToolNotFoundException;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;


/**
 * 
 * 
 * @author Chris Callendar
 * @date 30-Nov-06
 */
public class SpaceInvadersAdapter extends DefaultToolAction {

	private static final String SPACE_INVADERS_GREEN = "Space_Invaders_Green";
	private static final String SPACE_INVADERS_BLUE = "Space_Invaders_Blue";
	private static final String SPACE_INVADERS_RED = "Space_Invaders_Red";
	private static final String SPACE_INVADERS_SHIELD = "Space_Invaders_Shield";
	private static final String SPACE_INVADERS_SHIP = "Space_Invaders_Ship";
	
	public static final String ACTION_NAME = "Space Invaders";
	public static final String TOOLTIP = "Allows the use of Space Invaders node shapes"; 

	public SpaceInvadersAdapter(ShrimpProject project, ShrimpTool tool) {
		super(ACTION_NAME, project, tool);
	}

	public void startAction() {
		System.out.println("Starting space invaders");
		try {
			DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
			displayBean.addNodeShape(new SpaceInvadersMainShape(), false);
			displayBean.addNodeShape(new SpaceInvadersBlockerShape(), false);
			displayBean.addNodeShape(new SpaceInvadersRedShape(), false);
			displayBean.addNodeShape(new SpaceInvadersBlueShape(), false);
			displayBean.addNodeShape(new SpaceInvadersGreenShape(), false);
			
			Vector nodes = displayBean.getVisibleNodes();
			for (Iterator iter = nodes.iterator(); iter.hasNext(); ) {
				ShrimpNode node = (ShrimpNode) iter.next();
				String type = node.getArtifact().getType();
				if (SPACE_INVADERS_SHIP.equals(type)) {
					node.setNodeShape(new SpaceInvadersMainShape());
				} else if (SPACE_INVADERS_SHIELD.equals(type)) {
					node.setNodeShape(new SpaceInvadersBlockerShape());
				} else if (SPACE_INVADERS_RED.equals(type)) {
					node.setNodeShape(new SpaceInvadersRedShape());
				} else if (SPACE_INVADERS_BLUE.equals(type)) {
					node.setNodeShape(new SpaceInvadersBlueShape());
				} else if (SPACE_INVADERS_GREEN.equals(type)) {
					node.setNodeShape(new SpaceInvadersGreenShape());
				} 
			}
			
			// refresh node types palette
			try {
				ShrimpTool tool = ApplicationAccessor.getApplication().getTool(ShrimpApplication.NODE_FILTER);
				tool.refresh();
			} catch (ShrimpToolNotFoundException e) {
				e.printStackTrace();
			}
			
//			BulletNode node = new BulletNode(new Point(400, 400), new Point(400, 100));
//			displayBean.getNodeLayer().addChild(node);
//			displayBean.getNodeLayer().repaint();
//			node.start();
		} catch (BeanNotFoundException bfe) {
			bfe.printStackTrace();
		}
	}
	
	
//	class BulletNode extends PNode implements PActivityDelegate {
//		
//		private int shift = 20;
//		private int shiftX;
//		private int shiftY;
//		private Point start;
//		private Point end;
//		private int x1, x2, y1, y2;
//		private PActivity activity;
//
//		public BulletNode(Point start, Point end) {
//			super();
//			this.start = start;
//			this.end = end;
//			
//			if (end.x > start.x) {
//				shiftX = shift;
//			} else if (end.x < start.x) {
//				shiftX = -shift;
//			} else {
//				shiftX = 0;
//			}
//			
//			if (end.y > start.y) {
//				shiftY = shift;
//			} else if (end.y < start.y) {
//				shiftY = -shift;
//			} else {
//				shiftY = 0;
//			}
//			
//			setBounds(start.x, start.y, shift, shift);
//			setVisible(true);
//			
//			activity = new PActivity(5000, 500);
//			activity.setDelegate(this);
//		}
//		
//		public void start() {
//			if (activity != null) {
//				addActivity(activity);
//			}
//		}
//		
//		protected void paint(PPaintContext paintContext) {
//			System.out.println("Painting");
//			Graphics2D g = paintContext.getGraphics();
//			g.setColor(Color.green);
//			g.setStroke(new BasicStroke(5f));
//			g.drawLine(x1, y1, x2, y2);
//		}
//		
//		public void activityStarted(PActivity activity) {
//			System.out.println("Activity started");
//			x1 = start.x;
//			y1 = start.y;
//			x2 = x1 + shiftX;
//			y2 = y1 + shiftY;
//			// paint the starting position
//			repaint();
//		}
//				
//		public void activityStepped(PActivity activity) {
//			System.out.println("Activity step");
//			
//			if (!isFinished()) {
//				// move line
//				shift();
//				
//				// repaint in new position
//				repaint();
//			} else {
//				activity.terminate();
//				System.out.println("Activity terminated");
//				activity = null;
//				getParent().removeChild(this);
//			}
//		}
//		
//		public void activityFinished(PActivity activity) {
//			System.out.println("Activity finished");
//		}
//
//		private void shift() {
//			x1 = x2;
//			y1 = y2;
//			x2 += shiftX;
//			y2 += shiftY;
//		}
//		
//		private boolean isFinished() {
//			boolean done = false;
//			if (shiftX != 0) {
//				if ((shiftX > 0) && (x2 >= end.x)) {
//					done = true;
//				} else if ((shiftX < 0) && (x2 <= end.x)) {
//					done = true;
//				}
//			}
//			if (shiftY != 0) {
//				if ((shiftY > 0) && (y2 >= end.y)) {
//					done = true;
//				} else if ((shiftY < 0) && (y2 <= end.y)) {
//					done = true;
//				}
//			}
//			return done;
//		}
//	}
	
}
