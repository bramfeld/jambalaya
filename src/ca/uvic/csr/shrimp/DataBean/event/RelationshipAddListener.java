/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

import ca.uvic.csr.shrimp.DataBean.Relationship;


/**
 * This defines the interface needed to listen for the relationship add event
 *
 * @author Jeff Michaud
 * @date November 19, 2002
 */
public interface RelationshipAddListener {

	public void addRelationship(RelationshipAddEvent rre);

	public class RelationshipAddEvent {

		private Relationship relationshipAdded;

		public RelationshipAddEvent(Relationship relationshipAdded){
			this.relationshipAdded = relationshipAdded;
		}

		public Relationship getRelationshipAdded(){
			return this.relationshipAdded;
		}
	}
}
