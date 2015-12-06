/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;

/**
 * A collection of general utilites for Shrimp.
 *
 * @author Rob Lintern, Chris Callendar
 */
public class ShrimpUtils {

	public static boolean isVisibleOnScreen(Point p) {
		return isVisibleOnScreen(p.x, p.y);
	}

	/**
	 * Determines if the given x, y point is a displayable point on the screen.
	 * This is useful when multiple monitors are used.
	 * @return true if the x, y point will be visible on the screen
	 */
	public static boolean isVisibleOnScreen(int x, int y) {
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		boolean ok = false;
		for (int i = 0; i < devices.length; i++) {
			Rectangle rect = devices[i].getDefaultConfiguration().getBounds();
			if (rect.contains(x, y)) {
				ok = true;
				break;
			}
		}
		return ok;
	}

	public static boolean isVisibleOnScreen(Rectangle rect) {
		return isVisibleOnScreen(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Determines if the given rectangle is a displayable region on the screen.
	 * This is useful when multiple monitors are used.
	 * @return true if the rectangle will be visible on the screen
	 */
	public static boolean isVisibleOnScreen(int x, int y, int w, int h) {
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		boolean ok = false;
		for (int i = 0; i < devices.length; i++) {
			if ((devices[i] != null) && (devices[i].getDefaultConfiguration() != null)) {
				Rectangle rect = devices[i].getDefaultConfiguration().getBounds();
				if ((rect != null) && rect.contains(x, y, w, h)) {
					ok = true;
					break;
				}
			}
		}
		return ok;
	}

	/**
	 * Saves the bounds in the project properties file.
	 * @param bounds the bounds to convert to a string like "x,y,w,h"
	 */
	public static String boundsToString(Rectangle bounds) {
		String str = "";
		if (bounds != null) {
			str = bounds.x+","+bounds.y+","+bounds.width+","+bounds.height;
		}
		return str;
	}

	public static Rectangle stringToBounds(String str) {
		return stringToBounds(str, null);
	}

	/**
	 * Parses the string into a Rectangle.
	 * @param str the string in the form "x,y,w,h"
	 * @param defaultRect the default rectangle to use if the string can't be parsed
	 */
	public static Rectangle stringToBounds(String str, Rectangle defaultRect) {
		Rectangle rect = defaultRect;
		if ((str != null) && (str.length() > 0)) {
			String[] xywh = str.split(",");
			if (xywh.length == 4) {
				try {
					Rectangle r = new Rectangle(
							Integer.parseInt(xywh[0]),		// X
							Integer.parseInt(xywh[1]),		// Y
							Integer.parseInt(xywh[2]),		// WIDTH
							Integer.parseInt(xywh[3]));		// HEIGHT
					rect = r;
				} catch (Exception ignore) {}
			}
		}
		return rect;
	}

	/**
	 * Gets the {@link GraphicsConfiguration} whose bounds contain the location
	 * of the given {@link Rectangle}.  The default configuration will be returned
	 * if the location isn't inside any of the screen device configurations.
	 * @param rect
	 */
	public static GraphicsConfiguration getGraphicsConfiguration(Rectangle rect) {
		return getGraphicsConfiguration(rect.getLocation());
	}

	/**
	 * Gets the {@link GraphicsConfiguration} whose bounds contain the given {@link Point}.
	 * The default configuration will be returned if the location isn't inside any of the screen device configurations.
	 * @param p
	 */
	public static GraphicsConfiguration getGraphicsConfiguration(Point p) {
		GraphicsConfiguration config = null;
		if (p != null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			for (int j = 0; j < gs.length; j++) {
				GraphicsDevice gd = gs[j];
				GraphicsConfiguration gc = gd.getDefaultConfiguration();
				Rectangle gcBounds = gc.getBounds();
				if (gcBounds.contains(p)) {
					config = gc;
					break;
				}
			}
		}
		if (config == null) {
			config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		}
		return config;
	}

	/**
	 * Returns the string parsed into a boolean.
	 * If the string can't be parsed then false is returned.
	 * @see ShrimpUtils#parseBoolean(String, boolean)
	 */
	public static boolean parseBoolean(String s) {
		return parseBoolean(s, false);
	}

	/**
	 * Parses the string into a boolean value.
	 * If the string is "true" or "false" (ignoring case) then true or false is returned.
	 * If the string is the number "0" then false is returned.
	 * If the string is the number "1" then true is returned.
	 * Otherwise defaultValue is returned.
	 * @param s the string to parse into a boolean
	 * @param defaultValue the value to return if the string can't be parsed.
	 * @return true or false
	 */
	public static boolean parseBoolean(String s, boolean defaultValue) {
		boolean b = defaultValue;
		if ((s != null) && (s.length() > 0)) {
			if ("true".equalsIgnoreCase(s) || "1".equals(s)) {
				b = true;
			} else if ("false".equalsIgnoreCase(s) || "0".equals(s)) {
				b = false;
			}
		}
		return b;
	}

	public static int parseInt(String s) {
		return parseInt(s, 0);
	}

	public static int parseInt(String s, int def) {
		int rv = def;
		if ((s != null) && (s.length() > 0)) {
			try {
				rv = Integer.parseInt(s);
			} catch (NumberFormatException ignore) {}
		}
		return rv;
	}

	public static List toList(Object[] array) {
		List list = Collections.EMPTY_LIST;
		if (array != null) {
			list = new ArrayList(array.length);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
		return list;
	}

	public static Vector toVector(Object obj) {
		Vector v = new Vector(1);
		v.add(obj);
		return v;
	}

	public static List toList(Object obj) {
		ArrayList list = new ArrayList(1);
		list.add(obj);
		return list;
	}

	public static Set toSet(Object obj) {
		HashSet set = new HashSet(2);
		set.add(obj);
		return set;
	}

	public static Map toMap(Object key, Object value) {
		HashMap map = new HashMap(2);
		map.put(key, value);
		return map;
	}

	public static Collection toCollection(Object obj) {
		return toList(obj);
	}

	/**
	 * Compares the two objects and returns true if they are equal.<br>
	 * If both are null then <b>true</b> is retured.<br>
	 * If one object is null and the other is not null then <b>false</b> is returned.<br>
	 * Otherwise if both objects are not null then the .equals method is used to compared the two.
	 * @param oldValue
	 * @param newValue
	 * @return boolean
	 */
	public static boolean equals(Object oldValue, Object newValue) {
		boolean equal;
		// case 1: both are null
		if ((oldValue == null) && (newValue == null)) {
			equal = true;
		}
		// case 2: neither are null
		else if ((oldValue != null) && (newValue != null)) {
			equal = oldValue.equals(newValue);
		}
		// case 3: one is null, the other is not null
		else {
			equal = false;
		}
		return equal;
	}

	/**
	 * Determines if the bsf.jar and js.jar libraries are in the classpath.
	 * These are needed for Scripting.
	 * @return true if scripting libraries are installed
	 */
	public static boolean isScriptingToolInstalled() {
		try {
			new org.apache.bsf.BSFManager();
		    new org.apache.bsf.engines.javascript.JavaScriptEngine();
		    new org.mozilla.javascript.Context();
		    return true;
		} catch (Throwable ignore) {
			return false;
		}
	}

	public static boolean shouldShowManyRoots(int numRoots) {
	    boolean shouldShow = true;
	    // check if too many roots to show
		Properties properties = ApplicationAccessor.getProperties();
		String showWarningStr = properties.getProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING, DisplayBean.DEFAULT_SHOW_MANY_CHILDREN_WARNING);
		boolean showWarning = (new Boolean(showWarningStr)).booleanValue();
		if (showWarning) {
			String thresholdStr = properties.getProperty(DisplayBean.PROPERTY_KEY__SHOW_MANY_CHILDREN_WARNING_THRESHOLD, DisplayBean.DEFAULT_SHOW_MANY_CHILDREN_WARNING_THRESHOLD);
			int threshold = (new Integer(thresholdStr)).intValue();
			if (numRoots > threshold) {
				String title = ApplicationAccessor.getAppName();
				String message = "All data has been loaded from the back-end, but there will be " + numRoots + " nodes!" +
							"\nIt may take some time to create them all.\n\nAre you sure you want to continue?";
				int result = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(), message, title, JOptionPane.YES_NO_OPTION);
				shouldShow = (result == JOptionPane.OK_OPTION);
			} else {
				shouldShow = true;
			}
		}
		return shouldShow;
	}

	public static int showDialogWithPropertyCheckbox(Object message, int optionType) {
		Component parentComp = ApplicationAccessor.getParentFrame();
		String title = ApplicationAccessor.getAppName();
		int result = JOptionPane.showConfirmDialog(parentComp, message, title, optionType);
		return result;
	}

	public static Dialog getParentDialog(Component parent) {
		if (parent instanceof Dialog) {
			return (Dialog) parent;
		}
		Window window = SwingUtilities.windowForComponent(parent);
		return (window instanceof Dialog ? (Dialog) window : null);
	}

	public static Frame getParentFrame(Component parent) {
		if (parent instanceof Frame) {
			return (Frame) parent;
		}
		Window window = SwingUtilities.windowForComponent(parent);
		return (window instanceof Frame ? (Frame) window : null);
	}

	/**
	 * Centers the given window on the window for the parent component.
	 * Doesn't allow the window to be placed off screen.
	 */
	public static void centerWindowOnParent(Window window, Component parent) {
		Window parentWindow = null;
		if (parent != null) {
			parentWindow = (parent instanceof Window ? (Window) parent : SwingUtilities.windowForComponent(parent));
		}

		int x = 0, y = 0;
		if (parentWindow != null) {
			x = parentWindow.getX() + (parentWindow.getWidth() / 2) - (window.getWidth() / 2);
			y = parentWindow.getY() + (parentWindow.getHeight() / 2) - (window.getHeight() / 2);

			// ensure that the window won't be off the edge off the bottom or right side of the screen
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if ((x + window.getWidth()) > screenSize.width) {
				x = screenSize.width - window.getWidth();
			}
			if ((y + window.getHeight()) > screenSize.height) {
				y = screenSize.height - window.getWidth();
			}
			// ensure that the window is not off the top or left side of the screen
			x = Math.max(0, x);
			y = Math.max(0, y);
		}

		window.setLocation(x, y);
	}

	/**
	 * Centers the given component (probably a {@link Window}) on the screen.
	 */
	public static void centerOnScreen(Component comp) {
		// put in center of screen
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)(screen.getWidth() - comp.getWidth()) / 2;
		int y = (int)(screen.getHeight() - comp.getHeight()) / 2;
		comp.setLocation(x,y);
	}

