/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ShrimpApplication;

import java.awt.Frame;
import java.util.Properties;


/**
 * The purpose of this class is to provide global access to the "one and only" shrimp application.
 * This class is neccessary because Java does not allow static methods to be defined in interfaces
 * such as our ShrimpApplication interface. We allow this global access to the application because
 * there will only ever be one application in a single session.
 *
 * There are other ways to implement a singleton in Java, but we chose this way because it
 * is easy to understand.
 *
 * One alternative would be to define a static getApplication method in AbstractShrimpApplication
 * be we we do not want to cast every from ShrimpApplication to AbstractShrimpApplication.
 *
 * @author Rob Lintern
 */
public class ApplicationAccessor {

	/** The one and only shrimp application */
	private static ShrimpApplication app;

	/**
	 * Sets the one and only shrimp application.
	 */
	public static void setApplication(ShrimpApplication application) {
		app = application;
	}

	/**
	 * Returns the one and only shrimp application.
	 * NOTE: <code>setApplication</code> must be called before calling this method.
	 */
	public static ShrimpApplication getApplication() {
		if (app == null) {
			(new Exception ("Must call setApplication once before calling getApplication!")).printStackTrace();
		}
		return app;
	}

	/**
	 * Checks if the application is set (not null).
	 * @return boolean
	 */
	public static boolean isApplicationSet() {
		return (app != null);
	}

	/**
	 * @return the parent frame or null if no application is set
	 */
	public static Frame getParentFrame() {
		return (app != null ? app.getParentFrame() : null);
	}

	/**
	 * @return the application properties (or a new {@link Properties} if application isn't set)
	 */
	public static Properties getProperties() {
		return (app != null ? app.getProperties() : new Properties());
	}

	/**
	 * Gets an application property.
	 */
	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	/**
	 * Gets an application property.
	 */
	public static String getProperty(String key, String def) {
		return getProperties().getProperty(key, def);
	}

	/**
	 * Sets an application property.
	 */
	public static void setProperty(String key, String value) {
		if (key != null) {
			getProperties().setProperty(key, value);
		}
	}

	/**
	 * @return the application name
	 */
	public static String getAppName() {
		return (app != null ? app.getName() : "No Application");
	}

	/**
	 * Calls {@link ShrimpApplication#waitCursor()}
	 */
	public static void waitCursor() {
		if (app != null) {
			app.waitCursor();
		}
	}

	/**
	 * Calls {@link ShrimpApplication#defaultCursor()}
	 */
	public static void defaultCursor() {
		if (app != null) {
			app.defaultCursor();
		}
	}

}
