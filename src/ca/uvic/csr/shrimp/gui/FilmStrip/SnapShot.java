/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.FilmStrip;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;
import ca.uvic.csr.shrimp.ShrimpTool.ViewTool;

/**
 * This is one snap shot of something.  A snapshot represents the state of the program when
 * the snapshot was taken.  For example, when a shrimp snapshot is taken, the state of the nodes,
 * arcs, filters, etc is stored in the snapshot so the state can be restored.
 *
 * @author Casey Best
 * @date Oct 3, 2001
 */
public interface SnapShot {
	
	public final static int IMAGE_HEIGHT = 100;
	public final static int IMAGE_WIDTH = 100;

	/**
	 * Returns the comment associated with this snapshot
	 */
	public String getComment();

	/**
	 * Sets the comment associated with this snapshot
	 */
	public void setComment(String comment);
	
	/**
	 * Presents the comment to the user for modifications
	 */
	public boolean promptUserToChangeComment(Frame parentOfDialog);

	/**
	 * Change the comment on this snapshot to the given string
	 *
	 * @param newComment The comment to put on this snapshot.  The old comment is erased.
	 */
	public boolean changeComment(String newComment);

	/**
	 * Returns a small image which is a preview of this snapshot.
	 */
	public Image getPreviewShot();

	/**
	 * Returns true if this snapshot has changed.
	 */
	public boolean hasChanged();

	/**
	 * Returns the size of the preview shot
	 */
	public Dimension getPreviewShotSize();

	/**
	 * Reverts the view back to this snapshot.  For example, the display in shrimp will revert to the
	 * same layout as it was in when this snapshot was taken.
	 * 
	 * @param view The view to make look like this snapshot
	 */
	public void revertViewToSnapShotState(ShrimpProject project, ViewTool view);

	/**
	 * Saves this snapShot to the given filename
	 */
	public void save(String fileName) throws IOException;

	/**
	 * Saves this snapShot to the given output stream
	 */
	public void save(FileOutputStream outputStream) throws IOException;

	/**
	 * Cleans up any resources this snap shot was using.  You can
	 * assume that this snap shot won't be used once this method
	 * is called.
	 */
	public void cleanUp();
}