	public static String cprelsToKey(String[] cprels) {
		if (cprels.length == 0) {
			return "";
		}
		String key = " (";
		for (int i = 0; i < cprels.length; i++) {
			String cprel = cprels[i];
			key += cprel;
			if (i < cprels.length - 1) {
				key += ", ";
			}
		}
		key += " hierarchy)";
		return key;
	}

	/**
	 * Returns a {@link Vector} of {@link String} objects that are the names of all the
	 * <b>public</b> {@link Field}s.
	 */
	public static Vector getFields(Class cls) {
		Field[] fieldsArray = cls.getFields();
		Vector/*<String>*/ fields = new Vector/*<String>*/(fieldsArray.length);
		for (int i = 0; i < fieldsArray.length; i++) {
			fields.add(fieldsArray[i].getName());
		}
		return fields;
	}

	/**
	 * Returns a {@link Vector} of {@link String}s that are the full
	 * {@link Method} names, with parameters for the given class.
	 * @param insertDefaultValues if true then default values will be used instead of the paramter class names
	 *  for primitive types and strings. For example the empty string is inserted for strings, and 0 is inserted for numbers.
	 * @return Vector of String method names.
	 */
	public static Vector getMethodNames(Class cls, boolean insertDefaultValues) {
		Method[] methodsArray = cls.getMethods();
		Vector/*<String>*/ methods = new Vector/*<String>*/(methodsArray.length);
		for (int i = 0; i < methodsArray.length; i++) {
			String term = getMethodName(methodsArray[i], insertDefaultValues);
			methods.add(term);
		}
		return methods;
	}

