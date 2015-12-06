/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import ca.uvic.csr.shrimp.resource.ResourceHandler;

/**
 * PRJFile: This class defines a project file utility class that keeps track
 * of locations storing the information associated with an Rigi project, such
 * as domain directory, code directory, and so on.
 *
 * @author Anton An
 * @date July 26, 2000
 */
public class PRJFile {

	public static final String DEFAULT_RSF_STYLE = "Structured";

    // the path of the project file
    final private String basePath;

    // the default package to look for RSF parsers in
    final static String defaultPackage = "ca.uvic.csr.shrimp.PersistentStorageBean.PRJPersistentStorageBean";

    // The img directory of the project file.
    private String imgDir;

    // The doc url directory of the project file.
    private String docDir;

    // The code url directory of the project file.
    private String codeDir;

	// The actual source directory of the project file.
    private String sourceDir;

    // The RSF file referenced by the project file.
    private String rsfFile;

    // The RSF domain directory of the project file.
    private String domainDir;

    // The uml url directory of the project file.
    private String umlDir;

    // specify the kind of RSF file:  Structured, ThreeTupleRSF, or KeyValueRSF
    private String rsfStyle = DEFAULT_RSF_STYLE;


    public PRJFile(URI uri) {

		if(uri == null) {
			throw new NullPointerException("The argument can not be null!");
		}
		if(!uri.toString().endsWith(".prj")) {
			throw new IllegalArgumentException("The specified file must end with .prj!");
		}

        basePath = uri.toString().substring(0, uri.toString().lastIndexOf('/'));

        try {
			parseFile(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void parseFile(URI uri) throws IOException {
		BufferedReader reader = ResourceHandler.getReader(uri);
		if (reader == null) {
		    return;
		}
		String line;
		while((line = reader.readLine()) != null) {
            parseLine(line);
        }
		reader.close();
	}



    private void parseLine(String line) {
		final StringTokenizer st = new StringTokenizer(line, " :\t\n\r\f");

		while(st.hasMoreTokens()) {
			final String token = st.nextToken();
			if (token.equals("RSF-File")) {
				rsfFile = st.nextToken();
				while (st.hasMoreElements()) {
					rsfFile += " " + st.nextToken();
				}
			} else if(token.equals("RSF-Style")) {
				rsfStyle = st.nextToken();
			} else if(token.equals("RSF-Domain")) {
				domainDir = st.nextToken();
			} else if(token.equals("IMG-Directory")) {
				imgDir = st.nextToken();
			} else if(token.equals("DOC-Directory")) {
				docDir = st.nextToken();
			} else if(token.equals("CODE-Directory")) {
				codeDir = st.nextToken();
			} else if(token.equals("SOURCE-Directory")) {
				sourceDir = st.nextToken();
			} else if(token.equals("LAYOUT_UML-Directory")) {
				umlDir = st.nextToken();
			}
		}
	}

    public RSF getRSFStyle() {
        // make sure it's not garbage
        if (rsfStyle == null) {
			rsfStyle = "Structured";
		}
        if (rsfStyle.length() < 1) {
			rsfStyle = "Structured";
		}

        // see if it's a fully qualified class name
        // if not, assume it's in the PRJPersistentStorageBean package
        final String className;
        if (rsfStyle.indexOf('.') == -1) {
            className = defaultPackage + "." + rsfStyle + "RSF";
        } else {
            className = rsfStyle + "RSF";
        }

        try {
            final Class c = Class.forName(className);
            final RSF rsf = (RSF) c.newInstance();
            rsf.setPRJFile(this);
            return rsf;

        } catch (Exception e) {
            System.err.println("Exception determining RSF-Style: ");
            System.err.println("   " + e.getClass().toString());
            System.err.println("   " + e.getMessage());
            System.err.println("   " + e.toString());
            System.err.println("Using Structured RSF-Style (default).");
            final RSF rsf = new StructuredRSF();
            rsf.setPRJFile(this);
            return rsf;
        }

    }


    /** Gets the RSF file referenced by this PRJ file. */
    public URI getRSFFile() {
        URI uri = null;
        if (rsfFile != null) {
	        try {
	            uri = new URI (basePath + "/" + rsfFile);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
	}

    /** Gets the RSF domain directory referenced by this PRJ file. */
    public URI getRSFDomain() {
        URI uri = null;
        if (domainDir != null) {
	        try {
	            uri = new URI (basePath + "/" + domainDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        if (uri == null || !new File(uri).exists()) {
            // try getting the domain dir relative to the application instead
            File f = ResourceHandler.getFile(domainDir);
            if (f.exists()) {
                uri = f.toURI();
            }
        }
        return uri;
    }

    /** Gets the relevant image directory referenced by this PRJ file. */
    public URI getImgDirectory() {
        URI uri = null;
        if (imgDir != null) {
	        try {
	            uri = new URI (basePath + "/" + imgDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
    }

    /** Gets the relevant doc url directory referenced by this PRJ file. */
    public URI getDocDirectory() {
        URI uri = null;
        if (docDir != null) {
	        try {
	            uri = new URI(basePath + "/" + docDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
    }

    /** Gets the relevant uml url directory referenced by this PRJ file. */
    public URI getUmlDirectory() {
        URI uri = null;
        if (umlDir != null) {
	        try {
	            uri = new URI(basePath + "/" + umlDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
    }

    /** Gets the relevant code url directory referenced by this PRJ file. */
    public URI getCodeDirectory() {
        URI uri = null;
        if (codeDir != null) {
	        try {
	            uri = new URI(basePath + "/" + codeDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
    }

    /**
     * Gets the relevant code url directory referenced by this PRJ file.
     */
    public URI getSourceDirectory() {
        URI uri = null;
        if (sourceDir != null) {
	        try {
	            uri = new URI(basePath + "/" + sourceDir);
	        } catch (URISyntaxException e) {
	            e.printStackTrace();
	        }
        }
        return uri;
    }


    ///////////////////////  COMMON STUFF FOR PARSERS /////////////////


    URI constructImageURI( String value) {
        return constructURI(value, getImgDirectory());
    }

    URI constructCodeURI(String value) {
        return constructURI(value, getCodeDirectory());
    }

    URI constructDocURI(String value) {
        return constructURI(value, getDocDirectory());
    }

    URI constructUmlURI(String value) {
        return constructURI(value, getUmlDirectory());
    }

    /**
     * @param value
     * @param dirURI
     * @return concatenation of dirURI and value
     */
    private URI constructURI(String value, URI dirURI) {
        URI uri = null;
        try {
            uri = new URI (dirURI.toString() + '/' + value);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri;
    }



}//class PRJFile
