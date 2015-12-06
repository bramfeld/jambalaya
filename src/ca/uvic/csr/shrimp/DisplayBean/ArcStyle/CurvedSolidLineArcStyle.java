/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;


/**
 *
 * @author Rob Lintern
 */
public class CurvedSolidLineArcStyle extends CurvedLineArcStyle {

	public final static String NAME = "Solid Line - Curved";

	public CurvedSolidLineArcStyle() {
		super(NAME);
	}

	public Object clone() {
		ArcStyle style = new CurvedSolidLineArcStyle();
		return style;
	}

	/**
	 * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getThumbnail(Color)
	 */
	/*
	public JComponent getThumbnail(final Color arcColor) {
		JPanel panel = new JPanel() {
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D)g;

				Point2D.Double srcPoint = new Point2D.Double (0, 0);
				Point2D.Double destPoint = new Point2D.Double (40, 10);

				CubicCurve2D.Double curve = new CubicCurve2D.Double();
				Point2D.Double cp1 = new Point2D.Double ();
				Point2D.Double cp2 = new Point2D.Double ();
				cp1.setLocation(10, 10);
				cp2.setLocation(30, 0);
				curve.setCurve(srcPoint, cp1, cp2, destPoint);
				g2.setColor(arcColor);
				g2.setStroke(new BasicStroke (2.0f));
				g2.draw (curve);
			}
		};
		panel.setPreferredSize(new Dimension(40,10));
		panel.setSize(new Dimension(40,10));
		panel.setBorder(null);
		return panel;
	}
	*/

}
