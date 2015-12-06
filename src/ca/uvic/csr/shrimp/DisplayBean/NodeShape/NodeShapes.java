/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;

import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Loads all the node shapes.
 *
 * @tag Shrimp.NodeShapes : this defines all the applicable {@link NodeShape} objects.
 * @author Chris Callendar
 * @date 5-Mar-07
 */
public class NodeShapes {

	private static final String MODE = NodeImage.STRETCHED;
	private static boolean DRAW = false;
	private static boolean FILL = false;

	/**
	 * Gets all the defined node shapes.
	 * @return array of the defined {@link NodeShape} objects
	 */
	public static NodeShape[] getNodeShapes() {
		Vector shapes = new Vector(12);
		shapes.add(new RectangleNodeShape());
		shapes.add(new RoundedRectangleNodeShape());
		shapes.add(new StackedRectangleNodeShape());
		shapes.add(new DropShadowRectangleNodeShape());
		shapes.add(new RectangleTriangleNodeShape());
		shapes.add(new RoundedRectangleTriangleNodeShape());
		shapes.add(new EllipseNodeShape());
		shapes.add(new TriangleNodeShape());
		shapes.add(new InvertedTriangleNodeShape());
		shapes.add(new DiamondNodeShape());
		shapes.add(new ObjectLifeLineNodeShape());
		shapes.add(new FileNodeShape());
		shapes.add(new FolderNodeShape());
		shapes.add(new CylinderNodeShape());

		// @tag Shrimp.NodeShapes.Christmas : only show when November or December!
		if (Calendar.getInstance().get(Calendar.MONTH) >= Calendar.NOVEMBER) {
			Vector xmasShapes = getChristmasShapes();
			shapes.addAll(xmasShapes);
		}

		return (NodeShape[]) shapes.toArray(new NodeShape[shapes.size()]);
	}

	public static Vector getChristmasShapes() {
		// these two have rectangle shaped images
		NodeShape candycane = new RectangleNodeShape("Christmas Candy Cane");
		candycane.setCustomRendering(new NodeImage(getFullImagePath("candycane.png"), MODE, FILL, DRAW));

		NodeShape present = new RectangleNodeShape("Christmas Present");
		present.setCustomRendering(new NodeImage(getFullImagePath("present.png"), MODE, FILL, DRAW));

		// for these next three we have to override the inner bounds method
		// to return a rectangular shape.  It looks much better this way.
		final RectangleNodeShape RECT = new RectangleNodeShape();

		NodeShape hat = new TriangleNodeShape("Christmas Hat") {
			public Double getInnerBounds(Rectangle2D outerBounds) {
				return RECT.getInnerBounds(outerBounds);
			}
		};
		hat.setCustomRendering(new NodeImage(getFullImagePath("hat.png"), MODE, FILL, DRAW));

		NodeShape ornament = new EllipseNodeShape("Christmas Ornament") {
			public Double getInnerBounds(Rectangle2D outerBounds) {
				return RECT.getInnerBounds(outerBounds);
			}
		};
		ornament.setCustomRendering(new NodeImage(getFullImagePath("ornament.png"), MODE, FILL, DRAW));

		NodeShape tree = new TriangleNodeShape("Christmas Tree") {
			public Double getInnerBounds(Rectangle2D outerBounds) {
				return RECT.getInnerBounds(outerBounds);
			}
		};
		tree.setCustomRendering(new NodeImage(getFullImagePath("tree.png"), MODE, FILL, DRAW));

		Vector shapes = new Vector(5);
		shapes.add(candycane);
		shapes.add(present);
		shapes.add(hat);
		shapes.add(ornament);
		shapes.add(tree);
		return shapes;
	}

	private static String getFullImagePath(String relPath) {
		String str = "";
		try {
			URL url = ResourceHandler.class.getResource("images/christmas/" + relPath);
			File file = ResourceHandler.toFile(url);
			str = file.getAbsolutePath();
		} catch (Exception ignore) {
		}
		return str;
	}

}
