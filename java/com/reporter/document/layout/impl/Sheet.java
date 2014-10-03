/**
 * 
 */
package com.reporter.document.layout.impl;

import com.reporter.document.layout.XLSCommands;
import com.reporter.document.layout.XlsLayoutDirection;

/**
 * @author Igor Zhadenov
 *
 */
public class Sheet extends GenericDinamicLayout {
    /**
     * xls sheet name 
     */
    private String sheetName;

    /**
     * resultset field used to set sheet name for bound sheet   
     */
    private String captionField;

    /**
     * Constructor
     */
    public Sheet() {
        super();
        setCommand(XLSCommands.XLSCommandSheet);
        setDirection(XlsLayoutDirection.SheetDirection);
        setRegionName("");
    }

    /**
     * Constructor
     * 
     * @param Name
     */
    public Sheet(String Name) {
        super();
        setCommand(XLSCommands.XLSCommandSheet);
        setDirection(XlsLayoutDirection.SheetDirection);
        setSheetName(Name);
        setRegionName("");
    }

    /**
     * @return xls sheet name
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * @param sheetName sheet Name to set
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * @return the captionField
     */
    public String getCaptionField() {
        return captionField;
    }

    /**
     * @param captionField the captionField to set
     */
    public void setCaptionField(String captionField) {
        this.captionField = captionField;
    }
}
