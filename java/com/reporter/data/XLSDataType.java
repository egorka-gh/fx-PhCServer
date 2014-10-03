package com.reporter.data;

/**
 * @author egorka
 * reporter data type enum
 */
public enum  XLSDataType {

    XLSDataAutoDetect((short) -1),
    XLSDataNumeric((short) 0),
    XLSDataText((short) 1),
    XLSDataDate((short) 2),
    XLSDataFormula((short) 3);
    
    private short index;
    
    private XLSDataType(short index){
        this.index = index;
    }
    public short getIndex() {
        return index;
    }
}