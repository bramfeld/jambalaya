/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import ca.uvic.csr.shrimp.DataBean.Relationship;


/**
 * @author Jeff Michaud
 * @date Nov 18, 2002
 */
public interface RelationshipChangeListener {

	public void changeRelationship (RelationshipChangeEvent rce);

	/**
	 * @author Jeff Michaud
	 */
	public class RelationshipChangeEvent {

		private Relationship changedRelationship;

		public RelationshipChangeEvent (Relationship changedRelationship) {
			this.changedRelationship = changedRelationship;
		}

		public Relationship getChangedRelationship() {
			return changedRelationship;
		}

	}

}
