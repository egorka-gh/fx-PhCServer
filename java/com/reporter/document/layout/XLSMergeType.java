package com.reporter.document.layout;

/**
 * @author egorka
 * merge type enum
 */
public enum XLSMergeType{
	
	XLSMergeNone((short) 0),
	XLSMergeStatic((short) 1),
	XLSMergeCurrent((short) 2),
	XLSMergeSection((short) 3);
    
    private XLSMergeType(short index){
        this.index = index;
    }
    private short index;
	public short getIndex() {
		return index;
	}
}
