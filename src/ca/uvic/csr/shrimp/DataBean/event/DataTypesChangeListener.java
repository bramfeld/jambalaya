/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;


/**
 * @author Rob Lintern
 * @date Feb 20, 2002
 */
public interface DataTypesChangeListener {

	public void dataTypesChange(DataTypesChangeEvent event);

	/**
	 * Nothing here yet! This event is fired when there are changes
	 * to the types (artifact and relationship) in the databean.
	 * @author Rob Lintern
	 * @date Feb 20, 2002.
	 */
	public class DataTypesChangeEvent {
	    // nothing yet
	}

}
