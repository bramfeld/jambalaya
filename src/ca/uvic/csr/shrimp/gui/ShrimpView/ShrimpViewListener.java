/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.gui.ShrimpView;

/**
 * @author Rob Lintern
 */
public interface ShrimpViewListener {

	public void shrimpViewCprelsChanged(ShrimpViewCprelsChangedEvent event);

	public void shrimpViewMouseModeChanged(ShrimpViewMouseModeChangedEvent event);

}
