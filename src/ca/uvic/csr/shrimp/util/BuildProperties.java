/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 *
 *
 * @author Chris Callendar
 * @date 17-Oct-06
 */
public class BuildProperties {

	protected static final String BUILD_VERSION = "build.version";
	protected static final String BUILD_NUMBER = "build.number";
	protected static final String BUILD_DATE = "build.date";
	protected static final String PROGRAM_NAME = "program.name";
	protected static final String UNKNOWN = "unknown";

	private Properties properties;
	private final String defaultAppName;

	public BuildProperties(String defaultAppName) {
		this.properties = new Properties();
		this.defaultAppName = defaultAppName;
	}

	public void loadBuildProperties(String filename, Class cls) {
		properties.clear();
		if (filename != null) {
			try {
	            InputStream stream = cls.getResourceAsStream(filename);
	            if (stream != null) {
	            	properties.load(stream);
	            	try {
	            	    stream.close();
	            	} catch (IOException e) {
	                    System.err.println(getName() + " Warning: Couldn't close build properties stream.");
	            	}
	            } else {
	                System.err.println(getName() + " Warning: Couldn't load build properties. Stream is null.");
	            }
	        } catch (IOException e) {
	            System.err.println(getName() + " Warning: Couldn't load build properties. IOException");
	            e.printStackTrace();
	        }
		}
	}

	public String toString() {
		String info = getName() + " version " + getBuildVersion();

		String buildNumber = getBuildNumber();
		if (!UNKNOWN.equals(buildNumber)) {
			info += ", Build: " + buildNumber;
		}
		String buildDate = getBuildDate();
		if (!UNKNOWN.equals(buildDate)) {
			info += ", " + buildDate;
		}
		return info;
	}

	/**
	 * @return the name and the build version.
	 */
	public String toShortString() {
		return getName() + " v" + getBuildVersion();
	}

	/**
	 * Gets the name of the application.
	 */
	public String getName() {
		return properties.getProperty(PROGRAM_NAME, defaultAppName);
	}

	/**
	 * Gets the build date of the application.
	 */
	public String getBuildDate() {
		return properties.getProperty(BUILD_DATE, UNKNOWN);
	}

	/**
	 * Sets the build date. <b>Note</b>: the properties file never gets saved with any changes.
	 * @param date
	 */
	public void setBuildDate(String date) {
		properties.setProperty(BUILD_DATE, date);
	}

	/**
	 * gets the build number of the application.
	 */
	public String getBuildNumber() {
		return properties.getProperty(BUILD_NUMBER, UNKNOWN);
	}
	/**
	 * Sets the build number. <b>Note</b>: the properties file never gets saved with any changes.
	 * @param number
	 */
	public void setBuildNumber(String number) {
		properties.setProperty(BUILD_NUMBER, number);
	}

	/**
	 * Gets the build version of the application.
	 */
	public String getBuildVersion() {
		return properties.getProperty(BUILD_VERSION, UNKNOWN);
	}

	/**
	 * Sets the build version. <b>Note</b>: the properties file never gets saved with any changes.
	 * @param version
	 */
	public void setBuildVersion(String version) {
		properties.setProperty(BUILD_VERSION, version);
	}

}
