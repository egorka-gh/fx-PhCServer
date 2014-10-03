/**
 * 
 */
package com.reporter.document.layout;

/**
 * @author Administrator
 *
 */
public enum XlsLayoutDirection {
    NoneDirection("None"),
    RowDirection("Row"),
    ColumnDirection("Column"),
    AnyDirection("RowColumn"), //frame
    SheetDirection("Sheet");
    
    /**
     * direction name
     */
    private String directionName;
    
    /**
     * copnstructor
     * 
     * @param directionName
     */
    private XlsLayoutDirection(String directionName){
        
    }

    /**
     * get direction name
     * 
     * @return the directionName
     */
    public String getDirectionName() {
        return directionName;
    }

    /**
     * set new direction name
     * 
     * @param directionName the directionName to set
     */
    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

}