	/**
	 * Returns the string name and parameters for the {@link Method}.
	 * @param insertDefaultValues if true then default values will be used instead of the paramter class names
	 *  for primitive types and strings. For example the empty string is inserted for strings, and 0 is inserted for numbers.
	 */
	public static String getMethodName(Method method, boolean insertDefaultValues) {
		Class[] params = method.getParameterTypes();
		StringBuffer term = new StringBuffer();
		term.append(method.getName());
		term.append("(");
		if (params.length > 0) {
			for (int j = 0; j < params.length; j++) {
				if (j > 0) {
					term.append(", ");
				}
				Class cls = params[j];
				String className = cls.getName();
				boolean set = false;
				if (insertDefaultValues) {
					set = true;
					if (cls == String.class) {
						term.append("\"\"");
					} else if (cls == boolean.class) {
						term.append("true");
					} else if ((cls == long.class) || (cls == int.class) || (cls == short.class) || (cls == byte.class)) {
						term.append("0");
					} else if ((cls == float.class) || (cls == double.class)) {
						term.append("0.0");
					} else if (cls == char.class) {
						term.append("''");
					} else {
						set = false;
					}
				}
				if (!set) {
					int dot = className.lastIndexOf(".");
					if (dot >= 0) {
						className = className.substring(dot + 1);
					}
					// special case for arrays
					if (className.endsWith(";")) {
						className = className.substring(0, className.length() - 1) + "[]";
					}
					term.append(className);
				}
			}
		}
		term.append(")");
		return term.toString();
	}

