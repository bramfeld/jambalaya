/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.resource;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Rob Lintern
 */
public class ResourceHandler {

    private static Component mediaTrackerComponent = new JPanel();

    private static MediaTracker mediaTracker = new MediaTracker(mediaTrackerComponent);

    private static int nextImageID = 0;

	/**
	 * Converts the {@link URL} into a {@link URI} using the {@link ResourceHandler#toURI(URL)}
	 * method and then passes the URI into the {@link File} constructor.
	 * @param url
	 * @return the File or null if the URL couldn't be converted into a URI
	 */
	public static File toFile(URL url) {
		return toFile(toURI(url));
	}

	/**
	 * Converts the {@link URI} into a File.
	 * In the case when the uri is a location inside a jar file it should return
	 * the location of the jar file.
	 * @param uri
	 * @return the File or null if the URL couldn't be converted into a URI
	 */
	public static File toFile(URI uri) {
		File file = null;
		if (uri != null) {
			try {
				file = new File(uri);
			} catch (IllegalArgumentException e) {
				// uri might be a jar file
				String[] paths = getJarAndRelativePath(uri);
				file = new File(paths[0]);
			}
		}
		return file;
	}

	/**
	 * Converts the {@link URL} into a {@link URI}.
	 * @param url
	 * @return the URI or null if a {@link URISyntaxException} occurred.
	 */
	public static URI toURI(URL url) {
		URI uri = null;
		try {
			String urlStr = url.toString();
			// must convert it to a valid string - no spaces!
			if (urlStr.indexOf(" ") != -1) {
				urlStr = urlStr.replaceAll(" ", "%20");
			}
			//uri = url.toURI();		// @tag Shrimp.Java5.toURI
			uri = new URI(urlStr);
		} catch (URISyntaxException e) {
			System.err.println("Warning - couldn't convert URL to URI: " + e.getMessage());
		}
		return uri;
	}

    /**
     * Loads and returns an Icon with the given name and path. If the icon
     * cannot be loaded, null is returned.
     */
    public static ImageIcon getIcon(String name) {
        ImageIcon icon = null;
        if (name != null) {
	        name = "icons/" + name;
	        URL url = ResourceHandler.class.getResource(name);
	        // ImageIcon uses a media tracker
	        icon = (url != null) ? new ImageIcon(url) : new ImageIcon(name);
        }
        return icon;
    }

    /**
     * Loads an image from the "icons" directory and returns it.
     * @param name the name of the image relative to the icons directory (e.g. "icon_close.gif")
     * @return Image or null if not found
     */
    public static Image getIconAsImage(String name) {
    	ImageIcon icon = getIcon(name);
    	if (icon != null) {
    		return icon.getImage();
    	}
    	return null;
    }

    /**
     * Gets all the icons and their filenames that are found in the "icons" directory
     * that start with the given text.
     * @param startingWith the starting text for the icon filenames.
     * @return an array of {@link IconFilename} objects
     * @throws Exception
     */
    public static IconFilename[] getIcons(String startingWith) throws Exception {
    	URL url = ResourceHandler.class.getResource("icons/");
    	return getIcons(startingWith, url);
    }

    public static IconFilename[] getIcons(final String startingWith, URL url) throws Exception {
    	IconFilename[] icons = new IconFilename[0];
    	if (url.getProtocol().startsWith("jar")) {
    		return getIconsFromJarFile(url, startingWith);
    	}

    	File dir = toFile(url);
    	if (dir.exists()) {
    		FilenameFilter filter = new FilenameFilter() {
    			public boolean accept(File dir, String name) {
    				return name.toLowerCase().startsWith(startingWith.toLowerCase());
    			}
    		};
    		String[] files = dir.list(filter);
    		if ((files != null) && (files.length > 0)) {
    			icons = new IconFilename[files.length];
    			for (int i = 0; i < files.length; i++) {
    				File file = new File(dir, files[i]);
    				icons[i] = new IconFilename(file.getName(), new ImageIcon(file.getAbsolutePath()));
    			}
    		}
    	}
    	return icons;
    }

