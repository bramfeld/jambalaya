/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.NodeShape;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashMap;

/**
 * Caches images.
 * 
 * @author Chris Callendar
 * @date 12-Jan-07
 */
public class NodeImage {

	public static final String NO_SCALING = "Normal";
	public static final String CENTERED = "Centered";
	public static final String STRETCHED = "Stretched";
	public static final String TILED = "Tiled";
	
	private static final HashMap imageMap = new HashMap();

	private String imagePath = "";
	private String imageSizing = STRETCHED;
	private boolean fillBackground = true;
	private boolean drawOuterBorder = true;
	private boolean drawInnerBorder = false;
	
	public NodeImage() {
	}
	
	public NodeImage(String fullString) {
		parseAndUpdate(this, fullString);
	}

	public NodeImage(String imagePath, String imageSizing, boolean fillBackground, boolean drawBorder) {
		setImagePath(imagePath);
		this.imageSizing = imageSizing;
		this.fillBackground = fillBackground;
		this.drawOuterBorder = drawBorder;
	}

	private static void removeImage(String path) {
		if (imageMap.containsKey(path)) {
			Image image = (Image) imageMap.get(path);
			if (image != null) {
				image.flush();
				image = null;
			}
			imageMap.put(path, (Image)null);
		}
	}

	private static void addImagePath(String path) {
		if (!imageMap.containsKey(path)) {
			imageMap.put(path, (Image)null);
		}
	}
	
	private static Image loadImage(String path) {
		Image image = null;
		if (imageMap.containsKey(path)) {
			image = (Image) imageMap.get(path);
			if ((image == null) && (path.length() > 0)) {
				File file = new File(path);
				if (file.exists()) {
					image = Toolkit.getDefaultToolkit().createImage(path);
					//System.out.println("Loaded image: " + path);
					imageMap.put(path, image);
				}
			}
		}
		return image;
	}
	
	public static void parseAndUpdate(NodeImage nodeImage, String str) {
		String path = "";
		String imageSizing = STRETCHED;
		boolean fill = true;
		boolean drawOuter = true;
		boolean drawInner = false;
		String[] split = str.split(";");
		if (split.length >= 2) {
			path = split[0];
			imageSizing = validateImageSizing(split[1]);
			if (split.length >= 4) {
				fill = "true".equals(split[2]);
				drawOuter = "true".equals(split[3]);
			}
			if (split.length >= 5) {
				drawInner = "true".equals(split[4]);
			}
		}
		nodeImage.setImagePath(path);
		nodeImage.setImageSizing(imageSizing);
		nodeImage.setFillBackground(fill);
		nodeImage.setDrawOuterBorder(drawOuter);
		nodeImage.setDrawInnerBorder(drawInner);
	}
	
	private static String validateImageSizing(String imageSizing) {
		if (CENTERED.equals(imageSizing) || STRETCHED.equals(imageSizing) ||
			TILED.equals(imageSizing) || NO_SCALING.equals(imageSizing)) {
			return imageSizing;
		}
		return STRETCHED;
	}

	public String toString() {
		return imagePath + ";" + imageSizing + ";" + fillBackground + ";" +
			drawOuterBorder + ";" + drawInnerBorder;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public void setImagePath(String imagePath) {
		imagePath = (imagePath == null ? "" : imagePath);
		if (!this.imagePath.equalsIgnoreCase(imagePath)) {
			removeImage(this.imagePath);
			this.imagePath = imagePath;
			addImagePath(this.imagePath);
		}
	}

	public boolean isFillBackground() {
		return fillBackground;
	}
	
	public void setFillBackground(boolean fillBackground) {
		this.fillBackground = fillBackground;
	}

	
	public boolean isDrawOuterBorder() {
		return drawOuterBorder;
	}

	public boolean isDrawInnerBorder() {
		return drawInnerBorder;
	}

	public void setDrawOuterBorder(boolean drawBorder) {
		this.drawOuterBorder = drawBorder;
	}

	public void setDrawInnerBorder(boolean drawBorder) {
		this.drawInnerBorder = drawBorder;
	}

	public Image getImage() {
		return loadImage(imagePath);
	}
	
	public String getImageSizing() {
		return imageSizing;
	}

	
	public void setImageSizing(String imageSizing) {
		this.imageSizing = imageSizing;
	}

	public void flush() {
		removeImage(imagePath);
	}

	public boolean hasImage() {
		return (imagePath.length() > 0);
	}

	/**
	 * Resets back to the default node image.
	 */
	public void reset() {
		if (imagePath.length() > 0) {
			removeImage(imagePath);
		}
		imagePath = "";
		imageSizing = STRETCHED;
		fillBackground = true;
		drawOuterBorder = true;
		drawInnerBorder = false;
	}
	
}
