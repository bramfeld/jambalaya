/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada. All rights reserved.
 */
package ca.uvic.csr.shrimp.usercontrols;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import ca.uvic.csr.shrimp.DisplayBean.DisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PNestedDisplayBean;
import ca.uvic.csr.shrimp.DisplayBean.PNestedDisplayBean.PShrimpArc;
import ca.uvic.csr.shrimp.ShrimpApplication.ApplicationAccessor;
import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ShrimpTool;
import ca.uvic.csr.shrimp.resource.ResourceHandler;
import ca.uvic.csr.shrimp.util.ShrimpFileFilter;
import edu.umd.cs.piccolo.PCanvas;

/**
 * Exports the current view to an image file.
 *
 * @author Rob Lintern
 */
public class ExportToImageFileAction extends DefaultProjectAction {

	private String toolName;
	private FileFilter jpegFileFilter = null;
	private FileFilter pngFileFilter = null;
	private FileFilter gifFileFilter = null;
	private File lastDirectory = null;

	public ExportToImageFileAction(ShrimpProject project, String toolName) {
		super("Export to Image File...", ResourceHandler.getIcon("icon_export.gif"), project);
		this.toolName = toolName;
	}

	public void startAction() {
		boolean jpegAvailable = false;
		boolean pngAvailable = false;
		boolean gifAvailable = false;
		String[] formats = ImageIO.getWriterFormatNames();
		for (int i = 0; i < formats.length; i++) {
			String format = formats[i];
			if (format.toLowerCase().equals("jpeg") || format.toLowerCase().equals("jpg")) {
				jpegAvailable = true;
			} else if (format.toLowerCase().equals("png")) {
				pngAvailable = true;
			} else if (format.toLowerCase().equals("gif")) {
				gifAvailable = true;
			}
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);

		String allFormats = "";
		if (jpegAvailable) {
			allFormats += "JPG ";
		}
		if (gifAvailable) {
			allFormats += "GIF ";
		}
		if (pngAvailable) {
			allFormats += "PNG";
		}
		if (allFormats.length() > 0) {
			allFormats = " (" + allFormats.trim() + ")";
		}
		
		chooser.setDialogTitle("Export to Image File" + allFormats);
		File dir = (lastDirectory != null ? lastDirectory : new File(System.getProperty("user.dir")));
		chooser.setCurrentDirectory(dir);

		if (pngAvailable) {
			pngFileFilter = new ShrimpFileFilter(new String[] { "png" }, "PNG Images");
			chooser.addChoosableFileFilter(pngFileFilter);
		}
		if (gifAvailable) {
			gifFileFilter = new ShrimpFileFilter(new String[] { "gif" }, "GIF Images");
			chooser.addChoosableFileFilter(gifFileFilter);
		}

		if (jpegAvailable) {
			jpegFileFilter = new ShrimpFileFilter(new String[] { "jpg", "jpeg" }, "JPEG Images");
			chooser.addChoosableFileFilter(jpegFileFilter);
		}

		int state = chooser.showSaveDialog(ApplicationAccessor.getParentFrame());
		if (state == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			lastDirectory = file.getParentFile();
			FileFilter fileFilter = chooser.getFileFilter();
			String filePath = file.toString();
			String formatName = "";
			if (jpegFileFilter != null && jpegFileFilter.equals(fileFilter)) {
				formatName = "jpg";
				if (!filePath.toLowerCase().endsWith(".jpg") && !filePath.toLowerCase().endsWith(".jpeg")) {
					filePath += ".jpg";
					file = new File(filePath);
				}
			}
			if (pngFileFilter != null && pngFileFilter.equals(fileFilter)) {
				formatName = "png";
				if (!filePath.toLowerCase().endsWith(".png")) {
					filePath += ".png";
					file = new File(filePath);
				}
			}
			if (gifFileFilter != null && gifFileFilter.equals(fileFilter)) {
				formatName = "gif";
				if (!filePath.toLowerCase().endsWith(".gif")) {
					filePath += ".gif";
					file = new File(filePath);
				}
			}
			boolean saveImage = false;
			if (file.exists()) {
				String msg = "The file " + file.toString() + " already exists.\nDo you want to overwrite this existing file?";
				int opt = JOptionPane.showConfirmDialog(ApplicationAccessor.getParentFrame(), msg,
						"Export Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				saveImage = (opt == JOptionPane.YES_OPTION);
			} else {
				try {
					file.createNewFile();
					saveImage = true;
				} catch (IOException e) {
					e.printStackTrace();
					String msg = "There was a problem creating " + file.toString() + ". The image has not been exported.";
					JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(), msg, "Export Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			if (saveImage) {
				saveImage(file, formatName);
			}
		}
	}

	private void saveImage(File file, String formatName) {
		if (hasProject()) {
			try {
				ShrimpTool tool = getTool(toolName);
				DisplayBean displayBean = (DisplayBean) tool.getBean(ShrimpTool.DISPLAY_BEAN);
				if (displayBean instanceof PNestedDisplayBean) {
					PCanvas canvas = ((PNestedDisplayBean) displayBean).getPCanvas();

					boolean saved = false;
					// if the parent is a viewport then save the entire canvas, not just the visible part
					if (canvas.getParent() instanceof JViewport) {
						saved = saveViewport(file, formatName, canvas, (JViewport)canvas.getParent());
					} else {
						// save only the visible region of the canvas
						saved = paintComponent(canvas, file, formatName);
					}
					if (saved) {
						JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
							file.toString() + " has been saved successfully.");
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
					"There was a problem saving " + file.toString() +
					". The image has not been exported.\nReason: " + e.getMessage(),
					"Export Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Prompts the user to save the entire canvas or just the visible region.
	 * If the entire canvas is selected then the canvas is removed from the viewport and resized, then
	 * painted into the image and returned to the viewport.
	 * @return true if the image is saved
	 */
	private boolean saveViewport(File file, String formatName, PCanvas canvas, JViewport viewport) {
		boolean saved = false;

		Dimension fullSize = viewport.getViewSize();

		// ask the user if they want an image of the whole canvas or just the visible area
		// check if the canvas is smaller than the viewport view size
		if ((fullSize.width > canvas.getWidth()) || (fullSize.height > canvas.getHeight())) {
			String msg = "Do you want to save the visible region of the canvas " + dimToString(canvas.getSize()) +
					" or the entire canvas " + dimToString(fullSize) + "?\n" +
					"Warning: saving the entire canvas can cause an out of memory error if it is too large.";
			String[] options = new String[] { " Just The Visible Region ",
										" The Entire Canvas ", " Cancel " };
			int choice = JOptionPane.showOptionDialog(viewport,
					msg, "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[0]);

			if (choice == JOptionPane.CANCEL_OPTION) {
				// do nothing, false will be returned
			} else if (choice == JOptionPane.YES_OPTION) {
				// just save the visible region of the canvas
				saved = paintComponent(canvas, file, formatName);
			} else if (choice == JOptionPane.NO_OPTION) {
				Point viewPosition = viewport.getViewPosition();
				Rectangle oldBounds = canvas.getBounds();
				
				// save the entire canvas, need to start in the top left corner
				viewport.setViewPosition(new Point(0, 0));

				JPanel fullPanel = new JPanel(new BorderLayout());
				fullPanel.setBounds(0, 0, fullSize.width, fullSize.height);
				fullPanel.add(canvas, BorderLayout.CENTER);
				// have to make the canvas the full size so that everything is painted
				canvas.setBounds(0, 0, fullSize.width, fullSize.height);

				// we have to set a flag on the arcs to signal that we want to paint all arcs,
				// even if they aren't showing on the canvas
				PShrimpArc.PRINTING_ENTIRE_CANVAS = true;
				
				saved = paintComponent(canvas, file, formatName);

				PShrimpArc.PRINTING_ENTIRE_CANVAS = false;

				// restore the canvas as the original view with original position and bounds
				viewport.setView(canvas);
				viewport.setViewPosition(viewPosition);
				canvas.setBounds(oldBounds);
			}
		} else {
			// just save the visible region of the canvas
			saved = paintComponent(canvas, file, formatName);
		}
		return saved;
	}

	private boolean paintComponent(JComponent comp, File file, String formatName) {
		try {
			BufferedImage bufferedImage = new BufferedImage(comp.getWidth(), comp.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.getGraphics();
			
			comp.paint(g); // paint the canvas to the image
			ImageIO.write(bufferedImage, formatName, file);
		} catch (Throwable t) {
			// sometimes an out of memory error will occur!
			JOptionPane.showMessageDialog(ApplicationAccessor.getParentFrame(),
					"There was a problem saving " + file.toString() +
					". The image has not been exported.\nReason: " + t.getMessage(),
					"Export Error", JOptionPane.ERROR_MESSAGE);
			t.printStackTrace();
			return false;
		}
		return true;
	}

	private static String dimToString(Dimension d) {
		return "(" + d.width + "x" + d.height + ")";
	}

}
