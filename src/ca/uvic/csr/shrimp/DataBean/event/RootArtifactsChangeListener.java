/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import java.util.Set;


/**
 * @author Chris Callendar
 * @date Apr 30, 2007
 */
public interface RootArtifactsChangeListener {

	public void rootArtifactsChange(RootArtifactsChangeEvent race);


	public class RootArtifactsChangeEvent {

		private Set/*<Artifact>*/ rootArtifacts;

		public RootArtifactsChangeEvent(Set/*<Artifact>*/ rootArtifacts) {
			this.rootArtifacts = rootArtifacts;
		}

		public Set/*<Artifact>*/ getRootArtifacts() {
			return rootArtifacts;
		}
	}
}
