/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DataBean.event;

/**
 * 
 * @author Rob Lintern
 *
 * Handles a request to check if an object should be filtered from the data.
 */
public interface DataFilterRequestListener {
    public boolean isFiltered(Object obj, String targetFilterType);
}
