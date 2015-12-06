/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp;

import java.awt.Color;


/**
 * Some color constants used in Shrimp.
 *
 * @author Chris Callendar
 * @date 2-May-07
 */
public class ShrimpColorConstants {

	// Windows plus colors
	private static final Color WINDOWS_BORDER = new Color(120, 152, 181);
	//private static final Color PLUS_CORNER = new Color(176, 194, 211);
	private static final Color WINDOWS_START = new Color(255, 255, 255);
	private static final Color WINDOWS_END = new Color(195, 186, 170);
	private static final Color WINDOWS_PLUS = new Color(0, 0, 0);
	// Eclipse plus colors
	private static final Color ECLIPSE_BORDER = new Color(120, 152, 181);
	private static final Color ECLIPSE_START = new Color(233, 246, 251);
	private static final Color ECLIPSE_END = new Color(168, 212, 237);
	private static final Color ECLIPSE_PLUS = new Color(36, 44, 67);

	public static boolean USE_ECLIPSE_COLORS = false;

	public static final Color SHRIMP_BACKGROUND = new Color(0, 46, 123);

	public static Color getPlusBorderColor() {
		return (USE_ECLIPSE_COLORS ? ECLIPSE_BORDER : WINDOWS_BORDER);
	}

	public static Color getPlusStartColor() {
		return (USE_ECLIPSE_COLORS ? ECLIPSE_START : WINDOWS_START);
	}

	public static Color getPlusEndColor() {
		return (USE_ECLIPSE_COLORS ? ECLIPSE_END : WINDOWS_END);
	}

	public static Color getPlusColor() {
		return (USE_ECLIPSE_COLORS ? ECLIPSE_PLUS : WINDOWS_PLUS);
	}

}
