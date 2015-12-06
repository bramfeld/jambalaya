/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

import java.awt.Container;

import ca.uvic.csr.shrimp.ShrimpProject.ShrimpProject;

/**
 * This interface defines a factory for creating a Shrimp View.
 *
 * @author Rob Lintern
 */
public interface ShrimpViewFactory {

	/**
	 * Creates the {@link ShrimpView} for the given project and container.
	 * Uses default {@link ShrimpViewConfiguration}.
	 * @param project The project to be displayed in the Shrimp View
	 * @param shrimpViewContainer The GUI container to place the Shrimp View into.
	 */
	public ShrimpView createShrimpView(final ShrimpProject project, Container shrimpViewContainer);

	/**
	 * Creates the {@link ShrimpView} for the given project, container and configuration.
	 * @param project The project to be displayed in the Shrimp View
	 * @param shrimpViewContainer The GUI container to place the Shrimp View into.
	 * @param config the {@link ShrimpView} configuration
	 * @return The newly created Shrimp View.
	 */
	public abstract ShrimpView createShrimpView(final ShrimpProject project,
			Container shrimpViewContainer, ShrimpViewConfiguration config);
}