/*
 * Copyright 1998-2008, CHISEL Group, University of Victoria, Victoria, BC, Canada.
 * All rights reserved.
 */
package ca.uvic.csr.shrimp.DisplayBean.event;

/**
 * Event thrown before and after magnifying from one object to another.
 * 
 * @author Rob Lintern
 */
public class MagnifyEvent {
    private Object fromObject;
    private Object toObject;
    
    public MagnifyEvent (Object fromObject, Object toObject) {
        this.fromObject = fromObject;
        this.toObject = toObject;
    }
    
    public Object getFromObject() {
        return fromObject;
    }
    
    public Object getToObject() {
        return toObject;
    }
}
