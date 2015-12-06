/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.ArcStyle;


/**
 * This is the normal Arc style used to display a single solid coloured line between two nodes.
 * Note: Two instances of this class are considered equal if they have the same name.
 *
 * @author Casey Best, Rob Lintern
 */
public class StraightSolidLineArcStyle extends StraightLineArcStyle {

	public final static String NAME = "Solid Line - Straight";

	public StraightSolidLineArcStyle(String name) {
		super(name, false);
	}

	public StraightSolidLineArcStyle() {
		super(NAME, false);
	}

	public Object clone() {
		ArcStyle style = new StraightSolidLineArcStyle();
		return style;
	}

	/**
	  * @see ca.uvic.csr.shrimp.DisplayBean.ArcStyle.ArcStyle#getThumbNail()
	  */
	/*
	 public JComponent getThumbnail(Color arcColor) {
		 final Color c = arcColor;

		 JPanel panel = new JPanel() {
			 public void paint(Graphics g) {
				 super.paint(g);

				 g.setColor(c);

				 // draw the base line
				 g.fillRect(0,5,40,2);
			 }
		 };
		 panel.setPreferredSize(new Dimension(40,11));
		 panel.setBorder(null);
		 return panel;
	 }
	 */

}