    /**
     * Attempts to get all the icons from the shrimp jar file inside the icons directory that start with
     * the given string.
     * @param url the url of the icons directory (inside the jar file).
     * @param startingWith
     * @return array of {@link IconFilename} objects
     * @throws Exception
     */
	protected static IconFilename[] getIconsFromJarFile(URL url, String startingWith) throws Exception {
		IconFilename[] icons = new IconFilename[0];
		// must decode in case there is a space in the path (e.g. "Program Files" is really "Program%20Files")
		String[] paths = getJarAndRelativePath(toURI(url));
		String jarPath = paths[0];
		String relPath = paths[1];
		//System.out.println("Jar: " + jarPath + "  Relative path: " + relPath);

		File file = new File(jarPath);
		//System.out.println("Jar File = " + file.getAbsolutePath() + "  exists? " + file.exists() + "  canRead? " + file.canRead());
		if (file.exists() && file.canRead()) {
			LinkedList iconList = new LinkedList();	// list of icon filenames
			JarFile jar = new JarFile(file);
			String iconStart = relPath + startingWith;
			for (Enumeration en = jar.entries(); en.hasMoreElements(); ) {
				JarEntry entry = (JarEntry) en.nextElement();
				String name = entry.getName();
				if (name.startsWith(iconStart)) {
					int lastSlash = name.lastIndexOf('/');
					if (lastSlash != -1) {
						name = name.substring(lastSlash + 1);
					}
					try {
						Icon icon = getIconFromStream(jar.getInputStream(entry));
						//System.out.println("Got icon from jar file: " + (icon == null ? "null" : name));
						IconFilename iconFilename = new IconFilename(name, icon);
						iconList.add(iconFilename);
					} catch (Exception e) {
						System.err.println("Error getting icon [" + name + "] from jar file: " + jarPath);
						System.err.println("Error message: " + e.getMessage());
					}
				}
			}

			icons = (IconFilename[]) iconList.toArray(new IconFilename[iconList.size()]);
		}
		return icons;
	}

	/**
	 * Gets the {@link File} object for the jar file defined by the {@link URL}.
	 * It shouldn't matter if the file is not a jar file.
	 * @param url
	 * @return File the jar file
	 * @throws URISyntaxException
	 */
	public static File getJarFile(URL url) {
		String paths[] = getJarAndRelativePath(toURI(url));
		return new File(paths[0]);
	}

	/**
	 * Decodes the URI into a String, then splits it into the jar file path and the relative path inside the jar file.
	 * If the URI is not of a jar file, then the second parameter returned (relative path) will be the empty string.
	 * @param uri
	 * @return String array with two values: the jar file path, and the relative path inside the jar file
	 */
	public static String[] getJarAndRelativePath(URI uri) {
		String urlStr = uri.toString();
		try {
			// must decode in case there is a space in the path (e.g. "Program%20Files" is really "Program Files")
			urlStr = URLDecoder.decode(urlStr, "utf-8");
		} catch (UnsupportedEncodingException ignore) {
		}

		String jarPath = urlStr;
		String relPath = "";
		String lower = jarPath.toLowerCase();

		// url will be something like this - 'jar:file:/C:/temp/shrimp.jar!/ca/uvic/csr/shrimp/resource/icons/'
		if (lower.startsWith("jar:")) {
			jarPath = jarPath.substring(4);
			lower = lower.substring(4);
		}
		if (lower.startsWith("file:/")) {
			jarPath = jarPath.substring(6);
			lower = lower.substring(6);
		}
		// now it will be something like - 'C:/temp/shrimp.jar!/ca/uvic/csr/shrimp/resource/icons/'
		int ex = jarPath.indexOf('!');
		if (ex != -1) {
			relPath = jarPath.substring(ex + 2);	// e.g. 'ca/uvic/csr/shrimp/resource/icons/'
			jarPath = jarPath.substring(0, ex);		// e.g. 'C:/temp/shrimp.jar'
		}
		//System.out.println("Jar: " + jarPath + "  Relative path: " + relPath);

		return new String[] { jarPath, relPath };
	}

	/**
	 * Reads an Icon in from a jar file using a input stream.
	 * @param path the relative path of the image to this {@link ResourceHandler} class in the jar file
	 * @param size the size of the image (uncompressed size)
	 * @return Icon or null
	 */
	protected static Icon getIconFromJarFile(String path, long size) throws Exception {
		size = (size == -1 ? 15000 : size);
		BufferedInputStream imgStream = new BufferedInputStream(getIconAsStream(path));
		if (imgStream != null) {
			byte buf[] = new byte[(int)size];
			imgStream.read(buf); //throws exception in applet

			try {
				imgStream.close();
			} catch (IOException ieo) {}

			return new ImageIcon(Toolkit.getDefaultToolkit().createImage(buf));
		} else {
			throw new NullPointerException("Image input stream is null");
		}
	}