	/**
	 * Creates a temporary directory.
	 * @param dirName the name for the directory
	 * @param unique if true then the directory name will have numbers on the end of it to ensure
	 *  that it is unique.  If false then the directory will have the name "dirName".
	 * @return the directory
	 * @throws IOException
	 */
	public static File createTempDir(String dirName, boolean unique) throws IOException {
		File tempDir;
		if (unique) {
			tempDir = File.createTempFile(dirName, "");
			tempDir.delete();
		} else {
			File tempFile = File.createTempFile("test", ".tmp");
			tempDir = new File(tempFile.getParentFile(), dirName);
			tempFile.delete();
		}
		tempDir.mkdirs();
		return tempDir;
	}

	/**
	 * Recursively searches all the children (and grandchildren) of the given container
	 * looking for the first one that has the same class name.
	 * @param c the container to start searching the children
	 * @param cls
	 * @return a component which is an instanceof of the given cls or null if not found
	 */
	public static Component getChildComponent(Container c, Class cls) {
		Component foundComponent = null;
		if (c != null) {
			// check for the class name
			for (int i = 0; i < c.getComponentCount(); i++) {
				Component comp = c.getComponent(i);
				if (cls.isInstance(comp)) {
					return comp;
				}
			}
			// recurse on the child containers
			for (int i = 0; i < c.getComponentCount(); i++) {
				Component comp = c.getComponent(i);
				if (comp instanceof Container) {
					foundComponent = getChildComponent((Container)comp, cls);
					if (foundComponent != null) {
						break;
					}
				}
			}
		}
		return foundComponent;
	}

	public static List findAllChildComponents(Container c, Class cls) {
		ArrayList children = new ArrayList();
		findAllChildComponents(c, cls, children);
		return children;
	}

	/**
	 * Recursively goes through all descendents of the given container adding each component
	 * that is an instance of cls to the list.  If one or more matching components are found
	 * at this level it returns.  So if it was looking for a JPanel, it won't find any JPanel
	 * objects that are descendents of (nested inside of) a JPanel.
	 * @param c
	 * @param cls
	 * @param foundComponents
	 */
	public static void findAllChildComponents(Container c, Class cls, ArrayList foundComponents) {
		if (c != null)  {
			boolean found = false;
			// check for the class name
			for (int i = 0; i < c.getComponentCount(); i++) {
				Component comp = c.getComponent(i);
				if (cls.isInstance(comp)) {
					found = true;
					foundComponents.add(cls.cast(comp));
				}
			}

			// stop once at least one has been found at this level?  This might not be the expected behavior...
			if (found) {
				return;
			}

			// recurse on the child containers
			for (int i = 0; i < c.getComponentCount(); i++) {
				Component comp = c.getComponent(i);
				if (comp instanceof Container) {
					findAllChildComponents((Container)comp, cls, foundComponents);
				}
			}
		}
	}

}
