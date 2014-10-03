package com.reporter.data;

import org.apache.poi.hssf.usermodel.HSSFCell;

public class XLSCellValue {
    private Object value;
    private XLSDataType valueType = XLSDataType.XLSDataAutoDetect;
    
    /**
     * @param value
     * @param valueType
     */
    public XLSCellValue(Object value, XLSDataType type) {
        super();
        this.value = value;
        this.valueType = type;
    }

    /**
     * @param string value
     */
    public XLSCellValue(String value) {
        super();
        this.value = value;
        this.valueType = XLSDataType.XLSDataAutoDetect ;
    }

    /**
     * @param HSSFCell source cell
     */
    public XLSCellValue(HSSFCell cell) {
        super();
        String val = "";
        if ((cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) || (cell.getCellType() == HSSFCell.CELL_TYPE_ERROR)){
            val = "";
        } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC){
            val = String.valueOf(cell.getNumericCellValue());
        } else if(cell.getCellType() == HSSFCell.CELL_TYPE_BOOLEAN){
            val = String.valueOf(cell.getBooleanCellValue());
        } else { 
            val =cell.getRichStringCellValue().toString();
        }
        this.value = val;
        this.valueType = XLSDataType.XLSDataAutoDetect;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return value.toString();
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return the valueType
     */
    public XLSDataType getType() {
        return valueType;
    }

    /**
     * @param valueType the valueType to set
     */
    public void setType(XLSDataType type) {
        this.valueType = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return value.equals(arg0);
    }

    public boolean isEpmpty(){
        return (value == null) || (value.toString().length() == 0);
    }
}
