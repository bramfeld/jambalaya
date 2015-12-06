/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.event.HyperlinkListener;

import ca.uvic.csr.shrimp.resource.ResourceHandler;


/**
 * Holds information about a document which can be displayed inside a node's content panel.
 *
 * @author Chris Callendar
 * @date 27-Sep-07
 */
public class NodeDocument implements Comparable {

	public static final String TYPE_UNKNOWN = "unknown";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMAGE = "image";
	public static final String TYPE_RTF = "rtf";
	public static final String TYPE_WEB = "web";
	public static final String TYPE_PDF = "pdf";
	public static final String TYPE_URL = "url";
	public static final String TYPE_ANNOTATION = "annotation";

	private static final JFileChooser CHOOSER = new JFileChooser();

	protected Icon icon;
	protected String path;
	protected String filename;
	protected String directory;
	protected String type;
	private boolean isURL;

	public NodeDocument() {
		icon = null;
		path = "";
		filename = "";
		directory = "";
		type = TYPE_UNKNOWN;
		isURL = false;
	}

	public NodeDocument(String path) {
		this.path = path;
		this.isURL = isURL(path);
		if (isURL) {
			this.type = TYPE_URL;
			this.icon = getIconForType(type);
			setDirectoryAndFilenameFromURL(path);
		} else {
	    	File file = new File(path);
			this.filename = file.getName();
			this.directory = file.getParentFile().getAbsolutePath();
			this.icon = getIcon(file);
			this.type = determineFileType(filename);
		}
    }

	public int compareTo(Object obj) {
		if (obj instanceof NodeDocument) {
			return path.compareTo(((NodeDocument)obj).getPath());
		}
		return 0;
	}

	public boolean equals(Object obj) {
		boolean eq = super.equals(obj);
		if (!eq && (obj instanceof NodeDocument) && (compareTo(obj) == 0)) {
			eq = true;
		}
		return eq;
	}

	private void setDirectoryAndFilenameFromURL(String path) {
		this.filename = path;
		this.directory = path;
		try {
			URI uri = new URI(path);
			this.filename = uri.getPath();
			if (filename.length() == 0) {
				filename = uri.getHost();
			}
			if (filename.endsWith("/")) {
				filename = filename.substring(0, filename.length() - 1);
			}
			int slash = filename.lastIndexOf('/');
			if (slash != -1) {
				filename = filename.substring(slash + 1);
				int fn = directory.indexOf(filename);
				if (fn != -1) {
					directory = directory.substring(0, fn);
				}
			}
		} catch (Exception ex) {
		}
	}

	public static boolean isURL(String url) {
		String lower = (url != null ? url.toLowerCase() : "");
		boolean isURL = ((lower.length() > 0) && (lower.startsWith("http://") ||
				lower.startsWith("https://") || lower.startsWith("ftp://") ||
				lower.startsWith("sftp://") || lower.startsWith("mailto:")));
		return isURL;
	}

	public static Icon getIcon(String path) {
		if (isURL(path)) {
			return getIconForType(TYPE_URL);
		}
		File file = new File(path);
		return getIcon(file);
	}

	public static Icon getIcon(File file) {
		// first try to get the icon from a file choosre
		Icon icon = CHOOSER.getIcon(file);
		if (icon == null) {
			// now use our predefined icons
			String type = determineFileType(file.getAbsolutePath());
			icon = getIconForType(type);
		}
		return icon;
	}

    protected static Icon getIconForType(String type) {
    	String name;
    	if (TYPE_TEXT.equals(type)) {
    		name = "icon_file_text.gif";
    	} else if (TYPE_IMAGE.equals(type)) {
    		name = "icon_file_image.gif";
    	} else if (TYPE_RTF.equals(type)) {
    		name = "icon_file_rtf.gif";
    	} else if (TYPE_WEB.equals(type) || TYPE_URL.equals(type)) {
    		name = "icon_file_web.gif";
    	} else if (TYPE_PDF.equals(type)) {
    		name = "icon_file_pdf.gif";
    	} else if (TYPE_ANNOTATION.equals(type)) {
    		name = "icon_annotation_shrimp.gif";
    	} else {
    		name = "icon_file.gif";
    	}
    	Icon icon = ResourceHandler.getIcon(name);
    	return icon;
    }

	private static String determineFileType(String name) {
		String type = TYPE_UNKNOWN;
		int dot = name.lastIndexOf(".");
		String ext = name.substring(dot + 1).toLowerCase();
		if ("txt".equals(ext) || "java".equals(ext) || "js".equals(ext) || "css".equals(ext) ||
			"cs".equals(ext) || "properties".equals(ext) || "csv".equals(ext)) {
			type = TYPE_TEXT;
		} else if ("jpg".equals(ext) || "jpeg".equals(ext) || "gif".equals(ext) || "png".equals(ext) || "bmp".equals(ext)) {
			type = TYPE_IMAGE;
		} else if ("rtf".equals(ext)) {
			type = TYPE_RTF;
		} else if ("xml".equals(ext) || "htm".equals(ext) || "html".equals(ext) || "xsl".equals(ext)) {
			type = TYPE_WEB;
		} else if ("pdf".equals(ext)) {
			type = TYPE_PDF;
		} else {
			type = TYPE_UNKNOWN;
		}
		return type;
	}

	public String toString() {
		return filename;
	}

	/**
	 * Returns the directory
	 * @return String the directory only, no filename
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Returns the filename
	 * @return String the filename only, no directory
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns the path
	 * @return String the full path (filename and directory)
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the type
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the icon for this document.
	 */
	public Icon getIcon() {
		return icon;
	}

	public URL getURL() throws MalformedURLException {
		if (isURL) {
			return new URL(path);
		}
		return new File(path).toURI().toURL();
	}

	/**
	 * @return true if the file exists
	 */
	public boolean exists() {
		if (isURL) {
			return true;	// don't check urls!
		}
		return new File(path).exists();
	}

	/**
	 * Returns true if the document can be removed.
	 * @return true
	 */
	public boolean canRemove() {
		return true;
	}

	/**
	 * Returns true if the document can be opened.
	 * @return true
	 */
	public boolean canOpen() {
		return true;
	}

	/**
	 * Returns true if the node can be editted.
	 * @return false
	 */
	public boolean canEdit() {
		return false;
	}

	/**
	 * Saves the new content, does nothing.
	 * @param newContent
	 */
	public void setContent(String newContent) {
		// does nothing
	}

	/**
	 * Returns the content of the node document.
	 * This is null by default.
	 * @return null
	 */
	public String getContent() {
		return null;
	}

	/**
	 * @return true if there is non blank content
	 */
	public boolean hasContent() {
		return false;
	}

	/**
	 * The content type for the content.
	 * @return text/plain
	 */
	public String getContentType() {
		return "text/plain";
	}

	public boolean isAnnotation() {
		return TYPE_ANNOTATION.equals(getType());
	}

	/**
	 * Whether the document should be saved in the project properties.
	 * @return true
	 */
	public boolean saveInProperties() {
		return true;
	}

	/** Not implemented. */
	public HyperlinkListener getHyperlinkListener() {
		return null;
	}

}
