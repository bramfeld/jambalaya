/**
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */

package ca.uvic.csr.shrimp.SearchBean;

import java.util.EventListener;


public interface SearchRequestListener extends EventListener {
    public void requestMade(SearchRequestEvent e);
}//interface SeachRequestListener
