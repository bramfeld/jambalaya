/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.ScriptingBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.jambalaya.JambalayaProject;

/**
 * This represents the "interface" which holds the reference to
 * the Script Engines.  UI calls this as do individual buttons.
 *
 * @author Neil Ernst, Chris Callendar
 */
public class ScriptingBean {

	private ScriptingConstants scriptingConstants;
	private DefaultMainViewScriptingBean svsb;
	private BSFManager manager;
	private String language;

	public ScriptingBean(ShrimpProject project) {
		language = "javascript";
		scriptingConstants = new ScriptingConstants();
		svsb = new DefaultMainViewScriptingBean(project);

		try {
            manager = new BSFManager();
            manager.registerBean("mainViewScriptingBean", svsb);
            manager.registerBean("scriptingConstants", scriptingConstants);
            // for legacy scripts
            manager.registerBean("shrimpConstants", scriptingConstants);
        } catch (ExceptionInInitializerError e) {
        	// thrown when Shrimp is an applet
            //e.printStackTrace();
        } catch (NoClassDefFoundError e) {
        	// thrown second constructor called from applet
            //e.printStackTrace();
        }

        // @tag Jambalaya.Scripting.ClassLoader
        // Jambalaya only - workaround to load js.jar properly
        // Have to use Protege's class loader instead of the default one
        if ((manager != null) && (project instanceof JambalayaProject)) {
        	useProtegeClassLoader();
		}
	}

	/**
	 * This method tells the {@link BSFManager} to use a custom class loader.
	 * This {@link ClassLoader} uses Protege's {@link PluginUtilities#forName(String, boolean)}
	 * to load classes.  For some reason the <b>js.jar</b> file doesn't get loaded properly otherwise.
	 * @tag Jambalaya.Scripting.ClassLoader
	 */
	private void useProtegeClassLoader() {
	/*
		try {
			ClassLoader cl = new ClassLoader(manager.getClassLoader()) {
				public Class loadClass(String name) throws ClassNotFoundException {
					Class cls = PluginUtilities.forName(name, true);
					if (cls == null) {
						cls = getParent().loadClass(name);
					}
					return cls;
				}
			};
			manager.setClassLoader(cl);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	*/
	}

	/**
	 * return reference to the SV scripting bean,
	 * home of the actions.  Called from ShrimpView
	 * (where most adapters are hosted).
	 */
	public DefaultMainViewScriptingBean getShrimpViewScriptingBean() {
		return svsb;
	}

	/**
	 * Runs the given script file.
	 * @param scriptFile the script file to run
	 */
	public void runScriptFile(File scriptFile) throws FileNotFoundException, IOException, BSFException {
		runScriptFile(scriptFile.getAbsolutePath());
	}

	/**
	 * Allows others to call this object with a filename to run.
	 * @param fileName the path to the script file
	 */
	public void runScriptFile(String fileName) throws FileNotFoundException, IOException, BSFException {
		// convert this file into a big string
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		StringBuffer allLines = new StringBuffer();
		String line = null;
		do {
			line = reader.readLine();
			if (line != null) {
				allLines.append(line + "\n");
			}
		} while (line != null);
		String language = BSFManager.getLangFromFilename(fileName);
		runScriptString(allLines.toString(), language);
	}

	/**
	 * Runs the script with the current language.
	 * @see ScriptingBean#getLanguage()
	 * @param scriptAsString the script string
	 * @throws BSFException
	 */
	public void runScriptString(String scriptAsString) throws BSFException {
		runScriptString(scriptAsString, getLanguage());
	}

	/**
	 * Runs the script with the given scripting language.
	 * @param scriptAsString the script string
	 * @param language the scripting language to use
	 * @throws BSFException
	 */
	public void runScriptString(String scriptAsString, String language) throws BSFException {
		manager.exec(language, "", 1, 1, scriptAsString);
	}

	/**
	 * @return The scripting language the bean is using.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param string the scripting language to set
	 */
	public void setLanguage(String string) {
		language = string;
	}

}