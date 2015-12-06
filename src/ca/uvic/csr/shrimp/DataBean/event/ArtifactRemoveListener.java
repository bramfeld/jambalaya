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
public interface ArtifactRemoveListener {

	public void removeArtifact(ArtifactRemoveEvent rae);


	public class ArtifactRemoveEvent {

		private Artifact removedArtifact;

		public ArtifactRemoveEvent(Artifact removedArtifact) {
			this.removedArtifact = removedArtifact;
		}

		public Artifact getRemovedArtifact() {
			return removedArtifact;
		}

	}
}
