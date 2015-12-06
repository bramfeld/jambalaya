/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import ca.uvic.csr.shrimp.DataBean.Relationship;


/**
 * @author Jeff Michaud
 * @date November 19, 2002
 */
public interface RelationshipRemoveListener {

	public void removeRelationship(RelationshipRemoveEvent rre);

	/**
	 * @author Jeff Michaud
	 */
	public class RelationshipRemoveEvent {

		private Relationship relationshipRemoved;

		public RelationshipRemoveEvent(Relationship relationshipRemoved){
			this.relationshipRemoved = relationshipRemoved;
		}

		public Relationship getRelationshipsRemoved() {
			return this.relationshipRemoved;
		}

	}
}