	/**
	 * Returns a new {@link Icon} from the input stream.
	 * @param stream
	 * @return {@link Icon}
	 * @throws IOException
	 */
	private static Icon getIconFromStream(InputStream stream) throws IOException {
		return new ImageIcon(ImageIO.read(stream));
	}

	public static InputStream getIconAsStream(String icon) {
    	return ResourceHandler.class.getResourceAsStream("icons/"+ icon);
    }

    public static URL getFileURL(String fileName) {
        URL url = null;
        File file = new File(fileName);
        try {
            if (file.exists()) {
                try {
                    URI uri = file.toURI();
                    url = uri.toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException e) { //thrown when shrimp is an applet
            //e.printStackTrace();
        }

        if (url == null) {
            url = ResourceHandler.class.getClassLoader().getResource(fileName);
            // Works when ResourceHandler is in a jar file and the file
            // specified is on local drive (not in jar file)
            // eg Protege/plugins/ca.uvic.cs.chisel.jambalaya/jambalaya.jar uses
            // script file Protege/plugins/scripts/snapshot_scripts.js
            //System.out.println("ResourceHandler.class.getClassLoader().getResource(fileName):
            // " + url);
        }

        if (url == null) {
            url = ResourceHandler.class.getResource(fileName);
            //Gets the file bin/ca/uvic/csr/shrimp/resource/name
            //works when ResourceHandler is not in a jar file (ie. running and
            // debugging from eclipse)
            //System.out.println("ResourceHandler.class.getResource(fileName):
            // " + url);
        }
        return url;
    }

    public static BufferedReader getReader(URI uri) {
        BufferedReader reader = null;
        try {
            URL url = uri.toURL();
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }

    public static BufferedReader getFileReader(String fileName) {
        BufferedReader reader = null;
        URL url = getFileURL(fileName);
        if (url != null) {
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return reader;
    }

    public static String getFileContents(String fileName) {
        StringBuffer fileContents = new StringBuffer();
        BufferedReader in = getFileReader(fileName);
        if (in != null) {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    fileContents.append(inputLine);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return fileContents.toString();
    }

    public static URI getFileURI(String fileName) {
        URI uri = null;
        URL url = getFileURL(fileName);
        if (url != null) {
            try {
                uri = new URI(url.toString());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    /**
     * Gets an image specified by the given url
     */
    public static Image getURLImage(URL url) {
        Image image = null;
        if (url == null || url.toString().equals("")) {
			return image;
		}
        // use the media tracker to wait until this image is loaded
        try {
            image = Toolkit.getDefaultToolkit().getImage(url);
            mediaTracker.addImage(image, nextImageID);
            mediaTracker.waitForID(nextImageID);
            nextImageID++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Image getURLImageThumbnail(URL url) {
        Image image = null;
        // use the media tracker to wait until this image is loaded
        try {
            url = new URL(url.toString() + ".thm.jpg");
            image = Toolkit.getDefaultToolkit().getImage(url);
            mediaTracker.addImage(image, nextImageID);
            mediaTracker.waitForID(nextImageID);
            nextImageID++;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * Gets an image with the given name relative to the location of this .class
     * file (ie. in .../ca/uvic/csr/shrimp/gui/images) A null image is returned
     * if none found with the given name
     */
    public static Image getResourceImage(String name) {
        Image image = null;
        if ((name != null) && !"".equals(name)) {
        	if (!name.startsWith("images/")) {
        		name = "images/" + name;
        	}
	        try {
	        	URL url = ResourceHandler.class.getResource(name);
	        	image = (url != null) ? Toolkit.getDefaultToolkit().getImage(url) : Toolkit.getDefaultToolkit().getImage(name);
	        	// use the media tracker to wait until this image is loaded
	            mediaTracker.addImage(image, nextImageID);
	            mediaTracker.waitForID(nextImageID);
	            nextImageID++;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        }
        return image;
    }

    public static File getFile(String fileName) {
        File file = null;
        file = new File(fileName);
        if (file.exists()) {
            return file;
        }
        URL url = getFileURL(fileName);
        if (url != null) {
            try {
                // get the file - doesn't matter if it isn't really a jar file
                file = getJarFile(url);
            } catch (Exception e) {
                e.printStackTrace();
			}
        }
        return file;
    }

}