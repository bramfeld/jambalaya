/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import ca.uvic.csr.shrimp.DataBean.Artifact;


/**
 * @author Jeff Michaud
 * @date Nov 18, 2002
 */
public interface ArtifactAddListener {

	public void addArtifact(ArtifactAddEvent aae);

	public class ArtifactAddEvent {

		private Artifact addedArtifact;

		public ArtifactAddEvent (Artifact addedArtifact) {
			this.addedArtifact = addedArtifact;
		}

		public Artifact getAddedArtifact() {
			return addedArtifact;
		}

	}
}
